package com.chancetop.naixt.agent;

import com.chancetop.naixt.agent.internal.ChatRequest;
import com.chancetop.naixt.agent.internal.ChatResponse;
import com.chancetop.naixt.ide.IdeUtils;
import com.chancetop.naixt.settings.NaixtSettingStateService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.io.IOException;

/**
 * @author stephen
 */
@Service
public final class AgentServerService {
    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new GsonBuilder().create();

    public static AgentServerService getInstance() {
        return ApplicationManager.getApplication().getService(AgentServerService.class);
    }

    private String toEndpoint(String path) {
        return "http://localhost:59527" + path;
    }

    public void stopServer() {
        var request = new Request.Builder()
                .method("PUT", RequestBody.create(new byte[0], MediaType.get("application/json")))
                .url(toEndpoint("/_app/stop-service"))
                .build();

        try (var response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new RuntimeException("Unexpected code " + response);
            }

            if (!response.message().equalsIgnoreCase("Ok")) {
                throw new RuntimeException("Failed to stop service");
            }
        } catch (IOException e) {
            if (!e.getMessage().contains("Failed to connect")) {
                throw new RuntimeException("Failed to send request", e);
            }
        }
    }

    public String send(String text, Project project) {
        var request = new Request.Builder()
                .method("PUT", RequestBody.create(createChatRequest(text, project), MediaType.get("application/json")))
                .url(toEndpoint("/naixt/chat"))
                .build();

        try (var response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new RuntimeException("Unexpected code " + response);
            }

            if (response.body() == null) {
                throw new RuntimeException("Empty response body");
            }
            return gson.fromJson(response.body().string(), ChatResponse.class).text;
        } catch (IOException e) {
            if (e.getMessage().contains("Failed to connect")) {
                Messages.showMessageDialog(project, "Agent server is not started!", "Warning", Messages.getWarningIcon());
            }
            throw new RuntimeException("Failed to send request", e);
        }
    }

    private String createChatRequest(String text, Project project) {
        var state = ApplicationManager.getApplication().getService(NaixtSettingStateService.class).getState();
        return gson.toJson(ChatRequest.of(
                text,
                IdeUtils.getCurrentFilePath(project),
                IdeUtils.getCurrentPosition(project).line(),
                IdeUtils.getCurrentPosition(project).column(),
                state == null ? "" : state.getLlmProviderModel()));
    }
}
