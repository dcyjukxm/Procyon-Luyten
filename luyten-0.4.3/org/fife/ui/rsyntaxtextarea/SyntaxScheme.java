package org.fife.ui.rsyntaxtextarea;

import javax.swing.text.*;
import org.fife.ui.rtextarea.*;
import java.io.*;
import java.awt.*;
import org.xml.sax.helpers.*;
import org.xml.sax.*;
import java.lang.reflect.*;

public class SyntaxScheme implements Cloneable, TokenTypes
{
    private Style[] styles;
    private static final String VERSION = "*ver1";
    
    public SyntaxScheme(final boolean useDefaults) {
        super();
        this.styles = new Style[39];
        if (useDefaults) {
            this.restoreDefaults(null);
        }
    }
    
    public SyntaxScheme(final Font baseFont) {
        this(baseFont, true);
    }
    
    public SyntaxScheme(final Font baseFont, final boolean fontStyles) {
        super();
        this.styles = new Style[39];
        this.restoreDefaults(baseFont, fontStyles);
    }
    
    void changeBaseFont(final Font old, final Font font) {
        for (int i = 0; i < this.styles.length; ++i) {
            final Style style = this.styles[i];
            if (style != null && style.font != null && style.font.getFamily().equals(old.getFamily()) && style.font.getSize() == old.getSize()) {
                final int s = style.font.getStyle();
                final StyleContext sc = StyleContext.getDefaultStyleContext();
                style.font = sc.getFont(font.getFamily(), s, font.getSize());
            }
        }
    }
    
    public Object clone() {
        SyntaxScheme shcs = null;
        try {
            shcs = (SyntaxScheme)super.clone();
        }
        catch (CloneNotSupportedException cnse) {
            cnse.printStackTrace();
            return null;
        }
        shcs.styles = new Style[this.styles.length];
        for (int i = 0; i < this.styles.length; ++i) {
            final Style s = this.styles[i];
            if (s != null) {
                shcs.styles[i] = (Style)s.clone();
            }
        }
        return shcs;
    }
    
    public boolean equals(final Object otherScheme) {
        if (!(otherScheme instanceof SyntaxScheme)) {
            return false;
        }
        final Style[] otherSchemes = ((SyntaxScheme)otherScheme).styles;
        for (int length = this.styles.length, i = 0; i < length; ++i) {
            if (this.styles[i] == null) {
                if (otherSchemes[i] != null) {
                    return false;
                }
            }
            else if (!this.styles[i].equals(otherSchemes[i])) {
                return false;
            }
        }
        return true;
    }
    
    private static final String getHexString(final Color c) {
        return "$" + Integer.toHexString((c.getRGB() & 0xFFFFFF) + 16777216).substring(1);
    }
    
    public Style getStyle(final int index) {
        return this.styles[index];
    }
    
    public int getStyleCount() {
        return this.styles.length;
    }
    
    public Style[] getStyles() {
        return this.styles;
    }
    
    public int hashCode() {
        int hashCode = 0;
        for (int count = this.styles.length, i = 0; i < count; ++i) {
            if (this.styles[i] != null) {
                hashCode ^= this.styles[i].hashCode();
                break;
            }
        }
        return hashCode;
    }
    
    public static SyntaxScheme load(Font baseFont, final InputStream in) throws IOException {
        if (baseFont == null) {
            baseFont = RTextAreaBase.getDefaultFont();
        }
        return SyntaxSchemeLoader.load(baseFont, in);
    }
    
    public static SyntaxScheme loadFromString(final String string) {
        return loadFromString(string, 39);
    }
    
    public static SyntaxScheme loadFromString(final String string, final int tokenTypeCount) {
        final SyntaxScheme scheme = new SyntaxScheme(true);
        try {
            if (string != null) {
                final String[] tokens = string.split(",", -1);
                if (tokens.length == 0 || !"*ver1".equals(tokens[0])) {
                    return scheme;
                }
                final int tokenCount = tokenTypeCount * 7 + 1;
                if (tokens.length != tokenCount) {
                    throw new Exception("Not enough tokens in packed color scheme: expected " + tokenCount + ", found " + tokens.length);
                }
                final StyleContext sc = StyleContext.getDefaultStyleContext();
                for (int i = 0; i < tokenTypeCount; ++i) {
                    final int pos = i * 7 + 1;
                    final int integer = Integer.parseInt(tokens[pos]);
                    if (integer != i) {
                        throw new Exception("Expected " + i + ", found " + integer);
                    }
                    Color fg = null;
                    String temp = tokens[pos + 1];
                    if (!"-".equals(temp)) {
                        fg = stringToColor(temp);
                    }
                    Color bg = null;
                    temp = tokens[pos + 2];
                    if (!"-".equals(temp)) {
                        bg = stringToColor(temp);
                    }
                    temp = tokens[pos + 3];
                    if (!"t".equals(temp) && !"f".equals(temp)) {
                        throw new Exception("Expected 't' or 'f', found " + temp);
                    }
                    final boolean underline = "t".equals(temp);
                    Font font = null;
                    final String family = tokens[pos + 4];
                    if (!"-".equals(family)) {
                        font = sc.getFont(family, Integer.parseInt(tokens[pos + 5]), Integer.parseInt(tokens[pos + 6]));
                    }
                    scheme.styles[i] = new Style(fg, bg, font, underline);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return scheme;
    }
    
    void refreshFontMetrics(final Graphics2D g2d) {
        for (int i = 0; i < this.styles.length; ++i) {
            final Style s = this.styles[i];
            if (s != null) {
                s.fontMetrics = ((s.font == null) ? null : g2d.getFontMetrics(s.font));
            }
        }
    }
    
    public void restoreDefaults(final Font baseFont) {
        this.restoreDefaults(baseFont, true);
    }
    
    public void restoreDefaults(Font baseFont, final boolean fontStyles) {
        final Color comment = new Color(0, 128, 0);
        final Color docComment = new Color(164, 0, 0);
        final Color markupComment = new Color(0, 96, 0);
        final Color keyword = Color.BLUE;
        final Color dataType = new Color(0, 128, 128);
        final Color function = new Color(173, 128, 0);
        final Color preprocessor = new Color(128, 128, 128);
        final Color operator = new Color(128, 64, 64);
        final Color regex = new Color(0, 128, 164);
        final Color variable = new Color(255, 153, 0);
        final Color literalNumber = new Color(100, 0, 200);
        final Color literalString = new Color(220, 0, 156);
        final Color error = new Color(148, 148, 0);
        if (baseFont == null) {
            baseFont = RTextAreaBase.getDefaultFont();
        }
        Font commentFont = baseFont;
        Font keywordFont = baseFont;
        if (fontStyles) {
            final StyleContext sc = StyleContext.getDefaultStyleContext();
            final Font boldFont = sc.getFont(baseFont.getFamily(), 1, baseFont.getSize());
            final Font italicFont = commentFont = sc.getFont(baseFont.getFamily(), 2, baseFont.getSize());
            keywordFont = boldFont;
        }
        this.styles[1] = new Style(comment, null, commentFont);
        this.styles[2] = new Style(comment, null, commentFont);
        this.styles[3] = new Style(docComment, null, commentFont);
        this.styles[4] = new Style(new Color(255, 152, 0), null, commentFont);
        this.styles[5] = new Style(Color.gray, null, commentFont);
        this.styles[6] = new Style(keyword, null, keywordFont);
        this.styles[7] = new Style(keyword, null, keywordFont);
        this.styles[8] = new Style(function);
        this.styles[9] = new Style(literalNumber);
        this.styles[10] = new Style(literalNumber);
        this.styles[11] = new Style(literalNumber);
        this.styles[12] = new Style(literalNumber);
        this.styles[13] = new Style(literalString);
        this.styles[14] = new Style(literalString);
        this.styles[15] = new Style(literalString);
        this.styles[16] = new Style(dataType, null, keywordFont);
        this.styles[17] = new Style(variable);
        this.styles[18] = new Style(regex);
        this.styles[19] = new Style(Color.gray);
        this.styles[20] = new Style(null);
        this.styles[21] = new Style(Color.gray);
        this.styles[22] = new Style(Color.RED);
        this.styles[23] = new Style(operator);
        this.styles[24] = new Style(preprocessor);
        this.styles[25] = new Style(Color.RED);
        this.styles[26] = new Style(Color.BLUE);
        this.styles[27] = new Style(new Color(63, 127, 127));
        this.styles[28] = new Style(literalString);
        this.styles[29] = new Style(markupComment, null, commentFont);
        this.styles[30] = new Style(function);
        this.styles[31] = new Style(preprocessor);
        this.styles[33] = new Style(new Color(13395456));
        this.styles[32] = new Style(new Color(32896));
        this.styles[34] = new Style(dataType);
        this.styles[35] = new Style(error);
        this.styles[36] = new Style(error);
        this.styles[37] = new Style(error);
        this.styles[38] = new Style(error);
        for (int i = 0; i < this.styles.length; ++i) {
            if (this.styles[i] == null) {
                this.styles[i] = new Style();
            }
        }
    }
    
    public void setStyle(final int type, final Style style) {
        this.styles[type] = style;
    }
    
    public void setStyles(final Style[] styles) {
        this.styles = styles;
    }
    
    private static final Color stringToColor(final String s) {
        final char ch = s.charAt(0);
        return new Color((ch == '$' || ch == '#') ? Integer.parseInt(s.substring(1), 16) : Integer.parseInt(s));
    }
    
    public String toCommaSeparatedString() {
        final StringBuilder sb = new StringBuilder("*ver1");
        sb.append(',');
        for (int i = 0; i < this.styles.length; ++i) {
            sb.append(i).append(',');
            final Style ss = this.styles[i];
            if (ss == null) {
                sb.append("-,-,f,-,,,");
            }
            else {
                Color c = ss.foreground;
                sb.append((c != null) ? (getHexString(c) + ",") : "-,");
                c = ss.background;
                sb.append((c != null) ? (getHexString(c) + ",") : "-,");
                sb.append(ss.underline ? "t," : "f,");
                final Font font = ss.font;
                if (font != null) {
                    sb.append(font.getFamily()).append(',').append(font.getStyle()).append(',').append(font.getSize()).append(',');
                }
                else {
                    sb.append("-,,,");
                }
            }
        }
        return sb.substring(0, sb.length() - 1);
    }
    
    static /* synthetic */ Color access$000(final String x0) {
        return stringToColor(x0);
    }
    
    static /* synthetic */ Style[] access$100(final SyntaxScheme x0) {
        return x0.styles;
    }
    
    private static class SyntaxSchemeLoader extends DefaultHandler
    {
        private Font baseFont;
        private SyntaxScheme scheme;
        
        public SyntaxSchemeLoader(final Font baseFont) {
            super();
            this.scheme = new SyntaxScheme(baseFont);
        }
        
        public static SyntaxScheme load(final Font baseFont, final InputStream in) throws IOException {
            SyntaxSchemeLoader parser = null;
            try {
                final XMLReader reader = XMLReaderFactory.createXMLReader();
                parser = new SyntaxSchemeLoader(baseFont);
                parser.baseFont = baseFont;
                reader.setContentHandler(parser);
                final InputSource is = new InputSource(in);
                is.setEncoding("UTF-8");
                reader.parse(is);
            }
            catch (SAXException se) {
                throw new IOException(se.toString());
            }
            return parser.scheme;
        }
        
        public void startElement(final String uri, final String localName, final String qName, final Attributes attrs) {
            if ("style".equals(qName)) {
                final String type = attrs.getValue("token");
                Field field = null;
                try {
                    field = Token.class.getField(type);
                }
                catch (RuntimeException re) {
                    throw re;
                }
                catch (Exception e3) {
                    System.err.println("Invalid token type: " + type);
                    return;
                }
                if (field.getType() == Integer.TYPE) {
                    int index = 0;
                    try {
                        index = field.getInt(this.scheme);
                    }
                    catch (IllegalArgumentException e) {
                        e.printStackTrace();
                        return;
                    }
                    catch (IllegalAccessException e2) {
                        e2.printStackTrace();
                        return;
                    }
                    final String fgStr = attrs.getValue("fg");
                    if (fgStr != null) {
                        final Color fg = SyntaxScheme.access$000(fgStr);
                        SyntaxScheme.access$100(this.scheme)[index].foreground = fg;
                    }
                    final String bgStr = attrs.getValue("bg");
                    if (bgStr != null) {
                        final Color bg = SyntaxScheme.access$000(bgStr);
                        SyntaxScheme.access$100(this.scheme)[index].background = bg;
                    }
                    boolean styleSpecified = false;
                    boolean bold = false;
                    boolean italic = false;
                    final String boldStr = attrs.getValue("bold");
                    if (boldStr != null) {
                        bold = Boolean.valueOf(boldStr);
                        styleSpecified = true;
                    }
                    final String italicStr = attrs.getValue("italic");
                    if (italicStr != null) {
                        italic = Boolean.valueOf(italicStr);
                        styleSpecified = true;
                    }
                    if (styleSpecified) {
                        int style = 0;
                        if (bold) {
                            style |= 0x1;
                        }
                        if (italic) {
                            style |= 0x2;
                        }
                        SyntaxScheme.access$100(this.scheme)[index].font = this.baseFont.deriveFont(style);
                    }
                    final String ulineStr = attrs.getValue("underline");
                    if (ulineStr != null) {
                        final boolean uline = Boolean.valueOf(ulineStr);
                        SyntaxScheme.access$100(this.scheme)[index].underline = uline;
                    }
                }
            }
        }
    }
}
