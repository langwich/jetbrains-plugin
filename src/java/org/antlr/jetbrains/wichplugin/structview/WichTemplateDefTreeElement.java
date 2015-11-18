package org.antlr.jetbrains.wichplugin.structview;

import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.ScrollType;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.jetbrains.annotations.NotNull;

public class WichTemplateDefTreeElement extends WichStructureViewTreeElement {
	public WichTemplateDefTreeElement(WichStructureViewModel model, ParseTree node) {
		super(model, node);
	}

	@NotNull
	@Override
	public ItemPresentation getPresentation() {
		return new WichTemplateDefItemPresentation(node);
	}

	@Override
	public boolean canNavigate() {
		return true;
	}

	@Override
	public boolean canNavigateToSource() {
		return true;
	}

	@Override
	public void navigate(boolean requestFocus) {
		CaretModel caretModel = model.editor.getCaretModel();
		model.editor.getScrollingModel().scrollToCaret(ScrollType.MAKE_VISIBLE);
		caretModel.moveToOffset(((TerminalNode)node).getSymbol().getStartIndex());
	}
}
