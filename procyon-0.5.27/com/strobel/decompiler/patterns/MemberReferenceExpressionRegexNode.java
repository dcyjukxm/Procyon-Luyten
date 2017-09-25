package com.strobel.decompiler.patterns;

import com.strobel.core.*;
import com.strobel.decompiler.languages.java.ast.*;

public final class MemberReferenceExpressionRegexNode extends Pattern
{
    private final String _groupName;
    private final INode _target;
    private final java.util.regex.Pattern _pattern;
    
    public MemberReferenceExpressionRegexNode(final INode target, final String pattern) {
        super();
        this._groupName = null;
        this._target = VerifyArgument.notNull(target, "target");
        this._pattern = java.util.regex.Pattern.compile(VerifyArgument.notNull(pattern, "pattern"));
    }
    
    public MemberReferenceExpressionRegexNode(final INode target, final java.util.regex.Pattern pattern) {
        super();
        this._groupName = null;
        this._target = VerifyArgument.notNull(target, "target");
        this._pattern = VerifyArgument.notNull(pattern, "pattern");
    }
    
    public MemberReferenceExpressionRegexNode(final String groupName, final INode target, final String pattern) {
        super();
        this._groupName = groupName;
        this._target = VerifyArgument.notNull(target, "target");
        this._pattern = java.util.regex.Pattern.compile(VerifyArgument.notNull(pattern, "pattern"));
    }
    
    public MemberReferenceExpressionRegexNode(final String groupName, final INode target, final java.util.regex.Pattern pattern) {
        super();
        this._groupName = groupName;
        this._target = VerifyArgument.notNull(target, "target");
        this._pattern = VerifyArgument.notNull(pattern, "pattern");
    }
    
    @Override
    public boolean matches(final INode other, final Match match) {
        if (other instanceof MemberReferenceExpression) {
            final MemberReferenceExpression reference = (MemberReferenceExpression)other;
            if (this._target.matches(reference.getTarget(), match) && this._pattern.matcher(reference.getMemberName()).matches()) {
                match.add(this._groupName, reference);
                return true;
            }
        }
        return false;
    }
}
