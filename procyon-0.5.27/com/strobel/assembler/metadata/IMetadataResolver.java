package com.strobel.assembler.metadata;

public interface IMetadataResolver
{
    public static final IMetadataResolver EMPTY = new IMetadataResolver() {
        @Override
        public void pushFrame(IResolverFrame frame) {
        }
        
        @Override
        public void popFrame() {
        }
        
        @Override
        public TypeReference lookupType(String descriptor) {
            return null;
        }
        
        @Override
        public TypeDefinition resolve(TypeReference type) {
            return (type instanceof TypeDefinition) ? ((TypeDefinition)type) : null;
        }
        
        @Override
        public FieldDefinition resolve(FieldReference field) {
            return (field instanceof FieldDefinition) ? ((FieldDefinition)field) : null;
        }
        
        @Override
        public MethodDefinition resolve(MethodReference method) {
            return (method instanceof MethodDefinition) ? ((MethodDefinition)method) : null;
        }
    };
    
    void pushFrame(IResolverFrame param_0);
    
    void popFrame();
    
    TypeReference lookupType(String param_0);
    
    TypeDefinition resolve(TypeReference param_0);
    
    FieldDefinition resolve(FieldReference param_0);
    
    MethodDefinition resolve(MethodReference param_0);
}
