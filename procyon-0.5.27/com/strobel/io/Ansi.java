package com.strobel.io;

import com.strobel.core.*;
import java.io.*;

public class Ansi
{
    public static final boolean SUPPORTED;
    private static final String PREFIX = "\u001b[";
    private static final String SUFFIX = "m";
    private static final String XTERM_256_SEPARATOR = "5;";
    private static final String SEPARATOR = ";";
    private static final String END = "\u001b[m";
    private String start;
    
    static {
        SUPPORTED = (Boolean.getBoolean("Ansi") || (OS.get().isUnix() && System.console() != null));
    }
    
    public Ansi(final Attribute attr, final Color foreground, final Color background) {
        super();
        this.start = "";
        this.init(attr, AnsiColor.forStandardColor(foreground), AnsiColor.forStandardColor(background));
    }
    
    public Ansi(final Attribute attr, final AnsiColor foreground, final AnsiColor background) {
        super();
        this.start = "";
        this.init(attr, foreground, background);
    }
    
    public Ansi(final String format) {
        super();
        this.start = "";
        final String[] tokens = format.split(";");
        Attribute attribute = null;
        try {
            if (tokens.length > 0 && tokens[0].length() > 0) {
                attribute = Attribute.valueOf(tokens[0]);
            }
        }
        catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
        Color foreground = null;
        try {
            if (tokens.length > 1 && tokens[1].length() > 0) {
                foreground = Color.valueOf(tokens[1]);
            }
        }
        catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        Color background = null;
        try {
            if (tokens.length > 2 && tokens[2].length() > 0) {
                background = Color.valueOf(tokens[2]);
            }
        }
        catch (IllegalArgumentException e2) {
            e2.printStackTrace();
        }
        this.init(attribute, AnsiColor.forStandardColor(foreground), AnsiColor.forStandardColor(background));
    }
    
    private void init(final Attribute attr, final AnsiColor foreground, final AnsiColor background) {
        final StringBuilder buff = new StringBuilder();
        if (attr != null) {
            buff.append(attr);
        }
        if (foreground != null) {
            if (buff.length() > 0) {
                buff.append(";");
            }
            if (foreground.isStandardColor()) {
                buff.append(30 + AnsiColor.access$1(foreground).ordinal());
            }
            else {
                buff.append(38).append(";").append("5;").append(AnsiColor.access$2(foreground));
            }
        }
        if (background != null) {
            if (buff.length() > 0) {
                buff.append(";");
            }
            if (background.isStandardColor()) {
                buff.append(40 + AnsiColor.access$1(background).ordinal());
            }
            else {
                buff.append(48).append(";").append("5;").append(AnsiColor.access$2(background));
            }
        }
        buff.insert(0, "\u001b[");
        buff.append("m");
        this.start = buff.toString();
    }
    
    @Override
    public String toString() {
        Attribute attr = null;
        Color foreground = null;
        Color background = null;
        String[] loc_1;
        for (int loc_0 = (loc_1 = this.start.substring("\u001b[".length(), this.start.length() - "m".length()).split(";")).length, loc_2 = 0; loc_2 < loc_0; ++loc_2) {
            final String token = loc_1[loc_2];
            final int i = Integer.parseInt(token);
            if (i < 30) {
                Attribute[] loc_4;
                for (int loc_3 = (loc_4 = Attribute.values()).length, loc_5 = 0; loc_5 < loc_3; ++loc_5) {
                    final Attribute value = loc_4[loc_5];
                    if (value.toString().equals(token)) {
                        attr = value;
                        break;
                    }
                }
            }
            else if (i < 40) {
                foreground = Color.values()[i - 30];
            }
            else {
                background = Color.values()[i - 40];
            }
        }
        final StringBuilder buff = new StringBuilder();
        if (attr != null) {
            buff.append(attr.name());
        }
        buff.append(';');
        if (foreground != null) {
            buff.append(foreground.name());
        }
        buff.append(';');
        if (background != null) {
            buff.append(background.name());
        }
        int end;
        for (end = buff.length() - 1; end >= 0 && buff.charAt(end) == ';'; --end) {}
        return buff.substring(0, end + 1);
    }
    
    public String colorize(final String message) {
        if (Ansi.SUPPORTED) {
            final StringBuilder buff = new StringBuilder(this.start.length() + message.length() + "\u001b[m".length());
            buff.append(this.start).append(message).append("\u001b[m");
            return buff.toString();
        }
        return message;
    }
    
    public void print(final PrintStream ps, final String message) {
        if (Ansi.SUPPORTED) {
            ps.print(this.start);
        }
        ps.print(message);
        if (Ansi.SUPPORTED) {
            ps.print("\u001b[m");
        }
    }
    
    public void println(final PrintStream ps, final String message) {
        this.print(ps, message);
        ps.println();
    }
    
    public void format(final PrintStream ps, final String format, final Object... args) {
        if (Ansi.SUPPORTED) {
            ps.print(this.start);
        }
        ps.format(format, args);
        if (Ansi.SUPPORTED) {
            ps.print("\u001b[m");
        }
    }
    
    public void out(final String message) {
        this.print(System.out, message);
    }
    
    public void outLine(final String message) {
        this.println(System.out, message);
    }
    
    public void outFormat(final String format, final Object... args) {
        this.format(System.out, format, args);
    }
    
    public void err(final String message) {
        this.print(System.err, message);
    }
    
    public void errLine(final String message) {
        this.print(System.err, message);
    }
    
    public void errFormat(final String format, final Object... args) {
        this.format(System.err, format, args);
    }
    
    public enum Attribute
    {
        NORMAL("NORMAL", 0, 0), 
        BRIGHT("BRIGHT", 1, 1), 
        DIM("DIM", 2, 2), 
        UNDERLINE("UNDERLINE", 3, 4), 
        BLINK("BLINK", 4, 5), 
        REVERSE("REVERSE", 5, 7), 
        HIDDEN("HIDDEN", 6, 8);
        
        private final String value;
        
        private Attribute(final String param_0, final int param_1, final int value) {
            this.value = String.valueOf(value);
        }
        
        @Override
        public String toString() {
            return new StringBuilder().append(this.value).toString();
        }
    }
    
    public enum Color
    {
        BLACK("BLACK", 0), 
        RED("RED", 1), 
        GREEN("GREEN", 2), 
        YELLOW("YELLOW", 3), 
        BLUE("BLUE", 4), 
        MAGENTA("MAGENTA", 5), 
        CYAN("CYAN", 6), 
        WHITE("WHITE", 7);
    }
    
    public static final class AnsiColor
    {
        public static final AnsiColor BLACK;
        public static final AnsiColor RED;
        public static final AnsiColor GREEN;
        public static final AnsiColor YELLOW;
        public static final AnsiColor BLUE;
        public static final AnsiColor MAGENTA;
        public static final AnsiColor CYAN;
        public static final AnsiColor WHITE;
        private final int _colorIndex;
        private final Color _standardColor;
        private static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$io$Ansi$Color;
        
        static {
            BLACK = new AnsiColor(Color.BLACK);
            RED = new AnsiColor(Color.RED);
            GREEN = new AnsiColor(Color.GREEN);
            YELLOW = new AnsiColor(Color.YELLOW);
            BLUE = new AnsiColor(Color.BLUE);
            MAGENTA = new AnsiColor(Color.MAGENTA);
            CYAN = new AnsiColor(Color.CYAN);
            WHITE = new AnsiColor(Color.WHITE);
        }
        
        public AnsiColor(final int colorIndex) {
            super();
            this._colorIndex = colorIndex;
            this._standardColor = null;
        }
        
        public AnsiColor(final Color standardColor) {
            super();
            this._colorIndex = -1;
            this._standardColor = standardColor;
        }
        
        public final int getColorIndex() {
            return this._colorIndex;
        }
        
        public final boolean isStandardColor() {
            return this._standardColor != null;
        }
        
        public final Color getStandardColor() {
            return this._standardColor;
        }
        
        public static AnsiColor forStandardColor(final Color color) {
            if (color == null) {
                return null;
            }
            switch ($SWITCH_TABLE$com$strobel$io$Ansi$Color()[color.ordinal()]) {
                case 1: {
                    return AnsiColor.BLACK;
                }
                case 2: {
                    return AnsiColor.RED;
                }
                case 3: {
                    return AnsiColor.GREEN;
                }
                case 4: {
                    return AnsiColor.YELLOW;
                }
                case 5: {
                    return AnsiColor.BLUE;
                }
                case 6: {
                    return AnsiColor.MAGENTA;
                }
                case 7: {
                    return AnsiColor.CYAN;
                }
                case 8: {
                    return AnsiColor.WHITE;
                }
                default: {
                    return new AnsiColor(color);
                }
            }
        }
        
        static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$io$Ansi$Color() {
            final int[] loc_0 = AnsiColor.$SWITCH_TABLE$com$strobel$io$Ansi$Color;
            if (loc_0 != null) {
                return loc_0;
            }
            final int[] loc_1 = new int[Color.values().length];
            try {
                loc_1[Color.BLACK.ordinal()] = 1;
            }
            catch (NoSuchFieldError loc_2) {}
            try {
                loc_1[Color.BLUE.ordinal()] = 5;
            }
            catch (NoSuchFieldError loc_3) {}
            try {
                loc_1[Color.CYAN.ordinal()] = 7;
            }
            catch (NoSuchFieldError loc_4) {}
            try {
                loc_1[Color.GREEN.ordinal()] = 3;
            }
            catch (NoSuchFieldError loc_5) {}
            try {
                loc_1[Color.MAGENTA.ordinal()] = 6;
            }
            catch (NoSuchFieldError loc_6) {}
            try {
                loc_1[Color.RED.ordinal()] = 2;
            }
            catch (NoSuchFieldError loc_7) {}
            try {
                loc_1[Color.WHITE.ordinal()] = 8;
            }
            catch (NoSuchFieldError loc_8) {}
            try {
                loc_1[Color.YELLOW.ordinal()] = 4;
            }
            catch (NoSuchFieldError loc_9) {}
            return AnsiColor.$SWITCH_TABLE$com$strobel$io$Ansi$Color = loc_1;
        }
        
        static /* synthetic */ Color access$1(final AnsiColor param_0) {
            return param_0._standardColor;
        }
        
        static /* synthetic */ int access$2(final AnsiColor param_0) {
            return param_0._colorIndex;
        }
    }
}
