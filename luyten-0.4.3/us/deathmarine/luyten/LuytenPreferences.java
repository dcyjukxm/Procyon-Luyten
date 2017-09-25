package us.deathmarine.luyten;

public class LuytenPreferences
{
    public static final String THEME_XML_PATH = "/themes/";
    public static final String DEFAULT_THEME_XML = "eclipse.xml";
    private String themeXml;
    private String fileOpenCurrentDirectory;
    private String fileSaveCurrentDirectory;
    private boolean isPackageExplorerStyle;
    private boolean isFilterOutInnerClassEntries;
    private boolean isSingleClickOpenEnabled;
    private boolean isExitByEscEnabled;
    
    public LuytenPreferences() {
        super();
        this.themeXml = "eclipse.xml";
        this.fileOpenCurrentDirectory = "";
        this.fileSaveCurrentDirectory = "";
        this.isPackageExplorerStyle = true;
        this.isFilterOutInnerClassEntries = true;
        this.isSingleClickOpenEnabled = true;
        this.isExitByEscEnabled = false;
    }
    
    public String getThemeXml() {
        return this.themeXml;
    }
    
    public void setThemeXml(final String themeXml) {
        this.themeXml = themeXml;
    }
    
    public String getFileOpenCurrentDirectory() {
        return this.fileOpenCurrentDirectory;
    }
    
    public void setFileOpenCurrentDirectory(final String fileOpenCurrentDirectory) {
        this.fileOpenCurrentDirectory = fileOpenCurrentDirectory;
    }
    
    public String getFileSaveCurrentDirectory() {
        return this.fileSaveCurrentDirectory;
    }
    
    public void setFileSaveCurrentDirectory(final String fileSaveCurrentDirectory) {
        this.fileSaveCurrentDirectory = fileSaveCurrentDirectory;
    }
    
    public boolean isPackageExplorerStyle() {
        return this.isPackageExplorerStyle;
    }
    
    public void setPackageExplorerStyle(final boolean isPackageExplorerStyle) {
        this.isPackageExplorerStyle = isPackageExplorerStyle;
    }
    
    public boolean isFilterOutInnerClassEntries() {
        return this.isFilterOutInnerClassEntries;
    }
    
    public void setFilterOutInnerClassEntries(final boolean isFilterOutInnerClassEntries) {
        this.isFilterOutInnerClassEntries = isFilterOutInnerClassEntries;
    }
    
    public boolean isSingleClickOpenEnabled() {
        return this.isSingleClickOpenEnabled;
    }
    
    public void setSingleClickOpenEnabled(final boolean isSingleClickOpenEnabled) {
        this.isSingleClickOpenEnabled = isSingleClickOpenEnabled;
    }
    
    public boolean isExitByEscEnabled() {
        return this.isExitByEscEnabled;
    }
    
    public void setExitByEscEnabled(final boolean isExitByEscEnabled) {
        this.isExitByEscEnabled = isExitByEscEnabled;
    }
}
