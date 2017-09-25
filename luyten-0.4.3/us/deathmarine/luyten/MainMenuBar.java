package us.deathmarine.luyten;

import com.strobel.decompiler.*;
import java.awt.event.*;
import javax.swing.text.*;
import java.awt.*;
import com.strobel.decompiler.languages.*;
import java.util.*;
import javax.swing.*;

public class MainMenuBar extends JMenuBar
{
    private static final long serialVersionUID = 1L;
    private final MainWindow mainWindow;
    private final Map<String, Language> languageLookup;
    private JCheckBox flattenSwitchBlocks;
    private JCheckBox forceExplicitImports;
    private JCheckBox forceExplicitTypes;
    private JCheckBox showSyntheticMembers;
    private JCheckBox excludeNestedTypes;
    private JCheckBox retainRedundantCasts;
    private JCheckBox showDebugInfo;
    private JRadioButtonMenuItem java;
    private JRadioButtonMenuItem bytecode;
    private JRadioButtonMenuItem bytecodeAST;
    private ButtonGroup languagesGroup;
    private ButtonGroup themesGroup;
    private JCheckBox packageExplorerStyle;
    private JCheckBox filterOutInnerClassEntries;
    private JCheckBox singleClickOpenEnabled;
    private JCheckBox exitByEscEnabled;
    private DecompilerSettings settings;
    private LuytenPreferences luytenPrefs;
    
    public MainMenuBar(final MainWindow mainWnd) {
        super();
        this.languageLookup = new HashMap<String, Language>();
        this.mainWindow = mainWnd;
        final ConfigSaver configSaver = ConfigSaver.getLoadedInstance();
        this.settings = configSaver.getDecompilerSettings();
        this.luytenPrefs = configSaver.getLuytenPreferences();
        final JMenu fileMenu = new JMenu("File");
        fileMenu.add(new JMenuItem("..."));
        this.add(fileMenu);
        final JMenu editMenu = new JMenu("Edit");
        editMenu.add(new JMenuItem("..."));
        this.add(editMenu);
        final JMenu themesMenu = new JMenu("Themes");
        themesMenu.add(new JMenuItem("..."));
        this.add(themesMenu);
        final JMenu operationMenu = new JMenu("Operation");
        operationMenu.add(new JMenuItem("..."));
        this.add(operationMenu);
        final JMenu settingsMenu = new JMenu("Settings");
        settingsMenu.add(new JMenuItem("..."));
        this.add(settingsMenu);
        final JMenu helpMenu = new JMenu("Help");
        helpMenu.add(new JMenuItem("..."));
        this.add(helpMenu);
        new Thread() {
            @Override
            public void run() {
                try {
                    MainMenuBar.access$2(MainMenuBar.this, fileMenu);
                    this.refreshMenuPopup(fileMenu);
                    MainMenuBar.access$3(MainMenuBar.this, editMenu);
                    this.refreshMenuPopup(editMenu);
                    MainMenuBar.access$4(MainMenuBar.this, themesMenu);
                    this.refreshMenuPopup(themesMenu);
                    MainMenuBar.access$5(MainMenuBar.this, operationMenu);
                    this.refreshMenuPopup(operationMenu);
                    MainMenuBar.access$6(MainMenuBar.this, settingsMenu);
                    this.refreshMenuPopup(settingsMenu);
                    MainMenuBar.access$7(MainMenuBar.this, helpMenu);
                    this.refreshMenuPopup(helpMenu);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            
            private void refreshMenuPopup(final JMenu menu) {
                try {
                    if (menu.isPopupMenuVisible()) {
                        menu.getPopupMenu().setVisible(false);
                        menu.getPopupMenu().setVisible(true);
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
    
    private void buildFileMenu(final JMenu fileMenu) {
        fileMenu.removeAll();
        JMenuItem menuItem = new JMenuItem("Open File...");
        menuItem.setAccelerator(KeyStroke.getKeyStroke(79, 2));
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                MainMenuBar.access$1(MainMenuBar.this).onOpenFileMenu();
            }
        });
        fileMenu.add(menuItem);
        fileMenu.addSeparator();
        menuItem = new JMenuItem("Close");
        menuItem.setAccelerator(KeyStroke.getKeyStroke(87, 2));
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                MainMenuBar.access$1(MainMenuBar.this).onCloseFileMenu();
            }
        });
        fileMenu.add(menuItem);
        fileMenu.addSeparator();
        menuItem = new JMenuItem("Save As...");
        menuItem.setAccelerator(KeyStroke.getKeyStroke(69, 2));
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                MainMenuBar.access$1(MainMenuBar.this).onSaveAsMenu();
            }
        });
        fileMenu.add(menuItem);
        menuItem = new JMenuItem("Save All...");
        menuItem.setAccelerator(KeyStroke.getKeyStroke(69, 2));
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                MainMenuBar.access$1(MainMenuBar.this).onSaveAllMenu();
            }
        });
        fileMenu.add(menuItem);
        fileMenu.addSeparator();
        menuItem = new JMenuItem("Recent Files");
        menuItem.setEnabled(false);
        fileMenu.add(menuItem);
        fileMenu.addSeparator();
        menuItem = new JMenuItem("Exit");
        menuItem.setAccelerator(KeyStroke.getKeyStroke(115, 8));
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                MainMenuBar.access$1(MainMenuBar.this).onExitMenu();
            }
        });
        fileMenu.add(menuItem);
    }
    
    private void buildEditMenu(final JMenu editMenu) {
        editMenu.removeAll();
        JMenuItem menuItem = new JMenuItem("Cut");
        menuItem.setAccelerator(KeyStroke.getKeyStroke(88, 2));
        menuItem.setEnabled(false);
        editMenu.add(menuItem);
        menuItem = new JMenuItem("Copy");
        menuItem.addActionListener(new DefaultEditorKit.CopyAction());
        menuItem.setAccelerator(KeyStroke.getKeyStroke(67, 2));
        editMenu.add(menuItem);
        menuItem = new JMenuItem("Paste");
        menuItem.setAccelerator(KeyStroke.getKeyStroke(86, 2));
        menuItem.setEnabled(false);
        editMenu.add(menuItem);
        editMenu.addSeparator();
        menuItem = new JMenuItem("Select All");
        menuItem.setAccelerator(KeyStroke.getKeyStroke(65, 2));
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                MainMenuBar.access$1(MainMenuBar.this).onSelectAllMenu();
            }
        });
        editMenu.add(menuItem);
        editMenu.addSeparator();
        menuItem = new JMenuItem("Find...");
        menuItem.setAccelerator(KeyStroke.getKeyStroke(70, 2));
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                MainMenuBar.access$1(MainMenuBar.this).onFindMenu();
            }
        });
        editMenu.add(menuItem);
        menuItem = new JMenuItem("Find All");
        menuItem.setAccelerator(KeyStroke.getKeyStroke(71, 2));
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                MainMenuBar.access$1(MainMenuBar.this).onFindAllMenu();
            }
        });
        editMenu.add(menuItem);
    }
    
    private void buildThemesMenu(final JMenu themesMenu) {
        themesMenu.removeAll();
        this.themesGroup = new ButtonGroup();
        JRadioButtonMenuItem a = new JRadioButtonMenuItem(new ThemeAction("Default", "default.xml"));
        a.setSelected("default.xml".equals(this.luytenPrefs.getThemeXml()));
        this.themesGroup.add(a);
        themesMenu.add(a);
        a = new JRadioButtonMenuItem(new ThemeAction("Dark", "dark.xml"));
        a.setSelected("dark.xml".equals(this.luytenPrefs.getThemeXml()));
        this.themesGroup.add(a);
        themesMenu.add(a);
        a = new JRadioButtonMenuItem(new ThemeAction("Eclipse", "eclipse.xml"));
        a.setSelected("eclipse.xml".equals(this.luytenPrefs.getThemeXml()));
        this.themesGroup.add(a);
        themesMenu.add(a);
        a = new JRadioButtonMenuItem(new ThemeAction("Visual Studio", "vs.xml"));
        a.setSelected("vs.xml".equals(this.luytenPrefs.getThemeXml()));
        this.themesGroup.add(a);
        themesMenu.add(a);
    }
    
    private void buildOperationMenu(final JMenu operationMenu) {
        operationMenu.removeAll();
        (this.packageExplorerStyle = new JCheckBox("    Package Explorer Style")).setSelected(this.luytenPrefs.isPackageExplorerStyle());
        this.packageExplorerStyle.setContentAreaFilled(false);
        this.packageExplorerStyle.setFocusable(false);
        this.packageExplorerStyle.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                MainMenuBar.access$0(MainMenuBar.this).setPackageExplorerStyle(MainMenuBar.access$8(MainMenuBar.this).isSelected());
                MainMenuBar.access$1(MainMenuBar.this).onTreeSettingsChanged();
            }
        });
        operationMenu.add(this.packageExplorerStyle);
        (this.filterOutInnerClassEntries = new JCheckBox("    Filter Out Inner Class Entries")).setSelected(this.luytenPrefs.isFilterOutInnerClassEntries());
        this.filterOutInnerClassEntries.setContentAreaFilled(false);
        this.filterOutInnerClassEntries.setFocusable(false);
        this.filterOutInnerClassEntries.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                MainMenuBar.access$0(MainMenuBar.this).setFilterOutInnerClassEntries(MainMenuBar.access$9(MainMenuBar.this).isSelected());
                MainMenuBar.access$1(MainMenuBar.this).onTreeSettingsChanged();
            }
        });
        operationMenu.add(this.filterOutInnerClassEntries);
        (this.singleClickOpenEnabled = new JCheckBox("    Single Click Open")).setSelected(this.luytenPrefs.isSingleClickOpenEnabled());
        this.singleClickOpenEnabled.setContentAreaFilled(false);
        this.singleClickOpenEnabled.setFocusable(false);
        this.singleClickOpenEnabled.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                MainMenuBar.access$0(MainMenuBar.this).setSingleClickOpenEnabled(MainMenuBar.access$10(MainMenuBar.this).isSelected());
            }
        });
        operationMenu.add(this.singleClickOpenEnabled);
        (this.exitByEscEnabled = new JCheckBox("    Exit By Esc")).setSelected(this.luytenPrefs.isExitByEscEnabled());
        this.exitByEscEnabled.setContentAreaFilled(false);
        this.exitByEscEnabled.setFocusable(false);
        this.exitByEscEnabled.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                MainMenuBar.access$0(MainMenuBar.this).setExitByEscEnabled(MainMenuBar.access$11(MainMenuBar.this).isSelected());
            }
        });
        operationMenu.add(this.exitByEscEnabled);
    }
    
    private void buildSettingsMenu(final JMenu settingsMenu) {
        settingsMenu.removeAll();
        final ActionListener settingsChanged = new ActionListener() {
            final /* synthetic */ MainMenuBar this$0;
            
            @Override
            public void actionPerformed(final ActionEvent e) {
                new Thread() {
                    @Override
                    public void run() {
                        MainMenuBar.access$12(MainMenuBar$14.access$0(ActionListener.this));
                        MainMenuBar.access$1(MainMenuBar$14.access$0(ActionListener.this)).onSettingsChanged();
                    }
                }.start();
            }
            
            static /* synthetic */ MainMenuBar access$0(final MainMenuBar$14 param_0) {
                return param_0.this$0;
            }
        };
        (this.flattenSwitchBlocks = new JCheckBox("    Flatten Switch Blocks")).setSelected(this.settings.getFlattenSwitchBlocks());
        this.flattenSwitchBlocks.setContentAreaFilled(false);
        this.flattenSwitchBlocks.setFocusable(false);
        this.flattenSwitchBlocks.addActionListener(settingsChanged);
        settingsMenu.add(this.flattenSwitchBlocks);
        (this.forceExplicitImports = new JCheckBox("    Force Explicit Imports")).setSelected(this.settings.getForceExplicitImports());
        this.forceExplicitImports.setContentAreaFilled(false);
        this.forceExplicitImports.setFocusable(false);
        this.forceExplicitImports.addActionListener(settingsChanged);
        settingsMenu.add(this.forceExplicitImports);
        (this.forceExplicitTypes = new JCheckBox("    Force Explicit Types")).setSelected(this.settings.getForceExplicitTypeArguments());
        this.forceExplicitTypes.setContentAreaFilled(false);
        this.forceExplicitTypes.setFocusable(false);
        this.forceExplicitTypes.addActionListener(settingsChanged);
        settingsMenu.add(this.forceExplicitTypes);
        (this.showSyntheticMembers = new JCheckBox("    Show Synthetic Members")).setSelected(this.settings.getShowSyntheticMembers());
        this.showSyntheticMembers.setContentAreaFilled(false);
        this.showSyntheticMembers.setFocusable(false);
        this.showSyntheticMembers.addActionListener(settingsChanged);
        settingsMenu.add(this.showSyntheticMembers);
        (this.excludeNestedTypes = new JCheckBox("    Exclude Nested Types")).setSelected(this.settings.getExcludeNestedTypes());
        this.excludeNestedTypes.setContentAreaFilled(false);
        this.excludeNestedTypes.setFocusable(false);
        this.excludeNestedTypes.addActionListener(settingsChanged);
        settingsMenu.add(this.excludeNestedTypes);
        (this.retainRedundantCasts = new JCheckBox("    Retain Redundant Casts")).setSelected(this.settings.getRetainRedundantCasts());
        this.retainRedundantCasts.setContentAreaFilled(false);
        this.retainRedundantCasts.setFocusable(false);
        this.retainRedundantCasts.addActionListener(settingsChanged);
        settingsMenu.add(this.retainRedundantCasts);
        final JMenu debugSettingsMenu = new JMenu("Debug Settings");
        (this.showDebugInfo = new JCheckBox("    Include Error Diagnostics")).setSelected(this.settings.getIncludeErrorDiagnostics());
        this.showDebugInfo.setContentAreaFilled(false);
        this.showDebugInfo.setFocusable(false);
        this.showDebugInfo.addActionListener(settingsChanged);
        debugSettingsMenu.add(this.showDebugInfo);
        settingsMenu.add(debugSettingsMenu);
        settingsMenu.addSeparator();
        this.languageLookup.put(Languages.java().getName(), Languages.java());
        this.languageLookup.put(Languages.bytecode().getName(), Languages.bytecode());
        this.languageLookup.put(Languages.bytecodeAst().getName(), Languages.bytecodeAst());
        this.languagesGroup = new ButtonGroup();
        this.java = new JRadioButtonMenuItem(Languages.java().getName());
        this.java.getModel().setActionCommand(Languages.java().getName());
        this.java.setSelected(Languages.java().getName().equals(this.settings.getLanguage().getName()));
        this.languagesGroup.add(this.java);
        settingsMenu.add(this.java);
        this.bytecode = new JRadioButtonMenuItem(Languages.bytecode().getName());
        this.bytecode.getModel().setActionCommand(Languages.bytecode().getName());
        this.bytecode.setSelected(Languages.bytecode().getName().equals(this.settings.getLanguage().getName()));
        this.languagesGroup.add(this.bytecode);
        settingsMenu.add(this.bytecode);
        this.bytecodeAST = new JRadioButtonMenuItem(Languages.bytecodeAst().getName());
        this.bytecodeAST.getModel().setActionCommand(Languages.bytecodeAst().getName());
        this.bytecodeAST.setSelected(Languages.bytecodeAst().getName().equals(this.settings.getLanguage().getName()));
        this.languagesGroup.add(this.bytecodeAST);
        settingsMenu.add(this.bytecodeAST);
        final JMenu debugLanguagesMenu = new JMenu("Debug Languages");
        for (final Language language : Languages.debug()) {
            final JRadioButtonMenuItem m = new JRadioButtonMenuItem(language.getName());
            m.getModel().setActionCommand(language.getName());
            m.setSelected(language.getName().equals(this.settings.getLanguage().getName()));
            this.languagesGroup.add(m);
            debugLanguagesMenu.add(m);
            this.languageLookup.put(language.getName(), language);
        }
        for (final AbstractButton button : Collections.list(this.languagesGroup.getElements())) {
            button.addActionListener(settingsChanged);
        }
        settingsMenu.add(debugLanguagesMenu);
    }
    
    private void buildHelpMenu(final JMenu helpMenu) {
        helpMenu.removeAll();
        JMenuItem menuItem = new JMenuItem("Legal");
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                MainMenuBar.access$1(MainMenuBar.this).onLegalMenu();
            }
        });
        helpMenu.add(menuItem);
        menuItem = new JMenuItem("About");
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent event) {
                JOptionPane.showMessageDialog(null, "Luyten Gui \nby Deathmarine, Zerdei\n\nPowered By\nProcyon\n(c) 2013 Mike Strobel\n\nRSyntaxTextArea\n(c) 2012 Robert Futrell\nAll rights reserved.");
            }
        });
        helpMenu.add(menuItem);
    }
    
    private void populateSettingsFromSettingsMenu() {
        synchronized (this.settings) {
            this.settings.setFlattenSwitchBlocks(this.flattenSwitchBlocks.isSelected());
            this.settings.setForceExplicitImports(this.forceExplicitImports.isSelected());
            this.settings.setShowSyntheticMembers(this.showSyntheticMembers.isSelected());
            this.settings.setExcludeNestedTypes(this.excludeNestedTypes.isSelected());
            this.settings.setForceExplicitTypeArguments(this.forceExplicitTypes.isSelected());
            this.settings.setRetainRedundantCasts(this.retainRedundantCasts.isSelected());
            this.settings.setIncludeErrorDiagnostics(this.showDebugInfo.isSelected());
            final ButtonModel selectedLanguage = this.languagesGroup.getSelection();
            if (selectedLanguage != null) {
                final Language language = this.languageLookup.get(selectedLanguage.getActionCommand());
                if (language != null) {
                    this.settings.setLanguage(language);
                }
            }
            if (this.java.isSelected()) {
                this.settings.setLanguage(Languages.java());
            }
            else if (this.bytecode.isSelected()) {
                this.settings.setLanguage(Languages.bytecode());
            }
            else if (this.bytecodeAST.isSelected()) {
                this.settings.setLanguage(Languages.bytecodeAst());
            }
        }
        // monitorexit(this.settings)
    }
    
    static /* synthetic */ LuytenPreferences access$0(final MainMenuBar param_0) {
        return param_0.luytenPrefs;
    }
    
    static /* synthetic */ MainWindow access$1(final MainMenuBar param_0) {
        return param_0.mainWindow;
    }
    
    static /* synthetic */ void access$2(final MainMenuBar param_0, final JMenu param_1) {
        param_0.buildFileMenu(param_1);
    }
    
    static /* synthetic */ void access$3(final MainMenuBar param_0, final JMenu param_1) {
        param_0.buildEditMenu(param_1);
    }
    
    static /* synthetic */ void access$4(final MainMenuBar param_0, final JMenu param_1) {
        param_0.buildThemesMenu(param_1);
    }
    
    static /* synthetic */ void access$5(final MainMenuBar param_0, final JMenu param_1) {
        param_0.buildOperationMenu(param_1);
    }
    
    static /* synthetic */ void access$6(final MainMenuBar param_0, final JMenu param_1) {
        param_0.buildSettingsMenu(param_1);
    }
    
    static /* synthetic */ void access$7(final MainMenuBar param_0, final JMenu param_1) {
        param_0.buildHelpMenu(param_1);
    }
    
    static /* synthetic */ JCheckBox access$8(final MainMenuBar param_0) {
        return param_0.packageExplorerStyle;
    }
    
    static /* synthetic */ JCheckBox access$9(final MainMenuBar param_0) {
        return param_0.filterOutInnerClassEntries;
    }
    
    static /* synthetic */ JCheckBox access$10(final MainMenuBar param_0) {
        return param_0.singleClickOpenEnabled;
    }
    
    static /* synthetic */ JCheckBox access$11(final MainMenuBar param_0) {
        return param_0.exitByEscEnabled;
    }
    
    static /* synthetic */ void access$12(final MainMenuBar param_0) {
        param_0.populateSettingsFromSettingsMenu();
    }
    
    private class ThemeAction extends AbstractAction
    {
        private static final long serialVersionUID = -6618680171943723199L;
        private String xml;
        
        public ThemeAction(final String name, final String xml) {
            super();
            this.putValue("Name", name);
            this.xml = xml;
        }
        
        @Override
        public void actionPerformed(final ActionEvent e) {
            MainMenuBar.access$0(MainMenuBar.this).setThemeXml(this.xml);
            MainMenuBar.access$1(MainMenuBar.this).onThemesChanged();
        }
    }
}
