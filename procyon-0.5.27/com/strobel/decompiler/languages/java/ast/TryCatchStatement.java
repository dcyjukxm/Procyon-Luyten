package com.strobel.decompiler.languages.java.ast;

import com.strobel.decompiler.patterns.*;

public class TryCatchStatement extends Statement
{
    public static final TokenRole TRY_KEYWORD_ROLE;
    public static final Role<BlockStatement> TRY_BLOCK_ROLE;
    public static final Role<CatchClause> CATCH_CLAUSE_ROLE;
    public static final TokenRole FINALLY_KEYWORD_ROLE;
    public static final Role<BlockStatement> FINALLY_BLOCK_ROLE;
    public static final Role<VariableDeclarationStatement> TRY_RESOURCE_ROLE;
    
    static {
        TRY_KEYWORD_ROLE = new TokenRole("try", 1);
        TRY_BLOCK_ROLE = new Role<BlockStatement>("TryBlock", BlockStatement.class, BlockStatement.NULL);
        CATCH_CLAUSE_ROLE = new Role<CatchClause>("CatchClause", CatchClause.class);
        FINALLY_KEYWORD_ROLE = new TokenRole("finally", 1);
        FINALLY_BLOCK_ROLE = new Role<BlockStatement>("FinallyBlock", BlockStatement.class, BlockStatement.NULL);
        TRY_RESOURCE_ROLE = new Role<VariableDeclarationStatement>("TryResource", VariableDeclarationStatement.class);
    }
    
    public TryCatchStatement(final int offset) {
        super(offset);
    }
    
    public final JavaTokenNode getTryToken() {
        return this.getChildByRole((Role<JavaTokenNode>)TryCatchStatement.TRY_KEYWORD_ROLE);
    }
    
    public final JavaTokenNode getFinallyToken() {
        return this.getChildByRole((Role<JavaTokenNode>)TryCatchStatement.FINALLY_KEYWORD_ROLE);
    }
    
    public final AstNodeCollection<CatchClause> getCatchClauses() {
        return this.getChildrenByRole(TryCatchStatement.CATCH_CLAUSE_ROLE);
    }
    
    public final AstNodeCollection<VariableDeclarationStatement> getResources() {
        return this.getChildrenByRole(TryCatchStatement.TRY_RESOURCE_ROLE);
    }
    
    public final BlockStatement getTryBlock() {
        return this.getChildByRole(TryCatchStatement.TRY_BLOCK_ROLE);
    }
    
    public final void setTryBlock(final BlockStatement value) {
        this.setChildByRole(TryCatchStatement.TRY_BLOCK_ROLE, value);
    }
    
    public final BlockStatement getFinallyBlock() {
        return this.getChildByRole(TryCatchStatement.FINALLY_BLOCK_ROLE);
    }
    
    public final void setFinallyBlock(final BlockStatement value) {
        this.setChildByRole(TryCatchStatement.FINALLY_BLOCK_ROLE, value);
    }
    
    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return (R)visitor.visitTryCatchStatement(this, (Object)data);
    }
    
    @Override
    public boolean matches(final INode other, final Match match) {
        if (other instanceof TryCatchStatement) {
            final TryCatchStatement otherStatement = (TryCatchStatement)other;
            return !otherStatement.isNull() && this.getTryBlock().matches(otherStatement.getTryBlock(), match) && this.getCatchClauses().matches(otherStatement.getCatchClauses(), match) && this.getFinallyBlock().matches(otherStatement.getFinallyBlock(), match);
        }
        return false;
    }
}
