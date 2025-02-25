package com.chancetop.naixt.plugin.idea;

import com.chancetop.naixt.plugin.idea.server.AgentServiceManagementService;
import com.intellij.ide.AppLifecycleListener;
import com.intellij.openapi.application.ApplicationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author stephen
 */
public class PluginShutdownHandler implements AppLifecycleListener {
    private final Logger logger = LoggerFactory.getLogger(PluginShutdownHandler.class);
    public PluginShutdownHandler() {
        ApplicationManager.getApplication().getMessageBus().connect().subscribe(AppLifecycleListener.TOPIC, this);
    }

    @Override
    public void appWillBeClosed(boolean isRestart) {
        try {
            ApplicationManager.getApplication().getService(AgentServiceManagementService.class).stop();
        } catch (IOException e) {
            logger.error("failed to stop agent service", e);
        }
    }
}
