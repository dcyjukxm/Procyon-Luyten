package org.fife.ui.rsyntaxtextarea.templates;

public abstract class AbstractCodeTemplate implements CodeTemplate
{
    private String id;
    
    public AbstractCodeTemplate() {
        super();
    }
    
    public AbstractCodeTemplate(final String id) {
        super();
        this.setID(id);
    }
    
    public Object clone() {
        try {
            return super.clone();
        }
        catch (CloneNotSupportedException e) {
            throw new InternalError("CodeTemplate implementation not Cloneable: " + this.getClass().getName());
        }
    }
    
    public int compareTo(final CodeTemplate o) {
        if (o == null) {
            return -1;
        }
        return this.getID().compareTo(o.getID());
    }
    
    public boolean equals(final Object obj) {
        return obj instanceof CodeTemplate && this.compareTo((CodeTemplate)obj) == 0;
    }
    
    public String getID() {
        return this.id;
    }
    
    public int hashCode() {
        return this.id.hashCode();
    }
    
    public void setID(final String id) {
        if (id == null) {
            throw new IllegalArgumentException("id cannot be null");
        }
        this.id = id;
    }
}
