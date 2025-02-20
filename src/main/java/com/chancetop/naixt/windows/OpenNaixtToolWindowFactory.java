package com.chancetop.naixt.windows;

import com.chancetop.naixt.agent.AgentServerService;
import com.chancetop.naixt.icons.NaixtIcons;
import com.chancetop.naixt.server.AgentServiceManagementService;
import com.chancetop.naixt.settings.NaixtSettingStateService;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
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
import java.util.Objects;

/**
 * @author stephen
 */
public final class OpenNaixtToolWindowFactory implements ToolWindowFactory, DumbAware {
    private AgentServerService agentServerService;
    private final JTextArea conversationTextField = new JTextArea();
    private final JTextField inputTextField = new JTextField();
    private final JButton sendButton = new JButton("Send");

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        agentServerService = ApplicationManager.getApplication().getService(AgentServerService.class);
        var mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(createHeaderPanel(project), BorderLayout.NORTH);
        mainPanel.add(createConversationPanel(), BorderLayout.CENTER);
        mainPanel.add(createInputPanel(project), BorderLayout.SOUTH);
        toolWindow.getContentManager().addContent(ContentFactory.getInstance().createContent(mainPanel, "", false));
        mainPanel.getRootPane().setDefaultButton(sendButton);
    }

    private @NotNull JPanel createHeaderPanel(@NotNull Project project) {
        var headerPanel = new JPanel(new BorderLayout());
        var nameLabel = new JLabel("  New Conversation");
        headerPanel.add(nameLabel, BorderLayout.WEST);

        var actionGroup = new DefaultActionGroup();
        actionGroup.add(new AnAction("New", "New conversation", AllIcons.General.Add) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                // handle new event
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
                var agentServicePackageUrl = Objects.requireNonNull(NaixtSettingStateService.getInstance().getState()).getAgentPackageDownloadUrl();
                if (agentServicePackageUrl.isEmpty()) {
                    Messages.showMessageDialog(project, "Please set the agent package download url in settings!", "Warning", Messages.getWarningIcon());
                    return;
                }
                AgentServiceManagementService.getInstance().start(agentServicePackageUrl, project);
            }
        });
        actionGroup.add(new AnAction("Stop Agent Server", "Stop agent server", AllIcons.Actions.Suspend) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                AgentServiceManagementService.getInstance().stop(project);
            }
        });

        var toolbar = ActionManager.getInstance().createActionToolbar("CustomToolBar", actionGroup, true);
        toolbar.setTargetComponent(headerPanel);

        headerPanel.add(toolbar.getComponent(), BorderLayout.EAST);
        return headerPanel;
    }

    private @NotNull JScrollPane createConversationPanel() {
        conversationTextField.setText("Hi @author, I'm Naixt, an AI agent that help you coding.");
        conversationTextField.setLineWrap(true);
        conversationTextField.setWrapStyleWord(true);
        return new JBScrollPane(conversationTextField);
    }

    private @NotNull JPanel createInputPanel(@NotNull Project project) {
        var inputPanel = new JPanel(new BorderLayout());
        sendButton.addActionListener(l -> {
            var text = inputTextField.getText();
            if (text.isEmpty()) {
                Messages.showMessageDialog(project, "Empty input!", "Warning", Messages.getWarningIcon());
            }
            var rsp = agentServerService.send(inputTextField.getText(), project);
            conversationTextField.append("\n\n" + toUserChat(text) + "\n\n" + toAgentChat(rsp));
            inputTextField.setText("");
        });
        inputPanel.add(inputTextField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        return inputPanel;
    }

    private String toUserChat(String text) {
        return "You:\n" + text;
    }

    private String toAgentChat(String text) {
        return "Naixt:\n" + text;
    }
}
