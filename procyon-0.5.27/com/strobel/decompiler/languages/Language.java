package com.strobel.decompiler.languages;

import com.strobel.decompiler.*;
import java.util.*;
import com.strobel.core.*;
import com.strobel.assembler.metadata.*;

public abstract class Language
{
    public abstract String getName();
    
    public abstract String getFileExtension();
    
    public void decompilePackage(final String packageName, final Iterable<TypeDefinition> types, final ITextOutput output, final DecompilationOptions options) {
        this.writeCommentLine(output, packageName);
    }
    
    public TypeDecompilationResults decompileType(final TypeDefinition type, final ITextOutput output, final DecompilationOptions options) {
        this.writeCommentLine(output, this.typeToString(type, true));
        return new TypeDecompilationResults(null);
    }
    
    public void decompileMethod(final MethodDefinition method, final ITextOutput output, final DecompilationOptions options) {
        this.writeCommentLine(output, String.valueOf(this.typeToString(method.getDeclaringType(), true)) + "." + method.getName());
    }
    
    public void decompileField(final FieldDefinition field, final ITextOutput output, final DecompilationOptions options) {
        this.writeCommentLine(output, String.valueOf(this.typeToString(field.getDeclaringType(), true)) + "." + field.getName());
    }
    
    public void writeCommentLine(final ITextOutput output, final String comment) {
        output.writeComment("// " + comment);
        output.writeLine();
    }
    
    public String typeToString(final TypeReference type, final boolean includePackage) {
        VerifyArgument.notNull(type, "type");
        return includePackage ? type.getFullName() : type.getName();
    }
    
    public String formatTypeName(final TypeReference type) {
        return VerifyArgument.notNull(type, "type").getName();
    }
    
    public boolean isMemberBrowsable(final MemberReference member) {
        return true;
    }
    
    public String getHint(final MemberReference member) {
        if (member instanceof TypeReference) {
            return this.typeToString((TypeReference)member, true);
        }
        return member.toString();
    }
}
