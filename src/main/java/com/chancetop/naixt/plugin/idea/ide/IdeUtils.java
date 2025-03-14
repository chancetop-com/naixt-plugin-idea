package com.chancetop.naixt.plugin.idea.ide;

import com.chancetop.naixt.plugin.idea.ide.internal.IdeCurrentInfo;
import com.chancetop.naixt.plugin.idea.ide.internal.Position;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.codeInsight.daemon.impl.DaemonCodeAnalyzerImpl;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.impl.DocumentMarkupModel;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiManager;
import core.framework.util.Strings;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
        return new Position(doc.getLineNumber(offset) + 1, offset - doc.getLineStartOffset(doc.getLineNumber(offset)) + 1);
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
            var document = psiFile.getFileDocument();
            var markupModel = DocumentMarkupModel.forDocument(document, project, true);
            var highlights = markupModel.getAllHighlighters();

            var highlightInfos = new ArrayList<String>();
            Arrays.stream(highlights).forEach(highlighter -> {
                var errorType = highlighter.getErrorStripeTooltip();
                if (!(errorType instanceof HighlightInfo info)) return;
                if (info.getSeverity() == HighlightSeverity.ERROR) {
                    var position = offsetToPosition(document, info.getActualStartOffset());
                    String desc = String.format(
                            "Error[start position -> line:%d, column:%d]: %s",
                            position.line(), position.column(), info.getDescription()
                    );
                    highlightInfos.add(desc);
                }
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
