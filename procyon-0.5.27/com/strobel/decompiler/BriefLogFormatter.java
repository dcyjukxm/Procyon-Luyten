package com.strobel.decompiler;

import java.text.*;
import java.util.logging.*;
import com.strobel.annotations.*;
import java.util.*;

final class BriefLogFormatter extends Formatter
{
    private static final DateFormat format;
    private static final String lineSep;
    
    static {
        format = new SimpleDateFormat("h:mm:ss");
        lineSep = System.getProperty("line.separator");
    }
    
    @Override
    public String format(@NotNull final LogRecord record) {
        String loggerName = record.getLoggerName();
        if (loggerName == null) {
            loggerName = "root";
        }
        return String.valueOf(BriefLogFormatter.format.format(new Date(record.getMillis()))) + " [" + record.getLevel() + "] " + loggerName + ": " + record.getMessage() + ' ' + BriefLogFormatter.lineSep;
    }
}
