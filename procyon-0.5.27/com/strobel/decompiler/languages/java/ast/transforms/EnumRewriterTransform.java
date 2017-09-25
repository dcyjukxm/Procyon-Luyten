package com.strobel.decompiler.languages.java.ast.transforms;

import com.strobel.decompiler.*;
import javax.lang.model.element.*;
import com.strobel.decompiler.patterns.*;
import java.util.*;
import com.strobel.assembler.metadata.*;
import com.strobel.decompiler.languages.java.ast.*;
import com.strobel.core.*;

public class EnumRewriterTransform implements IAstTransform
{
    private final DecompilerContext _context;
    
    public EnumRewriterTransform(final DecompilerContext context) {
        super();
        this._context = VerifyArgument.notNull(context, "context");
    }
    
    @Override
    public void run(final AstNode compilationUnit) {
        compilationUnit.acceptVisitor((IAstVisitor<? super Object, ?>)new Visitor(this._context), (Object)null);
    }
    
    private static final class Visitor extends ContextTrackingVisitor<Void>
    {
        private Map<String, FieldDeclaration> _valueFields;
        private Map<String, ObjectCreationExpression> _valueInitializers;
        private MemberReference _valuesField;
        
        protected Visitor(final DecompilerContext context) {
            super(context);
            this._valueFields = new LinkedHashMap<String, FieldDeclaration>();
            this._valueInitializers = new LinkedHashMap<String, ObjectCreationExpression>();
        }
        
        @Override
        public Void visitTypeDeclaration(final TypeDeclaration typeDeclaration, final Void _) {
            final MemberReference oldValuesField = this._valuesField;
            final Map<String, FieldDeclaration> oldValueFields = this._valueFields;
            final Map<String, ObjectCreationExpression> oldValueInitializers = this._valueInitializers;
            final LinkedHashMap<String, FieldDeclaration> valueFields = new LinkedHashMap<String, FieldDeclaration>();
            final LinkedHashMap<String, ObjectCreationExpression> valueInitializers = new LinkedHashMap<String, ObjectCreationExpression>();
            this._valuesField = this.findValuesField(typeDeclaration);
            this._valueFields = valueFields;
            this._valueInitializers = valueInitializers;
            try {
                super.visitTypeDeclaration(typeDeclaration, _);
            }
            finally {
                this._valuesField = oldValuesField;
                this._valueFields = oldValueFields;
                this._valueInitializers = oldValueInitializers;
            }
            this._valuesField = oldValuesField;
            this._valueFields = oldValueFields;
            this._valueInitializers = oldValueInitializers;
            this.rewrite(valueFields, valueInitializers);
            return null;
        }
        
        private MemberReference findValuesField(final TypeDeclaration declaration) {
            final TypeDefinition definition = declaration.getUserData(Keys.TYPE_DEFINITION);
            if (definition == null || !definition.isEnum()) {
                return null;
            }
            final AstBuilder astBuilder = this.context.getUserData(Keys.AST_BUILDER);
            if (astBuilder == null) {
                return null;
            }
            final MethodDeclaration pattern = new MethodDeclaration();
            pattern.setName("values");
            pattern.setReturnType(astBuilder.convertType(definition.makeArrayType()));
            pattern.getModifiers().add(new JavaModifierToken(Modifier.PUBLIC));
            pattern.getModifiers().add(new JavaModifierToken(Modifier.STATIC));
            pattern.setBody(new BlockStatement(new Statement[] { new ReturnStatement(-34, new Choice(new INode[] { new MemberReferenceExpression(-34, new NamedNode("valuesField", new TypeReferenceExpression(-34, astBuilder.convertType(definition)).member("$any$")).toExpression(), "clone", new AstType[0]).invoke(new Expression[0]), new CastExpression(astBuilder.convertType(definition.makeArrayType()), new MemberReferenceExpression(-34, new NamedNode("valuesField", new TypeReferenceExpression(-34, astBuilder.convertType(definition)).member("$any$")).toExpression(), "clone", new AstType[0]).invoke(new Expression[0])) }).toExpression()) }));
            for (final EntityDeclaration d : declaration.getMembers()) {
                if (d instanceof MethodDeclaration) {
                    final Match match = pattern.match(d);
                    if (match.success()) {
                        final MemberReferenceExpression reference = CollectionUtilities.firstOrDefault(match.get("valuesField"));
                        return reference.getUserData(Keys.MEMBER_REFERENCE);
                    }
                    continue;
                }
            }
            return null;
        }
        
        @Override
        public Void visitFieldDeclaration(final FieldDeclaration node, final Void data) {
            final TypeDefinition currentType = this.context.getCurrentType();
            if (currentType != null && currentType.isEnum()) {
                final FieldDefinition field = node.getUserData(Keys.FIELD_DEFINITION);
                if (field != null && field.isEnumConstant()) {
                    this._valueFields.put(field.getName(), node);
                }
            }
            return super.visitFieldDeclaration(node, data);
        }
        
        @Override
        public Void visitAssignmentExpression(final AssignmentExpression node, final Void data) {
            final TypeDefinition currentType = this.context.getCurrentType();
            final MethodDefinition currentMethod = this.context.getCurrentMethod();
            if (currentType != null && currentMethod != null && currentType.isEnum() && currentMethod.isTypeInitializer()) {
                final Expression left = node.getLeft();
                final Expression right = node.getRight();
                final MemberReference member = left.getUserData(Keys.MEMBER_REFERENCE);
                if (member instanceof FieldReference) {
                    final FieldDefinition resolvedField = ((FieldReference)member).resolve();
                    if (resolvedField != null && (right instanceof ObjectCreationExpression || right instanceof ArrayCreationExpression)) {
                        final String fieldName = resolvedField.getName();
                        if (resolvedField.isEnumConstant() && right instanceof ObjectCreationExpression && MetadataResolver.areEquivalent(currentType, resolvedField.getFieldType())) {
                            this._valueInitializers.put(fieldName, (ObjectCreationExpression)right);
                        }
                        else if (resolvedField.isSynthetic() && !this.context.getSettings().getShowSyntheticMembers() && this.matchesValuesField(resolvedField) && MetadataResolver.areEquivalent(currentType.makeArrayType(), resolvedField.getFieldType())) {
                            final Statement parentStatement = this.findStatement(node);
                            if (parentStatement != null) {
                                parentStatement.remove();
                            }
                        }
                    }
                }
            }
            return super.visitAssignmentExpression(node, data);
        }
        
        @Override
        public Void visitConstructorDeclaration(final ConstructorDeclaration node, final Void _) {
            final TypeDefinition currentType = this.context.getCurrentType();
            final MethodDefinition constructor = node.getUserData(Keys.METHOD_DEFINITION);
            if (currentType != null && currentType.isEnum()) {
                final List<ParameterDefinition> pDefinitions = constructor.getParameters();
                final AstNodeCollection<ParameterDeclaration> pDeclarations = node.getParameters();
                for (int i = 0; i < pDefinitions.size() && i < pDeclarations.size() && pDefinitions.get(i).isSynthetic(); ++i) {
                    pDeclarations.firstOrNullObject().remove();
                }
                final AstNodeCollection<Statement> statements = node.getBody().getStatements();
                final Statement firstStatement = statements.firstOrNullObject();
                if (firstStatement instanceof ExpressionStatement) {
                    final Expression e = ((ExpressionStatement)firstStatement).getExpression();
                    if (e instanceof InvocationExpression && ((InvocationExpression)e).getTarget() instanceof SuperReferenceExpression) {
                        firstStatement.remove();
                    }
                }
                if (statements.isEmpty()) {
                    node.remove();
                }
                else if (currentType.isAnonymous()) {
                    final InstanceInitializer initializer = new InstanceInitializer();
                    final BlockStatement initializerBody = new BlockStatement();
                    for (final Statement statement : statements) {
                        statement.remove();
                        initializerBody.add(statement);
                    }
                    initializer.setBody(initializerBody);
                    node.replaceWith(initializer);
                }
            }
            return super.visitConstructorDeclaration(node, _);
        }
        
        @Override
        public Void visitMethodDeclaration(final MethodDeclaration node, final Void _) {
            final TypeDefinition currentType = this.context.getCurrentType();
            if (currentType != null && currentType.isEnum() && !this.context.getSettings().getShowSyntheticMembers()) {
                final MethodDefinition method = node.getUserData(Keys.METHOD_DEFINITION);
                if (method != null && method.isPublic() && method.isStatic()) {
                    final String loc_0;
                    switch (loc_0 = method.getName()) {
                        case "values": {
                            if (method.getParameters().isEmpty() && MetadataResolver.areEquivalent(currentType.makeArrayType(), method.getReturnType())) {
                                node.remove();
                                break;
                            }
                            break;
                        }
                        case "valueOf": {
                            if (!currentType.equals(method.getReturnType().resolve()) || method.getParameters().size() != 1) {
                                break;
                            }
                            final ParameterDefinition p = method.getParameters().get(0);
                            if ("java/lang/String".equals(p.getParameterType().getInternalName())) {
                                node.remove();
                                break;
                            }
                            break;
                        }
                        default:
                            break;
                    }
                }
            }
            return super.visitMethodDeclaration(node, _);
        }
        
        private void rewrite(final LinkedHashMap<String, FieldDeclaration> valueFields, final LinkedHashMap<String, ObjectCreationExpression> valueInitializers) {
            if (valueFields.isEmpty() || valueFields.size() != valueInitializers.size()) {
                return;
            }
            final MethodDeclaration typeInitializer = this.findMethodDeclaration(CollectionUtilities.first(valueInitializers.values()));
            for (final String name : valueFields.keySet()) {
                final FieldDeclaration field = valueFields.get(name);
                final ObjectCreationExpression initializer = valueInitializers.get(name);
                assert field != null && initializer != null;
                final MethodReference constructor = initializer.getUserData(Keys.MEMBER_REFERENCE);
                final MethodDefinition resolvedConstructor = constructor.resolve();
                final EnumValueDeclaration enumDeclaration = new EnumValueDeclaration();
                final Statement initializerStatement = this.findStatement(initializer);
                assert initializerStatement != null;
                initializerStatement.remove();
                enumDeclaration.setName(name);
                enumDeclaration.putUserData(Keys.FIELD_DEFINITION, (FieldDefinition)field.getUserData(Keys.FIELD_DEFINITION));
                enumDeclaration.putUserData(Keys.MEMBER_REFERENCE, (MemberReference)field.getUserData(Keys.MEMBER_REFERENCE));
                if (resolvedConstructor != null) {
                    enumDeclaration.putUserData(Keys.TYPE_DEFINITION, resolvedConstructor.getDeclaringType());
                }
                int i = 0;
                final AstNodeCollection<Expression> arguments = initializer.getArguments();
                final boolean trimArguments = arguments.size() == constructor.getParameters().size();
                for (final Expression argument : arguments) {
                    if (trimArguments && resolvedConstructor != null && resolvedConstructor.isSynthetic() && i++ < 2) {
                        continue;
                    }
                    argument.remove();
                    enumDeclaration.getArguments().add(argument);
                }
                if (initializer instanceof AnonymousObjectCreationExpression) {
                    final AnonymousObjectCreationExpression creation = (AnonymousObjectCreationExpression)initializer;
                    for (final EntityDeclaration member : creation.getTypeDeclaration().getMembers()) {
                        member.remove();
                        enumDeclaration.getMembers().add(member);
                    }
                }
                field.replaceWith(enumDeclaration);
            }
            if (typeInitializer != null && typeInitializer.getBody().getStatements().isEmpty()) {
                typeInitializer.remove();
            }
        }
        
        private Statement findStatement(final AstNode node) {
            for (AstNode current = node; current != null; current = current.getParent()) {
                if (current instanceof Statement) {
                    return (Statement)current;
                }
            }
            return null;
        }
        
        private MethodDeclaration findMethodDeclaration(final AstNode node) {
            for (AstNode current = node; current != null; current = current.getParent()) {
                if (current instanceof MethodDeclaration) {
                    return (MethodDeclaration)current;
                }
            }
            return null;
        }
        
        private boolean matchesValuesField(final FieldDefinition field) {
            if (field == null) {
                return false;
            }
            if (field.isEquivalentTo(this._valuesField)) {
                return true;
            }
            final String fieldName = field.getName();
            return StringUtilities.equals(fieldName, "$VALUES") || StringUtilities.equals(fieldName, "ENUM$VALUES");
        }
    }
}
