package org.fife.ui.rsyntaxtextarea;

import org.fife.ui.rtextarea.*;
import javax.swing.*;
import java.awt.*;
import javax.swing.plaf.*;
import javax.swing.text.*;
import javax.xml.transform.dom.*;
import org.fife.io.*;
import java.io.*;
import javax.xml.transform.stream.*;
import org.w3c.dom.*;
import java.lang.reflect.*;
import javax.xml.transform.*;
import org.xml.sax.helpers.*;
import javax.xml.parsers.*;
import org.xml.sax.*;

public class Theme
{
    private Font baseFont;
    private Color bgColor;
    private Color caretColor;
    private boolean useSelctionFG;
    private Color selectionFG;
    private Color selectionBG;
    private boolean selectionRoundedEdges;
    private Color currentLineHighlight;
    private boolean fadeCurrentLineHighlight;
    private Color marginLineColor;
    private Color markAllHighlightColor;
    private Color markOccurrencesColor;
    private boolean markOccurrencesBorder;
    private Color matchedBracketFG;
    private Color matchedBracketBG;
    private boolean matchedBracketHighlightBoth;
    private boolean matchedBracketAnimate;
    private Color hyperlinkFG;
    private Color[] secondaryLanguages;
    private SyntaxScheme scheme;
    private Color gutterBorderColor;
    private Color activeLineRangeColor;
    private boolean iconRowHeaderInheritsGutterBG;
    private Color lineNumberColor;
    private String lineNumberFont;
    private int lineNumberFontSize;
    private Color foldIndicatorFG;
    private Color foldBG;
    
    private Theme(final Font baseFont) {
        super();
        this.baseFont = ((baseFont != null) ? baseFont : RTextAreaBase.getDefaultFont());
        this.secondaryLanguages = new Color[3];
        this.activeLineRangeColor = Gutter.DEFAULT_ACTIVE_LINE_RANGE_COLOR;
    }
    
    public Theme(final RSyntaxTextArea textArea) {
        super();
        this.baseFont = textArea.getFont();
        this.bgColor = textArea.getBackground();
        this.caretColor = textArea.getCaretColor();
        this.useSelctionFG = textArea.getUseSelectedTextColor();
        this.selectionFG = textArea.getSelectedTextColor();
        this.selectionBG = textArea.getSelectionColor();
        this.selectionRoundedEdges = textArea.getRoundedSelectionEdges();
        this.currentLineHighlight = textArea.getCurrentLineHighlightColor();
        this.fadeCurrentLineHighlight = textArea.getFadeCurrentLineHighlight();
        this.marginLineColor = textArea.getMarginLineColor();
        this.markAllHighlightColor = textArea.getMarkAllHighlightColor();
        this.markOccurrencesColor = textArea.getMarkOccurrencesColor();
        this.markOccurrencesBorder = textArea.getPaintMarkOccurrencesBorder();
        this.matchedBracketBG = textArea.getMatchedBracketBGColor();
        this.matchedBracketFG = textArea.getMatchedBracketBorderColor();
        this.matchedBracketHighlightBoth = textArea.getPaintMatchedBracketPair();
        this.matchedBracketAnimate = textArea.getAnimateBracketMatching();
        this.hyperlinkFG = textArea.getHyperlinkForeground();
        final int count = textArea.getSecondaryLanguageCount();
        this.secondaryLanguages = new Color[count];
        for (int i = 0; i < count; ++i) {
            this.secondaryLanguages[i] = textArea.getSecondaryLanguageBackground(i + 1);
        }
        this.scheme = textArea.getSyntaxScheme();
        final Gutter gutter = RSyntaxUtilities.getGutter(textArea);
        if (gutter != null) {
            this.bgColor = gutter.getBackground();
            this.gutterBorderColor = gutter.getBorderColor();
            this.activeLineRangeColor = gutter.getActiveLineRangeColor();
            this.iconRowHeaderInheritsGutterBG = gutter.getIconRowHeaderInheritsGutterBackground();
            this.lineNumberColor = gutter.getLineNumberColor();
            this.lineNumberFont = gutter.getLineNumberFont().getFamily();
            this.lineNumberFontSize = gutter.getLineNumberFont().getSize();
            this.foldIndicatorFG = gutter.getFoldIndicatorForeground();
            this.foldBG = gutter.getFoldBackground();
        }
    }
    
    public void apply(final RSyntaxTextArea textArea) {
        textArea.setFont(this.baseFont);
        textArea.setBackground(this.bgColor);
        textArea.setCaretColor(this.caretColor);
        textArea.setUseSelectedTextColor(this.useSelctionFG);
        textArea.setSelectedTextColor(this.selectionFG);
        textArea.setSelectionColor(this.selectionBG);
        textArea.setRoundedSelectionEdges(this.selectionRoundedEdges);
        textArea.setCurrentLineHighlightColor(this.currentLineHighlight);
        textArea.setFadeCurrentLineHighlight(this.fadeCurrentLineHighlight);
        textArea.setMarginLineColor(this.marginLineColor);
        textArea.setMarkAllHighlightColor(this.markAllHighlightColor);
        textArea.setMarkOccurrencesColor(this.markOccurrencesColor);
        textArea.setPaintMarkOccurrencesBorder(this.markOccurrencesBorder);
        textArea.setMatchedBracketBGColor(this.matchedBracketBG);
        textArea.setMatchedBracketBorderColor(this.matchedBracketFG);
        textArea.setPaintMatchedBracketPair(this.matchedBracketHighlightBoth);
        textArea.setAnimateBracketMatching(this.matchedBracketAnimate);
        textArea.setHyperlinkForeground(this.hyperlinkFG);
        for (int count = this.secondaryLanguages.length, i = 0; i < count; ++i) {
            textArea.setSecondaryLanguageBackground(i + 1, this.secondaryLanguages[i]);
        }
        textArea.setSyntaxScheme(this.scheme);
        final Gutter gutter = RSyntaxUtilities.getGutter(textArea);
        if (gutter != null) {
            gutter.setBackground(this.bgColor);
            gutter.setBorderColor(this.gutterBorderColor);
            gutter.setActiveLineRangeColor(this.activeLineRangeColor);
            gutter.setIconRowHeaderInheritsGutterBackground(this.iconRowHeaderInheritsGutterBG);
            gutter.setLineNumberColor(this.lineNumberColor);
            final String fontName = (this.lineNumberFont != null) ? this.lineNumberFont : this.baseFont.getFamily();
            final int fontSize = (this.lineNumberFontSize > 0) ? this.lineNumberFontSize : this.baseFont.getSize();
            final Font font = getFont(fontName, 0, fontSize);
            gutter.setLineNumberFont(font);
            gutter.setFoldIndicatorForeground(this.foldIndicatorFG);
            gutter.setFoldBackground(this.foldBG);
        }
    }
    
    private static final String colorToString(final Color c) {
        final int color = c.getRGB() & 0xFFFFFF;
        String str;
        for (str = Integer.toHexString(color); str.length() < 6; str = "0" + str) {}
        return str;
    }
    
    private static final Color getDefaultBG() {
        Color c = UIManager.getColor("nimbusLightBackground");
        if (c == null) {
            c = UIManager.getColor("TextArea.background");
            if (c == null) {
                c = new ColorUIResource(SystemColor.text);
            }
        }
        return c;
    }
    
    private static final Color getDefaultSelectionBG() {
        Color c = UIManager.getColor("TextArea.selectionBackground");
        if (c == null) {
            c = UIManager.getColor("textHighlight");
            if (c == null) {
                c = UIManager.getColor("nimbusSelectionBackground");
                if (c == null) {
                    c = new ColorUIResource(SystemColor.textHighlight);
                }
            }
        }
        return c;
    }
    
    private static Color getDefaultSelectionFG() {
        Color c = UIManager.getColor("TextArea.selectionForeground");
        if (c == null) {
            c = UIManager.getColor("textHighlightText");
            if (c == null) {
                c = UIManager.getColor("nimbusSelectedText");
                if (c == null) {
                    c = new ColorUIResource(SystemColor.textHighlightText);
                }
            }
        }
        return c;
    }
    
    private static Font getFont(final String family, final int style, final int size) {
        final StyleContext sc = StyleContext.getDefaultStyleContext();
        return sc.getFont(family, style, size);
    }
    
    public static Theme load(final InputStream in) throws IOException {
        return load(in, null);
    }
    
    public static Theme load(final InputStream in, final Font baseFont) throws IOException {
        final Theme theme = new Theme(baseFont);
        final BufferedInputStream bin = new BufferedInputStream(in);
        try {
            XmlHandler.load(theme, bin);
        }
        finally {
            bin.close();
        }
        return theme;
    }
    
    public void save(final OutputStream out) throws IOException {
        final BufferedOutputStream bout = new BufferedOutputStream(out);
        try {
            final DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            final DOMImplementation impl = db.getDOMImplementation();
            final Document doc = impl.createDocument(null, "RSyntaxTheme", null);
            final Element root = doc.getDocumentElement();
            root.setAttribute("version", "1.0");
            Element elem = doc.createElement("baseFont");
            if (!this.baseFont.getFamily().equals(RTextAreaBase.getDefaultFont().getFamily())) {
                elem.setAttribute("family", this.baseFont.getFamily());
            }
            elem.setAttribute("size", Integer.toString(this.baseFont.getSize()));
            root.appendChild(elem);
            elem = doc.createElement("background");
            elem.setAttribute("color", colorToString(this.bgColor));
            root.appendChild(elem);
            elem = doc.createElement("caret");
            elem.setAttribute("color", colorToString(this.caretColor));
            root.appendChild(elem);
            elem = doc.createElement("selection");
            elem.setAttribute("useFG", Boolean.toString(this.useSelctionFG));
            elem.setAttribute("fg", colorToString(this.selectionFG));
            elem.setAttribute("bg", colorToString(this.selectionBG));
            elem.setAttribute("roundedEdges", Boolean.toString(this.selectionRoundedEdges));
            root.appendChild(elem);
            elem = doc.createElement("currentLineHighlight");
            elem.setAttribute("color", colorToString(this.currentLineHighlight));
            elem.setAttribute("fade", Boolean.toString(this.fadeCurrentLineHighlight));
            root.appendChild(elem);
            elem = doc.createElement("marginLine");
            elem.setAttribute("fg", colorToString(this.marginLineColor));
            root.appendChild(elem);
            elem = doc.createElement("markAllHighlight");
            elem.setAttribute("color", colorToString(this.markAllHighlightColor));
            root.appendChild(elem);
            elem = doc.createElement("markOccurrencesHighlight");
            elem.setAttribute("color", colorToString(this.markOccurrencesColor));
            elem.setAttribute("border", Boolean.toString(this.markOccurrencesBorder));
            root.appendChild(elem);
            elem = doc.createElement("matchedBracket");
            elem.setAttribute("fg", colorToString(this.matchedBracketFG));
            elem.setAttribute("bg", colorToString(this.matchedBracketBG));
            elem.setAttribute("highlightBoth", Boolean.toString(this.matchedBracketHighlightBoth));
            elem.setAttribute("animate", Boolean.toString(this.matchedBracketAnimate));
            root.appendChild(elem);
            elem = doc.createElement("hyperlinks");
            elem.setAttribute("fg", colorToString(this.hyperlinkFG));
            root.appendChild(elem);
            elem = doc.createElement("secondaryLanguages");
            for (int i = 0; i < this.secondaryLanguages.length; ++i) {
                final Color color = this.secondaryLanguages[i];
                final Element elem2 = doc.createElement("language");
                elem2.setAttribute("index", Integer.toString(i + 1));
                elem2.setAttribute("bg", (color == null) ? "" : colorToString(color));
                elem.appendChild(elem2);
            }
            root.appendChild(elem);
            elem = doc.createElement("gutterBorder");
            elem.setAttribute("color", colorToString(this.gutterBorderColor));
            root.appendChild(elem);
            elem = doc.createElement("lineNumbers");
            elem.setAttribute("fg", colorToString(this.lineNumberColor));
            if (this.lineNumberFont != null) {
                elem.setAttribute("lineNumberFont", this.lineNumberFont);
            }
            if (this.lineNumberFontSize > 0) {
                elem.setAttribute("lineNumberFontSize", Integer.toString(this.lineNumberFontSize));
            }
            root.appendChild(elem);
            elem = doc.createElement("foldIndicator");
            elem.setAttribute("fg", colorToString(this.foldIndicatorFG));
            elem.setAttribute("iconBg", colorToString(this.foldBG));
            root.appendChild(elem);
            elem = doc.createElement("iconRowHeader");
            elem.setAttribute("activeLineRange", colorToString(this.activeLineRangeColor));
            elem.setAttribute("inheritsGutterBG", Boolean.toString(this.iconRowHeaderInheritsGutterBG));
            root.appendChild(elem);
            elem = doc.createElement("tokenStyles");
            final Field[] fields = TokenTypes.class.getFields();
            for (int j = 0; j < fields.length; ++j) {
                final Field field = fields[j];
                final int value = field.getInt(null);
                if (value != 39) {
                    final Style style = this.scheme.getStyle(value);
                    if (style != null) {
                        final Element elem3 = doc.createElement("style");
                        elem3.setAttribute("token", field.getName());
                        final Color fg = style.foreground;
                        if (fg != null) {
                            elem3.setAttribute("fg", colorToString(fg));
                        }
                        final Color bg = style.background;
                        if (bg != null) {
                            elem3.setAttribute("bg", colorToString(bg));
                        }
                        final Font font = style.font;
                        if (font != null) {
                            if (!font.getFamily().equals(this.baseFont.getFamily())) {
                                elem3.setAttribute("fontFamily", font.getFamily());
                            }
                            if (font.getSize() != this.baseFont.getSize()) {
                                elem3.setAttribute("fontSize", Integer.toString(font.getSize()));
                            }
                            if (font.isBold()) {
                                elem3.setAttribute("bold", "true");
                            }
                            if (font.isItalic()) {
                                elem3.setAttribute("italic", "true");
                            }
                        }
                        if (style.underline) {
                            elem3.setAttribute("underline", "true");
                        }
                        elem.appendChild(elem3);
                    }
                }
            }
            root.appendChild(elem);
            final DOMSource source = new DOMSource(doc);
            final StreamResult result = new StreamResult(new PrintWriter(new UnicodeWriter(bout, "UTF-8")));
            final TransformerFactory transFac = TransformerFactory.newInstance();
            final Transformer transformer = transFac.newTransformer();
            transformer.setOutputProperty("indent", "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            transformer.setOutputProperty("encoding", "UTF-8");
            transformer.setOutputProperty("doctype-system", "theme.dtd");
            transformer.transform(source, result);
        }
        catch (RuntimeException re) {
            throw re;
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IOException("Error generating XML: " + e.getMessage());
        }
        finally {
            bout.close();
        }
    }
    
    private static final Color stringToColor(final String s) {
        return stringToColor(s, null);
    }
    
    private static final Color stringToColor(String s, final Color defaultVal) {
        if (s == null || "default".equalsIgnoreCase(s)) {
            return defaultVal;
        }
        if (s.length() == 6 || s.length() == 7) {
            if (s.charAt(0) == '$') {
                s = s.substring(1);
            }
            return new Color(Integer.parseInt(s, 16));
        }
        return null;
    }
    
    static /* synthetic */ Color access$002(final Theme x0, final Color x1) {
        return x0.bgColor = x1;
    }
    
    static /* synthetic */ Color access$100() {
        return getDefaultBG();
    }
    
    static /* synthetic */ Color access$200(final String x0, final Color x1) {
        return stringToColor(x0, x1);
    }
    
    static /* synthetic */ Font access$300(final Theme x0) {
        return x0.baseFont;
    }
    
    static /* synthetic */ Font access$302(final Theme x0, final Font x1) {
        return x0.baseFont = x1;
    }
    
    static /* synthetic */ Font access$400(final String x0, final int x1, final int x2) {
        return getFont(x0, x1, x2);
    }
    
    static /* synthetic */ Color access$502(final Theme x0, final Color x1) {
        return x0.caretColor = x1;
    }
    
    static /* synthetic */ Color access$600(final String x0) {
        return stringToColor(x0);
    }
    
    static /* synthetic */ Color access$702(final Theme x0, final Color x1) {
        return x0.currentLineHighlight = x1;
    }
    
    static /* synthetic */ boolean access$802(final Theme x0, final boolean x1) {
        return x0.fadeCurrentLineHighlight = x1;
    }
    
    static /* synthetic */ Color access$902(final Theme x0, final Color x1) {
        return x0.foldIndicatorFG = x1;
    }
    
    static /* synthetic */ Color access$1002(final Theme x0, final Color x1) {
        return x0.foldBG = x1;
    }
    
    static /* synthetic */ Color access$1102(final Theme x0, final Color x1) {
        return x0.gutterBorderColor = x1;
    }
    
    static /* synthetic */ Color access$1202(final Theme x0, final Color x1) {
        return x0.activeLineRangeColor = x1;
    }
    
    static /* synthetic */ boolean access$1302(final Theme x0, final boolean x1) {
        return x0.iconRowHeaderInheritsGutterBG = x1;
    }
    
    static /* synthetic */ Color access$1402(final Theme x0, final Color x1) {
        return x0.lineNumberColor = x1;
    }
    
    static /* synthetic */ String access$1502(final Theme x0, final String x1) {
        return x0.lineNumberFont = x1;
    }
    
    static /* synthetic */ int access$1602(final Theme x0, final int x1) {
        return x0.lineNumberFontSize = x1;
    }
    
    static /* synthetic */ Color access$1702(final Theme x0, final Color x1) {
        return x0.marginLineColor = x1;
    }
    
    static /* synthetic */ Color access$1802(final Theme x0, final Color x1) {
        return x0.markAllHighlightColor = x1;
    }
    
    static /* synthetic */ Color access$1902(final Theme x0, final Color x1) {
        return x0.markOccurrencesColor = x1;
    }
    
    static /* synthetic */ boolean access$2002(final Theme x0, final boolean x1) {
        return x0.markOccurrencesBorder = x1;
    }
    
    static /* synthetic */ Color access$2102(final Theme x0, final Color x1) {
        return x0.matchedBracketFG = x1;
    }
    
    static /* synthetic */ Color access$2202(final Theme x0, final Color x1) {
        return x0.matchedBracketBG = x1;
    }
    
    static /* synthetic */ boolean access$2302(final Theme x0, final boolean x1) {
        return x0.matchedBracketHighlightBoth = x1;
    }
    
    static /* synthetic */ boolean access$2402(final Theme x0, final boolean x1) {
        return x0.matchedBracketAnimate = x1;
    }
    
    static /* synthetic */ Color access$2502(final Theme x0, final Color x1) {
        return x0.hyperlinkFG = x1;
    }
    
    static /* synthetic */ Color[] access$2600(final Theme x0) {
        return x0.secondaryLanguages;
    }
    
    static /* synthetic */ boolean access$2702(final Theme x0, final boolean x1) {
        return x0.useSelctionFG = x1;
    }
    
    static /* synthetic */ Color access$2802(final Theme x0, final Color x1) {
        return x0.selectionFG = x1;
    }
    
    static /* synthetic */ Color access$2900() {
        return getDefaultSelectionFG();
    }
    
    static /* synthetic */ Color access$3002(final Theme x0, final Color x1) {
        return x0.selectionBG = x1;
    }
    
    static /* synthetic */ Color access$3100() {
        return getDefaultSelectionBG();
    }
    
    static /* synthetic */ boolean access$3202(final Theme x0, final boolean x1) {
        return x0.selectionRoundedEdges = x1;
    }
    
    static /* synthetic */ SyntaxScheme access$3302(final Theme x0, final SyntaxScheme x1) {
        return x0.scheme = x1;
    }
    
    static /* synthetic */ SyntaxScheme access$3300(final Theme x0) {
        return x0.scheme;
    }
    
    private static class XmlHandler extends DefaultHandler
    {
        private Theme theme;
        
        public void error(final SAXParseException e) throws SAXException {
            throw e;
        }
        
        public void fatalError(final SAXParseException e) throws SAXException {
            throw e;
        }
        
        public static void load(final Theme theme, final InputStream in) throws IOException {
            final SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setValidating(true);
            try {
                final SAXParser parser = spf.newSAXParser();
                final XMLReader reader = parser.getXMLReader();
                final XmlHandler handler = new XmlHandler();
                handler.theme = theme;
                reader.setEntityResolver(handler);
                reader.setContentHandler(handler);
                reader.setDTDHandler(handler);
                reader.setErrorHandler(handler);
                final InputSource is = new InputSource(in);
                is.setEncoding("UTF-8");
                reader.parse(is);
            }
            catch (Exception se) {
                se.printStackTrace();
                throw new IOException(se.toString());
            }
        }
        
        private static final int parseInt(final Attributes attrs, final String attr, final int def) {
            int value = def;
            final String temp = attrs.getValue(attr);
            if (temp != null) {
                try {
                    value = Integer.parseInt(temp);
                }
                catch (NumberFormatException nfe) {
                    nfe.printStackTrace();
                }
            }
            return value;
        }
        
        public InputSource resolveEntity(final String publicID, final String systemID) throws SAXException {
            return new InputSource(this.getClass().getResourceAsStream("/theme.dtd"));
        }
        
        public void startElement(final String uri, final String localName, final String qName, final Attributes attrs) {
            if ("background".equals(qName)) {
                final String color = attrs.getValue("color");
                if (color != null) {
                    Theme.access$002(this.theme, Theme.access$200(color, Theme.access$100()));
                }
                else {
                    final String img = attrs.getValue("image");
                    if (img != null) {
                        throw new IllegalArgumentException("Not yet implemented");
                    }
                }
            }
            else if ("baseFont".equals(qName)) {
                int size = Theme.access$300(this.theme).getSize();
                final String sizeStr = attrs.getValue("size");
                if (sizeStr != null) {
                    size = Integer.parseInt(sizeStr);
                }
                final String family = attrs.getValue("family");
                if (family != null) {
                    Theme.access$302(this.theme, Theme.access$400(family, 0, size));
                }
                else if (sizeStr != null) {
                    Theme.access$302(this.theme, Theme.access$300(this.theme).deriveFont(size * 1.0f));
                }
            }
            else if ("caret".equals(qName)) {
                final String color = attrs.getValue("color");
                Theme.access$502(this.theme, Theme.access$600(color));
            }
            else if ("currentLineHighlight".equals(qName)) {
                final String color = attrs.getValue("color");
                Theme.access$702(this.theme, Theme.access$600(color));
                final String fadeStr = attrs.getValue("fade");
                final boolean fade = Boolean.valueOf(fadeStr);
                Theme.access$802(this.theme, fade);
            }
            else if ("foldIndicator".equals(qName)) {
                String color = attrs.getValue("fg");
                Theme.access$902(this.theme, Theme.access$600(color));
                color = attrs.getValue("iconBg");
                Theme.access$1002(this.theme, Theme.access$600(color));
            }
            else if ("gutterBorder".equals(qName)) {
                final String color = attrs.getValue("color");
                Theme.access$1102(this.theme, Theme.access$600(color));
            }
            else if ("iconRowHeader".equals(qName)) {
                final String color = attrs.getValue("activeLineRange");
                Theme.access$1202(this.theme, Theme.access$600(color));
                final String inheritBGStr = attrs.getValue("inheritsGutterBG");
                Theme.access$1302(this.theme, inheritBGStr != null && Boolean.valueOf(inheritBGStr));
            }
            else if ("lineNumbers".equals(qName)) {
                final String color = attrs.getValue("fg");
                Theme.access$1402(this.theme, Theme.access$600(color));
                Theme.access$1502(this.theme, attrs.getValue("fontFamily"));
                Theme.access$1602(this.theme, parseInt(attrs, "fontSize", -1));
            }
            else if ("marginLine".equals(qName)) {
                final String color = attrs.getValue("fg");
                Theme.access$1702(this.theme, Theme.access$600(color));
            }
            else if ("markAllHighlight".equals(qName)) {
                final String color = attrs.getValue("color");
                Theme.access$1802(this.theme, Theme.access$600(color));
            }
            else if ("markOccurrencesHighlight".equals(qName)) {
                final String color = attrs.getValue("color");
                Theme.access$1902(this.theme, Theme.access$600(color));
                final String border = attrs.getValue("border");
                Theme.access$2002(this.theme, Boolean.valueOf(border));
            }
            else if ("matchedBracket".equals(qName)) {
                final String fg = attrs.getValue("fg");
                Theme.access$2102(this.theme, Theme.access$600(fg));
                final String bg = attrs.getValue("bg");
                Theme.access$2202(this.theme, Theme.access$600(bg));
                final String highlightBoth = attrs.getValue("highlightBoth");
                Theme.access$2302(this.theme, Boolean.valueOf(highlightBoth));
                final String animate = attrs.getValue("animate");
                Theme.access$2402(this.theme, Boolean.valueOf(animate));
            }
            else if ("hyperlinks".equals(qName)) {
                final String fg = attrs.getValue("fg");
                Theme.access$2502(this.theme, Theme.access$600(fg));
            }
            else if ("language".equals(qName)) {
                final String indexStr = attrs.getValue("index");
                final int index = Integer.parseInt(indexStr) - 1;
                if (Theme.access$2600(this.theme).length > index) {
                    final Color bg2 = Theme.access$600(attrs.getValue("bg"));
                    Theme.access$2600(this.theme)[index] = bg2;
                }
            }
            else if ("selection".equals(qName)) {
                final String useStr = attrs.getValue("useFG");
                Theme.access$2702(this.theme, Boolean.valueOf(useStr));
                String color2 = attrs.getValue("fg");
                Theme.access$2802(this.theme, Theme.access$200(color2, Theme.access$2900()));
                color2 = attrs.getValue("bg");
                Theme.access$3002(this.theme, Theme.access$200(color2, Theme.access$3100()));
                final String roundedStr = attrs.getValue("roundedEdges");
                Theme.access$3202(this.theme, Boolean.valueOf(roundedStr));
            }
            else if ("tokenStyles".equals(qName)) {
                Theme.access$3302(this.theme, new SyntaxScheme(Theme.access$300(this.theme), false));
            }
            else if ("style".equals(qName)) {
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
                    int index2 = 0;
                    try {
                        index2 = field.getInt(Theme.access$3300(this.theme));
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
                    final Color fg2 = Theme.access$600(fgStr);
                    Theme.access$3300(this.theme).getStyle(index2).foreground = fg2;
                    final String bgStr = attrs.getValue("bg");
                    final Color bg3 = Theme.access$600(bgStr);
                    Theme.access$3300(this.theme).getStyle(index2).background = bg3;
                    Font font = Theme.access$300(this.theme);
                    final String familyName = attrs.getValue("fontFamily");
                    if (familyName != null) {
                        font = Theme.access$400(familyName, font.getStyle(), font.getSize());
                    }
                    final String sizeStr2 = attrs.getValue("fontSize");
                    if (sizeStr2 != null) {
                        try {
                            float size2 = Float.parseFloat(sizeStr2);
                            size2 = Math.max(size2, 1.0f);
                            font = font.deriveFont(size2);
                        }
                        catch (NumberFormatException nfe) {
                            nfe.printStackTrace();
                        }
                    }
                    Theme.access$3300(this.theme).getStyle(index2).font = font;
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
                        final Font orig = Theme.access$3300(this.theme).getStyle(index2).font;
                        Theme.access$3300(this.theme).getStyle(index2).font = orig.deriveFont(style);
                    }
                    final String ulineStr = attrs.getValue("underline");
                    if (ulineStr != null) {
                        final boolean uline = Boolean.valueOf(ulineStr);
                        Theme.access$3300(this.theme).getStyle(index2).underline = uline;
                    }
                }
            }
        }
        
        public void warning(final SAXParseException e) throws SAXException {
            throw e;
        }
    }
}
