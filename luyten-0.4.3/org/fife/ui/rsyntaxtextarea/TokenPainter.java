package org.fife.ui.rsyntaxtextarea;

import java.awt.*;
import javax.swing.text.*;

interface TokenPainter
{
    float paint(Token param_0, Graphics2D param_1, float param_2, float param_3, RSyntaxTextArea param_4, TabExpander param_5);
    
    float paint(Token param_0, Graphics2D param_1, float param_2, float param_3, RSyntaxTextArea param_4, TabExpander param_5, float param_6);
    
    float paintSelected(Token param_0, Graphics2D param_1, float param_2, float param_3, RSyntaxTextArea param_4, TabExpander param_5);
    
    float paintSelected(Token param_0, Graphics2D param_1, float param_2, float param_3, RSyntaxTextArea param_4, TabExpander param_5, float param_6);
}
