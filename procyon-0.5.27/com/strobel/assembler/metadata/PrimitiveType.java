package com.strobel.assembler.metadata;

import com.strobel.core.*;

public final class PrimitiveType extends TypeDefinition
{
    private final JvmType _jvmType;
    
    PrimitiveType(final JvmType jvmType) {
        super(MetadataSystem.instance());
        this._jvmType = VerifyArgument.notNull(jvmType, "jvmType");
        this.setFlags(1L);
        this.setName(this._jvmType.getPrimitiveName());
    }
    
    @Override
    public String getInternalName() {
        return this._jvmType.getDescriptorPrefix();
    }
    
    @Override
    public final <R, P> R accept(final TypeMetadataVisitor<P, R> visitor, final P parameter) {
        return visitor.visitPrimitiveType(this, parameter);
    }
    
    @Override
    public String getSimpleName() {
        return this._jvmType.getPrimitiveName();
    }
    
    @Override
    public String getFullName() {
        return this._jvmType.getPrimitiveName();
    }
    
    @Override
    public final boolean isPrimitive() {
        return true;
    }
    
    @Override
    public final boolean isVoid() {
        return this._jvmType == JvmType.Void;
    }
    
    @Override
    public final JvmType getSimpleType() {
        return this._jvmType;
    }
    
    @Override
    protected StringBuilder appendName(final StringBuilder sb, final boolean fullName, final boolean dottedName) {
        return sb.append(this._jvmType.getPrimitiveName());
    }
    
    @Override
    protected StringBuilder appendBriefDescription(final StringBuilder sb) {
        return sb.append(this._jvmType.getPrimitiveName());
    }
    
    @Override
    protected StringBuilder appendSimpleDescription(final StringBuilder sb) {
        return sb.append(this._jvmType.getPrimitiveName());
    }
    
    @Override
    protected StringBuilder appendErasedDescription(final StringBuilder sb) {
        return sb.append(this._jvmType.getPrimitiveName());
    }
    
    @Override
    protected StringBuilder appendClassDescription(final StringBuilder sb) {
        return sb.append(this._jvmType.getPrimitiveName());
    }
    
    @Override
    protected StringBuilder appendSignature(final StringBuilder sb) {
        return sb.append(this._jvmType.getDescriptorPrefix());
    }
    
    @Override
    protected StringBuilder appendErasedSignature(final StringBuilder sb) {
        return sb.append(this._jvmType.getDescriptorPrefix());
    }
    
    @Override
    protected StringBuilder appendClassSignature(final StringBuilder sb) {
        return sb.append(this._jvmType.getDescriptorPrefix());
    }
    
    @Override
    protected StringBuilder appendErasedClassSignature(final StringBuilder sb) {
        return sb.append(this._jvmType.getDescriptorPrefix());
    }
    
    public StringBuilder appendGenericSignature(final StringBuilder sb) {
        return sb.append(this._jvmType.getDescriptorPrefix());
    }
}
