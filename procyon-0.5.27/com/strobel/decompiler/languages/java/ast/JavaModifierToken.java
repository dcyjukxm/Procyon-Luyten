package com.strobel.decompiler.languages.java.ast;

import java.util.*;
import javax.lang.model.element.*;
import com.strobel.core.*;
import com.strobel.decompiler.languages.*;
import com.strobel.decompiler.languages.java.*;
import com.strobel.decompiler.patterns.*;

public class JavaModifierToken extends JavaTokenNode
{
    private static final List<Modifier> ALL_MODIFIERS;
    private Modifier _modifier;
    
    static {
        ALL_MODIFIERS = ArrayUtilities.asUnmodifiableList(Modifier.values());
    }
    
    public static List<Modifier> allModifiers() {
        return JavaModifierToken.ALL_MODIFIERS;
    }
    
    public JavaModifierToken(final Modifier modifier) {
        this(TextLocation.EMPTY, modifier);
    }
    
    public JavaModifierToken(final TextLocation startLocation, final Modifier modifier) {
        super(startLocation);
        this._modifier = modifier;
    }
    
    public final Modifier getModifier() {
        return this._modifier;
    }
    
    public final void setModifier(final Modifier modifier) {
        this.verifyNotFrozen();
        this._modifier = modifier;
    }
    
    public static String getModifierName(final Modifier modifier) {
        return String.valueOf(modifier);
    }
    
    @Override
    public String getText(final JavaFormattingOptions options) {
        return getModifierName(this._modifier);
    }
    
    @Override
    protected int getTokenLength() {
        return getModifierName(this._modifier).length();
    }
    
    @Override
    public boolean matches(final INode other, final Match match) {
        return other instanceof JavaModifierToken && ((JavaModifierToken)other)._modifier == this._modifier;
    }
}
