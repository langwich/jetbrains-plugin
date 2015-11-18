package org.antlr.jetbrains.wichplugin.parsing;

import org.antlr.v4.runtime.Token;

public class Issue {
	public String annotation;
	public Token offendingToken;

	public Issue(String annotation, Token offendingToken) {
		this.annotation = annotation;
		this.offendingToken = offendingToken;
	}
}
