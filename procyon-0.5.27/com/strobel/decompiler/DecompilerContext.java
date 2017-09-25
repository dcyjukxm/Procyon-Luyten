package com.strobel.decompiler;

import com.strobel.componentmodel.*;
import com.strobel.core.*;
import com.strobel.assembler.metadata.*;
import com.strobel.assembler.*;
import java.util.*;

public final class DecompilerContext extends UserDataStoreBase
{
    private final List<String> _reservedVariableNames;
    private final Set<IMemberDefinition> _forcedVisibleMembers;
    private DecompilerSettings _settings;
    private BooleanBox _isCanceled;
    private TypeDefinition _currentType;
    private MethodDefinition _currentMethod;
    
    public DecompilerContext() {
        super();
        this._reservedVariableNames = new Collection<String>();
        this._forcedVisibleMembers = new LinkedHashSet<IMemberDefinition>();
        this._settings = new DecompilerSettings();
    }
    
    public DecompilerContext(final DecompilerSettings settings) {
        super();
        this._reservedVariableNames = new Collection<String>();
        this._forcedVisibleMembers = new LinkedHashSet<IMemberDefinition>();
        this._settings = new DecompilerSettings();
        this._settings = settings;
    }
    
    public DecompilerSettings getSettings() {
        return this._settings;
    }
    
    public void setSettings(final DecompilerSettings settings) {
        this._settings = settings;
    }
    
    public BooleanBox getCanceled() {
        return this._isCanceled;
    }
    
    public void setCanceled(final BooleanBox canceled) {
        this._isCanceled = canceled;
    }
    
    public TypeDefinition getCurrentType() {
        return this._currentType;
    }
    
    public void setCurrentType(final TypeDefinition currentType) {
        this._currentType = currentType;
    }
    
    public MethodDefinition getCurrentMethod() {
        return this._currentMethod;
    }
    
    public void setCurrentMethod(final MethodDefinition currentMethod) {
        this._currentMethod = currentMethod;
    }
    
    public List<String> getReservedVariableNames() {
        return this._reservedVariableNames;
    }
    
    public Set<IMemberDefinition> getForcedVisibleMembers() {
        return this._forcedVisibleMembers;
    }
}
