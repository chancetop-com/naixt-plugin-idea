package com.chancetop.naixt.plugin.idea.server;

import com.chancetop.naixt.plugin.idea.agent.AgentServerService;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

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

    public AgentStartResult start(String url) {
        if (isRunning) {
            return new AgentStartResult(true, true, "Agent server is running already");
        }
        try {
            process = AgentServiceStarter.start(url);
            isRunning = true;
            return new AgentStartResult(true, false, "Start agent server success!");
        } catch (Exception e) {
            return new AgentStartResult(false, false, "Start agent server failed!" + e.getMessage());
        }
    }

    public void stop() {
        AgentServerService.getInstance().stop();
    }

    public void stop(Project project) {
        stop();
        if (!isRunning || process == null) {
            Messages.showMessageDialog(project, "Agent server is not running", "Warning", Messages.getWarningIcon());
            return;
        }
        process.destroyForcibly();
        isRunning = false;
        Messages.showMessageDialog(project, "Stop agent server success!", "Success", Messages.getInformationIcon());
    }
}