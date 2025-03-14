package com.chancetop.naixt.plugin.idea.settings;

/**
 * @author stephen
 */
public class NaixtSettingState {
    private String llmProvider = "";
    private String llmProviderUrl = "";
    private String llmProviderApiKey = "";
    private String llmProviderModel = "o1-mini";
    private String planningModel = "o1-mini";
    private String agentPackageDownloadUrl = "https://github.com/chancetop-com/naixt-agent/releases/download/1.0.3/agent-service.tar";

    public String getLlmProvider() {
        return llmProvider;
    }

    public void setLlmProvider(String llmProvider) {
        this.llmProvider = llmProvider;
    }

    public String getLlmProviderUrl() {
        return llmProviderUrl;
    }

    public void setLlmProviderUrl(String llmProviderUrl) {
        this.llmProviderUrl = llmProviderUrl;
    }

    public String getLlmProviderApiKey() {
        return llmProviderApiKey;
    }

    public void setLlmProviderApiKey(String llmProviderApiKey) {
        this.llmProviderApiKey = llmProviderApiKey;
    }

    public String getLlmProviderModel() {
        return llmProviderModel;
    }

    public void setLlmProviderModel(String llmProviderModel) {
        this.llmProviderModel = llmProviderModel;
    }

    public String getAgentPackageDownloadUrl() {
        return agentPackageDownloadUrl;
    }

    public void setAgentPackageDownloadUrl(String agentPackageDownloadUrl) {
        this.agentPackageDownloadUrl = agentPackageDownloadUrl;
    }

    public String getPlanningModel() {
        return planningModel;
    }

    public void setPlanningModel(String planningModel) {
        this.planningModel = planningModel;
    }
}
