package com.strobel.core;

public class Fences
{
    private static volatile int theVolatile;
    
    public static <T> T orderReads(final T ref) {
        final int ignore = Fences.theVolatile;
        return ref;
    }
    
    public static <T> T orderWrites(final T ref) {
        Fences.theVolatile = 0;
        return ref;
    }
    
    public static <T> T orderAccesses(final T ref) {
        Fences.theVolatile = 0;
        return ref;
    }
    
    public static void reachabilityFence(final Object ref) {
        if (ref != null) {
        }
        // monitorenter(ref)
        // monitorexit(ref)
    }
}
