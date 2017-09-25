package com.strobel.decompiler;

public class DecompilationOptions
{
    private boolean _fullDecompilation;
    private DecompilerSettings _settings;
    
    public DecompilationOptions() {
        super();
        this._fullDecompilation = true;
    }
    
    public final boolean isFullDecompilation() {
        return this._fullDecompilation;
    }
    
    public final void setFullDecompilation(final boolean fullDecompilation) {
        this._fullDecompilation = fullDecompilation;
    }
    
    public final DecompilerSettings getSettings() {
        if (this._settings == null) {
            this._settings = new DecompilerSettings();
        }
        return this._settings;
    }
    
    public final void setSettings(final DecompilerSettings settings) {
        this._settings = settings;
    }
}
