package com.chancetop.naixt.plugin.idea.windows;

import com.chancetop.naixt.plugin.idea.agent.AgentServerService;
import com.chancetop.naixt.agent.api.naixt.AgentChatResponse;
import com.chancetop.naixt.plugin.idea.agent.ChatResult;
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
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.List;
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
        var context = new NaixtToolWindowContext(project, toolWindow, mainPanel, conversationPanel, conversationScrollPane, workspaceBasePath, agentServerService);

        conversationPanel.setLayout(new BoxLayout(conversationPanel, BoxLayout.Y_AXIS));
        toolWindow.getContentManager().addContent(ContentFactory.getInstance().createContent(mainPanel, "", false));

        mainPanel.add(createHeaderPanel(context), BorderLayout.NORTH);
        mainPanel.add(conversationScrollPane, BorderLayout.CENTER);
        // take care of this callback context
        context.setCallback(textCallback(context));
        mainPanel.add(InputPanel.createBottomInputAreaAndButtonsPanel(context), BorderLayout.SOUTH);

        sendWelcomeMessage(context);
    }

    private void sendWelcomeMessage(NaixtToolWindowContext context) {
        context.conversationPanel().removeAll();

        SwingUtilities.invokeLater(() -> {
            var helloMessagePanel = MessagePanel.createMessagePanel(context, false, false,
                    new ChatResult(true, AgentChatResponse.of(MessagePanel.HELLO_MESSAGE)), List.of());
            context.conversationPanel().add(helloMessagePanel);
            context.conversationPanel().validate();
            context.conversationPanel().repaint();
        });

        IdeUtils.getInfo(context.project(), info -> CompletableFuture.runAsync(() -> {
            var suggestions = agentServerService.suggestion(info);
            if (context.conversationPanel().getComponents().length != 1) return;
            context.conversationPanel().removeAll();

            SwingUtilities.invokeLater(() -> {
                var helloMessagePanel = MessagePanel.createMessagePanel(context, false, false,
                        new ChatResult(true, AgentChatResponse.of(MessagePanel.HELLO_MESSAGE)), suggestions);
                context.conversationPanel().add(helloMessagePanel);
                context.conversationPanel().validate();
                context.conversationPanel().repaint();
            });
        }));
    }

    private @NotNull JPanel createHeaderPanel(NaixtToolWindowContext context) {
        var headerPanel = new JPanel(new BorderLayout());
        headerPanel.setName("HeaderPanel");
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
                context.conversationPanel().removeAll();
                repaintConversationPanel(context.conversationPanel());
                agentServerService.clearShortTermMemory();
                sendWelcomeMessage(context);
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
        return (text, show, after) -> {
            if (show) {
                if (text.isEmpty()) {
                    Messages.showMessageDialog(context.project(), "Empty input!", "Warning", Messages.getWarningIcon());
                    return;
                }
                MessagePanel.clearLastMessageRegenerateButton(context.conversationPanel());
                addMessageToConversation(context, new ChatResult(true, AgentChatResponse.of(text)), true, false);
                after.run();
            }
            responseToConversation(context, text);
        };
    }

    private void responseToConversation(NaixtToolWindowContext context, String text) {
        var thinkingIndicatorPanel = ThinkingIndicatorPanel.addThinkingIndicator(context.conversationPanel(), context.conversationScrollPane());
        WindowsUtils.scrollBottom(context.conversationPanel(), context.conversationScrollPane());
        IdeUtils.getInfo(context.project(), info -> {
            var isFirstResponse = new AtomicBoolean(true);
            CompletableFuture.runAsync(() -> agentServerService.chatSse(text, info, result -> SwingUtilities.invokeLater(() -> {
                if (isFirstResponse.getAndSet(false)) {
                    context.conversationPanel().remove(thinkingIndicatorPanel);
                    addMessageToConversation(context, result, false, true);
                } else {
                    updateLastMessage(context, result.response());
                }
                WindowsUtils.scrollBottom(context.conversationPanel(), context.conversationScrollPane());
            })));
        });
    }

    private void updateLastMessage(NaixtToolWindowContext context, AgentChatResponse response) {
        var components = context.conversationPanel().getComponents();
        if (components.length == 0) return;

        var lastComponent = components[components.length - 1];
        if (lastComponent instanceof JPanel lastMessagePanel) {
            WindowsUtils.findChildComponentByName(lastMessagePanel, "MessageMediaPanel")
                    .flatMap(panel -> WindowsUtils.findChildComponentByName((Container) panel, "MessageTextArea"))
                    .ifPresent(textArea -> ((JTextArea) textArea).setText(ChatUtils.buildContent(((JTextArea) textArea).getText(), response)));
            if (ChatUtils.hasAction(response)) {
                WindowsUtils.findChildComponentByName(lastMessagePanel, "ApproveButtonPanel")
                        .ifPresent(panel -> {
                            ((JPanel) panel).add(MessagePanel.createApproveButton(context, response));
                            panel.revalidate();
                            panel.repaint();
                        });
            }
        }
    }

    private void addMessageToConversation(NaixtToolWindowContext context, ChatResult result, boolean isUser, boolean showRegenerate) {
        var messagePanel = MessagePanel.createMessagePanel(context, isUser, showRegenerate, result, null);
        context.conversationPanel().add(messagePanel);
        context.conversationPanel().validate();
        context.conversationPanel().paintImmediately(context.conversationPanel().getBounds());
        WindowsUtils.scrollBottom(context.conversationPanel(), context.conversationScrollPane());
    }

    private void repaintConversationPanel(JPanel conversationPanel) {
        conversationPanel.revalidate();
        conversationPanel.repaint();
    }
}