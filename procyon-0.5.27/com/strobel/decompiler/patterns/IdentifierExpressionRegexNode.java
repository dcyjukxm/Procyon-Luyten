package com.strobel.decompiler.patterns;

import com.strobel.core.*;
import com.strobel.decompiler.languages.java.ast.*;

public final class IdentifierExpressionRegexNode extends Pattern
{
    private final String _groupName;
    private final java.util.regex.Pattern _pattern;
    
    public IdentifierExpressionRegexNode(final String pattern) {
        super();
        this._groupName = null;
        this._pattern = java.util.regex.Pattern.compile(VerifyArgument.notNull(pattern, "pattern"));
    }
    
    public IdentifierExpressionRegexNode(final java.util.regex.Pattern pattern) {
        super();
        this._groupName = null;
        this._pattern = VerifyArgument.notNull(pattern, "pattern");
    }
    
    public IdentifierExpressionRegexNode(final String groupName, final String pattern) {
        super();
        this._groupName = groupName;
        this._pattern = java.util.regex.Pattern.compile(VerifyArgument.notNull(pattern, "pattern"));
    }
    
    public IdentifierExpressionRegexNode(final String groupName, final java.util.regex.Pattern pattern) {
        super();
        this._groupName = groupName;
        this._pattern = VerifyArgument.notNull(pattern, "pattern");
    }
    
    @Override
    public boolean matches(final INode other, final Match match) {
        if (other instanceof IdentifierExpression) {
            final IdentifierExpression identifier = (IdentifierExpression)other;
            if (this._pattern.matcher(identifier.getIdentifier()).matches()) {
                match.add(this._groupName, identifier);
                return true;
            }
        }
        return false;
    }
}
