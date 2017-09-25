package org.fife.ui.rsyntaxtextarea.modes;

import javax.swing.text.*;
import org.fife.ui.rsyntaxtextarea.*;
import java.io.*;

public class CSSTokenMaker extends AbstractJFlexCTokenMaker
{
    public static final int YYEOF = -1;
    public static final int CSS_C_STYLE_COMMENT = 5;
    public static final int YYINITIAL = 0;
    public static final int CSS_STRING = 3;
    public static final int CSS_VALUE = 2;
    public static final int CSS_PROPERTY = 1;
    public static final int CSS_CHAR_LITERAL = 4;
    private static final String ZZ_CMAP_PACKED = "\t\u0000\u0001 \u00011\u0015\u0000\u0001 \u00013\u0001.\u0001\u001e\u0001*\u0001%\u0001'\u0001/\u0001#\u00014\u0001\u0005\u0001)\u0001-\u0001\u0004\u0001\u0006\u0001!\n\u0001\u0001\u0007\u0001\u001f\u0001\u0000\u0001)\u00010\u0001'\u0001\u001d\u0006&\u0014\u0002\u0001(\u0001\"\u0001(\u00010\u0001\u0003\u0001\u0000\u0001\u0011\u0001\u001c\u0001\r\u0001\u0010\u0001\u0016\u0001\u0013\u0001\u001b\u0001\f\u0001\u000e\u0001\u0002\u0001\u0018\u0001\u000f\u0001\u0017\u0001\u000b\u0001\t\u0001\u0015\u0001\u0002\u0001\b\u0001\u0012\u0001\n\u0001\u001a\u0001\u0019\u0001+\u0001$\u0001\u0014\u0001\u0002\u0001,\u00010\u00012\u0001)\uff81\u0000";
    private static final char[] ZZ_CMAP;
    private static final int[] ZZ_ACTION;
    private static final String ZZ_ACTION_PACKED_0 = "\u0002\u0000\u0001\u0001\u0003\u0000\u0001\u0002\u0001\u0003\u0001\u0004\u0002\u0002\u0001\u0005\u0001\u0006\u0001\u0002\u0001\u0007\u0001\b\u0001\u0001\u0001\t\u0001\n\u0001\u000b\u0001\f\u0001\r\u0001\f\u0001\u000e\u0001\f\u0001\u000f\u0001\u0010\u0001\u0011\u0001\u0012\u0001\u0013\u0002\u0001\u0001\u0012\u0001\u0014\u0001\u0001\u0001\u0015\u0001\u0016\u0001\u0012\u0001\u0017\u0001\u0018\u0001\u0019\u0001\u001a\u0001\u001b\u0001\u0018\u0001\u001c\u0001\u001d\u0005\u0018\u0001\u001e\r\u0000\u0001\u001f\u0001 \u0001!\u0002\u0000\u0001\u0013\u0003\u0000\u0001\u0013\u0001\u0000\u0001\u0019\u0001\"!\u0000\u0001\r\u000f\u0000\u0001#'\u0000\u0001$";
    private static final int[] ZZ_ROWMAP;
    private static final String ZZ_ROWMAP_PACKED_0 = "\u0000\u0000\u00005\u0000j\u0000\u009f\u0000\u00d4\u0000\u0109\u0000\u013e\u0000\u0173\u0000\u01a8\u0000\u01dd\u0000\u0212\u0000\u013e\u0000\u0247\u0000\u027c\u0000\u013e\u0000\u013e\u0000\u013e\u0000\u013e\u0000\u013e\u0000\u013e\u0000\u013e\u0000\u02b1\u0000\u02e6\u0000\u013e\u0000\u027c\u0000\u013e\u0000\u013e\u0000\u013e\u0000\u013e\u0000\u031b\u0000\u0350\u0000\u0385\u0000\u03ba\u0000\u013e\u0000\u03ef\u0000\u013e\u0000\u013e\u0000\u0424\u0000\u013e\u0000\u0459\u0000\u048e\u0000\u013e\u0000\u013e\u0000\u04c3\u0000\u013e\u0000\u013e\u0000\u04f8\u0000\u052d\u0000\u0562\u0000\u0597\u0000\u05cc\u0000\u013e\u0000\u0601\u0000\u0636\u0000\u066b\u0000\u06a0\u0000\u06d5\u0000\u070a\u0000\u073f\u0000\u0774\u0000\u07a9\u0000\u07de\u0000\u0813\u0000\u0848\u0000\u087d\u0000\u08b2\u0000\u08e7\u0000\u013e\u0000\u091c\u0000\u0951\u0000\u013e\u0000\u0986\u0000\u09bb\u0000\u09f0\u0000\u03ba\u0000\u0a25\u0000\u013e\u0000\u013e\u0000\u0a5a\u0000\u0a8f\u0000\u0ac4\u0000\u0af9\u0000\u0b2e\u0000\u0b63\u0000\u0b98\u0000\u0bcd\u0000\u0c02\u0000\u0c37\u0000\u0c6c\u0000\u0ca1\u0000\u0cd6\u0000\u0d0b\u0000\u0d40\u0000\u0d75\u0000\u0daa\u0000\u0ddf\u0000\u0e14\u0000\u0e49\u0000\u0e7e\u0000\u0eb3\u0000\u0ee8\u0000\u0f1d\u0000\u0f52\u0000\u0f87\u0000\u0fbc\u0000\u0ff1\u0000\u1026\u0000\u105b\u0000\u1090\u0000\u10c5\u0000\u10fa\u0000\u013e\u0000\u112f\u0000\u1164\u0000\u1199\u0000\u11ce\u0000\u1203\u0000\u1238\u0000\u126d\u0000\u12a2\u0000\u12d7\u0000\u130c\u0000\u1341\u0000\u1376\u0000\u13ab\u0000\u13e0\u0000\u1415\u0000\u144a\u0000\u147f\u0000\u14b4\u0000\u14e9\u0000\u151e\u0000\u1553\u0000\u1588\u0000\u15bd\u0000\u15f2\u0000\u1627\u0000\u165c\u0000\u1691\u0000\u16c6\u0000\u16fb\u0000\u1730\u0000\u144a\u0000\u1765\u0000\u179a\u0000\u17cf\u0000\u1804\u0000\u1839\u0000\u186e\u0000\u18a3\u0000\u18d8\u0000\u190d\u0000\u1942\u0000\u1977\u0000\u19ac\u0000\u19e1\u0000\u1a16\u0000\u1a4b\u0000\u1a80\u0000\u1ab5\u0000\u1aea\u0000\u1b1f\u0000\u1b54\u0000\u1b89\u0000\u1bbe\u0000\u1bf3\u0000\u1c28\u0000\u013e";
    private static final int[] ZZ_TRANS;
    private static final String ZZ_TRANS_PACKED_0 = "\u0002\u0007\u0005\b\u0001\t\u0015\b\u0001\n\u0001\u000b\u0001\f\u0001\r\u0001\u000e\u0001\u0007\u0001\f\u0001\b\u0001\u0007\u0001\b\u0001\u0007\u0001\f\u0002\u000f\u0001\b\u0001\u0010\u0001\u0011\u0001\u0012\u0001\u0013\u0001\u000f\u0001\u0014\u0002\u0007\u0001\f\u0002\u0015\u0003\u0016\u0001\u0017\u0001\u0015\u0001\u0018\u0015\u0016\u0003\u0015\u0001\r\u0001\u0019\u0002\u0015\u0001\u0016\u0001\u0015\u0001\u0016\u0004\u0015\u0001\u0016\u0001\u001a\u0004\u0015\u0001\u001b\u0001\u001c\u0002\u0015\u0001\u001d\u0001\u001e\u0002\u001f\u0001 \u0001\u001d\u0001\u0011\u0001\u001d\u0015\u001f\u0001\u001d\u0001!\u0001\"\u0001\r\u0001#\u0001\u001f\u0001$\u0001\u001f\u0001\u001d\u0001\u001f\u0004\u001d\u0001\u001f\u0001\u001d\u0001\u0011\u0001\u0012\u0001\u0013\u0001\u001d\u0001%\u0001\u001c\u0001&\u0001'\"(\u0001)\u000b(\u0001*\u0002(\u0001+\u0003(\",\u0001)\f,\u0001-\u0001,\u0001.\u0003,\u0005/\u00010\u0006/\u00011\u0006/\u00012\u0017/\u00013\u0005/\u00014\u0003/6\u0000\u0004\b\u0001\u0000\u0001\b\u0001\u0000\u0015\b\u0007\u0000\u0001\b\u0001\u0000\u0001\b\u0004\u0000\u0001\b\u0010\u0000\u00015\u00016\u00017\u00018\u00019\u0001:\u0001;\u0001\u0000\u0001<\u0001=\u0001>\u0001\u0000\u0001?\u0002\u0000\u0001@\u0002\u0000\u0001A\u001d\u0000\u0005B\u0001\u0000\u0015B\u0007\u0000\u0001B\u0001\u0000\u0001B\u0004\u0000\u0001B\u000b\u0000\u0005C\u0001\u0000\u0015C\u0007\u0000\u0001C\u0001\u0000\u0001C\u0004\u0000\u0001C)\u0000\u0001\r\u0019\u0000\u0001D0\u0000\u0004\u0016\u0003\u0000\u0015\u0016\u0007\u0000\u0001\u0016\u0001\u0000\u0001\u0016\u0004\u0000\u0001\u0016\u000b\u0000\u0003\u0016\u0003\u0000\u0015\u0016\u0007\u0000\u0001\u0016\u0001\u0000\u0001\u0016\u0004\u0000\u0001\u0016\n\u0000\u0001\u001e\u0004\u0000\u0001\u001e\u0006\u0000\u0001E\u0001F\u0003\u0000\u0001G\u0002\u0000\u0001H\u0001I\u0001J\r\u0000\u0001G\u0011\u0000\u0003\u001f\u0003\u0000\u0015\u001f\u0004\u0000\u0002\u001f\u0001$\u0001\u001f\u0001\u0000\u0001\u001f\u0004\u0000\u0001\u001f\n\u0000\u0001\u001e\u0003\u001f\u0003\u0000\u0015\u001f\u0004\u0000\u0002\u001f\u0001$\u0001\u001f\u0001\u0000\u0001\u001f\u0004\u0000\u0001\u001f\n\u0000\u0001K\u000b\u0000\u0001K\u0002\u0000\u0002K\u0001\u0000\u0001K\u0002\u0000\u0001K\u0005\u0000\u0001K\t\u0000\u0001K\u0010\u0000\u0003\u001f\u0001D\u0002\u0000\u0015\u001f\u0004\u0000\u0002\u001f\u0001$\u0001\u001f\u0001\u0000\u0001\u001f\u0004\u0000\u0001\u001f\u0017\u0000\u0001L&\u0000\"(\u0001\u0000\u000b(\u0001\u0000\u0002(\u0001\u0000\u0003(1M\u0001\u0000\u0003M\",\u0001\u0000\f,\u0001\u0000\u0001,\u0001\u0000\u0003,\u0005/\u0001\u0000\u0006/\u0001\u0000\u0006/\u0001\u0000\u0017/\u0001\u0000\u0005/\u0001\u0000\u0003/!\u0000\u0001N\u001d\u0000\u0001O4\u0000\u0001P\u0003\u0000\u0001QQ\u0000\u0001R\u001a\u0000\u0001S\u0001\u0000\u0001T\b\u0000\u0001U!\u0000\u0001V6\u0000\u0001W:\u0000\u0001X,\u0000\u0001Y\u0001Z3\u0000\u0001[7\u0000\u0001\\6\u0000\u0001]\u0002\u0000\u0001^1\u0000\u0001_3\u0000\u0001`0\u0000\u0001a\u0004\u0000\u0001b1\u0000\u0001c\u000b\u0000\u0001d+\u0000\u0001e'\u0000\u0004B\u0001\u0000\u0001B\u0001\u0000\u0015B\u0007\u0000\u0001B\u0001\u0000\u0001B\u0004\u0000\u0001B\n\u0000\u0004C\u0001\u0000\u0001C\u0001\u0000\u0015C\u0007\u0000\u0001C\u0001\u0000\u0001C\u0004\u0000\u0001C \u0000\u0001G(\u0000\u0001G3\u0000\u0001G\u0002\u0000\u0001G\u0016\u0000\u0001G'\u0000\u0001G\f\u0000\u0001G\"\u0000\u0001G\u0004\u0000\u0001G4\u0000\u0001f'\u0000\u0001g?\u0000\u0001h.\u0000\u0001iP\u0000\u0001j\u001c\u0000\u0001k/\u0000\u0001l<\u0000\u0001m'\u0000\u0001Y:\u0000\u0001n-\u0000\u0001o6\u0000\u0001p6\u0000\u0001qA\u0000\u0001r1\u0000\u0001s)\u0000\u0001t4\u0000\u0001u\u0006\u0000\u0001v4\u0000\u0001c,\u0000\u0001w7\u0000\u0001x/\u0000\u0001y=\u0000\u0001z8\u0000\u0001{1\u0000\u0001|7\u0000\u0001}4\u0000\u0001~&\u0000\u0001\u007fC\u0000\u0001h$\u0000\u0001\u00808\u0000\u0001r2\u0000\u0001\u0081?\u0000\u0001\u00825\u0000\u0001\u0083;\u0000\u0001\u0084\u001d\u0000\u0001\u0085F\u0000\u0001\u0086+\u0000\u0001\u0087?\u0000\u0001p7\u0000\u0001p#\u0000\u0001\u00838\u0000\u0001\u0088@\u0000\u0001\u0089,\u0000\u0001v>\u0000\u0001\u008a\"\u0000\u0001\u008b8\u0000\u0001\u008c/\u0000\u0001\u008d2\u0000\u0001\u007f\n\u0000\u0001hC\u0000\u0001\u008e\u0014\u0000\u0002\u0080\u0005\u008f\u0015\u0080\u0003\u008f\u0001\u0000\u0001\u0080\u0001\u0000\u0001\u008f\u0001\u0080\u0001\u008f\u0001\u0080\u0003\u008f\u0002\u0080\u0001\u0000\u0001\u008f\u0001\u0000\u0001\u008f\u0003\u0000\u0002\u008f\u0012\u0000\u0001\u0090+\u0000\u0001\u0091/\u0000\u0001\u0092F\u0000\u0001Y'\u0000\u0001\u0093\u0003\u0000\u0001\u0094\u0001\u0000\u0001\u0095-\u0000\u0001pD\u0000\u0001\u00965\u0000\u0001\u0097-\u0000\u0001p1\u0000\u0001\u00969\u0000\u0001p*\u0000\u0001\u00962\u0000\u0001\u0098M\u0000\u0001\u0080\u001d\u0000\u0001\u00992\u0000\u0001\u00975\u0000\u0001\u0093\u0003\u0000\u0001\u0094:\u0000\u0001\u009a-\u0000\u0001\u009b9\u0000\u0001y9\u0000\u0001\u009c4\u0000\u0001p(\u0000\u0001\u009d.\u0000\u0001\u009e4\u0000\u0001\u009f>\u0000\u0001 6\u0000\u0001p5\u0000\u0001¡2\u0000\u0001¢/\u0000\u0001£9\u0000\u0001\u009c0\u0000\u0001¤7\u0000\u0001¥\u0007\u0000\u0001¦2\u0000\u0001§*\u0000\u0001¨5\u0000\u0001\u00973\u0000\u0001k?\u0000\u0001\u0097\u001f\u0000";
    private static final int ZZ_UNKNOWN_ERROR = 0;
    private static final int ZZ_NO_MATCH = 1;
    private static final int ZZ_PUSHBACK_2BIG = 2;
    private static final String[] ZZ_ERROR_MSG;
    private static final int[] ZZ_ATTRIBUTE;
    private static final String ZZ_ATTRIBUTE_PACKED_0 = "\u0002\u0000\u0001\u0001\u0003\u0000\u0001\t\u0004\u0001\u0001\t\u0002\u0001\u0007\t\u0002\u0001\u0001\t\u0001\u0001\u0004\t\u0004\u0001\u0001\t\u0001\u0001\u0002\t\u0001\u0001\u0001\t\u0002\u0001\u0002\t\u0001\u0001\u0002\t\u0005\u0001\u0001\t\r\u0000\u0002\u0001\u0001\t\u0002\u0000\u0001\t\u0003\u0000\u0001\u0001\u0001\u0000\u0002\t!\u0000\u0001\t\u000f\u0000\u0001\u0001'\u0000\u0001\t";
    private Reader zzReader;
    private int zzState;
    private int zzLexicalState;
    private char[] zzBuffer;
    private int zzMarkedPos;
    private int zzCurrentPos;
    private int zzStartRead;
    private int zzEndRead;
    private boolean zzAtEOF;
    public static final int INTERNAL_CSS_PROPERTY = -1;
    public static final int INTERNAL_CSS_VALUE = -2;
    public static final int INTERNAL_CSS_STRING = -2048;
    public static final int INTERNAL_CSS_CHAR = -4096;
    public static final int INTERNAL_CSS_MLC = -6144;
    private int cssPrevState;
    
    private static int[] zzUnpackAction() {
        final int[] result = new int[168];
        int offset = 0;
        offset = zzUnpackAction("\u0002\u0000\u0001\u0001\u0003\u0000\u0001\u0002\u0001\u0003\u0001\u0004\u0002\u0002\u0001\u0005\u0001\u0006\u0001\u0002\u0001\u0007\u0001\b\u0001\u0001\u0001\t\u0001\n\u0001\u000b\u0001\f\u0001\r\u0001\f\u0001\u000e\u0001\f\u0001\u000f\u0001\u0010\u0001\u0011\u0001\u0012\u0001\u0013\u0002\u0001\u0001\u0012\u0001\u0014\u0001\u0001\u0001\u0015\u0001\u0016\u0001\u0012\u0001\u0017\u0001\u0018\u0001\u0019\u0001\u001a\u0001\u001b\u0001\u0018\u0001\u001c\u0001\u001d\u0005\u0018\u0001\u001e\r\u0000\u0001\u001f\u0001 \u0001!\u0002\u0000\u0001\u0013\u0003\u0000\u0001\u0013\u0001\u0000\u0001\u0019\u0001\"!\u0000\u0001\r\u000f\u0000\u0001#'\u0000\u0001$", offset, result);
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
        final int[] result = new int[168];
        int offset = 0;
        offset = zzUnpackRowMap("\u0000\u0000\u00005\u0000j\u0000\u009f\u0000\u00d4\u0000\u0109\u0000\u013e\u0000\u0173\u0000\u01a8\u0000\u01dd\u0000\u0212\u0000\u013e\u0000\u0247\u0000\u027c\u0000\u013e\u0000\u013e\u0000\u013e\u0000\u013e\u0000\u013e\u0000\u013e\u0000\u013e\u0000\u02b1\u0000\u02e6\u0000\u013e\u0000\u027c\u0000\u013e\u0000\u013e\u0000\u013e\u0000\u013e\u0000\u031b\u0000\u0350\u0000\u0385\u0000\u03ba\u0000\u013e\u0000\u03ef\u0000\u013e\u0000\u013e\u0000\u0424\u0000\u013e\u0000\u0459\u0000\u048e\u0000\u013e\u0000\u013e\u0000\u04c3\u0000\u013e\u0000\u013e\u0000\u04f8\u0000\u052d\u0000\u0562\u0000\u0597\u0000\u05cc\u0000\u013e\u0000\u0601\u0000\u0636\u0000\u066b\u0000\u06a0\u0000\u06d5\u0000\u070a\u0000\u073f\u0000\u0774\u0000\u07a9\u0000\u07de\u0000\u0813\u0000\u0848\u0000\u087d\u0000\u08b2\u0000\u08e7\u0000\u013e\u0000\u091c\u0000\u0951\u0000\u013e\u0000\u0986\u0000\u09bb\u0000\u09f0\u0000\u03ba\u0000\u0a25\u0000\u013e\u0000\u013e\u0000\u0a5a\u0000\u0a8f\u0000\u0ac4\u0000\u0af9\u0000\u0b2e\u0000\u0b63\u0000\u0b98\u0000\u0bcd\u0000\u0c02\u0000\u0c37\u0000\u0c6c\u0000\u0ca1\u0000\u0cd6\u0000\u0d0b\u0000\u0d40\u0000\u0d75\u0000\u0daa\u0000\u0ddf\u0000\u0e14\u0000\u0e49\u0000\u0e7e\u0000\u0eb3\u0000\u0ee8\u0000\u0f1d\u0000\u0f52\u0000\u0f87\u0000\u0fbc\u0000\u0ff1\u0000\u1026\u0000\u105b\u0000\u1090\u0000\u10c5\u0000\u10fa\u0000\u013e\u0000\u112f\u0000\u1164\u0000\u1199\u0000\u11ce\u0000\u1203\u0000\u1238\u0000\u126d\u0000\u12a2\u0000\u12d7\u0000\u130c\u0000\u1341\u0000\u1376\u0000\u13ab\u0000\u13e0\u0000\u1415\u0000\u144a\u0000\u147f\u0000\u14b4\u0000\u14e9\u0000\u151e\u0000\u1553\u0000\u1588\u0000\u15bd\u0000\u15f2\u0000\u1627\u0000\u165c\u0000\u1691\u0000\u16c6\u0000\u16fb\u0000\u1730\u0000\u144a\u0000\u1765\u0000\u179a\u0000\u17cf\u0000\u1804\u0000\u1839\u0000\u186e\u0000\u18a3\u0000\u18d8\u0000\u190d\u0000\u1942\u0000\u1977\u0000\u19ac\u0000\u19e1\u0000\u1a16\u0000\u1a4b\u0000\u1a80\u0000\u1ab5\u0000\u1aea\u0000\u1b1f\u0000\u1b54\u0000\u1b89\u0000\u1bbe\u0000\u1bf3\u0000\u1c28\u0000\u013e", offset, result);
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
        final int[] result = new int[7261];
        int offset = 0;
        offset = zzUnpackTrans("\u0002\u0007\u0005\b\u0001\t\u0015\b\u0001\n\u0001\u000b\u0001\f\u0001\r\u0001\u000e\u0001\u0007\u0001\f\u0001\b\u0001\u0007\u0001\b\u0001\u0007\u0001\f\u0002\u000f\u0001\b\u0001\u0010\u0001\u0011\u0001\u0012\u0001\u0013\u0001\u000f\u0001\u0014\u0002\u0007\u0001\f\u0002\u0015\u0003\u0016\u0001\u0017\u0001\u0015\u0001\u0018\u0015\u0016\u0003\u0015\u0001\r\u0001\u0019\u0002\u0015\u0001\u0016\u0001\u0015\u0001\u0016\u0004\u0015\u0001\u0016\u0001\u001a\u0004\u0015\u0001\u001b\u0001\u001c\u0002\u0015\u0001\u001d\u0001\u001e\u0002\u001f\u0001 \u0001\u001d\u0001\u0011\u0001\u001d\u0015\u001f\u0001\u001d\u0001!\u0001\"\u0001\r\u0001#\u0001\u001f\u0001$\u0001\u001f\u0001\u001d\u0001\u001f\u0004\u001d\u0001\u001f\u0001\u001d\u0001\u0011\u0001\u0012\u0001\u0013\u0001\u001d\u0001%\u0001\u001c\u0001&\u0001'\"(\u0001)\u000b(\u0001*\u0002(\u0001+\u0003(\",\u0001)\f,\u0001-\u0001,\u0001.\u0003,\u0005/\u00010\u0006/\u00011\u0006/\u00012\u0017/\u00013\u0005/\u00014\u0003/6\u0000\u0004\b\u0001\u0000\u0001\b\u0001\u0000\u0015\b\u0007\u0000\u0001\b\u0001\u0000\u0001\b\u0004\u0000\u0001\b\u0010\u0000\u00015\u00016\u00017\u00018\u00019\u0001:\u0001;\u0001\u0000\u0001<\u0001=\u0001>\u0001\u0000\u0001?\u0002\u0000\u0001@\u0002\u0000\u0001A\u001d\u0000\u0005B\u0001\u0000\u0015B\u0007\u0000\u0001B\u0001\u0000\u0001B\u0004\u0000\u0001B\u000b\u0000\u0005C\u0001\u0000\u0015C\u0007\u0000\u0001C\u0001\u0000\u0001C\u0004\u0000\u0001C)\u0000\u0001\r\u0019\u0000\u0001D0\u0000\u0004\u0016\u0003\u0000\u0015\u0016\u0007\u0000\u0001\u0016\u0001\u0000\u0001\u0016\u0004\u0000\u0001\u0016\u000b\u0000\u0003\u0016\u0003\u0000\u0015\u0016\u0007\u0000\u0001\u0016\u0001\u0000\u0001\u0016\u0004\u0000\u0001\u0016\n\u0000\u0001\u001e\u0004\u0000\u0001\u001e\u0006\u0000\u0001E\u0001F\u0003\u0000\u0001G\u0002\u0000\u0001H\u0001I\u0001J\r\u0000\u0001G\u0011\u0000\u0003\u001f\u0003\u0000\u0015\u001f\u0004\u0000\u0002\u001f\u0001$\u0001\u001f\u0001\u0000\u0001\u001f\u0004\u0000\u0001\u001f\n\u0000\u0001\u001e\u0003\u001f\u0003\u0000\u0015\u001f\u0004\u0000\u0002\u001f\u0001$\u0001\u001f\u0001\u0000\u0001\u001f\u0004\u0000\u0001\u001f\n\u0000\u0001K\u000b\u0000\u0001K\u0002\u0000\u0002K\u0001\u0000\u0001K\u0002\u0000\u0001K\u0005\u0000\u0001K\t\u0000\u0001K\u0010\u0000\u0003\u001f\u0001D\u0002\u0000\u0015\u001f\u0004\u0000\u0002\u001f\u0001$\u0001\u001f\u0001\u0000\u0001\u001f\u0004\u0000\u0001\u001f\u0017\u0000\u0001L&\u0000\"(\u0001\u0000\u000b(\u0001\u0000\u0002(\u0001\u0000\u0003(1M\u0001\u0000\u0003M\",\u0001\u0000\f,\u0001\u0000\u0001,\u0001\u0000\u0003,\u0005/\u0001\u0000\u0006/\u0001\u0000\u0006/\u0001\u0000\u0017/\u0001\u0000\u0005/\u0001\u0000\u0003/!\u0000\u0001N\u001d\u0000\u0001O4\u0000\u0001P\u0003\u0000\u0001QQ\u0000\u0001R\u001a\u0000\u0001S\u0001\u0000\u0001T\b\u0000\u0001U!\u0000\u0001V6\u0000\u0001W:\u0000\u0001X,\u0000\u0001Y\u0001Z3\u0000\u0001[7\u0000\u0001\\6\u0000\u0001]\u0002\u0000\u0001^1\u0000\u0001_3\u0000\u0001`0\u0000\u0001a\u0004\u0000\u0001b1\u0000\u0001c\u000b\u0000\u0001d+\u0000\u0001e'\u0000\u0004B\u0001\u0000\u0001B\u0001\u0000\u0015B\u0007\u0000\u0001B\u0001\u0000\u0001B\u0004\u0000\u0001B\n\u0000\u0004C\u0001\u0000\u0001C\u0001\u0000\u0015C\u0007\u0000\u0001C\u0001\u0000\u0001C\u0004\u0000\u0001C \u0000\u0001G(\u0000\u0001G3\u0000\u0001G\u0002\u0000\u0001G\u0016\u0000\u0001G'\u0000\u0001G\f\u0000\u0001G\"\u0000\u0001G\u0004\u0000\u0001G4\u0000\u0001f'\u0000\u0001g?\u0000\u0001h.\u0000\u0001iP\u0000\u0001j\u001c\u0000\u0001k/\u0000\u0001l<\u0000\u0001m'\u0000\u0001Y:\u0000\u0001n-\u0000\u0001o6\u0000\u0001p6\u0000\u0001qA\u0000\u0001r1\u0000\u0001s)\u0000\u0001t4\u0000\u0001u\u0006\u0000\u0001v4\u0000\u0001c,\u0000\u0001w7\u0000\u0001x/\u0000\u0001y=\u0000\u0001z8\u0000\u0001{1\u0000\u0001|7\u0000\u0001}4\u0000\u0001~&\u0000\u0001\u007fC\u0000\u0001h$\u0000\u0001\u00808\u0000\u0001r2\u0000\u0001\u0081?\u0000\u0001\u00825\u0000\u0001\u0083;\u0000\u0001\u0084\u001d\u0000\u0001\u0085F\u0000\u0001\u0086+\u0000\u0001\u0087?\u0000\u0001p7\u0000\u0001p#\u0000\u0001\u00838\u0000\u0001\u0088@\u0000\u0001\u0089,\u0000\u0001v>\u0000\u0001\u008a\"\u0000\u0001\u008b8\u0000\u0001\u008c/\u0000\u0001\u008d2\u0000\u0001\u007f\n\u0000\u0001hC\u0000\u0001\u008e\u0014\u0000\u0002\u0080\u0005\u008f\u0015\u0080\u0003\u008f\u0001\u0000\u0001\u0080\u0001\u0000\u0001\u008f\u0001\u0080\u0001\u008f\u0001\u0080\u0003\u008f\u0002\u0080\u0001\u0000\u0001\u008f\u0001\u0000\u0001\u008f\u0003\u0000\u0002\u008f\u0012\u0000\u0001\u0090+\u0000\u0001\u0091/\u0000\u0001\u0092F\u0000\u0001Y'\u0000\u0001\u0093\u0003\u0000\u0001\u0094\u0001\u0000\u0001\u0095-\u0000\u0001pD\u0000\u0001\u00965\u0000\u0001\u0097-\u0000\u0001p1\u0000\u0001\u00969\u0000\u0001p*\u0000\u0001\u00962\u0000\u0001\u0098M\u0000\u0001\u0080\u001d\u0000\u0001\u00992\u0000\u0001\u00975\u0000\u0001\u0093\u0003\u0000\u0001\u0094:\u0000\u0001\u009a-\u0000\u0001\u009b9\u0000\u0001y9\u0000\u0001\u009c4\u0000\u0001p(\u0000\u0001\u009d.\u0000\u0001\u009e4\u0000\u0001\u009f>\u0000\u0001 6\u0000\u0001p5\u0000\u0001¡2\u0000\u0001¢/\u0000\u0001£9\u0000\u0001\u009c0\u0000\u0001¤7\u0000\u0001¥\u0007\u0000\u0001¦2\u0000\u0001§*\u0000\u0001¨5\u0000\u0001\u00973\u0000\u0001k?\u0000\u0001\u0097\u001f\u0000", offset, result);
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
        final int[] result = new int[168];
        int offset = 0;
        offset = zzUnpackAttribute("\u0002\u0000\u0001\u0001\u0003\u0000\u0001\t\u0004\u0001\u0001\t\u0002\u0001\u0007\t\u0002\u0001\u0001\t\u0001\u0001\u0004\t\u0004\u0001\u0001\t\u0001\u0001\u0002\t\u0001\u0001\u0001\t\u0002\u0001\u0002\t\u0001\u0001\u0002\t\u0005\u0001\u0001\t\r\u0000\u0002\u0001\u0001\t\u0002\u0000\u0001\t\u0003\u0000\u0001\u0001\u0001\u0000\u0002\t!\u0000\u0001\t\u000f\u0000\u0001\u0001'\u0000\u0001\t", offset, result);
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
    
    public CSSTokenMaker() {
        super();
        this.zzLexicalState = 0;
    }
    
    private void addEndToken(final int tokenType) {
        this.addToken(this.zzMarkedPos, this.zzMarkedPos, tokenType);
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
        this.addToken(this.zzBuffer, start, end, tokenType, so);
    }
    
    public void addToken(final char[] array, final int start, final int end, final int tokenType, final int startOffset) {
        super.addToken(array, start, end, tokenType, startOffset);
        this.zzStartRead = this.zzMarkedPos;
    }
    
    public int getClosestStandardTokenTypeForInternalType(final int type) {
        switch (type) {
            case -4096:
            case -2048: {
                return 13;
            }
            case -6144: {
                return 2;
            }
            default: {
                return type;
            }
        }
    }
    
    public boolean getCurlyBracesDenoteCodeBlocks() {
        return true;
    }
    
    public boolean getMarkOccurrencesOfTokenType(final int type) {
        return type == 6;
    }
    
    public boolean getShouldIndentNextLineAfter(final Token t) {
        if (t != null && t.length() == 1) {
            final char ch = t.charAt(0);
            return ch == '{' || ch == '(';
        }
        return false;
    }
    
    public Token getTokenList(final Segment text, final int initialTokenType, final int startOffset) {
        this.resetTokenList();
        this.offsetShift = -text.offset + startOffset;
        this.cssPrevState = 0;
        int state = 0;
        switch (initialTokenType) {
            case 13: {
                state = 3;
                break;
            }
            case 14: {
                state = 4;
                break;
            }
            case 2: {
                state = 5;
                break;
            }
            case -1: {
                state = 1;
                break;
            }
            case -2: {
                state = 2;
                break;
            }
            default: {
                if (initialTokenType < -1024) {
                    final int main = -(-initialTokenType & 0xFFFFFF00);
                    switch (main) {
                        default: {
                            state = 3;
                            break;
                        }
                        case -4096: {
                            state = 4;
                            break;
                        }
                        case -6144: {
                            state = 5;
                            break;
                        }
                    }
                    this.cssPrevState = (-initialTokenType & 0xFF);
                    break;
                }
                state = 0;
                break;
            }
        }
        this.start = text.offset;
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
    
    public CSSTokenMaker(final Reader in) {
        super();
        this.zzLexicalState = 0;
        this.zzReader = in;
    }
    
    public CSSTokenMaker(final InputStream in) {
        this(new InputStreamReader(in));
    }
    
    private static char[] zzUnpackCMap(final String packed) {
        final char[] map = new char[65536];
        int i = 0;
        int j = 0;
        while (i < 134) {
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
            message = CSSTokenMaker.ZZ_ERROR_MSG[errorCode];
        }
        catch (ArrayIndexOutOfBoundsException e) {
            message = CSSTokenMaker.ZZ_ERROR_MSG[0];
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
        final char[] zzCMapL = CSSTokenMaker.ZZ_CMAP;
        final int[] zzTransL = CSSTokenMaker.ZZ_TRANS;
        final int[] zzRowMapL = CSSTokenMaker.ZZ_ROWMAP;
        final int[] zzAttrL = CSSTokenMaker.ZZ_ATTRIBUTE;
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
            switch ((zzAction < 0) ? zzAction : CSSTokenMaker.ZZ_ACTION[zzAction]) {
                case 1: {
                    this.addToken(20);
                }
                case 37: {
                    continue;
                }
                case 2: {
                    this.addToken(20);
                }
                case 38: {
                    continue;
                }
                case 22: {
                    this.addEndToken(-2);
                    return this.firstToken;
                }
                case 39: {
                    continue;
                }
                case 36: {
                    this.addToken(19);
                }
                case 40: {
                    continue;
                }
                case 32: {
                    this.addToken(17);
                }
                case 41: {
                    continue;
                }
                case 8: {
                    this.addToken(22);
                    this.yybegin(1);
                }
                case 42: {
                    continue;
                }
                case 26: {
                    this.addToken(this.start, this.zzStartRead, 13);
                    this.yybegin(this.cssPrevState);
                }
                case 43: {
                    continue;
                }
                case 29: {
                    this.addToken(this.start, this.zzStartRead - 1, 14);
                    this.addEndToken(-4096 - this.cssPrevState);
                    return this.firstToken;
                }
                case 44: {
                    continue;
                }
                case 33: {
                    this.start = this.zzMarkedPos - 2;
                    this.cssPrevState = this.zzLexicalState;
                    this.yybegin(5);
                }
                case 45: {
                    continue;
                }
                case 28: {
                    this.addToken(this.start, this.zzStartRead, 14);
                    this.yybegin(this.cssPrevState);
                }
                case 46: {
                    continue;
                }
                case 34: {
                    this.addToken(this.start, this.zzStartRead + 1, 2);
                    this.yybegin(this.cssPrevState);
                }
                case 47: {
                    continue;
                }
                case 9: {
                    this.start = this.zzMarkedPos - 1;
                    this.cssPrevState = this.zzLexicalState;
                    this.yybegin(3);
                }
                case 48: {
                    continue;
                }
                case 14: {
                    this.addToken(23);
                    this.yybegin(2);
                }
                case 49: {
                    continue;
                }
                case 4: {
                    this.addToken(16);
                }
                case 50: {
                    continue;
                }
                case 20: {
                    this.addToken(23);
                    this.yybegin(1);
                }
                case 51: {
                    continue;
                }
                case 25:
                case 52: {
                    continue;
                }
                case 31: {
                    this.addToken(18);
                }
                case 53: {
                    continue;
                }
                case 16: {
                    this.addEndToken(-1);
                    return this.firstToken;
                }
                case 54: {
                    continue;
                }
                case 21: {
                    final int temp = this.zzMarkedPos - 2;
                    this.addToken(this.zzStartRead, temp, 8);
                    this.addToken(this.zzMarkedPos - 1, this.zzMarkedPos - 1, 22);
                    final int loc_1 = this.zzMarkedPos;
                    this.zzCurrentPos = loc_1;
                    this.zzStartRead = loc_1;
                }
                case 55: {
                    continue;
                }
                case 6: {
                    this.addToken(21);
                }
                case 56: {
                    continue;
                }
                case 17: {
                    this.addToken(22);
                    this.yybegin(0);
                }
                case 57: {
                    continue;
                }
                case 3: {
                    this.addToken(16);
                }
                case 58: {
                    continue;
                }
                case 15: {
                    this.addToken(22);
                }
                case 59: {
                    continue;
                }
                case 19: {
                    this.addToken(10);
                }
                case 60: {
                    continue;
                }
                case 27: {
                    this.addToken(this.start, this.zzStartRead - 1, 13);
                    this.addEndToken(-2048 - this.cssPrevState);
                    return this.firstToken;
                }
                case 61: {
                    continue;
                }
                case 30: {
                    this.addToken(this.start, this.zzStartRead - 1, 2);
                    this.addEndToken(-6144 - this.cssPrevState);
                    return this.firstToken;
                }
                case 62: {
                    continue;
                }
                case 23: {
                    this.addToken(22);
                }
                case 63: {
                    continue;
                }
                case 12: {
                    this.addToken(20);
                }
                case 64: {
                    continue;
                }
                case 13: {
                    this.addToken(6);
                }
                case 65: {
                    continue;
                }
                case 35: {
                    final int temp = this.zzStartRead;
                    this.addToken(this.start, this.zzStartRead - 1, 2);
                    this.addHyperlinkToken(temp, this.zzMarkedPos - 1, 2);
                    this.start = this.zzMarkedPos;
                }
                case 66: {
                    continue;
                }
                case 10: {
                    this.start = this.zzMarkedPos - 1;
                    this.cssPrevState = this.zzLexicalState;
                    this.yybegin(4);
                }
                case 67: {
                    continue;
                }
                case 5: {
                    this.addToken(22);
                }
                case 68: {
                    continue;
                }
                case 11: {
                    this.addNullToken();
                    return this.firstToken;
                }
                case 69: {
                    continue;
                }
                case 7: {
                    this.addToken(23);
                }
                case 70: {
                    continue;
                }
                case 18: {
                    this.addToken(20);
                }
                case 71: {
                    continue;
                }
                case 24:
                case 72: {
                    continue;
                }
                default: {
                    if (zzInput != -1 || this.zzStartRead != this.zzCurrentPos) {
                        this.zzScanError(1);
                        continue;
                    }
                    this.zzAtEOF = true;
                    switch (this.zzLexicalState) {
                        case 5: {
                            this.addToken(this.start, this.zzStartRead - 1, 2);
                            this.addEndToken(-6144 - this.cssPrevState);
                            return this.firstToken;
                        }
                        case 169: {
                            continue;
                        }
                        case 0: {
                            this.addNullToken();
                            return this.firstToken;
                        }
                        case 170: {
                            continue;
                        }
                        case 3: {
                            this.addToken(this.start, this.zzStartRead - 1, 13);
                            this.addEndToken(-2048 - this.cssPrevState);
                            return this.firstToken;
                        }
                        case 171: {
                            continue;
                        }
                        case 2: {
                            this.addEndToken(-2);
                            return this.firstToken;
                        }
                        case 172: {
                            continue;
                        }
                        case 1: {
                            this.addEndToken(-1);
                            return this.firstToken;
                        }
                        case 173: {
                            continue;
                        }
                        case 4: {
                            this.addToken(this.start, this.zzStartRead - 1, 14);
                            this.addEndToken(-4096 - this.cssPrevState);
                            return this.firstToken;
                        }
                        case 174: {
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
        ZZ_CMAP = zzUnpackCMap("\t\u0000\u0001 \u00011\u0015\u0000\u0001 \u00013\u0001.\u0001\u001e\u0001*\u0001%\u0001'\u0001/\u0001#\u00014\u0001\u0005\u0001)\u0001-\u0001\u0004\u0001\u0006\u0001!\n\u0001\u0001\u0007\u0001\u001f\u0001\u0000\u0001)\u00010\u0001'\u0001\u001d\u0006&\u0014\u0002\u0001(\u0001\"\u0001(\u00010\u0001\u0003\u0001\u0000\u0001\u0011\u0001\u001c\u0001\r\u0001\u0010\u0001\u0016\u0001\u0013\u0001\u001b\u0001\f\u0001\u000e\u0001\u0002\u0001\u0018\u0001\u000f\u0001\u0017\u0001\u000b\u0001\t\u0001\u0015\u0001\u0002\u0001\b\u0001\u0012\u0001\n\u0001\u001a\u0001\u0019\u0001+\u0001$\u0001\u0014\u0001\u0002\u0001,\u00010\u00012\u0001)\uff81\u0000");
        ZZ_ACTION = zzUnpackAction();
        ZZ_ROWMAP = zzUnpackRowMap();
        ZZ_TRANS = zzUnpackTrans();
        ZZ_ERROR_MSG = new String[] { "Unkown internal scanner error", "Error: could not match input", "Error: pushback value was too large" };
        ZZ_ATTRIBUTE = zzUnpackAttribute();
    }
}
