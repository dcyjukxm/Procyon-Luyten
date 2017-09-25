package com.strobel.assembler.metadata;

import java.util.*;

public enum CompilerTarget
{
    JDK1_1("JDK1_1", 0, "1.1", 45, 3), 
    JDK1_2("JDK1_2", 1, "1.2", 46, 0), 
    JDK1_3("JDK1_3", 2, "1.3", 47, 0), 
    JDK1_4("JDK1_4", 3, "1.4", 48, 0), 
    JDK1_5("JDK1_5", 4, "1.5", 49, 0), 
    JDK1_6("JDK1_6", 5, "1.6", 50, 0), 
    JDK1_7("JDK1_7", 6, "1.7", 51, 0), 
    JDK1_8("JDK1_8", 7, "1.8", 52, 0);
    
    private static final CompilerTarget[] VALUES;
    private static final CompilerTarget MIN;
    private static final CompilerTarget MAX;
    private static final Map<String, CompilerTarget> tab;
    public final String name;
    public final int majorVersion;
    public final int minorVersion;
    public static final CompilerTarget DEFAULT;
    
    static {
        VALUES = values();
        MIN = CompilerTarget.VALUES[0];
        MAX = CompilerTarget.VALUES[CompilerTarget.VALUES.length - 1];
        tab = new HashMap<String, CompilerTarget>();
        CompilerTarget[] loc_1;
        for (int loc_0 = (loc_1 = values()).length, loc_2 = 0; loc_2 < loc_0; ++loc_2) {
            final CompilerTarget t = loc_1[loc_2];
            CompilerTarget.tab.put(t.name, t);
        }
        CompilerTarget.tab.put("5", CompilerTarget.JDK1_5);
        CompilerTarget.tab.put("6", CompilerTarget.JDK1_6);
        CompilerTarget.tab.put("7", CompilerTarget.JDK1_7);
        CompilerTarget.tab.put("8", CompilerTarget.JDK1_8);
        DEFAULT = CompilerTarget.JDK1_8;
    }
    
    public static CompilerTarget MIN() {
        return CompilerTarget.MIN;
    }
    
    public static CompilerTarget MAX() {
        return CompilerTarget.MAX;
    }
    
    private CompilerTarget(final String param_0, final int param_1, final String name, final int majorVersion, final int minorVersion) {
        this.name = name;
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
    }
    
    public static CompilerTarget lookup(final String name) {
        return CompilerTarget.tab.get(name);
    }
    
    public static CompilerTarget lookup(final int majorVersion, final int minorVersion) {
        CompilerTarget[] loc_1;
        for (int loc_0 = (loc_1 = CompilerTarget.VALUES).length, loc_2 = 0; loc_2 < loc_0; ++loc_2) {
            final CompilerTarget target = loc_1[loc_2];
            if (majorVersion < target.majorVersion) {
                return target;
            }
            if (minorVersion <= target.minorVersion && majorVersion == target.majorVersion) {
                return target;
            }
        }
        return CompilerTarget.MAX;
    }
    
    public boolean requiresIProxy() {
        return this.compareTo(CompilerTarget.JDK1_1) <= 0;
    }
    
    public boolean initializeFieldsBeforeSuper() {
        return this.compareTo(CompilerTarget.JDK1_4) >= 0;
    }
    
    public boolean obeyBinaryCompatibility() {
        return this.compareTo(CompilerTarget.JDK1_2) >= 0;
    }
    
    public boolean arrayBinaryCompatibility() {
        return this.compareTo(CompilerTarget.JDK1_5) >= 0;
    }
    
    public boolean interfaceFieldsBinaryCompatibility() {
        return this.compareTo(CompilerTarget.JDK1_2) > 0;
    }
    
    public boolean interfaceObjectOverridesBinaryCompatibility() {
        return this.compareTo(CompilerTarget.JDK1_5) >= 0;
    }
    
    public boolean usePrivateSyntheticFields() {
        return this.compareTo(CompilerTarget.JDK1_5) < 0;
    }
    
    public boolean useInnerCacheClass() {
        return this.compareTo(CompilerTarget.JDK1_5) >= 0;
    }
    
    public boolean generateCLDCStackMap() {
        return false;
    }
    
    public boolean generateStackMapTable() {
        return this.compareTo(CompilerTarget.JDK1_6) >= 0;
    }
    
    public boolean isPackageInfoSynthetic() {
        return this.compareTo(CompilerTarget.JDK1_6) >= 0;
    }
    
    public boolean generateEmptyAfterBig() {
        return false;
    }
    
    public boolean useStringBuilder() {
        return this.compareTo(CompilerTarget.JDK1_5) >= 0;
    }
    
    public boolean useSyntheticFlag() {
        return this.compareTo(CompilerTarget.JDK1_5) >= 0;
    }
    
    public boolean useEnumFlag() {
        return this.compareTo(CompilerTarget.JDK1_5) >= 0;
    }
    
    public boolean useAnnotationFlag() {
        return this.compareTo(CompilerTarget.JDK1_5) >= 0;
    }
    
    public boolean useVarargsFlag() {
        return this.compareTo(CompilerTarget.JDK1_5) >= 0;
    }
    
    public boolean useBridgeFlag() {
        return this.compareTo(CompilerTarget.JDK1_5) >= 0;
    }
    
    public char syntheticNameChar() {
        return '$';
    }
    
    public boolean hasClassLiterals() {
        return this.compareTo(CompilerTarget.JDK1_5) >= 0;
    }
    
    public boolean hasInvokedynamic() {
        return this.compareTo(CompilerTarget.JDK1_7) >= 0;
    }
    
    public boolean hasMethodHandles() {
        return this.hasInvokedynamic();
    }
    
    public boolean classLiteralsNoInit() {
        return this.compareTo(CompilerTarget.JDK1_5) >= 0;
    }
    
    public boolean hasInitCause() {
        return this.compareTo(CompilerTarget.JDK1_4) >= 0;
    }
    
    public boolean boxWithConstructors() {
        return this.compareTo(CompilerTarget.JDK1_5) < 0;
    }
    
    public boolean hasIterable() {
        return this.compareTo(CompilerTarget.JDK1_5) >= 0;
    }
    
    public boolean hasEnclosingMethodAttribute() {
        return this.compareTo(CompilerTarget.JDK1_5) >= 0;
    }
}
