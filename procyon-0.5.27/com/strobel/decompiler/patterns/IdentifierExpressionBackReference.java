package com.strobel.decompiler.patterns;

import com.strobel.decompiler.languages.java.ast.*;
import com.strobel.core.*;

public final class IdentifierExpressionBackReference extends Pattern
{
    private final String _referencedGroupName;
    
    public IdentifierExpressionBackReference(final String referencedGroupName) {
        super();
        this._referencedGroupName = VerifyArgument.notNull(referencedGroupName, "referencedGroupName");
    }
    
    public final String getReferencedGroupName() {
        return this._referencedGroupName;
    }
    
    @Override
    public final boolean matches(final INode other, final Match match) {
        if (other instanceof IdentifierExpression && !CollectionUtilities.any(((IdentifierExpression)other).getTypeArguments())) {
            final INode referenced = CollectionUtilities.lastOrDefault(match.get(this._referencedGroupName));
            return referenced instanceof AstNode && StringUtilities.equals(((IdentifierExpression)other).getIdentifier(), ((AstNode)referenced).getChildByRole(Roles.IDENTIFIER).getName());
        }
        return false;
    }
}
