package com.strobel.assembler.metadata;

public abstract class DefaultTypeVisitor<P, R> implements TypeMetadataVisitor<P, R>
{
    public R visit(final TypeReference t) {
        return this.visit(t, null);
    }
    
    public R visit(final TypeReference t, final P p) {
        return t.accept((TypeMetadataVisitor<P, R>)this, p);
    }
    
    @Override
    public R visitType(final TypeReference t, final P p) {
        return null;
    }
    
    @Override
    public R visitArrayType(final ArrayType t, final P p) {
        return this.visitType(t, p);
    }
    
    @Override
    public R visitBottomType(final TypeReference t, final P p) {
        return this.visitType(t, p);
    }
    
    @Override
    public R visitClassType(final TypeReference t, final P p) {
        return this.visitType(t, p);
    }
    
    @Override
    public R visitCompoundType(final CompoundTypeReference t, final P p) {
        return this.visitType(t, p);
    }
    
    @Override
    public R visitGenericParameter(final GenericParameter t, final P p) {
        return this.visitType(t, p);
    }
    
    @Override
    public R visitNullType(final TypeReference t, final P p) {
        return this.visitType(t, p);
    }
    
    @Override
    public R visitParameterizedType(final TypeReference t, final P p) {
        return this.visitClassType(t, p);
    }
    
    @Override
    public R visitPrimitiveType(final PrimitiveType t, final P p) {
        return this.visitType(t, p);
    }
    
    @Override
    public R visitRawType(final RawType t, final P p) {
        return this.visitClassType(t, p);
    }
    
    @Override
    public R visitWildcard(final WildcardType t, final P p) {
        return this.visitType(t, p);
    }
    
    @Override
    public R visitCapturedType(final CapturedType t, final P p) {
        return this.visitType(t, p);
    }
}
