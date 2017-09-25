package us.deathmarine.luyten;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.jar.*;
import com.strobel.core.*;
import com.strobel.decompiler.*;
import java.io.*;
import java.util.*;
import com.strobel.assembler.metadata.*;

public class FindAllBox extends JDialog
{
    private static final long serialVersionUID = -4125409760166690462L;
    private boolean cancel;
    private boolean searching;
    private JButton findButton;
    private JTextField textField;
    JProgressBar progressBar;
    private DefaultListModel<String> classesList;
    private JLabel statusLabel;
    
    public FindAllBox() {
        super();
        this.classesList = new DefaultListModel<String>();
        this.statusLabel = new JLabel("");
        this.progressBar = new JProgressBar(0, 100);
        final JLabel label = new JLabel("Find What:");
        this.textField = new JTextField();
        (this.findButton = new JButton("Find")).addActionListener(new FindButton((FindButton)null));
        this.getRootPane().setDefaultButton(this.findButton);
        final JList<String> list = new JList<String>(this.classesList);
        list.setSelectionMode(1);
        list.setLayoutOrientation(1);
        list.setVisibleRowCount(-1);
        final JScrollPane listScroller = new JScrollPane(list);
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        final Dimension center = new Dimension((int)(screenSize.width * 0.35), 500);
        final int x = (int)(center.width * 0.2);
        final int y = (int)(center.height * 0.2);
        this.setBounds(x, y, center.width, center.height);
        this.setResizable(false);
        final GroupLayout layout = new GroupLayout(this.getRootPane());
        this.getRootPane().setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        final JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                if (FindAllBox.this.isSearching()) {
                    FindAllBox.this.setCancel(true);
                }
            }
        });
        layout.setHorizontalGroup(layout.createSequentialGroup().addComponent(label).addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(this.statusLabel).addComponent(this.textField).addGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(listScroller).addComponent(this.progressBar)))).addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(this.findButton).addComponent(cancelButton)));
        layout.linkSize(0, this.findButton);
        layout.setVerticalGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(label).addComponent(this.textField).addComponent(this.findButton)).addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(listScroller).addComponent(cancelButton)))).addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)).addComponent(this.statusLabel).addComponent(this.progressBar));
        this.setDefaultCloseOperation(1);
        this.setHideOnEscapeButton();
        this.adjustWindowPositionBySavedState();
        this.setSaveWindowPositionOnClosing();
        this.setModalityType(ModalityType.APPLICATION_MODAL);
        this.setName("Find All");
        this.setTitle("Find All");
    }
    
    private void setHideOnEscapeButton() {
        final Action escapeAction = new AbstractAction() {
            private static final long serialVersionUID = 1L;
            
            @Override
            public void actionPerformed(final ActionEvent e) {
                FindAllBox.this.setVisible(false);
            }
        };
        final KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke(27, 0, false);
        this.getRootPane().getInputMap(2).put(escapeKeyStroke, "ESCAPE");
        this.getRootPane().getActionMap().put("ESCAPE", escapeAction);
    }
    
    private void adjustWindowPositionBySavedState() {
        final WindowPosition windowPosition = ConfigSaver.getLoadedInstance().getFindWindowPosition();
        if (windowPosition.isSavedWindowPositionValid()) {
            this.setLocation(windowPosition.getWindowX(), windowPosition.getWindowY());
        }
    }
    
    private void setSaveWindowPositionOnClosing() {
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowDeactivated(final WindowEvent e) {
                final WindowPosition windowPosition = ConfigSaver.getLoadedInstance().getFindWindowPosition();
                windowPosition.readPositionFromDialog(FindAllBox.this);
                if (FindAllBox.this.isSearching()) {
                    FindAllBox.this.setCancel(true);
                }
            }
        });
    }
    
    public void showFindBox() {
        this.setVisible(true);
        this.textField.requestFocus();
    }
    
    public void hideFindBox() {
        this.setVisible(false);
    }
    
    public void setStatus(final String text) {
        if (text.length() > 25) {
            this.statusLabel.setText("Searching in file: ..." + text.substring(text.length() - 25));
        }
        else {
            this.statusLabel.setText("Searching in file: " + text);
        }
        this.progressBar.setValue(this.progressBar.getValue() + 1);
    }
    
    public void addClassName(final String className) {
        this.classesList.addElement(className);
    }
    
    public void initProgressBar(final Integer length) {
        this.progressBar.setMaximum(length);
        this.progressBar.setValue(0);
        this.progressBar.setStringPainted(true);
    }
    
    public boolean isCancel() {
        return this.cancel;
    }
    
    public void setCancel(final boolean cancel) {
        this.cancel = cancel;
    }
    
    public boolean isSearching() {
        return this.searching;
    }
    
    public void setSearching(final boolean searching) {
        this.searching = searching;
    }
    
    static /* synthetic */ DefaultListModel access$0(final FindAllBox param_0) {
        return param_0.classesList;
    }
    
    static /* synthetic */ JTextField access$1(final FindAllBox param_0) {
        return param_0.textField;
    }
    
    private class FindButton extends AbstractAction
    {
        private static final long serialVersionUID = 75954129199541874L;
        final /* synthetic */ FindAllBox this$0;
        
        @Override
        public void actionPerformed(final ActionEvent event) {
            final Thread tmp_thread = new Thread() {
                @Override
                public void run() {
                    FindButton.access$1(FindButton.this).setSearching(true);
                    FindAllBox.access$0(FindButton.access$1(FindButton.this)).clear();
                    final ConfigSaver configSaver = ConfigSaver.getLoadedInstance();
                    final DecompilerSettings settings = configSaver.getDecompilerSettings();
                    final File inFile = MainWindow.model.getOpenedFile();
                    try {
                        final JarFile jfile = new JarFile(inFile);
                        final Enumeration<JarEntry> entLength = jfile.entries();
                        FindButton.access$1(FindButton.this).initProgressBar(Collections.list(entLength).size());
                        final Enumeration<JarEntry> ent = jfile.entries();
                        while (ent.hasMoreElements() && !FindButton.access$1(FindButton.this).isCancel()) {
                            final JarEntry entry = ent.nextElement();
                            FindButton.access$1(FindButton.this).setStatus(entry.getName());
                            if (entry.getName().endsWith(".class")) {
                                synchronized (settings) {
                                    final String internalName = StringUtilities.removeRight(entry.getName(), ".class");
                                    final TypeReference type = Model.metadataSystem.lookupType(internalName);
                                    TypeDefinition resolvedType = null;
                                    if (type == null || (resolvedType = type.resolve()) == null) {
                                        throw new Exception("Unable to resolve type.");
                                    }
                                    final StringWriter stringwriter = new StringWriter();
                                    final DecompilationOptions decompilationOptions = new DecompilationOptions();
                                    decompilationOptions.setSettings(settings);
                                    decompilationOptions.setFullDecompilation(true);
                                    settings.getLanguage().decompileType(resolvedType, new PlainTextOutput(stringwriter), decompilationOptions);
                                    final String decompiledSource = stringwriter.toString().toLowerCase();
                                    if (decompiledSource.contains(FindAllBox.access$1(FindButton.access$1(FindButton.this)).getText().toLowerCase())) {
                                        FindButton.access$1(FindButton.this).addClassName(entry.getName());
                                    }
                                }
                                // monitorexit(settings)
                            }
                        }
                        FindButton.access$1(FindButton.this).setSearching(false);
                        if (FindButton.access$1(FindButton.this).isCancel()) {
                            FindButton.access$1(FindButton.this).setCancel(false);
                            FindButton.access$1(FindButton.this).setStatus("Cancelled.");
                        }
                        else {
                            FindButton.access$1(FindButton.this).setStatus("Done.");
                        }
                        jfile.close();
                    }
                    catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
            };
            tmp_thread.start();
        }
        
        static /* synthetic */ FindAllBox access$1(final FindButton param_0) {
            return param_0.this$0;
        }
    }
}
