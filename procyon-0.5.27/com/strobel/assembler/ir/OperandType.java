package com.strobel.assembler.ir;

public enum OperandType
{
    None("None", 0, 0), 
    PrimitiveTypeCode("PrimitiveTypeCode", 1, 1), 
    TypeReference("TypeReference", 2, 2), 
    TypeReferenceU1("TypeReferenceU1", 3, 3), 
    DynamicCallSite("DynamicCallSite", 4, 4), 
    MethodReference("MethodReference", 5, 2), 
    FieldReference("FieldReference", 6, 2), 
    BranchTarget("BranchTarget", 7, 2), 
    BranchTargetWide("BranchTargetWide", 8, 4), 
    I1("I1", 9, 1), 
    I2("I2", 10, 2), 
    I8("I8", 11, 8), 
    Constant("Constant", 12, 1), 
    WideConstant("WideConstant", 13, 2), 
    Switch("Switch", 14, -1), 
    Local("Local", 15, 1), 
    LocalI1("LocalI1", 16, 2), 
    LocalI2("LocalI2", 17, 4);
    
    private final int size;
    
    private OperandType(final String param_0, final int param_1, final int size) {
        this.size = size;
    }
    
    public final int getBaseSize() {
        return this.size;
    }
}
