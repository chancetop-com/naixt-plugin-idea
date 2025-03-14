package com.chancetop.naixt.plugin.idea.ide;

import com.chancetop.naixt.plugin.idea.ide.internal.IdeCurrentInfo;
import com.chancetop.naixt.plugin.idea.ide.internal.Position;
import com.intellij.codeInsight.daemon.impl.DaemonCodeAnalyzerImpl;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.psi.PsiManager;
import core.framework.util.Strings;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.function.Consumer;

/**
 * @author stephen
 */
public class IdeUtils {
    public static String getCurrentFilePath(Project project) {
        var files = FileEditorManager.getInstance(project).getSelectedFiles();
        return files.length == 0 || files[0].getPath().equalsIgnoreCase("/diff") ? "" : files[0].getPath();
    }

    public static Position offsetToPosition(Document doc, int offset) {
        return new Position(doc.getLineNumber(offset), offset - doc.getLineStartOffset(doc.getLineNumber(offset)));
    }

    public static Position getCurrentPosition(Project project) {
        return ReadAction.compute(() -> {
            var editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
            if (editor == null) {
                return new Position(0, 0);
            }
            var offset = editor.getCaretModel().getOffset();
            return offsetToPosition(editor.getDocument(), offset);
        });
    }

    public static String getProjectBase(Project project) {
        return project.getBasePath();
    }

    public static String getProjectPath(Project project) {
        if (project == null) {
            return "";
        }
        var contentRoots = ProjectRootManager.getInstance(project).getContentRoots();
        if (contentRoots.length > 0) {
            return contentRoots[0].getPath();
        }
        return "";
    }

    public static String toAbsolutePath(Project project, String path) {
        var projectPath = getProjectBase(project);
        if (path.startsWith(projectPath)) return path;
        return Paths.get(projectPath, path).toAbsolutePath().toString();
    }

    public static void getInfo(Project project, Consumer<IdeCurrentInfo> consumer) {
        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Fetch edit current info", false) {
            private IdeCurrentInfo info;
            @Override
            public void run(@NotNull ProgressIndicator progressIndicator) {
                var currentFilePath = getCurrentFilePath(project);
                info = new IdeCurrentInfo(getProjectBase(project), currentFilePath, getCurrentPosition(project), getCurrentFileDiagnostic(project, currentFilePath));
            }
            @Override
            public void onSuccess() {
                ApplicationManager.getApplication().invokeLater(() -> consumer.accept(info));
            }
        });
    }

    public static String getCurrentFileDiagnostic(Project project, String currentFilePath) {
        var virtualFile = LocalFileSystem.getInstance().findFileByPath(currentFilePath);
        if (virtualFile == null) return "";

        return ReadAction.compute(() -> {
            var psiFile = PsiManager.getInstance(project).findFile(virtualFile);
            if (psiFile == null) return "";

            var highlightInfos = new ArrayList<String>();
            DaemonCodeAnalyzerImpl.processHighlights(
                    psiFile.getFileDocument(),
                    project,
                    HighlightSeverity.ERROR,
                    0,
                    psiFile.getTextLength(),
                    highlightInfo -> {
                        if (highlightInfo.getSeverity() == HighlightSeverity.ERROR) {
                            var position = offsetToPosition(psiFile.getFileDocument(), highlightInfo.getActualStartOffset());
                            var desc = Strings.format("Error[start position -> line:{}, column:{}]: {}", position.line(), position.column(), highlightInfo.getDescription());
                            highlightInfos.add(desc);
                        }
                        return true;
                    });

            return String.join("\n", highlightInfos);
        });
    }

    public static void refreshWorkspace(String workspacePath) {
        ApplicationManager.getApplication().invokeLater(() -> {
            var baseDir = LocalFileSystem.getInstance().findFileByPath(workspacePath);
            if (baseDir != null) {
                VfsUtil.markDirtyAndRefresh(true, true, false, baseDir);
            }
        });
    }
}
