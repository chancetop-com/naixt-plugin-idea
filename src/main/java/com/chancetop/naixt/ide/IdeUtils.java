package com.chancetop.naixt.ide;

import com.chancetop.naixt.ide.internal.Position;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;

/**
 * @author stephen
 */
public class IdeUtils {
    public static String getCurrentFilePath(Project project) {
        var files = FileEditorManager.getInstance(project).getSelectedFiles();
        return files.length == 0 ? "" : files[0].getPath();
    }

    public static Position getCurrentPosition(Project project) {
        var editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        if (editor == null) {
            return new Position(0, 0);
        }
        var doc = editor.getDocument();
        var offset = editor.getCaretModel().getOffset();
        return new Position(doc.getLineNumber(offset), offset - doc.getLineStartOffset(doc.getLineNumber(offset)));
    }
}
