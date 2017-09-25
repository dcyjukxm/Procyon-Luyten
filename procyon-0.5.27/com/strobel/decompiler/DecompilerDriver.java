package com.strobel.decompiler;

import com.beust.jcommander.*;
import com.strobel.core.*;
import com.strobel.assembler.*;
import com.strobel.decompiler.languages.java.*;
import java.util.logging.*;
import java.util.jar.*;
import java.util.*;
import com.strobel.assembler.metadata.*;
import com.strobel.decompiler.languages.*;
import java.nio.charset.*;
import java.io.*;
import com.strobel.io.*;

public class DecompilerDriver
{
    public static void main(final String[] args) {
        final CommandLineOptions options = new CommandLineOptions();
        JCommander jCommander;
        List<String> typeNames;
        try {
            jCommander = new JCommander(options);
            jCommander.setAllowAbbreviatedOptions(true);
            jCommander.parse(args);
            typeNames = options.getInputs();
        }
        catch (Throwable t) {
            System.err.println(ExceptionUtilities.getMessage(t));
            System.exit(-1);
            return;
        }
        configureLogging(options);
        final String jarFile = options.getJarFile();
        final boolean decompileJar = !StringUtilities.isNullOrWhitespace(jarFile);
        if (options.getPrintUsage() || (typeNames.isEmpty() && !decompileJar)) {
            jCommander.usage();
            return;
        }
        final DecompilerSettings settings = new DecompilerSettings();
        settings.setFlattenSwitchBlocks(options.getFlattenSwitchBlocks());
        settings.setForceExplicitImports(options.getForceExplicitImports());
        settings.setForceExplicitTypeArguments(options.getForceExplicitTypeArguments());
        settings.setRetainRedundantCasts(options.getRetainRedundantCasts());
        settings.setShowSyntheticMembers(options.getShowSyntheticMembers());
        settings.setExcludeNestedTypes(options.getExcludeNestedTypes());
        settings.setOutputDirectory(options.getOutputDirectory());
        settings.setIncludeLineNumbersInBytecode(options.getIncludeLineNumbers());
        settings.setRetainPointlessSwitches(options.getRetainPointlessSwitches());
        settings.setUnicodeOutputEnabled(options.isUnicodeOutputEnabled());
        settings.setMergeVariables(options.getMergeVariables());
        settings.setShowDebugLineNumbers(options.getShowDebugLineNumbers());
        settings.setSimplifyMemberReferences(options.getSimplifyMemberReferences());
        settings.setDisableForEachTransforms(options.getDisableForEachTransforms());
        settings.setTypeLoader(new InputTypeLoader());
        if (options.isRawBytecode()) {
            settings.setLanguage(Languages.bytecode());
        }
        else if (options.isBytecodeAst()) {
            settings.setLanguage(options.isUnoptimized() ? Languages.bytecodeAstUnoptimized() : Languages.bytecodeAst());
        }
        final DecompilationOptions decompilationOptions = new DecompilationOptions();
        decompilationOptions.setSettings(settings);
        decompilationOptions.setFullDecompilation(true);
        if (settings.getFormattingOptions() == null) {
            settings.setFormattingOptions(JavaFormattingOptions.createDefault());
        }
        if (decompileJar) {
            try {
                decompileJar(jarFile, options, decompilationOptions);
            }
            catch (Throwable t2) {
                System.err.println(ExceptionUtilities.getMessage(t2));
                System.exit(-1);
            }
        }
        else {
            final MetadataSystem metadataSystem = new NoRetryMetadataSystem(settings.getTypeLoader());
            metadataSystem.setEagerMethodLoadingEnabled(options.isEagerMethodLoadingEnabled());
            for (final String typeName : typeNames) {
                try {
                    if (typeName.endsWith(".jar")) {
                        decompileJar(typeName, options, decompilationOptions);
                    }
                    else {
                        decompileType(metadataSystem, typeName, options, decompilationOptions, true);
                    }
                }
                catch (Throwable t3) {
                    t3.printStackTrace();
                }
            }
        }
    }
    
    private static void configureLogging(final CommandLineOptions options) {
        final Logger globalLogger = Logger.getGlobal();
        final Logger rootLogger = Logger.getAnonymousLogger().getParent();
        Handler[] loc_1;
        for (int loc_0 = (loc_1 = globalLogger.getHandlers()).length, loc_2 = 0; loc_2 < loc_0; ++loc_2) {
            final Handler handler = loc_1[loc_2];
            globalLogger.removeHandler(handler);
        }
        Handler[] loc_4;
        for (int loc_3 = (loc_4 = rootLogger.getHandlers()).length, loc_5 = 0; loc_5 < loc_3; ++loc_5) {
            final Handler handler = loc_4[loc_5];
            rootLogger.removeHandler(handler);
        }
        Level verboseLevel = null;
        switch (options.getVerboseLevel()) {
            case 0: {
                verboseLevel = Level.SEVERE;
                break;
            }
            case 1: {
                verboseLevel = Level.FINE;
                break;
            }
            case 2: {
                verboseLevel = Level.FINER;
                break;
            }
            default: {
                verboseLevel = Level.FINEST;
                break;
            }
        }
        globalLogger.setLevel(verboseLevel);
        rootLogger.setLevel(verboseLevel);
        final ConsoleHandler handler2 = new ConsoleHandler();
        handler2.setLevel(verboseLevel);
        handler2.setFormatter(new BriefLogFormatter());
        globalLogger.addHandler(handler2);
        rootLogger.addHandler(handler2);
    }
    
    private static void decompileJar(final String jarFilePath, final CommandLineOptions options, final DecompilationOptions decompilationOptions) throws IOException {
        final File jarFile = new File(jarFilePath);
        if (!jarFile.exists()) {
            throw new FileNotFoundException("File not found: " + jarFilePath);
        }
        final DecompilerSettings settings = decompilationOptions.getSettings();
        final JarFile jar = new JarFile(jarFile);
        final Enumeration<JarEntry> entries = jar.entries();
        final boolean oldShowSyntheticMembers = settings.getShowSyntheticMembers();
        final ITypeLoader oldTypeLoader = settings.getTypeLoader();
        settings.setShowSyntheticMembers(false);
        settings.setTypeLoader(new CompositeTypeLoader(new ITypeLoader[] { new JarTypeLoader(jar), settings.getTypeLoader() }));
        try {
            MetadataSystem metadataSystem = new NoRetryMetadataSystem(settings.getTypeLoader());
            metadataSystem.setEagerMethodLoadingEnabled(options.isEagerMethodLoadingEnabled());
            int classesDecompiled = 0;
            while (entries.hasMoreElements()) {
                final JarEntry entry = entries.nextElement();
                final String name = entry.getName();
                if (!name.endsWith(".class")) {
                    continue;
                }
                final String internalName = StringUtilities.removeRight(name, ".class");
                try {
                    decompileType(metadataSystem, internalName, options, decompilationOptions, false);
                    if (++classesDecompiled % 100 != 0) {
                        continue;
                    }
                    metadataSystem = new NoRetryMetadataSystem(settings.getTypeLoader());
                }
                catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }
        finally {
            settings.setShowSyntheticMembers(oldShowSyntheticMembers);
            settings.setTypeLoader(oldTypeLoader);
        }
        settings.setShowSyntheticMembers(oldShowSyntheticMembers);
        settings.setTypeLoader(oldTypeLoader);
    }
    
    private static void decompileType(final MetadataSystem metadataSystem, final String typeName, final CommandLineOptions commandLineOptions, final DecompilationOptions options, final boolean includeNested) throws IOException {
        final DecompilerSettings settings = options.getSettings();
        TypeReference type;
        if (typeName.length() == 1) {
            final MetadataParser parser = new MetadataParser(IMetadataResolver.EMPTY);
            final TypeReference reference = parser.parseTypeDescriptor(typeName);
            type = metadataSystem.resolve(reference);
        }
        else {
            type = metadataSystem.lookupType(typeName);
        }
        final TypeDefinition resolvedType;
        if (type == null || (resolvedType = type.resolve()) == null) {
            System.err.printf("!!! ERROR: Failed to load class %s.\n", typeName);
            return;
        }
        DeobfuscationUtilities.processType(resolvedType);
        if (!includeNested && (resolvedType.isNested() || resolvedType.isAnonymous() || resolvedType.isSynthetic())) {
            return;
        }
        final Writer writer = createWriter(resolvedType, settings);
        final boolean writeToFile = writer instanceof FileOutputWriter;
        PlainTextOutput output;
        if (writeToFile) {
            output = new PlainTextOutput(writer);
        }
        else {
            output = new AnsiTextOutput(writer, commandLineOptions.getUseLightColorScheme() ? AnsiTextOutput.ColorScheme.LIGHT : AnsiTextOutput.ColorScheme.DARK);
        }
        output.setUnicodeOutputEnabled(settings.isUnicodeOutputEnabled());
        if (settings.getLanguage() instanceof BytecodeLanguage) {
            output.setIndentToken("  ");
        }
        if (writeToFile) {
            System.out.printf("Decompiling %s...\n", typeName);
        }
        final TypeDecompilationResults results = settings.getLanguage().decompileType(resolvedType, output, options);
        writer.flush();
        writer.close();
        final List<LineNumberPosition> lineNumberPositions = results.getLineNumberPositions();
        if ((commandLineOptions.getIncludeLineNumbers() || commandLineOptions.getStretchLines()) && writer instanceof FileOutputWriter) {
            final EnumSet<LineNumberFormatter.LineNumberOption> lineNumberOptions = EnumSet.noneOf(LineNumberFormatter.LineNumberOption.class);
            if (commandLineOptions.getIncludeLineNumbers()) {
                lineNumberOptions.add(LineNumberFormatter.LineNumberOption.LEADING_COMMENTS);
            }
            if (commandLineOptions.getStretchLines()) {
                lineNumberOptions.add(LineNumberFormatter.LineNumberOption.STRETCHED);
            }
            final LineNumberFormatter lineFormatter = new LineNumberFormatter(((FileOutputWriter)writer).getFile(), lineNumberPositions, lineNumberOptions);
            lineFormatter.reformatFile();
        }
    }
    
    private static Writer createWriter(final TypeDefinition type, final DecompilerSettings settings) throws IOException {
        final String outputDirectory = settings.getOutputDirectory();
        if (StringUtilities.isNullOrWhitespace(outputDirectory)) {
            return new OutputStreamWriter(System.out, settings.isUnicodeOutputEnabled() ? Charset.forName("UTF-8") : Charset.defaultCharset());
        }
        final String fileName = String.valueOf(type.getName()) + settings.getLanguage().getFileExtension();
        final String packageName = type.getPackageName();
        String outputPath;
        if (StringUtilities.isNullOrWhitespace(packageName)) {
            outputPath = PathHelper.combine(outputDirectory, fileName);
        }
        else {
            outputPath = PathHelper.combine(outputDirectory, packageName.replace('.', PathHelper.DirectorySeparator), fileName);
        }
        final File outputFile = new File(outputPath);
        final File parentFile = outputFile.getParentFile();
        if (parentFile != null && !parentFile.exists() && !parentFile.mkdirs()) {
            throw new IllegalStateException(String.format("Could not create output directory for file \"%s\".", outputPath));
        }
        if (!outputFile.exists() && !outputFile.createNewFile()) {
            throw new IllegalStateException(String.format("Could not create output file \"%s\".", outputPath));
        }
        return new FileOutputWriter(outputFile, settings);
    }
}
