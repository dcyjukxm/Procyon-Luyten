package com.strobel.decompiler.languages.java.ast.transforms;

import com.strobel.decompiler.*;
import java.util.*;
import com.strobel.functions.*;
import com.strobel.decompiler.semantics.*;
import com.strobel.core.*;
import com.strobel.assembler.metadata.*;
import com.strobel.decompiler.languages.java.ast.*;

public class DeclareLocalClassesTransform implements IAstTransform
{
    protected final List<TypeToDeclare> typesToDeclare;
    protected final DecompilerContext context;
    protected final AstBuilder astBuilder;
    
    public DeclareLocalClassesTransform(final DecompilerContext context) {
        super();
        this.typesToDeclare = new ArrayList<TypeToDeclare>();
        this.context = VerifyArgument.notNull(context, "context");
        this.astBuilder = context.getUserData(Keys.AST_BUILDER);
    }
    
    @Override
    public void run(final AstNode node) {
        if (this.astBuilder == null) {
            return;
        }
        this.run(node, null);
        for (final TypeToDeclare v : this.typesToDeclare) {
            final BlockStatement block = (BlockStatement)v.getInsertionPoint().getParent();
            if (block == null) {
                continue;
            }
            Statement insertionPoint;
            for (insertionPoint = v.getInsertionPoint(); insertionPoint.getPreviousSibling() instanceof LabelStatement; insertionPoint = (Statement)insertionPoint.getPreviousSibling()) {}
            block.insertChildBefore(insertionPoint, new LocalTypeDeclarationStatement(-34, v.getDeclaration()), BlockStatement.STATEMENT_ROLE);
        }
        this.typesToDeclare.clear();
    }
    
    private void run(final AstNode node, final DefiniteAssignmentAnalysis daa) {
        DefiniteAssignmentAnalysis analysis = daa;
        if (node instanceof MethodDeclaration) {
            final MethodDeclaration method = (MethodDeclaration)node;
            final List<TypeDeclaration> localTypes = new ArrayList<TypeDeclaration>();
            for (final TypeDeclaration localType : method.getDeclaredTypes()) {
                localTypes.add(localType);
            }
            if (!localTypes.isEmpty()) {
                for (final TypeDeclaration localType : localTypes) {
                    localType.remove();
                }
            }
            if (analysis == null) {
                analysis = new DefiniteAssignmentAnalysis(method.getBody(), new JavaResolver(this.context));
            }
            for (final TypeDeclaration localType : localTypes) {
                this.declareTypeInBlock(method.getBody(), localType, true);
            }
        }
        for (AstNode child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child instanceof TypeDeclaration) {
                final TypeDefinition currentType = this.context.getCurrentType();
                final MethodDefinition currentMethod = this.context.getCurrentMethod();
                this.context.setCurrentType(null);
                this.context.setCurrentMethod(null);
                try {
                    final TypeDefinition type = child.getUserData(Keys.TYPE_DEFINITION);
                    if (type != null && type.isInterface()) {
                        continue;
                    }
                    new DeclareLocalClassesTransform(this.context).run(child);
                }
                finally {
                    this.context.setCurrentType(currentType);
                    this.context.setCurrentMethod(currentMethod);
                }
                this.context.setCurrentType(currentType);
                this.context.setCurrentMethod(currentMethod);
            }
            else {
                this.run(child, analysis);
            }
        }
    }
    
    private void declareTypeInBlock(final BlockStatement block, final TypeDeclaration type, final boolean allowPassIntoLoops) {
        final StrongBox<Statement> declarationPoint = new StrongBox<Statement>();
        final TypeDefinition typeDefinition = type.getUserData(Keys.TYPE_DEFINITION);
        final boolean canMoveVariableIntoSubBlocks = findDeclarationPoint(typeDefinition, allowPassIntoLoops, block, declarationPoint, null);
        if (declarationPoint.get() == null) {
            return;
        }
        if (canMoveVariableIntoSubBlocks) {
            for (final Statement statement : block.getStatements()) {
                if (!referencesType(statement, typeDefinition)) {
                    continue;
                }
                for (final AstNode child : statement.getChildren()) {
                    if (child instanceof BlockStatement) {
                        this.declareTypeInBlock((BlockStatement)child, type, allowPassIntoLoops);
                    }
                    else {
                        if (!hasNestedBlocks(child)) {
                            continue;
                        }
                        for (final AstNode nestedChild : child.getChildren()) {
                            if (nestedChild instanceof BlockStatement) {
                                this.declareTypeInBlock((BlockStatement)nestedChild, type, allowPassIntoLoops);
                            }
                        }
                    }
                }
                final boolean canStillMoveIntoSubBlocks = findDeclarationPoint(typeDefinition, allowPassIntoLoops, block, declarationPoint, statement);
                if (!canStillMoveIntoSubBlocks && declarationPoint.get() != null) {
                    final TypeToDeclare vtd = new TypeToDeclare(type, typeDefinition, declarationPoint.get(), block);
                    this.typesToDeclare.add(vtd);
                }
            }
        }
        else {
            final TypeToDeclare vtd2 = new TypeToDeclare(type, typeDefinition, declarationPoint.get(), block);
            this.typesToDeclare.add(vtd2);
        }
    }
    
    public static boolean findDeclarationPoint(final TypeDeclaration declaration, final BlockStatement block, final StrongBox<Statement> declarationPoint, final Statement skipUpThrough) {
        return findDeclarationPoint(declaration.getUserData(Keys.TYPE_DEFINITION), true, block, declarationPoint, skipUpThrough);
    }
    
    static boolean findDeclarationPoint(final TypeReference localType, final boolean allowPassIntoLoops, final BlockStatement block, final StrongBox<Statement> declarationPoint, final Statement skipUpThrough) {
        declarationPoint.set(null);
        Statement waitFor = skipUpThrough;
        for (final Statement statement : block.getStatements()) {
            if (waitFor != null) {
                if (statement != waitFor) {
                    continue;
                }
                waitFor = null;
            }
            else {
                if (!referencesType(statement, localType)) {
                    continue;
                }
                if (declarationPoint.get() != null) {
                    return false;
                }
                declarationPoint.set(statement);
                if (!canMoveLocalTypeIntoSubBlock(statement, localType, allowPassIntoLoops)) {
                    return false;
                }
                for (AstNode nextNode = statement.getNextSibling(); nextNode != null; nextNode = nextNode.getNextSibling()) {
                    if (referencesType(nextNode, localType)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
    private static boolean canMoveLocalTypeIntoSubBlock(final Statement statement, final TypeReference localType, final boolean allowPassIntoLoops) {
        if (!allowPassIntoLoops && AstNode.isLoop(statement)) {
            return false;
        }
        for (AstNode child = statement.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (!(child instanceof BlockStatement) && referencesType(child, localType)) {
                if (!hasNestedBlocks(child)) {
                    return false;
                }
                for (AstNode grandChild = child.getFirstChild(); grandChild != null; grandChild = grandChild.getNextSibling()) {
                    if (!(grandChild instanceof BlockStatement) && referencesType(grandChild, localType)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
    private static boolean referencesType(final AstType reference, final TypeReference localType) {
        return reference != null && referencesType(reference.getUserData(Keys.TYPE_REFERENCE), localType);
    }
    
    private static boolean referencesType(final TypeReference reference, final TypeReference localType) {
        if (reference == null || localType == null) {
            return false;
        }
        TypeReference type;
        for (type = reference; type.isArray(); type = type.getElementType()) {}
        TypeReference target;
        for (target = localType; target.isArray(); target = target.getElementType()) {}
        if (StringUtilities.equals(type.getInternalName(), target.getInternalName())) {
            return true;
        }
        if (type.hasExtendsBound()) {
            final TypeReference bound = type.getExtendsBound();
            if (!bound.isGenericParameter() && !MetadataHelper.isSameType(bound, type) && referencesType(bound, localType)) {
                return true;
            }
        }
        if (type.hasSuperBound()) {
            final TypeReference bound = type.getSuperBound();
            if (!bound.isGenericParameter() && !MetadataHelper.isSameType(bound, type) && referencesType(bound, localType)) {
                return true;
            }
        }
        if (type.isGenericType()) {
            if (type instanceof IGenericInstance) {
                final List<TypeReference> typeArguments = ((IGenericInstance)type).getTypeArguments();
                for (final TypeReference typeArgument : typeArguments) {
                    if (!MetadataHelper.isSameType(typeArgument, type) && referencesType(typeArgument, localType)) {
                        return true;
                    }
                }
            }
            else {
                for (final TypeReference typeArgument2 : type.getGenericParameters()) {
                    if (!MetadataHelper.isSameType(typeArgument2, type) && referencesType(typeArgument2, localType)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    private static boolean referencesType(final AstNode node, final TypeReference localType) {
        if (node instanceof AnonymousObjectCreationExpression) {
            for (final Expression argument : ((AnonymousObjectCreationExpression)node).getArguments()) {
                if (referencesType(argument, localType)) {
                    return true;
                }
            }
            return false;
        }
        if (node instanceof TypeDeclaration) {
            final TypeDeclaration type = (TypeDeclaration)node;
            for (final FieldDeclaration field : CollectionUtilities.ofType(type.getMembers(), FieldDeclaration.class)) {
                final FieldDefinition fieldDefinition = field.getUserData(Keys.FIELD_DEFINITION);
                if (fieldDefinition != null && StringUtilities.equals(fieldDefinition.getFieldType().getInternalName(), localType.getInternalName())) {
                    return true;
                }
                if (!field.getVariables().isEmpty() && referencesType(CollectionUtilities.first(field.getVariables()), localType)) {
                    return true;
                }
            }
            for (final MethodDeclaration method : CollectionUtilities.ofType(type.getMembers(), MethodDeclaration.class)) {
                final MethodDefinition methodDefinition = method.getUserData(Keys.METHOD_DEFINITION);
                if (methodDefinition != null) {
                    if (StringUtilities.equals(methodDefinition.getReturnType().getInternalName(), localType.getInternalName())) {
                        return true;
                    }
                    for (final ParameterDefinition parameter : methodDefinition.getParameters()) {
                        if (StringUtilities.equals(parameter.getParameterType().getInternalName(), localType.getInternalName())) {
                            return true;
                        }
                    }
                }
                if (referencesType(method.getBody(), localType)) {
                    return true;
                }
            }
            return false;
        }
        if (node instanceof AstType) {
            return referencesType((AstType)node, localType);
        }
        if (node instanceof ForStatement) {
            final ForStatement forLoop = (ForStatement)node;
            for (final Statement statement : forLoop.getInitializers()) {
                if (statement instanceof VariableDeclarationStatement) {
                    final AstType type2 = ((VariableDeclarationStatement)statement).getType();
                    if (referencesType(type2, localType)) {
                        return true;
                    }
                    continue;
                }
            }
        }
        if (node instanceof ForEachStatement) {
            final ForEachStatement forEach = (ForEachStatement)node;
            if (referencesType(forEach.getVariableType(), localType)) {
                return true;
            }
        }
        if (node instanceof TryCatchStatement) {
            final TryCatchStatement tryCatch = (TryCatchStatement)node;
            for (final VariableDeclarationStatement resource : tryCatch.getResources()) {
                if (referencesType(resource.getType(), localType)) {
                    return true;
                }
            }
        }
        if (node instanceof CatchClause) {
            for (final AstType type3 : ((CatchClause)node).getExceptionTypes()) {
                if (referencesType(type3, localType)) {
                    return true;
                }
            }
        }
        for (AstNode child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (referencesType(child, localType)) {
                return true;
            }
        }
        return false;
    }
    
    private static boolean hasNestedBlocks(final AstNode node) {
        return node.getChildByRole(Roles.EMBEDDED_STATEMENT) instanceof BlockStatement || node instanceof TryCatchStatement || node instanceof CatchClause || node instanceof SwitchSection;
    }
    
    protected static final class TypeToDeclare
    {
        private final TypeDeclaration _declaration;
        private final TypeDefinition _typeDefinition;
        private final Statement _insertionPoint;
        private final BlockStatement _block;
        
        public TypeToDeclare(final TypeDeclaration declaration, final TypeDefinition definition, final Statement insertionPoint, final BlockStatement block) {
            super();
            this._declaration = declaration;
            this._typeDefinition = definition;
            this._insertionPoint = insertionPoint;
            this._block = block;
        }
        
        public BlockStatement getBlock() {
            return this._block;
        }
        
        public TypeDeclaration getDeclaration() {
            return this._declaration;
        }
        
        public TypeDefinition getTypeDefinition() {
            return this._typeDefinition;
        }
        
        public Statement getInsertionPoint() {
            return this._insertionPoint;
        }
        
        @Override
        public String toString() {
            return "TypeToDeclare{Type=" + this._typeDefinition.getSignature() + ", InsertionPoint=" + this._insertionPoint + '}';
        }
    }
}
