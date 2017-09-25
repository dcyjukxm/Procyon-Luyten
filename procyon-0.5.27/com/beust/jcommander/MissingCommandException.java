package com.beust.jcommander;

public class MissingCommandException extends ParameterException
{
    public MissingCommandException(final String string) {
        super(string);
    }
    
    public MissingCommandException(final Throwable t) {
        super(t);
    }
}
