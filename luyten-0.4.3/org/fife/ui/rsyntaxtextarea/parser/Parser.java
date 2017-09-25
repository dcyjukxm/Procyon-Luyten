package org.fife.ui.rsyntaxtextarea.parser;

import java.net.*;
import org.fife.ui.rsyntaxtextarea.*;

public interface Parser
{
    ExtendedHyperlinkListener getHyperlinkListener();
    
    URL getImageBase();
    
    boolean isEnabled();
    
    ParseResult parse(RSyntaxDocument param_0, String param_1);
}
