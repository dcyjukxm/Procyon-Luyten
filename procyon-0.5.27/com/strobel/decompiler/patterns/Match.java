package com.strobel.decompiler.patterns;

import com.strobel.core.*;
import com.strobel.annotations.*;
import java.util.*;

public final class Match
{
    private static final Match FAILURE;
    private final List<Pair<String, INode>> _results;
    
    static {
        FAILURE = new Match(null);
    }
    
    private Match(final List<Pair<String, INode>> results) {
        super();
        this._results = results;
    }
    
    public final boolean success() {
        return this._results != null;
    }
    
    public final void add(final String groupName, final INode node) {
        if (groupName != null && node != null) {
            this._results.add(Pair.create(groupName, node));
        }
    }
    
    public final boolean has(final String groupName) {
        for (int i = 0; i < this._results.size(); ++i) {
            if (StringUtilities.equals(groupName, this._results.get(i).getFirst())) {
                return true;
            }
        }
        return false;
    }
    
    public final <T extends INode> Iterable<T> get(final String groupName) {
        if (this._results == null) {
            return (Iterable<T>)Collections.emptyList();
        }
        return new Iterable<T>() {
            final /* synthetic */ Match this$0;
            
            @NotNull
            @Override
            public final Iterator<T> iterator() {
                return new Iterator<T>() {
                    int index = 0;
                    boolean ready;
                    T next;
                    
                    @Override
                    public boolean hasNext() {
                        if (!this.ready) {
                            this.selectNext();
                        }
                        return this.ready;
                    }
                    
                    private void selectNext() {
                        while (this.index < Match.access$0(Match$1.access$0(Iterable.this)).size()) {
                            final Pair<String, INode> pair = Match.access$0(Match$1.access$0(Iterable.this)).get(this.index);
                            if (StringUtilities.equals(groupName, pair.getFirst())) {
                                this.next = (T)pair.getSecond();
                                this.ready = true;
                                ++this.index;
                                return;
                            }
                            ++this.index;
                        }
                    }
                    
                    @Override
                    public T next() {
                        if (!this.ready) {
                            this.selectNext();
                        }
                        if (this.ready) {
                            final T result = this.next;
                            this.next = null;
                            this.ready = false;
                            return result;
                        }
                        throw new NoSuchElementException();
                    }
                    
                    @Override
                    public void remove() {
                    }
                };
            }
            
            static /* synthetic */ Match access$0(final Match$1 param_0) {
                return param_0.this$0;
            }
        };
    }
    
    final int getCheckPoint() {
        return this._results.size();
    }
    
    final void restoreCheckPoint(final int checkpoint) {
        for (int i = this._results.size() - 1; i >= checkpoint; --i) {
            this._results.remove(i);
        }
    }
    
    public static Match createNew() {
        return new Match(new ArrayList<Pair<String, INode>>());
    }
    
    public static Match failure() {
        return Match.FAILURE;
    }
    
    static /* synthetic */ List access$0(final Match param_0) {
        return param_0._results;
    }
}
