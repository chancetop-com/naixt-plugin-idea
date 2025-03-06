package com.chancetop.naixt.plugin.idea.windows;

import com.chancetop.naixt.agent.api.naixt.ChatResponse;
import com.chancetop.naixt.plugin.idea.ide.IdeUtils;
import com.chancetop.naixt.plugin.idea.windows.inernal.FileChangeItem;
import com.intellij.diff.DiffContentFactory;
import com.intellij.diff.DiffManager;
import com.intellij.diff.requests.SimpleDiffRequest;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.wm.ToolWindowContentUiType;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.ContentFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author stephen
 *
 * ApprovePanel handles the display and interaction of file changes within the Naixt Changelist tool window.
 * It allows users to view diffs and apply changes, maintaining an updated list of file changes.
 */
public class ApprovePanel {

    /**
     * Displays the Approve Panel within the Naixt Changelist tool window.
     * @param project The current project.
     * @param msg The ChatResponse containing file change information.
     * @param afterApproved The runnable to execute after approval.
     */
    public static void showApprovePanel(Project project, ChatResponse msg, Runnable afterApproved) {
        try {
            var window = ToolWindowManager.getInstance(project).getToolWindow("Naixt Changelist");
            var panel = createChangelistPanel(project, msg, afterApproved);
            var content = ContentFactory.getInstance().createContent(panel, "", false);
            content.setDisposer(new DisposeParentOnClose(panel));

            assert window != null;
            window.getContentManager().removeAllContents(true);
            window.getContentManager().addContent(content);

            window.setDefaultContentUiType(ToolWindowContentUiType.COMBO);
            window.setAutoHide(false);
            window.activate(() -> {
                if (window.isVisible()) {
                    Component parent = panel.getParent();
                    if (parent != null) parent.revalidate();
                }
            });
        } catch (Exception e) {
            Messages.showErrorDialog(project, "Failed to setup changelist window: " + e.getMessage(), "ToolWindow Error");
        }
    }

    /**
     * Creates the changelist panel containing the list of file changes and the 'Apply Changes' button.
     * @param project The current project.
     * @param msg The ChatResponse containing file change information.
     * @param afterApproved The runnable to execute after approval.
     * @return A JPanel representing the changelist panel.
     */
    public static JPanel createChangelistPanel(Project project, ChatResponse msg, Runnable afterApproved) {
        var panel = new JPanel(new BorderLayout());
        if (msg == null || msg.fileContents.isEmpty()) msg = ChatResponse.of("No changes found");

        var fileListModel = new DefaultListModel<FileChangeItem>();
        msg.fileContents.forEach(fc -> {
            var fileName = fc.filePath.substring(fc.filePath.lastIndexOf("/") + 1);
            var item = new FileChangeItem(fc.action, fc.filePath, fileName, fc.content);
            fileListModel.addElement(item);
        });

        var fileList = new JBList<>(fileListModel);
        fileList.setCellRenderer(new FileChangeListRenderer(project));

        fileList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    showFileDiff(project, fileList.getSelectedValue());
                }
            }
        });

        if (!msg.fileContents.isEmpty()) {
            var buttonPanel = createButtonPanel(afterApproved, fileListModel, panel);
            panel.add(buttonPanel, BorderLayout.SOUTH);
        }
        panel.add(new JBScrollPane(fileList), BorderLayout.CENTER);
        return panel;
    }

    private static JPanel createButtonPanel(Runnable afterApproved, DefaultListModel<FileChangeItem> fileListModel, JPanel panel) {
        var buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        var apply = new JButton("Apply Changes");
        var cancel = new JButton("Cancel");
        apply.addActionListener(l -> {
            afterApproved.run();
            fileListModel.clear();
            panel.remove(buttonPanel);
            panel.revalidate();
            panel.repaint();
        });
        cancel.addActionListener(l -> {
            fileListModel.clear();
            panel.remove(buttonPanel);
            panel.revalidate();
            panel.repaint();
        });
        buttonPanel.add(apply);
        buttonPanel.add(cancel);
        return buttonPanel;
    }

    /**
     * Displays the file diff for the selected file change item.
     * @param project The current project.
     * @param item The selected file change item.
     */
    private static void showFileDiff(Project project, FileChangeItem item) {
        try {
            var file = LocalFileSystem.getInstance().findFileByPath(IdeUtils.toAbsolutePath(project, item.filePath()));
            var original = file != null ? DiffContentFactory.getInstance().create(project, file) : DiffContentFactory.getInstance().create("");
            var modified = DiffContentFactory.getInstance().create(item.content());
            var request = new SimpleDiffRequest("Changes in " + item.fileName(), original, modified, "Original", "Modified");

            DiffManager.getInstance().showDiff(project, request);
        } catch (Exception ex) {
            Messages.showErrorDialog("Error showing diff: " + ex.getMessage(), "Diff Error");
        }
    }

    /**
     * DisposeParentOnClose ensures that the parent container disposes of the component when closed.
     */
    private record DisposeParentOnClose(JComponent component) implements Disposable {
        @Override
        public void dispose() {
            Container parent = component.getParent();
            if (parent == null) return;
            parent.remove(component);
            parent.revalidate();
            parent.repaint();
        }
    }
}
