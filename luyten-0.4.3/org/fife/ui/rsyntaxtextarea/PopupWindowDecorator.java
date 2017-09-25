package org.fife.ui.rsyntaxtextarea;

import javax.swing.*;

public abstract class PopupWindowDecorator
{
    private static PopupWindowDecorator decorator;
    
    public abstract void decorate(final JWindow param_0);
    
    public static PopupWindowDecorator get() {
        return PopupWindowDecorator.decorator;
    }
    
    public static void set(final PopupWindowDecorator decorator) {
        PopupWindowDecorator.decorator = decorator;
    }
}
