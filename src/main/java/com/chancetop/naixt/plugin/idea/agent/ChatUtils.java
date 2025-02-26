package com.chancetop.naixt.plugin.idea.agent;

import com.chancetop.naixt.agent.api.naixt.ChatResponse;

/**
 * @author stephen
 */
public class ChatUtils {
    public static boolean hasAction(ChatResponse rsp) {
        return rsp.fileContents != null && !rsp.fileContents.isEmpty();
    }
}
