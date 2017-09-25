package com.strobel.assembler.ir;

import com.strobel.core.*;
import java.util.*;
import com.strobel.assembler.metadata.*;

public final class StackMapAnalyzer
{
    private static IMetadataResolver getResolver(final MethodBody body) {
        final MethodReference method = body.getMethod();
        if (method != null) {
            final MethodDefinition resolvedMethod = method.resolve();
            if (resolvedMethod != null) {
                final TypeDefinition declaringType = resolvedMethod.getDeclaringType();
                if (declaringType != null) {
                    return declaringType.getResolver();
                }
            }
        }
        return MetadataSystem.instance();
    }
    
    public static List<StackMapFrame> computeStackMapTable(final MethodBody body) {
        VerifyArgument.notNull(body, "body");
        final InstructionCollection instructions = body.getInstructions();
        final List<ExceptionHandler> exceptionHandlers = body.getExceptionHandlers();
        if (instructions.isEmpty()) {
            return Collections.emptyList();
        }
        final StackMappingVisitor stackMappingVisitor = new StackMappingVisitor();
        final InstructionVisitor executor = stackMappingVisitor.visitBody(body);
        final Set<Instruction> agenda = new LinkedHashSet<Instruction>();
        final Map<Instruction, Frame> frames = new IdentityHashMap<Instruction, Frame>();
        final Set<Instruction> branchTargets = new LinkedHashSet<Instruction>();
        final IMetadataResolver resolver = getResolver(body);
        final TypeReference throwableType = resolver.lookupType("java/lang/Throwable");
        for (final ExceptionHandler handler : exceptionHandlers) {
            final Instruction handlerStart = handler.getHandlerBlock().getFirstInstruction();
            branchTargets.add(handlerStart);
            frames.put(handlerStart, new Frame(FrameType.New, FrameValue.EMPTY_VALUES, new FrameValue[] { FrameValue.makeReference(handler.isCatch() ? handler.getCatchType() : throwableType) }));
        }
        final ParameterDefinition thisParameter = body.getThisParameter();
        final boolean hasThis = thisParameter != null;
        if (hasThis) {
            stackMappingVisitor.set(0, thisParameter.getParameterType());
        }
        for (final ParameterDefinition parameter : body.getMethod().getParameters()) {
            stackMappingVisitor.set(parameter.getSlot(), parameter.getParameterType());
        }
        final Instruction firstInstruction = instructions.get(0);
        final Frame initialFrame = stackMappingVisitor.buildFrame();
        agenda.add(firstInstruction);
        frames.put(firstInstruction, initialFrame);
        while (!agenda.isEmpty()) {
            final Instruction instruction = agenda.iterator().next();
            final Frame inputFrame = frames.get(instruction);
            assert inputFrame != null;
            agenda.remove(instruction);
            stackMappingVisitor.visitFrame(inputFrame);
            executor.visit(instruction);
            final Frame outputFrame = stackMappingVisitor.buildFrame();
            final OpCode opCode = instruction.getOpCode();
            final OperandType operandType = opCode.getOperandType();
            if (!opCode.isUnconditionalBranch()) {
                final Instruction nextInstruction = instruction.getNext();
                if (nextInstruction != null) {
                    pruneLocals(stackMappingVisitor, nextInstruction, body.getVariables());
                    final boolean changed = updateFrame(nextInstruction, inputFrame, stackMappingVisitor.buildFrame(), stackMappingVisitor.getInitializations(), frames);
                    if (changed) {
                        agenda.add(nextInstruction);
                    }
                    stackMappingVisitor.visitFrame(outputFrame);
                }
            }
            if (operandType == OperandType.BranchTarget || operandType == OperandType.BranchTargetWide) {
                final Instruction branchTarget = instruction.getOperand(0);
                assert branchTarget != null;
                pruneLocals(stackMappingVisitor, branchTarget, body.getVariables());
                final boolean changed = updateFrame(branchTarget, inputFrame, stackMappingVisitor.buildFrame(), stackMappingVisitor.getInitializations(), frames);
                if (changed) {
                    agenda.add(branchTarget);
                }
                branchTargets.add(branchTarget);
                stackMappingVisitor.visitFrame(outputFrame);
            }
            else if (operandType == OperandType.Switch) {
                final SwitchInfo switchInfo = instruction.getOperand(0);
                final Instruction defaultTarget = switchInfo.getDefaultTarget();
                assert defaultTarget != null;
                pruneLocals(stackMappingVisitor, defaultTarget, body.getVariables());
                boolean changed2 = updateFrame(defaultTarget, inputFrame, stackMappingVisitor.buildFrame(), stackMappingVisitor.getInitializations(), frames);
                if (changed2) {
                    agenda.add(defaultTarget);
                }
                branchTargets.add(defaultTarget);
                stackMappingVisitor.visitFrame(outputFrame);
                Instruction[] loc_3;
                for (int loc_2 = (loc_3 = switchInfo.getTargets()).length, loc_4 = 0; loc_4 < loc_2; ++loc_4) {
                    final Instruction branchTarget2 = loc_3[loc_4];
                    assert branchTarget2 != null;
                    pruneLocals(stackMappingVisitor, branchTarget2, body.getVariables());
                    changed2 = updateFrame(branchTarget2, inputFrame, stackMappingVisitor.buildFrame(), stackMappingVisitor.getInitializations(), frames);
                    if (changed2) {
                        agenda.add(branchTarget2);
                    }
                    branchTargets.add(branchTarget2);
                    stackMappingVisitor.visitFrame(outputFrame);
                }
            }
            if (!opCode.canThrow()) {
                continue;
            }
            final ExceptionHandler handler2 = findInnermostExceptionHandler(exceptionHandlers, instruction.getOffset());
            if (handler2 == null) {
                continue;
            }
            final Instruction handlerStart2 = handler2.getHandlerBlock().getFirstInstruction();
            while (stackMappingVisitor.getStackSize() > 0) {
                stackMappingVisitor.pop();
            }
            if (handler2.isCatch()) {
                stackMappingVisitor.push(handler2.getCatchType());
            }
            else {
                stackMappingVisitor.push(throwableType);
            }
            pruneLocals(stackMappingVisitor, handlerStart2, body.getVariables());
            boolean changed2 = updateFrame(handlerStart2, inputFrame, stackMappingVisitor.buildFrame(), stackMappingVisitor.getInitializations(), frames);
            if (!changed2) {
                continue;
            }
            agenda.add(handlerStart2);
        }
        final StackMapFrame[] framesInStackMap = new StackMapFrame[branchTargets.size()];
        int i = 0;
        for (final Instruction branchTarget3 : branchTargets) {
            framesInStackMap[i++] = new StackMapFrame(frames.get(branchTarget3), branchTarget3);
        }
        Arrays.sort(framesInStackMap, new Comparator<StackMapFrame>() {
            @Override
            public int compare(final StackMapFrame o1, final StackMapFrame o2) {
                return Integer.compare(o1.getStartInstruction().getOffset(), o2.getStartInstruction().getOffset());
            }
        });
        Frame lastFrame = initialFrame;
        for (i = 0; i < framesInStackMap.length; ++i) {
            final StackMapFrame frame = framesInStackMap[i];
            final Frame deltaFrame = Frame.computeDelta(lastFrame, frame.getFrame());
            framesInStackMap[i] = new StackMapFrame(deltaFrame, frame.getStartInstruction());
            lastFrame = frame.getFrame();
        }
        return ArrayUtilities.asUnmodifiableList(framesInStackMap);
    }
    
    private static boolean pruneLocals(final StackMappingVisitor stackMappingVisitor, final Instruction target, final VariableDefinitionCollection variables) {
        boolean changed = false;
        for (int i = 0, n = stackMappingVisitor.getLocalCount(); i < n; ++i) {
            final VariableDefinition v = variables.tryFind(i, target.getOffset());
            if (v == null) {
                stackMappingVisitor.set(i, FrameValue.OUT_OF_SCOPE);
                changed = true;
            }
        }
        if (changed) {
            stackMappingVisitor.pruneLocals();
            return true;
        }
        return false;
    }
    
    private static boolean updateFrame(final Instruction instruction, final Frame inputFrame, final Frame outputFrame, final Map<Instruction, TypeReference> initializations, final Map<Instruction, Frame> frames) {
        final Frame oldFrame = frames.get(instruction);
        if (oldFrame == null) {
            frames.put(instruction, outputFrame);
            return true;
        }
        assert oldFrame.getStackValues().size() == outputFrame.getStackValues().size();
        final Frame mergedFrame = Frame.merge(inputFrame, outputFrame, oldFrame, initializations);
        frames.put(instruction, mergedFrame);
        return mergedFrame != oldFrame;
    }
    
    private static ExceptionHandler findInnermostExceptionHandler(final List<ExceptionHandler> exceptionHandlers, final int offsetInTryBlock) {
        for (final ExceptionHandler handler : exceptionHandlers) {
            final InstructionBlock tryBlock = handler.getTryBlock();
            final InstructionBlock handlerBlock = handler.getHandlerBlock();
            if (tryBlock.getFirstInstruction().getOffset() <= offsetInTryBlock && offsetInTryBlock < handlerBlock.getFirstInstruction().getOffset()) {
                return handler;
            }
        }
        return null;
    }
}
