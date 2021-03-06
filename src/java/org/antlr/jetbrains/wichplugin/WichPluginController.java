package org.antlr.jetbrains.wichplugin;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.DocumentAdapter;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.event.EditorFactoryAdapter;
import com.intellij.openapi.editor.event.EditorFactoryEvent;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerAdapter;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileAdapter;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.util.messages.MessageBusConnection;
import org.antlr.jetbrains.wichplugin.genwindow.WichToolWindowPanel;
import org.antlr.jetbrains.wichplugin.structview.WichStructureViewModel;
import org.jetbrains.annotations.NotNull;

public class WichPluginController implements ProjectComponent {
	public static final String PLUGIN_ID = "org.antlr.jetbrains.wichplugin";
	public static final Logger LOG = Logger.getInstance("WichPluginController");
	public static final Key<STGroupFileEditorListener> EDITOR_DOCUMENT_LISTENER_KEY =
		Key.create("EDITOR_DOCUMENT_LISTENER_KEY");
	public static final Key<DocumentListener> EDITOR_STRUCTVIEW_LISTENER_KEY =
		Key.create("EDITOR_STRUCTVIEW_LISTENER_KEY");
    public static final String WICH_WINDOW_ID = "Wich compilation";

    public ToolWindow wichWindow;
	public WichToolWindowPanel wichPanel;

    public Project project;
	public boolean projectIsClosed = false;

	public MyVirtualFileListener myVirtualFileListener = new MyVirtualFileListener();
	public MyFileEditorManagerListener myFileEditorManagerListener = new MyFileEditorManagerListener();

	public WichPluginController(Project project) {
		this.project = project;
	}

	public static WichPluginController getInstance(Project project) {
		if ( project==null ) {
			LOG.error("getInstance: project is null");
			return null;
		}
		WichPluginController pc = project.getComponent(WichPluginController.class);
		if ( pc==null ) {
			LOG.error("getInstance: getComponent() for "+project.getName()+" returns null");
		}
		return pc;
	}

	@Override
	public void projectOpened() {
		IdeaPluginDescriptor plugin = PluginManager.getPlugin(PluginId.getId(PLUGIN_ID));
		String version = "unknown";
		if ( plugin!=null ) {
			version = plugin.getVersion();
		}
		LOG.info("Wich Plugin version "+version+", Java version "+ SystemInfo.JAVA_VERSION);

		installListeners();

		createToolWindow();
	}

	@Override
	public void projectClosed() {
		LOG.info("projectClosed " + project.getName());
		//synchronized ( shutdownLock ) { // They should be called from EDT only so no lock
		projectIsClosed = true;
		project = null;
		uninstallListeners();
	}

	@Override
	public void initComponent() { }

	@Override
	public void disposeComponent() { }

	@NotNull
	@Override
	public String getComponentName() {
		return "st.ProjectComponent";
	}

	public void createToolWindow() {
		ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
		wichPanel = new WichToolWindowPanel(project);
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(wichPanel, "", false);

        wichWindow = toolWindowManager.registerToolWindow(WICH_WINDOW_ID, true, ToolWindowAnchor.BOTTOM);
        wichWindow.getContentManager().addContent(content);
        wichWindow.setIcon(Icons.WICH_FILE);
	}

	public void installListeners() {
		LOG.info("installListeners " + project.getName());
		// Listen for .w file saves
		VirtualFileManager.getInstance().addVirtualFileListener(myVirtualFileListener);

		// Listen for editor window changes
		MessageBusConnection msgBus = project.getMessageBus().connect(project);
		msgBus.subscribe(
			FileEditorManagerListener.FILE_EDITOR_MANAGER,
			myFileEditorManagerListener
		);

		// Listen for editor creation and release so that we can install
		// keyboard listeners that notify us when to reanalyze the file.
		// listener should be removed by Intellij when project is disposed
		// per doc.
		EditorFactory factory = EditorFactory.getInstance();
		factory.addEditorFactoryListener(new MyEditorFactoryListener(), project);
	}

	// seems that intellij can kill and reload a project w/o user knowing.
	// a ptr was left around that pointed at a disposed project.
	// Probably was a listener still attached and triggering
	// editor listeners events.
	public void uninstallListeners() {
		VirtualFileManager.getInstance().removeVirtualFileListener(myVirtualFileListener);
		if ( !projectIsClosed ) {
			MessageBusConnection msgBus = project.getMessageBus().connect(project);
			msgBus.disconnect();
		}
	}

	public void fileSavedEvent(VirtualFile file) {
		LOG.info("fileSavedEvent " + (file != null ? file.getPath() : "none") + " " + project.getName());
	}

	public void currentEditorFileSwitchedEvent(VirtualFile oldFile, VirtualFile newFile) {
		LOG.info("currentEditorFileSwitchedEvent "+(oldFile!=null?oldFile.getPath():"none")+
				 " -> "+(newFile!=null?newFile.getPath():"none")+" "+project.getName());
		if ( newFile==null ) return;
		final Document doc = FileDocumentManager.getInstance().getDocument(newFile);
		if ( doc!=null ) {
			editorDocumentAlteredEvent(doc);
		}
	}

	public void editorDocumentAlteredEvent(Document doc) {
		ApplicationManager.getApplication().executeOnPooledThread(()->wichPanel.updateOutput(doc));
	}

	public void editorFileClosedEvent(VirtualFile vfile) {
		// hopefully called only from swing EDT
		String fileName = vfile.getPath();
		LOG.info("editorFileClosedEvent "+fileName+" "+project.getName());
	}

	public Editor getEditor(Document doc) {
		if (doc == null) return null;

		EditorFactory factory = EditorFactory.getInstance();
		final Editor[] editors = factory.getEditors(doc, project);
		if ( editors.length==0 ) {
			// no editor found for this file. likely an out-of-sequence issue
			// where Intellij is opening a project and doesn't fire events
			// in order we'd expect.
			return null;
		}
		return editors[0]; // hope just one
	}

	/** Invalidate tree upon doc change */
	public void registerStructureViewModel(final Editor editor, final WichStructureViewModel model) {
		final Document doc = editor.getDocument();
		final DocumentListener listener = new DocumentAdapter() {
			@Override
			public void documentChanged(DocumentEvent e) { model.invalidate(); }
		};
		DocumentListener oldListener = doc.getUserData(EDITOR_STRUCTVIEW_LISTENER_KEY);
		if ( oldListener!=null ) {
			doc.removeDocumentListener(oldListener);
		}
		doc.putUserData(EDITOR_STRUCTVIEW_LISTENER_KEY, listener);
		doc.addDocumentListener(listener);
	}

	// E v e n t  L i s t e n e r s

	private class MyVirtualFileListener extends VirtualFileAdapter {
		@Override
		public void contentsChanged(VirtualFileEvent event) {
			final VirtualFile vfile = event.getFile();
			if ( !vfile.getName().endsWith(".w") ) return;
			if ( !projectIsClosed ) fileSavedEvent(vfile);
		}
	}

	private class MyFileEditorManagerListener extends FileEditorManagerAdapter {
		@Override
		public void selectionChanged(FileEditorManagerEvent event) {
			if ( !projectIsClosed ) {
				final VirtualFile vfile = event.getNewFile();
				if ( vfile!=null && vfile.getName().endsWith(".w") ) {
					currentEditorFileSwitchedEvent(event.getOldFile(), event.getNewFile());
				}
			}
		}

		@Override
		public void fileClosed(FileEditorManager source, VirtualFile vfile) {
			if ( !projectIsClosed ) {
				if ( vfile!=null && vfile.getName().endsWith(".w") ) {
					editorFileClosedEvent(vfile);
				}
			}
		}
	}

	private class STGroupFileEditorListener extends DocumentAdapter {
		@Override
		public void documentChanged(DocumentEvent e) {
			VirtualFile vfile = FileDocumentManager.getInstance().getFile(e.getDocument());
			if ( vfile!=null && vfile.getName().endsWith(".w") ) {
				editorDocumentAlteredEvent(e.getDocument());
			}
		}
	}

	private class MyEditorFactoryListener extends EditorFactoryAdapter {
		@Override
		public void editorCreated(@NotNull EditorFactoryEvent event) {
			final Editor editor = event.getEditor();
			final Document doc = editor.getDocument();
			VirtualFile vfile = FileDocumentManager.getInstance().getFile(doc);
			if ( vfile!=null && vfile.getName().endsWith(".w") ) {
				STGroupFileEditorListener listener = new STGroupFileEditorListener();
				doc.putUserData(EDITOR_DOCUMENT_LISTENER_KEY, listener);
				doc.addDocumentListener(listener);
			}
		}

		@Override
		public void editorReleased(@NotNull EditorFactoryEvent event) {
			Editor editor = event.getEditor();
			final Document doc = editor.getDocument();
			if (editor.getProject() != null && editor.getProject() != project) {
				return;
			}
			STGroupFileEditorListener listener = editor.getUserData(EDITOR_DOCUMENT_LISTENER_KEY);
			if (listener != null) {
				doc.removeDocumentListener(listener);
				doc.putUserData(EDITOR_DOCUMENT_LISTENER_KEY, null);
			}
			DocumentListener listener2 = editor.getUserData(EDITOR_STRUCTVIEW_LISTENER_KEY);
			if (listener2 != null) {
				doc.removeDocumentListener(listener2);
				doc.putUserData(EDITOR_STRUCTVIEW_LISTENER_KEY, null);
			}
		}
	}
}
