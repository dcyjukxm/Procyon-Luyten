package com.strobel.assembler.ir;

import com.strobel.assembler.metadata.*;

final class Error
{
    public static RuntimeException notGenericParameter(final TypeReference type) {
        return new UnsupportedOperationException(String.format("TypeReference '%s' is not a generic parameter.", type.getFullName()));
    }
    
    public static RuntimeException notWildcard(final TypeReference type) {
        throw new UnsupportedOperationException(String.format("TypeReference '%s' is not a wildcard or captured type.", type.getFullName()));
    }
    
    public static RuntimeException notBoundedType(final TypeReference type) {
        throw new UnsupportedOperationException(String.format("TypeReference '%s' is not a bounded type.", type.getFullName()));
    }
    
    public static RuntimeException notGenericType(final TypeReference type) {
        return new UnsupportedOperationException(String.format("TypeReference '%s' is not a generic type.", type.getFullName()));
    }
    
    public static RuntimeException notGenericMethod(final MethodReference method) {
        return new UnsupportedOperationException(String.format("TypeReference '%s' is not a generic method.", method.getName()));
    }
    
    public static RuntimeException notGenericMethodDefinition(final MethodReference method) {
        return new UnsupportedOperationException(String.format("TypeReference '%s' is not a generic method definition.", method.getName()));
    }
    
    public static RuntimeException noElementType(final TypeReference type) {
        return new UnsupportedOperationException(String.format("TypeReference '%s' does not have an element type.", type.getFullName()));
    }
    
    public static RuntimeException notEnumType(final TypeReference type) {
        return new UnsupportedOperationException(String.format("TypeReference '%s' is not an enum type.", type.getFullName()));
    }
    
    public static RuntimeException notArrayType(final TypeReference type) {
        return new UnsupportedOperationException(String.format("TypeReference '%s' is not an array type.", type.getFullName()));
    }
    
    public static RuntimeException invalidSignatureTypeExpected(final String signature, final int position) {
        return new IllegalArgumentException(String.format("Invalid signature: type expected at position %d (%s).", position, signature));
    }
    
    public static RuntimeException invalidSignatureTopLevelGenericParameterUnexpected(final String signature, final int position) {
        return new IllegalArgumentException(String.format("Invalid signature: unexpected generic parameter at position %d.  (%s)", position, signature));
    }
    
    public static RuntimeException invalidSignatureNonGenericTypeTypeArguments(final TypeReference type) {
        return new IllegalArgumentException(String.format("Invalid signature: unexpected type arguments specified for non-generic type '%s'.", type.getBriefDescription()));
    }
    
    public static RuntimeException invalidSignatureUnexpectedToken(final String signature, final int position) {
        return new IllegalArgumentException(String.format("Invalid signature: unexpected token at position %d.  (%s)", position, signature));
    }
    
    public static RuntimeException invalidSignatureUnexpectedEnd(final String signature, final int position) {
        return new IllegalArgumentException(String.format("Invalid signature: unexpected end of signature at position %d.  (%s)", position, signature));
    }
    
    public static RuntimeException invalidSignatureExpectedEndOfTypeArguments(final String signature, final int position) {
        return new IllegalArgumentException(String.format("Invalid signature: expected end of type argument list at position %d.  (%s)", position, signature));
    }
    
    public static RuntimeException invalidSignatureExpectedEndOfTypeVariables(final String signature, final int position) {
        return new IllegalArgumentException(String.format("Invalid signature: expected end of type variable list at position %d.  (%s)", position, signature));
    }
    
    public static RuntimeException invalidSignatureExpectedTypeArgument(final String signature, final int position) {
        return new IllegalArgumentException(String.format("Invalid signature: expected type argument at position %d.  (%s)", position, signature));
    }
    
    public static RuntimeException invalidSignatureExpectedParameterList(final String signature, final int position) {
        return new IllegalArgumentException(String.format("Invalid signature: expected parameter type list at position %d.  (%s)", position, signature));
    }
    
    public static RuntimeException invalidSignatureExpectedReturnType(final String signature, final int position) {
        return new IllegalArgumentException(String.format("Invalid signature: expected return type at position %d.  (%s)", position, signature));
    }
    
    public static RuntimeException invalidSignatureExpectedTypeVariable(final String signature, final int position) {
        return new IllegalArgumentException(String.format("Invalid signature: expected type variable name at position %d.  (%s)", position, signature));
    }
    
    public static RuntimeException stackMapperCalledWithUnexpandedFrame(final FrameType frameType) {
        throw new IllegalStateException(String.format("StackMappingVisitor.visitFrame() was called with an unexpanded frame (%s).", frameType.name()));
    }
    
    public static RuntimeException invalidBootstrapMethodEntry(final MethodReference bootstrapMethod, final int parameterCount, final int argumentCount) {
        if (argumentCount > parameterCount + 3) {
            return new IllegalStateException(String.format("Invalid BootstrapMethods attribute entry: %d too many arguments specifiedfor method %s.", argumentCount - (parameterCount + 3), bootstrapMethod.getFullName()));
        }
        return new IllegalStateException(String.format("Invalid BootstrapMethods attribute entry: %d additional arguments required for method %s, but only %d specified.", parameterCount - 3, bootstrapMethod.getFullName(), argumentCount));
    }
}
