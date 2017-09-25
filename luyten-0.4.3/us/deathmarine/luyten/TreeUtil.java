package us.deathmarine.luyten;

import javax.swing.*;
import java.util.*;
import javax.swing.tree.*;

public class TreeUtil
{
    private JTree tree;
    
    public TreeUtil() {
        super();
    }
    
    public TreeUtil(final JTree tree) {
        super();
        this.tree = tree;
    }
    
    public Set<String> getExpansionState() {
        final Set<String> openedSet = new HashSet<String>();
        if (this.tree != null) {
            for (int rowCount = this.tree.getRowCount(), i = 0; i < rowCount; ++i) {
                final TreePath path = this.tree.getPathForRow(i);
                if (this.tree.isExpanded(path)) {
                    final String rowPathStr = this.getRowPathStr(path);
                    openedSet.addAll(this.getAllParentPathsStr(rowPathStr));
                }
            }
        }
        return openedSet;
    }
    
    private Set<String> getAllParentPathsStr(final String rowPathStr) {
        final Set<String> parents = new HashSet<String>();
        parents.add(rowPathStr);
        if (rowPathStr.contains("/")) {
            final String[] pathElements = rowPathStr.split("/");
            String path = "";
            String[] loc_1;
            for (int loc_0 = (loc_1 = pathElements).length, loc_2 = 0; loc_2 < loc_0; ++loc_2) {
                final String pathElement = loc_1[loc_2];
                path = String.valueOf(path) + pathElement + "/";
                parents.add(path);
            }
        }
        return parents;
    }
    
    public void restoreExpanstionState(final Set<String> expansionState) {
        if (this.tree != null && expansionState != null) {
            for (int i = 0; i < this.tree.getRowCount(); ++i) {
                final TreePath path = this.tree.getPathForRow(i);
                if (expansionState.contains(this.getRowPathStr(path))) {
                    this.tree.expandRow(i);
                }
            }
        }
    }
    
    private String getRowPathStr(final TreePath trp) {
        String pathStr = "";
        if (trp.getPathCount() > 1) {
            for (int i = 1; i < trp.getPathCount(); ++i) {
                final DefaultMutableTreeNode node = (DefaultMutableTreeNode)trp.getPathComponent(i);
                final TreeNodeUserObject userObject = (TreeNodeUserObject)node.getUserObject();
                pathStr = String.valueOf(pathStr) + userObject.getOriginalName() + "/";
            }
        }
        return pathStr;
    }
    
    public JTree getTree() {
        return this.tree;
    }
    
    public void setTree(final JTree tree) {
        this.tree = tree;
    }
}
