package org.fife.ui.rsyntaxtextarea;

import javax.swing.text.*;
import java.awt.*;

public interface Token extends TokenTypes
{
    StringBuilder appendHTMLRepresentation(StringBuilder param_0, RSyntaxTextArea param_1, boolean param_2);
    
    StringBuilder appendHTMLRepresentation(StringBuilder param_0, RSyntaxTextArea param_1, boolean param_2, boolean param_3);
    
    char charAt(int param_0);
    
    boolean containsPosition(int param_0);
    
    int documentToToken(int param_0);
    
    boolean endsWith(char[] param_0);
    
    int getEndOffset();
    
    String getHTMLRepresentation(RSyntaxTextArea param_0);
    
    int getLanguageIndex();
    
    Token getLastNonCommentNonWhitespaceToken();
    
    Token getLastPaintableToken();
    
    String getLexeme();
    
    int getListOffset(RSyntaxTextArea param_0, TabExpander param_1, float param_2, float param_3);
    
    Token getNextToken();
    
    int getOffset();
    
    int getOffsetBeforeX(RSyntaxTextArea param_0, TabExpander param_1, float param_2, float param_3);
    
    char[] getTextArray();
    
    int getTextOffset();
    
    int getType();
    
    float getWidth(RSyntaxTextArea param_0, TabExpander param_1, float param_2);
    
    float getWidthUpTo(int param_0, RSyntaxTextArea param_1, TabExpander param_2, float param_3);
    
    boolean is(char[] param_0);
    
    boolean is(int param_0, char[] param_1);
    
    boolean is(int param_0, String param_1);
    
    boolean isComment();
    
    boolean isCommentOrWhitespace();
    
    boolean isHyperlink();
    
    boolean isIdentifier();
    
    boolean isLeftCurly();
    
    boolean isRightCurly();
    
    boolean isPaintable();
    
    boolean isSingleChar(char param_0);
    
    boolean isSingleChar(int param_0, char param_1);
    
    boolean isWhitespace();
    
    int length();
    
    Rectangle listOffsetToView(RSyntaxTextArea param_0, TabExpander param_1, int param_2, int param_3, Rectangle param_4);
    
    void setHyperlink(boolean param_0);
    
    void setLanguageIndex(int param_0);
    
    void setType(int param_0);
    
    boolean startsWith(char[] param_0);
    
    int tokenToDocument(int param_0);
}
