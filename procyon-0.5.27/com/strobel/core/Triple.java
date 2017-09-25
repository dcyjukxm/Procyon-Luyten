package com.strobel.core;

public final class Triple<TFirst, TSecond, TThird> implements Comparable<Triple<TFirst, TSecond, TThird>>
{
    private static final int UninitializedHashCode = Integer.MIN_VALUE;
    private static final int FirstNullHash = 1642088727;
    private static final int SecondNullHash = 428791459;
    private static final int ThirdNullHash = 1090263159;
    private final TFirst _first;
    private final TSecond _second;
    private final TThird _third;
    private int _cachedHashCode;
    
    public Triple(final TFirst first, final TSecond second, final TThird third) {
        super();
        this._cachedHashCode = Integer.MIN_VALUE;
        this._first = first;
        this._second = second;
        this._third = third;
    }
    
    public final TFirst getFirst() {
        return this._first;
    }
    
    public final TSecond getSecond() {
        return this._second;
    }
    
    public final TThird getThird() {
        return this._third;
    }
    
    @Override
    public final boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Triple)) {
            return false;
        }
        final Triple<?, ?, ?> other = (Triple<?, ?, ?>)obj;
        return Comparer.equals(this._first, other._first) && Comparer.equals(this._second, other._second) && Comparer.equals(this._third, other._third);
    }
    
    public final boolean equals(final Triple<? extends TFirst, ? extends TSecond, ? extends TThird> other) {
        return other != null && Comparer.equals(this._first, (TFirst)other._first) && Comparer.equals(this._second, (TSecond)other._second) && Comparer.equals(this._third, (TThird)other._third);
    }
    
    @Override
    public final int hashCode() {
        if (this._cachedHashCode != Integer.MIN_VALUE) {
            return this._cachedHashCode;
        }
        final int combinedHash = HashUtilities.combineHashCodes((this._first == null) ? 1642088727 : this._first.hashCode(), (this._second == null) ? 428791459 : this._second.hashCode(), (this._third == null) ? 1090263159 : this._third.hashCode());
        return this._cachedHashCode = combinedHash;
    }
    
    @Override
    public int compareTo(final Triple<TFirst, TSecond, TThird> o) {
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
        final int secondCompare = Comparer.compare((Object)this._second, (Object)o._second);
        if (secondCompare != 0) {
            return secondCompare;
        }
        return Comparer.compare((Object)this._third, (Object)o._third);
    }
    
    @Override
    public final String toString() {
        return String.format("Triple[%s, %s, %s]", this._first, this._second, this._third);
    }
    
    public static <TFirst, TSecond, TThird> Triple<TFirst, TSecond, TThird> create(final TFirst first, final TSecond second, final TThird third) {
        return new Triple<TFirst, TSecond, TThird>(first, second, third);
    }
}
