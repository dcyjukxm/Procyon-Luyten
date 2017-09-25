package com.strobel.decompiler.ast;

import com.strobel.decompiler.*;
import com.strobel.assembler.flowanalysis.*;
import com.strobel.annotations.*;
import java.util.*;
import com.strobel.assembler.metadata.*;
import com.strobel.core.*;

final class LoopsAndConditions
{
    private final Map<Label, ControlFlowNode> labelsToNodes;
    private final DecompilerContext context;
    private int _nextLabelIndex;
    
    LoopsAndConditions(final DecompilerContext context) {
        super();
        this.labelsToNodes = new IdentityHashMap<Label, ControlFlowNode>();
        this.context = context;
    }
    
    public final void findConditions(final Block block) {
        final List<Node> body = block.getBody();
        if (body.isEmpty() || block.getEntryGoto() == null) {
            return;
        }
        final ControlFlowGraph graph = this.buildGraph(body, (Label)block.getEntryGoto().getOperand());
        graph.computeDominance();
        graph.computeDominanceFrontier();
        final Set<ControlFlowNode> cfNodes = new LinkedHashSet<ControlFlowNode>();
        final List<ControlFlowNode> graphNodes = graph.getNodes();
        for (int i = 3; i < graphNodes.size(); ++i) {
            cfNodes.add(graphNodes.get(i));
        }
        final List<Node> newBody = this.findConditions(cfNodes, graph.getEntryPoint());
        block.getBody().clear();
        block.getBody().addAll(newBody);
    }
    
    public final void findLoops(final Block block) {
        final List<Node> body = block.getBody();
        if (body.isEmpty() || block.getEntryGoto() == null) {
            return;
        }
        final ControlFlowGraph graph = this.buildGraph(body, (Label)block.getEntryGoto().getOperand());
        graph.computeDominance();
        graph.computeDominanceFrontier();
        final Set<ControlFlowNode> cfNodes = new LinkedHashSet<ControlFlowNode>();
        final List<ControlFlowNode> graphNodes = graph.getNodes();
        for (int i = 3; i < graphNodes.size(); ++i) {
            cfNodes.add(graphNodes.get(i));
        }
        final List<Node> newBody = this.findLoops(cfNodes, graph.getEntryPoint(), false);
        block.getBody().clear();
        block.getBody().addAll(newBody);
    }
    
    private ControlFlowGraph buildGraph(final List<Node> nodes, final Label entryLabel) {
        int index = 0;
        final List<ControlFlowNode> cfNodes = new ArrayList<ControlFlowNode>();
        final ControlFlowNode entryPoint = new ControlFlowNode(index++, 0, ControlFlowNodeType.EntryPoint);
        final ControlFlowNode regularExit = new ControlFlowNode(index++, -1, ControlFlowNodeType.RegularExit);
        final ControlFlowNode exceptionalExit = new ControlFlowNode(index++, -1, ControlFlowNodeType.ExceptionalExit);
        cfNodes.add(entryPoint);
        cfNodes.add(regularExit);
        cfNodes.add(exceptionalExit);
        this.labelsToNodes.clear();
        final Map<Node, ControlFlowNode> astNodesToControlFlowNodes = new IdentityHashMap<Node, ControlFlowNode>();
        for (final Node node : nodes) {
            final ControlFlowNode cfNode = new ControlFlowNode(index++, -1, ControlFlowNodeType.Normal);
            cfNodes.add(cfNode);
            astNodesToControlFlowNodes.put(node, cfNode);
            cfNode.setUserData(node);
            for (final Label label : node.getSelfAndChildrenRecursive(Label.class)) {
                this.labelsToNodes.put(label, cfNode);
            }
        }
        final ControlFlowNode entryNode = this.labelsToNodes.get(entryLabel);
        final ControlFlowEdge entryEdge = new ControlFlowEdge(entryPoint, entryNode, JumpType.Normal);
        entryPoint.getOutgoing().add(entryEdge);
        entryNode.getIncoming().add(entryEdge);
        for (final Node node2 : nodes) {
            final ControlFlowNode source = astNodesToControlFlowNodes.get(node2);
            for (final Expression e : node2.getSelfAndChildrenRecursive(Expression.class)) {
                if (!e.isBranch()) {
                    continue;
                }
                for (final Label target : e.getBranchTargets()) {
                    final ControlFlowNode destination = this.labelsToNodes.get(target);
                    if (destination != null && (destination != source || this.canBeSelfContainedLoop((BasicBlock)node2, e, target))) {
                        final ControlFlowEdge edge = new ControlFlowEdge(source, destination, JumpType.Normal);
                        if (!source.getOutgoing().contains(edge)) {
                            source.getOutgoing().add(edge);
                        }
                        if (destination.getIncoming().contains(edge)) {
                            continue;
                        }
                        destination.getIncoming().add(edge);
                    }
                }
            }
        }
        return new ControlFlowGraph((ControlFlowNode[])cfNodes.toArray(new ControlFlowNode[cfNodes.size()]));
    }
    
    private boolean canBeSelfContainedLoop(final BasicBlock node, final Expression branch, final Label target) {
        final List<Node> nodeBody = node.getBody();
        if (target == null || nodeBody.isEmpty()) {
            return false;
        }
        if (target == nodeBody.get(0)) {
            return true;
        }
        final Node secondNode = CollectionUtilities.getOrDefault(nodeBody, 1);
        if (!(secondNode instanceof TryCatchBlock)) {
            return false;
        }
        final Node next = CollectionUtilities.getOrDefault(nodeBody, 2);
        if (next != branch) {
            return false;
        }
        final TryCatchBlock tryCatch = (TryCatchBlock)secondNode;
        final Block tryBlock = tryCatch.getTryBlock();
        final Predicate<Expression> labelMatch = new Predicate<Expression>() {
            @Override
            public boolean test(final Expression e) {
                return e != tryBlock.getEntryGoto() && e.getBranchTargets().contains(target);
            }
        };
        for (final CatchBlock catchBlock : tryCatch.getCatchBlocks()) {
            if (CollectionUtilities.any(catchBlock.getSelfAndChildrenRecursive(Expression.class), labelMatch)) {
                return true;
            }
        }
        return tryCatch.getFinallyBlock() == null || !CollectionUtilities.any(tryCatch.getFinallyBlock().getSelfAndChildrenRecursive(Expression.class), labelMatch) || true;
    }
    
    private List<Node> findLoops(final Set<ControlFlowNode> scopeNodes, final ControlFlowNode entryPoint, final boolean excludeEntryPoint) {
        final List<Node> result = new ArrayList<Node>();
        final StrongBox<Label[]> switchLabels = new StrongBox<Label[]>();
        final Set<ControlFlowNode> scope = new LinkedHashSet<ControlFlowNode>(scopeNodes);
        final ArrayDeque<ControlFlowNode> agenda = new ArrayDeque<ControlFlowNode>();
        agenda.addLast(entryPoint);
        while (!agenda.isEmpty()) {
            final ControlFlowNode node = agenda.pollFirst();
            if (scope.contains(node) && node.getDominanceFrontier().contains(node) && (node != entryPoint || !excludeEntryPoint)) {
                final Set<ControlFlowNode> loopContents = findLoopContents(scope, node);
                final BasicBlock basicBlock = (BasicBlock)node.getUserData();
                final StrongBox<Expression> condition = new StrongBox<Expression>();
                final StrongBox<Label> trueLabel = new StrongBox<Label>();
                final StrongBox<Label> falseLabel = new StrongBox<Label>();
                final ControlFlowNode lastInLoop = CollectionUtilities.lastOrDefault(loopContents);
                final BasicBlock lastBlock = (BasicBlock)lastInLoop.getUserData();
                if (loopContents.size() == 1 && PatternMatching.matchSimpleBreak(basicBlock, trueLabel) && trueLabel.get() == CollectionUtilities.first(basicBlock.getBody())) {
                    final Loop emptyLoop = new Loop();
                    emptyLoop.setBody(new Block());
                    final BasicBlock block = new BasicBlock();
                    final List<Node> blockBody = block.getBody();
                    blockBody.add(basicBlock.getBody().get(0));
                    blockBody.add(emptyLoop);
                    result.add(block);
                    scope.remove(lastInLoop);
                    continue;
                }
                for (int pass = 0; pass < 2; ++pass) {
                    final boolean isPostCondition = pass == 1;
                    final boolean foundCondition = isPostCondition ? PatternMatching.matchLastAndBreak(lastBlock, AstCode.IfTrue, trueLabel, condition, falseLabel) : PatternMatching.matchSingleAndBreak(basicBlock, AstCode.IfTrue, trueLabel, condition, falseLabel);
                    if (foundCondition) {
                        final ControlFlowNode trueTarget = this.labelsToNodes.get(trueLabel.get());
                        final ControlFlowNode falseTarget = this.labelsToNodes.get(falseLabel.get());
                        if (!loopContents.contains(falseTarget) || loopContents.contains(trueTarget)) {
                            if (!loopContents.contains(trueTarget)) {
                                continue;
                            }
                            if (loopContents.contains(falseTarget)) {
                                continue;
                            }
                        }
                        final boolean flipped = loopContents.contains(falseTarget) || falseTarget == node;
                        if (flipped) {
                            final Label temp = trueLabel.get();
                            trueLabel.set(falseLabel.get());
                            falseLabel.set(temp);
                            condition.set(AstOptimizer.simplifyLogicalNot(new Expression(AstCode.LogicalNot, null, condition.get().getOffset(), new Expression[] { condition.get() })));
                        }
                        boolean canWriteConditionalLoop;
                        if (isPostCondition) {
                            Expression continueGoto;
                            if (flipped) {
                                continueGoto = CollectionUtilities.last(lastBlock.getBody());
                            }
                            else {
                                continueGoto = lastBlock.getBody().get(lastBlock.getBody().size() - 2);
                            }
                            canWriteConditionalLoop = (this.countJumps(loopContents, trueLabel.get(), continueGoto) == 0);
                        }
                        else {
                            canWriteConditionalLoop = true;
                        }
                        if (canWriteConditionalLoop) {
                            AstOptimizer.removeOrThrow(loopContents, node);
                            AstOptimizer.removeOrThrow(scope, node);
                            final ControlFlowNode postLoopTarget = this.labelsToNodes.get(falseLabel.get());
                            if (postLoopTarget != null) {
                                final Set<ControlFlowNode> postLoopContents = findDominatedNodes(scope, postLoopTarget);
                                final LinkedHashSet<ControlFlowNode> pullIn = new LinkedHashSet<ControlFlowNode>(scope);
                                pullIn.removeAll(postLoopContents);
                                for (final ControlFlowNode n : pullIn) {
                                    if (node.dominates(n)) {
                                        loopContents.add(n);
                                    }
                                }
                            }
                            BasicBlock block2;
                            List<Node> basicBlockBody;
                            if (isPostCondition) {
                                block2 = new BasicBlock();
                                basicBlockBody = block2.getBody();
                                AstOptimizer.removeTail(lastBlock.getBody(), AstCode.IfTrue, AstCode.Goto);
                                Label loopLabel;
                                if (lastBlock.getBody().size() > 1) {
                                    lastBlock.getBody().add(new Expression(AstCode.Goto, trueLabel.get(), -34, new Expression[0]));
                                    loopLabel = new Label("Loop_" + this._nextLabelIndex++);
                                }
                                else {
                                    scope.remove(lastInLoop);
                                    loopContents.remove(lastInLoop);
                                    loopLabel = lastBlock.getBody().get(0);
                                }
                                basicBlockBody.add(loopLabel);
                            }
                            else {
                                block2 = basicBlock;
                                basicBlockBody = block2.getBody();
                                AstOptimizer.removeTail(basicBlockBody, AstCode.IfTrue, AstCode.Goto);
                            }
                            final Loop loop = new Loop();
                            final Block bodyBlock = new Block();
                            loop.setCondition(condition.get());
                            loop.setBody(bodyBlock);
                            if (isPostCondition) {
                                loop.setLoopType(LoopType.PostCondition);
                                bodyBlock.getBody().add(basicBlock);
                            }
                            bodyBlock.setEntryGoto(new Expression(AstCode.Goto, trueLabel.get(), -34, new Expression[0]));
                            bodyBlock.getBody().addAll(this.findLoops(loopContents, node, isPostCondition));
                            basicBlockBody.add(loop);
                            if (isPostCondition) {
                                basicBlockBody.add(new Expression(AstCode.Goto, falseLabel.get(), -34, new Expression[0]));
                            }
                            else {
                                basicBlockBody.add(new Expression(AstCode.Goto, falseLabel.get(), -34, new Expression[0]));
                            }
                            result.add(block2);
                            scope.removeAll(loopContents);
                            break;
                        }
                    }
                }
                if (scope.contains(node)) {
                    final BasicBlock block3 = new BasicBlock();
                    final List<Node> blockBody2 = block3.getBody();
                    final Loop loop2 = new Loop();
                    final Block bodyBlock2 = new Block();
                    loop2.setBody(bodyBlock2);
                    final LoopExitInfo exitInfo = this.findLoopExitInfo(loopContents);
                    if (exitInfo.exitLabel != null) {
                        final ControlFlowNode postLoopTarget2 = this.labelsToNodes.get(exitInfo.exitLabel);
                        if (postLoopTarget2.getIncoming().size() == 1) {
                            final ControlFlowNode predecessor = CollectionUtilities.firstOrDefault(postLoopTarget2.getPredecessors());
                            if (predecessor != null && loopContents.contains(predecessor)) {
                                final BasicBlock b = (BasicBlock)predecessor.getUserData();
                                if (PatternMatching.matchLast(b, AstCode.Switch, switchLabels, condition) && !ArrayUtilities.isNullOrEmpty(switchLabels.get()) && exitInfo.exitLabel == switchLabels.get()[0]) {
                                    final Set<ControlFlowNode> defaultContents = findDominatedNodes(scope, postLoopTarget2);
                                    for (final ControlFlowNode n2 : defaultContents) {
                                        if (scope.contains(n2) && node.dominates(n2)) {
                                            loopContents.add(n2);
                                        }
                                    }
                                }
                            }
                        }
                        if (!loopContents.contains(postLoopTarget2)) {
                            final Set<ControlFlowNode> postLoopContents2 = findDominatedNodes(scope, postLoopTarget2);
                            final LinkedHashSet<ControlFlowNode> pullIn2 = new LinkedHashSet<ControlFlowNode>(scope);
                            pullIn2.removeAll(postLoopContents2);
                            for (final ControlFlowNode n3 : pullIn2) {
                                if (n3.getBlockIndex() < postLoopTarget2.getBlockIndex() && scope.contains(n3) && node.dominates(n3)) {
                                    loopContents.add(n3);
                                }
                            }
                        }
                    }
                    else if (exitInfo.additionalNodes.size() == 1) {
                        final ControlFlowNode postLoopTarget2 = CollectionUtilities.first(exitInfo.additionalNodes);
                        final BasicBlock postLoopBlock = (BasicBlock)postLoopTarget2.getUserData();
                        final Node postLoopBlockHead = CollectionUtilities.firstOrDefault(postLoopBlock.getBody());
                        final ControlFlowNode predecessor2 = CollectionUtilities.single(postLoopTarget2.getPredecessors());
                        if (postLoopBlockHead instanceof Label && loopContents.contains(predecessor2)) {
                            final BasicBlock b2 = (BasicBlock)predecessor2.getUserData();
                            if (PatternMatching.matchLast(b2, AstCode.Switch, switchLabels, condition) && !ArrayUtilities.isNullOrEmpty(switchLabels.get()) && postLoopBlockHead == switchLabels.get()[0]) {
                                final Set<ControlFlowNode> defaultContents2 = findDominatedNodes(scope, postLoopTarget2);
                                for (final ControlFlowNode n4 : defaultContents2) {
                                    if (scope.contains(n4) && node.dominates(n4)) {
                                        loopContents.add(n4);
                                    }
                                }
                            }
                        }
                    }
                    else if (exitInfo.additionalNodes.size() > 1) {
                        final Set<ControlFlowNode> auxNodes = new LinkedHashSet<ControlFlowNode>();
                        for (final ControlFlowNode n5 : exitInfo.additionalNodes) {
                            if (scope.contains(n5) && node.dominates(n5)) {
                                auxNodes.addAll(findDominatedNodes(scope, n5));
                            }
                        }
                        final List<ControlFlowNode> sortedNodes = CollectionUtilities.toList(auxNodes);
                        Collections.sort(sortedNodes);
                        loopContents.addAll(sortedNodes);
                    }
                    bodyBlock2.setEntryGoto(new Expression(AstCode.Goto, basicBlock.getBody().get(0), -34, new Expression[0]));
                    bodyBlock2.getBody().addAll(this.findLoops(loopContents, node, true));
                    blockBody2.add(new Label("Loop_" + this._nextLabelIndex++));
                    blockBody2.add(loop2);
                    result.add(block3);
                    scope.removeAll(loopContents);
                }
            }
            for (final ControlFlowNode child : node.getDominatorTreeChildren()) {
                agenda.addLast(child);
            }
        }
        final Iterator<ControlFlowNode> loc_6 = scope.iterator();
        while (loc_6.hasNext()) {
            final ControlFlowNode node = loc_6.next();
            result.add((Node)node.getUserData());
        }
        scope.clear();
        return result;
    }
    
    private LoopExitInfo findLoopExitInfo(final Set<ControlFlowNode> contents) {
        final LoopExitInfo exitInfo = new LoopExitInfo(null);
        boolean noCommonExit = false;
        for (final ControlFlowNode node : contents) {
            final BasicBlock basicBlock = (BasicBlock)node.getUserData();
            for (final Expression e : basicBlock.getSelfAndChildrenRecursive(Expression.class)) {
                for (final Label target : e.getBranchTargets()) {
                    final ControlFlowNode targetNode = this.labelsToNodes.get(target);
                    if (targetNode != null) {
                        if (contents.contains(targetNode)) {
                            continue;
                        }
                        if (targetNode.getIncoming().size() == 1) {
                            exitInfo.additionalNodes.add(targetNode);
                        }
                        else if (exitInfo.exitLabel == null) {
                            exitInfo.exitLabel = target;
                        }
                        else {
                            if (exitInfo.exitLabel == target) {
                                continue;
                            }
                            noCommonExit = true;
                        }
                    }
                }
            }
        }
        if (noCommonExit) {
            exitInfo.exitLabel = null;
        }
        return exitInfo;
    }
    
    private int countJumps(final Set<ControlFlowNode> nodes, final Label target, final Expression ignore) {
        int jumpCount = 0;
        for (final ControlFlowNode node : nodes) {
            final BasicBlock basicBlock = (BasicBlock)node.getUserData();
            for (final Expression e : basicBlock.getSelfAndChildrenRecursive(Expression.class)) {
                if (e != ignore && e.getBranchTargets().contains(target)) {
                    ++jumpCount;
                }
            }
        }
        return jumpCount;
    }
    
    private static Set<ControlFlowNode> findLoopContents(final Set<ControlFlowNode> scope, final ControlFlowNode head) {
        final Set<ControlFlowNode> viaBackEdges = new LinkedHashSet<ControlFlowNode>();
        for (final ControlFlowNode predecessor : head.getPredecessors()) {
            if (head.dominates(predecessor)) {
                viaBackEdges.add(predecessor);
            }
        }
        final Set<ControlFlowNode> agenda = new LinkedHashSet<ControlFlowNode>(viaBackEdges);
        final Set<ControlFlowNode> result = new LinkedHashSet<ControlFlowNode>();
        while (!agenda.isEmpty()) {
            final ControlFlowNode addNode = agenda.iterator().next();
            agenda.remove(addNode);
            if (scope.contains(addNode) && head.dominates(addNode) && result.add(addNode)) {
                for (final ControlFlowNode predecessor2 : addNode.getPredecessors()) {
                    agenda.add(predecessor2);
                }
            }
        }
        if (scope.contains(head)) {
            result.add(head);
        }
        if (result.size() <= 1) {
            return result;
        }
        final List<ControlFlowNode> sortedResult = new ArrayList<ControlFlowNode>(result);
        Collections.sort(sortedResult, new Comparator<ControlFlowNode>() {
            @Override
            public int compare(@NotNull final ControlFlowNode o1, @NotNull final ControlFlowNode o2) {
                return Integer.compare(o1.getBlockIndex(), o2.getBlockIndex());
            }
        });
        result.clear();
        result.addAll(sortedResult);
        return result;
    }
    
    private List<Node> findConditions(final Set<ControlFlowNode> scopeNodes, final ControlFlowNode entryNode) {
        final List<Node> result = new ArrayList<Node>();
        final Set<ControlFlowNode> scope = new HashSet<ControlFlowNode>(scopeNodes);
        final Stack<ControlFlowNode> agenda = new Stack<ControlFlowNode>();
        agenda.push(entryNode);
        while (!agenda.isEmpty()) {
            final ControlFlowNode node = agenda.pop();
            if (node == null) {
                continue;
            }
            if (scope.contains(node)) {
                final BasicBlock block = (BasicBlock)node.getUserData();
                final List<Node> blockBody = block.getBody();
                final StrongBox<Label[]> caseLabels = new StrongBox<Label[]>();
                final StrongBox<Expression> switchArgument = new StrongBox<Expression>();
                final StrongBox<Label> tempTarget = new StrongBox<Label>();
                if (PatternMatching.matchLast(block, AstCode.Switch, caseLabels, switchArgument)) {
                    final Expression switchExpression = blockBody.get(blockBody.size() - 1);
                    final Switch switchNode = new Switch();
                    switchNode.setCondition(switchArgument.get());
                    AstOptimizer.removeTail(blockBody, AstCode.Switch);
                    blockBody.add(switchNode);
                    result.add(block);
                    AstOptimizer.removeOrThrow(scope, node);
                    final Label[] labels = caseLabels.get();
                    final SwitchInfo switchInfo = switchExpression.getUserData(AstKeys.SWITCH_INFO);
                    final int lowValue = switchInfo.getLowValue();
                    final int[] keys = switchInfo.getKeys();
                    final Label defaultLabel = labels[0];
                    final ControlFlowNode defaultTarget = this.labelsToNodes.get(defaultLabel);
                    boolean defaultFollowsSwitch = false;
                    for (int i = 1; i < labels.length; ++i) {
                        final Label caseLabel = labels[i];
                        if (caseLabel != defaultLabel) {
                            CaseBlock caseBlock = null;
                            for (final CaseBlock cb : switchNode.getCaseBlocks()) {
                                if (cb.getEntryGoto().getOperand() == caseLabel) {
                                    caseBlock = cb;
                                    break;
                                }
                            }
                            if (caseBlock == null) {
                                caseBlock = new CaseBlock();
                                caseBlock.setEntryGoto(new Expression(AstCode.Goto, caseLabel, -34, new Expression[0]));
                                final ControlFlowNode caseTarget = this.labelsToNodes.get(caseLabel);
                                final List<Node> caseBody = caseBlock.getBody();
                                switchNode.getCaseBlocks().add(caseBlock);
                                if (caseTarget != null) {
                                    if (caseTarget.getDominanceFrontier().contains(defaultTarget)) {
                                        defaultFollowsSwitch = true;
                                    }
                                    final Set<ControlFlowNode> content = findDominatedNodes(scope, caseTarget);
                                    scope.removeAll(content);
                                    caseBody.addAll(this.findConditions(content, caseTarget));
                                }
                                else {
                                    final BasicBlock explicitGoto = new BasicBlock();
                                    explicitGoto.getBody().add(new Label("SwitchGoto_" + this._nextLabelIndex++));
                                    explicitGoto.getBody().add(new Expression(AstCode.Goto, caseLabel, -34, new Expression[0]));
                                    caseBody.add(explicitGoto);
                                }
                                if (caseBody.isEmpty() || !PatternMatching.matchLast(caseBody.get(caseBody.size() - 1), AstCode.Goto, tempTarget) || !ArrayUtilities.contains(labels, tempTarget.get())) {
                                    final BasicBlock explicitBreak = new BasicBlock();
                                    explicitBreak.getBody().add(new Label("SwitchBreak_" + this._nextLabelIndex++));
                                    explicitBreak.getBody().add(new Expression(AstCode.LoopOrSwitchBreak, null, -34, new Expression[0]));
                                    caseBody.add(explicitBreak);
                                }
                            }
                            if (switchInfo.hasKeys()) {
                                caseBlock.getValues().add(keys[i - 1]);
                            }
                            else {
                                caseBlock.getValues().add(lowValue + i - 1);
                            }
                        }
                    }
                    if (!defaultFollowsSwitch) {
                        final CaseBlock defaultBlock = new CaseBlock();
                        defaultBlock.setEntryGoto(new Expression(AstCode.Goto, defaultLabel, -34, new Expression[0]));
                        switchNode.getCaseBlocks().add(defaultBlock);
                        final Set<ControlFlowNode> content2 = findDominatedNodes(scope, defaultTarget);
                        scope.removeAll(content2);
                        defaultBlock.getBody().addAll(this.findConditions(content2, defaultTarget));
                        final BasicBlock explicitBreak2 = new BasicBlock();
                        explicitBreak2.getBody().add(new Label("SwitchBreak_" + this._nextLabelIndex++));
                        explicitBreak2.getBody().add(new Expression(AstCode.LoopOrSwitchBreak, null, -34, new Expression[0]));
                        defaultBlock.getBody().add(explicitBreak2);
                    }
                    this.reorderCaseBlocks(switchNode);
                }
                final StrongBox<Expression> condition = new StrongBox<Expression>();
                final StrongBox<Label> trueLabel = new StrongBox<Label>();
                final StrongBox<Label> falseLabel = new StrongBox<Label>();
                if (PatternMatching.matchLastAndBreak(block, AstCode.IfTrue, trueLabel, condition, falseLabel)) {
                    final Label temp = trueLabel.get();
                    trueLabel.set(falseLabel.get());
                    falseLabel.set(temp);
                    condition.set(AstOptimizer.simplifyLogicalNot(new Expression(AstCode.LogicalNot, null, condition.get().getOffset(), new Expression[] { condition.get() })));
                    final Condition conditionNode = new Condition();
                    final Block trueBlock = new Block();
                    final Block falseBlock = new Block();
                    trueBlock.setEntryGoto(new Expression(AstCode.Goto, trueLabel.get(), -34, new Expression[0]));
                    falseBlock.setEntryGoto(new Expression(AstCode.Goto, falseLabel.get(), -34, new Expression[0]));
                    conditionNode.setCondition(condition.get());
                    conditionNode.setTrueBlock(trueBlock);
                    conditionNode.setFalseBlock(falseBlock);
                    AstOptimizer.removeTail(blockBody, AstCode.IfTrue, AstCode.Goto);
                    blockBody.add(conditionNode);
                    result.add(block);
                    AstOptimizer.removeOrThrow(scope, node);
                    final ControlFlowNode trueTarget = this.labelsToNodes.get(trueLabel.get());
                    final ControlFlowNode falseTarget = this.labelsToNodes.get(falseLabel.get());
                    if (trueTarget != null && hasSingleEdgeEnteringBlock(trueTarget)) {
                        final Set<ControlFlowNode> content3 = findDominatedNodes(scope, trueTarget);
                        scope.removeAll(content3);
                        conditionNode.getTrueBlock().getBody().addAll(this.findConditions(content3, trueTarget));
                    }
                    if (falseTarget != null && hasSingleEdgeEnteringBlock(falseTarget)) {
                        final Set<ControlFlowNode> content3 = findDominatedNodes(scope, falseTarget);
                        scope.removeAll(content3);
                        conditionNode.getFalseBlock().getBody().addAll(this.findConditions(content3, falseTarget));
                    }
                }
                if (scope.contains(node)) {
                    result.add((Node)node.getUserData());
                    scope.remove(node);
                }
            }
            final List<ControlFlowNode> dominatorTreeChildren = node.getDominatorTreeChildren();
            for (int j = dominatorTreeChildren.size() - 1; j >= 0; --j) {
                agenda.push(dominatorTreeChildren.get(j));
            }
        }
        final Iterator<ControlFlowNode> loc_1 = scope.iterator();
        while (loc_1.hasNext()) {
            final ControlFlowNode node = loc_1.next();
            result.add((Node)node.getUserData());
        }
        return result;
    }
    
    private void reorderCaseBlocks(final Switch switchNode) {
        Collections.sort(switchNode.getCaseBlocks(), new Comparator<CaseBlock>() {
            @Override
            public int compare(@NotNull final CaseBlock o1, @NotNull final CaseBlock o2) {
                final Label l1 = (Label)o1.getEntryGoto().getOperand();
                final Label l2 = (Label)o2.getEntryGoto().getOperand();
                return Integer.compare(l1.getOffset(), l2.getOffset());
            }
        });
        final List<CaseBlock> caseBlocks = switchNode.getCaseBlocks();
        final Map<Label, Pair<CaseBlock, Integer>> caseLookup = new IdentityHashMap<Label, Pair<CaseBlock, Integer>>();
        for (int i = 0; i < caseBlocks.size(); ++i) {
            final CaseBlock block = caseBlocks.get(i);
            caseLookup.put((Label)block.getEntryGoto().getOperand(), Pair.create(block, i));
        }
        final StrongBox<Label> label = new StrongBox<Label>();
        final Set<CaseBlock> movedBlocks = new HashSet<CaseBlock>();
        for (int j = 0; j < caseBlocks.size(); ++j) {
            final CaseBlock block2 = caseBlocks.get(j);
            final List<Node> caseBody = block2.getBody();
            Node lastInCase = CollectionUtilities.lastOrDefault(caseBody);
            if (lastInCase instanceof BasicBlock) {
                lastInCase = CollectionUtilities.lastOrDefault(((BasicBlock)lastInCase).getBody());
            }
            else if (lastInCase instanceof Block) {
                lastInCase = CollectionUtilities.lastOrDefault(((Block)lastInCase).getBody());
            }
            if (PatternMatching.matchGetOperand(lastInCase, AstCode.Goto, label)) {
                final Pair<CaseBlock, Integer> caseInfo = caseLookup.get(label.get());
                if (caseInfo != null) {
                    final int targetIndex = caseInfo.getSecond();
                    if (targetIndex != j + 1) {
                        if (!movedBlocks.contains(block2)) {
                            caseBlocks.remove(j);
                            caseBlocks.add(targetIndex, block2);
                            movedBlocks.add(block2);
                            if (targetIndex > j) {
                                --j;
                            }
                        }
                    }
                }
            }
        }
    }
    
    private static boolean hasSingleEdgeEnteringBlock(final ControlFlowNode node) {
        int count = 0;
        for (final ControlFlowEdge edge : node.getIncoming()) {
            if (!node.dominates(edge.getSource()) && ++count > 1) {
                return false;
            }
        }
        return count == 1;
    }
    
    private static Set<ControlFlowNode> findDominatedNodes(final Set<ControlFlowNode> scope, final ControlFlowNode head) {
        final Set<ControlFlowNode> agenda = new LinkedHashSet<ControlFlowNode>();
        final Set<ControlFlowNode> result = new LinkedHashSet<ControlFlowNode>();
        agenda.add(head);
        while (!agenda.isEmpty()) {
            final ControlFlowNode addNode = agenda.iterator().next();
            agenda.remove(addNode);
            if (scope.contains(addNode) && head.dominates(addNode) && result.add(addNode)) {
                for (final ControlFlowNode successor : addNode.getSuccessors()) {
                    agenda.add(successor);
                }
            }
        }
        return result;
    }
    
    private static final class LoopExitInfo
    {
        Label exitLabel;
        final Set<ControlFlowNode> additionalNodes;
        
        private LoopExitInfo() {
            super();
            this.additionalNodes = new LinkedHashSet<ControlFlowNode>();
        }
    }
}
