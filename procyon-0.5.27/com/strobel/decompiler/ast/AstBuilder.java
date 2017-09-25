package com.strobel.decompiler.ast;

import java.util.logging.*;
import com.strobel.annotations.*;
import com.strobel.functions.*;
import com.strobel.core.*;
import com.strobel.assembler.metadata.*;
import java.util.*;
import com.strobel.assembler.ir.*;
import com.strobel.decompiler.*;
import com.strobel.assembler.flowanalysis.*;

public final class AstBuilder
{
    private static final Logger LOG;
    private static final AstCode[] CODES;
    private static final StackSlot[] EMPTY_STACK;
    private static final ByteCode[] EMPTY_DEFINITIONS;
    private final Map<ExceptionHandler, ByteCode> _loadExceptions;
    private final Set<Instruction> _removed;
    private Map<Instruction, Instruction> _originalInstructionMap;
    private ControlFlowGraph _cfg;
    private InstructionCollection _instructions;
    private List<ExceptionHandler> _exceptionHandlers;
    private MethodBody _body;
    private boolean _optimize;
    private DecompilerContext _context;
    private CoreMetadataFactory _factory;
    private static final Predicate<Node> NOT_A_LABEL_OR_NOP;
    private static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$assembler$ir$OperandType;
    private static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$assembler$metadata$JvmType;
    private static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$assembler$ir$FrameValueType;
    private static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$decompiler$ast$AstCode;
    
    static {
        LOG = Logger.getLogger(AstBuilder.class.getSimpleName());
        CODES = AstCode.values();
        EMPTY_STACK = new StackSlot[0];
        EMPTY_DEFINITIONS = new ByteCode[0];
        NOT_A_LABEL_OR_NOP = new Predicate<Node>() {
            @Override
            public boolean test(final Node node) {
                return !(node instanceof Label) && !PatternMatching.match(node, AstCode.Nop);
            }
        };
    }
    
    public AstBuilder() {
        super();
        this._loadExceptions = new LinkedHashMap<ExceptionHandler, ByteCode>();
        this._removed = new LinkedHashSet<Instruction>();
    }
    
    public static List<Node> build(final MethodBody body, final boolean optimize, final DecompilerContext context) {
        final AstBuilder builder = new AstBuilder();
        builder._body = VerifyArgument.notNull(body, "body");
        builder._optimize = optimize;
        builder._context = VerifyArgument.notNull(context, "context");
        if (AstBuilder.LOG.isLoggable(Level.FINE)) {
            AstBuilder.LOG.fine(String.format("Beginning bytecode AST construction for %s:%s...", body.getMethod().getFullName(), body.getMethod().getSignature()));
        }
        if (body.getInstructions().isEmpty()) {
            return Collections.emptyList();
        }
        builder._instructions = copyInstructions(body.getInstructions());
        final InstructionCollection oldInstructions = body.getInstructions();
        final InstructionCollection newInstructions = builder._instructions;
        builder._originalInstructionMap = new IdentityHashMap<Instruction, Instruction>();
        for (int i = 0; i < newInstructions.size(); ++i) {
            builder._originalInstructionMap.put(newInstructions.get(i), oldInstructions.get(i));
        }
        Collections.sort(builder._exceptionHandlers = remapHandlers(body.getExceptionHandlers(), builder._instructions));
        builder.removeGetClassCallsForInvokeDynamic();
        builder.pruneExceptionHandlers();
        FinallyInlining.run(builder._body, builder._instructions, builder._exceptionHandlers, builder._removed);
        builder.inlineSubroutines();
        (builder._cfg = ControlFlowGraphBuilder.build(builder._instructions, builder._exceptionHandlers)).computeDominance();
        builder._cfg.computeDominanceFrontier();
        AstBuilder.LOG.fine("Performing stack analysis...");
        final List<ByteCode> byteCode = builder.performStackAnalysis();
        AstBuilder.LOG.fine("Creating bytecode AST...");
        final List<Node> ast = builder.convertToAst(byteCode, new LinkedHashSet<ExceptionHandler>(builder._exceptionHandlers), 0, new MutableInteger(byteCode.size()));
        if (AstBuilder.LOG.isLoggable(Level.FINE)) {
            AstBuilder.LOG.fine(String.format("Finished bytecode AST construction for %s:%s.", body.getMethod().getFullName(), body.getMethod().getSignature()));
        }
        return ast;
    }
    
    private static boolean isGetClassInvocation(final Instruction p) {
        return p != null && p.getOpCode() == OpCode.INVOKEVIRTUAL && p.getOperand(0).getParameters().isEmpty() && StringUtilities.equals(p.getOperand(0).getName(), "getClass");
    }
    
    private void removeGetClassCallsForInvokeDynamic() {
        for (final Instruction i : this._instructions) {
            if (i.getOpCode() != OpCode.INVOKEDYNAMIC) {
                continue;
            }
            final Instruction p1 = i.getPrevious();
            if (p1 == null) {
                continue;
            }
            if (p1.getOpCode() != OpCode.POP) {
                continue;
            }
            final Instruction p2 = p1.getPrevious();
            if (p2 == null) {
                continue;
            }
            if (!isGetClassInvocation(p2)) {
                continue;
            }
            final Instruction p3 = p2.getPrevious();
            if (p3 == null) {
                continue;
            }
            if (p3.getOpCode() != OpCode.DUP) {
                continue;
            }
            p1.setOpCode(OpCode.NOP);
            p1.setOperand(null);
            p2.setOpCode(OpCode.NOP);
            p2.setOperand(null);
            p3.setOpCode(OpCode.NOP);
            p3.setOperand(null);
        }
    }
    
    private void inlineSubroutines() {
        AstBuilder.LOG.fine("Inlining subroutines...");
        final List<SubroutineInfo> subroutines = this.findSubroutines();
        if (subroutines.isEmpty()) {
            return;
        }
        final List<ExceptionHandler> handlers = this._exceptionHandlers;
        final Set<ExceptionHandler> originalHandlers = new HashSet<ExceptionHandler>(handlers);
        final List<SubroutineInfo> inlinedSubroutines = new ArrayList<SubroutineInfo>();
        final Set<Instruction> instructionsToKeep = new HashSet<Instruction>();
        for (final SubroutineInfo subroutine : subroutines) {
            if (this.callsOtherSubroutine(subroutine, subroutines)) {
                continue;
            }
            boolean fullyInlined = true;
            for (final Instruction reference : subroutine.liveReferences) {
                fullyInlined &= this.inlineSubroutine(subroutine, reference);
            }
            for (final Instruction p : subroutine.deadReferences) {
                p.setOpCode(OpCode.NOP);
                p.setOperand(null);
                this._removed.add(p);
            }
            if (fullyInlined) {
                inlinedSubroutines.add(subroutine);
            }
            else {
                for (final ControlFlowNode node : subroutine.contents) {
                    for (Instruction p2 = node.getStart(); p2 != null && p2.getOffset() < node.getStart().getEndOffset(); p2 = p2.getNext()) {
                        instructionsToKeep.add(p2);
                    }
                }
            }
        }
        for (final SubroutineInfo subroutine : inlinedSubroutines) {
            for (Instruction p3 = subroutine.start; p3 != null && p3.getOffset() < subroutine.end.getEndOffset(); p3 = p3.getNext()) {
                if (!instructionsToKeep.contains(p3)) {
                    p3.setOpCode(OpCode.NOP);
                    p3.setOperand(null);
                    this._removed.add(p3);
                }
            }
            for (final ExceptionHandler handler : subroutine.containedHandlers) {
                if (originalHandlers.contains(handler)) {
                    handlers.remove(handler);
                }
            }
        }
    }
    
    private boolean inlineSubroutine(final SubroutineInfo subroutine, final Instruction reference) {
        if (!subroutine.start.getOpCode().isStore()) {
            return false;
        }
        final InstructionCollection instructions = this._instructions;
        final Map<Instruction, Instruction> originalInstructionMap = this._originalInstructionMap;
        final boolean nonEmpty = subroutine.start != subroutine.end && subroutine.start.getNext() != subroutine.end;
        if (nonEmpty) {
            final int jumpIndex = instructions.indexOf(reference);
            final List<Instruction> originalContents = new ArrayList<Instruction>();
            for (final ControlFlowNode node : subroutine.contents) {
                for (Instruction p = node.getStart(); p != null && p.getOffset() < node.getEnd().getEndOffset(); p = p.getNext()) {
                    originalContents.add(p);
                }
            }
            final Map<Instruction, Instruction> remappedJumps = new IdentityHashMap<Instruction, Instruction>();
            final List<Instruction> contents = copyInstructions(originalContents);
            for (int i = 0, n = originalContents.size(); i < n; ++i) {
                remappedJumps.put(originalContents.get(i), contents.get(i));
                originalInstructionMap.put(contents.get(i), mappedInstruction(originalInstructionMap, originalContents.get(i)));
            }
            final Instruction newStart = mappedInstruction(remappedJumps, subroutine.start);
            final Instruction newEnd = (reference.getNext() != null) ? reference.getNext() : mappedInstruction(remappedJumps, subroutine.end).getPrevious();
            for (final ControlFlowNode exitNode : subroutine.exitNodes) {
                final Instruction newExit = mappedInstruction(remappedJumps, exitNode.getEnd());
                if (newExit != null) {
                    newExit.setOpCode(OpCode.GOTO);
                    newExit.setOperand(newEnd);
                    remappedJumps.put(newExit, newEnd);
                }
            }
            newStart.setOpCode(OpCode.NOP);
            newStart.setOperand(null);
            instructions.addAll(jumpIndex, CollectionUtilities.toList(contents));
            if (newStart != CollectionUtilities.first(contents)) {
                instructions.add(jumpIndex, new Instruction(OpCode.GOTO, newStart));
            }
            instructions.remove(reference);
            instructions.recomputeOffsets();
            remappedJumps.put(reference, CollectionUtilities.first(contents));
            remappedJumps.put(subroutine.end, newEnd);
            remappedJumps.put(subroutine.start, newStart);
            this.remapJumps(Collections.singletonMap(reference, newStart));
            this.remapHandlersForInlinedSubroutine(reference, CollectionUtilities.first(contents), CollectionUtilities.last(contents));
            this.duplicateHandlersForInlinedSubroutine(subroutine, remappedJumps);
        }
        else {
            reference.setOpCode(OpCode.NOP);
            reference.setOperand(OpCode.NOP);
        }
        return true;
    }
    
    private void remapHandlersForInlinedSubroutine(final Instruction jump, final Instruction start, final Instruction end) {
        final List<ExceptionHandler> handlers = this._exceptionHandlers;
        for (int i = 0; i < handlers.size(); ++i) {
            final ExceptionHandler handler = handlers.get(i);
            final InstructionBlock oldTry = handler.getTryBlock();
            final InstructionBlock oldHandler = handler.getHandlerBlock();
            InstructionBlock newTryBlock;
            if (oldTry.getFirstInstruction() == jump || oldTry.getLastInstruction() == jump) {
                newTryBlock = new InstructionBlock((oldTry.getFirstInstruction() == jump) ? start : oldTry.getFirstInstruction(), (oldTry.getLastInstruction() == jump) ? end : oldTry.getLastInstruction());
            }
            else {
                newTryBlock = oldTry;
            }
            InstructionBlock newHandlerBlock;
            if (oldHandler.getFirstInstruction() == jump || oldHandler.getLastInstruction() == jump) {
                newHandlerBlock = new InstructionBlock((oldHandler.getFirstInstruction() == jump) ? start : oldHandler.getFirstInstruction(), (oldHandler.getLastInstruction() == jump) ? end : oldHandler.getLastInstruction());
            }
            else {
                newHandlerBlock = oldHandler;
            }
            if (newTryBlock != oldTry || newHandlerBlock != oldHandler) {
                if (handler.isCatch()) {
                    handlers.set(i, ExceptionHandler.createCatch(newTryBlock, newHandlerBlock, handler.getCatchType()));
                }
                else {
                    handlers.set(i, ExceptionHandler.createFinally(newTryBlock, newHandlerBlock));
                }
            }
        }
    }
    
    private void duplicateHandlersForInlinedSubroutine(final SubroutineInfo subroutine, final Map<Instruction, Instruction> oldToNew) {
        final List<ExceptionHandler> handlers = this._exceptionHandlers;
        for (final ExceptionHandler handler : subroutine.containedHandlers) {
            final InstructionBlock oldTry = handler.getTryBlock();
            final InstructionBlock oldHandler = handler.getHandlerBlock();
            final Instruction newTryStart = mappedInstruction(oldToNew, oldTry.getFirstInstruction());
            final Instruction newTryEnd = mappedInstruction(oldToNew, oldTry.getLastInstruction());
            final Instruction newHandlerStart = mappedInstruction(oldToNew, oldHandler.getFirstInstruction());
            final Instruction newHandlerEnd = mappedInstruction(oldToNew, oldHandler.getLastInstruction());
            InstructionBlock newTryBlock;
            if (newTryStart != null || newTryEnd != null) {
                newTryBlock = new InstructionBlock((newTryStart != null) ? newTryStart : oldTry.getFirstInstruction(), (newTryEnd != null) ? newTryEnd : oldTry.getLastInstruction());
            }
            else {
                newTryBlock = oldTry;
            }
            InstructionBlock newHandlerBlock;
            if (newHandlerStart != null || newHandlerEnd != null) {
                newHandlerBlock = new InstructionBlock((newHandlerStart != null) ? newHandlerStart : oldHandler.getFirstInstruction(), (newHandlerEnd != null) ? newHandlerEnd : oldHandler.getLastInstruction());
            }
            else {
                newHandlerBlock = oldHandler;
            }
            if (newTryBlock != oldTry || newHandlerBlock != oldHandler) {
                handlers.add(handler.isCatch() ? ExceptionHandler.createCatch(newTryBlock, newHandlerBlock, handler.getCatchType()) : ExceptionHandler.createFinally(newTryBlock, newHandlerBlock));
            }
        }
    }
    
    private void remapJumps(final Map<Instruction, Instruction> remappedJumps) {
        for (final Instruction instruction : this._instructions) {
            if (instruction.hasLabel()) {
                instruction.getLabel().setIndex(instruction.getOffset());
            }
            if (instruction.getOperandCount() == 0) {
                continue;
            }
            final Object operand = instruction.getOperand(0);
            if (operand instanceof Instruction) {
                final Instruction oldTarget = (Instruction)operand;
                final Instruction newTarget = mappedInstruction(remappedJumps, oldTarget);
                if (newTarget == null) {
                    continue;
                }
                if (newTarget == instruction) {
                    instruction.setOpCode(OpCode.NOP);
                    instruction.setOperand(null);
                }
                else {
                    instruction.setOperand(newTarget);
                    if (newTarget.hasLabel()) {
                        continue;
                    }
                    newTarget.setLabel(new com.strobel.assembler.metadata.Label(newTarget.getOffset()));
                }
            }
            else {
                if (!(operand instanceof SwitchInfo)) {
                    continue;
                }
                final SwitchInfo oldOperand = (SwitchInfo)operand;
                final Instruction oldDefault = oldOperand.getDefaultTarget();
                final Instruction newDefault = mappedInstruction(remappedJumps, oldDefault);
                if (newDefault != null && !newDefault.hasLabel()) {
                    newDefault.setLabel(new com.strobel.assembler.metadata.Label(newDefault.getOffset()));
                }
                final Instruction[] oldTargets = oldOperand.getTargets();
                Instruction[] newTargets = null;
                for (int i = 0; i < oldTargets.length; ++i) {
                    final Instruction newTarget2 = mappedInstruction(remappedJumps, oldTargets[i]);
                    if (newTarget2 != null) {
                        if (newTargets == null) {
                            newTargets = Arrays.copyOf(oldTargets, oldTargets.length);
                        }
                        newTargets[i] = newTarget2;
                        if (!newTarget2.hasLabel()) {
                            newTarget2.setLabel(new com.strobel.assembler.metadata.Label(newTarget2.getOffset()));
                        }
                    }
                }
                if (newDefault == null && newTargets == null) {
                    continue;
                }
                final SwitchInfo newOperand = new SwitchInfo(oldOperand.getKeys(), (newDefault != null) ? newDefault : oldDefault, (newTargets != null) ? newTargets : oldTargets);
                instruction.setOperand(newOperand);
            }
        }
    }
    
    private boolean callsOtherSubroutine(final SubroutineInfo subroutine, final List<SubroutineInfo> subroutines) {
        return CollectionUtilities.any(subroutines, new Predicate<SubroutineInfo>() {
            @Override
            public boolean test(final SubroutineInfo info) {
                return info != subroutine && CollectionUtilities.any(info.liveReferences, new Predicate<Instruction>() {
                    @Override
                    public boolean test(final Instruction p) {
                        return p.getOffset() >= subroutine.start.getOffset() && p.getOffset() < subroutine.end.getEndOffset();
                    }
                }) && !subroutine.contents.containsAll(info.contents);
            }
        });
    }
    
    private List<SubroutineInfo> findSubroutines() {
        final InstructionCollection instructions = this._instructions;
        if (instructions.isEmpty()) {
            return Collections.emptyList();
        }
        Map<ExceptionHandler, Pair<Set<ControlFlowNode>, Set<ControlFlowNode>>> handlerContents = null;
        Map<Instruction, SubroutineInfo> subroutineMap = null;
        ControlFlowGraph cfg = null;
        for (Instruction p = CollectionUtilities.first((List<Instruction>)instructions); p != null; p = p.getNext()) {
            if (p.getOpCode().isJumpToSubroutine()) {
                final boolean isLive = !this._removed.contains(p);
                if (cfg == null) {
                    cfg = ControlFlowGraphBuilder.build(instructions, this._exceptionHandlers);
                    cfg.computeDominance();
                    cfg.computeDominanceFrontier();
                    subroutineMap = new IdentityHashMap<Instruction, SubroutineInfo>();
                    handlerContents = new IdentityHashMap<ExceptionHandler, Pair<Set<ControlFlowNode>, Set<ControlFlowNode>>>();
                    for (final ExceptionHandler handler : this._exceptionHandlers) {
                        final InstructionBlock tryBlock = handler.getTryBlock();
                        final InstructionBlock handlerBlock = handler.getHandlerBlock();
                        final Set<ControlFlowNode> tryNodes = findDominatedNodes(cfg, findNode(cfg, tryBlock.getFirstInstruction()), true, Collections.emptySet());
                        final Set<ControlFlowNode> handlerNodes = findDominatedNodes(cfg, findNode(cfg, handlerBlock.getFirstInstruction()), true, Collections.emptySet());
                        handlerContents.put(handler, Pair.create(tryNodes, handlerNodes));
                    }
                }
                final Instruction target = p.getOperand(0);
                if (!this._removed.contains(target)) {
                    SubroutineInfo info = subroutineMap.get(target);
                    if (info == null) {
                        final ControlFlowNode start = findNode(cfg, target);
                        final List<ControlFlowNode> contents = CollectionUtilities.toList(findDominatedNodes(cfg, start, true, Collections.emptySet()));
                        Collections.sort(contents);
                        subroutineMap.put(target, info = new SubroutineInfo(start, contents, cfg));
                        for (final ExceptionHandler handler2 : this._exceptionHandlers) {
                            final Pair<Set<ControlFlowNode>, Set<ControlFlowNode>> pair = handlerContents.get(handler2);
                            if (contents.containsAll(pair.getFirst()) && contents.containsAll(pair.getSecond())) {
                                info.containedHandlers.add(handler2);
                            }
                        }
                    }
                    if (isLive) {
                        info.liveReferences.add(p);
                    }
                    else {
                        info.deadReferences.add(p);
                    }
                }
            }
        }
        if (subroutineMap == null) {
            return Collections.emptyList();
        }
        final List<SubroutineInfo> subroutines = CollectionUtilities.toList(subroutineMap.values());
        Collections.sort(subroutines, new Comparator<SubroutineInfo>() {
            @Override
            public int compare(@NotNull final SubroutineInfo o1, @NotNull final SubroutineInfo o2) {
                if (o1.contents.containsAll(o2.contents)) {
                    return 1;
                }
                if (o2.contents.containsAll(o1.contents)) {
                    return -1;
                }
                return Integer.compare(o2.start.getOffset(), o1.start.getOffset());
            }
        });
        return subroutines;
    }
    
    private static ControlFlowNode findNode(final ControlFlowGraph cfg, final Instruction instruction) {
        final int offset = instruction.getOffset();
        for (final ControlFlowNode node : cfg.getNodes()) {
            if (node.getNodeType() != ControlFlowNodeType.Normal) {
                continue;
            }
            if (offset >= node.getStart().getOffset() && offset < node.getEnd().getEndOffset()) {
                return node;
            }
        }
        return null;
    }
    
    private static Set<ControlFlowNode> findDominatedNodes(final ControlFlowGraph cfg, final ControlFlowNode head, final boolean diveIntoHandlers, final Set<ControlFlowNode> terminals) {
        final Set<ControlFlowNode> visited = new LinkedHashSet<ControlFlowNode>();
        final ArrayDeque<ControlFlowNode> agenda = new ArrayDeque<ControlFlowNode>();
        final Set<ControlFlowNode> result = new LinkedHashSet<ControlFlowNode>();
        agenda.add(head);
        visited.add(head);
        while (!agenda.isEmpty()) {
            ControlFlowNode addNode = agenda.removeFirst();
            if (terminals.contains(addNode)) {
                continue;
            }
            if (diveIntoHandlers && addNode.getExceptionHandler() != null) {
                addNode = findNode(cfg, addNode.getExceptionHandler().getHandlerBlock().getFirstInstruction());
            }
            else if (diveIntoHandlers && addNode.getNodeType() == ControlFlowNodeType.EndFinally) {
                agenda.addAll((Collection<?>)addNode.getDominatorTreeChildren());
                continue;
            }
            if (addNode == null) {
                continue;
            }
            if (addNode.getNodeType() != ControlFlowNodeType.Normal) {
                continue;
            }
            if (!head.dominates(addNode) && !shouldIncludeExceptionalExit(cfg, head, addNode)) {
                continue;
            }
            if (!result.add(addNode)) {
                continue;
            }
            for (final ControlFlowNode successor : addNode.getSuccessors()) {
                if (visited.add(successor)) {
                    agenda.add(successor);
                }
            }
        }
        return result;
    }
    
    private static boolean shouldIncludeExceptionalExit(final ControlFlowGraph cfg, final ControlFlowNode head, final ControlFlowNode node) {
        if (node.getNodeType() != ControlFlowNodeType.Normal) {
            return false;
        }
        if (!node.getDominanceFrontier().contains(cfg.getExceptionalExit()) && !node.dominates(cfg.getExceptionalExit())) {
            final ControlFlowNode innermostHandlerNode = findInnermostExceptionHandlerNode(cfg, node.getEnd().getOffset(), false);
            if (innermostHandlerNode == null || !node.getDominanceFrontier().contains(innermostHandlerNode)) {
                return false;
            }
        }
        return head.getNodeType() == ControlFlowNodeType.Normal && node.getNodeType() == ControlFlowNodeType.Normal && node.getStart().getNext() == node.getEnd() && head.getStart().getOpCode().isStore() && node.getStart().getOpCode().isLoad() && node.getEnd().getOpCode() == OpCode.ATHROW && InstructionHelper.getLoadOrStoreSlot(head.getStart()) == InstructionHelper.getLoadOrStoreSlot(node.getStart());
    }
    
    private static ControlFlowNode findInnermostExceptionHandlerNode(final ControlFlowGraph cfg, final int offsetInTryBlock, final boolean finallyOnly) {
        ExceptionHandler result = null;
        ControlFlowNode resultNode = null;
        final List<ControlFlowNode> nodes = cfg.getNodes();
        for (int i = nodes.size() - 1; i >= 0; --i) {
            final ControlFlowNode node = nodes.get(i);
            final ExceptionHandler handler = node.getExceptionHandler();
            if (handler == null) {
                break;
            }
            if (!finallyOnly || !handler.isCatch()) {
                final InstructionBlock tryBlock = handler.getTryBlock();
                if (tryBlock.getFirstInstruction().getOffset() <= offsetInTryBlock && offsetInTryBlock < tryBlock.getLastInstruction().getEndOffset() && (result == null || tryBlock.getFirstInstruction().getOffset() > result.getTryBlock().getFirstInstruction().getOffset())) {
                    result = handler;
                    resultNode = node;
                }
            }
        }
        return (resultNode != null) ? resultNode : cfg.getExceptionalExit();
    }
    
    private static boolean opCodesMatch(final Instruction tail1, final Instruction tail2, final int count, final Function<Instruction, Instruction> previous) {
        int i = 0;
        if (tail1 == null || tail2 == null) {
            return false;
        }
        for (Instruction p1 = tail1, p2 = tail2; p1 != null && p2 != null && i < count; p1 = previous.apply(p1), p2 = previous.apply(p2), ++i) {
            final OpCode c1 = p1.getOpCode();
            final OpCode c2 = p2.getOpCode();
            if (c1.isLoad()) {
                if (!c2.isLoad() || c2.getStackBehaviorPush() != c1.getStackBehaviorPush()) {
                    return false;
                }
            }
            else if (c1.isStore()) {
                if (!c2.isStore() || c2.getStackBehaviorPop() != c1.getStackBehaviorPop()) {
                    return false;
                }
            }
            else if (c1 != p2.getOpCode()) {
                return false;
            }
            switch ($SWITCH_TABLE$com$strobel$assembler$ir$OperandType()[c1.getOperandType().ordinal()]) {
                case 4: {
                    if (!Objects.equals(p1.getOperand(1), p2.getOperand(1))) {
                        return false;
                    }
                }
                case 2:
                case 3: {
                    if (!Objects.equals(p1.getOperand(0), p2.getOperand(0))) {
                        return false;
                    }
                    break;
                }
                case 6:
                case 7: {
                    final MemberReference m1 = p1.getOperand(0);
                    final MemberReference m2 = p2.getOperand(0);
                    if (!StringUtilities.equals(m1.getFullName(), m2.getFullName()) || !StringUtilities.equals(m1.getErasedSignature(), m2.getErasedSignature())) {
                        return false;
                    }
                    break;
                }
                case 10:
                case 11:
                case 12:
                case 13:
                case 14: {
                    if (!Objects.equals(p1.getOperand(0), p2.getOperand(0))) {
                        return false;
                    }
                    break;
                }
                case 17:
                case 18: {
                    if (!Objects.equals(p1.getOperand(1), p2.getOperand(1))) {
                        return false;
                    }
                    break;
                }
            }
        }
        return i == count;
    }
    
    private static Map<Instruction, ControlFlowNode> createNodeMap(final ControlFlowGraph cfg) {
        final Map<Instruction, ControlFlowNode> nodeMap = new IdentityHashMap<Instruction, ControlFlowNode>();
        for (final ControlFlowNode node : cfg.getNodes()) {
            if (node.getNodeType() != ControlFlowNodeType.Normal) {
                continue;
            }
            for (Instruction p = node.getStart(); p != null && p.getOffset() < node.getEnd().getEndOffset(); p = p.getNext()) {
                nodeMap.put(p, node);
            }
        }
        return nodeMap;
    }
    
    private static List<ExceptionHandler> remapHandlers(final List<ExceptionHandler> handlers, final InstructionCollection instructions) {
        final List<ExceptionHandler> newHandlers = new ArrayList<ExceptionHandler>();
        for (final ExceptionHandler handler : handlers) {
            final InstructionBlock oldTry = handler.getTryBlock();
            final InstructionBlock oldHandler = handler.getHandlerBlock();
            final InstructionBlock newTry = new InstructionBlock(instructions.atOffset(oldTry.getFirstInstruction().getOffset()), instructions.atOffset(oldTry.getLastInstruction().getOffset()));
            final InstructionBlock newHandler = new InstructionBlock(instructions.atOffset(oldHandler.getFirstInstruction().getOffset()), instructions.atOffset(oldHandler.getLastInstruction().getOffset()));
            if (handler.isCatch()) {
                newHandlers.add(ExceptionHandler.createCatch(newTry, newHandler, handler.getCatchType()));
            }
            else {
                newHandlers.add(ExceptionHandler.createFinally(newTry, newHandler));
            }
        }
        return newHandlers;
    }
    
    private static InstructionCollection copyInstructions(final List<Instruction> instructions) {
        final InstructionCollection instructionsCopy = new InstructionCollection();
        final Map<Instruction, Instruction> oldToNew = new IdentityHashMap<Instruction, Instruction>();
        for (final Instruction instruction : instructions) {
            final Instruction copy = new Instruction(instruction.getOffset(), instruction.getOpCode());
            if (instruction.getOperandCount() > 1) {
                final Object[] operands = new Object[instruction.getOperandCount()];
                for (int i = 0; i < operands.length; ++i) {
                    operands[i] = instruction.getOperand(i);
                }
                copy.setOperand(operands);
            }
            else {
                copy.setOperand(instruction.getOperand(0));
            }
            copy.setLabel(instruction.getLabel());
            instructionsCopy.add(copy);
            oldToNew.put(instruction, copy);
        }
        for (final Instruction instruction : instructionsCopy) {
            if (!instruction.hasOperand()) {
                continue;
            }
            final Object operand = instruction.getOperand(0);
            if (operand instanceof Instruction) {
                instruction.setOperand(mappedInstruction(oldToNew, (Instruction)operand));
            }
            else {
                if (!(operand instanceof SwitchInfo)) {
                    continue;
                }
                final SwitchInfo oldOperand = (SwitchInfo)operand;
                final Instruction oldDefault = oldOperand.getDefaultTarget();
                final Instruction newDefault = mappedInstruction(oldToNew, oldDefault);
                final Instruction[] oldTargets = oldOperand.getTargets();
                final Instruction[] newTargets = new Instruction[oldTargets.length];
                for (int j = 0; j < newTargets.length; ++j) {
                    newTargets[j] = mappedInstruction(oldToNew, oldTargets[j]);
                }
                final SwitchInfo newOperand = new SwitchInfo(oldOperand.getKeys(), newDefault, newTargets);
                newOperand.setLowValue(oldOperand.getLowValue());
                newOperand.setHighValue(oldOperand.getHighValue());
                instruction.setOperand(newOperand);
            }
        }
        instructionsCopy.recomputeOffsets();
        return instructionsCopy;
    }
    
    private void pruneExceptionHandlers() {
        AstBuilder.LOG.fine("Pruning exception handlers...");
        final List<ExceptionHandler> handlers = this._exceptionHandlers;
        if (handlers.isEmpty()) {
            return;
        }
        this.removeSelfHandlingFinallyHandlers();
        this.removeEmptyCatchBlockBodies();
        this.trimAggressiveFinallyBlocks();
        this.trimAggressiveCatchBlocks();
        this.closeTryHandlerGaps();
        this.mergeSharedHandlers();
        this.alignFinallyBlocksWithSiblingCatchBlocks();
        this.ensureDesiredProtectedRanges();
        for (int i = 0; i < handlers.size(); ++i) {
            final ExceptionHandler handler = handlers.get(i);
            if (handler.isFinally()) {
                final InstructionBlock tryBlock = handler.getTryBlock();
                final List<ExceptionHandler> siblings = findHandlers(tryBlock, handlers);
                for (int j = 0; j < siblings.size(); ++j) {
                    final ExceptionHandler sibling = siblings.get(j);
                    if (sibling.isCatch() && j < siblings.size() - 1) {
                        final ExceptionHandler nextSibling = siblings.get(j + 1);
                        if (sibling.getHandlerBlock().getLastInstruction() != nextSibling.getHandlerBlock().getFirstInstruction().getPrevious()) {
                            final int index = handlers.indexOf(sibling);
                            handlers.set(index, ExceptionHandler.createCatch(sibling.getTryBlock(), new InstructionBlock(sibling.getHandlerBlock().getFirstInstruction(), nextSibling.getHandlerBlock().getFirstInstruction().getPrevious()), sibling.getCatchType()));
                            siblings.set(j, handlers.get(j));
                        }
                    }
                }
            }
        }
    Label_0493:
        for (int i = 0; i < handlers.size(); ++i) {
            final ExceptionHandler handler = handlers.get(i);
            if (handler.isFinally()) {
                final InstructionBlock tryBlock = handler.getTryBlock();
                final List<ExceptionHandler> siblings = findHandlers(tryBlock, handlers);
                for (final ExceptionHandler sibling2 : siblings) {
                    if (sibling2 != handler) {
                        if (sibling2.isFinally()) {
                            continue;
                        }
                        for (int k = 0; k < handlers.size(); ++k) {
                            final ExceptionHandler e = handlers.get(k);
                            if (e != handler && e != sibling2) {
                                if (e.isFinally()) {
                                    if (e.getTryBlock().getFirstInstruction() == sibling2.getHandlerBlock().getFirstInstruction() && e.getHandlerBlock().equals(handler.getHandlerBlock())) {
                                        handlers.remove(k);
                                        final int removeIndex = k--;
                                        if (removeIndex < i) {
                                            --i;
                                            continue Label_0493;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        for (int i = 0; i < handlers.size(); ++i) {
            final ExceptionHandler handler = handlers.get(i);
            if (handler.isFinally()) {
                final InstructionBlock tryBlock = handler.getTryBlock();
                final InstructionBlock handlerBlock = handler.getHandlerBlock();
                for (int j = 0; j < handlers.size(); ++j) {
                    final ExceptionHandler other = handlers.get(j);
                    if (other != handler && other.isFinally() && other.getHandlerBlock().equals(handlerBlock) && tryBlock.contains(other.getTryBlock()) && tryBlock.getLastInstruction() == other.getTryBlock().getLastInstruction()) {
                        handlers.remove(j);
                        if (j < i) {
                            --i;
                            break;
                        }
                        --j;
                    }
                }
            }
        }
        for (int i = 0; i < handlers.size(); ++i) {
            final ExceptionHandler handler = handlers.get(i);
            final InstructionBlock tryBlock = handler.getTryBlock();
            final ExceptionHandler firstHandler = findFirstHandler(tryBlock, handlers);
            final InstructionBlock firstHandlerBlock = firstHandler.getHandlerBlock();
            final Instruction firstAfterTry = tryBlock.getLastInstruction().getNext();
            final Instruction firstInHandler = firstHandlerBlock.getFirstInstruction();
            final Instruction lastBeforeHandler = firstInHandler.getPrevious();
            if (firstAfterTry != firstInHandler && firstAfterTry != null && lastBeforeHandler != null) {
                InstructionBlock newTryBlock = null;
                final FlowControl flowControl = lastBeforeHandler.getOpCode().getFlowControl();
                if (flowControl == FlowControl.Branch || (flowControl == FlowControl.Return && lastBeforeHandler.getOpCode() == OpCode.RETURN)) {
                    if (lastBeforeHandler == firstAfterTry) {
                        newTryBlock = new InstructionBlock(tryBlock.getFirstInstruction(), lastBeforeHandler);
                    }
                }
                else if ((flowControl == FlowControl.Throw || (flowControl == FlowControl.Return && lastBeforeHandler.getOpCode() != OpCode.RETURN)) && lastBeforeHandler.getPrevious() == firstAfterTry) {
                    newTryBlock = new InstructionBlock(tryBlock.getFirstInstruction(), lastBeforeHandler);
                }
                if (newTryBlock != null) {
                    final List<ExceptionHandler> siblings2 = findHandlers(tryBlock, handlers);
                    for (int l = 0; l < siblings2.size(); ++l) {
                        final ExceptionHandler sibling3 = siblings2.get(l);
                        final int index2 = handlers.indexOf(sibling3);
                        if (sibling3.isCatch()) {
                            handlers.set(index2, ExceptionHandler.createCatch(newTryBlock, sibling3.getHandlerBlock(), sibling3.getCatchType()));
                        }
                        else {
                            handlers.set(index2, ExceptionHandler.createFinally(newTryBlock, sibling3.getHandlerBlock()));
                        }
                    }
                }
            }
        }
        for (int i = 0; i < handlers.size(); ++i) {
            final ExceptionHandler handler = handlers.get(i);
            final InstructionBlock tryBlock = handler.getTryBlock();
            final InstructionBlock handlerBlock = handler.getHandlerBlock();
            if (handler.isFinally()) {
                final ExceptionHandler innermostHandler = this.findInnermostExceptionHandler(tryBlock.getFirstInstruction().getOffset(), handler);
                if (innermostHandler != null && innermostHandler != handler) {
                    if (!innermostHandler.isFinally()) {
                        for (int m = 0; m < handlers.size(); ++m) {
                            final ExceptionHandler sibling4 = handlers.get(m);
                            if (sibling4 != handler && sibling4 != innermostHandler && sibling4.getTryBlock().equals(handlerBlock) && sibling4.getHandlerBlock().equals(innermostHandler.getHandlerBlock())) {
                                handlers.remove(m);
                                if (m < i) {
                                    --i;
                                    break;
                                }
                                --m;
                            }
                        }
                    }
                }
            }
        }
    }
    
    private void removeEmptyCatchBlockBodies() {
        final List<ExceptionHandler> handlers = this._exceptionHandlers;
        for (int i = 0; i < handlers.size(); ++i) {
            final ExceptionHandler handler = handlers.get(i);
            if (handler.isCatch()) {
                final InstructionBlock catchBlock = handler.getHandlerBlock();
                final Instruction start = catchBlock.getFirstInstruction();
                final Instruction end = catchBlock.getLastInstruction();
                if (start == end) {
                    if (start.getOpCode().isStore()) {
                        end.setOpCode(OpCode.POP);
                        end.setOperand(null);
                        this._removed.add(end);
                    }
                }
            }
        }
    }
    
    private void ensureDesiredProtectedRanges() {
        final List<ExceptionHandler> handlers = this._exceptionHandlers;
        for (int i = 0; i < handlers.size(); ++i) {
            final ExceptionHandler handler = handlers.get(i);
            final InstructionBlock tryBlock = handler.getTryBlock();
            final List<ExceptionHandler> siblings = findHandlers(tryBlock, handlers);
            final ExceptionHandler firstSibling = CollectionUtilities.first(siblings);
            final InstructionBlock firstHandler = firstSibling.getHandlerBlock();
            final Instruction desiredEndTry = firstHandler.getFirstInstruction().getPrevious();
            for (int j = 0; j < siblings.size(); ++j) {
                ExceptionHandler sibling = siblings.get(j);
                if (handler.getTryBlock().getLastInstruction() != desiredEndTry) {
                    final int index = handlers.indexOf(sibling);
                    if (sibling.isCatch()) {
                        handlers.set(index, ExceptionHandler.createCatch(new InstructionBlock(tryBlock.getFirstInstruction(), desiredEndTry), sibling.getHandlerBlock(), sibling.getCatchType()));
                    }
                    else {
                        handlers.set(index, ExceptionHandler.createFinally(new InstructionBlock(tryBlock.getFirstInstruction(), desiredEndTry), sibling.getHandlerBlock()));
                    }
                    sibling = handlers.get(index);
                    siblings.set(j, sibling);
                }
            }
        }
    }
    
    private void alignFinallyBlocksWithSiblingCatchBlocks() {
        final List<ExceptionHandler> handlers = this._exceptionHandlers;
        for (int i = 0; i < handlers.size(); ++i) {
            final ExceptionHandler handler = handlers.get(i);
            if (!handler.isCatch()) {
                final InstructionBlock tryBlock = handler.getTryBlock();
                final InstructionBlock handlerBlock = handler.getHandlerBlock();
                for (int j = 0; j < handlers.size(); ++j) {
                    if (i != j) {
                        final ExceptionHandler other = handlers.get(j);
                        final InstructionBlock otherTry = other.getTryBlock();
                        final InstructionBlock otherHandler = other.getHandlerBlock();
                        if (other.isCatch() && otherHandler.getLastInstruction().getNext() == handlerBlock.getFirstInstruction() && otherTry.getFirstInstruction() == tryBlock.getFirstInstruction() && otherTry.getLastInstruction().getOffset() < tryBlock.getLastInstruction().getOffset() && tryBlock.getLastInstruction().getEndOffset() > otherHandler.getFirstInstruction().getOffset()) {
                            handlers.set(i, ExceptionHandler.createFinally(new InstructionBlock(tryBlock.getFirstInstruction(), otherHandler.getFirstInstruction().getPrevious()), handlerBlock));
                            --i;
                            break;
                        }
                    }
                }
            }
        }
    }
    
    private void mergeSharedHandlers() {
        final List<ExceptionHandler> handlers = this._exceptionHandlers;
        for (int i = 0; i < handlers.size(); ++i) {
            final ExceptionHandler handler = handlers.get(i);
            final List<ExceptionHandler> duplicates = findDuplicateHandlers(handler, handlers);
            for (int j = 0; j < duplicates.size() - 1; ++j) {
                final ExceptionHandler h1 = duplicates.get(j);
                final ExceptionHandler h2 = duplicates.get(1 + j);
                final InstructionBlock try1 = h1.getTryBlock();
                final InstructionBlock try2 = h2.getTryBlock();
                final Instruction head = try1.getLastInstruction().getNext();
                final Instruction tail = try2.getFirstInstruction().getPrevious();
                final int i2 = handlers.indexOf(h1);
                final int i3 = handlers.indexOf(h2);
                if (head != tail) {
                    if (h1.isCatch()) {
                        handlers.set(i2, ExceptionHandler.createCatch(new InstructionBlock(try1.getFirstInstruction(), try2.getLastInstruction()), h1.getHandlerBlock(), h1.getCatchType()));
                    }
                    else {
                        handlers.set(i2, ExceptionHandler.createFinally(new InstructionBlock(try1.getFirstInstruction(), try2.getLastInstruction()), h1.getHandlerBlock()));
                    }
                    duplicates.set(j, handlers.get(i2));
                    duplicates.remove(j + 1);
                    handlers.remove(i3);
                    if (i3 <= i) {
                        --i;
                    }
                    --j;
                }
            }
        }
    }
    
    private void trimAggressiveCatchBlocks() {
        final List<ExceptionHandler> handlers = this._exceptionHandlers;
        for (int i = 0; i < handlers.size(); ++i) {
            final ExceptionHandler handler = handlers.get(i);
            final InstructionBlock tryBlock = handler.getTryBlock();
            final InstructionBlock handlerBlock = handler.getHandlerBlock();
            if (handler.isCatch()) {
                for (int j = 0; j < handlers.size(); ++j) {
                    if (i != j) {
                        final ExceptionHandler other = handlers.get(j);
                        if (other.isFinally()) {
                            final InstructionBlock otherTry = other.getTryBlock();
                            final InstructionBlock otherHandler = other.getHandlerBlock();
                            if (handlerBlock.getFirstInstruction().getOffset() < otherHandler.getFirstInstruction().getOffset() && handlerBlock.intersects(otherHandler) && (!handlerBlock.contains(otherTry) || !handlerBlock.contains(otherHandler)) && !otherTry.contains(tryBlock)) {
                                handlers.set(i--, ExceptionHandler.createCatch(tryBlock, new InstructionBlock(handlerBlock.getFirstInstruction(), otherHandler.getFirstInstruction().getPrevious()), handler.getCatchType()));
                                break;
                            }
                        }
                    }
                }
            }
        }
    }
    
    private void removeSelfHandlingFinallyHandlers() {
        final List<ExceptionHandler> handlers = this._exceptionHandlers;
        for (int i = 0; i < handlers.size(); ++i) {
            final ExceptionHandler handler = handlers.get(i);
            final InstructionBlock tryBlock = handler.getTryBlock();
            final InstructionBlock handlerBlock = handler.getHandlerBlock();
            if (handler.isFinally() && handlerBlock.getFirstInstruction() == tryBlock.getFirstInstruction() && tryBlock.getLastInstruction().getOffset() < handlerBlock.getLastInstruction().getEndOffset()) {
                handlers.remove(i--);
            }
        }
    }
    
    private void trimAggressiveFinallyBlocks() {
        final List<ExceptionHandler> handlers = this._exceptionHandlers;
        for (int i = 0; i < handlers.size(); ++i) {
            final ExceptionHandler handler = handlers.get(i);
            final InstructionBlock tryBlock = handler.getTryBlock();
            final InstructionBlock handlerBlock = handler.getHandlerBlock();
            if (handler.isFinally()) {
                for (int j = 0; j < handlers.size(); ++j) {
                    if (i != j) {
                        final ExceptionHandler other = handlers.get(j);
                        if (other.isCatch()) {
                            final InstructionBlock otherTry = other.getTryBlock();
                            final InstructionBlock otherHandler = other.getHandlerBlock();
                            if (tryBlock.getFirstInstruction() == otherTry.getFirstInstruction() && tryBlock.getLastInstruction() == otherHandler.getFirstInstruction()) {
                                handlers.set(i--, ExceptionHandler.createFinally(new InstructionBlock(tryBlock.getFirstInstruction(), otherHandler.getFirstInstruction().getPrevious()), handlerBlock));
                                break;
                            }
                        }
                    }
                }
            }
        }
    }
    
    private static ControlFlowNode findHandlerNode(final ControlFlowGraph cfg, final ExceptionHandler handler) {
        final List<ControlFlowNode> nodes = cfg.getNodes();
        for (int i = nodes.size() - 1; i >= 0; --i) {
            final ControlFlowNode node = nodes.get(i);
            if (node.getExceptionHandler() == handler) {
                return node;
            }
        }
        return null;
    }
    
    private ExceptionHandler findInnermostExceptionHandler(final int offsetInTryBlock, final ExceptionHandler exclude) {
        ExceptionHandler result = null;
        for (final ExceptionHandler handler : this._exceptionHandlers) {
            if (handler == exclude) {
                continue;
            }
            final InstructionBlock tryBlock = handler.getTryBlock();
            if (tryBlock.getFirstInstruction().getOffset() > offsetInTryBlock || offsetInTryBlock >= tryBlock.getLastInstruction().getEndOffset() || (result != null && tryBlock.getFirstInstruction().getOffset() <= result.getTryBlock().getFirstInstruction().getOffset())) {
                continue;
            }
            result = handler;
        }
        return result;
    }
    
    private void closeTryHandlerGaps() {
        final List<ExceptionHandler> handlers = this._exceptionHandlers;
        for (int i = 0; i < handlers.size() - 1; ++i) {
            final ExceptionHandler current = handlers.get(i);
            final ExceptionHandler next = handlers.get(i + 1);
            if (current.getHandlerBlock().equals(next.getHandlerBlock())) {
                final Instruction lastInCurrent = current.getTryBlock().getLastInstruction();
                final Instruction firstInNext = next.getTryBlock().getFirstInstruction();
                final Instruction branchInBetween = firstInNext.getPrevious();
                Instruction beforeBranch;
                if (branchInBetween != null) {
                    beforeBranch = branchInBetween.getPrevious();
                }
                else {
                    beforeBranch = null;
                }
                if (branchInBetween != null && branchInBetween.getOpCode().isBranch() && (lastInCurrent == beforeBranch || lastInCurrent == branchInBetween)) {
                    ExceptionHandler newHandler;
                    if (current.isFinally()) {
                        newHandler = ExceptionHandler.createFinally(new InstructionBlock(current.getTryBlock().getFirstInstruction(), next.getTryBlock().getLastInstruction()), new InstructionBlock(current.getHandlerBlock().getFirstInstruction(), current.getHandlerBlock().getLastInstruction()));
                    }
                    else {
                        newHandler = ExceptionHandler.createCatch(new InstructionBlock(current.getTryBlock().getFirstInstruction(), next.getTryBlock().getLastInstruction()), new InstructionBlock(current.getHandlerBlock().getFirstInstruction(), current.getHandlerBlock().getLastInstruction()), current.getCatchType());
                    }
                    handlers.set(i, newHandler);
                    handlers.remove(i + 1);
                    --i;
                }
            }
        }
    }
    
    private static ExceptionHandler findFirstHandler(final InstructionBlock tryBlock, final Collection<ExceptionHandler> handlers) {
        ExceptionHandler result = null;
        for (final ExceptionHandler handler : handlers) {
            if (handler.getTryBlock().equals(tryBlock) && (result == null || handler.getHandlerBlock().getFirstInstruction().getOffset() < result.getHandlerBlock().getFirstInstruction().getOffset())) {
                result = handler;
            }
        }
        return result;
    }
    
    private static List<ExceptionHandler> findHandlers(final InstructionBlock tryBlock, final Collection<ExceptionHandler> handlers) {
        List<ExceptionHandler> result = null;
        for (final ExceptionHandler handler : handlers) {
            if (handler.getTryBlock().equals(tryBlock)) {
                if (result == null) {
                    result = new ArrayList<ExceptionHandler>();
                }
                result.add(handler);
            }
        }
        if (result == null) {
            return Collections.emptyList();
        }
        Collections.sort(result, new Comparator<ExceptionHandler>() {
            @Override
            public int compare(@NotNull final ExceptionHandler o1, @NotNull final ExceptionHandler o2) {
                return Integer.compare(o1.getHandlerBlock().getFirstInstruction().getOffset(), o2.getHandlerBlock().getFirstInstruction().getOffset());
            }
        });
        return result;
    }
    
    private static List<ExceptionHandler> findDuplicateHandlers(final ExceptionHandler handler, final Collection<ExceptionHandler> handlers) {
        final List<ExceptionHandler> result = new ArrayList<ExceptionHandler>();
        for (final ExceptionHandler other : handlers) {
            if (other.getHandlerBlock().equals(handler.getHandlerBlock())) {
                if (handler.isFinally()) {
                    if (!other.isFinally()) {
                        continue;
                    }
                    result.add(other);
                }
                else {
                    if (!other.isCatch() || !MetadataHelper.isSameType(other.getCatchType(), handler.getCatchType())) {
                        continue;
                    }
                    result.add(other);
                }
            }
        }
        Collections.sort(result, new Comparator<ExceptionHandler>() {
            @Override
            public int compare(@NotNull final ExceptionHandler o1, @NotNull final ExceptionHandler o2) {
                return Integer.compare(o1.getTryBlock().getFirstInstruction().getOffset(), o2.getTryBlock().getFirstInstruction().getOffset());
            }
        });
        return result;
    }
    
    private List<ByteCode> performStackAnalysis() {
        final Set<ByteCode> handlerStarts = new HashSet<ByteCode>();
        final Map<Instruction, ByteCode> byteCodeMap = new LinkedHashMap<Instruction, ByteCode>();
        final Map<Instruction, ControlFlowNode> nodeMap = new IdentityHashMap<Instruction, ControlFlowNode>();
        final InstructionCollection instructions = this._instructions;
        final List<ExceptionHandler> exceptionHandlers = new ArrayList<ExceptionHandler>();
        final List<ControlFlowNode> successors = new ArrayList<ControlFlowNode>();
        for (final ControlFlowNode node : this._cfg.getNodes()) {
            if (node.getExceptionHandler() != null) {
                exceptionHandlers.add(node.getExceptionHandler());
            }
            if (node.getNodeType() != ControlFlowNodeType.Normal) {
                continue;
            }
            for (Instruction p = node.getStart(); p != null && p.getOffset() < node.getEnd().getEndOffset(); p = p.getNext()) {
                nodeMap.put(p, node);
            }
        }
        this._exceptionHandlers.retainAll(exceptionHandlers);
        final List<ByteCode> body = new ArrayList<ByteCode>(instructions.size());
        final StackMappingVisitor stackMapper = new StackMappingVisitor();
        final InstructionVisitor instructionVisitor = stackMapper.visitBody(this._body);
        final StrongBox<AstCode> codeBox = new StrongBox<AstCode>();
        final StrongBox<Object> operandBox = new StrongBox<Object>();
        this._factory = CoreMetadataFactory.make(this._context.getCurrentType(), this._context.getCurrentMethod());
        for (final Instruction instruction : instructions) {
            final OpCode opCode = instruction.getOpCode();
            AstCode code = AstBuilder.CODES[opCode.ordinal()];
            Object operand = instruction.hasOperand() ? instruction.getOperand(0) : null;
            final Object secondOperand = (instruction.getOperandCount() > 1) ? instruction.getOperand(1) : null;
            codeBox.set(code);
            operandBox.set(operand);
            final int offset = mappedInstruction(this._originalInstructionMap, instruction).getOffset();
            if (AstCode.expandMacro(codeBox, operandBox, this._body, offset)) {
                code = codeBox.get();
                operand = operandBox.get();
            }
            final ByteCode byteCode = new ByteCode(null);
            byteCode.instruction = instruction;
            byteCode.offset = instruction.getOffset();
            byteCode.endOffset = instruction.getEndOffset();
            byteCode.code = code;
            byteCode.operand = operand;
            byteCode.secondOperand = secondOperand;
            byteCode.popCount = InstructionHelper.getPopDelta(instruction, this._body);
            byteCode.pushCount = InstructionHelper.getPushDelta(instruction, this._body);
            byteCodeMap.put(instruction, byteCode);
            body.add(byteCode);
        }
        for (int i = 0, n = body.size() - 1; i < n; ++i) {
            final ByteCode next = body.get(i + 1);
            final ByteCode current = body.get(i);
            current.next = next;
            next.previous = current;
        }
        final ArrayDeque<ByteCode> agenda = new ArrayDeque<ByteCode>();
        final ArrayDeque<ByteCode> handlerAgenda = new ArrayDeque<ByteCode>();
        final int variableCount = this._body.getMaxLocals();
        final VariableSlot[] unknownVariables = VariableSlot.makeUnknownState(variableCount);
        final MethodReference method = this._body.getMethod();
        final List<ParameterDefinition> parameters = method.getParameters();
        final boolean hasThis = this._body.hasThis();
        if (hasThis) {
            if (method.isConstructor()) {
                unknownVariables[0] = new VariableSlot(FrameValue.UNINITIALIZED_THIS, AstBuilder.EMPTY_DEFINITIONS);
            }
            else {
                unknownVariables[0] = new VariableSlot(FrameValue.makeReference(this._context.getCurrentType()), AstBuilder.EMPTY_DEFINITIONS);
            }
        }
        final ByteCode[] definitions = { new ByteCode(null) };
        for (int j = 0; j < parameters.size(); ++j) {
            final ParameterDefinition parameter = parameters.get(j);
            final TypeReference parameterType = parameter.getParameterType();
            final int slot = parameter.getSlot();
            switch ($SWITCH_TABLE$com$strobel$assembler$metadata$JvmType()[parameterType.getSimpleType().ordinal()]) {
                case 1:
                case 2:
                case 3:
                case 4:
                case 5: {
                    unknownVariables[slot] = new VariableSlot(FrameValue.INTEGER, definitions);
                    break;
                }
                case 6: {
                    unknownVariables[slot] = new VariableSlot(FrameValue.LONG, definitions);
                    unknownVariables[slot + 1] = new VariableSlot(FrameValue.TOP, definitions);
                    break;
                }
                case 7: {
                    unknownVariables[slot] = new VariableSlot(FrameValue.FLOAT, definitions);
                    break;
                }
                case 8: {
                    unknownVariables[slot] = new VariableSlot(FrameValue.DOUBLE, definitions);
                    unknownVariables[slot + 1] = new VariableSlot(FrameValue.TOP, definitions);
                    break;
                }
                default: {
                    unknownVariables[slot] = new VariableSlot(FrameValue.makeReference(parameterType), definitions);
                    break;
                }
            }
        }
        for (final ExceptionHandler handler : exceptionHandlers) {
            final ByteCode handlerStart = byteCodeMap.get(handler.getHandlerBlock().getFirstInstruction());
            handlerStarts.add(handlerStart);
            handlerStart.stackBefore = AstBuilder.EMPTY_STACK;
            handlerStart.variablesBefore = VariableSlot.cloneVariableState(unknownVariables);
            final ByteCode loadException = new ByteCode(null);
            TypeReference catchType;
            if (handler.isFinally()) {
                catchType = this._factory.makeNamedType("java.lang.Throwable");
            }
            else {
                catchType = handler.getCatchType();
            }
            loadException.code = AstCode.LoadException;
            loadException.operand = catchType;
            loadException.popCount = 0;
            loadException.pushCount = 1;
            this._loadExceptions.put(handler, loadException);
            handlerStart.stackBefore = new StackSlot[] { new StackSlot(FrameValue.makeReference(catchType), new ByteCode[] { loadException }) };
            handlerAgenda.addLast(handlerStart);
        }
        body.get(0).stackBefore = AstBuilder.EMPTY_STACK;
        body.get(0).variablesBefore = unknownVariables;
        agenda.addFirst(body.get(0));
        while (!agenda.isEmpty() || !handlerAgenda.isEmpty()) {
            final ByteCode byteCode2 = agenda.isEmpty() ? handlerAgenda.removeFirst() : agenda.removeFirst();
            stackMapper.visitFrame(byteCode2.getFrameBefore());
            instructionVisitor.visit(byteCode2.instruction);
            final StackSlot[] newStack = createModifiedStack(byteCode2, stackMapper);
            final VariableSlot[] newVariableState = VariableSlot.cloneVariableState(byteCode2.variablesBefore);
            final Map<Instruction, TypeReference> initializations = stackMapper.getInitializations();
            for (int k = 0; k < newVariableState.length; ++k) {
                final VariableSlot slot2 = newVariableState[k];
                if (slot2.isUninitialized()) {
                    final Object parameter2 = slot2.value.getParameter();
                    if (parameter2 instanceof Instruction) {
                        final Instruction instruction2 = (Instruction)parameter2;
                        final TypeReference initializedType = initializations.get(instruction2);
                        if (initializedType != null) {
                            newVariableState[k] = new VariableSlot(FrameValue.makeReference(initializedType), slot2.definitions);
                        }
                    }
                }
            }
            if (byteCode2.isVariableDefinition()) {
                final int slot3 = ((VariableReference)byteCode2.operand).getSlot();
                newVariableState[slot3] = new VariableSlot(stackMapper.getLocalValue(slot3), new ByteCode[] { byteCode2 });
                if (newVariableState[slot3].value.getType().isDoubleWord()) {
                    newVariableState[slot3 + 1] = new VariableSlot(stackMapper.getLocalValue(slot3 + 1), new ByteCode[] { byteCode2 });
                }
            }
            final ArrayList<ByteCode> branchTargets = new ArrayList<ByteCode>();
            final ControlFlowNode node2 = nodeMap.get(byteCode2.instruction);
            successors.clear();
            if (byteCode2.instruction != node2.getEnd()) {
                branchTargets.add(byteCode2.next);
            }
            else {
                if (!byteCode2.instruction.getOpCode().isUnconditionalBranch()) {
                    branchTargets.add(byteCode2.next);
                }
                for (final ControlFlowNode successor : node2.getSuccessors()) {
                    if (successor.getNodeType() == ControlFlowNodeType.Normal) {
                        successors.add(successor);
                    }
                    else {
                        if (successor.getNodeType() != ControlFlowNodeType.EndFinally) {
                            continue;
                        }
                        for (final ControlFlowNode s : successor.getSuccessors()) {
                            successors.add(s);
                        }
                    }
                }
            }
            for (final ControlFlowNode successor : node2.getSuccessors()) {
                if (successor.getExceptionHandler() != null) {
                    successors.add(nodeMap.get(successor.getExceptionHandler().getHandlerBlock().getFirstInstruction()));
                }
            }
            for (final ControlFlowNode successor : successors) {
                if (successor.getNodeType() != ControlFlowNodeType.Normal) {
                    continue;
                }
                final Instruction targetInstruction = successor.getStart();
                final ByteCode target = byteCodeMap.get(targetInstruction);
                if (target.label == null) {
                    (target.label = new Label()).setOffset(target.offset);
                    target.label.setName(target.makeLabelName());
                }
                branchTargets.add(target);
            }
            for (final ByteCode branchTarget : branchTargets) {
                final boolean isSubroutineJump = byteCode2.code == AstCode.Jsr && byteCode2.instruction.getOperand(0) == branchTarget.instruction;
                StackSlot[] effectiveStack;
                if (isSubroutineJump) {
                    effectiveStack = ArrayUtilities.append(newStack, new StackSlot(FrameValue.makeAddress(byteCode2.next.instruction), new ByteCode[] { byteCode2 }));
                }
                else {
                    effectiveStack = newStack;
                }
                if (branchTarget.stackBefore == null && branchTarget.variablesBefore == null) {
                    branchTarget.stackBefore = StackSlot.modifyStack(effectiveStack, 0, null, new FrameValue[0]);
                    branchTarget.variablesBefore = VariableSlot.cloneVariableState(newVariableState);
                    agenda.push(branchTarget);
                }
                else {
                    final boolean isHandlerStart = handlerStarts.contains(branchTarget);
                    if (branchTarget.stackBefore.length != effectiveStack.length && !isHandlerStart && !isSubroutineJump) {
                        throw new IllegalStateException("Inconsistent stack size at " + branchTarget.name() + " (coming from " + byteCode2.name() + ").");
                    }
                    boolean modified = false;
                    final int stackSize = newStack.length;
                    final Frame inputFrame;
                    final Frame outputFrame = inputFrame = createFrame(effectiveStack, newVariableState);
                    final Frame nextFrame = createFrame((branchTarget.stackBefore.length > stackSize) ? Arrays.copyOfRange(branchTarget.stackBefore, 0, stackSize) : branchTarget.stackBefore, branchTarget.variablesBefore);
                    final Frame mergedFrame = Frame.merge(inputFrame, outputFrame, nextFrame, initializations);
                    final List<FrameValue> stack = mergedFrame.getStackValues();
                    final List<FrameValue> locals = mergedFrame.getLocalValues();
                    if (!isHandlerStart) {
                        final StackSlot[] oldStack = branchTarget.stackBefore;
                        final int oldStart = (oldStack != null && oldStack.length > stackSize) ? (oldStack.length - 1) : (stackSize - 1);
                        for (int l = stack.size() - 1, m = oldStart; l >= 0 && m >= 0; --l, --m) {
                            final FrameValue oldValue = oldStack[m].value;
                            final FrameValue newValue = stack.get(l);
                            final ByteCode[] oldDefinitions = oldStack[m].definitions;
                            final ByteCode[] newDefinitions = ArrayUtilities.union(oldDefinitions, effectiveStack[l].definitions);
                            if (!Comparer.equals(newValue, oldValue) || newDefinitions.length > oldDefinitions.length) {
                                oldStack[m] = new StackSlot(newValue, newDefinitions);
                                modified = true;
                            }
                        }
                    }
                    for (int i2 = 0, n2 = locals.size(); i2 < n2; ++i2) {
                        final VariableSlot oldSlot = branchTarget.variablesBefore[i2];
                        final VariableSlot newSlot = newVariableState[i2];
                        final FrameValue oldLocal = oldSlot.value;
                        final FrameValue newLocal = locals.get(i2);
                        final ByteCode[] oldDefinitions = oldSlot.definitions;
                        final ByteCode[] newDefinitions = ArrayUtilities.union(oldSlot.definitions, newSlot.definitions);
                        if (!Comparer.equals(oldLocal, newLocal) || newDefinitions.length > oldDefinitions.length) {
                            branchTarget.variablesBefore[i2] = new VariableSlot(newLocal, newDefinitions);
                            modified = true;
                        }
                    }
                    if (!modified) {
                        continue;
                    }
                    agenda.addLast(branchTarget);
                }
            }
        }
        ArrayList<ByteCode> unreachable = null;
        for (final ByteCode byteCode3 : body) {
            if (byteCode3.stackBefore == null) {
                if (unreachable == null) {
                    unreachable = new ArrayList<ByteCode>();
                }
                unreachable.add(byteCode3);
            }
        }
        if (unreachable != null) {
            body.removeAll(unreachable);
        }
        for (final ByteCode byteCode3 : body) {
            final int popCount = (byteCode3.popCount != -1) ? byteCode3.popCount : byteCode3.stackBefore.length;
            int argumentIndex = 0;
            for (int i3 = byteCode3.stackBefore.length - popCount; i3 < byteCode3.stackBefore.length; ++i3) {
                final Variable tempVariable = new Variable();
                tempVariable.setName(String.format("stack_%1$02X_%2$d", byteCode3.offset, argumentIndex));
                tempVariable.setGenerated(true);
                final FrameValue value = byteCode3.stackBefore[i3].value;
                switch ($SWITCH_TABLE$com$strobel$assembler$ir$FrameValueType()[value.getType().ordinal()]) {
                    case 3: {
                        tempVariable.setType(BuiltinTypes.Integer);
                        break;
                    }
                    case 4: {
                        tempVariable.setType(BuiltinTypes.Float);
                        break;
                    }
                    case 5: {
                        tempVariable.setType(BuiltinTypes.Long);
                        break;
                    }
                    case 6: {
                        tempVariable.setType(BuiltinTypes.Double);
                        break;
                    }
                    case 8: {
                        tempVariable.setType(this._context.getCurrentType());
                        break;
                    }
                    case 9: {
                        TypeReference refType = (TypeReference)value.getParameter();
                        if (refType.isWildcardType()) {
                            refType = (refType.hasSuperBound() ? refType.getSuperBound() : refType.getExtendsBound());
                        }
                        tempVariable.setType(refType);
                        break;
                    }
                }
                byteCode3.stackBefore[i3] = new StackSlot(value, byteCode3.stackBefore[i3].definitions, tempVariable);
                ByteCode[] loc_11;
                for (int loc_10 = (loc_11 = byteCode3.stackBefore[i3].definitions).length, loc_12 = 0; loc_12 < loc_10; ++loc_12) {
                    final ByteCode pushedBy = loc_11[loc_12];
                    if (pushedBy.storeTo == null) {
                        pushedBy.storeTo = new ArrayList<Variable>();
                    }
                    pushedBy.storeTo.add(tempVariable);
                }
                ++argumentIndex;
            }
        }
    Label_3234_Outer:
        for (final ByteCode byteCode3 : body) {
            if (byteCode3.storeTo != null && byteCode3.storeTo.size() > 1) {
                final List<Variable> localVariables = byteCode3.storeTo;
                List<StackSlot> loadedBy = null;
            Label_3234:
                while (true) {
                    for (final Variable local : localVariables) {
                        for (final ByteCode bc : body) {
                            StackSlot[] loc_17;
                            for (int loc_16 = (loc_17 = bc.stackBefore).length, loc_18 = 0; loc_18 < loc_16; ++loc_18) {
                                final StackSlot s2 = loc_17[loc_18];
                                if (s2.loadFrom == local) {
                                    if (loadedBy == null) {
                                        loadedBy = new ArrayList<StackSlot>();
                                    }
                                    loadedBy.add(s2);
                                    continue Label_3234;
                                }
                            }
                        }
                    }
                    break;
                }
                if (loadedBy == null) {
                    continue Label_3234_Outer;
                }
                boolean singleStore = true;
                TypeReference type = null;
                for (final StackSlot slot4 : loadedBy) {
                    if (slot4.definitions.length != 1) {
                        singleStore = false;
                        break;
                    }
                    if (slot4.definitions[0] != byteCode3) {
                        singleStore = false;
                        break;
                    }
                    if (type != null) {
                        continue Label_3234_Outer;
                    }
                    switch ($SWITCH_TABLE$com$strobel$assembler$ir$FrameValueType()[slot4.value.getType().ordinal()]) {
                        case 9: {
                            type = (TypeReference)slot4.value.getParameter();
                            if (type.isWildcardType()) {
                                type = (type.hasSuperBound() ? type.getSuperBound() : type.getExtendsBound());
                                continue Label_3234_Outer;
                            }
                            continue Label_3234_Outer;
                        }
                        default: {
                            continue Label_3234_Outer;
                        }
                        case 3: {
                            type = BuiltinTypes.Integer;
                            continue Label_3234_Outer;
                        }
                        case 4: {
                            type = BuiltinTypes.Float;
                            continue Label_3234_Outer;
                        }
                        case 5: {
                            type = BuiltinTypes.Long;
                            continue Label_3234_Outer;
                        }
                        case 6: {
                            type = BuiltinTypes.Double;
                            continue Label_3234_Outer;
                        }
                    }
                }
                if (!singleStore) {
                    continue Label_3234_Outer;
                }
                final Variable tempVariable2 = new Variable();
                tempVariable2.setName(String.format("expr_%1$02X", byteCode3.offset));
                tempVariable2.setGenerated(true);
                tempVariable2.setType(type);
                byteCode3.storeTo = Collections.singletonList(tempVariable2);
                for (final ByteCode bc2 : body) {
                    for (int i4 = 0; i4 < bc2.stackBefore.length; ++i4) {
                        if (localVariables.contains(bc2.stackBefore[i4].loadFrom)) {
                            bc2.stackBefore[i4] = new StackSlot(bc2.stackBefore[i4].value, bc2.stackBefore[i4].definitions, tempVariable2);
                        }
                    }
                }
            }
        }
        this.convertLocalVariables(definitions, body);
        for (final ByteCode byteCode3 : body) {
            if (byteCode3.operand instanceof Instruction[]) {
                final Instruction[] branchTargets2 = (Instruction[])byteCode3.operand;
                final Label[] newOperand = new Label[branchTargets2.length];
                for (int i3 = 0; i3 < branchTargets2.length; ++i3) {
                    newOperand[i3] = byteCodeMap.get(branchTargets2[i3]).label;
                }
                byteCode3.operand = newOperand;
            }
            else if (byteCode3.operand instanceof Instruction) {
                byteCode3.operand = byteCodeMap.get(byteCode3.operand).label;
            }
            else {
                if (!(byteCode3.operand instanceof SwitchInfo)) {
                    continue;
                }
                final SwitchInfo switchInfo = (SwitchInfo)byteCode3.operand;
                final Instruction[] branchTargets3 = ArrayUtilities.prepend(switchInfo.getTargets(), switchInfo.getDefaultTarget());
                final Label[] newOperand2 = new Label[branchTargets3.length];
                for (int i5 = 0; i5 < branchTargets3.length; ++i5) {
                    newOperand2[i5] = byteCodeMap.get(branchTargets3[i5]).label;
                }
                byteCode3.operand = newOperand2;
            }
        }
        return body;
    }
    
    private static Instruction mappedInstruction(final Map<Instruction, Instruction> map, final Instruction instruction) {
        Instruction current;
        Instruction newInstruction;
        for (current = instruction; (newInstruction = map.get(current)) != null; current = newInstruction) {
            if (newInstruction == current) {
                return current;
            }
        }
        return current;
    }
    
    private static StackSlot[] createModifiedStack(final ByteCode byteCode, final StackMappingVisitor stackMapper) {
        final Map<Instruction, TypeReference> initializations = stackMapper.getInitializations();
        final StackSlot[] oldStack = byteCode.stackBefore.clone();
        for (int i = 0; i < oldStack.length; ++i) {
            if (oldStack[i].value.getParameter() instanceof Instruction) {
                final TypeReference initializedType = initializations.get(oldStack[i].value.getParameter());
                if (initializedType != null) {
                    oldStack[i] = new StackSlot(FrameValue.makeReference(initializedType), oldStack[i].definitions, oldStack[i].loadFrom);
                }
            }
        }
        if (byteCode.popCount == 0 && byteCode.pushCount == 0) {
            return oldStack;
        }
        switch ($SWITCH_TABLE$com$strobel$decompiler$ast$AstCode()[byteCode.code.ordinal()]) {
            case 90: {
                return ArrayUtilities.append(oldStack, new StackSlot(stackMapper.getStackValue(0), oldStack[oldStack.length - 1].definitions));
            }
            case 91: {
                return ArrayUtilities.insert(oldStack, oldStack.length - 2, new StackSlot(stackMapper.getStackValue(0), oldStack[oldStack.length - 1].definitions));
            }
            case 92: {
                return ArrayUtilities.insert(oldStack, oldStack.length - 3, new StackSlot(stackMapper.getStackValue(0), oldStack[oldStack.length - 1].definitions));
            }
            case 93: {
                return ArrayUtilities.append(oldStack, new StackSlot(stackMapper.getStackValue(1), oldStack[oldStack.length - 2].definitions), new StackSlot(stackMapper.getStackValue(0), oldStack[oldStack.length - 1].definitions));
            }
            case 94: {
                return ArrayUtilities.insert(oldStack, oldStack.length - 3, new StackSlot(stackMapper.getStackValue(1), oldStack[oldStack.length - 2].definitions), new StackSlot(stackMapper.getStackValue(0), oldStack[oldStack.length - 1].definitions));
            }
            case 95: {
                return ArrayUtilities.insert(oldStack, oldStack.length - 4, new StackSlot(stackMapper.getStackValue(1), oldStack[oldStack.length - 2].definitions), new StackSlot(stackMapper.getStackValue(0), oldStack[oldStack.length - 1].definitions));
            }
            case 96: {
                final StackSlot[] newStack = new StackSlot[oldStack.length];
                ArrayUtilities.copy(oldStack, newStack);
                final StackSlot temp = newStack[oldStack.length - 1];
                newStack[oldStack.length - 1] = newStack[oldStack.length - 2];
                newStack[oldStack.length - 2] = temp;
                return newStack;
            }
            default: {
                final FrameValue[] pushValues = new FrameValue[byteCode.pushCount];
                for (int j = 0; j < byteCode.pushCount; ++j) {
                    pushValues[pushValues.length - j - 1] = stackMapper.getStackValue(j);
                }
                return StackSlot.modifyStack(oldStack, (byteCode.popCount != -1) ? byteCode.popCount : oldStack.length, byteCode, pushValues);
            }
        }
    }
    
    private void convertLocalVariables(final ByteCode[] parameterDefinitions, final List<ByteCode> body) {
        final MethodDefinition method = this._context.getCurrentMethod();
        final List<ParameterDefinition> parameters = method.getParameters();
        final VariableDefinitionCollection variables = this._body.getVariables();
        final ParameterDefinition[] parameterMap = new ParameterDefinition[this._body.getMaxLocals()];
        final boolean hasThis = this._body.hasThis();
        if (hasThis) {
            parameterMap[0] = this._body.getThisParameter();
        }
        for (final ParameterDefinition parameter : parameters) {
            parameterMap[parameter.getSlot()] = parameter;
        }
        final Set<Pair<Integer, JvmType>> undefinedSlots = new HashSet<Pair<Integer, JvmType>>();
        final List<VariableReference> varReferences = new ArrayList<VariableReference>();
        final Map<String, VariableDefinition> lookup = makeVariableLookup(variables);
        for (final VariableDefinition variableDefinition : variables) {
            varReferences.add(variableDefinition);
        }
        for (final ByteCode b : body) {
            if (b.operand instanceof VariableReference && !(b.operand instanceof VariableDefinition)) {
                final VariableReference reference = (VariableReference)b.operand;
                if (!undefinedSlots.add(Pair.create(reference.getSlot(), this.getStackType(reference.getVariableType())))) {
                    continue;
                }
                varReferences.add(reference);
            }
        }
        for (final VariableReference vRef : varReferences) {
            final int slot = vRef.getSlot();
            final List<ByteCode> definitions = new ArrayList<ByteCode>();
            final List<ByteCode> references = new ArrayList<ByteCode>();
            final VariableDefinition vDef = (vRef instanceof VariableDefinition) ? lookup.get(key((VariableDefinition)vRef)) : null;
            for (final ByteCode b2 : body) {
                if (vDef != null) {
                    if (!(b2.operand instanceof VariableDefinition) || lookup.get(key((VariableDefinition)b2.operand)) != vDef) {
                        continue;
                    }
                    if (b2.isVariableDefinition()) {
                        definitions.add(b2);
                    }
                    else {
                        references.add(b2);
                    }
                }
                else {
                    if (!(b2.operand instanceof VariableReference) || !this.variablesMatch(vRef, (VariableReference)b2.operand)) {
                        continue;
                    }
                    if (b2.isVariableDefinition()) {
                        definitions.add(b2);
                    }
                    else {
                        references.add(b2);
                    }
                }
            }
            final ParameterDefinition parameter2 = parameterMap[slot];
            List<VariableInfo> newVariables;
            if (!this._optimize) {
                final Variable variable = new Variable();
                if (vDef != null) {
                    variable.setType(vDef.getVariableType());
                    variable.setName(StringUtilities.isNullOrEmpty(vDef.getName()) ? ("var_" + slot) : vDef.getName());
                }
                else {
                    variable.setName("var_" + slot);
                    for (final ByteCode b3 : definitions) {
                        final FrameValue stackValue = b3.stackBefore[b3.stackBefore.length - b3.popCount].value;
                        if (stackValue != FrameValue.UNINITIALIZED && stackValue != FrameValue.UNINITIALIZED_THIS) {
                            TypeReference variableType = null;
                            switch ($SWITCH_TABLE$com$strobel$assembler$ir$FrameValueType()[stackValue.getType().ordinal()]) {
                                case 3: {
                                    variableType = BuiltinTypes.Integer;
                                    break;
                                }
                                case 4: {
                                    variableType = BuiltinTypes.Float;
                                    break;
                                }
                                case 5: {
                                    variableType = BuiltinTypes.Long;
                                    break;
                                }
                                case 6: {
                                    variableType = BuiltinTypes.Double;
                                    break;
                                }
                                case 10: {
                                    if (stackValue.getParameter() instanceof Instruction && ((Instruction)stackValue.getParameter()).getOpCode() == OpCode.NEW) {
                                        variableType = ((Instruction)stackValue.getParameter()).getOperand(0);
                                        break;
                                    }
                                    if (vDef != null) {
                                        variableType = vDef.getVariableType();
                                        break;
                                    }
                                    variableType = BuiltinTypes.Object;
                                    break;
                                }
                                case 8: {
                                    variableType = this._context.getCurrentType();
                                    break;
                                }
                                case 9: {
                                    variableType = (TypeReference)stackValue.getParameter();
                                    break;
                                }
                                case 11: {
                                    variableType = BuiltinTypes.Integer;
                                    break;
                                }
                                case 7: {
                                    variableType = BuiltinTypes.Null;
                                    break;
                                }
                                default: {
                                    if (vDef != null) {
                                        variableType = vDef.getVariableType();
                                        break;
                                    }
                                    variableType = BuiltinTypes.Object;
                                    break;
                                }
                            }
                            variable.setType(variableType);
                            break;
                        }
                    }
                    if (variable.getType() == null) {
                        variable.setType(BuiltinTypes.Object);
                    }
                }
                if (vDef == null) {
                    variable.setOriginalVariable(new VariableDefinition(slot, variable.getName(), method, variable.getType()));
                }
                else {
                    variable.setOriginalVariable(vDef);
                }
                variable.setGenerated(false);
                final VariableInfo variableInfo = new VariableInfo(slot, variable, definitions, references);
                newVariables = Collections.singletonList(variableInfo);
            }
            else {
                newVariables = new ArrayList<VariableInfo>();
                boolean parameterVariableAdded = false;
                VariableInfo parameterVariable = null;
                if (parameter2 != null) {
                    final Variable variable2 = new Variable();
                    variable2.setName(StringUtilities.isNullOrEmpty(parameter2.getName()) ? ("p" + parameter2.getPosition()) : parameter2.getName());
                    variable2.setType(parameter2.getParameterType());
                    variable2.setOriginalParameter(parameter2);
                    variable2.setOriginalVariable(vDef);
                    parameterVariable = new VariableInfo(slot, variable2, new ArrayList<ByteCode>(), new ArrayList<ByteCode>());
                    Collections.addAll(parameterVariable.definitions, parameterDefinitions);
                }
                for (final ByteCode b4 : definitions) {
                    FrameValue stackValue2;
                    if (b4.code == AstCode.Inc) {
                        stackValue2 = FrameValue.INTEGER;
                    }
                    else {
                        stackValue2 = b4.stackBefore[b4.stackBefore.length - b4.popCount].value;
                    }
                    TypeReference variableType = null;
                    if (vDef != null && vDef.isFromMetadata()) {
                        variableType = vDef.getVariableType();
                    }
                    else {
                        switch ($SWITCH_TABLE$com$strobel$assembler$ir$FrameValueType()[stackValue2.getType().ordinal()]) {
                            case 3: {
                                variableType = BuiltinTypes.Integer;
                                break;
                            }
                            case 4: {
                                variableType = BuiltinTypes.Float;
                                break;
                            }
                            case 5: {
                                variableType = BuiltinTypes.Long;
                                break;
                            }
                            case 6: {
                                variableType = BuiltinTypes.Double;
                                break;
                            }
                            case 8: {
                                variableType = this._context.getCurrentType();
                                break;
                            }
                            case 9: {
                                variableType = (TypeReference)stackValue2.getParameter();
                                break;
                            }
                            case 11: {
                                variableType = BuiltinTypes.Integer;
                                break;
                            }
                            case 7: {
                                variableType = BuiltinTypes.Null;
                                break;
                            }
                            default: {
                                if (vDef != null) {
                                    variableType = vDef.getVariableType();
                                    break;
                                }
                                variableType = BuiltinTypes.Object;
                                break;
                            }
                        }
                    }
                    if (parameterVariable != null) {
                        boolean useParameter;
                        if (variableType.isPrimitive() || parameterVariable.variable.getType().isPrimitive()) {
                            useParameter = (variableType.getSimpleType() == parameterVariable.variable.getType().getSimpleType());
                        }
                        else {
                            useParameter = MetadataHelper.isSameType(variableType, parameterVariable.variable.getType());
                        }
                        if (useParameter) {
                            if (!parameterVariableAdded) {
                                newVariables.add(parameterVariable);
                                parameterVariableAdded = true;
                            }
                            parameterVariable.definitions.add(b4);
                            continue;
                        }
                    }
                    final Variable variable3 = new Variable();
                    if (vDef != null && !StringUtilities.isNullOrEmpty(vDef.getName())) {
                        variable3.setName(vDef.getName());
                    }
                    else {
                        variable3.setName(String.format("var_%1$d_%2$02X", slot, b4.offset));
                    }
                    variable3.setType(variableType);
                    if (vDef == null) {
                        variable3.setOriginalVariable(new VariableDefinition(slot, variable3.getName(), method, variable3.getType()));
                    }
                    else {
                        variable3.setOriginalVariable(vDef);
                    }
                    variable3.setGenerated(false);
                    final VariableInfo variableInfo2 = new VariableInfo(slot, variable3, new ArrayList<ByteCode>(), new ArrayList<ByteCode>());
                    variableInfo2.definitions.add(b4);
                    newVariables.add(variableInfo2);
                }
                for (final ByteCode ref : references) {
                    final ByteCode[] refDefinitions = ref.variablesBefore[slot].definitions;
                    if (refDefinitions.length == 0 && parameterVariable != null) {
                        parameterVariable.references.add(ref);
                        if (parameterVariableAdded) {
                            continue;
                        }
                        newVariables.add(parameterVariable);
                        parameterVariableAdded = true;
                    }
                    else if (refDefinitions.length == 1) {
                        VariableInfo newVariable = null;
                        for (final VariableInfo v : newVariables) {
                            if (v.definitions.contains(refDefinitions[0])) {
                                newVariable = v;
                                break;
                            }
                        }
                        if (newVariable == null && parameterVariable != null) {
                            newVariable = parameterVariable;
                            if (!parameterVariableAdded) {
                                newVariables.add(parameterVariable);
                                parameterVariableAdded = true;
                            }
                        }
                        assert newVariable != null;
                        newVariable.references.add(ref);
                    }
                    else {
                        final ArrayList<VariableInfo> mergeVariables = new ArrayList<VariableInfo>();
                        for (final VariableInfo v : newVariables) {
                            boolean hasIntersection = false;
                        Label_2034:
                            for (final ByteCode b5 : v.definitions) {
                                ByteCode[] loc_12;
                                for (int loc_11 = (loc_12 = refDefinitions).length, loc_13 = 0; loc_13 < loc_11; ++loc_13) {
                                    final ByteCode b6 = loc_12[loc_13];
                                    if (b5 == b6) {
                                        hasIntersection = true;
                                        break Label_2034;
                                    }
                                }
                            }
                            if (hasIntersection) {
                                mergeVariables.add(v);
                            }
                        }
                        final ArrayList<ByteCode> mergedDefinitions = new ArrayList<ByteCode>();
                        final ArrayList<ByteCode> mergedReferences = new ArrayList<ByteCode>();
                        if (parameterVariable != null && (mergeVariables.isEmpty() || (!mergeVariables.contains(parameterVariable) && ArrayUtilities.contains(refDefinitions, parameterDefinitions[0])))) {
                            mergeVariables.add(parameterVariable);
                            parameterVariableAdded = true;
                        }
                        for (final VariableInfo v2 : mergeVariables) {
                            mergedDefinitions.addAll(v2.definitions);
                            mergedReferences.addAll(v2.references);
                        }
                        final VariableInfo mergedVariable = new VariableInfo(slot, mergeVariables.get(0).variable, mergedDefinitions, mergedReferences);
                        if (parameterVariable != null && mergeVariables.contains(parameterVariable)) {
                            parameterVariable = mergedVariable;
                            parameterVariable.variable.setOriginalParameter(parameter2);
                            parameterVariableAdded = true;
                        }
                        mergedVariable.variable.setType(this.mergeVariableType(mergeVariables));
                        mergedVariable.references.add(ref);
                        newVariables.removeAll(mergeVariables);
                        newVariables.add(mergedVariable);
                    }
                }
            }
            if (this._context.getSettings().getMergeVariables()) {
                for (final VariableInfo variable4 : newVariables) {
                    variable4.recomputeLifetime();
                }
                Collections.sort(newVariables, new Comparator<VariableInfo>() {
                    @Override
                    public int compare(@NotNull final VariableInfo o1, @NotNull final VariableInfo o2) {
                        return o1.lifetime.compareTo(o2.lifetime);
                    }
                });
                for (int j = 0; j < newVariables.size() - 1; ++j) {
                    final VariableInfo prev = newVariables.get(j);
                    if (prev.variable.getType().isPrimitive()) {
                        if (prev.variable.getOriginalVariable() == null || !prev.variable.getOriginalVariable().isFromMetadata()) {
                            for (int k = j + 1; k < newVariables.size(); ++k) {
                                final VariableInfo next = newVariables.get(k);
                                if (next.variable.getOriginalVariable().isFromMetadata() || !MetadataHelper.isSameType(prev.variable.getType(), next.variable.getType())) {
                                    break;
                                }
                                if (this.mightBeBoolean(prev) != this.mightBeBoolean(next)) {
                                    break;
                                }
                                prev.definitions.addAll(next.definitions);
                                prev.references.addAll(next.references);
                                newVariables.remove(k--);
                                prev.lifetime.setStart(Math.min(prev.lifetime.getStart(), next.lifetime.getStart()));
                                prev.lifetime.setEnd(Math.max(prev.lifetime.getEnd(), next.lifetime.getEnd()));
                            }
                        }
                    }
                }
            }
            for (final VariableInfo newVariable2 : newVariables) {
                if (newVariable2.variable.getType() == BuiltinTypes.Null) {
                    newVariable2.variable.setType(BuiltinTypes.Null);
                }
                for (final ByteCode definition : newVariable2.definitions) {
                    definition.operand = newVariable2.variable;
                }
                for (final ByteCode reference2 : newVariable2.references) {
                    reference2.operand = newVariable2.variable;
                }
            }
        }
    }
    
    private boolean mightBeBoolean(final VariableInfo info) {
        final TypeReference type = info.variable.getType();
        if (type == BuiltinTypes.Boolean) {
            return true;
        }
        if (type != BuiltinTypes.Integer) {
            return false;
        }
        for (final ByteCode b : info.definitions) {
            if (b.code != AstCode.Store || b.stackBefore.length < 1) {
                return false;
            }
            final StackSlot value = b.stackBefore[b.stackBefore.length - 1];
            ByteCode[] loc_2;
            for (int loc_1 = (loc_2 = value.definitions).length, loc_3 = 0; loc_3 < loc_1; ++loc_3) {
                final ByteCode d = loc_2[loc_3];
                switch ($SWITCH_TABLE$com$strobel$decompiler$ast$AstCode()[d.code.ordinal()]) {
                    case 19: {
                        if (!Objects.equals(d.operand, 0) && !Objects.equals(d.operand, 1)) {
                            return false;
                        }
                        break;
                    }
                    case 179:
                    case 181: {
                        if (((FieldReference)d.operand).getFieldType() != BuiltinTypes.Boolean) {
                            return false;
                        }
                        break;
                    }
                    case 219: {
                        if (d.instruction.getOpCode() != OpCode.BALOAD) {
                            return false;
                        }
                        break;
                    }
                    case 183:
                    case 184:
                    case 185:
                    case 186: {
                        if (((MethodReference)d.operand).getReturnType() != BuiltinTypes.Boolean) {
                            return false;
                        }
                        break;
                    }
                    default: {
                        return false;
                    }
                }
            }
        }
        for (final ByteCode r : info.references) {
            if (r.code == AstCode.Inc) {
                return false;
            }
        }
        return true;
    }
    
    private TypeReference mergeVariableType(final List<VariableInfo> info) {
        TypeReference result = CollectionUtilities.first(info).variable.getType();
        for (int i = 0; i < info.size(); ++i) {
            final VariableInfo variableInfo = info.get(i);
            final TypeReference t = variableInfo.variable.getType();
            if (result == BuiltinTypes.Null) {
                result = t;
            }
            else if (t != BuiltinTypes.Null) {
                result = MetadataHelper.findCommonSuperType(result, t);
            }
        }
        return (result != null) ? result : BuiltinTypes.Object;
    }
    
    private JvmType getStackType(final TypeReference type) {
        final JvmType t = type.getSimpleType();
        switch ($SWITCH_TABLE$com$strobel$assembler$metadata$JvmType()[t.ordinal()]) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 5: {
                return JvmType.Integer;
            }
            case 6:
            case 7:
            case 8: {
                return t;
            }
            default: {
                return JvmType.Object;
            }
        }
    }
    
    private boolean variablesMatch(final VariableReference v1, final VariableReference v2) {
        if (v1.getSlot() == v2.getSlot()) {
            final JvmType t1 = this.getStackType(v1.getVariableType());
            final JvmType t2 = this.getStackType(v2.getVariableType());
            return t1 == t2;
        }
        return false;
    }
    
    private static Map<String, VariableDefinition> makeVariableLookup(final VariableDefinitionCollection variables) {
        final Map<String, VariableDefinition> lookup = new HashMap<String, VariableDefinition>();
        for (final VariableDefinition variable : variables) {
            final String key = key(variable);
            if (lookup.containsKey(key)) {
                continue;
            }
            lookup.put(key, variable);
        }
        return lookup;
    }
    
    private static String key(final VariableDefinition variable) {
        final StringBuilder sb = new StringBuilder().append(variable.getSlot()).append(':');
        if (variable.hasName()) {
            sb.append(variable.getName());
        }
        else {
            sb.append("#unnamed_").append(variable.getScopeStart()).append('_').append(variable.getScopeEnd());
        }
        return sb.append(':').append(variable.getVariableType().getSignature()).toString();
    }
    
    private List<Node> convertToAst(final List<ByteCode> body, final Set<ExceptionHandler> exceptionHandlers, final int startIndex, final MutableInteger endIndex) {
        final ArrayList<Node> ast = new ArrayList<Node>();
        int headStartIndex = startIndex;
        int tailStartIndex = startIndex;
        final MutableInteger tempIndex = new MutableInteger();
        while (!exceptionHandlers.isEmpty()) {
            final TryCatchBlock tryCatchBlock = new TryCatchBlock();
            final int minTryStart = body.get(headStartIndex).offset;
            int tryStart = Integer.MAX_VALUE;
            int tryEnd = -1;
            int firstHandlerStart = -1;
            headStartIndex = tailStartIndex;
            for (final ExceptionHandler handler : exceptionHandlers) {
                final int start = handler.getTryBlock().getFirstInstruction().getOffset();
                if (start < tryStart && start >= minTryStart) {
                    tryStart = start;
                }
            }
            for (final ExceptionHandler handler : exceptionHandlers) {
                final int start = handler.getTryBlock().getFirstInstruction().getOffset();
                if (start == tryStart) {
                    final Instruction lastInstruction = handler.getTryBlock().getLastInstruction();
                    final int end = lastInstruction.getEndOffset();
                    if (end <= tryEnd) {
                        continue;
                    }
                    tryEnd = end;
                    final int handlerStart = handler.getHandlerBlock().getFirstInstruction().getOffset();
                    if (firstHandlerStart >= 0 && handlerStart >= firstHandlerStart) {
                        continue;
                    }
                    firstHandlerStart = handlerStart;
                }
            }
            final ArrayList<ExceptionHandler> handlers = new ArrayList<ExceptionHandler>();
            for (final ExceptionHandler handler2 : exceptionHandlers) {
                final int start2 = handler2.getTryBlock().getFirstInstruction().getOffset();
                final int end = handler2.getTryBlock().getLastInstruction().getEndOffset();
                if (start2 == tryStart && end == tryEnd) {
                    handlers.add(handler2);
                }
            }
            Collections.sort(handlers, new Comparator<ExceptionHandler>() {
                @Override
                public int compare(@NotNull final ExceptionHandler o1, @NotNull final ExceptionHandler o2) {
                    return Integer.compare(o1.getTryBlock().getFirstInstruction().getOffset(), o2.getTryBlock().getFirstInstruction().getOffset());
                }
            });
            int tryStartIndex;
            for (tryStartIndex = 0; tryStartIndex < body.size() && body.get(tryStartIndex).offset < tryStart; ++tryStartIndex) {}
            if (headStartIndex < tryStartIndex) {
                ast.addAll(this.convertToAst(body.subList(headStartIndex, tryStartIndex)));
            }
            final Set<ExceptionHandler> nestedHandlers = new LinkedHashSet<ExceptionHandler>();
            for (final ExceptionHandler eh : exceptionHandlers) {
                final int ts = eh.getTryBlock().getFirstInstruction().getOffset();
                final int te = eh.getTryBlock().getLastInstruction().getEndOffset();
                if ((tryStart < ts && te <= tryEnd) || (tryStart <= ts && te < tryEnd)) {
                    nestedHandlers.add(eh);
                }
            }
            exceptionHandlers.removeAll(nestedHandlers);
            int tryEndIndex;
            for (tryEndIndex = 0; tryEndIndex < body.size() && body.get(tryEndIndex).offset < tryEnd; ++tryEndIndex) {}
            final Block tryBlock = new Block();
            tempIndex.setValue(tryEndIndex);
            final List<Node> tryAst = this.convertToAst(body, nestedHandlers, tryStartIndex, tempIndex);
            if (tempIndex.getValue() > tailStartIndex) {
                tailStartIndex = tempIndex.getValue();
            }
            final Node lastInTry = CollectionUtilities.lastOrDefault(tryAst, AstBuilder.NOT_A_LABEL_OR_NOP);
            if (lastInTry == null || !lastInTry.isUnconditionalControlFlow()) {
                tryAst.add(new Expression(AstCode.Leave, null, -34, new Expression[0]));
            }
            tryBlock.getBody().addAll(tryAst);
            tryCatchBlock.setTryBlock(tryBlock);
            tailStartIndex = Math.max(tryEndIndex, tailStartIndex);
        Label_1656:
            for (int i = 0, n = handlers.size(); i < n; ++i) {
                final ExceptionHandler eh2 = handlers.get(i);
                final TypeReference catchType = eh2.getCatchType();
                final InstructionBlock handlerBlock = eh2.getHandlerBlock();
                final int handlerStart2 = handlerBlock.getFirstInstruction().getOffset();
                final int handlerEnd = (handlerBlock.getLastInstruction() != null) ? handlerBlock.getLastInstruction().getEndOffset() : this._body.getCodeSize();
                int handlersStartIndex;
                for (handlersStartIndex = tailStartIndex; handlersStartIndex < body.size() && body.get(handlersStartIndex).offset < handlerStart2; ++handlersStartIndex) {}
                int handlersEndIndex;
                for (handlersEndIndex = handlersStartIndex; handlersEndIndex < body.size() && body.get(handlersEndIndex).offset < handlerEnd; ++handlersEndIndex) {}
                tailStartIndex = Math.max(tailStartIndex, handlersEndIndex);
                if (eh2.isCatch()) {
                    for (final CatchBlock catchBlock : tryCatchBlock.getCatchBlocks()) {
                        final Expression firstExpression = CollectionUtilities.firstOrDefault(catchBlock.getSelfAndChildrenRecursive(Expression.class), new Predicate<Expression>() {
                            @Override
                            public boolean test(final Expression e) {
                                return !e.getRanges().isEmpty();
                            }
                        });
                        if (firstExpression == null) {
                            continue;
                        }
                        final int otherHandlerStart = firstExpression.getRanges().get(0).getStart();
                        if (otherHandlerStart != handlerStart2) {
                            continue;
                        }
                        catchBlock.getCaughtTypes().add(catchType);
                        final TypeReference commonCatchType = MetadataHelper.findCommonSuperType(catchBlock.getExceptionType(), catchType);
                        catchBlock.setExceptionType(commonCatchType);
                        if (catchBlock.getExceptionVariable() == null) {
                            this.updateExceptionVariable(catchBlock, eh2);
                        }
                        continue Label_1656;
                    }
                }
                final Set<ExceptionHandler> nestedHandlers2 = new LinkedHashSet<ExceptionHandler>();
                for (final ExceptionHandler e : exceptionHandlers) {
                    final int ts2 = e.getTryBlock().getFirstInstruction().getOffset();
                    final int te2 = e.getTryBlock().getLastInstruction().getOffset();
                    if (ts2 != tryStart || te2 != tryEnd) {
                        if (e == eh2) {
                            continue;
                        }
                        if (handlerStart2 > ts2 || te2 >= handlerEnd) {
                            continue;
                        }
                        nestedHandlers2.add(e);
                        final int nestedEndIndex = CollectionUtilities.firstIndexWhere(body, new Predicate<ByteCode>() {
                            @Override
                            public boolean test(final ByteCode code) {
                                return code.instruction == e.getHandlerBlock().getLastInstruction();
                            }
                        });
                        if (nestedEndIndex <= handlersEndIndex) {
                            continue;
                        }
                        handlersEndIndex = nestedEndIndex;
                    }
                }
                tailStartIndex = Math.max(tailStartIndex, handlersEndIndex);
                exceptionHandlers.removeAll(nestedHandlers2);
                tempIndex.setValue(handlersEndIndex);
                final List<Node> handlerAst = this.convertToAst(body, nestedHandlers2, handlersStartIndex, tempIndex);
                final Node lastInHandler = CollectionUtilities.lastOrDefault(handlerAst, AstBuilder.NOT_A_LABEL_OR_NOP);
                if (tempIndex.getValue() > tailStartIndex) {
                    tailStartIndex = tempIndex.getValue();
                }
                if (lastInHandler == null || !lastInHandler.isUnconditionalControlFlow()) {
                    handlerAst.add(new Expression(eh2.isCatch() ? AstCode.Leave : AstCode.EndFinally, null, -34, new Expression[0]));
                }
                if (eh2.isCatch()) {
                    final CatchBlock catchBlock2 = new CatchBlock();
                    catchBlock2.setExceptionType(catchType);
                    catchBlock2.getCaughtTypes().add(catchType);
                    catchBlock2.getBody().addAll(handlerAst);
                    this.updateExceptionVariable(catchBlock2, eh2);
                    tryCatchBlock.getCatchBlocks().add(catchBlock2);
                }
                else if (eh2.isFinally()) {
                    final ByteCode loadException = this._loadExceptions.get(eh2);
                    final Block finallyBlock = new Block();
                    finallyBlock.getBody().addAll(handlerAst);
                    tryCatchBlock.setFinallyBlock(finallyBlock);
                    final Variable exceptionTemp = new Variable();
                    exceptionTemp.setName(String.format("ex_%1$02X", handlerStart2));
                    exceptionTemp.setGenerated(true);
                    if (loadException == null || loadException.storeTo == null) {
                        final Expression finallyStart = CollectionUtilities.firstOrDefault(finallyBlock.getSelfAndChildrenRecursive(Expression.class));
                        if (PatternMatching.match(finallyStart, AstCode.Store)) {
                            finallyStart.getArguments().set(0, new Expression(AstCode.Load, exceptionTemp, -34, new Expression[0]));
                        }
                    }
                    else {
                        for (final Variable storeTo : loadException.storeTo) {
                            finallyBlock.getBody().add(0, new Expression(AstCode.Store, storeTo, -34, new Expression[] { new Expression(AstCode.Load, exceptionTemp, -34, new Expression[0]) }));
                        }
                    }
                    finallyBlock.getBody().add(0, new Expression(AstCode.Store, exceptionTemp, -34, new Expression[] { new Expression(AstCode.LoadException, this._factory.makeNamedType("java.lang.Throwable"), -34, new Expression[0]) }));
                }
            }
            exceptionHandlers.removeAll(handlers);
            final Expression first = CollectionUtilities.firstOrDefault(tryCatchBlock.getTryBlock().getSelfAndChildrenRecursive(Expression.class));
            Expression last;
            if (!tryCatchBlock.getCatchBlocks().isEmpty()) {
                final CatchBlock lastCatch = CollectionUtilities.lastOrDefault(tryCatchBlock.getCatchBlocks());
                if (lastCatch == null) {
                    last = null;
                }
                else {
                    last = CollectionUtilities.lastOrDefault(lastCatch.getSelfAndChildrenRecursive(Expression.class));
                }
            }
            else {
                final Block finallyBlock2 = tryCatchBlock.getFinallyBlock();
                if (finallyBlock2 == null) {
                    last = null;
                }
                else {
                    last = CollectionUtilities.lastOrDefault(finallyBlock2.getSelfAndChildrenRecursive(Expression.class));
                }
            }
            if (first == null && last == null) {
                continue;
            }
            ast.add(tryCatchBlock);
        }
        if (tailStartIndex < endIndex.getValue()) {
            ast.addAll(this.convertToAst(body.subList(tailStartIndex, endIndex.getValue())));
        }
        else {
            endIndex.setValue(tailStartIndex);
        }
        return ast;
    }
    
    private void updateExceptionVariable(final CatchBlock catchBlock, final ExceptionHandler handler) {
        final ByteCode loadException = this._loadExceptions.get(handler);
        final int handlerStart = handler.getHandlerBlock().getFirstInstruction().getOffset();
        if (loadException.storeTo == null || loadException.storeTo.isEmpty()) {
            catchBlock.setExceptionVariable(null);
        }
        else if (loadException.storeTo.size() == 1) {
            if (!catchBlock.getBody().isEmpty() && catchBlock.getBody().get(0) instanceof Expression && !catchBlock.getBody().get(0).getArguments().isEmpty()) {
                final Expression first = catchBlock.getBody().get(0);
                final AstCode firstCode = first.getCode();
                final Expression firstArgument = first.getArguments().get(0);
                if (firstCode == AstCode.Pop && firstArgument.getCode() == AstCode.Load && firstArgument.getOperand() == loadException.storeTo.get(0)) {
                    if (this._context.getSettings().getAlwaysGenerateExceptionVariableForCatchBlocks()) {
                        final Variable exceptionVariable = new Variable();
                        exceptionVariable.setName(String.format("ex_%1$02X", handlerStart));
                        exceptionVariable.setGenerated(true);
                        catchBlock.setExceptionVariable(exceptionVariable);
                    }
                    else {
                        catchBlock.setExceptionVariable(null);
                    }
                }
                else {
                    catchBlock.setExceptionVariable(loadException.storeTo.get(0));
                }
            }
            else {
                catchBlock.setExceptionVariable(loadException.storeTo.get(0));
            }
        }
        else {
            final Variable exceptionTemp = new Variable();
            exceptionTemp.setName(String.format("ex_%1$02X", handlerStart));
            exceptionTemp.setGenerated(true);
            catchBlock.setExceptionVariable(exceptionTemp);
            for (final Variable storeTo : loadException.storeTo) {
                catchBlock.getBody().add(0, new Expression(AstCode.Store, storeTo, -34, new Expression[] { new Expression(AstCode.Load, exceptionTemp, -34, new Expression[0]) }));
            }
        }
    }
    
    private List<Node> convertToAst(final List<ByteCode> body) {
        final ArrayList<Node> ast = new ArrayList<Node>();
        for (final ByteCode byteCode : body) {
            final Instruction originalInstruction = mappedInstruction(this._originalInstructionMap, byteCode.instruction);
            final Range codeRange = new Range(originalInstruction.getOffset(), originalInstruction.getEndOffset());
            if (byteCode.stackBefore == null) {
                continue;
            }
            if (byteCode.label != null) {
                ast.add(byteCode.label);
            }
            switch ($SWITCH_TABLE$com$strobel$decompiler$ast$AstCode()[byteCode.code.ordinal()]) {
                case 90:
                case 91:
                case 92:
                case 93:
                case 94:
                case 95:
                case 96: {
                    continue;
                }
                default: {
                    if (this._removed.contains(byteCode.instruction)) {
                        final Expression expression = new Expression(AstCode.Nop, null, -34, new Expression[0]);
                        ast.add(expression);
                        continue;
                    }
                    final Expression expression = new Expression(byteCode.code, byteCode.operand, byteCode.offset, new Expression[0]);
                    if (byteCode.code == AstCode.Inc) {
                        assert byteCode.secondOperand instanceof Integer;
                        expression.setCode(AstCode.Inc);
                        expression.getArguments().add(new Expression(AstCode.LdC, byteCode.secondOperand, byteCode.offset, new Expression[0]));
                    }
                    else if (byteCode.code == AstCode.Switch) {
                        expression.putUserData(AstKeys.SWITCH_INFO, byteCode.instruction.getOperand(0));
                    }
                    expression.getRanges().add(codeRange);
                    final int popCount = (byteCode.popCount != -1) ? byteCode.popCount : byteCode.stackBefore.length;
                    for (int i = byteCode.stackBefore.length - popCount; i < byteCode.stackBefore.length; ++i) {
                        final StackSlot slot = byteCode.stackBefore[i];
                        if (slot.value.getType().isDoubleWord()) {
                            ++i;
                        }
                        expression.getArguments().add(new Expression(AstCode.Load, slot.loadFrom, byteCode.offset, new Expression[0]));
                    }
                    if (byteCode.storeTo == null || byteCode.storeTo.isEmpty()) {
                        ast.add(expression);
                        continue;
                    }
                    if (byteCode.storeTo.size() == 1) {
                        ast.add(new Expression(AstCode.Store, byteCode.storeTo.get(0), expression.getOffset(), new Expression[] { expression }));
                        continue;
                    }
                    final Variable tempVariable = new Variable();
                    tempVariable.setName(String.format("expr_%1$02X", byteCode.offset));
                    tempVariable.setGenerated(true);
                    ast.add(new Expression(AstCode.Store, tempVariable, expression.getOffset(), new Expression[] { expression }));
                    for (int j = byteCode.storeTo.size() - 1; j >= 0; --j) {
                        ast.add(new Expression(AstCode.Store, byteCode.storeTo.get(j), -34, new Expression[] { new Expression(AstCode.Load, tempVariable, byteCode.offset, new Expression[0]) }));
                    }
                    continue;
                }
            }
        }
        return ast;
    }
    
    private static Frame createFrame(final StackSlot[] stack, final VariableSlot[] locals) {
        FrameValue[] stackValues;
        if (stack.length == 0) {
            stackValues = FrameValue.EMPTY_VALUES;
        }
        else {
            stackValues = new FrameValue[stack.length];
            for (int i = 0; i < stack.length; ++i) {
                stackValues[i] = stack[i].value;
            }
        }
        FrameValue[] variableValues;
        if (locals.length == 0) {
            variableValues = FrameValue.EMPTY_VALUES;
        }
        else {
            variableValues = new FrameValue[locals.length];
            for (int i = 0; i < locals.length; ++i) {
                variableValues[i] = locals[i].value;
            }
        }
        return new Frame(FrameType.New, variableValues, stackValues);
    }
    
    static /* synthetic */ ByteCode[] access$0() {
        return AstBuilder.EMPTY_DEFINITIONS;
    }
    
    static /* synthetic */ Frame access$1(final StackSlot[] param_0, final VariableSlot[] param_1) {
        return createFrame(param_0, param_1);
    }
    
    static /* synthetic */ Map access$2(final ControlFlowGraph param_0) {
        return createNodeMap(param_0);
    }
    
    static /* synthetic */ ControlFlowNode access$3(final ControlFlowGraph param_0, final ExceptionHandler param_1) {
        return findHandlerNode(param_0, param_1);
    }
    
    static /* synthetic */ Set access$4(final ControlFlowGraph param_0, final ControlFlowNode param_1, final boolean param_2, final Set param_3) {
        return findDominatedNodes(param_0, param_1, param_2, param_3);
    }
    
    static /* synthetic */ Logger access$5() {
        return AstBuilder.LOG;
    }
    
    static /* synthetic */ boolean access$6(final Instruction param_0, final Instruction param_1, final int param_2, final Function param_3) {
        return opCodesMatch(param_0, param_1, param_2, param_3);
    }
    
    static /* synthetic */ ControlFlowNode access$7(final ControlFlowGraph param_0, final Instruction param_1) {
        return findNode(param_0, param_1);
    }
    
    static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$assembler$ir$OperandType() {
        final int[] loc_0 = AstBuilder.$SWITCH_TABLE$com$strobel$assembler$ir$OperandType;
        if (loc_0 != null) {
            return loc_0;
        }
        final int[] loc_1 = new int[OperandType.values().length];
        try {
            loc_1[OperandType.BranchTarget.ordinal()] = 8;
        }
        catch (NoSuchFieldError loc_2) {}
        try {
            loc_1[OperandType.BranchTargetWide.ordinal()] = 9;
        }
        catch (NoSuchFieldError loc_3) {}
        try {
            loc_1[OperandType.Constant.ordinal()] = 13;
        }
        catch (NoSuchFieldError loc_4) {}
        try {
            loc_1[OperandType.DynamicCallSite.ordinal()] = 5;
        }
        catch (NoSuchFieldError loc_5) {}
        try {
            loc_1[OperandType.FieldReference.ordinal()] = 7;
        }
        catch (NoSuchFieldError loc_6) {}
        try {
            loc_1[OperandType.I1.ordinal()] = 10;
        }
        catch (NoSuchFieldError loc_7) {}
        try {
            loc_1[OperandType.I2.ordinal()] = 11;
        }
        catch (NoSuchFieldError loc_8) {}
        try {
            loc_1[OperandType.I8.ordinal()] = 12;
        }
        catch (NoSuchFieldError loc_9) {}
        try {
            loc_1[OperandType.Local.ordinal()] = 16;
        }
        catch (NoSuchFieldError loc_10) {}
        try {
            loc_1[OperandType.LocalI1.ordinal()] = 17;
        }
        catch (NoSuchFieldError loc_11) {}
        try {
            loc_1[OperandType.LocalI2.ordinal()] = 18;
        }
        catch (NoSuchFieldError loc_12) {}
        try {
            loc_1[OperandType.MethodReference.ordinal()] = 6;
        }
        catch (NoSuchFieldError loc_13) {}
        try {
            loc_1[OperandType.None.ordinal()] = 1;
        }
        catch (NoSuchFieldError loc_14) {}
        try {
            loc_1[OperandType.PrimitiveTypeCode.ordinal()] = 2;
        }
        catch (NoSuchFieldError loc_15) {}
        try {
            loc_1[OperandType.Switch.ordinal()] = 15;
        }
        catch (NoSuchFieldError loc_16) {}
        try {
            loc_1[OperandType.TypeReference.ordinal()] = 3;
        }
        catch (NoSuchFieldError loc_17) {}
        try {
            loc_1[OperandType.TypeReferenceU1.ordinal()] = 4;
        }
        catch (NoSuchFieldError loc_18) {}
        try {
            loc_1[OperandType.WideConstant.ordinal()] = 14;
        }
        catch (NoSuchFieldError loc_19) {}
        return AstBuilder.$SWITCH_TABLE$com$strobel$assembler$ir$OperandType = loc_1;
    }
    
    static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$assembler$metadata$JvmType() {
        final int[] loc_0 = AstBuilder.$SWITCH_TABLE$com$strobel$assembler$metadata$JvmType;
        if (loc_0 != null) {
            return loc_0;
        }
        final int[] loc_1 = new int[JvmType.values().length];
        try {
            loc_1[JvmType.Array.ordinal()] = 10;
        }
        catch (NoSuchFieldError loc_2) {}
        try {
            loc_1[JvmType.Boolean.ordinal()] = 1;
        }
        catch (NoSuchFieldError loc_3) {}
        try {
            loc_1[JvmType.Byte.ordinal()] = 2;
        }
        catch (NoSuchFieldError loc_4) {}
        try {
            loc_1[JvmType.Character.ordinal()] = 3;
        }
        catch (NoSuchFieldError loc_5) {}
        try {
            loc_1[JvmType.Double.ordinal()] = 8;
        }
        catch (NoSuchFieldError loc_6) {}
        try {
            loc_1[JvmType.Float.ordinal()] = 7;
        }
        catch (NoSuchFieldError loc_7) {}
        try {
            loc_1[JvmType.Integer.ordinal()] = 5;
        }
        catch (NoSuchFieldError loc_8) {}
        try {
            loc_1[JvmType.Long.ordinal()] = 6;
        }
        catch (NoSuchFieldError loc_9) {}
        try {
            loc_1[JvmType.Object.ordinal()] = 9;
        }
        catch (NoSuchFieldError loc_10) {}
        try {
            loc_1[JvmType.Short.ordinal()] = 4;
        }
        catch (NoSuchFieldError loc_11) {}
        try {
            loc_1[JvmType.TypeVariable.ordinal()] = 11;
        }
        catch (NoSuchFieldError loc_12) {}
        try {
            loc_1[JvmType.Void.ordinal()] = 13;
        }
        catch (NoSuchFieldError loc_13) {}
        try {
            loc_1[JvmType.Wildcard.ordinal()] = 12;
        }
        catch (NoSuchFieldError loc_14) {}
        return AstBuilder.$SWITCH_TABLE$com$strobel$assembler$metadata$JvmType = loc_1;
    }
    
    static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$assembler$ir$FrameValueType() {
        final int[] loc_0 = AstBuilder.$SWITCH_TABLE$com$strobel$assembler$ir$FrameValueType;
        if (loc_0 != null) {
            return loc_0;
        }
        final int[] loc_1 = new int[FrameValueType.values().length];
        try {
            loc_1[FrameValueType.Address.ordinal()] = 11;
        }
        catch (NoSuchFieldError loc_2) {}
        try {
            loc_1[FrameValueType.Double.ordinal()] = 6;
        }
        catch (NoSuchFieldError loc_3) {}
        try {
            loc_1[FrameValueType.Empty.ordinal()] = 1;
        }
        catch (NoSuchFieldError loc_4) {}
        try {
            loc_1[FrameValueType.Float.ordinal()] = 4;
        }
        catch (NoSuchFieldError loc_5) {}
        try {
            loc_1[FrameValueType.Integer.ordinal()] = 3;
        }
        catch (NoSuchFieldError loc_6) {}
        try {
            loc_1[FrameValueType.Long.ordinal()] = 5;
        }
        catch (NoSuchFieldError loc_7) {}
        try {
            loc_1[FrameValueType.Null.ordinal()] = 7;
        }
        catch (NoSuchFieldError loc_8) {}
        try {
            loc_1[FrameValueType.Reference.ordinal()] = 9;
        }
        catch (NoSuchFieldError loc_9) {}
        try {
            loc_1[FrameValueType.Top.ordinal()] = 2;
        }
        catch (NoSuchFieldError loc_10) {}
        try {
            loc_1[FrameValueType.Uninitialized.ordinal()] = 10;
        }
        catch (NoSuchFieldError loc_11) {}
        try {
            loc_1[FrameValueType.UninitializedThis.ordinal()] = 8;
        }
        catch (NoSuchFieldError loc_12) {}
        return AstBuilder.$SWITCH_TABLE$com$strobel$assembler$ir$FrameValueType = loc_1;
    }
    
    static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$decompiler$ast$AstCode() {
        final int[] loc_0 = AstBuilder.$SWITCH_TABLE$com$strobel$decompiler$ast$AstCode;
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
        return AstBuilder.$SWITCH_TABLE$com$strobel$decompiler$ast$AstCode = loc_1;
    }
    
    private static final class StackSlot
    {
        final FrameValue value;
        final ByteCode[] definitions;
        final Variable loadFrom;
        
        public StackSlot(final FrameValue value, final ByteCode[] definitions) {
            super();
            this.value = VerifyArgument.notNull(value, "value");
            this.definitions = VerifyArgument.notNull(definitions, "definitions");
            this.loadFrom = null;
        }
        
        public StackSlot(final FrameValue value, final ByteCode[] definitions, final Variable loadFrom) {
            super();
            this.value = VerifyArgument.notNull(value, "value");
            this.definitions = VerifyArgument.notNull(definitions, "definitions");
            this.loadFrom = loadFrom;
        }
        
        public static StackSlot[] modifyStack(final StackSlot[] stack, final int popCount, final ByteCode pushDefinition, final FrameValue... pushTypes) {
            VerifyArgument.notNull(stack, "stack");
            VerifyArgument.isNonNegative(popCount, "popCount");
            VerifyArgument.noNullElements(pushTypes, "pushTypes");
            final StackSlot[] newStack = new StackSlot[stack.length - popCount + pushTypes.length];
            System.arraycopy(stack, 0, newStack, 0, stack.length - popCount);
            for (int i = stack.length - popCount, j = 0; i < newStack.length; ++i, ++j) {
                newStack[i] = new StackSlot(pushTypes[j], new ByteCode[] { pushDefinition });
            }
            return newStack;
        }
        
        @Override
        public String toString() {
            return "StackSlot(" + this.value + ')';
        }
        
        @Override
        protected final StackSlot clone() {
            return new StackSlot(this.value, this.definitions.clone(), this.loadFrom);
        }
    }
    
    private static final class VariableSlot
    {
        static final VariableSlot UNKNOWN_INSTANCE;
        final ByteCode[] definitions;
        final FrameValue value;
        
        static {
            UNKNOWN_INSTANCE = new VariableSlot(FrameValue.EMPTY, AstBuilder.access$0());
        }
        
        public VariableSlot(final FrameValue value, final ByteCode[] definitions) {
            super();
            this.value = VerifyArgument.notNull(value, "value");
            this.definitions = VerifyArgument.notNull(definitions, "definitions");
        }
        
        public static VariableSlot[] cloneVariableState(final VariableSlot[] state) {
            return state.clone();
        }
        
        public static VariableSlot[] makeUnknownState(final int variableCount) {
            final VariableSlot[] unknownVariableState = new VariableSlot[variableCount];
            for (int i = 0; i < variableCount; ++i) {
                unknownVariableState[i] = VariableSlot.UNKNOWN_INSTANCE;
            }
            return unknownVariableState;
        }
        
        public final boolean isUninitialized() {
            return this.value == FrameValue.UNINITIALIZED || this.value == FrameValue.UNINITIALIZED_THIS;
        }
        
        @Override
        protected final VariableSlot clone() {
            return new VariableSlot(this.value, this.definitions.clone());
        }
    }
    
    private static final class SubroutineInfo
    {
        final Instruction start;
        final Instruction end;
        final List<Instruction> liveReferences;
        final List<Instruction> deadReferences;
        final List<ControlFlowNode> contents;
        final ControlFlowNode entryNode;
        final List<ControlFlowNode> exitNodes;
        final List<ExceptionHandler> containedHandlers;
        final ControlFlowGraph cfg;
        
        public SubroutineInfo(final ControlFlowNode entryNode, final List<ControlFlowNode> contents, final ControlFlowGraph cfg) {
            super();
            this.liveReferences = new ArrayList<Instruction>();
            this.deadReferences = new ArrayList<Instruction>();
            this.exitNodes = new ArrayList<ControlFlowNode>();
            this.containedHandlers = new ArrayList<ExceptionHandler>();
            this.start = entryNode.getStart();
            this.end = CollectionUtilities.last(contents).getEnd();
            this.entryNode = entryNode;
            this.contents = contents;
            this.cfg = cfg;
            for (final ControlFlowNode node : contents) {
                if (node.getNodeType() == ControlFlowNodeType.Normal && node.getEnd().getOpCode().isReturnFromSubroutine()) {
                    this.exitNodes.add(node);
                }
            }
        }
    }
    
    private static final class HandlerInfo
    {
        final ExceptionHandler handler;
        final ControlFlowNode handlerNode;
        final ControlFlowNode head;
        final ControlFlowNode tail;
        final List<ControlFlowNode> tryNodes;
        final List<ControlFlowNode> handlerNodes;
        
        HandlerInfo(final ExceptionHandler handler, final ControlFlowNode handlerNode, final ControlFlowNode head, final ControlFlowNode tail, final List<ControlFlowNode> tryNodes, final List<ControlFlowNode> handlerNodes) {
            super();
            this.handler = handler;
            this.handlerNode = handlerNode;
            this.head = head;
            this.tail = tail;
            this.tryNodes = tryNodes;
            this.handlerNodes = handlerNodes;
        }
    }
    
    private static final class VariableInfo
    {
        final int slot;
        final Variable variable;
        final List<ByteCode> definitions;
        final List<ByteCode> references;
        Range lifetime;
        
        VariableInfo(final int slot, final Variable variable, final List<ByteCode> definitions, final List<ByteCode> references) {
            super();
            this.slot = slot;
            this.variable = variable;
            this.definitions = definitions;
            this.references = references;
        }
        
        void recomputeLifetime() {
            int start = Integer.MAX_VALUE;
            int end = Integer.MIN_VALUE;
            for (final ByteCode d : this.definitions) {
                start = Math.min(d.offset, start);
                end = Math.max(d.offset, end);
            }
            for (final ByteCode r : this.references) {
                start = Math.min(r.offset, start);
                end = Math.max(r.offset, end);
            }
            this.lifetime = new Range(start, end);
        }
    }
    
    private static final class ByteCode
    {
        Label label;
        Instruction instruction;
        String name;
        int offset;
        int endOffset;
        AstCode code;
        Object operand;
        Object secondOperand;
        int popCount;
        int pushCount;
        ByteCode next;
        ByteCode previous;
        FrameValue type;
        StackSlot[] stackBefore;
        VariableSlot[] variablesBefore;
        List<Variable> storeTo;
        
        private ByteCode() {
            super();
            this.popCount = -1;
        }
        
        public final String name() {
            if (this.name == null) {
                this.name = String.format("#%1$04d", this.offset);
            }
            return this.name;
        }
        
        public final String makeLabelName() {
            return String.format("Label_%1$04d", this.offset);
        }
        
        public final Frame getFrameBefore() {
            return AstBuilder.access$1(this.stackBefore, this.variablesBefore);
        }
        
        public final boolean isVariableDefinition() {
            return this.code == AstCode.Store;
        }
        
        @Override
        public final String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append(this.name()).append(':');
            if (this.label != null) {
                sb.append('*');
            }
            sb.append(' ');
            sb.append(this.code.getName());
            if (this.operand != null) {
                sb.append(' ');
                if (this.operand instanceof Instruction) {
                    sb.append(String.format("#%1$04d", ((Instruction)this.operand).getOffset()));
                }
                else if (this.operand instanceof Instruction[]) {
                    Instruction[] loc_1;
                    for (int loc_0 = (loc_1 = (Instruction[])this.operand).length, loc_2 = 0; loc_2 < loc_0; ++loc_2) {
                        final Instruction instruction = loc_1[loc_2];
                        sb.append(String.format("#%1$04d", instruction.getOffset()));
                        sb.append(' ');
                    }
                }
                else if (this.operand instanceof Label) {
                    sb.append(((Label)this.operand).getName());
                }
                else if (this.operand instanceof Label[]) {
                    Label[] loc_4;
                    for (int loc_3 = (loc_4 = (Label[])this.operand).length, loc_5 = 0; loc_5 < loc_3; ++loc_5) {
                        final Label l = loc_4[loc_5];
                        sb.append(l.getName());
                        sb.append(' ');
                    }
                }
                else if (this.operand instanceof VariableReference) {
                    final VariableReference variable = (VariableReference)this.operand;
                    if (variable.hasName()) {
                        sb.append(variable.getName());
                    }
                    else {
                        sb.append("$").append(String.valueOf(variable.getSlot()));
                    }
                }
                else {
                    sb.append(this.operand);
                }
            }
            if (this.stackBefore != null) {
                sb.append(" StackBefore={");
                for (int i = 0; i < this.stackBefore.length; ++i) {
                    if (i != 0) {
                        sb.append(',');
                    }
                    final StackSlot slot = this.stackBefore[i];
                    final ByteCode[] definitions = slot.definitions;
                    for (int j = 0; j < definitions.length; ++j) {
                        if (j != 0) {
                            sb.append('|');
                        }
                        sb.append(String.format("#%1$04d", definitions[j].offset));
                    }
                }
                sb.append('}');
            }
            if (this.storeTo != null && !this.storeTo.isEmpty()) {
                sb.append(" StoreTo={");
                for (int i = 0; i < this.storeTo.size(); ++i) {
                    if (i != 0) {
                        sb.append(',');
                    }
                    sb.append(this.storeTo.get(i).getName());
                }
                sb.append('}');
            }
            if (this.variablesBefore != null) {
                sb.append(" VariablesBefore={");
                for (int i = 0; i < this.variablesBefore.length; ++i) {
                    if (i != 0) {
                        sb.append(',');
                    }
                    final VariableSlot slot2 = this.variablesBefore[i];
                    if (slot2.isUninitialized()) {
                        sb.append('?');
                    }
                    else {
                        final ByteCode[] definitions = slot2.definitions;
                        for (int j = 0; j < definitions.length; ++j) {
                            if (j != 0) {
                                sb.append('|');
                            }
                            sb.append(String.format("#%1$04d", definitions[j].offset));
                        }
                    }
                }
                sb.append('}');
            }
            return sb.toString();
        }
    }
    
    private static final class FinallyInlining
    {
        private final MethodBody _body;
        private final InstructionCollection _instructions;
        private final List<ExceptionHandler> _exceptionHandlers;
        private final Set<Instruction> _removed;
        private final Function<Instruction, Instruction> _previous;
        private final ControlFlowGraph _cfg;
        private final Map<Instruction, ControlFlowNode> _nodeMap;
        private final Map<ExceptionHandler, HandlerInfo> _handlerMap;
        private final Set<ControlFlowNode> _processedNodes;
        private final Set<ControlFlowNode> _allFinallyNodes;
        private static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$assembler$ir$OpCode;
        
        private FinallyInlining(final MethodBody body, final InstructionCollection instructions, final List<ExceptionHandler> handlers, final Set<Instruction> removedInstructions) {
            super();
            this._handlerMap = new IdentityHashMap<ExceptionHandler, HandlerInfo>();
            this._processedNodes = new LinkedHashSet<ControlFlowNode>();
            this._allFinallyNodes = new LinkedHashSet<ControlFlowNode>();
            this._body = body;
            this._instructions = instructions;
            this._exceptionHandlers = handlers;
            this._removed = removedInstructions;
            this._previous = new Function<Instruction, Instruction>() {
                @Override
                public Instruction apply(final Instruction i) {
                    return FinallyInlining.access$0(FinallyInlining.this, i);
                }
            };
            this.preProcess();
            (this._cfg = ControlFlowGraphBuilder.build(instructions, handlers)).computeDominance();
            this._cfg.computeDominanceFrontier();
            this._nodeMap = AstBuilder.access$2(this._cfg);
            final Set<ControlFlowNode> terminals = new HashSet<ControlFlowNode>();
            for (int i = 0; i < handlers.size(); ++i) {
                final ExceptionHandler handler = handlers.get(i);
                final InstructionBlock handlerBlock = handler.getHandlerBlock();
                final ControlFlowNode handlerNode = AstBuilder.access$3(this._cfg, handler);
                final ControlFlowNode head = this._nodeMap.get(handlerBlock.getFirstInstruction());
                final ControlFlowNode tryHead = this._nodeMap.get(handler.getTryBlock().getFirstInstruction());
                terminals.clear();
                for (int j = 0; j < handlers.size(); ++j) {
                    final ExceptionHandler otherHandler = handlers.get(j);
                    if (otherHandler.getTryBlock().equals(handler.getTryBlock())) {
                        terminals.add(AstBuilder.access$3(this._cfg, otherHandler));
                    }
                }
                final List<ControlFlowNode> tryNodes = new ArrayList<ControlFlowNode>(AstBuilder.access$4(this._cfg, tryHead, true, terminals));
                terminals.remove(handlerNode);
                if (handler.isFinally()) {
                    terminals.add(handlerNode.getEndFinallyNode());
                }
                final List<ControlFlowNode> handlerNodes = new ArrayList<ControlFlowNode>(AstBuilder.access$4(this._cfg, head, true, terminals));
                Collections.sort(tryNodes);
                Collections.sort(handlerNodes);
                final ControlFlowNode tail = CollectionUtilities.last(handlerNodes);
                final HandlerInfo handlerInfo = new HandlerInfo(handler, handlerNode, head, tail, tryNodes, handlerNodes);
                this._handlerMap.put(handler, handlerInfo);
                if (handler.isFinally()) {
                    this._allFinallyNodes.addAll(handlerNodes);
                }
            }
        }
        
        private static void dumpHandlerNodes(final ExceptionHandler handler, final List<ControlFlowNode> tryNodes, final List<ControlFlowNode> handlerNodes) {
            final ITextOutput output = new PlainTextOutput();
            output.writeLine(handler.toString());
            output.writeLine("Try Nodes:");
            output.indent();
            for (final ControlFlowNode node : tryNodes) {
                output.writeLine(node.toString());
            }
            output.unindent();
            output.writeLine("Handler Nodes:");
            output.indent();
            for (final ControlFlowNode node : handlerNodes) {
                output.writeLine(node.toString());
            }
            output.unindent();
            output.writeLine();
            System.out.println(output);
        }
        
        static void run(final MethodBody body, final InstructionCollection instructions, final List<ExceptionHandler> handlers, final Set<Instruction> removedInstructions) {
            Collections.reverse(handlers);
            try {
                AstBuilder.access$5().fine("Removing inlined `finally` code...");
                final FinallyInlining inlining = new FinallyInlining(body, instructions, handlers, removedInstructions);
                inlining.runCore();
            }
            finally {
                Collections.reverse(handlers);
            }
            Collections.reverse(handlers);
        }
        
        private void runCore() {
            final List<ExceptionHandler> handlers = this._exceptionHandlers;
            if (handlers.isEmpty()) {
                return;
            }
            final List<ExceptionHandler> originalHandlers = CollectionUtilities.toList(this._exceptionHandlers);
            final List<ExceptionHandler> sortedHandlers = CollectionUtilities.toList(originalHandlers);
            Collections.sort(sortedHandlers, new Comparator<ExceptionHandler>() {
                @Override
                public int compare(@NotNull final ExceptionHandler o1, @NotNull final ExceptionHandler o2) {
                    if (o1.getHandlerBlock().contains(o2.getHandlerBlock())) {
                        return -1;
                    }
                    if (o2.getHandlerBlock().contains(o1.getHandlerBlock())) {
                        return 1;
                    }
                    return Integer.compare(originalHandlers.indexOf(o1), originalHandlers.indexOf(o2));
                }
            });
            for (final ExceptionHandler handler : sortedHandlers) {
                if (handler.isFinally()) {
                    this.processFinally(handler);
                }
            }
        }
        
        private void processFinally(final ExceptionHandler handler) {
            final HandlerInfo handlerInfo = this._handlerMap.get(handler);
            Instruction first = handlerInfo.head.getStart();
            Instruction last = handlerInfo.handler.getHandlerBlock().getLastInstruction();
            if (last.getOpCode() == OpCode.ENDFINALLY) {
                first = first.getNext();
                last = this.previous(last);
            }
            else if (first.getOpCode().isStore() || first.getOpCode() == OpCode.POP) {
                first = first.getNext();
            }
            if (first == null || last == null) {
                return;
            }
            int instructionCount = 0;
            for (Instruction p = last; p != null && p.getOffset() >= first.getOffset(); p = this.previous(p)) {
                ++instructionCount;
            }
            if (instructionCount == 0 || (instructionCount == 1 && !this._removed.contains(last) && last.getOpCode().isUnconditionalBranch())) {
                return;
            }
            final Set<ControlFlowNode> toProcess = this.collectNodes(handlerInfo);
            final Set<ControlFlowNode> forbiddenNodes = new LinkedHashSet<ControlFlowNode>(this._allFinallyNodes);
            forbiddenNodes.removeAll(handlerInfo.tryNodes);
            this._processedNodes.clear();
            this.processNodes(handlerInfo, first, last, instructionCount, toProcess, forbiddenNodes);
        }
        
        private void processNodes(final HandlerInfo handlerInfo, final Instruction first, final Instruction last, final int instructionCount, final Set<ControlFlowNode> toProcess, final Set<ControlFlowNode> forbiddenNodes) {
            final ExceptionHandler handler = handlerInfo.handler;
            final ControlFlowNode tryHead = this._nodeMap.get(handler.getTryBlock().getFirstInstruction());
            final ControlFlowNode finallyTail = this._nodeMap.get(handler.getHandlerBlock().getLastInstruction());
            final List<Pair<Instruction, Instruction>> startingPoints = new ArrayList<Pair<Instruction, Instruction>>();
        Label_1044:
            for (ControlFlowNode node : toProcess) {
                final ExceptionHandler nodeHandler = node.getExceptionHandler();
                if (node.getNodeType() == ControlFlowNodeType.EndFinally) {
                    continue;
                }
                if (nodeHandler != null) {
                    node = this._nodeMap.get(nodeHandler.getHandlerBlock().getLastInstruction());
                }
                if (this._processedNodes.contains(node)) {
                    continue;
                }
                if (forbiddenNodes.contains(node)) {
                    continue;
                }
                Instruction tail = node.getEnd();
                boolean isLeave = false;
                boolean tryNext = false;
                boolean tryPrevious = false;
                if (finallyTail.getEnd().getOpCode().isReturn() || finallyTail.getEnd().getOpCode().isThrow()) {
                    isLeave = true;
                }
                if (last.getOpCode() == OpCode.GOTO || last.getOpCode() == OpCode.GOTO_W) {
                    tryNext = true;
                }
                if (tail.getOpCode().isUnconditionalBranch()) {
                    switch ($SWITCH_TABLE$com$strobel$assembler$ir$OpCode()[tail.getOpCode().ordinal()]) {
                        case 168:
                        case 200: {
                            tryPrevious = true;
                            break;
                        }
                        case 178: {
                            tail = this.previous(tail);
                            tryPrevious = true;
                            break;
                        }
                        case 173:
                        case 174:
                        case 175:
                        case 176:
                        case 177: {
                            if (finallyTail.getEnd().getOpCode().getFlowControl() != FlowControl.Return) {
                                tail = this.previous(tail);
                            }
                            tryPrevious = true;
                            break;
                        }
                        case 192: {
                            tryNext = true;
                            tryPrevious = true;
                            break;
                        }
                    }
                }
                if (tail == null) {
                    continue;
                }
                startingPoints.add(Pair.create(last, tail));
                if (tryPrevious) {
                    startingPoints.add(Pair.create(last, this.previous(tail)));
                }
                if (tryNext) {
                    startingPoints.add(Pair.create(last, tail.getNext()));
                }
                boolean matchFound = false;
                for (final Pair<Instruction, Instruction> startingPoint : startingPoints) {
                    if (forbiddenNodes.contains(this._nodeMap.get(startingPoint.getSecond()))) {
                        continue;
                    }
                    if (AstBuilder.access$6(startingPoint.getFirst(), startingPoint.getSecond(), instructionCount, this._previous)) {
                        tail = startingPoint.getSecond();
                        matchFound = true;
                        break;
                    }
                }
                startingPoints.clear();
                if (!matchFound) {
                    if (last.getOpCode() != OpCode.JSR) {
                        continue;
                    }
                    final Instruction lastInTry = handlerInfo.handler.getTryBlock().getLastInstruction();
                    if (tail != lastInTry || (lastInTry.getOpCode() != OpCode.GOTO && lastInTry.getOpCode() != OpCode.GOTO_W)) {
                        continue;
                    }
                    final Instruction target = lastInTry.getOperand(0);
                    if (target.getOpCode() != OpCode.JSR || target.getOperand(0) != last.getOperand(0)) {
                        continue;
                    }
                    target.setOpCode(OpCode.NOP);
                    target.setOperand(null);
                }
                else {
                    if (tail.getOffset() - tryHead.getOffset() == last.getOffset() - first.getOffset() && handlerInfo.tryNodes.contains(node)) {
                        continue;
                    }
                    for (int i = 0; i < instructionCount; ++i) {
                        this._removed.add(tail);
                        tail = this.previous(tail);
                        if (tail == null) {
                            continue Label_1044;
                        }
                    }
                    if (isLeave) {
                        if (tail != null && tail.getOpCode().isStore() && !this._body.getMethod().getReturnType().isVoid()) {
                            final Instruction load = InstructionHelper.reverseLoadOrStore(tail);
                            final Instruction returnSite = node.getEnd();
                            final Instruction loadSite = returnSite.getPrevious();
                            loadSite.setOpCode(load.getOpCode());
                            if (load.getOperandCount() == 1) {
                                loadSite.setOperand(load.getOperand(0));
                            }
                            switch (load.getOpCode().name().charAt(0)) {
                                case 'I': {
                                    returnSite.setOpCode(OpCode.IRETURN);
                                    break;
                                }
                                case 'L': {
                                    returnSite.setOpCode(OpCode.LRETURN);
                                    break;
                                }
                                case 'F': {
                                    returnSite.setOpCode(OpCode.FRETURN);
                                    break;
                                }
                                case 'D': {
                                    returnSite.setOpCode(OpCode.DRETURN);
                                    break;
                                }
                                case 'A': {
                                    returnSite.setOpCode(OpCode.ARETURN);
                                    break;
                                }
                            }
                            returnSite.setOperand(null);
                            this._removed.remove(loadSite);
                            this._removed.remove(returnSite);
                        }
                        else {
                            this._removed.add(node.getEnd());
                        }
                    }
                    this._processedNodes.add(node);
                }
            }
        }
        
        private Set<ControlFlowNode> collectNodes(final HandlerInfo handlerInfo) {
            final ControlFlowGraph cfg = this._cfg;
            final List<ControlFlowNode> successors = new ArrayList<ControlFlowNode>();
            final Set<ControlFlowNode> toProcess = new LinkedHashSet<ControlFlowNode>();
            final ControlFlowNode endFinallyNode = handlerInfo.handlerNode.getEndFinallyNode();
            final Set<ControlFlowNode> exitOnlySuccessors = new LinkedHashSet<ControlFlowNode>();
            final InstructionBlock tryBlock = handlerInfo.handler.getTryBlock();
            if (endFinallyNode != null) {
                successors.add(handlerInfo.handlerNode);
            }
            for (final ControlFlowNode exit : cfg.getRegularExit().getPredecessors()) {
                if (exit.getNodeType() == ControlFlowNodeType.Normal && tryBlock.contains(exit.getEnd())) {
                    toProcess.add(exit);
                }
            }
            for (final ControlFlowNode exit : cfg.getExceptionalExit().getPredecessors()) {
                if (exit.getNodeType() == ControlFlowNodeType.Normal && tryBlock.contains(exit.getEnd())) {
                    toProcess.add(exit);
                }
            }
            for (int i = 0; i < successors.size(); ++i) {
                final ControlFlowNode successor = successors.get(i);
                for (final ControlFlowEdge edge : successor.getIncoming()) {
                    if (edge.getSource() == successor) {
                        continue;
                    }
                    if (edge.getType() == JumpType.Normal && edge.getSource().getNodeType() == ControlFlowNodeType.Normal && !exitOnlySuccessors.contains(successor)) {
                        toProcess.add(edge.getSource());
                    }
                    else if (edge.getType() == JumpType.JumpToExceptionHandler && edge.getSource().getNodeType() == ControlFlowNodeType.Normal && (edge.getSource().getEnd().getOpCode().isThrow() || edge.getSource().getEnd().getOpCode().isReturn())) {
                        toProcess.add(edge.getSource());
                        if (!exitOnlySuccessors.contains(successor)) {
                            continue;
                        }
                        exitOnlySuccessors.add(edge.getSource());
                    }
                    else if (edge.getSource().getNodeType() == ControlFlowNodeType.CatchHandler) {
                        final ControlFlowNode endCatch = AstBuilder.access$7(cfg, edge.getSource().getExceptionHandler().getHandlerBlock().getLastInstruction());
                        if (!handlerInfo.handler.getTryBlock().contains(endCatch.getEnd())) {
                            continue;
                        }
                        toProcess.add(endCatch);
                    }
                    else if (edge.getSource().getNodeType() == ControlFlowNodeType.FinallyHandler) {
                        successors.add(edge.getSource());
                        exitOnlySuccessors.add(edge.getSource());
                    }
                    else {
                        if (edge.getSource().getNodeType() != ControlFlowNodeType.EndFinally) {
                            continue;
                        }
                        successors.add(edge.getSource());
                        final HandlerInfo precedingFinally = CollectionUtilities.firstOrDefault(this._handlerMap.values(), new Predicate<HandlerInfo>() {
                            @Override
                            public boolean test(final HandlerInfo o) {
                                return o.handlerNode.getEndFinallyNode() == edge.getSource();
                            }
                        });
                        if (precedingFinally == null) {
                            continue;
                        }
                        successors.add(precedingFinally.handlerNode);
                        exitOnlySuccessors.remove(precedingFinally.handlerNode);
                    }
                }
            }
            List<ControlFlowNode> finallyNodes = null;
            for (final ControlFlowNode node : toProcess) {
                if (this._allFinallyNodes.contains(node)) {
                    if (finallyNodes == null) {
                        finallyNodes = new ArrayList<ControlFlowNode>();
                    }
                    finallyNodes.add(node);
                }
            }
            if (finallyNodes != null) {
                toProcess.removeAll(finallyNodes);
                toProcess.addAll(finallyNodes);
            }
            return toProcess;
        }
        
        private void preProcess() {
            final InstructionCollection instructions = this._instructions;
            final List<ExceptionHandler> handlers = this._exceptionHandlers;
            final ControlFlowGraph cfg = ControlFlowGraphBuilder.build(instructions, handlers);
            cfg.computeDominance();
            cfg.computeDominanceFrontier();
        Label_0247:
            for (int i = 0; i < handlers.size(); ++i) {
                final ExceptionHandler handler = handlers.get(i);
                if (handler.isFinally()) {
                    final InstructionBlock handlerBlock = handler.getHandlerBlock();
                    final ControlFlowNode finallyHead = AstBuilder.access$7(cfg, handler.getHandlerBlock().getFirstInstruction());
                    final List<ControlFlowNode> finallyNodes = CollectionUtilities.toList((Iterable<ControlFlowNode>)AstBuilder.access$4(cfg, finallyHead, true, Collections.emptySet()));
                    Collections.sort(finallyNodes);
                    final Instruction first = handlerBlock.getFirstInstruction();
                    Instruction last = CollectionUtilities.last(finallyNodes).getEnd();
                    Instruction nextToLast = last.getPrevious();
                    boolean firstPass = true;
                    while (!first.getOpCode().isStore() || last.getOpCode() != OpCode.ATHROW || !nextToLast.getOpCode().isLoad() || InstructionHelper.getLoadOrStoreSlot(first) != InstructionHelper.getLoadOrStoreSlot(nextToLast)) {
                        if (firstPass = !firstPass) {
                            continue Label_0247;
                        }
                        last = handlerBlock.getLastInstruction();
                        nextToLast = last.getPrevious();
                    }
                    nextToLast.setOpCode(OpCode.NOP);
                    nextToLast.setOperand(null);
                    this._removed.add(nextToLast);
                    last.setOpCode(OpCode.ENDFINALLY);
                    last.setOperand(null);
                }
            }
        }
        
        private Instruction previous(final Instruction i) {
            Instruction p;
            for (p = i.getPrevious(); p != null && this._removed.contains(p); p = p.getPrevious()) {}
            return p;
        }
        
        static /* synthetic */ Instruction access$0(final FinallyInlining param_0, final Instruction param_1) {
            return param_0.previous(param_1);
        }
        
        static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$assembler$ir$OpCode() {
            final int[] loc_0 = FinallyInlining.$SWITCH_TABLE$com$strobel$assembler$ir$OpCode;
            if (loc_0 != null) {
                return loc_0;
            }
            final int[] loc_1 = new int[OpCode.values().length];
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
            return FinallyInlining.$SWITCH_TABLE$com$strobel$assembler$ir$OpCode = loc_1;
        }
    }
}
