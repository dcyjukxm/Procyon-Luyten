package com.strobel.decompiler.languages.java.ast;

import com.strobel.decompiler.patterns.*;
import com.strobel.core.*;

public class Comment extends AstNode
{
    private CommentType _commentType;
    private boolean _startsLine;
    private String _content;
    
    public Comment(final String content) {
        this(content, CommentType.SingleLine);
    }
    
    public Comment(final String content, final CommentType commentType) {
        super();
        this._commentType = commentType;
        this._content = content;
    }
    
    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return (R)visitor.visitComment(this, (Object)data);
    }
    
    @Override
    public NodeType getNodeType() {
        return NodeType.WHITESPACE;
    }
    
    public final CommentType getCommentType() {
        return this._commentType;
    }
    
    public final void setCommentType(final CommentType commentType) {
        this.verifyNotFrozen();
        this._commentType = commentType;
    }
    
    public final boolean getStartsLine() {
        return this._startsLine;
    }
    
    public final void setStartsLine(final boolean startsLine) {
        this.verifyNotFrozen();
        this._startsLine = startsLine;
    }
    
    public final String getContent() {
        return this._content;
    }
    
    public final void setContent(final String content) {
        this.verifyNotFrozen();
        this._content = content;
    }
    
    @Override
    public boolean matches(final INode other, final Match match) {
        if (other instanceof Comment) {
            final Comment otherComment = (Comment)other;
            return otherComment._commentType == this._commentType && Comparer.equals(otherComment._content, this._content);
        }
        return false;
    }
}
