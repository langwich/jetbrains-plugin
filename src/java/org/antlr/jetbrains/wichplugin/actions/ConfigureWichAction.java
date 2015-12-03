package org.antlr.jetbrains.wichplugin.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.DialogWrapper;
import org.antlr.jetbrains.wichplugin.dialogs.WichConfigDialog;

public class ConfigureWichAction extends AnAction {
	public void actionPerformed(AnActionEvent e) {
		if ( e.getProject()==null ) {
			return; // whoa!
		}
		WichConfigDialog configDialog = new WichConfigDialog(e.getProject());
		configDialog.setTitle("Configure Wich Plugin");
		configDialog.show();

		if ( configDialog.getExitCode()==DialogWrapper.OK_EXIT_CODE ) {
			System.out.println("saving values\n");
			configDialog.saveValues(e.getProject());
		}
	}
}
