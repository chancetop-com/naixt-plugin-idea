package com.chancetop.naixt.plugin.idea.windows;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

/**
 * @author stephen
 */
public class OpenChangelistToolWindowFactory implements ToolWindowFactory {
    @Override
    public void init(ToolWindow window) {
        window.setStripeTitle("Naixt Changelist");
        window.setTitle("Code Changes Review");
    }

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        var panel = ApprovePanel.createChangelistPanel(project, null, null);
        var content = ContentFactory.getInstance().createContent(panel, "", false);
        toolWindow.getContentManager().addContent(content);
    }
}
