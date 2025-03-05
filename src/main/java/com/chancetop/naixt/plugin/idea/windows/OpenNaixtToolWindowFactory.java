package com.chancetop.naixt.plugin.idea.windows;

import com.chancetop.naixt.plugin.idea.agent.AgentServerService;
import com.chancetop.naixt.agent.api.naixt.ChatResponse;
import com.chancetop.naixt.plugin.idea.agent.ChatUtils;
import com.chancetop.naixt.plugin.idea.icons.NaixtIcons;
import com.chancetop.naixt.plugin.idea.ide.IdeUtils;
import com.chancetop.naixt.plugin.idea.server.AgentServiceManagementService;
import com.chancetop.naixt.plugin.idea.settings.NaixtSettingStateService;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Objects;

/**
 * @author stephen
 */
public final class OpenNaixtToolWindowFactory implements ToolWindowFactory, DumbAware {
    private final JPanel conversationPanel = new JPanel();
    private final JScrollPane conversationScrollPane = new JBScrollPane(conversationPanel);
    private final JTextField inputTextField = new JTextField();
    private final JButton sendButton = new JButton("Send");
    private AgentServiceManagementService agentServiceManagementService;
    private AgentServerService agentServerService;
    private NaixtSettingStateService naixtSettingStateService;
    private Project project;
    private String workspaceBasePath;

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        agentServerService = AgentServerService.getInstance();
        agentServiceManagementService = AgentServiceManagementService.getInstance();
        naixtSettingStateService = NaixtSettingStateService.getInstance();
        this.project = project;
        workspaceBasePath = project.getBasePath();
        var mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);
        mainPanel.add(conversationScrollPane, BorderLayout.CENTER);
        mainPanel.add(createInputPanel(), BorderLayout.SOUTH);
        conversationPanel.setLayout(new BoxLayout(conversationPanel, BoxLayout.Y_AXIS));
        toolWindow.getContentManager().addContent(ContentFactory.getInstance().createContent(mainPanel, "", false));
        mainPanel.getRootPane().setDefaultButton(sendButton);
        sendWelcomeMessage();
    }

    private void sendWelcomeMessage() {
        addMessageToConversation(ChatResponse.of(MessageHeaderPanel.HELLO_MESSAGE), false, false, false);
    }

    private @NotNull JPanel createHeaderPanel() {
        var headerPanel = new JPanel(new BorderLayout());
        var nameLabel = new JLabel("New Conversation");
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        headerPanel.add(nameLabel, BorderLayout.WEST);

        var actionGroup = new DefaultActionGroup();
        actionGroup.add(new AnAction("New", "New conversation", AllIcons.General.Add) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                // handle new event
                // for now, clear all
                conversationPanel.removeAll();
                sendWelcomeMessage();
                repaintConversationPanel();
                agentServerService.clearShortTermMemory();
            }
        });
        actionGroup.add(new AnAction("History", "Show history", NaixtIcons.History) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                // handle click event
            }
        });
        actionGroup.add(new AnAction("Settings", "Open settings", AllIcons.Actions.More) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                ShowSettingsUtil.getInstance().showSettingsDialog(project, "Naixt Settings");
            }
        });
        actionGroup.addSeparator();
        actionGroup.add(new AnAction("Start Agent Server", "Start agent server", AllIcons.Actions.Run_anything) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                var agentServicePackageUrl = Objects.requireNonNull(naixtSettingStateService.getState()).getAgentPackageDownloadUrl();
                if (agentServicePackageUrl.isEmpty()) {
                    Messages.showMessageDialog(project, "Please set the agent package download url in settings!", "Warning", Messages.getWarningIcon());
                    return;
                }
                agentServiceManagementService.start(agentServicePackageUrl, project);
            }
        });
        actionGroup.add(new AnAction("Stop Agent Server", "Stop agent server", AllIcons.Actions.Suspend) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                agentServiceManagementService.stop(project);
            }
        });

        var toolbar = ActionManager.getInstance().createActionToolbar("CustomToolBar", actionGroup, true);
        toolbar.setTargetComponent(headerPanel);

        headerPanel.add(toolbar.getComponent(), BorderLayout.EAST);
        return headerPanel;
    }

    private @NotNull JPanel createInputPanel() {
        var inputPanel = new JPanel(new BorderLayout());
        sendButton.addActionListener(l -> {
            var text = inputTextField.getText();
            if (text.isEmpty()) {
                Messages.showMessageDialog(project, "Empty input!", "Warning", Messages.getWarningIcon());
                return;
            }
            MessageHeaderPanel.clearLastMessageRegenerateButton(conversationPanel);
            addMessageToConversation(ChatResponse.of(text), true, false, false);
            inputTextField.setText("");

            responseToConversation(text);
        });

        inputPanel.add(inputTextField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        return inputPanel;
    }

    private void responseToConversation(String text) {
        var thinkingPanel = addThinkingIndicator();
        scrollBottom();

        var info = IdeUtils.getInfo(project);
        new SwingWorker<ChatResponse, Void>() {
            @Override
            protected ChatResponse doInBackground() throws Exception {
                try {
                    return agentServerService.send(text, info);
                } catch (Exception e) {
                    Messages.showMessageDialog(project, "Failed to send message to agent", "Warning", Messages.getWarningIcon());
                }
                return ChatResponse.of("Sorry, an error occurred");
            }
            @Override
            protected void done() {
                try {
                    var rsp = get();
                    conversationPanel.remove(thinkingPanel);
                    addMessageToConversation(rsp, false, ChatUtils.hasAction(rsp), true);
                    scrollBottom();
                } catch (Exception ex) {
                    conversationPanel.remove(thinkingPanel);
                    addMessageToConversation(ChatResponse.of("Sorry, an error occurred: " + ex.getMessage()), false, false, true);
                    scrollBottom();
                }
            }
        }.execute();
    }

    private JPanel addThinkingIndicator() {
        var thinkingPanel = ThinkingIndicator.createThinkingIndicator();
        conversationPanel.add(thinkingPanel);
        scrollBottom();
        return thinkingPanel;
    }

    private void addMessageToConversation(ChatResponse message, boolean isUser, boolean showApprove, boolean showRegenerate) {
        var messagePanel = new JPanel(new BorderLayout());
        messagePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, isUser ? JBColor.LIGHT_GRAY : JBColor.DARK_GRAY),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        var header = MessageHeaderPanel.createMessageHeaderPanel(message, isUser, showRegenerate, () -> {
            conversationPanel.remove(messagePanel);
            responseToConversation("Think and regenerate the response");
        });
        messagePanel.add(header, BorderLayout.NORTH);

        var textArea = new JTextArea(message.text);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        textArea.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        messagePanel.add(textArea, BorderLayout.CENTER);

        if (!isUser) {
            var buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            if (showApprove) {
                var approveButton = new JButton("Need Your Approve!");
                approveButton.addActionListener(e -> handleApprove(approveButton, e, message));
                buttonPanel.add(approveButton);
            }
            messagePanel.add(buttonPanel, BorderLayout.SOUTH);
        }

        conversationPanel.add(messagePanel);
        conversationPanel.validate();
        conversationPanel.paintImmediately(conversationPanel.getBounds());
        scrollBottom();
    }

    private void scrollBottom() {
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

    private void repaintConversationPanel() {
        conversationPanel.revalidate();
        conversationPanel.repaint();
    }

    private void handleApprove(JButton button, ActionEvent e, ChatResponse msg) {
        ApprovePanel.showApprovePanel(project, msg, () -> {
            try {
                button.setText("Approved");
                agentServerService.approve(msg, workspaceBasePath);
                IdeUtils.refreshWorkspace(workspaceBasePath);
            } catch (Exception ex) {
                Messages.showMessageDialog(project, "Failed to approve, please check the agent status and try again", "Warning", Messages.getWarningIcon());
            }
        });
    }
}
