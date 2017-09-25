package com.beust.jcommander.internal;

import java.io.*;
import java.lang.reflect.*;
import com.beust.jcommander.*;

public class JDK6Console implements Console
{
    private Object console;
    private PrintWriter writer;
    
    public JDK6Console(final Object console) throws Exception {
        super();
        this.console = console;
        final Method writerMethod = console.getClass().getDeclaredMethod("writer", (Class<?>[])new Class[0]);
        this.writer = (PrintWriter)writerMethod.invoke(console, new Object[0]);
    }
    
    public void print(final String msg) {
        this.writer.print(msg);
    }
    
    public void println(final String msg) {
        this.writer.println(msg);
    }
    
    public char[] readPassword(final boolean echoInput) {
        try {
            this.writer.flush();
            if (echoInput) {
                final Method method = this.console.getClass().getDeclaredMethod("readLine", (Class<?>[])new Class[0]);
                return ((String)method.invoke(this.console, new Object[0])).toCharArray();
            }
            final Method method = this.console.getClass().getDeclaredMethod("readPassword", (Class<?>[])new Class[0]);
            return (char[])method.invoke(this.console, new Object[0]);
        }
        catch (Exception e) {
            throw new ParameterException(e);
        }
    }
}
