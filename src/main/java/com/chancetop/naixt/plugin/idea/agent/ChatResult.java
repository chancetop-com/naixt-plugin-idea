package com.chancetop.naixt.plugin.idea.agent;

import com.chancetop.naixt.agent.api.naixt.AgentChatResponse;

/**
 * @author stephen
 */
public record ChatResult(boolean success, AgentChatResponse response) {
}
