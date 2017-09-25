package com.strobel.decompiler.languages.java.ast;

import com.strobel.decompiler.patterns.*;

public class SwitchSection extends AstNode
{
    public static final Role<CaseLabel> CaseLabelRole;
    
    static {
        CaseLabelRole = new Role<CaseLabel>("CaseLabel", CaseLabel.class);
    }
    
    public final AstNodeCollection<Statement> getStatements() {
        return this.getChildrenByRole(Roles.EMBEDDED_STATEMENT);
    }
    
    public final AstNodeCollection<CaseLabel> getCaseLabels() {
        return this.getChildrenByRole(SwitchSection.CaseLabelRole);
    }
    
    @Override
    public NodeType getNodeType() {
        return NodeType.UNKNOWN;
    }
    
    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return (R)visitor.visitSwitchSection(this, (Object)data);
    }
    
    @Override
    public boolean matches(final INode other, final Match match) {
        if (other instanceof SwitchSection) {
            final SwitchSection otherSection = (SwitchSection)other;
            return !otherSection.isNull() && this.getCaseLabels().matches(otherSection.getCaseLabels(), match) && this.getStatements().matches(otherSection.getStatements(), match);
        }
        return false;
    }
}
