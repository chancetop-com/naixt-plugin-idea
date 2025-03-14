package com.chancetop.naixt.plugin.idea.windows.inernal;

import com.chancetop.naixt.plugin.idea.agent.AgentServerService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.components.JBScrollPane;
import core.framework.util.Strings;

import javax.swing.*;

/**
 * @author stephen
 */
public class NaixtToolWindowContext {
    private final Project project;
    private final ToolWindow toolWindow;
    private final JPanel mainPanel;
    private final JPanel conversationPanel;
    private final JBScrollPane conversationScrollPane;
    private final String workspaceBasePath;
    private final AgentServerService agentServerService;
    private TextAreaCallback callback;

    public NaixtToolWindowContext(Project project,
                                  ToolWindow toolWindow,
                                  JPanel mainPanel,
                                  JPanel conversationPanel,
                                  JBScrollPane conversationScrollPane,
                                  String workspaceBasePath,
                                  AgentServerService agentServerService) {
        this.project = project;
        this.toolWindow = toolWindow;
        this.mainPanel = mainPanel;
        this.conversationPanel = conversationPanel;
        this.conversationScrollPane = conversationScrollPane;
        this.workspaceBasePath = workspaceBasePath;
        this.agentServerService = agentServerService;
    }

    public void setCallback(TextAreaCallback callback) {
        this.callback = callback;
    }

    public Project project() {
        return project;
    }

    public ToolWindow toolWindow() {
        return toolWindow;
    }

    public JPanel mainPanel() {
        return mainPanel;
    }

    public JPanel conversationPanel() {
        return conversationPanel;
    }

    public JBScrollPane conversationScrollPane() {
        return conversationScrollPane;
    }

    public String workspaceBasePath() {
        return workspaceBasePath;
    }

    public TextAreaCallback callback() {
        return callback;
    }

    public AgentServerService agentServerService() {
        return agentServerService;
    }
}
