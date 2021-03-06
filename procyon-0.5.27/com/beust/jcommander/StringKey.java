package com.beust.jcommander;

public class StringKey implements FuzzyMap.IKey
{
    private String m_name;
    
    public StringKey(final String name) {
        super();
        this.m_name = name;
    }
    
    public String getName() {
        return this.m_name;
    }
    
    public String toString() {
        return this.m_name;
    }
    
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = 31 * result + ((this.m_name == null) ? 0 : this.m_name.hashCode());
        return result;
    }
    
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        final StringKey other = (StringKey)obj;
        if (this.m_name == null) {
            if (other.m_name != null) {
                return false;
            }
        }
        else if (!this.m_name.equals(other.m_name)) {
            return false;
        }
        return true;
    }
}
