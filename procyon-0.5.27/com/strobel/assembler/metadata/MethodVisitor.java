package com.strobel.assembler.metadata;

import com.strobel.assembler.ir.*;
import com.strobel.assembler.ir.attributes.*;
import com.strobel.assembler.metadata.annotations.*;

public interface MethodVisitor
{
    boolean canVisitBody();
    
    InstructionVisitor visitBody(MethodBody param_0);
    
    void visitEnd();
    
    void visitFrame(Frame param_0);
    
    void visitLineNumber(Instruction param_0, int param_1);
    
    void visitAttribute(SourceAttribute param_0);
    
    void visitAnnotation(CustomAnnotation param_0, boolean param_1);
    
    void visitParameterAnnotation(int param_0, CustomAnnotation param_1, boolean param_2);
}
