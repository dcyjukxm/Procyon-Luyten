package com.strobel.decompiler.ast;

import com.strobel.util.*;

final class Error
{
    private Error() {
        super();
        throw ContractUtils.unreachable();
    }
    
    public static RuntimeException expressionLinkedFromMultipleLocations(final Node node) {
        return new IllegalStateException("Expression is linked from several locations: " + node);
    }
    
    public static RuntimeException unsupportedNode(final Node node) {
        final String nodeType = (node != null) ? node.getClass().getName() : String.valueOf(node);
        return new IllegalStateException("Unsupported node type: " + nodeType);
    }
}
