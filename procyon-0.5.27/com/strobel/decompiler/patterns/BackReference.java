package com.strobel.decompiler.patterns;

import com.strobel.core.*;

public final class BackReference extends Pattern
{
    private final String _referencedGroupName;
    
    public BackReference(final String referencedGroupName) {
        super();
        this._referencedGroupName = referencedGroupName;
    }
    
    public final String getReferencedGroupName() {
        return this._referencedGroupName;
    }
    
    @Override
    public final boolean matches(final INode other, final Match match) {
        final INode node = CollectionUtilities.lastOrDefault(match.get(this._referencedGroupName));
        return node != null && node.matches(other);
    }
}
