package org.fife.io;

import java.io.*;

public class UnicodeWriter extends Writer
{
    public static final String PROPERTY_WRITE_UTF8_BOM = "UnicodeWriter.writeUtf8BOM";
    private OutputStreamWriter internalOut;
    private static final byte[] UTF8_BOM;
    private static final byte[] UTF16LE_BOM;
    private static final byte[] UTF16BE_BOM;
    private static final byte[] UTF32LE_BOM;
    private static final byte[] UTF32BE_BOM;
    
    public UnicodeWriter(final String fileName, final String encoding) throws UnsupportedEncodingException, IOException {
        this(new FileOutputStream(fileName), encoding);
    }
    
    public UnicodeWriter(final File file, final String encoding) throws UnsupportedEncodingException, IOException {
        this(new FileOutputStream(file), encoding);
    }
    
    public UnicodeWriter(final OutputStream out, final String encoding) throws UnsupportedEncodingException, IOException {
        super();
        this.init(out, encoding);
    }
    
    public void close() throws IOException {
        this.internalOut.close();
    }
    
    public void flush() throws IOException {
        this.internalOut.flush();
    }
    
    public String getEncoding() {
        return this.internalOut.getEncoding();
    }
    
    public static boolean getWriteUtf8BOM() {
        final String prop = System.getProperty("UnicodeWriter.writeUtf8BOM");
        return prop == null || !Boolean.valueOf(prop).equals(Boolean.FALSE);
    }
    
    private void init(final OutputStream out, final String encoding) throws UnsupportedEncodingException, IOException {
        this.internalOut = new OutputStreamWriter(out, encoding);
        if ("UTF-8".equals(encoding)) {
            if (getWriteUtf8BOM()) {
                out.write(UnicodeWriter.UTF8_BOM, 0, UnicodeWriter.UTF8_BOM.length);
            }
        }
        else if ("UTF-16LE".equals(encoding)) {
            out.write(UnicodeWriter.UTF16LE_BOM, 0, UnicodeWriter.UTF16LE_BOM.length);
        }
        else if ("UTF-16BE".equals(encoding)) {
            out.write(UnicodeWriter.UTF16BE_BOM, 0, UnicodeWriter.UTF16BE_BOM.length);
        }
        else if ("UTF-32LE".equals(encoding)) {
            out.write(UnicodeWriter.UTF32LE_BOM, 0, UnicodeWriter.UTF32LE_BOM.length);
        }
        else if ("UTF-32".equals(encoding) || "UTF-32BE".equals(encoding)) {
            out.write(UnicodeWriter.UTF32BE_BOM, 0, UnicodeWriter.UTF32BE_BOM.length);
        }
    }
    
    public static void setWriteUtf8BOM(final boolean write) {
        System.setProperty("UnicodeWriter.writeUtf8BOM", Boolean.toString(write));
    }
    
    public void write(final char[] cbuf, final int off, final int len) throws IOException {
        this.internalOut.write(cbuf, off, len);
    }
    
    public void write(final int c) throws IOException {
        this.internalOut.write(c);
    }
    
    public void write(final String str, final int off, final int len) throws IOException {
        this.internalOut.write(str, off, len);
    }
    
    static {
        UTF8_BOM = new byte[] { -17, -69, -65 };
        UTF16LE_BOM = new byte[] { -1, -2 };
        UTF16BE_BOM = new byte[] { -2, -1 };
        UTF32LE_BOM = new byte[] { -1, -2, 0, 0 };
        UTF32BE_BOM = new byte[] { 0, 0, -2, -1 };
    }
}
