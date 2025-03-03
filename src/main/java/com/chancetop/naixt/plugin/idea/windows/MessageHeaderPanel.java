package com.chancetop.naixt.plugin.idea.windows;

import com.chancetop.naixt.agent.api.naixt.ChatResponse;
import com.intellij.icons.AllIcons;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;

/**
 * @author stephen
 */
public class MessageHeaderPanel {
	public final static String HELLO_MESSAGE = "Hi @author, I'm Naixt, an AI agent that helps you coding.";

	public static JPanel createMessageHeaderPanel(ChatResponse message, boolean isUser, boolean showRegenerate, Runnable afterRegenerate) {
		var panel = new JPanel(new BorderLayout());
		var label = new JLabel(isUser ? "You:" : "Naixt:");
		label.setFont(label.getFont().deriveFont(Font.BOLD));
		panel.add(label, BorderLayout.WEST);
		if (!isUser && message.text.contains(HELLO_MESSAGE)) return panel;

		var buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		if (showRegenerate) {
			var regenerateButton = new JButton(AllIcons.Actions.Refresh);
			regenerateButton.setToolTipText("Regenerate");
			regenerateButton.setBorderPainted(false);
			regenerateButton.setContentAreaFilled(false);
			regenerateButton.addActionListener(e -> afterRegenerate.run());
			buttonPanel.add(regenerateButton);
		}

		var button = new JButton(AllIcons.Actions.Copy);
		button.setToolTipText("Copy to clipboard");
		button.setBorderPainted(false);
		button.setContentAreaFilled(false);
		button.addActionListener(e -> copyToClipboard(message.text, button));
		buttonPanel.add(button);

		panel.add(buttonPanel, BorderLayout.EAST);
		return panel;
	}

	private static void copyToClipboard(String text, JButton button) {
		var selection = new StringSelection(text);
		var clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(selection, null);

		showCopyNotification(button);
	}

	private static void showCopyNotification(JComponent source) {
		var popup = new JWindow(SwingUtilities.getWindowAncestor(source));
		var label = new JLabel(" Copied! ");
		label.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(JBColor.GRAY),
				BorderFactory.createEmptyBorder(2, 5, 2, 5)
		));
		label.setBackground(new JBColor(new Color(230, 245, 230), new Color(40, 55, 40)));
		label.setOpaque(true);
		popup.getContentPane().add(label);
		popup.pack();

		var screenPos = source.getLocationOnScreen();
		screenPos.translate(source.getWidth() - 30, -label.getPreferredSize().height);
		popup.setLocation(screenPos);

		popup.setVisible(true);
		new Timer(1000, e -> popup.dispose()).start();
	}

	public static void clearLastMessageRegenerateButton(JPanel conversationPanel) {
		var lastComponent = conversationPanel.getComponent(conversationPanel.getComponentCount() - 1);
		if (!(lastComponent instanceof JPanel messagePanel)) return;

		var northComponent = messagePanel.getComponent(0);
		if (!(northComponent instanceof JPanel headerPanel)) return;

		if (isUserQuery((JLabel) headerPanel.getComponent(0))) return;

		for (var panelComponent : headerPanel.getComponents()) {
			if (!(panelComponent instanceof JPanel buttonPanel)) continue;

			for (var btnComponent : buttonPanel.getComponents()) {
				if (btnComponent instanceof JButton && "Regenerate".equals(((JButton) btnComponent).getToolTipText())) {
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
