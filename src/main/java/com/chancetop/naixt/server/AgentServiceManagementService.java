package com.chancetop.naixt.server;

import com.chancetop.naixt.agent.AgentServerService;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

import java.io.IOException;

/**
 * @author stephen
 */
@Service
public final class AgentServiceManagementService {

    private static Process process;
    private static boolean isRunning = false;

    public static AgentServiceManagementService getInstance() {
        return ApplicationManager.getApplication().getService(AgentServiceManagementService.class);
    }

    public void start(String url, Project project) {
        if (isRunning) {
            Messages.showMessageDialog(project, "Agent server is running already", "Warning", Messages.getWarningIcon());
            return;
        }
        try {
            process = AgentServiceStarter.start(url);
            isRunning = true;
            Messages.showMessageDialog(project, "Start agent server success!", "Success", Messages.getInformationIcon());
        } catch (Exception e) {
            Messages.showMessageDialog(project, "Start agent server failed!" + e.getMessage(), "Warning", Messages.getErrorIcon());
            throw new RuntimeException("Failed to start service", e);
        }
    }

    public void stop() throws IOException {
        AgentServerService.getInstance().stopServer();
    }

    public void stop(Project project) {
        try {
            stop();
        } catch (IOException e) {
            if (!e.getMessage().contains("Failed to connect")) {
                Messages.showMessageDialog(project, "Stop agent server failed!" + e.getMessage(), "Warning", Messages.getErrorIcon());
                throw new RuntimeException("Failed to stop service", e);
            }
        }
        if (!isRunning || process == null) {
            Messages.showMessageDialog(project, "Agent server is not running", "Warning", Messages.getWarningIcon());
            return;
        }
        process.destroyForcibly();
        isRunning = false;
        Messages.showMessageDialog(project, "Stop agent server success!", "Success", Messages.getInformationIcon());
    }
}