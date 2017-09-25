package com.strobel.assembler.metadata;

import com.strobel.assembler.ir.attributes.*;
import com.strobel.assembler.metadata.annotations.*;

public interface FieldVisitor
{
    public static final FieldVisitor EMPTY = new FieldVisitor() {
        @Override
        public void visitAttribute(SourceAttribute attribute) {
        }
        
        @Override
        public void visitAnnotation(CustomAnnotation annotation, boolean visible) {
        }
        
        @Override
        public void visitEnd() {
        }
    };
    
    void visitAttribute(SourceAttribute param_0);
    
    void visitAnnotation(CustomAnnotation param_0, boolean param_1);
    
    void visitEnd();
}
