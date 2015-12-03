package org.antlr.jetbrains.wichplugin.dialogs;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class WichConfigDialog extends DialogWrapper {
	public static final String PROP_CLANG_HOME = "clang-home";
	public static final String PROP_WICH_HOME = "wich-home";
	public static final String DEFAULT_CLANG_HOME = "/usr/local/clang-3.7.0";
	public static final String DEFAULT_WICH_HOME = "/usr/local/wich";

	final Project project;

	private JPanel contentPane;
	private JTextField clangHomeTextField;
	private JTextField installDirTextField;

	public WichConfigDialog(final Project project) {
		super(project, false);
		init();

		this.project = project;

		setModal(true);

		loadValues(project);
	}

	public void loadValues(Project project) {
		PropertiesComponent props = PropertiesComponent.getInstance(project);
		String s = props.getValue(getPropNameForFile(PROP_CLANG_HOME), DEFAULT_CLANG_HOME);
		clangHomeTextField.setText(s);
		s = props.getValue(getPropNameForFile(PROP_WICH_HOME), DEFAULT_WICH_HOME);
		installDirTextField.setText(s);
	}

	public void saveValues(Project project) {
		PropertiesComponent props = PropertiesComponent.getInstance(project);
		String v = clangHomeTextField.getText();
		if ( v.trim().length()>0 ) {
			props.setValue(getPropNameForFile(PROP_CLANG_HOME), v);
		}
		else {
			props.unsetValue(getPropNameForFile(PROP_CLANG_HOME));
		}
		v = installDirTextField.getText();
		if ( v.trim().length()>0 ) {
			props.setValue(getPropNameForFile(PROP_WICH_HOME), v);
		}
		else {
			props.unsetValue(getPropNameForFile(PROP_WICH_HOME));
		}
	}

	public static String getPropNameForFile(String prop) {
		return "wich_"+prop;
	}

	@Nullable
	@Override
	protected JComponent createCenterPanel() {
		return contentPane;
	}

	public static String getProp(Project project, String name, String defaultValue) {
		PropertiesComponent props = PropertiesComponent.getInstance(project);
		String v = props.getValue(getPropNameForFile(name));
		if ( v==null || v.trim().length()==0 ) return defaultValue;
		return v;
	}
}
