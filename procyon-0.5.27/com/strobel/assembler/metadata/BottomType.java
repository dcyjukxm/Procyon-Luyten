package com.strobel.assembler.metadata;

final class BottomType extends TypeDefinition
{
    static final BottomType INSTANCE;
    
    static {
        INSTANCE = new BottomType();
    }
    
    private BottomType() {
        super();
        this.setName("__Bottom");
    }
    
    @Override
    public String getSimpleName() {
        return "__Bottom";
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
        return visitor.visitBottomType(this, parameter);
    }
}
