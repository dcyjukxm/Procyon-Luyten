package com.strobel.decompiler.languages.java.ast;

public final class ConvertTypeOptions
{
    private boolean _includePackage;
    private boolean _includeTypeArguments;
    private boolean _includeTypeParameterDefinitions;
    private boolean _allowWildcards;
    private boolean _addImports;
    
    public ConvertTypeOptions() {
        super();
        this._includeTypeArguments = true;
        this._includeTypeParameterDefinitions = true;
        this._allowWildcards = true;
        this._addImports = true;
    }
    
    public ConvertTypeOptions(final boolean includePackage, final boolean includeTypeParameterDefinitions) {
        super();
        this._includeTypeArguments = true;
        this._includeTypeParameterDefinitions = true;
        this._allowWildcards = true;
        this._addImports = true;
        this._includePackage = includePackage;
        this._includeTypeParameterDefinitions = includeTypeParameterDefinitions;
    }
    
    public boolean getIncludePackage() {
        return this._includePackage;
    }
    
    public void setIncludePackage(final boolean value) {
        this._includePackage = value;
    }
    
    public boolean getIncludeTypeParameterDefinitions() {
        return this._includeTypeParameterDefinitions;
    }
    
    public void setIncludeTypeParameterDefinitions(final boolean value) {
        this._includeTypeParameterDefinitions = value;
    }
    
    public boolean getAllowWildcards() {
        return this._allowWildcards;
    }
    
    public void setAllowWildcards(final boolean allowWildcards) {
        this._allowWildcards = allowWildcards;
    }
    
    public boolean getIncludeTypeArguments() {
        return this._includeTypeArguments;
    }
    
    public void setIncludeTypeArguments(final boolean includeTypeArguments) {
        this._includeTypeArguments = includeTypeArguments;
    }
    
    public boolean getAddImports() {
        return this._addImports;
    }
    
    public void setAddImports(final boolean addImports) {
        this._addImports = addImports;
    }
}
