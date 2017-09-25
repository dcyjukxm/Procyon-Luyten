package com.strobel.decompiler.languages.java.ast.transforms;

import com.strobel.decompiler.*;
import com.strobel.core.*;
import com.strobel.decompiler.patterns.*;
import com.strobel.decompiler.languages.java.ast.*;
import javax.lang.model.element.*;
import com.strobel.decompiler.semantics.*;
import java.util.*;

public class TryWithResourcesTransform extends ContextTrackingVisitor<Void>
{
    private static final INode RESOURCE_INIT_PATTERN;
    private static final INode CLEAR_SAVED_EXCEPTION_PATTERN;
    private final TryCatchStatement _tryPattern;
    private final AstBuilder _astBuilder;
    private final JavaResolver _resolver;
    
    static {
        final Expression resource = new NamedNode("resource", new IdentifierExpression(-34, "$any$")).toExpression();
        final Expression savedException = new NamedNode("savedException", new IdentifierExpression(-34, "$any$")).toExpression();
        RESOURCE_INIT_PATTERN = new ExpressionStatement(new AssignmentExpression(resource, AssignmentOperatorType.ASSIGN, new AnyNode("resourceInitializer").toExpression()));
        CLEAR_SAVED_EXCEPTION_PATTERN = new ExpressionStatement(new AssignmentExpression(savedException, AssignmentOperatorType.ASSIGN, new NullReferenceExpression(-34)));
    }
    
    public TryWithResourcesTransform(final DecompilerContext context) {
        super(context);
        this._astBuilder = context.getUserData(Keys.AST_BUILDER);
        if (this._astBuilder == null) {
            this._tryPattern = null;
            this._resolver = null;
            return;
        }
        this._resolver = new JavaResolver(context);
        final TryCatchStatement tryPattern = new TryCatchStatement(-34);
        tryPattern.setTryBlock(new AnyNode("tryContent").toBlockStatement());
        final CatchClause catchClause = new CatchClause(new BlockStatement(new Statement[] { new ExpressionStatement(new AssignmentExpression(new IdentifierExpressionBackReference("savedException").toExpression(), new NamedNode("caughtException", new IdentifierExpression(-34, "$any$")).toExpression())), new ThrowStatement(new IdentifierExpressionBackReference("caughtException").toExpression()) }));
        catchClause.setVariableName("$any$");
        catchClause.getExceptionTypes().add(new SimpleType("Throwable"));
        tryPattern.getCatchClauses().add(catchClause);
        final TryCatchStatement disposeTry = new TryCatchStatement(-34);
        disposeTry.setTryBlock(new BlockStatement(new Statement[] { new ExpressionStatement(new IdentifierExpressionBackReference("resource").toExpression().invoke("close", new Expression[0])) }));
        final CatchClause disposeCatch = new CatchClause(new BlockStatement(new Statement[] { new ExpressionStatement(new IdentifierExpressionBackReference("savedException").toExpression().invoke("addSuppressed", new NamedNode("caughtOnClose", new IdentifierExpression(-34, "$any$")).toExpression())) }));
        disposeCatch.setVariableName("$any$");
        disposeCatch.getExceptionTypes().add(new SimpleType("Throwable"));
        disposeTry.getCatchClauses().add(disposeCatch);
        tryPattern.setFinallyBlock(new BlockStatement(new Statement[] { new IfElseStatement(-34, new BinaryOperatorExpression(new IdentifierExpressionBackReference("resource").toExpression(), BinaryOperatorType.INEQUALITY, new NullReferenceExpression(-34)), new BlockStatement(new Statement[] { new IfElseStatement(-34, new BinaryOperatorExpression(new IdentifierExpressionBackReference("savedException").toExpression(), BinaryOperatorType.INEQUALITY, new NullReferenceExpression(-34)), new BlockStatement(new Statement[] { disposeTry }), new BlockStatement(new Statement[] { new ExpressionStatement(new IdentifierExpressionBackReference("resource").toExpression().invoke("close", new Expression[0])) })) })) }));
        this._tryPattern = tryPattern;
    }
    
    @Override
    public void run(final AstNode compilationUnit) {
        if (this._tryPattern == null) {
            return;
        }
        super.run(compilationUnit);
        new MergeResourceTryStatementsVisitor(this.context).run(compilationUnit);
    }
    
    @Override
    public Void visitTryCatchStatement(final TryCatchStatement node, final Void data) {
        super.visitTryCatchStatement(node, data);
        if (!(node.getParent() instanceof BlockStatement)) {
            return null;
        }
        final BlockStatement parent = (BlockStatement)node.getParent();
        final Statement p = node.getPreviousSibling(BlockStatement.STATEMENT_ROLE);
        final Statement pp = (p != null) ? p.getPreviousSibling(BlockStatement.STATEMENT_ROLE) : null;
        if (pp == null) {
            return null;
        }
        final Statement initializeResource = pp;
        final Statement clearCaughtException = p;
        final Match m = Match.createNew();
        if (TryWithResourcesTransform.RESOURCE_INIT_PATTERN.matches(initializeResource, m) && TryWithResourcesTransform.CLEAR_SAVED_EXCEPTION_PATTERN.matches(clearCaughtException, m) && this._tryPattern.matches(node, m)) {
            final IdentifierExpression resource = CollectionUtilities.first(m.get("resource"));
            final ResolveResult resourceResult = this._resolver.apply((AstNode)resource);
            if (resourceResult == null || resourceResult.getType() == null) {
                return null;
            }
            final BlockStatement tryContent = CollectionUtilities.first(m.get("tryContent"));
            final Expression resourceInitializer = CollectionUtilities.first(m.get("resourceInitializer"));
            final IdentifierExpression caughtException = CollectionUtilities.first(m.get("caughtException"));
            final IdentifierExpression caughtOnClose = CollectionUtilities.first(m.get("caughtOnClose"));
            final CatchClause caughtParent = CollectionUtilities.first(caughtException.getAncestors(CatchClause.class));
            final CatchClause caughtOnCloseParent = CollectionUtilities.first(caughtOnClose.getAncestors(CatchClause.class));
            if (caughtParent == null || caughtOnCloseParent == null || !Pattern.matchString(caughtException.getIdentifier(), caughtParent.getVariableName()) || !Pattern.matchString(caughtOnClose.getIdentifier(), caughtOnCloseParent.getVariableName())) {
                return null;
            }
            final VariableDeclarationStatement resourceDeclaration = ConvertLoopsTransform.findVariableDeclaration(node, resource.getIdentifier());
            if (resourceDeclaration == null || !(resourceDeclaration.getParent() instanceof BlockStatement)) {
                return null;
            }
            final BlockStatement outerTemp = new BlockStatement();
            final BlockStatement temp = new BlockStatement();
            initializeResource.remove();
            clearCaughtException.remove();
            node.replaceWith(outerTemp);
            temp.add(initializeResource);
            temp.add(clearCaughtException);
            temp.add(node);
            outerTemp.add(temp);
            final Statement declarationPoint = ConvertLoopsTransform.canMoveVariableDeclarationIntoStatement(this.context, resourceDeclaration, node);
            node.remove();
            outerTemp.replaceWith(node);
            if (declarationPoint != outerTemp) {
                initializeResource.remove();
                clearCaughtException.remove();
                parent.insertChildBefore(node, initializeResource, BlockStatement.STATEMENT_ROLE);
                parent.insertChildBefore(node, clearCaughtException, BlockStatement.STATEMENT_ROLE);
                return null;
            }
            tryContent.remove();
            resource.remove();
            resourceInitializer.remove();
            final VariableDeclarationStatement newResourceDeclaration = new VariableDeclarationStatement(this._astBuilder.convertType(resourceResult.getType()), resource.getIdentifier(), resourceInitializer);
            final Statement firstStatement = CollectionUtilities.firstOrDefault(tryContent.getStatements());
            final Statement lastStatement = CollectionUtilities.lastOrDefault(tryContent.getStatements());
            if (firstStatement != null) {
                final DefiniteAssignmentAnalysis analysis = new DefiniteAssignmentAnalysis(this.context, tryContent);
                analysis.setAnalyzedRange(firstStatement, lastStatement);
                analysis.analyze(resource.getIdentifier(), DefiniteAssignmentStatus.DEFINITELY_NOT_ASSIGNED);
                if (!analysis.isPotentiallyAssigned()) {
                    newResourceDeclaration.addModifier(Modifier.FINAL);
                }
            }
            else {
                newResourceDeclaration.addModifier(Modifier.FINAL);
            }
            node.setTryBlock(tryContent);
            node.getResources().add(newResourceDeclaration);
            node.getCatchClauses().clear();
            node.setFinallyBlock(null);
        }
        return null;
    }
    
    private static final class MergeResourceTryStatementsVisitor extends ContextTrackingVisitor<Void>
    {
        MergeResourceTryStatementsVisitor(final DecompilerContext context) {
            super(context);
        }
        
        @Override
        public Void visitTryCatchStatement(final TryCatchStatement node, final Void data) {
            super.visitTryCatchStatement(node, data);
            if (node.getResources().isEmpty()) {
                return null;
            }
            final List<VariableDeclarationStatement> resources = new ArrayList<VariableDeclarationStatement>();
            TryCatchStatement current;
            TryCatchStatement parentTry;
            for (current = node; current.getCatchClauses().isEmpty() && current.getFinallyBlock().isNull(); current = parentTry) {
                final AstNode parent = current.getParent();
                if (!(parent instanceof BlockStatement) || !(parent.getParent() instanceof TryCatchStatement)) {
                    break;
                }
                parentTry = (TryCatchStatement)parent.getParent();
                if (!parentTry.getTryBlock().getStatements().hasSingleElement()) {
                    break;
                }
                if (!current.getResources().isEmpty()) {
                    resources.addAll(0, current.getResources());
                }
            }
            final BlockStatement tryContent = node.getTryBlock();
            if (current != node) {
                for (final VariableDeclarationStatement resource : resources) {
                    resource.remove();
                    current.getResources().add(resource);
                }
                tryContent.remove();
                current.setTryBlock(tryContent);
            }
            return null;
        }
    }
}
