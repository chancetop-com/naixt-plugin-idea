package com.chancetop.naixt.plugin.idea.agent;

import com.chancetop.naixt.agent.api.NaixtAgentWebService;
import com.chancetop.naixt.agent.api.NaixtWebService;
import com.chancetop.naixt.agent.api.naixt.ApproveChangeRequest;
import com.chancetop.naixt.agent.api.naixt.ChatResponse;
import com.chancetop.naixt.agent.api.naixt.NaixtChatRequest;
import com.chancetop.naixt.plugin.idea.ide.internal.IdeCurrentInfo;
import com.chancetop.naixt.plugin.idea.settings.NaixtSettingStateService;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import core.framework.http.HTTPClient;
import core.framework.internal.bean.BeanClassValidator;
import core.framework.internal.web.bean.RequestBeanWriter;
import core.framework.internal.web.bean.ResponseBeanReader;
import core.framework.internal.web.service.WebServiceClient;
import core.framework.internal.web.service.WebServiceClientBuilder;
import core.framework.internal.web.service.WebServiceInterfaceValidator;
import org.slf4j.Logger;

import java.time.Duration;

/**
 * @author stephen
 */
@Service
public final class AgentServerService {
    private final Logger logger = org.slf4j.LoggerFactory.getLogger(AgentServerService.class);
    private final NaixtWebService naixtWebService;
    private final NaixtAgentWebService naixtAgentWebService;
    private final BeanClassValidator beanClassValidator = new BeanClassValidator();

    public  <T> T createClient(Class<T> t, String endpoint) {
        var client = HTTPClient.builder().timeout(Duration.ofSeconds(120)).build();
        var writer = new RequestBeanWriter();
        var reader = new ResponseBeanReader();
        logger.info("create web service client, interface={}, serviceURL={}", t.getCanonicalName(), endpoint);
        var validator = new WebServiceInterfaceValidator(t, beanClassValidator);
        validator.requestBeanWriter = writer;
        validator.responseBeanReader = reader;
        validator.validate();
        return new WebServiceClientBuilder<>(t, new WebServiceClient(endpoint, client, writer, reader)).build();
    }

    public AgentServerService() {
        var endpoint = "http://localhost:59527";
        // caution: this is a very tricky way to set the class loader, it should be used with caution
        // set the thread context class loader for the java assist library to load the class from the plugin classpath but the system classpath
        var systemClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
        naixtAgentWebService = createClient(NaixtAgentWebService.class, endpoint);
        naixtWebService = createClient(NaixtWebService.class, endpoint);
        Thread.currentThread().setContextClassLoader(systemClassLoader);
    }

    public static AgentServerService getInstance() {
        return ApplicationManager.getApplication().getService(AgentServerService.class);
    }

    public void stop() {
        try {
            naixtWebService.stop();
        } catch (Exception e) {
            logger.warn("failed to stop agent server", e);
        }
    }

    public void clearShortTermMemory() {
        try {
            naixtAgentWebService.clear();
        } catch (Exception e) {
            logger.warn("failed to clear memory", e);
        }
    }

    public ChatResponse send(String text, IdeCurrentInfo info) {
        var state = NaixtSettingStateService.getInstance().getState();
        var request = new NaixtChatRequest();
        request.query = text;
        request.workspacePath = info.workspacePath();
        request.currentFilePath = info.currentFilePath();
        request.currentLineNumber = info.position().line();
        request.currentColumnNumber = info.position().column();
        request.model = state == null ? "" : state.getLlmProviderModel();
        request.planningModel = state == null ? "" : state.getPlanningModel();
        return naixtAgentWebService.chat(request);
    }

    public void approve(ChatResponse msg, String workspacePath) {
        var req = new ApproveChangeRequest();
        req.workspacePath = workspacePath;
        req.fileContents = msg.fileContents;
        naixtAgentWebService.approved(req);
    }
}
