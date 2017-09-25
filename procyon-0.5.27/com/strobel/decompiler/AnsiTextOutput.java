package com.strobel.decompiler;

import com.strobel.io.*;
import java.io.*;
import com.strobel.core.*;
import com.strobel.assembler.ir.*;
import com.strobel.decompiler.ast.*;
import com.strobel.assembler.metadata.*;

public class AnsiTextOutput extends PlainTextOutput
{
    private final Ansi _keyword;
    private final Ansi _instruction;
    private final Ansi _label;
    private final Ansi _type;
    private final Ansi _typeVariable;
    private final Ansi _package;
    private final Ansi _method;
    private final Ansi _field;
    private final Ansi _local;
    private final Ansi _literal;
    private final Ansi _textLiteral;
    private final Ansi _comment;
    private final Ansi _operator;
    private final Ansi _delimiter;
    private final Ansi _attribute;
    private final Ansi _error;
    
    public AnsiTextOutput() {
        this(new StringWriter(), ColorScheme.DARK);
    }
    
    public AnsiTextOutput(final ColorScheme colorScheme) {
        this(new StringWriter(), colorScheme);
    }
    
    public AnsiTextOutput(final Writer writer) {
        this(writer, ColorScheme.DARK);
    }
    
    public AnsiTextOutput(final Writer writer, final ColorScheme colorScheme) {
        super(writer);
        final boolean light = colorScheme == ColorScheme.LIGHT;
        this._keyword = new Ansi(Ansi.Attribute.NORMAL, new Ansi.AnsiColor(light ? 21 : 33), null);
        this._instruction = new Ansi(Ansi.Attribute.NORMAL, new Ansi.AnsiColor(light ? 91 : 141), null);
        this._label = new Ansi(Ansi.Attribute.NORMAL, new Ansi.AnsiColor(light ? 249 : 249), null);
        this._type = new Ansi(Ansi.Attribute.NORMAL, new Ansi.AnsiColor(light ? 25 : 45), null);
        this._typeVariable = new Ansi(Ansi.Attribute.NORMAL, new Ansi.AnsiColor(light ? 29 : 79), null);
        this._package = new Ansi(Ansi.Attribute.NORMAL, new Ansi.AnsiColor(light ? 32 : 111), null);
        this._method = new Ansi(Ansi.Attribute.NORMAL, new Ansi.AnsiColor(light ? 162 : 212), null);
        this._field = new Ansi(Ansi.Attribute.NORMAL, new Ansi.AnsiColor(light ? 136 : 222), null);
        this._local = new Ansi(Ansi.Attribute.NORMAL, null, (Ansi.AnsiColor)null);
        this._literal = new Ansi(Ansi.Attribute.NORMAL, new Ansi.AnsiColor(light ? 197 : 204), null);
        this._textLiteral = new Ansi(Ansi.Attribute.NORMAL, new Ansi.AnsiColor(light ? 28 : 42), null);
        this._comment = new Ansi(Ansi.Attribute.NORMAL, new Ansi.AnsiColor(light ? 244 : 244), null);
        this._operator = new Ansi(Ansi.Attribute.NORMAL, new Ansi.AnsiColor(light ? 242 : 247), null);
        this._delimiter = new Ansi(Ansi.Attribute.NORMAL, new Ansi.AnsiColor(light ? 242 : 252), null);
        this._attribute = new Ansi(Ansi.Attribute.NORMAL, new Ansi.AnsiColor(light ? 166 : 214), null);
        this._error = new Ansi(Ansi.Attribute.NORMAL, new Ansi.AnsiColor(light ? 196 : 196), null);
    }
    
    private String colorize(final String value, final Ansi ansi) {
        return ansi.colorize(StringUtilities.escape(value, false, this.isUnicodeOutputEnabled()));
    }
    
    @Override
    public void writeError(final String value) {
        this.writeAnsi(value, this.colorize(value, this._error));
    }
    
    @Override
    public void writeLabel(final String value) {
        this.writeAnsi(value, this.colorize(value, this._label));
    }
    
    protected final void writeAnsi(final String originalText, final String ansiText) {
        super.writeRaw(ansiText);
        if (originalText != null && ansiText != null) {
            super.column -= ansiText.length() - originalText.length();
        }
    }
    
    @Override
    public void writeLiteral(final Object value) {
        final String literal = String.valueOf(value);
        this.writeAnsi(literal, this.colorize(literal, this._literal));
    }
    
    @Override
    public void writeTextLiteral(final Object value) {
        final String literal = String.valueOf(value);
        this.writeAnsi(literal, this.colorize(literal, this._textLiteral));
    }
    
    @Override
    public void writeComment(final String value) {
        this.writeAnsi(value, this.colorize(value, this._comment));
    }
    
    @Override
    public void writeComment(final String format, final Object... args) {
        final String text = String.format(format, args);
        this.writeAnsi(text, this.colorize(text, this._comment));
    }
    
    @Override
    public void writeDelimiter(final String text) {
        this.writeAnsi(text, this.colorize(text, this._delimiter));
    }
    
    @Override
    public void writeAttribute(final String text) {
        this.writeAnsi(text, this.colorize(text, this._attribute));
    }
    
    @Override
    public void writeOperator(final String text) {
        this.writeAnsi(text, this.colorize(text, this._operator));
    }
    
    @Override
    public void writeKeyword(final String text) {
        this.writeAnsi(text, this.colorize(text, this._keyword));
    }
    
    @Override
    public void writeDefinition(final String text, final Object definition, final boolean isLocal) {
        if (text == null) {
            super.write(text);
            return;
        }
        String colorizedText;
        if (definition instanceof Instruction || definition instanceof OpCode || definition instanceof AstCode) {
            colorizedText = this.colorize(text, this._instruction);
        }
        else if (definition instanceof TypeReference) {
            colorizedText = this.colorizeType(text, (TypeReference)definition);
        }
        else if (definition instanceof MethodReference || definition instanceof IMethodSignature) {
            colorizedText = this.colorize(text, this._method);
        }
        else if (definition instanceof FieldReference) {
            colorizedText = this.colorize(text, this._field);
        }
        else if (definition instanceof VariableReference || definition instanceof ParameterReference || definition instanceof Variable) {
            colorizedText = this.colorize(text, this._local);
        }
        else if (definition instanceof PackageReference) {
            colorizedText = this.colorizePackage(text);
        }
        else if (definition instanceof Label || definition instanceof com.strobel.decompiler.ast.Label) {
            colorizedText = this.colorize(text, this._label);
        }
        else {
            colorizedText = text;
        }
        this.writeAnsi(text, colorizedText);
    }
    
    @Override
    public void writeReference(final String text, final Object reference, final boolean isLocal) {
        if (text == null) {
            super.write(text);
            return;
        }
        String colorizedText;
        if (reference instanceof Instruction || reference instanceof OpCode || reference instanceof AstCode) {
            colorizedText = this.colorize(text, this._instruction);
        }
        else if (reference instanceof TypeReference) {
            colorizedText = this.colorizeType(text, (TypeReference)reference);
        }
        else if (reference instanceof MethodReference || reference instanceof IMethodSignature) {
            colorizedText = this.colorize(text, this._method);
        }
        else if (reference instanceof FieldReference) {
            colorizedText = this.colorize(text, this._field);
        }
        else if (reference instanceof VariableReference || reference instanceof ParameterReference || reference instanceof Variable) {
            colorizedText = this.colorize(text, this._local);
        }
        else if (reference instanceof PackageReference) {
            colorizedText = this.colorizePackage(text);
        }
        else if (reference instanceof Label || reference instanceof com.strobel.decompiler.ast.Label) {
            colorizedText = this.colorize(text, this._label);
        }
        else {
            colorizedText = StringUtilities.escape(text, false, this.isUnicodeOutputEnabled());
        }
        this.writeAnsi(text, colorizedText);
    }
    
    private String colorizeType(final String text, final TypeReference type) {
        if (type.isPrimitive()) {
            return this.colorize(text, this._keyword);
        }
        final String packageName = type.getPackageName();
        final TypeDefinition resolvedType = type.resolve();
        Ansi typeColor = type.isGenericParameter() ? this._typeVariable : this._type;
        if (!StringUtilities.isNullOrEmpty(packageName)) {
            String s = text;
            char delimiter = '.';
            String packagePrefix = String.valueOf(packageName) + delimiter;
            int arrayDepth;
            for (arrayDepth = 0; arrayDepth < s.length() && s.charAt(arrayDepth) == '['; ++arrayDepth) {}
            if (arrayDepth > 0) {
                s = s.substring(arrayDepth);
            }
            final boolean isTypeVariable = s.startsWith("T") && s.endsWith(";");
            final boolean isSignature = isTypeVariable || (s.startsWith("L") && s.endsWith(";"));
            if (isSignature) {
                s = s.substring(1, s.length() - 1);
            }
            if (!StringUtilities.startsWith(s, packagePrefix)) {
                delimiter = '/';
                packagePrefix = String.valueOf(packageName.replace('.', delimiter)) + delimiter;
            }
            final StringBuilder sb = new StringBuilder();
            String typeName;
            if (StringUtilities.startsWith(s, packagePrefix)) {
                final String[] packageParts = packageName.split("\\.");
                for (int i = 0; i < arrayDepth; ++i) {
                    sb.append(this.colorize("[", this._delimiter));
                }
                if (isSignature) {
                    sb.append(this.colorize(isTypeVariable ? "T" : "L", this._delimiter));
                }
                for (int i = 0; i < packageParts.length; ++i) {
                    if (i != 0) {
                        sb.append(this.colorize(String.valueOf(delimiter), this._delimiter));
                    }
                    sb.append(this.colorize(packageParts[i], this._package));
                }
                sb.append(this.colorize(String.valueOf(delimiter), this._delimiter));
                typeName = s.substring(packagePrefix.length());
            }
            else {
                typeName = text;
            }
            typeColor = ((resolvedType != null && resolvedType.isAnnotation()) ? this._attribute : typeColor);
            this.colorizeDelimitedName(sb, typeName, typeColor);
            if (isSignature) {
                sb.append(this.colorize(";", this._delimiter));
            }
            return sb.toString();
        }
        if (resolvedType != null && resolvedType.isAnnotation()) {
            return this.colorize(text, this._attribute);
        }
        return this.colorize(text, typeColor);
    }
    
    private StringBuilder colorizeDelimitedName(final StringBuilder sb, final String typeName, final Ansi typeColor) {
        final int end = typeName.length();
        if (end == 0) {
            return sb;
        }
        int i;
        int start;
        for (start = (i = 0); i < end; ++i) {
            final char ch = typeName.charAt(i);
            switch (ch) {
                case '$':
                case '.': {
                    sb.append(this.colorize(typeName.substring(start, i), typeColor));
                    sb.append(this.colorize((ch == '.') ? "." : "$", this._delimiter));
                    start = i + 1;
                    break;
                }
            }
        }
        if (start < end) {
            sb.append(this.colorize(typeName.substring(start, end), typeColor));
        }
        return sb;
    }
    
    private String colorizePackage(final String text) {
        final String[] packageParts = text.split("\\.");
        final StringBuilder sb = new StringBuilder(text.length() * 2);
        for (int i = 0; i < packageParts.length; ++i) {
            if (i != 0) {
                sb.append(this.colorize(".", this._delimiter));
            }
            final String packagePart = packageParts[i];
            if ("*".equals(packagePart)) {
                sb.append(packagePart);
            }
            else {
                sb.append(this.colorize(packagePart, this._package));
            }
        }
        return sb.toString();
    }
    
    public enum ColorScheme
    {
        DARK("DARK", 0), 
        LIGHT("LIGHT", 1);
    }
    
    private static final class Delimiters
    {
        static final String L = "L";
        static final String T = "T";
        static final String DOLLAR = "$";
        static final String DOT = ".";
        static final String SLASH = "/";
        static final String LEFT_BRACKET = "[";
        static final String SEMICOLON = ";";
    }
}
