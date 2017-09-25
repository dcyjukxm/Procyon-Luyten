package com.strobel.decompiler.languages.java.ast;

import com.strobel.decompiler.languages.*;
import com.strobel.decompiler.patterns.*;
import com.strobel.core.*;

public class TextNode extends AstNode
{
    private String _text;
    private TextLocation _startLocation;
    private TextLocation _endLocation;
    
    public TextNode() {
        super();
    }
    
    public TextNode(final String text) {
        this(text, TextLocation.EMPTY, TextLocation.EMPTY);
    }
    
    public TextNode(final String text, final TextLocation startLocation, final TextLocation endLocation) {
        super();
        this._text = text;
        this._startLocation = startLocation;
        this._endLocation = endLocation;
    }
    
    @Override
    public final String getText() {
        return this._text;
    }
    
    public final void setText(final String text) {
        this.verifyNotFrozen();
        this._text = text;
    }
    
    @Override
    public final TextLocation getStartLocation() {
        return this._startLocation;
    }
    
    public final void setStartLocation(final TextLocation startLocation) {
        this.verifyNotFrozen();
        this._startLocation = startLocation;
    }
    
    @Override
    public final TextLocation getEndLocation() {
        return this._endLocation;
    }
    
    public final void setEndLocation(final TextLocation endLocation) {
        this.verifyNotFrozen();
        this._endLocation = endLocation;
    }
    
    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return (R)visitor.visitText(this, (Object)data);
    }
    
    @Override
    public NodeType getNodeType() {
        return NodeType.WHITESPACE;
    }
    
    @Override
    public boolean matches(final INode other, final Match match) {
        return other instanceof TextNode && StringUtilities.equals(this.getText(), ((TextNode)other).getText());
    }
}
