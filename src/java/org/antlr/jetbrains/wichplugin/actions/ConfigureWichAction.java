package org.antlr.jetbrains.wichplugin.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import org.antlr.jetbrains.wichplugin.WichPluginController;
import org.antlr.jetbrains.wichplugin.dialogs.WichConfigDialog;

public class ConfigureWichAction extends AnAction {
	public void actionPerformed(AnActionEvent e) {
		Project project = e.getProject();
		if ( project==null ) {
			return; // whoa!
		}
		WichConfigDialog configDialog = new WichConfigDialog(project);
		configDialog.setTitle("Configure Wich Plugin");
		configDialog.show();

		if ( configDialog.getExitCode()==DialogWrapper.OK_EXIT_CODE ) {
			configDialog.saveValues(project);

			WichPluginController controller = WichPluginController.getInstance(project);
			if ( controller!=null ) {
				FileEditorManager fmgr = FileEditorManager.getInstance(project);
				VirtualFile[] files = fmgr.getSelectedFiles();
				for (int i = 0; i<files.length; i++) {
					VirtualFile file = files[i];
					if ( file.getName().endsWith(".w") ) {
						final Document doc = FileDocumentManager.getInstance().getDocument(file);
						controller.editorDocumentAlteredEvent(doc);
					}
				}
			}
		}
	}
}
