package com.strobel.assembler.metadata.signatures;

public interface TypeTreeVisitor<T>
{
    T getResult();
    
    void visitFormalTypeParameter(FormalTypeParameter param_0);
    
    void visitClassTypeSignature(ClassTypeSignature param_0);
    
    void visitArrayTypeSignature(ArrayTypeSignature param_0);
    
    void visitTypeVariableSignature(TypeVariableSignature param_0);
    
    void visitWildcard(Wildcard param_0);
    
    void visitSimpleClassTypeSignature(SimpleClassTypeSignature param_0);
    
    void visitBottomSignature(BottomSignature param_0);
    
    void visitByteSignature(ByteSignature param_0);
    
    void visitBooleanSignature(BooleanSignature param_0);
    
    void visitShortSignature(ShortSignature param_0);
    
    void visitCharSignature(CharSignature param_0);
    
    void visitIntSignature(IntSignature param_0);
    
    void visitLongSignature(LongSignature param_0);
    
    void visitFloatSignature(FloatSignature param_0);
    
    void visitDoubleSignature(DoubleSignature param_0);
    
    void visitVoidSignature(VoidSignature param_0);
}
