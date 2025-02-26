package com.chancetop.naixt.plugin.idea.windows;

import javax.swing.*;
import java.awt.*;

/**
 * @author stephen
 */
public class ThinkingIndicator {
    public static JPanel createThinkingIndicator() {
        var thinkingPanel = new JPanel(new BorderLayout());
        thinkingPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Color.DARK_GRAY),
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
