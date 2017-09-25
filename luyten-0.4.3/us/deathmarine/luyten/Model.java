package us.deathmarine.luyten;

import javax.swing.border.*;
import org.fife.ui.rtextarea.*;
import org.fife.ui.rsyntaxtextarea.*;
import java.util.zip.*;
import java.util.jar.*;
import com.strobel.decompiler.*;
import com.strobel.assembler.metadata.*;
import java.io.*;
import javax.swing.tree.*;
import com.strobel.assembler.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.event.*;
import com.strobel.core.*;
import javax.swing.*;
import java.awt.*;

public class Model extends JSplitPane
{
    private static final long serialVersionUID = 6896857630400910200L;
    private static final long MAX_JAR_FILE_SIZE_BYTES = 1000000000L;
    private static final long MAX_UNPACKED_FILE_SIZE_BYTES = 1000000L;
    private static LuytenTypeLoader typeLoader;
    public static MetadataSystem metadataSystem;
    private JTree tree;
    private JTabbedPane house;
    private File file;
    private DecompilerSettings settings;
    private DecompilationOptions decompilationOptions;
    private Theme theme;
    private MainWindow mainWindow;
    private JProgressBar bar;
    private JLabel label;
    private HashSet<OpenFile> hmap;
    private Set<String> treeExpansionState;
    private boolean open;
    private State state;
    private ConfigSaver configSaver;
    private LuytenPreferences luytenPrefs;
    
    static {
        Model.typeLoader = new LuytenTypeLoader();
        Model.metadataSystem = new MetadataSystem(Model.typeLoader);
    }
    
    public Model(final MainWindow mainWindow) {
        super();
        this.hmap = new HashSet<OpenFile>();
        this.open = false;
        this.mainWindow = mainWindow;
        this.bar = mainWindow.getBar();
        this.label = mainWindow.getLabel();
        this.configSaver = ConfigSaver.getLoadedInstance();
        this.settings = this.configSaver.getDecompilerSettings();
        this.luytenPrefs = this.configSaver.getLuytenPreferences();
        try {
            final String themeXml = this.luytenPrefs.getThemeXml();
            this.theme = Theme.load(this.getClass().getResourceAsStream("/themes/" + themeXml));
        }
        catch (Exception e1) {
            try {
                e1.printStackTrace();
                final String themeXml2 = "eclipse.xml";
                this.luytenPrefs.setThemeXml(themeXml2);
                this.theme = Theme.load(this.getClass().getResourceAsStream("/themes/" + themeXml2));
            }
            catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        (this.tree = new JTree()).setModel(new DefaultTreeModel(null));
        this.tree.getSelectionModel().setSelectionMode(1);
        this.tree.setCellRenderer(new CellRenderer());
        final TreeListener tl = new TreeListener((TreeListener)null);
        this.tree.addMouseListener(tl);
        JPanel panel2 = new JPanel();
        panel2.setLayout(new BoxLayout(panel2, 1));
        panel2.setBorder(BorderFactory.createTitledBorder("Structure"));
        panel2.add(new JScrollPane(this.tree));
        (this.house = new JTabbedPane()).setTabLayoutPolicy(1);
        this.house.addChangeListener(new TabChangeListener((TabChangeListener)null));
        panel2 = new JPanel();
        panel2.setLayout(new BoxLayout(panel2, 1));
        panel2.setBorder(BorderFactory.createTitledBorder("Code"));
        panel2.add(this.house);
        this.setOrientation(1);
        this.setDividerLocation(250 % mainWindow.getWidth());
        this.setLeftComponent(panel2);
        this.setRightComponent(panel2);
        (this.decompilationOptions = new DecompilationOptions()).setSettings(this.settings);
        this.decompilationOptions.setFullDecompilation(true);
    }
    
    public void showLegal(final String legalStr) {
        final OpenFile open = new OpenFile("Legal", "*/Legal", legalStr, this.theme);
        this.hmap.add(open);
        this.addOrSwitchToTab(open);
    }
    
    private void addOrSwitchToTab(final OpenFile open) {
        final String title = open.name;
        final RTextScrollPane rTextScrollPane = open.scrollPane;
        if (this.house.indexOfTab(title) < 0) {
            this.house.addTab(title, rTextScrollPane);
            this.house.setSelectedIndex(this.house.indexOfTab(title));
            final int index = this.house.indexOfTab(title);
            final Tab ct = new Tab(title);
            ct.getButton().addMouseListener(new CloseTab(title));
            this.house.setTabComponentAt(index, ct);
        }
        else {
            this.house.setSelectedIndex(this.house.indexOfTab(title));
        }
    }
    
    private void closeOpenTab(final int index) {
        final RTextScrollPane co = (RTextScrollPane)this.house.getComponentAt(index);
        final RSyntaxTextArea pane = (RSyntaxTextArea)co.getViewport().getView();
        OpenFile open = null;
        for (final OpenFile file : this.hmap) {
            if (pane.equals(file.textArea)) {
                open = file;
            }
        }
        if (open != null && this.hmap.contains(open)) {
            this.hmap.remove(open);
        }
        this.house.remove(co);
    }
    
    private String getName(final String path) {
        if (path == null) {
            return "";
        }
        int i = path.lastIndexOf("/");
        if (i == -1) {
            i = path.lastIndexOf("\\");
        }
        if (i != -1) {
            return path.substring(i + 1);
        }
        return path;
    }
    
    private void openEntryByTreePath(final TreePath trp) {
        String name = "";
        String path = "";
        try {
            this.bar.setVisible(true);
            Label_0689: {
                if (trp.getPathCount() > 1) {
                    for (int i = 1; i < trp.getPathCount(); ++i) {
                        final DefaultMutableTreeNode node = (DefaultMutableTreeNode)trp.getPathComponent(i);
                        final TreeNodeUserObject userObject = (TreeNodeUserObject)node.getUserObject();
                        if (i == trp.getPathCount() - 1) {
                            name = userObject.getOriginalName();
                        }
                        else {
                            path = String.valueOf(path) + userObject.getOriginalName() + "/";
                        }
                    }
                    path = String.valueOf(path) + name;
                    if (!this.file.getName().endsWith(".jar") && !this.file.getName().endsWith(".zip")) {
                        break Label_0689;
                    }
                    if (this.state == null) {
                        final JarFile jfile = new JarFile(this.file);
                        final ITypeLoader jarLoader = new JarTypeLoader(jfile);
                        Model.typeLoader.getTypeLoaders().add(jarLoader);
                        this.state = new State(this.file.getCanonicalPath(), this.file, jfile, jarLoader, (State)null);
                    }
                    final JarEntry entry = this.state.jarFile.getJarEntry(path);
                    if (entry == null) {
                        throw new FileEntryNotFoundException();
                    }
                    if (entry.getSize() > 1000000L) {
                        throw new TooLargeFileException(entry.getSize());
                    }
                    final String entryName = entry.getName();
                    if (entryName.endsWith(".class")) {
                        this.label.setText("Extracting: " + name);
                        final String internalName = StringUtilities.removeRight(entryName, ".class");
                        final TypeReference type = Model.metadataSystem.lookupType(internalName);
                        this.extractClassToTextPane(type, name, path);
                        break Label_0689;
                    }
                    this.label.setText("Opening: " + name);
                    Throwable loc_0 = null;
                    try {
                        final InputStream in = this.state.jarFile.getInputStream(entry);
                        try {
                            this.extractSimpleFileEntryToTextPane(in, name, path);
                        }
                        finally {
                            if (in != null) {
                                in.close();
                            }
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
                    }
                }
                name = this.file.getName();
                path = this.file.getPath().replaceAll("\\\\", "/");
                if (this.file.length() > 1000000L) {
                    throw new TooLargeFileException(this.file.length());
                }
                if (name.endsWith(".class")) {
                    this.label.setText("Extracting: " + name);
                    final TypeReference type2 = Model.metadataSystem.lookupType(path);
                    this.extractClassToTextPane(type2, name, path);
                }
                else {
                    this.label.setText("Opening: " + name);
                    Throwable loc_2 = null;
                    try {
                        final InputStream in2 = new FileInputStream(this.file);
                        try {
                            this.extractSimpleFileEntryToTextPane(in2, name, path);
                        }
                        finally {
                            if (in2 != null) {
                                in2.close();
                            }
                        }
                    }
                    finally {
                        if (loc_2 == null) {
                            final Throwable loc_3;
                            loc_2 = loc_3;
                        }
                        else {
                            final Throwable loc_3;
                            if (loc_2 != loc_3) {
                                loc_2.addSuppressed(loc_3);
                            }
                        }
                    }
                }
            }
            this.label.setText("Complete");
        }
        catch (FileEntryNotFoundException e3) {
            this.label.setText("File not found: " + name);
        }
        catch (FileIsBinaryException e4) {
            this.label.setText("Binary resource: " + name);
        }
        catch (TooLargeFileException e) {
            this.label.setText("File is too large: " + name + " - size: " + e.getReadableFileSize());
        }
        catch (Exception e2) {
            this.label.setText("Cannot open: " + name);
            e2.printStackTrace();
            JOptionPane.showMessageDialog(null, e2.toString(), "Error!", 0);
        }
        finally {
            this.bar.setVisible(false);
        }
        this.bar.setVisible(false);
    }
    
    private void extractClassToTextPane(final TypeReference type, final String tabTitle, final String path) throws Exception {
        if (tabTitle == null || tabTitle.trim().length() < 1 || path == null) {
            throw new FileEntryNotFoundException();
        }
        OpenFile sameTitledOpen = null;
        for (final OpenFile nextOpen : this.hmap) {
            if (tabTitle.equals(nextOpen.name)) {
                sameTitledOpen = nextOpen;
                break;
            }
        }
        if (sameTitledOpen != null && path.equals(sameTitledOpen.getPath()) && type.equals(sameTitledOpen.getType()) && sameTitledOpen.isContentValid()) {
            this.addOrSwitchToTab(sameTitledOpen);
            return;
        }
        final String decompiledSource = this.extractClassToString(type);
        if (sameTitledOpen != null) {
            sameTitledOpen.setContent(decompiledSource);
            sameTitledOpen.setPath(path);
            sameTitledOpen.setType(type);
            sameTitledOpen.setContentValid(true);
            this.addOrSwitchToTab(sameTitledOpen);
        }
        else {
            final OpenFile open = new OpenFile(type, tabTitle, path, decompiledSource, this.theme);
            open.setContentValid(true);
            this.hmap.add(open);
            this.addOrSwitchToTab(open);
        }
    }
    
    private String extractClassToString(final TypeReference type) throws Exception {
        synchronized (this.settings) {
            TypeDefinition resolvedType = null;
            if (type == null || (resolvedType = type.resolve()) == null) {
                throw new Exception("Unable to resolve type.");
            }
            final StringWriter stringwriter = new StringWriter();
            this.settings.getLanguage().decompileType(resolvedType, new PlainTextOutput(stringwriter), this.decompilationOptions);
            // monitorexit(this.settings)
            return stringwriter.toString();
        }
    }
    
    private void extractSimpleFileEntryToTextPane(final InputStream inputStream, final String tabTitle, final String path) throws Exception {
        if (inputStream == null || tabTitle == null || tabTitle.trim().length() < 1 || path == null) {
            throw new FileEntryNotFoundException();
        }
        OpenFile sameTitledOpen = null;
        for (final OpenFile nextOpen : this.hmap) {
            if (tabTitle.equals(nextOpen.name)) {
                sameTitledOpen = nextOpen;
                break;
            }
        }
        if (sameTitledOpen != null && path.equals(sameTitledOpen.getPath())) {
            this.addOrSwitchToTab(sameTitledOpen);
            return;
        }
        final StringBuilder sb = new StringBuilder();
        long nonprintableCharactersCount = 0L;
        Throwable loc_1 = null;
        try {
            final InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            try {
                final BufferedReader reader = new BufferedReader(inputStreamReader);
                try {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line).append("\n");
                        byte[] loc_3;
                        for (int loc_2 = (loc_3 = line.getBytes()).length, loc_4 = 0; loc_4 < loc_2; ++loc_4) {
                            final byte nextByte = loc_3[loc_4];
                            if (nextByte <= 0) {
                                ++nonprintableCharactersCount;
                            }
                        }
                    }
                }
                finally {
                    if (reader != null) {
                        reader.close();
                    }
                }
                if (inputStreamReader != null) {
                    inputStreamReader.close();
                }
            }
            finally {
                if (loc_1 == null) {
                    final Throwable loc_5;
                    loc_1 = loc_5;
                }
                else {
                    final Throwable loc_5;
                    if (loc_1 != loc_5) {
                        loc_1.addSuppressed(loc_5);
                    }
                }
                if (inputStreamReader != null) {
                    inputStreamReader.close();
                }
            }
        }
        finally {
            if (loc_1 == null) {
                final Throwable loc_6;
                loc_1 = loc_6;
            }
            else {
                final Throwable loc_6;
                if (loc_1 != loc_6) {
                    loc_1.addSuppressed(loc_6);
                }
            }
        }
        final String extension = "." + tabTitle.replaceAll("^[^\\.]*$", "").replaceAll("[^\\.]*\\.", "");
        final boolean isTextFile = OpenFile.WELL_KNOWN_TEXT_FILE_EXTENSIONS.contains(extension) || nonprintableCharactersCount < sb.length() / 5;
        if (!isTextFile) {
            throw new FileIsBinaryException();
        }
        if (sameTitledOpen != null) {
            sameTitledOpen.setContent(sb.toString());
            sameTitledOpen.setPath(path);
            this.addOrSwitchToTab(sameTitledOpen);
        }
        else {
            final OpenFile open = new OpenFile(tabTitle, path, sb.toString(), this.theme);
            this.hmap.add(open);
            this.addOrSwitchToTab(open);
        }
    }
    
    public void updateOpenClasses() {
        for (final OpenFile open : this.hmap) {
            open.setContentValid(false);
        }
        for (final OpenFile open : this.hmap) {
            if (open.getType() != null) {
                open.setContent("");
            }
        }
        for (final OpenFile open : this.hmap) {
            if (open.getType() != null && this.isTabInForeground(open)) {
                this.updateOpenClass(open);
                break;
            }
        }
    }
    
    private void updateOpenClass(final OpenFile open) {
        if (open.getType() == null) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Model.access$8(Model.this).setVisible(true);
                    Model.access$9(Model.this).setText("Extracting: " + open.name);
                    final String decompiledSource = Model.access$10(Model.this, open.getType());
                    open.setContent(decompiledSource);
                    open.setContentValid(true);
                    Model.access$9(Model.this).setText("Complete");
                }
                catch (Exception e) {
                    Model.access$9(Model.this).setText("Error, cannot update: " + open.name);
                    return;
                }
                finally {
                    Model.access$8(Model.this).setVisible(false);
                }
                Model.access$8(Model.this).setVisible(false);
            }
        }).start();
    }
    
    private boolean isTabInForeground(final OpenFile open) {
        final String title = open.name;
        final int selectedIndex = this.house.getSelectedIndex();
        return selectedIndex >= 0 && selectedIndex == this.house.indexOfTab(title);
    }
    
    public DefaultMutableTreeNode loadNodesByNames(final DefaultMutableTreeNode node, final List<String> originalNames) {
        final List<TreeNodeUserObject> args = new ArrayList<TreeNodeUserObject>();
        for (final String originalName : originalNames) {
            args.add(new TreeNodeUserObject(originalName));
        }
        return this.loadNodesByUserObj(node, args);
    }
    
    public DefaultMutableTreeNode loadNodesByUserObj(final DefaultMutableTreeNode node, final List<TreeNodeUserObject> args) {
        if (args.size() > 0) {
            final TreeNodeUserObject name = args.remove(0);
            DefaultMutableTreeNode nod = this.getChild(node, name);
            if (nod == null) {
                nod = new DefaultMutableTreeNode(name);
            }
            node.add(this.loadNodesByUserObj(nod, args));
        }
        return node;
    }
    
    public DefaultMutableTreeNode getChild(final DefaultMutableTreeNode node, final TreeNodeUserObject name) {
        final Enumeration<DefaultMutableTreeNode> entry = node.children();
        while (entry.hasMoreElements()) {
            final DefaultMutableTreeNode nods = entry.nextElement();
            if (((TreeNodeUserObject)nods.getUserObject()).getOriginalName().equals(name.getOriginalName())) {
                return nods;
            }
        }
        return null;
    }
    
    public void loadFile(final File file) {
        if (this.open) {
            this.closeFile();
        }
        this.file = file;
        this.loadTree();
    }
    
    public void updateTree() {
        final TreeUtil treeUtil = new TreeUtil(this.tree);
        this.treeExpansionState = treeUtil.getExpansionState();
        this.loadTree();
    }
    
    public void loadTree() {
        new Thread(new Runnable() {
            final /* synthetic */ Model this$0;
            
            @Override
            public void run() {
                try {
                    if (Model.access$12(Model.this) == null) {
                        return;
                    }
                    Model.access$1(Model.this).setModel(new DefaultTreeModel(null));
                    if (Model.access$12(Model.this).length() > 1000000000L) {
                        throw new TooLargeFileException(Model.access$12(Model.this).length());
                    }
                    if (Model.access$12(Model.this).getName().endsWith(".zip") || Model.access$12(Model.this).getName().endsWith(".jar")) {
                        final JarFile jfile = new JarFile(Model.access$12(Model.this));
                        Model.access$9(Model.this).setText("Loading: " + jfile.getName());
                        Model.access$8(Model.this).setVisible(true);
                        final JarEntryFilter jarEntryFilter = new JarEntryFilter(jfile);
                        List<String> mass = null;
                        if (Model.access$0(Model.this).isFilterOutInnerClassEntries()) {
                            mass = jarEntryFilter.getEntriesWithoutInnerClasses();
                        }
                        else {
                            mass = jarEntryFilter.getAllEntriesFromJar();
                        }
                        Model.access$14(Model.this, mass);
                        if (Model.access$15(Model.this) == null) {
                            final ITypeLoader jarLoader = new JarTypeLoader(jfile);
                            Model.access$6().getTypeLoaders().add(jarLoader);
                            Model.access$16(Model.this, new State(Model.access$12(Model.this).getCanonicalPath(), Model.access$12(Model.this), jfile, jarLoader, (State)null));
                        }
                        Model.access$17(Model.this, true);
                        Model.access$9(Model.this).setText("Complete");
                    }
                    else {
                        final TreeNodeUserObject topNodeUserObject = new TreeNodeUserObject(Model.access$18(Model.this, Model.access$12(Model.this).getName()));
                        final DefaultMutableTreeNode top = new DefaultMutableTreeNode(topNodeUserObject);
                        Model.access$1(Model.this).setModel(new DefaultTreeModel(top));
                        Model.access$19(Model.this).setTypeLoader(new InputTypeLoader());
                        Model.access$17(Model.this, true);
                        Model.access$9(Model.this).setText("Complete");
                        new Thread() {
                            @Override
                            public void run() {
                                final TreePath trp = new TreePath(top.getPath());
                                Model.access$2(Model$2.access$0(Runnable.this), trp);
                            }
                        }.start();
                    }
                    if (Model.access$20(Model.this) != null) {
                        try {
                            final TreeUtil treeUtil = new TreeUtil(Model.access$1(Model.this));
                            treeUtil.restoreExpanstionState(Model.access$20(Model.this));
                        }
                        catch (Exception exc) {
                            exc.printStackTrace();
                        }
                    }
                }
                catch (TooLargeFileException e) {
                    Model.access$9(Model.this).setText("File is too large: " + Model.access$12(Model.this).getName() + " - size: " + e.getReadableFileSize());
                    Model.this.closeFile();
                }
                catch (Exception e2) {
                    e2.printStackTrace();
                    Model.access$9(Model.this).setText("Cannot open: " + Model.access$12(Model.this).getName());
                    Model.this.closeFile();
                }
                finally {
                    Model.access$11(Model.this).onFileLoadEnded(Model.access$12(Model.this), Model.access$13(Model.this));
                    Model.access$8(Model.this).setVisible(false);
                }
                Model.access$11(Model.this).onFileLoadEnded(Model.access$12(Model.this), Model.access$13(Model.this));
                Model.access$8(Model.this).setVisible(false);
            }
            
            static /* synthetic */ Model access$0(final Model$2 param_0) {
                return param_0.this$0;
            }
        }).start();
    }
    
    private void buildTreeFromMass(final List<String> mass) {
        if (this.luytenPrefs.isPackageExplorerStyle()) {
            this.buildFlatTreeFromMass(mass);
        }
        else {
            this.buildDirectoryTreeFromMass(mass);
        }
    }
    
    private void buildDirectoryTreeFromMass(final List<String> mass) {
        final TreeNodeUserObject topNodeUserObject = new TreeNodeUserObject(this.getName(this.file.getName()));
        final DefaultMutableTreeNode top = new DefaultMutableTreeNode(topNodeUserObject);
        final List<String> sort = new ArrayList<String>();
        Collections.sort(mass, String.CASE_INSENSITIVE_ORDER);
        for (final String m : mass) {
            if (m.contains("META-INF") && !sort.contains(m)) {
                sort.add(m);
            }
        }
        final Set<String> set = new HashSet<String>();
        for (final String i : mass) {
            if (i.contains("/")) {
                set.add(i.substring(0, i.lastIndexOf("/") + 1));
            }
        }
        final List<String> packs = Arrays.asList((String[])set.toArray((T[])new String[0]));
        Collections.sort(packs, String.CASE_INSENSITIVE_ORDER);
        Collections.sort(packs, new Comparator<String>() {
            @Override
            public int compare(final String o1, final String o2) {
                return o2.split("/").length - o1.split("/").length;
            }
        });
        for (final String pack : packs) {
            for (final String j : mass) {
                if (!j.contains("META-INF") && j.contains(pack) && !j.replace(pack, "").contains("/")) {
                    sort.add(j);
                }
            }
        }
        for (final String k : mass) {
            if (!k.contains("META-INF") && !k.contains("/") && !sort.contains(k)) {
                sort.add(k);
            }
        }
        for (final String pack : sort) {
            final LinkedList<String> list = new LinkedList<String>(Arrays.asList(pack.split("/")));
            this.loadNodesByNames(top, list);
        }
        this.tree.setModel(new DefaultTreeModel(top));
    }
    
    private void buildFlatTreeFromMass(final List<String> mass) {
        final TreeNodeUserObject topNodeUserObject = new TreeNodeUserObject(this.getName(this.file.getName()));
        final DefaultMutableTreeNode top = new DefaultMutableTreeNode(topNodeUserObject);
        final TreeMap<String, TreeSet<String>> packages = new TreeMap<String, TreeSet<String>>();
        final HashSet<String> classContainingPackageRoots = new HashSet<String>();
        final Comparator<String> sortByFileExtensionsComparator = new Comparator<String>() {
            @Override
            public int compare(final String o1, final String o2) {
                final int comp = o1.replaceAll("[^\\.]*\\.", "").compareTo(o2.replaceAll("[^\\.]*\\.", ""));
                if (comp != 0) {
                    return comp;
                }
                return o1.compareTo(o2);
            }
        };
        for (final String entry : mass) {
            String packagePath = "";
            String packageRoot = "";
            if (entry.contains("/")) {
                packagePath = entry.replaceAll("/[^/]*$", "");
                packageRoot = entry.replaceAll("/.*$", "");
            }
            final String packageEntry = entry.replace(String.valueOf(packagePath) + "/", "");
            if (!packages.containsKey(packagePath)) {
                packages.put(packagePath, new TreeSet<String>(sortByFileExtensionsComparator));
            }
            packages.get(packagePath).add(packageEntry);
            if (!entry.startsWith("META-INF") && packageRoot.trim().length() > 0 && entry.matches(".*\\.(class|java|prop|properties)$")) {
                classContainingPackageRoots.add(packageRoot);
            }
        }
        for (final String packagePath2 : packages.keySet()) {
            if (packagePath2.startsWith("META-INF")) {
                final List<String> packagePathElements = Arrays.asList(packagePath2.split("/"));
                for (final String entry2 : packages.get(packagePath2)) {
                    final ArrayList<String> list = new ArrayList<String>(packagePathElements);
                    list.add(entry2);
                    this.loadNodesByNames(top, list);
                }
            }
        }
        for (final String packagePath2 : packages.keySet()) {
            final String packageRoot2 = packagePath2.replaceAll("/.*$", "");
            if (classContainingPackageRoots.contains(packageRoot2)) {
                for (final String entry2 : packages.get(packagePath2)) {
                    final ArrayList<TreeNodeUserObject> list2 = new ArrayList<TreeNodeUserObject>();
                    list2.add(new TreeNodeUserObject(packagePath2, packagePath2.replaceAll("/", ".")));
                    list2.add(new TreeNodeUserObject(entry2));
                    this.loadNodesByUserObj(top, list2);
                }
            }
        }
        for (final String packagePath2 : packages.keySet()) {
            final String packageRoot2 = packagePath2.replaceAll("/.*$", "");
            if (!classContainingPackageRoots.contains(packageRoot2) && !packagePath2.startsWith("META-INF") && packagePath2.length() > 0) {
                final List<String> packagePathElements2 = Arrays.asList(packagePath2.split("/"));
                for (final String entry3 : packages.get(packagePath2)) {
                    final ArrayList<String> list3 = new ArrayList<String>(packagePathElements2);
                    list3.add(entry3);
                    this.loadNodesByNames(top, list3);
                }
            }
        }
        String packagePath2 = "";
        if (packages.containsKey(packagePath2)) {
            for (final String entry4 : packages.get(packagePath2)) {
                final ArrayList<String> list4 = new ArrayList<String>();
                list4.add(entry4);
                this.loadNodesByNames(top, list4);
            }
        }
        this.tree.setModel(new DefaultTreeModel(top));
    }
    
    public void closeFile() {
        for (final OpenFile co : this.hmap) {
            final int pos = this.house.indexOfTab(co.name);
            if (pos >= 0) {
                this.house.remove(pos);
            }
        }
        final State oldState = this.state;
        this.state = null;
        if (oldState != null) {
            Closer.tryClose(oldState);
        }
        this.hmap.clear();
        this.tree.setModel(new DefaultTreeModel(null));
        Model.metadataSystem = new MetadataSystem(Model.typeLoader);
        this.file = null;
        this.treeExpansionState = null;
        this.open = false;
        this.mainWindow.onFileLoadEnded(this.file, this.open);
    }
    
    public void changeTheme(final String xml) {
        final InputStream in = this.getClass().getResourceAsStream("/themes/" + xml);
        try {
            if (in != null) {
                this.theme = Theme.load(in);
                for (final OpenFile f : this.hmap) {
                    this.theme.apply(f.textArea);
                }
            }
        }
        catch (Exception e1) {
            e1.printStackTrace();
            JOptionPane.showMessageDialog(null, e1.toString(), "Error!", 0);
        }
    }
    
    public File getOpenedFile() {
        File openedFile = null;
        if (this.file != null && this.open) {
            openedFile = this.file;
        }
        if (openedFile == null) {
            this.label.setText("No open file");
        }
        return openedFile;
    }
    
    public String getCurrentTabTitle() {
        String tabTitle = null;
        try {
            final int pos = this.house.getSelectedIndex();
            if (pos >= 0) {
                tabTitle = this.house.getTitleAt(pos);
            }
        }
        catch (Exception e1) {
            e1.printStackTrace();
        }
        if (tabTitle == null) {
            this.label.setText("No open tab");
        }
        return tabTitle;
    }
    
    public RSyntaxTextArea getCurrentTextArea() {
        RSyntaxTextArea currentTextArea = null;
        try {
            final int pos = this.house.getSelectedIndex();
            if (pos >= 0) {
                final RTextScrollPane co = (RTextScrollPane)this.house.getComponentAt(pos);
                currentTextArea = (RSyntaxTextArea)co.getViewport().getView();
            }
        }
        catch (Exception e1) {
            e1.printStackTrace();
        }
        if (currentTextArea == null) {
            this.label.setText("No open tab");
        }
        return currentTextArea;
    }
    
    public void startWarmUpThread() {
        new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(500L);
                    final String internalName = FindBox.class.getName();
                    final TypeReference type = Model.metadataSystem.lookupType(internalName);
                    TypeDefinition resolvedType = null;
                    if (type == null || (resolvedType = type.resolve()) == null) {
                        return;
                    }
                    final StringWriter stringwriter = new StringWriter();
                    Model.access$19(Model.this).getLanguage().decompileType(resolvedType, new PlainTextOutput(stringwriter), Model.access$21(Model.this));
                    final String decompiledSource = stringwriter.toString();
                    final OpenFile open = new OpenFile(internalName, "*/" + internalName, decompiledSource, Model.access$22(Model.this));
                    final JTabbedPane pane = new JTabbedPane();
                    pane.setTabLayoutPolicy(1);
                    pane.addTab("title", open.scrollPane);
                    pane.setSelectedIndex(pane.indexOfTab("title"));
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
    
    static /* synthetic */ LuytenPreferences access$0(final Model param_0) {
        return param_0.luytenPrefs;
    }
    
    static /* synthetic */ JTree access$1(final Model param_0) {
        return param_0.tree;
    }
    
    static /* synthetic */ void access$2(final Model param_0, final TreePath param_1) {
        param_0.openEntryByTreePath(param_1);
    }
    
    static /* synthetic */ JTabbedPane access$3(final Model param_0) {
        return param_0.house;
    }
    
    static /* synthetic */ HashSet access$4(final Model param_0) {
        return param_0.hmap;
    }
    
    static /* synthetic */ void access$5(final Model param_0, final OpenFile param_1) {
        param_0.updateOpenClass(param_1);
    }
    
    static /* synthetic */ LuytenTypeLoader access$6() {
        return Model.typeLoader;
    }
    
    static /* synthetic */ void access$7(final Model param_0, final int param_1) {
        param_0.closeOpenTab(param_1);
    }
    
    static /* synthetic */ JProgressBar access$8(final Model param_0) {
        return param_0.bar;
    }
    
    static /* synthetic */ JLabel access$9(final Model param_0) {
        return param_0.label;
    }
    
    static /* synthetic */ String access$10(final Model param_0, final TypeReference param_1) throws Exception {
        return param_0.extractClassToString(param_1);
    }
    
    static /* synthetic */ MainWindow access$11(final Model param_0) {
        return param_0.mainWindow;
    }
    
    static /* synthetic */ File access$12(final Model param_0) {
        return param_0.file;
    }
    
    static /* synthetic */ boolean access$13(final Model param_0) {
        return param_0.open;
    }
    
    static /* synthetic */ void access$14(final Model param_0, final List param_1) {
        param_0.buildTreeFromMass(param_1);
    }
    
    static /* synthetic */ State access$15(final Model param_0) {
        return param_0.state;
    }
    
    static /* synthetic */ void access$16(final Model param_0, final State param_1) {
        param_0.state = param_1;
    }
    
    static /* synthetic */ void access$17(final Model param_0, final boolean param_1) {
        param_0.open = param_1;
    }
    
    static /* synthetic */ String access$18(final Model param_0, final String param_1) {
        return param_0.getName(param_1);
    }
    
    static /* synthetic */ DecompilerSettings access$19(final Model param_0) {
        return param_0.settings;
    }
    
    static /* synthetic */ Set access$20(final Model param_0) {
        return param_0.treeExpansionState;
    }
    
    static /* synthetic */ DecompilationOptions access$21(final Model param_0) {
        return param_0.decompilationOptions;
    }
    
    static /* synthetic */ Theme access$22(final Model param_0) {
        return param_0.theme;
    }
    
    private class TreeListener extends MouseAdapter
    {
        final /* synthetic */ Model this$0;
        
        @Override
        public void mousePressed(final MouseEvent event) {
            final boolean isClickCountMatches = (event.getClickCount() == 1 && Model.access$0(Model.this).isSingleClickOpenEnabled()) || (event.getClickCount() == 2 && !Model.access$0(Model.this).isSingleClickOpenEnabled());
            if (!isClickCountMatches) {
                return;
            }
            if (!SwingUtilities.isLeftMouseButton(event)) {
                return;
            }
            final TreePath trp = Model.access$1(Model.this).getPathForLocation(event.getX(), event.getY());
            if (trp == null) {
                return;
            }
            final Object lastPathComponent = trp.getLastPathComponent();
            final boolean isLeaf = lastPathComponent instanceof TreeNode && ((TreeNode)lastPathComponent).isLeaf();
            if (!isLeaf) {
                return;
            }
            new Thread() {
                @Override
                public void run() {
                    Model.access$2(TreeListener.access$1(TreeListener.this), trp);
                }
            }.start();
        }
        
        static /* synthetic */ Model access$1(final TreeListener param_0) {
            return param_0.this$0;
        }
    }
    
    private class TabChangeListener implements ChangeListener
    {
        @Override
        public void stateChanged(final ChangeEvent e) {
            final int selectedIndex = Model.access$3(Model.this).getSelectedIndex();
            if (selectedIndex < 0) {
                return;
            }
            for (final OpenFile open : Model.access$4(Model.this)) {
                if (Model.access$3(Model.this).indexOfTab(open.name) == selectedIndex && open.getType() != null && !open.isContentValid()) {
                    Model.access$5(Model.this, open);
                    break;
                }
            }
        }
    }
    
    private final class State implements AutoCloseable
    {
        private final String key;
        private final File file;
        final JarFile jarFile;
        final ITypeLoader typeLoader;
        
        private State(final String key, final File file, final JarFile jarFile, final ITypeLoader typeLoader) {
            super();
            this.key = VerifyArgument.notNull(key, "key");
            this.file = VerifyArgument.notNull(file, "file");
            this.jarFile = jarFile;
            this.typeLoader = typeLoader;
        }
        
        @Override
        public void close() {
            if (this.typeLoader != null) {
                Model.access$6().getTypeLoaders().remove(this.typeLoader);
            }
            Closer.tryClose(this.jarFile);
        }
        
        public File getFile() {
            return this.file;
        }
        
        public String getKey() {
            return this.key;
        }
    }
    
    private class Tab extends JPanel
    {
        private static final long serialVersionUID = -514663009333644974L;
        private JLabel closeButton;
        private JLabel tabTitle;
        private String title;
        
        public Tab(final String t) {
            super(new GridBagLayout());
            this.closeButton = new JLabel(new ImageIcon(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/resources/icon_close.png"))));
            this.tabTitle = new JLabel();
            this.title = "";
            this.setOpaque(false);
            this.title = t;
            this.tabTitle = new JLabel(this.title);
            this.createTab();
        }
        
        public JLabel getButton() {
            return this.closeButton;
        }
        
        public void createTab() {
            final GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 1.0;
            this.add(this.tabTitle, gbc);
            final GridBagConstraints loc_0 = gbc;
            ++loc_0.gridx;
            gbc.insets = new Insets(0, 5, 0, 0);
            gbc.anchor = 13;
            this.add(this.closeButton, gbc);
        }
    }
    
    private class CloseTab extends MouseAdapter
    {
        String title;
        
        public CloseTab(final String title) {
            super();
            this.title = title;
        }
        
        @Override
        public void mouseClicked(final MouseEvent e) {
            final int index = Model.access$3(Model.this).indexOfTab(this.title);
            Model.access$7(Model.this, index);
        }
    }
}
