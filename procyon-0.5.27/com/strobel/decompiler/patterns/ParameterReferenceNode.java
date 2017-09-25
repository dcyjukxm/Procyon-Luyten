package com.strobel.decompiler.patterns;

import com.strobel.decompiler.ast.*;
import com.strobel.decompiler.languages.java.ast.*;

public final class ParameterReferenceNode extends Pattern
{
    private final int _parameterPosition;
    private final String _groupName;
    
    public ParameterReferenceNode(final int parameterPosition) {
        super();
        this._parameterPosition = parameterPosition;
        this._groupName = null;
    }
    
    public ParameterReferenceNode(final int parameterPosition, final String groupName) {
        super();
        this._parameterPosition = parameterPosition;
        this._groupName = groupName;
    }
    
    public final String getGroupName() {
        return this._groupName;
    }
    
    public final int getParameterPosition() {
        return this._parameterPosition;
    }
    
    @Override
    public boolean matches(final INode other, final Match match) {
        if (other instanceof IdentifierExpression) {
            final IdentifierExpression identifier = (IdentifierExpression)other;
            final Variable variable = identifier.getUserData(Keys.VARIABLE);
            if (variable != null && variable.isParameter() && variable.getOriginalParameter().getPosition() == this._parameterPosition) {
                if (this._groupName != null) {
                    match.add(this._groupName, identifier);
                }
                return true;
            }
        }
        return false;
    }
}
