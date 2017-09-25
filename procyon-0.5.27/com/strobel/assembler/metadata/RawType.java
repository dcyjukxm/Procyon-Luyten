package com.strobel.assembler.metadata;

import com.strobel.core.*;

public final class RawType extends TypeReference
{
    private final TypeReference _genericTypeDefinition;
    
    public RawType(final TypeReference genericTypeDefinition) {
        super();
        this._genericTypeDefinition = VerifyArgument.notNull(genericTypeDefinition, "genericTypeDefinition");
    }
    
    @Override
    public String getFullName() {
        return this._genericTypeDefinition.getFullName();
    }
    
    @Override
    public String getInternalName() {
        return this._genericTypeDefinition.getInternalName();
    }
    
    @Override
    public TypeReference getDeclaringType() {
        return this._genericTypeDefinition.getDeclaringType();
    }
    
    @Override
    public String getSimpleName() {
        return this._genericTypeDefinition.getSimpleName();
    }
    
    @Override
    public String getPackageName() {
        return this._genericTypeDefinition.getPackageName();
    }
    
    @Override
    public String getName() {
        return this._genericTypeDefinition.getName();
    }
    
    @Override
    public TypeReference getUnderlyingType() {
        return this._genericTypeDefinition;
    }
    
    @Override
    public final <R, P> R accept(final TypeMetadataVisitor<P, R> visitor, final P parameter) {
        return visitor.visitRawType(this, parameter);
    }
    
    @Override
    public TypeDefinition resolve() {
        return this.getUnderlyingType().resolve();
    }
}
