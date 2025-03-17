package com.chancetop.naixt.plugin.idea.agent;

import com.chancetop.naixt.agent.api.naixt.AgentChatResponse;

/**
 * @author stephen
 */
public class ChatUtils {
    public static final String STILL_THINKING = "\n\nStill thinking and progress...Please wait...";

    public static boolean hasAction(AgentChatResponse rsp) {
        return rsp.fileContents != null && !rsp.fileContents.isEmpty();
    }

    public static String buildContent(String lastContent, AgentChatResponse rsp) {
        var content = lastContent.contains(STILL_THINKING) ? lastContent.substring(0, lastContent.lastIndexOf(STILL_THINKING)) + "\n\n" + rsp.text : lastContent + "\n\n" + rsp.text;
        return hasAction(rsp) || rsp.groupFinished ? content : content + STILL_THINKING;
    }
}
