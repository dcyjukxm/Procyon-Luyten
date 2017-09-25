package com.strobel.decompiler.languages.java;

public enum BraceStyle
{
    DoNotChange("DoNotChange", 0), 
    EndOfLine("EndOfLine", 1), 
    EndOfLineWithoutSpace("EndOfLineWithoutSpace", 2), 
    NextLine("NextLine", 3), 
    NextLineShifted("NextLineShifted", 4), 
    NextLineShifted2("NextLineShifted2", 5), 
    BannerStyle("BannerStyle", 6);
}
