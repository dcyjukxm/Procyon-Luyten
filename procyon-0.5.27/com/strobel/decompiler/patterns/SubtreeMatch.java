package com.strobel.decompiler.patterns;

import com.strobel.decompiler.utilities.*;
import com.strobel.core.*;
import java.util.*;

public final class SubtreeMatch extends Pattern
{
    private final boolean _matchMultiple;
    private final INode _target;
    
    public SubtreeMatch(final INode target) {
        this(target, false);
    }
    
    public SubtreeMatch(final INode target, final boolean matchMultiple) {
        super();
        this._matchMultiple = matchMultiple;
        this._target = VerifyArgument.notNull(target, "target");
    }
    
    public final INode getTarget() {
        return this._target;
    }
    
    @Override
    public final boolean matches(final INode other, final Match match) {
        if (this._matchMultiple) {
            boolean result = false;
            for (final INode n : TreeTraversal.preOrder(other, INode.CHILD_ITERATOR)) {
                if (this._target.matches(n, match)) {
                    result = true;
                }
            }
            return result;
        }
        return CollectionUtilities.any(TreeTraversal.preOrder(other, INode.CHILD_ITERATOR), new Predicate<INode>() {
            @Override
            public boolean test(final INode n) {
                return SubtreeMatch.access$0(SubtreeMatch.this).matches(n, match);
            }
        });
    }
    
    static /* synthetic */ INode access$0(final SubtreeMatch param_0) {
        return param_0._target;
    }
}
