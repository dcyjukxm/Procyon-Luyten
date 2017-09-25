package org.fife.ui.rsyntaxtextarea;

import java.io.*;

class RtfToText
{
    private Reader r;
    private StringBuilder sb;
    private StringBuilder controlWord;
    private int blockCount;
    private boolean inControlWord;
    
    private RtfToText(final Reader r) {
        super();
        this.r = r;
        this.sb = new StringBuilder();
        this.controlWord = new StringBuilder();
        this.blockCount = 0;
        this.inControlWord = false;
    }
    
    private String convert() throws IOException {
        int i = this.r.read();
        if (i != 123) {
            throw new IOException("Invalid RTF file");
        }
        while ((i = this.r.read()) != -1) {
            final char ch = (char)i;
            switch (ch) {
                case '{': {
                    if (this.inControlWord && this.controlWord.length() == 0) {
                        this.sb.append('{');
                        this.controlWord.setLength(0);
                        this.inControlWord = false;
                        continue;
                    }
                    ++this.blockCount;
                    continue;
                }
                case '}': {
                    if (this.inControlWord && this.controlWord.length() == 0) {
                        this.sb.append('}');
                        this.controlWord.setLength(0);
                        this.inControlWord = false;
                        continue;
                    }
                    --this.blockCount;
                    continue;
                }
                case '\\': {
                    if (this.blockCount != 0) {
                        continue;
                    }
                    if (!this.inControlWord) {
                        this.inControlWord = true;
                        continue;
                    }
                    if (this.controlWord.length() == 0) {
                        this.sb.append('\\');
                        this.controlWord.setLength(0);
                        this.inControlWord = false;
                        continue;
                    }
                    this.endControlWord();
                    this.inControlWord = true;
                    continue;
                }
                case ' ': {
                    if (this.blockCount != 0) {
                        continue;
                    }
                    if (this.inControlWord) {
                        this.endControlWord();
                        continue;
                    }
                    this.sb.append(' ');
                    continue;
                }
                case '\n':
                case '\r': {
                    if (this.blockCount == 0 && this.inControlWord) {
                        this.endControlWord();
                        continue;
                    }
                    continue;
                }
                default: {
                    if (this.blockCount != 0) {
                        continue;
                    }
                    if (this.inControlWord) {
                        this.controlWord.append(ch);
                        continue;
                    }
                    this.sb.append(ch);
                    continue;
                }
            }
        }
        return this.sb.toString();
    }
    
    private void endControlWord() {
        final String word = this.controlWord.toString();
        if ("par".equals(word)) {
            this.sb.append('\n');
        }
        else if ("tab".equals(word)) {
            this.sb.append('\t');
        }
        this.controlWord.setLength(0);
        this.inControlWord = false;
    }
    
    public static String getPlainText(final byte[] rtf) throws IOException {
        return getPlainText(new ByteArrayInputStream(rtf));
    }
    
    public static String getPlainText(final File file) throws IOException {
        return getPlainText(new BufferedReader(new FileReader(file)));
    }
    
    public static String getPlainText(final InputStream in) throws IOException {
        return getPlainText(new InputStreamReader(in, "US-ASCII"));
    }
    
    private static String getPlainText(final Reader r) throws IOException {
        try {
            final RtfToText converter = new RtfToText(r);
            return converter.convert();
        }
        finally {
            r.close();
        }
    }
    
    public static String getPlainText(final String rtf) throws IOException {
        return getPlainText(new StringReader(rtf));
    }
}
