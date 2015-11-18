package org.antlr.jetbrains.wichplugin.structview;

import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.ide.structureView.StructureViewBuilderProvider;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.antlr.jetbrains.wichplugin.WichFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WichStructureViewBuilderProvider implements StructureViewBuilderProvider {
	@Nullable
	@Override
	public StructureViewBuilder getStructureViewBuilder(@NotNull FileType fileType,
	                                                    @NotNull VirtualFile file,
	                                                    @NotNull Project project)
	{
		if ( fileType instanceof WichFileType) {
			WichStructureViewBuilder builder = new WichStructureViewBuilder(file, project);
			return builder;
		}
		return null;
	}
}
