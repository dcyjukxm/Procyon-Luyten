package org.fife.ui.rsyntaxtextarea;

import java.awt.datatransfer.*;
import java.io.*;

class RtfTransferable implements Transferable
{
    private byte[] data;
    private final DataFlavor[] FLAVORS;
    
    public RtfTransferable(final byte[] data) {
        super();
        this.FLAVORS = new DataFlavor[] { new DataFlavor("text/rtf", "RTF"), DataFlavor.stringFlavor, DataFlavor.plainTextFlavor };
        this.data = data;
    }
    
    public Object getTransferData(final DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (flavor.equals(this.FLAVORS[0])) {
            return new ByteArrayInputStream((this.data == null) ? new byte[0] : this.data);
        }
        if (flavor.equals(this.FLAVORS[1])) {
            return (this.data == null) ? "" : RtfToText.getPlainText(this.data);
        }
        if (flavor.equals(this.FLAVORS[2])) {
            String text = "";
            if (this.data != null) {
                text = RtfToText.getPlainText(this.data);
            }
            return new StringReader(text);
        }
        throw new UnsupportedFlavorException(flavor);
    }
    
    public DataFlavor[] getTransferDataFlavors() {
        return this.FLAVORS.clone();
    }
    
    public boolean isDataFlavorSupported(final DataFlavor flavor) {
        for (int i = 0; i < this.FLAVORS.length; ++i) {
            if (flavor.equals(this.FLAVORS[i])) {
                return true;
            }
        }
        return false;
    }
}
