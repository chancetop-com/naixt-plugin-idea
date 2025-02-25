package com.chancetop.naixt.plugin.idea.agent;

import com.chancetop.naixt.agent.api.NaixtAgentWebService;
import com.chancetop.naixt.agent.api.NaixtWebService;
import com.chancetop.naixt.agent.api.naixt.ChatResponse;
import com.chancetop.naixt.agent.api.naixt.NaixtChatRequest;
import com.chancetop.naixt.plugin.idea.agent.internal.ApiClient;
import com.chancetop.naixt.plugin.idea.ide.IdeUtils;
import com.chancetop.naixt.plugin.idea.settings.NaixtSettingStateService;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;

/**
 * @author stephen
 */
@Service
public final class AgentServerService {
    private final NaixtWebService naixtWebService;
    private final NaixtAgentWebService naixtAgentWebService;

    public AgentServerService() {
        var endpoint = "http://localhost:59527";
        var apiClient = new ApiClient();
        naixtAgentWebService = apiClient.createClient(NaixtAgentWebService.class, endpoint);
        naixtWebService = apiClient.createClient(NaixtWebService.class, endpoint);
    }

    public static AgentServerService getInstance() {
        return ApplicationManager.getApplication().getService(AgentServerService.class);
    }

    public void stop() {
        naixtWebService.stop();
    }

    public void clearShortTermMemory() {
        naixtAgentWebService.clear();
    }

    public ChatResponse send(String text, Project project) {
        var state = ApplicationManager.getApplication().getService(NaixtSettingStateService.class).getState();
        var request = new NaixtChatRequest();
        request.query = text;
        request.workspacePath = IdeUtils.getProjectPath(project);
        request.currentFilePath = IdeUtils.getCurrentFilePath(project);
        request.currentLineNumber = IdeUtils.getCurrentPosition(project).line();
        request.currentColumnNumber = IdeUtils.getCurrentPosition(project).column();
        request.model = state == null ? "" : state.getLlmProviderModel();
        return naixtAgentWebService.chat(request);
    }
}
