package com.strobel.decompiler.ast;

import com.strobel.decompiler.*;
import com.strobel.core.*;
import java.util.*;

public abstract class Node
{
    public static final Node NULL;
    
    static {
        NULL = new Node() {
            @Override
            public void writeTo(final ITextOutput output) {
                output.writeKeyword("null");
            }
        };
    }
    
    public abstract void writeTo(final ITextOutput param_0);
    
    @Override
    public String toString() {
        final PlainTextOutput output = new PlainTextOutput();
        this.writeTo(output);
        return output.toString();
    }
    
    public final boolean isConditionalControlFlow() {
        return this instanceof Expression && ((Expression)this).getCode().isConditionalControlFlow();
    }
    
    public final boolean isUnconditionalControlFlow() {
        return this instanceof Expression && ((Expression)this).getCode().isUnconditionalControlFlow();
    }
    
    public List<Node> getChildren() {
        return Collections.emptyList();
    }
    
    public final List<Node> getSelfAndChildrenRecursive() {
        final ArrayList<Node> results = new ArrayList<Node>();
        this.accumulateSelfAndChildrenRecursive(results, Node.class, null, false);
        return results;
    }
    
    public final List<Node> getSelfAndChildrenRecursive(final Predicate<Node> predicate) {
        final ArrayList<Node> results = new ArrayList<Node>();
        this.accumulateSelfAndChildrenRecursive(results, Node.class, predicate, false);
        return results;
    }
    
    public final <T extends Node> List<T> getSelfAndChildrenRecursive(final Class<T> type) {
        final ArrayList<T> results = new ArrayList<T>();
        this.accumulateSelfAndChildrenRecursive(results, type, null, false);
        return results;
    }
    
    public final <T extends Node> List<T> getSelfAndChildrenRecursive(final Class<T> type, final Predicate<T> predicate) {
        final ArrayList<T> results = new ArrayList<T>();
        this.accumulateSelfAndChildrenRecursive(results, type, predicate, false);
        return results;
    }
    
    public final List<Node> getChildrenAndSelfRecursive() {
        final ArrayList<Node> results = new ArrayList<Node>();
        this.accumulateSelfAndChildrenRecursive(results, Node.class, null, true);
        return results;
    }
    
    public final List<Node> getChildrenAndSelfRecursive(final Predicate<Node> predicate) {
        final ArrayList<Node> results = new ArrayList<Node>();
        this.accumulateSelfAndChildrenRecursive(results, Node.class, predicate, true);
        return results;
    }
    
    public final <T extends Node> List<T> getChildrenAndSelfRecursive(final Class<T> type) {
        final ArrayList<T> results = new ArrayList<T>();
        this.accumulateSelfAndChildrenRecursive(results, type, null, true);
        return results;
    }
    
    public final <T extends Node> List<T> getChildrenAndSelfRecursive(final Class<T> type, final Predicate<T> predicate) {
        final ArrayList<T> results = new ArrayList<T>();
        this.accumulateSelfAndChildrenRecursive(results, type, predicate, true);
        return results;
    }
    
    private <T extends Node> void accumulateSelfAndChildrenRecursive(final List<T> list, final Class<T> type, final Predicate<T> predicate, final boolean childrenFirst) {
        if (!childrenFirst && type.isInstance(this) && (predicate == null || predicate.test((T)this))) {
            list.add((T)this);
        }
        for (final Node child : this.getChildren()) {
            child.accumulateSelfAndChildrenRecursive(list, type, predicate, childrenFirst);
        }
        if (childrenFirst && type.isInstance(this) && (predicate == null || predicate.test((T)this))) {
            list.add((T)this);
        }
    }
}
