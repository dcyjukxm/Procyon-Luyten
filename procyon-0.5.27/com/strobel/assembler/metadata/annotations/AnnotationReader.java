package com.strobel.assembler.metadata.annotations;

import com.strobel.core.*;
import com.strobel.assembler.metadata.*;
import com.strobel.util.*;

public final class AnnotationReader
{
    private static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$assembler$metadata$annotations$AnnotationElementType;
    
    public static CustomAnnotation read(final IMetadataScope scope, final Buffer input) {
        final int typeToken = input.readUnsignedShort();
        final int parameterCount = input.readUnsignedShort();
        final TypeReference annotationType = scope.lookupType(typeToken);
        final AnnotationParameter[] parameters = new AnnotationParameter[parameterCount];
        readParameters(parameters, scope, input, true);
        return new CustomAnnotation(annotationType, ArrayUtilities.asUnmodifiableList(parameters));
    }
    
    private static void readParameters(final AnnotationParameter[] parameters, final IMetadataScope scope, final Buffer input, final boolean namedParameter) {
        for (int i = 0; i < parameters.length; ++i) {
            parameters[i] = new AnnotationParameter(namedParameter ? scope.lookupConstant(input.readUnsignedShort()) : "value", readElement(scope, input));
        }
    }
    
    private static AnnotationElement readElement(final IMetadataScope scope, final Buffer input) {
        final char tag = (char)input.readUnsignedByte();
        final AnnotationElementType elementType = AnnotationElementType.forTag(tag);
        switch ($SWITCH_TABLE$com$strobel$assembler$metadata$annotations$AnnotationElementType()[elementType.ordinal()]) {
            case 1: {
                Object constantValue = scope.lookupConstant(input.readUnsignedShort());
                switch (tag) {
                    case 'B': {
                        constantValue = ((Number)constantValue).byteValue();
                        break;
                    }
                    case 'C': {
                        constantValue = (char)((Number)constantValue).intValue();
                        break;
                    }
                    case 'S': {
                        constantValue = ((Number)constantValue).shortValue();
                        break;
                    }
                    case 'Z': {
                        constantValue = ((((Number)constantValue).intValue() == 0) ? Boolean.FALSE : Boolean.TRUE);
                        break;
                    }
                }
                return new ConstantAnnotationElement(constantValue);
            }
            case 2: {
                final TypeReference enumType = scope.lookupType(input.readUnsignedShort());
                final String constantName = scope.lookupConstant(input.readUnsignedShort());
                return new EnumAnnotationElement(enumType, constantName);
            }
            case 3: {
                final AnnotationElement[] elements = new AnnotationElement[input.readUnsignedShort()];
                for (int i = 0; i < elements.length; ++i) {
                    elements[i] = readElement(scope, input);
                }
                return new ArrayAnnotationElement(elements);
            }
            case 4: {
                final TypeReference type = scope.lookupType(input.readUnsignedShort());
                return new ClassAnnotationElement(type);
            }
            case 5: {
                final CustomAnnotation annotation = read(scope, input);
                return new AnnotationAnnotationElement(annotation);
            }
            default: {
                throw ContractUtils.unreachable();
            }
        }
    }
    
    static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$assembler$metadata$annotations$AnnotationElementType() {
        final int[] loc_0 = AnnotationReader.$SWITCH_TABLE$com$strobel$assembler$metadata$annotations$AnnotationElementType;
        if (loc_0 != null) {
            return loc_0;
        }
        final int[] loc_1 = new int[AnnotationElementType.values().length];
        try {
            loc_1[AnnotationElementType.Annotation.ordinal()] = 5;
        }
        catch (NoSuchFieldError loc_2) {}
        try {
            loc_1[AnnotationElementType.Array.ordinal()] = 3;
        }
        catch (NoSuchFieldError loc_3) {}
        try {
            loc_1[AnnotationElementType.Class.ordinal()] = 4;
        }
        catch (NoSuchFieldError loc_4) {}
        try {
            loc_1[AnnotationElementType.Constant.ordinal()] = 1;
        }
        catch (NoSuchFieldError loc_5) {}
        try {
            loc_1[AnnotationElementType.Enum.ordinal()] = 2;
        }
        catch (NoSuchFieldError loc_6) {}
        return AnnotationReader.$SWITCH_TABLE$com$strobel$assembler$metadata$annotations$AnnotationElementType = loc_1;
    }
}
