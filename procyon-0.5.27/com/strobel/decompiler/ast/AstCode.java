package com.strobel.decompiler.ast;

import com.strobel.assembler.ir.*;
import com.strobel.core.*;
import com.strobel.assembler.metadata.*;

public enum AstCode
{
    Nop("Nop", 0), 
    AConstNull("AConstNull", 1), 
    __IConstM1("__IConstM1", 2), 
    __IConst0("__IConst0", 3), 
    __IConst1("__IConst1", 4), 
    __IConst2("__IConst2", 5), 
    __IConst3("__IConst3", 6), 
    __IConst4("__IConst4", 7), 
    __IConst5("__IConst5", 8), 
    __LConst0("__LConst0", 9), 
    __LConst1("__LConst1", 10), 
    __FConst0("__FConst0", 11), 
    __FConst1("__FConst1", 12), 
    __FConst2("__FConst2", 13), 
    __DConst0("__DConst0", 14), 
    __DConst1("__DConst1", 15), 
    __BIPush("__BIPush", 16), 
    __SIPush("__SIPush", 17), 
    LdC("LdC", 18), 
    __LdCW("__LdCW", 19), 
    __LdC2W("__LdC2W", 20), 
    __ILoad("__ILoad", 21), 
    __LLoad("__LLoad", 22), 
    __FLoad("__FLoad", 23), 
    __DLoad("__DLoad", 24), 
    __ALoad("__ALoad", 25), 
    __ILoad0("__ILoad0", 26), 
    __ILoad1("__ILoad1", 27), 
    __ILoad2("__ILoad2", 28), 
    __ILoad3("__ILoad3", 29), 
    __LLoad0("__LLoad0", 30), 
    __LLoad1("__LLoad1", 31), 
    __LLoad2("__LLoad2", 32), 
    __LLoad3("__LLoad3", 33), 
    __FLoad0("__FLoad0", 34), 
    __FLoad1("__FLoad1", 35), 
    __FLoad2("__FLoad2", 36), 
    __FLoad3("__FLoad3", 37), 
    __DLoad0("__DLoad0", 38), 
    __DLoad1("__DLoad1", 39), 
    __DLoad2("__DLoad2", 40), 
    __DLoad3("__DLoad3", 41), 
    __ALoad0("__ALoad0", 42), 
    __ALoad1("__ALoad1", 43), 
    __ALoad2("__ALoad2", 44), 
    __ALoad3("__ALoad3", 45), 
    __IALoad("__IALoad", 46), 
    __LALoad("__LALoad", 47), 
    __FALoad("__FALoad", 48), 
    __DALoad("__DALoad", 49), 
    __AALoad("__AALoad", 50), 
    __BALoad("__BALoad", 51), 
    __CALoad("__CALoad", 52), 
    __SALoad("__SALoad", 53), 
    __IStore("__IStore", 54), 
    __LStore("__LStore", 55), 
    __FStore("__FStore", 56), 
    __DStore("__DStore", 57), 
    __AStore("__AStore", 58), 
    __IStore0("__IStore0", 59), 
    __IStore1("__IStore1", 60), 
    __IStore2("__IStore2", 61), 
    __IStore3("__IStore3", 62), 
    __LStore0("__LStore0", 63), 
    __LStore1("__LStore1", 64), 
    __LStore2("__LStore2", 65), 
    __LStore3("__LStore3", 66), 
    __FStore0("__FStore0", 67), 
    __FStore1("__FStore1", 68), 
    __FStore2("__FStore2", 69), 
    __FStore3("__FStore3", 70), 
    __DStore0("__DStore0", 71), 
    __DStore1("__DStore1", 72), 
    __DStore2("__DStore2", 73), 
    __DStore3("__DStore3", 74), 
    __AStore0("__AStore0", 75), 
    __AStore1("__AStore1", 76), 
    __AStore2("__AStore2", 77), 
    __AStore3("__AStore3", 78), 
    __IAStore("__IAStore", 79), 
    __LAStore("__LAStore", 80), 
    __FAStore("__FAStore", 81), 
    __DAStore("__DAStore", 82), 
    __AAStore("__AAStore", 83), 
    __BAStore("__BAStore", 84), 
    __CAStore("__CAStore", 85), 
    __SAStore("__SAStore", 86), 
    Pop("Pop", 87), 
    Pop2("Pop2", 88), 
    Dup("Dup", 89), 
    DupX1("DupX1", 90), 
    DupX2("DupX2", 91), 
    Dup2("Dup2", 92), 
    Dup2X1("Dup2X1", 93), 
    Dup2X2("Dup2X2", 94), 
    Swap("Swap", 95), 
    __IAdd("__IAdd", 96), 
    __LAdd("__LAdd", 97), 
    __FAdd("__FAdd", 98), 
    __DAdd("__DAdd", 99), 
    __ISub("__ISub", 100), 
    __LSub("__LSub", 101), 
    __FSub("__FSub", 102), 
    __DSub("__DSub", 103), 
    __IMul("__IMul", 104), 
    __LMul("__LMul", 105), 
    __FMul("__FMul", 106), 
    __DMul("__DMul", 107), 
    __IDiv("__IDiv", 108), 
    __LDiv("__LDiv", 109), 
    __FDiv("__FDiv", 110), 
    __DDiv("__DDiv", 111), 
    __IRem("__IRem", 112), 
    __LRem("__LRem", 113), 
    __FRem("__FRem", 114), 
    __DRem("__DRem", 115), 
    __INeg("__INeg", 116), 
    __LNeg("__LNeg", 117), 
    __FNeg("__FNeg", 118), 
    __DNeg("__DNeg", 119), 
    __IShl("__IShl", 120), 
    __LShl("__LShl", 121), 
    __IShr("__IShr", 122), 
    __LShr("__LShr", 123), 
    __IUShr("__IUShr", 124), 
    __LUShr("__LUShr", 125), 
    __IAnd("__IAnd", 126), 
    __LAnd("__LAnd", 127), 
    __IOr("__IOr", 128), 
    __LOr("__LOr", 129), 
    __IXor("__IXor", 130), 
    __LXor("__LXor", 131), 
    __IInc("__IInc", 132), 
    I2L("I2L", 133), 
    I2F("I2F", 134), 
    I2D("I2D", 135), 
    L2I("L2I", 136), 
    L2F("L2F", 137), 
    L2D("L2D", 138), 
    F2I("F2I", 139), 
    F2L("F2L", 140), 
    F2D("F2D", 141), 
    D2I("D2I", 142), 
    D2L("D2L", 143), 
    D2F("D2F", 144), 
    I2B("I2B", 145), 
    I2C("I2C", 146), 
    I2S("I2S", 147), 
    __LCmp("__LCmp", 148), 
    __FCmpL("__FCmpL", 149), 
    __FCmpG("__FCmpG", 150), 
    __DCmpL("__DCmpL", 151), 
    __DCmpG("__DCmpG", 152), 
    __IfEq("__IfEq", 153), 
    __IfNe("__IfNe", 154), 
    __IfLt("__IfLt", 155), 
    __IfGe("__IfGe", 156), 
    __IfGt("__IfGt", 157), 
    __IfLe("__IfLe", 158), 
    __IfICmpEq("__IfICmpEq", 159), 
    __IfICmpNe("__IfICmpNe", 160), 
    __IfICmpLt("__IfICmpLt", 161), 
    __IfICmpGe("__IfICmpGe", 162), 
    __IfICmpGt("__IfICmpGt", 163), 
    __IfICmpLe("__IfICmpLe", 164), 
    __IfACmpEq("__IfACmpEq", 165), 
    __IfACmpNe("__IfACmpNe", 166), 
    Goto("Goto", 167), 
    Jsr("Jsr", 168), 
    Ret("Ret", 169), 
    __TableSwitch("__TableSwitch", 170), 
    __LookupSwitch("__LookupSwitch", 171), 
    __IReturn("__IReturn", 172), 
    __LReturn("__LReturn", 173), 
    __FReturn("__FReturn", 174), 
    __DReturn("__DReturn", 175), 
    __AReturn("__AReturn", 176), 
    __Return("__Return", 177), 
    GetStatic("GetStatic", 178), 
    PutStatic("PutStatic", 179), 
    GetField("GetField", 180), 
    PutField("PutField", 181), 
    InvokeVirtual("InvokeVirtual", 182), 
    InvokeSpecial("InvokeSpecial", 183), 
    InvokeStatic("InvokeStatic", 184), 
    InvokeInterface("InvokeInterface", 185), 
    InvokeDynamic("InvokeDynamic", 186), 
    __New("__New", 187), 
    __NewArray("__NewArray", 188), 
    __ANewArray("__ANewArray", 189), 
    ArrayLength("ArrayLength", 190), 
    AThrow("AThrow", 191), 
    CheckCast("CheckCast", 192), 
    InstanceOf("InstanceOf", 193), 
    MonitorEnter("MonitorEnter", 194), 
    MonitorExit("MonitorExit", 195), 
    MultiANewArray("MultiANewArray", 196), 
    __IfNull("__IfNull", 197), 
    __IfNonNull("__IfNonNull", 198), 
    __GotoW("__GotoW", 199), 
    __JsrW("__JsrW", 200), 
    Breakpoint("Breakpoint", 201), 
    __ILoadW("__ILoadW", 202), 
    __LLoadW("__LLoadW", 203), 
    __FLoadW("__FLoadW", 204), 
    __DLoadW("__DLoadW", 205), 
    __ALoadW("__ALoadW", 206), 
    __IStoreW("__IStoreW", 207), 
    __LStoreW("__LStoreW", 208), 
    __FStoreW("__FStoreW", 209), 
    __DStoreW("__DStoreW", 210), 
    __AStoreW("__AStoreW", 211), 
    __IIncW("__IIncW", 212), 
    __RetW("__RetW", 213), 
    Leave("Leave", 214), 
    EndFinally("EndFinally", 215), 
    Load("Load", 216), 
    Store("Store", 217), 
    LoadElement("LoadElement", 218), 
    StoreElement("StoreElement", 219), 
    Add("Add", 220), 
    Sub("Sub", 221), 
    Mul("Mul", 222), 
    Div("Div", 223), 
    Rem("Rem", 224), 
    Neg("Neg", 225), 
    Shl("Shl", 226), 
    Shr("Shr", 227), 
    UShr("UShr", 228), 
    And("And", 229), 
    Or("Or", 230), 
    Not("Not", 231), 
    Xor("Xor", 232), 
    Inc("Inc", 233), 
    CmpEq("CmpEq", 234), 
    CmpNe("CmpNe", 235), 
    CmpLt("CmpLt", 236), 
    CmpGe("CmpGe", 237), 
    CmpGt("CmpGt", 238), 
    CmpLe("CmpLe", 239), 
    IfTrue("IfTrue", 240), 
    Return("Return", 241), 
    NewArray("NewArray", 242), 
    LoadException("LoadException", 243), 
    LogicalNot("LogicalNot", 244), 
    LogicalAnd("LogicalAnd", 245), 
    LogicalOr("LogicalOr", 246), 
    InitObject("InitObject", 247), 
    InitArray("InitArray", 248), 
    Switch("Switch", 249), 
    Wrap("Wrap", 250), 
    Bind("Bind", 251), 
    TernaryOp("TernaryOp", 252), 
    LoopOrSwitchBreak("LoopOrSwitchBreak", 253), 
    LoopContinue("LoopContinue", 254), 
    CompoundAssignment("CompoundAssignment", 255), 
    PreIncrement("PreIncrement", 256), 
    PostIncrement("PostIncrement", 257), 
    Box("Box", 258), 
    Unbox("Unbox", 259), 
    DefaultValue("DefaultValue", 260);
    
    private static final OpCode[] STANDARD_CODES;
    private static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$decompiler$ast$AstCode;
    
    static {
        STANDARD_CODES = OpCode.values();
    }
    
    public final String getName() {
        return StringUtilities.trimAndRemoveLeft(this.name().toLowerCase(), "__");
    }
    
    public final boolean isLoad() {
        switch ($SWITCH_TABLE$com$strobel$decompiler$ast$AstCode()[this.ordinal()]) {
            case 170:
            case 217:
            case 234: {
                return true;
            }
            default: {
                return false;
            }
        }
    }
    
    public final boolean isFieldRead() {
        switch ($SWITCH_TABLE$com$strobel$decompiler$ast$AstCode()[this.ordinal()]) {
            case 179:
            case 181: {
                return true;
            }
            default: {
                return false;
            }
        }
    }
    
    public final boolean isFieldWrite() {
        switch ($SWITCH_TABLE$com$strobel$decompiler$ast$AstCode()[this.ordinal()]) {
            case 180:
            case 182: {
                return true;
            }
            default: {
                return false;
            }
        }
    }
    
    public final boolean isStore() {
        switch ($SWITCH_TABLE$com$strobel$decompiler$ast$AstCode()[this.ordinal()]) {
            case 218:
            case 234:
            case 257:
            case 258: {
                return true;
            }
            default: {
                return false;
            }
        }
    }
    
    public final boolean isDup() {
        switch ($SWITCH_TABLE$com$strobel$decompiler$ast$AstCode()[this.ordinal()]) {
            case 90:
            case 91:
            case 92:
            case 93:
            case 94:
            case 95: {
                return true;
            }
            default: {
                return false;
            }
        }
    }
    
    public final boolean isComparison() {
        switch ($SWITCH_TABLE$com$strobel$decompiler$ast$AstCode()[this.ordinal()]) {
            case 235:
            case 236:
            case 237:
            case 238:
            case 239:
            case 240: {
                return true;
            }
            default: {
                return false;
            }
        }
    }
    
    public final boolean isLogical() {
        switch ($SWITCH_TABLE$com$strobel$decompiler$ast$AstCode()[this.ordinal()]) {
            case 245:
            case 246:
            case 247: {
                return true;
            }
            default: {
                return false;
            }
        }
    }
    
    public final boolean isShortCircuiting() {
        switch ($SWITCH_TABLE$com$strobel$decompiler$ast$AstCode()[this.ordinal()]) {
            case 246:
            case 247: {
                return true;
            }
            default: {
                return false;
            }
        }
    }
    
    public final boolean isWriteOperation() {
        switch ($SWITCH_TABLE$com$strobel$decompiler$ast$AstCode()[this.ordinal()]) {
            case 180:
            case 182:
            case 218:
            case 220: {
                return true;
            }
            default: {
                return false;
            }
        }
    }
    
    public final AstCode reverse() {
        switch ($SWITCH_TABLE$com$strobel$decompiler$ast$AstCode()[this.ordinal()]) {
            case 235: {
                return AstCode.CmpNe;
            }
            case 236: {
                return AstCode.CmpEq;
            }
            case 240: {
                return AstCode.CmpGt;
            }
            case 239: {
                return AstCode.CmpLe;
            }
            case 238: {
                return AstCode.CmpLt;
            }
            case 237: {
                return AstCode.CmpGe;
            }
            case 246: {
                return AstCode.LogicalOr;
            }
            case 247: {
                return AstCode.LogicalAnd;
            }
            default: {
                return this;
            }
        }
    }
    
    public final boolean isConditionalControlFlow() {
        final int ordinal = this.ordinal();
        if (ordinal < AstCode.STANDARD_CODES.length) {
            final OpCode standardCode = AstCode.STANDARD_CODES[ordinal];
            return standardCode.isBranch() && !standardCode.isUnconditionalBranch();
        }
        return this == AstCode.IfTrue;
    }
    
    public final boolean isUnconditionalControlFlow() {
        switch ($SWITCH_TABLE$com$strobel$decompiler$ast$AstCode()[this.ordinal()]) {
            case 215:
            case 216:
            case 242:
            case 250:
            case 254:
            case 255: {
                return true;
            }
            default: {
                final int ordinal = this.ordinal();
                if (ordinal < AstCode.STANDARD_CODES.length) {
                    final OpCode standardCode = AstCode.STANDARD_CODES[ordinal];
                    return standardCode.isUnconditionalBranch();
                }
                return false;
            }
        }
    }
    
    public static boolean expandMacro(final StrongBox<AstCode> code, final StrongBox<Object> operand, final MethodBody body, final int offset) {
        final AstCode op = code.get();
        switch ($SWITCH_TABLE$com$strobel$decompiler$ast$AstCode()[op.ordinal()]) {
            case 3: {
                code.set(AstCode.LdC);
                operand.set(-1);
                return true;
            }
            case 4: {
                code.set(AstCode.LdC);
                operand.set(0);
                return true;
            }
            case 5: {
                code.set(AstCode.LdC);
                operand.set(1);
                return true;
            }
            case 6: {
                code.set(AstCode.LdC);
                operand.set(2);
                return true;
            }
            case 7: {
                code.set(AstCode.LdC);
                operand.set(3);
                return true;
            }
            case 8: {
                code.set(AstCode.LdC);
                operand.set(4);
                return true;
            }
            case 9: {
                code.set(AstCode.LdC);
                operand.set(5);
                return true;
            }
            case 10: {
                code.set(AstCode.LdC);
                operand.set(0L);
                return true;
            }
            case 11: {
                code.set(AstCode.LdC);
                operand.set(1L);
                return true;
            }
            case 12: {
                code.set(AstCode.LdC);
                operand.set(0.0f);
                return true;
            }
            case 13: {
                code.set(AstCode.LdC);
                operand.set(1.0f);
                return true;
            }
            case 14: {
                code.set(AstCode.LdC);
                operand.set(2.0f);
                return true;
            }
            case 15: {
                code.set(AstCode.LdC);
                operand.set(0.0);
                return true;
            }
            case 16: {
                code.set(AstCode.LdC);
                operand.set(1.0);
                return true;
            }
            case 17:
            case 18: {
                code.set(AstCode.LdC);
                operand.set(operand.get().intValue());
                return true;
            }
            case 20:
            case 21: {
                code.set(AstCode.LdC);
                return true;
            }
            case 201: {
                code.set(AstCode.Jsr);
                return true;
            }
            case 214: {
                code.set(AstCode.Ret);
                return true;
            }
            case 133:
            case 213: {
                code.set(AstCode.Inc);
                return true;
            }
            case 173:
            case 174:
            case 175:
            case 176:
            case 177:
            case 178: {
                code.set(AstCode.Return);
                return true;
            }
            case 189:
            case 190: {
                code.set(AstCode.NewArray);
                return true;
            }
            case 22:
            case 23:
            case 24:
            case 25:
            case 26:
            case 203:
            case 204:
            case 205:
            case 206:
            case 207: {
                code.set(AstCode.Load);
                return true;
            }
            case 27: {
                code.set(AstCode.Load);
                operand.set(body.getVariables().reference(0, AstCode.STANDARD_CODES[op.ordinal()], offset));
                return true;
            }
            case 28: {
                code.set(AstCode.Load);
                operand.set(body.getVariables().reference(1, AstCode.STANDARD_CODES[op.ordinal()], offset));
                return true;
            }
            case 29: {
                code.set(AstCode.Load);
                operand.set(body.getVariables().reference(2, AstCode.STANDARD_CODES[op.ordinal()], offset));
                return true;
            }
            case 30: {
                code.set(AstCode.Load);
                operand.set(body.getVariables().reference(3, AstCode.STANDARD_CODES[op.ordinal()], offset));
                return true;
            }
            case 31: {
                code.set(AstCode.Load);
                operand.set(body.getVariables().reference(0, AstCode.STANDARD_CODES[op.ordinal()], offset));
                return true;
            }
            case 32: {
                code.set(AstCode.Load);
                operand.set(body.getVariables().reference(1, AstCode.STANDARD_CODES[op.ordinal()], offset));
                return true;
            }
            case 33: {
                code.set(AstCode.Load);
                operand.set(body.getVariables().reference(2, AstCode.STANDARD_CODES[op.ordinal()], offset));
                return true;
            }
            case 34: {
                code.set(AstCode.Load);
                operand.set(body.getVariables().reference(3, AstCode.STANDARD_CODES[op.ordinal()], offset));
                return true;
            }
            case 35: {
                code.set(AstCode.Load);
                operand.set(body.getVariables().reference(0, AstCode.STANDARD_CODES[op.ordinal()], offset));
                return true;
            }
            case 36: {
                code.set(AstCode.Load);
                operand.set(body.getVariables().reference(1, AstCode.STANDARD_CODES[op.ordinal()], offset));
                return true;
            }
            case 37: {
                code.set(AstCode.Load);
                operand.set(body.getVariables().reference(2, AstCode.STANDARD_CODES[op.ordinal()], offset));
                return true;
            }
            case 38: {
                code.set(AstCode.Load);
                operand.set(body.getVariables().reference(3, AstCode.STANDARD_CODES[op.ordinal()], offset));
                return true;
            }
            case 39: {
                code.set(AstCode.Load);
                operand.set(body.getVariables().reference(0, AstCode.STANDARD_CODES[op.ordinal()], offset));
                return true;
            }
            case 40: {
                code.set(AstCode.Load);
                operand.set(body.getVariables().reference(1, AstCode.STANDARD_CODES[op.ordinal()], offset));
                return true;
            }
            case 41: {
                code.set(AstCode.Load);
                operand.set(body.getVariables().reference(2, AstCode.STANDARD_CODES[op.ordinal()], offset));
                return true;
            }
            case 42: {
                code.set(AstCode.Load);
                operand.set(body.getVariables().reference(3, AstCode.STANDARD_CODES[op.ordinal()], offset));
                return true;
            }
            case 43: {
                code.set(AstCode.Load);
                operand.set(body.getVariables().reference(0, AstCode.STANDARD_CODES[op.ordinal()], offset));
                return true;
            }
            case 44: {
                code.set(AstCode.Load);
                operand.set(body.getVariables().reference(1, AstCode.STANDARD_CODES[op.ordinal()], offset));
                return true;
            }
            case 45: {
                code.set(AstCode.Load);
                operand.set(body.getVariables().reference(2, AstCode.STANDARD_CODES[op.ordinal()], offset));
                return true;
            }
            case 46: {
                code.set(AstCode.Load);
                operand.set(body.getVariables().reference(3, AstCode.STANDARD_CODES[op.ordinal()], offset));
                return true;
            }
            case 47:
            case 48:
            case 49:
            case 50:
            case 51:
            case 52:
            case 53:
            case 54: {
                code.set(AstCode.LoadElement);
                return true;
            }
            case 200: {
                code.set(AstCode.Goto);
                return true;
            }
            case 55:
            case 56:
            case 57:
            case 58:
            case 59:
            case 208:
            case 209:
            case 210:
            case 211:
            case 212: {
                code.set(AstCode.Store);
                return true;
            }
            case 60: {
                code.set(AstCode.Store);
                operand.set(body.getVariables().reference(0, AstCode.STANDARD_CODES[op.ordinal()], offset));
                return true;
            }
            case 61: {
                code.set(AstCode.Store);
                operand.set(body.getVariables().reference(1, AstCode.STANDARD_CODES[op.ordinal()], offset));
                return true;
            }
            case 62: {
                code.set(AstCode.Store);
                operand.set(body.getVariables().reference(2, AstCode.STANDARD_CODES[op.ordinal()], offset));
                return true;
            }
            case 63: {
                code.set(AstCode.Store);
                operand.set(body.getVariables().reference(3, AstCode.STANDARD_CODES[op.ordinal()], offset));
                return true;
            }
            case 64: {
                code.set(AstCode.Store);
                operand.set(body.getVariables().reference(0, AstCode.STANDARD_CODES[op.ordinal()], offset));
                return true;
            }
            case 65: {
                code.set(AstCode.Store);
                operand.set(body.getVariables().reference(1, AstCode.STANDARD_CODES[op.ordinal()], offset));
                return true;
            }
            case 66: {
                code.set(AstCode.Store);
                operand.set(body.getVariables().reference(2, AstCode.STANDARD_CODES[op.ordinal()], offset));
                return true;
            }
            case 67: {
                code.set(AstCode.Store);
                operand.set(body.getVariables().reference(3, AstCode.STANDARD_CODES[op.ordinal()], offset));
                return true;
            }
            case 68: {
                code.set(AstCode.Store);
                operand.set(body.getVariables().reference(0, AstCode.STANDARD_CODES[op.ordinal()], offset));
                return true;
            }
            case 69: {
                code.set(AstCode.Store);
                operand.set(body.getVariables().reference(1, AstCode.STANDARD_CODES[op.ordinal()], offset));
                return true;
            }
            case 70: {
                code.set(AstCode.Store);
                operand.set(body.getVariables().reference(2, AstCode.STANDARD_CODES[op.ordinal()], offset));
                return true;
            }
            case 71: {
                code.set(AstCode.Store);
                operand.set(body.getVariables().reference(3, AstCode.STANDARD_CODES[op.ordinal()], offset));
                return true;
            }
            case 72: {
                code.set(AstCode.Store);
                operand.set(body.getVariables().reference(0, AstCode.STANDARD_CODES[op.ordinal()], offset));
                return true;
            }
            case 73: {
                code.set(AstCode.Store);
                operand.set(body.getVariables().reference(1, AstCode.STANDARD_CODES[op.ordinal()], offset));
                return true;
            }
            case 74: {
                code.set(AstCode.Store);
                operand.set(body.getVariables().reference(2, AstCode.STANDARD_CODES[op.ordinal()], offset));
                return true;
            }
            case 75: {
                code.set(AstCode.Store);
                operand.set(body.getVariables().reference(3, AstCode.STANDARD_CODES[op.ordinal()], offset));
                return true;
            }
            case 76: {
                code.set(AstCode.Store);
                operand.set(body.getVariables().reference(0, AstCode.STANDARD_CODES[op.ordinal()], offset));
                return true;
            }
            case 77: {
                code.set(AstCode.Store);
                operand.set(body.getVariables().reference(1, AstCode.STANDARD_CODES[op.ordinal()], offset));
                return true;
            }
            case 78: {
                code.set(AstCode.Store);
                operand.set(body.getVariables().reference(2, AstCode.STANDARD_CODES[op.ordinal()], offset));
                return true;
            }
            case 79: {
                code.set(AstCode.Store);
                operand.set(body.getVariables().reference(3, AstCode.STANDARD_CODES[op.ordinal()], offset));
                return true;
            }
            case 80:
            case 81:
            case 82:
            case 83:
            case 84:
            case 85:
            case 86:
            case 87: {
                code.set(AstCode.StoreElement);
                return true;
            }
            case 97:
            case 98:
            case 99:
            case 100: {
                code.set(AstCode.Add);
                return true;
            }
            case 101:
            case 102:
            case 103:
            case 104: {
                code.set(AstCode.Sub);
                return true;
            }
            case 105:
            case 106:
            case 107:
            case 108: {
                code.set(AstCode.Mul);
                return true;
            }
            case 109:
            case 110:
            case 111:
            case 112: {
                code.set(AstCode.Div);
                return true;
            }
            case 113:
            case 114:
            case 115:
            case 116: {
                code.set(AstCode.Rem);
                return true;
            }
            case 117:
            case 118:
            case 119:
            case 120: {
                code.set(AstCode.Neg);
                return true;
            }
            case 121:
            case 122: {
                code.set(AstCode.Shl);
                return true;
            }
            case 123:
            case 124: {
                code.set(AstCode.Shr);
                return true;
            }
            case 125:
            case 126: {
                code.set(AstCode.UShr);
                return true;
            }
            case 127:
            case 128: {
                code.set(AstCode.And);
                return true;
            }
            case 129:
            case 130: {
                code.set(AstCode.Or);
                return true;
            }
            case 131:
            case 132: {
                code.set(AstCode.Xor);
                return true;
            }
            case 171:
            case 172: {
                code.set(AstCode.Switch);
                return true;
            }
            default: {
                return false;
            }
        }
    }
    
    static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$decompiler$ast$AstCode() {
        final int[] loc_0 = AstCode.$SWITCH_TABLE$com$strobel$decompiler$ast$AstCode;
        if (loc_0 != null) {
            return loc_0;
        }
        final int[] loc_1 = new int[values().length];
        try {
            loc_1[AstCode.AConstNull.ordinal()] = 2;
        }
        catch (NoSuchFieldError loc_2) {}
        try {
            loc_1[AstCode.AThrow.ordinal()] = 192;
        }
        catch (NoSuchFieldError loc_3) {}
        try {
            loc_1[AstCode.Add.ordinal()] = 221;
        }
        catch (NoSuchFieldError loc_4) {}
        try {
            loc_1[AstCode.And.ordinal()] = 230;
        }
        catch (NoSuchFieldError loc_5) {}
        try {
            loc_1[AstCode.ArrayLength.ordinal()] = 191;
        }
        catch (NoSuchFieldError loc_6) {}
        try {
            loc_1[AstCode.Bind.ordinal()] = 252;
        }
        catch (NoSuchFieldError loc_7) {}
        try {
            loc_1[AstCode.Box.ordinal()] = 259;
        }
        catch (NoSuchFieldError loc_8) {}
        try {
            loc_1[AstCode.Breakpoint.ordinal()] = 202;
        }
        catch (NoSuchFieldError loc_9) {}
        try {
            loc_1[AstCode.CheckCast.ordinal()] = 193;
        }
        catch (NoSuchFieldError loc_10) {}
        try {
            loc_1[AstCode.CmpEq.ordinal()] = 235;
        }
        catch (NoSuchFieldError loc_11) {}
        try {
            loc_1[AstCode.CmpGe.ordinal()] = 238;
        }
        catch (NoSuchFieldError loc_12) {}
        try {
            loc_1[AstCode.CmpGt.ordinal()] = 239;
        }
        catch (NoSuchFieldError loc_13) {}
        try {
            loc_1[AstCode.CmpLe.ordinal()] = 240;
        }
        catch (NoSuchFieldError loc_14) {}
        try {
            loc_1[AstCode.CmpLt.ordinal()] = 237;
        }
        catch (NoSuchFieldError loc_15) {}
        try {
            loc_1[AstCode.CmpNe.ordinal()] = 236;
        }
        catch (NoSuchFieldError loc_16) {}
        try {
            loc_1[AstCode.CompoundAssignment.ordinal()] = 256;
        }
        catch (NoSuchFieldError loc_17) {}
        try {
            loc_1[AstCode.D2F.ordinal()] = 145;
        }
        catch (NoSuchFieldError loc_18) {}
        try {
            loc_1[AstCode.D2I.ordinal()] = 143;
        }
        catch (NoSuchFieldError loc_19) {}
        try {
            loc_1[AstCode.D2L.ordinal()] = 144;
        }
        catch (NoSuchFieldError loc_20) {}
        try {
            loc_1[AstCode.DefaultValue.ordinal()] = 261;
        }
        catch (NoSuchFieldError loc_21) {}
        try {
            loc_1[AstCode.Div.ordinal()] = 224;
        }
        catch (NoSuchFieldError loc_22) {}
        try {
            loc_1[AstCode.Dup.ordinal()] = 90;
        }
        catch (NoSuchFieldError loc_23) {}
        try {
            loc_1[AstCode.Dup2.ordinal()] = 93;
        }
        catch (NoSuchFieldError loc_24) {}
        try {
            loc_1[AstCode.Dup2X1.ordinal()] = 94;
        }
        catch (NoSuchFieldError loc_25) {}
        try {
            loc_1[AstCode.Dup2X2.ordinal()] = 95;
        }
        catch (NoSuchFieldError loc_26) {}
        try {
            loc_1[AstCode.DupX1.ordinal()] = 91;
        }
        catch (NoSuchFieldError loc_27) {}
        try {
            loc_1[AstCode.DupX2.ordinal()] = 92;
        }
        catch (NoSuchFieldError loc_28) {}
        try {
            loc_1[AstCode.EndFinally.ordinal()] = 216;
        }
        catch (NoSuchFieldError loc_29) {}
        try {
            loc_1[AstCode.F2D.ordinal()] = 142;
        }
        catch (NoSuchFieldError loc_30) {}
        try {
            loc_1[AstCode.F2I.ordinal()] = 140;
        }
        catch (NoSuchFieldError loc_31) {}
        try {
            loc_1[AstCode.F2L.ordinal()] = 141;
        }
        catch (NoSuchFieldError loc_32) {}
        try {
            loc_1[AstCode.GetField.ordinal()] = 181;
        }
        catch (NoSuchFieldError loc_33) {}
        try {
            loc_1[AstCode.GetStatic.ordinal()] = 179;
        }
        catch (NoSuchFieldError loc_34) {}
        try {
            loc_1[AstCode.Goto.ordinal()] = 168;
        }
        catch (NoSuchFieldError loc_35) {}
        try {
            loc_1[AstCode.I2B.ordinal()] = 146;
        }
        catch (NoSuchFieldError loc_36) {}
        try {
            loc_1[AstCode.I2C.ordinal()] = 147;
        }
        catch (NoSuchFieldError loc_37) {}
        try {
            loc_1[AstCode.I2D.ordinal()] = 136;
        }
        catch (NoSuchFieldError loc_38) {}
        try {
            loc_1[AstCode.I2F.ordinal()] = 135;
        }
        catch (NoSuchFieldError loc_39) {}
        try {
            loc_1[AstCode.I2L.ordinal()] = 134;
        }
        catch (NoSuchFieldError loc_40) {}
        try {
            loc_1[AstCode.I2S.ordinal()] = 148;
        }
        catch (NoSuchFieldError loc_41) {}
        try {
            loc_1[AstCode.IfTrue.ordinal()] = 241;
        }
        catch (NoSuchFieldError loc_42) {}
        try {
            loc_1[AstCode.Inc.ordinal()] = 234;
        }
        catch (NoSuchFieldError loc_43) {}
        try {
            loc_1[AstCode.InitArray.ordinal()] = 249;
        }
        catch (NoSuchFieldError loc_44) {}
        try {
            loc_1[AstCode.InitObject.ordinal()] = 248;
        }
        catch (NoSuchFieldError loc_45) {}
        try {
            loc_1[AstCode.InstanceOf.ordinal()] = 194;
        }
        catch (NoSuchFieldError loc_46) {}
        try {
            loc_1[AstCode.InvokeDynamic.ordinal()] = 187;
        }
        catch (NoSuchFieldError loc_47) {}
        try {
            loc_1[AstCode.InvokeInterface.ordinal()] = 186;
        }
        catch (NoSuchFieldError loc_48) {}
        try {
            loc_1[AstCode.InvokeSpecial.ordinal()] = 184;
        }
        catch (NoSuchFieldError loc_49) {}
        try {
            loc_1[AstCode.InvokeStatic.ordinal()] = 185;
        }
        catch (NoSuchFieldError loc_50) {}
        try {
            loc_1[AstCode.InvokeVirtual.ordinal()] = 183;
        }
        catch (NoSuchFieldError loc_51) {}
        try {
            loc_1[AstCode.Jsr.ordinal()] = 169;
        }
        catch (NoSuchFieldError loc_52) {}
        try {
            loc_1[AstCode.L2D.ordinal()] = 139;
        }
        catch (NoSuchFieldError loc_53) {}
        try {
            loc_1[AstCode.L2F.ordinal()] = 138;
        }
        catch (NoSuchFieldError loc_54) {}
        try {
            loc_1[AstCode.L2I.ordinal()] = 137;
        }
        catch (NoSuchFieldError loc_55) {}
        try {
            loc_1[AstCode.LdC.ordinal()] = 19;
        }
        catch (NoSuchFieldError loc_56) {}
        try {
            loc_1[AstCode.Leave.ordinal()] = 215;
        }
        catch (NoSuchFieldError loc_57) {}
        try {
            loc_1[AstCode.Load.ordinal()] = 217;
        }
        catch (NoSuchFieldError loc_58) {}
        try {
            loc_1[AstCode.LoadElement.ordinal()] = 219;
        }
        catch (NoSuchFieldError loc_59) {}
        try {
            loc_1[AstCode.LoadException.ordinal()] = 244;
        }
        catch (NoSuchFieldError loc_60) {}
        try {
            loc_1[AstCode.LogicalAnd.ordinal()] = 246;
        }
        catch (NoSuchFieldError loc_61) {}
        try {
            loc_1[AstCode.LogicalNot.ordinal()] = 245;
        }
        catch (NoSuchFieldError loc_62) {}
        try {
            loc_1[AstCode.LogicalOr.ordinal()] = 247;
        }
        catch (NoSuchFieldError loc_63) {}
        try {
            loc_1[AstCode.LoopContinue.ordinal()] = 255;
        }
        catch (NoSuchFieldError loc_64) {}
        try {
            loc_1[AstCode.LoopOrSwitchBreak.ordinal()] = 254;
        }
        catch (NoSuchFieldError loc_65) {}
        try {
            loc_1[AstCode.MonitorEnter.ordinal()] = 195;
        }
        catch (NoSuchFieldError loc_66) {}
        try {
            loc_1[AstCode.MonitorExit.ordinal()] = 196;
        }
        catch (NoSuchFieldError loc_67) {}
        try {
            loc_1[AstCode.Mul.ordinal()] = 223;
        }
        catch (NoSuchFieldError loc_68) {}
        try {
            loc_1[AstCode.MultiANewArray.ordinal()] = 197;
        }
        catch (NoSuchFieldError loc_69) {}
        try {
            loc_1[AstCode.Neg.ordinal()] = 226;
        }
        catch (NoSuchFieldError loc_70) {}
        try {
            loc_1[AstCode.NewArray.ordinal()] = 243;
        }
        catch (NoSuchFieldError loc_71) {}
        try {
            loc_1[AstCode.Nop.ordinal()] = 1;
        }
        catch (NoSuchFieldError loc_72) {}
        try {
            loc_1[AstCode.Not.ordinal()] = 232;
        }
        catch (NoSuchFieldError loc_73) {}
        try {
            loc_1[AstCode.Or.ordinal()] = 231;
        }
        catch (NoSuchFieldError loc_74) {}
        try {
            loc_1[AstCode.Pop.ordinal()] = 88;
        }
        catch (NoSuchFieldError loc_75) {}
        try {
            loc_1[AstCode.Pop2.ordinal()] = 89;
        }
        catch (NoSuchFieldError loc_76) {}
        try {
            loc_1[AstCode.PostIncrement.ordinal()] = 258;
        }
        catch (NoSuchFieldError loc_77) {}
        try {
            loc_1[AstCode.PreIncrement.ordinal()] = 257;
        }
        catch (NoSuchFieldError loc_78) {}
        try {
            loc_1[AstCode.PutField.ordinal()] = 182;
        }
        catch (NoSuchFieldError loc_79) {}
        try {
            loc_1[AstCode.PutStatic.ordinal()] = 180;
        }
        catch (NoSuchFieldError loc_80) {}
        try {
            loc_1[AstCode.Rem.ordinal()] = 225;
        }
        catch (NoSuchFieldError loc_81) {}
        try {
            loc_1[AstCode.Ret.ordinal()] = 170;
        }
        catch (NoSuchFieldError loc_82) {}
        try {
            loc_1[AstCode.Return.ordinal()] = 242;
        }
        catch (NoSuchFieldError loc_83) {}
        try {
            loc_1[AstCode.Shl.ordinal()] = 227;
        }
        catch (NoSuchFieldError loc_84) {}
        try {
            loc_1[AstCode.Shr.ordinal()] = 228;
        }
        catch (NoSuchFieldError loc_85) {}
        try {
            loc_1[AstCode.Store.ordinal()] = 218;
        }
        catch (NoSuchFieldError loc_86) {}
        try {
            loc_1[AstCode.StoreElement.ordinal()] = 220;
        }
        catch (NoSuchFieldError loc_87) {}
        try {
            loc_1[AstCode.Sub.ordinal()] = 222;
        }
        catch (NoSuchFieldError loc_88) {}
        try {
            loc_1[AstCode.Swap.ordinal()] = 96;
        }
        catch (NoSuchFieldError loc_89) {}
        try {
            loc_1[AstCode.Switch.ordinal()] = 250;
        }
        catch (NoSuchFieldError loc_90) {}
        try {
            loc_1[AstCode.TernaryOp.ordinal()] = 253;
        }
        catch (NoSuchFieldError loc_91) {}
        try {
            loc_1[AstCode.UShr.ordinal()] = 229;
        }
        catch (NoSuchFieldError loc_92) {}
        try {
            loc_1[AstCode.Unbox.ordinal()] = 260;
        }
        catch (NoSuchFieldError loc_93) {}
        try {
            loc_1[AstCode.Wrap.ordinal()] = 251;
        }
        catch (NoSuchFieldError loc_94) {}
        try {
            loc_1[AstCode.Xor.ordinal()] = 233;
        }
        catch (NoSuchFieldError loc_95) {}
        try {
            loc_1[AstCode.__AALoad.ordinal()] = 51;
        }
        catch (NoSuchFieldError loc_96) {}
        try {
            loc_1[AstCode.__AAStore.ordinal()] = 84;
        }
        catch (NoSuchFieldError loc_97) {}
        try {
            loc_1[AstCode.__ALoad.ordinal()] = 26;
        }
        catch (NoSuchFieldError loc_98) {}
        try {
            loc_1[AstCode.__ALoad0.ordinal()] = 43;
        }
        catch (NoSuchFieldError loc_99) {}
        try {
            loc_1[AstCode.__ALoad1.ordinal()] = 44;
        }
        catch (NoSuchFieldError loc_100) {}
        try {
            loc_1[AstCode.__ALoad2.ordinal()] = 45;
        }
        catch (NoSuchFieldError loc_101) {}
        try {
            loc_1[AstCode.__ALoad3.ordinal()] = 46;
        }
        catch (NoSuchFieldError loc_102) {}
        try {
            loc_1[AstCode.__ALoadW.ordinal()] = 207;
        }
        catch (NoSuchFieldError loc_103) {}
        try {
            loc_1[AstCode.__ANewArray.ordinal()] = 190;
        }
        catch (NoSuchFieldError loc_104) {}
        try {
            loc_1[AstCode.__AReturn.ordinal()] = 177;
        }
        catch (NoSuchFieldError loc_105) {}
        try {
            loc_1[AstCode.__AStore.ordinal()] = 59;
        }
        catch (NoSuchFieldError loc_106) {}
        try {
            loc_1[AstCode.__AStore0.ordinal()] = 76;
        }
        catch (NoSuchFieldError loc_107) {}
        try {
            loc_1[AstCode.__AStore1.ordinal()] = 77;
        }
        catch (NoSuchFieldError loc_108) {}
        try {
            loc_1[AstCode.__AStore2.ordinal()] = 78;
        }
        catch (NoSuchFieldError loc_109) {}
        try {
            loc_1[AstCode.__AStore3.ordinal()] = 79;
        }
        catch (NoSuchFieldError loc_110) {}
        try {
            loc_1[AstCode.__AStoreW.ordinal()] = 212;
        }
        catch (NoSuchFieldError loc_111) {}
        try {
            loc_1[AstCode.__BALoad.ordinal()] = 52;
        }
        catch (NoSuchFieldError loc_112) {}
        try {
            loc_1[AstCode.__BAStore.ordinal()] = 85;
        }
        catch (NoSuchFieldError loc_113) {}
        try {
            loc_1[AstCode.__BIPush.ordinal()] = 17;
        }
        catch (NoSuchFieldError loc_114) {}
        try {
            loc_1[AstCode.__CALoad.ordinal()] = 53;
        }
        catch (NoSuchFieldError loc_115) {}
        try {
            loc_1[AstCode.__CAStore.ordinal()] = 86;
        }
        catch (NoSuchFieldError loc_116) {}
        try {
            loc_1[AstCode.__DALoad.ordinal()] = 50;
        }
        catch (NoSuchFieldError loc_117) {}
        try {
            loc_1[AstCode.__DAStore.ordinal()] = 83;
        }
        catch (NoSuchFieldError loc_118) {}
        try {
            loc_1[AstCode.__DAdd.ordinal()] = 100;
        }
        catch (NoSuchFieldError loc_119) {}
        try {
            loc_1[AstCode.__DCmpG.ordinal()] = 153;
        }
        catch (NoSuchFieldError loc_120) {}
        try {
            loc_1[AstCode.__DCmpL.ordinal()] = 152;
        }
        catch (NoSuchFieldError loc_121) {}
        try {
            loc_1[AstCode.__DConst0.ordinal()] = 15;
        }
        catch (NoSuchFieldError loc_122) {}
        try {
            loc_1[AstCode.__DConst1.ordinal()] = 16;
        }
        catch (NoSuchFieldError loc_123) {}
        try {
            loc_1[AstCode.__DDiv.ordinal()] = 112;
        }
        catch (NoSuchFieldError loc_124) {}
        try {
            loc_1[AstCode.__DLoad.ordinal()] = 25;
        }
        catch (NoSuchFieldError loc_125) {}
        try {
            loc_1[AstCode.__DLoad0.ordinal()] = 39;
        }
        catch (NoSuchFieldError loc_126) {}
        try {
            loc_1[AstCode.__DLoad1.ordinal()] = 40;
        }
        catch (NoSuchFieldError loc_127) {}
        try {
            loc_1[AstCode.__DLoad2.ordinal()] = 41;
        }
        catch (NoSuchFieldError loc_128) {}
        try {
            loc_1[AstCode.__DLoad3.ordinal()] = 42;
        }
        catch (NoSuchFieldError loc_129) {}
        try {
            loc_1[AstCode.__DLoadW.ordinal()] = 206;
        }
        catch (NoSuchFieldError loc_130) {}
        try {
            loc_1[AstCode.__DMul.ordinal()] = 108;
        }
        catch (NoSuchFieldError loc_131) {}
        try {
            loc_1[AstCode.__DNeg.ordinal()] = 120;
        }
        catch (NoSuchFieldError loc_132) {}
        try {
            loc_1[AstCode.__DRem.ordinal()] = 116;
        }
        catch (NoSuchFieldError loc_133) {}
        try {
            loc_1[AstCode.__DReturn.ordinal()] = 176;
        }
        catch (NoSuchFieldError loc_134) {}
        try {
            loc_1[AstCode.__DStore.ordinal()] = 58;
        }
        catch (NoSuchFieldError loc_135) {}
        try {
            loc_1[AstCode.__DStore0.ordinal()] = 72;
        }
        catch (NoSuchFieldError loc_136) {}
        try {
            loc_1[AstCode.__DStore1.ordinal()] = 73;
        }
        catch (NoSuchFieldError loc_137) {}
        try {
            loc_1[AstCode.__DStore2.ordinal()] = 74;
        }
        catch (NoSuchFieldError loc_138) {}
        try {
            loc_1[AstCode.__DStore3.ordinal()] = 75;
        }
        catch (NoSuchFieldError loc_139) {}
        try {
            loc_1[AstCode.__DStoreW.ordinal()] = 211;
        }
        catch (NoSuchFieldError loc_140) {}
        try {
            loc_1[AstCode.__DSub.ordinal()] = 104;
        }
        catch (NoSuchFieldError loc_141) {}
        try {
            loc_1[AstCode.__FALoad.ordinal()] = 49;
        }
        catch (NoSuchFieldError loc_142) {}
        try {
            loc_1[AstCode.__FAStore.ordinal()] = 82;
        }
        catch (NoSuchFieldError loc_143) {}
        try {
            loc_1[AstCode.__FAdd.ordinal()] = 99;
        }
        catch (NoSuchFieldError loc_144) {}
        try {
            loc_1[AstCode.__FCmpG.ordinal()] = 151;
        }
        catch (NoSuchFieldError loc_145) {}
        try {
            loc_1[AstCode.__FCmpL.ordinal()] = 150;
        }
        catch (NoSuchFieldError loc_146) {}
        try {
            loc_1[AstCode.__FConst0.ordinal()] = 12;
        }
        catch (NoSuchFieldError loc_147) {}
        try {
            loc_1[AstCode.__FConst1.ordinal()] = 13;
        }
        catch (NoSuchFieldError loc_148) {}
        try {
            loc_1[AstCode.__FConst2.ordinal()] = 14;
        }
        catch (NoSuchFieldError loc_149) {}
        try {
            loc_1[AstCode.__FDiv.ordinal()] = 111;
        }
        catch (NoSuchFieldError loc_150) {}
        try {
            loc_1[AstCode.__FLoad.ordinal()] = 24;
        }
        catch (NoSuchFieldError loc_151) {}
        try {
            loc_1[AstCode.__FLoad0.ordinal()] = 35;
        }
        catch (NoSuchFieldError loc_152) {}
        try {
            loc_1[AstCode.__FLoad1.ordinal()] = 36;
        }
        catch (NoSuchFieldError loc_153) {}
        try {
            loc_1[AstCode.__FLoad2.ordinal()] = 37;
        }
        catch (NoSuchFieldError loc_154) {}
        try {
            loc_1[AstCode.__FLoad3.ordinal()] = 38;
        }
        catch (NoSuchFieldError loc_155) {}
        try {
            loc_1[AstCode.__FLoadW.ordinal()] = 205;
        }
        catch (NoSuchFieldError loc_156) {}
        try {
            loc_1[AstCode.__FMul.ordinal()] = 107;
        }
        catch (NoSuchFieldError loc_157) {}
        try {
            loc_1[AstCode.__FNeg.ordinal()] = 119;
        }
        catch (NoSuchFieldError loc_158) {}
        try {
            loc_1[AstCode.__FRem.ordinal()] = 115;
        }
        catch (NoSuchFieldError loc_159) {}
        try {
            loc_1[AstCode.__FReturn.ordinal()] = 175;
        }
        catch (NoSuchFieldError loc_160) {}
        try {
            loc_1[AstCode.__FStore.ordinal()] = 57;
        }
        catch (NoSuchFieldError loc_161) {}
        try {
            loc_1[AstCode.__FStore0.ordinal()] = 68;
        }
        catch (NoSuchFieldError loc_162) {}
        try {
            loc_1[AstCode.__FStore1.ordinal()] = 69;
        }
        catch (NoSuchFieldError loc_163) {}
        try {
            loc_1[AstCode.__FStore2.ordinal()] = 70;
        }
        catch (NoSuchFieldError loc_164) {}
        try {
            loc_1[AstCode.__FStore3.ordinal()] = 71;
        }
        catch (NoSuchFieldError loc_165) {}
        try {
            loc_1[AstCode.__FStoreW.ordinal()] = 210;
        }
        catch (NoSuchFieldError loc_166) {}
        try {
            loc_1[AstCode.__FSub.ordinal()] = 103;
        }
        catch (NoSuchFieldError loc_167) {}
        try {
            loc_1[AstCode.__GotoW.ordinal()] = 200;
        }
        catch (NoSuchFieldError loc_168) {}
        try {
            loc_1[AstCode.__IALoad.ordinal()] = 47;
        }
        catch (NoSuchFieldError loc_169) {}
        try {
            loc_1[AstCode.__IAStore.ordinal()] = 80;
        }
        catch (NoSuchFieldError loc_170) {}
        try {
            loc_1[AstCode.__IAdd.ordinal()] = 97;
        }
        catch (NoSuchFieldError loc_171) {}
        try {
            loc_1[AstCode.__IAnd.ordinal()] = 127;
        }
        catch (NoSuchFieldError loc_172) {}
        try {
            loc_1[AstCode.__IConst0.ordinal()] = 4;
        }
        catch (NoSuchFieldError loc_173) {}
        try {
            loc_1[AstCode.__IConst1.ordinal()] = 5;
        }
        catch (NoSuchFieldError loc_174) {}
        try {
            loc_1[AstCode.__IConst2.ordinal()] = 6;
        }
        catch (NoSuchFieldError loc_175) {}
        try {
            loc_1[AstCode.__IConst3.ordinal()] = 7;
        }
        catch (NoSuchFieldError loc_176) {}
        try {
            loc_1[AstCode.__IConst4.ordinal()] = 8;
        }
        catch (NoSuchFieldError loc_177) {}
        try {
            loc_1[AstCode.__IConst5.ordinal()] = 9;
        }
        catch (NoSuchFieldError loc_178) {}
        try {
            loc_1[AstCode.__IConstM1.ordinal()] = 3;
        }
        catch (NoSuchFieldError loc_179) {}
        try {
            loc_1[AstCode.__IDiv.ordinal()] = 109;
        }
        catch (NoSuchFieldError loc_180) {}
        try {
            loc_1[AstCode.__IInc.ordinal()] = 133;
        }
        catch (NoSuchFieldError loc_181) {}
        try {
            loc_1[AstCode.__IIncW.ordinal()] = 213;
        }
        catch (NoSuchFieldError loc_182) {}
        try {
            loc_1[AstCode.__ILoad.ordinal()] = 22;
        }
        catch (NoSuchFieldError loc_183) {}
        try {
            loc_1[AstCode.__ILoad0.ordinal()] = 27;
        }
        catch (NoSuchFieldError loc_184) {}
        try {
            loc_1[AstCode.__ILoad1.ordinal()] = 28;
        }
        catch (NoSuchFieldError loc_185) {}
        try {
            loc_1[AstCode.__ILoad2.ordinal()] = 29;
        }
        catch (NoSuchFieldError loc_186) {}
        try {
            loc_1[AstCode.__ILoad3.ordinal()] = 30;
        }
        catch (NoSuchFieldError loc_187) {}
        try {
            loc_1[AstCode.__ILoadW.ordinal()] = 203;
        }
        catch (NoSuchFieldError loc_188) {}
        try {
            loc_1[AstCode.__IMul.ordinal()] = 105;
        }
        catch (NoSuchFieldError loc_189) {}
        try {
            loc_1[AstCode.__INeg.ordinal()] = 117;
        }
        catch (NoSuchFieldError loc_190) {}
        try {
            loc_1[AstCode.__IOr.ordinal()] = 129;
        }
        catch (NoSuchFieldError loc_191) {}
        try {
            loc_1[AstCode.__IRem.ordinal()] = 113;
        }
        catch (NoSuchFieldError loc_192) {}
        try {
            loc_1[AstCode.__IReturn.ordinal()] = 173;
        }
        catch (NoSuchFieldError loc_193) {}
        try {
            loc_1[AstCode.__IShl.ordinal()] = 121;
        }
        catch (NoSuchFieldError loc_194) {}
        try {
            loc_1[AstCode.__IShr.ordinal()] = 123;
        }
        catch (NoSuchFieldError loc_195) {}
        try {
            loc_1[AstCode.__IStore.ordinal()] = 55;
        }
        catch (NoSuchFieldError loc_196) {}
        try {
            loc_1[AstCode.__IStore0.ordinal()] = 60;
        }
        catch (NoSuchFieldError loc_197) {}
        try {
            loc_1[AstCode.__IStore1.ordinal()] = 61;
        }
        catch (NoSuchFieldError loc_198) {}
        try {
            loc_1[AstCode.__IStore2.ordinal()] = 62;
        }
        catch (NoSuchFieldError loc_199) {}
        try {
            loc_1[AstCode.__IStore3.ordinal()] = 63;
        }
        catch (NoSuchFieldError loc_200) {}
        try {
            loc_1[AstCode.__IStoreW.ordinal()] = 208;
        }
        catch (NoSuchFieldError loc_201) {}
        try {
            loc_1[AstCode.__ISub.ordinal()] = 101;
        }
        catch (NoSuchFieldError loc_202) {}
        try {
            loc_1[AstCode.__IUShr.ordinal()] = 125;
        }
        catch (NoSuchFieldError loc_203) {}
        try {
            loc_1[AstCode.__IXor.ordinal()] = 131;
        }
        catch (NoSuchFieldError loc_204) {}
        try {
            loc_1[AstCode.__IfACmpEq.ordinal()] = 166;
        }
        catch (NoSuchFieldError loc_205) {}
        try {
            loc_1[AstCode.__IfACmpNe.ordinal()] = 167;
        }
        catch (NoSuchFieldError loc_206) {}
        try {
            loc_1[AstCode.__IfEq.ordinal()] = 154;
        }
        catch (NoSuchFieldError loc_207) {}
        try {
            loc_1[AstCode.__IfGe.ordinal()] = 157;
        }
        catch (NoSuchFieldError loc_208) {}
        try {
            loc_1[AstCode.__IfGt.ordinal()] = 158;
        }
        catch (NoSuchFieldError loc_209) {}
        try {
            loc_1[AstCode.__IfICmpEq.ordinal()] = 160;
        }
        catch (NoSuchFieldError loc_210) {}
        try {
            loc_1[AstCode.__IfICmpGe.ordinal()] = 163;
        }
        catch (NoSuchFieldError loc_211) {}
        try {
            loc_1[AstCode.__IfICmpGt.ordinal()] = 164;
        }
        catch (NoSuchFieldError loc_212) {}
        try {
            loc_1[AstCode.__IfICmpLe.ordinal()] = 165;
        }
        catch (NoSuchFieldError loc_213) {}
        try {
            loc_1[AstCode.__IfICmpLt.ordinal()] = 162;
        }
        catch (NoSuchFieldError loc_214) {}
        try {
            loc_1[AstCode.__IfICmpNe.ordinal()] = 161;
        }
        catch (NoSuchFieldError loc_215) {}
        try {
            loc_1[AstCode.__IfLe.ordinal()] = 159;
        }
        catch (NoSuchFieldError loc_216) {}
        try {
            loc_1[AstCode.__IfLt.ordinal()] = 156;
        }
        catch (NoSuchFieldError loc_217) {}
        try {
            loc_1[AstCode.__IfNe.ordinal()] = 155;
        }
        catch (NoSuchFieldError loc_218) {}
        try {
            loc_1[AstCode.__IfNonNull.ordinal()] = 199;
        }
        catch (NoSuchFieldError loc_219) {}
        try {
            loc_1[AstCode.__IfNull.ordinal()] = 198;
        }
        catch (NoSuchFieldError loc_220) {}
        try {
            loc_1[AstCode.__JsrW.ordinal()] = 201;
        }
        catch (NoSuchFieldError loc_221) {}
        try {
            loc_1[AstCode.__LALoad.ordinal()] = 48;
        }
        catch (NoSuchFieldError loc_222) {}
        try {
            loc_1[AstCode.__LAStore.ordinal()] = 81;
        }
        catch (NoSuchFieldError loc_223) {}
        try {
            loc_1[AstCode.__LAdd.ordinal()] = 98;
        }
        catch (NoSuchFieldError loc_224) {}
        try {
            loc_1[AstCode.__LAnd.ordinal()] = 128;
        }
        catch (NoSuchFieldError loc_225) {}
        try {
            loc_1[AstCode.__LCmp.ordinal()] = 149;
        }
        catch (NoSuchFieldError loc_226) {}
        try {
            loc_1[AstCode.__LConst0.ordinal()] = 10;
        }
        catch (NoSuchFieldError loc_227) {}
        try {
            loc_1[AstCode.__LConst1.ordinal()] = 11;
        }
        catch (NoSuchFieldError loc_228) {}
        try {
            loc_1[AstCode.__LDiv.ordinal()] = 110;
        }
        catch (NoSuchFieldError loc_229) {}
        try {
            loc_1[AstCode.__LLoad.ordinal()] = 23;
        }
        catch (NoSuchFieldError loc_230) {}
        try {
            loc_1[AstCode.__LLoad0.ordinal()] = 31;
        }
        catch (NoSuchFieldError loc_231) {}
        try {
            loc_1[AstCode.__LLoad1.ordinal()] = 32;
        }
        catch (NoSuchFieldError loc_232) {}
        try {
            loc_1[AstCode.__LLoad2.ordinal()] = 33;
        }
        catch (NoSuchFieldError loc_233) {}
        try {
            loc_1[AstCode.__LLoad3.ordinal()] = 34;
        }
        catch (NoSuchFieldError loc_234) {}
        try {
            loc_1[AstCode.__LLoadW.ordinal()] = 204;
        }
        catch (NoSuchFieldError loc_235) {}
        try {
            loc_1[AstCode.__LMul.ordinal()] = 106;
        }
        catch (NoSuchFieldError loc_236) {}
        try {
            loc_1[AstCode.__LNeg.ordinal()] = 118;
        }
        catch (NoSuchFieldError loc_237) {}
        try {
            loc_1[AstCode.__LOr.ordinal()] = 130;
        }
        catch (NoSuchFieldError loc_238) {}
        try {
            loc_1[AstCode.__LRem.ordinal()] = 114;
        }
        catch (NoSuchFieldError loc_239) {}
        try {
            loc_1[AstCode.__LReturn.ordinal()] = 174;
        }
        catch (NoSuchFieldError loc_240) {}
        try {
            loc_1[AstCode.__LShl.ordinal()] = 122;
        }
        catch (NoSuchFieldError loc_241) {}
        try {
            loc_1[AstCode.__LShr.ordinal()] = 124;
        }
        catch (NoSuchFieldError loc_242) {}
        try {
            loc_1[AstCode.__LStore.ordinal()] = 56;
        }
        catch (NoSuchFieldError loc_243) {}
        try {
            loc_1[AstCode.__LStore0.ordinal()] = 64;
        }
        catch (NoSuchFieldError loc_244) {}
        try {
            loc_1[AstCode.__LStore1.ordinal()] = 65;
        }
        catch (NoSuchFieldError loc_245) {}
        try {
            loc_1[AstCode.__LStore2.ordinal()] = 66;
        }
        catch (NoSuchFieldError loc_246) {}
        try {
            loc_1[AstCode.__LStore3.ordinal()] = 67;
        }
        catch (NoSuchFieldError loc_247) {}
        try {
            loc_1[AstCode.__LStoreW.ordinal()] = 209;
        }
        catch (NoSuchFieldError loc_248) {}
        try {
            loc_1[AstCode.__LSub.ordinal()] = 102;
        }
        catch (NoSuchFieldError loc_249) {}
        try {
            loc_1[AstCode.__LUShr.ordinal()] = 126;
        }
        catch (NoSuchFieldError loc_250) {}
        try {
            loc_1[AstCode.__LXor.ordinal()] = 132;
        }
        catch (NoSuchFieldError loc_251) {}
        try {
            loc_1[AstCode.__LdC2W.ordinal()] = 21;
        }
        catch (NoSuchFieldError loc_252) {}
        try {
            loc_1[AstCode.__LdCW.ordinal()] = 20;
        }
        catch (NoSuchFieldError loc_253) {}
        try {
            loc_1[AstCode.__LookupSwitch.ordinal()] = 172;
        }
        catch (NoSuchFieldError loc_254) {}
        try {
            loc_1[AstCode.__New.ordinal()] = 188;
        }
        catch (NoSuchFieldError loc_255) {}
        try {
            loc_1[AstCode.__NewArray.ordinal()] = 189;
        }
        catch (NoSuchFieldError loc_256) {}
        try {
            loc_1[AstCode.__RetW.ordinal()] = 214;
        }
        catch (NoSuchFieldError loc_257) {}
        try {
            loc_1[AstCode.__Return.ordinal()] = 178;
        }
        catch (NoSuchFieldError loc_258) {}
        try {
            loc_1[AstCode.__SALoad.ordinal()] = 54;
        }
        catch (NoSuchFieldError loc_259) {}
        try {
            loc_1[AstCode.__SAStore.ordinal()] = 87;
        }
        catch (NoSuchFieldError loc_260) {}
        try {
            loc_1[AstCode.__SIPush.ordinal()] = 18;
        }
        catch (NoSuchFieldError loc_261) {}
        try {
            loc_1[AstCode.__TableSwitch.ordinal()] = 171;
        }
        catch (NoSuchFieldError loc_262) {}
        return AstCode.$SWITCH_TABLE$com$strobel$decompiler$ast$AstCode = loc_1;
    }
}
