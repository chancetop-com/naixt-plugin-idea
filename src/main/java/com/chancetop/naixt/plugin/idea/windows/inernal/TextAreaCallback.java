package com.chancetop.naixt.plugin.idea.windows.inernal;

/**
 * @author stephen
 */
@FunctionalInterface
public interface TextAreaCallback {
    void run(String text, boolean show, Runnable afterRun);
}
