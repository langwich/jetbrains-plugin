package org.antlr.jetbrains.wichplugin.structview;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.jetbrains.annotations.Nullable;

public class WichTemplateDefItemPresentation extends WichItemPresentation {
	public WichTemplateDefItemPresentation(ParseTree node) {
		super(node);
	}

	@Nullable
	@Override
	public String getPresentableText() {
		return ((TerminalNode)node).getSymbol().getText();
	}
}
