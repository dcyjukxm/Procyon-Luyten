package com.strobel.compilerservices;

public final class CallerResolver extends SecurityManager
{
    private static final CallerResolver CALLER_RESOLVER;
    private static final int CALL_CONTEXT_OFFSET = 3;
    
    static {
        CALLER_RESOLVER = new CallerResolver();
    }
    
    @Override
    protected Class[] getClassContext() {
        return super.getClassContext();
    }
    
    public static Class getCallerClass(final int callerOffset) {
        return CallerResolver.CALLER_RESOLVER.getClassContext()[3 + callerOffset];
    }
    
    public static int getContextSize(final int callerOffset) {
        return CallerResolver.CALLER_RESOLVER.getClassContext().length - callerOffset;
    }
    
    public static int getContextSize() {
        return getContextSize(3);
    }
}
