package com.chancetop.naixt.plugin.idea.windows;

import com.chancetop.naixt.agent.api.naixt.ChatResponse;
import com.intellij.icons.AllIcons;
import com.intellij.ui.JBColor;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;

/**
 * @author stephen
 */
public class MessageHeaderPanel {
	public final static String HELLO_MESSAGE = "Hi @author, I'm Naixt, an AI agent that helps you coding.";
	public static JPanel createMessageHeaderPanel(ChatResponse message, boolean isUser) {
		var panel = new JPanel(new BorderLayout());
		var label = new JLabel(isUser ? "You:" : "Naixt:");
		label.setFont(label.getFont().deriveFont(Font.BOLD));
		panel.add(label, BorderLayout.WEST);
		if (!isUser && message.text.contains(HELLO_MESSAGE)) return panel;

		var button = new JButton(AllIcons.Actions.Copy);
		button.setToolTipText("Copy to clipboard");
		button.setBorderPainted(false);
		button.setContentAreaFilled(false);
		button.addActionListener(e -> {
			var textToCopy = message.text;
			var selection = new StringSelection(textToCopy);
			var clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			clipboard.setContents(selection, null);

			showCopyNotification(button);
		});

		panel.add(button, BorderLayout.EAST);
		return panel;
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
}
