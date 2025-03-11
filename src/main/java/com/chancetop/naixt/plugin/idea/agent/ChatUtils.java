package com.chancetop.naixt.plugin.idea.agent;

import com.chancetop.naixt.agent.api.naixt.ChatResponse;

/**
 * @author stephen
 */
public class ChatUtils {
    public static final String STILL_THINKING = "\n\nStill thinking and progress...Please wait...";

    public static boolean hasAction(ChatResponse rsp) {
        return rsp.fileContents != null && !rsp.fileContents.isEmpty();
    }

    public static String buildContent(String lastContent, ChatResponse rsp) {
        var content = lastContent.substring(0, lastContent.lastIndexOf(STILL_THINKING)) + "\n\n" + rsp.text;
        return hasAction(rsp) || rsp.groupFinished ? content : content + STILL_THINKING;
    }
}
