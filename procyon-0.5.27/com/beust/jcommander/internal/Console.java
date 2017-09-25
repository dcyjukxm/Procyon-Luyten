package com.beust.jcommander.internal;

public interface Console
{
    void print(String param_0);
    
    void println(String param_0);
    
    char[] readPassword(boolean param_0);
}
