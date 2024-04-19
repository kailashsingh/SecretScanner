package com.kailash.secretscanner;

import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentIterator;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.content.Content;
import com.intellij.util.indexing.AdditionalIndexedRootsScope;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class ScanSecret extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {

        Project project = e.getProject();
        GlobalSearchScope baseScope = GlobalSearchScope.projectScope(project);
        AdditionalIndexedRootsScope scope = new AdditionalIndexedRootsScope(baseScope);

        // Collection<VirtualFile> files = FileTypeIndex.getFiles(JavaFileType.INSTANCE, baseScope);

        //Collection<VirtualFile> files = FilenameIndex.getAllFilesByExt(project, "java", baseScope);

        Collection<VirtualFile> files = new ArrayList<>();

        ProjectFileIndex.SERVICE.getInstance(project).iterateContent(new ContentIterator() {
            public boolean processFile(final VirtualFile virtualFile) {
                if (virtualFile.isCharsetSet()) {
                    files.add(virtualFile);
                }
                return true;
            }
        });

        ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow("Secret Scanner");
        ConsoleView consoleView = TextConsoleBuilderFactory.getInstance().createBuilder(project).getConsole();
        Content content = toolWindow.getContentManager().getFactory().createContent(consoleView.getComponent(), "Secret Scanner Output", false);
        toolWindow.getContentManager().addContent(content);

        consoleView.print("Starting Scanning for Secrets ...", ConsoleViewContentType.LOG_WARNING_OUTPUT);
        consoleView.print("\nfile count: " + files.size(), ConsoleViewContentType.LOG_WARNING_OUTPUT);
        files.stream().forEach(file -> {
            try {
                consoleView.print("\n" + new String(file.contentsToByteArray()), ConsoleViewContentType.NORMAL_OUTPUT);
            } catch (IOException exp) {
                throw new RuntimeException("Issue reading file content");
            }
        });
        consoleView.print("\nSecrets Scanning finished", ConsoleViewContentType.LOG_WARNING_OUTPUT);
    }
}
