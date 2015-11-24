package org.antlr.jetbrains.wichplugin.genwindow;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.ui.Splitter;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTabbedPane;
import org.antlr.v4.runtime.misc.Triple;
import org.antlr.v4.runtime.misc.Utils;
import wich.codegen.CompilerUtils;
import wich.errors.WichErrorHandler;
import wich.semantics.SymbolTable;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WichToolWindowPanel extends JBPanel {
	protected static final String WORKING_DIR = "/tmp/";
	protected static final String LIB_DIR = "/usr/local/wich/lib";
	protected static final String INCLUDE_DIR = "/usr/local/wich/include";
	public static final String CLANG = "/usr/local/clang-3.7.0";

	private JBTabbedPane translationTabbedPane;

	private JBScrollPane[] tabs = new JBScrollPane[4];
	private JTextArea[] translations = new JTextArea[4];
	private JTextArea[] execOutputs = new JTextArea[4];
	private String[] names = new String[] { "C", "C w/ref counting", "LLVM", "Bytecode" };
	private JTextArea[] consoles = new JTextArea[4];

	private JBScrollPane execScrollPane;
	private JBScrollPane consoleScrollPane;
	private JBPanel outputPanel; // has title, output
	private JBLabel outputLabel; // says which target gen'd this output

	public WichToolWindowPanel() {
		super(new BorderLayout());
		setupGUI();

		translationTabbedPane.addChangeListener(
			new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					switchTabs(translationTabbedPane.getSelectedIndex());
				}
			});
	}

	public void setupGUI() {
		translationTabbedPane = new JBTabbedPane();

		for (int i = 0; i<execOutputs.length; i++) {
			tabs[i] = new JBScrollPane();
			translationTabbedPane.addTab(names[i], tabs[i]);
			translations[i] = new JTextArea();
			tabs[i].setViewportView(translations[i]);
			execOutputs[i] = new JTextArea(4, 80);
			execOutputs[i].setText("");
			consoles[i] = new JTextArea(3, 80);
			consoles[i].setText("");
		}

		execScrollPane = new JBScrollPane();
		consoleScrollPane = new JBScrollPane();
		outputLabel = new JBLabel("");
		outputPanel = new JBPanel(new BorderLayout());
		outputPanel.add(outputLabel, BorderLayout.NORTH);
		outputPanel.add(execScrollPane, BorderLayout.CENTER);
		outputPanel.add(consoleScrollPane, BorderLayout.SOUTH);

		switchTabs(0); // show plain C output first

		Splitter splitPane = new Splitter();
		splitPane.setFirstComponent(translationTabbedPane);
		splitPane.setSecondComponent(outputPanel);

		add(splitPane, BorderLayout.CENTER);
	}

	public void switchTabs(int selectedIndex) {
		execScrollPane.setViewportView(execOutputs[selectedIndex]);
		outputLabel.setText(names[selectedIndex]);
		consoleScrollPane.setViewportView(consoles[selectedIndex]);
	}

	public void updateOutput(Document doc) {
		String wichCode = doc.getText();
		genCode(wichCode, 0, CompilerUtils.CodeGenTarget.PLAIN);
		genCode(wichCode, 1, CompilerUtils.CodeGenTarget.REFCOUNTING);
		genCode(wichCode, 2, CompilerUtils.CodeGenTarget.LLVM);
		genCode(wichCode, 3, CompilerUtils.CodeGenTarget.BYTECODE);
	}

	public void genCode(String wichCode, int targetIndex,
	                    CompilerUtils.CodeGenTarget target)
	{
		JTextArea translation = this.translations[targetIndex];
		JTextArea output = execOutputs[targetIndex];
		JTextArea console = consoles[targetIndex];
		translation.setText("");
		output.setText("");

		SymbolTable symtab = new SymbolTable();
		WichErrorHandler err = new WichErrorHandler();
		String genCode = CompilerUtils.genCode(wichCode, symtab, err, target);

		if ( err.getErrorNum()>0 ) {
			translation.setText(err.toString());
			return;
		}

		translation.setText(genCode);
		String gendCodeFile = WORKING_DIR+"script"+target.fileExtension;
		try {
			CompilerUtils.writeFile(gendCodeFile, genCode, StandardCharsets.UTF_8);
		}
		catch (IOException ioe) {
			console.setText("can't write "+gendCodeFile+":"+Arrays.toString(ioe.getStackTrace()));
			return;
		}
		List<String> cc = new ArrayList<>();
		String executable = "script";
		File execF = new File(executable);
		if ( execF.exists() ) {
			execF.delete();
		}
		if (target == CompilerUtils.CodeGenTarget.LLVM ) {
			cc.addAll(
					Arrays.asList(
							CLANG+"/bin/clang", "-o", executable,
							gendCodeFile,
							"-L", LIB_DIR,
							"-D" + target.flag,
							"-I", INCLUDE_DIR
					)
			);
		}
		else {
			cc.addAll(
				Arrays.asList(
					"cc", "-g", "-o", executable,
					gendCodeFile,
					"-L", LIB_DIR,
					"-D"+target.flag,
					"-I", INCLUDE_DIR, "-std=c99", "-O0")
			         );
		}
		for (String lib : target.libs) {
			cc.add("-l"+lib);
		}
		String[] cmd = cc.toArray(new String[cc.size()]);
		Triple<Integer, String, String> resultTriple = null;
		try { resultTriple = exec(cmd); }
		catch (Exception e) {
			console.setText("can't compile "+gendCodeFile+Arrays.toString(e.getStackTrace()));
		}
		String cmdS = Utils.join(cmd, " ");
		console.setText("$ "+cmdS+"\n");
		if ( resultTriple!=null && resultTriple.a!=0 ) {
			console.insert("failed compilation of "+gendCodeFile+" with result code "+resultTriple.a+
				                  " from\n"+
				                  cmdS+"\nstderr:\n"+resultTriple.c,
			               console.getText().length());
		}
		try {
			console.insert("$ ./"+executable+"\n", console.getText().length());
			String result = executeC(executable);
			output.insert(result, output.getText().length());
		}
		catch (Exception ie) {
			System.err.println("problem with exec of "+executable);
		}
	}

	protected Triple<Integer, String, String> exec(String[] cmd) throws IOException {
		ProcessBuilder pb = new ProcessBuilder();
		pb.command(Arrays.asList(cmd)).directory(new File(WORKING_DIR));
		Process process = pb.start();
		try {
			int resultCode = process.waitFor();
			String stdout = dump(process.getInputStream());
			String stderr = dump(process.getErrorStream());
			Triple<Integer, String, String> ret = new Triple<>(resultCode, stdout, stderr);
			return ret;
		}
		catch (InterruptedException ie) {
			System.err.println("interrupted exec of "+Arrays.toString(cmd));
		}
		return null;
	}

	protected String dump(InputStream is) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		String line;
		StringBuilder out = new StringBuilder();
		while ((line = reader.readLine()) != null) {
			out.append(line);
			out.append(System.getProperty("line.separator"));
		}
		return out.toString();
	}

	protected String executeC(String executable) throws IOException, InterruptedException {
		Triple<Integer, String, String> result = exec(new String[]{"./"+executable});
		if ( result.a!=0 ) {
			throw new RuntimeException("failed execution of "+executable+" with result code "+result.a+"; stderr:\n"+result.c);
		}
		return result.b;
	}
}
