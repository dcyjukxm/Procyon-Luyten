package com.strobel.assembler.metadata;

import javax.lang.model.element.*;
import java.util.concurrent.*;
import com.strobel.util.*;
import java.util.*;

public class Flags
{
    public static final int PUBLIC = 1;
    public static final int PRIVATE = 2;
    public static final int PROTECTED = 4;
    public static final int STATIC = 8;
    public static final int FINAL = 16;
    public static final int SYNCHRONIZED = 32;
    public static final int VOLATILE = 64;
    public static final int TRANSIENT = 128;
    public static final int NATIVE = 256;
    public static final int INTERFACE = 512;
    public static final int ABSTRACT = 1024;
    public static final int STRICTFP = 2048;
    public static final int SYNTHETIC = 4096;
    public static final int ANNOTATION = 8192;
    public static final int ENUM = 16384;
    public static final int MANDATED = 32768;
    public static final int StandardFlags = 4095;
    public static final int ModifierFlags = 3583;
    public static final int ACC_SUPER = 32;
    public static final int ACC_BRIDGE = 64;
    public static final int ACC_VARARGS = 128;
    public static final int DEPRECATED = 131072;
    public static final int HASINIT = 262144;
    public static final int BLOCK = 1048576;
    public static final int IPROXY = 2097152;
    public static final int NOOUTERTHIS = 4194304;
    public static final int EXISTS = 8388608;
    public static final int COMPOUND = 16777216;
    public static final int CLASS_SEEN = 33554432;
    public static final int SOURCE_SEEN = 67108864;
    public static final int LOCKED = 134217728;
    public static final int UNATTRIBUTED = 268435456;
    public static final int ANONCONSTR = 536870912;
    public static final int ACYCLIC = 1073741824;
    public static final long BRIDGE = 2147483648L;
    public static final long PARAMETER = 8589934592L;
    public static final long VARARGS = 17179869184L;
    public static final long ACYCLIC_ANN = 34359738368L;
    public static final long GENERATEDCONSTR = 68719476736L;
    public static final long HYPOTHETICAL = 137438953472L;
    public static final long PROPRIETARY = 274877906944L;
    public static final long UNION = 549755813888L;
    public static final long OVERRIDE_BRIDGE = 1099511627776L;
    public static final long EFFECTIVELY_FINAL = 2199023255552L;
    public static final long CLASH = 4398046511104L;
    public static final long DEFAULT = 8796093022208L;
    public static final long ANONYMOUS = 17592186044416L;
    public static final long SUPER = 35184372088832L;
    public static final long LOAD_BODY_FAILED = 70368744177664L;
    public static final long DEOBFUSCATED = 140737488355328L;
    public static final int AccessFlags = 7;
    public static final int LocalClassFlags = 23568;
    public static final int MemberClassFlags = 24087;
    public static final int ClassFlags = 32273;
    public static final int InterfaceVarFlags = 25;
    public static final int VarFlags = 16607;
    public static final int ConstructorFlags = 7;
    public static final int InterfaceMethodFlags = 1025;
    public static final int MethodFlags = 3391;
    public static final long LocalVarFlags = 8589934608L;
    private static final Map<Long, Set<Modifier>> modifierSets;
    
    static {
        modifierSets = new ConcurrentHashMap<Long, Set<Modifier>>(64);
    }
    
    private Flags() {
        super();
        throw ContractUtils.unreachable();
    }
    
    public static String toString(final long flags) {
        final StringBuilder buf = new StringBuilder();
        String sep = "";
        for (final Flag s : asFlagSet(flags)) {
            buf.append(sep);
            buf.append(s.name());
            sep = ", ";
        }
        return buf.toString();
    }
    
    public static String toString(final long flags, final Kind kind) {
        final StringBuilder buf = new StringBuilder();
        String sep = "";
        for (final Flag s : asFlagSet(flags, kind)) {
            buf.append(sep);
            buf.append(s.name());
            sep = ", ";
        }
        return buf.toString();
    }
    
    public static EnumSet<Flag> asFlagSet(final long mask) {
        final EnumSet<Flag> flags = EnumSet.noneOf(Flag.class);
        if ((mask & 0x1L) != 0x0L) {
            flags.add(Flag.PUBLIC);
        }
        if ((mask & 0x2L) != 0x0L) {
            flags.add(Flag.PRIVATE);
        }
        if ((mask & 0x4L) != 0x0L) {
            flags.add(Flag.PROTECTED);
        }
        if ((mask & 0x8L) != 0x0L) {
            flags.add(Flag.STATIC);
        }
        if ((mask & 0x10L) != 0x0L) {
            flags.add(Flag.FINAL);
        }
        if ((mask & 0x20L) != 0x0L) {
            flags.add(Flag.SYNCHRONIZED);
        }
        if ((mask & 0x40L) != 0x0L) {
            flags.add(Flag.VOLATILE);
        }
        if ((mask & 0x80L) != 0x0L) {
            flags.add(Flag.TRANSIENT);
        }
        if ((mask & 0x100L) != 0x0L) {
            flags.add(Flag.NATIVE);
        }
        if ((mask & 0x200L) != 0x0L) {
            flags.add(Flag.INTERFACE);
        }
        if ((mask & 0x400L) != 0x0L) {
            flags.add(Flag.ABSTRACT);
        }
        if ((mask & 0x80000000000L) != 0x0L) {
            flags.add(Flag.DEFAULT);
        }
        if ((mask & 0x80000000000L) != 0x0L) {
            flags.add(Flag.DEFAULT);
        }
        if ((mask & 0x800L) != 0x0L) {
            flags.add(Flag.STRICTFP);
        }
        if ((mask & 0x200000000000L) != 0x0L) {
            flags.add(Flag.SUPER);
        }
        if ((mask & 0x80000000L) != 0x0L) {
            flags.add(Flag.BRIDGE);
        }
        if ((mask & 0x1000L) != 0x0L) {
            flags.add(Flag.SYNTHETIC);
        }
        if ((mask & 0x20000L) != 0x0L) {
            flags.add(Flag.DEPRECATED);
        }
        if ((mask & 0x40000L) != 0x0L) {
            flags.add(Flag.HASINIT);
        }
        if ((mask & 0x4000L) != 0x0L) {
            flags.add(Flag.ENUM);
        }
        if ((mask & 0x8000L) != 0x0L) {
            flags.add(Flag.MANDATED);
        }
        if ((mask & 0x200000L) != 0x0L) {
            flags.add(Flag.IPROXY);
        }
        if ((mask & 0x400000L) != 0x0L) {
            flags.add(Flag.NOOUTERTHIS);
        }
        if ((mask & 0x800000L) != 0x0L) {
            flags.add(Flag.EXISTS);
        }
        if ((mask & 0x1000000L) != 0x0L) {
            flags.add(Flag.COMPOUND);
        }
        if ((mask & 0x2000000L) != 0x0L) {
            flags.add(Flag.CLASS_SEEN);
        }
        if ((mask & 0x4000000L) != 0x0L) {
            flags.add(Flag.SOURCE_SEEN);
        }
        if ((mask & 0x8000000L) != 0x0L) {
            flags.add(Flag.LOCKED);
        }
        if ((mask & 0x10000000L) != 0x0L) {
            flags.add(Flag.UNATTRIBUTED);
        }
        if ((mask & 0x20000000L) != 0x0L) {
            flags.add(Flag.ANONCONSTR);
        }
        if ((mask & 0x40000000L) != 0x0L) {
            flags.add(Flag.ACYCLIC);
        }
        if ((mask & 0x200000000L) != 0x0L) {
            flags.add(Flag.PARAMETER);
        }
        if ((mask & 0x400000000L) != 0x0L) {
            flags.add(Flag.VARARGS);
        }
        return flags;
    }
    
    public static EnumSet<Flag> asFlagSet(final long mask, final Kind kind) {
        final EnumSet<Flag> flags = EnumSet.noneOf(Flag.class);
        if ((mask & 0x1L) != 0x0L) {
            flags.add(Flag.PUBLIC);
        }
        if ((mask & 0x2L) != 0x0L) {
            flags.add(Flag.PRIVATE);
        }
        if ((mask & 0x4L) != 0x0L) {
            flags.add(Flag.PROTECTED);
        }
        if ((mask & 0x8L) != 0x0L) {
            flags.add(Flag.STATIC);
        }
        if ((mask & 0x10L) != 0x0L) {
            flags.add(Flag.FINAL);
        }
        if ((mask & 0x20L) != 0x0L) {
            flags.add((kind == Kind.Class || kind == Kind.InnerClass) ? Flag.SUPER : Flag.SYNCHRONIZED);
        }
        if ((mask & 0x40L) != 0x0L) {
            flags.add((kind == Kind.Method) ? Flag.BRIDGE : Flag.VOLATILE);
        }
        if ((mask & 0x80L) != 0x0L) {
            flags.add((kind == Kind.Method) ? Flag.VARARGS : Flag.TRANSIENT);
        }
        if ((mask & 0x100L) != 0x0L) {
            flags.add(Flag.NATIVE);
        }
        if ((mask & 0x200L) != 0x0L) {
            flags.add(Flag.INTERFACE);
        }
        if ((mask & 0x400L) != 0x0L) {
            flags.add(Flag.ABSTRACT);
        }
        if ((mask & 0x80000000000L) != 0x0L) {
            flags.add(Flag.DEFAULT);
        }
        if ((mask & 0x800L) != 0x0L) {
            flags.add(Flag.STRICTFP);
        }
        if ((mask & 0x200000000000L) != 0x0L) {
            flags.add(Flag.SUPER);
        }
        if ((mask & 0x80000000L) != 0x0L) {
            flags.add(Flag.BRIDGE);
        }
        if ((mask & 0x1000L) != 0x0L) {
            flags.add(Flag.SYNTHETIC);
        }
        if ((mask & 0x20000L) != 0x0L) {
            flags.add(Flag.DEPRECATED);
        }
        if ((mask & 0x40000L) != 0x0L) {
            flags.add(Flag.HASINIT);
        }
        if ((mask & 0x4000L) != 0x0L) {
            flags.add(Flag.ENUM);
        }
        if ((mask & 0x200000L) != 0x0L) {
            flags.add(Flag.IPROXY);
        }
        if ((mask & 0x400000L) != 0x0L) {
            flags.add(Flag.NOOUTERTHIS);
        }
        if ((mask & 0x800000L) != 0x0L) {
            flags.add(Flag.EXISTS);
        }
        if ((mask & 0x1000000L) != 0x0L) {
            flags.add(Flag.COMPOUND);
        }
        if ((mask & 0x2000000L) != 0x0L) {
            flags.add(Flag.CLASS_SEEN);
        }
        if ((mask & 0x4000000L) != 0x0L) {
            flags.add(Flag.SOURCE_SEEN);
        }
        if ((mask & 0x8000000L) != 0x0L) {
            flags.add(Flag.LOCKED);
        }
        if ((mask & 0x10000000L) != 0x0L) {
            flags.add(Flag.UNATTRIBUTED);
        }
        if ((mask & 0x20000000L) != 0x0L) {
            flags.add(Flag.ANONCONSTR);
        }
        if ((mask & 0x40000000L) != 0x0L) {
            flags.add(Flag.ACYCLIC);
        }
        if ((mask & 0x200000000L) != 0x0L) {
            flags.add(Flag.PARAMETER);
        }
        if ((mask & 0x400000000L) != 0x0L) {
            flags.add(Flag.VARARGS);
        }
        return flags;
    }
    
    public static Set<Modifier> asModifierSet(final long flags) {
        Set<Modifier> modifiers = Flags.modifierSets.get(flags);
        if (modifiers == null) {
            modifiers = EnumSet.noneOf(Modifier.class);
            if (0x0L != (flags & 0x1L)) {
                modifiers.add(Modifier.PUBLIC);
            }
            if (0x0L != (flags & 0x4L)) {
                modifiers.add(Modifier.PROTECTED);
            }
            if (0x0L != (flags & 0x2L)) {
                modifiers.add(Modifier.PRIVATE);
            }
            if (0x0L != (flags & 0x400L)) {
                modifiers.add(Modifier.ABSTRACT);
            }
            if (0x0L != (flags & 0x8L)) {
                modifiers.add(Modifier.STATIC);
            }
            if (0x0L != (flags & 0x10L)) {
                modifiers.add(Modifier.FINAL);
            }
            if (0x0L != (flags & 0x80L)) {
                modifiers.add(Modifier.TRANSIENT);
            }
            if (0x0L != (flags & 0x40L)) {
                modifiers.add(Modifier.VOLATILE);
            }
            if (0x0L != (flags & 0x20L)) {
                modifiers.add(Modifier.SYNCHRONIZED);
            }
            if (0x0L != (flags & 0x100L)) {
                modifiers.add(Modifier.NATIVE);
            }
            if (0x0L != (flags & 0x800L)) {
                modifiers.add(Modifier.STRICTFP);
            }
            modifiers = Collections.unmodifiableSet(modifiers);
            Flags.modifierSets.put(flags, modifiers);
        }
        return modifiers;
    }
    
    public static int toModifiers(final long flags) {
        int modifiers = 0;
        if ((flags & 0x1L) != 0x0L) {
            modifiers |= 0x1;
        }
        if ((flags & 0x4L) != 0x0L) {
            modifiers |= 0x4;
        }
        if ((flags & 0x2L) != 0x0L) {
            modifiers |= 0x2;
        }
        if ((flags & 0x400L) != 0x0L) {
            modifiers |= 0x400;
        }
        if ((flags & 0x8L) != 0x0L) {
            modifiers |= 0x8;
        }
        if ((flags & 0x10L) != 0x0L) {
            modifiers |= 0x10;
        }
        if ((flags & 0x80L) != 0x0L) {
            modifiers |= 0x80;
        }
        if ((flags & 0x40L) != 0x0L) {
            modifiers |= 0x40;
        }
        if ((flags & 0x20L) != 0x0L) {
            modifiers |= 0x20;
        }
        if ((flags & 0x100L) != 0x0L) {
            modifiers |= 0x100;
        }
        if ((flags & 0x800L) != 0x0L) {
            modifiers |= 0x800;
        }
        return modifiers;
    }
    
    public static boolean testAny(final int value, final int flags) {
        return (value & flags) != 0x0;
    }
    
    public static boolean testAll(final int value, final int flags) {
        return (value & flags) == flags;
    }
    
    public static boolean testAny(final long value, final long flags) {
        return (value & flags) != 0x0L;
    }
    
    public static boolean testAll(final long value, final long flags) {
        return (value & flags) == flags;
    }
    
    public static boolean isEnum(final TypeDefinition symbol) {
        return (symbol.getModifiers() & 0x4000) != 0x0;
    }
    
    public static long fromStandardFlags(final long accessFlags, final Kind kind) {
        long flags = accessFlags;
        if (testAny(accessFlags, 32L)) {
            flags |= ((kind == Kind.Class || kind == Kind.InnerClass) ? 35184372088832L : 32L);
        }
        if (testAny(accessFlags, 64L)) {
            flags |= ((kind == Kind.Field) ? 64L : 2147483648L);
        }
        if (testAny(accessFlags, 128L)) {
            flags |= ((kind == Kind.Field) ? 128L : 17179869184L);
        }
        return flags;
    }
    
    public enum Flag
    {
        PUBLIC("PUBLIC", 0, "public"), 
        PRIVATE("PRIVATE", 1, "private"), 
        PROTECTED("PROTECTED", 2, "protected"), 
        STATIC("STATIC", 3, "static"), 
        FINAL("FINAL", 4, "final"), 
        SYNCHRONIZED("SYNCHRONIZED", 5, "synchronized"), 
        VOLATILE("VOLATILE", 6, "volatile"), 
        TRANSIENT("TRANSIENT", 7, "transient"), 
        NATIVE("NATIVE", 8, "native"), 
        INTERFACE("INTERFACE", 9, "interface"), 
        ABSTRACT("ABSTRACT", 10, "abstract"), 
        DEFAULT("DEFAULT", 11, "default"), 
        STRICTFP("STRICTFP", 12, "strictfp"), 
        SUPER("SUPER", 13, "super"), 
        BRIDGE("BRIDGE", 14, "bridge"), 
        SYNTHETIC("SYNTHETIC", 15, "synthetic"), 
        DEPRECATED("DEPRECATED", 16, "deprecated"), 
        HASINIT("HASINIT", 17, "hasinit"), 
        ENUM("ENUM", 18, "enum"), 
        MANDATED("MANDATED", 19, "mandated"), 
        IPROXY("IPROXY", 20, "iproxy"), 
        NOOUTERTHIS("NOOUTERTHIS", 21, "noouterthis"), 
        EXISTS("EXISTS", 22, "exists"), 
        COMPOUND("COMPOUND", 23, "compound"), 
        CLASS_SEEN("CLASS_SEEN", 24, "class_seen"), 
        SOURCE_SEEN("SOURCE_SEEN", 25, "source_seen"), 
        LOCKED("LOCKED", 26, "locked"), 
        UNATTRIBUTED("UNATTRIBUTED", 27, "unattributed"), 
        ANONCONSTR("ANONCONSTR", 28, "anonconstr"), 
        ACYCLIC("ACYCLIC", 29, "acyclic"), 
        PARAMETER("PARAMETER", 30, "parameter"), 
        VARARGS("VARARGS", 31, "varargs"), 
        PACKAGE("PACKAGE", 32, "package");
        
        public final String name;
        
        private Flag(final String param_0, final int param_1, final String name) {
            this.name = name;
        }
        
        @Override
        public String toString() {
            return this.name;
        }
    }
    
    public enum Kind
    {
        Class("Class", 0), 
        InnerClass("InnerClass", 1), 
        Field("Field", 2), 
        Method("Method", 3);
    }
}
