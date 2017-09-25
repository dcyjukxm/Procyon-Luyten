package com.strobel.assembler.metadata;

import com.strobel.assembler.ir.*;
import com.strobel.core.*;

public final class SwitchInfo
{
    private int _lowValue;
    private int _highValue;
    private int[] _keys;
    private Instruction _defaultTarget;
    private Instruction[] _targets;
    
    public SwitchInfo() {
        super();
    }
    
    public SwitchInfo(final Instruction defaultTarget, final Instruction[] targets) {
        super();
        this._keys = null;
        this._defaultTarget = defaultTarget;
        this._targets = targets;
    }
    
    public SwitchInfo(final int[] keys, final Instruction defaultTarget, final Instruction[] targets) {
        super();
        this._keys = keys;
        this._defaultTarget = defaultTarget;
        this._targets = targets;
    }
    
    public int getLowValue() {
        return this._lowValue;
    }
    
    public void setLowValue(final int lowValue) {
        this._lowValue = lowValue;
    }
    
    public int getHighValue() {
        return this._highValue;
    }
    
    public void setHighValue(final int highValue) {
        this._highValue = highValue;
    }
    
    public boolean hasKeys() {
        return this._keys != null;
    }
    
    public int[] getKeys() {
        return this._keys;
    }
    
    public Instruction getDefaultTarget() {
        return this._defaultTarget;
    }
    
    public Instruction[] getTargets() {
        return this._targets;
    }
    
    public void setKeys(final int... keys) {
        this._keys = keys;
    }
    
    public void setDefaultTarget(final Instruction defaultTarget) {
        this._defaultTarget = defaultTarget;
    }
    
    public void setTargets(final Instruction... targets) {
        this._targets = VerifyArgument.noNullElements(targets, "targets");
    }
}
