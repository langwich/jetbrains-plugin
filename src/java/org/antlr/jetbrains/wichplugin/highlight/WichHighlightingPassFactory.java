package org.antlr.jetbrains.wichplugin.highlight;

import com.intellij.codeHighlighting.TextEditorHighlightingPass;
import com.intellij.codeHighlighting.TextEditorHighlightingPassFactory;
import com.intellij.codeHighlighting.TextEditorHighlightingPassRegistrar;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.Nullable;

public class WichHighlightingPassFactory
	extends AbstractProjectComponent
	implements TextEditorHighlightingPassFactory
{
	public WichHighlightingPassFactory(Project project,
									   TextEditorHighlightingPassRegistrar registrar)
	{
		super(project);
		registrar.registerTextEditorHighlightingPass(this, null, null, true, -1);
	}

	@Nullable
	@Override
	public TextEditorHighlightingPass createHighlightingPass(PsiFile file, Editor editor) {
		if ( editor==null ) return null;
		Document doc = editor.getDocument();
		VirtualFile vfile = FileDocumentManager.getInstance().getFile(doc);
		if ( vfile==null || !vfile.getName().endsWith(".w") ) return null;
		return new WichHighlightingPass(myProject, editor);
	}
}
