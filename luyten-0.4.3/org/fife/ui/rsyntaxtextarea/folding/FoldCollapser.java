package org.fife.ui.rsyntaxtextarea.folding;

import java.util.*;

public class FoldCollapser
{
    private List<Integer> typesToCollapse;
    
    public FoldCollapser() {
        this(1);
    }
    
    public FoldCollapser(final int typeToCollapse) {
        super();
        this.typesToCollapse = new ArrayList<Integer>(3);
        this.addTypeToCollapse(typeToCollapse);
    }
    
    public void addTypeToCollapse(final int typeToCollapse) {
        this.typesToCollapse.add(typeToCollapse);
    }
    
    public void collapseFolds(final FoldManager fm) {
        for (int i = 0; i < fm.getFoldCount(); ++i) {
            final Fold fold = fm.getFold(i);
            this.collapseImpl(fold);
        }
    }
    
    protected void collapseImpl(final Fold fold) {
        if (this.getShouldCollapse(fold)) {
            fold.setCollapsed(true);
        }
        for (int i = 0; i < fold.getChildCount(); ++i) {
            this.collapseImpl(fold.getChild(i));
        }
    }
    
    public boolean getShouldCollapse(final Fold fold) {
        final int type = fold.getFoldType();
        for (final Integer typeToCollapse : this.typesToCollapse) {
            if (type == typeToCollapse) {
                return true;
            }
        }
        return false;
    }
}
