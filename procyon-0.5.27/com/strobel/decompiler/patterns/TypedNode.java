package com.strobel.decompiler.patterns;

import com.strobel.core.*;

public class TypedNode extends Pattern
{
    private final Class<? extends INode> _nodeType;
    private final String _groupName;
    
    public TypedNode(final Class<? extends INode> nodeType) {
        super();
        this._nodeType = VerifyArgument.notNull(nodeType, "nodeType");
        this._groupName = null;
    }
    
    public TypedNode(final String groupName, final Class<? extends INode> nodeType) {
        super();
        this._groupName = groupName;
        this._nodeType = VerifyArgument.notNull(nodeType, "nodeType");
    }
    
    public final Class<? extends INode> getNodeType() {
        return this._nodeType;
    }
    
    public final String getGroupName() {
        return this._groupName;
    }
    
    @Override
    public final boolean matches(final INode other, final Match match) {
        if (this._nodeType.isInstance(other)) {
            match.add(this._groupName, other);
            return !other.isNull();
        }
        return false;
    }
}
