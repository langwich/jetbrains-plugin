package org.antlr.jetbrains.wichplugin.highlight;

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import org.antlr.jetbrains.wichplugin.parsing.ParserErrorListener;
import org.antlr.jetbrains.wichplugin.parsing.ParsingResult;
import org.antlr.jetbrains.wichplugin.parsing.WichLexer;
import org.antlr.jetbrains.wichplugin.parsing.WichParser;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.xpath.XPath;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;

import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;

public class WichSyntaxHighlighter extends SyntaxHighlighter {
	public static final TextAttributesKey TEMPLATE_NAME =
		createTextAttributesKey("TEMPLATE_NAME", DefaultLanguageHighlighterColors.INSTANCE_METHOD);

	public WichSyntaxHighlighter(Editor editor, int startIndex) {
		super(editor, startIndex);
	}

	@Override
	public Lexer getLexer(String text) {
		final ANTLRInputStream input;
		try {
			input = new ANTLRInputStream(new StringReader(text));
			final WichLexer lexer = new WichLexer(input);
			return lexer;
		}
		catch (IOException ioe) {
			System.err.println("huh? can't happen");
		}
		return null;
	}

	@Override
	public ParsingResult parse(CommonTokenStream tokens) {
		WichParser parser = new WichParser(tokens);
		parser.removeErrorListeners();
		ParserErrorListener errorListener = new ParserErrorListener();
		parser.addErrorListener(errorListener);
		ParserRuleContext tree = parser.script();
		return new ParsingResult(parser, tree, errorListener);
	}

	@Override
	public void highlightTree(ParserRuleContext tree, Parser parser) {
		final Collection<ParseTree> options = XPath.findAll(tree, "//formalArg/ID", parser);
		for (ParseTree o : options) {
			TerminalNode tnode = (TerminalNode)o;
			if ( !(tnode instanceof ErrorNode) ) {
				highlightToken(tnode.getSymbol(),
							   new TextAttributesKey[]{DefaultLanguageHighlighterColors.INSTANCE_FIELD});
			}
		}
		final Collection<ParseTree> ids = XPath.findAll(tree, "//template/ID", parser);
		for (ParseTree id : ids) {
			TerminalNode tnode = (TerminalNode)id;
			highlightToken(tnode.getSymbol(), new TextAttributesKey[]{TEMPLATE_NAME});
		}
	}

	@NotNull
	@Override
	public TextAttributesKey[] getAttributesKey(Token t) {
		switch (t.getType()) {
			case WichLexer.LINE_COMMENT:
				return new TextAttributesKey[]{DefaultLanguageHighlighterColors.LINE_COMMENT};
			case WichLexer.COMMENT:
				return new TextAttributesKey[]{DefaultLanguageHighlighterColors.BLOCK_COMMENT};
			case WichLexer.ID:
				return new TextAttributesKey[]{DefaultLanguageHighlighterColors.IDENTIFIER};

			case WichLexer.IF :
			case WichLexer.ELSE :
			case WichLexer.WHILE :
			case WichLexer.VAR :
			case WichLexer.EQUAL :
			case WichLexer.RETURN :
			case WichLexer.	PRINT :
			case WichLexer.	FUNC :
			case WichLexer.TYPEINT :
			case WichLexer.TYPEFLOAT :
			case WichLexer.TYPESTRING :
			case WichLexer.TYPEBOOLEAN :
			case WichLexer.TRUE :
			case WichLexer.FALSE :
				return new TextAttributesKey[]{DefaultLanguageHighlighterColors.KEYWORD};
			case Token.INVALID_TYPE:
				return new TextAttributesKey[]{HighlighterColors.BAD_CHARACTER};
			default:
				return NO_ATTR;
		}
	}
}
