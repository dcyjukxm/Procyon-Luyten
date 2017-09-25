package com.strobel.decompiler.languages.java.ast;

import java.util.*;

public abstract class NameResolveResult
{
    public abstract List<Object> getCandidates();
    
    public abstract NameResolveMode getMode();
    
    public boolean hasMatch() {
        return !this.getCandidates().isEmpty();
    }
    
    public boolean isAmbiguous() {
        return this.getCandidates().size() > 1;
    }
}
