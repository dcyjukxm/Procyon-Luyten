package com.strobel.assembler.ir;

public enum OpCode
{
    NOP("NOP", 0, 0, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.Pop0, StackBehavior.Push0), 
    ACONST_NULL("ACONST_NULL", 1, 1, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.Pop0, StackBehavior.PushA), 
    ICONST_M1("ICONST_M1", 2, 2, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.Pop0, StackBehavior.PushI4), 
    ICONST_0("ICONST_0", 3, 3, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.Pop0, StackBehavior.PushI4), 
    ICONST_1("ICONST_1", 4, 4, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.Pop0, StackBehavior.PushI4), 
    ICONST_2("ICONST_2", 5, 5, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.Pop0, StackBehavior.PushI4), 
    ICONST_3("ICONST_3", 6, 6, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.Pop0, StackBehavior.PushI4), 
    ICONST_4("ICONST_4", 7, 7, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.Pop0, StackBehavior.PushI4), 
    ICONST_5("ICONST_5", 8, 8, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.Pop0, StackBehavior.PushI4), 
    LCONST_0("LCONST_0", 9, 9, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.Pop0, StackBehavior.PushI8), 
    LCONST_1("LCONST_1", 10, 10, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.Pop0, StackBehavior.PushI8), 
    FCONST_0("FCONST_0", 11, 11, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.Pop0, StackBehavior.PushR4), 
    FCONST_1("FCONST_1", 12, 12, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.Pop0, StackBehavior.PushR4), 
    FCONST_2("FCONST_2", 13, 13, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.Pop0, StackBehavior.PushR4), 
    DCONST_0("DCONST_0", 14, 14, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.Pop0, StackBehavior.PushR8), 
    DCONST_1("DCONST_1", 15, 15, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.Pop0, StackBehavior.PushR8), 
    BIPUSH("BIPUSH", 16, 16, FlowControl.Next, OpCodeType.Primitive, OperandType.I1, StackBehavior.Pop0, StackBehavior.PushI4), 
    SIPUSH("SIPUSH", 17, 17, FlowControl.Next, OpCodeType.Primitive, OperandType.I2, StackBehavior.Pop0, StackBehavior.PushI4), 
    LDC("LDC", 18, 18, FlowControl.Next, OpCodeType.Primitive, OperandType.Constant, StackBehavior.Pop0, StackBehavior.Push1), 
    LDC_W("LDC_W", 19, 19, FlowControl.Next, OpCodeType.Primitive, OperandType.WideConstant, StackBehavior.Pop0, StackBehavior.Push1), 
    LDC2_W("LDC2_W", 20, 20, FlowControl.Next, OpCodeType.Primitive, OperandType.WideConstant, StackBehavior.Pop0, StackBehavior.Push2), 
    ILOAD("ILOAD", 21, 21, FlowControl.Next, OpCodeType.Primitive, OperandType.Local, StackBehavior.Pop0, StackBehavior.PushI4), 
    LLOAD("LLOAD", 22, 22, FlowControl.Next, OpCodeType.Primitive, OperandType.Local, StackBehavior.Pop0, StackBehavior.PushI8), 
    FLOAD("FLOAD", 23, 23, FlowControl.Next, OpCodeType.Primitive, OperandType.Local, StackBehavior.Pop0, StackBehavior.PushR4), 
    DLOAD("DLOAD", 24, 24, FlowControl.Next, OpCodeType.Primitive, OperandType.Local, StackBehavior.Pop0, StackBehavior.PushR8), 
    ALOAD("ALOAD", 25, 25, FlowControl.Next, OpCodeType.Primitive, OperandType.Local, StackBehavior.Pop0, StackBehavior.PushA), 
    ILOAD_0("ILOAD_0", 26, 26, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.Pop0, StackBehavior.PushI4), 
    ILOAD_1("ILOAD_1", 27, 27, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.Pop0, StackBehavior.PushI4), 
    ILOAD_2("ILOAD_2", 28, 28, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.Pop0, StackBehavior.PushI4), 
    ILOAD_3("ILOAD_3", 29, 29, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.Pop0, StackBehavior.PushI4), 
    LLOAD_0("LLOAD_0", 30, 30, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.Pop0, StackBehavior.PushI8), 
    LLOAD_1("LLOAD_1", 31, 31, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.Pop0, StackBehavior.PushI8), 
    LLOAD_2("LLOAD_2", 32, 32, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.Pop0, StackBehavior.PushI8), 
    LLOAD_3("LLOAD_3", 33, 33, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.Pop0, StackBehavior.PushI8), 
    FLOAD_0("FLOAD_0", 34, 34, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.Pop0, StackBehavior.PushR4), 
    FLOAD_1("FLOAD_1", 35, 35, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.Pop0, StackBehavior.PushR4), 
    FLOAD_2("FLOAD_2", 36, 36, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.Pop0, StackBehavior.PushR4), 
    FLOAD_3("FLOAD_3", 37, 37, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.Pop0, StackBehavior.PushR4), 
    DLOAD_0("DLOAD_0", 38, 38, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.Pop0, StackBehavior.PushR8), 
    DLOAD_1("DLOAD_1", 39, 39, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.Pop0, StackBehavior.PushR8), 
    DLOAD_2("DLOAD_2", 40, 40, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.Pop0, StackBehavior.PushR8), 
    DLOAD_3("DLOAD_3", 41, 41, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.Pop0, StackBehavior.PushR8), 
    ALOAD_0("ALOAD_0", 42, 42, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.Pop0, StackBehavior.PushA), 
    ALOAD_1("ALOAD_1", 43, 43, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.Pop0, StackBehavior.PushA), 
    ALOAD_2("ALOAD_2", 44, 44, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.Pop0, StackBehavior.PushA), 
    ALOAD_3("ALOAD_3", 45, 45, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.Pop0, StackBehavior.PushA), 
    IALOAD("IALOAD", 46, 46, FlowControl.Next, OpCodeType.ObjectModel, OperandType.None, StackBehavior.PopI4_PopA, StackBehavior.PushI4), 
    LALOAD("LALOAD", 47, 47, FlowControl.Next, OpCodeType.ObjectModel, OperandType.None, StackBehavior.PopI4_PopA, StackBehavior.PushI8), 
    FALOAD("FALOAD", 48, 48, FlowControl.Next, OpCodeType.ObjectModel, OperandType.None, StackBehavior.PopI4_PopA, StackBehavior.PushR4), 
    DALOAD("DALOAD", 49, 49, FlowControl.Next, OpCodeType.ObjectModel, OperandType.None, StackBehavior.PopI4_PopA, StackBehavior.PushR8), 
    AALOAD("AALOAD", 50, 50, FlowControl.Next, OpCodeType.ObjectModel, OperandType.None, StackBehavior.PopI4_PopA, StackBehavior.PushA), 
    BALOAD("BALOAD", 51, 51, FlowControl.Next, OpCodeType.ObjectModel, OperandType.None, StackBehavior.PopI4_PopA, StackBehavior.PushI4), 
    CALOAD("CALOAD", 52, 52, FlowControl.Next, OpCodeType.ObjectModel, OperandType.None, StackBehavior.PopI4_PopA, StackBehavior.PushI4), 
    SALOAD("SALOAD", 53, 53, FlowControl.Next, OpCodeType.ObjectModel, OperandType.None, StackBehavior.PopI4_PopA, StackBehavior.PushI4), 
    ISTORE("ISTORE", 54, 54, FlowControl.Next, OpCodeType.Primitive, OperandType.Local, StackBehavior.PopI4, StackBehavior.Push0), 
    LSTORE("LSTORE", 55, 55, FlowControl.Next, OpCodeType.Primitive, OperandType.Local, StackBehavior.PopI8, StackBehavior.Push0), 
    FSTORE("FSTORE", 56, 56, FlowControl.Next, OpCodeType.Primitive, OperandType.Local, StackBehavior.PopR4, StackBehavior.Push0), 
    DSTORE("DSTORE", 57, 57, FlowControl.Next, OpCodeType.Primitive, OperandType.Local, StackBehavior.PopR8, StackBehavior.Push0), 
    ASTORE("ASTORE", 58, 58, FlowControl.Next, OpCodeType.Primitive, OperandType.Local, StackBehavior.PopA, StackBehavior.Push0), 
    ISTORE_0("ISTORE_0", 59, 59, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.PopI4, StackBehavior.Push0), 
    ISTORE_1("ISTORE_1", 60, 60, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.PopI4, StackBehavior.Push0), 
    ISTORE_2("ISTORE_2", 61, 61, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.PopI4, StackBehavior.Push0), 
    ISTORE_3("ISTORE_3", 62, 62, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.PopI4, StackBehavior.Push0), 
    LSTORE_0("LSTORE_0", 63, 63, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.PopI8, StackBehavior.Push0), 
    LSTORE_1("LSTORE_1", 64, 64, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.PopI8, StackBehavior.Push0), 
    LSTORE_2("LSTORE_2", 65, 65, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.PopI8, StackBehavior.Push0), 
    LSTORE_3("LSTORE_3", 66, 66, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.PopI8, StackBehavior.Push0), 
    FSTORE_0("FSTORE_0", 67, 67, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.PopR4, StackBehavior.Push0), 
    FSTORE_1("FSTORE_1", 68, 68, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.PopR4, StackBehavior.Push0), 
    FSTORE_2("FSTORE_2", 69, 69, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.PopR4, StackBehavior.Push0), 
    FSTORE_3("FSTORE_3", 70, 70, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.PopR4, StackBehavior.Push0), 
    DSTORE_0("DSTORE_0", 71, 71, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.PopR8, StackBehavior.Push0), 
    DSTORE_1("DSTORE_1", 72, 72, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.PopR8, StackBehavior.Push0), 
    DSTORE_2("DSTORE_2", 73, 73, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.PopR8, StackBehavior.Push0), 
    DSTORE_3("DSTORE_3", 74, 74, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.PopR8, StackBehavior.Push0), 
    ASTORE_0("ASTORE_0", 75, 75, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.PopA, StackBehavior.Push0), 
    ASTORE_1("ASTORE_1", 76, 76, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.PopA, StackBehavior.Push0), 
    ASTORE_2("ASTORE_2", 77, 77, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.PopA, StackBehavior.Push0), 
    ASTORE_3("ASTORE_3", 78, 78, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.PopA, StackBehavior.Push0), 
    IASTORE("IASTORE", 79, 79, FlowControl.Next, OpCodeType.ObjectModel, OperandType.None, StackBehavior.PopI4_PopI4_PopA, StackBehavior.Push0), 
    LASTORE("LASTORE", 80, 80, FlowControl.Next, OpCodeType.ObjectModel, OperandType.None, StackBehavior.PopI8_PopI4_PopA, StackBehavior.Push0), 
    FASTORE("FASTORE", 81, 81, FlowControl.Next, OpCodeType.ObjectModel, OperandType.None, StackBehavior.PopR4_PopI4_PopA, StackBehavior.Push0), 
    DASTORE("DASTORE", 82, 82, FlowControl.Next, OpCodeType.ObjectModel, OperandType.None, StackBehavior.PopR8_PopI4_PopA, StackBehavior.Push0), 
    AASTORE("AASTORE", 83, 83, FlowControl.Next, OpCodeType.ObjectModel, OperandType.None, StackBehavior.PopA_PopI4_PopA, StackBehavior.Push0), 
    BASTORE("BASTORE", 84, 84, FlowControl.Next, OpCodeType.ObjectModel, OperandType.None, StackBehavior.PopI4_PopI4_PopA, StackBehavior.Push0), 
    CASTORE("CASTORE", 85, 85, FlowControl.Next, OpCodeType.ObjectModel, OperandType.None, StackBehavior.PopI4_PopI4_PopA, StackBehavior.Push0), 
    SASTORE("SASTORE", 86, 86, FlowControl.Next, OpCodeType.ObjectModel, OperandType.None, StackBehavior.PopI4_PopI4_PopA, StackBehavior.Push0), 
    POP("POP", 87, 87, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.Pop1, StackBehavior.Push0), 
    POP2("POP2", 88, 88, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.Pop2, StackBehavior.Push0), 
    DUP("DUP", 89, 89, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.Pop1, StackBehavior.Push1_Push1), 
    DUP_X1("DUP_X1", 90, 90, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.Pop1_Pop1, StackBehavior.Push1_Push1_Push1), 
    DUP_X2("DUP_X2", 91, 91, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.Pop2_Pop1, StackBehavior.Push1_Push2_Push1), 
    DUP2("DUP2", 92, 92, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.Pop2, StackBehavior.Push2_Push2), 
    DUP2_X1("DUP2_X1", 93, 93, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.Pop1_Pop2, StackBehavior.Push2_Push1_Push2), 
    DUP2_X2("DUP2_X2", 94, 94, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.Pop2_Pop2, StackBehavior.Push2_Push2_Push2), 
    SWAP("SWAP", 95, 95, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.Pop1_Pop1, StackBehavior.Push1_Push1), 
    IADD("IADD", 96, 96, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopI4_PopI4, StackBehavior.PushI4), 
    LADD("LADD", 97, 97, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopI8_PopI8, StackBehavior.PushI8), 
    FADD("FADD", 98, 98, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopR4_PopR4, StackBehavior.PushR4), 
    DADD("DADD", 99, 99, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopR8_PopR8, StackBehavior.PushR8), 
    ISUB("ISUB", 100, 100, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopI4_PopI4, StackBehavior.PushI4), 
    LSUB("LSUB", 101, 101, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopI8_PopI8, StackBehavior.PushI8), 
    FSUB("FSUB", 102, 102, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopR4_PopR4, StackBehavior.PushR4), 
    DSUB("DSUB", 103, 103, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopR8_PopR8, StackBehavior.PushR8), 
    IMUL("IMUL", 104, 104, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopI4_PopI4, StackBehavior.PushI4), 
    LMUL("LMUL", 105, 105, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopI8_PopI8, StackBehavior.PushI8), 
    FMUL("FMUL", 106, 106, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopR4_PopR4, StackBehavior.PushR4), 
    DMUL("DMUL", 107, 107, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopR8_PopR8, StackBehavior.PushR8), 
    IDIV("IDIV", 108, 108, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopI4_PopI4, StackBehavior.PushI4), 
    LDIV("LDIV", 109, 109, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopI8_PopI8, StackBehavior.PushI8), 
    FDIV("FDIV", 110, 110, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopR4_PopR4, StackBehavior.PushR4), 
    DDIV("DDIV", 111, 111, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopR8_PopR8, StackBehavior.PushR8), 
    IREM("IREM", 112, 112, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopI4_PopI4, StackBehavior.PushI4), 
    LREM("LREM", 113, 113, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopI8_PopI8, StackBehavior.PushI8), 
    FREM("FREM", 114, 114, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopR4_PopR4, StackBehavior.PushR4), 
    DREM("DREM", 115, 115, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopR8_PopR8, StackBehavior.PushR8), 
    INEG("INEG", 116, 116, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopI4, StackBehavior.PushI4), 
    LNEG("LNEG", 117, 117, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopI8, StackBehavior.PushI8), 
    FNEG("FNEG", 118, 118, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopR4, StackBehavior.PushR4), 
    DNEG("DNEG", 119, 119, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopR8, StackBehavior.PushR8), 
    ISHL("ISHL", 120, 120, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopI4_PopI4, StackBehavior.PushI4), 
    LSHL("LSHL", 121, 121, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopI4_PopI8, StackBehavior.PushI8), 
    ISHR("ISHR", 122, 122, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopI4_PopI4, StackBehavior.PushI4), 
    LSHR("LSHR", 123, 123, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopI4_PopI8, StackBehavior.PushI8), 
    IUSHR("IUSHR", 124, 124, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopI4_PopI4, StackBehavior.PushI4), 
    LUSHR("LUSHR", 125, 125, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopI4_PopI8, StackBehavior.PushI8), 
    IAND("IAND", 126, 126, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopI4_PopI4, StackBehavior.PushI4), 
    LAND("LAND", 127, 127, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopI8_PopI8, StackBehavior.PushI8), 
    IOR("IOR", 128, 128, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopI4_PopI4, StackBehavior.PushI4), 
    LOR("LOR", 129, 129, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopI8_PopI8, StackBehavior.PushI8), 
    IXOR("IXOR", 130, 130, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopI4_PopI4, StackBehavior.PushI4), 
    LXOR("LXOR", 131, 131, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopI8_PopI8, StackBehavior.PushI8), 
    IINC("IINC", 132, 132, FlowControl.Next, OpCodeType.Primitive, OperandType.LocalI1, StackBehavior.Pop0, StackBehavior.Push0), 
    I2L("I2L", 133, 133, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopI4, StackBehavior.PushI8), 
    I2F("I2F", 134, 134, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopI4, StackBehavior.PushR4), 
    I2D("I2D", 135, 135, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopI4, StackBehavior.PushR8), 
    L2I("L2I", 136, 136, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopI8, StackBehavior.PushI4), 
    L2F("L2F", 137, 137, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopI8, StackBehavior.PushR4), 
    L2D("L2D", 138, 138, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopI8, StackBehavior.PushR8), 
    F2I("F2I", 139, 139, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopR4, StackBehavior.PushI4), 
    F2L("F2L", 140, 140, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopR4, StackBehavior.PushI8), 
    F2D("F2D", 141, 141, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopR4, StackBehavior.PushR8), 
    D2I("D2I", 142, 142, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopR8, StackBehavior.PushI4), 
    D2L("D2L", 143, 143, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopR8, StackBehavior.PushI8), 
    D2F("D2F", 144, 144, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopR8, StackBehavior.PushR4), 
    I2B("I2B", 145, 145, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopI4, StackBehavior.PushI4), 
    I2C("I2C", 146, 146, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopI4, StackBehavior.PushI4), 
    I2S("I2S", 147, 147, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopI4, StackBehavior.PushI4), 
    LCMP("LCMP", 148, 148, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopI8_PopI8, StackBehavior.PushI4), 
    FCMPL("FCMPL", 149, 149, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopR4_PopR4, StackBehavior.PushI4), 
    FCMPG("FCMPG", 150, 150, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopR4_PopR4, StackBehavior.PushI4), 
    DCMPL("DCMPL", 151, 151, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopR8_PopR8, StackBehavior.PushI4), 
    DCMPG("DCMPG", 152, 152, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopR8_PopR8, StackBehavior.PushI4), 
    IFEQ("IFEQ", 153, 153, FlowControl.ConditionalBranch, OpCodeType.Primitive, OperandType.BranchTarget, StackBehavior.PopI4, StackBehavior.Push0), 
    IFNE("IFNE", 154, 154, FlowControl.ConditionalBranch, OpCodeType.Primitive, OperandType.BranchTarget, StackBehavior.PopI4, StackBehavior.Push0), 
    IFLT("IFLT", 155, 155, FlowControl.ConditionalBranch, OpCodeType.Primitive, OperandType.BranchTarget, StackBehavior.PopI4, StackBehavior.Push0), 
    IFGE("IFGE", 156, 156, FlowControl.ConditionalBranch, OpCodeType.Primitive, OperandType.BranchTarget, StackBehavior.PopI4, StackBehavior.Push0), 
    IFGT("IFGT", 157, 157, FlowControl.ConditionalBranch, OpCodeType.Primitive, OperandType.BranchTarget, StackBehavior.PopI4, StackBehavior.Push0), 
    IFLE("IFLE", 158, 158, FlowControl.ConditionalBranch, OpCodeType.Primitive, OperandType.BranchTarget, StackBehavior.PopI4, StackBehavior.Push0), 
    IF_ICMPEQ("IF_ICMPEQ", 159, 159, FlowControl.ConditionalBranch, OpCodeType.Macro, OperandType.BranchTarget, StackBehavior.PopI4_PopI4, StackBehavior.Push0), 
    IF_ICMPNE("IF_ICMPNE", 160, 160, FlowControl.ConditionalBranch, OpCodeType.Macro, OperandType.BranchTarget, StackBehavior.PopI4_PopI4, StackBehavior.Push0), 
    IF_ICMPLT("IF_ICMPLT", 161, 161, FlowControl.ConditionalBranch, OpCodeType.Macro, OperandType.BranchTarget, StackBehavior.PopI4_PopI4, StackBehavior.Push0), 
    IF_ICMPGE("IF_ICMPGE", 162, 162, FlowControl.ConditionalBranch, OpCodeType.Macro, OperandType.BranchTarget, StackBehavior.PopI4_PopI4, StackBehavior.Push0), 
    IF_ICMPGT("IF_ICMPGT", 163, 163, FlowControl.ConditionalBranch, OpCodeType.Macro, OperandType.BranchTarget, StackBehavior.PopI4_PopI4, StackBehavior.Push0), 
    IF_ICMPLE("IF_ICMPLE", 164, 164, FlowControl.ConditionalBranch, OpCodeType.Macro, OperandType.BranchTarget, StackBehavior.PopI4_PopI4, StackBehavior.Push0), 
    IF_ACMPEQ("IF_ACMPEQ", 165, 165, FlowControl.ConditionalBranch, OpCodeType.Macro, OperandType.BranchTarget, StackBehavior.PopA_PopA, StackBehavior.Push0), 
    IF_ACMPNE("IF_ACMPNE", 166, 166, FlowControl.ConditionalBranch, OpCodeType.Macro, OperandType.BranchTarget, StackBehavior.PopA_PopA, StackBehavior.Push0), 
    GOTO("GOTO", 167, 167, FlowControl.Branch, OpCodeType.Primitive, OperandType.BranchTarget, StackBehavior.Pop0, StackBehavior.Push0), 
    JSR("JSR", 168, 168, FlowControl.Branch, OpCodeType.Primitive, OperandType.BranchTarget, StackBehavior.Pop0, StackBehavior.Push0), 
    RET("RET", 169, 169, FlowControl.Branch, OpCodeType.Primitive, OperandType.Local, StackBehavior.Pop0, StackBehavior.Push0), 
    TABLESWITCH("TABLESWITCH", 170, 170, FlowControl.Branch, OpCodeType.Primitive, OperandType.Switch, StackBehavior.PopI4, StackBehavior.Push0), 
    LOOKUPSWITCH("LOOKUPSWITCH", 171, 171, FlowControl.Branch, OpCodeType.Primitive, OperandType.Switch, StackBehavior.PopI4, StackBehavior.Push0), 
    IRETURN("IRETURN", 172, 172, FlowControl.Return, OpCodeType.Primitive, OperandType.None, StackBehavior.PopI4, StackBehavior.Push0), 
    LRETURN("LRETURN", 173, 173, FlowControl.Return, OpCodeType.Primitive, OperandType.None, StackBehavior.PopI8, StackBehavior.Push0), 
    FRETURN("FRETURN", 174, 174, FlowControl.Return, OpCodeType.Primitive, OperandType.None, StackBehavior.PopR4, StackBehavior.Push0), 
    DRETURN("DRETURN", 175, 175, FlowControl.Return, OpCodeType.Primitive, OperandType.None, StackBehavior.PopR8, StackBehavior.Push0), 
    ARETURN("ARETURN", 176, 176, FlowControl.Return, OpCodeType.Primitive, OperandType.None, StackBehavior.PopA, StackBehavior.Push0), 
    RETURN("RETURN", 177, 177, FlowControl.Return, OpCodeType.Primitive, OperandType.None, StackBehavior.Pop0, StackBehavior.Push0), 
    GETSTATIC("GETSTATIC", 178, 178, FlowControl.Next, OpCodeType.ObjectModel, OperandType.FieldReference, StackBehavior.Pop0, StackBehavior.Push1), 
    PUTSTATIC("PUTSTATIC", 179, 179, FlowControl.Next, OpCodeType.ObjectModel, OperandType.FieldReference, StackBehavior.Pop1, StackBehavior.Push0), 
    GETFIELD("GETFIELD", 180, 180, FlowControl.Next, OpCodeType.ObjectModel, OperandType.FieldReference, StackBehavior.PopA, StackBehavior.Push1), 
    PUTFIELD("PUTFIELD", 181, 181, FlowControl.Next, OpCodeType.ObjectModel, OperandType.FieldReference, StackBehavior.Pop1_PopA, StackBehavior.Push0), 
    INVOKEVIRTUAL("INVOKEVIRTUAL", 182, 182, FlowControl.Call, OpCodeType.ObjectModel, OperandType.MethodReference, StackBehavior.VarPop, StackBehavior.VarPush), 
    INVOKESPECIAL("INVOKESPECIAL", 183, 183, FlowControl.Call, OpCodeType.ObjectModel, OperandType.MethodReference, StackBehavior.VarPop, StackBehavior.VarPush), 
    INVOKESTATIC("INVOKESTATIC", 184, 184, FlowControl.Call, OpCodeType.Primitive, OperandType.MethodReference, StackBehavior.VarPop, StackBehavior.VarPush), 
    INVOKEINTERFACE("INVOKEINTERFACE", 185, 185, FlowControl.Call, OpCodeType.ObjectModel, OperandType.MethodReference, StackBehavior.VarPop, StackBehavior.VarPush), 
    INVOKEDYNAMIC("INVOKEDYNAMIC", 186, 186, FlowControl.Call, OpCodeType.ObjectModel, OperandType.DynamicCallSite, StackBehavior.VarPop, StackBehavior.VarPush), 
    NEW("NEW", 187, 187, FlowControl.Next, OpCodeType.ObjectModel, OperandType.TypeReference, StackBehavior.Pop0, StackBehavior.PushA), 
    NEWARRAY("NEWARRAY", 188, 188, FlowControl.Next, OpCodeType.ObjectModel, OperandType.PrimitiveTypeCode, StackBehavior.PopI4, StackBehavior.PushA), 
    ANEWARRAY("ANEWARRAY", 189, 189, FlowControl.Next, OpCodeType.ObjectModel, OperandType.TypeReference, StackBehavior.PopI4, StackBehavior.PushA), 
    ARRAYLENGTH("ARRAYLENGTH", 190, 190, FlowControl.Next, OpCodeType.ObjectModel, OperandType.None, StackBehavior.PopA, StackBehavior.PushI4), 
    ATHROW("ATHROW", 191, 191, FlowControl.Throw, OpCodeType.ObjectModel, OperandType.None, StackBehavior.VarPop, StackBehavior.Push0), 
    CHECKCAST("CHECKCAST", 192, 192, FlowControl.Next, OpCodeType.ObjectModel, OperandType.TypeReference, StackBehavior.PopA, StackBehavior.PushA), 
    INSTANCEOF("INSTANCEOF", 193, 193, FlowControl.Next, OpCodeType.ObjectModel, OperandType.TypeReference, StackBehavior.PopA, StackBehavior.PushI4), 
    MONITORENTER("MONITORENTER", 194, 194, FlowControl.Next, OpCodeType.ObjectModel, OperandType.None, StackBehavior.PopA, StackBehavior.Push0), 
    MONITOREXIT("MONITOREXIT", 195, 195, FlowControl.Next, OpCodeType.ObjectModel, OperandType.None, StackBehavior.PopA, StackBehavior.Push0), 
    MULTIANEWARRAY("MULTIANEWARRAY", 196, 197, FlowControl.Next, OpCodeType.ObjectModel, OperandType.TypeReferenceU1, StackBehavior.VarPop, StackBehavior.PushA), 
    IFNULL("IFNULL", 197, 198, FlowControl.ConditionalBranch, OpCodeType.Primitive, OperandType.BranchTarget, StackBehavior.PopA, StackBehavior.Push0), 
    IFNONNULL("IFNONNULL", 198, 199, FlowControl.ConditionalBranch, OpCodeType.Primitive, OperandType.BranchTarget, StackBehavior.PopA, StackBehavior.Push0), 
    GOTO_W("GOTO_W", 199, 200, FlowControl.Branch, OpCodeType.Primitive, OperandType.BranchTargetWide, StackBehavior.Pop0, StackBehavior.Push0), 
    JSR_W("JSR_W", 200, 201, FlowControl.Branch, OpCodeType.Primitive, OperandType.BranchTargetWide, StackBehavior.Pop0, StackBehavior.Push0), 
    BREAKPOINT("BREAKPOINT", 201, 201, FlowControl.Breakpoint, OpCodeType.Primitive, OperandType.None, StackBehavior.Pop0, StackBehavior.Push0), 
    ILOAD_W("ILOAD_W", 202, 50197, FlowControl.Next, OpCodeType.Primitive, OperandType.Local, StackBehavior.Pop0, StackBehavior.PushI4), 
    LLOAD_W("LLOAD_W", 203, 50198, FlowControl.Next, OpCodeType.Primitive, OperandType.Local, StackBehavior.Pop0, StackBehavior.PushI8), 
    FLOAD_W("FLOAD_W", 204, 50199, FlowControl.Next, OpCodeType.Primitive, OperandType.Local, StackBehavior.Pop0, StackBehavior.PushR4), 
    DLOAD_W("DLOAD_W", 205, 50200, FlowControl.Next, OpCodeType.Primitive, OperandType.Local, StackBehavior.Pop0, StackBehavior.PushR8), 
    ALOAD_W("ALOAD_W", 206, 50201, FlowControl.Next, OpCodeType.Primitive, OperandType.Local, StackBehavior.Pop0, StackBehavior.PushA), 
    ISTORE_W("ISTORE_W", 207, 50230, FlowControl.Next, OpCodeType.Primitive, OperandType.Local, StackBehavior.PopI4, StackBehavior.Push0), 
    LSTORE_W("LSTORE_W", 208, 50231, FlowControl.Next, OpCodeType.Primitive, OperandType.Local, StackBehavior.PopI8, StackBehavior.Push0), 
    FSTORE_W("FSTORE_W", 209, 50232, FlowControl.Next, OpCodeType.Primitive, OperandType.Local, StackBehavior.PopR4, StackBehavior.Push0), 
    DSTORE_W("DSTORE_W", 210, 50233, FlowControl.Next, OpCodeType.Primitive, OperandType.Local, StackBehavior.PopR8, StackBehavior.Push0), 
    ASTORE_W("ASTORE_W", 211, 50234, FlowControl.Next, OpCodeType.Primitive, OperandType.Local, StackBehavior.PopA, StackBehavior.Push0), 
    IINC_W("IINC_W", 212, 50308, FlowControl.Next, OpCodeType.Primitive, OperandType.LocalI2, StackBehavior.Pop0, StackBehavior.Push0), 
    RET_W("RET_W", 213, 50345, FlowControl.Branch, OpCodeType.Primitive, OperandType.Local, StackBehavior.Pop0, StackBehavior.Push0), 
    LEAVE("LEAVE", 214, 254, FlowControl.Branch, OpCodeType.Primitive, OperandType.None, StackBehavior.Pop0, StackBehavior.Push0), 
    ENDFINALLY("ENDFINALLY", 215, 255, FlowControl.Branch, OpCodeType.Primitive, OperandType.None, StackBehavior.Pop0, StackBehavior.Push0);
    
    private final int _code;
    private final FlowControl _flowControl;
    private final OpCodeType _opCodeType;
    private final OperandType _operandType;
    private final StackBehavior _stackBehaviorPop;
    private final StackBehavior _stackBehaviorPush;
    public static final int STANDARD = 0;
    public static final int WIDE = 196;
    private static final OpCode[] standardOpCodes;
    private static final OpCode[] wideOpCodes;
    private static final byte[] stackChange;
    private static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$assembler$ir$OpCode;
    private static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$assembler$ir$FlowControl;
    
    static {
        standardOpCodes = new OpCode[256];
        wideOpCodes = new OpCode[256];
        OpCode[] loc_1;
        for (int loc_0 = (loc_1 = values()).length, loc_2 = 0; loc_2 < loc_0; ++loc_2) {
            final OpCode o = loc_1[loc_2];
            getOpcodeBlock(o._code >> 8)[o._code & 0xFF] = o;
        }
        stackChange = new byte[] { 0, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 1, 1, 1, 2, 2, 1, 1, 1, 1, 2, 1, 2, 1, 2, 1, 1, 1, 1, 1, 2, 2, 2, 2, 1, 1, 1, 1, 2, 2, 2, 2, 1, 1, 1, 1, -1, 0, -1, 0, -1, -1, -1, -1, -1, -2, -1, -2, -1, -1, -1, -1, -1, -2, -2, -2, -2, -1, -1, -1, -1, -2, -2, -2, -2, -1, -1, -1, -1, -3, -4, -3, -4, -3, -3, -3, -3, -1, -2, 1, 1, 1, 2, 2, 2, 0, -1, -2, -1, -2, -1, -2, -1, -2, -1, -2, -1, -2, -1, -2, -1, -2, -1, -2, -1, -2, 0, 0, 0, 0, -1, -1, -1, -1, -1, -1, -1, -2, -1, -2, -1, -2, 0, 1, 0, 1, -1, -1, 0, 0, 1, 1, -1, 0, -1, 0, 0, 0, -3, -1, -1, -3, -3, -1, -1, -1, -1, -1, -1, -2, -2, -2, -2, -2, -2, -2, -2, 0, 1, 0, -1, -1, -1, -2, -1, -2, -1, 0, 1, -1, 1, -1, -1, -1, 0, -1, -1, 1, 0, 0, 0, -1, 0, 0, -1, -1, 0, 1, -1, -1, 0, 1, 0, 1, 1, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    }
    
    private OpCode(final String param_0, final int param_1, final int code, final FlowControl flowControl, final OpCodeType opCodeType, final OperandType operandType, final StackBehavior stackBehaviorPop, final StackBehavior stackBehaviorPush) {
        this._code = code;
        this._flowControl = flowControl;
        this._opCodeType = opCodeType;
        this._operandType = operandType;
        this._stackBehaviorPop = stackBehaviorPop;
        this._stackBehaviorPush = stackBehaviorPush;
    }
    
    public int getCode() {
        return this._code;
    }
    
    public boolean isWide() {
        return (this._code >> 8 & 0xC4) == 0xC4;
    }
    
    public OperandType getOperandType() {
        return this._operandType;
    }
    
    public FlowControl getFlowControl() {
        return this._flowControl;
    }
    
    public OpCodeType getOpCodeType() {
        return this._opCodeType;
    }
    
    public StackBehavior getStackBehaviorPop() {
        return this._stackBehaviorPop;
    }
    
    public StackBehavior getStackBehaviorPush() {
        return this._stackBehaviorPush;
    }
    
    public boolean hasVariableStackBehavior() {
        return this._stackBehaviorPop == StackBehavior.VarPop || this._stackBehaviorPush == StackBehavior.VarPush;
    }
    
    public boolean isReturn() {
        return this._flowControl == FlowControl.Return;
    }
    
    public boolean isThrow() {
        return this._flowControl == FlowControl.Throw;
    }
    
    public boolean isJumpToSubroutine() {
        switch ($SWITCH_TABLE$com$strobel$assembler$ir$OpCode()[this.ordinal()]) {
            case 169:
            case 201: {
                return true;
            }
            default: {
                return false;
            }
        }
    }
    
    public boolean isReturnFromSubroutine() {
        switch ($SWITCH_TABLE$com$strobel$assembler$ir$OpCode()[this.ordinal()]) {
            case 170:
            case 214: {
                return true;
            }
            default: {
                return false;
            }
        }
    }
    
    public boolean isLeave() {
        switch ($SWITCH_TABLE$com$strobel$assembler$ir$OpCode()[this.ordinal()]) {
            case 169:
            case 201:
            case 215:
            case 216: {
                return true;
            }
            default: {
                return false;
            }
        }
    }
    
    public boolean isBranch() {
        switch ($SWITCH_TABLE$com$strobel$assembler$ir$FlowControl()[this._flowControl.ordinal()]) {
            case 1:
            case 4:
            case 6:
            case 7: {
                return true;
            }
            default: {
                return false;
            }
        }
    }
    
    public boolean isUnconditionalBranch() {
        switch ($SWITCH_TABLE$com$strobel$assembler$ir$FlowControl()[this._flowControl.ordinal()]) {
            case 1:
            case 6:
            case 7: {
                return true;
            }
            default: {
                return false;
            }
        }
    }
    
    public boolean isMoveInstruction() {
        return this.isLoad() || this.isStore();
    }
    
    public boolean isLoad() {
        switch ($SWITCH_TABLE$com$strobel$assembler$ir$OpCode()[this.ordinal()]) {
            case 22:
            case 23:
            case 24:
            case 25:
            case 26:
            case 27:
            case 28:
            case 29:
            case 30:
            case 31:
            case 32:
            case 33:
            case 34:
            case 35:
            case 36:
            case 37:
            case 38:
            case 39:
            case 40:
            case 41:
            case 42:
            case 43:
            case 44:
            case 45:
            case 46: {
                return true;
            }
            case 203:
            case 204:
            case 205:
            case 206:
            case 207: {
                return true;
            }
            case 170:
            case 214: {
                return true;
            }
            default: {
                return false;
            }
        }
    }
    
    public boolean isStore() {
        switch ($SWITCH_TABLE$com$strobel$assembler$ir$OpCode()[this.ordinal()]) {
            case 55:
            case 56:
            case 57:
            case 58:
            case 59:
            case 60:
            case 61:
            case 62:
            case 63:
            case 64:
            case 65:
            case 66:
            case 67:
            case 68:
            case 69:
            case 70:
            case 71:
            case 72:
            case 73:
            case 74:
            case 75:
            case 76:
            case 77:
            case 78:
            case 79: {
                return true;
            }
            case 208:
            case 209:
            case 210:
            case 211:
            case 212: {
                return true;
            }
            default: {
                return false;
            }
        }
    }
    
    public boolean isArrayLoad() {
        switch ($SWITCH_TABLE$com$strobel$assembler$ir$OpCode()[this.ordinal()]) {
            case 47:
            case 48:
            case 49:
            case 50:
            case 51: {
                return true;
            }
            default: {
                return false;
            }
        }
    }
    
    public boolean isArrayStore() {
        switch ($SWITCH_TABLE$com$strobel$assembler$ir$OpCode()[this.ordinal()]) {
            case 80:
            case 81:
            case 82:
            case 83:
            case 84: {
                return true;
            }
            default: {
                return false;
            }
        }
    }
    
    public int getSize() {
        return (this._code >> 8 == 196) ? 2 : 1;
    }
    
    public int getStackChange() {
        return OpCode.stackChange[this._code & 0xFF];
    }
    
    public boolean endsUnconditionalJumpBlock() {
        switch ($SWITCH_TABLE$com$strobel$assembler$ir$OpCode()[this.ordinal()]) {
            case 168:
            case 169:
            case 170: {
                return true;
            }
            case 173:
            case 174:
            case 175:
            case 176:
            case 177:
            case 178: {
                return true;
            }
            case 192: {
                return true;
            }
            case 200:
            case 201: {
                return true;
            }
            case 214: {
                return true;
            }
            default: {
                return false;
            }
        }
    }
    
    public boolean canThrow() {
        if (this._opCodeType == OpCodeType.ObjectModel) {
            return this != OpCode.INSTANCEOF;
        }
        switch ($SWITCH_TABLE$com$strobel$assembler$ir$OpCode()[this.ordinal()]) {
            case 109:
            case 110: {
                return true;
            }
            case 113:
            case 114: {
                return true;
            }
            default: {
                return false;
            }
        }
    }
    
    public OpCode negate() {
        if (this == OpCode.IFNULL) {
            return OpCode.IFNONNULL;
        }
        if (this == OpCode.IFNONNULL) {
            return OpCode.IFNULL;
        }
        return get((this._code + 1 ^ 0x1) - 1);
    }
    
    public static OpCode get(final int code) {
        return getOpcodeBlock(code >> 8)[code & 0xFF];
    }
    
    private static OpCode[] getOpcodeBlock(final int prefix) {
        switch (prefix) {
            case 0: {
                return OpCode.standardOpCodes;
            }
            case 196: {
                return OpCode.wideOpCodes;
            }
            default: {
                return null;
            }
        }
    }
    
    static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$assembler$ir$OpCode() {
        final int[] loc_0 = OpCode.$SWITCH_TABLE$com$strobel$assembler$ir$OpCode;
        if (loc_0 != null) {
            return loc_0;
        }
        final int[] loc_1 = new int[values().length];
        try {
            loc_1[OpCode.AALOAD.ordinal()] = 51;
        }
        catch (NoSuchFieldError loc_2) {}
        try {
            loc_1[OpCode.AASTORE.ordinal()] = 84;
        }
        catch (NoSuchFieldError loc_3) {}
        try {
            loc_1[OpCode.ACONST_NULL.ordinal()] = 2;
        }
        catch (NoSuchFieldError loc_4) {}
        try {
            loc_1[OpCode.ALOAD.ordinal()] = 26;
        }
        catch (NoSuchFieldError loc_5) {}
        try {
            loc_1[OpCode.ALOAD_0.ordinal()] = 43;
        }
        catch (NoSuchFieldError loc_6) {}
        try {
            loc_1[OpCode.ALOAD_1.ordinal()] = 44;
        }
        catch (NoSuchFieldError loc_7) {}
        try {
            loc_1[OpCode.ALOAD_2.ordinal()] = 45;
        }
        catch (NoSuchFieldError loc_8) {}
        try {
            loc_1[OpCode.ALOAD_3.ordinal()] = 46;
        }
        catch (NoSuchFieldError loc_9) {}
        try {
            loc_1[OpCode.ALOAD_W.ordinal()] = 207;
        }
        catch (NoSuchFieldError loc_10) {}
        try {
            loc_1[OpCode.ANEWARRAY.ordinal()] = 190;
        }
        catch (NoSuchFieldError loc_11) {}
        try {
            loc_1[OpCode.ARETURN.ordinal()] = 177;
        }
        catch (NoSuchFieldError loc_12) {}
        try {
            loc_1[OpCode.ARRAYLENGTH.ordinal()] = 191;
        }
        catch (NoSuchFieldError loc_13) {}
        try {
            loc_1[OpCode.ASTORE.ordinal()] = 59;
        }
        catch (NoSuchFieldError loc_14) {}
        try {
            loc_1[OpCode.ASTORE_0.ordinal()] = 76;
        }
        catch (NoSuchFieldError loc_15) {}
        try {
            loc_1[OpCode.ASTORE_1.ordinal()] = 77;
        }
        catch (NoSuchFieldError loc_16) {}
        try {
            loc_1[OpCode.ASTORE_2.ordinal()] = 78;
        }
        catch (NoSuchFieldError loc_17) {}
        try {
            loc_1[OpCode.ASTORE_3.ordinal()] = 79;
        }
        catch (NoSuchFieldError loc_18) {}
        try {
            loc_1[OpCode.ASTORE_W.ordinal()] = 212;
        }
        catch (NoSuchFieldError loc_19) {}
        try {
            loc_1[OpCode.ATHROW.ordinal()] = 192;
        }
        catch (NoSuchFieldError loc_20) {}
        try {
            loc_1[OpCode.BALOAD.ordinal()] = 52;
        }
        catch (NoSuchFieldError loc_21) {}
        try {
            loc_1[OpCode.BASTORE.ordinal()] = 85;
        }
        catch (NoSuchFieldError loc_22) {}
        try {
            loc_1[OpCode.BIPUSH.ordinal()] = 17;
        }
        catch (NoSuchFieldError loc_23) {}
        try {
            loc_1[OpCode.BREAKPOINT.ordinal()] = 202;
        }
        catch (NoSuchFieldError loc_24) {}
        try {
            loc_1[OpCode.CALOAD.ordinal()] = 53;
        }
        catch (NoSuchFieldError loc_25) {}
        try {
            loc_1[OpCode.CASTORE.ordinal()] = 86;
        }
        catch (NoSuchFieldError loc_26) {}
        try {
            loc_1[OpCode.CHECKCAST.ordinal()] = 193;
        }
        catch (NoSuchFieldError loc_27) {}
        try {
            loc_1[OpCode.D2F.ordinal()] = 145;
        }
        catch (NoSuchFieldError loc_28) {}
        try {
            loc_1[OpCode.D2I.ordinal()] = 143;
        }
        catch (NoSuchFieldError loc_29) {}
        try {
            loc_1[OpCode.D2L.ordinal()] = 144;
        }
        catch (NoSuchFieldError loc_30) {}
        try {
            loc_1[OpCode.DADD.ordinal()] = 100;
        }
        catch (NoSuchFieldError loc_31) {}
        try {
            loc_1[OpCode.DALOAD.ordinal()] = 50;
        }
        catch (NoSuchFieldError loc_32) {}
        try {
            loc_1[OpCode.DASTORE.ordinal()] = 83;
        }
        catch (NoSuchFieldError loc_33) {}
        try {
            loc_1[OpCode.DCMPG.ordinal()] = 153;
        }
        catch (NoSuchFieldError loc_34) {}
        try {
            loc_1[OpCode.DCMPL.ordinal()] = 152;
        }
        catch (NoSuchFieldError loc_35) {}
        try {
            loc_1[OpCode.DCONST_0.ordinal()] = 15;
        }
        catch (NoSuchFieldError loc_36) {}
        try {
            loc_1[OpCode.DCONST_1.ordinal()] = 16;
        }
        catch (NoSuchFieldError loc_37) {}
        try {
            loc_1[OpCode.DDIV.ordinal()] = 112;
        }
        catch (NoSuchFieldError loc_38) {}
        try {
            loc_1[OpCode.DLOAD.ordinal()] = 25;
        }
        catch (NoSuchFieldError loc_39) {}
        try {
            loc_1[OpCode.DLOAD_0.ordinal()] = 39;
        }
        catch (NoSuchFieldError loc_40) {}
        try {
            loc_1[OpCode.DLOAD_1.ordinal()] = 40;
        }
        catch (NoSuchFieldError loc_41) {}
        try {
            loc_1[OpCode.DLOAD_2.ordinal()] = 41;
        }
        catch (NoSuchFieldError loc_42) {}
        try {
            loc_1[OpCode.DLOAD_3.ordinal()] = 42;
        }
        catch (NoSuchFieldError loc_43) {}
        try {
            loc_1[OpCode.DLOAD_W.ordinal()] = 206;
        }
        catch (NoSuchFieldError loc_44) {}
        try {
            loc_1[OpCode.DMUL.ordinal()] = 108;
        }
        catch (NoSuchFieldError loc_45) {}
        try {
            loc_1[OpCode.DNEG.ordinal()] = 120;
        }
        catch (NoSuchFieldError loc_46) {}
        try {
            loc_1[OpCode.DREM.ordinal()] = 116;
        }
        catch (NoSuchFieldError loc_47) {}
        try {
            loc_1[OpCode.DRETURN.ordinal()] = 176;
        }
        catch (NoSuchFieldError loc_48) {}
        try {
            loc_1[OpCode.DSTORE.ordinal()] = 58;
        }
        catch (NoSuchFieldError loc_49) {}
        try {
            loc_1[OpCode.DSTORE_0.ordinal()] = 72;
        }
        catch (NoSuchFieldError loc_50) {}
        try {
            loc_1[OpCode.DSTORE_1.ordinal()] = 73;
        }
        catch (NoSuchFieldError loc_51) {}
        try {
            loc_1[OpCode.DSTORE_2.ordinal()] = 74;
        }
        catch (NoSuchFieldError loc_52) {}
        try {
            loc_1[OpCode.DSTORE_3.ordinal()] = 75;
        }
        catch (NoSuchFieldError loc_53) {}
        try {
            loc_1[OpCode.DSTORE_W.ordinal()] = 211;
        }
        catch (NoSuchFieldError loc_54) {}
        try {
            loc_1[OpCode.DSUB.ordinal()] = 104;
        }
        catch (NoSuchFieldError loc_55) {}
        try {
            loc_1[OpCode.DUP.ordinal()] = 90;
        }
        catch (NoSuchFieldError loc_56) {}
        try {
            loc_1[OpCode.DUP2.ordinal()] = 93;
        }
        catch (NoSuchFieldError loc_57) {}
        try {
            loc_1[OpCode.DUP2_X1.ordinal()] = 94;
        }
        catch (NoSuchFieldError loc_58) {}
        try {
            loc_1[OpCode.DUP2_X2.ordinal()] = 95;
        }
        catch (NoSuchFieldError loc_59) {}
        try {
            loc_1[OpCode.DUP_X1.ordinal()] = 91;
        }
        catch (NoSuchFieldError loc_60) {}
        try {
            loc_1[OpCode.DUP_X2.ordinal()] = 92;
        }
        catch (NoSuchFieldError loc_61) {}
        try {
            loc_1[OpCode.ENDFINALLY.ordinal()] = 216;
        }
        catch (NoSuchFieldError loc_62) {}
        try {
            loc_1[OpCode.F2D.ordinal()] = 142;
        }
        catch (NoSuchFieldError loc_63) {}
        try {
            loc_1[OpCode.F2I.ordinal()] = 140;
        }
        catch (NoSuchFieldError loc_64) {}
        try {
            loc_1[OpCode.F2L.ordinal()] = 141;
        }
        catch (NoSuchFieldError loc_65) {}
        try {
            loc_1[OpCode.FADD.ordinal()] = 99;
        }
        catch (NoSuchFieldError loc_66) {}
        try {
            loc_1[OpCode.FALOAD.ordinal()] = 49;
        }
        catch (NoSuchFieldError loc_67) {}
        try {
            loc_1[OpCode.FASTORE.ordinal()] = 82;
        }
        catch (NoSuchFieldError loc_68) {}
        try {
            loc_1[OpCode.FCMPG.ordinal()] = 151;
        }
        catch (NoSuchFieldError loc_69) {}
        try {
            loc_1[OpCode.FCMPL.ordinal()] = 150;
        }
        catch (NoSuchFieldError loc_70) {}
        try {
            loc_1[OpCode.FCONST_0.ordinal()] = 12;
        }
        catch (NoSuchFieldError loc_71) {}
        try {
            loc_1[OpCode.FCONST_1.ordinal()] = 13;
        }
        catch (NoSuchFieldError loc_72) {}
        try {
            loc_1[OpCode.FCONST_2.ordinal()] = 14;
        }
        catch (NoSuchFieldError loc_73) {}
        try {
            loc_1[OpCode.FDIV.ordinal()] = 111;
        }
        catch (NoSuchFieldError loc_74) {}
        try {
            loc_1[OpCode.FLOAD.ordinal()] = 24;
        }
        catch (NoSuchFieldError loc_75) {}
        try {
            loc_1[OpCode.FLOAD_0.ordinal()] = 35;
        }
        catch (NoSuchFieldError loc_76) {}
        try {
            loc_1[OpCode.FLOAD_1.ordinal()] = 36;
        }
        catch (NoSuchFieldError loc_77) {}
        try {
            loc_1[OpCode.FLOAD_2.ordinal()] = 37;
        }
        catch (NoSuchFieldError loc_78) {}
        try {
            loc_1[OpCode.FLOAD_3.ordinal()] = 38;
        }
        catch (NoSuchFieldError loc_79) {}
        try {
            loc_1[OpCode.FLOAD_W.ordinal()] = 205;
        }
        catch (NoSuchFieldError loc_80) {}
        try {
            loc_1[OpCode.FMUL.ordinal()] = 107;
        }
        catch (NoSuchFieldError loc_81) {}
        try {
            loc_1[OpCode.FNEG.ordinal()] = 119;
        }
        catch (NoSuchFieldError loc_82) {}
        try {
            loc_1[OpCode.FREM.ordinal()] = 115;
        }
        catch (NoSuchFieldError loc_83) {}
        try {
            loc_1[OpCode.FRETURN.ordinal()] = 175;
        }
        catch (NoSuchFieldError loc_84) {}
        try {
            loc_1[OpCode.FSTORE.ordinal()] = 57;
        }
        catch (NoSuchFieldError loc_85) {}
        try {
            loc_1[OpCode.FSTORE_0.ordinal()] = 68;
        }
        catch (NoSuchFieldError loc_86) {}
        try {
            loc_1[OpCode.FSTORE_1.ordinal()] = 69;
        }
        catch (NoSuchFieldError loc_87) {}
        try {
            loc_1[OpCode.FSTORE_2.ordinal()] = 70;
        }
        catch (NoSuchFieldError loc_88) {}
        try {
            loc_1[OpCode.FSTORE_3.ordinal()] = 71;
        }
        catch (NoSuchFieldError loc_89) {}
        try {
            loc_1[OpCode.FSTORE_W.ordinal()] = 210;
        }
        catch (NoSuchFieldError loc_90) {}
        try {
            loc_1[OpCode.FSUB.ordinal()] = 103;
        }
        catch (NoSuchFieldError loc_91) {}
        try {
            loc_1[OpCode.GETFIELD.ordinal()] = 181;
        }
        catch (NoSuchFieldError loc_92) {}
        try {
            loc_1[OpCode.GETSTATIC.ordinal()] = 179;
        }
        catch (NoSuchFieldError loc_93) {}
        try {
            loc_1[OpCode.GOTO.ordinal()] = 168;
        }
        catch (NoSuchFieldError loc_94) {}
        try {
            loc_1[OpCode.GOTO_W.ordinal()] = 200;
        }
        catch (NoSuchFieldError loc_95) {}
        try {
            loc_1[OpCode.I2B.ordinal()] = 146;
        }
        catch (NoSuchFieldError loc_96) {}
        try {
            loc_1[OpCode.I2C.ordinal()] = 147;
        }
        catch (NoSuchFieldError loc_97) {}
        try {
            loc_1[OpCode.I2D.ordinal()] = 136;
        }
        catch (NoSuchFieldError loc_98) {}
        try {
            loc_1[OpCode.I2F.ordinal()] = 135;
        }
        catch (NoSuchFieldError loc_99) {}
        try {
            loc_1[OpCode.I2L.ordinal()] = 134;
        }
        catch (NoSuchFieldError loc_100) {}
        try {
            loc_1[OpCode.I2S.ordinal()] = 148;
        }
        catch (NoSuchFieldError loc_101) {}
        try {
            loc_1[OpCode.IADD.ordinal()] = 97;
        }
        catch (NoSuchFieldError loc_102) {}
        try {
            loc_1[OpCode.IALOAD.ordinal()] = 47;
        }
        catch (NoSuchFieldError loc_103) {}
        try {
            loc_1[OpCode.IAND.ordinal()] = 127;
        }
        catch (NoSuchFieldError loc_104) {}
        try {
            loc_1[OpCode.IASTORE.ordinal()] = 80;
        }
        catch (NoSuchFieldError loc_105) {}
        try {
            loc_1[OpCode.ICONST_0.ordinal()] = 4;
        }
        catch (NoSuchFieldError loc_106) {}
        try {
            loc_1[OpCode.ICONST_1.ordinal()] = 5;
        }
        catch (NoSuchFieldError loc_107) {}
        try {
            loc_1[OpCode.ICONST_2.ordinal()] = 6;
        }
        catch (NoSuchFieldError loc_108) {}
        try {
            loc_1[OpCode.ICONST_3.ordinal()] = 7;
        }
        catch (NoSuchFieldError loc_109) {}
        try {
            loc_1[OpCode.ICONST_4.ordinal()] = 8;
        }
        catch (NoSuchFieldError loc_110) {}
        try {
            loc_1[OpCode.ICONST_5.ordinal()] = 9;
        }
        catch (NoSuchFieldError loc_111) {}
        try {
            loc_1[OpCode.ICONST_M1.ordinal()] = 3;
        }
        catch (NoSuchFieldError loc_112) {}
        try {
            loc_1[OpCode.IDIV.ordinal()] = 109;
        }
        catch (NoSuchFieldError loc_113) {}
        try {
            loc_1[OpCode.IFEQ.ordinal()] = 154;
        }
        catch (NoSuchFieldError loc_114) {}
        try {
            loc_1[OpCode.IFGE.ordinal()] = 157;
        }
        catch (NoSuchFieldError loc_115) {}
        try {
            loc_1[OpCode.IFGT.ordinal()] = 158;
        }
        catch (NoSuchFieldError loc_116) {}
        try {
            loc_1[OpCode.IFLE.ordinal()] = 159;
        }
        catch (NoSuchFieldError loc_117) {}
        try {
            loc_1[OpCode.IFLT.ordinal()] = 156;
        }
        catch (NoSuchFieldError loc_118) {}
        try {
            loc_1[OpCode.IFNE.ordinal()] = 155;
        }
        catch (NoSuchFieldError loc_119) {}
        try {
            loc_1[OpCode.IFNONNULL.ordinal()] = 199;
        }
        catch (NoSuchFieldError loc_120) {}
        try {
            loc_1[OpCode.IFNULL.ordinal()] = 198;
        }
        catch (NoSuchFieldError loc_121) {}
        try {
            loc_1[OpCode.IF_ACMPEQ.ordinal()] = 166;
        }
        catch (NoSuchFieldError loc_122) {}
        try {
            loc_1[OpCode.IF_ACMPNE.ordinal()] = 167;
        }
        catch (NoSuchFieldError loc_123) {}
        try {
            loc_1[OpCode.IF_ICMPEQ.ordinal()] = 160;
        }
        catch (NoSuchFieldError loc_124) {}
        try {
            loc_1[OpCode.IF_ICMPGE.ordinal()] = 163;
        }
        catch (NoSuchFieldError loc_125) {}
        try {
            loc_1[OpCode.IF_ICMPGT.ordinal()] = 164;
        }
        catch (NoSuchFieldError loc_126) {}
        try {
            loc_1[OpCode.IF_ICMPLE.ordinal()] = 165;
        }
        catch (NoSuchFieldError loc_127) {}
        try {
            loc_1[OpCode.IF_ICMPLT.ordinal()] = 162;
        }
        catch (NoSuchFieldError loc_128) {}
        try {
            loc_1[OpCode.IF_ICMPNE.ordinal()] = 161;
        }
        catch (NoSuchFieldError loc_129) {}
        try {
            loc_1[OpCode.IINC.ordinal()] = 133;
        }
        catch (NoSuchFieldError loc_130) {}
        try {
            loc_1[OpCode.IINC_W.ordinal()] = 213;
        }
        catch (NoSuchFieldError loc_131) {}
        try {
            loc_1[OpCode.ILOAD.ordinal()] = 22;
        }
        catch (NoSuchFieldError loc_132) {}
        try {
            loc_1[OpCode.ILOAD_0.ordinal()] = 27;
        }
        catch (NoSuchFieldError loc_133) {}
        try {
            loc_1[OpCode.ILOAD_1.ordinal()] = 28;
        }
        catch (NoSuchFieldError loc_134) {}
        try {
            loc_1[OpCode.ILOAD_2.ordinal()] = 29;
        }
        catch (NoSuchFieldError loc_135) {}
        try {
            loc_1[OpCode.ILOAD_3.ordinal()] = 30;
        }
        catch (NoSuchFieldError loc_136) {}
        try {
            loc_1[OpCode.ILOAD_W.ordinal()] = 203;
        }
        catch (NoSuchFieldError loc_137) {}
        try {
            loc_1[OpCode.IMUL.ordinal()] = 105;
        }
        catch (NoSuchFieldError loc_138) {}
        try {
            loc_1[OpCode.INEG.ordinal()] = 117;
        }
        catch (NoSuchFieldError loc_139) {}
        try {
            loc_1[OpCode.INSTANCEOF.ordinal()] = 194;
        }
        catch (NoSuchFieldError loc_140) {}
        try {
            loc_1[OpCode.INVOKEDYNAMIC.ordinal()] = 187;
        }
        catch (NoSuchFieldError loc_141) {}
        try {
            loc_1[OpCode.INVOKEINTERFACE.ordinal()] = 186;
        }
        catch (NoSuchFieldError loc_142) {}
        try {
            loc_1[OpCode.INVOKESPECIAL.ordinal()] = 184;
        }
        catch (NoSuchFieldError loc_143) {}
        try {
            loc_1[OpCode.INVOKESTATIC.ordinal()] = 185;
        }
        catch (NoSuchFieldError loc_144) {}
        try {
            loc_1[OpCode.INVOKEVIRTUAL.ordinal()] = 183;
        }
        catch (NoSuchFieldError loc_145) {}
        try {
            loc_1[OpCode.IOR.ordinal()] = 129;
        }
        catch (NoSuchFieldError loc_146) {}
        try {
            loc_1[OpCode.IREM.ordinal()] = 113;
        }
        catch (NoSuchFieldError loc_147) {}
        try {
            loc_1[OpCode.IRETURN.ordinal()] = 173;
        }
        catch (NoSuchFieldError loc_148) {}
        try {
            loc_1[OpCode.ISHL.ordinal()] = 121;
        }
        catch (NoSuchFieldError loc_149) {}
        try {
            loc_1[OpCode.ISHR.ordinal()] = 123;
        }
        catch (NoSuchFieldError loc_150) {}
        try {
            loc_1[OpCode.ISTORE.ordinal()] = 55;
        }
        catch (NoSuchFieldError loc_151) {}
        try {
            loc_1[OpCode.ISTORE_0.ordinal()] = 60;
        }
        catch (NoSuchFieldError loc_152) {}
        try {
            loc_1[OpCode.ISTORE_1.ordinal()] = 61;
        }
        catch (NoSuchFieldError loc_153) {}
        try {
            loc_1[OpCode.ISTORE_2.ordinal()] = 62;
        }
        catch (NoSuchFieldError loc_154) {}
        try {
            loc_1[OpCode.ISTORE_3.ordinal()] = 63;
        }
        catch (NoSuchFieldError loc_155) {}
        try {
            loc_1[OpCode.ISTORE_W.ordinal()] = 208;
        }
        catch (NoSuchFieldError loc_156) {}
        try {
            loc_1[OpCode.ISUB.ordinal()] = 101;
        }
        catch (NoSuchFieldError loc_157) {}
        try {
            loc_1[OpCode.IUSHR.ordinal()] = 125;
        }
        catch (NoSuchFieldError loc_158) {}
        try {
            loc_1[OpCode.IXOR.ordinal()] = 131;
        }
        catch (NoSuchFieldError loc_159) {}
        try {
            loc_1[OpCode.JSR.ordinal()] = 169;
        }
        catch (NoSuchFieldError loc_160) {}
        try {
            loc_1[OpCode.JSR_W.ordinal()] = 201;
        }
        catch (NoSuchFieldError loc_161) {}
        try {
            loc_1[OpCode.L2D.ordinal()] = 139;
        }
        catch (NoSuchFieldError loc_162) {}
        try {
            loc_1[OpCode.L2F.ordinal()] = 138;
        }
        catch (NoSuchFieldError loc_163) {}
        try {
            loc_1[OpCode.L2I.ordinal()] = 137;
        }
        catch (NoSuchFieldError loc_164) {}
        try {
            loc_1[OpCode.LADD.ordinal()] = 98;
        }
        catch (NoSuchFieldError loc_165) {}
        try {
            loc_1[OpCode.LALOAD.ordinal()] = 48;
        }
        catch (NoSuchFieldError loc_166) {}
        try {
            loc_1[OpCode.LAND.ordinal()] = 128;
        }
        catch (NoSuchFieldError loc_167) {}
        try {
            loc_1[OpCode.LASTORE.ordinal()] = 81;
        }
        catch (NoSuchFieldError loc_168) {}
        try {
            loc_1[OpCode.LCMP.ordinal()] = 149;
        }
        catch (NoSuchFieldError loc_169) {}
        try {
            loc_1[OpCode.LCONST_0.ordinal()] = 10;
        }
        catch (NoSuchFieldError loc_170) {}
        try {
            loc_1[OpCode.LCONST_1.ordinal()] = 11;
        }
        catch (NoSuchFieldError loc_171) {}
        try {
            loc_1[OpCode.LDC.ordinal()] = 19;
        }
        catch (NoSuchFieldError loc_172) {}
        try {
            loc_1[OpCode.LDC2_W.ordinal()] = 21;
        }
        catch (NoSuchFieldError loc_173) {}
        try {
            loc_1[OpCode.LDC_W.ordinal()] = 20;
        }
        catch (NoSuchFieldError loc_174) {}
        try {
            loc_1[OpCode.LDIV.ordinal()] = 110;
        }
        catch (NoSuchFieldError loc_175) {}
        try {
            loc_1[OpCode.LEAVE.ordinal()] = 215;
        }
        catch (NoSuchFieldError loc_176) {}
        try {
            loc_1[OpCode.LLOAD.ordinal()] = 23;
        }
        catch (NoSuchFieldError loc_177) {}
        try {
            loc_1[OpCode.LLOAD_0.ordinal()] = 31;
        }
        catch (NoSuchFieldError loc_178) {}
        try {
            loc_1[OpCode.LLOAD_1.ordinal()] = 32;
        }
        catch (NoSuchFieldError loc_179) {}
        try {
            loc_1[OpCode.LLOAD_2.ordinal()] = 33;
        }
        catch (NoSuchFieldError loc_180) {}
        try {
            loc_1[OpCode.LLOAD_3.ordinal()] = 34;
        }
        catch (NoSuchFieldError loc_181) {}
        try {
            loc_1[OpCode.LLOAD_W.ordinal()] = 204;
        }
        catch (NoSuchFieldError loc_182) {}
        try {
            loc_1[OpCode.LMUL.ordinal()] = 106;
        }
        catch (NoSuchFieldError loc_183) {}
        try {
            loc_1[OpCode.LNEG.ordinal()] = 118;
        }
        catch (NoSuchFieldError loc_184) {}
        try {
            loc_1[OpCode.LOOKUPSWITCH.ordinal()] = 172;
        }
        catch (NoSuchFieldError loc_185) {}
        try {
            loc_1[OpCode.LOR.ordinal()] = 130;
        }
        catch (NoSuchFieldError loc_186) {}
        try {
            loc_1[OpCode.LREM.ordinal()] = 114;
        }
        catch (NoSuchFieldError loc_187) {}
        try {
            loc_1[OpCode.LRETURN.ordinal()] = 174;
        }
        catch (NoSuchFieldError loc_188) {}
        try {
            loc_1[OpCode.LSHL.ordinal()] = 122;
        }
        catch (NoSuchFieldError loc_189) {}
        try {
            loc_1[OpCode.LSHR.ordinal()] = 124;
        }
        catch (NoSuchFieldError loc_190) {}
        try {
            loc_1[OpCode.LSTORE.ordinal()] = 56;
        }
        catch (NoSuchFieldError loc_191) {}
        try {
            loc_1[OpCode.LSTORE_0.ordinal()] = 64;
        }
        catch (NoSuchFieldError loc_192) {}
        try {
            loc_1[OpCode.LSTORE_1.ordinal()] = 65;
        }
        catch (NoSuchFieldError loc_193) {}
        try {
            loc_1[OpCode.LSTORE_2.ordinal()] = 66;
        }
        catch (NoSuchFieldError loc_194) {}
        try {
            loc_1[OpCode.LSTORE_3.ordinal()] = 67;
        }
        catch (NoSuchFieldError loc_195) {}
        try {
            loc_1[OpCode.LSTORE_W.ordinal()] = 209;
        }
        catch (NoSuchFieldError loc_196) {}
        try {
            loc_1[OpCode.LSUB.ordinal()] = 102;
        }
        catch (NoSuchFieldError loc_197) {}
        try {
            loc_1[OpCode.LUSHR.ordinal()] = 126;
        }
        catch (NoSuchFieldError loc_198) {}
        try {
            loc_1[OpCode.LXOR.ordinal()] = 132;
        }
        catch (NoSuchFieldError loc_199) {}
        try {
            loc_1[OpCode.MONITORENTER.ordinal()] = 195;
        }
        catch (NoSuchFieldError loc_200) {}
        try {
            loc_1[OpCode.MONITOREXIT.ordinal()] = 196;
        }
        catch (NoSuchFieldError loc_201) {}
        try {
            loc_1[OpCode.MULTIANEWARRAY.ordinal()] = 197;
        }
        catch (NoSuchFieldError loc_202) {}
        try {
            loc_1[OpCode.NEW.ordinal()] = 188;
        }
        catch (NoSuchFieldError loc_203) {}
        try {
            loc_1[OpCode.NEWARRAY.ordinal()] = 189;
        }
        catch (NoSuchFieldError loc_204) {}
        try {
            loc_1[OpCode.NOP.ordinal()] = 1;
        }
        catch (NoSuchFieldError loc_205) {}
        try {
            loc_1[OpCode.POP.ordinal()] = 88;
        }
        catch (NoSuchFieldError loc_206) {}
        try {
            loc_1[OpCode.POP2.ordinal()] = 89;
        }
        catch (NoSuchFieldError loc_207) {}
        try {
            loc_1[OpCode.PUTFIELD.ordinal()] = 182;
        }
        catch (NoSuchFieldError loc_208) {}
        try {
            loc_1[OpCode.PUTSTATIC.ordinal()] = 180;
        }
        catch (NoSuchFieldError loc_209) {}
        try {
            loc_1[OpCode.RET.ordinal()] = 170;
        }
        catch (NoSuchFieldError loc_210) {}
        try {
            loc_1[OpCode.RETURN.ordinal()] = 178;
        }
        catch (NoSuchFieldError loc_211) {}
        try {
            loc_1[OpCode.RET_W.ordinal()] = 214;
        }
        catch (NoSuchFieldError loc_212) {}
        try {
            loc_1[OpCode.SALOAD.ordinal()] = 54;
        }
        catch (NoSuchFieldError loc_213) {}
        try {
            loc_1[OpCode.SASTORE.ordinal()] = 87;
        }
        catch (NoSuchFieldError loc_214) {}
        try {
            loc_1[OpCode.SIPUSH.ordinal()] = 18;
        }
        catch (NoSuchFieldError loc_215) {}
        try {
            loc_1[OpCode.SWAP.ordinal()] = 96;
        }
        catch (NoSuchFieldError loc_216) {}
        try {
            loc_1[OpCode.TABLESWITCH.ordinal()] = 171;
        }
        catch (NoSuchFieldError loc_217) {}
        return OpCode.$SWITCH_TABLE$com$strobel$assembler$ir$OpCode = loc_1;
    }
    
    static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$assembler$ir$FlowControl() {
        final int[] loc_0 = OpCode.$SWITCH_TABLE$com$strobel$assembler$ir$FlowControl;
        if (loc_0 != null) {
            return loc_0;
        }
        final int[] loc_1 = new int[FlowControl.values().length];
        try {
            loc_1[FlowControl.Branch.ordinal()] = 1;
        }
        catch (NoSuchFieldError loc_2) {}
        try {
            loc_1[FlowControl.Breakpoint.ordinal()] = 2;
        }
        catch (NoSuchFieldError loc_3) {}
        try {
            loc_1[FlowControl.Call.ordinal()] = 3;
        }
        catch (NoSuchFieldError loc_4) {}
        try {
            loc_1[FlowControl.ConditionalBranch.ordinal()] = 4;
        }
        catch (NoSuchFieldError loc_5) {}
        try {
            loc_1[FlowControl.Next.ordinal()] = 5;
        }
        catch (NoSuchFieldError loc_6) {}
        try {
            loc_1[FlowControl.Return.ordinal()] = 6;
        }
        catch (NoSuchFieldError loc_7) {}
        try {
            loc_1[FlowControl.Throw.ordinal()] = 7;
        }
        catch (NoSuchFieldError loc_8) {}
        return OpCode.$SWITCH_TABLE$com$strobel$assembler$ir$FlowControl = loc_1;
    }
}
