package com.strobel.decompiler.languages.java.ast;

import com.strobel.core.*;
import com.strobel.decompiler.patterns.*;

public class CatchClause extends AstNode
{
    public static final TokenRole CATCH_KEYWORD_ROLE;
    
    static {
        CATCH_KEYWORD_ROLE = new TokenRole("catch", 1);
    }
    
    public CatchClause() {
        super();
    }
    
    public CatchClause(final BlockStatement body) {
        super();
        this.setBody(body);
    }
    
    public final JavaTokenNode getCatchToken() {
        return this.getChildByRole((Role<JavaTokenNode>)CatchClause.CATCH_KEYWORD_ROLE);
    }
    
    public final JavaTokenNode getLeftParenthesisToken() {
        return this.getChildByRole((Role<JavaTokenNode>)Roles.LEFT_PARENTHESIS);
    }
    
    public final JavaTokenNode getRightParenthesisToken() {
        return this.getChildByRole((Role<JavaTokenNode>)Roles.RIGHT_PARENTHESIS);
    }
    
    public final AstNodeCollection<AstType> getExceptionTypes() {
        return this.getChildrenByRole(Roles.TYPE);
    }
    
    public final String getVariableName() {
        return this.getChildByRole(Roles.IDENTIFIER).getName();
    }
    
    public final void setVariableName(final String value) {
        this.setChildByRole(Roles.IDENTIFIER, Identifier.create(value));
    }
    
    public final Identifier getVariableNameToken() {
        return this.getChildByRole(Roles.IDENTIFIER);
    }
    
    public final void setVariableNameToken(final Identifier value) {
        this.setChildByRole(Roles.IDENTIFIER, value);
    }
    
    public final BlockStatement getBody() {
        return this.getChildByRole(Roles.BODY);
    }
    
    public final void setBody(final BlockStatement value) {
        this.setChildByRole(Roles.BODY, value);
    }
    
    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return (R)visitor.visitCatchClause(this, (Object)data);
    }
    
    @Override
    public NodeType getNodeType() {
        return NodeType.UNKNOWN;
    }
    
    @Override
    public boolean matches(final INode other, final Match match) {
        if (other instanceof CatchClause) {
            final CatchClause otherClause = (CatchClause)other;
            return !otherClause.isNull() && this.getExceptionTypes().matches(otherClause.getExceptionTypes(), match) && AstNode.matchString(this.getVariableName(), otherClause.getVariableName()) && this.getBody().matches(otherClause.getBody(), match);
        }
        return false;
    }
    
    public static CatchClause forPattern(final Pattern pattern) {
        return new PatternPlaceholder(VerifyArgument.notNull(pattern, "pattern"));
    }
    
    private static final class PatternPlaceholder extends CatchClause
    {
        final Pattern child;
        
        PatternPlaceholder(final Pattern child) {
            super();
            this.child = child;
        }
        
        @Override
        public NodeType getNodeType() {
            return NodeType.PATTERN;
        }
        
        @Override
        public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
            return (R)visitor.visitPatternPlaceholder((AstNode)this, this.child, (Object)data);
        }
        
        @Override
        public boolean matchesCollection(final Role role, final INode position, final Match match, final BacktrackingInfo backtrackingInfo) {
            return this.child.matchesCollection(role, position, match, backtrackingInfo);
        }
        
        @Override
        public boolean matches(final INode other, final Match match) {
            return this.child.matches(other, match);
        }
    }
}
