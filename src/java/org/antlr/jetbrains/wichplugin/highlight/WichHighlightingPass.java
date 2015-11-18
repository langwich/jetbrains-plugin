package org.antlr.jetbrains.wichplugin.highlight;

import com.intellij.codeHighlighting.TextEditorHighlightingPass;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;

public class WichHighlightingPass extends TextEditorHighlightingPass {
	protected Editor editor;

	public WichHighlightingPass(Project project, Editor editor) {
		super(project, editor.getDocument());
		this.editor = editor;
	}

	@Override
	public void doApplyInformationToEditor() {
		if ( editor==null ) return;
		WichSyntaxHighlighter groupHighlighter = new WichSyntaxHighlighter(editor,0);
		groupHighlighter.highlight();
	}

	@Override
	public void doCollectInformation(ProgressIndicator progress) {
	}
}
