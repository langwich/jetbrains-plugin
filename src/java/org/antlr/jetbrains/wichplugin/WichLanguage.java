package org.antlr.jetbrains.wichplugin;

import com.intellij.lang.Language;

public class WichLanguage extends Language {
    public static final WichLanguage INSTANCE = new WichLanguage();

    private WichLanguage() {
        super("Wich");
    }
}
