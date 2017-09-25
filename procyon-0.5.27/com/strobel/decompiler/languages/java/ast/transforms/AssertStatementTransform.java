package com.strobel.decompiler.languages.java.ast.transforms;

import com.strobel.decompiler.*;
import com.strobel.core.*;
import com.strobel.decompiler.patterns.*;
import com.strobel.assembler.metadata.*;
import com.strobel.functions.*;
import com.strobel.decompiler.languages.java.ast.*;

public class AssertStatementTransform extends ContextTrackingVisitor<Void>
{
    private static final IfElseStatement ASSERT_PATTERN;
    private static final AssignmentExpression ASSERTIONS_DISABLED_PATTERN;
    
    static {
        ASSERT_PATTERN = new IfElseStatement(-34, new Choice(new INode[] { new UnaryOperatorExpression(UnaryOperatorType.NOT, new Choice(new INode[] { new BinaryOperatorExpression(new LeftmostBinaryOperandNode(new NamedNode("assertionsDisabledCheck", new TypeReferenceExpression(-34, new SimpleType("$any$")).member("$assertionsDisabled")), BinaryOperatorType.LOGICAL_OR, true).toExpression(), BinaryOperatorType.LOGICAL_OR, new AnyNode("condition").toExpression()), new TypeReferenceExpression(-34, new SimpleType("$any$")).member("$assertionsDisabled") }).toExpression()), new BinaryOperatorExpression(new LeftmostBinaryOperandNode(new UnaryOperatorExpression(UnaryOperatorType.NOT, new NamedNode("assertionsDisabledCheck", new TypeReferenceExpression(-34, new SimpleType("$any$")).member("$assertionsDisabled")).toExpression()), BinaryOperatorType.LOGICAL_AND, true).toExpression(), BinaryOperatorType.LOGICAL_AND, new AnyNode("invertedCondition").toExpression()) }).toExpression(), new BlockStatement(new Statement[] { new ThrowStatement(new ObjectCreationExpression(-34, new SimpleType("AssertionError"), new Expression[] { new OptionalNode(new AnyNode("message")).toExpression() })) }));
        ASSERTIONS_DISABLED_PATTERN = new AssignmentExpression(new NamedNode("$assertionsDisabled", new Choice(new INode[] { new IdentifierExpression(-34, "$assertionsDisabled"), new TypedNode(TypeReferenceExpression.class).toExpression().member("$assertionsDisabled") })).toExpression(), new UnaryOperatorExpression(UnaryOperatorType.NOT, new InvocationExpression(-34, new MemberReferenceExpression(-34, new NamedNode("type", new ClassOfExpression(-34, new SimpleType("$any$"))).toExpression(), "desiredAssertionStatus", new AstType[0]), new Expression[0])));
    }
    
    public AssertStatementTransform(final DecompilerContext context) {
        super(context);
    }
    
    @Override
    public Void visitIfElseStatement(final IfElseStatement node, final Void data) {
        super.visitIfElseStatement(node, data);
        this.transformAssert(node);
        return null;
    }
    
    @Override
    public Void visitAssignmentExpression(final AssignmentExpression node, final Void data) {
        super.visitAssignmentExpression(node, data);
        this.removeAssertionsDisabledAssignment(node);
        return null;
    }
    
    private void removeAssertionsDisabledAssignment(final AssignmentExpression node) {
        if (this.context.getSettings().getShowSyntheticMembers()) {
            return;
        }
        final Match m = AssertStatementTransform.ASSERTIONS_DISABLED_PATTERN.match(node);
        if (!m.success()) {
            return;
        }
        final AstNode parent = node.getParent();
        if (!(parent instanceof ExpressionStatement) || !(parent.getParent() instanceof BlockStatement) || !(parent.getParent().getParent() instanceof MethodDeclaration)) {
            return;
        }
        final MethodDeclaration staticInitializer = (MethodDeclaration)parent.getParent().getParent();
        final MethodDefinition methodDefinition = staticInitializer.getUserData(Keys.METHOD_DEFINITION);
        if (methodDefinition == null || !methodDefinition.isTypeInitializer()) {
            return;
        }
        final Expression field = CollectionUtilities.first(m.get("$assertionsDisabled"));
        final ClassOfExpression type = m.get("type").iterator().next();
        final MemberReference reference = field.getUserData(Keys.MEMBER_REFERENCE);
        if (!(reference instanceof FieldReference)) {
            return;
        }
        final FieldDefinition resolvedField = ((FieldReference)reference).resolve();
        if (resolvedField == null || !resolvedField.isSynthetic()) {
            return;
        }
        final TypeReference typeReference = type.getType().getUserData(Keys.TYPE_REFERENCE);
        if (typeReference != null && (MetadataResolver.areEquivalent(this.context.getCurrentType(), typeReference) || MetadataHelper.isEnclosedBy(this.context.getCurrentType(), typeReference))) {
            parent.remove();
            if (staticInitializer.getBody().getStatements().isEmpty()) {
                staticInitializer.remove();
            }
        }
    }
    
    private AssertStatement transformAssert(final IfElseStatement ifElse) {
        final Match m = AssertStatementTransform.ASSERT_PATTERN.match(ifElse);
        if (!m.success()) {
            return null;
        }
        final Expression assertionsDisabledCheck = CollectionUtilities.firstOrDefault(m.get("assertionsDisabledCheck"));
        Expression condition = CollectionUtilities.firstOrDefault(m.get("condition"));
        if (condition == null) {
            condition = CollectionUtilities.firstOrDefault(m.get("invertedCondition"));
            if (condition != null) {
                condition = condition.replaceWith((Function<? super AstNode, ? extends Expression>)new Function<AstNode, Expression>() {
                    @Override
                    public Expression apply(final AstNode input) {
                        return new UnaryOperatorExpression(UnaryOperatorType.NOT, (Expression)input);
                    }
                });
            }
        }
        if (condition != null && assertionsDisabledCheck != null && assertionsDisabledCheck.getParent() instanceof BinaryOperatorExpression && assertionsDisabledCheck.getParent().getParent() instanceof BinaryOperatorExpression) {
            final BinaryOperatorExpression logicalOr = (BinaryOperatorExpression)assertionsDisabledCheck.getParent();
            final Expression right = logicalOr.getRight();
            right.remove();
            assertionsDisabledCheck.replaceWith(right);
            condition.remove();
            logicalOr.setRight(condition);
            condition = logicalOr;
        }
        final AssertStatement assertStatement = new AssertStatement((condition == null) ? ifElse.getOffset() : condition.getOffset());
        if (condition != null) {
            condition.remove();
            assertStatement.setCondition(condition);
        }
        else {
            assertStatement.setCondition(new PrimitiveExpression(-34, false));
        }
        if (m.has("message")) {
            Expression message;
            for (message = CollectionUtilities.firstOrDefault(m.get("message")); message instanceof CastExpression; message = ((CastExpression)message).getExpression()) {}
            message.remove();
            assertStatement.setMessage(message);
        }
        ifElse.replaceWith(assertStatement);
        return assertStatement;
    }
}
