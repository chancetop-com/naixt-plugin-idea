package com.chancetop.naixt.plugin.idea.windows;

import com.chancetop.naixt.plugin.idea.ide.IdeUtils;
import com.chancetop.naixt.plugin.idea.windows.inernal.FileChangeItem;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.ui.JBColor;
import com.intellij.ui.SimpleColoredComponent;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;

/**
 * @author stephen
 */
public class FileChangeListRenderer implements ListCellRenderer<FileChangeItem> {
    private final SimpleColoredComponent component = new SimpleColoredComponent();
    private final JPanel panel = new JPanel(new BorderLayout());
    private final Project project;

    public FileChangeListRenderer(Project project) {
        panel.setBorder(JBUI.Borders.empty(2, 5));
        panel.add(component, BorderLayout.CENTER);
        this.project = project;
    }

    @Override
    public Component getListCellRendererComponent(
            JList<? extends FileChangeItem> list,
            FileChangeItem value,
            int index,
            boolean isSelected,
            boolean cellHasFocus) {

        component.clear();
        panel.setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());

        var fileIcon = getFileIcon(IdeUtils.toAbsolutePath(project, value.filePath()));
        component.setIcon(fileIcon);

        component.append(" " + value.fileName() + " ", new SimpleTextAttributes(
                SimpleTextAttributes.STYLE_PLAIN,
                getActionColor(value.action())));

        component.append(" " + value.filePath(), new SimpleTextAttributes(
                SimpleTextAttributes.STYLE_SMALLER,
                JBColor.GRAY));

        component.setBackground(panel.getBackground());
        return panel;
    }

    private Color getActionColor(com.chancetop.naixt.agent.api.naixt.Action action) {
        return switch (action) {
            case ADD -> JBColor.GREEN;
            case MODIFY -> JBColor.BLUE;
            case DELETE -> JBColor.LIGHT_GRAY;
        };
    }

    private Icon getFileIcon(String filePath) {
        var vFile = LocalFileSystem.getInstance().findFileByPath(filePath);
        if (vFile != null) {
            var type = FileTypeManager.getInstance().getFileTypeByFile(vFile);
            return type.getIcon();
        }
        int dotIndex = filePath.lastIndexOf('.');
        if (dotIndex > 0) {
            String ext = filePath.substring(dotIndex + 1);
            return FileTypeManager.getInstance().getFileTypeByExtension(ext).getIcon();
        }
        return AllIcons.FileTypes.Unknown;
    }
}
