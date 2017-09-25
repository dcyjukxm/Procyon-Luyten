package com.strobel.assembler.metadata;

public final class WildcardType extends TypeReference
{
    private static final WildcardType UNBOUNDED;
    private final TypeReference _bound;
    private final boolean _hasSuperBound;
    private String _name;
    
    static {
        UNBOUNDED = new WildcardType(BuiltinTypes.Object, BuiltinTypes.Bottom);
    }
    
    private WildcardType(final TypeReference extendsBound, final TypeReference superBound) {
        super();
        this._hasSuperBound = (superBound != BuiltinTypes.Bottom);
        this._bound = (this._hasSuperBound ? superBound : extendsBound);
    }
    
    @Override
    public TypeReference getDeclaringType() {
        return null;
    }
    
    @Override
    public String getSimpleName() {
        return this._name;
    }
    
    @Override
    public JvmType getSimpleType() {
        return JvmType.Wildcard;
    }
    
    @Override
    public boolean containsGenericParameters() {
        if (this.hasSuperBound()) {
            return this.getSuperBound().containsGenericParameters();
        }
        return this.hasExtendsBound() && this.getExtendsBound().containsGenericParameters();
    }
    
    @Override
    public String getName() {
        if (this._name == null) {
            this._name = this.appendSimpleDescription(new StringBuilder()).toString();
        }
        return this._name;
    }
    
    @Override
    public String getFullName() {
        return this.getName();
    }
    
    @Override
    public String getInternalName() {
        return this.getName();
    }
    
    @Override
    public final <R, P> R accept(final TypeMetadataVisitor<P, R> visitor, final P parameter) {
        return visitor.visitWildcard(this, parameter);
    }
    
    @Override
    public boolean isWildcardType() {
        return true;
    }
    
    @Override
    public boolean isBoundedType() {
        return true;
    }
    
    @Override
    public boolean isUnbounded() {
        return this._bound == null || (!this._hasSuperBound && BuiltinTypes.Object.equals(this._bound));
    }
    
    @Override
    public boolean hasExtendsBound() {
        return !this._hasSuperBound && this._bound != null && !BuiltinTypes.Object.equals(this._bound);
    }
    
    @Override
    public boolean hasSuperBound() {
        return this._hasSuperBound;
    }
    
    @Override
    public TypeReference getSuperBound() {
        return this._hasSuperBound ? this._bound : BuiltinTypes.Bottom;
    }
    
    @Override
    public TypeReference getExtendsBound() {
        return this._hasSuperBound ? BuiltinTypes.Object : this._bound;
    }
    
    @Override
    protected StringBuilder appendName(final StringBuilder sb, final boolean fullName, final boolean dottedName) {
        return this.appendSimpleDescription(sb);
    }
    
    public StringBuilder appendSignature(final StringBuilder sb) {
        if (this.isUnbounded()) {
            return sb.append('*');
        }
        if (this.hasSuperBound()) {
            return this._bound.appendSignature(sb.append('-'));
        }
        return this._bound.appendSignature(sb.append('+'));
    }
    
    public StringBuilder appendBriefDescription(final StringBuilder sb) {
        if (this.isUnbounded()) {
            return sb.append("?");
        }
        if (this.hasSuperBound()) {
            sb.append("? super ");
            if (this._bound.isGenericParameter()) {
                return sb.append(this._bound.getFullName());
            }
            return this._bound.appendErasedDescription(sb);
        }
        else {
            sb.append("? extends ");
            if (this._bound.isGenericParameter()) {
                return sb.append(this._bound.getFullName());
            }
            return this._bound.appendErasedDescription(sb);
        }
    }
    
    public StringBuilder appendSimpleDescription(final StringBuilder sb) {
        if (this.isUnbounded()) {
            return sb.append("?");
        }
        if (this.hasSuperBound()) {
            sb.append("? super ");
            if (this._bound.isGenericParameter() || this._bound.isWildcardType()) {
                return sb.append(this._bound.getSimpleName());
            }
            return this._bound.appendSimpleDescription(sb);
        }
        else {
            sb.append("? extends ");
            if (this._bound.isGenericParameter() || this._bound.isWildcardType()) {
                return sb.append(this._bound.getSimpleName());
            }
            return this._bound.appendSimpleDescription(sb);
        }
    }
    
    public StringBuilder appendErasedDescription(final StringBuilder sb) {
        return this.appendBriefDescription(sb);
    }
    
    public StringBuilder appendDescription(final StringBuilder sb) {
        return this.appendBriefDescription(sb);
    }
    
    public static WildcardType unbounded() {
        return WildcardType.UNBOUNDED;
    }
    
    public static WildcardType makeSuper(final TypeReference superBound) {
        return new WildcardType(BuiltinTypes.Object, superBound);
    }
    
    public static WildcardType makeExtends(final TypeReference extendsBound) {
        return new WildcardType(extendsBound, BuiltinTypes.Bottom);
    }
}
