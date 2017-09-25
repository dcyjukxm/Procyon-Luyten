package com.strobel.decompiler.languages.java.ast;

import java.util.*;
import com.strobel.decompiler.patterns.*;

final class CommentStatement extends Statement
{
    private final String _comment;
    
    CommentStatement(final String comment) {
        super(-34);
        this._comment = comment;
    }
    
    final String getComment() {
        return this._comment;
    }
    
    public static void replaceAll(final AstNode tree) {
        for (final AstNode node : tree.getDescendants()) {
            if (node instanceof CommentStatement) {
                node.getParent().insertChildBefore(node, new Comment(((CommentStatement)node).getComment()), Roles.COMMENT);
                node.remove();
            }
        }
    }
    
    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return null;
    }
    
    @Override
    public boolean matches(final INode other, final Match match) {
        return other instanceof CommentStatement && AstNode.matchString(this._comment, ((CommentStatement)other)._comment);
    }
}
