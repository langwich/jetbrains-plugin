package org.antlr.jetbrains.wichplugin.dialogs;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class WichConfigDialog /*extends DialogWrapper*/ implements Configurable {
	public static final String PROP_CLANG_HOME = "clang-home";
	public static final String PROP_WICH_HOME = "wich-home";
	public static final String DEFAULT_CLANG_HOME = "/usr/local/clang-3.7.0";
	public static final String DEFAULT_WICH_HOME = "/usr/local/wich";

	private JPanel contentPane;
	private JTextField clangHomeTextField;
	private JTextField installDirTextField;

	private boolean modified = false;

	public WichConfigDialog() {
		loadValues();
	}

	@Nls
	@Override
	public String getDisplayName() {
		return "Wich Configuration";
	}

	@Nullable
	@Override
	public String getHelpTopic() {
		return null;
	}

	@Nullable
	@Override
	public JComponent createComponent() {
		clangHomeTextField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				modified=true;
			}
		});
		installDirTextField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				modified=true;
			}
		});
		return contentPane;
	}

	@Override
	public boolean isModified() {
		return modified;
	}

	@Override
	public void apply() throws ConfigurationException {
		saveValues();
	}

	@Override
	public void reset() {
		modified = false;
		loadValues();
	}

	@Override
	public void disposeUIResources() {
	}

	public void loadValues() {
		PropertiesComponent props = PropertiesComponent.getInstance();
		String s = props.getValue(getPropNameForFile(PROP_CLANG_HOME), DEFAULT_CLANG_HOME);
		clangHomeTextField.setText(s);
		s = props.getValue(getPropNameForFile(PROP_WICH_HOME), DEFAULT_WICH_HOME);
		installDirTextField.setText(s);
	}

	public void saveValues() {
		PropertiesComponent props = PropertiesComponent.getInstance();
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

	public static String getProp(Project project, String name, String defaultValue) {
		PropertiesComponent props = PropertiesComponent.getInstance(project);
		String v = props.getValue(getPropNameForFile(name));
		if ( v==null || v.trim().length()==0 ) return defaultValue;
		return v;
	}
}
