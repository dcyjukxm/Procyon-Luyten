package com.strobel.decompiler.patterns;

public final class AnyNode extends Pattern
{
    private final String _groupName;
    
    public AnyNode() {
        super();
        this._groupName = null;
    }
    
    public AnyNode(final String groupName) {
        super();
        this._groupName = groupName;
    }
    
    public final String getGroupName() {
        return this._groupName;
    }
    
    @Override
    public final boolean matches(final INode other, final Match match) {
        match.add(this._groupName, other);
        return other != null && !other.isNull();
    }
}
