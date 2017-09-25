package com.strobel.decompiler.ast;

public final class AstCodeHelpers
{
    private static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$decompiler$ast$AstCode;
    
    public static boolean isLocalStore(final AstCode code) {
        if (code == null) {
            return false;
        }
        switch ($SWITCH_TABLE$com$strobel$decompiler$ast$AstCode()[code.ordinal()]) {
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
            case 218: {
                return true;
            }
            default: {
                return false;
            }
        }
    }
    
    public static boolean isLocalLoad(final AstCode code) {
        if (code == null) {
            return false;
        }
        switch ($SWITCH_TABLE$com$strobel$decompiler$ast$AstCode()[code.ordinal()]) {
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
            case 217: {
                return true;
            }
            default: {
                return false;
            }
        }
    }
    
    public static int getLoadStoreMacroArgumentIndex(final AstCode code) {
        if (code == null) {
            return -1;
        }
        switch ($SWITCH_TABLE$com$strobel$decompiler$ast$AstCode()[code.ordinal()]) {
            case 27:
            case 31:
            case 35:
            case 39:
            case 43:
            case 60:
            case 64:
            case 68:
            case 72:
            case 76: {
                return 0;
            }
            case 28:
            case 32:
            case 36:
            case 40:
            case 44:
            case 61:
            case 65:
            case 69:
            case 73:
            case 77: {
                return 1;
            }
            case 29:
            case 33:
            case 37:
            case 41:
            case 45:
            case 62:
            case 66:
            case 70:
            case 74:
            case 78: {
                return 2;
            }
            case 30:
            case 34:
            case 38:
            case 42:
            case 46:
            case 63:
            case 67:
            case 71:
            case 75:
            case 79: {
                return 3;
            }
            default: {
                return -1;
            }
        }
    }
    
    static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$decompiler$ast$AstCode() {
        final int[] loc_0 = AstCodeHelpers.$SWITCH_TABLE$com$strobel$decompiler$ast$AstCode;
        if (loc_0 != null) {
            return loc_0;
        }
        final int[] loc_1 = new int[AstCode.values().length];
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
        return AstCodeHelpers.$SWITCH_TABLE$com$strobel$decompiler$ast$AstCode = loc_1;
    }
}
