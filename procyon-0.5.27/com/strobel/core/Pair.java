package com.strobel.core;

public final class Pair<TFirst, TSecond> implements Comparable<Pair<TFirst, TSecond>>
{
    private static final int UninitializedHashCode = Integer.MIN_VALUE;
    private static final int FirstNullHash = 1642088727;
    private static final int SecondNullHash = 428791459;
    private final TFirst _first;
    private final TSecond _second;
    private int _cachedHashCode;
    
    public Pair(final TFirst first, final TSecond second) {
        super();
        this._cachedHashCode = Integer.MIN_VALUE;
        this._first = first;
        this._second = second;
    }
    
    public final TFirst getFirst() {
        return this._first;
    }
    
    public final TSecond getSecond() {
        return this._second;
    }
    
    @Override
    public final boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Pair)) {
            return false;
        }
        final Pair<?, ?> other = (Pair<?, ?>)obj;
        return Comparer.equals(this._first, other._first) && Comparer.equals(this._second, other._second);
    }
    
    public final boolean equals(final Pair<? extends TFirst, ? extends TSecond> other) {
        return other != null && Comparer.equals(this._first, (TFirst)other._first) && Comparer.equals(this._second, (TSecond)other._second);
    }
    
    @Override
    public final int hashCode() {
        if (this._cachedHashCode != Integer.MIN_VALUE) {
            return this._cachedHashCode;
        }
        final int combinedHash = HashUtilities.combineHashCodes((this._first == null) ? 1642088727 : this._first.hashCode(), (this._second == null) ? 428791459 : this._second.hashCode());
        return this._cachedHashCode = combinedHash;
    }
    
    @Override
    public int compareTo(final Pair<TFirst, TSecond> o) {
        if (o == this) {
            return 0;
        }
        if (o == null) {
            return 1;
        }
        final int firstCompare = Comparer.compare((Object)this._first, (Object)o._first);
        if (firstCompare != 0) {
            return firstCompare;
        }
        return Comparer.compare((Object)this._second, (Object)o._second);
    }
    
    @Override
    public final String toString() {
        return String.format("(%s; %s)", this._first, this._second);
    }
    
    public static <TFirst, TSecond> Pair<TFirst, TSecond> create(final TFirst first, final TSecond second) {
        return new Pair<TFirst, TSecond>(first, second);
    }
}
