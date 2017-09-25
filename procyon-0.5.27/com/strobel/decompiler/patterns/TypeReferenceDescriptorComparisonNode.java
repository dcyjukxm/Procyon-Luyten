package com.strobel.decompiler.patterns;

import com.strobel.assembler.metadata.*;
import com.strobel.decompiler.languages.java.ast.*;
import com.strobel.core.*;

public final class TypeReferenceDescriptorComparisonNode extends Pattern
{
    private final String _descriptor;
    
    public TypeReferenceDescriptorComparisonNode(final String descriptor) {
        super();
        this._descriptor = VerifyArgument.notNull(descriptor, "descriptor");
    }
    
    @Override
    public boolean matches(final INode other, final Match match) {
        if (other instanceof TypeReferenceExpression) {
            final TypeReferenceExpression typeReferenceExpression = (TypeReferenceExpression)other;
            final TypeReference typeReference = typeReferenceExpression.getType().getUserData(Keys.TYPE_REFERENCE);
            return typeReference != null && StringUtilities.equals(this._descriptor, typeReference.getInternalName());
        }
        return false;
    }
}
