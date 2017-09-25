package us.deathmarine.luyten;

import java.util.*;
import java.net.*;
import java.awt.datatransfer.*;
import java.io.*;
import java.awt.dnd.*;

public class DropListener implements DropTargetListener
{
    private MainWindow mainWindow;
    
    public DropListener(final MainWindow mainWindow) {
        super();
        this.mainWindow = mainWindow;
    }
    
    @Override
    public void drop(final DropTargetDropEvent event) {
        event.acceptDrop(1);
        final Transferable transferable = event.getTransferable();
        if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            final DataFlavor[] flavors = transferable.getTransferDataFlavors();
            DataFlavor[] loc_1;
            for (int loc_0 = (loc_1 = flavors).length, loc_2 = 0; loc_2 < loc_0; ++loc_2) {
                final DataFlavor flavor = loc_1[loc_2];
                try {
                    if (flavor.isFlavorJavaFileListType()) {
                        final List<File> files = (List<File>)transferable.getTransferData(flavor);
                        if (files.size() > 1) {
                            event.rejectDrop();
                            return;
                        }
                        if (files.size() == 1) {
                            this.mainWindow.onFileDropped(files.get(0));
                        }
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            event.dropComplete(true);
        }
        else {
            final DataFlavor[] flavors = transferable.getTransferDataFlavors();
            boolean handled = false;
            for (int zz = 0; zz < flavors.length; ++zz) {
                if (flavors[zz].isRepresentationClassReader()) {
                    try {
                        final Reader reader = flavors[zz].getReaderForText(transferable);
                        final BufferedReader br = new BufferedReader(reader);
                        final List<File> list = new ArrayList<File>();
                        String line = null;
                        while ((line = br.readLine()) != null) {
                            try {
                                if (new String("\u0000").equals(line)) {
                                    continue;
                                }
                                final File file = new File(new URI(line));
                                list.add(file);
                            }
                            catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                        if (list.size() > 1) {
                            event.rejectDrop();
                            return;
                        }
                        if (list.size() == 1) {
                            this.mainWindow.onFileDropped(list.get(0));
                        }
                        event.getDropTargetContext().dropComplete(true);
                        handled = true;
                    }
                    catch (Exception e2) {
                        e2.printStackTrace();
                    }
                    break;
                }
            }
            if (!handled) {
                event.rejectDrop();
            }
        }
    }
    
    @Override
    public void dragEnter(final DropTargetDragEvent arg0) {
    }
    
    @Override
    public void dragExit(final DropTargetEvent arg0) {
    }
    
    @Override
    public void dragOver(final DropTargetDragEvent arg0) {
    }
    
    @Override
    public void dropActionChanged(final DropTargetDragEvent arg0) {
    }
}
