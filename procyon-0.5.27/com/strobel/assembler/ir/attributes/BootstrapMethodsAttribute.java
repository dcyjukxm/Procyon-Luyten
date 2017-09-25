package com.strobel.assembler.ir.attributes;

import com.strobel.core.*;
import java.util.*;

public final class BootstrapMethodsAttribute extends SourceAttribute
{
    private final List<BootstrapMethodsTableEntry> _bootstrapMethods;
    
    public BootstrapMethodsAttribute(final List<BootstrapMethodsTableEntry> bootstrapMethods) {
        this((BootstrapMethodsTableEntry[])VerifyArgument.notNull(bootstrapMethods, "bootstrapMethods").toArray(new BootstrapMethodsTableEntry[bootstrapMethods.size()]));
    }
    
    public BootstrapMethodsAttribute(final BootstrapMethodsTableEntry... bootstrapMethods) {
        super("BootstrapMethods", computeSize(bootstrapMethods));
        this._bootstrapMethods = (ArrayUtilities.isNullOrEmpty(bootstrapMethods) ? Collections.emptyList() : ArrayUtilities.asUnmodifiableList(bootstrapMethods));
    }
    
    public final List<BootstrapMethodsTableEntry> getBootstrapMethods() {
        return this._bootstrapMethods;
    }
    
    private static int computeSize(final BootstrapMethodsTableEntry[] bootstrapMethods) {
        int size = 2;
        if (bootstrapMethods == null) {
            return size;
        }
        for (final BootstrapMethodsTableEntry bootstrapMethod : bootstrapMethods) {
            size += 2 + 2 * bootstrapMethod.getArguments().size();
        }
        return size;
    }
}
