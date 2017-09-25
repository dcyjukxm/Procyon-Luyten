package com.beust.jcommander;

public class ParameterException extends RuntimeException
{
    public ParameterException(final Throwable t) {
        super(t);
    }
    
    public ParameterException(final String string) {
        super(string);
    }
}
