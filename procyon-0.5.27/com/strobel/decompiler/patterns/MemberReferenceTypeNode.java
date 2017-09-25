package com.strobel.decompiler.patterns;

import com.strobel.assembler.metadata.*;
import com.strobel.core.*;
import com.strobel.decompiler.languages.java.ast.*;

public final class MemberReferenceTypeNode extends Pattern
{
    private final String _groupName;
    private final INode _target;
    private final Class<? extends MemberReference> _referenceType;
    
    public MemberReferenceTypeNode(final INode target, final Class<? extends MemberReference> referenceType) {
        super();
        this._groupName = null;
        this._target = VerifyArgument.notNull(target, "target");
        this._referenceType = VerifyArgument.notNull(referenceType, "referenceType");
    }
    
    public MemberReferenceTypeNode(final String groupName, final INode target, final Class<? extends MemberReference> referenceType) {
        super();
        this._groupName = groupName;
        this._target = VerifyArgument.notNull(target, "target");
        this._referenceType = VerifyArgument.notNull(referenceType, "referenceType");
    }
    
    public final String getGroupName() {
        return this._groupName;
    }
    
    public final Class<? extends MemberReference> getReferenceType() {
        return this._referenceType;
    }
    
    public final INode getTarget() {
        return this._target;
    }
    
    @Override
    public boolean matches(final INode other, final Match match) {
        if (other instanceof AstNode) {
            final AstNode reference = (AstNode)other;
            final MemberReference memberReference = reference.getUserData(Keys.MEMBER_REFERENCE);
            if (this._target.matches(reference, match) && this._referenceType.isInstance(memberReference)) {
                match.add(this._groupName, reference);
                return true;
            }
        }
        return false;
    }
}
