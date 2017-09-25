package com.strobel.decompiler.patterns;

import com.strobel.core.*;
import java.util.*;
import com.strobel.annotations.*;

public final class Choice extends Pattern implements Iterable<INode>
{
    private final ArrayList<INode> _alternatives;
    
    public Choice() {
        super();
        this._alternatives = new ArrayList<INode>();
    }
    
    public Choice(final INode... alternatives) {
        super();
        Collections.addAll(this._alternatives = new ArrayList<INode>(), (INode[])VerifyArgument.notNull((T[])alternatives, "alternatives"));
    }
    
    public final void add(final INode alternative) {
        this._alternatives.add(VerifyArgument.notNull(alternative, "alternative"));
    }
    
    public final void add(final String name, final INode alternative) {
        this._alternatives.add(new NamedNode(name, VerifyArgument.notNull(alternative, "alternative")));
    }
    
    @NotNull
    @Override
    public final Iterator<INode> iterator() {
        return this._alternatives.iterator();
    }
    
    @Override
    public final boolean matches(final INode other, final Match match) {
        final int checkpoint = match.getCheckPoint();
        for (final INode alternative : this._alternatives) {
            if (alternative.matches(other, match)) {
                return true;
            }
            match.restoreCheckPoint(checkpoint);
        }
        return false;
    }
}
