package org.fife.print;

import java.awt.print.*;
import java.awt.*;
import javax.swing.text.*;

public abstract class RPrintUtilities
{
    private static int currentDocLineNumber;
    private static int numDocLines;
    private static Element rootElement;
    private static final char[] breakChars;
    private static int xOffset;
    private static int tabSizeInSpaces;
    private static FontMetrics fm;
    
    private static int getLineBreakPoint(final String line, final int maxCharsPerLine) {
        int breakPoint = -1;
        for (int i = 0; i < RPrintUtilities.breakChars.length; ++i) {
            final int breakCharPos = line.lastIndexOf(RPrintUtilities.breakChars[i], maxCharsPerLine - 1);
            if (breakCharPos > breakPoint) {
                breakPoint = breakCharPos;
            }
        }
        return (breakPoint == -1) ? (maxCharsPerLine - 1) : breakPoint;
    }
    
    public static int printDocumentMonospaced(final Graphics g, final Document doc, final int fontSize, final int pageIndex, final PageFormat pageFormat, final int tabSize) {
        g.setColor(Color.BLACK);
        g.setFont(new Font("Monospaced", 0, fontSize));
        RPrintUtilities.tabSizeInSpaces = tabSize;
        RPrintUtilities.fm = g.getFontMetrics();
        final int fontWidth = RPrintUtilities.fm.charWidth('w');
        final int fontHeight = RPrintUtilities.fm.getHeight();
        final int MAX_CHARS_PER_LINE = (int)pageFormat.getImageableWidth() / fontWidth;
        final int MAX_LINES_PER_PAGE = (int)pageFormat.getImageableHeight() / fontHeight;
        final int STARTING_LINE_NUMBER = MAX_LINES_PER_PAGE * pageIndex;
        RPrintUtilities.xOffset = (int)pageFormat.getImageableX();
        int y = (int)pageFormat.getImageableY() + RPrintUtilities.fm.getAscent() + 1;
        int numPrintedLines = 0;
        RPrintUtilities.currentDocLineNumber = 0;
        RPrintUtilities.rootElement = doc.getDefaultRootElement();
        RPrintUtilities.numDocLines = RPrintUtilities.rootElement.getElementCount();
        while (RPrintUtilities.currentDocLineNumber < RPrintUtilities.numDocLines) {
            final Element currentLine = RPrintUtilities.rootElement.getElement(RPrintUtilities.currentDocLineNumber);
            final int startOffs = currentLine.getStartOffset();
            String curLineString;
            try {
                curLineString = doc.getText(startOffs, currentLine.getEndOffset() - startOffs);
            }
            catch (BadLocationException ble) {
                ble.printStackTrace();
                return 1;
            }
            curLineString = curLineString.replaceAll("\n", "");
            if (RPrintUtilities.tabSizeInSpaces == 0) {
                curLineString = curLineString.replaceAll("\t", "");
            }
            else {
                for (int tabIndex = curLineString.indexOf(9); tabIndex > -1; tabIndex = curLineString.indexOf(9)) {
                    final int spacesNeeded = RPrintUtilities.tabSizeInSpaces - tabIndex % RPrintUtilities.tabSizeInSpaces;
                    String replacementString = "";
                    for (int i = 0; i < spacesNeeded; ++i) {
                        replacementString += ' ';
                    }
                    curLineString = curLineString.replaceFirst("\t", replacementString);
                }
            }
            while (curLineString.length() > MAX_CHARS_PER_LINE) {
                if (++numPrintedLines > STARTING_LINE_NUMBER) {
                    g.drawString(curLineString.substring(0, MAX_CHARS_PER_LINE), RPrintUtilities.xOffset, y);
                    y += fontHeight;
                    if (numPrintedLines == STARTING_LINE_NUMBER + MAX_LINES_PER_PAGE) {
                        return 0;
                    }
                }
                curLineString = curLineString.substring(MAX_CHARS_PER_LINE, curLineString.length());
            }
            ++RPrintUtilities.currentDocLineNumber;
            if (++numPrintedLines > STARTING_LINE_NUMBER) {
                g.drawString(curLineString, RPrintUtilities.xOffset, y);
                y += fontHeight;
                if (numPrintedLines == STARTING_LINE_NUMBER + MAX_LINES_PER_PAGE) {
                    return 0;
                }
                continue;
            }
        }
        if (numPrintedLines > STARTING_LINE_NUMBER) {
            return 0;
        }
        return 1;
    }
    
    public static int printDocumentMonospacedWordWrap(final Graphics g, final Document doc, final int fontSize, final int pageIndex, final PageFormat pageFormat, final int tabSize) {
        g.setColor(Color.BLACK);
        g.setFont(new Font("Monospaced", 0, fontSize));
        RPrintUtilities.tabSizeInSpaces = tabSize;
        RPrintUtilities.fm = g.getFontMetrics();
        final int fontWidth = RPrintUtilities.fm.charWidth('w');
        final int fontHeight = RPrintUtilities.fm.getHeight();
        final int MAX_CHARS_PER_LINE = (int)pageFormat.getImageableWidth() / fontWidth;
        final int MAX_LINES_PER_PAGE = (int)pageFormat.getImageableHeight() / fontHeight;
        final int STARTING_LINE_NUMBER = MAX_LINES_PER_PAGE * pageIndex;
        RPrintUtilities.xOffset = (int)pageFormat.getImageableX();
        int y = (int)pageFormat.getImageableY() + RPrintUtilities.fm.getAscent() + 1;
        int numPrintedLines = 0;
        RPrintUtilities.currentDocLineNumber = 0;
        RPrintUtilities.rootElement = doc.getDefaultRootElement();
        RPrintUtilities.numDocLines = RPrintUtilities.rootElement.getElementCount();
        while (RPrintUtilities.currentDocLineNumber < RPrintUtilities.numDocLines) {
            final Element currentLine = RPrintUtilities.rootElement.getElement(RPrintUtilities.currentDocLineNumber);
            final int startOffs = currentLine.getStartOffset();
            String curLineString;
            try {
                curLineString = doc.getText(startOffs, currentLine.getEndOffset() - startOffs);
            }
            catch (BadLocationException ble) {
                ble.printStackTrace();
                return 1;
            }
            curLineString = curLineString.replaceAll("\n", "");
            if (RPrintUtilities.tabSizeInSpaces == 0) {
                curLineString = curLineString.replaceAll("\t", "");
            }
            else {
                for (int tabIndex = curLineString.indexOf(9); tabIndex > -1; tabIndex = curLineString.indexOf(9)) {
                    final int spacesNeeded = RPrintUtilities.tabSizeInSpaces - tabIndex % RPrintUtilities.tabSizeInSpaces;
                    String replacementString = "";
                    for (int i = 0; i < spacesNeeded; ++i) {
                        replacementString += ' ';
                    }
                    curLineString = curLineString.replaceFirst("\t", replacementString);
                }
            }
            while (curLineString.length() > MAX_CHARS_PER_LINE) {
                final int breakPoint = getLineBreakPoint(curLineString, MAX_CHARS_PER_LINE) + 1;
                if (++numPrintedLines > STARTING_LINE_NUMBER) {
                    g.drawString(curLineString.substring(0, breakPoint), RPrintUtilities.xOffset, y);
                    y += fontHeight;
                    if (numPrintedLines == STARTING_LINE_NUMBER + MAX_LINES_PER_PAGE) {
                        return 0;
                    }
                }
                curLineString = curLineString.substring(breakPoint, curLineString.length());
            }
            ++RPrintUtilities.currentDocLineNumber;
            if (++numPrintedLines > STARTING_LINE_NUMBER) {
                g.drawString(curLineString, RPrintUtilities.xOffset, y);
                y += fontHeight;
                if (numPrintedLines == STARTING_LINE_NUMBER + MAX_LINES_PER_PAGE) {
                    return 0;
                }
                continue;
            }
        }
        if (numPrintedLines > STARTING_LINE_NUMBER) {
            return 0;
        }
        return 1;
    }
    
    public static int printDocumentWordWrap(final Graphics g, final JTextComponent textComponent, final Font font, final int pageIndex, final PageFormat pageFormat, final int tabSize) {
        g.setColor(Color.BLACK);
        g.setFont((font != null) ? font : textComponent.getFont());
        RPrintUtilities.tabSizeInSpaces = tabSize;
        RPrintUtilities.fm = g.getFontMetrics();
        final int fontHeight = RPrintUtilities.fm.getHeight();
        final int LINE_LENGTH_IN_PIXELS = (int)pageFormat.getImageableWidth();
        final int MAX_LINES_PER_PAGE = (int)pageFormat.getImageableHeight() / fontHeight;
        final int STARTING_LINE_NUMBER = MAX_LINES_PER_PAGE * pageIndex;
        final RPrintTabExpander tabExpander = new RPrintTabExpander();
        RPrintUtilities.xOffset = (int)pageFormat.getImageableX();
        int y = (int)pageFormat.getImageableY() + RPrintUtilities.fm.getAscent() + 1;
        int numPrintedLines = 0;
        final Document doc = textComponent.getDocument();
        RPrintUtilities.rootElement = doc.getDefaultRootElement();
        RPrintUtilities.numDocLines = RPrintUtilities.rootElement.getElementCount();
        RPrintUtilities.currentDocLineNumber = 0;
        int startingOffset = 0;
        while (RPrintUtilities.currentDocLineNumber < RPrintUtilities.numDocLines) {
            Segment currentLineSeg = new Segment();
            final Element currentLine = RPrintUtilities.rootElement.getElement(RPrintUtilities.currentDocLineNumber);
            final int currentLineStart = currentLine.getStartOffset();
            final int currentLineEnd = currentLine.getEndOffset();
            try {
                doc.getText(currentLineStart + startingOffset, currentLineEnd - (currentLineStart + startingOffset), currentLineSeg);
            }
            catch (BadLocationException ble) {
                System.err.println("BadLocationException in print (where there shouldn't be one!): " + ble);
                return 1;
            }
            currentLineSeg = removeEndingWhitespace(currentLineSeg);
            int currentLineLengthInPixels = Utilities.getTabbedTextWidth(currentLineSeg, RPrintUtilities.fm, 0, tabExpander, 0);
            if (currentLineLengthInPixels <= LINE_LENGTH_IN_PIXELS) {
                ++RPrintUtilities.currentDocLineNumber;
                startingOffset = 0;
            }
            else {
                int currentPos = -1;
                while (currentLineLengthInPixels > LINE_LENGTH_IN_PIXELS) {
                    currentLineSeg = removeEndingWhitespace(currentLineSeg);
                    currentPos = -1;
                    final String currentLineString = currentLineSeg.toString();
                    for (int i = 0; i < RPrintUtilities.breakChars.length; ++i) {
                        final int pos = currentLineString.lastIndexOf(RPrintUtilities.breakChars[i]) + 1;
                        if (pos > 0 && (pos > currentPos & pos != currentLineString.length())) {
                            currentPos = pos;
                        }
                    }
                    if (currentPos == -1) {
                        currentPos = 0;
                        do {
                            ++currentPos;
                            try {
                                doc.getText(currentLineStart + startingOffset, currentPos, currentLineSeg);
                            }
                            catch (BadLocationException ble2) {
                                System.err.println(ble2);
                                return 1;
                            }
                            currentLineLengthInPixels = Utilities.getTabbedTextWidth(currentLineSeg, RPrintUtilities.fm, 0, tabExpander, 0);
                        } while (currentLineLengthInPixels <= LINE_LENGTH_IN_PIXELS);
                        --currentPos;
                    }
                    try {
                        doc.getText(currentLineStart + startingOffset, currentPos, currentLineSeg);
                    }
                    catch (BadLocationException ble2) {
                        System.err.println("BadLocationException in print (a):");
                        System.err.println("==> currentLineStart: " + currentLineStart + "; startingOffset: " + startingOffset + "; currentPos: " + currentPos);
                        System.err.println("==> Range: " + (currentLineStart + startingOffset) + " - " + (currentLineStart + startingOffset + currentPos));
                        ble2.printStackTrace();
                        return 1;
                    }
                    currentLineLengthInPixels = Utilities.getTabbedTextWidth(currentLineSeg, RPrintUtilities.fm, 0, tabExpander, 0);
                }
                startingOffset += currentPos;
            }
            if (++numPrintedLines > STARTING_LINE_NUMBER) {
                Utilities.drawTabbedText(currentLineSeg, RPrintUtilities.xOffset, y, g, tabExpander, 0);
                y += fontHeight;
                if (numPrintedLines == STARTING_LINE_NUMBER + MAX_LINES_PER_PAGE) {
                    return 0;
                }
                continue;
            }
        }
        if (numPrintedLines > STARTING_LINE_NUMBER) {
            return 0;
        }
        return 1;
    }
    
    private static Segment removeEndingWhitespace(final Segment segment) {
        int toTrim = 0;
        for (char currentChar = segment.setIndex(segment.getEndIndex() - 1); (currentChar == ' ' || currentChar == '\t') && currentChar != '\uffff'; currentChar = segment.previous()) {
            ++toTrim;
        }
        final String stringVal = segment.toString();
        final String newStringVal = stringVal.substring(0, stringVal.length() - toTrim);
        return new Segment(newStringVal.toCharArray(), 0, newStringVal.length());
    }
    
    static /* synthetic */ int access$000() {
        return RPrintUtilities.tabSizeInSpaces;
    }
    
    static /* synthetic */ FontMetrics access$100() {
        return RPrintUtilities.fm;
    }
    
    static /* synthetic */ int access$200() {
        return RPrintUtilities.xOffset;
    }
    
    static {
        breakChars = new char[] { ' ', '\t', ',', '.', ';', '?', '!' };
    }
    
    private static class RPrintTabExpander implements TabExpander
    {
        public float nextTabStop(final float x, final int tabOffset) {
            if (RPrintUtilities.access$000() == 0) {
                return x;
            }
            final int tabSizeInPixels = RPrintUtilities.access$000() * RPrintUtilities.access$100().charWidth(' ');
            final int ntabs = ((int)x - RPrintUtilities.access$200()) / tabSizeInPixels;
            return RPrintUtilities.access$200() + (ntabs + 1) * tabSizeInPixels;
        }
    }
}
