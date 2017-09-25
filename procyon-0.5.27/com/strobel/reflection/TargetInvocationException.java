package com.strobel.reflection;

public class TargetInvocationException extends RuntimeException
{
    private static final String DefaultMessage = "Exception has been thrown by the target of an invocation.";
    
    public TargetInvocationException() {
        super("Exception has been thrown by the target of an invocation.");
    }
    
    public TargetInvocationException(final String message) {
        super(message);
    }
    
    public TargetInvocationException(final String message, final Throwable cause) {
        super(message, cause);
    }
    
    public TargetInvocationException(final Throwable cause) {
        super("Exception has been thrown by the target of an invocation.", cause);
    }
    
    public TargetInvocationException(final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
        super("Exception has been thrown by the target of an invocation.", cause, enableSuppression, writableStackTrace);
    }
    
    public TargetInvocationException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
