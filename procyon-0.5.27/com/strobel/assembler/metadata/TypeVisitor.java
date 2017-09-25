package com.strobel.assembler.metadata;

import com.strobel.assembler.ir.attributes.*;
import com.strobel.assembler.metadata.annotations.*;
import com.strobel.assembler.ir.*;

public interface TypeVisitor
{
    void visitParser(MetadataParser param_0);
    
    void visit(int param_0, int param_1, long param_2, String param_3, String param_4, String param_5, String[] param_6);
    
    void visitDeclaringMethod(MethodReference param_0);
    
    void visitOuterType(TypeReference param_0);
    
    void visitInnerType(TypeDefinition param_0);
    
    void visitAttribute(SourceAttribute param_0);
    
    void visitAnnotation(CustomAnnotation param_0, boolean param_1);
    
    FieldVisitor visitField(long param_0, String param_1, TypeReference param_2);
    
    MethodVisitor visitMethod(long param_0, String param_1, IMethodSignature param_2, TypeReference... param_3);
    
    ConstantPool.Visitor visitConstantPool();
    
    void visitEnd();
}
