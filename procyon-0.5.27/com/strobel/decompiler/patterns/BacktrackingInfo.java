package com.strobel.decompiler.patterns;

import java.util.*;

public class BacktrackingInfo
{
    final Stack<PossibleMatch> stack;
    
    public BacktrackingInfo() {
        super();
        this.stack = new Stack<PossibleMatch>();
    }
}
