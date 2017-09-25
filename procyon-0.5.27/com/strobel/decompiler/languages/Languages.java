package com.strobel.decompiler.languages;

import java.util.*;
import com.strobel.decompiler.languages.java.*;
import com.strobel.core.*;

public final class Languages
{
    private static final List<Language> ALL_LANGUAGES;
    private static final List<Language> DEBUG_LANGUAGES;
    private static final JavaLanguage JAVA;
    private static final Language BYTECODE_AST_UNOPTIMIZED;
    private static final Language BYTECODE_AST;
    private static final Language BYTECODE;
    
    static {
        final List<BytecodeAstLanguage> bytecodeAstLanguages = BytecodeAstLanguage.getDebugLanguages();
        JAVA = new JavaLanguage();
        BYTECODE = new BytecodeLanguage();
        BYTECODE_AST_UNOPTIMIZED = bytecodeAstLanguages.get(0);
        BYTECODE_AST = new BytecodeAstLanguage();
        final Language[] languages = new Language[bytecodeAstLanguages.size()];
        for (int i = 0; i < languages.length; ++i) {
            languages[i] = bytecodeAstLanguages.get(i);
        }
        ALL_LANGUAGES = ArrayUtilities.asUnmodifiableList(Languages.JAVA, Languages.BYTECODE_AST, Languages.BYTECODE_AST_UNOPTIMIZED);
        DEBUG_LANGUAGES = ArrayUtilities.asUnmodifiableList(languages);
    }
    
    public static List<Language> all() {
        return Languages.ALL_LANGUAGES;
    }
    
    public static List<Language> debug() {
        return Languages.DEBUG_LANGUAGES;
    }
    
    public static JavaLanguage java() {
        return Languages.JAVA;
    }
    
    public static Language bytecode() {
        return Languages.BYTECODE;
    }
    
    public static Language bytecodeAst() {
        return Languages.BYTECODE_AST;
    }
    
    public static Language bytecodeAstUnoptimized() {
        return Languages.BYTECODE_AST_UNOPTIMIZED;
    }
}
