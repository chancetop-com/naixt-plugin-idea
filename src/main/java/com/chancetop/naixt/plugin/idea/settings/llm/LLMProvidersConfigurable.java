package com.chancetop.naixt.plugin.idea.settings.llm;

import com.chancetop.naixt.plugin.idea.settings.NaixtSettingState;
import com.chancetop.naixt.plugin.idea.settings.NaixtSettingStateService;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * Author: stephen
 */
public class LLMProvidersConfigurable implements Configurable {
    private final JComboBox<String> providerComboBox = new ComboBox<>(new String[]{"LiteLLM", "Azure OpenAI", "OpenAI"});
    private final JPanel mainPanel = new JPanel();
    private final JPanel dynamicPanel = new JPanel();
    private final JBTextField endpointField = new JBTextField();
    private final JBTextField modelField = new JBTextField();
    private final JBTextField planningModelField = new JBTextField();
    private final JBTextField atlassianMcpUrlField = new JBTextField();
    private final JBTextField apiKeyField = new JBTextField();
    private final JBTextField agentPackageUrlField = new JBTextField();
    private final JBCheckBox atlassianEnabledCheckBox = new JBCheckBox();
    private NaixtSettingState state;

    @Override
    public @NlsContexts.ConfigurableName String getDisplayName() {
        return "Naixt Settings";
    }

    @Override
    public @Nullable JComponent createComponent() {
        state = NaixtSettingStateService.getInstance().getState();

        mainPanel.setLayout(new GridBagLayout());
        var gbcMain = new GridBagConstraints();
        gbcMain.gridx = 0;
        gbcMain.gridy = 0;
        gbcMain.anchor = GridBagConstraints.NORTHWEST;
        gbcMain.fill = GridBagConstraints.HORIZONTAL;
        gbcMain.weightx = 1.0;
        gbcMain.weighty = 0;
        mainPanel.add(createAgentPanel(), gbcMain);

        gbcMain.gridy++;
        mainPanel.add(createModelPanel(), gbcMain);

        gbcMain.gridy++;
        mainPanel.add(createAtlassianPanel(), gbcMain);

        return mainPanel;
    }

    private JPanel createAgentPanel() {
        var panel = new JPanel(new GridBagLayout());
        var gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = JBUI.insets(5);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        panel.add(new JLabel("Package URL:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;

        panel.add(agentPackageUrlField, gbc);
        return panel;
    }

    private JPanel createModelPanel() {
        var panel = new JPanel(new GridBagLayout());
        var gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = JBUI.insets(5);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        panel.add(new JLabel("Model Name:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;

        panel.add(modelField, gbc);

        gbc.gridx += 1;
        gbc.weightx = 0;
        panel.add(new JLabel("Planning Model Name:"), gbc);
        gbc.gridx += 1;
        gbc.weightx = 1.0;
        panel.add(planningModelField, gbc);
        return panel;
    }

    private JPanel createAtlassianPanel() {
        var panel = new JPanel(new GridBagLayout());
        var gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = JBUI.insets(5);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        panel.add(new JLabel("Atlassian Enabled:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;

        panel.add(atlassianEnabledCheckBox, gbc);

        gbc.gridx += 1;
        gbc.weightx = 0;
        panel.add(new JLabel("Atlassian MCP URL:"), gbc);
        gbc.gridx += 1;
        gbc.weightx = 1.0;
        panel.add(atlassianMcpUrlField, gbc);
        return panel;
    }

    private JPanel createLLMTopPanel() {
        var panel = new JPanel(new GridBagLayout());
        var gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = JBUI.insets(5);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        panel.add(new JLabel("API Provider:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        providerComboBox.setPreferredSize(new Dimension(200, 30));
        panel.add(providerComboBox, gbc);

        return panel;
    }

    private void updateDynamicPanel() {
        var provider = (String) providerComboBox.getSelectedItem();
        dynamicPanel.removeAll();

        var gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = JBUI.insets(5, 0, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        if ("LiteLLM".equals(provider)) {
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 0;
            dynamicPanel.add(new JLabel("Endpoint:"), gbc);

            gbc.gridx = 1;
            gbc.weightx = 1.0;
            endpointField.setPreferredSize(new Dimension(200, 30));
            dynamicPanel.add(endpointField, gbc);

            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.weightx = 0;
            dynamicPanel.add(new JLabel("Model Name:"), gbc);

            gbc.gridx = 1;
            gbc.weightx = 1.0;
            modelField.setPreferredSize(new Dimension(200, 30));
            dynamicPanel.add(modelField, gbc);
        } else {
            gbc.gridy = 0;
            gbc.gridx = 0;
            gbc.weightx = 0;
            dynamicPanel.add(new JLabel("API Key:"), gbc);

            gbc.gridx = 1;
            gbc.weightx = 1.0;
            apiKeyField.setPreferredSize(new Dimension(200, 30));
            dynamicPanel.add(apiKeyField, gbc);
        }

        dynamicPanel.revalidate();
        dynamicPanel.repaint();
    }

    @Override
    public boolean isModified() {
        boolean agentUrlModified = !agentPackageUrlField.getText().equals(state.getAgentPackageDownloadUrl());
        boolean modelModified = !modelField.getText().equals(state.getLlmProviderModel());
        boolean planingModified = !planningModelField.getText().equals(state.getPlanningModel());
        boolean atlassianMcpUrlModified = !atlassianMcpUrlField.getText().equals(state.getAtlassianMcpUrl());
        boolean atlassianEnabledModified = atlassianEnabledCheckBox.isEnabled() != state.getAtlassianEnabled();
        return agentUrlModified || modelModified || planingModified || atlassianMcpUrlModified || atlassianEnabledModified;
    }

    @Override
    public void apply() {
        state.setAgentPackageDownloadUrl(agentPackageUrlField.getText());
        state.setLlmProviderModel(modelField.getText());
        state.setPlanningModel(planningModelField.getText());
        state.setAtlassianEnabled(atlassianEnabledCheckBox.isSelected());
        state.setAtlassianMcpUrl(atlassianMcpUrlField.getText());
    }

    @Override
    public void reset() {
        agentPackageUrlField.setText(state.getAgentPackageDownloadUrl());
        modelField.setText(state.getLlmProviderModel());
        planningModelField.setText(state.getPlanningModel());
        atlassianEnabledCheckBox.setSelected(state.getAtlassianEnabled());
        atlassianMcpUrlField.setText(state.getAtlassianMcpUrl());
    }
}
