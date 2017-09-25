package com.strobel.assembler.metadata;

import com.strobel.assembler.ir.*;
import com.strobel.core.*;
import com.strobel.decompiler.*;

public class ConstantPoolPrinter implements ConstantPool.Visitor
{
    private static final int MAX_TAG_LENGTH;
    private final ITextOutput _output;
    private final DecompilerSettings _settings;
    private boolean _isHeaderPrinted;
    
    static {
        int maxTagLength = 0;
        ConstantPool.Tag[] loc_1;
        for (int loc_0 = (loc_1 = ConstantPool.Tag.values()).length, loc_2 = 0; loc_2 < loc_0; ++loc_2) {
            final ConstantPool.Tag tag = loc_1[loc_2];
            final int length = tag.name().length();
            if (length > maxTagLength) {
                maxTagLength = length;
            }
        }
        MAX_TAG_LENGTH = maxTagLength;
    }
    
    public ConstantPoolPrinter(final ITextOutput output) {
        this(output, DecompilerSettings.javaDefaults());
    }
    
    public ConstantPoolPrinter(final ITextOutput output, final DecompilerSettings settings) {
        super();
        this._output = VerifyArgument.notNull(output, "output");
        this._settings = VerifyArgument.notNull(settings, "settings");
    }
    
    protected void printTag(final ConstantPool.Tag tag) {
        this._output.writeAttribute(String.format("%1$-" + ConstantPoolPrinter.MAX_TAG_LENGTH + "s  ", tag));
    }
    
    @Override
    public void visit(final ConstantPool.Entry entry) {
        VerifyArgument.notNull(entry, "entry");
        if (!this._isHeaderPrinted) {
            this._output.writeAttribute("Constant Pool");
            this._output.write(':');
            this._output.writeLine();
            this._isHeaderPrinted = true;
        }
        this._output.indent();
        this._output.writeLiteral(String.format("%1$5d", entry.index));
        this._output.write(": ");
        this.printTag(entry.getTag());
        entry.accept(this);
        this._output.writeLine();
        this._output.unindent();
    }
    
    @Override
    public void visitTypeInfo(final ConstantPool.TypeInfoEntry info) {
        this._output.writeDelimiter("#");
        this._output.writeLiteral(String.format("%1$-14d", info.nameIndex));
        this._output.writeComment(String.format("//  %1$s", StringUtilities.escape(info.getName())), false, this._settings.isUnicodeOutputEnabled());
    }
    
    @Override
    public void visitDoubleConstant(final ConstantPool.DoubleConstantEntry info) {
        DecompilerHelpers.writePrimitiveValue(this._output, info.getConstantValue());
    }
    
    @Override
    public void visitFieldReference(final ConstantPool.FieldReferenceEntry info) {
        final ConstantPool.NameAndTypeDescriptorEntry nameAndTypeInfo = info.getNameAndTypeInfo();
        final int startColumn = this._output.getColumn();
        this._output.writeDelimiter("#");
        this._output.writeLiteral(info.typeInfoIndex);
        this._output.writeDelimiter(".");
        this._output.writeDelimiter("#");
        this._output.writeLiteral(info.nameAndTypeDescriptorIndex);
        final int endColumn = this._output.getColumn();
        final int padding = 14 - (endColumn - startColumn);
        final String paddingText = (padding > 0) ? StringUtilities.repeat(' ', padding) : "";
        this._output.writeComment(String.format(String.valueOf(paddingText) + " //  %1$s.%2$s:%3$s", StringUtilities.escape(info.getClassName(), false, this._settings.isUnicodeOutputEnabled()), StringUtilities.escape(nameAndTypeInfo.getName(), false, this._settings.isUnicodeOutputEnabled()), StringUtilities.escape(nameAndTypeInfo.getType(), false, this._settings.isUnicodeOutputEnabled())));
    }
    
    @Override
    public void visitFloatConstant(final ConstantPool.FloatConstantEntry info) {
        DecompilerHelpers.writePrimitiveValue(this._output, info.getConstantValue());
    }
    
    @Override
    public void visitIntegerConstant(final ConstantPool.IntegerConstantEntry info) {
        DecompilerHelpers.writePrimitiveValue(this._output, info.getConstantValue());
    }
    
    @Override
    public void visitInterfaceMethodReference(final ConstantPool.InterfaceMethodReferenceEntry info) {
        final ConstantPool.NameAndTypeDescriptorEntry nameAndTypeInfo = info.getNameAndTypeInfo();
        final int startColumn = this._output.getColumn();
        this._output.writeDelimiter("#");
        this._output.writeLiteral(info.typeInfoIndex);
        this._output.writeDelimiter(".");
        this._output.writeDelimiter("#");
        this._output.writeLiteral(info.nameAndTypeDescriptorIndex);
        final int endColumn = this._output.getColumn();
        final int padding = 14 - (endColumn - startColumn);
        final String paddingText = (padding > 0) ? StringUtilities.repeat(' ', padding) : "";
        this._output.writeComment(String.format(String.valueOf(paddingText) + " //  %1$s.%2$s:%3$s", StringUtilities.escape(info.getClassName(), false, this._settings.isUnicodeOutputEnabled()), StringUtilities.escape(nameAndTypeInfo.getName(), false, this._settings.isUnicodeOutputEnabled()), StringUtilities.escape(nameAndTypeInfo.getType(), false, this._settings.isUnicodeOutputEnabled())));
    }
    
    @Override
    public void visitInvokeDynamicInfo(final ConstantPool.InvokeDynamicInfoEntry info) {
        final ConstantPool.NameAndTypeDescriptorEntry nameAndTypeInfo = info.getNameAndTypeDescriptor();
        final int startColumn = this._output.getColumn();
        this._output.writeLiteral(info.bootstrapMethodAttributeIndex);
        this._output.writeDelimiter(", ");
        this._output.writeDelimiter("#");
        this._output.writeLiteral(nameAndTypeInfo.nameIndex);
        this._output.writeDelimiter(".");
        this._output.writeDelimiter("#");
        this._output.writeLiteral(nameAndTypeInfo.typeDescriptorIndex);
        final int endColumn = this._output.getColumn();
        final int padding = 14 - (endColumn - startColumn);
        final String paddingText = (padding > 0) ? StringUtilities.repeat(' ', padding) : "";
        this._output.writeComment(String.format(String.valueOf(paddingText) + " //  %1$s:%2$s", StringUtilities.escape(nameAndTypeInfo.getName(), false, this._settings.isUnicodeOutputEnabled()), StringUtilities.escape(nameAndTypeInfo.getType(), false, this._settings.isUnicodeOutputEnabled())));
    }
    
    @Override
    public void visitLongConstant(final ConstantPool.LongConstantEntry info) {
        DecompilerHelpers.writePrimitiveValue(this._output, info.getConstantValue());
    }
    
    @Override
    public void visitNameAndTypeDescriptor(final ConstantPool.NameAndTypeDescriptorEntry info) {
        final int startColumn = this._output.getColumn();
        this._output.writeDelimiter("#");
        this._output.writeLiteral(info.nameIndex);
        this._output.writeDelimiter(".");
        this._output.writeDelimiter("#");
        this._output.writeLiteral(info.typeDescriptorIndex);
        final int endColumn = this._output.getColumn();
        final int padding = 14 - (endColumn - startColumn);
        final String paddingText = (padding > 0) ? StringUtilities.repeat(' ', padding) : "";
        this._output.writeComment(String.format(String.valueOf(paddingText) + " //  %1$s:%2$s", StringUtilities.escape(info.getName(), false, this._settings.isUnicodeOutputEnabled()), StringUtilities.escape(info.getType(), false, this._settings.isUnicodeOutputEnabled())));
    }
    
    @Override
    public void visitMethodReference(final ConstantPool.MethodReferenceEntry info) {
        final ConstantPool.NameAndTypeDescriptorEntry nameAndTypeInfo = info.getNameAndTypeInfo();
        final int startColumn = this._output.getColumn();
        this._output.writeDelimiter("#");
        this._output.writeLiteral(info.typeInfoIndex);
        this._output.writeDelimiter(".");
        this._output.writeDelimiter("#");
        this._output.writeLiteral(info.nameAndTypeDescriptorIndex);
        final int endColumn = this._output.getColumn();
        final int padding = 14 - (endColumn - startColumn);
        final String paddingText = (padding > 0) ? StringUtilities.repeat(' ', padding) : "";
        this._output.writeComment(String.format(String.valueOf(paddingText) + " //  %1$s.%2$s:%3$s", StringUtilities.escape(info.getClassName(), false, this._settings.isUnicodeOutputEnabled()), StringUtilities.escape(nameAndTypeInfo.getName(), false, this._settings.isUnicodeOutputEnabled()), StringUtilities.escape(nameAndTypeInfo.getType(), false, this._settings.isUnicodeOutputEnabled())));
    }
    
    @Override
    public void visitMethodHandle(final ConstantPool.MethodHandleEntry info) {
        final ConstantPool.ReferenceEntry reference = info.getReference();
        final ConstantPool.NameAndTypeDescriptorEntry nameAndTypeInfo = reference.getNameAndTypeInfo();
        final int startColumn = this._output.getColumn();
        this._output.writeLiteral(info.referenceKind);
        this._output.write(' ');
        this._output.writeDelimiter("#");
        this._output.writeLiteral(reference.typeInfoIndex);
        this._output.writeDelimiter(".");
        this._output.writeDelimiter("#");
        this._output.writeLiteral(reference.nameAndTypeDescriptorIndex);
        final int endColumn = this._output.getColumn();
        final int padding = 28 - (endColumn - startColumn);
        final String paddingText = (padding > 0) ? StringUtilities.repeat(' ', padding) : "";
        this._output.writeComment(String.format(String.valueOf(paddingText) + " //  %1$s.%2$s:%3$s", StringUtilities.escape(reference.getClassName(), false, this._settings.isUnicodeOutputEnabled()), StringUtilities.escape(nameAndTypeInfo.getName(), false, this._settings.isUnicodeOutputEnabled()), StringUtilities.escape(nameAndTypeInfo.getType(), false, this._settings.isUnicodeOutputEnabled())));
    }
    
    @Override
    public void visitMethodType(final ConstantPool.MethodTypeEntry info) {
        this._output.write("%1$-13s", info.getType());
    }
    
    @Override
    public void visitStringConstant(final ConstantPool.StringConstantEntry info) {
        this._output.writeDelimiter("#");
        this._output.writeLiteral(String.format("%1$-14d", info.stringIndex));
        this._output.writeComment(String.format("//  %1$s", StringUtilities.escape(info.getValue(), true, this._settings.isUnicodeOutputEnabled())));
    }
    
    @Override
    public void visitUtf8StringConstant(final ConstantPool.Utf8StringConstantEntry info) {
        DecompilerHelpers.writePrimitiveValue(this._output, info.getConstantValue());
    }
    
    @Override
    public void visitEnd() {
    }
}
