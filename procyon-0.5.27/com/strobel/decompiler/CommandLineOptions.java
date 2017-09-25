package com.strobel.decompiler;

import com.beust.jcommander.*;
import java.util.*;

public class CommandLineOptions
{
    @Parameter(description = "<type names or class/jar files>")
    private final List<String> _inputs;
    @Parameter(names = { "-?", "--help" }, help = true, description = "Display this usage information and exit.")
    private boolean _printUsage;
    @Parameter(names = { "-mv", "--merge-variables" }, description = "Attempt to merge as many variables as possible.  This may lead to fewer declarations, but at the expense of inlining and useful naming.  This feature is experimental and may be removed or become the standard behavior in future releases.")
    private boolean _mergeVariables;
    @Parameter(names = { "-ei", "--explicit-imports" }, description = "Force explicit type imports (i.e., never import '*').")
    private boolean _forceExplicitImports;
    @Parameter(names = { "-eta", "--explicit-type-arguments" }, description = "Always print type arguments to generic methods.")
    private boolean _forceExplicitTypeArguments;
    @Parameter(names = { "-ec", "--retain-explicit-casts" }, description = "Do not remove redundant explicit casts.")
    private boolean _retainRedundantCasts;
    @Parameter(names = { "-fsb", "--flatten-switch-blocks" }, description = "Drop the braces statements around switch sections when possible.")
    private boolean _flattenSwitchBlocks;
    @Parameter(names = { "-ss", "--show-synthetic" }, description = "Show synthetic (compiler-generated) members.")
    private boolean _showSyntheticMembers;
    @Parameter(names = { "-b", "--bytecode-ast" }, description = "Output Bytecode AST instead of Java.")
    private boolean _bytecodeAst;
    @Parameter(names = { "-r", "--raw-bytecode" }, description = "Output Raw Bytecode instead of Java.")
    private boolean _rawBytecode;
    @Parameter(names = { "-u", "--unoptimized" }, description = "Show unoptimized code (only in combination with -b).")
    private boolean _unoptimized;
    @Parameter(names = { "-ent", "--exclude-nested" }, description = "Exclude nested types when decompiling their enclosing types.")
    private boolean _excludeNestedTypes;
    @Parameter(names = { "-o", "--output-directory" }, description = "Write decompiled results to specified directory instead of the console.")
    private String _outputDirectory;
    @Parameter(names = { "-jar", "--jar-file" }, description = "Decompile all classes in the specified jar file (disables -ent and -s) [DEPRECATED].")
    private String _jarFile;
    @Parameter(names = { "-ln", "--with-line-numbers" }, description = "Include line numbers in raw bytecode mode; supports Java mode with -o only.")
    private boolean _includeLineNumbers;
    @Parameter(names = { "-sl", "--stretch-lines" }, description = "Stretch Java lines to match original line numbers (only in combination with -o) [EXPERIMENTAL].")
    private boolean _stretchLines;
    @Parameter(names = { "-dl", "--debug-line-numbers" }, description = "For debugging, show Java line numbers as inline comments (implies -ln; requires -o).")
    private boolean _showDebugLineNumbers;
    @Parameter(names = { "-ps", "--retain-pointless-switches" }, description = "Do not lift the contents of switches having only a default label.")
    private boolean _retainPointlessSwitches;
    @Parameter(names = { "-v", "--verbose" }, description = "Set the level of log verbosity (0-3).  Level 0 disables logging.", arity = 1)
    private int _verboseLevel;
    @Parameter(names = { "-lc", "--light" }, description = "Use a color scheme designed for consoles with light background colors.")
    private boolean _useLightColorScheme;
    @Parameter(names = { "--unicode" }, description = "Enable Unicode output (printable non-ASCII characters will not be escaped).")
    private boolean _isUnicodeOutputEnabled;
    @Parameter(names = { "-eml", "--eager-method-loading" }, description = "Enable eager loading of method bodies (may speed up decompilation of larger archives).")
    private boolean _isEagerMethodLoadingEnabled;
    @Parameter(names = { "-sm", "--simplify-member-references" }, description = "Simplify type-qualified member references in Java output [EXPERIMENTAL].")
    private boolean _simplifyMemberReferences;
    @Parameter(names = { "--disable-foreach" }, description = "Disable 'for each' loop transforms.")
    private boolean _disableForEachTransforms;
    
    public CommandLineOptions() {
        super();
        this._inputs = new ArrayList<String>();
    }
    
    public final List<String> getInputs() {
        return this._inputs;
    }
    
    public final boolean isBytecodeAst() {
        return this._bytecodeAst;
    }
    
    public final boolean isRawBytecode() {
        return this._rawBytecode;
    }
    
    public final boolean getFlattenSwitchBlocks() {
        return this._flattenSwitchBlocks;
    }
    
    public final boolean getExcludeNestedTypes() {
        return this._excludeNestedTypes;
    }
    
    public final void setExcludeNestedTypes(final boolean excludeNestedTypes) {
        this._excludeNestedTypes = excludeNestedTypes;
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
    
    public boolean getRetainRedundantCasts() {
        return this._retainRedundantCasts;
    }
    
    public void setRetainRedundantCasts(final boolean retainRedundantCasts) {
        this._retainRedundantCasts = retainRedundantCasts;
    }
    
    public final void setRawBytecode(final boolean rawBytecode) {
        this._rawBytecode = rawBytecode;
    }
    
    public final void setBytecodeAst(final boolean bytecodeAst) {
        this._bytecodeAst = bytecodeAst;
    }
    
    public final boolean isUnoptimized() {
        return this._unoptimized;
    }
    
    public final void setUnoptimized(final boolean unoptimized) {
        this._unoptimized = unoptimized;
    }
    
    public final boolean getShowSyntheticMembers() {
        return this._showSyntheticMembers;
    }
    
    public final void setShowSyntheticMembers(final boolean showSyntheticMembers) {
        this._showSyntheticMembers = showSyntheticMembers;
    }
    
    public final boolean getPrintUsage() {
        return this._printUsage;
    }
    
    public final void setPrintUsage(final boolean printUsage) {
        this._printUsage = printUsage;
    }
    
    public final String getOutputDirectory() {
        return this._outputDirectory;
    }
    
    public final void setOutputDirectory(final String outputDirectory) {
        this._outputDirectory = outputDirectory;
    }
    
    public final String getJarFile() {
        return this._jarFile;
    }
    
    public final void setJarFile(final String jarFile) {
        this._jarFile = jarFile;
    }
    
    public final boolean getIncludeLineNumbers() {
        return this._includeLineNumbers;
    }
    
    public final void setIncludeLineNumbers(final boolean includeLineNumbers) {
        this._includeLineNumbers = includeLineNumbers;
    }
    
    public final boolean getStretchLines() {
        return this._stretchLines;
    }
    
    public final void setStretchLines(final boolean stretchLines) {
        this._stretchLines = stretchLines;
    }
    
    public final boolean getShowDebugLineNumbers() {
        return this._showDebugLineNumbers;
    }
    
    public final void setShowDebugLineNumbers(final boolean showDebugLineNumbers) {
        this._showDebugLineNumbers = showDebugLineNumbers;
    }
    
    public final boolean getRetainPointlessSwitches() {
        return this._retainPointlessSwitches;
    }
    
    public final void setRetainPointlessSwitches(final boolean retainPointlessSwitches) {
        this._retainPointlessSwitches = retainPointlessSwitches;
    }
    
    public final int getVerboseLevel() {
        return this._verboseLevel;
    }
    
    public final void setVerboseLevel(final int verboseLevel) {
        this._verboseLevel = verboseLevel;
    }
    
    public final boolean getUseLightColorScheme() {
        return this._useLightColorScheme;
    }
    
    public final void setUseLightColorScheme(final boolean useLightColorScheme) {
        this._useLightColorScheme = useLightColorScheme;
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
    
    public final boolean isEagerMethodLoadingEnabled() {
        return this._isEagerMethodLoadingEnabled;
    }
    
    public final void setEagerMethodLoadingEnabled(final boolean isEagerMethodLoadingEnabled) {
        this._isEagerMethodLoadingEnabled = isEagerMethodLoadingEnabled;
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
}
