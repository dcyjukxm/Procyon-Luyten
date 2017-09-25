package com.strobel.assembler.metadata;

import com.strobel.annotations.*;
import com.strobel.core.*;
import com.strobel.assembler.ir.*;
import java.util.*;

public class DeobfuscationUtilities
{
    public static void processType(@NotNull final TypeDefinition type) {
        VerifyArgument.notNull(type, "type");
        if (Flags.testAny(type.getFlags(), 140737488355328L)) {
            return;
        }
        type.setFlags(type.getFlags() | 0x800000000000L);
        flagAnonymousEnumDefinitions(type);
    }
    
    private static void flagAnonymousEnumDefinitions(final TypeDefinition type) {
        if (!type.isEnum() || type.getDeclaringType() != null) {
            return;
        }
        final TypeReference baseType = type.getBaseType();
        if (!"java/lang/Enum".equals(baseType.getInternalName())) {
            final TypeDefinition resolvedBaseType = baseType.resolve();
            if (resolvedBaseType != null) {
                processType(resolvedBaseType);
            }
        }
        if (type.getDeclaringType() != null && type.isAnonymous()) {
            return;
        }
        for (final MethodDefinition method : type.getDeclaredMethods()) {
            if (!method.isTypeInitializer()) {
                continue;
            }
            final MethodBody body = method.getBody();
            if (body == null) {
                continue;
            }
            for (final Instruction p : body.getInstructions()) {
                if (p.getOpCode() != OpCode.NEW) {
                    continue;
                }
                final TypeReference instantiatedType = p.getOperand(0);
                final TypeDefinition instantiatedTypeResolved = (instantiatedType != null) ? instantiatedType.resolve() : null;
                if (instantiatedTypeResolved == null) {
                    continue;
                }
                if (!instantiatedTypeResolved.isEnum() || !type.isEquivalentTo(instantiatedTypeResolved.getBaseType())) {
                    continue;
                }
                instantiatedTypeResolved.setDeclaringType(type);
                type.getDeclaredTypesInternal().add(instantiatedTypeResolved);
                instantiatedTypeResolved.setFlags(instantiatedTypeResolved.getFlags() | 0x100000000000L);
            }
        }
    }
}
