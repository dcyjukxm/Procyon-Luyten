package us.deathmarine.luyten;

import java.awt.event.*;
import javax.swing.*;
import java.awt.*;
import java.util.jar.*;
import com.strobel.core.*;
import java.util.zip.*;
import com.strobel.decompiler.*;
import java.util.*;
import com.strobel.assembler.metadata.*;
import java.io.*;
import com.strobel.decompiler.languages.java.*;

public class FileSaver
{
    private JProgressBar bar;
    private JLabel label;
    private boolean cancel;
    private boolean extracting;
    
    public FileSaver(final JProgressBar bar, final JLabel label) {
        super();
        this.bar = bar;
        this.label = label;
        final JPopupMenu menu = new JPopupMenu("Cancel");
        final JMenuItem item = new JMenuItem("Cancel");
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent arg0) {
                FileSaver.this.setCancel(true);
            }
        });
        menu.add(item);
        this.label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent ev) {
                if (SwingUtilities.isRightMouseButton(ev) && FileSaver.this.isExtracting()) {
                    menu.show(ev.getComponent(), ev.getX(), ev.getY());
                }
            }
        });
    }
    
    public void saveText(final String text, final File file) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final long time = System.currentTimeMillis();
                try {
                    Throwable loc_0 = null;
                    try {
                        final FileWriter fw = new FileWriter(file);
                        try {
                            final BufferedWriter bw = new BufferedWriter(fw);
                            try {
                                FileSaver.access$1(FileSaver.this).setText("Extracting: " + file.getName());
                                FileSaver.access$0(FileSaver.this).setVisible(true);
                                bw.write(text);
                                bw.flush();
                                FileSaver.access$1(FileSaver.this).setText("Completed: " + FileSaver.getTime(time));
                            }
                            finally {
                                if (bw != null) {
                                    bw.close();
                                }
                            }
                            if (fw != null) {
                                fw.close();
                            }
                        }
                        finally {
                            if (loc_0 == null) {
                                final Throwable loc_1;
                                loc_0 = loc_1;
                            }
                            else {
                                final Throwable loc_1;
                                if (loc_0 != loc_1) {
                                    loc_0.addSuppressed(loc_1);
                                }
                            }
                            if (fw != null) {
                                fw.close();
                            }
                        }
                    }
                    finally {
                        if (loc_0 == null) {
                            final Throwable loc_2;
                            loc_0 = loc_2;
                        }
                        else {
                            final Throwable loc_2;
                            if (loc_0 != loc_2) {
                                loc_0.addSuppressed(loc_2);
                            }
                        }
                    }
                }
                catch (Exception e1) {
                    FileSaver.access$1(FileSaver.this).setText("Cannot save file: " + file.getName());
                    e1.printStackTrace();
                    JOptionPane.showMessageDialog(null, e1.toString(), "Error!", 0);
                    return;
                }
                finally {
                    FileSaver.this.setExtracting(false);
                    FileSaver.access$0(FileSaver.this).setVisible(false);
                }
                FileSaver.this.setExtracting(false);
                FileSaver.access$0(FileSaver.this).setVisible(false);
            }
        }).start();
    }
    
    public void saveAllDecompiled(final File inFile, final File outFile) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final long time = System.currentTimeMillis();
                try {
                    FileSaver.access$0(FileSaver.this).setVisible(true);
                    FileSaver.this.setExtracting(true);
                    FileSaver.access$1(FileSaver.this).setText("Extracting: " + outFile.getName());
                    final String inFileName = inFile.getName().toLowerCase();
                    if (inFileName.endsWith(".jar") || inFileName.endsWith(".zip")) {
                        FileSaver.access$2(FileSaver.this, inFile, outFile);
                    }
                    else if (inFileName.endsWith(".class")) {
                        FileSaver.access$3(FileSaver.this, inFile, outFile);
                    }
                    else {
                        FileSaver.access$4(FileSaver.this, inFile, outFile);
                    }
                    if (FileSaver.access$5(FileSaver.this)) {
                        FileSaver.access$1(FileSaver.this).setText("Cancelled");
                        outFile.delete();
                        FileSaver.this.setCancel(false);
                    }
                    else {
                        FileSaver.access$1(FileSaver.this).setText("Completed: " + FileSaver.getTime(time));
                    }
                }
                catch (Exception e1) {
                    e1.printStackTrace();
                    FileSaver.access$1(FileSaver.this).setText("Cannot save file: " + outFile.getName());
                    JOptionPane.showMessageDialog(null, e1.toString(), "Error!", 0);
                    return;
                }
                finally {
                    FileSaver.this.setExtracting(false);
                    FileSaver.access$0(FileSaver.this).setVisible(false);
                }
                FileSaver.this.setExtracting(false);
                FileSaver.access$0(FileSaver.this).setVisible(false);
            }
        }).start();
    }
    
    private void doSaveJarDecompiled(final File inFile, final File outFile) throws Exception {
        Throwable loc_0 = null;
        try {
            final JarFile jfile = new JarFile(inFile);
            try {
                final FileOutputStream dest = new FileOutputStream(outFile);
                try {
                    final BufferedOutputStream buffDest = new BufferedOutputStream(dest);
                    try {
                        final ZipOutputStream out = new ZipOutputStream(buffDest);
                        try {
                            this.bar.setMinimum(0);
                            this.bar.setMaximum(jfile.size());
                            final byte[] data = new byte[1024];
                            final DecompilerSettings settings = this.cloneSettings();
                            final LuytenTypeLoader typeLoader = new LuytenTypeLoader();
                            final MetadataSystem metadataSystem = new MetadataSystem(typeLoader);
                            final ITypeLoader jarLoader = new JarTypeLoader(jfile);
                            typeLoader.getTypeLoaders().add(jarLoader);
                            final DecompilationOptions decompilationOptions = new DecompilationOptions();
                            decompilationOptions.setSettings(settings);
                            decompilationOptions.setFullDecompilation(true);
                            List<String> mass = null;
                            final JarEntryFilter jarEntryFilter = new JarEntryFilter(jfile);
                            final LuytenPreferences luytenPrefs = ConfigSaver.getLoadedInstance().getLuytenPreferences();
                            if (luytenPrefs.isFilterOutInnerClassEntries()) {
                                mass = jarEntryFilter.getEntriesWithoutInnerClasses();
                            }
                            else {
                                mass = jarEntryFilter.getAllEntriesFromJar();
                            }
                            final Enumeration<JarEntry> ent = jfile.entries();
                            final Set<JarEntry> history = new HashSet<JarEntry>();
                            int tick = 0;
                            while (ent.hasMoreElements() && !this.cancel) {
                                this.bar.setValue(++tick);
                                final JarEntry entry = ent.nextElement();
                                if (!mass.contains(entry.getName())) {
                                    continue;
                                }
                                this.label.setText("Extracting: " + entry.getName());
                                this.bar.setVisible(true);
                                if (entry.getName().endsWith(".class")) {
                                    final JarEntry etn = new JarEntry(entry.getName().replace(".class", ".java"));
                                    this.label.setText("Extracting: " + etn.getName());
                                    if (!history.add(etn)) {
                                        continue;
                                    }
                                    out.putNextEntry(etn);
                                    try {
                                        final String internalName = StringUtilities.removeRight(entry.getName(), ".class");
                                        final TypeReference type = metadataSystem.lookupType(internalName);
                                        TypeDefinition resolvedType = null;
                                        if (type == null || (resolvedType = type.resolve()) == null) {
                                            throw new Exception("Unable to resolve type.");
                                        }
                                        final Writer writer = new OutputStreamWriter(out);
                                        settings.getLanguage().decompileType(resolvedType, new PlainTextOutput(writer), decompilationOptions);
                                        writer.flush();
                                    }
                                    finally {
                                        out.closeEntry();
                                    }
                                    out.closeEntry();
                                }
                                else {
                                    try {
                                        final JarEntry etn = new JarEntry(entry.getName());
                                        if (history.add(etn)) {
                                            continue;
                                        }
                                        history.add(etn);
                                        out.putNextEntry(etn);
                                        try {
                                            final InputStream in = jfile.getInputStream(entry);
                                            if (in != null) {
                                                try {
                                                    int count;
                                                    while ((count = in.read(data, 0, 1024)) != -1) {
                                                        out.write(data, 0, count);
                                                    }
                                                }
                                                finally {
                                                    in.close();
                                                }
                                                in.close();
                                            }
                                        }
                                        finally {
                                            out.closeEntry();
                                        }
                                        out.closeEntry();
                                    }
                                    catch (ZipException ze) {
                                        if (!ze.getMessage().contains("duplicate")) {
                                            throw ze;
                                        }
                                        continue;
                                    }
                                }
                            }
                        }
                        finally {
                            if (out != null) {
                                out.close();
                            }
                        }
                        if (buffDest != null) {
                            buffDest.close();
                        }
                    }
                    finally {
                        if (loc_0 == null) {
                            final Throwable loc_1;
                            loc_0 = loc_1;
                        }
                        else {
                            final Throwable loc_1;
                            if (loc_0 != loc_1) {
                                loc_0.addSuppressed(loc_1);
                            }
                        }
                        if (buffDest != null) {
                            buffDest.close();
                        }
                    }
                    if (dest != null) {
                        dest.close();
                    }
                }
                finally {
                    if (loc_0 == null) {
                        final Throwable loc_2;
                        loc_0 = loc_2;
                    }
                    else {
                        final Throwable loc_2;
                        if (loc_0 != loc_2) {
                            loc_0.addSuppressed(loc_2);
                        }
                    }
                    if (dest != null) {
                        dest.close();
                    }
                }
                if (jfile != null) {
                    jfile.close();
                }
            }
            finally {
                if (loc_0 == null) {
                    final Throwable loc_3;
                    loc_0 = loc_3;
                }
                else {
                    final Throwable loc_3;
                    if (loc_0 != loc_3) {
                        loc_0.addSuppressed(loc_3);
                    }
                }
                if (jfile != null) {
                    jfile.close();
                }
            }
        }
        finally {
            if (loc_0 == null) {
                final Throwable loc_4;
                loc_0 = loc_4;
            }
            else {
                final Throwable loc_4;
                if (loc_0 != loc_4) {
                    loc_0.addSuppressed(loc_4);
                }
            }
        }
    }
    
    private void doSaveClassDecompiled(final File inFile, final File outFile) throws Exception {
        final DecompilerSettings settings = this.cloneSettings();
        final LuytenTypeLoader typeLoader = new LuytenTypeLoader();
        final MetadataSystem metadataSystem = new MetadataSystem(typeLoader);
        final TypeReference type = metadataSystem.lookupType(inFile.getCanonicalPath());
        final DecompilationOptions decompilationOptions = new DecompilationOptions();
        decompilationOptions.setSettings(settings);
        decompilationOptions.setFullDecompilation(true);
        TypeDefinition resolvedType = null;
        if (type == null || (resolvedType = type.resolve()) == null) {
            throw new Exception("Unable to resolve type.");
        }
        final StringWriter stringwriter = new StringWriter();
        settings.getLanguage().decompileType(resolvedType, new PlainTextOutput(stringwriter), decompilationOptions);
        final String decompiledSource = stringwriter.toString();
        Throwable loc_0 = null;
        try {
            final FileWriter fw = new FileWriter(outFile);
            try {
                final BufferedWriter bw = new BufferedWriter(fw);
                try {
                    bw.write(decompiledSource);
                    bw.flush();
                }
                finally {
                    if (bw != null) {
                        bw.close();
                    }
                }
                if (fw != null) {
                    fw.close();
                }
            }
            finally {
                if (loc_0 == null) {
                    final Throwable loc_1;
                    loc_0 = loc_1;
                }
                else {
                    final Throwable loc_1;
                    if (loc_0 != loc_1) {
                        loc_0.addSuppressed(loc_1);
                    }
                }
                if (fw != null) {
                    fw.close();
                }
            }
        }
        finally {
            if (loc_0 == null) {
                final Throwable loc_2;
                loc_0 = loc_2;
            }
            else {
                final Throwable loc_2;
                if (loc_0 != loc_2) {
                    loc_0.addSuppressed(loc_2);
                }
            }
        }
    }
    
    private void doSaveUnknownFile(final File inFile, final File outFile) throws Exception {
        Throwable loc_0 = null;
        try {
            final FileInputStream in = new FileInputStream(inFile);
            try {
                final FileOutputStream out = new FileOutputStream(outFile);
                try {
                    final byte[] data = new byte[1024];
                    int count;
                    while ((count = in.read(data, 0, 1024)) != -1) {
                        out.write(data, 0, count);
                    }
                }
                finally {
                    if (out != null) {
                        out.close();
                    }
                }
                if (in != null) {
                    in.close();
                }
            }
            finally {
                if (loc_0 == null) {
                    final Throwable loc_1;
                    loc_0 = loc_1;
                }
                else {
                    final Throwable loc_1;
                    if (loc_0 != loc_1) {
                        loc_0.addSuppressed(loc_1);
                    }
                }
                if (in != null) {
                    in.close();
                }
            }
        }
        finally {
            if (loc_0 == null) {
                final Throwable loc_2;
                loc_0 = loc_2;
            }
            else {
                final Throwable loc_2;
                if (loc_0 != loc_2) {
                    loc_0.addSuppressed(loc_2);
                }
            }
        }
    }
    
    private DecompilerSettings cloneSettings() {
        final DecompilerSettings settings = ConfigSaver.getLoadedInstance().getDecompilerSettings();
        final DecompilerSettings newSettings = new DecompilerSettings();
        if (newSettings.getFormattingOptions() == null) {
            newSettings.setFormattingOptions(JavaFormattingOptions.createDefault());
        }
        synchronized (settings) {
            newSettings.setExcludeNestedTypes(settings.getExcludeNestedTypes());
            newSettings.setFlattenSwitchBlocks(settings.getFlattenSwitchBlocks());
            newSettings.setForceExplicitImports(settings.getForceExplicitImports());
            newSettings.setForceExplicitTypeArguments(settings.getForceExplicitTypeArguments());
            newSettings.setOutputFileHeaderText(settings.getOutputFileHeaderText());
            newSettings.setLanguage(settings.getLanguage());
            newSettings.setShowSyntheticMembers(settings.getShowSyntheticMembers());
            newSettings.setAlwaysGenerateExceptionVariableForCatchBlocks(settings.getAlwaysGenerateExceptionVariableForCatchBlocks());
            newSettings.setOutputDirectory(settings.getOutputDirectory());
            newSettings.setRetainRedundantCasts(settings.getRetainRedundantCasts());
            newSettings.setIncludeErrorDiagnostics(settings.getIncludeErrorDiagnostics());
            newSettings.setIncludeLineNumbersInBytecode(settings.getIncludeLineNumbersInBytecode());
            newSettings.setRetainPointlessSwitches(settings.getRetainPointlessSwitches());
            newSettings.setUnicodeOutputEnabled(settings.isUnicodeOutputEnabled());
            newSettings.setMergeVariables(settings.getMergeVariables());
            newSettings.setShowDebugLineNumbers(settings.getShowDebugLineNumbers());
        }
        return newSettings;
    }
    
    public boolean isCancel() {
        return this.cancel;
    }
    
    public void setCancel(final boolean cancel) {
        this.cancel = cancel;
    }
    
    public boolean isExtracting() {
        return this.extracting;
    }
    
    public void setExtracting(final boolean extracting) {
        this.extracting = extracting;
    }
    
    public static String getTime(final long time) {
        long lap = System.currentTimeMillis() - time;
        lap /= 1000L;
        final StringBuilder sb = new StringBuilder();
        final int hour = (int)(lap / 60L / 60L);
        final int min = (int)((lap - hour * 60 * 60) / 60L);
        final int sec = (int)((lap - hour * 60 * 60 - min * 60) / 60L);
        if (hour > 0) {
            sb.append("Hour:").append(hour).append(" ");
        }
        sb.append("Min(s): ").append(min).append(" Sec: ").append(sec);
        return sb.toString();
    }
    
    static /* synthetic */ JProgressBar access$0(final FileSaver param_0) {
        return param_0.bar;
    }
    
    static /* synthetic */ JLabel access$1(final FileSaver param_0) {
        return param_0.label;
    }
    
    static /* synthetic */ void access$2(final FileSaver param_0, final File param_1, final File param_2) throws Exception {
        param_0.doSaveJarDecompiled(param_1, param_2);
    }
    
    static /* synthetic */ void access$3(final FileSaver param_0, final File param_1, final File param_2) throws Exception {
        param_0.doSaveClassDecompiled(param_1, param_2);
    }
    
    static /* synthetic */ void access$4(final FileSaver param_0, final File param_1, final File param_2) throws Exception {
        param_0.doSaveUnknownFile(param_1, param_2);
    }
    
    static /* synthetic */ boolean access$5(final FileSaver param_0) {
        return param_0.cancel;
    }
}
