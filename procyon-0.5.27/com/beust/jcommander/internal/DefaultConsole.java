package com.beust.jcommander.internal;

import com.beust.jcommander.*;
import java.io.*;

public class DefaultConsole implements Console
{
    public void print(final String msg) {
        System.out.print(msg);
    }
    
    public void println(final String msg) {
        System.out.println(msg);
    }
    
    public char[] readPassword(final boolean echoInput) {
        try {
            final InputStreamReader isr = new InputStreamReader(System.in);
            final BufferedReader in = new BufferedReader(isr);
            final String result = in.readLine();
            in.close();
            isr.close();
            return result.toCharArray();
        }
        catch (IOException e) {
            throw new ParameterException(e);
        }
    }
}
