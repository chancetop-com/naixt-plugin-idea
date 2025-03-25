package com.chancetop.naixt.plugin.idea.settings;

/**
 * @author stephen
 */
public class NaixtSettingState {
    private String llmProviderModel = "o1-mini";
    private String planningModel = "o1-mini";
    private String agentPackageDownloadUrl = "https://github.com/chancetop-com/naixt-agent/releases/download/1.0.3/agent-service.tar";
    private Boolean atlassianEnabled = Boolean.FALSE;
    private String atlassianMcpUrl = "http://localhost:8000";

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

    public Boolean getAtlassianEnabled() {
        return atlassianEnabled;
    }

    public void setAtlassianEnabled(Boolean atlassianEnabled) {
        this.atlassianEnabled = atlassianEnabled;
    }

    public String getAtlassianMcpUrl() {
        return atlassianMcpUrl;
    }

    public void setAtlassianMcpUrl(String atlassianMcpUrl) {
        this.atlassianMcpUrl = atlassianMcpUrl;
    }
}
