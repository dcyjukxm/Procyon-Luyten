package com.strobel.assembler.ir;

import com.strobel.decompiler.ast.*;
import com.strobel.core.*;

public final class InstructionBlock
{
    private final Instruction _firstInstruction;
    private final Instruction _lastInstruction;
    
    public InstructionBlock(final Instruction firstInstruction, final Instruction lastInstruction) {
        super();
        this._firstInstruction = VerifyArgument.notNull(firstInstruction, "firstInstruction");
        this._lastInstruction = lastInstruction;
    }
    
    public final Instruction getFirstInstruction() {
        return this._firstInstruction;
    }
    
    public final Instruction getLastInstruction() {
        return this._lastInstruction;
    }
    
    public final boolean contains(final Instruction instruction) {
        return instruction != null && instruction.getOffset() >= this.getFirstInstruction().getOffset() && instruction.getOffset() <= this.getLastInstruction().getOffset();
    }
    
    public final boolean contains(final InstructionBlock block) {
        return block != null && block.getFirstInstruction().getOffset() >= this.getFirstInstruction().getOffset() && block.getLastInstruction().getOffset() <= this.getLastInstruction().getOffset();
    }
    
    public final boolean contains(final Range range) {
        return range != null && range.getStart() >= this.getFirstInstruction().getOffset() && range.getEnd() <= this.getLastInstruction().getEndOffset();
    }
    
    public final boolean intersects(final InstructionBlock block) {
        return block != null && block.getFirstInstruction().getOffset() <= this.getLastInstruction().getOffset() && block.getLastInstruction().getOffset() >= this.getFirstInstruction().getOffset();
    }
    
    public final boolean intersects(final Range range) {
        return range != null && range.getStart() <= this.getLastInstruction().getOffset() && range.getEnd() >= this.getFirstInstruction().getOffset();
    }
    
    @Override
    public final boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof InstructionBlock) {
            final InstructionBlock block = (InstructionBlock)o;
            return Comparer.equals(this._firstInstruction, block._firstInstruction) && Comparer.equals(this._lastInstruction, block._lastInstruction);
        }
        return false;
    }
    
    @Override
    public final int hashCode() {
        int result = (this._firstInstruction != null) ? this._firstInstruction.hashCode() : 0;
        result = 31 * result + ((this._lastInstruction != null) ? this._lastInstruction.hashCode() : 0);
        return result;
    }
    
    public static final Predicate<InstructionBlock> containsInstructionPredicate(final Instruction instruction) {
        return new Predicate<InstructionBlock>() {
            @Override
            public boolean test(final InstructionBlock b) {
                return b.contains(instruction);
            }
        };
    }
    
    public static final Predicate<InstructionBlock> containsBlockPredicate(final InstructionBlock block) {
        return new Predicate<InstructionBlock>() {
            @Override
            public boolean test(final InstructionBlock b) {
                return b.contains(block);
            }
        };
    }
}
