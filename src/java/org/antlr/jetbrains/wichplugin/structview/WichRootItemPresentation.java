package org.antlr.jetbrains.wichplugin.structview;

import com.intellij.openapi.vfs.VirtualFile;
import org.antlr.v4.runtime.tree.ParseTree;
import org.jetbrains.annotations.Nullable;

public class WichRootItemPresentation extends WichItemPresentation {
	protected final VirtualFile file;
	public WichRootItemPresentation(ParseTree node, VirtualFile file) {
		super(node);
		this.file = file;
	}

	@Nullable
	@Override
	public String getPresentableText() {
		return file.getPresentableName();
	}
}
