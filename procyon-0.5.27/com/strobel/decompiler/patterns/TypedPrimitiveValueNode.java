package com.strobel.decompiler.patterns;

import com.strobel.core.*;
import com.strobel.decompiler.languages.java.ast.*;

public final class TypedPrimitiveValueNode extends Pattern
{
    private final String _groupName;
    private final Class<?> _primitiveType;
    
    public TypedPrimitiveValueNode(final Class<?> primitiveType) {
        super();
        this._groupName = null;
        this._primitiveType = VerifyArgument.notNull(primitiveType, "primitiveType");
    }
    
    public TypedPrimitiveValueNode(final String groupName, final Class<?> primitiveType) {
        super();
        this._groupName = groupName;
        this._primitiveType = VerifyArgument.notNull(primitiveType, "primitiveType");
    }
    
    @Override
    public final boolean matches(final INode other, final Match match) {
        if (other instanceof PrimitiveExpression) {
            final PrimitiveExpression primitive = (PrimitiveExpression)other;
            if (this._primitiveType.isInstance(primitive.getValue())) {
                match.add(this._groupName, other);
                return true;
            }
        }
        return false;
    }
}
