package com.strobel.decompiler.languages.java.ast;

import com.strobel.core.*;
import java.util.*;
import com.strobel.decompiler.patterns.*;
import com.strobel.decompiler.ast.*;
import com.strobel.assembler.metadata.*;

public final class JavaNameResolver
{
    public static NameResolveResult resolve(final String name, final AstNode node) {
        return new Result(NameResolveMode.EXPRESSION, resolveCore(node, name, NameResolveMode.EXPRESSION));
    }
    
    public static NameResolveResult resolveAsType(final String name, final AstNode node) {
        return new Result(NameResolveMode.TYPE, resolveCore(node, name, NameResolveMode.TYPE));
    }
    
    private static List<Object> resolveCore(final AstNode location, final String name, final NameResolveMode mode) {
        final Set<Object> results = FindDeclarationVisitor.resolveName(location, name, mode);
        if (results.isEmpty()) {
            return ReadOnlyList.emptyList();
        }
        return new ReadOnlyList<Object>(Object.class, results);
    }
    
    private static final class FindDeclarationVisitor implements IAstVisitor<String, Set<Object>>
    {
        private final NameResolveMode _mode;
        private boolean _isStaticContext;
        
        FindDeclarationVisitor(final NameResolveMode mode, final boolean isStaticContext) {
            super();
            this._isStaticContext = false;
            this._mode = VerifyArgument.notNull(mode, "mode");
            this._isStaticContext = isStaticContext;
        }
        
        static Set<Object> resolveName(final AstNode node, final String name, final NameResolveMode mode) {
            VerifyArgument.notNull(node, "node");
            VerifyArgument.notNull(name, "name");
            VerifyArgument.notNull(mode, "mode");
            AstNode n = node;
            Set<Object> results = null;
            while (n instanceof Expression) {
                n = n.getParent();
            }
            if (n == null || n.isNull()) {
                return Collections.emptySet();
            }
            TypeDeclaration lastTypeDeclaration = null;
            final FindDeclarationVisitor visitor = new FindDeclarationVisitor(mode, isStaticContext(node));
            while (n != null && !n.isNull()) {
                if (n instanceof CompilationUnit) {
                    final Set<Object> unitResults = n.acceptVisitor((IAstVisitor<? super String, ? extends Set<Object>>)visitor, name);
                    if (!unitResults.isEmpty()) {
                        if (results == null) {
                            return unitResults;
                        }
                        results.addAll(unitResults);
                    }
                }
                AstNode parent = n.getParent();
                if (n instanceof MethodDeclaration) {
                    final Set<Object> methodResults = n.acceptVisitor((IAstVisitor<? super String, ? extends Set<Object>>)visitor, name);
                    if (!methodResults.isEmpty()) {
                        if (results == null) {
                            results = new LinkedHashSet<Object>();
                        }
                        results.addAll(methodResults);
                    }
                    final MethodDefinition method = n.getUserData(Keys.METHOD_DEFINITION);
                    if (method != null) {
                        visitor._isStaticContext = method.isStatic();
                    }
                }
                else if (n instanceof TypeDeclaration) {
                    final Set<Object> typeResults = n.acceptVisitor((IAstVisitor<? super String, ? extends Set<Object>>)visitor, name);
                    if (!typeResults.isEmpty()) {
                        if (results == null) {
                            results = new LinkedHashSet<Object>();
                        }
                        results.addAll(typeResults);
                        return results;
                    }
                    if (parent instanceof TypeDeclaration) {
                        final TypeDefinition type = n.getUserData(Keys.TYPE_DEFINITION);
                        if (type != null) {
                            visitor._isStaticContext = type.isStatic();
                        }
                    }
                    else if (parent instanceof LocalTypeDeclarationStatement) {
                        n = ((LocalTypeDeclarationStatement)parent).getTypeDeclaration();
                        parent = n.getParent();
                    }
                    lastTypeDeclaration = (TypeDeclaration)n;
                }
                else if (n instanceof Statement) {
                    final Statement s = (Statement)n;
                    final Set<Object> statementResults = s.acceptVisitor((IAstVisitor<? super String, ? extends Set<Object>>)visitor, name);
                    if (!statementResults.isEmpty()) {
                        if (results == null) {
                            results = new LinkedHashSet<Object>();
                        }
                        results.addAll(statementResults);
                        if (mode == NameResolveMode.EXPRESSION || (mode == NameResolveMode.TYPE && CollectionUtilities.any(results, Predicates.instanceOf(TypeReference.class)))) {
                            return results;
                        }
                    }
                    final Statement previousStatement = ((Statement)n).getPreviousStatement();
                    if (previousStatement != null) {
                        n = previousStatement;
                        continue;
                    }
                }
                n = parent;
            }
            if (results != null) {
                return results;
            }
            if (lastTypeDeclaration != null) {
                return visitor.searchUpScope(name, lastTypeDeclaration.getUserData(Keys.TYPE_DEFINITION), new LinkedHashSet<String>(), true);
            }
            return Collections.emptySet();
        }
        
        private Set<Object> searchUpScope(final String name, final TypeDefinition type, final Set<String> visitedTypes, final boolean searchGenericParameters) {
            if (type == null || visitedTypes.contains(type.getInternalName())) {
                return Collections.emptySet();
            }
            Set<Object> results = null;
            if (this._mode == NameResolveMode.EXPRESSION) {
                for (final FieldDefinition f : type.getDeclaredFields()) {
                    if (StringUtilities.equals(f.getName(), name) && (!this._isStaticContext || f.isStatic())) {
                        return Collections.singleton(f);
                    }
                }
            }
            if (StringUtilities.equals(type.getSimpleName(), name)) {
                results = new LinkedHashSet<Object>();
                results.add(type);
            }
            if (searchGenericParameters) {
                for (final GenericParameter gp : type.getGenericParameters()) {
                    if (StringUtilities.equals(gp.getName(), name)) {
                        if (results == null) {
                            results = new LinkedHashSet<Object>();
                        }
                        results.add(gp);
                    }
                }
            }
            for (final TypeDefinition declaredType : type.getDeclaredTypes()) {
                if (StringUtilities.equals(declaredType.getSimpleName(), name)) {
                    if (results == null) {
                        results = new LinkedHashSet<Object>();
                    }
                    results.add(declaredType);
                }
            }
            if (results != null && !results.isEmpty()) {
                return results;
            }
            final TypeReference baseType = type.getBaseType();
            if (baseType != null) {
                final TypeDefinition resolvedBaseType = baseType.resolve();
                if (resolvedBaseType != null) {
                    final Set<Object> baseTypeResults = this.searchUpScope(name, resolvedBaseType, visitedTypes, false);
                    if (baseTypeResults != null && !baseTypeResults.isEmpty()) {
                        if (results == null) {
                            results = ((baseTypeResults instanceof LinkedHashSet) ? baseTypeResults : new LinkedHashSet<Object>(baseTypeResults));
                        }
                        else {
                            results.addAll(baseTypeResults);
                        }
                    }
                }
            }
            for (final TypeReference ifType : MetadataHelper.getInterfaces(type)) {
                final TypeDefinition resultIfType = ifType.resolve();
                if (resultIfType != null) {
                    final Set<Object> ifTypeResults = this.searchUpScope(name, resultIfType, visitedTypes, false);
                    if (ifTypeResults == null || ifTypeResults.isEmpty()) {
                        continue;
                    }
                    if (results == null) {
                        results = ((ifTypeResults instanceof LinkedHashSet) ? ifTypeResults : new LinkedHashSet<Object>(ifTypeResults));
                    }
                    else {
                        results.addAll(ifTypeResults);
                    }
                }
            }
            final MethodReference declaringMethod = type.getDeclaringMethod();
            if (declaringMethod != null) {
                final TypeReference declaringType = declaringMethod.getDeclaringType();
                if (declaringType != null) {
                    final TypeDefinition resolvedType = declaringType.resolve();
                    if (resolvedType != null) {
                        final Set<Object> declaringTypeResults = this.searchUpScope(name, resolvedType, visitedTypes, true);
                        if (declaringTypeResults != null && !declaringTypeResults.isEmpty()) {
                            if (results == null) {
                                results = ((declaringTypeResults instanceof LinkedHashSet) ? declaringTypeResults : new LinkedHashSet<Object>(declaringTypeResults));
                            }
                            else {
                                results.addAll(declaringTypeResults);
                            }
                        }
                    }
                    else if (StringUtilities.equals(declaringType.getSimpleName(), name)) {
                        if (results == null) {
                            results = new LinkedHashSet<Object>();
                        }
                        results.add(declaringType);
                    }
                }
            }
            final TypeReference declaringType = type.getDeclaringType();
            if (declaringType != null) {
                final TypeDefinition resolvedType = declaringType.resolve();
                if (resolvedType != null) {
                    final Set<Object> declaringTypeResults = this.searchUpScope(name, resolvedType, visitedTypes, true);
                    if (declaringTypeResults != null && !declaringTypeResults.isEmpty()) {
                        if (results == null) {
                            results = ((declaringTypeResults instanceof LinkedHashSet) ? declaringTypeResults : new LinkedHashSet<Object>(declaringTypeResults));
                        }
                        else {
                            results.addAll(declaringTypeResults);
                        }
                    }
                }
                else if (StringUtilities.equals(declaringType.getSimpleName(), name)) {
                    if (results == null) {
                        results = new LinkedHashSet<Object>();
                    }
                    results.add(declaringType);
                }
            }
            if (results != null) {
                return results;
            }
            return Collections.emptySet();
        }
        
        private static boolean isStaticContext(final AstNode node) {
            for (AstNode n = node; n != null && !n.isNull(); n = n.getParent()) {
                if (n instanceof MethodDeclaration) {
                    final MethodDefinition method = n.getUserData(Keys.METHOD_DEFINITION);
                    if (method != null) {
                        return method.isStatic();
                    }
                }
                if (n instanceof TypeDeclaration) {
                    final TypeDefinition type = n.getUserData(Keys.TYPE_DEFINITION);
                    if (type != null) {
                        return type.isStatic();
                    }
                }
            }
            return false;
        }
        
        @Override
        public Set<Object> visitComment(final Comment node, final String name) {
            return Collections.emptySet();
        }
        
        @Override
        public Set<Object> visitPatternPlaceholder(final AstNode node, final Pattern pattern, final String name) {
            return Collections.emptySet();
        }
        
        @Override
        public Set<Object> visitInvocationExpression(final InvocationExpression node, final String name) {
            return Collections.emptySet();
        }
        
        @Override
        public Set<Object> visitTypeReference(final TypeReferenceExpression node, final String name) {
            return Collections.emptySet();
        }
        
        @Override
        public Set<Object> visitJavaTokenNode(final JavaTokenNode node, final String name) {
            return Collections.emptySet();
        }
        
        @Override
        public Set<Object> visitMemberReferenceExpression(final MemberReferenceExpression node, final String name) {
            return Collections.emptySet();
        }
        
        @Override
        public Set<Object> visitIdentifier(final Identifier node, final String name) {
            return Collections.emptySet();
        }
        
        @Override
        public Set<Object> visitNullReferenceExpression(final NullReferenceExpression node, final String name) {
            return Collections.emptySet();
        }
        
        @Override
        public Set<Object> visitThisReferenceExpression(final ThisReferenceExpression node, final String name) {
            return Collections.emptySet();
        }
        
        @Override
        public Set<Object> visitSuperReferenceExpression(final SuperReferenceExpression node, final String name) {
            return Collections.emptySet();
        }
        
        @Override
        public Set<Object> visitClassOfExpression(final ClassOfExpression node, final String name) {
            return Collections.emptySet();
        }
        
        @Override
        public Set<Object> visitBlockStatement(final BlockStatement node, final String name) {
            return Collections.emptySet();
        }
        
        @Override
        public Set<Object> visitExpressionStatement(final ExpressionStatement node, final String name) {
            return Collections.emptySet();
        }
        
        @Override
        public Set<Object> visitBreakStatement(final BreakStatement node, final String name) {
            return Collections.emptySet();
        }
        
        @Override
        public Set<Object> visitContinueStatement(final ContinueStatement node, final String name) {
            return Collections.emptySet();
        }
        
        @Override
        public Set<Object> visitDoWhileStatement(final DoWhileStatement node, final String name) {
            return Collections.emptySet();
        }
        
        @Override
        public Set<Object> visitEmptyStatement(final EmptyStatement node, final String name) {
            return Collections.emptySet();
        }
        
        @Override
        public Set<Object> visitIfElseStatement(final IfElseStatement node, final String name) {
            return Collections.emptySet();
        }
        
        @Override
        public Set<Object> visitLabelStatement(final LabelStatement node, final String name) {
            return Collections.emptySet();
        }
        
        @Override
        public Set<Object> visitLabeledStatement(final LabeledStatement node, final String name) {
            return Collections.emptySet();
        }
        
        @Override
        public Set<Object> visitReturnStatement(final ReturnStatement node, final String name) {
            return Collections.emptySet();
        }
        
        @Override
        public Set<Object> visitSwitchStatement(final SwitchStatement node, final String name) {
            return Collections.emptySet();
        }
        
        @Override
        public Set<Object> visitSwitchSection(final SwitchSection node, final String name) {
            return Collections.emptySet();
        }
        
        @Override
        public Set<Object> visitCaseLabel(final CaseLabel node, final String name) {
            return Collections.emptySet();
        }
        
        @Override
        public Set<Object> visitThrowStatement(final ThrowStatement node, final String name) {
            return Collections.emptySet();
        }
        
        @Override
        public Set<Object> visitCatchClause(final CatchClause node, final String name) {
            if (this._mode == NameResolveMode.EXPRESSION && StringUtilities.equals(node.getVariableName(), name)) {
                final Variable exceptionVariable = node.getUserData(Keys.VARIABLE);
                if (exceptionVariable != null) {
                    return Collections.singleton(exceptionVariable);
                }
            }
            return Collections.emptySet();
        }
        
        @Override
        public Set<Object> visitAnnotation(final Annotation node, final String name) {
            return Collections.emptySet();
        }
        
        @Override
        public Set<Object> visitNewLine(final NewLineNode node, final String name) {
            return Collections.emptySet();
        }
        
        @Override
        public Set<Object> visitVariableDeclaration(final VariableDeclarationStatement node, final String name) {
            if (this._mode == NameResolveMode.EXPRESSION) {
                final VariableInitializer v = node.getVariable(name);
                if (v != null) {
                    final Variable variable = v.getUserData(Keys.VARIABLE);
                    if (variable != null) {
                        return Collections.singleton(variable);
                    }
                }
            }
            return Collections.emptySet();
        }
        
        @Override
        public Set<Object> visitVariableInitializer(final VariableInitializer node, final String name) {
            return Collections.emptySet();
        }
        
        @Override
        public Set<Object> visitText(final TextNode node, final String name) {
            return Collections.emptySet();
        }
        
        @Override
        public Set<Object> visitImportDeclaration(final ImportDeclaration node, final String name) {
            final TypeReference importedType = node.getUserData(Keys.TYPE_REFERENCE);
            if (importedType != null && StringUtilities.equals(importedType.getSimpleName(), name)) {
                return Collections.singleton(importedType);
            }
            return Collections.emptySet();
        }
        
        @Override
        public Set<Object> visitSimpleType(final SimpleType node, final String name) {
            if (StringUtilities.equals(node.getIdentifier(), name)) {
                return Collections.singleton(node.toTypeReference());
            }
            return Collections.emptySet();
        }
        
        @Override
        public Set<Object> visitMethodDeclaration(final MethodDeclaration node, final String name) {
            Set<Object> results = null;
            if (this._mode == NameResolveMode.EXPRESSION) {
                for (final ParameterDeclaration p : node.getParameters()) {
                    if (StringUtilities.equals(p.getName(), name)) {
                        final ParameterDefinition pd = p.getUserData(Keys.PARAMETER_DEFINITION);
                        if (pd == null) {
                            continue;
                        }
                        if (results == null) {
                            results = new LinkedHashSet<Object>();
                        }
                        results.add(pd);
                    }
                }
            }
            for (final TypeParameterDeclaration tp : node.getTypeParameters()) {
                final TypeDefinition gp = tp.getUserData(Keys.TYPE_DEFINITION);
                if (gp != null && StringUtilities.equals(gp.getName(), name)) {
                    if (results == null) {
                        results = new LinkedHashSet<Object>();
                    }
                    results.add(gp);
                }
            }
            if (results != null) {
                return results;
            }
            return Collections.emptySet();
        }
        
        @Override
        public Set<Object> visitInitializerBlock(final InstanceInitializer node, final String name) {
            return Collections.emptySet();
        }
        
        @Override
        public Set<Object> visitConstructorDeclaration(final ConstructorDeclaration node, final String name) {
            return Collections.emptySet();
        }
        
        @Override
        public Set<Object> visitTypeParameterDeclaration(final TypeParameterDeclaration node, final String name) {
            return Collections.emptySet();
        }
        
        @Override
        public Set<Object> visitParameterDeclaration(final ParameterDeclaration node, final String name) {
            return Collections.emptySet();
        }
        
        @Override
        public Set<Object> visitFieldDeclaration(final FieldDeclaration node, final String name) {
            if (this._mode == NameResolveMode.EXPRESSION && StringUtilities.equals(node.getName(), name)) {
                final FieldDefinition f = node.getUserData(Keys.FIELD_DEFINITION);
                if (f != null && (!this._isStaticContext || f.isStatic())) {
                    return Collections.singleton(f);
                }
            }
            return Collections.emptySet();
        }
        
        @Override
        public Set<Object> visitTypeDeclaration(final TypeDeclaration node, final String name) {
            Set<Object> results = null;
            if (this._mode == NameResolveMode.EXPRESSION) {
                for (final EntityDeclaration member : node.getMembers()) {
                    if (member instanceof FieldDeclaration) {
                        final Set<Object> fieldResults = member.acceptVisitor((IAstVisitor<? super String, ? extends Set<Object>>)this, name);
                        if (fieldResults.isEmpty()) {
                            continue;
                        }
                        return fieldResults;
                    }
                }
            }
            if (StringUtilities.equals(node.getName(), name)) {
                final TypeDefinition typeDefinition = node.getUserData(Keys.TYPE_DEFINITION);
                if (typeDefinition != null) {
                    results = new LinkedHashSet<Object>();
                    results.add(typeDefinition);
                }
            }
            for (final EntityDeclaration member : node.getMembers()) {
                if (member instanceof TypeDeclaration) {
                    final TypeDeclaration td = (TypeDeclaration)member;
                    if (!StringUtilities.equals(td.getName(), name)) {
                        continue;
                    }
                    final TypeDefinition t = td.getUserData(Keys.TYPE_DEFINITION);
                    if (t == null) {
                        continue;
                    }
                    if (results == null) {
                        results = new LinkedHashSet<Object>();
                    }
                    results.add(t);
                }
            }
            if (this._mode == NameResolveMode.TYPE && results != null && !results.isEmpty()) {
                return results;
            }
            for (final TypeParameterDeclaration tp : node.getTypeParameters()) {
                final TypeDefinition gp = tp.getUserData(Keys.TYPE_DEFINITION);
                if (gp != null && StringUtilities.equals(gp.getName(), name)) {
                    if (results == null) {
                        results = new LinkedHashSet<Object>();
                    }
                    results.add(gp);
                }
            }
            if (results != null && !results.isEmpty()) {
                return results;
            }
            return this.searchUpScope(name, node.getUserData(Keys.TYPE_DEFINITION), new LinkedHashSet<String>(), true);
        }
        
        @Override
        public Set<Object> visitLocalTypeDeclarationStatement(final LocalTypeDeclarationStatement node, final String name) {
            final TypeDeclaration typeDeclaration = node.getTypeDeclaration();
            if (typeDeclaration.isNull()) {
                return Collections.emptySet();
            }
            if (StringUtilities.equals(typeDeclaration.getName(), name)) {
                final TypeDefinition type = typeDeclaration.getUserData(Keys.TYPE_DEFINITION);
                if (type != null) {
                    return Collections.singleton(type);
                }
            }
            return this.searchUpScope(name, typeDeclaration.getUserData(Keys.TYPE_DEFINITION), new LinkedHashSet<String>(), true);
        }
        
        @Override
        public Set<Object> visitCompilationUnit(final CompilationUnit node, final String name) {
            Set<Object> results = null;
            for (final TypeDeclaration typeDeclaration : node.getTypes()) {
                final Set<Object> typeResults = typeDeclaration.acceptVisitor((IAstVisitor<? super String, ? extends Set<Object>>)this, name);
                if (typeResults.isEmpty()) {
                    continue;
                }
                if (results == null) {
                    results = new LinkedHashSet<Object>();
                }
                results.addAll(typeResults);
            }
            for (final ImportDeclaration typeImport : node.getImports()) {
                final Set<Object> importResults = typeImport.acceptVisitor((IAstVisitor<? super String, ? extends Set<Object>>)this, name);
                if (importResults.isEmpty()) {
                    continue;
                }
                if (results == null) {
                    results = new LinkedHashSet<Object>();
                }
                results.addAll(importResults);
            }
            if (results != null) {
                return results;
            }
            return Collections.emptySet();
        }
        
        @Override
        public Set<Object> visitPackageDeclaration(final PackageDeclaration node, final String name) {
            return Collections.emptySet();
        }
        
        @Override
        public Set<Object> visitArraySpecifier(final ArraySpecifier node, final String name) {
            return Collections.emptySet();
        }
        
        @Override
        public Set<Object> visitComposedType(final ComposedType node, final String name) {
            return node.getBaseType().acceptVisitor((IAstVisitor<? super String, ? extends Set<Object>>)this, name);
        }
        
        @Override
        public Set<Object> visitWhileStatement(final WhileStatement node, final String name) {
            return Collections.emptySet();
        }
        
        @Override
        public Set<Object> visitPrimitiveExpression(final PrimitiveExpression node, final String name) {
            return Collections.emptySet();
        }
        
        @Override
        public Set<Object> visitCastExpression(final CastExpression node, final String name) {
            return Collections.emptySet();
        }
        
        @Override
        public Set<Object> visitBinaryOperatorExpression(final BinaryOperatorExpression node, final String name) {
            return Collections.emptySet();
        }
        
        @Override
        public Set<Object> visitInstanceOfExpression(final InstanceOfExpression node, final String name) {
            return Collections.emptySet();
        }
        
        @Override
        public Set<Object> visitIndexerExpression(final IndexerExpression node, final String name) {
            return Collections.emptySet();
        }
        
        @Override
        public Set<Object> visitIdentifierExpression(final IdentifierExpression node, final String name) {
            return Collections.emptySet();
        }
        
        @Override
        public Set<Object> visitUnaryOperatorExpression(final UnaryOperatorExpression node, final String name) {
            return Collections.emptySet();
        }
        
        @Override
        public Set<Object> visitConditionalExpression(final ConditionalExpression node, final String name) {
            return Collections.emptySet();
        }
        
        @Override
        public Set<Object> visitArrayInitializerExpression(final ArrayInitializerExpression arrayInitializerExpression, final String name) {
            return Collections.emptySet();
        }
        
        @Override
        public Set<Object> visitObjectCreationExpression(final ObjectCreationExpression node, final String name) {
            return Collections.emptySet();
        }
        
        @Override
        public Set<Object> visitArrayCreationExpression(final ArrayCreationExpression node, final String name) {
            return Collections.emptySet();
        }
        
        @Override
        public Set<Object> visitAssignmentExpression(final AssignmentExpression node, final String name) {
            return Collections.emptySet();
        }
        
        @Override
        public Set<Object> visitForStatement(final ForStatement node, final String name) {
            if (this._mode == NameResolveMode.EXPRESSION) {
                Set<Object> results = null;
                for (final Statement initializer : node.getInitializers()) {
                    final Set<Object> initializerResults = initializer.acceptVisitor((IAstVisitor<? super String, ? extends Set<Object>>)this, name);
                    if (node.getInitializers().isEmpty()) {
                        continue;
                    }
                    if (results == null) {
                        results = new LinkedHashSet<Object>();
                    }
                    results.addAll(initializerResults);
                }
            }
            return Collections.emptySet();
        }
        
        @Override
        public Set<Object> visitForEachStatement(final ForEachStatement node, final String name) {
            if (this._mode == NameResolveMode.EXPRESSION && StringUtilities.equals(node.getVariableName(), name)) {
                final Variable v = node.getUserData(Keys.VARIABLE);
                if (v != null) {
                    return Collections.singleton(v);
                }
            }
            return Collections.emptySet();
        }
        
        @Override
        public Set<Object> visitTryCatchStatement(final TryCatchStatement node, final String name) {
            return Collections.emptySet();
        }
        
        @Override
        public Set<Object> visitGotoStatement(final GotoStatement node, final String name) {
            return Collections.emptySet();
        }
        
        @Override
        public Set<Object> visitParenthesizedExpression(final ParenthesizedExpression node, final String name) {
            return Collections.emptySet();
        }
        
        @Override
        public Set<Object> visitSynchronizedStatement(final SynchronizedStatement node, final String name) {
            return Collections.emptySet();
        }
        
        @Override
        public Set<Object> visitAnonymousObjectCreationExpression(final AnonymousObjectCreationExpression node, final String name) {
            return Collections.emptySet();
        }
        
        @Override
        public Set<Object> visitWildcardType(final WildcardType node, final String name) {
            return Collections.emptySet();
        }
        
        @Override
        public Set<Object> visitMethodGroupExpression(final MethodGroupExpression node, final String name) {
            return Collections.emptySet();
        }
        
        @Override
        public Set<Object> visitEnumValueDeclaration(final EnumValueDeclaration node, final String name) {
            return Collections.emptySet();
        }
        
        @Override
        public Set<Object> visitAssertStatement(final AssertStatement node, final String name) {
            return Collections.emptySet();
        }
        
        @Override
        public Set<Object> visitLambdaExpression(final LambdaExpression node, final String name) {
            if (this._mode == NameResolveMode.EXPRESSION) {
                Set<Object> results = null;
                for (final ParameterDeclaration pd : node.getParameters()) {
                    if (StringUtilities.equals(pd.getName(), name)) {
                        final ParameterDefinition p = pd.getUserData(Keys.PARAMETER_DEFINITION);
                        if (p == null) {
                            continue;
                        }
                        if (results == null) {
                            results = new LinkedHashSet<Object>();
                        }
                        results.add(p);
                    }
                }
                if (results != null) {
                    return results;
                }
            }
            return Collections.emptySet();
        }
    }
    
    private static final class Result extends NameResolveResult
    {
        private final NameResolveMode _mode;
        private final List<Object> _candidates;
        
        Result(final NameResolveMode mode, final List<Object> candidates) {
            super();
            this._mode = mode;
            this._candidates = candidates;
        }
        
        @Override
        public final List<Object> getCandidates() {
            return this._candidates;
        }
        
        @Override
        public final NameResolveMode getMode() {
            return this._mode;
        }
    }
}
