package org.fife.ui.rtextarea;

import javax.swing.text.*;

class RDocumentCharSequence implements CharSequence
{
    private RDocument doc;
    private int start;
    private int end;
    
    public RDocumentCharSequence(final RDocument doc, final int start) {
        this(doc, start, doc.getLength());
    }
    
    public RDocumentCharSequence(final RDocument doc, final int start, final int end) {
        super();
        this.doc = doc;
        this.start = start;
        this.end = end;
    }
    
    public char charAt(final int index) {
        if (index < 0 || index >= this.length()) {
            throw new IndexOutOfBoundsException("Index " + index + " is not in range [0-" + this.length() + ")");
        }
        try {
            return this.doc.charAt(this.start + index);
        }
        catch (BadLocationException ble) {
            throw new IndexOutOfBoundsException(ble.toString());
        }
    }
    
    public int length() {
        return this.end - this.start;
    }
    
    public CharSequence subSequence(final int start, final int end) {
        if (start < 0) {
            throw new IndexOutOfBoundsException("start must be >= 0 (" + start + ")");
        }
        if (end < 0) {
            throw new IndexOutOfBoundsException("end must be >= 0 (" + end + ")");
        }
        if (end > this.length()) {
            throw new IndexOutOfBoundsException("end must be <= " + this.length() + " (" + end + ")");
        }
        if (start > end) {
            throw new IndexOutOfBoundsException("start (" + start + ") cannot be > end (" + end + ")");
        }
        final int newStart = this.start + start;
        final int newEnd = this.start + end;
        return new RDocumentCharSequence(this.doc, newStart, newEnd);
    }
    
    public String toString() {
        try {
            return this.doc.getText(this.start, this.length());
        }
        catch (BadLocationException ble) {
            ble.printStackTrace();
            return "";
        }
    }
}
