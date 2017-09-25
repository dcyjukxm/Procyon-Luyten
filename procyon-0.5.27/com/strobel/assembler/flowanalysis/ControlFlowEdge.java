package com.strobel.assembler.flowanalysis;

import com.strobel.core.*;

public final class ControlFlowEdge
{
    private final ControlFlowNode _source;
    private final ControlFlowNode _target;
    private final JumpType _type;
    private static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$assembler$flowanalysis$JumpType;
    
    public ControlFlowEdge(final ControlFlowNode source, final ControlFlowNode target, final JumpType type) {
        super();
        this._source = VerifyArgument.notNull(source, "source");
        this._target = VerifyArgument.notNull(target, "target");
        this._type = VerifyArgument.notNull(type, "type");
    }
    
    public final ControlFlowNode getSource() {
        return this._source;
    }
    
    public final ControlFlowNode getTarget() {
        return this._target;
    }
    
    public final JumpType getType() {
        return this._type;
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof ControlFlowEdge) {
            final ControlFlowEdge other = (ControlFlowEdge)obj;
            return other._source == this._source && other._target == this._target;
        }
        return false;
    }
    
    @Override
    public final String toString() {
        switch ($SWITCH_TABLE$com$strobel$assembler$flowanalysis$JumpType()[this._type.ordinal()]) {
            case 1: {
                return "#" + this._target.getBlockIndex();
            }
            case 2: {
                return "e:#" + this._target.getBlockIndex();
            }
            default: {
                return this._type + ":#" + this._target.getBlockIndex();
            }
        }
    }
    
    static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$assembler$flowanalysis$JumpType() {
        final int[] loc_0 = ControlFlowEdge.$SWITCH_TABLE$com$strobel$assembler$flowanalysis$JumpType;
        if (loc_0 != null) {
            return loc_0;
        }
        final int[] loc_1 = new int[JumpType.values().length];
        try {
            loc_1[JumpType.EndFinally.ordinal()] = 4;
        }
        catch (NoSuchFieldError loc_2) {}
        try {
            loc_1[JumpType.JumpToExceptionHandler.ordinal()] = 2;
        }
        catch (NoSuchFieldError loc_3) {}
        try {
            loc_1[JumpType.LeaveTry.ordinal()] = 3;
        }
        catch (NoSuchFieldError loc_4) {}
        try {
            loc_1[JumpType.Normal.ordinal()] = 1;
        }
        catch (NoSuchFieldError loc_5) {}
        return ControlFlowEdge.$SWITCH_TABLE$com$strobel$assembler$flowanalysis$JumpType = loc_1;
    }
}
