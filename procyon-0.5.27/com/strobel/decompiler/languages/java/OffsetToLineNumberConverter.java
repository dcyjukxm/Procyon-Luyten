package com.strobel.decompiler.languages.java;

public interface OffsetToLineNumberConverter
{
    public static final int UNKNOWN_LINE_NUMBER = -100;
    public static final OffsetToLineNumberConverter NOOP_CONVERTER = new OffsetToLineNumberConverter() {
        @Override
        public int getLineForOffset(int offset) {
            return -100;
        }
    };
    
    int getLineForOffset(int param_0);
}
