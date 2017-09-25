package org.fife.io;

import java.io.*;

public class UnicodeReader extends Reader
{
    private InputStreamReader internalIn;
    private String encoding;
    private static final int BOM_SIZE = 4;
    
    public UnicodeReader(final String file) throws IOException, FileNotFoundException, SecurityException {
        this(new File(file));
    }
    
    public UnicodeReader(final File file) throws IOException, FileNotFoundException, SecurityException {
        this(new FileInputStream(file));
    }
    
    public UnicodeReader(final File file, final String defaultEncoding) throws IOException, FileNotFoundException, SecurityException {
        this(new FileInputStream(file), defaultEncoding);
    }
    
    public UnicodeReader(final InputStream in) throws IOException {
        this(in, null);
    }
    
    public UnicodeReader(final InputStream in, final String defaultEncoding) throws IOException {
        super();
        this.internalIn = null;
        this.init(in, defaultEncoding);
    }
    
    public void close() throws IOException {
        this.internalIn.close();
    }
    
    public String getEncoding() {
        return this.encoding;
    }
    
    protected void init(final InputStream in, final String defaultEncoding) throws IOException {
        final PushbackInputStream tempIn = new PushbackInputStream(in, 4);
        final byte[] bom = new byte[4];
        final int n = tempIn.read(bom, 0, bom.length);
        int unread;
        if (bom[0] == 0 && bom[1] == 0 && bom[2] == -2 && bom[3] == -1) {
            this.encoding = "UTF-32BE";
            unread = n - 4;
        }
        else if (n == 4 && bom[0] == -1 && bom[1] == -2 && bom[2] == 0 && bom[3] == 0) {
            this.encoding = "UTF-32LE";
            unread = n - 4;
        }
        else if (bom[0] == -17 && bom[1] == -69 && bom[2] == -65) {
            this.encoding = "UTF-8";
            unread = n - 3;
        }
        else if (bom[0] == -2 && bom[1] == -1) {
            this.encoding = "UTF-16BE";
            unread = n - 2;
        }
        else if (bom[0] == -1 && bom[1] == -2) {
            this.encoding = "UTF-16LE";
            unread = n - 2;
        }
        else {
            this.encoding = defaultEncoding;
            unread = n;
        }
        if (unread > 0) {
            tempIn.unread(bom, n - unread, unread);
        }
        else if (unread < -1) {
            tempIn.unread(bom, 0, 0);
        }
        if (this.encoding == null) {
            this.internalIn = new InputStreamReader(tempIn);
            this.encoding = this.internalIn.getEncoding();
        }
        else {
            this.internalIn = new InputStreamReader(tempIn, this.encoding);
        }
    }
    
    public int read(final char[] cbuf, final int off, final int len) throws IOException {
        return this.internalIn.read(cbuf, off, len);
    }
}
