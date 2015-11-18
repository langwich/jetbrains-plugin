package org.antlr.jetbrains.wichplugin.structview;

import com.intellij.ide.structureView.StructureViewModel;
import com.intellij.ide.structureView.TreeBasedStructureViewBuilder;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.antlr.jetbrains.wichplugin.WichPluginController;
import org.jetbrains.annotations.NotNull;

public class WichStructureViewBuilder extends TreeBasedStructureViewBuilder {
	public final VirtualFile file;
	public final Project project;

	public WichStructureViewBuilder(@NotNull VirtualFile file,
									@NotNull Project project)
	{
		this.file = file;
		this.project = project;
	}

	@NotNull
	@Override
	public StructureViewModel createStructureViewModel(final Editor editor) {
		final WichStructureViewModel model = new WichStructureViewModel(editor, file);
		WichPluginController controller = WichPluginController.getInstance(project);
		if ( controller!=null ) {
			controller.registerStructureViewModel(editor, model);
		}

		return model;
	}
}
