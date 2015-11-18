package org.antlr.jetbrains.wichplugin.parsing;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;

public class ParsingUtils {
	public static CommonTokenStream tokenize(String text) {
		ANTLRInputStream input = new ANTLRInputStream(text);
		WichLexer lexer = new WichLexer(input);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		tokens.fill();
		return tokens;
	}
}
