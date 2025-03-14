package com.chancetop.naixt.plugin.idea.windows;

import com.chancetop.naixt.plugin.idea.windows.inernal.WindowsUtils;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBScrollPane;

import javax.swing.*;
import java.awt.*;

/**
 * @author stephen
 */
public class ThinkingIndicatorPanel {

    public static JPanel addThinkingIndicator(JPanel conversationPanel, JBScrollPane conversationScrollPane) {
        var thinkingPanel = createThinkingIndicator();
        conversationPanel.add(thinkingPanel);
        WindowsUtils.scrollBottom(conversationPanel, conversationScrollPane);
        return thinkingPanel;
    }

    public static JPanel createThinkingIndicator() {
        var thinkingPanel = new JPanel(new BorderLayout());
        thinkingPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, JBColor.DARK_GRAY),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        var label = new JLabel("Naixt:");
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        thinkingPanel.add(label, BorderLayout.NORTH);

        var indicatorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        var thinkingLabel = new JLabel("Thinking...Please wait...");
        indicatorPanel.add(thinkingLabel);
        thinkingPanel.add(indicatorPanel, BorderLayout.CENTER);
        return thinkingPanel;
    }
}
