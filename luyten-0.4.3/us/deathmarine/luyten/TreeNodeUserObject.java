package us.deathmarine.luyten;

public class TreeNodeUserObject
{
    private String originalName;
    private String displayName;
    
    public TreeNodeUserObject(final String name) {
        this(name, name);
    }
    
    public TreeNodeUserObject(final String originalName, final String displayName) {
        super();
        this.originalName = originalName;
        this.displayName = displayName;
    }
    
    public String getOriginalName() {
        return this.originalName;
    }
    
    public void setOriginalName(final String originalName) {
        this.originalName = originalName;
    }
    
    public String getDisplayName() {
        return this.displayName;
    }
    
    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }
    
    @Override
    public String toString() {
        return this.displayName;
    }
}
