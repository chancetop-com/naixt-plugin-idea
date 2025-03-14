package com.chancetop.naixt.plugin.idea.windows;

import com.chancetop.naixt.agent.api.naixt.AgentChatResponse;
import com.chancetop.naixt.plugin.idea.agent.ChatResult;
import com.chancetop.naixt.plugin.idea.agent.ChatUtils;
import com.chancetop.naixt.plugin.idea.ide.IdeUtils;
import com.chancetop.naixt.plugin.idea.windows.inernal.NaixtToolWindowContext;
import com.chancetop.naixt.plugin.idea.windows.inernal.TextAreaCallback;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * @author stephen
 */
public class MessagePanel {
	public final static String HELLO_MESSAGE = "Hi @author, I'm Naixt, an AI agent that helps you coding.";

	public static JPanel createMessagePanel(NaixtToolWindowContext context, boolean isUser, boolean showRegenerate, ChatResult result, List<String> suggestions) {
		var messagePanel = new JPanel(new BorderLayout());
		messagePanel.setName("MessagePanel");

		messagePanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(0, 0, 1, 0, isUser ? JBColor.LIGHT_GRAY : JBColor.DARK_GRAY),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)
		));

		var header = MessagePanel.createMessageHeaderPanel(result.response().text, isUser, showRegenerate, () -> {
			context.conversationPanel().remove(messagePanel);
			context.callback().run("User want you to think and regenerate.", false, () -> {});
		});
		messagePanel.add(header, BorderLayout.NORTH);

		var mediaPanel = new JPanel(new BorderLayout());
		mediaPanel.setName("MessageMediaPanel");
		var text = showRegenerate && result.success() ? result.response().text + ChatUtils.STILL_THINKING : result.response().text;
		var textArea = new JTextArea(text);
		textArea.setName("MessageTextArea");
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setEditable(false);
		textArea.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
		mediaPanel.add(textArea, BorderLayout.NORTH);

		if (suggestions != null && !suggestions.isEmpty()) {
			var suggestionsPanel = new JPanel();
			suggestionsPanel.setName("MessageSuggestionsPanel");
			suggestionsPanel.setLayout(new BoxLayout(suggestionsPanel, BoxLayout.Y_AXIS));
			suggestionsPanel.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));

			var suggestionsLabel = new JLabel("Query Suggestions:");
			suggestionsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
			suggestionsLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
			suggestionsPanel.add(suggestionsLabel);

			for (String suggestion : suggestions) {
				var label = new JLabel(suggestion);
				label.setAlignmentX(Component.LEFT_ALIGNMENT);
				label.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 5));
				label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				label.setForeground(JBColor.BLUE);
				label.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						if (e.getClickCount() == 1) context.callback().run(label.getText(), true, () -> {});
					}
				});
				suggestionsPanel.add(label);
			}
			mediaPanel.add(suggestionsPanel, BorderLayout.CENTER);
		}
		mediaPanel.add(Box.createVerticalBox(), BorderLayout.SOUTH);

		messagePanel.add(mediaPanel, BorderLayout.CENTER);

		if (!isUser) {
			messagePanel.add(createApproveButtonPanel(context, !result.response().fileContents.isEmpty(), result.response()), BorderLayout.SOUTH);
		}
		return messagePanel;
	}

	public static JPanel createApproveButtonPanel(NaixtToolWindowContext context, Boolean showApprove, AgentChatResponse message) {
		var panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		panel.setName("ApproveButtonPanel");
		if (showApprove) {
			panel.add(createApproveButton(context, message));
		}
		return panel;
	}

	public static JButton createApproveButton(NaixtToolWindowContext context, AgentChatResponse message) {
		var approveButton = new JButton("Need Your Approve!");
		approveButton.addActionListener(e -> handleApprove(context, approveButton, e, message));
		return approveButton;
	}

	public static void handleApprove(NaixtToolWindowContext context, JButton button, ActionEvent e, AgentChatResponse msg) {
		ApprovePanel.showApprovePanel(context.project(), msg, () -> {
			try {
				button.setText("Approved");
				context.agentServerService().approve(msg, context.workspaceBasePath());
				IdeUtils.refreshWorkspace(context.workspaceBasePath());
			} catch (Exception ex) {
				Messages.showMessageDialog(context.project(), "Failed to approve, please check the agent status and try again", "Warning", Messages.getWarningIcon());
			}
		});
	}

	public static JPanel createMessageHeaderPanel(String text, boolean isUser, boolean showRegenerate, Runnable afterRegenerate) {
		var panel = new JPanel(new BorderLayout());
		panel.setName("MessageHeaderPanel");
		var label = new JLabel(isUser ? "You:" : "Naixt:");
		label.setFont(label.getFont().deriveFont(Font.BOLD));
		panel.add(label, BorderLayout.WEST);
		if (!isUser && text.contains(HELLO_MESSAGE)) return panel;

		var buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		if (showRegenerate) {
			var regenerateButton = new JButton(AllIcons.Actions.Refresh);
			regenerateButton.setToolTipText("Regenerate");
			regenerateButton.setBorderPainted(false);
			regenerateButton.setContentAreaFilled(false);
			regenerateButton.setPreferredSize(new Dimension(JBUI.scale(30), JBUI.scale(30)));
			regenerateButton.addActionListener(e -> afterRegenerate.run());
			buttonPanel.add(regenerateButton);
		}

		var button = new JButton(AllIcons.Actions.Copy);
		button.setToolTipText("Copy to clipboard");
		button.setBorderPainted(false);
		button.setContentAreaFilled(false);
		button.setPreferredSize(new Dimension(JBUI.scale(30), JBUI.scale(30)));
		button.addActionListener(e -> copyToClipboard(text, button));
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
