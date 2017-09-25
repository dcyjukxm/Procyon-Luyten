package com.strobel.decompiler.patterns;

import com.strobel.core.*;
import com.strobel.decompiler.languages.java.ast.*;

public final class DeclaredVariableBackReference extends Pattern
{
    private final String _referencedGroupName;
    
    public DeclaredVariableBackReference(final String referencedGroupName) {
        super();
        this._referencedGroupName = VerifyArgument.notNull(referencedGroupName, "referencedGroupName");
    }
    
    public final String getReferencedGroupName() {
        return this._referencedGroupName;
    }
    
    @Override
    public final boolean matches(final INode other, final Match match) {
        if (other instanceof AstNode) {
            final INode lastInGroup = CollectionUtilities.lastOrDefault(match.get(this._referencedGroupName));
            if (lastInGroup instanceof VariableDeclarationStatement) {
                final VariableDeclarationStatement referenced = (VariableDeclarationStatement)lastInGroup;
                final AstNodeCollection<VariableInitializer> variables = referenced.getVariables();
                return variables.hasSingleElement() && Pattern.matchString(variables.firstOrNullObject().getName(), ((AstNode)other).getChildByRole(Roles.IDENTIFIER).getName());
            }
        }
        return false;
    }
}
