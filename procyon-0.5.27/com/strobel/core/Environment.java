package com.strobel.core;

import com.strobel.util.*;
import java.util.logging.*;
import java.util.regex.*;

public final class Environment
{
    private static final Logger logger;
    private static final Pattern VARIABLE_PATTERN;
    private static final String OS_NAME;
    private static final String OS_NAME_LOWER;
    private static final String OS_ARCH;
    private static final String ARCH_DATA_MODEL;
    
    static {
        logger = Logger.getLogger(Environment.class.getName());
        VARIABLE_PATTERN = Pattern.compile("\\$([a-zA-Z0-9_]+)", 4);
        OS_NAME = System.getProperty("os.name");
        OS_NAME_LOWER = Environment.OS_NAME.toLowerCase();
        OS_ARCH = System.getProperty("os.arch");
        ARCH_DATA_MODEL = System.getProperty("sun.arch.data.model");
    }
    
    private Environment() {
        super();
        throw ContractUtils.unreachable();
    }
    
    public static boolean isWindows() {
        return Environment.OS_NAME_LOWER.startsWith("windows");
    }
    
    public static boolean isOS2() {
        return Environment.OS_NAME_LOWER.startsWith("os/2") || Environment.OS_NAME_LOWER.startsWith("os2");
    }
    
    public static boolean isMac() {
        return Environment.OS_NAME_LOWER.startsWith("mac");
    }
    
    public static boolean isLinux() {
        return Environment.OS_NAME_LOWER.startsWith("linux");
    }
    
    public static boolean isUnix() {
        return !isWindows() && !isOS2();
    }
    
    public static boolean isFileSystemCaseSensitive() {
        return isUnix() && !isMac();
    }
    
    public static boolean is32Bit() {
        return Environment.ARCH_DATA_MODEL == null || Environment.ARCH_DATA_MODEL.equals("32");
    }
    
    public static boolean is64Bit() {
        return !is32Bit();
    }
    
    public static boolean isAmd64() {
        return "amd64".equals(Environment.OS_ARCH);
    }
    
    public static boolean isMacX64() {
        return isMac() && "x86_64".equals(Environment.OS_ARCH);
    }
    
    public static String getVariable(final String variable) {
        if (variable == null) {
            return "";
        }
        final String expanded = System.getenv(variable);
        return (expanded != null) ? expanded : "";
    }
    
    public static String expandVariables(final String s) {
        return expandVariables(s, true);
    }
    
    public static String expandVariables(final String s, final boolean recursive) {
        final Matcher variableMatcher = Environment.VARIABLE_PATTERN.matcher(s);
        StringBuffer expanded = null;
        String variable = null;
        try {
            while (variableMatcher.find()) {
                for (int matches = variableMatcher.groupCount(), i = 1; i <= matches; ++i) {
                    variable = variableMatcher.group(i);
                    if (expanded == null) {
                        expanded = new StringBuffer();
                    }
                    final String variableValue = getVariable(variable);
                    variableMatcher.appendReplacement(expanded, (recursive ? expandVariables(variableValue, true) : variableValue).replace("\\", "\\\\"));
                }
            }
            if (expanded != null) {
                variableMatcher.appendTail(expanded);
            }
        }
        catch (Throwable t) {
            Environment.logger.log(Level.WARNING, String.format("Unable to expand the variable '%s', returning original value: %s", variable, s), t);
            return s;
        }
        if (expanded != null) {
            return expanded.toString();
        }
        return s;
    }
    
    public static int getProcessorCount() {
        return Runtime.getRuntime().availableProcessors();
    }
    
    public static boolean isSingleProcessor() {
        return getProcessorCount() == 1;
    }
}
