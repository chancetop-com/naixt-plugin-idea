package com.chancetop.naixt.plugin.idea.windows;

import com.chancetop.naixt.plugin.idea.windows.inernal.NaixtToolWindowContext;
import com.chancetop.naixt.plugin.idea.windows.inernal.TextAreaCallback;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * @author stephen
 */
public class InputPanel {

    public static JPanel createBottomInputAreaAndButtonsPanel(NaixtToolWindowContext context) {
        var inputPanel = new JPanel();
        inputPanel.setName("InputPanel");
        inputPanel.setLayout(new BorderLayout(JBUI.scale(5), JBUI.scale(5)));
        inputPanel.setBorder(JBUI.Borders.empty(JBUI.scale(5), JBUI.scale(5), JBUI.scale(5), JBUI.scale(5)));

        var inputTextArea = new JBTextArea(5, 20);
        inputTextArea.setLineWrap(true);
        inputTextArea.setWrapStyleWord(true);
        inputTextArea.setBorder(new LineBorder(JBColor.GRAY, 1, true));
        inputTextArea.setFont(UIManager.getFont("TextArea.font"));

        var inputScrollPane = new JBScrollPane(inputTextArea);
        inputScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        inputScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        inputScrollPane.setBorder(BorderFactory.createEmptyBorder());

        var buttonPanel = new JPanel();
        buttonPanel.setLayout(new BorderLayout());

        var addButton = createAddButton(context);
        var maximizeButton = createMaximizeButton(context, context.callback());
        var sendButton = createSendButton(inputTextArea, context.callback());

        buttonPanel.add(maximizeButton, BorderLayout.WEST);

        var rightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, JBUI.scale(5), 0));
        rightButtons.add(addButton);
        rightButtons.add(sendButton);
        buttonPanel.add(rightButtons, BorderLayout.EAST);

        context.mainPanel().getRootPane().setDefaultButton(sendButton);

        inputTextArea.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "sendMessage");
        inputTextArea.getActionMap().put("sendMessage", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendButton.doClick();
            }
        });

        inputPanel.add(inputScrollPane, BorderLayout.CENTER);
        inputPanel.add(buttonPanel, BorderLayout.SOUTH);
        return inputPanel;
    }

    public static void openMaximizedWindow(NaixtToolWindowContext context, TextAreaCallback callback) {
        var dialog = new JDialog(WindowManager.getInstance().getFrame(context.project()), "Maximized Input", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(JBUI.scale(600), JBUI.scale(400));
        dialog.setLocationRelativeTo(WindowManager.getInstance().getFrame(context.project()));

        var panel = new JPanel();
        panel.setLayout(new BorderLayout(JBUI.scale(10), JBUI.scale(10)));
        panel.setBorder(JBUI.Borders.empty(JBUI.scale(10), JBUI.scale(10), JBUI.scale(10), JBUI.scale(10)));

        var inputTextArea = new JBTextArea(10, 30);
        inputTextArea.setLineWrap(true);
        inputTextArea.setWrapStyleWord(true);
        inputTextArea.setBorder(new LineBorder(JBColor.GRAY, 1, true));
        inputTextArea.setFont(UIManager.getFont("TextArea.font"));

        var inputScrollPane = new JBScrollPane(inputTextArea);
        inputScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        inputScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        inputScrollPane.setBorder(BorderFactory.createEmptyBorder());

        var buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, JBUI.scale(5), 0));

        var addButton = createAddButton(context);
        var sendButton = createSendButton(inputTextArea, callback);

        buttonPanel.add(addButton);
        buttonPanel.add(sendButton);

        inputTextArea.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "sendMessage2");
        inputTextArea.getActionMap().put("sendMessage2", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendButton.doClick();
                dialog.dispose();
            }
        });
        sendButton.addActionListener(l -> dialog.dispose());

        panel.add(inputScrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private static JButton createMaximizeButton(NaixtToolWindowContext context, TextAreaCallback callback) {
        var button = new JButton();
        button.setIcon(AllIcons.Windows.Maximize); // Updated icon
        button.setBorderPainted(false);
        button.setFocusable(false);
        button.setPreferredSize(new Dimension(JBUI.scale(30), JBUI.scale(30)));
        button.setToolTipText("Maximize");
        button.addActionListener(e -> InputPanel.openMaximizedWindow(context, callback));
        return button;
    }

    private static JButton createAddButton(NaixtToolWindowContext context) {
        var button = new JButton();
        button.setIcon(AllIcons.General.Add);
        button.setBorderPainted(false);
        button.setFocusable(false);
        button.setPreferredSize(new Dimension(JBUI.scale(30), JBUI.scale(30)));
        button.setToolTipText("Add");
        button.addActionListener(e -> {
            Messages.showMessageDialog(context.project(), "'+' 按钮被点击！", "Information", Messages.getInformationIcon());
        });
        return button;
    }

    public static @NotNull JButton createSendButton(JTextArea inputTextArea, TextAreaCallback callback) {
        var sendButton = new JButton("Send");
        sendButton.setFocusable(false);
        sendButton.setPreferredSize(new Dimension(JBUI.scale(80), JBUI.scale(30)));
        sendButton.setFont(UIManager.getFont("Button.font"));
        sendButton.setBackground(JBColor.background());
        sendButton.setForeground(JBColor.foreground());
        sendButton.addActionListener(l -> callback.run(inputTextArea.getText(), true, () -> inputTextArea.setText("")));
        return sendButton;
    }
}