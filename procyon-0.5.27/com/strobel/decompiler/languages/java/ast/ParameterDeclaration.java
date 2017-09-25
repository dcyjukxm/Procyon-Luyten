package com.strobel.decompiler.languages.java.ast;

import com.strobel.decompiler.languages.*;
import com.strobel.core.*;
import com.strobel.decompiler.patterns.*;

public class ParameterDeclaration extends EntityDeclaration
{
    public static final Role<Annotation> ANNOTATION_ROLE;
    
    static {
        ANNOTATION_ROLE = EntityDeclaration.ANNOTATION_ROLE;
    }
    
    public ParameterDeclaration() {
        super();
    }
    
    public ParameterDeclaration(final String name, final AstType type) {
        super();
        this.setName(name);
        this.setType(type);
    }
    
    public final AstType getType() {
        return this.getChildByRole(Roles.TYPE);
    }
    
    public final void setType(final AstType value) {
        this.setChildByRole(Roles.TYPE, value);
    }
    
    @Override
    public NodeType getNodeType() {
        return NodeType.UNKNOWN;
    }
    
    @Override
    public EntityType getEntityType() {
        return EntityType.PARAMETER;
    }
    
    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return (R)visitor.visitParameterDeclaration(this, (Object)data);
    }
    
    @Override
    public boolean matches(final INode other, final Match match) {
        if (other instanceof ParameterDeclaration) {
            final ParameterDeclaration otherDeclaration = (ParameterDeclaration)other;
            return !otherDeclaration.isNull() && this.matchAnnotationsAndModifiers(otherDeclaration, match) && AstNode.matchString(this.getName(), otherDeclaration.getName()) && this.getType().matches(otherDeclaration.getType(), match);
        }
        return false;
    }
    
    public static ParameterDeclaration forPattern(final Pattern pattern) {
        return new PatternPlaceholder(VerifyArgument.notNull(pattern, "pattern"));
    }
    
    private static final class PatternPlaceholder extends ParameterDeclaration
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
