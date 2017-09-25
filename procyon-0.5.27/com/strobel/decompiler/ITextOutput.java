package com.strobel.decompiler;

public interface ITextOutput
{
    int getRow();
    
    int getColumn();
    
    void indent();
    
    void unindent();
    
    void write(char param_0);
    
    void write(String param_0);
    
    void writeError(String param_0);
    
    void writeLabel(String param_0);
    
    void writeLiteral(Object param_0);
    
    void writeTextLiteral(Object param_0);
    
    void writeComment(String param_0);
    
    void writeComment(String param_0, Object... param_1);
    
    void write(String param_0, Object... param_1);
    
    void writeLine(String param_0);
    
    void writeLine(String param_0, Object... param_1);
    
    void writeLine();
    
    void writeDelimiter(String param_0);
    
    void writeOperator(String param_0);
    
    void writeKeyword(String param_0);
    
    void writeAttribute(String param_0);
    
    void writeDefinition(String param_0, Object param_1);
    
    void writeDefinition(String param_0, Object param_1, boolean param_2);
    
    void writeReference(String param_0, Object param_1);
    
    void writeReference(String param_0, Object param_1, boolean param_2);
    
    boolean isFoldingSupported();
    
    void markFoldStart(String param_0, boolean param_1);
    
    void markFoldEnd();
    
    String getIndentToken();
    
    void setIndentToken(String param_0);
}
