package org.fife.ui.rsyntaxtextarea.modes;

import javax.swing.text.*;
import org.fife.ui.rsyntaxtextarea.*;
import java.io.*;

public class LatexTokenMaker extends AbstractJFlexTokenMaker
{
    public static final int YYEOF = -1;
    public static final int EOL_COMMENT = 1;
    public static final int YYINITIAL = 0;
    private static final String ZZ_CMAP_PACKED = "\t\u0000\u0001\u0003\u0001\u001a\u0001\u0000\u0001\u0003\u0013\u0000\u0001\u0003\u0001\u0005\u0001\u0000\u0001\u0005\u0001\u0007\u0001\u0004\u0007\u0005\u0001\u0002\u0001\u0012\u0001\u0006\n\u0001\u0001\u0010\u0001\u0005\u0001\u0000\u0001\u0005\u0001\u0000\u0002\u0005\u001a\u0001\u0001\u0005\u0001\u0013\u0001\u0005\u0001\u0000\u0001\u0002\u0001\u0000\u0001\u0001\u0001\u0015\u0001\u0001\u0001\u0019\u0001\u000f\u0001\f\u0001\u0016\u0001\b\u0001\r\u0002\u0001\u0001\u000e\u0001\u0001\u0001\u0017\u0001\u0001\u0001\n\u0002\u0001\u0001\u000b\u0001\t\u0002\u0001\u0001\u0011\u0003\u0001\u0001\u0018\u0001\u0000\u0001\u0014\u0001\u0005\uff81\u0000";
    private static final char[] ZZ_CMAP;
    private static final int[] ZZ_ACTION;
    private static final String ZZ_ACTION_PACKED_0 = "\u0002\u0000\u0002\u0001\u0001\u0002\u0001\u0003\u0001\u0001\u0001\u0004\u0001\u0005\u0004\u0006\u0001\u0007\u0003\b\u0004\u0000\u0002\b\u0004\u0000\u0002\b\u0002\u0000\u0001\t\u0001\u0000\u0001\b\u0003\u0000\u0001\b\u0001\n\u0002\u0000\u0001\u000b";
    private static final int[] ZZ_ROWMAP;
    private static final String ZZ_ROWMAP_PACKED_0 = "\u0000\u0000\u0000\u001b\u00006\u0000Q\u00006\u00006\u0000l\u00006\u00006\u0000\u0087\u0000¢\u0000½\u0000\u00d8\u00006\u0000\u00f3\u0000\u010e\u0000\u0129\u0000\u0144\u0000\u015f\u0000\u017a\u0000\u0195\u0000\u01b0\u0000\u01cb\u0000\u01e6\u0000\u0201\u0000\u021c\u0000\u0237\u0000\u0252\u0000\u026d\u0000\u0288\u0000\u02a3\u0000\u02be\u0000\u02d9\u0000\u02f4\u0000\u030f\u0000\u02be\u0000\u032a\u0000\u0345\u00006\u0000\u0360\u0000\u037b\u00006";
    private static final int[] ZZ_TRANS;
    private static final String ZZ_TRANS_PACKED_0 = "\u0001\u0003\u0002\u0004\u0001\u0005\u0001\u0006\u0003\u0003\b\u0004\u0001\u0003\u0001\u0004\u0001\u0003\u0001\u0007\u0001\b\u0003\u0004\u0001\b\u0001\u0004\u0001\t\b\n\u0001\u000b\u0003\n\u0001\f\u0004\n\u0001\r\b\n\u0001\u000e\u001c\u0000\u0002\u0004\u0005\u0000\b\u0004\u0001\u0000\u0001\u0004\u0003\u0000\u0003\u0004\u0001\u0000\u0001\u0004\u0002\u0000\u0002\u000f\u0005\u0000\u0007\u000f\u0001\u0010\u0001\u0000\u0001\u000f\u0003\u0000\u0001\u0011\u0002\u000f\u0001\u0000\u0001\u000f\u0001\u0000\b\n\u0001\u0000\u0003\n\u0001\u0000\u0004\n\u0001\u0000\b\n\n\u0000\u0001\u0012\u001a\u0000\u0001\u0013\u0003\u0000\u0001\u0014\u001e\u0000\u0001\u0015\n\u0000\u0002\u000f\u0005\u0000\b\u000f\u0001\u0000\u0001\u000f\u0003\u0000\u0003\u000f\u0001\u0000\u0001\u000f\u0002\u0000\u0002\u000f\u0005\u0000\b\u000f\u0001\u0000\u0001\u000f\u0003\u0000\u0002\u000f\u0001\u0016\u0001\u0000\u0001\u000f\u0002\u0000\u0002\u000f\u0005\u0000\u0007\u000f\u0001\u0017\u0001\u0000\u0001\u000f\u0003\u0000\u0003\u000f\u0001\u0000\u0001\u000f\n\u0000\u0001\u0018\u001b\u0000\u0001\u0019\u001e\u0000\u0001\u001a\u001d\u0000\u0001\u001b\n\u0000\u0002\u000f\u0005\u0000\b\u000f\u0001\u0000\u0001\u000f\u0003\u0000\u0003\u000f\u0001\u0000\u0001\u001c\u0002\u0000\u0002\u000f\u0005\u0000\b\u000f\u0001\u0000\u0001\u000f\u0003\u0000\u0001\u000f\u0001\u001d\u0001\u000f\u0001\u0000\u0001\u000f\u000b\u0000\u0001\u001e \u0000\u0001\u001f\u0019\u0000\u0001\u0019\u001d\u0000\u0001 \t\u0000\u0002\u000f\u0005\u0000\b\u000f\u0001\u0000\u0001\u000f\u0003\u0000\u0003\u000f\u0001!\u0001\u000f\u0002\u0000\u0002\u000f\u0005\u0000\u0005\u000f\u0001\"\u0002\u000f\u0001\u0000\u0001\u000f\u0003\u0000\u0003\u000f\u0001\u0000\u0001\u000f\f\u0000\u0001\u0019\u0004\u0000\u0001\u001f\u0010\u0000\u0001#\u0015\u0000\u0001 \u0001$\u0001\u0000\u0002$\n \u0001$\u0001 \u0001$\u0002\u0000\u0003 \u0001\u0000\u0001 \u0002\u0000\u0002%\u0005\u0000\b%\u0001\u0000\u0001%\u0003\u0000\u0003%\u0001\u0000\u0001%\u0002\u0000\u0002\u000f\u0005\u0000\b\u000f\u0001\u0000\u0001\u000f\u0003\u0000\u0002\u000f\u0001&\u0001\u0000\u0001\u000f\u0007\u0000\u0001 \u0015\u0000\u0002%\u0005\u0000\b%\u0001\u0000\u0001%\u0002\u0000\u0001'\u0003%\u0001\u0000\u0001%\u0002\u0000\u0002\u000f\u0005\u0000\b\u000f\u0001\u0000\u0001\u000f\u0003\u0000\u0003\u000f\u0001(\u0001\u000f\u0002\u0000\u0002)\u0005\u0000\b)\u0001\u0000\u0001)\u0003\u0000\u0003)\u0001\u0000\u0001)\u0002\u0000\u0002)\u0005\u0000\b)\u0001\u0000\u0001)\u0002\u0000\u0001*\u0003)\u0001\u0000\u0001)\u0001\u0000";
    private static final int ZZ_UNKNOWN_ERROR = 0;
    private static final int ZZ_NO_MATCH = 1;
    private static final int ZZ_PUSHBACK_2BIG = 2;
    private static final String[] ZZ_ERROR_MSG;
    private static final int[] ZZ_ATTRIBUTE;
    private static final String ZZ_ATTRIBUTE_PACKED_0 = "\u0002\u0000\u0001\t\u0001\u0001\u0002\t\u0001\u0001\u0002\t\u0004\u0001\u0001\t\u0003\u0001\u0004\u0000\u0002\u0001\u0004\u0000\u0002\u0001\u0002\u0000\u0001\u0001\u0001\u0000\u0001\u0001\u0003\u0000\u0001\u0001\u0001\t\u0002\u0000\u0001\t";
    private Reader zzReader;
    private int zzState;
    private int zzLexicalState;
    private char[] zzBuffer;
    private int zzMarkedPos;
    private int zzCurrentPos;
    private int zzStartRead;
    private int zzEndRead;
    private boolean zzAtEOF;
    
    private static int[] zzUnpackAction() {
        final int[] result = new int[42];
        int offset = 0;
        offset = zzUnpackAction("\u0002\u0000\u0002\u0001\u0001\u0002\u0001\u0003\u0001\u0001\u0001\u0004\u0001\u0005\u0004\u0006\u0001\u0007\u0003\b\u0004\u0000\u0002\b\u0004\u0000\u0002\b\u0002\u0000\u0001\t\u0001\u0000\u0001\b\u0003\u0000\u0001\b\u0001\n\u0002\u0000\u0001\u000b", offset, result);
        return result;
    }
    
    private static int zzUnpackAction(final String packed, final int offset, final int[] result) {
        int i = 0;
        int j = offset;
        final int l = packed.length();
        while (i < l) {
            int count = packed.charAt(i++);
            final int value = packed.charAt(i++);
            do {
                result[j++] = value;
            } while (--count > 0);
        }
        return j;
    }
    
    private static int[] zzUnpackRowMap() {
        final int[] result = new int[42];
        int offset = 0;
        offset = zzUnpackRowMap("\u0000\u0000\u0000\u001b\u00006\u0000Q\u00006\u00006\u0000l\u00006\u00006\u0000\u0087\u0000¢\u0000½\u0000\u00d8\u00006\u0000\u00f3\u0000\u010e\u0000\u0129\u0000\u0144\u0000\u015f\u0000\u017a\u0000\u0195\u0000\u01b0\u0000\u01cb\u0000\u01e6\u0000\u0201\u0000\u021c\u0000\u0237\u0000\u0252\u0000\u026d\u0000\u0288\u0000\u02a3\u0000\u02be\u0000\u02d9\u0000\u02f4\u0000\u030f\u0000\u02be\u0000\u032a\u0000\u0345\u00006\u0000\u0360\u0000\u037b\u00006", offset, result);
        return result;
    }
    
    private static int zzUnpackRowMap(final String packed, final int offset, final int[] result) {
        int i = 0;
        int j = offset;
        int high;
        for (int l = packed.length(); i < l; high = packed.charAt(i++) << 16, result[j++] = (high | packed.charAt(i++))) {}
        return j;
    }
    
    private static int[] zzUnpackTrans() {
        final int[] result = new int[918];
        int offset = 0;
        offset = zzUnpackTrans("\u0001\u0003\u0002\u0004\u0001\u0005\u0001\u0006\u0003\u0003\b\u0004\u0001\u0003\u0001\u0004\u0001\u0003\u0001\u0007\u0001\b\u0003\u0004\u0001\b\u0001\u0004\u0001\t\b\n\u0001\u000b\u0003\n\u0001\f\u0004\n\u0001\r\b\n\u0001\u000e\u001c\u0000\u0002\u0004\u0005\u0000\b\u0004\u0001\u0000\u0001\u0004\u0003\u0000\u0003\u0004\u0001\u0000\u0001\u0004\u0002\u0000\u0002\u000f\u0005\u0000\u0007\u000f\u0001\u0010\u0001\u0000\u0001\u000f\u0003\u0000\u0001\u0011\u0002\u000f\u0001\u0000\u0001\u000f\u0001\u0000\b\n\u0001\u0000\u0003\n\u0001\u0000\u0004\n\u0001\u0000\b\n\n\u0000\u0001\u0012\u001a\u0000\u0001\u0013\u0003\u0000\u0001\u0014\u001e\u0000\u0001\u0015\n\u0000\u0002\u000f\u0005\u0000\b\u000f\u0001\u0000\u0001\u000f\u0003\u0000\u0003\u000f\u0001\u0000\u0001\u000f\u0002\u0000\u0002\u000f\u0005\u0000\b\u000f\u0001\u0000\u0001\u000f\u0003\u0000\u0002\u000f\u0001\u0016\u0001\u0000\u0001\u000f\u0002\u0000\u0002\u000f\u0005\u0000\u0007\u000f\u0001\u0017\u0001\u0000\u0001\u000f\u0003\u0000\u0003\u000f\u0001\u0000\u0001\u000f\n\u0000\u0001\u0018\u001b\u0000\u0001\u0019\u001e\u0000\u0001\u001a\u001d\u0000\u0001\u001b\n\u0000\u0002\u000f\u0005\u0000\b\u000f\u0001\u0000\u0001\u000f\u0003\u0000\u0003\u000f\u0001\u0000\u0001\u001c\u0002\u0000\u0002\u000f\u0005\u0000\b\u000f\u0001\u0000\u0001\u000f\u0003\u0000\u0001\u000f\u0001\u001d\u0001\u000f\u0001\u0000\u0001\u000f\u000b\u0000\u0001\u001e \u0000\u0001\u001f\u0019\u0000\u0001\u0019\u001d\u0000\u0001 \t\u0000\u0002\u000f\u0005\u0000\b\u000f\u0001\u0000\u0001\u000f\u0003\u0000\u0003\u000f\u0001!\u0001\u000f\u0002\u0000\u0002\u000f\u0005\u0000\u0005\u000f\u0001\"\u0002\u000f\u0001\u0000\u0001\u000f\u0003\u0000\u0003\u000f\u0001\u0000\u0001\u000f\f\u0000\u0001\u0019\u0004\u0000\u0001\u001f\u0010\u0000\u0001#\u0015\u0000\u0001 \u0001$\u0001\u0000\u0002$\n \u0001$\u0001 \u0001$\u0002\u0000\u0003 \u0001\u0000\u0001 \u0002\u0000\u0002%\u0005\u0000\b%\u0001\u0000\u0001%\u0003\u0000\u0003%\u0001\u0000\u0001%\u0002\u0000\u0002\u000f\u0005\u0000\b\u000f\u0001\u0000\u0001\u000f\u0003\u0000\u0002\u000f\u0001&\u0001\u0000\u0001\u000f\u0007\u0000\u0001 \u0015\u0000\u0002%\u0005\u0000\b%\u0001\u0000\u0001%\u0002\u0000\u0001'\u0003%\u0001\u0000\u0001%\u0002\u0000\u0002\u000f\u0005\u0000\b\u000f\u0001\u0000\u0001\u000f\u0003\u0000\u0003\u000f\u0001(\u0001\u000f\u0002\u0000\u0002)\u0005\u0000\b)\u0001\u0000\u0001)\u0003\u0000\u0003)\u0001\u0000\u0001)\u0002\u0000\u0002)\u0005\u0000\b)\u0001\u0000\u0001)\u0002\u0000\u0001*\u0003)\u0001\u0000\u0001)\u0001\u0000", offset, result);
        return result;
    }
    
    private static int zzUnpackTrans(final String packed, final int offset, final int[] result) {
        int i = 0;
        int j = offset;
        final int l = packed.length();
        while (i < l) {
            int count = packed.charAt(i++);
            int value = packed.charAt(i++);
            --value;
            do {
                result[j++] = value;
            } while (--count > 0);
        }
        return j;
    }
    
    private static int[] zzUnpackAttribute() {
        final int[] result = new int[42];
        int offset = 0;
        offset = zzUnpackAttribute("\u0002\u0000\u0001\t\u0001\u0001\u0002\t\u0001\u0001\u0002\t\u0004\u0001\u0001\t\u0003\u0001\u0004\u0000\u0002\u0001\u0004\u0000\u0002\u0001\u0002\u0000\u0001\u0001\u0001\u0000\u0001\u0001\u0003\u0000\u0001\u0001\u0001\t\u0002\u0000\u0001\t", offset, result);
        return result;
    }
    
    private static int zzUnpackAttribute(final String packed, final int offset, final int[] result) {
        int i = 0;
        int j = offset;
        final int l = packed.length();
        while (i < l) {
            int count = packed.charAt(i++);
            final int value = packed.charAt(i++);
            do {
                result[j++] = value;
            } while (--count > 0);
        }
        return j;
    }
    
    public LatexTokenMaker() {
        super();
        this.zzLexicalState = 0;
    }
    
    private void addHyperlinkToken(final int start, final int end, final int tokenType) {
        final int so = start + this.offsetShift;
        this.addToken(this.zzBuffer, start, end, tokenType, so, true);
    }
    
    private void addToken(final int tokenType) {
        this.addToken(this.zzStartRead, this.zzMarkedPos - 1, tokenType);
    }
    
    private void addToken(final int start, final int end, final int tokenType) {
        final int so = start + this.offsetShift;
        this.addToken(this.zzBuffer, start, end, tokenType, so, false);
    }
    
    public void addToken(final char[] array, final int start, final int end, final int tokenType, final int startOffset, final boolean hyperlink) {
        super.addToken(array, start, end, tokenType, startOffset, hyperlink);
        this.zzStartRead = this.zzMarkedPos;
    }
    
    public String[] getLineCommentStartAndEnd(final int languageIndex) {
        return new String[] { "%", null };
    }
    
    public Token getTokenList(final Segment text, final int initialTokenType, final int startOffset) {
        this.resetTokenList();
        this.offsetShift = -text.offset + startOffset;
        final int state = 0;
        this.s = text;
        try {
            this.yyreset(this.zzReader);
            this.yybegin(state);
            return this.yylex();
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
            return new TokenImpl();
        }
    }
    
    private boolean zzRefill() {
        return this.zzCurrentPos >= this.s.offset + this.s.count;
    }
    
    public final void yyreset(final Reader reader) {
        this.zzBuffer = this.s.array;
        this.zzStartRead = this.s.offset;
        this.zzEndRead = this.zzStartRead + this.s.count - 1;
        final int loc_0 = this.s.offset;
        this.zzMarkedPos = loc_0;
        this.zzCurrentPos = loc_0;
        this.zzLexicalState = 0;
        this.zzReader = reader;
        this.zzAtEOF = false;
    }
    
    public LatexTokenMaker(final Reader in) {
        super();
        this.zzLexicalState = 0;
        this.zzReader = in;
    }
    
    public LatexTokenMaker(final InputStream in) {
        this(new InputStreamReader(in));
    }
    
    private static char[] zzUnpackCMap(final String packed) {
        final char[] map = new char[65536];
        int i = 0;
        int j = 0;
        while (i < 112) {
            int count = packed.charAt(i++);
            final char value = packed.charAt(i++);
            do {
                map[j++] = value;
            } while (--count > 0);
        }
        return map;
    }
    
    public final void yyclose() throws IOException {
        this.zzAtEOF = true;
        this.zzEndRead = this.zzStartRead;
        if (this.zzReader != null) {
            this.zzReader.close();
        }
    }
    
    public final int yystate() {
        return this.zzLexicalState;
    }
    
    public final void yybegin(final int newState) {
        this.zzLexicalState = newState;
    }
    
    public final String yytext() {
        return new String(this.zzBuffer, this.zzStartRead, this.zzMarkedPos - this.zzStartRead);
    }
    
    public final char yycharat(final int pos) {
        return this.zzBuffer[this.zzStartRead + pos];
    }
    
    public final int yylength() {
        return this.zzMarkedPos - this.zzStartRead;
    }
    
    private void zzScanError(final int errorCode) {
        String message;
        try {
            message = LatexTokenMaker.ZZ_ERROR_MSG[errorCode];
        }
        catch (ArrayIndexOutOfBoundsException e) {
            message = LatexTokenMaker.ZZ_ERROR_MSG[0];
        }
        throw new Error(message);
    }
    
    public void yypushback(final int number) {
        if (number > this.yylength()) {
            this.zzScanError(2);
        }
        this.zzMarkedPos -= number;
    }
    
    public Token yylex() throws IOException {
        int zzEndReadL = this.zzEndRead;
        char[] zzBufferL = this.zzBuffer;
        final char[] zzCMapL = LatexTokenMaker.ZZ_CMAP;
        final int[] zzTransL = LatexTokenMaker.ZZ_TRANS;
        final int[] zzRowMapL = LatexTokenMaker.ZZ_ROWMAP;
        final int[] zzAttrL = LatexTokenMaker.ZZ_ATTRIBUTE;
        while (true) {
            int zzMarkedPosL = this.zzMarkedPos;
            int zzAction = -1;
            final int loc_0 = zzMarkedPosL;
            this.zzStartRead = loc_0;
            this.zzCurrentPos = loc_0;
            int zzCurrentPosL = loc_0;
            this.zzState = this.zzLexicalState;
            int zzInput;
            while (true) {
                if (zzCurrentPosL < zzEndReadL) {
                    zzInput = zzBufferL[zzCurrentPosL++];
                }
                else {
                    if (this.zzAtEOF) {
                        zzInput = -1;
                        break;
                    }
                    this.zzCurrentPos = zzCurrentPosL;
                    this.zzMarkedPos = zzMarkedPosL;
                    final boolean eof = this.zzRefill();
                    zzCurrentPosL = this.zzCurrentPos;
                    zzMarkedPosL = this.zzMarkedPos;
                    zzBufferL = this.zzBuffer;
                    zzEndReadL = this.zzEndRead;
                    if (eof) {
                        zzInput = -1;
                        break;
                    }
                    zzInput = zzBufferL[zzCurrentPosL++];
                }
                final int zzNext = zzTransL[zzRowMapL[this.zzState] + zzCMapL[zzInput]];
                if (zzNext == -1) {
                    break;
                }
                this.zzState = zzNext;
                final int zzAttributes = zzAttrL[this.zzState];
                if ((zzAttributes & 0x1) != 0x1) {
                    continue;
                }
                zzAction = this.zzState;
                zzMarkedPosL = zzCurrentPosL;
                if ((zzAttributes & 0x8) == 0x8) {
                    break;
                }
            }
            this.zzMarkedPos = zzMarkedPosL;
            switch ((zzAction < 0) ? zzAction : LatexTokenMaker.ZZ_ACTION[zzAction]) {
                case 1: {
                    this.addToken(20);
                }
                case 12: {
                    continue;
                }
                case 8: {
                    this.addToken(8);
                }
                case 13: {
                    continue;
                }
                case 2: {
                    this.addToken(21);
                }
                case 14: {
                    continue;
                }
                case 9: {
                    final int temp = this.zzStartRead;
                    this.addToken(this.start, this.zzStartRead - 1, 1);
                    this.addHyperlinkToken(temp, this.zzMarkedPos - 1, 1);
                    this.start = this.zzMarkedPos;
                }
                case 15: {
                    continue;
                }
                case 3: {
                    this.start = this.zzMarkedPos - 1;
                    this.yybegin(1);
                }
                case 16: {
                    continue;
                }
                case 5: {
                    this.addNullToken();
                    return this.firstToken;
                }
                case 17: {
                    continue;
                }
                case 7: {
                    this.addToken(this.start, this.zzStartRead - 1, 1);
                    this.addNullToken();
                    return this.firstToken;
                }
                case 18: {
                    continue;
                }
                case 10: {
                    final int temp = this.zzStartRead;
                    this.addToken(temp, temp + 3, 6);
                    this.addToken(temp + 4, temp + 4, 22);
                    this.addToken(temp + 5, this.zzMarkedPos - 2, 6);
                    this.addToken(this.zzMarkedPos - 1, this.zzMarkedPos - 1, 22);
                }
                case 19: {
                    continue;
                }
                case 11: {
                    final int temp = this.zzStartRead;
                    this.addToken(temp, temp + 5, 6);
                    this.addToken(temp + 6, temp + 6, 22);
                    this.addToken(temp + 7, this.zzMarkedPos - 2, 6);
                    this.addToken(this.zzMarkedPos - 1, this.zzMarkedPos - 1, 22);
                }
                case 20: {
                    continue;
                }
                case 6:
                case 21: {
                    continue;
                }
                case 4: {
                    this.addToken(22);
                }
                case 22: {
                    continue;
                }
                default: {
                    if (zzInput != -1 || this.zzStartRead != this.zzCurrentPos) {
                        this.zzScanError(1);
                        continue;
                    }
                    this.zzAtEOF = true;
                    switch (this.zzLexicalState) {
                        case 1: {
                            this.addToken(this.start, this.zzStartRead - 1, 1);
                            this.addNullToken();
                            return this.firstToken;
                        }
                        case 43: {
                            continue;
                        }
                        case 0: {
                            this.addNullToken();
                            return this.firstToken;
                        }
                        case 44: {
                            continue;
                        }
                        default: {
                            return null;
                        }
                    }
                    break;
                }
            }
        }
    }
    
    static {
        ZZ_CMAP = zzUnpackCMap("\t\u0000\u0001\u0003\u0001\u001a\u0001\u0000\u0001\u0003\u0013\u0000\u0001\u0003\u0001\u0005\u0001\u0000\u0001\u0005\u0001\u0007\u0001\u0004\u0007\u0005\u0001\u0002\u0001\u0012\u0001\u0006\n\u0001\u0001\u0010\u0001\u0005\u0001\u0000\u0001\u0005\u0001\u0000\u0002\u0005\u001a\u0001\u0001\u0005\u0001\u0013\u0001\u0005\u0001\u0000\u0001\u0002\u0001\u0000\u0001\u0001\u0001\u0015\u0001\u0001\u0001\u0019\u0001\u000f\u0001\f\u0001\u0016\u0001\b\u0001\r\u0002\u0001\u0001\u000e\u0001\u0001\u0001\u0017\u0001\u0001\u0001\n\u0002\u0001\u0001\u000b\u0001\t\u0002\u0001\u0001\u0011\u0003\u0001\u0001\u0018\u0001\u0000\u0001\u0014\u0001\u0005\uff81\u0000");
        ZZ_ACTION = zzUnpackAction();
        ZZ_ROWMAP = zzUnpackRowMap();
        ZZ_TRANS = zzUnpackTrans();
        ZZ_ERROR_MSG = new String[] { "Unkown internal scanner error", "Error: could not match input", "Error: pushback value was too large" };
        ZZ_ATTRIBUTE = zzUnpackAttribute();
    }
}
