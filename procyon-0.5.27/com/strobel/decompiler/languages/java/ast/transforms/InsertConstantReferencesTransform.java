package com.strobel.decompiler.languages.java.ast.transforms;

import com.strobel.decompiler.*;
import com.strobel.core.*;
import com.strobel.decompiler.languages.java.ast.*;
import com.strobel.assembler.metadata.*;

public class InsertConstantReferencesTransform extends ContextTrackingVisitor<Void>
{
    public InsertConstantReferencesTransform(final DecompilerContext context) {
        super(context);
    }
    
    @Override
    public Void visitPrimitiveExpression(final PrimitiveExpression node, final Void data) {
        final Object value = node.getValue();
        if (value instanceof Number) {
            this.tryRewriteConstant(node, value);
        }
        return null;
    }
    
    private void tryRewriteConstant(final PrimitiveExpression node, final Object value) {
        JvmType jvmType;
        String fieldName;
        if (value instanceof Double) {
            final double d = (double)value;
            jvmType = JvmType.Double;
            if (d == Double.POSITIVE_INFINITY) {
                fieldName = "POSITIVE_INFINITY";
            }
            else if (d == Double.NEGATIVE_INFINITY) {
                fieldName = "NEGATIVE_INFINITY";
            }
            else if (Double.isNaN(d)) {
                fieldName = "NaN";
            }
            else if (d == Double.MIN_VALUE) {
                fieldName = "MIN_VALUE";
            }
            else if (d == Double.MAX_VALUE) {
                fieldName = "MAX_VALUE";
            }
            else {
                if (d != Double.MIN_NORMAL) {
                    return;
                }
                fieldName = "MIN_NORMAL";
            }
        }
        else if (value instanceof Float) {
            final float f = (float)value;
            jvmType = JvmType.Float;
            if (f == Float.POSITIVE_INFINITY) {
                fieldName = "POSITIVE_INFINITY";
            }
            else if (f == Float.NEGATIVE_INFINITY) {
                fieldName = "NEGATIVE_INFINITY";
            }
            else if (Float.isNaN(f)) {
                fieldName = "NaN";
            }
            else if (f == Float.MIN_VALUE) {
                fieldName = "MIN_VALUE";
            }
            else if (f == Float.MAX_VALUE) {
                fieldName = "MAX_VALUE";
            }
            else {
                if (f != Float.MIN_NORMAL) {
                    return;
                }
                fieldName = "MIN_NORMAL";
            }
        }
        else if (value instanceof Long) {
            final long l = (long)value;
            jvmType = JvmType.Long;
            if (l == Long.MIN_VALUE) {
                fieldName = "MIN_VALUE";
            }
            else {
                if (l != Long.MAX_VALUE) {
                    return;
                }
                fieldName = "MAX_VALUE";
            }
        }
        else if (value instanceof Integer) {
            final int i = (int)value;
            jvmType = JvmType.Integer;
            if (i == Integer.MIN_VALUE) {
                fieldName = "MIN_VALUE";
            }
            else {
                if (i != Integer.MAX_VALUE) {
                    return;
                }
                fieldName = "MAX_VALUE";
            }
        }
        else if (value instanceof Short) {
            final short s = (short)value;
            jvmType = JvmType.Short;
            if (s == -32768) {
                fieldName = "MIN_VALUE";
            }
            else {
                if (s != 32767) {
                    return;
                }
                fieldName = "MAX_VALUE";
            }
        }
        else {
            if (!(value instanceof Byte)) {
                return;
            }
            final byte b = (byte)value;
            jvmType = JvmType.Byte;
            if (b == -128) {
                fieldName = "MIN_VALUE";
            }
            else {
                if (b != 127) {
                    return;
                }
                fieldName = "MAX_VALUE";
            }
        }
        final TypeDefinition currentType = this.context.getCurrentType();
        MetadataParser parser;
        if (currentType != null) {
            parser = new MetadataParser(currentType);
        }
        else {
            parser = new MetadataParser(IMetadataResolver.EMPTY);
        }
        final TypeReference declaringType = parser.parseTypeDescriptor("java/lang/" + jvmType.name());
        final FieldReference field = parser.parseField(declaringType, fieldName, jvmType.getDescriptorPrefix());
        if (currentType != null && node.getParent() instanceof VariableInitializer && node.getParent().getParent() instanceof FieldDeclaration && StringUtilities.equals(currentType.getInternalName(), declaringType.getInternalName())) {
            final FieldDeclaration declaration = (FieldDeclaration)node.getParent().getParent();
            final FieldDefinition actualField = declaration.getUserData(Keys.FIELD_DEFINITION);
            if (actualField == null || StringUtilities.equals(actualField.getName(), fieldName)) {
                final String loc_0;
                switch (loc_0 = fieldName) {
                    case "POSITIVE_INFINITY": {
                        node.replaceWith(new BinaryOperatorExpression(new PrimitiveExpression(node.getOffset(), (jvmType == JvmType.Double) ? 1.0 : 1.0), BinaryOperatorType.DIVIDE, new PrimitiveExpression(node.getOffset(), (jvmType == JvmType.Double) ? 0.0 : 0.0)));
                    }
                    case "NaN": {
                        node.replaceWith(new BinaryOperatorExpression(new PrimitiveExpression(node.getOffset(), (jvmType == JvmType.Double) ? 0.0 : 0.0), BinaryOperatorType.DIVIDE, new PrimitiveExpression(node.getOffset(), (jvmType == JvmType.Double) ? 0.0 : 0.0)));
                    }
                    case "NEGATIVE_INFINITY": {
                        node.replaceWith(new BinaryOperatorExpression(new PrimitiveExpression(node.getOffset(), (jvmType == JvmType.Double) ? -1.0 : -1.0), BinaryOperatorType.DIVIDE, new PrimitiveExpression(node.getOffset(), (jvmType == JvmType.Double) ? 0.0 : 0.0)));
                    }
                    default:
                        break;
                }
                return;
            }
        }
        final AstBuilder astBuilder = this.context.getUserData(Keys.AST_BUILDER);
        AstType astType;
        if (astBuilder != null) {
            astType = astBuilder.convertType(declaringType);
        }
        else {
            astType = new SimpleType(declaringType.getName());
            astType.putUserData(Keys.TYPE_REFERENCE, declaringType);
        }
        final MemberReferenceExpression memberReference = new MemberReferenceExpression(node.getOffset(), new TypeReferenceExpression(node.getOffset(), astType), fieldName, new AstType[0]);
        memberReference.putUserData(Keys.MEMBER_REFERENCE, field);
        memberReference.putUserData(Keys.CONSTANT_VALUE, value);
        node.replaceWith(memberReference);
    }
}
