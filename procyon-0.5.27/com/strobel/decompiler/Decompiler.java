package com.strobel.decompiler;

import com.strobel.core.*;
import com.strobel.assembler.*;
import com.strobel.decompiler.languages.java.*;
import com.strobel.assembler.metadata.*;

public final class Decompiler
{
    public static void decompile(final String internalName, final ITextOutput output) {
        decompile(internalName, output, new DecompilerSettings());
    }
    
    public static void decompile(final String internalName, final ITextOutput output, final DecompilerSettings settings) {
        VerifyArgument.notNull(internalName, "internalName");
        VerifyArgument.notNull(settings, "settings");
        final ITypeLoader typeLoader = (settings.getTypeLoader() != null) ? settings.getTypeLoader() : new InputTypeLoader();
        final MetadataSystem metadataSystem = new MetadataSystem(typeLoader);
        TypeReference type;
        if (internalName.length() == 1) {
            final MetadataParser parser = new MetadataParser(IMetadataResolver.EMPTY);
            final TypeReference reference = parser.parseTypeDescriptor(internalName);
            type = metadataSystem.resolve(reference);
        }
        else {
            type = metadataSystem.lookupType(internalName);
        }
        final TypeDefinition resolvedType;
        if (type == null || (resolvedType = type.resolve()) == null) {
            output.writeLine("!!! ERROR: Failed to load class %s.", internalName);
            return;
        }
        DeobfuscationUtilities.processType(resolvedType);
        final DecompilationOptions options = new DecompilationOptions();
        options.setSettings(settings);
        options.setFullDecompilation(true);
        if (settings.getFormattingOptions() == null) {
            settings.setFormattingOptions(JavaFormattingOptions.createDefault());
        }
        settings.getLanguage().decompileType(resolvedType, output, options);
    }
}
