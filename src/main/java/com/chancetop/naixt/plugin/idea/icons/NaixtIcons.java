package com.chancetop.naixt.plugin.idea.icons;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

/**
 * @author stephen
 */
public class NaixtIcons {
    private static Icon load(String path) {
        return IconLoader.getIcon(path, NaixtIcons.class.getClassLoader());
    }

    public static final Icon NAIXT_TOOL_WINDOW = load("/icons/naixt.svg");
    public static final Icon HISTORY = load("/icons/history.svg");
    public static final Icon CHANGELIST = load("/icons/changelist.svg");
}
