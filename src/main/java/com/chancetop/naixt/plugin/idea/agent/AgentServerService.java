package com.chancetop.naixt.plugin.idea.agent;

import com.chancetop.naixt.agent.api.NaixtAgentWebService;
import com.chancetop.naixt.agent.api.NaixtWebService;
import com.chancetop.naixt.agent.api.naixt.AgentApproveRequest;
import com.chancetop.naixt.agent.api.naixt.AgentChatResponse;
import com.chancetop.naixt.agent.api.naixt.AgentSuggestionRequest;
import com.chancetop.naixt.agent.api.naixt.CurrentEditInfoView;
import com.chancetop.naixt.agent.api.naixt.AgentChatRequest;
import com.chancetop.naixt.agent.api.naixt.NaixtPluginSettingsView;
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
import java.util.List;
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

    public AgentChatResponse chat(String text, IdeCurrentInfo info) {
        return naixtAgentWebService.chat(buildChatRequest(text, info));
    }

    public void chatSse(String text, IdeCurrentInfo info, Consumer<ChatResult> consumer) {
        var request = new HTTPRequest(HTTPMethod.PUT, endpoint + "/naixt/agent/chat-sse");
        request.body = JSON.toJSON(buildChatRequest(text, info)).getBytes();
        try (var response = client.sse(request)) {
            for (var event : response) {
                var rsp = JSON.fromJSON(AgentChatResponse.class, event.data());
                consumer.accept(new ChatResult(true, rsp));
                if (!rsp.fileContents.isEmpty()) {
                    response.close();
                }
            }
        } catch (Exception e) {
            if (e instanceof IllegalStateException && e.getMessage().contains("closed")) {
                consumer.accept(new ChatResult(true, AgentChatResponse.of("finished", true)));
                return;
            }
            logger.error("SSE request failed", e);
            consumer.accept(new ChatResult(false, AgentChatResponse.of("Chat to Agent failed, please make sure you started the Agent Server, Error: " + e.getMessage(), true)));
        }
    }

    private AgentChatRequest buildChatRequest(String text, IdeCurrentInfo info) {
        var request = new AgentChatRequest();
        request.query = text;
        request.settings = buildSettings();
        request.editInfo = buildEditInfo(info);
        return request;
    }

    private CurrentEditInfoView buildEditInfo(IdeCurrentInfo info) {
        var editInfo = new CurrentEditInfoView();
        editInfo.workspacePath = info.workspacePath();
        editInfo.currentFilePath = info.currentFilePath();
        editInfo.currentLineNumber = info.position().line();
        editInfo.currentColumnNumber = info.position().column();
        editInfo.currentFileDiagnostic = info.currentFileDiagnostic();
        return editInfo;
    }

    private NaixtPluginSettingsView buildSettings() {
        var state = NaixtSettingStateService.getInstance().getState();
        var settings = new NaixtPluginSettingsView();
        settings.model = state == null ? "" : state.getLlmProviderModel();
        settings.planningModel = state == null ? "" : state.getPlanningModel();
        return settings;
    }

    public void approve(AgentChatResponse msg, String workspacePath) {
        var req = new AgentApproveRequest();
        req.workspacePath = workspacePath;
        req.fileContents = msg.fileContents;
        naixtAgentWebService.approved(req);
    }

    public List<String> suggestion(IdeCurrentInfo info) {
        var request = new AgentSuggestionRequest();
        request.settings = buildSettings();
        request.editInfo = buildEditInfo(info);
        var rsp = naixtAgentWebService.suggestion(request);
        return rsp.suggestions;
    }
}
