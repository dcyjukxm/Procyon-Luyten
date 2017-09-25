package com.strobel.assembler.ir;

import com.strobel.assembler.*;
import com.strobel.annotations.*;
import java.util.*;
import com.strobel.core.*;

public final class InstructionCollection extends Collection<Instruction>
{
    public Instruction atOffset(final int offset) {
        final Instruction result = this.tryGetAtOffset(offset);
        if (result != null) {
            return result;
        }
        throw new IndexOutOfBoundsException("No instruction found at offset " + offset + '.');
    }
    
    public Instruction tryGetAtOffset(final int offset) {
        final int index = Collections.binarySearch(this, new Instruction(offset, OpCode.NOP), new Comparator<Instruction>() {
            @Override
            public int compare(@NotNull final Instruction o1, @NotNull final Instruction o2) {
                return Integer.compare(o1.getOffset(), o2.getOffset());
            }
        });
        if (index >= 0) {
            return this.get(index);
        }
        final Instruction last = CollectionUtilities.lastOrDefault((Iterable<Instruction>)this);
        if (last != null && last.getNext() != null && last.getNext().getOffset() == offset) {
            return last.getNext();
        }
        return null;
    }
    
    @Override
    protected void afterAdd(final int index, final Instruction item, final boolean appended) {
        final Instruction next = (index < this.size() - 1) ? this.get(index + 1) : null;
        final Instruction previous = (index > 0) ? this.get(index - 1) : null;
        if (previous != null) {
            previous.setNext(item);
        }
        if (next != null) {
            next.setPrevious(item);
        }
        item.setPrevious(previous);
        item.setNext(next);
    }
    
    @Override
    protected void beforeSet(final int index, final Instruction item) {
        final Instruction current = this.get(index);
        item.setPrevious(current.getPrevious());
        item.setNext(current.getNext());
        current.setPrevious(null);
        current.setNext(null);
    }
    
    @Override
    protected void afterRemove(final int index, final Instruction item) {
        final Instruction current = item.getNext();
        final Instruction previous = item.getPrevious();
        if (previous != null) {
            previous.setNext(current);
        }
        if (current != null) {
            current.setPrevious(previous);
        }
        item.setPrevious(null);
        item.setNext(null);
    }
    
    @Override
    protected void beforeClear() {
        for (int i = 0; i < this.size(); ++i) {
            this.get(i).setNext(null);
            this.get(i).setPrevious(null);
        }
    }
    
    public void recomputeOffsets() {
        if (this.isEmpty()) {
            return;
        }
        Instruction previous = this.get(0);
        previous.setOffset(0);
        for (int i = 1; i < this.size(); ++i) {
            final Instruction current = this.get(i);
            current.setOffset(previous.getOffset() + previous.getSize());
            previous = current;
        }
    }
}
