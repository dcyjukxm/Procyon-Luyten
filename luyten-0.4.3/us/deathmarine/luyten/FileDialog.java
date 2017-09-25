package us.deathmarine.luyten;

import java.awt.*;
import javax.swing.*;
import java.io.*;
import javax.swing.filechooser.*;

public class FileDialog
{
    private ConfigSaver configSaver;
    private LuytenPreferences luytenPrefs;
    private Component parent;
    private JFileChooser fcOpen;
    private JFileChooser fcSave;
    private JFileChooser fcSaveAll;
    
    public FileDialog(final Component parent) {
        super();
        this.parent = parent;
        this.configSaver = ConfigSaver.getLoadedInstance();
        this.luytenPrefs = this.configSaver.getLuytenPreferences();
        new Thread() {
            @Override
            public void run() {
                FileDialog.this.initOpenDialog();
                FileDialog.this.initSaveDialog();
                FileDialog.this.initSaveAllDialog();
            }
        }.start();
    }
    
    public File doOpenDialog() {
        File selectedFile = null;
        this.initOpenDialog();
        this.retrieveOpenDialogDir(this.fcOpen);
        final int returnVal = this.fcOpen.showOpenDialog(this.parent);
        this.saveOpenDialogDir(this.fcOpen);
        if (returnVal == 0) {
            selectedFile = this.fcOpen.getSelectedFile();
        }
        return selectedFile;
    }
    
    public File doSaveDialog(final String recommendedFileName) {
        File selectedFile = null;
        this.initSaveDialog();
        this.retrieveSaveDialogDir(this.fcSave);
        this.fcSave.setSelectedFile(new File(recommendedFileName));
        final int returnVal = this.fcSave.showSaveDialog(this.parent);
        this.saveSaveDialogDir(this.fcSave);
        if (returnVal == 0) {
            selectedFile = this.fcSave.getSelectedFile();
        }
        return selectedFile;
    }
    
    public File doSaveAllDialog(final String recommendedFileName) {
        File selectedFile = null;
        this.initSaveAllDialog();
        this.retrieveSaveDialogDir(this.fcSaveAll);
        this.fcSaveAll.setSelectedFile(new File(recommendedFileName));
        final int returnVal = this.fcSaveAll.showSaveDialog(this.parent);
        this.saveSaveDialogDir(this.fcSaveAll);
        if (returnVal == 0) {
            selectedFile = this.fcSaveAll.getSelectedFile();
        }
        return selectedFile;
    }
    
    public synchronized void initOpenDialog() {
        if (this.fcOpen == null) {
            this.retrieveOpenDialogDir(this.fcOpen = this.createFileChooser("*.jar", "*.zip", "*.class"));
        }
    }
    
    public synchronized void initSaveDialog() {
        if (this.fcSave == null) {
            this.retrieveSaveDialogDir(this.fcSave = this.createFileChooser("*.txt", "*.java"));
        }
    }
    
    public synchronized void initSaveAllDialog() {
        if (this.fcSaveAll == null) {
            this.retrieveSaveDialogDir(this.fcSaveAll = this.createFileChooser("*.jar", "*.zip"));
        }
    }
    
    private JFileChooser createFileChooser(final String... fileFilters) {
        final JFileChooser fc = new JFileChooser();
        for (final String fileFilter : fileFilters) {
            fc.addChoosableFileFilter(new FileChooserFileFilter(fileFilter));
        }
        fc.setFileSelectionMode(0);
        fc.setMultiSelectionEnabled(false);
        return fc;
    }
    
    private void retrieveOpenDialogDir(final JFileChooser fc) {
        try {
            final String currentDirStr = this.luytenPrefs.getFileOpenCurrentDirectory();
            if (currentDirStr != null && currentDirStr.trim().length() > 0) {
                final File currentDir = new File(currentDirStr);
                if (currentDir.exists() && currentDir.isDirectory()) {
                    fc.setCurrentDirectory(currentDir);
                }
            }
        }
        catch (Exception exc) {
            exc.printStackTrace();
        }
    }
    
    private void saveOpenDialogDir(final JFileChooser fc) {
        try {
            final File currentDir = fc.getCurrentDirectory();
            if (currentDir != null && currentDir.exists() && currentDir.isDirectory()) {
                this.luytenPrefs.setFileOpenCurrentDirectory(currentDir.getAbsolutePath());
            }
        }
        catch (Exception exc) {
            exc.printStackTrace();
        }
    }
    
    private void retrieveSaveDialogDir(final JFileChooser fc) {
        try {
            final String currentDirStr = this.luytenPrefs.getFileSaveCurrentDirectory();
            if (currentDirStr != null && currentDirStr.trim().length() > 0) {
                final File currentDir = new File(currentDirStr);
                if (currentDir.exists() && currentDir.isDirectory()) {
                    fc.setCurrentDirectory(currentDir);
                }
            }
        }
        catch (Exception exc) {
            exc.printStackTrace();
        }
    }
    
    private void saveSaveDialogDir(final JFileChooser fc) {
        try {
            final File currentDir = fc.getCurrentDirectory();
            if (currentDir != null && currentDir.exists() && currentDir.isDirectory()) {
                this.luytenPrefs.setFileSaveCurrentDirectory(currentDir.getAbsolutePath());
            }
        }
        catch (Exception exc) {
            exc.printStackTrace();
        }
    }
    
    public class FileChooserFileFilter extends FileFilter
    {
        String objType;
        
        public FileChooserFileFilter(final String string) {
            super();
            this.objType = string;
        }
        
        @Override
        public boolean accept(final File f) {
            return !f.isDirectory() && f.getName().toLowerCase().endsWith(this.objType.substring(1));
        }
        
        @Override
        public String getDescription() {
            return this.objType;
        }
    }
}
