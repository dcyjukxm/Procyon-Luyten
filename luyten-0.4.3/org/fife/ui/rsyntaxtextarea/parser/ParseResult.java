package org.fife.ui.rsyntaxtextarea.parser;

import java.util.*;

public interface ParseResult
{
    Exception getError();
    
    int getFirstLineParsed();
    
    int getLastLineParsed();
    
    List<ParserNotice> getNotices();
    
    Parser getParser();
    
    long getParseTime();
}
