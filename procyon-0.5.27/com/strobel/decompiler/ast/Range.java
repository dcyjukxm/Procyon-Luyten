package com.strobel.decompiler.ast;

import com.strobel.core.*;
import java.util.*;

public final class Range implements Comparable<Range>
{
    private int _start;
    private int _end;
    
    public Range() {
        super();
    }
    
    public Range(final int start, final int end) {
        super();
        this._start = start;
        this._end = end;
    }
    
    public final int getStart() {
        return this._start;
    }
    
    public final void setStart(final int start) {
        this._start = start;
    }
    
    public final int getEnd() {
        return this._end;
    }
    
    public final void setEnd(final int end) {
        this._end = end;
    }
    
    @Override
    public final boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof Range) {
            final Range range = (Range)o;
            return range._end == this._end && range._start == this._start;
        }
        return false;
    }
    
    public final boolean contains(final int location) {
        return location >= this._start && location <= this._end;
    }
    
    public final boolean contains(final int start, final int end) {
        return start >= this._start && end <= this._end;
    }
    
    public final boolean contains(final Range range) {
        return range != null && range._start >= this._start && range._end <= this._end;
    }
    
    public final boolean intersects(final Range range) {
        return range != null && range._start <= this._end && range._end >= this._start;
    }
    
    @Override
    public final int hashCode() {
        int result = this._start;
        result = 31 * result + this._end;
        return result;
    }
    
    @Override
    public final int compareTo(final Range o) {
        if (o == null) {
            return 1;
        }
        final int compareResult = Integer.compare(this._start, o._start);
        return (compareResult != 0) ? compareResult : Integer.compare(this._end, o._end);
    }
    
    @Override
    public final String toString() {
        return String.format("Range(%d, %d)", this._start, this._end);
    }
    
    public static List<Range> orderAndJoint(final Iterable<Range> input) {
        VerifyArgument.notNull(input, "input");
        final ArrayList<Range> ranges = new ArrayList<Range>();
        for (final Range range : input) {
            if (range != null) {
                ranges.add(range);
            }
        }
        Collections.sort(ranges);
        int i = 0;
        while (i < ranges.size() - 1) {
            final Range current = ranges.get(i);
            final Range next = ranges.get(i + 1);
            if (current.getStart() <= next.getStart() && next.getStart() <= current.getEnd()) {
                current.setEnd(Math.max(current.getEnd(), next.getEnd()));
                ranges.remove(i + 1);
            }
            else {
                ++i;
            }
        }
        return ranges;
    }
    
    public static List<Range> invert(final Iterable<Range> input, final int codeSize) {
        VerifyArgument.notNull(input, "input");
        VerifyArgument.isPositive(codeSize, "codeSize");
        final List<Range> ordered = orderAndJoint(input);
        if (ordered.isEmpty()) {
            return Collections.singletonList(new Range(0, codeSize));
        }
        final List<Range> inverted = new ArrayList<Range>();
        if (ordered.get(0).getStart() != 0) {
            inverted.add(new Range(0, ordered.get(0).getStart()));
        }
        for (int i = 0; i < ordered.size() - 1; ++i) {
            inverted.add(new Range(ordered.get(i).getEnd(), ordered.get(i + 1).getStart()));
        }
        assert ordered.get(ordered.size() - 1).getEnd() <= codeSize;
        if (ordered.get(ordered.size() - 1).getEnd() != codeSize) {
            inverted.add(new Range(ordered.get(ordered.size() - 1).getEnd(), codeSize));
        }
        return inverted;
    }
}
