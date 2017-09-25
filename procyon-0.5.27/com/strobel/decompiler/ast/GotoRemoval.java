package com.strobel.decompiler.ast;

import com.strobel.util.*;
import com.strobel.annotations.*;
import com.strobel.core.*;
import java.util.*;
import com.strobel.assembler.metadata.*;

final class GotoRemoval
{
    static final int OPTION_MERGE_ADJACENT_LABELS = 1;
    static final int OPTION_REMOVE_REDUNDANT_RETURNS = 2;
    final Map<Node, Label> labels;
    final Map<Label, Node> labelLookup;
    final Map<Node, Node> parentLookup;
    final Map<Node, Node> nextSibling;
    final int options;
    private static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$decompiler$ast$AstCode;
    
    GotoRemoval() {
        this(0);
    }
    
    GotoRemoval(final int options) {
        super();
        this.labels = new IdentityHashMap<Node, Label>();
        this.labelLookup = new IdentityHashMap<Label, Node>();
        this.parentLookup = new IdentityHashMap<Node, Node>();
        this.nextSibling = new IdentityHashMap<Node, Node>();
        this.options = options;
    }
    
    public final void removeGotos(final Block method) {
        this.traverseGraph(method);
        this.removeGotosCore(method);
    }
    
    private void removeGotosCore(final Block method) {
        this.transformLeaveStatements(method);
        boolean modified;
        do {
            modified = false;
            for (final Expression e : method.getSelfAndChildrenRecursive(Expression.class)) {
                if (e.getCode() == AstCode.Goto) {
                    modified |= this.trySimplifyGoto(e);
                }
            }
        } while (modified);
        this.removeRedundantCodeCore(method);
    }
    
    private void traverseGraph(final Block method) {
        this.labels.clear();
        this.labelLookup.clear();
        this.parentLookup.clear();
        this.nextSibling.clear();
        this.parentLookup.put(method, Node.NULL);
        for (final Node node : method.getSelfAndChildrenRecursive(Node.class)) {
            Node previousChild = null;
            for (final Node child : node.getChildren()) {
                if (this.parentLookup.containsKey(child)) {
                    throw Error.expressionLinkedFromMultipleLocations(child);
                }
                this.parentLookup.put(child, node);
                if (previousChild != null) {
                    if (previousChild instanceof Label) {
                        this.labels.put(child, (Label)previousChild);
                        this.labelLookup.put((Label)previousChild, child);
                    }
                    this.nextSibling.put(previousChild, child);
                }
                previousChild = child;
            }
            if (previousChild != null) {
                this.nextSibling.put(previousChild, Node.NULL);
            }
        }
    }
    
    private boolean trySimplifyGoto(final Expression gotoExpression) {
        assert gotoExpression.getCode() == AstCode.Goto;
        assert gotoExpression.getOperand() instanceof Label;
        final Node target = this.enter(gotoExpression, new LinkedHashSet<Node>());
        if (target == null) {
            return false;
        }
        final Set<Node> visitedNodes = new LinkedHashSet<Node>();
        visitedNodes.add(gotoExpression);
        final Node exitTo = this.exit(gotoExpression, visitedNodes);
        final boolean isRedundant = target == exitTo;
        if (isRedundant) {
            final Node parent = this.parentLookup.get(gotoExpression);
            if (!(parent instanceof Block) || ((Block)parent).getBody().size() != 1 || !(this.parentLookup.get(parent) instanceof Condition)) {
                gotoExpression.setCode(AstCode.Nop);
                gotoExpression.setOperand(null);
                if (target instanceof Expression) {
                    ((Expression)target).getRanges().addAll(gotoExpression.getRanges());
                }
                gotoExpression.getRanges().clear();
                return true;
            }
        }
        visitedNodes.clear();
        visitedNodes.add(gotoExpression);
        for (final TryCatchBlock tryCatchBlock : this.getParents(gotoExpression, TryCatchBlock.class)) {
            final Block finallyBlock = tryCatchBlock.getFinallyBlock();
            if (finallyBlock == null) {
                continue;
            }
            if (target == this.enter(finallyBlock, visitedNodes)) {
                gotoExpression.setCode(AstCode.Nop);
                gotoExpression.setOperand(null);
                gotoExpression.getRanges().clear();
                return true;
            }
        }
        visitedNodes.clear();
        visitedNodes.add(gotoExpression);
        Loop continueBlock = null;
        for (final Node parent2 : this.getParents(gotoExpression)) {
            if (parent2 instanceof Loop) {
                final Node enter = this.enter(parent2, visitedNodes);
                if (target == enter) {
                    continueBlock = (Loop)parent2;
                    break;
                }
                if (!(enter instanceof TryCatchBlock)) {
                    break;
                }
                final Node firstChild = CollectionUtilities.firstOrDefault(enter.getChildren());
                if (firstChild == null) {
                    break;
                }
                visitedNodes.clear();
                if (this.enter(firstChild, visitedNodes) == target) {
                    continueBlock = (Loop)parent2;
                    break;
                }
                break;
            }
        }
        if (continueBlock != null) {
            gotoExpression.setCode(AstCode.LoopContinue);
            gotoExpression.setOperand(null);
            return true;
        }
        if (isRedundant) {
            gotoExpression.setCode(AstCode.Nop);
            gotoExpression.setOperand(null);
            if (target instanceof Expression) {
                ((Expression)target).getRanges().addAll(gotoExpression.getRanges());
            }
            gotoExpression.getRanges().clear();
            return true;
        }
        visitedNodes.clear();
        visitedNodes.add(gotoExpression);
        int loopDepth = 0;
        int switchDepth = 0;
        Node breakBlock = null;
        for (final Node parent3 : this.getParents(gotoExpression)) {
            if (parent3 instanceof Loop) {
                ++loopDepth;
                final Node exit = this.exit(parent3, visitedNodes);
                if (target == exit) {
                    breakBlock = parent3;
                    break;
                }
                if (!(exit instanceof TryCatchBlock)) {
                    continue;
                }
                final Node firstChild2 = CollectionUtilities.firstOrDefault(exit.getChildren());
                if (firstChild2 == null) {
                    continue;
                }
                visitedNodes.clear();
                if (this.enter(firstChild2, visitedNodes) == target) {
                    breakBlock = parent3;
                    break;
                }
                continue;
            }
            else {
                if (!(parent3 instanceof Switch)) {
                    continue;
                }
                ++switchDepth;
                final Node exit = this.exit(parent3, visitedNodes);
                if (target == exit) {
                    breakBlock = parent3;
                    break;
                }
                continue;
            }
        }
        if (breakBlock != null) {
            gotoExpression.setCode(AstCode.LoopOrSwitchBreak);
            gotoExpression.setOperand((loopDepth + switchDepth > 1) ? gotoExpression.getOperand() : null);
            return true;
        }
        visitedNodes.clear();
        visitedNodes.add(gotoExpression);
        loopDepth = 0;
        for (final Node parent3 : this.getParents(gotoExpression)) {
            if (parent3 instanceof Loop) {
                ++loopDepth;
                final Node enter2 = this.enter(parent3, visitedNodes);
                if (target == enter2) {
                    continueBlock = (Loop)parent3;
                    break;
                }
                if (!(enter2 instanceof TryCatchBlock)) {
                    continue;
                }
                final Node firstChild2 = CollectionUtilities.firstOrDefault(enter2.getChildren());
                if (firstChild2 == null) {
                    continue;
                }
                visitedNodes.clear();
                if (this.enter(firstChild2, visitedNodes) == target) {
                    continueBlock = (Loop)parent3;
                    break;
                }
                continue;
            }
        }
        if (continueBlock != null) {
            gotoExpression.setCode(AstCode.LoopContinue);
            gotoExpression.setOperand((loopDepth > 1) ? gotoExpression.getOperand() : null);
            return true;
        }
        return this.tryInlineReturn(gotoExpression, target, AstCode.Return) || this.tryInlineReturn(gotoExpression, target, AstCode.AThrow);
    }
    
    private boolean tryInlineReturn(final Expression gotoExpression, final Node target, final AstCode code) {
        final List<Expression> expressions = new ArrayList<Expression>();
        if (PatternMatching.matchGetArguments(target, code, expressions) && (expressions.isEmpty() || expressions.size() == 1)) {
            gotoExpression.setCode(code);
            gotoExpression.setOperand(null);
            gotoExpression.getArguments().clear();
            if (!expressions.isEmpty()) {
                gotoExpression.getArguments().add(expressions.get(0).clone());
            }
            return true;
        }
        final StrongBox<Variable> v = new StrongBox<Variable>();
        final StrongBox<Variable> v2 = new StrongBox<Variable>();
        Node next;
        for (next = this.nextSibling.get(target); next instanceof Label; next = this.nextSibling.get(next)) {}
        if (PatternMatching.matchGetArguments(target, AstCode.Store, v, expressions) && expressions.size() == 1 && PatternMatching.matchGetArguments(next, code, expressions) && expressions.size() == 1 && PatternMatching.matchGetOperand(expressions.get(0), AstCode.Load, v2) && v2.get() == v.get()) {
            gotoExpression.setCode(code);
            gotoExpression.setOperand(null);
            gotoExpression.getArguments().clear();
            gotoExpression.getArguments().add(((Expression)target).getArguments().get(0).clone());
            return true;
        }
        return false;
    }
    
    private Iterable<Node> getParents(final Node node) {
        return this.getParents(node, Node.class);
    }
    
    private <T extends Node> Iterable<T> getParents(final Node node, final Class<T> parentType) {
        return new Iterable<T>() {
            final /* synthetic */ GotoRemoval this$0;
            
            @NotNull
            @Override
            public final Iterator<T> iterator() {
                return new Iterator<T>(node) {
                    T current = this.updateCurrent(param_1);
                    
                    private T updateCurrent(Node node) {
                        while (node != null && node != Node.NULL) {
                            node = GotoRemoval$1.access$0(Iterable.this).parentLookup.get(node);
                            if (parentType.isInstance(node)) {
                                return (T)node;
                            }
                        }
                        return null;
                    }
                    
                    @Override
                    public final boolean hasNext() {
                        return this.current != null;
                    }
                    
                    @Override
                    public final T next() {
                        final T next = this.current;
                        if (next == null) {
                            throw new NoSuchElementException();
                        }
                        this.current = this.updateCurrent(next);
                        return next;
                    }
                    
                    @Override
                    public final void remove() {
                        throw ContractUtils.unsupported();
                    }
                };
            }
            
            static /* synthetic */ GotoRemoval access$0(final GotoRemoval$1 param_0) {
                return param_0.this$0;
            }
        };
    }
    
    private Node enter(final Node node, final Set<Node> visitedNodes) {
        VerifyArgument.notNull(node, "node");
        VerifyArgument.notNull(visitedNodes, "visitedNodes");
        if (!visitedNodes.add(node)) {
            return null;
        }
        if (node instanceof Label) {
            return this.exit(node, visitedNodes);
        }
        if (node instanceof Expression) {
            final Expression e = (Expression)node;
            switch ($SWITCH_TABLE$com$strobel$decompiler$ast$AstCode()[e.getCode().ordinal()]) {
                case 168: {
                    final Label target = (Label)e.getOperand();
                    if (CollectionUtilities.firstOrDefault(this.getParents(e, TryCatchBlock.class)) == CollectionUtilities.firstOrDefault(this.getParents(target, TryCatchBlock.class))) {
                        return this.enter(target, visitedNodes);
                    }
                    final List<TryCatchBlock> sourceTryBlocks = CollectionUtilities.toList(this.getParents(e, TryCatchBlock.class));
                    final List<TryCatchBlock> targetTryBlocks = CollectionUtilities.toList(this.getParents(target, TryCatchBlock.class));
                    Collections.reverse(sourceTryBlocks);
                    Collections.reverse(targetTryBlocks);
                    int i;
                    for (i = 0; i < sourceTryBlocks.size() && i < targetTryBlocks.size() && sourceTryBlocks.get(i) == targetTryBlocks.get(i); ++i) {}
                    if (i == targetTryBlocks.size()) {
                        return this.enter(target, visitedNodes);
                    }
                    TryCatchBlock current;
                    Node n = null;
                    for (TryCatchBlock targetTryBlock = current = targetTryBlocks.get(i); current != null; current = ((n instanceof TryCatchBlock) ? ((TryCatchBlock)n) : null)) {
                        final List<Node> body = current.getTryBlock().getBody();
                        current = null;
                        final Iterator<Node> loc_0 = body.iterator();
                        while (loc_0.hasNext()) {
                            n = loc_0.next();
                            if (n instanceof Label) {
                                if (n == target) {
                                    return targetTryBlock;
                                }
                                continue;
                            }
                            else {
                                if (!PatternMatching.match(n, AstCode.Nop)) {
                                    break;
                                }
                                continue;
                            }
                        }
                    }
                    return null;
                }
                default: {
                    return e;
                }
            }
        }
        else if (node instanceof Block) {
            final Block block = (Block)node;
            if (block.getEntryGoto() != null) {
                return this.enter(block.getEntryGoto(), visitedNodes);
            }
            if (block.getBody().isEmpty()) {
                return this.exit(block, visitedNodes);
            }
            return this.enter(block.getBody().get(0), visitedNodes);
        }
        else {
            if (node instanceof Condition) {
                return ((Condition)node).getCondition();
            }
            if (node instanceof Loop) {
                final Loop loop = (Loop)node;
                if (loop.getLoopType() == LoopType.PreCondition && loop.getCondition() != null) {
                    return loop.getCondition();
                }
                return this.enter(loop.getBody(), visitedNodes);
            }
            else {
                if (node instanceof TryCatchBlock) {
                    return node;
                }
                if (node instanceof Switch) {
                    return ((Switch)node).getCondition();
                }
                throw Error.unsupportedNode(node);
            }
        }
    }
    
    private Node exit(final Node node, final Set<Node> visitedNodes) {
        VerifyArgument.notNull(node, "node");
        VerifyArgument.notNull(visitedNodes, "visitedNodes");
        final Node parent = this.parentLookup.get(node);
        if (parent == null || parent == Node.NULL) {
            return null;
        }
        if (parent instanceof Block) {
            final Node nextNode = this.nextSibling.get(node);
            if (nextNode != null && nextNode != Node.NULL) {
                return this.enter(nextNode, visitedNodes);
            }
            if (parent instanceof CaseBlock) {
                final Node nextCase = this.nextSibling.get(parent);
                if (nextCase != null && nextCase != Node.NULL) {
                    return this.enter(nextCase, visitedNodes);
                }
            }
            return this.exit(parent, visitedNodes);
        }
        else {
            if (parent instanceof Condition) {
                return this.exit(parent, visitedNodes);
            }
            if (parent instanceof TryCatchBlock) {
                return this.exit(parent, visitedNodes);
            }
            if (parent instanceof Switch) {
                return null;
            }
            if (parent instanceof Loop) {
                return this.enter(parent, visitedNodes);
            }
            throw Error.unsupportedNode(parent);
        }
    }
    
    private void transformLeaveStatements(final Block method) {
        final StrongBox<Label> target = new StrongBox<Label>();
        final Set<Node> visitedNodes = new LinkedHashSet<Node>();
        for (final Expression e : method.getSelfAndChildrenRecursive(Expression.class)) {
            if (PatternMatching.matchGetOperand(e, AstCode.Goto, target)) {
                visitedNodes.clear();
                final Node exit = this.exit(e, new HashSet<Node>());
                if (exit == null || !PatternMatching.matchLeaveHandler(exit)) {
                    continue;
                }
                final Node parent = this.parentLookup.get(e);
                final Node grandParent = (parent != null) ? this.parentLookup.get(parent) : null;
                if (!(parent instanceof Block) || (!(grandParent instanceof CatchBlock) && !(grandParent instanceof TryCatchBlock)) || e != CollectionUtilities.last(((Block)parent).getBody())) {
                    continue;
                }
                if (grandParent instanceof TryCatchBlock && parent == ((TryCatchBlock)grandParent).getFinallyBlock()) {
                    e.setCode(AstCode.EndFinally);
                }
                else {
                    e.setCode(AstCode.Leave);
                }
                e.setOperand(null);
            }
        }
    }
    
    public static void removeRedundantCode(final Block method) {
        removeRedundantCode(method, 0);
    }
    
    public static void removeRedundantCode(final Block method, final int options) {
        final GotoRemoval gotoRemoval = new GotoRemoval(options);
        gotoRemoval.traverseGraph(method);
        gotoRemoval.removeRedundantCodeCore(method);
    }
    
    private void removeRedundantCodeCore(final Block method) {
        final Set<Label> liveLabels = new LinkedHashSet<Label>();
        final StrongBox<Label> target = new StrongBox<Label>();
        final Set<Expression> returns = new LinkedHashSet<Expression>();
        final Map<Label, List<Expression>> jumps = new DefaultMap<Label, List<Expression>>(CollectionUtilities.listFactory());
        List<TryCatchBlock> tryCatchBlocks = null;
    Label_0336:
        for (final Expression e : method.getSelfAndChildrenRecursive(Expression.class)) {
            if (PatternMatching.matchEmptyReturn(e)) {
                returns.add(e);
            }
            if (e.isBranch()) {
                if (PatternMatching.matchGetOperand(e, AstCode.Goto, target)) {
                    if (tryCatchBlocks == null) {
                        tryCatchBlocks = method.getSelfAndChildrenRecursive(TryCatchBlock.class);
                    }
                    for (final TryCatchBlock tryCatchBlock : tryCatchBlocks) {
                        final Block finallyBlock = tryCatchBlock.getFinallyBlock();
                        if (finallyBlock != null) {
                            final Node firstInBody = CollectionUtilities.firstOrDefault(finallyBlock.getBody());
                            if (firstInBody == target.get()) {
                                e.setCode(AstCode.Leave);
                                e.setOperand(null);
                                continue Label_0336;
                            }
                            continue;
                        }
                        else {
                            if (tryCatchBlock.getCatchBlocks().size() != 1) {
                                continue;
                            }
                            final Node firstInBody = CollectionUtilities.firstOrDefault(CollectionUtilities.first(tryCatchBlock.getCatchBlocks()).getBody());
                            if (firstInBody == target.get()) {
                                e.setCode(AstCode.Leave);
                                e.setOperand(null);
                                continue Label_0336;
                            }
                            continue;
                        }
                    }
                }
                final List<Label> branchTargets = e.getBranchTargets();
                for (final Label label : branchTargets) {
                    jumps.get(label).add(e);
                }
                liveLabels.addAll(branchTargets);
            }
        }
        final boolean mergeAdjacentLabels = Flags.testAny(this.options, 1);
        for (final Block block : method.getSelfAndChildrenRecursive(Block.class)) {
            final List<Node> body = block.getBody();
            for (int i = 0; i < body.size(); ++i) {
                final Node n = body.get(i);
                if (PatternMatching.match(n, AstCode.Nop) || PatternMatching.match(n, AstCode.Leave) || PatternMatching.match(n, AstCode.EndFinally) || (n instanceof Label && !liveLabels.contains(n))) {
                    body.remove(i--);
                }
                if (mergeAdjacentLabels && n instanceof Label && i < body.size() - 1 && body.get(i + 1) instanceof Label) {
                    final Label newLabel = (Label)n;
                    final Label oldLabel = body.remove(i + 1);
                    final List<Expression> oldLabelJumps = jumps.get(oldLabel);
                    for (final Expression jump : oldLabelJumps) {
                        if (jump.getOperand() instanceof Label) {
                            jump.setOperand(n);
                        }
                        else {
                            final Label[] branchTargets2 = (Label[])jump.getOperand();
                            for (int j = 0; j < branchTargets2.length; ++j) {
                                if (branchTargets2[j] == oldLabel) {
                                    branchTargets2[j] = newLabel;
                                }
                            }
                        }
                    }
                }
            }
        }
        for (final Loop loop : method.getSelfAndChildrenRecursive(Loop.class)) {
            final Block body2 = loop.getBody();
            final Node lastInLoop = CollectionUtilities.lastOrDefault(body2.getBody());
            if (lastInLoop == null) {
                continue;
            }
            if (PatternMatching.match(lastInLoop, AstCode.LoopContinue)) {
                final Expression last = CollectionUtilities.last(body2.getBody());
                if (last.getOperand() != null) {
                    continue;
                }
                body2.getBody().remove(last);
            }
            else {
                if (!(lastInLoop instanceof Condition)) {
                    continue;
                }
                final Condition condition = (Condition)lastInLoop;
                final Block falseBlock = condition.getFalseBlock();
                if (!PatternMatching.matchSingle(falseBlock, AstCode.LoopContinue, target) || target.get() != null) {
                    continue;
                }
                falseBlock.getBody().clear();
            }
        }
        for (final Switch switchNode : method.getSelfAndChildrenRecursive(Switch.class)) {
            CaseBlock defaultCase = null;
            final List<CaseBlock> caseBlocks = switchNode.getCaseBlocks();
            for (final CaseBlock caseBlock : caseBlocks) {
                assert caseBlock.getEntryGoto() == null;
                if (caseBlock.getValues().isEmpty()) {
                    defaultCase = caseBlock;
                }
                final List<Node> caseBody = caseBlock.getBody();
                final int size = caseBody.size();
                if (size < 2 || !caseBody.get(size - 2).isUnconditionalControlFlow() || !PatternMatching.match(caseBody.get(size - 1), AstCode.LoopOrSwitchBreak)) {
                    continue;
                }
                caseBody.remove(size - 1);
            }
            if (defaultCase == null || (defaultCase.getBody().size() == 1 && PatternMatching.match(CollectionUtilities.firstOrDefault(defaultCase.getBody()), AstCode.LoopOrSwitchBreak))) {
                for (int k = 0; k < caseBlocks.size(); ++k) {
                    final List<Node> body3 = caseBlocks.get(k).getBody();
                    if (body3.size() == 1 && PatternMatching.matchGetOperand(CollectionUtilities.firstOrDefault(body3), AstCode.LoopOrSwitchBreak, target) && target.get() == null) {
                        caseBlocks.remove(k--);
                    }
                }
            }
        }
        final List<Node> methodBody = method.getBody();
        final Node lastStatement = CollectionUtilities.lastOrDefault(methodBody);
        if (PatternMatching.match(lastStatement, AstCode.Return) && ((Expression)lastStatement).getArguments().isEmpty()) {
            methodBody.remove(methodBody.size() - 1);
            returns.remove(lastStatement);
        }
        boolean modified = false;
        for (final Block block2 : method.getSelfAndChildrenRecursive(Block.class)) {
            final List<Node> blockBody = block2.getBody();
            for (int l = 0; l < blockBody.size() - 1; ++l) {
                final Node node = blockBody.get(l);
                if (node.isUnconditionalControlFlow() && (PatternMatching.match(blockBody.get(l + 1), AstCode.Return) || PatternMatching.match(blockBody.get(l + 1), AstCode.AThrow))) {
                    modified = true;
                    blockBody.remove(l-- + 1);
                    returns.remove(blockBody.get(l + 1));
                }
            }
        }
        if (Flags.testAny(this.options, 2)) {
            for (final Expression r : returns) {
                final Node immediateParent = this.parentLookup.get(r);
                Node current = r;
                Node parent = immediateParent;
                boolean firstBlock = true;
                boolean isRedundant = true;
                while (parent != null && parent != Node.NULL) {
                    if (parent instanceof BasicBlock || parent instanceof Block) {
                        final List<Node> body4 = (parent instanceof BasicBlock) ? ((BasicBlock)parent).getBody() : ((Block)parent).getBody();
                        if (firstBlock) {
                            final Node grandparent = this.parentLookup.get(parent);
                            if (grandparent instanceof Condition) {
                                final Condition c = (Condition)grandparent;
                                if (c.getTrueBlock().getBody().size() == 1 && r == CollectionUtilities.last(c.getTrueBlock().getBody()) && (PatternMatching.matchNullOrEmpty(c.getFalseBlock()) || PatternMatching.matchEmptyReturn(c.getFalseBlock()))) {
                                    isRedundant = false;
                                    break;
                                }
                            }
                            firstBlock = false;
                        }
                        final Node last2 = CollectionUtilities.last(body4);
                        if (last2 != current) {
                            if (PatternMatching.matchEmptyReturn(last2) && body4.size() > 1 && body4.get(body4.size() - 2) == current) {
                                break;
                            }
                            isRedundant = false;
                            break;
                        }
                    }
                    current = parent;
                    parent = this.parentLookup.get(current);
                }
                if (isRedundant) {
                    if (immediateParent instanceof Block) {
                        ((Block)immediateParent).getBody().remove(r);
                    }
                    else {
                        if (!(immediateParent instanceof BasicBlock)) {
                            continue;
                        }
                        ((BasicBlock)immediateParent).getBody().remove(r);
                    }
                }
            }
        }
        if (modified) {
            this.removeGotosCore(method);
        }
    }
    
    static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$decompiler$ast$AstCode() {
        final int[] loc_0 = GotoRemoval.$SWITCH_TABLE$com$strobel$decompiler$ast$AstCode;
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
        return GotoRemoval.$SWITCH_TABLE$com$strobel$decompiler$ast$AstCode = loc_1;
    }
}
