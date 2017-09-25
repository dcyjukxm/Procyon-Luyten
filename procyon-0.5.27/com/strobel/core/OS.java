package com.strobel.core;

import com.strobel.util.*;

public enum OS
{
    WINDOWS_NT("WINDOWS_NT", 0, new String[] { "Windows NT" }), 
    WINDOWS_95("WINDOWS_95", 1, new String[] { "Windows 95" }), 
    WINDOWS_98("WINDOWS_98", 2, new String[] { "Windows 98" }), 
    WINDOWS_2000("WINDOWS_2000", 3, new String[] { "Windows 2000" }), 
    WINDOWS_VISTA("WINDOWS_VISTA", 4, new String[] { "Windows Vista" }), 
    WINDOWS_7("WINDOWS_7", 5, new String[] { "Windows 7" }), 
    WINDOWS_OTHER("WINDOWS_OTHER", 6, new String[] { "Windows" }), 
    SOLARIS("SOLARIS", 7, new String[] { "Solaris" }), 
    LINUX("LINUX", 8, new String[] { "Linux" }), 
    HP_UX("HP_UX", 9, new String[] { "HP-UX" }), 
    IBM_AIX("IBM_AIX", 10, new String[] { "AIX" }), 
    SGI_IRIX("SGI_IRIX", 11, new String[] { "Irix" }), 
    SUN_OS("SUN_OS", 12, new String[] { "SunOS" }), 
    COMPAQ_TRU64_UNIX("COMPAQ_TRU64_UNIX", 13, new String[] { "Digital UNIX" }), 
    MAC("MAC", 14, new String[] { "Mac OS X", "Darwin" }), 
    FREE_BSD("FREE_BSD", 15, new String[] { "freebsd" }), 
    OS2("OS2", 16, new String[] { "OS/2" }), 
    COMPAQ_OPEN_VMS("COMPAQ_OPEN_VMS", 17, new String[] { "OpenVMS" }), 
    OTHER("OTHER", 18, new String[] { "" });
    
    private final String[] names;
    private static OS current;
    
    private OS(final String param_0, final int param_1, final String... names) {
        this.names = names;
    }
    
    public boolean isWindows() {
        return this.ordinal() <= OS.WINDOWS_OTHER.ordinal();
    }
    
    public boolean isUnix() {
        return this.ordinal() > OS.WINDOWS_OTHER.ordinal() && this.ordinal() < OS.OS2.ordinal();
    }
    
    public static OS get(String osName) {
        osName = osName.toLowerCase();
        OS[] loc_1;
        for (int loc_0 = (loc_1 = values()).length, loc_2 = 0; loc_2 < loc_0; ++loc_2) {
            final OS os = loc_1[loc_2];
            String[] loc_4;
            for (int loc_3 = (loc_4 = os.names).length, loc_5 = 0; loc_5 < loc_3; ++loc_5) {
                final String name = loc_4[loc_5];
                if (osName.contains(name.toLowerCase())) {
                    return os;
                }
            }
        }
        throw ContractUtils.unreachable();
    }
    
    public static OS get() {
        if (OS.current == null) {
            OS.current = get(System.getProperty("os.name"));
        }
        return OS.current;
    }
}
