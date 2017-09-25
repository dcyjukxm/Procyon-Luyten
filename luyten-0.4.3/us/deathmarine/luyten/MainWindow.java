package us.deathmarine.luyten;

import javax.swing.border.*;
import java.awt.*;
import java.awt.dnd.*;
import org.fife.ui.rsyntaxtextarea.*;
import java.io.*;
import java.awt.event.*;
import javax.swing.*;

public class MainWindow extends JFrame
{
    private static final long serialVersionUID = 1L;
    private static final String TITLE = "Luyten";
    public static Model model;
    private JProgressBar bar;
    private JLabel label;
    private FindBox findBox;
    private FindAllBox findAllBox;
    private ConfigSaver configSaver;
    private WindowPosition windowPosition;
    private LuytenPreferences luytenPrefs;
    private FileDialog fileDialog;
    private FileSaver fileSaver;
    
    public MainWindow(final File fileFromCommandLine) {
        super();
        this.configSaver = ConfigSaver.getLoadedInstance();
        this.windowPosition = this.configSaver.getMainWindowPosition();
        this.luytenPrefs = this.configSaver.getLuytenPreferences();
        final MainMenuBar mainMenuBar = new MainMenuBar(this);
        this.setJMenuBar(mainMenuBar);
        this.adjustWindowPositionBySavedState();
        this.setHideFindBoxOnMainWindowFocus();
        this.setShowFindAllBoxOnMainWindowFocus();
        this.setQuitOnWindowClosing();
        this.setTitle("Luyten");
        this.setIconImage(new ImageIcon(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/resources/Luyten.png"))).getImage());
        final JPanel panel1 = new JPanel(new FlowLayout(0));
        (this.label = new JLabel()).setHorizontalAlignment(2);
        panel1.setBorder(new BevelBorder(1));
        panel1.setPreferredSize(new Dimension(this.getWidth() / 2, 20));
        panel1.add(this.label);
        final JPanel panel2 = new JPanel(new FlowLayout(2));
        (this.bar = new JProgressBar()).setStringPainted(true);
        this.bar.setOpaque(false);
        this.bar.setVisible(false);
        panel2.setPreferredSize(new Dimension(this.getWidth() / 3, 20));
        panel2.add(this.bar);
        MainWindow.model = new Model(this);
        this.getContentPane().add(MainWindow.model);
        final JSplitPane spt = new JSplitPane(1, panel1, panel2) {
            private static final long serialVersionUID = 2189946972124687305L;
            private final int location = 400;
            
            {
                this.setDividerLocation(this.location);
            }
            
            @Override
            public int getDividerLocation() {
                return 400;
            }
            
            @Override
            public int getLastDividerLocation() {
                return 400;
            }
        };
        spt.setBorder(new BevelBorder(1));
        spt.setPreferredSize(new Dimension(this.getWidth(), 24));
        this.add(spt, "South");
        if (fileFromCommandLine != null) {
            MainWindow.model.loadFile(fileFromCommandLine);
        }
        try {
            final DropTarget dt = new DropTarget();
            dt.addDropTargetListener(new DropListener(this));
            this.setDropTarget(dt);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        this.fileDialog = new FileDialog(this);
        this.fileSaver = new FileSaver(this.bar, this.label);
        this.setExitOnEscWhenEnabled(MainWindow.model);
        if (fileFromCommandLine == null || fileFromCommandLine.getName().toLowerCase().endsWith(".jar") || fileFromCommandLine.getName().toLowerCase().endsWith(".zip")) {
            MainWindow.model.startWarmUpThread();
        }
    }
    
    public void onOpenFileMenu() {
        final File selectedFile = this.fileDialog.doOpenDialog();
        if (selectedFile != null) {
            this.getModel().loadFile(selectedFile);
        }
    }
    
    public void onCloseFileMenu() {
        this.getModel().closeFile();
    }
    
    public void onSaveAsMenu() {
        final RSyntaxTextArea pane = this.getModel().getCurrentTextArea();
        if (pane == null) {
            return;
        }
        final String tabTitle = this.getModel().getCurrentTabTitle();
        if (tabTitle == null) {
            return;
        }
        final String recommendedFileName = tabTitle.replace(".class", ".java");
        final File selectedFile = this.fileDialog.doSaveDialog(recommendedFileName);
        if (selectedFile != null) {
            this.fileSaver.saveText(pane.getText(), selectedFile);
        }
    }
    
    public void onSaveAllMenu() {
        final File openedFile = this.getModel().getOpenedFile();
        if (openedFile == null) {
            return;
        }
        String fileName = openedFile.getName();
        if (fileName.endsWith(".class")) {
            fileName = fileName.replace(".class", ".java");
        }
        else if (fileName.toLowerCase().endsWith(".jar")) {
            fileName = "decompiled-" + fileName.replaceAll("\\.[jJ][aA][rR]", ".zip");
        }
        else {
            fileName = "saved-" + fileName;
        }
        final File selectedFileToSave = this.fileDialog.doSaveAllDialog(fileName);
        if (selectedFileToSave != null) {
            this.fileSaver.saveAllDecompiled(openedFile, selectedFileToSave);
        }
    }
    
    public void onExitMenu() {
        this.quit();
    }
    
    public void onSelectAllMenu() {
        try {
            final RSyntaxTextArea pane = this.getModel().getCurrentTextArea();
            if (pane != null) {
                pane.requestFocusInWindow();
                pane.setSelectionStart(0);
                pane.setSelectionEnd(pane.getText().length());
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void onFindMenu() {
        try {
            final RSyntaxTextArea pane = this.getModel().getCurrentTextArea();
            if (pane != null) {
                if (this.findBox == null) {
                    this.findBox = new FindBox(this);
                }
                this.findBox.showFindBox();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void onFindAllMenu() {
        try {
            if (this.findAllBox == null) {
                this.findAllBox = new FindAllBox();
            }
            this.findAllBox.showFindBox();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void onLegalMenu() {
        new Thread() {
            @Override
            public void run() {
                try {
                    MainWindow.access$0(MainWindow.this).setVisible(true);
                    MainWindow.access$0(MainWindow.this).setIndeterminate(true);
                    final String legalStr = MainWindow.access$1(MainWindow.this);
                    MainWindow.this.getModel().showLegal(legalStr);
                }
                finally {
                    MainWindow.access$0(MainWindow.this).setIndeterminate(false);
                    MainWindow.access$0(MainWindow.this).setVisible(false);
                }
                MainWindow.access$0(MainWindow.this).setIndeterminate(false);
                MainWindow.access$0(MainWindow.this).setVisible(false);
            }
        }.start();
    }
    
    private String getLegalStr() {
        final StringBuilder sb = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("/distfiles/Procyon.License.txt")));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            sb.append("\n\n\n\n\n");
            reader = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("/distfiles/RSyntaxTextArea.License.txt")));
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
    
    public void onThemesChanged() {
        this.getModel().changeTheme(this.luytenPrefs.getThemeXml());
    }
    
    public void onSettingsChanged() {
        this.getModel().updateOpenClasses();
    }
    
    public void onTreeSettingsChanged() {
        this.getModel().updateTree();
    }
    
    public void onFileDropped(final File file) {
        if (file != null) {
            this.getModel().loadFile(file);
        }
    }
    
    public void onFileLoadEnded(final File file, final boolean isSuccess) {
        try {
            if (file != null && isSuccess) {
                this.setTitle("Luyten - " + file.getName());
            }
            else {
                this.setTitle("Luyten");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void adjustWindowPositionBySavedState() {
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        if (!this.windowPosition.isSavedWindowPositionValid()) {
            final Dimension center = new Dimension((int)(screenSize.width * 0.75), (int)(screenSize.height * 0.75));
            final int x = (int)(center.width * 0.2);
            final int y = (int)(center.height * 0.2);
            this.setBounds(x, y, center.width, center.height);
        }
        else if (this.windowPosition.isFullScreen()) {
            int heightMinusTray = screenSize.height;
            if (screenSize.height > 30) {
                heightMinusTray -= 30;
            }
            this.setBounds(0, 0, screenSize.width, heightMinusTray);
            this.setExtendedState(6);
            this.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(final ComponentEvent e) {
                    if (MainWindow.this.getExtendedState() != 6) {
                        MainWindow.access$2(MainWindow.this).setFullScreen(false);
                        if (MainWindow.access$2(MainWindow.this).isSavedWindowPositionValid()) {
                            MainWindow.this.setBounds(MainWindow.access$2(MainWindow.this).getWindowX(), MainWindow.access$2(MainWindow.this).getWindowY(), MainWindow.access$2(MainWindow.this).getWindowWidth(), MainWindow.access$2(MainWindow.this).getWindowHeight());
                        }
                        MainWindow.this.removeComponentListener(this);
                    }
                }
            });
        }
        else {
            this.setBounds(this.windowPosition.getWindowX(), this.windowPosition.getWindowY(), this.windowPosition.getWindowWidth(), this.windowPosition.getWindowHeight());
        }
    }
    
    private void setHideFindBoxOnMainWindowFocus() {
        this.addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowGainedFocus(final WindowEvent e) {
                if (MainWindow.access$3(MainWindow.this) != null && MainWindow.access$3(MainWindow.this).isVisible()) {
                    MainWindow.access$3(MainWindow.this).setVisible(false);
                }
            }
        });
    }
    
    private void setShowFindAllBoxOnMainWindowFocus() {
        this.addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowGainedFocus(final WindowEvent e) {
                if (MainWindow.access$4(MainWindow.this) != null && MainWindow.access$4(MainWindow.this).isVisible()) {
                    MainWindow.access$4(MainWindow.this).requestFocus();
                }
            }
        });
    }
    
    private void setQuitOnWindowClosing() {
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent e) {
                MainWindow.access$5(MainWindow.this);
            }
        });
    }
    
    private void quit() {
        try {
            this.windowPosition.readPositionFromWindow(this);
            this.configSaver.saveConfig();
        }
        catch (Exception exc) {
            exc.printStackTrace();
            return;
        }
        finally {
            try {
                this.dispose();
            }
            finally {
                System.exit(0);
            }
            System.exit(0);
        }
        try {
            this.dispose();
        }
        finally {
            System.exit(0);
        }
        System.exit(0);
    }
    
    private void setExitOnEscWhenEnabled(final JComponent mainComponent) {
        final Action escapeAction = new AbstractAction() {
            private static final long serialVersionUID = 1L;
            
            @Override
            public void actionPerformed(final ActionEvent e) {
                if (MainWindow.access$6(MainWindow.this).isExitByEscEnabled()) {
                    MainWindow.access$5(MainWindow.this);
                }
            }
        };
        final KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke(27, 0, false);
        mainComponent.getInputMap(1).put(escapeKeyStroke, "ESCAPE");
        mainComponent.getActionMap().put("ESCAPE", escapeAction);
    }
    
    public Model getModel() {
        return MainWindow.model;
    }
    
    public JProgressBar getBar() {
        return this.bar;
    }
    
    public JLabel getLabel() {
        return this.label;
    }
    
    static /* synthetic */ JProgressBar access$0(final MainWindow param_0) {
        return param_0.bar;
    }
    
    static /* synthetic */ String access$1(final MainWindow param_0) {
        return param_0.getLegalStr();
    }
    
    static /* synthetic */ WindowPosition access$2(final MainWindow param_0) {
        return param_0.windowPosition;
    }
    
    static /* synthetic */ FindBox access$3(final MainWindow param_0) {
        return param_0.findBox;
    }
    
    static /* synthetic */ FindAllBox access$4(final MainWindow param_0) {
        return param_0.findAllBox;
    }
    
    static /* synthetic */ void access$5(final MainWindow param_0) {
        param_0.quit();
    }
    
    static /* synthetic */ LuytenPreferences access$6(final MainWindow param_0) {
        return param_0.luytenPrefs;
    }
}
