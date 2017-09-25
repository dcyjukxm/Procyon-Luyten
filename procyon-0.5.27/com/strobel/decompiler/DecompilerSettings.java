package com.strobel.decompiler;

import com.strobel.assembler.metadata.*;
import com.strobel.decompiler.languages.java.*;
import com.strobel.decompiler.languages.*;

public class DecompilerSettings
{
    private ITypeLoader _typeLoader;
    private boolean _includeLineNumbersInBytecode;
    private boolean _showSyntheticMembers;
    private boolean _alwaysGenerateExceptionVariableForCatchBlocks;
    private boolean _forceExplicitImports;
    private boolean _forceExplicitTypeArguments;
    private boolean _flattenSwitchBlocks;
    private boolean _excludeNestedTypes;
    private boolean _retainRedundantCasts;
    private boolean _retainPointlessSwitches;
    private boolean _isUnicodeOutputEnabled;
    private boolean _includeErrorDiagnostics;
    private boolean _mergeVariables;
    private boolean _disableForEachTransforms;
    private JavaFormattingOptions _formattingOptions;
    private Language _language;
    private String _outputFileHeaderText;
    private String _outputDirectory;
    private boolean _showDebugLineNumbers;
    private boolean _simplifyMemberReferences;
    
    public DecompilerSettings() {
        super();
        this._includeLineNumbersInBytecode = true;
        this._alwaysGenerateExceptionVariableForCatchBlocks = true;
        this._includeErrorDiagnostics = true;
    }
    
    public final boolean getExcludeNestedTypes() {
        return this._excludeNestedTypes;
    }
    
    public final void setExcludeNestedTypes(final boolean excludeNestedTypes) {
        this._excludeNestedTypes = excludeNestedTypes;
    }
    
    public final boolean getFlattenSwitchBlocks() {
        return this._flattenSwitchBlocks;
    }
    
    public final void setFlattenSwitchBlocks(final boolean flattenSwitchBlocks) {
        this._flattenSwitchBlocks = flattenSwitchBlocks;
    }
    
    public final boolean getForceExplicitImports() {
        return this._forceExplicitImports;
    }
    
    public final void setForceExplicitImports(final boolean forceExplicitImports) {
        this._forceExplicitImports = forceExplicitImports;
    }
    
    public final boolean getForceExplicitTypeArguments() {
        return this._forceExplicitTypeArguments;
    }
    
    public final void setForceExplicitTypeArguments(final boolean forceExplicitTypeArguments) {
        this._forceExplicitTypeArguments = forceExplicitTypeArguments;
    }
    
    public final String getOutputFileHeaderText() {
        return this._outputFileHeaderText;
    }
    
    public final void setOutputFileHeaderText(final String outputFileHeaderText) {
        this._outputFileHeaderText = outputFileHeaderText;
    }
    
    public final ITypeLoader getTypeLoader() {
        return this._typeLoader;
    }
    
    public final void setTypeLoader(final ITypeLoader typeLoader) {
        this._typeLoader = typeLoader;
    }
    
    public final Language getLanguage() {
        return (this._language != null) ? this._language : Languages.java();
    }
    
    public final void setLanguage(final Language language) {
        this._language = language;
    }
    
    public final boolean getShowSyntheticMembers() {
        return this._showSyntheticMembers;
    }
    
    public final void setShowSyntheticMembers(final boolean showSyntheticMembers) {
        this._showSyntheticMembers = showSyntheticMembers;
    }
    
    public final JavaFormattingOptions getFormattingOptions() {
        return this._formattingOptions;
    }
    
    public final void setFormattingOptions(final JavaFormattingOptions formattingOptions) {
        this._formattingOptions = formattingOptions;
    }
    
    public final boolean getAlwaysGenerateExceptionVariableForCatchBlocks() {
        return this._alwaysGenerateExceptionVariableForCatchBlocks;
    }
    
    public final void setAlwaysGenerateExceptionVariableForCatchBlocks(final boolean value) {
        this._alwaysGenerateExceptionVariableForCatchBlocks = value;
    }
    
    public final String getOutputDirectory() {
        return this._outputDirectory;
    }
    
    public final void setOutputDirectory(final String outputDirectory) {
        this._outputDirectory = outputDirectory;
    }
    
    public final boolean getRetainRedundantCasts() {
        return this._retainRedundantCasts;
    }
    
    public final void setRetainRedundantCasts(final boolean retainRedundantCasts) {
        this._retainRedundantCasts = retainRedundantCasts;
    }
    
    public final boolean getIncludeErrorDiagnostics() {
        return this._includeErrorDiagnostics;
    }
    
    public final void setIncludeErrorDiagnostics(final boolean value) {
        this._includeErrorDiagnostics = value;
    }
    
    public final boolean getIncludeLineNumbersInBytecode() {
        return this._includeLineNumbersInBytecode;
    }
    
    public final void setIncludeLineNumbersInBytecode(final boolean value) {
        this._includeLineNumbersInBytecode = value;
    }
    
    public final boolean getRetainPointlessSwitches() {
        return this._retainPointlessSwitches;
    }
    
    public final void setRetainPointlessSwitches(final boolean retainPointlessSwitches) {
        this._retainPointlessSwitches = retainPointlessSwitches;
    }
    
    public final boolean isUnicodeOutputEnabled() {
        return this._isUnicodeOutputEnabled;
    }
    
    public final void setUnicodeOutputEnabled(final boolean unicodeOutputEnabled) {
        this._isUnicodeOutputEnabled = unicodeOutputEnabled;
    }
    
    public final boolean getMergeVariables() {
        return this._mergeVariables;
    }
    
    public final void setMergeVariables(final boolean mergeVariables) {
        this._mergeVariables = mergeVariables;
    }
    
    public final void setShowDebugLineNumbers(final boolean showDebugLineNumbers) {
        this._showDebugLineNumbers = showDebugLineNumbers;
    }
    
    public final boolean getShowDebugLineNumbers() {
        return this._showDebugLineNumbers;
    }
    
    public final boolean getSimplifyMemberReferences() {
        return this._simplifyMemberReferences;
    }
    
    public final void setSimplifyMemberReferences(final boolean simplifyMemberReferences) {
        this._simplifyMemberReferences = simplifyMemberReferences;
    }
    
    public final boolean getDisableForEachTransforms() {
        return this._disableForEachTransforms;
    }
    
    public final void setDisableForEachTransforms(final boolean disableForEachTransforms) {
        this._disableForEachTransforms = disableForEachTransforms;
    }
    
    public static DecompilerSettings javaDefaults() {
        final DecompilerSettings settings = new DecompilerSettings();
        settings.setFormattingOptions(JavaFormattingOptions.createDefault());
        return settings;
    }
}
