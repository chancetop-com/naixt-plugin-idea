package com.chancetop.naixt.plugin.idea.windows;

import javax.swing.*;

/**
 * @author stephen
 */
public class NaixtToolWindowUtil {

    public static void clearLastMessageRegenerateButton(JPanel conversationPanel) {
        var lastComponent = conversationPanel.getComponent(conversationPanel.getComponentCount() - 1);
        if (!(lastComponent instanceof JPanel messagePanel)) return;

        var northComponent = messagePanel.getComponent(0);
        if (!(northComponent instanceof JLabel)) return;

        if (isUserQuery((JLabel) northComponent)) return;

        for (var panelComponent : messagePanel.getComponents()) {
            if (!(panelComponent instanceof JPanel buttonPanel)) continue;

            for (var btnComponent : buttonPanel.getComponents()) {
                if (btnComponent instanceof JButton && "Regenerate".equals(((JButton) btnComponent).getText())) {
                    buttonPanel.remove(btnComponent);
                    buttonPanel.revalidate();
                    buttonPanel.repaint();
                }
            }
        }
    }

    private static boolean isUserQuery(JLabel label) {
        return label.getText().equals("You:");
    }
}
