package com.chancetop.naixt.plugin.idea.agent;

import com.chancetop.naixt.agent.api.naixt.ChatResponse;

/**
 * @author stephen
 */
public record ChatResult(boolean success, ChatResponse response) {
}
