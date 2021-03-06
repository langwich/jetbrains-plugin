package org.antlr.jetbrains.wichplugin;

import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class WichFileType extends LanguageFileType {
	public static final WichFileType INSTANCE = new WichFileType();

	protected WichFileType() {
		super(WichLanguage.INSTANCE);
	}



	@NotNull
	@Override
	public String getName() {
		return "Wich file";
	}

	@NotNull
	@Override
	public String getDescription() {
		return "Wich file";
	}

	@NotNull
	@Override
	public String getDefaultExtension() {
		return "w";
	}

	@Nullable
	@Override
	public Icon getIcon() {
		return Icons.WICH_FILE;
	}
}
