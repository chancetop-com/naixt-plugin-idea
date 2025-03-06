package com.chancetop.naixt.plugin.idea.windows.inernal;

import com.chancetop.naixt.agent.api.naixt.Action;

/**
 * @author stephen
 */
public record FileChangeItem(Action action, String filePath, String fileName, String content) {
}
