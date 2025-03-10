package com.chancetop.naixt.plugin.idea.windows.inernal;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.components.JBScrollPane;

import javax.swing.*;

/**
 * @author stephen
 */
public record NaixtToolWindowContext(Project project,
                                     ToolWindow toolWindow,
                                     JPanel mainPanel,
                                     JPanel conversationPanel,
                                     JBScrollPane conversationScrollPane,
                                     String workspaceBasePath) {
}
