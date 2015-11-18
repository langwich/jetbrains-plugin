package org.antlr.jetbrains.wichplugin.structview;

import com.intellij.navigation.ItemPresentation;
import org.antlr.jetbrains.wichplugin.Icons;
import org.antlr.v4.runtime.tree.ParseTree;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public abstract class WichItemPresentation implements ItemPresentation {
	protected ParseTree node;

	public WichItemPresentation(ParseTree node) {
		this.node = node;
	}

	@Nullable
	@Override
	public Icon getIcon(boolean unused) {
		if ( node.getParent()==null ) return null;
		return Icons.WICH_FILE;
	}

	@Nullable
	@Override
	public String getPresentableText() {
		return "n/a";
	}

	@Nullable
	@Override
	public String getLocationString() {
		return null;
	}
}
