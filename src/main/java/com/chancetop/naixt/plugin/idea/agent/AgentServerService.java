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
import core.framework.http.HTTPMethod;
import core.framework.http.HTTPRequest;
import core.framework.internal.bean.BeanClassValidator;
import core.framework.internal.web.bean.RequestBeanWriter;
import core.framework.internal.web.bean.ResponseBeanReader;
import core.framework.internal.web.service.WebServiceClient;
import core.framework.internal.web.service.WebServiceClientBuilder;
import core.framework.internal.web.service.WebServiceInterfaceValidator;
import core.framework.json.JSON;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.function.Consumer;

/**
 * @author stephen
 */
@Service
public final class AgentServerService {
    private final Logger logger = org.slf4j.LoggerFactory.getLogger(AgentServerService.class);
    private final String endpoint = "http://localhost:59527";
    private final NaixtWebService naixtWebService;
    private final NaixtAgentWebService naixtAgentWebService;
    private final BeanClassValidator beanClassValidator = new BeanClassValidator();
    private final HTTPClient client = HTTPClient.builder().connectTimeout(Duration.ofMillis(100)).timeout(Duration.ofSeconds(180)).build();

    public  <T> T createClient(Class<T> t, String endpoint) {
        logger.info("create web service client, interface={}, serviceURL={}", t.getCanonicalName(), endpoint);
        var writer = new RequestBeanWriter();
        var reader = new ResponseBeanReader();
        var validator = new WebServiceInterfaceValidator(t, beanClassValidator);
        validator.requestBeanWriter = writer;
        validator.responseBeanReader = reader;
        validator.validate();
        return new WebServiceClientBuilder<>(t, new WebServiceClient(endpoint, client, writer, reader)).build();
    }

    public AgentServerService() {
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

    public ChatResponse chat(String text, IdeCurrentInfo info) {
        return naixtAgentWebService.chat(buildChatRequest(text, info));
    }

    public void chatSse(String text, IdeCurrentInfo info, Consumer<ChatResponse> consumer) {
        var request = new HTTPRequest(HTTPMethod.PUT, endpoint + "/naixt/agent/chat-sse");
        request.body = JSON.toJSON(buildChatRequest(text, info)).getBytes();
        try (var response = client.sse(request)) {
            for (var event : response) {
                var chatResponse = JSON.fromJSON(ChatResponse.class, event.data());
                consumer.accept(chatResponse);
                if (!chatResponse.fileContents.isEmpty()) {
                    response.close();
                }
            }
        } catch (Exception e) {
            if (e instanceof IllegalStateException && e.getMessage().contains("closed")) {
                return;
            }
            logger.error("SSE request failed", e);
            consumer.accept(ChatResponse.of("Error: " + e.getMessage()));
        }
    }

    private NaixtChatRequest buildChatRequest(String text, IdeCurrentInfo info) {
        var state = NaixtSettingStateService.getInstance().getState();
        var request = new NaixtChatRequest();
        request.query = text;
        request.workspacePath = info.workspacePath();
        request.currentFilePath = info.currentFilePath();
        request.currentLineNumber = info.position().line();
        request.currentColumnNumber = info.position().column();
        request.model = state == null ? "" : state.getLlmProviderModel();
        request.planningModel = state == null ? "" : state.getPlanningModel();
        return request;
    }

    public void approve(ChatResponse msg, String workspacePath) {
        var req = new ApproveChangeRequest();
        req.workspacePath = workspacePath;
        req.fileContents = msg.fileContents;
        naixtAgentWebService.approved(req);
    }
}
