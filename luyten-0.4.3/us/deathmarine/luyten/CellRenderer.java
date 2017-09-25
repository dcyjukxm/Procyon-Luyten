package us.deathmarine.luyten;

import javax.swing.*;
import java.awt.*;
import javax.swing.tree.*;

public class CellRenderer extends DefaultTreeCellRenderer
{
    private static final long serialVersionUID = -5691181006363313993L;
    Icon pack;
    Icon java_image;
    Icon file_image;
    
    public CellRenderer() {
        super();
        this.pack = new ImageIcon(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/resources/package_obj.png")));
        this.java_image = new ImageIcon(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/resources/java.png")));
        this.file_image = new ImageIcon(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/resources/file.png")));
    }
    
    @Override
    public Component getTreeCellRendererComponent(final JTree tree, final Object value, final boolean sel, final boolean expanded, final boolean leaf, final int row, final boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
        final DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
        if (node.getChildCount() > 0) {
            this.setIcon(this.pack);
        }
        else if (this.getFileName(node).endsWith(".class") || this.getFileName(node).endsWith(".java")) {
            this.setIcon(this.java_image);
        }
        else {
            this.setIcon(this.file_image);
        }
        return this;
    }
    
    public String getFileName(final DefaultMutableTreeNode node) {
        return ((TreeNodeUserObject)node.getUserObject()).getOriginalName();
    }
}
