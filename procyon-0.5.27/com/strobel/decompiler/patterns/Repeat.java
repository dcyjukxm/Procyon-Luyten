package com.strobel.decompiler.patterns;

import com.strobel.core.*;
import java.util.*;

public final class Repeat extends Pattern
{
    private final INode _node;
    private int _minCount;
    private int _maxCount;
    
    public Repeat(final INode node) {
        super();
        this._node = VerifyArgument.notNull(node, "node");
        this._minCount = 0;
        this._maxCount = Integer.MAX_VALUE;
    }
    
    public final INode getNode() {
        return this._node;
    }
    
    public final int getMinCount() {
        return this._minCount;
    }
    
    public final void setMinCount(final int minCount) {
        this._minCount = minCount;
    }
    
    public final int getMaxCount() {
        return this._maxCount;
    }
    
    public final void setMaxCount(final int maxCount) {
        this._maxCount = maxCount;
    }
    
    @Override
    public final boolean matchesCollection(final Role role, final INode position, final Match match, final BacktrackingInfo backtrackingInfo) {
        final Stack<PossibleMatch> backtrackingStack = backtrackingInfo.stack;
        assert position.getRole() == role;
        int matchCount = 0;
        INode current = position;
        if (this._minCount <= 0) {
            backtrackingStack.push(new PossibleMatch(current, match.getCheckPoint()));
        }
        while (matchCount < this._maxCount && current != null && this._node.matches(current, match)) {
            ++matchCount;
            do {
                current = current.getNextSibling();
            } while (current != null && current.getRole() != role);
            if (matchCount >= this._minCount) {
                backtrackingStack.push(new PossibleMatch(current, match.getCheckPoint()));
            }
        }
        return false;
    }
    
    @Override
    public boolean matches(final INode other, final Match match) {
        if (other == null || other.isNull()) {
            return this._minCount <= 0;
        }
        return this._maxCount >= 1 && this._node.matches(other, match);
    }
}
