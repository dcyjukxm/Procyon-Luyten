package com.strobel.core;

import java.lang.reflect.*;
import com.strobel.reflection.*;
import java.io.*;

public final class ExceptionUtilities
{
    public static RuntimeException asRuntimeException(final Throwable t) {
        VerifyArgument.notNull(t, "t");
        if (t instanceof RuntimeException) {
            return (RuntimeException)t;
        }
        return new UndeclaredThrowableException(t, "An unhandled checked exception occurred.");
    }
    
    public static Throwable unwrap(final Throwable t) {
        final Throwable cause = t.getCause();
        if (cause == null || cause == t) {
            return t;
        }
        if (t instanceof InvocationTargetException || t instanceof TargetInvocationException || t instanceof UndeclaredThrowableException) {
            return unwrap(cause);
        }
        return t;
    }
    
    public static String getMessage(final Throwable t) {
        final String message = VerifyArgument.notNull(t, "t").getMessage();
        if (StringUtilities.isNullOrWhitespace(message)) {
            return String.valueOf(t.getClass().getSimpleName()) + " was thrown.";
        }
        return message;
    }
    
    public static String getStackTraceString(final Throwable t) {
        VerifyArgument.notNull(t, "t");
        try {
            Throwable loc_0 = null;
            try {
                final ByteArrayOutputStream stream = new ByteArrayOutputStream(1024);
                try {
                    final PrintWriter writer = new PrintWriter(stream);
                    try {
                        t.printStackTrace(writer);
                        writer.flush();
                        stream.flush();
                        final String loc_1 = StringUtilities.trimRight(stream.toString());
                        if (writer != null) {
                            writer.close();
                        }
                        if (stream != null) {
                            stream.close();
                        }
                        return loc_1;
                    }
                    finally {
                        if (writer != null) {
                            writer.close();
                        }
                    }
                }
                finally {
                    if (loc_0 == null) {
                        final Throwable loc_2;
                        loc_0 = loc_2;
                    }
                    else {
                        final Throwable loc_2;
                        if (loc_0 != loc_2) {
                            loc_0.addSuppressed(loc_2);
                        }
                    }
                    if (stream != null) {
                        stream.close();
                    }
                }
            }
            finally {
                if (loc_0 == null) {
                    final Throwable loc_3;
                    loc_0 = loc_3;
                }
                else {
                    final Throwable loc_3;
                    if (loc_0 != loc_3) {
                        loc_0.addSuppressed(loc_3);
                    }
                }
            }
        }
        catch (Throwable ignored) {
            return t.toString();
        }
    }
}
