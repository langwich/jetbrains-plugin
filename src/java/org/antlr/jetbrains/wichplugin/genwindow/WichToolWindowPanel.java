package org.antlr.jetbrains.wichplugin.genwindow;

import com.intellij.openapi.editor.Document;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTabbedPane;
import org.antlr.v4.runtime.misc.Triple;
import org.antlr.v4.runtime.misc.Utils;
import wich.codegen.CompilerUtils;
import wich.errors.WichErrorHandler;
import wich.semantics.SymbolTable;

import javax.swing.*;
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

import static junit.framework.Assert.assertTrue;

public class WichToolWindowPanel extends JBPanel {
	protected static final String WORKING_DIR = "/tmp/";
	protected static final String LIB_DIR = "/usr/local/wich/lib";
	protected static final String INCLUDE_DIR = "/usr/local/wich/include";

	private JBTabbedPane tabs;
	private JBScrollPane CTab;
	private JTextArea Coutput;
	private JBScrollPane LLVMTab;
	private JTextArea LLVMoutput;
	private JBScrollPane BytecodeTab;
	private JTextArea Bytecodeoutput;
	private JTextArea execOutput;
	private JBScrollPane execScrollPane;

	public WichToolWindowPanel() {
		super(new BorderLayout());
		setupGUI();
	}

	public void setupGUI() {
		CTab = new JBScrollPane();
		Coutput = new JTextArea();
		Coutput.setText("Generated C code");
		CTab.setViewportView(Coutput);

		LLVMTab = new JBScrollPane();
		LLVMoutput = new JTextArea();
		LLVMoutput.setText("Generated LLVM code");
		LLVMTab.setViewportView(LLVMoutput);

		BytecodeTab = new JBScrollPane();
		Bytecodeoutput = new JTextArea();
		Bytecodeoutput.setText("Generated Bytecode");
		BytecodeTab.setViewportView(Bytecodeoutput);

		tabs = new JBTabbedPane();
		tabs.addTab("C", CTab);
		tabs.addTab("LLVM", LLVMTab);
		tabs.addTab("Bytecode", BytecodeTab);

		execScrollPane = new JBScrollPane();
		execOutput = new JTextArea(4,80);
		execOutput.setText("foo");
		execScrollPane.setViewportView(execOutput);

		add(tabs, BorderLayout.CENTER);
		add(execScrollPane, BorderLayout.SOUTH);
	}

	public void updateOutput(Document doc) {
		String wichCode = doc.getText();
		SymbolTable symtab = new SymbolTable();
		WichErrorHandler err = new WichErrorHandler();
		CompilerUtils.CodeGenTarget target = CompilerUtils.CodeGenTarget.PLAIN;
		String genCode = CompilerUtils.genCode(wichCode, symtab, err, target);
		Coutput.setText(genCode);

		if ( err.getErrorNum()==0 ) {
			String gendCodeFile = WORKING_DIR+"script.c";
			try {
				CompilerUtils.writeFile(gendCodeFile, genCode, StandardCharsets.UTF_8);
			}
			catch (IOException ioe) {
				execOutput.setText("can't write "+gendCodeFile+":"+Arrays.toString(ioe.getStackTrace()));
			}
			java.util.List<String> cc = new ArrayList<>();
			String executable = "script";
			File execF = new File(executable);
			if ( execF.exists() ) {
				execF.delete();
			}
			cc.addAll(
				Arrays.asList(
					"cc", "-g", "-o", executable,
					gendCodeFile,
					"-L", LIB_DIR,
					"-D"+target.flag,
					"-I", INCLUDE_DIR, "-std=c99", "-O0")
			         );
			for (String lib : target.libs) {
				cc.add("-l"+lib);
			}
			String[] cmd = cc.toArray(new String[cc.size()]);
			Triple<Integer, String, String> result = null;
			try { result = exec(cmd); }
			catch (Exception e) {
				execOutput.setText("can't compile "+gendCodeFile+Arrays.toString(e.getStackTrace()));
			}
			String cmdS = Utils.join(cmd, " ");
			execOutput.setText("$ "+cmdS+"\n");
			if ( result!=null && result.a!=0 ) {
				throw new RuntimeException("failed compilation of "+gendCodeFile+" with result code "+result.a+
										   " from\n"+
				                           cmdS+"\nstderr:\n"+result.c);
			}
			try {
				execOutput.insert("$ ./"+executable+"\n", execOutput.getText().length());
				String output = executeC(executable);
				execOutput.insert(output, execOutput.getText().length());
			}
			catch (Exception ie) {
				System.err.println("problem with exec of "+executable);
			}
		}
	}

	protected void compile(String wichInputFilename, CompilerUtils.CodeGenTarget target, List<String> cc, String gen, String exec)
		throws IOException
	{
		// Translate to C file.
		SymbolTable symtab = new SymbolTable();
		WichErrorHandler err = new WichErrorHandler();
		String wichInput = CompilerUtils.readFile(wichInputFilename, CompilerUtils.FILE_ENCODING);
		String actual = CompilerUtils.genCode(wichInput, symtab, err, target);
		assertTrue(err.toString(), err.getErrorNum()==0);
		CompilerUtils.writeFile(gen, actual, StandardCharsets.UTF_8);

		File execF = new File(exec);
		if ( execF.exists() ) {
			execF.delete();
		}
		for (String lib : target.libs) {
			cc.add("-l"+lib);
		}
		String[] cmd = cc.toArray(new String[cc.size()]);
//		if ( mallocImpl!=CompilerUtils.MallocImpl.SYSTEM ) {
//			cc.addAll(Arrays.asList("-l"+mallocImpl.lib, "-lmalloc_common"));
//		}
		final Triple<Integer, String, String> result = exec(cmd);
		String cmdS = Utils.join(cmd, " ");
		System.out.println(cmdS);
		if ( result.a!=0 ) {
			throw new RuntimeException("failed compilation of "+gen+" with result code "+result.a+
									   " from\n"+
			                           cmdS+"\nstderr:\n"+result.c);
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
