package com.strobel.decompiler.languages.java.ast;

import java.util.*;
import com.strobel.decompiler.patterns.*;

public class MemberReferenceExpression extends Expression
{
    public MemberReferenceExpression(final int offset, final Expression target, final String memberName, final Iterable<AstType> typeArguments) {
        super(offset);
        this.addChild(target, Roles.TARGET_EXPRESSION);
        this.setMemberName(memberName);
        if (typeArguments != null) {
            for (final AstType argument : typeArguments) {
                this.addChild(argument, Roles.TYPE_ARGUMENT);
            }
        }
    }
    
    public MemberReferenceExpression(final int offset, final Expression target, final String memberName, final AstType... typeArguments) {
        super(offset);
        this.addChild(target, Roles.TARGET_EXPRESSION);
        this.setMemberName(memberName);
        if (typeArguments != null) {
            for (final AstType argument : typeArguments) {
                this.addChild(argument, Roles.TYPE_ARGUMENT);
            }
        }
    }
    
    public final String getMemberName() {
        return this.getChildByRole(Roles.IDENTIFIER).getName();
    }
    
    public final void setMemberName(final String name) {
        this.setChildByRole(Roles.IDENTIFIER, Identifier.create(name));
    }
    
    public final Identifier getMemberNameToken() {
        return this.getChildByRole(Roles.IDENTIFIER);
    }
    
    public final void setMemberNameToken(final Identifier token) {
        this.setChildByRole(Roles.IDENTIFIER, token);
    }
    
    public final Expression getTarget() {
        return this.getChildByRole(Roles.TARGET_EXPRESSION);
    }
    
    public final void setTarget(final Expression value) {
        this.setChildByRole(Roles.TARGET_EXPRESSION, value);
    }
    
    public final AstNodeCollection<AstType> getTypeArguments() {
        return this.getChildrenByRole(Roles.TYPE_ARGUMENT);
    }
    
    public final JavaTokenNode getDotToken() {
        return this.getChildByRole((Role<JavaTokenNode>)Roles.DOT);
    }
    
    public final JavaTokenNode getLeftChevronToken() {
        return this.getChildByRole((Role<JavaTokenNode>)Roles.LEFT_CHEVRON);
    }
    
    public final JavaTokenNode getRightChevronToken() {
        return this.getChildByRole((Role<JavaTokenNode>)Roles.RIGHT_CHEVRON);
    }
    
    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return (R)visitor.visitMemberReferenceExpression(this, (Object)data);
    }
    
    @Override
    public boolean matches(final INode other, final Match match) {
        if (other instanceof MemberReferenceExpression) {
            final MemberReferenceExpression otherExpression = (MemberReferenceExpression)other;
            return !otherExpression.isNull() && this.getTarget().matches(otherExpression.getTarget(), match) && AstNode.matchString(this.getMemberName(), otherExpression.getMemberName()) && this.getTypeArguments().matches(otherExpression.getTypeArguments(), match);
        }
        return false;
    }
}
