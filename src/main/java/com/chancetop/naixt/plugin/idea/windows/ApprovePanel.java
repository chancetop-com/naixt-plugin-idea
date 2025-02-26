package com.chancetop.naixt.plugin.idea.windows;

import com.chancetop.naixt.agent.api.naixt.ChatResponse;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

/**
 * @author stephen
 */
public class ApprovePanel {
    public static void showApprovePanel(ChatResponse msg, Runnable afterApproved) {
        var dialog = new JDialog();
        dialog.setTitle("Change List");
        dialog.setSize(600, 400);
        dialog.setLayout(new BorderLayout());

        var filePanel = new JPanel();
        filePanel.setLayout(new BoxLayout(filePanel, BoxLayout.Y_AXIS));
        for (var fileContent : msg.fileContents) {
            var fileTextArea = new JTextArea(String.format("File: %s\nAction: %s\nDiff: \n%s\n", fileContent.filePath, fileContent.action.toString(), fileContent.content));
            fileTextArea.setEditable(false);
            fileTextArea.setLineWrap(true);
            fileTextArea.setWrapStyleWord(true);
            filePanel.add(fileTextArea);
            filePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        }
        dialog.add(new JScrollPane(filePanel));

        var buttonPanel = createButtonPanel(dialog, afterApproved);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);

    }

    private static @NotNull JPanel createButtonPanel(JDialog dialog, Runnable afterApproved) {
        var buttonPanel = new JPanel();
        var confirmButton = new JButton("OK");
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
