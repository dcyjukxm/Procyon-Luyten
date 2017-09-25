package org.fife.ui.rsyntaxtextarea;

import javax.swing.event.*;
import javax.swing.text.*;
import org.fife.io.*;
import java.io.*;
import java.nio.charset.*;

public class TextEditorPane extends RSyntaxTextArea implements DocumentListener
{
    private static final long serialVersionUID = 1L;
    public static final String FULL_PATH_PROPERTY = "TextEditorPane.fileFullPath";
    public static final String DIRTY_PROPERTY = "TextEditorPane.dirty";
    public static final String READ_ONLY_PROPERTY = "TextEditorPane.readOnly";
    private FileLocation loc;
    private String charSet;
    private boolean readOnly;
    private boolean dirty;
    private long lastSaveOrLoadTime;
    public static final long LAST_MODIFIED_UNKNOWN = 0L;
    private static final String DEFAULT_FILE_NAME = "Untitled.txt";
    
    public TextEditorPane() {
        this(0);
    }
    
    public TextEditorPane(final int textMode) {
        this(textMode, false);
    }
    
    public TextEditorPane(final int textMode, final boolean wordWrapEnabled) {
        super(textMode);
        this.setLineWrap(wordWrapEnabled);
        try {
            this.init(null, null);
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
    
    public TextEditorPane(final int textMode, final boolean wordWrapEnabled, final FileLocation loc) throws IOException {
        this(textMode, wordWrapEnabled, loc, null);
    }
    
    public TextEditorPane(final int textMode, final boolean wordWrapEnabled, final FileLocation loc, final String defaultEnc) throws IOException {
        super(textMode);
        this.setLineWrap(wordWrapEnabled);
        this.init(loc, defaultEnc);
    }
    
    public void changedUpdate(final DocumentEvent e) {
    }
    
    private static final String getDefaultEncoding() {
        String encoding = Charset.defaultCharset().name();
        if (encoding == null) {
            try {
                final File f = File.createTempFile("rsta", null);
                final FileWriter w = new FileWriter(f);
                encoding = w.getEncoding();
                w.close();
                f.deleteOnExit();
            }
            catch (IOException ioe) {
                encoding = "US-ASCII";
            }
        }
        return encoding;
    }
    
    public String getEncoding() {
        return this.charSet;
    }
    
    public String getFileFullPath() {
        return (this.loc == null) ? null : this.loc.getFileFullPath();
    }
    
    public String getFileName() {
        return (this.loc == null) ? null : this.loc.getFileName();
    }
    
    public long getLastSaveOrLoadTime() {
        return this.lastSaveOrLoadTime;
    }
    
    public Object getLineSeparator() {
        return this.getDocument().getProperty("__EndOfLine__");
    }
    
    private void init(final FileLocation loc, final String defaultEnc) throws IOException {
        if (loc == null) {
            this.loc = FileLocation.create("Untitled.txt");
            this.charSet = ((defaultEnc == null) ? getDefaultEncoding() : defaultEnc);
            this.setLineSeparator(System.getProperty("line.separator"));
        }
        else {
            this.load(loc, defaultEnc);
        }
        if (this.loc.isLocalAndExists()) {
            final File file = new File(this.loc.getFileFullPath());
            this.lastSaveOrLoadTime = file.lastModified();
            this.setReadOnly(!file.canWrite());
        }
        else {
            this.lastSaveOrLoadTime = 0L;
            this.setReadOnly(false);
        }
        this.setDirty(false);
    }
    
    public void insertUpdate(final DocumentEvent e) {
        if (!this.dirty) {
            this.setDirty(true);
        }
    }
    
    public boolean isDirty() {
        return this.dirty;
    }
    
    public boolean isLocal() {
        return this.loc.isLocal();
    }
    
    public boolean isLocalAndExists() {
        return this.loc.isLocalAndExists();
    }
    
    public boolean isModifiedOutsideEditor() {
        return this.loc.getActualLastModified() > this.getLastSaveOrLoadTime();
    }
    
    public boolean isReadOnly() {
        return this.readOnly;
    }
    
    public void load(final FileLocation loc, final String defaultEnc) throws IOException {
        if (loc.isLocal() && !loc.isLocalAndExists()) {
            this.charSet = ((defaultEnc != null) ? defaultEnc : getDefaultEncoding());
            this.loc = loc;
            this.setText(null);
            this.discardAllEdits();
            this.setDirty(false);
            return;
        }
        final UnicodeReader ur = new UnicodeReader(loc.getInputStream(), defaultEnc);
        final Document doc = this.getDocument();
        doc.removeDocumentListener(this);
        final BufferedReader r = new BufferedReader(ur);
        try {
            this.read(r, null);
        }
        finally {
            doc.addDocumentListener(this);
            r.close();
        }
        this.charSet = ur.getEncoding();
        final String old = this.getFileFullPath();
        this.loc = loc;
        this.setDirty(false);
        this.setCaretPosition(0);
        this.firePropertyChange("TextEditorPane.fileFullPath", old, this.getFileFullPath());
    }
    
    public void reload() throws IOException {
        final String oldEncoding = this.getEncoding();
        final UnicodeReader ur = new UnicodeReader(this.loc.getInputStream(), oldEncoding);
        final String encoding = ur.getEncoding();
        final BufferedReader r = new BufferedReader(ur);
        try {
            this.read(r, null);
        }
        finally {
            r.close();
        }
        this.setEncoding(encoding);
        this.setDirty(false);
        this.syncLastSaveOrLoadTimeToActualFile();
        this.discardAllEdits();
    }
    
    public void removeUpdate(final DocumentEvent e) {
        if (!this.dirty) {
            this.setDirty(true);
        }
    }
    
    public void save() throws IOException {
        this.saveImpl(this.loc);
        this.setDirty(false);
        this.syncLastSaveOrLoadTimeToActualFile();
    }
    
    public void saveAs(final FileLocation loc) throws IOException {
        this.saveImpl(loc);
        final String old = this.getFileFullPath();
        this.loc = loc;
        this.setDirty(false);
        this.lastSaveOrLoadTime = loc.getActualLastModified();
        this.firePropertyChange("TextEditorPane.fileFullPath", old, this.getFileFullPath());
    }
    
    private void saveImpl(final FileLocation loc) throws IOException {
        final OutputStream out = loc.getOutputStream();
        final BufferedWriter w = new BufferedWriter(new UnicodeWriter(out, this.getEncoding()));
        try {
            this.write(w);
        }
        finally {
            w.close();
        }
    }
    
    public void setDirty(final boolean dirty) {
        if (this.dirty != dirty) {
            this.dirty = dirty;
            this.firePropertyChange("TextEditorPane.dirty", !dirty, dirty);
        }
    }
    
    public void setDocument(final Document doc) {
        final Document old = this.getDocument();
        if (old != null) {
            old.removeDocumentListener(this);
        }
        super.setDocument(doc);
        doc.addDocumentListener(this);
    }
    
    public void setEncoding(final String encoding) {
        if (encoding == null) {
            throw new NullPointerException("encoding cannot be null");
        }
        if (!Charset.isSupported(encoding)) {
            throw new UnsupportedCharsetException(encoding);
        }
        if (this.charSet == null || !this.charSet.equals(encoding)) {
            this.charSet = encoding;
            this.setDirty(true);
        }
    }
    
    public void setLineSeparator(final String separator) {
        this.setLineSeparator(separator, true);
    }
    
    public void setLineSeparator(final String separator, final boolean setDirty) {
        if (separator == null) {
            throw new NullPointerException("terminator cannot be null");
        }
        if (!"\r\n".equals(separator) && !"\n".equals(separator) && !"\r".equals(separator)) {
            throw new IllegalArgumentException("Invalid line terminator");
        }
        final Document doc = this.getDocument();
        final Object old = doc.getProperty("__EndOfLine__");
        if (!separator.equals(old)) {
            doc.putProperty("__EndOfLine__", separator);
            if (setDirty) {
                this.setDirty(true);
            }
        }
    }
    
    public void setReadOnly(final boolean readOnly) {
        if (this.readOnly != readOnly) {
            this.readOnly = readOnly;
            this.firePropertyChange("TextEditorPane.readOnly", !readOnly, readOnly);
        }
    }
    
    public void syncLastSaveOrLoadTimeToActualFile() {
        if (this.loc.isLocalAndExists()) {
            this.lastSaveOrLoadTime = this.loc.getActualLastModified();
        }
    }
}
