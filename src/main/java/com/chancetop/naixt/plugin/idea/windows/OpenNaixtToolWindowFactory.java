package com.chancetop.naixt.plugin.idea.windows;

import com.chancetop.naixt.plugin.idea.agent.AgentServerService;
import com.chancetop.naixt.agent.api.naixt.ChatResponse;
import com.chancetop.naixt.plugin.idea.agent.ChatUtils;
import com.chancetop.naixt.plugin.idea.icons.NaixtIcons;
import com.chancetop.naixt.plugin.idea.ide.IdeUtils;
import com.chancetop.naixt.plugin.idea.server.AgentServiceManagementService;
import com.chancetop.naixt.plugin.idea.server.AgentStartResult;
import com.chancetop.naixt.plugin.idea.settings.NaixtSettingStateService;
import com.chancetop.naixt.plugin.idea.windows.inernal.NaixtToolWindowContext;
import com.chancetop.naixt.plugin.idea.windows.inernal.TextAreaCallback;
import com.chancetop.naixt.plugin.idea.windows.inernal.WindowsUtils;
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
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author stephen
 */
public final class OpenNaixtToolWindowFactory implements ToolWindowFactory, DumbAware {
    private final AgentServerService agentServerService = AgentServerService.getInstance();
    private final NaixtSettingStateService naixtSettingStateService = NaixtSettingStateService.getInstance();
    private final AgentServiceManagementService agentServiceManagementService = AgentServiceManagementService.getInstance();

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        var workspaceBasePath = project.getBasePath();
        var mainPanel = new JPanel(new BorderLayout());
        var conversationPanel = new JPanel();
        var conversationScrollPane = new JBScrollPane(conversationPanel);
        var context = new NaixtToolWindowContext(project, toolWindow, mainPanel, conversationPanel, conversationScrollPane, workspaceBasePath);

        conversationPanel.setLayout(new BoxLayout(conversationPanel, BoxLayout.Y_AXIS));
        toolWindow.getContentManager().addContent(ContentFactory.getInstance().createContent(mainPanel, "", false));

        mainPanel.add(createHeaderPanel(context), BorderLayout.NORTH);
        mainPanel.add(conversationScrollPane, BorderLayout.CENTER);
        mainPanel.add(InputPanel.createBottomInputAreaAndButtonsPanel(context, textCallback(context)), BorderLayout.SOUTH);

        sendWelcomeMessage(context);
    }

    private void sendWelcomeMessage(NaixtToolWindowContext context) {
        context.conversationPanel().removeAll();
        addMessageToConversation(context, ChatResponse.of(MessageHeaderPanel.HELLO_MESSAGE), false, false, false);
    }

    private @NotNull JPanel createHeaderPanel(NaixtToolWindowContext context) {
        var headerPanel = new JPanel(new BorderLayout());
        var nameLabel = new JLabel("New Conversation");
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        headerPanel.add(nameLabel, BorderLayout.WEST);

        var actionGroup = new DefaultActionGroup();
        actionGroup.addSeparator();
        actionGroup.add(newConversationAction(context));
        actionGroup.add(historyAction(context));
        actionGroup.add(settingAction(context));
        actionGroup.add(startAgentServerAction(context));
        actionGroup.add(stopAgentServerAction(context));

        var toolbar = ActionManager.getInstance().createActionToolbar("CustomToolBar", actionGroup, true);
        toolbar.setTargetComponent(headerPanel);

        headerPanel.add(toolbar.getComponent(), BorderLayout.EAST);
        return headerPanel;
    }

    private @NotNull AnAction stopAgentServerAction(NaixtToolWindowContext context) {
        return new AnAction("Stop Agent Server", "Stop agent server", AllIcons.Actions.Suspend) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                agentServiceManagementService.stop(context.project());
            }
        };
    }

    private @NotNull AnAction settingAction(NaixtToolWindowContext context) {
        return new AnAction("Settings", "Open settings", AllIcons.Actions.More) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                ShowSettingsUtil.getInstance().showSettingsDialog(context.project(), "Naixt Settings");
            }
        };
    }

    private @NotNull AnAction startAgentServerAction(NaixtToolWindowContext context) {
        return new AnAction("Start Agent Server", "Start agent server", AllIcons.Actions.Run_anything) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                var agentServicePackageUrl = Objects.requireNonNull(naixtSettingStateService.getState()).getAgentPackageDownloadUrl();
                if (agentServicePackageUrl.isEmpty()) {
                    Messages.showMessageDialog(context.project(), "Please set the agent package download url in settings!", "Warning", Messages.getWarningIcon());
                    return;
                }
                Messages.showMessageDialog(context.project(), "If it's the first time you start the server, plugin need to download the agent package, please wait a moment!", "Info", Messages.getInformationIcon());

                new SwingWorker<AgentStartResult, Void>() {
                    @Override
                    protected AgentStartResult doInBackground() throws Exception {
                        return agentServiceManagementService.start(agentServicePackageUrl);
                    }

                    @Override
                    protected void done() {
                        try {
                            afterStartAgentServer(context, get());
                        } catch (InterruptedException | ExecutionException e) {
                            Messages.showMessageDialog(context.project(), "Failed to start agent server, please check the log for more information\n" + e.getMessage(), "Warning", Messages.getErrorIcon());
                        }
                    }
                }.execute();
            }
        };
    }

    private @NotNull AnAction historyAction(NaixtToolWindowContext context) {
        return new AnAction("History", "Show history", NaixtIcons.HISTORY) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                Messages.showMessageDialog(context.project(), "Not implemented yet", "Info", Messages.getInformationIcon());
            }
        };
    }

    private @NotNull AnAction newConversationAction(NaixtToolWindowContext context) {
        return new AnAction("New", "New conversation", AllIcons.General.Add) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                // handle new event
                // for now, clear all
                context.conversationPanel().removeAll();
                sendWelcomeMessage(context);
                repaintConversationPanel(context.conversationPanel());
                agentServerService.clearShortTermMemory();
            }
        };
    }

    private void afterStartAgentServer(NaixtToolWindowContext context, AgentStartResult result) {
        if (!result.success()) {
            Messages.showMessageDialog(context.project(), result.message(), "Error", Messages.getErrorIcon());
            return;
        }
        if (result.warning()) {
            Messages.showMessageDialog(context.project(), result.message(), "Warning", Messages.getWarningIcon());
            return;
        }
        Messages.showMessageDialog(context.project(), result.message(), "Info", Messages.getInformationIcon());
    }

    private TextAreaCallback textCallback(NaixtToolWindowContext context) {
        return textArea -> {
            var text = textArea.getText();
            if (text.isEmpty()) {
                Messages.showMessageDialog(context.project(), "Empty input!", "Warning", Messages.getWarningIcon());
                return;
            }
            MessageHeaderPanel.clearLastMessageRegenerateButton(context.conversationPanel());
            addMessageToConversation(context, ChatResponse.of(text), true, false, false);
            textArea.setText("");

            responseToConversation(context, text);
        };
    }

    private void responseToConversation(NaixtToolWindowContext context, String text) {
        var thinkingIndicatorPanel = ThinkingIndicator.addThinkingIndicator(context.conversationPanel(), context.conversationScrollPane());
        WindowsUtils.scrollBottom(context.conversationPanel(), context.conversationScrollPane());

        var info = IdeUtils.getInfo(context.project());
        var isFirstResponse = new AtomicBoolean(true);

        CompletableFuture.runAsync(() -> agentServerService.chatSse(text, info, response -> SwingUtilities.invokeLater(() -> {
            if (isFirstResponse.getAndSet(false)) {
                context.conversationPanel().remove(thinkingIndicatorPanel);
                addMessageToConversation(context, response, false, ChatUtils.hasAction(response), true);
            } else {
                updateLastMessage(context, response);
            }
            WindowsUtils.scrollBottom(context.conversationPanel(), context.conversationScrollPane());
        })));
    }

    private void updateLastMessage(NaixtToolWindowContext context, ChatResponse response) {
        var components = context.conversationPanel().getComponents();
        if (components.length == 0) return;

        var lastComponent = components[components.length - 1];
        if (lastComponent instanceof JPanel messagePanel) {
            Arrays.stream(messagePanel.getComponents())
                    .filter(v -> "MessageTextArea".equals(v.getName()))
                    .findFirst()
                    .ifPresent(textArea -> ((JTextArea) textArea).setText(ChatUtils.buildContent(((JTextArea) textArea).getText(), response)));
            if (ChatUtils.hasAction(response)) {
                Arrays.stream(messagePanel.getComponents())
                        .filter(v -> "ApproveButtonPanel".equals(v.getName()))
                        .findFirst()
                        .ifPresent(panel -> {
                            ((JPanel) panel).add(createApproveButton(context, response));
                            panel.revalidate();
                            panel.repaint();
                        });
            }
        }
    }

    private void addMessageToConversation(NaixtToolWindowContext context, ChatResponse message, boolean isUser, boolean showApprove, boolean showRegenerate) {
        var messagePanel = new JPanel(new BorderLayout());
        messagePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, isUser ? JBColor.LIGHT_GRAY : JBColor.DARK_GRAY),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        var header = MessageHeaderPanel.createMessageHeaderPanel(message, isUser, showRegenerate, () -> {
            context.conversationPanel().remove(messagePanel);
            responseToConversation(context, "Think and regenerate the response");
        });
        messagePanel.add(header, BorderLayout.NORTH);

        var textArea = new JTextArea(showRegenerate ? message.text + ChatUtils.STILL_THINKING : message.text);
        textArea.setName("MessageTextArea");
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        textArea.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        messagePanel.add(textArea, BorderLayout.CENTER);

        if (!isUser) {
            messagePanel.add(createApproveButtonPanel(context, showApprove, message), BorderLayout.SOUTH);
        }

        context.conversationPanel().add(messagePanel);
        context.conversationPanel().validate();
        context.conversationPanel().paintImmediately(context.conversationPanel().getBounds());
        WindowsUtils.scrollBottom(context.conversationPanel(), context.conversationScrollPane());
    }

    private JPanel createApproveButtonPanel(NaixtToolWindowContext context, Boolean showApprove, ChatResponse message) {
        var panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setName("ApproveButtonPanel");
        if (showApprove) {
            panel.add(createApproveButton(context, message));
        }
        return panel;
    }

    private JButton createApproveButton(NaixtToolWindowContext context, ChatResponse message) {
        var approveButton = new JButton("Need Your Approve!");
        approveButton.addActionListener(e -> handleApprove(context, approveButton, e, message));
        return approveButton;
    }

    private void repaintConversationPanel(JPanel conversationPanel) {
        conversationPanel.revalidate();
        conversationPanel.repaint();
    }

    private void handleApprove(NaixtToolWindowContext context, JButton button, ActionEvent e, ChatResponse msg) {
        ApprovePanel.showApprovePanel(context.project(), msg, () -> {
            try {
                button.setText("Approved");
                agentServerService.approve(msg, context.workspaceBasePath());
                IdeUtils.refreshWorkspace(context.workspaceBasePath());
            } catch (Exception ex) {
                Messages.showMessageDialog(context.project(), "Failed to approve, please check the agent status and try again", "Warning", Messages.getWarningIcon());
            }
        });
    }
}
