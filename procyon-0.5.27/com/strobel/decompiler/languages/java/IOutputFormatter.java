package com.strobel.decompiler.languages.java;

import com.strobel.decompiler.languages.java.ast.*;

public interface IOutputFormatter
{
    void startNode(AstNode param_0);
    
    void endNode(AstNode param_0);
    
    void writeLabel(String param_0);
    
    void writeIdentifier(String param_0);
    
    void writeKeyword(String param_0);
    
    void writeOperator(String param_0);
    
    void writeDelimiter(String param_0);
    
    void writeToken(String param_0);
    
    void writeLiteral(String param_0);
    
    void writeTextLiteral(String param_0);
    
    void space();
    
    void openBrace(BraceStyle param_0);
    
    void closeBrace(BraceStyle param_0);
    
    void indent();
    
    void unindent();
    
    void newLine();
    
    void writeComment(CommentType param_0, String param_1);
    
    void resetLineNumberOffsets(OffsetToLineNumberConverter param_0);
}
