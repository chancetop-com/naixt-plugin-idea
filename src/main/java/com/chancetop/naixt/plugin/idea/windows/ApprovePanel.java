package com.chancetop.naixt.plugin.idea.windows;

import com.chancetop.naixt.agent.api.naixt.ChatResponse;
import com.chancetop.naixt.plugin.idea.ide.IdeUtils;
import com.intellij.diff.DiffContentFactory;
import com.intellij.diff.DiffManager;
import com.intellij.diff.requests.SimpleDiffRequest;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author stephen
 */
public class ApprovePanel {
    public static void showApprovePanel(Project project, ChatResponse msg, Runnable afterApproved) {
        var dialog = new JDialog();
        dialog.setTitle("Change List");
        dialog.setSize(500, 400);
        dialog.setLayout(new BorderLayout());

        var fileListModel = new DefaultListModel<String>();
        for (var fileContent : msg.fileContents) {
            fileListModel.addElement(fileContent.filePath + " - " + fileContent.action.toString());
        }
        var fileList = new JBList<>(fileListModel);
        fileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        var diffArea = new JTextPane();
        diffArea.setEditable(false);

        fileList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (e.getClickCount() != 2) return;
                var selectedValue = fileList.getSelectedValue();
                if (selectedValue == null) return;
                var selected = msg.fileContents.stream().filter(fc -> (fc.filePath + " - " + fc.action.toString()).equals(selectedValue)).findFirst().orElse(null);
                if (selected == null) return;
                try {
                    var originalFile = LocalFileSystem.getInstance().findFileByPath(IdeUtils.toAbsolutePath(project, selected.filePath));
                    var originalContent = originalFile != null ? DiffContentFactory.getInstance().create(project, originalFile) : DiffContentFactory.getInstance().create("");
                    var modifiedContent = DiffContentFactory.getInstance().create(selected.content);
                    var request = new SimpleDiffRequest("Change in " + selected.filePath, originalContent, modifiedContent, "Original", "Modified");
                    DiffManager.getInstance().showDiff(project, request);
                } catch (Exception ex) {
                    diffArea.setText("Error loading diff: " + ex.getMessage());
                }
            }
        });

        dialog.add(new JBScrollPane(fileList), BorderLayout.CENTER);

        var buttonPanel = createButtonPanel(dialog, afterApproved);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    private static @NotNull JPanel createButtonPanel(JDialog dialog, Runnable afterApproved) {
        var buttonPanel = new JPanel();
        var confirmButton = new JButton("Apply");
        var cancelButton = new JButton("Cancel");

        confirmButton.addActionListener(e1 -> {
            afterApproved.run();
            dialog.dispose();
        });
        cancelButton.addActionListener(e1 -> dialog.dispose());

        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);
        return buttonPanel;
    }
}
