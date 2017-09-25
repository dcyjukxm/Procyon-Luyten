package com.strobel.assembler.ir;

import com.strobel.core.*;

public final class StackMapFrame
{
    private final Frame _frame;
    private final Instruction _startInstruction;
    
    public StackMapFrame(final Frame frame, final Instruction startInstruction) {
        super();
        this._frame = VerifyArgument.notNull(frame, "frame");
        this._startInstruction = VerifyArgument.notNull(startInstruction, "startInstruction");
    }
    
    public final Frame getFrame() {
        return this._frame;
    }
    
    public final Instruction getStartInstruction() {
        return this._startInstruction;
    }
    
    @Override
    public final String toString() {
        return String.format("#%1$04d: %2$s", this._startInstruction.getOffset(), this._frame);
    }
}
