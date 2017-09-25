package com.strobel.assembler.ir;

import com.strobel.assembler.metadata.*;

public interface InstructionVisitor
{
    public static final InstructionVisitor EMPTY = new InstructionVisitor() {
        @Override
        public void visit(Instruction instruction) {
        }
        
        @Override
        public void visit(OpCode opCode) {
        }
        
        @Override
        public void visitConstant(OpCode opCode, TypeReference value) {
        }
        
        @Override
        public void visitConstant(OpCode opCode, int value) {
        }
        
        @Override
        public void visitConstant(OpCode opCode, long value) {
        }
        
        @Override
        public void visitConstant(OpCode opCode, float value) {
        }
        
        @Override
        public void visitConstant(OpCode opCode, double value) {
        }
        
        @Override
        public void visitConstant(OpCode opCode, String value) {
        }
        
        @Override
        public void visitBranch(OpCode opCode, Instruction target) {
        }
        
        @Override
        public void visitVariable(OpCode opCode, VariableReference variable) {
        }
        
        @Override
        public void visitVariable(OpCode opCode, VariableReference variable, int operand) {
        }
        
        @Override
        public void visitType(OpCode opCode, TypeReference type) {
        }
        
        @Override
        public void visitMethod(OpCode opCode, MethodReference method) {
        }
        
        @Override
        public void visitDynamicCallSite(OpCode opCode, DynamicCallSite callSite) {
        }
        
        @Override
        public void visitField(OpCode opCode, FieldReference field) {
        }
        
        @Override
        public void visitLabel(Label label) {
        }
        
        @Override
        public void visitSwitch(OpCode opCode, SwitchInfo switchInfo) {
        }
        
        @Override
        public void visitEnd() {
        }
    };
    
    void visit(Instruction param_0);
    
    void visit(OpCode param_0);
    
    void visitConstant(OpCode param_0, TypeReference param_1);
    
    void visitConstant(OpCode param_0, int param_1);
    
    void visitConstant(OpCode param_0, long param_1);
    
    void visitConstant(OpCode param_0, float param_1);
    
    void visitConstant(OpCode param_0, double param_1);
    
    void visitConstant(OpCode param_0, String param_1);
    
    void visitBranch(OpCode param_0, Instruction param_1);
    
    void visitVariable(OpCode param_0, VariableReference param_1);
    
    void visitVariable(OpCode param_0, VariableReference param_1, int param_2);
    
    void visitType(OpCode param_0, TypeReference param_1);
    
    void visitMethod(OpCode param_0, MethodReference param_1);
    
    void visitDynamicCallSite(OpCode param_0, DynamicCallSite param_1);
    
    void visitField(OpCode param_0, FieldReference param_1);
    
    void visitLabel(Label param_0);
    
    void visitSwitch(OpCode param_0, SwitchInfo param_1);
    
    void visitEnd();
}
