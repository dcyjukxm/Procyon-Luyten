package org.fife.ui.rsyntaxtextarea.modes;

import javax.swing.text.*;
import org.fife.ui.rsyntaxtextarea.*;
import java.io.*;

public class BBCodeTokenMaker extends AbstractMarkupTokenMaker
{
    public static final int YYEOF = -1;
    public static final int INTAG = 1;
    public static final int YYINITIAL = 0;
    private static final String ZZ_CMAP_PACKED = "\t\u0000\u0001\u0001\u0001\u0002\u0001\u0000\u0001\u0001\u0013\u0000\u0001\u0001\u000e\u0000\u0001\u0017\r\u0000\u0001\u0018\u001d\u0000\u0001\u0003\u0001\u0000\u0001\u0004\u0004\u0000\u0001\u0005\u0001\u000b\u0001\u0016\u0001\n\u0001\u0000\u0001\u0013\u0001\u0000\u0001\u0006\u0002\u0000\u0001\r\u0001\u0012\u0001\u000f\u0001\f\u0001\u0000\u0001\u0011\u0001\u000e\u0001\b\u0001\u0010\u0001\u0007\u0001\u0015\u0002\u0000\u0001\u0014\u0001\t\uff85\u0000";
    private static final char[] ZZ_CMAP;
    private static final int[] ZZ_ACTION;
    private static final String ZZ_ACTION_PACKED_0 = "\u0002\u0000\u0001\u0001\u0001\u0002\u0001\u0003\u0001\u0004\u0001\u0005\u0001\u0006\u0001\u0007\u0004\b\u0006\u0005\u0001\t\u0001\n\u0001\u0004\u0013\u0005";
    private static final int[] ZZ_ROWMAP;
    private static final String ZZ_ROWMAP_PACKED_0 = "\u0000\u0000\u0000\u0019\u00002\u0000K\u0000d\u0000}\u0000\u0096\u0000d\u0000d\u0000\u0096\u0000¯\u0000\u00c8\u0000\u00e1\u0000\u00fa\u0000\u0113\u0000\u012c\u0000\u0145\u0000\u015e\u0000\u0177\u0000\u0190\u0000d\u0000d\u0000\u01a9\u0000\u01c2\u0000\u01db\u0000\u01f4\u0000\u020d\u0000\u0226\u0000\u023f\u0000\u0258\u0000\u0271\u0000\u028a\u0000\u02a3\u0000\u02bc\u0000\u02d5\u0000\u02ee\u0000\u0307\u0000\u0320\u0000\u0339\u0000\u0352\u0000\u036b";
    private static final int[] ZZ_TRANS;
    private static final String ZZ_TRANS_PACKED_0 = "\u0001\u0003\u0001\u0004\u0001\u0005\u0001\u0006\u0015\u0003\u0001\u0007\u0001\u0004\u0001\u0000\u0001\b\u0001\t\u0001\n\u0001\u000b\u0001\f\u0001\r\u0002\u0007\u0001\u000e\u0001\u000f\u0001\u0010\u0003\u0007\u0001\u0011\u0001\u0007\u0001\u0012\u0001\u0013\u0002\u0007\u0001\u0014\u0001\u0015\u0001\u0003\u0003\u0000\u0015\u0003\u0001\u0000\u0001\u0004G\u0000\u0001\u0016\u0001\u0000\u0001\u0007\u0004\u0000\u0012\u0007\u0002\u0000\u0001\u0007\u0004\u0000\r\u0007\u0001\u0017\u0004\u0007\u0002\u0000\u0001\u0007\u0004\u0000\b\u0007\u0001\n\u0001\u000f\b\u0007\u0002\u0000\u0001\u0007\u0004\u0000\u0001\u0007\u0001\u0018\u0010\u0007\u0002\u0000\u0001\u0007\u0004\u0000\u0005\u0007\u0001\u0019\u0001\u0007\u0001\u001a\n\u0007\u0002\u0000\u0001\u0007\u0004\u0000\b\u0007\u0001\n\t\u0007\u0002\u0000\u0001\u0007\u0004\u0000\u0001\u0007\u0001\n\u0010\u0007\u0002\u0000\u0001\u0007\u0004\u0000\u0002\u0007\u0001\u001b\u000f\u0007\u0002\u0000\u0001\u0007\u0004\u0000\u0010\u0007\u0001\u001c\u0001\u0007\u0002\u0000\u0001\u0007\u0004\u0000\u0007\u0007\u0001\u001d\n\u0007\u0006\u0000\u0001\t\u0014\u0000\u0001\u0007\u0004\u0000\u000e\u0007\u0001\n\u0003\u0007\u0002\u0000\u0001\u0007\u0004\u0000\u0004\u0007\u0001\u001e\r\u0007\u0002\u0000\u0001\u0007\u0004\u0000\n\u0007\u0001\u001f\u0007\u0007\u0002\u0000\u0001\u0007\u0004\u0000\b\u0007\u0001 \t\u0007\u0002\u0000\u0001\u0007\u0004\u0000\u0007\u0007\u0001!\n\u0007\u0002\u0000\u0001\u0007\u0004\u0000\u0001\u0007\u0001\"\u0010\u0007\u0002\u0000\u0001\u0007\u0004\u0000\u0002\u0007\u0001#\u000f\u0007\u0002\u0000\u0001\u0007\u0004\u0000\u0005\u0007\u0001\n\f\u0007\u0002\u0000\u0001\u0007\u0004\u0000\u000b\u0007\u0001$\u0006\u0007\u0002\u0000\u0001\u0007\u0004\u0000\u0007\u0007\u0001%\n\u0007\u0002\u0000\u0001\u0007\u0004\u0000\u000b\u0007\u0001\u001e\u0006\u0007\u0002\u0000\u0001\u0007\u0004\u0000\u0011\u0007\u0001&\u0002\u0000\u0001\u0007\u0004\u0000\u000b\u0007\u0001'\u0006\u0007\u0002\u0000\u0001\u0007\u0004\u0000\u0005\u0007\u0001%\f\u0007\u0002\u0000\u0001\u0007\u0004\u0000\t\u0007\u0001\n\b\u0007\u0002\u0000\u0001\u0007\u0004\u0000\u0005\u0007\u0001(\f\u0007\u0002\u0000\u0001\u0007\u0004\u0000\u0002\u0007\u0001)\u000f\u0007\u0002\u0000\u0001\u0007\u0004\u0000\u0007\u0007\u0001\n\n\u0007\u0002\u0000\u0001\u0007\u0004\u0000\u0001\u001e\u0011\u0007\u0002\u0000";
    private static final int ZZ_UNKNOWN_ERROR = 0;
    private static final int ZZ_NO_MATCH = 1;
    private static final int ZZ_PUSHBACK_2BIG = 2;
    private static final String[] ZZ_ERROR_MSG;
    private static final int[] ZZ_ATTRIBUTE;
    private static final String ZZ_ATTRIBUTE_PACKED_0 = "\u0002\u0000\u0002\u0001\u0001\t\u0002\u0001\u0002\t\u000b\u0001\u0002\t\u0013\u0001";
    private Reader zzReader;
    private int zzState;
    private int zzLexicalState;
    private char[] zzBuffer;
    private int zzMarkedPos;
    private int zzCurrentPos;
    private int zzStartRead;
    private int zzEndRead;
    private boolean zzAtEOF;
    public static final int INTERNAL_INTAG = -1;
    private static boolean completeCloseTags;
    
    private static int[] zzUnpackAction() {
        final int[] result = new int[41];
        int offset = 0;
        offset = zzUnpackAction("\u0002\u0000\u0001\u0001\u0001\u0002\u0001\u0003\u0001\u0004\u0001\u0005\u0001\u0006\u0001\u0007\u0004\b\u0006\u0005\u0001\t\u0001\n\u0001\u0004\u0013\u0005", offset, result);
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
        final int[] result = new int[41];
        int offset = 0;
        offset = zzUnpackRowMap("\u0000\u0000\u0000\u0019\u00002\u0000K\u0000d\u0000}\u0000\u0096\u0000d\u0000d\u0000\u0096\u0000¯\u0000\u00c8\u0000\u00e1\u0000\u00fa\u0000\u0113\u0000\u012c\u0000\u0145\u0000\u015e\u0000\u0177\u0000\u0190\u0000d\u0000d\u0000\u01a9\u0000\u01c2\u0000\u01db\u0000\u01f4\u0000\u020d\u0000\u0226\u0000\u023f\u0000\u0258\u0000\u0271\u0000\u028a\u0000\u02a3\u0000\u02bc\u0000\u02d5\u0000\u02ee\u0000\u0307\u0000\u0320\u0000\u0339\u0000\u0352\u0000\u036b", offset, result);
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
        final int[] result = new int[900];
        int offset = 0;
        offset = zzUnpackTrans("\u0001\u0003\u0001\u0004\u0001\u0005\u0001\u0006\u0015\u0003\u0001\u0007\u0001\u0004\u0001\u0000\u0001\b\u0001\t\u0001\n\u0001\u000b\u0001\f\u0001\r\u0002\u0007\u0001\u000e\u0001\u000f\u0001\u0010\u0003\u0007\u0001\u0011\u0001\u0007\u0001\u0012\u0001\u0013\u0002\u0007\u0001\u0014\u0001\u0015\u0001\u0003\u0003\u0000\u0015\u0003\u0001\u0000\u0001\u0004G\u0000\u0001\u0016\u0001\u0000\u0001\u0007\u0004\u0000\u0012\u0007\u0002\u0000\u0001\u0007\u0004\u0000\r\u0007\u0001\u0017\u0004\u0007\u0002\u0000\u0001\u0007\u0004\u0000\b\u0007\u0001\n\u0001\u000f\b\u0007\u0002\u0000\u0001\u0007\u0004\u0000\u0001\u0007\u0001\u0018\u0010\u0007\u0002\u0000\u0001\u0007\u0004\u0000\u0005\u0007\u0001\u0019\u0001\u0007\u0001\u001a\n\u0007\u0002\u0000\u0001\u0007\u0004\u0000\b\u0007\u0001\n\t\u0007\u0002\u0000\u0001\u0007\u0004\u0000\u0001\u0007\u0001\n\u0010\u0007\u0002\u0000\u0001\u0007\u0004\u0000\u0002\u0007\u0001\u001b\u000f\u0007\u0002\u0000\u0001\u0007\u0004\u0000\u0010\u0007\u0001\u001c\u0001\u0007\u0002\u0000\u0001\u0007\u0004\u0000\u0007\u0007\u0001\u001d\n\u0007\u0006\u0000\u0001\t\u0014\u0000\u0001\u0007\u0004\u0000\u000e\u0007\u0001\n\u0003\u0007\u0002\u0000\u0001\u0007\u0004\u0000\u0004\u0007\u0001\u001e\r\u0007\u0002\u0000\u0001\u0007\u0004\u0000\n\u0007\u0001\u001f\u0007\u0007\u0002\u0000\u0001\u0007\u0004\u0000\b\u0007\u0001 \t\u0007\u0002\u0000\u0001\u0007\u0004\u0000\u0007\u0007\u0001!\n\u0007\u0002\u0000\u0001\u0007\u0004\u0000\u0001\u0007\u0001\"\u0010\u0007\u0002\u0000\u0001\u0007\u0004\u0000\u0002\u0007\u0001#\u000f\u0007\u0002\u0000\u0001\u0007\u0004\u0000\u0005\u0007\u0001\n\f\u0007\u0002\u0000\u0001\u0007\u0004\u0000\u000b\u0007\u0001$\u0006\u0007\u0002\u0000\u0001\u0007\u0004\u0000\u0007\u0007\u0001%\n\u0007\u0002\u0000\u0001\u0007\u0004\u0000\u000b\u0007\u0001\u001e\u0006\u0007\u0002\u0000\u0001\u0007\u0004\u0000\u0011\u0007\u0001&\u0002\u0000\u0001\u0007\u0004\u0000\u000b\u0007\u0001'\u0006\u0007\u0002\u0000\u0001\u0007\u0004\u0000\u0005\u0007\u0001%\f\u0007\u0002\u0000\u0001\u0007\u0004\u0000\t\u0007\u0001\n\b\u0007\u0002\u0000\u0001\u0007\u0004\u0000\u0005\u0007\u0001(\f\u0007\u0002\u0000\u0001\u0007\u0004\u0000\u0002\u0007\u0001)\u000f\u0007\u0002\u0000\u0001\u0007\u0004\u0000\u0007\u0007\u0001\n\n\u0007\u0002\u0000\u0001\u0007\u0004\u0000\u0001\u001e\u0011\u0007\u0002\u0000", offset, result);
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
        final int[] result = new int[41];
        int offset = 0;
        offset = zzUnpackAttribute("\u0002\u0000\u0002\u0001\u0001\t\u0002\u0001\u0002\t\u000b\u0001\u0002\t\u0013\u0001", offset, result);
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
    
    public BBCodeTokenMaker() {
        super();
        this.zzLexicalState = 0;
    }
    
    private void addToken(final int tokenType) {
        this.addToken(this.zzStartRead, this.zzMarkedPos - 1, tokenType);
    }
    
    private void addToken(final int start, final int end, final int tokenType) {
        final int so = start + this.offsetShift;
        this.addToken(this.zzBuffer, start, end, tokenType, so);
    }
    
    public void addToken(final char[] array, final int start, final int end, final int tokenType, final int startOffset) {
        super.addToken(array, start, end, tokenType, startOffset);
        this.zzStartRead = this.zzMarkedPos;
    }
    
    public boolean getCompleteCloseTags() {
        return BBCodeTokenMaker.completeCloseTags;
    }
    
    public String[] getLineCommentStartAndEnd(final int languageIndex) {
        return null;
    }
    
    public Token getTokenList(final Segment text, final int initialTokenType, final int startOffset) {
        this.resetTokenList();
        this.offsetShift = -text.offset + startOffset;
        int state = 0;
        switch (initialTokenType) {
            case -1: {
                state = 1;
                this.start = text.offset;
                break;
            }
            default: {
                state = 0;
                break;
            }
        }
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
    
    public static void setCompleteCloseTags(final boolean complete) {
        BBCodeTokenMaker.completeCloseTags = complete;
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
    
    public BBCodeTokenMaker(final Reader in) {
        super();
        this.zzLexicalState = 0;
        this.zzReader = in;
    }
    
    public BBCodeTokenMaker(final InputStream in) {
        this(new InputStreamReader(in));
    }
    
    private static char[] zzUnpackCMap(final String packed) {
        final char[] map = new char[65536];
        int i = 0;
        int j = 0;
        while (i < 80) {
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
            message = BBCodeTokenMaker.ZZ_ERROR_MSG[errorCode];
        }
        catch (ArrayIndexOutOfBoundsException e) {
            message = BBCodeTokenMaker.ZZ_ERROR_MSG[0];
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
        final char[] zzCMapL = BBCodeTokenMaker.ZZ_CMAP;
        final int[] zzTransL = BBCodeTokenMaker.ZZ_TRANS;
        final int[] zzRowMapL = BBCodeTokenMaker.ZZ_ROWMAP;
        final int[] zzAttrL = BBCodeTokenMaker.ZZ_ATTRIBUTE;
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
            switch ((zzAction < 0) ? zzAction : BBCodeTokenMaker.ZZ_ACTION[zzAction]) {
                case 1: {
                    this.addToken(20);
                }
                case 11: {
                    continue;
                }
                case 9: {
                    this.addToken(25);
                }
                case 12: {
                    continue;
                }
                case 2: {
                    this.addToken(21);
                }
                case 13: {
                    continue;
                }
                case 10: {
                    this.addToken(23);
                }
                case 14: {
                    continue;
                }
                case 8: {
                    this.addToken(26);
                }
                case 15: {
                    continue;
                }
                case 4: {
                    this.addToken(25);
                    this.yybegin(1);
                }
                case 16: {
                    continue;
                }
                case 6: {
                    this.addToken(20);
                }
                case 17: {
                    continue;
                }
                case 5: {
                    this.addToken(27);
                }
                case 18: {
                    continue;
                }
                case 3: {
                    this.addNullToken();
                    return this.firstToken;
                }
                case 19: {
                    continue;
                }
                case 7: {
                    this.yybegin(0);
                    this.addToken(25);
                }
                case 20: {
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
                            this.addToken(this.zzMarkedPos, this.zzMarkedPos, -1);
                            return this.firstToken;
                        }
                        case 42: {
                            continue;
                        }
                        case 0: {
                            this.addNullToken();
                            return this.firstToken;
                        }
                        case 43: {
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
        ZZ_CMAP = zzUnpackCMap("\t\u0000\u0001\u0001\u0001\u0002\u0001\u0000\u0001\u0001\u0013\u0000\u0001\u0001\u000e\u0000\u0001\u0017\r\u0000\u0001\u0018\u001d\u0000\u0001\u0003\u0001\u0000\u0001\u0004\u0004\u0000\u0001\u0005\u0001\u000b\u0001\u0016\u0001\n\u0001\u0000\u0001\u0013\u0001\u0000\u0001\u0006\u0002\u0000\u0001\r\u0001\u0012\u0001\u000f\u0001\f\u0001\u0000\u0001\u0011\u0001\u000e\u0001\b\u0001\u0010\u0001\u0007\u0001\u0015\u0002\u0000\u0001\u0014\u0001\t\uff85\u0000");
        ZZ_ACTION = zzUnpackAction();
        ZZ_ROWMAP = zzUnpackRowMap();
        ZZ_TRANS = zzUnpackTrans();
        ZZ_ERROR_MSG = new String[] { "Unkown internal scanner error", "Error: could not match input", "Error: pushback value was too large" };
        ZZ_ATTRIBUTE = zzUnpackAttribute();
        BBCodeTokenMaker.completeCloseTags = true;
    }
}
