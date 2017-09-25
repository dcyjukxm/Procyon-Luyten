package com.strobel.assembler.flowanalysis;

import com.strobel.assembler.*;
import com.strobel.assembler.metadata.*;
import com.strobel.core.*;
import com.strobel.assembler.ir.*;
import java.util.*;
import com.strobel.util.*;

public final class ControlFlowGraphBuilder
{
    private final List<Instruction> _instructions;
    private final List<ExceptionHandler> _exceptionHandlers;
    private final List<ControlFlowNode> _nodes;
    private final int[] _offsets;
    private final boolean[] _hasIncomingJumps;
    private final ControlFlowNode _entryPoint;
    private final ControlFlowNode _regularExit;
    private final ControlFlowNode _exceptionalExit;
    private int _nextBlockId;
    boolean copyFinallyBlocks;
    
    public static ControlFlowGraph build(final MethodBody methodBody) {
        VerifyArgument.notNull(methodBody, "methodBody");
        final ControlFlowGraphBuilder builder = new ControlFlowGraphBuilder(methodBody.getInstructions(), methodBody.getExceptionHandlers());
        return builder.build();
    }
    
    public static ControlFlowGraph build(final List<Instruction> instructions, final List<ExceptionHandler> exceptionHandlers) {
        final ControlFlowGraphBuilder builder = new ControlFlowGraphBuilder(VerifyArgument.notNull(instructions, "instructions"), VerifyArgument.notNull(exceptionHandlers, "exceptionHandlers"));
        return builder.build();
    }
    
    private ControlFlowGraphBuilder(final List<Instruction> instructions, final List<ExceptionHandler> exceptionHandlers) {
        super();
        this._nodes = new Collection<ControlFlowNode>();
        this.copyFinallyBlocks = false;
        this._instructions = VerifyArgument.notNull(instructions, "instructions");
        this._exceptionHandlers = coalesceExceptionHandlers(VerifyArgument.notNull(exceptionHandlers, "exceptionHandlers"));
        this._offsets = new int[instructions.size()];
        this._hasIncomingJumps = new boolean[this._offsets.length];
        for (int i = 0; i < instructions.size(); ++i) {
            this._offsets[i] = instructions.get(i).getOffset();
        }
        this._entryPoint = new ControlFlowNode(this._nextBlockId++, 0, ControlFlowNodeType.EntryPoint);
        this._regularExit = new ControlFlowNode(this._nextBlockId++, -1, ControlFlowNodeType.RegularExit);
        this._exceptionalExit = new ControlFlowNode(this._nextBlockId++, -1, ControlFlowNodeType.ExceptionalExit);
        this._nodes.add(this._entryPoint);
        this._nodes.add(this._regularExit);
        this._nodes.add(this._exceptionalExit);
    }
    
    public final ControlFlowGraph build() {
        this.calculateIncomingJumps();
        this.createNodes();
        this.createRegularControlFlow();
        this.createExceptionalControlFlow();
        if (this.copyFinallyBlocks) {
            this.copyFinallyBlocksIntoLeaveEdges();
        }
        else {
            this.transformLeaveEdges();
        }
        return new ControlFlowGraph((ControlFlowNode[])this._nodes.toArray(new ControlFlowNode[this._nodes.size()]));
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
        for (final ExceptionHandler handler : this._exceptionHandlers) {
            this._hasIncomingJumps[this.getInstructionIndex(handler.getHandlerBlock().getFirstInstruction())] = true;
        }
    }
    
    private void createNodes() {
        final List<Instruction> instructions = this._instructions;
        for (int i = 0, n = instructions.size(); i < n; ++i) {
            final Instruction blockStart = instructions.get(i);
            final ExceptionHandler blockStartExceptionHandler = this.findInnermostExceptionHandler(blockStart.getOffset());
            while (i + 1 < n) {
                final Instruction instruction = instructions.get(i);
                final OpCode opCode = instruction.getOpCode();
                if (opCode.isBranch()) {
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
            this._nodes.add(new ControlFlowNode(this._nodes.size(), blockStart, instructions.get(i)));
        }
        for (final ExceptionHandler handler : this._exceptionHandlers) {
            final int index = this._nodes.size();
            ControlFlowNode endFinallyNode;
            if (handler.getHandlerType() == ExceptionHandlerType.Finally) {
                endFinallyNode = new ControlFlowNode(index, handler.getHandlerBlock().getLastInstruction().getEndOffset(), ControlFlowNodeType.EndFinally);
            }
            else {
                endFinallyNode = null;
            }
            this._nodes.add(new ControlFlowNode(index, handler, endFinallyNode));
        }
    }
    
    private void createRegularControlFlow() {
        final List<Instruction> instructions = this._instructions;
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
                    if (next != null) {
                        final boolean isHandlerStart = CollectionUtilities.any(this._exceptionHandlers, new Predicate<ExceptionHandler>() {
                            @Override
                            public boolean test(final ExceptionHandler handler) {
                                return handler.getHandlerBlock().getFirstInstruction() == next;
                            }
                        });
                        if (!isHandlerStart) {
                            this.createEdge(node, next, JumpType.Normal);
                        }
                    }
                }
                for (Instruction instruction = node.getStart(); instruction != null && instruction.getOffset() <= end.getOffset(); instruction = instruction.getNext()) {
                    final OpCode opCode = instruction.getOpCode();
                    if (opCode.getOperandType() == OperandType.BranchTarget || opCode.getOperandType() == OperandType.BranchTargetWide) {
                        this.createBranchControlFlow(node, instruction, instruction.getOperand(0));
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
                if (endOpCode == OpCode.ENDFINALLY) {
                    final ControlFlowNode handlerBlock = this.findInnermostFinallyBlock(end.getOffset());
                    if (handlerBlock.getEndFinallyNode() == null) {
                        continue;
                    }
                    this.createEdge(node, handlerBlock.getEndFinallyNode(), JumpType.Normal);
                }
                else if (endOpCode == OpCode.LEAVE) {
                    ControlFlowNode handlerBlock = this.findInnermostHandlerBlock(end.getOffset());
                    if (handlerBlock == this._exceptionalExit) {
                        continue;
                    }
                    if (handlerBlock.getEndFinallyNode() == null) {
                        handlerBlock = this.findInnermostFinallyHandlerNode(handlerBlock.getExceptionHandler().getTryBlock().getLastInstruction().getOffset());
                    }
                    if (handlerBlock.getEndFinallyNode() == null) {
                        continue;
                    }
                    this.createEdge(node, handlerBlock.getEndFinallyNode(), JumpType.LeaveTry);
                }
                else {
                    if (!endOpCode.isReturn()) {
                        continue;
                    }
                    this.createReturnControlFlow(node, end);
                }
            }
        }
    }
    
    private void createExceptionalControlFlow() {
        for (final ControlFlowNode node : this._nodes) {
            final Instruction end = node.getEnd();
            if (end != null && end.getOffset() < this._instructions.get(this._instructions.size() - 1).getEndOffset()) {
                final ControlFlowNode innermostHandler = this.findInnermostExceptionHandlerNode(node.getEnd().getOffset());
                if (innermostHandler == this._exceptionalExit) {
                    final ControlFlowNode handlerBlock = this.findInnermostHandlerBlock(node.getEnd().getOffset());
                    ControlFlowNode finallyBlock;
                    if (handlerBlock.getExceptionHandler() != null) {
                        finallyBlock = this.findInnermostFinallyHandlerNode(handlerBlock.getExceptionHandler().getTryBlock().getLastInstruction().getOffset());
                        if (finallyBlock.getNodeType() == ControlFlowNodeType.FinallyHandler && finallyBlock.getExceptionHandler().getHandlerBlock().contains(end)) {
                            finallyBlock = this._exceptionalExit;
                        }
                    }
                    else {
                        finallyBlock = this._exceptionalExit;
                    }
                    this.createEdge(node, finallyBlock, JumpType.JumpToExceptionHandler);
                }
                else {
                    for (final ExceptionHandler handler : this._exceptionHandlers) {
                        if (Comparer.equals(handler.getTryBlock(), innermostHandler.getExceptionHandler().getTryBlock())) {
                            final ControlFlowNode handlerNode = CollectionUtilities.firstOrDefault(this._nodes, new Predicate<ControlFlowNode>() {
                                @Override
                                public boolean test(final ControlFlowNode node) {
                                    return node.getExceptionHandler() == handler;
                                }
                            });
                            this.createEdge(node, handlerNode, JumpType.JumpToExceptionHandler);
                        }
                    }
                    final ControlFlowNode handlerBlock = this.findInnermostHandlerBlock(node.getEnd().getOffset());
                    if (handlerBlock != innermostHandler && handlerBlock.getNodeType() == ControlFlowNodeType.CatchHandler) {
                        final ControlFlowNode finallyBlock = this.findInnermostFinallyHandlerNode(handlerBlock.getExceptionHandler().getTryBlock().getLastInstruction().getOffset());
                        if (finallyBlock.getNodeType() == ControlFlowNodeType.FinallyHandler) {
                            this.createEdge(node, finallyBlock, JumpType.JumpToExceptionHandler);
                        }
                    }
                }
            }
            final ExceptionHandler exceptionHandler = node.getExceptionHandler();
            if (exceptionHandler != null) {
                if (exceptionHandler.isFinally()) {
                    final ControlFlowNode handlerBlock = this.findInnermostFinallyHandlerNode(exceptionHandler.getHandlerBlock().getLastInstruction().getOffset());
                    if (handlerBlock.getNodeType() == ControlFlowNodeType.FinallyHandler && handlerBlock != node) {
                        this.createEdge(node, handlerBlock, JumpType.JumpToExceptionHandler);
                    }
                }
                else {
                    final ControlFlowNode adjacentFinally = this.findInnermostFinallyHandlerNode(exceptionHandler.getTryBlock().getLastInstruction().getOffset());
                    this.createEdge(node, (adjacentFinally != null) ? adjacentFinally : this.findParentExceptionHandlerNode(node), JumpType.JumpToExceptionHandler);
                }
                this.createEdge(node, exceptionHandler.getHandlerBlock().getFirstInstruction(), JumpType.Normal);
            }
        }
    }
    
    private void createBranchControlFlow(final ControlFlowNode node, final Instruction jump, final Instruction target) {
        final ControlFlowNode handlerNode = this.findInnermostHandlerBlock(jump.getOffset());
        final ControlFlowNode outerFinally = this.findInnermostHandlerBlock(jump.getOffset(), true);
        final ControlFlowNode targetHandlerNode = this.findInnermostHandlerBlock(target.getOffset());
        final ExceptionHandler handler = handlerNode.getExceptionHandler();
        Label_0099: {
            if (!jump.getOpCode().isJumpToSubroutine() && targetHandlerNode != handlerNode) {
                if (handler != null) {
                    if (handler.getTryBlock().contains(jump)) {
                        if (handler.getTryBlock().contains(target)) {
                            break Label_0099;
                        }
                    }
                    else if (handler.getHandlerBlock().contains(target)) {
                        break Label_0099;
                    }
                }
                if (handlerNode.getNodeType() == ControlFlowNodeType.CatchHandler) {
                    ControlFlowNode finallyHandlerNode = this.findInnermostFinallyHandlerNode(handler.getTryBlock().getLastInstruction().getOffset());
                    final ExceptionHandler finallyHandler = finallyHandlerNode.getExceptionHandler();
                    final ExceptionHandler outerFinallyHandler = outerFinally.getExceptionHandler();
                    if (finallyHandlerNode.getNodeType() != ControlFlowNodeType.FinallyHandler || (outerFinally.getNodeType() == ControlFlowNodeType.FinallyHandler && finallyHandler.getTryBlock().contains(outerFinallyHandler.getHandlerBlock()))) {
                        finallyHandlerNode = outerFinally;
                    }
                    if (finallyHandlerNode.getNodeType() == ControlFlowNodeType.FinallyHandler && finallyHandlerNode != targetHandlerNode) {
                        this.createEdge(node, target, JumpType.LeaveTry);
                    }
                    else {
                        this.createEdge(node, target, JumpType.Normal);
                    }
                    return;
                }
                if (handlerNode.getNodeType() == ControlFlowNodeType.FinallyHandler) {
                    if (handler.getTryBlock().contains(jump)) {
                        this.createEdge(node, target, JumpType.LeaveTry);
                    }
                    else {
                        ControlFlowNode parentHandler;
                        for (parentHandler = this.findParentExceptionHandlerNode(handlerNode); parentHandler != handlerNode && parentHandler.getNodeType() == ControlFlowNodeType.CatchHandler; parentHandler = this.findParentExceptionHandlerNode(parentHandler)) {}
                        if (parentHandler.getNodeType() == ControlFlowNodeType.FinallyHandler && !parentHandler.getExceptionHandler().getTryBlock().contains(target)) {
                            this.createEdge(node, target, JumpType.LeaveTry);
                        }
                        else {
                            this.createEdge(node, handlerNode.getEndFinallyNode(), JumpType.Normal);
                            this.createEdge(handlerNode.getEndFinallyNode(), target, JumpType.Normal);
                        }
                    }
                    return;
                }
                this.createEdge(node, target, JumpType.Normal);
                return;
            }
        }
        this.createEdge(node, target, JumpType.Normal);
    }
    
    private void createReturnControlFlow(final ControlFlowNode node, final Instruction end) {
        this.createEdge(node, this._regularExit, JumpType.Normal);
    }
    
    private void transformLeaveEdges() {
        final int n = this._nodes.size();
        for (int i = n - 1; i >= 0; --i) {
            final ControlFlowNode node = this._nodes.get(i);
            final Instruction end = node.getEnd();
            if (end != null && !node.getOutgoing().isEmpty()) {
                for (final ControlFlowEdge edge : node.getOutgoing()) {
                    if (edge.getType() == JumpType.LeaveTry) {
                        assert end.getOpCode().isBranch();
                        final ControlFlowNode handlerBlock = this.findInnermostHandlerBlock(end.getOffset());
                        ControlFlowNode finallyBlock = this.findInnermostFinallyHandlerNode(end.getOffset());
                        if (handlerBlock != finallyBlock) {
                            final ExceptionHandler handler = handlerBlock.getExceptionHandler();
                            final ControlFlowNode adjacentFinally = this.findInnermostFinallyHandlerNode(handler.getTryBlock().getLastInstruction().getOffset());
                            if (finallyBlock.getNodeType() != ControlFlowNodeType.FinallyHandler || finallyBlock != adjacentFinally) {
                                finallyBlock = adjacentFinally;
                            }
                        }
                        final ControlFlowNode target = edge.getTarget();
                        target.getIncoming().remove(edge);
                        node.getOutgoing().remove(edge);
                        if (finallyBlock.getNodeType() == ControlFlowNodeType.ExceptionalExit) {
                            this.createEdge(node, finallyBlock, JumpType.Normal);
                        }
                        else {
                            assert finallyBlock.getNodeType() == ControlFlowNodeType.FinallyHandler;
                            Instruction targetAddress = target.getStart();
                            if (targetAddress == null && target.getExceptionHandler() != null) {
                                targetAddress = target.getExceptionHandler().getHandlerBlock().getFirstInstruction();
                            }
                            if (finallyBlock.getExceptionHandler().getHandlerBlock().contains(end)) {
                                this.createEdge(node, finallyBlock.getEndFinallyNode(), JumpType.Normal);
                            }
                            else {
                                this.createEdge(node, finallyBlock, JumpType.Normal);
                            }
                            if (targetAddress != null) {
                                while (true) {
                                    ControlFlowNode parentHandler;
                                    for (parentHandler = this.findParentExceptionHandlerNode(finallyBlock); parentHandler.getNodeType() == ControlFlowNodeType.CatchHandler && !parentHandler.getExceptionHandler().getTryBlock().contains(targetAddress); parentHandler = this.findParentExceptionHandlerNode(finallyBlock)) {
                                        parentHandler = this.findInnermostFinallyHandlerNode(parentHandler.getExceptionHandler().getTryBlock().getLastInstruction().getOffset());
                                        if (parentHandler == finallyBlock) {}
                                    }
                                    if (parentHandler.getNodeType() != ControlFlowNodeType.FinallyHandler) {
                                        break;
                                    }
                                    if (parentHandler.getExceptionHandler().getTryBlock().contains(targetAddress)) {
                                        break;
                                    }
                                    this.createEdge(finallyBlock.getEndFinallyNode(), parentHandler, JumpType.EndFinally);
                                    finallyBlock = parentHandler;
                                }
                            }
                            if (finallyBlock == target) {
                                continue;
                            }
                            this.createEdge(finallyBlock.getEndFinallyNode(), target, JumpType.EndFinally);
                            this.createEdge(this.findNode(finallyBlock.getExceptionHandler().getHandlerBlock().getLastInstruction()), finallyBlock.getEndFinallyNode(), JumpType.Normal);
                        }
                    }
                }
            }
        }
    }
    
    private void copyFinallyBlocksIntoLeaveEdges() {
        final int n = this._nodes.size();
        for (int i = n - 1; i >= 0; --i) {
            final ControlFlowNode node = this._nodes.get(i);
            final Instruction end = node.getEnd();
            if (end != null && node.getOutgoing().size() == 1 && node.getOutgoing().get(0).getType() == JumpType.LeaveTry) {
                assert end.getOpCode() == OpCode.GOTO_W;
                final ControlFlowEdge edge = node.getOutgoing().get(0);
                final ControlFlowNode target = edge.getTarget();
                target.getIncoming().remove(edge);
                node.getOutgoing().clear();
                final ControlFlowNode handler = this.findInnermostExceptionHandlerNode(end.getEndOffset());
                assert handler.getNodeType() == ControlFlowNodeType.FinallyHandler;
                final ControlFlowNode copy = this.copyFinallySubGraph(handler, handler.getEndFinallyNode(), target);
                this.createEdge(node, copy, JumpType.Normal);
            }
        }
    }
    
    private ControlFlowNode copyFinallySubGraph(final ControlFlowNode start, final ControlFlowNode end, final ControlFlowNode newEnd) {
        return new CopyFinallySubGraphLogic(start, end, newEnd).copyFinallySubGraph();
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
    
    private static boolean isNarrower(final InstructionBlock block, final InstructionBlock anchor) {
        if (block == null || anchor == null) {
            return false;
        }
        final Instruction start = block.getFirstInstruction();
        final Instruction anchorStart = anchor.getFirstInstruction();
        final Instruction end = block.getLastInstruction();
        final Instruction anchorEnd = anchor.getLastInstruction();
        if (start.getOffset() > anchorStart.getOffset()) {
            return end.getOffset() < anchorEnd.getEndOffset();
        }
        return start.getOffset() == anchorStart.getOffset() && end.getOffset() < anchorEnd.getOffset();
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
    
    private ControlFlowNode findInnermostExceptionHandlerNode(final int offset) {
        final ExceptionHandler handler = this.findInnermostExceptionHandler(offset);
        if (handler == null) {
            return this._exceptionalExit;
        }
        for (final ControlFlowNode node : this._nodes) {
            if (node.getExceptionHandler() == handler && node.getCopyFrom() == null) {
                return node;
            }
        }
        throw new IllegalStateException("Could not find node for exception handler!");
    }
    
    private ControlFlowNode findInnermostFinallyHandlerNode(final int offset) {
        final ExceptionHandler handler = this.findInnermostFinallyHandler(offset);
        if (handler == null) {
            return this._exceptionalExit;
        }
        for (final ControlFlowNode node : this._nodes) {
            if (node.getExceptionHandler() == handler && node.getCopyFrom() == null) {
                return node;
            }
        }
        throw new IllegalStateException("Could not find node for exception handler!");
    }
    
    private int getInstructionIndex(final Instruction instruction) {
        final int index = Arrays.binarySearch(this._offsets, instruction.getOffset());
        assert index >= 0;
        return index;
    }
    
    private ControlFlowNode findNode(final Instruction instruction) {
        final int offset = instruction.getOffset();
        for (final ControlFlowNode node : this._nodes) {
            if (node.getNodeType() != ControlFlowNodeType.Normal) {
                continue;
            }
            if (offset >= node.getStart().getOffset() && offset < node.getEnd().getEndOffset()) {
                return node;
            }
        }
        return null;
    }
    
    private ExceptionHandler findInnermostExceptionHandler(final int offsetInTryBlock) {
        ExceptionHandler result = null;
        for (final ExceptionHandler handler : this._exceptionHandlers) {
            final InstructionBlock tryBlock = handler.getTryBlock();
            if (tryBlock.getFirstInstruction().getOffset() <= offsetInTryBlock && offsetInTryBlock < tryBlock.getLastInstruction().getEndOffset() && (result == null || isNarrower(handler, result))) {
                result = handler;
            }
        }
        return result;
    }
    
    private ExceptionHandler findInnermostFinallyHandler(final int offsetInTryBlock) {
        ExceptionHandler result = null;
        for (final ExceptionHandler handler : this._exceptionHandlers) {
            if (!handler.isFinally()) {
                continue;
            }
            final InstructionBlock tryBlock = handler.getTryBlock();
            if (tryBlock.getFirstInstruction().getOffset() > offsetInTryBlock || offsetInTryBlock >= tryBlock.getLastInstruction().getEndOffset() || (result != null && !isNarrower(handler, result))) {
                continue;
            }
            result = handler;
        }
        return result;
    }
    
    private ControlFlowNode findInnermostHandlerBlock(final int instructionOffset) {
        return this.findInnermostHandlerBlock(instructionOffset, false);
    }
    
    private ControlFlowNode findInnermostFinallyBlock(final int instructionOffset) {
        return this.findInnermostHandlerBlock(instructionOffset, true);
    }
    
    private ControlFlowNode findInnermostHandlerBlock(final int instructionOffset, final boolean finallyOnly) {
        ExceptionHandler result = null;
        InstructionBlock resultBlock = null;
        for (final ExceptionHandler handler : this._exceptionHandlers) {
            if (finallyOnly && handler.isCatch()) {
                continue;
            }
            final InstructionBlock handlerBlock = handler.getHandlerBlock();
            if (handlerBlock.getFirstInstruction().getOffset() > instructionOffset || instructionOffset >= handlerBlock.getLastInstruction().getEndOffset() || (resultBlock != null && !isNarrower(handler.getHandlerBlock(), resultBlock))) {
                continue;
            }
            result = handler;
            resultBlock = handlerBlock;
        }
        final ControlFlowNode innerMost = finallyOnly ? this.findInnermostExceptionHandlerNode(instructionOffset) : this.findInnermostFinallyHandlerNode(instructionOffset);
        final ExceptionHandler innerHandler = innerMost.getExceptionHandler();
        final InstructionBlock innerBlock = (innerHandler != null) ? innerHandler.getTryBlock() : null;
        if (innerBlock != null && (resultBlock == null || isNarrower(innerBlock, resultBlock))) {
            result = innerHandler;
        }
        if (result == null) {
            return this._exceptionalExit;
        }
        for (final ControlFlowNode node : this._nodes) {
            if (node.getExceptionHandler() == result && node.getCopyFrom() == null) {
                return node;
            }
        }
        throw new IllegalStateException("Could not find innermost handler block!");
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
        for (final ControlFlowEdge existingEdge : fromNode.getOutgoing()) {
            if (existingEdge.getSource() == fromNode && existingEdge.getTarget() == toNode && existingEdge.getType() == type) {
                return existingEdge;
            }
        }
        fromNode.getOutgoing().add(edge);
        toNode.getIncoming().add(edge);
        return edge;
    }
    
    private static List<ExceptionHandler> coalesceExceptionHandlers(final List<ExceptionHandler> handlers) {
        final ArrayList<ExceptionHandler> copy = new ArrayList<ExceptionHandler>(handlers);
        return copy;
    }
    
    static /* synthetic */ List access$0(final ControlFlowGraphBuilder param_0) {
        return param_0._nodes;
    }
    
    static /* synthetic */ ControlFlowEdge access$1(final ControlFlowGraphBuilder param_0, final ControlFlowNode param_1, final ControlFlowNode param_2, final JumpType param_3) {
        return param_0.createEdge(param_1, param_2, param_3);
    }
    
    private final class CopyFinallySubGraphLogic
    {
        final Map<ControlFlowNode, ControlFlowNode> oldToNew;
        final ControlFlowNode start;
        final ControlFlowNode end;
        final ControlFlowNode newEnd;
        private static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$assembler$flowanalysis$ControlFlowNodeType;
        
        CopyFinallySubGraphLogic(final ControlFlowNode start, final ControlFlowNode end, final ControlFlowNode newEnd) {
            super();
            this.oldToNew = new IdentityHashMap<ControlFlowNode, ControlFlowNode>();
            this.start = start;
            this.end = end;
            this.newEnd = newEnd;
        }
        
        final ControlFlowNode copyFinallySubGraph() {
            for (final ControlFlowNode node : this.end.getPredecessors()) {
                this.collectNodes(node);
            }
            for (final ControlFlowNode old : this.oldToNew.keySet()) {
                this.reconstructEdges(old, this.oldToNew.get(old));
            }
            return this.getNew(this.start);
        }
        
        private void collectNodes(final ControlFlowNode node) {
            if (node == this.end || node == this.newEnd) {
                throw new IllegalStateException("Unexpected cycle involving finally constructs!");
            }
            if (this.oldToNew.containsKey(node)) {
                return;
            }
            final int newBlockIndex = ControlFlowGraphBuilder.access$0(ControlFlowGraphBuilder.this).size();
            ControlFlowNode copy = null;
            switch ($SWITCH_TABLE$com$strobel$assembler$flowanalysis$ControlFlowNodeType()[node.getNodeType().ordinal()]) {
                case 1: {
                    copy = new ControlFlowNode(newBlockIndex, node.getStart(), node.getEnd());
                    break;
                }
                case 6: {
                    copy = new ControlFlowNode(newBlockIndex, node.getExceptionHandler(), node.getEndFinallyNode());
                    break;
                }
                default: {
                    throw ContractUtils.unsupported();
                }
            }
            copy.setCopyFrom(node);
            ControlFlowGraphBuilder.access$0(ControlFlowGraphBuilder.this).add(copy);
            this.oldToNew.put(node, copy);
            if (node != this.start) {
                for (final ControlFlowNode predecessor : node.getPredecessors()) {
                    this.collectNodes(predecessor);
                }
            }
        }
        
        private void reconstructEdges(final ControlFlowNode oldNode, final ControlFlowNode newNode) {
            for (final ControlFlowEdge oldEdge : oldNode.getOutgoing()) {
                ControlFlowGraphBuilder.access$1(ControlFlowGraphBuilder.this, newNode, this.getNew(oldEdge.getTarget()), oldEdge.getType());
            }
        }
        
        private ControlFlowNode getNew(final ControlFlowNode oldNode) {
            if (oldNode == this.end) {
                return this.newEnd;
            }
            final ControlFlowNode newNode = this.oldToNew.get(oldNode);
            return (newNode != null) ? newNode : oldNode;
        }
        
        static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$assembler$flowanalysis$ControlFlowNodeType() {
            final int[] loc_0 = CopyFinallySubGraphLogic.$SWITCH_TABLE$com$strobel$assembler$flowanalysis$ControlFlowNodeType;
            if (loc_0 != null) {
                return loc_0;
            }
            final int[] loc_1 = new int[ControlFlowNodeType.values().length];
            try {
                loc_1[ControlFlowNodeType.CatchHandler.ordinal()] = 5;
            }
            catch (NoSuchFieldError loc_2) {}
            try {
                loc_1[ControlFlowNodeType.EndFinally.ordinal()] = 7;
            }
            catch (NoSuchFieldError loc_3) {}
            try {
                loc_1[ControlFlowNodeType.EntryPoint.ordinal()] = 2;
            }
            catch (NoSuchFieldError loc_4) {}
            try {
                loc_1[ControlFlowNodeType.ExceptionalExit.ordinal()] = 4;
            }
            catch (NoSuchFieldError loc_5) {}
            try {
                loc_1[ControlFlowNodeType.FinallyHandler.ordinal()] = 6;
            }
            catch (NoSuchFieldError loc_6) {}
            try {
                loc_1[ControlFlowNodeType.Normal.ordinal()] = 1;
            }
            catch (NoSuchFieldError loc_7) {}
            try {
                loc_1[ControlFlowNodeType.RegularExit.ordinal()] = 3;
            }
            catch (NoSuchFieldError loc_8) {}
            return CopyFinallySubGraphLogic.$SWITCH_TABLE$com$strobel$assembler$flowanalysis$ControlFlowNodeType = loc_1;
        }
    }
}
