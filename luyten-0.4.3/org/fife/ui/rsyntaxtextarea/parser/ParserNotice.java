package org.fife.ui.rsyntaxtextarea.parser;

import java.awt.*;

public interface ParserNotice extends Comparable<ParserNotice>
{
    boolean containsPosition(int param_0);
    
    Color getColor();
    
    int getLength();
    
    Level getLevel();
    
    int getLine();
    
    boolean getKnowsOffsetAndLength();
    
    String getMessage();
    
    int getOffset();
    
    Parser getParser();
    
    boolean getShowInEditor();
    
    String getToolTipText();
    
    public enum Level
    {
        INFO(2), 
        WARNING(1), 
        ERROR(0);
        
        private int value;
        
        private Level(int value) {
            this.value = value;
        }
        
        public int getNumericValue() {
            return this.value;
        }
        
        public boolean isEqualToOrWorseThan(Level other) {
            return this.value <= other.getNumericValue();
        }
    }
}
