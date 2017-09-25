package com.strobel.assembler.ir;

import com.strobel.assembler.metadata.*;
import com.strobel.core.*;
import com.strobel.decompiler.*;

public final class ExceptionHandler implements Comparable<ExceptionHandler>
{
    private final InstructionBlock _tryBlock;
    private final InstructionBlock _handlerBlock;
    private final ExceptionHandlerType _handlerType;
    private final TypeReference _catchType;
    
    private ExceptionHandler(final InstructionBlock tryBlock, final InstructionBlock handlerBlock, final ExceptionHandlerType handlerType, final TypeReference catchType) {
        super();
        this._tryBlock = tryBlock;
        this._handlerBlock = handlerBlock;
        this._handlerType = handlerType;
        this._catchType = catchType;
    }
    
    public static ExceptionHandler createCatch(final InstructionBlock tryBlock, final InstructionBlock handlerBlock, final TypeReference catchType) {
        VerifyArgument.notNull(tryBlock, "tryBlock");
        VerifyArgument.notNull(handlerBlock, "handlerBlock");
        VerifyArgument.notNull(catchType, "catchType");
        return new ExceptionHandler(tryBlock, handlerBlock, ExceptionHandlerType.Catch, catchType);
    }
    
    public static ExceptionHandler createFinally(final InstructionBlock tryBlock, final InstructionBlock handlerBlock) {
        VerifyArgument.notNull(tryBlock, "tryBlock");
        VerifyArgument.notNull(handlerBlock, "handlerBlock");
        return new ExceptionHandler(tryBlock, handlerBlock, ExceptionHandlerType.Finally, null);
    }
    
    public final boolean isFinally() {
        return this._handlerType == ExceptionHandlerType.Finally;
    }
    
    public final boolean isCatch() {
        return this._handlerType == ExceptionHandlerType.Catch;
    }
    
    public final InstructionBlock getTryBlock() {
        return this._tryBlock;
    }
    
    public final InstructionBlock getHandlerBlock() {
        return this._handlerBlock;
    }
    
    public final ExceptionHandlerType getHandlerType() {
        return this._handlerType;
    }
    
    public final TypeReference getCatchType() {
        return this._catchType;
    }
    
    @Override
    public final String toString() {
        final PlainTextOutput output = new PlainTextOutput();
        DecompilerHelpers.writeExceptionHandler(output, this);
        return output.toString();
    }
    
    @Override
    public int compareTo(final ExceptionHandler o) {
        if (o == null) {
            return 1;
        }
        final InstructionBlock h1 = this._handlerBlock;
        final InstructionBlock h2 = o._handlerBlock;
        int result = h1.getFirstInstruction().compareTo(h2.getFirstInstruction());
        if (result != 0) {
            return result;
        }
        final InstructionBlock t1 = this._tryBlock;
        final InstructionBlock t2 = o._tryBlock;
        result = t1.getFirstInstruction().compareTo(t2.getFirstInstruction());
        if (result != 0) {
            return result;
        }
        result = t2.getLastInstruction().compareTo(t1.getLastInstruction());
        if (result != 0) {
            return result;
        }
        return h2.getLastInstruction().compareTo(h1.getLastInstruction());
    }
}
