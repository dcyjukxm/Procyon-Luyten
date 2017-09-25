package org.fife.ui.rtextarea;

import javax.swing.*;
import java.awt.datatransfer.*;
import java.awt.im.*;
import javax.swing.text.*;
import java.io.*;

public class RTATextTransferHandler extends TransferHandler
{
    private JTextComponent exportComp;
    private boolean shouldRemove;
    private int p0;
    private int p1;
    private boolean withinSameComponent;
    
    protected DataFlavor getImportFlavor(final DataFlavor[] flavors, final JTextComponent c) {
        DataFlavor refFlavor = null;
        DataFlavor stringFlavor = null;
        for (int i = 0; i < flavors.length; ++i) {
            final String mime = flavors[i].getMimeType();
            if (mime.startsWith("text/plain")) {
                return flavors[i];
            }
            if (refFlavor == null && mime.startsWith("application/x-java-jvm-local-objectref") && flavors[i].getRepresentationClass() == String.class) {
                refFlavor = flavors[i];
            }
            else if (stringFlavor == null && flavors[i].equals(DataFlavor.stringFlavor)) {
                stringFlavor = flavors[i];
            }
        }
        if (refFlavor != null) {
            return refFlavor;
        }
        if (stringFlavor != null) {
            return stringFlavor;
        }
        return null;
    }
    
    protected void handleReaderImport(final Reader in, final JTextComponent c) throws BadLocationException, IOException {
        final char[] buff = new char[1024];
        boolean lastWasCR = false;
        StringBuilder sbuff = null;
        int nch;
        while ((nch = in.read(buff, 0, buff.length)) != -1) {
            if (sbuff == null) {
                sbuff = new StringBuilder(nch);
            }
            int last = 0;
            for (int counter = 0; counter < nch; ++counter) {
                switch (buff[counter]) {
                    case '\r': {
                        if (!lastWasCR) {
                            lastWasCR = true;
                            break;
                        }
                        if (counter == 0) {
                            sbuff.append('\n');
                            break;
                        }
                        buff[counter - 1] = '\n';
                        break;
                    }
                    case '\n': {
                        if (lastWasCR) {
                            if (counter > last + 1) {
                                sbuff.append(buff, last, counter - last - 1);
                            }
                            lastWasCR = false;
                            last = counter;
                            break;
                        }
                        break;
                    }
                    default: {
                        if (lastWasCR) {
                            if (counter == 0) {
                                sbuff.append('\n');
                            }
                            else {
                                buff[counter - 1] = '\n';
                            }
                            lastWasCR = false;
                            break;
                        }
                        break;
                    }
                }
            }
            if (last < nch) {
                if (lastWasCR) {
                    if (last >= nch - 1) {
                        continue;
                    }
                    sbuff.append(buff, last, nch - last - 1);
                }
                else {
                    sbuff.append(buff, last, nch - last);
                }
            }
        }
        if (this.withinSameComponent) {
            ((RTextArea)c).beginAtomicEdit();
        }
        if (lastWasCR) {
            sbuff.append('\n');
        }
        c.replaceSelection((sbuff != null) ? sbuff.toString() : "");
    }
    
    public int getSourceActions(final JComponent c) {
        if (((JTextComponent)c).isEditable()) {
            return 3;
        }
        return 1;
    }
    
    protected Transferable createTransferable(final JComponent comp) {
        this.exportComp = (JTextComponent)comp;
        this.shouldRemove = true;
        this.p0 = this.exportComp.getSelectionStart();
        this.p1 = this.exportComp.getSelectionEnd();
        return (this.p0 != this.p1) ? new TextTransferable(this.exportComp, this.p0, this.p1) : null;
    }
    
    protected void exportDone(final JComponent source, final Transferable data, final int action) {
        if (this.shouldRemove && action == 2) {
            final TextTransferable t = (TextTransferable)data;
            t.removeText();
            if (this.withinSameComponent) {
                ((RTextArea)source).endAtomicEdit();
                this.withinSameComponent = false;
            }
        }
        this.exportComp = null;
    }
    
    public boolean importData(final JComponent comp, final Transferable t) {
        final JTextComponent c = (JTextComponent)comp;
        this.withinSameComponent = (c == this.exportComp);
        if (this.withinSameComponent && c.getCaretPosition() >= this.p0 && c.getCaretPosition() <= this.p1) {
            this.shouldRemove = false;
            return true;
        }
        boolean imported = false;
        final DataFlavor importFlavor = this.getImportFlavor(t.getTransferDataFlavors(), c);
        if (importFlavor != null) {
            try {
                final InputContext ic = c.getInputContext();
                if (ic != null) {
                    ic.endComposition();
                }
                final Reader r = importFlavor.getReaderForText(t);
                this.handleReaderImport(r, c);
                imported = true;
            }
            catch (UnsupportedFlavorException ufe) {
                ufe.printStackTrace();
            }
            catch (BadLocationException ble) {
                ble.printStackTrace();
            }
            catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
        return imported;
    }
    
    public boolean canImport(final JComponent comp, final DataFlavor[] flavors) {
        final JTextComponent c = (JTextComponent)comp;
        return c.isEditable() && c.isEnabled() && this.getImportFlavor(flavors, c) != null;
    }
    
    static class TextTransferable implements Transferable
    {
        Position p0;
        Position p1;
        JTextComponent c;
        protected String plainData;
        private static DataFlavor[] stringFlavors;
        private static DataFlavor[] plainFlavors;
        
        TextTransferable(final JTextComponent c, final int start, final int end) {
            super();
            this.c = c;
            final Document doc = c.getDocument();
            try {
                this.p0 = doc.createPosition(start);
                this.p1 = doc.createPosition(end);
                this.plainData = c.getSelectedText();
            }
            catch (BadLocationException loc_0) {}
        }
        
        protected String getPlainData() {
            return this.plainData;
        }
        
        public Object getTransferData(final DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (this.isPlainFlavor(flavor)) {
                String data = this.getPlainData();
                data = ((data == null) ? "" : data);
                if (String.class.equals(flavor.getRepresentationClass())) {
                    return data;
                }
                if (Reader.class.equals(flavor.getRepresentationClass())) {
                    return new StringReader(data);
                }
                if (InputStream.class.equals(flavor.getRepresentationClass())) {
                    return new StringBufferInputStream(data);
                }
            }
            else if (this.isStringFlavor(flavor)) {
                String data = this.getPlainData();
                data = ((data == null) ? "" : data);
                return data;
            }
            throw new UnsupportedFlavorException(flavor);
        }
        
        public DataFlavor[] getTransferDataFlavors() {
            final int plainCount = this.isPlainSupported() ? TextTransferable.plainFlavors.length : 0;
            final int stringCount = this.isPlainSupported() ? TextTransferable.stringFlavors.length : 0;
            final int totalCount = plainCount + stringCount;
            final DataFlavor[] flavors = new DataFlavor[totalCount];
            int pos = 0;
            if (plainCount > 0) {
                System.arraycopy(TextTransferable.plainFlavors, 0, flavors, pos, plainCount);
                pos += plainCount;
            }
            if (stringCount > 0) {
                System.arraycopy(TextTransferable.stringFlavors, 0, flavors, pos, stringCount);
            }
            return flavors;
        }
        
        public boolean isDataFlavorSupported(final DataFlavor flavor) {
            final DataFlavor[] flavors = this.getTransferDataFlavors();
            for (int i = 0; i < flavors.length; ++i) {
                if (flavors[i].equals(flavor)) {
                    return true;
                }
            }
            return false;
        }
        
        protected boolean isPlainFlavor(final DataFlavor flavor) {
            final DataFlavor[] flavors = TextTransferable.plainFlavors;
            for (int i = 0; i < flavors.length; ++i) {
                if (flavors[i].equals(flavor)) {
                    return true;
                }
            }
            return false;
        }
        
        protected boolean isPlainSupported() {
            return this.plainData != null;
        }
        
        protected boolean isStringFlavor(final DataFlavor flavor) {
            final DataFlavor[] flavors = TextTransferable.stringFlavors;
            for (int i = 0; i < flavors.length; ++i) {
                if (flavors[i].equals(flavor)) {
                    return true;
                }
            }
            return false;
        }
        
        void removeText() {
            if (this.p0 != null && this.p1 != null && this.p0.getOffset() != this.p1.getOffset()) {
                try {
                    final Document doc = this.c.getDocument();
                    doc.remove(this.p0.getOffset(), this.p1.getOffset() - this.p0.getOffset());
                }
                catch (BadLocationException loc_0) {}
            }
        }
        
        static {
            try {
                (TextTransferable.plainFlavors = new DataFlavor[3])[0] = new DataFlavor("text/plain;class=java.lang.String");
                TextTransferable.plainFlavors[1] = new DataFlavor("text/plain;class=java.io.Reader");
                TextTransferable.plainFlavors[2] = new DataFlavor("text/plain;charset=unicode;class=java.io.InputStream");
                (TextTransferable.stringFlavors = new DataFlavor[2])[0] = new DataFlavor("application/x-java-jvm-local-objectref;class=java.lang.String");
                TextTransferable.stringFlavors[1] = DataFlavor.stringFlavor;
            }
            catch (ClassNotFoundException cle) {
                System.err.println("Error initializing org.fife.ui.RTATextTransferHandler");
            }
        }
    }
}
