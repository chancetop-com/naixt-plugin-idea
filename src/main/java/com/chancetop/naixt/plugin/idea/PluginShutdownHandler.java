package com.chancetop.naixt.plugin.idea;

import com.chancetop.naixt.plugin.idea.server.AgentServiceManagementService;
import com.intellij.ide.AppLifecycleListener;
import com.intellij.openapi.application.ApplicationManager;

import java.io.IOException;

/**
 * @author stephen
 */
public class PluginShutdownHandler implements AppLifecycleListener {
    public PluginShutdownHandler() {
        ApplicationManager.getApplication().getMessageBus().connect().subscribe(AppLifecycleListener.TOPIC, this);
    }

    @Override
    public void appWillBeClosed(boolean isRestart) {
        try {
            ApplicationManager.getApplication().getService(AgentServiceManagementService.class).stop();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
