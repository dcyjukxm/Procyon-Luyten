package com.strobel.decompiler;

import com.strobel.decompiler.languages.*;
import java.io.*;
import java.util.*;

public class LineNumberFormatter
{
    private final List<LineNumberPosition> _positions;
    private final File _file;
    private final EnumSet<LineNumberOption> _options;
    
    public LineNumberFormatter(final File file, final List<LineNumberPosition> lineNumberPositions, final EnumSet<LineNumberOption> options) {
        super();
        this._file = file;
        this._positions = lineNumberPositions;
        this._options = ((options == null) ? EnumSet.noneOf(LineNumberOption.class) : options);
    }
    
    public void reformatFile() throws IOException {
        final List<LineNumberPosition> lineBrokenPositions = new ArrayList<LineNumberPosition>();
        final List<String> brokenLines = this.breakLines(lineBrokenPositions);
        this.emitFormatted(brokenLines, lineBrokenPositions);
    }
    
    private List<String> breakLines(final List<LineNumberPosition> o_LineBrokenPositions) throws IOException {
        int numLinesRead = 0;
        int lineOffset = 0;
        final List<String> brokenLines = new ArrayList<String>();
        Throwable loc_0 = null;
        try {
            final BufferedReader r = new BufferedReader(new FileReader(this._file));
            try {
                for (int posIndex = 0; posIndex < this._positions.size(); ++posIndex) {
                    final LineNumberPosition pos = this._positions.get(posIndex);
                    o_LineBrokenPositions.add(new LineNumberPosition(pos.getOriginalLine(), pos.getEmittedLine() + lineOffset, pos.getEmittedColumn()));
                    while (numLinesRead < pos.getEmittedLine() - 1) {
                        brokenLines.add(r.readLine());
                        ++numLinesRead;
                    }
                    String line = r.readLine();
                    ++numLinesRead;
                    int prevPartLen = 0;
                    char[] indent = new char[0];
                    LineNumberPosition nextPos;
                    do {
                        nextPos = ((posIndex < this._positions.size() - 1) ? this._positions.get(posIndex + 1) : null);
                        if (nextPos != null && nextPos.getEmittedLine() == pos.getEmittedLine() && nextPos.getOriginalLine() > pos.getOriginalLine()) {
                            ++posIndex;
                            ++lineOffset;
                            final String firstPart = line.substring(0, nextPos.getEmittedColumn() - prevPartLen - 1);
                            brokenLines.add(String.valueOf(new String(indent)) + firstPart);
                            prevPartLen += firstPart.length();
                            indent = new char[prevPartLen];
                            Arrays.fill(indent, ' ');
                            line = line.substring(firstPart.length(), line.length());
                            o_LineBrokenPositions.add(new LineNumberPosition(nextPos.getOriginalLine(), nextPos.getEmittedLine() + lineOffset, nextPos.getEmittedColumn()));
                        }
                        else {
                            nextPos = null;
                        }
                    } while (nextPos != null);
                    brokenLines.add(String.valueOf(new String(indent)) + line);
                }
                String line2;
                while ((line2 = r.readLine()) != null) {
                    brokenLines.add(line2);
                }
            }
            finally {
                if (r != null) {
                    r.close();
                }
            }
        }
        finally {
            if (loc_0 == null) {
                final Throwable loc_1;
                loc_0 = loc_1;
            }
            else {
                final Throwable loc_1;
                if (loc_0 != loc_1) {
                    loc_0.addSuppressed(loc_1);
                }
            }
        }
        return brokenLines;
    }
    
    private void emitFormatted(final List<String> brokenLines, final List<LineNumberPosition> lineBrokenPositions) throws IOException {
        final File tempFile = new File(String.valueOf(this._file.getAbsolutePath()) + ".fixed");
        int globalOffset = 0;
        int numLinesRead = 0;
        final Iterator<String> lines = brokenLines.iterator();
        final int maxLineNo = LineNumberPosition.computeMaxLineNumber(lineBrokenPositions);
        Throwable loc_0 = null;
        try {
            final LineNumberPrintWriter w = new LineNumberPrintWriter(maxLineNo, new BufferedWriter(new FileWriter(tempFile)));
            try {
                if (!this._options.contains(LineNumberOption.LEADING_COMMENTS)) {
                    w.suppressLineNumbers();
                }
                final boolean doStretching = this._options.contains(LineNumberOption.STRETCHED);
                for (final LineNumberPosition pos : lineBrokenPositions) {
                    final int nextTarget = pos.getOriginalLine();
                    final int nextActual = pos.getEmittedLine();
                    int requiredAdjustment = nextTarget - nextActual - globalOffset;
                    if (doStretching && requiredAdjustment < 0) {
                        final List<String> stripped = new ArrayList<String>();
                        while (numLinesRead < nextActual - 1) {
                            final String line = lines.next();
                            ++numLinesRead;
                            if (requiredAdjustment < 0 && line.trim().isEmpty()) {
                                ++requiredAdjustment;
                                --globalOffset;
                            }
                            else {
                                stripped.add(line);
                            }
                        }
                        final int lineNoToPrint = (stripped.size() + requiredAdjustment <= 0) ? nextTarget : -1;
                        for (final String line2 : stripped) {
                            if (requiredAdjustment < 0) {
                                w.print(lineNoToPrint, line2);
                                w.print("  ");
                                ++requiredAdjustment;
                                --globalOffset;
                            }
                            else {
                                w.println(lineNoToPrint, line2);
                            }
                        }
                        String line2 = lines.next();
                        ++numLinesRead;
                        if (requiredAdjustment < 0) {
                            w.print(nextTarget, line2);
                            w.print("  ");
                            --globalOffset;
                        }
                        else {
                            w.println(nextTarget, line2);
                        }
                    }
                    else {
                        while (numLinesRead < nextActual) {
                            final String line3 = lines.next();
                            final boolean isLast = ++numLinesRead >= nextActual;
                            final int lineNoToPrint2 = isLast ? nextTarget : -1;
                            if (requiredAdjustment > 0 && doStretching) {
                                do {
                                    w.println("");
                                    --requiredAdjustment;
                                    ++globalOffset;
                                } while (isLast && requiredAdjustment > 0);
                                w.println(lineNoToPrint2, line3);
                            }
                            else {
                                w.println(lineNoToPrint2, line3);
                            }
                        }
                    }
                }
                while (lines.hasNext()) {
                    final String line4 = lines.next();
                    w.println(line4);
                }
            }
            finally {
                if (w != null) {
                    w.close();
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
        this._file.delete();
        tempFile.renameTo(this._file);
    }
    
    public enum LineNumberOption
    {
        LEADING_COMMENTS("LEADING_COMMENTS", 0), 
        STRETCHED("STRETCHED", 1);
    }
}
