package com.chancetop.naixt.icons;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

/**
 * @author stephen
 */
public class NaixtIcons {
    private static Icon load(String path) {
        return IconLoader.getIcon(path, NaixtIcons.class.getClassLoader());
    }

    public static final Icon NaixtToolWindow = load("/icons/naixt.svg");
    public static final Icon History = load("/icons/history.svg");
}
