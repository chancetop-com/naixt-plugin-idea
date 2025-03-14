package com.chancetop.naixt.plugin.idea.windows.inernal;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.Optional;

/**
 * @author stephen
 */
public class WindowsUtils {
    public static void scrollBottom(JPanel conversationPanel, JScrollPane conversationScrollPane) {
        SwingUtilities.invokeLater(() -> {
            conversationPanel.validate();
            conversationPanel.paintImmediately(conversationPanel.getBounds());
            conversationScrollPane.validate();
            conversationScrollPane.paintImmediately(conversationScrollPane.getBounds());

            var components = conversationPanel.getComponents();
            if (components.length > 0) {
                Rectangle lastRect = components[components.length - 1].getBounds();
                conversationPanel.scrollRectToVisible(new Rectangle(
                        0,
                        lastRect.y + lastRect.height + 10,
                        1,
                        1
                ));
            }

            var vertical = conversationScrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    public static Optional<Component> findChildComponentByName(Container component, String name) {
        return Arrays.stream(component.getComponents())
                .filter(v -> name.equals(v.getName()))
                .findFirst();
    }
}
