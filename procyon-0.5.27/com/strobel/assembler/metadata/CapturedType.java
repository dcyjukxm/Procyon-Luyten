package com.strobel.assembler.metadata;

import com.strobel.core.*;

public final class CapturedType extends TypeReference implements ICapturedType
{
    private final TypeReference _superBound;
    private final TypeReference _extendsBound;
    private final WildcardType _wildcard;
    
    CapturedType(final TypeReference superBound, final TypeReference extendsBound, final WildcardType wildcard) {
        super();
        this._superBound = ((superBound != null) ? superBound : BuiltinTypes.Bottom);
        this._extendsBound = ((extendsBound != null) ? extendsBound : BuiltinTypes.Object);
        this._wildcard = VerifyArgument.notNull(wildcard, "wildcard");
    }
    
    @Override
    public final WildcardType getWildcard() {
        return this._wildcard;
    }
    
    @Override
    public final TypeReference getExtendsBound() {
        return this._extendsBound;
    }
    
    @Override
    public final TypeReference getSuperBound() {
        return this._superBound;
    }
    
    @Override
    public final boolean hasExtendsBound() {
        return this._extendsBound != null && !MetadataHelper.isSameType(this._extendsBound, BuiltinTypes.Object);
    }
    
    @Override
    public final boolean hasSuperBound() {
        return this._superBound != BuiltinTypes.Bottom;
    }
    
    @Override
    public final boolean isBoundedType() {
        return true;
    }
    
    @Override
    public String getSimpleName() {
        return "capture of " + this._wildcard.getSimpleName();
    }
    
    @Override
    public final <R, P> R accept(final TypeMetadataVisitor<P, R> visitor, final P parameter) {
        return visitor.visitCapturedType(this, parameter);
    }
    
    @Override
    protected final StringBuilder appendName(final StringBuilder sb, final boolean fullName, final boolean dottedName) {
        return this._wildcard.appendName(sb.append("capture of "), fullName, dottedName);
    }
}
