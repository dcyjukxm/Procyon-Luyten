package com.strobel.assembler.metadata;

import com.strobel.assembler.ir.attributes.*;
import com.strobel.annotations.*;
import com.strobel.decompiler.*;
import com.strobel.assembler.*;
import com.strobel.assembler.ir.*;
import com.strobel.core.*;
import java.util.*;
import com.strobel.assembler.flowanalysis.*;

public final class ExceptionHandlerMapper
{
    private final InstructionCollection _instructions;
    private final List<ExceptionTableEntry> _tableEntries;
    private final List<ExceptionHandler> _handlerPlaceholders;
    private final List<ControlFlowNode> _nodes;
    private final int[] _offsets;
    private final boolean[] _hasIncomingJumps;
    private final ControlFlowNode _entryPoint;
    private final ControlFlowNode _regularExit;
    private final ControlFlowNode _exceptionalExit;
    private int _nextBlockId;
    boolean copyFinallyBlocks;
    
    public static List<ExceptionHandler> run(final InstructionCollection instructions, final List<ExceptionTableEntry> tableEntries) {
        VerifyArgument.notNull(instructions, "instructions");
        VerifyArgument.notNull(tableEntries, "tableEntries");
        final ExceptionHandlerMapper builder = new ExceptionHandlerMapper(instructions, tableEntries);
        final ControlFlowGraph cfg = builder.build();
        final List<ExceptionHandler> handlers = new ArrayList<ExceptionHandler>();
        final Map<ExceptionTableEntry, ControlFlowNode> handlerStartNodes = new IdentityHashMap<ExceptionTableEntry, ControlFlowNode>();
        for (final ExceptionTableEntry entry : builder._tableEntries) {
            final Instruction handlerStart = instructions.atOffset(entry.getHandlerOffset());
            final ControlFlowNode handlerStartNode = builder.findNode(handlerStart);
            if (handlerStartNode == null) {
                throw new IllegalStateException(String.format("Could not find entry node for handler at offset %d.", handlerStart.getOffset()));
            }
            if (handlerStartNode.getIncoming().isEmpty()) {
                builder.createEdge(cfg.getEntryPoint(), handlerStartNode, JumpType.Normal);
            }
            handlerStartNodes.put(entry, handlerStartNode);
        }
        cfg.computeDominance();
        cfg.computeDominanceFrontier();
        for (final ExceptionTableEntry entry : builder._tableEntries) {
            final ControlFlowNode handlerStart2 = handlerStartNodes.get(entry);
            final List<ControlFlowNode> dominatedNodes = new ArrayList<ControlFlowNode>();
            for (final ControlFlowNode node : findDominatedNodes(cfg, handlerStart2)) {
                if (node.getNodeType() == ControlFlowNodeType.Normal) {
                    dominatedNodes.add(node);
                }
            }
            Collections.sort(dominatedNodes, new Comparator<ControlFlowNode>() {
                @Override
                public int compare(@NotNull final ControlFlowNode o1, @NotNull final ControlFlowNode o2) {
                    return Integer.compare(o1.getBlockIndex(), o2.getBlockIndex());
                }
            });
            for (int i = 1; i < dominatedNodes.size(); ++i) {
                final ControlFlowNode prev = dominatedNodes.get(i - 1);
                final ControlFlowNode node2 = dominatedNodes.get(i);
                if (node2.getBlockIndex() != prev.getBlockIndex() + 1) {
                    final int j = i;
                    if (j < dominatedNodes.size()) {
                        dominatedNodes.remove(i);
                    }
                }
            }
            final Instruction lastInstruction = instructions.get(instructions.size() - 1);
            InstructionBlock tryBlock;
            if (entry.getEndOffset() == lastInstruction.getEndOffset()) {
                tryBlock = new InstructionBlock(instructions.atOffset(entry.getStartOffset()), lastInstruction);
            }
            else {
                tryBlock = new InstructionBlock(instructions.atOffset(entry.getStartOffset()), instructions.atOffset(entry.getEndOffset()).getPrevious());
            }
            if (entry.getCatchType() == null) {
                handlers.add(ExceptionHandler.createFinally(tryBlock, new InstructionBlock(handlerStart2.getStart(), CollectionUtilities.lastOrDefault(dominatedNodes).getEnd())));
            }
            else {
                handlers.add(ExceptionHandler.createCatch(tryBlock, new InstructionBlock(handlerStart2.getStart(), CollectionUtilities.lastOrDefault(dominatedNodes).getEnd()), entry.getCatchType()));
            }
        }
        return handlers;
    }
    
    private ControlFlowNode findNode(final Instruction instruction) {
        if (instruction == null) {
            return null;
        }
        return CollectionUtilities.firstOrDefault(this._nodes, new Predicate<ControlFlowNode>() {
            @Override
            public boolean test(final ControlFlowNode node) {
                return node.getNodeType() == ControlFlowNodeType.Normal && instruction.getOffset() >= node.getStart().getOffset() && instruction.getOffset() < node.getEnd().getEndOffset();
            }
        });
    }
    
    private static Set<ControlFlowNode> findDominatedNodes(final ControlFlowGraph cfg, final ControlFlowNode head) {
        final Set<ControlFlowNode> agenda = new LinkedHashSet<ControlFlowNode>();
        final Set<ControlFlowNode> result = new LinkedHashSet<ControlFlowNode>();
        agenda.add(head);
        while (!agenda.isEmpty()) {
            final ControlFlowNode addNode = agenda.iterator().next();
            agenda.remove(addNode);
            if (!head.dominates(addNode) && !shouldIncludeExceptionalExit(cfg, head, addNode)) {
                continue;
            }
            if (!result.add(addNode)) {
                continue;
            }
            for (final ControlFlowNode successor : addNode.getSuccessors()) {
                agenda.add(successor);
            }
        }
        return result;
    }
    
    private static boolean shouldIncludeExceptionalExit(final ControlFlowGraph cfg, final ControlFlowNode head, final ControlFlowNode node) {
        if (node.getNodeType() != ControlFlowNodeType.Normal) {
            return false;
        }
        if (!node.getDominanceFrontier().contains(cfg.getExceptionalExit()) && !node.dominates(cfg.getExceptionalExit())) {
            final ControlFlowNode innermostHandlerNode = findInnermostExceptionHandlerNode(cfg, node.getStart().getOffset());
            if (innermostHandlerNode == null || !node.getDominanceFrontier().contains(innermostHandlerNode)) {
                return false;
            }
        }
        return node.getStart().getNext() == node.getEnd() && (head.getStart().getOpCode().isStore() && node.getStart().getOpCode().isLoad() && node.getEnd().getOpCode() == OpCode.ATHROW) && InstructionHelper.getLoadOrStoreSlot(head.getStart()) == InstructionHelper.getLoadOrStoreSlot(node.getStart());
    }
    
    private ExceptionHandlerMapper(final InstructionCollection instructions, final List<ExceptionTableEntry> tableEntries) {
        super();
        this._nodes = new Collection<ControlFlowNode>();
        this.copyFinallyBlocks = false;
        this._instructions = VerifyArgument.notNull(instructions, "instructions");
        this._tableEntries = VerifyArgument.notNull(tableEntries, "tableEntries");
        this._handlerPlaceholders = this.createHandlerPlaceholders();
        this._offsets = new int[instructions.size()];
        this._hasIncomingJumps = new boolean[instructions.size()];
        for (int i = 0; i < instructions.size(); ++i) {
            this._offsets[i] = instructions.get(i).getOffset();
        }
        this._entryPoint = new ControlFlowNode(this._nextBlockId++, 0, ControlFlowNodeType.EntryPoint);
        this._regularExit = new ControlFlowNode(this._nextBlockId++, -1, ControlFlowNodeType.RegularExit);
        this._exceptionalExit = new ControlFlowNode(this._nextBlockId++, -2, ControlFlowNodeType.ExceptionalExit);
        this._nodes.add(this._entryPoint);
        this._nodes.add(this._regularExit);
        this._nodes.add(this._exceptionalExit);
    }
    
    private ControlFlowGraph build() {
        this.calculateIncomingJumps();
        this.createNodes();
        this.createRegularControlFlow();
        this.createExceptionalControlFlow();
        return new ControlFlowGraph((ControlFlowNode[])this._nodes.toArray(new ControlFlowNode[this._nodes.size()]));
    }
    
    private boolean isHandlerStart(final Instruction instruction) {
        for (final ExceptionTableEntry entry : this._tableEntries) {
            if (entry.getHandlerOffset() == instruction.getOffset()) {
                return true;
            }
        }
        return false;
    }
    
    private void calculateIncomingJumps() {
        for (final Instruction instruction : this._instructions) {
            final OpCode opCode = instruction.getOpCode();
            if (opCode.getOperandType() == OperandType.BranchTarget || opCode.getOperandType() == OperandType.BranchTargetWide) {
                this._hasIncomingJumps[this.getInstructionIndex(instruction.getOperand(0))] = true;
            }
            else {
                if (opCode.getOperandType() != OperandType.Switch) {
                    continue;
                }
                final SwitchInfo switchInfo = instruction.getOperand(0);
                this._hasIncomingJumps[this.getInstructionIndex(switchInfo.getDefaultTarget())] = true;
                Instruction[] loc_2;
                for (int loc_1 = (loc_2 = switchInfo.getTargets()).length, loc_3 = 0; loc_3 < loc_1; ++loc_3) {
                    final Instruction target = loc_2[loc_3];
                    this._hasIncomingJumps[this.getInstructionIndex(target)] = true;
                }
            }
        }
        for (final ExceptionTableEntry entry : this._tableEntries) {
            this._hasIncomingJumps[this.getInstructionIndex(this._instructions.atOffset(entry.getHandlerOffset()))] = true;
        }
    }
    
    private void createNodes() {
        final InstructionCollection instructions = this._instructions;
        for (int i = 0, n = instructions.size(); i < n; ++i) {
            final Instruction blockStart = instructions.get(i);
            final ExceptionHandler blockStartExceptionHandler = this.findInnermostExceptionHandler(blockStart.getOffset());
            while (i + 1 < n) {
                final Instruction instruction = instructions.get(i);
                final OpCode opCode = instruction.getOpCode();
                if (opCode.isBranch() && !opCode.isJumpToSubroutine()) {
                    break;
                }
                if (this._hasIncomingJumps[i + 1]) {
                    break;
                }
                final Instruction next = instruction.getNext();
                if (next != null) {
                    final ExceptionHandler innermostExceptionHandler = this.findInnermostExceptionHandler(next.getOffset());
                    if (innermostExceptionHandler != blockStartExceptionHandler) {
                        break;
                    }
                }
                ++i;
            }
            final ControlFlowNode node = new ControlFlowNode(this._nodes.size(), blockStart, instructions.get(i));
            node.setUserData(blockStartExceptionHandler);
            this._nodes.add(node);
        }
        for (final ExceptionHandler handler : this._handlerPlaceholders) {
            final int index = this._nodes.size();
            this._nodes.add(new ControlFlowNode(index, handler, null));
        }
    }
    
    private void createRegularControlFlow() {
        final InstructionCollection instructions = this._instructions;
        this.createEdge(this._entryPoint, instructions.get(0), JumpType.Normal);
        for (final ControlFlowNode node : this._nodes) {
            final Instruction end = node.getEnd();
            if (end != null) {
                if (end.getOffset() >= this._instructions.get(this._instructions.size() - 1).getEndOffset()) {
                    continue;
                }
                final OpCode endOpCode = end.getOpCode();
                if (!endOpCode.isUnconditionalBranch() || endOpCode.isJumpToSubroutine()) {
                    final Instruction next = end.getNext();
                    if (next != null && !this.isHandlerStart(next)) {
                        this.createEdge(node, next, JumpType.Normal);
                    }
                }
                for (Instruction instruction = node.getStart(); instruction != null && instruction.getOffset() <= end.getOffset(); instruction = instruction.getNext()) {
                    final OpCode opCode = instruction.getOpCode();
                    if (opCode.getOperandType() == OperandType.BranchTarget || opCode.getOperandType() == OperandType.BranchTargetWide) {
                        this.createEdge(node, instruction.getOperand(0), JumpType.Normal);
                    }
                    else if (opCode.getOperandType() == OperandType.Switch) {
                        final SwitchInfo switchInfo = instruction.getOperand(0);
                        this.createEdge(node, switchInfo.getDefaultTarget(), JumpType.Normal);
                        Instruction[] loc_2;
                        for (int loc_1 = (loc_2 = switchInfo.getTargets()).length, loc_3 = 0; loc_3 < loc_1; ++loc_3) {
                            final Instruction target = loc_2[loc_3];
                            this.createEdge(node, target, JumpType.Normal);
                        }
                    }
                }
                if (endOpCode.getFlowControl() != FlowControl.Return) {
                    continue;
                }
                this.createEdge(node, this._regularExit, JumpType.Normal);
            }
        }
    }
    
    private void createExceptionalControlFlow() {
        for (final ControlFlowNode node : this._nodes) {
            if (node.getNodeType() == ControlFlowNodeType.Normal) {
                final Instruction end = node.getEnd();
                final ExceptionHandler innermostHandler = this.findInnermostExceptionHandler(node.getEnd().getOffset());
                if (innermostHandler != null) {
                    for (final ExceptionHandler other : this._handlerPlaceholders) {
                        if (other.getTryBlock().equals(innermostHandler.getTryBlock())) {
                            final ControlFlowNode handlerNode = CollectionUtilities.firstOrDefault(this._nodes, new Predicate<ControlFlowNode>() {
                                @Override
                                public boolean test(final ControlFlowNode node) {
                                    return node.getExceptionHandler() == other;
                                }
                            });
                            if (node == handlerNode) {
                                continue;
                            }
                            this.createEdge(node, handlerNode, JumpType.JumpToExceptionHandler);
                        }
                    }
                }
                else if (end.getOpCode() == OpCode.ATHROW) {
                    this.createEdge(node, this._exceptionalExit, JumpType.JumpToExceptionHandler);
                }
            }
            final ExceptionHandler exceptionHandler = node.getExceptionHandler();
            if (exceptionHandler != null) {
                final ControlFlowNode parentHandler = this.findParentExceptionHandlerNode(node);
                if (parentHandler.getNodeType() != ControlFlowNodeType.ExceptionalExit) {
                    for (final ExceptionHandler other : this._handlerPlaceholders) {
                        if (Comparer.equals(other.getTryBlock(), parentHandler.getExceptionHandler().getTryBlock())) {
                            final ControlFlowNode handlerNode = CollectionUtilities.firstOrDefault(this._nodes, new Predicate<ControlFlowNode>() {
                                @Override
                                public boolean test(final ControlFlowNode node) {
                                    return node.getExceptionHandler() == other;
                                }
                            });
                            if (handlerNode == node) {
                                continue;
                            }
                            this.createEdge(node, handlerNode, JumpType.JumpToExceptionHandler);
                        }
                    }
                }
                this.createEdge(node, exceptionHandler.getHandlerBlock().getFirstInstruction(), JumpType.Normal);
            }
        }
    }
    
    private static ControlFlowNode findInnermostExceptionHandlerNode(final ControlFlowGraph cfg, final int offsetInTryBlock) {
        ExceptionHandler result = null;
        ControlFlowNode resultNode = null;
        final List<ControlFlowNode> nodes = cfg.getNodes();
        for (int i = nodes.size() - 1; i >= 0; --i) {
            final ControlFlowNode node = nodes.get(i);
            final ExceptionHandler handler = node.getExceptionHandler();
            if (handler == null) {
                break;
            }
            final InstructionBlock tryBlock = handler.getTryBlock();
            if (tryBlock.getFirstInstruction().getOffset() <= offsetInTryBlock && offsetInTryBlock < tryBlock.getLastInstruction().getEndOffset() && isNarrower(handler, result)) {
                result = handler;
                resultNode = node;
            }
        }
        return resultNode;
    }
    
    private static boolean isNarrower(final ExceptionHandler handler, final ExceptionHandler anchor) {
        if (handler == null || anchor == null) {
            return false;
        }
        final Instruction tryStart = handler.getTryBlock().getFirstInstruction();
        final Instruction anchorTryStart = anchor.getTryBlock().getFirstInstruction();
        if (tryStart.getOffset() > anchorTryStart.getOffset()) {
            return true;
        }
        final Instruction tryEnd = handler.getTryBlock().getLastInstruction();
        final Instruction anchorTryEnd = anchor.getTryBlock().getLastInstruction();
        return tryStart.getOffset() == anchorTryStart.getOffset() && tryEnd.getOffset() < anchorTryEnd.getOffset();
    }
    
    private ExceptionHandler findInnermostExceptionHandler(final int offsetInTryBlock) {
        ExceptionHandler result = null;
        for (final ExceptionHandler handler : this._handlerPlaceholders) {
            final InstructionBlock tryBlock = handler.getTryBlock();
            if (tryBlock.getFirstInstruction().getOffset() <= offsetInTryBlock && offsetInTryBlock < tryBlock.getLastInstruction().getEndOffset() && (result == null || isNarrower(handler, result))) {
                result = handler;
            }
        }
        return result;
    }
    
    private ControlFlowNode findParentExceptionHandlerNode(final ControlFlowNode node) {
        assert node.getNodeType() == ControlFlowNodeType.FinallyHandler;
        ControlFlowNode result = null;
        ExceptionHandler resultHandler = null;
        final int offset = node.getExceptionHandler().getHandlerBlock().getFirstInstruction().getOffset();
        for (int i = 0, n = this._nodes.size(); i < n; ++i) {
            final ControlFlowNode currentNode = this._nodes.get(i);
            final ExceptionHandler handler = currentNode.getExceptionHandler();
            if (handler != null && handler.getTryBlock().getFirstInstruction().getOffset() <= offset && offset < handler.getTryBlock().getLastInstruction().getEndOffset() && (resultHandler == null || isNarrower(handler, resultHandler))) {
                result = currentNode;
                resultHandler = handler;
            }
        }
        return (result != null) ? result : this._exceptionalExit;
    }
    
    private int getInstructionIndex(final Instruction instruction) {
        final int index = Arrays.binarySearch(this._offsets, instruction.getOffset());
        assert index >= 0;
        return index;
    }
    
    private ControlFlowEdge createEdge(final ControlFlowNode fromNode, final Instruction toInstruction, final JumpType type) {
        ControlFlowNode target = null;
        for (final ControlFlowNode node : this._nodes) {
            if (node.getStart() != null && node.getStart().getOffset() == toInstruction.getOffset()) {
                if (target != null) {
                    throw new IllegalStateException("Multiple edge targets detected!");
                }
                target = node;
            }
        }
        if (target != null) {
            return this.createEdge(fromNode, target, type);
        }
        throw new IllegalStateException("Could not find target node!");
    }
    
    private ControlFlowEdge createEdge(final ControlFlowNode fromNode, final ControlFlowNode toNode, final JumpType type) {
        final ControlFlowEdge edge = new ControlFlowEdge(fromNode, toNode, type);
        fromNode.getOutgoing().add(edge);
        toNode.getIncoming().add(edge);
        return edge;
    }
    
    private List<ExceptionHandler> createHandlerPlaceholders() {
        final ArrayList<ExceptionHandler> handlers = new ArrayList<ExceptionHandler>();
        for (final ExceptionTableEntry entry : this._tableEntries) {
            final Instruction afterTry = this._instructions.tryGetAtOffset(entry.getEndOffset());
            ExceptionHandler handler;
            if (entry.getCatchType() == null) {
                handler = ExceptionHandler.createFinally(new InstructionBlock(this._instructions.atOffset(entry.getStartOffset()), (afterTry != null) ? afterTry.getPrevious() : CollectionUtilities.last((List<Instruction>)this._instructions)), new InstructionBlock(this._instructions.atOffset(entry.getHandlerOffset()), this._instructions.atOffset(entry.getHandlerOffset())));
            }
            else {
                handler = ExceptionHandler.createCatch(new InstructionBlock(this._instructions.atOffset(entry.getStartOffset()), (afterTry != null) ? afterTry.getPrevious() : CollectionUtilities.last((List<Instruction>)this._instructions)), new InstructionBlock(this._instructions.atOffset(entry.getHandlerOffset()), this._instructions.atOffset(entry.getHandlerOffset())), entry.getCatchType());
            }
            handlers.add(handler);
        }
        return handlers;
    }
}
