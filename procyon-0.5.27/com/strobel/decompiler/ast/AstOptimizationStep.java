package com.strobel.decompiler.ast;

public enum AstOptimizationStep
{
    RemoveRedundantCode("RemoveRedundantCode", 0), 
    ReduceBranchInstructionSet("ReduceBranchInstructionSet", 1), 
    InlineVariables("InlineVariables", 2), 
    CopyPropagation("CopyPropagation", 3), 
    RewriteFinallyBlocks("RewriteFinallyBlocks", 4), 
    SplitToMovableBlocks("SplitToMovableBlocks", 5), 
    RemoveUnreachableBlocks("RemoveUnreachableBlocks", 6), 
    TypeInference("TypeInference", 7), 
    RemoveInnerClassInitSecurityChecks("RemoveInnerClassInitSecurityChecks", 8), 
    PreProcessShortCircuitAssignments("PreProcessShortCircuitAssignments", 9), 
    SimplifyShortCircuit("SimplifyShortCircuit", 10), 
    JoinBranchConditions("JoinBranchConditions", 11), 
    SimplifyTernaryOperator("SimplifyTernaryOperator", 12), 
    JoinBasicBlocks("JoinBasicBlocks", 13), 
    SimplifyLogicalNot("SimplifyLogicalNot", 14), 
    SimplifyShiftOperations("SimplifyShiftOperations", 15), 
    SimplifyLoadAndStore("SimplifyLoadAndStore", 16), 
    TransformObjectInitializers("TransformObjectInitializers", 17), 
    TransformArrayInitializers("TransformArrayInitializers", 18), 
    InlineConditionalAssignments("InlineConditionalAssignments", 19), 
    MakeAssignmentExpressions("MakeAssignmentExpressions", 20), 
    IntroducePostIncrement("IntroducePostIncrement", 21), 
    InlineLambdas("InlineLambdas", 22), 
    InlineVariables2("InlineVariables2", 23), 
    MergeDisparateObjectInitializations("MergeDisparateObjectInitializations", 24), 
    FindLoops("FindLoops", 25), 
    FindConditions("FindConditions", 26), 
    FlattenNestedMovableBlocks("FlattenNestedMovableBlocks", 27), 
    RemoveRedundantCode2("RemoveRedundantCode2", 28), 
    GotoRemoval("GotoRemoval", 29), 
    DuplicateReturns("DuplicateReturns", 30), 
    ReduceIfNesting("ReduceIfNesting", 31), 
    GotoRemoval2("GotoRemoval2", 32), 
    ReduceComparisonInstructionSet("ReduceComparisonInstructionSet", 33), 
    RecombineVariables("RecombineVariables", 34), 
    RemoveRedundantCode3("RemoveRedundantCode3", 35), 
    CleanUpTryBlocks("CleanUpTryBlocks", 36), 
    InlineVariables3("InlineVariables3", 37), 
    TypeInference2("TypeInference2", 38), 
    None("None", 39);
    
    private static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$decompiler$ast$AstOptimizationStep;
    
    public boolean isBlockLevelOptimization() {
        switch ($SWITCH_TABLE$com$strobel$decompiler$ast$AstOptimizationStep()[this.ordinal()]) {
            case 9:
            case 11:
            case 13:
            case 14:
            case 15:
            case 16:
            case 17:
            case 18:
            case 19:
            case 21:
            case 22:
            case 23:
            case 24:
            case 25: {
                return true;
            }
            default: {
                return false;
            }
        }
    }
    
    static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$decompiler$ast$AstOptimizationStep() {
        final int[] loc_0 = AstOptimizationStep.$SWITCH_TABLE$com$strobel$decompiler$ast$AstOptimizationStep;
        if (loc_0 != null) {
            return loc_0;
        }
        final int[] loc_1 = new int[values().length];
        try {
            loc_1[AstOptimizationStep.CleanUpTryBlocks.ordinal()] = 37;
        }
        catch (NoSuchFieldError loc_2) {}
        try {
            loc_1[AstOptimizationStep.CopyPropagation.ordinal()] = 4;
        }
        catch (NoSuchFieldError loc_3) {}
        try {
            loc_1[AstOptimizationStep.DuplicateReturns.ordinal()] = 31;
        }
        catch (NoSuchFieldError loc_4) {}
        try {
            loc_1[AstOptimizationStep.FindConditions.ordinal()] = 27;
        }
        catch (NoSuchFieldError loc_5) {}
        try {
            loc_1[AstOptimizationStep.FindLoops.ordinal()] = 26;
        }
        catch (NoSuchFieldError loc_6) {}
        try {
            loc_1[AstOptimizationStep.FlattenNestedMovableBlocks.ordinal()] = 28;
        }
        catch (NoSuchFieldError loc_7) {}
        try {
            loc_1[AstOptimizationStep.GotoRemoval.ordinal()] = 30;
        }
        catch (NoSuchFieldError loc_8) {}
        try {
            loc_1[AstOptimizationStep.GotoRemoval2.ordinal()] = 33;
        }
        catch (NoSuchFieldError loc_9) {}
        try {
            loc_1[AstOptimizationStep.InlineConditionalAssignments.ordinal()] = 20;
        }
        catch (NoSuchFieldError loc_10) {}
        try {
            loc_1[AstOptimizationStep.InlineLambdas.ordinal()] = 23;
        }
        catch (NoSuchFieldError loc_11) {}
        try {
            loc_1[AstOptimizationStep.InlineVariables.ordinal()] = 3;
        }
        catch (NoSuchFieldError loc_12) {}
        try {
            loc_1[AstOptimizationStep.InlineVariables2.ordinal()] = 24;
        }
        catch (NoSuchFieldError loc_13) {}
        try {
            loc_1[AstOptimizationStep.InlineVariables3.ordinal()] = 38;
        }
        catch (NoSuchFieldError loc_14) {}
        try {
            loc_1[AstOptimizationStep.IntroducePostIncrement.ordinal()] = 22;
        }
        catch (NoSuchFieldError loc_15) {}
        try {
            loc_1[AstOptimizationStep.JoinBasicBlocks.ordinal()] = 14;
        }
        catch (NoSuchFieldError loc_16) {}
        try {
            loc_1[AstOptimizationStep.JoinBranchConditions.ordinal()] = 12;
        }
        catch (NoSuchFieldError loc_17) {}
        try {
            loc_1[AstOptimizationStep.MakeAssignmentExpressions.ordinal()] = 21;
        }
        catch (NoSuchFieldError loc_18) {}
        try {
            loc_1[AstOptimizationStep.MergeDisparateObjectInitializations.ordinal()] = 25;
        }
        catch (NoSuchFieldError loc_19) {}
        try {
            loc_1[AstOptimizationStep.None.ordinal()] = 40;
        }
        catch (NoSuchFieldError loc_20) {}
        try {
            loc_1[AstOptimizationStep.PreProcessShortCircuitAssignments.ordinal()] = 10;
        }
        catch (NoSuchFieldError loc_21) {}
        try {
            loc_1[AstOptimizationStep.RecombineVariables.ordinal()] = 35;
        }
        catch (NoSuchFieldError loc_22) {}
        try {
            loc_1[AstOptimizationStep.ReduceBranchInstructionSet.ordinal()] = 2;
        }
        catch (NoSuchFieldError loc_23) {}
        try {
            loc_1[AstOptimizationStep.ReduceComparisonInstructionSet.ordinal()] = 34;
        }
        catch (NoSuchFieldError loc_24) {}
        try {
            loc_1[AstOptimizationStep.ReduceIfNesting.ordinal()] = 32;
        }
        catch (NoSuchFieldError loc_25) {}
        try {
            loc_1[AstOptimizationStep.RemoveInnerClassInitSecurityChecks.ordinal()] = 9;
        }
        catch (NoSuchFieldError loc_26) {}
        try {
            loc_1[AstOptimizationStep.RemoveRedundantCode.ordinal()] = 1;
        }
        catch (NoSuchFieldError loc_27) {}
        try {
            loc_1[AstOptimizationStep.RemoveRedundantCode2.ordinal()] = 29;
        }
        catch (NoSuchFieldError loc_28) {}
        try {
            loc_1[AstOptimizationStep.RemoveRedundantCode3.ordinal()] = 36;
        }
        catch (NoSuchFieldError loc_29) {}
        try {
            loc_1[AstOptimizationStep.RemoveUnreachableBlocks.ordinal()] = 7;
        }
        catch (NoSuchFieldError loc_30) {}
        try {
            loc_1[AstOptimizationStep.RewriteFinallyBlocks.ordinal()] = 5;
        }
        catch (NoSuchFieldError loc_31) {}
        try {
            loc_1[AstOptimizationStep.SimplifyLoadAndStore.ordinal()] = 17;
        }
        catch (NoSuchFieldError loc_32) {}
        try {
            loc_1[AstOptimizationStep.SimplifyLogicalNot.ordinal()] = 15;
        }
        catch (NoSuchFieldError loc_33) {}
        try {
            loc_1[AstOptimizationStep.SimplifyShiftOperations.ordinal()] = 16;
        }
        catch (NoSuchFieldError loc_34) {}
        try {
            loc_1[AstOptimizationStep.SimplifyShortCircuit.ordinal()] = 11;
        }
        catch (NoSuchFieldError loc_35) {}
        try {
            loc_1[AstOptimizationStep.SimplifyTernaryOperator.ordinal()] = 13;
        }
        catch (NoSuchFieldError loc_36) {}
        try {
            loc_1[AstOptimizationStep.SplitToMovableBlocks.ordinal()] = 6;
        }
        catch (NoSuchFieldError loc_37) {}
        try {
            loc_1[AstOptimizationStep.TransformArrayInitializers.ordinal()] = 19;
        }
        catch (NoSuchFieldError loc_38) {}
        try {
            loc_1[AstOptimizationStep.TransformObjectInitializers.ordinal()] = 18;
        }
        catch (NoSuchFieldError loc_39) {}
        try {
            loc_1[AstOptimizationStep.TypeInference.ordinal()] = 8;
        }
        catch (NoSuchFieldError loc_40) {}
        try {
            loc_1[AstOptimizationStep.TypeInference2.ordinal()] = 39;
        }
        catch (NoSuchFieldError loc_41) {}
        return AstOptimizationStep.$SWITCH_TABLE$com$strobel$decompiler$ast$AstOptimizationStep = loc_1;
    }
}
