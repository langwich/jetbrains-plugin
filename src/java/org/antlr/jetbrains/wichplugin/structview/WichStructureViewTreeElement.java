package org.antlr.jetbrains.wichplugin.structview;

import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.treeView.smartTree.SortableTreeElement;
import com.intellij.ide.util.treeView.smartTree.TreeElement;
import com.intellij.navigation.ItemPresentation;
import org.antlr.jetbrains.wichplugin.Icons;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public abstract class WichStructureViewTreeElement
	implements StructureViewTreeElement, ItemPresentation, SortableTreeElement
{
	protected ParseTree node;
	protected WichStructureViewModel model;

	public WichStructureViewTreeElement(WichStructureViewModel model, ParseTree node) {
		this.model = model;
		this.node = node;
	}

	@Nullable
	@Override
	public Icon getIcon(boolean unused) {
		return Icons.WICH_FILE;
	}

	@Nullable
	@Override
	public String getPresentableText() {
		if ( node instanceof TerminalNode ) {
			return ((TerminalNode) node).getSymbol().getText();
		}
		return node.getClass().getSimpleName();
	}

	@Override
	public void navigate(boolean requestFocus) {
	}

	@Override
	public boolean canNavigate() {
		return false;
	}

	@Override
	public boolean canNavigateToSource() {
		return false;
	}

	@Nullable
	@Override
	public String getLocationString() {
		return null;
	}

	@Override
	public Object getValue() {
		return node;
	}

	@NotNull
	@Override
	public String getAlphaSortKey() {
		return getPresentableText();
	}

	@NotNull
	@Override
	public TreeElement[] getChildren() {
		return EMPTY_ARRAY;
	}
}
