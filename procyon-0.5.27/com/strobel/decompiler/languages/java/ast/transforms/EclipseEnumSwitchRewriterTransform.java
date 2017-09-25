package com.strobel.decompiler.languages.java.ast.transforms;

import com.strobel.decompiler.*;
import com.strobel.decompiler.patterns.*;
import com.strobel.decompiler.languages.java.ast.*;
import com.strobel.core.*;
import com.strobel.assembler.metadata.*;
import java.util.*;

public class EclipseEnumSwitchRewriterTransform implements IAstTransform
{
    private final DecompilerContext _context;
    
    public EclipseEnumSwitchRewriterTransform(final DecompilerContext context) {
        super();
        this._context = VerifyArgument.notNull(context, "context");
    }
    
    @Override
    public void run(final AstNode compilationUnit) {
        final Visitor visitor = new Visitor(this._context);
        compilationUnit.acceptVisitor((IAstVisitor<? super Object, ?>)visitor, (Object)null);
        Visitor.access$3(visitor);
    }
    
    private static final class Visitor extends ContextTrackingVisitor<Void>
    {
        private final Map<String, SwitchMapInfo> _switchMaps;
        private static final INode SWITCH_INPUT;
        private static final INode SWITCH_TABLE_METHOD_BODY;
        
        static {
            final SimpleType intType = new SimpleType("int");
            intType.putUserData(Keys.TYPE_REFERENCE, BuiltinTypes.Integer);
            final AstType intArrayType = new ComposedType(intType).makeArrayType();
            final BlockStatement body = new BlockStatement();
            final VariableDeclarationStatement v1 = new VariableDeclarationStatement(intArrayType, "$any$", -34);
            final VariableDeclarationStatement v2 = new VariableDeclarationStatement(intArrayType.clone(), "$any$", -34);
            body.add(new NamedNode("v1", v1).toStatement());
            body.add(new NamedNode("v2", v2).toStatement());
            body.add(new ExpressionStatement(new AssignmentExpression(new DeclaredVariableBackReference("v1").toExpression(), new MemberReferenceExpressionRegexNode("fieldAccess", new TypedNode(TypeReferenceExpression.class), "\\$SWITCH_TABLE\\$.*").toExpression())));
            body.add(new IfElseStatement(-34, new BinaryOperatorExpression(new DeclaredVariableBackReference("v1").toExpression(), BinaryOperatorType.INEQUALITY, new NullReferenceExpression(-34)), new BlockStatement(new Statement[] { new ReturnStatement(-34, new DeclaredVariableBackReference("v1").toExpression()) })));
            final ArrayCreationExpression arrayCreation = new ArrayCreationExpression(-34);
            final Expression dimension = new MemberReferenceExpression(-34, new InvocationExpression(-34, new MemberReferenceExpression(-34, new Choice(new INode[] { new TypedNode("enumType", TypeReferenceExpression.class), Expression.NULL }).toExpression(), "values", new AstType[0]), new Expression[0]), "length", new AstType[0]);
            arrayCreation.setType(intType.clone());
            arrayCreation.getDimensions().add(dimension);
            body.add(new AssignmentExpression(new DeclaredVariableBackReference("v2").toExpression(), arrayCreation));
            final ExpressionStatement assignment = new ExpressionStatement(new AssignmentExpression(new IndexerExpression(-34, new DeclaredVariableBackReference("v2").toExpression(), new InvocationExpression(-34, new MemberReferenceExpression(-34, new NamedNode("enumValue", new MemberReferenceExpression(-34, new TypedNode(TypeReferenceExpression.class).toExpression(), "$any$", new AstType[0])).toExpression(), "ordinal", new AstType[0]), new Expression[0])), new TypedPrimitiveValueNode("tableValue", Integer.class).toExpression()));
            final TryCatchStatement tryCatch = new TryCatchStatement(-34);
            final CatchClause catchClause = new CatchClause(new BlockStatement());
            catchClause.setVariableName("$any$");
            catchClause.getExceptionTypes().add(new SimpleType("NoSuchFieldError"));
            tryCatch.setTryBlock(new BlockStatement(new Statement[] { assignment.clone() }));
            tryCatch.getCatchClauses().add(catchClause);
            body.add(new Repeat(tryCatch).toStatement());
            body.add(new ExpressionStatement(new AssignmentExpression(new BackReference("fieldAccess").toExpression(), new DeclaredVariableBackReference("v2").toExpression())));
            body.add(new ReturnStatement(-34, new DeclaredVariableBackReference("v2").toExpression()));
            SWITCH_TABLE_METHOD_BODY = body;
            SWITCH_INPUT = new IndexerExpression(-34, new NamedNode("switchMapMethodCall", new InvocationExpression(-34, new MemberReferenceExpressionRegexNode(Expression.NULL, "\\$SWITCH_TABLE\\$.*").toExpression(), new Expression[0])).toExpression(), new NamedNode("ordinalCall", new InvocationExpression(-34, new MemberReferenceExpression(-34, new AnyNode("target").toExpression(), "ordinal", new AstType[0]), new Expression[0])).toExpression());
        }
        
        protected Visitor(final DecompilerContext context) {
            super(context);
            this._switchMaps = new LinkedHashMap<String, SwitchMapInfo>();
        }
        
        @Override
        public Void visitSwitchStatement(final SwitchStatement node, final Void data) {
            final TypeDefinition currentType = this.context.getCurrentType();
            if (currentType == null) {
                return super.visitSwitchStatement(node, data);
            }
            final Expression test = node.getExpression();
            final Match m = Visitor.SWITCH_INPUT.match(test);
            if (m.success()) {
                final InvocationExpression switchMapMethodCall = CollectionUtilities.first(m.get("switchMapMethodCall"));
                final MethodReference switchMapMethod = switchMapMethodCall.getUserData(Keys.MEMBER_REFERENCE);
                if (!isSwitchMapMethod(switchMapMethod)) {
                    return super.visitSwitchStatement(node, data);
                }
                FieldDefinition switchMapField;
                try {
                    final FieldReference r = new MetadataParser(currentType.getResolver()).parseField(currentType, switchMapMethod.getName(), switchMapMethod.getReturnType().getErasedSignature());
                    switchMapField = r.resolve();
                }
                catch (Throwable t) {
                    return super.visitSwitchStatement(node, data);
                }
                final String key = makeKey(switchMapField);
                SwitchMapInfo info = this._switchMaps.get(key);
                if (info == null) {
                    this._switchMaps.put(key, info = new SwitchMapInfo(switchMapField));
                }
                info.switches.add(node);
            }
            return super.visitSwitchStatement(node, data);
        }
        
        @Override
        public Void visitFieldDeclaration(final FieldDeclaration node, final Void data) {
            final FieldReference field = node.getUserData(Keys.MEMBER_REFERENCE);
            if (isSwitchMapField(field)) {
                final String key = makeKey(field);
                SwitchMapInfo info = this._switchMaps.get(key);
                if (info == null) {
                    this._switchMaps.put(key, info = new SwitchMapInfo(field));
                }
                info.switchMapFieldDeclaration = node;
            }
            return super.visitFieldDeclaration(node, data);
        }
        
        @Override
        public Void visitMethodDeclaration(final MethodDeclaration node, final Void _) {
            final MethodDefinition methodDefinition = node.getUserData(Keys.METHOD_DEFINITION);
            if (isSwitchMapMethod(methodDefinition)) {
                final Match m = Visitor.SWITCH_TABLE_METHOD_BODY.match(node.getBody());
                if (m.success()) {
                    final MemberReferenceExpression fieldAccess = CollectionUtilities.first(m.get("fieldAccess"));
                    final FieldReference field = fieldAccess.getUserData(Keys.MEMBER_REFERENCE);
                    final List<MemberReferenceExpression> enumValues = CollectionUtilities.toList(m.get("enumValue"));
                    final List<PrimitiveExpression> tableValues = CollectionUtilities.toList(m.get("tableValue"));
                    assert field != null && tableValues.size() == enumValues.size();
                    final String key = makeKey(field);
                    SwitchMapInfo info = this._switchMaps.get(key);
                    if (info == null) {
                        this._switchMaps.put(key, info = new SwitchMapInfo(field));
                    }
                    info.switchMapMethodDeclaration = node;
                    for (int i = 0; i < enumValues.size(); ++i) {
                        final MemberReferenceExpression memberReference = enumValues.get(i);
                        final IdentifierExpression identifier = new IdentifierExpression(-34, memberReference.getMemberName());
                        identifier.putUserData(Keys.MEMBER_REFERENCE, (MemberReference)memberReference.getUserData(Keys.MEMBER_REFERENCE));
                        info.mappings.put((Integer)tableValues.get(i).getValue(), identifier);
                    }
                }
            }
            return super.visitMethodDeclaration(node, _);
        }
        
        private void rewrite() {
            if (this._switchMaps.isEmpty()) {
                return;
            }
            for (final SwitchMapInfo info : this._switchMaps.values()) {
                this.rewrite(info);
            }
            for (final SwitchMapInfo info : this._switchMaps.values()) {
                if (info.switchMapMethod != null && info.switchMapFieldDeclaration != null) {
                    if (info.switchMapMethodDeclaration == null) {
                        continue;
                    }
                    final List<SwitchStatement> switches = info.switches;
                    if (!switches.isEmpty() || this.context.getSettings().getShowSyntheticMembers()) {
                        continue;
                    }
                    info.switchMapFieldDeclaration.remove();
                    info.switchMapMethodDeclaration.remove();
                }
            }
        }
        
        private void rewrite(final SwitchMapInfo info) {
            if (info.switches.isEmpty()) {
                return;
            }
            final List<SwitchStatement> switches = info.switches;
            final Map<Integer, Expression> mappings = info.mappings;
            for (int i = 0; i < switches.size(); ++i) {
                if (this.rewriteSwitch(switches.get(i), mappings)) {
                    switches.remove(i--);
                }
            }
        }
        
        private boolean rewriteSwitch(final SwitchStatement s, final Map<Integer, Expression> mappings) {
            final Match m = Visitor.SWITCH_INPUT.match(s.getExpression());
            if (!m.success()) {
                return false;
            }
            final Map<Expression, Expression> replacements = new IdentityHashMap<Expression, Expression>();
            for (final SwitchSection section : s.getSwitchSections()) {
                for (final CaseLabel caseLabel : section.getCaseLabels()) {
                    final Expression expression = caseLabel.getExpression();
                    if (expression != null) {
                        if (expression.isNull()) {
                            continue;
                        }
                        if (expression instanceof PrimitiveExpression) {
                            final Object value = ((PrimitiveExpression)expression).getValue();
                            if (value instanceof Integer) {
                                final Expression replacement = mappings.get(value);
                                if (replacement != null) {
                                    replacements.put(expression, replacement);
                                    continue;
                                }
                            }
                        }
                        return false;
                    }
                }
            }
            final Expression newTest = CollectionUtilities.first(m.get("target"));
            newTest.remove();
            s.getExpression().replaceWith(newTest);
            for (final Map.Entry<Expression, Expression> entry : replacements.entrySet()) {
                entry.getKey().replaceWith(entry.getValue().clone());
            }
            return true;
        }
        
        private static boolean isSwitchMapMethod(final MethodReference method) {
            if (method == null) {
                return false;
            }
            final MethodDefinition definition = (MethodDefinition)((method instanceof MethodDefinition) ? method : method.resolve());
            return definition != null && definition.isSynthetic() && definition.isStatic() && definition.isPackagePrivate() && StringUtilities.startsWith(definition.getName(), "$SWITCH_TABLE$") && MetadataResolver.areEquivalent(BuiltinTypes.Integer.makeArrayType(), definition.getReturnType());
        }
        
        private static boolean isSwitchMapField(final FieldReference field) {
            if (field == null) {
                return false;
            }
            final FieldDefinition definition = (FieldDefinition)((field instanceof FieldDefinition) ? field : field.resolve());
            return definition != null && definition.isSynthetic() && definition.isStatic() && definition.isPrivate() && StringUtilities.startsWith(definition.getName(), "$SWITCH_TABLE$") && MetadataResolver.areEquivalent(BuiltinTypes.Integer.makeArrayType(), definition.getFieldType());
        }
        
        private static String makeKey(final FieldReference field) {
            return String.valueOf(field.getFullName()) + ":" + field.getErasedSignature();
        }
        
        static /* synthetic */ void access$3(final Visitor param_0) {
            param_0.rewrite();
        }
        
        private static final class SwitchMapInfo
        {
            final FieldReference switchMapField;
            final List<SwitchStatement> switches;
            final Map<Integer, Expression> mappings;
            MethodReference switchMapMethod;
            MethodDeclaration switchMapMethodDeclaration;
            FieldDeclaration switchMapFieldDeclaration;
            
            SwitchMapInfo(final FieldReference switchMapField) {
                super();
                this.switches = new ArrayList<SwitchStatement>();
                this.mappings = new LinkedHashMap<Integer, Expression>();
                this.switchMapField = switchMapField;
            }
        }
    }
}
