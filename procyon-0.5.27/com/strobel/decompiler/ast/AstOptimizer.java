package com.strobel.decompiler.ast;

import java.util.logging.*;
import com.strobel.decompiler.*;
import com.strobel.util.*;
import java.util.*;
import com.strobel.core.*;
import com.strobel.functions.*;
import com.strobel.assembler.metadata.*;

public final class AstOptimizer
{
    private static final Logger LOG;
    private int _nextLabelIndex;
    private static final BooleanBox SCRATCH_BOOLEAN_BOX;
    private static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$decompiler$ast$AstCode;
    
    static {
        LOG = Logger.getLogger(AstOptimizer.class.getSimpleName());
        SCRATCH_BOOLEAN_BOX = new BooleanBox();
    }
    
    public static void optimize(final DecompilerContext context, final Block method) {
        optimize(context, method, AstOptimizationStep.None);
    }
    
    public static void optimize(final DecompilerContext context, final Block method, final AstOptimizationStep abortBeforeStep) {
        VerifyArgument.notNull(context, "context");
        VerifyArgument.notNull(method, "method");
        AstOptimizer.LOG.fine("Beginning bytecode AST optimization...");
        if (!shouldPerformStep(abortBeforeStep, AstOptimizationStep.RemoveRedundantCode)) {
            return;
        }
        final AstOptimizer optimizer = new AstOptimizer();
        removeRedundantCode(method, context.getSettings());
        if (!shouldPerformStep(abortBeforeStep, AstOptimizationStep.ReduceBranchInstructionSet)) {
            return;
        }
        introducePreIncrementOptimization(context, method);
        for (final Block block : method.getSelfAndChildrenRecursive(Block.class)) {
            reduceBranchInstructionSet(block);
        }
        if (!shouldPerformStep(abortBeforeStep, AstOptimizationStep.InlineVariables)) {
            return;
        }
        final Inlining inliningPhase1 = new Inlining(context, method);
        while (inliningPhase1.inlineAllVariables()) {
            inliningPhase1.analyzeMethod();
        }
        if (!shouldPerformStep(abortBeforeStep, AstOptimizationStep.CopyPropagation)) {
            return;
        }
        inliningPhase1.copyPropagation();
        if (!shouldPerformStep(abortBeforeStep, AstOptimizationStep.RewriteFinallyBlocks)) {
            return;
        }
        rewriteFinallyBlocks(method);
        if (!shouldPerformStep(abortBeforeStep, AstOptimizationStep.SplitToMovableBlocks)) {
            return;
        }
        for (final Block block2 : method.getSelfAndChildrenRecursive(Block.class)) {
            optimizer.splitToMovableBlocks(block2);
        }
        if (!shouldPerformStep(abortBeforeStep, AstOptimizationStep.RemoveUnreachableBlocks)) {
            return;
        }
        removeUnreachableBlocks(method);
        if (!shouldPerformStep(abortBeforeStep, AstOptimizationStep.TypeInference)) {
            return;
        }
        TypeAnalysis.run(context, method);
        boolean done = false;
        AstOptimizer.LOG.fine("Performing block-level bytecode AST optimizations (enable FINER for more detail)...");
        int blockNumber = 0;
        for (final Block block3 : method.getSelfAndChildrenRecursive(Block.class)) {
            int blockRound = 0;
            ++blockNumber;
            boolean modified;
            do {
                if (AstOptimizer.LOG.isLoggable(Level.FINER)) {
                    AstOptimizer.LOG.finer("Optimizing block #" + blockNumber + ", round " + ++blockRound + "...");
                }
                modified = false;
                if (!shouldPerformStep(abortBeforeStep, AstOptimizationStep.RemoveInnerClassInitSecurityChecks)) {
                    done = true;
                    break;
                }
                modified |= runOptimization(block3, new RemoveInnerClassInitSecurityChecksOptimization(context, method));
                if (!shouldPerformStep(abortBeforeStep, AstOptimizationStep.PreProcessShortCircuitAssignments)) {
                    done = true;
                    break;
                }
                modified |= runOptimization(block3, new PreProcessShortCircuitAssignmentsOptimization(context, method));
                if (!shouldPerformStep(abortBeforeStep, AstOptimizationStep.SimplifyShortCircuit)) {
                    done = true;
                    break;
                }
                modified |= runOptimization(block3, new SimplifyShortCircuitOptimization(context, method));
                if (!shouldPerformStep(abortBeforeStep, AstOptimizationStep.JoinBranchConditions)) {
                    done = true;
                    break;
                }
                modified |= runOptimization(block3, new JoinBranchConditionsOptimization(context, method));
                if (!shouldPerformStep(abortBeforeStep, AstOptimizationStep.SimplifyTernaryOperator)) {
                    done = true;
                    break;
                }
                modified |= runOptimization(block3, new SimplifyTernaryOperatorOptimization(context, method));
                modified |= runOptimization(block3, new SimplifyTernaryOperatorRoundTwoOptimization(context, method));
                if (!shouldPerformStep(abortBeforeStep, AstOptimizationStep.JoinBasicBlocks)) {
                    done = true;
                    break;
                }
                modified |= runOptimization(block3, new JoinBasicBlocksOptimization(context, method));
                if (!shouldPerformStep(abortBeforeStep, AstOptimizationStep.SimplifyLogicalNot)) {
                    done = true;
                    break;
                }
                modified |= runOptimization(block3, new SimplifyLogicalNotOptimization(context, method));
                if (!shouldPerformStep(abortBeforeStep, AstOptimizationStep.TransformObjectInitializers)) {
                    done = true;
                    break;
                }
                modified |= runOptimization(block3, new TransformObjectInitializersOptimization(context, method));
                if (!shouldPerformStep(abortBeforeStep, AstOptimizationStep.TransformArrayInitializers)) {
                    done = true;
                    break;
                }
                modified |= new Inlining(context, method, true).inlineAllInBlock(block3);
                modified |= runOptimization(block3, new TransformArrayInitializersOptimization(context, method));
                if (!shouldPerformStep(abortBeforeStep, AstOptimizationStep.IntroducePostIncrement)) {
                    done = true;
                    break;
                }
                modified |= runOptimization(block3, new IntroducePostIncrementOptimization(context, method));
                if (!shouldPerformStep(abortBeforeStep, AstOptimizationStep.InlineConditionalAssignments)) {
                    done = true;
                    break;
                }
                modified |= runOptimization(block3, new InlineConditionalAssignmentsOptimization(context, method));
                if (!shouldPerformStep(abortBeforeStep, AstOptimizationStep.MakeAssignmentExpressions)) {
                    done = true;
                    break;
                }
                modified |= runOptimization(block3, new MakeAssignmentExpressionsOptimization(context, method));
                if (!shouldPerformStep(abortBeforeStep, AstOptimizationStep.InlineLambdas)) {
                    return;
                }
                modified |= runOptimization(block3, new InlineLambdasOptimization(context, method));
                if (!shouldPerformStep(abortBeforeStep, AstOptimizationStep.InlineVariables2)) {
                    done = true;
                    break;
                }
                modified |= new Inlining(context, method, true).inlineAllInBlock(block3);
                new Inlining(context, method).copyPropagation();
                if (!shouldPerformStep(abortBeforeStep, AstOptimizationStep.MergeDisparateObjectInitializations)) {
                    done = true;
                    break;
                }
                modified |= mergeDisparateObjectInitializations(context, block3);
            } while (modified);
        }
        if (done) {
            return;
        }
        if (!shouldPerformStep(abortBeforeStep, AstOptimizationStep.FindLoops)) {
            return;
        }
        for (final Block block3 : method.getSelfAndChildrenRecursive(Block.class)) {
            new LoopsAndConditions(context).findLoops(block3);
        }
        if (!shouldPerformStep(abortBeforeStep, AstOptimizationStep.FindConditions)) {
            return;
        }
        for (final Block block3 : method.getSelfAndChildrenRecursive(Block.class)) {
            new LoopsAndConditions(context).findConditions(block3);
        }
        if (!shouldPerformStep(abortBeforeStep, AstOptimizationStep.FlattenNestedMovableBlocks)) {
            return;
        }
        flattenBasicBlocks(method);
        if (!shouldPerformStep(abortBeforeStep, AstOptimizationStep.RemoveRedundantCode2)) {
            return;
        }
        removeRedundantCode(method, context.getSettings());
        if (!shouldPerformStep(abortBeforeStep, AstOptimizationStep.GotoRemoval)) {
            return;
        }
        new GotoRemoval().removeGotos(method);
        if (!shouldPerformStep(abortBeforeStep, AstOptimizationStep.DuplicateReturns)) {
            return;
        }
        duplicateReturnStatements(method);
        if (!shouldPerformStep(abortBeforeStep, AstOptimizationStep.ReduceIfNesting)) {
            return;
        }
        reduceIfNesting(method);
        if (!shouldPerformStep(abortBeforeStep, AstOptimizationStep.GotoRemoval2)) {
            return;
        }
        new GotoRemoval().removeGotos(method);
        if (!shouldPerformStep(abortBeforeStep, AstOptimizationStep.ReduceComparisonInstructionSet)) {
            return;
        }
        for (final Expression e : method.getChildrenAndSelfRecursive(Expression.class)) {
            reduceComparisonInstructionSet(e);
        }
        if (!shouldPerformStep(abortBeforeStep, AstOptimizationStep.RecombineVariables)) {
            return;
        }
        recombineVariables(method);
        if (!shouldPerformStep(abortBeforeStep, AstOptimizationStep.RemoveRedundantCode3)) {
            return;
        }
        GotoRemoval.removeRedundantCode(method, 3);
        if (!shouldPerformStep(abortBeforeStep, AstOptimizationStep.CleanUpTryBlocks)) {
            return;
        }
        cleanUpTryBlocks(method);
        if (!shouldPerformStep(abortBeforeStep, AstOptimizationStep.InlineVariables3)) {
            return;
        }
        final Inlining inliningPhase2 = new Inlining(context, method, true);
        inliningPhase2.inlineAllVariables();
        if (!shouldPerformStep(abortBeforeStep, AstOptimizationStep.TypeInference2)) {
            return;
        }
        TypeAnalysis.reset(context, method);
        TypeAnalysis.run(context, method);
        AstOptimizer.LOG.fine("Finished bytecode AST optimization.");
    }
    
    private static boolean shouldPerformStep(final AstOptimizationStep abortBeforeStep, final AstOptimizationStep nextStep) {
        if (abortBeforeStep == nextStep) {
            return false;
        }
        if (nextStep.isBlockLevelOptimization()) {
            if (AstOptimizer.LOG.isLoggable(Level.FINER)) {
                AstOptimizer.LOG.finer("Performing block-level optimization: " + nextStep + ".");
            }
        }
        else if (AstOptimizer.LOG.isLoggable(Level.FINE)) {
            AstOptimizer.LOG.fine("Performing optimization: " + nextStep + ".");
        }
        return true;
    }
    
    private static void removeUnreachableBlocks(final Block method) {
        final BasicBlock entryBlock = CollectionUtilities.first(CollectionUtilities.ofType(method.getBody(), BasicBlock.class));
        final Set<Label> liveLabels = new LinkedHashSet<Label>();
        final Map<BasicBlock, List<Label>> embeddedLabels = new DefaultMap<BasicBlock, List<Label>>(new Supplier<List<Label>>() {
            @Override
            public List<Label> get() {
                return new ArrayList<Label>();
            }
        });
        for (final BasicBlock basicBlock : method.getChildrenAndSelfRecursive(BasicBlock.class)) {
            for (final Label label : basicBlock.getChildrenAndSelfRecursive(Label.class)) {
                embeddedLabels.get(basicBlock).add(label);
            }
        }
        for (final Expression e : method.getChildrenAndSelfRecursive(Expression.class)) {
            if (e.getOperand() instanceof Label) {
                liveLabels.add((Label)e.getOperand());
            }
            else {
                if (!(e.getOperand() instanceof Label[])) {
                    continue;
                }
                Collections.addAll(liveLabels, (Label[])e.getOperand());
            }
        }
    Label_0384:
        for (final BasicBlock basicBlock : method.getChildrenAndSelfRecursive(BasicBlock.class)) {
            final List<Node> body = basicBlock.getBody();
            final Label entryLabel = body.get(0);
            if (basicBlock != entryBlock && !liveLabels.contains(entryLabel)) {
                for (final Label label2 : embeddedLabels.get(basicBlock)) {
                    if (liveLabels.contains(label2)) {
                        continue Label_0384;
                    }
                }
                while (body.size() > 1) {
                    body.remove(body.size() - 1);
                }
            }
        }
    }
    
    private static void cleanUpTryBlocks(final Block method) {
        for (final Block block : method.getChildrenAndSelfRecursive(Block.class)) {
            final List<Node> body = block.getBody();
            for (int i = 0; i < body.size(); ++i) {
                if (body.get(i) instanceof TryCatchBlock) {
                    final TryCatchBlock tryCatch = body.get(i);
                    if (tryCatch.getTryBlock().getBody().isEmpty() && (tryCatch.getFinallyBlock() == null || tryCatch.getFinallyBlock().getBody().isEmpty())) {
                        body.remove(i--);
                    }
                    else if (tryCatch.getFinallyBlock() != null && tryCatch.getCatchBlocks().isEmpty() && tryCatch.getTryBlock().getBody().size() == 1 && tryCatch.getTryBlock().getBody().get(0) instanceof TryCatchBlock) {
                        final TryCatchBlock innerTryCatch = tryCatch.getTryBlock().getBody().get(0);
                        if (innerTryCatch.getFinallyBlock() == null) {
                            tryCatch.setTryBlock(innerTryCatch.getTryBlock());
                            tryCatch.getCatchBlocks().addAll(innerTryCatch.getCatchBlocks());
                        }
                    }
                }
            }
        }
    }
    
    private static void rewriteFinallyBlocks(final Block method) {
        rewriteSynchronized(method);
        final List<Expression> a = new ArrayList<Expression>();
        final StrongBox<Variable> v = new StrongBox<Variable>();
        int endFinallyCount = 0;
        for (final TryCatchBlock tryCatch : method.getChildrenAndSelfRecursive(TryCatchBlock.class)) {
            final Block finallyBlock = tryCatch.getFinallyBlock();
            if (finallyBlock != null) {
                if (finallyBlock.getBody().size() < 2) {
                    continue;
                }
                final List<Node> body = finallyBlock.getBody();
                final List<Variable> exceptionCopies = new ArrayList<Variable>();
                final Node lastInFinally = CollectionUtilities.last(finallyBlock.getBody());
                if (!PatternMatching.matchGetArguments(body.get(0), AstCode.Store, v, a) || !PatternMatching.match(a.get(0), AstCode.LoadException)) {
                    continue;
                }
                body.remove(0);
                exceptionCopies.add(v.get());
                if (body.isEmpty() || !PatternMatching.matchLoadStore(body.get(0), v.get(), v)) {
                    v.set(null);
                }
                else {
                    exceptionCopies.add(v.get());
                }
                Label endFinallyLabel;
                if (body.size() > 1 && body.get(body.size() - 2) instanceof Label) {
                    endFinallyLabel = body.get(body.size() - 2);
                }
                else {
                    endFinallyLabel = new Label();
                    endFinallyLabel.setName("EndFinally_" + endFinallyCount++);
                    body.add(body.size() - 1, endFinallyLabel);
                }
                for (final Block b : finallyBlock.getSelfAndChildrenRecursive(Block.class)) {
                    final List<Node> blockBody = b.getBody();
                    for (int i = 0; i < blockBody.size(); ++i) {
                        final Node node = blockBody.get(i);
                        if (node instanceof Expression) {
                            final Expression e = (Expression)node;
                            if (PatternMatching.matchLoadStoreAny(node, exceptionCopies, v)) {
                                exceptionCopies.add(v.get());
                            }
                            else if (e != lastInFinally && PatternMatching.matchGetArguments(e, AstCode.AThrow, a) && PatternMatching.matchLoadAny(a.get(0), exceptionCopies)) {
                                e.setCode(AstCode.Goto);
                                e.setOperand(endFinallyLabel);
                                e.getArguments().clear();
                            }
                        }
                    }
                }
                if (body.size() < 1 || !PatternMatching.matchGetArguments(body.get(body.size() - 1), AstCode.AThrow, a) || !PatternMatching.matchLoadAny(a.get(0), exceptionCopies)) {
                    continue;
                }
                body.set(body.size() - 1, new Expression(AstCode.EndFinally, null, -34, new Expression[0]));
            }
        }
    }
    
    private static void rewriteSynchronized(final Block method) {
        final StrongBox<LockInfo> lockInfoBox = new StrongBox<LockInfo>();
        for (final Block block : method.getSelfAndChildrenRecursive(Block.class)) {
            final List<Node> body = block.getBody();
            for (int i = 0; i < body.size() - 1; ++i) {
                if (PatternMatching.matchLock(body, i, lockInfoBox) && i + lockInfoBox.get().operationCount < body.size() && body.get(i + lockInfoBox.get().operationCount) instanceof TryCatchBlock) {
                    final TryCatchBlock tryCatch = body.get(i + lockInfoBox.get().operationCount);
                    if (!tryCatch.isSynchronized()) {
                        final Block finallyBlock = tryCatch.getFinallyBlock();
                        if (finallyBlock != null) {
                            final List<Node> finallyBody = finallyBlock.getBody();
                            final LockInfo lockInfo = lockInfoBox.get();
                            if (finallyBody.size() == 3 && PatternMatching.matchUnlock(finallyBody.get(1), lockInfo)) {
                                if (rewriteSynchronizedCore(tryCatch, lockInfo.operationCount)) {
                                    tryCatch.setSynchronized(true);
                                }
                                else {
                                    final StrongBox<Variable> v = new StrongBox<Variable>();
                                    final List<Variable> lockCopies = new ArrayList<Variable>();
                                    if (lockInfo.lockCopy != null) {
                                        lockCopies.add(lockInfo.lockCopy);
                                    }
                                    for (final Expression e : tryCatch.getChildrenAndSelfRecursive(Expression.class)) {
                                        if (PatternMatching.matchLoadAny(e, lockCopies)) {
                                            e.setOperand(lockInfo.lock);
                                        }
                                        else {
                                            if (!PatternMatching.matchLoadStore(e, lockInfo.lock, v) || v.get() == lockInfo.lock) {
                                                continue;
                                            }
                                            lockCopies.add(v.get());
                                        }
                                    }
                                }
                                inlineLockAccess(tryCatch, body, lockInfo);
                            }
                        }
                    }
                }
            }
        }
    }
    
    private static boolean rewriteSynchronizedCore(final TryCatchBlock tryCatch, final int depth) {
        final Block tryBlock = tryCatch.getTryBlock();
        final List<Node> tryBody = tryBlock.getBody();
        final StrongBox<LockInfo> lockInfoBox = new StrongBox<LockInfo>();
        LockInfo lockInfo = null;
        switch (tryBody.size()) {
            case 0: {
                return false;
            }
            case 1: {
                lockInfo = null;
                break;
            }
            default: {
                if (!PatternMatching.matchLock(tryBody, 0, lockInfoBox)) {
                    lockInfo = null;
                    break;
                }
                lockInfo = lockInfoBox.get();
                if (lockInfo.operationCount >= tryBody.size() || !(tryBody.get(lockInfo.operationCount) instanceof TryCatchBlock)) {
                    break;
                }
                final TryCatchBlock nestedTry = tryBody.get(lockInfo.operationCount);
                final Block finallyBlock = nestedTry.getFinallyBlock();
                if (finallyBlock == null) {
                    return false;
                }
                final List<Node> finallyBody = finallyBlock.getBody();
                if (finallyBody.size() == 3 && PatternMatching.matchUnlock(finallyBody.get(1), lockInfo) && rewriteSynchronizedCore(nestedTry, depth + 1)) {
                    tryCatch.setSynchronized(true);
                    inlineLockAccess(tryCatch, tryBody, lockInfo);
                    return true;
                }
                break;
            }
        }
        final boolean skipTrailingBranch = PatternMatching.matchUnconditionalBranch(tryBody.get(tryBody.size() - 1));
        if (tryBody.size() < (skipTrailingBranch ? (depth + 1) : depth)) {
            return false;
        }
        final int removeTail = tryBody.size() - (skipTrailingBranch ? 1 : 0);
        List<Node> monitorExitNodes;
        if (removeTail > 0 && tryBody.get(removeTail - 1) instanceof TryCatchBlock) {
            final TryCatchBlock innerTry = tryBody.get(removeTail - 1);
            final List<Node> innerTryBody = innerTry.getTryBlock().getBody();
            if (PatternMatching.matchLock(innerTryBody, 0, lockInfoBox) && rewriteSynchronizedCore(innerTry, depth)) {
                inlineLockAccess(tryCatch, tryBody, lockInfo);
                tryCatch.setSynchronized(true);
                return true;
            }
            final boolean skipInnerTrailingBranch = PatternMatching.matchUnconditionalBranch(innerTryBody.get(innerTryBody.size() - 1));
            if (innerTryBody.size() < (skipInnerTrailingBranch ? (depth + 1) : depth)) {
                return false;
            }
            final int innerRemoveTail = innerTryBody.size() - (skipInnerTrailingBranch ? 1 : 0);
            monitorExitNodes = innerTryBody.subList(innerRemoveTail - depth, innerRemoveTail);
        }
        else {
            monitorExitNodes = tryBody.subList(removeTail - depth, removeTail);
        }
        final boolean removeAll = CollectionUtilities.all(monitorExitNodes, new Predicate<Node>() {
            @Override
            public boolean test(final Node node) {
                return PatternMatching.match(node, AstCode.MonitorExit);
            }
        });
        if (removeAll) {
            monitorExitNodes.clear();
            if (!tryCatch.getCatchBlocks().isEmpty()) {
                final TryCatchBlock newTryCatch = new TryCatchBlock();
                newTryCatch.setTryBlock(tryCatch.getTryBlock());
                newTryCatch.getCatchBlocks().addAll(tryCatch.getCatchBlocks());
                tryCatch.getCatchBlocks().clear();
                tryCatch.setTryBlock(new Block(new Node[] { newTryCatch }));
            }
            inlineLockAccess(tryCatch, tryBody, lockInfo);
            tryCatch.setSynchronized(true);
            return true;
        }
        return false;
    }
    
    private static void inlineLockAccess(final Node owner, final List<Node> body, final LockInfo lockInfo) {
        if (lockInfo == null || lockInfo.lockInit == null) {
            return;
        }
        boolean lockCopyUsed = false;
        final StrongBox<Expression> a = new StrongBox<Expression>();
        final List<Expression> lockAccesses = new ArrayList<Expression>();
        final Set<Expression> lockAccessLoads = new HashSet<Expression>();
        for (final Expression e : owner.getSelfAndChildrenRecursive(Expression.class)) {
            if (PatternMatching.matchLoad(e, lockInfo.lock) && !lockAccessLoads.contains(e)) {
                return;
            }
            if (lockInfo.lockCopy != null && PatternMatching.matchLoad(e, lockInfo.lockCopy) && !lockAccessLoads.contains(e)) {
                lockCopyUsed = true;
            }
            else {
                if ((!PatternMatching.matchGetArgument(e, AstCode.MonitorEnter, a) && !PatternMatching.matchGetArgument(e, AstCode.MonitorExit, a)) || (!PatternMatching.matchLoad(a.get(), lockInfo.lock) && (lockInfo.lockCopy == null || !PatternMatching.matchLoad(a.get(), lockInfo.lockCopy)))) {
                    continue;
                }
                lockAccesses.add(e);
                lockAccessLoads.add(a.get());
            }
        }
        for (final Expression e : lockAccesses) {
            e.getArguments().set(0, lockInfo.lockInit.clone());
        }
        body.remove(lockInfo.lockStore);
        lockInfo.lockAcquire.getArguments().set(0, lockInfo.lockInit.clone());
        if (lockInfo.lockCopy != null && !lockCopyUsed) {
            body.remove(lockInfo.lockStoreCopy);
        }
    }
    
    static void removeRedundantCode(final Block method, final DecompilerSettings settings) {
        final Map<Label, MutableInteger> labelReferenceCount = new IdentityHashMap<Label, MutableInteger>();
        final List<Expression> branchExpressions = method.getSelfAndChildrenRecursive(Expression.class, new Predicate<Expression>() {
            @Override
            public boolean test(final Expression e) {
                return e.isBranch();
            }
        });
        for (final Expression e : branchExpressions) {
            for (final Label branchTarget : e.getBranchTargets()) {
                final MutableInteger referenceCount = labelReferenceCount.get(branchTarget);
                if (referenceCount == null) {
                    labelReferenceCount.put(branchTarget, new MutableInteger(1));
                }
                else {
                    referenceCount.increment();
                }
            }
        }
        for (final Block block : method.getSelfAndChildrenRecursive(Block.class)) {
            final List<Node> body = block.getBody();
            final List<Node> newBody = new ArrayList<Node>(body.size());
            for (int i = 0, n = body.size(); i < n; ++i) {
                final Node node = body.get(i);
                final StrongBox<Label> target = new StrongBox<Label>();
                final List<Expression> args = new ArrayList<Expression>();
                if (PatternMatching.matchGetOperand(node, AstCode.Goto, target) && i + 1 < body.size() && body.get(i + 1) == target.get()) {
                    if (labelReferenceCount.get(target.get()).getValue() == 1) {
                        ++i;
                    }
                }
                else if (!PatternMatching.match(node, AstCode.Nop) && !PatternMatching.match(node, AstCode.Load)) {
                    if (PatternMatching.matchGetArguments(node, AstCode.Pop, args)) {
                        final StrongBox<Variable> variable = new StrongBox<Variable>();
                        if (!PatternMatching.matchGetOperand(args.get(0), AstCode.Load, variable)) {
                            throw new IllegalStateException("Pop should just have Load at this stage.");
                        }
                        final StrongBox<Variable> previousVariable = new StrongBox<Variable>();
                        final StrongBox<Expression> previousExpression = new StrongBox<Expression>();
                        if (i - 1 >= 0 && PatternMatching.matchGetArgument(body.get(i - 1), AstCode.Store, previousVariable, previousExpression) && previousVariable.get() == variable.get()) {
                            previousExpression.get().getRanges().addAll(((Expression)node).getRanges());
                        }
                    }
                    else if (PatternMatching.matchGetArguments(node, AstCode.Pop2, args)) {
                        final StrongBox<Variable> v1 = new StrongBox<Variable>();
                        final StrongBox<Variable> v2 = new StrongBox<Variable>();
                        final StrongBox<Variable> pv1 = new StrongBox<Variable>();
                        final StrongBox<Expression> pe1 = new StrongBox<Expression>();
                        if (args.size() == 1) {
                            if (!PatternMatching.matchGetOperand(args.get(0), AstCode.Load, v1)) {
                                throw new IllegalStateException("Pop2 should just have Load arguments at this stage.");
                            }
                            if (!v1.get().getType().getSimpleType().isDoubleWord()) {
                                throw new IllegalStateException("Pop2 instruction has only one single-word operand.");
                            }
                            if (i - 1 >= 0 && PatternMatching.matchGetArgument(body.get(i - 1), AstCode.Store, pv1, pe1) && pv1.get() == v1.get()) {
                                pe1.get().getRanges().addAll(((Expression)node).getRanges());
                            }
                        }
                        else {
                            if (!PatternMatching.matchGetOperand(args.get(0), AstCode.Load, v1) || !PatternMatching.matchGetOperand(args.get(1), AstCode.Load, v2)) {
                                throw new IllegalStateException("Pop2 should just have Load arguments at this stage.");
                            }
                            final StrongBox<Variable> pv2 = new StrongBox<Variable>();
                            final StrongBox<Expression> pe2 = new StrongBox<Expression>();
                            if (i - 2 >= 0 && PatternMatching.matchGetArgument(body.get(i - 2), AstCode.Store, pv1, pe1) && pv1.get() == v1.get() && PatternMatching.matchGetArgument(body.get(i - 1), AstCode.Store, pv2, pe2) && pv2.get() == v2.get()) {
                                pe1.get().getRanges().addAll(((Expression)node).getRanges());
                                pe2.get().getRanges().addAll(((Expression)node).getRanges());
                            }
                        }
                    }
                    else if (node instanceof Label) {
                        final Label label = (Label)node;
                        final MutableInteger referenceCount2 = labelReferenceCount.get(label);
                        if (referenceCount2 != null && referenceCount2.getValue() > 0) {
                            newBody.add(label);
                        }
                    }
                    else if (node instanceof TryCatchBlock) {
                        final TryCatchBlock tryCatch = (TryCatchBlock)node;
                        if (!isEmptyTryCatch(tryCatch)) {
                            newBody.add(node);
                        }
                    }
                    else if (PatternMatching.match(node, AstCode.Switch) && !settings.getRetainPointlessSwitches()) {
                        final Expression e2 = (Expression)node;
                        final Label[] targets = (Label[])e2.getOperand();
                        if (targets.length == 1) {
                            final Expression test = e2.getArguments().get(0);
                            e2.setCode(AstCode.Goto);
                            e2.setOperand(targets[0]);
                            if (Inlining.canBeExpressionStatement(test)) {
                                newBody.add(test);
                            }
                            e2.getArguments().clear();
                        }
                        newBody.add(node);
                    }
                    else {
                        newBody.add(node);
                    }
                }
            }
            body.clear();
            body.addAll(newBody);
        }
        for (final Expression e : method.getSelfAndChildrenRecursive(Expression.class)) {
            final List<Expression> arguments = e.getArguments();
            for (int j = 0, n2 = arguments.size(); j < n2; ++j) {
                final Expression argument = arguments.get(j);
                switch ($SWITCH_TABLE$com$strobel$decompiler$ast$AstCode()[argument.getCode().ordinal()]) {
                    case 90:
                    case 91:
                    case 92:
                    case 93:
                    case 94:
                    case 95: {
                        final Expression firstArgument = argument.getArguments().get(0);
                        firstArgument.getRanges().addAll(argument.getRanges());
                        arguments.set(j, firstArgument);
                        break;
                    }
                }
            }
        }
        cleanUpTryBlocks(method);
    }
    
    private static boolean isEmptyTryCatch(final TryCatchBlock tryCatch) {
        if (tryCatch.getFinallyBlock() != null && !tryCatch.getFinallyBlock().getBody().isEmpty()) {
            return false;
        }
        final List<Node> body = tryCatch.getTryBlock().getBody();
        if (body.isEmpty()) {
            return true;
        }
        final StrongBox<Label> label = new StrongBox<Label>();
        return body.size() == 3 && PatternMatching.matchGetOperand(body.get(0), AstCode.Goto, label) && body.get(1) == label.get() && PatternMatching.match(body.get(2), AstCode.EndFinally);
    }
    
    private static void introducePreIncrementOptimization(final DecompilerContext context, final Block method) {
        final Inlining inlining = new Inlining(context, method);
        inlining.analyzeMethod();
        for (final Block block : method.getSelfAndChildrenRecursive(Block.class)) {
            final List<Node> body = block.getBody();
            final MutableInteger position = new MutableInteger();
            while (position.getValue() < body.size() - 1) {
                if (!introducePreIncrementForVariables(body, position) && !introducePreIncrementForStaticFields(body, position, inlining)) {
                    introducePreIncrementForInstanceFields(body, position, inlining);
                }
                position.increment();
            }
        }
    }
    
    private static boolean introducePreIncrementForVariables(final List<Node> body, final MutableInteger position) {
        final int i = position.getValue();
        if (i >= body.size() - 1) {
            return false;
        }
        final Node node = body.get(i);
        final Node next = body.get(i + 1);
        final StrongBox<Variable> v = new StrongBox<Variable>();
        final StrongBox<Expression> t = new StrongBox<Expression>();
        final StrongBox<Integer> d = new StrongBox<Integer>();
        if (!(node instanceof Expression) || !(next instanceof Expression)) {
            return false;
        }
        final Expression e = (Expression)node;
        final Expression n = (Expression)next;
        if (PatternMatching.matchGetArgument(e, AstCode.Inc, v, t) && PatternMatching.matchGetOperand(t.get(), AstCode.LdC, Integer.class, d) && Math.abs(d.get()) == 1 && PatternMatching.match(n, AstCode.Store) && PatternMatching.matchLoad(n.getArguments().get(0), v.get())) {
            n.getArguments().set(0, new Expression(AstCode.PreIncrement, d.get(), n.getArguments().get(0).getOffset(), new Expression[] { n.getArguments().get(0) }));
            body.remove(i);
            position.decrement();
            return true;
        }
        return false;
    }
    
    private static boolean introducePreIncrementForStaticFields(final List<Node> body, final MutableInteger position, final Inlining inlining) {
        final int i = position.getValue();
        if (i >= body.size() - 3) {
            return false;
        }
        final Node n1 = body.get(i);
        final Node n2 = body.get(i + 1);
        final Node n3 = body.get(i + 2);
        final Node n4 = body.get(i + 3);
        final StrongBox<Object> tAny = new StrongBox<Object>();
        final List<Expression> a = new ArrayList<Expression>();
        if (!PatternMatching.matchGetArguments(n1, AstCode.Store, tAny, a)) {
            return false;
        }
        final Variable t = tAny.get();
        if (!PatternMatching.matchGetOperand(a.get(0), AstCode.GetStatic, tAny)) {
            return false;
        }
        final FieldReference f = tAny.get();
        final Variable u;
        if (!PatternMatching.matchGetArguments(n2, AstCode.Store, tAny, a) || (u = tAny.get()) == null || !PatternMatching.matchGetOperand(a.get(0), AstCode.LdC, tAny) || !(tAny.get() instanceof Integer) || Math.abs(tAny.get()) != 1) {
            return false;
        }
        final int amount = tAny.get();
        final Variable v;
        if (PatternMatching.matchGetArguments(n3, AstCode.Store, tAny, a) && inlining.loadCounts.get(v = tAny.get()).getValue() > 1 && PatternMatching.matchGetArguments(a.get(0), AstCode.Add, a) && PatternMatching.matchLoad(a.get(0), t) && PatternMatching.matchLoad(a.get(1), u) && PatternMatching.matchGetArguments(n4, AstCode.PutStatic, tAny, a) && tAny.get() instanceof FieldReference && StringUtilities.equals(f.getFullName(), tAny.get().getFullName()) && PatternMatching.matchLoad(a.get(0), v)) {
            ((Expression)n3).getArguments().set(0, new Expression(AstCode.PreIncrement, amount, ((Expression)n1).getArguments().get(0).getOffset(), new Expression[] { ((Expression)n1).getArguments().get(0) }));
            body.remove(i);
            body.remove(i);
            body.remove(i + 1);
            position.decrement();
            return true;
        }
        return false;
    }
    
    private static boolean introducePreIncrementForInstanceFields(final List<Node> body, final MutableInteger position, final Inlining inlining) {
        final int i = position.getValue();
        if (i < 1 || i >= body.size() - 3) {
            return false;
        }
        if (!(body.get(i) instanceof Expression) || !(body.get(i - 1) instanceof Expression) || !(body.get(i + 1) instanceof Expression) || !(body.get(i + 2) instanceof Expression) || !(body.get(i + 3) instanceof Expression)) {
            return false;
        }
        final Expression e0 = body.get(i - 1);
        final Expression e = body.get(i);
        final List<Expression> a = new ArrayList<Expression>();
        final StrongBox<Variable> tVar = new StrongBox<Variable>();
        if (!PatternMatching.matchGetArguments(e0, AstCode.Store, tVar, a)) {
            return false;
        }
        final Variable n = tVar.get();
        final StrongBox<Object> unused = new StrongBox<Object>();
        if (PatternMatching.matchGetArguments(e, AstCode.Store, tVar, a)) {
            final boolean field;
            if (field = PatternMatching.match(a.get(0), AstCode.GetField)) {
                if (!PatternMatching.matchGetArguments(a.get(0), AstCode.GetField, unused, a)) {
                    return false;
                }
            }
            else if (!PatternMatching.matchGetArguments(a.get(0), AstCode.LoadElement, a)) {
                return false;
            }
            if (PatternMatching.matchLoad(a.get((int)(field ? 0 : 1)), n)) {
                final Variable t = tVar.get();
                final Variable o = field ? null : ((Variable)a.get(0).getOperand());
                final FieldReference f = field ? unused.get() : null;
                final Expression e2 = body.get(i + 1);
                final StrongBox<Integer> amount = new StrongBox<Integer>();
                if (!PatternMatching.matchGetArguments(e2, AstCode.Store, tVar, a) || !PatternMatching.matchGetOperand(a.get(0), AstCode.LdC, Integer.class, amount) || Math.abs(amount.get()) != 1) {
                    return false;
                }
                final Variable u = tVar.get();
                final Expression e3 = body.get(i + 2);
                if (!PatternMatching.matchGetArguments(e3, AstCode.Store, tVar, a) || (tVar.get().isGenerated() && inlining.loadCounts.get(tVar.get()).getValue() <= 1) || !PatternMatching.matchGetArguments(a.get(0), AstCode.Add, a) || !PatternMatching.matchLoad(a.get(0), t) || !PatternMatching.matchLoad(a.get(1), u)) {
                    return false;
                }
                final Variable v = tVar.get();
                final Expression e4 = body.get(i + 3);
                if (field) {
                    if (!PatternMatching.matchGetArguments(e4, AstCode.PutField, unused, a)) {
                        return false;
                    }
                }
                else if (!PatternMatching.matchGetArguments(e4, AstCode.StoreElement, a)) {
                    return false;
                }
                if (field) {
                    if (!StringUtilities.equals(f.getFullName(), unused.get().getFullName())) {
                        return false;
                    }
                }
                else if (!PatternMatching.matchLoad(a.get(0), o)) {
                    return false;
                }
                if (PatternMatching.matchLoad(a.get((int)(field ? 0 : 1)), n) && PatternMatching.matchLoad(a.get(field ? 1 : 2), v)) {
                    final Expression newExpression = new Expression(AstCode.PreIncrement, amount.get(), e.getArguments().get(0).getOffset(), new Expression[] { e.getArguments().get(0) });
                    e3.getArguments().set(0, newExpression);
                    body.remove(i);
                    body.remove(i);
                    body.remove(i + 1);
                    position.decrement();
                    return true;
                }
                return false;
            }
        }
        return false;
    }
    
    private static void reduceBranchInstructionSet(final Block block) {
        final List<Node> body = block.getBody();
        for (int i = 0; i < body.size(); ++i) {
            final Node node = body.get(i);
            if (node instanceof Expression) {
                final Expression e = (Expression)node;
                AstCode code = null;
                switch ($SWITCH_TABLE$com$strobel$decompiler$ast$AstCode()[e.getCode().ordinal()]) {
                    case 171:
                    case 172:
                    case 250: {
                        e.getArguments().get(0).getRanges().addAll(e.getRanges());
                        e.getRanges().clear();
                        continue;
                    }
                    case 149:
                    case 150:
                    case 151:
                    case 152:
                    case 153: {
                        if (i == body.size() - 1) {
                            continue;
                        }
                        if (!(body.get(i + 1) instanceof Expression)) {
                            continue;
                        }
                        final Expression next = body.get(i + 1);
                        switch ($SWITCH_TABLE$com$strobel$decompiler$ast$AstCode()[next.getCode().ordinal()]) {
                            case 154: {
                                code = AstCode.CmpEq;
                                break;
                            }
                            case 155: {
                                code = AstCode.CmpNe;
                                break;
                            }
                            case 156: {
                                code = AstCode.CmpLt;
                                break;
                            }
                            case 157: {
                                code = AstCode.CmpGe;
                                break;
                            }
                            case 158: {
                                code = AstCode.CmpGt;
                                break;
                            }
                            case 159: {
                                code = AstCode.CmpLe;
                                break;
                            }
                            default: {
                                continue;
                            }
                        }
                        body.remove(i);
                        break;
                    }
                    case 154: {
                        e.getArguments().add(new Expression(AstCode.LdC, 0, e.getOffset(), new Expression[0]));
                        code = AstCode.CmpEq;
                        break;
                    }
                    case 155: {
                        e.getArguments().add(new Expression(AstCode.LdC, 0, e.getOffset(), new Expression[0]));
                        code = AstCode.CmpNe;
                        break;
                    }
                    case 156: {
                        e.getArguments().add(new Expression(AstCode.LdC, 0, e.getOffset(), new Expression[0]));
                        code = AstCode.CmpLt;
                        break;
                    }
                    case 157: {
                        e.getArguments().add(new Expression(AstCode.LdC, 0, e.getOffset(), new Expression[0]));
                        code = AstCode.CmpGe;
                        break;
                    }
                    case 158: {
                        e.getArguments().add(new Expression(AstCode.LdC, 0, e.getOffset(), new Expression[0]));
                        code = AstCode.CmpGt;
                        break;
                    }
                    case 159: {
                        e.getArguments().add(new Expression(AstCode.LdC, 0, e.getOffset(), new Expression[0]));
                        code = AstCode.CmpLe;
                        break;
                    }
                    case 160: {
                        code = AstCode.CmpEq;
                        break;
                    }
                    case 161: {
                        code = AstCode.CmpNe;
                        break;
                    }
                    case 162: {
                        code = AstCode.CmpLt;
                        break;
                    }
                    case 163: {
                        code = AstCode.CmpGe;
                        break;
                    }
                    case 164: {
                        code = AstCode.CmpGt;
                        break;
                    }
                    case 165: {
                        code = AstCode.CmpLe;
                        break;
                    }
                    case 166: {
                        code = AstCode.CmpEq;
                        break;
                    }
                    case 167: {
                        code = AstCode.CmpNe;
                        break;
                    }
                    case 198: {
                        e.getArguments().add(new Expression(AstCode.AConstNull, null, e.getOffset(), new Expression[0]));
                        code = AstCode.CmpEq;
                        break;
                    }
                    case 199: {
                        e.getArguments().add(new Expression(AstCode.AConstNull, null, e.getOffset(), new Expression[0]));
                        code = AstCode.CmpNe;
                        break;
                    }
                    default: {
                        continue;
                    }
                }
                final Expression newExpression = new Expression(code, null, e.getOffset(), e.getArguments());
                body.set(i, new Expression(AstCode.IfTrue, e.getOperand(), newExpression.getOffset(), new Expression[] { newExpression }));
                newExpression.getRanges().addAll(e.getRanges());
            }
        }
    }
    
    private static void reduceComparisonInstructionSet(final Expression expression) {
        final List<Expression> arguments = expression.getArguments();
        final Expression firstArgument = arguments.isEmpty() ? null : arguments.get(0);
        if (PatternMatching.matchSimplifiableComparison(expression)) {
            arguments.clear();
            arguments.addAll(firstArgument.getArguments());
            expression.getRanges().addAll(firstArgument.getRanges());
        }
        if (PatternMatching.matchReversibleComparison(expression)) {
            AstCode reversedCode = null;
            switch ($SWITCH_TABLE$com$strobel$decompiler$ast$AstCode()[firstArgument.getCode().ordinal()]) {
                case 235: {
                    reversedCode = AstCode.CmpNe;
                    break;
                }
                case 236: {
                    reversedCode = AstCode.CmpEq;
                    break;
                }
                case 237: {
                    reversedCode = AstCode.CmpGe;
                    break;
                }
                case 238: {
                    reversedCode = AstCode.CmpLt;
                    break;
                }
                case 239: {
                    reversedCode = AstCode.CmpLe;
                    break;
                }
                case 240: {
                    reversedCode = AstCode.CmpGt;
                    break;
                }
                default: {
                    throw ContractUtils.unreachable();
                }
            }
            expression.setCode(reversedCode);
            expression.getRanges().addAll(firstArgument.getRanges());
            arguments.clear();
            arguments.addAll(firstArgument.getArguments());
        }
    }
    
    private void splitToMovableBlocks(final Block block) {
        final List<Node> basicBlocks = new ArrayList<Node>();
        final List<Node> body = block.getBody();
        final Object firstNode = CollectionUtilities.firstOrDefault(body);
        Label entryLabel;
        if (firstNode instanceof Label) {
            entryLabel = (Label)firstNode;
        }
        else {
            entryLabel = new Label();
            entryLabel.setName("Block_" + this._nextLabelIndex++);
        }
        BasicBlock basicBlock = new BasicBlock();
        List<Node> basicBlockBody = basicBlock.getBody();
        basicBlocks.add(basicBlock);
        basicBlockBody.add(entryLabel);
        block.setEntryGoto(new Expression(AstCode.Goto, entryLabel, -34, new Expression[0]));
        if (!body.isEmpty()) {
            if (body.get(0) != entryLabel) {
                basicBlockBody.add(body.get(0));
            }
            for (int i = 1; i < body.size(); ++i) {
                final Node lastNode = body.get(i - 1);
                final Node currentNode = body.get(i);
                if (currentNode instanceof Label || currentNode instanceof TryCatchBlock || lastNode.isConditionalControlFlow() || lastNode.isUnconditionalControlFlow()) {
                    final Label label = (Label)((currentNode instanceof Label) ? currentNode : new Label("Block_" + this._nextLabelIndex++));
                    if (!lastNode.isUnconditionalControlFlow()) {
                        basicBlockBody.add(new Expression(AstCode.Goto, label, -34, new Expression[0]));
                    }
                    basicBlock = new BasicBlock();
                    basicBlocks.add(basicBlock);
                    basicBlockBody = basicBlock.getBody();
                    basicBlockBody.add(label);
                    if (currentNode != label) {
                        basicBlockBody.add(currentNode);
                    }
                    if (currentNode instanceof TryCatchBlock) {
                        final Label exitLabel = this.checkExit(currentNode);
                        if (exitLabel != null) {
                            body.add(i + 1, new Expression(AstCode.Goto, exitLabel, -34, new Expression[0]));
                        }
                    }
                }
                else {
                    basicBlockBody.add(currentNode);
                }
            }
        }
        body.clear();
        body.addAll(basicBlocks);
    }
    
    private Label checkExit(final Node node) {
        if (node == null) {
            return null;
        }
        if (node instanceof BasicBlock) {
            return this.checkExit(CollectionUtilities.lastOrDefault(((BasicBlock)node).getBody()));
        }
        if (!(node instanceof TryCatchBlock)) {
            if (node instanceof Expression) {
                final Expression expression = (Expression)node;
                final AstCode code = expression.getCode();
                if (code == AstCode.Goto) {
                    return (Label)expression.getOperand();
                }
            }
            return null;
        }
        final TryCatchBlock tryCatch = (TryCatchBlock)node;
        final Label exitLabel = this.checkExit(CollectionUtilities.lastOrDefault(tryCatch.getTryBlock().getBody()));
        if (exitLabel == null) {
            return null;
        }
        for (final CatchBlock catchBlock : tryCatch.getCatchBlocks()) {
            if (this.checkExit(CollectionUtilities.lastOrDefault(catchBlock.getBody())) != exitLabel) {
                return null;
            }
        }
        final Block finallyBlock = tryCatch.getFinallyBlock();
        if (finallyBlock != null && this.checkExit(CollectionUtilities.lastOrDefault(finallyBlock.getBody())) != exitLabel) {
            return null;
        }
        return exitLabel;
    }
    
    private static boolean mergeDisparateObjectInitializations(final DecompilerContext context, final Block method) {
        final Inlining inlining = new Inlining(context, method);
        final Map<Node, Node> parentLookup = new IdentityHashMap<Node, Node>();
        final Map<Variable, Expression> newExpressions = new IdentityHashMap<Variable, Expression>();
        final StrongBox<Variable> variable = new StrongBox<Variable>();
        final StrongBox<MethodReference> ctor = new StrongBox<MethodReference>();
        final List<Expression> args = new ArrayList<Expression>();
        boolean anyChanged = false;
        parentLookup.put(method, Node.NULL);
        for (final Node node : method.getSelfAndChildrenRecursive(Node.class)) {
            if (PatternMatching.matchStore(node, variable, args) && PatternMatching.match(CollectionUtilities.single(args), AstCode.__New)) {
                newExpressions.put(variable.get(), (Expression)node);
            }
            for (final Node child : node.getChildren()) {
                if (parentLookup.containsKey(child)) {
                    throw Error.expressionLinkedFromMultipleLocations(child);
                }
                parentLookup.put(child, node);
            }
        }
        for (final Expression e : method.getSelfAndChildrenRecursive(Expression.class)) {
            if (PatternMatching.matchGetArguments(e, AstCode.InvokeSpecial, ctor, args) && ctor.get().isConstructor() && args.size() > 0 && PatternMatching.matchLoad(CollectionUtilities.first(args), variable)) {
                final Expression storeNew = newExpressions.get(variable.value);
                if (storeNew == null || Inlining.count(inlining.storeCounts, variable.value) != 1) {
                    continue;
                }
                final Node parent = parentLookup.get(storeNew);
                if (!(parent instanceof Block) && !(parent instanceof BasicBlock)) {
                    continue;
                }
                List<Node> body;
                if (parent instanceof Block) {
                    body = ((Block)parent).getBody();
                }
                else {
                    body = ((BasicBlock)parent).getBody();
                }
                boolean moveInitToNew = false;
                if (parentLookup.get(e) == parent) {
                    final int newIndex = body.indexOf(storeNew);
                    final int initIndex = body.indexOf(e);
                    if (initIndex > newIndex) {
                        for (int i = newIndex + 1; i < initIndex; ++i) {
                            if (references(body.get(i), variable.value)) {
                                moveInitToNew = true;
                                break;
                            }
                        }
                    }
                }
                final Expression toRemove = moveInitToNew ? e : storeNew;
                final Expression toRewrite = moveInitToNew ? storeNew : e;
                final List<Expression> arguments = e.getArguments();
                final Expression initExpression = new Expression(AstCode.InitObject, ctor.get(), storeNew.getOffset(), new Expression[0]);
                arguments.remove(0);
                initExpression.getArguments().addAll(arguments);
                initExpression.getRanges().addAll(e.getRanges());
                body.remove(toRemove);
                toRewrite.setCode(AstCode.Store);
                toRewrite.setOperand(variable.value);
                toRewrite.getArguments().clear();
                toRewrite.getArguments().add(initExpression);
                anyChanged = true;
            }
        }
        return anyChanged;
    }
    
    private static void flattenBasicBlocks(final Node node) {
        if (node instanceof Block) {
            final Block block = (Block)node;
            final List<Node> flatBody = new ArrayList<Node>();
            for (final Node child : block.getChildren()) {
                flattenBasicBlocks(child);
                if (child instanceof BasicBlock) {
                    final BasicBlock childBasicBlock = (BasicBlock)child;
                    final Node firstChild = CollectionUtilities.firstOrDefault(childBasicBlock.getBody());
                    final Node lastChild = CollectionUtilities.lastOrDefault(childBasicBlock.getBody());
                    if (!(firstChild instanceof Label)) {
                        throw new IllegalStateException("Basic block must start with a label.");
                    }
                    if (lastChild instanceof Expression && !lastChild.isUnconditionalControlFlow()) {
                        throw new IllegalStateException("Basic block must end with an unconditional branch.");
                    }
                    flatBody.addAll(childBasicBlock.getBody());
                }
                else {
                    flatBody.add(child);
                }
            }
            block.setEntryGoto(null);
            block.getBody().clear();
            block.getBody().addAll(flatBody);
        }
        else if (node != null) {
            for (final Node child2 : node.getChildren()) {
                flattenBasicBlocks(child2);
            }
        }
    }
    
    private static void duplicateReturnStatements(final Block method) {
        final List<Node> methodBody = method.getBody();
        final Map<Node, Node> nextSibling = new IdentityHashMap<Node, Node>();
        final StrongBox<Object> constant = new StrongBox<Object>();
        final StrongBox<Variable> localVariable = new StrongBox<Variable>();
        final StrongBox<Label> targetLabel = new StrongBox<Label>();
        final List<Expression> returnArguments = new ArrayList<Expression>();
        for (final Block block : method.getSelfAndChildrenRecursive(Block.class)) {
            final List<Node> body = block.getBody();
            for (int i = 0; i < body.size() - 1; ++i) {
                final Node current = body.get(i);
                if (current instanceof Label) {
                    nextSibling.put(current, body.get(i + 1));
                }
            }
        }
        for (final Block block : method.getSelfAndChildrenRecursive(Block.class)) {
            final List<Node> body = block.getBody();
            for (int i = 0; i < body.size(); ++i) {
                final Node node = body.get(i);
                if (PatternMatching.matchGetOperand(node, AstCode.Goto, targetLabel)) {
                    while (nextSibling.get(targetLabel.get()) instanceof Label) {
                        targetLabel.accept(nextSibling.get(targetLabel.get()));
                    }
                    final Node target = nextSibling.get(targetLabel.get());
                    if (target != null && PatternMatching.matchGetArguments(target, AstCode.Return, returnArguments)) {
                        if (returnArguments.isEmpty()) {
                            body.set(i, new Expression(AstCode.Return, null, -34, new Expression[0]));
                        }
                        else if (PatternMatching.matchGetOperand(returnArguments.get(0), AstCode.Load, localVariable)) {
                            body.set(i, new Expression(AstCode.Return, null, -34, new Expression[] { new Expression(AstCode.Load, localVariable.get(), -34, new Expression[0]) }));
                        }
                        else if (PatternMatching.matchGetOperand(returnArguments.get(0), AstCode.LdC, constant)) {
                            body.set(i, new Expression(AstCode.Return, null, -34, new Expression[] { new Expression(AstCode.LdC, constant.get(), -34, new Expression[0]) }));
                        }
                    }
                    else if (!methodBody.isEmpty() && methodBody.get(methodBody.size() - 1) == targetLabel.get()) {
                        body.set(i, new Expression(AstCode.Return, null, -34, new Expression[0]));
                    }
                }
            }
        }
    }
    
    private static void reduceIfNesting(final Node node) {
        if (node instanceof Block) {
            final Block block = (Block)node;
            final List<Node> blockBody = block.getBody();
            for (int i = 0; i < blockBody.size(); ++i) {
                final Node n = blockBody.get(i);
                if (n instanceof Condition) {
                    final Condition condition = (Condition)n;
                    final Node trueEnd = CollectionUtilities.lastOrDefault(condition.getTrueBlock().getBody());
                    final Node falseEnd = CollectionUtilities.lastOrDefault(condition.getFalseBlock().getBody());
                    final boolean trueExits = trueEnd != null && trueEnd.isUnconditionalControlFlow();
                    final boolean falseExits = falseEnd != null && falseEnd.isUnconditionalControlFlow();
                    if (trueExits) {
                        blockBody.addAll(i + 1, condition.getFalseBlock().getChildren());
                        condition.setFalseBlock(new Block());
                    }
                    else if (falseExits) {
                        blockBody.addAll(i + 1, condition.getTrueBlock().getChildren());
                        condition.setTrueBlock(new Block());
                    }
                    if (condition.getTrueBlock().getChildren().isEmpty() && !condition.getFalseBlock().getChildren().isEmpty()) {
                        final Block temp = condition.getTrueBlock();
                        final Expression conditionExpression = condition.getCondition();
                        condition.setTrueBlock(condition.getFalseBlock());
                        condition.setFalseBlock(temp);
                        condition.setCondition(simplifyLogicalNot(new Expression(AstCode.LogicalNot, null, conditionExpression.getOffset(), new Expression[] { conditionExpression })));
                    }
                }
            }
        }
        for (final Node child : node.getChildren()) {
            if (child != null && !(child instanceof Expression)) {
                reduceIfNesting(child);
            }
        }
    }
    
    private static void recombineVariables(final Block method) {
        final Map<VariableDefinition, Variable> map = new IdentityHashMap<VariableDefinition, Variable>();
        replaceVariables(method, new Function<Variable, Variable>() {
            @Override
            public final Variable apply(final Variable v) {
                final VariableDefinition originalVariable = v.getOriginalVariable();
                if (originalVariable == null) {
                    return v;
                }
                Variable combinedVariable = map.get(originalVariable);
                if (combinedVariable == null) {
                    map.put(originalVariable, v);
                    combinedVariable = v;
                }
                return combinedVariable;
            }
        });
    }
    
    private static boolean runOptimization(final Block block, final BasicBlockOptimization optimization) {
        boolean modified = false;
        final List<Node> body = block.getBody();
        for (int i = body.size() - 1; i >= 0; --i) {
            if (i < body.size() && optimization.run(body, body.get(i), i)) {
                modified = true;
                ++i;
            }
        }
        return modified;
    }
    
    private static boolean runOptimization(final Block block, final ExpressionOptimization optimization) {
        boolean modified = false;
        for (final Node node : block.getBody()) {
            final BasicBlock basicBlock = (BasicBlock)node;
            final List<Node> body = basicBlock.getBody();
            for (int i = body.size() - 1; i >= 0; --i) {
                if (i < body.size()) {
                    final Node n = body.get(i);
                    if (n instanceof Expression && optimization.run(body, (Expression)n, i)) {
                        modified = true;
                        ++i;
                    }
                }
            }
        }
        return modified;
    }
    
    public static void replaceVariables(final Node node, final Function<Variable, Variable> mapping) {
        if (node instanceof Expression) {
            final Expression expression = (Expression)node;
            final Object operand = expression.getOperand();
            if (operand instanceof Variable) {
                expression.setOperand(mapping.apply((Variable)operand));
            }
            for (final Expression argument : expression.getArguments()) {
                replaceVariables(argument, mapping);
            }
        }
        else {
            if (node instanceof CatchBlock) {
                final CatchBlock catchBlock = (CatchBlock)node;
                final Variable exceptionVariable = catchBlock.getExceptionVariable();
                if (exceptionVariable != null) {
                    catchBlock.setExceptionVariable(mapping.apply(exceptionVariable));
                }
            }
            for (final Node child : node.getChildren()) {
                replaceVariables(child, mapping);
            }
        }
    }
    
    static <T> void removeOrThrow(final Collection<T> collection, final T item) {
        if (!collection.remove(item)) {
            throw new IllegalStateException("The item was not found in the collection.");
        }
    }
    
    static void removeTail(final List<Node> body, final AstCode... codes) {
        for (int i = 0; i < codes.length; ++i) {
            if (body.get(body.size() - codes.length + i).getCode() != codes[i]) {
                throw new IllegalStateException("Tailing code does not match expected.");
            }
        }
        for (final AstCode code : codes) {
            body.remove(body.size() - 1);
        }
    }
    
    static Expression makeLeftAssociativeShortCircuit(final AstCode code, final Expression left, final Expression right) {
        if (PatternMatching.match(right, code)) {
            Expression current;
            for (current = right; PatternMatching.match(current.getArguments().get(0), code); current = current.getArguments().get(0)) {}
            final Expression newArgument = new Expression(code, null, left.getOffset(), new Expression[] { left, current.getArguments().get(0) });
            newArgument.setInferredType(BuiltinTypes.Boolean);
            current.getArguments().set(0, newArgument);
            return right;
        }
        final Expression newExpression = new Expression(code, null, left.getOffset(), new Expression[] { left, right });
        newExpression.setInferredType(BuiltinTypes.Boolean);
        return newExpression;
    }
    
    static Expression simplifyLogicalNot(final Expression expression) {
        final Expression result = simplifyLogicalNot(expression, AstOptimizer.SCRATCH_BOOLEAN_BOX);
        return (result != null) ? result : expression;
    }
    
    static Expression simplifyLogicalNot(final Expression expression, final BooleanBox modified) {
        Expression e = expression;
        List<Expression> arguments = e.getArguments();
        final StrongBox<Boolean> b = new StrongBox<Boolean>();
        final Expression operand = arguments.isEmpty() ? null : arguments.get(0);
        Expression a;
        if (e.getCode() == AstCode.CmpEq && TypeAnalysis.isBoolean(operand.getInferredType()) && PatternMatching.matchBooleanConstant(a = arguments.get(1), b) && Boolean.FALSE.equals(b.get())) {
            e.setCode(AstCode.LogicalNot);
            e.getRanges().addAll(a.getRanges());
            arguments.remove(1);
            modified.set(true);
        }
        Expression result = null;
        if (e.getCode() == AstCode.CmpNe && TypeAnalysis.isBoolean(operand.getInferredType()) && PatternMatching.matchBooleanConstant(arguments.get(1), b) && Boolean.FALSE.equals(b.get())) {
            modified.set(true);
            return e.getArguments().get(0);
        }
        if (e.getCode() == AstCode.TernaryOp) {
            final Expression condition = arguments.get(0);
            if (PatternMatching.match(condition, AstCode.LogicalNot)) {
                final Expression temp = arguments.get(1);
                arguments.set(0, condition.getArguments().get(0));
                arguments.set(1, arguments.get(2));
                arguments.set(2, temp);
            }
        }
        while (e.getCode() == AstCode.LogicalNot) {
            a = operand;
            if (a.getCode() == AstCode.LogicalNot) {
                result = a.getArguments().get(0);
                result.getRanges().addAll(e.getRanges());
                result.getRanges().addAll(a.getRanges());
                e = result;
                arguments = e.getArguments();
            }
            else {
                if (simplifyLogicalNotArgument(a)) {
                    e = (result = a);
                    arguments = e.getArguments();
                    modified.set(true);
                    break;
                }
                break;
            }
        }
        for (int i = 0; i < arguments.size(); ++i) {
            a = simplifyLogicalNot(arguments.get(i), modified);
            if (a != null) {
                arguments.set(i, a);
                modified.set(true);
            }
        }
        return result;
    }
    
    static boolean simplifyLogicalNotArgument(final Expression e) {
        if (!canSimplifyLogicalNotArgument(e)) {
            return false;
        }
        final List<Expression> arguments = e.getArguments();
        switch ($SWITCH_TABLE$com$strobel$decompiler$ast$AstCode()[e.getCode().ordinal()]) {
            case 235:
            case 236:
            case 237:
            case 238:
            case 239:
            case 240: {
                e.setCode(e.getCode().reverse());
                return true;
            }
            case 245: {
                final Expression a = arguments.get(0);
                e.setCode(a.getCode());
                e.setOperand(a.getOperand());
                arguments.clear();
                arguments.addAll(a.getArguments());
                e.getRanges().addAll(a.getRanges());
                return true;
            }
            case 246:
            case 247: {
                if (!simplifyLogicalNotArgument(arguments.get(0))) {
                    negate(arguments.get(0));
                }
                if (!simplifyLogicalNotArgument(arguments.get(1))) {
                    negate(arguments.get(1));
                }
                e.setCode(e.getCode().reverse());
                return true;
            }
            case 253: {
                simplifyLogicalNotArgument(arguments.get(1));
                simplifyLogicalNotArgument(arguments.get(2));
                return true;
            }
            default: {
                return TypeAnalysis.isBoolean(e.getInferredType()) && negate(e);
            }
        }
    }
    
    private static boolean negate(final Expression e) {
        if (TypeAnalysis.isBoolean(e.getInferredType())) {
            final Expression copy = e.clone();
            e.setCode(AstCode.LogicalNot);
            e.setOperand(null);
            e.getArguments().clear();
            e.getArguments().add(copy);
            return true;
        }
        return false;
    }
    
    private static boolean canSimplifyLogicalNotArgument(final Expression e) {
        switch ($SWITCH_TABLE$com$strobel$decompiler$ast$AstCode()[e.getCode().ordinal()]) {
            case 235:
            case 236:
            case 237:
            case 238:
            case 239:
            case 240: {
                return true;
            }
            case 245: {
                return true;
            }
            case 246:
            case 247: {
                final List<Expression> arguments = e.getArguments();
                return canSimplifyLogicalNotArgument(arguments.get(0)) || canSimplifyLogicalNotArgument(arguments.get(1));
            }
            case 253: {
                return TypeAnalysis.isBoolean(e.getInferredType()) && canSimplifyLogicalNotArgument(e.getArguments().get(1)) && canSimplifyLogicalNotArgument(e.getArguments().get(2));
            }
            default: {
                return false;
            }
        }
    }
    
    static boolean references(final Node node, final Variable v) {
        for (final Expression e : node.getSelfAndChildrenRecursive(Expression.class)) {
            if (PatternMatching.matchLoad(e, v)) {
                return true;
            }
        }
        return false;
    }
    
    private static boolean containsMatch(final Node node, final Expression pattern) {
        for (final Expression e : node.getSelfAndChildrenRecursive(Expression.class)) {
            if (e.isEquivalentTo(pattern)) {
                return true;
            }
        }
        return false;
    }
    
    static /* synthetic */ boolean access$0(final Node param_0, final Expression param_1) {
        return containsMatch(param_0, param_1);
    }
    
    static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$decompiler$ast$AstCode() {
        final int[] loc_0 = AstOptimizer.$SWITCH_TABLE$com$strobel$decompiler$ast$AstCode;
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
        return AstOptimizer.$SWITCH_TABLE$com$strobel$decompiler$ast$AstCode = loc_1;
    }
    
    private static final class RemoveInnerClassInitSecurityChecksOptimization extends AbstractExpressionOptimization
    {
        protected RemoveInnerClassInitSecurityChecksOptimization(final DecompilerContext context, final Block method) {
            super(context, method);
        }
        
        @Override
        public boolean run(final List<Node> body, final Expression head, final int position) {
            final StrongBox<Expression> getClassArgument = new StrongBox<Expression>();
            final StrongBox<Variable> getClassArgumentVariable = new StrongBox<Variable>();
            final StrongBox<Variable> constructorTargetVariable = new StrongBox<Variable>();
            final StrongBox<Variable> constructorArgumentVariable = new StrongBox<Variable>();
            final StrongBox<MethodReference> constructor = new StrongBox<MethodReference>();
            final StrongBox<MethodReference> getClassMethod = new StrongBox<MethodReference>();
            final List<Expression> arguments = new ArrayList<Expression>();
            if (position > 0) {
                final Node previous = body.get(position - 1);
                arguments.clear();
                if (PatternMatching.matchGetArguments(head, AstCode.InvokeSpecial, constructor, arguments) && arguments.size() > 1 && PatternMatching.matchGetOperand(arguments.get(0), AstCode.Load, constructorTargetVariable) && PatternMatching.matchGetOperand(arguments.get(1), AstCode.Load, constructorArgumentVariable) && PatternMatching.matchGetArgument(previous, AstCode.InvokeVirtual, getClassMethod, getClassArgument) && isGetClassMethod(getClassMethod.get()) && PatternMatching.matchGetOperand(getClassArgument.get(), AstCode.Load, getClassArgumentVariable) && getClassArgumentVariable.get() == constructorArgumentVariable.get()) {
                    final TypeReference constructorTargetType = constructorTargetVariable.get().getType();
                    final TypeReference constructorArgumentType = constructorArgumentVariable.get().getType();
                    if (constructorTargetType != null && constructorArgumentType != null) {
                        final TypeDefinition resolvedConstructorTargetType = constructorTargetType.resolve();
                        final TypeDefinition resolvedConstructorArgumentType = constructorArgumentType.resolve();
                        if (resolvedConstructorTargetType != null && resolvedConstructorArgumentType != null && resolvedConstructorTargetType.isNested() && !resolvedConstructorTargetType.isStatic() && (!resolvedConstructorArgumentType.isNested() || isEnclosedBy(resolvedConstructorTargetType, resolvedConstructorArgumentType))) {
                            body.remove(position - 1);
                            return true;
                        }
                    }
                }
            }
            return false;
        }
        
        private static boolean isGetClassMethod(final MethodReference method) {
            return method.getParameters().isEmpty() && StringUtilities.equals(method.getName(), "getClass");
        }
        
        private static boolean isEnclosedBy(final TypeReference innerType, final TypeReference outerType) {
            if (innerType == null) {
                return false;
            }
            for (TypeReference current = innerType.getDeclaringType(); current != null; current = current.getDeclaringType()) {
                if (MetadataResolver.areEquivalent(current, outerType)) {
                    return true;
                }
            }
            final TypeDefinition resolvedInnerType = innerType.resolve();
            return resolvedInnerType != null && isEnclosedBy(resolvedInnerType.getBaseType(), outerType);
        }
    }
    
    private static final class SimplifyShortCircuitOptimization extends AbstractBasicBlockOptimization
    {
        public SimplifyShortCircuitOptimization(final DecompilerContext context, final Block method) {
            super(context, method);
        }
        
        @Override
        public final boolean run(final List<Node> body, final BasicBlock head, final int position) {
            assert body.contains(head);
            final StrongBox<Expression> condition = new StrongBox<Expression>();
            final StrongBox<Label> trueLabel = new StrongBox<Label>();
            final StrongBox<Label> falseLabel = new StrongBox<Label>();
            final StrongBox<Expression> nextCondition = new StrongBox<Expression>();
            final StrongBox<Label> nextTrueLabel = new StrongBox<Label>();
            final StrongBox<Label> nextFalseLabel = new StrongBox<Label>();
            if (PatternMatching.matchLastAndBreak(head, AstCode.IfTrue, trueLabel, condition, falseLabel)) {
                for (int pass = 0; pass < 2; ++pass) {
                    final Label nextLabel = (pass == 0) ? trueLabel.get() : falseLabel.get();
                    final Label otherLabel = (pass == 0) ? falseLabel.get() : trueLabel.get();
                    final boolean negate = pass == 1;
                    final BasicBlock next = this.labelToBasicBlock.get(nextLabel);
                    if (body.contains(next) && next != head && this.labelGlobalRefCount.get(nextLabel).getValue() == 1 && PatternMatching.matchSingleAndBreak(next, AstCode.IfTrue, nextTrueLabel, nextCondition, nextFalseLabel) && (otherLabel == nextFalseLabel.get() || otherLabel == nextTrueLabel.get())) {
                        Expression logicExpression;
                        if (otherLabel == nextFalseLabel.get()) {
                            logicExpression = AstOptimizer.makeLeftAssociativeShortCircuit(AstCode.LogicalAnd, negate ? new Expression(AstCode.LogicalNot, null, condition.get().getOffset(), new Expression[] { condition.get() }) : condition.get(), nextCondition.get());
                        }
                        else {
                            logicExpression = AstOptimizer.makeLeftAssociativeShortCircuit(AstCode.LogicalOr, negate ? condition.get() : new Expression(AstCode.LogicalNot, null, condition.get().getOffset(), new Expression[] { condition.get() }), nextCondition.get());
                        }
                        final List<Node> headBody = head.getBody();
                        AstOptimizer.removeTail(headBody, AstCode.IfTrue, AstCode.Goto);
                        headBody.add(new Expression(AstCode.IfTrue, nextTrueLabel.get(), logicExpression.getOffset(), new Expression[] { logicExpression }));
                        headBody.add(new Expression(AstCode.Goto, nextFalseLabel.get(), logicExpression.getOffset(), new Expression[0]));
                        this.labelGlobalRefCount.get(trueLabel.get()).decrement();
                        this.labelGlobalRefCount.get(falseLabel.get()).decrement();
                        AstOptimizer.removeOrThrow(body, next);
                        return true;
                    }
                }
            }
            return false;
        }
    }
    
    private static final class PreProcessShortCircuitAssignmentsOptimization extends AbstractBasicBlockOptimization
    {
        public PreProcessShortCircuitAssignmentsOptimization(final DecompilerContext context, final Block method) {
            super(context, method);
        }
        
        @Override
        public final boolean run(final List<Node> body, final BasicBlock head, final int position) {
            assert body.contains(head);
            final StrongBox<Expression> condition = new StrongBox<Expression>();
            final StrongBox<Label> trueLabel = new StrongBox<Label>();
            final StrongBox<Label> falseLabel = new StrongBox<Label>();
            if (PatternMatching.matchLastAndBreak(head, AstCode.IfTrue, trueLabel, condition, falseLabel)) {
                final StrongBox<Label> nextTrueLabel = new StrongBox<Label>();
                final StrongBox<Label> nextFalseLabel = new StrongBox<Label>();
                final StrongBox<Variable> sourceVariable = new StrongBox<Variable>();
                final StrongBox<Expression> assignedValue = new StrongBox<Expression>();
                final StrongBox<Expression> equivalentLoad = new StrongBox<Expression>();
                final StrongBox<Expression> left = new StrongBox<Expression>();
                final StrongBox<Expression> right = new StrongBox<Expression>();
                boolean modified = false;
                int pass = 0;
                while (pass < 2) {
                    final Label nextLabel = (pass == 0) ? trueLabel.get() : falseLabel.get();
                    final Label otherLabel = (pass == 0) ? falseLabel.get() : trueLabel.get();
                    final BasicBlock next = this.labelToBasicBlock.get(nextLabel);
                    final BasicBlock other = this.labelToBasicBlock.get(otherLabel);
                    if (body.contains(next) && next != head && this.labelGlobalRefCount.get(nextLabel).getValue() == 1 && PatternMatching.matchLastAndBreak(next, AstCode.IfTrue, nextTrueLabel, condition, nextFalseLabel) && (otherLabel == nextFalseLabel.get() || otherLabel == nextTrueLabel.get())) {
                        final List<Node> nextBody = next.getBody();
                        final List<Node> otherBody = other.getBody();
                        while (nextBody.size() > 3 && PatternMatching.matchAssignment(nextBody.get(nextBody.size() - 3), assignedValue, equivalentLoad) && PatternMatching.matchLoad(assignedValue.value, sourceVariable) && PatternMatching.matchComparison(condition.value, left, right)) {
                            if (PatternMatching.matchLoad(left.value, sourceVariable.value)) {
                                condition.value.getArguments().set(0, nextBody.get(nextBody.size() - 3));
                                nextBody.remove(nextBody.size() - 3);
                                modified = true;
                            }
                            else {
                                if (!PatternMatching.matchLoad(right.value, sourceVariable.value) || AstOptimizer.access$0(left.value, equivalentLoad.value)) {
                                    break;
                                }
                                condition.value.getArguments().set(1, nextBody.get(nextBody.size() - 3));
                                nextBody.remove(nextBody.size() - 3);
                                modified = true;
                            }
                        }
                        final boolean modifiedNext = modified;
                        modified = false;
                        while (PatternMatching.matchAssignmentAndConditionalBreak(other, assignedValue, condition, trueLabel, falseLabel, equivalentLoad) && PatternMatching.matchLoad(assignedValue.value, sourceVariable) && PatternMatching.matchComparison(condition.value, left, right)) {
                            if (PatternMatching.matchLoad(left.value, sourceVariable.value)) {
                                condition.value.getArguments().set(0, otherBody.get(otherBody.size() - 3));
                                otherBody.remove(otherBody.size() - 3);
                                modified = true;
                            }
                            else {
                                if (!PatternMatching.matchLoad(right.value, sourceVariable.value) || AstOptimizer.access$0(left.value, equivalentLoad.value)) {
                                    break;
                                }
                                condition.value.getArguments().set(1, otherBody.get(otherBody.size() - 3));
                                otherBody.remove(otherBody.size() - 3);
                                modified = true;
                            }
                        }
                        final boolean modifiedOther = modified;
                        if (modifiedNext || modifiedOther) {
                            final Inlining inlining = new Inlining(this.context, this.method);
                            if (modifiedNext) {
                                inlining.inlineAllInBasicBlock(next);
                            }
                            if (modifiedOther) {
                                inlining.inlineAllInBasicBlock(other);
                            }
                            return true;
                        }
                        return false;
                    }
                    else {
                        ++pass;
                    }
                }
            }
            return false;
        }
    }
    
    private static final class InlineConditionalAssignmentsOptimization extends AbstractBasicBlockOptimization
    {
        public InlineConditionalAssignmentsOptimization(final DecompilerContext context, final Block method) {
            super(context, method);
        }
        
        @Override
        public final boolean run(final List<Node> body, final BasicBlock head, final int position) {
            assert body.contains(head);
            final StrongBox<Expression> condition = new StrongBox<Expression>();
            final StrongBox<Label> trueLabel = new StrongBox<Label>();
            final StrongBox<Label> falseLabel = new StrongBox<Label>();
            if (PatternMatching.matchLastAndBreak(head, AstCode.IfTrue, trueLabel, condition, falseLabel)) {
                final StrongBox<Variable> sourceVariable = new StrongBox<Variable>();
                final StrongBox<Expression> assignedValue = new StrongBox<Expression>();
                final StrongBox<Expression> equivalentLoad = new StrongBox<Expression>();
                final StrongBox<Expression> left = new StrongBox<Expression>();
                final StrongBox<Expression> right = new StrongBox<Expression>();
                final Label thenLabel = trueLabel.value;
                final Label elseLabel = falseLabel.value;
                final BasicBlock thenSuccessor = this.labelToBasicBlock.get(thenLabel);
                final BasicBlock elseSuccessor = this.labelToBasicBlock.get(elseLabel);
                boolean modified = false;
                if (PatternMatching.matchAssignmentAndConditionalBreak(elseSuccessor, assignedValue, condition, trueLabel, falseLabel, equivalentLoad) && PatternMatching.matchLoad(assignedValue.value, sourceVariable) && PatternMatching.matchComparison(condition.value, left, right)) {
                    final List<Node> b = elseSuccessor.getBody();
                    if (PatternMatching.matchLoad(left.value, sourceVariable.value)) {
                        condition.value.getArguments().set(0, b.get(b.size() - 3));
                        b.remove(b.size() - 3);
                        modified = true;
                    }
                    else if (PatternMatching.matchLoad(right.value, sourceVariable.value) && !AstOptimizer.access$0(left.value, equivalentLoad.value)) {
                        condition.value.getArguments().set(1, b.get(b.size() - 3));
                        b.remove(b.size() - 3);
                        modified = true;
                    }
                }
                if (PatternMatching.matchAssignmentAndConditionalBreak(thenSuccessor, assignedValue, condition, trueLabel, falseLabel, equivalentLoad) && PatternMatching.matchLoad(assignedValue.value, sourceVariable) && PatternMatching.matchComparison(condition.value, left, right)) {
                    final List<Node> b = thenSuccessor.getBody();
                    if (PatternMatching.matchLoad(left.value, sourceVariable.value)) {
                        condition.value.getArguments().set(0, b.get(b.size() - 3));
                        b.remove(b.size() - 3);
                        modified = true;
                    }
                    else if (PatternMatching.matchLoad(right.value, sourceVariable.value) && !AstOptimizer.access$0(left.value, equivalentLoad.value)) {
                        condition.value.getArguments().set(1, b.get(b.size() - 3));
                        b.remove(b.size() - 3);
                        modified = true;
                    }
                }
                return modified;
            }
            return false;
        }
    }
    
    private static final class JoinBasicBlocksOptimization extends AbstractBasicBlockOptimization
    {
        protected JoinBasicBlocksOptimization(final DecompilerContext context, final Block method) {
            super(context, method);
        }
        
        @Override
        public final boolean run(final List<Node> body, final BasicBlock head, final int position) {
            final StrongBox<Label> nextLabel = new StrongBox<Label>();
            final List<Node> headBody = head.getBody();
            final Node secondToLast = CollectionUtilities.getOrDefault(headBody, headBody.size() - 2);
            final BasicBlock nextBlock;
            if (secondToLast != null && !secondToLast.isConditionalControlFlow() && PatternMatching.matchGetOperand(headBody.get(headBody.size() - 1), AstCode.Goto, nextLabel) && (this.labelGlobalRefCount.get(nextLabel.get()).getValue() == 1 & (nextBlock = this.labelToBasicBlock.get(nextLabel.get())) != null) && nextBlock != JoinBasicBlocksOptimization.EMPTY_BLOCK && body.contains(nextBlock) && nextBlock.getBody().get(0) == nextLabel.get() && !CollectionUtilities.any(nextBlock.getBody(), Predicates.instanceOf(BasicBlock.class))) {
                final Node secondInNext = CollectionUtilities.getOrDefault(nextBlock.getBody(), 1);
                if (secondInNext instanceof TryCatchBlock) {
                    final Block tryBlock = ((TryCatchBlock)secondInNext).getTryBlock();
                    final Node firstInTry = CollectionUtilities.firstOrDefault(tryBlock.getBody());
                    if (firstInTry instanceof BasicBlock) {
                        final Node firstInTryBody = CollectionUtilities.firstOrDefault(((BasicBlock)firstInTry).getBody());
                        if (firstInTryBody instanceof Label && this.labelGlobalRefCount.get(firstInTryBody).getValue() > 1) {
                            return false;
                        }
                    }
                }
                AstOptimizer.removeTail(headBody, AstCode.Goto);
                nextBlock.getBody().remove(0);
                headBody.addAll(nextBlock.getBody());
                AstOptimizer.removeOrThrow(body, nextBlock);
                return true;
            }
            return false;
        }
    }
    
    private static final class SimplifyTernaryOperatorOptimization extends AbstractBasicBlockOptimization
    {
        protected SimplifyTernaryOperatorOptimization(final DecompilerContext context, final Block method) {
            super(context, method);
        }
        
        @Override
        public final boolean run(final List<Node> body, final BasicBlock head, final int position) {
            final StrongBox<Expression> condition = new StrongBox<Expression>();
            final StrongBox<Label> trueLabel = new StrongBox<Label>();
            final StrongBox<Label> falseLabel = new StrongBox<Label>();
            final StrongBox<Variable> trueVariable = new StrongBox<Variable>();
            final StrongBox<Expression> trueExpression = new StrongBox<Expression>();
            final StrongBox<Label> trueFall = new StrongBox<Label>();
            final StrongBox<Variable> falseVariable = new StrongBox<Variable>();
            final StrongBox<Expression> falseExpression = new StrongBox<Expression>();
            final StrongBox<Label> falseFall = new StrongBox<Label>();
            final StrongBox<Object> unused = new StrongBox<Object>();
            if (PatternMatching.matchLastAndBreak(head, AstCode.IfTrue, trueLabel, condition, falseLabel) && this.labelGlobalRefCount.get(trueLabel.value).getValue() == 1 && this.labelGlobalRefCount.get(falseLabel.value).getValue() == 1 && body.contains(this.labelToBasicBlock.get(trueLabel.value)) && body.contains(this.labelToBasicBlock.get(falseLabel.value))) {
                if ((PatternMatching.matchSingleAndBreak(this.labelToBasicBlock.get(trueLabel.value), AstCode.Store, trueVariable, trueExpression, trueFall) && PatternMatching.matchSingleAndBreak(this.labelToBasicBlock.get(falseLabel.value), AstCode.Store, falseVariable, falseExpression, falseFall) && trueVariable.value == falseVariable.value && trueFall.value == falseFall.value) || (PatternMatching.matchSingle(this.labelToBasicBlock.get(trueLabel.value), AstCode.Return, unused, trueExpression) && PatternMatching.matchSingle(this.labelToBasicBlock.get(falseLabel.value), AstCode.Return, unused, falseExpression))) {
                    final boolean isStore = trueVariable.value != null;
                    final AstCode opCode = isStore ? AstCode.Store : AstCode.Return;
                    final TypeReference returnType = isStore ? trueVariable.value.getType() : this.context.getCurrentMethod().getReturnType();
                    final boolean returnTypeIsBoolean = TypeAnalysis.isBoolean(returnType);
                    final StrongBox<Boolean> leftBooleanValue = new StrongBox<Boolean>();
                    final StrongBox<Boolean> rightBooleanValue = new StrongBox<Boolean>();
                    Expression newExpression;
                    if (returnTypeIsBoolean && PatternMatching.matchBooleanConstant(trueExpression.value, leftBooleanValue) && PatternMatching.matchBooleanConstant(falseExpression.value, rightBooleanValue) && ((leftBooleanValue.value && !rightBooleanValue.value) || (!leftBooleanValue.value && rightBooleanValue.value))) {
                        if (leftBooleanValue.value) {
                            newExpression = condition.value;
                        }
                        else {
                            newExpression = new Expression(AstCode.LogicalNot, null, condition.value.getOffset(), new Expression[] { condition.value });
                            newExpression.setInferredType(BuiltinTypes.Boolean);
                        }
                    }
                    else if ((returnTypeIsBoolean || TypeAnalysis.isBoolean(falseExpression.value.getInferredType())) && PatternMatching.matchBooleanConstant(trueExpression.value, leftBooleanValue)) {
                        if (leftBooleanValue.value) {
                            newExpression = AstOptimizer.makeLeftAssociativeShortCircuit(AstCode.LogicalOr, condition.value, falseExpression.value);
                        }
                        else {
                            newExpression = AstOptimizer.makeLeftAssociativeShortCircuit(AstCode.LogicalAnd, new Expression(AstCode.LogicalNot, null, condition.value.getOffset(), new Expression[] { condition.value }), falseExpression.value);
                        }
                    }
                    else if ((returnTypeIsBoolean || TypeAnalysis.isBoolean(trueExpression.value.getInferredType())) && PatternMatching.matchBooleanConstant(falseExpression.value, rightBooleanValue)) {
                        if (rightBooleanValue.value) {
                            newExpression = AstOptimizer.makeLeftAssociativeShortCircuit(AstCode.LogicalOr, new Expression(AstCode.LogicalNot, null, condition.value.getOffset(), new Expression[] { condition.value }), trueExpression.value);
                        }
                        else {
                            newExpression = AstOptimizer.makeLeftAssociativeShortCircuit(AstCode.LogicalAnd, condition.value, trueExpression.value);
                        }
                    }
                    else {
                        if (opCode == AstCode.Return) {
                            return false;
                        }
                        if (opCode == AstCode.Store && !trueVariable.value.isGenerated()) {
                            return false;
                        }
                        if (AstOptimizer.simplifyLogicalNotArgument(condition.value)) {
                            newExpression = new Expression(AstCode.TernaryOp, null, condition.value.getOffset(), new Expression[] { condition.value, falseExpression.value, trueExpression.value });
                        }
                        else {
                            newExpression = new Expression(AstCode.TernaryOp, null, condition.value.getOffset(), new Expression[] { condition.value, trueExpression.value, falseExpression.value });
                        }
                    }
                    final List<Node> headBody = head.getBody();
                    AstOptimizer.removeTail(headBody, AstCode.IfTrue, AstCode.Goto);
                    headBody.add(new Expression(opCode, trueVariable.value, newExpression.getOffset(), new Expression[] { newExpression }));
                    if (isStore) {
                        headBody.add(new Expression(AstCode.Goto, trueFall.value, trueFall.value.getOffset(), new Expression[0]));
                    }
                    AstOptimizer.removeOrThrow(body, this.labelToBasicBlock.get(trueLabel.value));
                    AstOptimizer.removeOrThrow(body, this.labelToBasicBlock.get(falseLabel.value));
                    return true;
                }
                final StrongBox<Label> innerTrue = new StrongBox<Label>();
                final StrongBox<Label> innerFalse = new StrongBox<Label>();
                final StrongBox<Label> trueBreak = new StrongBox<Label>();
                final StrongBox<Label> falseBreak = new StrongBox<Label>();
                final StrongBox<Label> intermediateJump = new StrongBox<Label>();
                if (PatternMatching.matchSingleAndBreak(this.labelToBasicBlock.get(trueLabel.value), AstCode.IfTrue, innerTrue, trueExpression, trueFall) && PatternMatching.matchSingleAndBreak(this.labelToBasicBlock.get(falseLabel.value), AstCode.IfTrue, unused, falseExpression, falseFall) && unused.value == innerTrue.value && PatternMatching.matchLast(this.labelToBasicBlock.get(falseFall.value), AstCode.Goto, innerFalse)) {
                    final StrongBox<Expression> innerTrueExpression = new StrongBox<Expression>();
                    final StrongBox<Expression> innerFalseExpression = new StrongBox<Expression>();
                    if (this.labelGlobalRefCount.get(innerTrue.value).getValue() == 2 && this.labelGlobalRefCount.get(innerFalse.value).getValue() == 2 && PatternMatching.matchSingleAndBreak(this.labelToBasicBlock.get(innerTrue.value), AstCode.Store, trueVariable, innerTrueExpression, trueBreak) && PatternMatching.matchSingleAndBreak(this.labelToBasicBlock.get(innerFalse.value), AstCode.Store, falseVariable, innerFalseExpression, falseBreak) && trueVariable.value == falseVariable.value && trueFall.value == innerFalse.value && trueBreak.value == falseBreak.value) {
                        final boolean negateInner = AstOptimizer.simplifyLogicalNotArgument(trueExpression.value);
                        if (negateInner && !AstOptimizer.simplifyLogicalNotArgument(falseExpression.value)) {
                            final Expression newFalseExpression = new Expression(AstCode.LogicalNot, null, falseExpression.value.getOffset(), new Expression[] { falseExpression.value });
                            newFalseExpression.getRanges().addAll(falseExpression.value.getRanges());
                            falseExpression.set(newFalseExpression);
                        }
                        Expression newCondition;
                        if (AstOptimizer.simplifyLogicalNotArgument(condition.value)) {
                            newCondition = new Expression(AstCode.TernaryOp, null, condition.value.getOffset(), new Expression[] { condition.value, falseExpression.value, trueExpression.value });
                        }
                        else {
                            newCondition = new Expression(AstCode.TernaryOp, null, condition.value.getOffset(), new Expression[] { condition.value, trueExpression.value, falseExpression.value });
                        }
                        Expression newExpression2;
                        if (negateInner) {
                            newExpression2 = new Expression(AstCode.TernaryOp, null, newCondition.getOffset(), new Expression[] { newCondition, innerFalseExpression.value, innerTrueExpression.value });
                        }
                        else {
                            newExpression2 = new Expression(AstCode.TernaryOp, null, newCondition.getOffset(), new Expression[] { newCondition, innerTrueExpression.value, innerFalseExpression.value });
                        }
                        final List<Node> headBody2 = head.getBody();
                        AstOptimizer.removeTail(headBody2, AstCode.IfTrue, AstCode.Goto);
                        headBody2.add(new Expression(AstCode.Store, trueVariable.value, newExpression2.getOffset(), new Expression[] { newExpression2 }));
                        headBody2.add(new Expression(AstCode.Goto, trueBreak.value, trueBreak.value.getOffset(), new Expression[0]));
                        AstOptimizer.removeOrThrow(body, this.labelToBasicBlock.get(trueLabel.value));
                        AstOptimizer.removeOrThrow(body, this.labelToBasicBlock.get(falseLabel.value));
                        AstOptimizer.removeOrThrow(body, this.labelToBasicBlock.get(falseFall.value));
                        AstOptimizer.removeOrThrow(body, this.labelToBasicBlock.get(innerTrue.value));
                        AstOptimizer.removeOrThrow(body, this.labelToBasicBlock.get(innerFalse.value));
                        return true;
                    }
                    if (PatternMatching.matchSingleAndBreak(this.labelToBasicBlock.get(innerTrue.value), AstCode.Store, trueVariable, innerTrueExpression, trueBreak) && (PatternMatching.matchSingleAndBreak(this.labelToBasicBlock.get(falseFall.value), AstCode.Store, falseVariable, innerFalseExpression, falseBreak) || (PatternMatching.matchSimpleBreak(this.labelToBasicBlock.get(falseFall.value), intermediateJump) && PatternMatching.matchSingleAndBreak(this.labelToBasicBlock.get(intermediateJump.value), AstCode.Store, falseVariable, innerFalseExpression, falseBreak))) && trueVariable.value == falseVariable.value && trueBreak.value == falseBreak.value) {
                        final List<Expression> arguments = condition.value.getArguments();
                        final Expression oldCondition = condition.value.clone();
                        condition.value.setCode(AstCode.TernaryOp);
                        arguments.clear();
                        Collections.addAll(arguments, new Expression[] { AstOptimizer.simplifyLogicalNot(oldCondition), AstOptimizer.simplifyLogicalNot(trueExpression.value), AstOptimizer.simplifyLogicalNot(falseExpression.value) });
                        final List<Node> headBody3 = head.getBody();
                        headBody3.get(headBody3.size() - 2).setOperand(innerTrue.value);
                        if (PatternMatching.matchSimpleBreak(this.labelToBasicBlock.get(falseFall.value), intermediateJump)) {
                            if (this.labelGlobalRefCount.get(falseFall.value).getValue() == 1) {
                                AstOptimizer.removeOrThrow(body, this.labelToBasicBlock.get(falseFall.value));
                            }
                            headBody3.get(headBody3.size() - 1).setOperand(intermediateJump.value);
                        }
                        else {
                            headBody3.get(headBody3.size() - 1).setOperand(falseFall.value);
                        }
                        if (this.labelGlobalRefCount.get(trueFall.value).getValue() == 1) {
                            AstOptimizer.removeOrThrow(body, this.labelToBasicBlock.get(trueFall.value));
                        }
                        AstOptimizer.removeOrThrow(body, this.labelToBasicBlock.get(trueLabel.value));
                        AstOptimizer.removeOrThrow(body, this.labelToBasicBlock.get(falseLabel.value));
                        return true;
                    }
                }
            }
            return false;
        }
    }
    
    private static final class SimplifyTernaryOperatorRoundTwoOptimization extends AbstractExpressionOptimization
    {
        protected SimplifyTernaryOperatorRoundTwoOptimization(final DecompilerContext context, final Block method) {
            super(context, method);
        }
        
        @Override
        public boolean run(final List<Node> body, final Expression head, final int position) {
            final BooleanBox modified = new BooleanBox();
            final Expression simplified = simplify(head, modified);
            if (simplified != head) {
                body.set(position, simplified);
            }
            return modified.get();
        }
        
        private static Expression simplify(final Expression head, final BooleanBox modified) {
            if (PatternMatching.match(head, AstCode.TernaryOp)) {
                return simplifyTernaryDirect(head);
            }
            final List<Expression> arguments = head.getArguments();
            for (int i = 0; i < arguments.size(); ++i) {
                final Expression argument = arguments.get(i);
                final Expression simplified = simplify(argument, modified);
                if (simplified != argument) {
                    arguments.set(i, simplified);
                    modified.set(true);
                }
            }
            final AstCode opType = head.getCode();
            if (opType != AstCode.CmpEq && opType != AstCode.CmpNe) {
                return head;
            }
            final Boolean right = PatternMatching.matchBooleanConstant(arguments.get(1));
            if (right == null) {
                return head;
            }
            final Expression ternary = arguments.get(0);
            if (ternary.getCode() != AstCode.TernaryOp) {
                return head;
            }
            final Boolean ifTrue = PatternMatching.matchBooleanConstant(ternary.getArguments().get(1));
            final Boolean ifFalse = PatternMatching.matchBooleanConstant(ternary.getArguments().get(2));
            if (ifTrue == null || ifFalse == null || ifTrue.equals(ifFalse)) {
                return head;
            }
            final boolean invert = !ifTrue.equals(right) ^ opType == AstCode.CmpNe;
            final Expression condition = ternary.getArguments().get(0);
            condition.getRanges().addAll(ternary.getRanges());
            modified.set(true);
            return invert ? new Expression(AstCode.LogicalNot, null, condition.getOffset(), new Expression[] { condition }) : condition;
        }
        
        private static Expression simplifyTernaryDirect(final Expression head) {
            final List<Expression> a = new ArrayList<Expression>();
            if (!PatternMatching.matchGetArguments(head, AstCode.TernaryOp, a)) {
                return head;
            }
            final StrongBox<Variable> v;
            final StrongBox<Expression> left;
            final StrongBox<Expression> right;
            if (PatternMatching.matchGetArgument(a.get(1), AstCode.Store, v = new StrongBox<Variable>(), left = new StrongBox<Expression>()) && PatternMatching.matchStore(a.get(2), v.get(), right = new StrongBox<Expression>())) {
                final Expression condition = a.get(0);
                final Expression leftValue = left.value;
                final Expression rightValue = right.value;
                final Expression newTernary = new Expression(AstCode.TernaryOp, null, condition.getOffset(), new Expression[] { condition, leftValue, rightValue });
                head.setCode(AstCode.Store);
                head.setOperand(v.get());
                head.getArguments().clear();
                head.getArguments().add(newTernary);
                newTernary.getRanges().addAll(head.getRanges());
                return head;
            }
            final Boolean ifTrue = PatternMatching.matchBooleanConstant(head.getArguments().get(1));
            final Boolean ifFalse = PatternMatching.matchBooleanConstant(head.getArguments().get(2));
            if (ifTrue == null || ifFalse == null || ifTrue.equals(ifFalse)) {
                return head;
            }
            final boolean invert = Boolean.FALSE.equals(ifTrue);
            final Expression condition2 = head.getArguments().get(0);
            condition2.getRanges().addAll(head.getRanges());
            return invert ? new Expression(AstCode.LogicalNot, null, condition2.getOffset(), new Expression[] { condition2 }) : condition2;
        }
    }
    
    private static final class SimplifyLogicalNotOptimization extends AbstractExpressionOptimization
    {
        protected SimplifyLogicalNotOptimization(final DecompilerContext context, final Block method) {
            super(context, method);
        }
        
        @Override
        public final boolean run(final List<Node> body, final Expression head, final int position) {
            final BooleanBox modified = new BooleanBox();
            final Expression simplified = AstOptimizer.simplifyLogicalNot(head, modified);
            assert simplified == null;
            return modified.get();
        }
    }
    
    private static final class TransformObjectInitializersOptimization extends AbstractExpressionOptimization
    {
        protected TransformObjectInitializersOptimization(final DecompilerContext context, final Block method) {
            super(context, method);
        }
        
        @Override
        public boolean run(final List<Node> body, final Expression head, final int position) {
            if (position >= body.size() - 1) {
                return false;
            }
            final StrongBox<Variable> v = new StrongBox<Variable>();
            final StrongBox<Expression> newObject = new StrongBox<Expression>();
            final StrongBox<TypeReference> objectType = new StrongBox<TypeReference>();
            final StrongBox<MethodReference> constructor = new StrongBox<MethodReference>();
            final List<Expression> arguments = new ArrayList<Expression>();
            if (position < body.size() - 1 && PatternMatching.matchGetArgument(head, AstCode.Store, v, newObject) && PatternMatching.matchGetOperand(newObject.get(), AstCode.__New, objectType)) {
                final Node next = body.get(position + 1);
                if (PatternMatching.matchGetArguments(next, AstCode.InvokeSpecial, constructor, arguments) && !arguments.isEmpty() && PatternMatching.matchLoad(arguments.get(0), v.get())) {
                    final Expression initExpression = new Expression(AstCode.InitObject, constructor.get(), ((Expression)next).getOffset(), new Expression[0]);
                    arguments.remove(0);
                    initExpression.getArguments().addAll(arguments);
                    initExpression.getRanges().addAll(((Expression)next).getRanges());
                    head.getArguments().set(0, initExpression);
                    body.remove(position + 1);
                    return true;
                }
            }
            if (PatternMatching.matchGetArguments(head, AstCode.InvokeSpecial, constructor, arguments) && constructor.get().isConstructor() && !arguments.isEmpty() && PatternMatching.matchGetArgument(arguments.get(0), AstCode.Store, v, newObject) && PatternMatching.matchGetOperand(newObject.get(), AstCode.__New, objectType)) {
                final Expression initExpression2 = new Expression(AstCode.InitObject, constructor.get(), newObject.get().getOffset(), new Expression[0]);
                arguments.remove(0);
                initExpression2.getArguments().addAll(arguments);
                initExpression2.getRanges().addAll(head.getRanges());
                body.set(position, new Expression(AstCode.Store, v.get(), initExpression2.getOffset(), new Expression[] { initExpression2 }));
                return true;
            }
            return false;
        }
    }
    
    private static final class TransformArrayInitializersOptimization extends AbstractExpressionOptimization
    {
        protected TransformArrayInitializersOptimization(final DecompilerContext context, final Block method) {
            super(context, method);
        }
        
        @Override
        public boolean run(final List<Node> body, final Expression head, final int position) {
            final StrongBox<Variable> v = new StrongBox<Variable>();
            final StrongBox<Expression> newArray = new StrongBox<Expression>();
            if (PatternMatching.matchGetArgument(head, AstCode.Store, v, newArray) && PatternMatching.match(newArray.get(), AstCode.InitArray)) {
                return this.tryRefineArrayInitialization(body, head, position);
            }
            final StrongBox<Variable> v2 = new StrongBox<Variable>();
            final StrongBox<TypeReference> elementType = new StrongBox<TypeReference>();
            final StrongBox<Expression> lengthExpression = new StrongBox<Expression>();
            final StrongBox<Number> arrayLength = new StrongBox<Number>();
            if (PatternMatching.matchGetArgument(head, AstCode.Store, v, newArray) && PatternMatching.matchGetArgument(newArray.get(), AstCode.NewArray, elementType, lengthExpression) && PatternMatching.matchGetOperand(lengthExpression.get(), AstCode.LdC, Number.class, arrayLength) && arrayLength.get().intValue() > 0) {
                final int actualArrayLength = arrayLength.get().intValue();
                final StrongBox<Number> arrayPosition = new StrongBox<Number>();
                final List<Expression> initializers = new ArrayList<Expression>();
                int instructionsToRemove = 0;
                for (int j = position + 1; j < body.size(); ++j) {
                    final Node node = body.get(j);
                    if (node instanceof Expression) {
                        final Expression next = (Expression)node;
                        if (next.getCode() != AstCode.StoreElement || !PatternMatching.matchGetOperand(next.getArguments().get(0), AstCode.Load, v2) || v2.get() != v.get() || !PatternMatching.matchGetOperand(next.getArguments().get(1), AstCode.LdC, Number.class, arrayPosition) || arrayPosition.get().intValue() < initializers.size() || next.getArguments().get(2).containsReferenceTo(v2.get())) {
                            break;
                        }
                        while (initializers.size() < arrayPosition.get().intValue()) {
                            initializers.add(new Expression(AstCode.DefaultValue, elementType.get(), next.getOffset(), new Expression[0]));
                        }
                        initializers.add(next.getArguments().get(2));
                        ++instructionsToRemove;
                    }
                }
                if (initializers.size() < actualArrayLength && initializers.size() >= actualArrayLength / 2) {
                    while (initializers.size() < actualArrayLength) {
                        initializers.add(new Expression(AstCode.DefaultValue, elementType.get(), head.getOffset(), new Expression[0]));
                    }
                }
                if (initializers.size() == actualArrayLength) {
                    final TypeReference arrayType = elementType.get().makeArrayType();
                    head.getArguments().set(0, new Expression(AstCode.InitArray, arrayType, head.getOffset(), initializers));
                    for (int i = 0; i < instructionsToRemove; ++i) {
                        body.remove(position + 1);
                    }
                    new Inlining(this.context, this.method).inlineIfPossible(body, new MutableInteger(position));
                    return true;
                }
            }
            return false;
        }
        
        private boolean tryRefineArrayInitialization(final List<Node> body, final Expression head, final int position) {
            final StrongBox<Variable> v = new StrongBox<Variable>();
            final List<Expression> a = new ArrayList<Expression>();
            final StrongBox<TypeReference> arrayType = new StrongBox<TypeReference>();
            if (PatternMatching.matchGetArguments(head, AstCode.Store, v, a) && PatternMatching.matchGetArguments(a.get(0), AstCode.InitArray, arrayType, a)) {
                final Expression initArray = head.getArguments().get(0);
                final List<Expression> initializers = initArray.getArguments();
                final int actualArrayLength = initializers.size();
                final StrongBox<Integer> arrayPosition = new StrongBox<Integer>();
                for (int j = position + 1; j < body.size(); ++j) {
                    final Node node = body.get(j);
                    if (!PatternMatching.matchGetArguments(node, AstCode.StoreElement, a) || !PatternMatching.matchLoad(a.get(0), v.get()) || a.get(2).containsReferenceTo(v.get()) || !PatternMatching.matchGetOperand(a.get(1), AstCode.LdC, Integer.class, arrayPosition) || arrayPosition.get() < 0 || arrayPosition.get() >= actualArrayLength || !PatternMatching.match(initializers.get(arrayPosition.get()), AstCode.DefaultValue)) {
                        break;
                    }
                    initializers.set(arrayPosition.get(), a.get(2));
                    body.remove(j--);
                }
            }
            return false;
        }
    }
    
    private static final class MakeAssignmentExpressionsOptimization extends AbstractExpressionOptimization
    {
        private static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$decompiler$ast$AstCode;
        
        protected MakeAssignmentExpressionsOptimization(final DecompilerContext context, final Block method) {
            super(context, method);
        }
        
        @Override
        public boolean run(final List<Node> body, final Expression head, final int position) {
            final StrongBox<Variable> ev = new StrongBox<Variable>();
            final StrongBox<Expression> initializer = new StrongBox<Expression>();
            final Node next = CollectionUtilities.getOrDefault(body, position + 1);
            final StrongBox<Variable> v = new StrongBox<Variable>();
            final StrongBox<Expression> storeArgument = new StrongBox<Expression>();
            if (!PatternMatching.matchGetArgument(head, AstCode.Store, ev, initializer) || PatternMatching.match(initializer.value, AstCode.__New)) {
                final StrongBox<Expression> equivalentLoad = new StrongBox<Expression>();
                Label_0849: {
                    if (PatternMatching.matchAssignment(head, initializer, equivalentLoad) && next instanceof Expression) {
                        if (equivalentLoad.get().getCode() == AstCode.GetField) {
                            final FieldReference field = (FieldReference)equivalentLoad.get().getOperand();
                            final FieldDefinition resolvedField = (field != null) ? field.resolve() : null;
                            if (resolvedField != null && resolvedField.isSynthetic()) {
                                return false;
                            }
                        }
                        final boolean isLoad = PatternMatching.matchLoad(initializer.value, v);
                        final ArrayDeque<Expression> agenda = new ArrayDeque<Expression>();
                        agenda.push((Expression)next);
                        while (!agenda.isEmpty()) {
                            final Expression e = agenda.removeFirst();
                            if (e.getCode().isShortCircuiting() || e.getCode().isStore()) {
                                break;
                            }
                            if (e.getCode().isFieldWrite()) {
                                break;
                            }
                            final List<Expression> arguments = e.getArguments();
                            for (int i = 0; i < arguments.size(); ++i) {
                                final Expression a = arguments.get(i);
                                if (a.isEquivalentTo(equivalentLoad.value) || (isLoad && PatternMatching.matchLoad(a, v.get())) || (Inlining.hasNoSideEffect(initializer.get()) && a.isEquivalentTo(initializer.get()) && initializer.get().getInferredType() != null && MetadataHelper.isSameType(initializer.get().getInferredType(), a.getInferredType(), true))) {
                                    arguments.set(i, head);
                                    body.remove(position);
                                    return true;
                                }
                                if (!Inlining.isSafeForInlineOver(a, head)) {
                                    break Label_0849;
                                }
                                agenda.push(a);
                            }
                        }
                    }
                }
                return false;
            }
            if (PatternMatching.matchGetArgument(next, AstCode.Store, v, storeArgument) && PatternMatching.matchLoad(storeArgument.get(), ev.get())) {
                final Expression nextExpression = (Expression)next;
                final Node store2 = CollectionUtilities.getOrDefault(body, position + 2);
                if (this.canConvertStoreToAssignment(store2, ev.get())) {
                    final Inlining inlining = new Inlining(this.context, this.method);
                    final MutableInteger loadCounts = inlining.loadCounts.get(ev.get());
                    final MutableInteger storeCounts = inlining.storeCounts.get(ev.get());
                    if (loadCounts != null && loadCounts.getValue() == 2 && storeCounts != null && storeCounts.getValue() == 1) {
                        final Expression storeExpression = (Expression)store2;
                        body.remove(position + 2);
                        body.remove(position);
                        nextExpression.getArguments().set(0, storeExpression);
                        storeExpression.getArguments().set(storeExpression.getArguments().size() - 1, initializer.get());
                        inlining.inlineIfPossible(body, new MutableInteger(position));
                        return true;
                    }
                }
                body.remove(position + 1);
                nextExpression.getArguments().set(0, initializer.get());
                body.get(position).getArguments().set(0, nextExpression);
                return true;
            }
            if (PatternMatching.match(next, AstCode.PutStatic)) {
                final Expression nextExpression = (Expression)next;
                if (PatternMatching.matchLoad(nextExpression.getArguments().get(0), ev.get())) {
                    body.remove(position + 1);
                    nextExpression.getArguments().set(0, initializer.get());
                    body.get(position).getArguments().set(0, nextExpression);
                    return true;
                }
            }
            return false;
        }
        
        private boolean canConvertStoreToAssignment(final Node store, final Variable variable) {
            if (store instanceof Expression) {
                final Expression storeExpression = (Expression)store;
                switch ($SWITCH_TABLE$com$strobel$decompiler$ast$AstCode()[storeExpression.getCode().ordinal()]) {
                    case 180:
                    case 182:
                    case 218:
                    case 220: {
                        return PatternMatching.matchLoad(CollectionUtilities.lastOrDefault(storeExpression.getArguments()), variable);
                    }
                }
            }
            return false;
        }
        
        static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$decompiler$ast$AstCode() {
            final int[] loc_0 = MakeAssignmentExpressionsOptimization.$SWITCH_TABLE$com$strobel$decompiler$ast$AstCode;
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
            return MakeAssignmentExpressionsOptimization.$SWITCH_TABLE$com$strobel$decompiler$ast$AstCode = loc_1;
        }
    }
    
    private static final class IntroducePostIncrementOptimization extends AbstractExpressionOptimization
    {
        private static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$decompiler$ast$AstCode;
        
        protected IntroducePostIncrementOptimization(final DecompilerContext context, final Block method) {
            super(context, method);
        }
        
        @Override
        public boolean run(final List<Node> body, final Expression head, final int position) {
            boolean modified = this.introducePostIncrementForVariables(body, head, position);
            assert body.get(position) == head;
            if (position > 0) {
                final Expression newExpression = this.introducePostIncrementForInstanceFields(head, body.get(position - 1));
                if (newExpression != null) {
                    modified = true;
                    body.remove(position);
                    new Inlining(this.context, this.method).inlineIfPossible(body, new MutableInteger(position - 1));
                }
            }
            return modified;
        }
        
        private boolean introducePostIncrementForVariables(final List<Node> body, final Expression e, final int position) {
            final StrongBox<Variable> variable = new StrongBox<Variable>();
            final StrongBox<Expression> initializer = new StrongBox<Expression>();
            if (!PatternMatching.matchGetArgument(e, AstCode.Store, variable, initializer) || !variable.get().isGenerated()) {
                return false;
            }
            final Node next = CollectionUtilities.getOrDefault(body, position + 1);
            if (!(next instanceof Expression)) {
                return false;
            }
            final Expression nextExpression = (Expression)next;
            final AstCode loadCode = initializer.get().getCode();
            final AstCode storeCode = nextExpression.getCode();
            boolean recombineVariables = false;
            switch ($SWITCH_TABLE$com$strobel$decompiler$ast$AstCode()[loadCode.ordinal()]) {
                case 217: {
                    if (storeCode != AstCode.Inc && storeCode != AstCode.Store) {
                        return false;
                    }
                    final Variable loadVariable = (Variable)initializer.get().getOperand();
                    final Variable storeVariable = (Variable)nextExpression.getOperand();
                    if (loadVariable == storeVariable) {
                        break;
                    }
                    if (loadVariable.getOriginalVariable() != null && loadVariable.getOriginalVariable() == storeVariable.getOriginalVariable()) {
                        recombineVariables = true;
                        break;
                    }
                    return false;
                }
                case 179: {
                    if (storeCode != AstCode.PutStatic) {
                        return false;
                    }
                    final FieldReference initializerOperand = (FieldReference)initializer.get().getOperand();
                    final FieldReference nextOperand = (FieldReference)nextExpression.getOperand();
                    if (initializerOperand == null || nextOperand == null || !StringUtilities.equals(initializerOperand.getFullName(), nextOperand.getFullName())) {
                        return false;
                    }
                    break;
                }
                default: {
                    return false;
                }
            }
            final Expression add = (storeCode == AstCode.Inc) ? nextExpression : nextExpression.getArguments().get(0);
            final StrongBox<Number> incrementAmount = new StrongBox<Number>();
            final AstCode incrementCode = this.getIncrementCode(add, incrementAmount);
            if (incrementCode == AstCode.Nop || (!PatternMatching.match(add, AstCode.Inc) && !PatternMatching.match(add.getArguments().get(0), AstCode.Load))) {
                return false;
            }
            if (recombineVariables) {
                AstOptimizer.replaceVariables(this.method, new Function<Variable, Variable>() {
                    @Override
                    public Variable apply(final Variable old) {
                        return (Variable)((old == nextExpression.getOperand()) ? initializer.get().getOperand() : old);
                    }
                });
            }
            e.getArguments().set(0, new Expression(incrementCode, incrementAmount.get(), initializer.get().getOffset(), new Expression[] { initializer.get() }));
            body.remove(position + 1);
            return true;
        }
        
        private Expression introducePostIncrementForInstanceFields(final Expression e, final Node previous) {
            if (!(previous instanceof Expression)) {
                return null;
            }
            final Expression p = (Expression)previous;
            final StrongBox<Variable> t = new StrongBox<Variable>();
            final StrongBox<Expression> initialValue = new StrongBox<Expression>();
            if (!PatternMatching.matchGetArgument(p, AstCode.Store, t, initialValue) || (initialValue.get().getCode() != AstCode.GetField && initialValue.get().getCode() != AstCode.LoadElement)) {
                return null;
            }
            final AstCode code = e.getCode();
            final Variable tempVariable = t.get();
            if (code != AstCode.PutField && code != AstCode.StoreElement) {
                return null;
            }
            final List<Expression> arguments = e.getArguments();
            for (int i = 0, n = arguments.size() - 1; i < n; ++i) {
                if (arguments.get(i).getCode() != AstCode.Load) {
                    return null;
                }
            }
            final StrongBox<Number> incrementAmount = new StrongBox<Number>();
            final Expression add = arguments.get(arguments.size() - 1);
            final AstCode incrementCode = this.getIncrementCode(add, incrementAmount);
            if (incrementCode == AstCode.Nop) {
                return null;
            }
            final List<Expression> addArguments = add.getArguments();
            if (!PatternMatching.matchGetOperand(addArguments.get(0), AstCode.Load, t) || t.get() != tempVariable) {
                return null;
            }
            if (e.getCode() == AstCode.PutField) {
                if (initialValue.get().getCode() != AstCode.GetField) {
                    return null;
                }
                final FieldReference getField = (FieldReference)initialValue.get().getOperand();
                final FieldReference setField = (FieldReference)e.getOperand();
                if (!StringUtilities.equals(getField.getFullName(), setField.getFullName())) {
                    return null;
                }
            }
            else if (initialValue.get().getCode() != AstCode.LoadElement) {
                return null;
            }
            final List<Expression> initialValueArguments = initialValue.get().getArguments();
            assert arguments.size() - 1 == initialValueArguments.size();
            for (int j = 0, n2 = initialValueArguments.size(); j < n2; ++j) {
                if (!PatternMatching.matchLoad(initialValueArguments.get(j), (Variable)arguments.get(j).getOperand())) {
                    return null;
                }
            }
            p.getArguments().set(0, new Expression(AstCode.PostIncrement, incrementAmount.get(), initialValue.get().getOffset(), new Expression[] { initialValue.get() }));
            return p;
        }
        
        private AstCode getIncrementCode(final Expression add, final StrongBox<Number> incrementAmount) {
            AstCode incrementCode = null;
            Expression amountArgument = null;
            boolean decrement = false;
            switch ($SWITCH_TABLE$com$strobel$decompiler$ast$AstCode()[add.getCode().ordinal()]) {
                case 221: {
                    incrementCode = AstCode.PostIncrement;
                    amountArgument = add.getArguments().get(1);
                    decrement = false;
                    break;
                }
                case 222: {
                    incrementCode = AstCode.PostIncrement;
                    amountArgument = add.getArguments().get(1);
                    decrement = true;
                    break;
                }
                case 234: {
                    incrementCode = AstCode.PostIncrement;
                    amountArgument = add.getArguments().get(0);
                    decrement = false;
                    break;
                }
                default: {
                    return AstCode.Nop;
                }
            }
            if (PatternMatching.matchGetOperand(amountArgument, AstCode.LdC, incrementAmount) && !(incrementAmount.get() instanceof Float) && !(incrementAmount.get() instanceof Double) && (incrementAmount.get().longValue() == 1L || incrementAmount.get().longValue() == -1L)) {
                incrementAmount.set(decrement ? (-incrementAmount.get().intValue()) : incrementAmount.get().intValue());
                return incrementCode;
            }
            return AstCode.Nop;
        }
        
        static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$decompiler$ast$AstCode() {
            final int[] loc_0 = IntroducePostIncrementOptimization.$SWITCH_TABLE$com$strobel$decompiler$ast$AstCode;
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
            return IntroducePostIncrementOptimization.$SWITCH_TABLE$com$strobel$decompiler$ast$AstCode = loc_1;
        }
    }
    
    private static final class InlineLambdasOptimization extends AbstractExpressionOptimization
    {
        private final MutableInteger _lambdaCount;
        
        protected InlineLambdasOptimization(final DecompilerContext context, final Block method) {
            super(context, method);
            this._lambdaCount = new MutableInteger();
        }
        
        @Override
        public boolean run(final List<Node> body, final Expression head, final int position) {
            final StrongBox<DynamicCallSite> c = new StrongBox<DynamicCallSite>();
            final List<Expression> a = new ArrayList<Expression>();
            boolean modified = false;
            for (final Expression e : head.getChildrenAndSelfRecursive(Expression.class)) {
                if (PatternMatching.matchGetArguments(e, AstCode.InvokeDynamic, c, a)) {
                    final Lambda lambda = this.tryInlineLambda(e, c.value);
                    if (lambda == null) {
                        continue;
                    }
                    modified = true;
                }
            }
            return modified;
        }
        
        private Lambda tryInlineLambda(final Expression site, final DynamicCallSite callSite) {
            final MethodReference bootstrapMethod = callSite.getBootstrapMethod();
            if (!"java/lang/invoke/LambdaMetafactory".equals(bootstrapMethod.getDeclaringType().getInternalName()) || (!StringUtilities.equals("metafactory", bootstrapMethod.getName(), StringComparison.OrdinalIgnoreCase) && !StringUtilities.equals("altMetafactory", bootstrapMethod.getName(), StringComparison.OrdinalIgnoreCase)) || callSite.getBootstrapArguments().size() < 3 || !(callSite.getBootstrapArguments().get(1) instanceof MethodHandle)) {
                return null;
            }
            final MethodHandle targetMethodHandle = callSite.getBootstrapArguments().get(1);
            final MethodReference targetMethod = targetMethodHandle.getMethod();
            final MethodDefinition resolvedMethod = targetMethod.resolve();
            if (resolvedMethod == null || resolvedMethod.getBody() == null || !resolvedMethod.isSynthetic() || !MetadataHelper.isEnclosedBy(resolvedMethod.getDeclaringType(), this.context.getCurrentType()) || (StringUtilities.equals(resolvedMethod.getFullName(), this.context.getCurrentMethod().getFullName()) && StringUtilities.equals(resolvedMethod.getSignature(), this.context.getCurrentMethod().getSignature()))) {
                return null;
            }
            final TypeReference functionType = callSite.getMethodType().getReturnType();
            final List<MethodReference> methods = MetadataHelper.findMethods(functionType, MetadataFilters.matchName(callSite.getMethodName()));
            MethodReference functionMethod = null;
            for (final MethodReference m : methods) {
                final MethodDefinition r = m.resolve();
                if (r != null && r.isAbstract() && !r.isStatic() && !r.isDefault()) {
                    functionMethod = r;
                    break;
                }
            }
            if (functionMethod == null) {
                return null;
            }
            final DecompilerContext innerContext = new DecompilerContext(this.context.getSettings());
            innerContext.setCurrentType(resolvedMethod.getDeclaringType());
            innerContext.setCurrentMethod(resolvedMethod);
            final MethodBody methodBody = resolvedMethod.getBody();
            final List<ParameterDefinition> parameters = resolvedMethod.getParameters();
            final Variable[] parameterMap = new Variable[methodBody.getMaxLocals()];
            final List<Node> nodes = new ArrayList<Node>();
            final Block body = new Block();
            final Lambda lambda = new Lambda(body, functionType);
            lambda.setMethod(functionMethod);
            lambda.setCallSite(callSite);
            final List<Variable> lambdaParameters = lambda.getParameters();
            if (resolvedMethod.hasThis()) {
                final Variable variable = new Variable();
                variable.setName("this");
                variable.setType(this.context.getCurrentMethod().getDeclaringType());
                variable.setOriginalParameter(this.context.getCurrentMethod().getBody().getThisParameter());
                lambdaParameters.add(parameterMap[0] = variable);
            }
            for (final ParameterDefinition p : parameters) {
                final Variable variable2 = new Variable();
                variable2.setName(p.getName());
                variable2.setType(p.getParameterType());
                variable2.setOriginalParameter(p);
                variable2.setLambdaParameter(true);
                lambdaParameters.add(parameterMap[p.getSlot()] = variable2);
            }
            final List<Expression> arguments = site.getArguments();
            for (int i = 0; i < arguments.size(); ++i) {
                final Variable v = lambdaParameters.get(0);
                v.setOriginalParameter(null);
                v.setGenerated(true);
                final Expression argument = arguments.get(i).clone();
                nodes.add(new Expression(AstCode.Store, v, argument.getOffset(), new Expression[] { argument }));
                lambdaParameters.remove(0);
            }
            arguments.clear();
            nodes.addAll(AstBuilder.build(methodBody, true, innerContext));
            body.getBody().addAll(nodes);
            for (final Expression e : body.getSelfAndChildrenRecursive(Expression.class)) {
                final Object operand = e.getOperand();
                if (operand instanceof Variable) {
                    final Variable oldVariable = (Variable)operand;
                    if (!oldVariable.isParameter() || oldVariable.getOriginalParameter().getMethod() != resolvedMethod) {
                        continue;
                    }
                    final Variable newVariable = parameterMap[oldVariable.getOriginalParameter().getSlot()];
                    if (newVariable == null) {
                        continue;
                    }
                    e.setOperand(newVariable);
                }
            }
            AstOptimizer.optimize(innerContext, body, AstOptimizationStep.InlineVariables2);
            final int lambdaId = this._lambdaCount.increment().getValue();
            final Set<Label> renamedLabels = new HashSet<Label>();
            for (final Node n : body.getSelfAndChildrenRecursive()) {
                if (n instanceof Label) {
                    final Label label = (Label)n;
                    if (!renamedLabels.add(label)) {
                        continue;
                    }
                    label.setName(String.valueOf(label.getName()) + "_" + lambdaId);
                }
                else {
                    if (!(n instanceof Expression)) {
                        continue;
                    }
                    final Expression e2 = (Expression)n;
                    final Object operand2 = e2.getOperand();
                    if (operand2 instanceof Label) {
                        final Label label2 = (Label)operand2;
                        if (renamedLabels.add(label2)) {
                            label2.setName(String.valueOf(label2.getName()) + "_" + lambdaId);
                        }
                    }
                    else if (operand2 instanceof Label[]) {
                        Label[] loc_5;
                        for (int loc_4 = (loc_5 = (Label[])operand2).length, loc_6 = 0; loc_6 < loc_4; ++loc_6) {
                            final Label label2 = loc_5[loc_6];
                            if (renamedLabels.add(label2)) {
                                label2.setName(String.valueOf(label2.getName()) + "_" + lambdaId);
                            }
                        }
                    }
                    if (!PatternMatching.match(e2, AstCode.Return)) {
                        continue;
                    }
                    e2.putUserData(AstKeys.PARENT_LAMBDA_BINDING, site);
                }
            }
            site.setCode(AstCode.Bind);
            site.setOperand(lambda);
            final List<Range> ranges = site.getRanges();
            for (final Expression e3 : lambda.getSelfAndChildrenRecursive(Expression.class)) {
                ranges.addAll(e3.getRanges());
            }
            return lambda;
        }
    }
    
    private static final class JoinBranchConditionsOptimization extends AbstractBranchBlockOptimization
    {
        public JoinBranchConditionsOptimization(final DecompilerContext context, final Block method) {
            super(context, method);
        }
        
        @Override
        protected boolean run(final List<Node> body, final BasicBlock branchBlock, final Expression branchCondition, final Label thenLabel, final Label elseLabel, final boolean negate) {
            if (this.labelGlobalRefCount.get(elseLabel).getValue() != 1) {
                return false;
            }
            final BasicBlock elseBlock = this.labelToBasicBlock.get(elseLabel);
            if (PatternMatching.matchSingleAndBreak(elseBlock, AstCode.IfTrue, this.label1, this.expression, this.label2)) {
                final Label elseThenLabel = this.label1.get();
                final Label elseElseLabel = this.label2.get();
                final Expression elseCondition = this.expression.get();
                return this.runCore(body, branchBlock, branchCondition, thenLabel, elseLabel, elseCondition, negate, elseThenLabel, elseElseLabel, false) || this.runCore(body, branchBlock, branchCondition, thenLabel, elseLabel, elseCondition, negate, elseElseLabel, elseThenLabel, true);
            }
            return false;
        }
        
        private boolean runCore(final List<Node> body, final BasicBlock branchBlock, final Expression branchCondition, final Label thenLabel, final Label elseLabel, final Expression elseCondition, final boolean negateFirst, final Label elseThenLabel, final Label elseElseLabel, final boolean negateSecond) {
            final BasicBlock thenBlock = this.labelToBasicBlock.get(thenLabel);
            final BasicBlock elseThenBlock = this.labelToBasicBlock.get(elseThenLabel);
            BasicBlock alsoRemove = null;
            Label alsoDecrement = null;
            if (elseThenBlock != thenBlock) {
                if (!PatternMatching.matchSimpleBreak(elseThenBlock, this.label1) || this.labelGlobalRefCount.get(this.label1.get()).getValue() > 2) {
                    return false;
                }
                final BasicBlock intermediateBlock = this.labelToBasicBlock.get(this.label1.get());
                if (intermediateBlock != thenBlock) {
                    return false;
                }
                alsoRemove = elseThenBlock;
                alsoDecrement = this.label1.get();
            }
            final BasicBlock elseBlock = this.labelToBasicBlock.get(elseLabel);
            final Expression logicExpression = new Expression(AstCode.LogicalOr, null, -34, new Expression[] { negateFirst ? (AstOptimizer.simplifyLogicalNotArgument(branchCondition) ? branchCondition : new Expression(AstCode.LogicalNot, null, branchCondition.getOffset(), new Expression[] { branchCondition })) : branchCondition, negateSecond ? (AstOptimizer.simplifyLogicalNotArgument(elseCondition) ? elseCondition : new Expression(AstCode.LogicalNot, null, elseCondition.getOffset(), new Expression[] { elseCondition })) : elseCondition });
            final List<Node> branchBody = branchBlock.getBody();
            AstOptimizer.removeTail(branchBody, AstCode.IfTrue, AstCode.Goto);
            branchBody.add(new Expression(AstCode.IfTrue, thenLabel, logicExpression.getOffset(), new Expression[] { logicExpression }));
            branchBody.add(new Expression(AstCode.Goto, elseElseLabel, -34, new Expression[0]));
            this.labelGlobalRefCount.get(elseLabel).decrement();
            this.labelGlobalRefCount.get(elseThenLabel).decrement();
            body.remove(elseBlock);
            if (alsoRemove != null) {
                body.remove(alsoRemove);
            }
            if (alsoDecrement != null) {
                this.labelGlobalRefCount.get(alsoDecrement).decrement();
            }
            return true;
        }
    }
    
    private abstract static class AbstractBasicBlockOptimization implements BasicBlockOptimization
    {
        protected static final BasicBlock EMPTY_BLOCK;
        protected final Map<Label, MutableInteger> labelGlobalRefCount;
        protected final Map<Label, BasicBlock> labelToBasicBlock;
        protected final DecompilerContext context;
        protected final IMetadataResolver resolver;
        protected final Block method;
        
        static {
            EMPTY_BLOCK = new BasicBlock();
        }
        
        protected AbstractBasicBlockOptimization(final DecompilerContext context, final Block method) {
            super();
            this.labelGlobalRefCount = new DefaultMap<Label, MutableInteger>(MutableInteger.SUPPLIER);
            this.labelToBasicBlock = new DefaultMap<Label, BasicBlock>(Suppliers.forValue(AbstractBasicBlockOptimization.EMPTY_BLOCK));
            this.context = VerifyArgument.notNull(context, "context");
            this.resolver = context.getCurrentType().getResolver();
            this.method = VerifyArgument.notNull(method, "method");
            for (final Expression e : method.getSelfAndChildrenRecursive(Expression.class)) {
                if (e.isBranch()) {
                    for (final Label target : e.getBranchTargets()) {
                        this.labelGlobalRefCount.get(target).increment();
                    }
                }
            }
            for (final BasicBlock basicBlock : method.getSelfAndChildrenRecursive(BasicBlock.class)) {
                for (final Node child : basicBlock.getChildren()) {
                    if (child instanceof Label) {
                        this.labelToBasicBlock.put((Label)child, basicBlock);
                    }
                }
            }
        }
    }
    
    private abstract static class AbstractExpressionOptimization implements ExpressionOptimization
    {
        protected final DecompilerContext context;
        protected final MetadataSystem metadataSystem;
        protected final Block method;
        
        protected AbstractExpressionOptimization(final DecompilerContext context, final Block method) {
            super();
            this.context = VerifyArgument.notNull(context, "context");
            this.metadataSystem = MetadataSystem.instance();
            this.method = VerifyArgument.notNull(method, "method");
        }
    }
    
    private abstract static class AbstractBranchBlockOptimization extends AbstractBasicBlockOptimization
    {
        protected final StrongBox<Expression> expression;
        protected final StrongBox<Label> label1;
        protected final StrongBox<Label> label2;
        
        public AbstractBranchBlockOptimization(final DecompilerContext context, final Block method) {
            super(context, method);
            this.expression = new StrongBox<Expression>();
            this.label1 = new StrongBox<Label>();
            this.label2 = new StrongBox<Label>();
        }
        
        @Override
        public final boolean run(final List<Node> body, final BasicBlock head, final int position) {
            if (PatternMatching.matchLastAndBreak(head, AstCode.IfTrue, this.label1, this.expression, this.label2)) {
                final Label thenLabel = this.label1.get();
                final Label elseLabel = this.label2.get();
                final Expression condition = this.expression.get();
                return this.run(body, head, condition, thenLabel, elseLabel, false) || this.run(body, head, condition, elseLabel, thenLabel, true);
            }
            return false;
        }
        
        protected abstract boolean run(final List<Node> param_0, final BasicBlock param_1, final Expression param_2, final Label param_3, final Label param_4, final boolean param_5);
    }
    
    private interface BasicBlockOptimization
    {
        boolean run(List<Node> param_0, BasicBlock param_1, int param_2);
    }
    
    private interface ExpressionOptimization
    {
        boolean run(List<Node> param_0, Expression param_1, int param_2);
    }
}
