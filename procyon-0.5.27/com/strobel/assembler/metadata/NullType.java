package com.strobel.assembler.metadata;

final class NullType extends TypeDefinition
{
    static final NullType INSTANCE;
    
    static {
        INSTANCE = new NullType();
    }
    
    private NullType() {
        super();
        this.setName("__Null");
    }
    
    @Override
    public String getSimpleName() {
        return "__Null";
    }
    
    @Override
    public String getFullName() {
        return this.getSimpleName();
    }
    
    @Override
    public String getInternalName() {
        return this.getSimpleName();
    }
    
    @Override
    public final <R, P> R accept(final TypeMetadataVisitor<P, R> visitor, final P parameter) {
        return visitor.visitNullType(this, parameter);
    }
}
