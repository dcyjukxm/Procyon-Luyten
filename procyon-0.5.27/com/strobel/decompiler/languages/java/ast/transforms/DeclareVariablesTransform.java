package com.strobel.decompiler.languages.java.ast.transforms;

import com.strobel.decompiler.*;
import javax.lang.model.element.*;
import com.strobel.decompiler.ast.*;
import com.strobel.functions.*;
import com.strobel.decompiler.semantics.*;
import java.util.*;
import com.strobel.core.*;
import com.strobel.assembler.metadata.*;
import com.strobel.decompiler.languages.java.ast.*;

public class DeclareVariablesTransform implements IAstTransform
{
    protected final List<VariableToDeclare> variablesToDeclare;
    protected final DecompilerContext context;
    
    public DeclareVariablesTransform(final DecompilerContext context) {
        super();
        this.variablesToDeclare = new ArrayList<VariableToDeclare>();
        this.context = VerifyArgument.notNull(context, "context");
    }
    
    @Override
    public void run(final AstNode node) {
        this.run(node, null);
        for (final VariableToDeclare v : this.variablesToDeclare) {
            final Variable variable = v.getVariable();
            final AssignmentExpression replacedAssignment = v.getReplacedAssignment();
            if (replacedAssignment == null) {
                final BlockStatement block = (BlockStatement)v.getInsertionPoint().getParent();
                final AnalysisResult analysisResult = this.analyze(v, block);
                final VariableDeclarationStatement declaration = new VariableDeclarationStatement(v.getType().clone(), v.getName(), -34);
                if (variable != null) {
                    declaration.getVariables().firstOrNullObject().putUserData(Keys.VARIABLE, variable);
                }
                if (analysisResult.isSingleAssignment) {
                    declaration.addModifier(Modifier.FINAL);
                }
                else if (analysisResult.needsInitializer && variable != null) {
                    declaration.getVariables().firstOrNullObject().setInitializer(AstBuilder.makeDefaultValue(variable.getType()));
                }
                Statement insertionPoint;
                for (insertionPoint = v.getInsertionPoint(); insertionPoint.getPreviousSibling() instanceof LabelStatement; insertionPoint = (Statement)insertionPoint.getPreviousSibling()) {}
                block.getStatements().insertBefore(insertionPoint, declaration);
            }
        }
        for (final VariableToDeclare v : this.variablesToDeclare) {
            final Variable variable = v.getVariable();
            final AssignmentExpression replacedAssignment = v.getReplacedAssignment();
            if (replacedAssignment != null) {
                final VariableInitializer initializer = new VariableInitializer(v.getName());
                final Expression right = replacedAssignment.getRight();
                final AstNode parent = replacedAssignment.getParent();
                if (parent.isNull()) {
                    continue;
                }
                if (parent.getParent() == null) {
                    continue;
                }
                final AnalysisResult analysisResult2 = this.analyze(v, parent.getParent());
                right.remove();
                right.putUserDataIfAbsent(Keys.MEMBER_REFERENCE, (MemberReference)replacedAssignment.getUserData(Keys.MEMBER_REFERENCE));
                right.putUserDataIfAbsent(Keys.VARIABLE, variable);
                initializer.setInitializer(right);
                initializer.putUserData(Keys.VARIABLE, variable);
                final VariableDeclarationStatement declaration2 = new VariableDeclarationStatement();
                declaration2.setType(v.getType().clone());
                declaration2.getVariables().add(initializer);
                if (parent instanceof ExpressionStatement) {
                    if (analysisResult2.isSingleAssignment) {
                        declaration2.addModifier(Modifier.FINAL);
                    }
                    declaration2.putUserDataIfAbsent(Keys.MEMBER_REFERENCE, (MemberReference)parent.getUserData(Keys.MEMBER_REFERENCE));
                    declaration2.putUserData(Keys.VARIABLE, variable);
                    parent.replaceWith(declaration2);
                }
                else {
                    if (analysisResult2.isSingleAssignment) {
                        declaration2.addModifier(Modifier.FINAL);
                    }
                    replacedAssignment.replaceWith(declaration2);
                }
            }
        }
        this.variablesToDeclare.clear();
    }
    
    private AnalysisResult analyze(final VariableToDeclare v, final AstNode scope) {
        final BlockStatement block = v.getBlock();
        final DefiniteAssignmentAnalysis analysis = new DefiniteAssignmentAnalysis(this.context, block);
        if (v.getInsertionPoint() != null) {
            final Statement parentStatement = v.getInsertionPoint();
            analysis.setAnalyzedRange(parentStatement, block);
        }
        else {
            final ExpressionStatement parentStatement2 = (ExpressionStatement)v.getReplacedAssignment().getParent();
            analysis.setAnalyzedRange(parentStatement2, block);
        }
        analysis.analyze(v.getName());
        final boolean needsInitializer = !analysis.getUnassignedVariableUses().isEmpty();
        final IsSingleAssignmentVisitor isSingleAssignmentVisitor = new IsSingleAssignmentVisitor(v.getName(), v.getReplacedAssignment());
        scope.acceptVisitor((IAstVisitor<? super Object, ?>)isSingleAssignmentVisitor, (Object)null);
        return new AnalysisResult(isSingleAssignmentVisitor.isSingleAssignment(), needsInitializer, null);
    }
    
    private void run(final AstNode node, final DefiniteAssignmentAnalysis daa) {
        DefiniteAssignmentAnalysis analysis = daa;
        if (node instanceof BlockStatement) {
            final BlockStatement block = (BlockStatement)node;
            final List<VariableDeclarationStatement> variables = new ArrayList<VariableDeclarationStatement>();
            for (final Statement statement : block.getStatements()) {
                if (statement instanceof VariableDeclarationStatement) {
                    variables.add((VariableDeclarationStatement)statement);
                }
            }
            if (!variables.isEmpty()) {
                for (final VariableDeclarationStatement declaration : variables) {
                    assert declaration.getVariables().size() == 1 && declaration.getVariables().firstOrNullObject().getInitializer().isNull();
                    declaration.remove();
                }
            }
            if (analysis == null) {
                analysis = new DefiniteAssignmentAnalysis(block, new JavaResolver(this.context));
            }
            for (final VariableDeclarationStatement declaration : variables) {
                final VariableInitializer initializer = declaration.getVariables().firstOrNullObject();
                final String variableName = initializer.getName();
                final Variable variable = declaration.getUserData(Keys.VARIABLE);
                this.declareVariableInBlock(analysis, block, declaration.getType(), variableName, variable, true);
            }
        }
        if (node instanceof MethodDeclaration || node instanceof ConstructorDeclaration) {
            final Set<ParameterDefinition> unassignedParameters = new HashSet<ParameterDefinition>();
            final AstNodeCollection<ParameterDeclaration> parameters = node.getChildrenByRole(Roles.PARAMETER);
            final Map<ParameterDefinition, ParameterDeclaration> declarationMap = new HashMap<ParameterDefinition, ParameterDeclaration>();
            final Map<String, ParameterDefinition> parametersByName = new HashMap<String, ParameterDefinition>();
            for (final ParameterDeclaration parameter : parameters) {
                final ParameterDefinition definition = parameter.getUserData(Keys.PARAMETER_DEFINITION);
                if (definition != null) {
                    unassignedParameters.add(definition);
                    declarationMap.put(definition, parameter);
                    parametersByName.put(parameter.getName(), definition);
                }
            }
            node.acceptVisitor((IAstVisitor<? super Object, ?>)new ParameterAssignmentVisitor(unassignedParameters, parametersByName), (Object)null);
            for (final ParameterDefinition definition2 : unassignedParameters) {
                final ParameterDeclaration declaration2 = declarationMap.get(definition2);
                if (declaration2 != null && !declaration2.hasModifier(Modifier.FINAL)) {
                    declaration2.addChild(new JavaModifierToken(Modifier.FINAL), EntityDeclaration.MODIFIER_ROLE);
                }
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
                    new DeclareVariablesTransform(this.context).run(child);
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
    
    private void declareVariableInBlock(final DefiniteAssignmentAnalysis analysis, final BlockStatement block, final AstType type, final String variableName, final Variable variable, final boolean allowPassIntoLoops) {
        final StrongBox<Statement> declarationPoint = new StrongBox<Statement>();
        final boolean canMoveVariableIntoSubBlocks = findDeclarationPoint(analysis, variableName, allowPassIntoLoops, block, declarationPoint, null);
        if (declarationPoint.get() == null) {
            return;
        }
        if (canMoveVariableIntoSubBlocks) {
            for (final Statement statement : block.getStatements()) {
                if (!usesVariable(statement, variableName)) {
                    continue;
                }
                boolean processChildren = true;
                if (statement instanceof ForStatement && statement == declarationPoint.get()) {
                    final ForStatement forStatement = (ForStatement)statement;
                    final AstNodeCollection<Statement> initializers = forStatement.getInitializers();
                    for (final Statement initializer : initializers) {
                        if (this.tryConvertAssignmentExpressionIntoVariableDeclaration(block, initializer, type, variableName)) {
                            processChildren = false;
                            break;
                        }
                    }
                }
                if (processChildren) {
                    for (final AstNode child : statement.getChildren()) {
                        if (child instanceof BlockStatement) {
                            this.declareVariableInBlock(analysis, (BlockStatement)child, type, variableName, variable, allowPassIntoLoops);
                        }
                        else {
                            if (!hasNestedBlocks(child)) {
                                continue;
                            }
                            for (final AstNode nestedChild : child.getChildren()) {
                                if (nestedChild instanceof BlockStatement) {
                                    this.declareVariableInBlock(analysis, (BlockStatement)nestedChild, type, variableName, variable, allowPassIntoLoops);
                                }
                            }
                        }
                    }
                }
                final boolean canStillMoveIntoSubBlocks = findDeclarationPoint(analysis, variableName, allowPassIntoLoops, block, declarationPoint, statement);
                if (!canStillMoveIntoSubBlocks && declarationPoint.get() != null) {
                    if (!this.tryConvertAssignmentExpressionIntoVariableDeclaration(block, declarationPoint.get(), type, variableName)) {
                        final VariableToDeclare vtd = new VariableToDeclare(type, variableName, variable, declarationPoint.get(), block);
                        this.variablesToDeclare.add(vtd);
                    }
                }
            }
        }
        else if (!this.tryConvertAssignmentExpressionIntoVariableDeclaration(block, declarationPoint.get(), type, variableName)) {
            final VariableToDeclare vtd2 = new VariableToDeclare(type, variableName, variable, declarationPoint.get(), block);
            this.variablesToDeclare.add(vtd2);
        }
    }
    
    public static boolean findDeclarationPoint(final DefiniteAssignmentAnalysis analysis, final VariableDeclarationStatement declaration, final BlockStatement block, final StrongBox<Statement> declarationPoint, final Statement skipUpThrough) {
        final String variableName = declaration.getVariables().firstOrNullObject().getName();
        return findDeclarationPoint(analysis, variableName, true, block, declarationPoint, skipUpThrough);
    }
    
    static boolean findDeclarationPoint(final DefiniteAssignmentAnalysis analysis, final String variableName, final boolean allowPassIntoLoops, final BlockStatement block, final StrongBox<Statement> declarationPoint, final Statement skipUpThrough) {
        declarationPoint.set(null);
        Statement waitFor = skipUpThrough;
        if (block.getParent() instanceof CatchClause) {
            final CatchClause catchClause = (CatchClause)block.getParent();
            if (StringUtilities.equals(catchClause.getVariableName(), variableName)) {
                return false;
            }
        }
        for (final Statement statement : block.getStatements()) {
            if (waitFor != null) {
                if (statement != waitFor) {
                    continue;
                }
                waitFor = null;
            }
            else {
                if (!usesVariable(statement, variableName)) {
                    continue;
                }
                if (declarationPoint.get() != null) {
                    return canRedeclareVariable(analysis, block, statement, variableName);
                }
                declarationPoint.set(statement);
                if (!canMoveVariableIntoSubBlock(analysis, block, statement, variableName, allowPassIntoLoops)) {
                    return false;
                }
                final Statement nextStatement = statement.getNextStatement();
                if (nextStatement == null) {
                    continue;
                }
                analysis.setAnalyzedRange(nextStatement, block);
                analysis.analyze(variableName);
                if (!analysis.getUnassignedVariableUses().isEmpty()) {
                    return false;
                }
                continue;
            }
        }
        return true;
    }
    
    private static boolean canMoveVariableIntoSubBlock(final DefiniteAssignmentAnalysis analysis, final BlockStatement block, final Statement statement, final String variableName, final boolean allowPassIntoLoops) {
        if (!allowPassIntoLoops && AstNode.isLoop(statement)) {
            return false;
        }
        if (statement instanceof ForStatement) {
            final ForStatement forStatement = (ForStatement)statement;
            if (!forStatement.getInitializers().isEmpty()) {
                boolean result = false;
                TypeReference lastInitializerType = null;
                StrongBox<Statement> declarationPoint = null;
                final Set<String> variableNames = new HashSet<String>();
                for (final Statement initializer : forStatement.getInitializers()) {
                    if (initializer instanceof ExpressionStatement && ((ExpressionStatement)initializer).getExpression() instanceof AssignmentExpression) {
                        final Expression e = ((ExpressionStatement)initializer).getExpression();
                        if (!(e instanceof AssignmentExpression) || ((AssignmentExpression)e).getOperator() != AssignmentOperatorType.ASSIGN || !(((AssignmentExpression)e).getLeft() instanceof IdentifierExpression)) {
                            continue;
                        }
                        final IdentifierExpression identifier = (IdentifierExpression)((AssignmentExpression)e).getLeft();
                        final boolean usedByInitializer = usesVariable(((AssignmentExpression)e).getRight(), variableName);
                        if (usedByInitializer) {
                            return false;
                        }
                        final Variable variable = identifier.getUserData(Keys.VARIABLE);
                        if (variable == null || variable.isParameter()) {
                            return false;
                        }
                        final TypeReference variableType = variable.getType();
                        if (lastInitializerType == null) {
                            lastInitializerType = variableType;
                        }
                        else if (!MetadataHelper.isSameType(lastInitializerType, variableType)) {
                            return false;
                        }
                        if (!variableNames.add(identifier.getIdentifier())) {
                            return false;
                        }
                        if (result) {
                            if (declarationPoint == null) {
                                declarationPoint = new StrongBox<Statement>();
                            }
                            if (!findDeclarationPoint(analysis, identifier.getIdentifier(), allowPassIntoLoops, block, declarationPoint, null) || declarationPoint.get() != statement) {
                                return false;
                            }
                            continue;
                        }
                        else {
                            if (!StringUtilities.equals(identifier.getIdentifier(), variableName)) {
                                continue;
                            }
                            result = true;
                        }
                    }
                }
                if (result) {
                    return true;
                }
            }
        }
        if (statement instanceof TryCatchStatement) {
            final TryCatchStatement tryCatch = (TryCatchStatement)statement;
            if (!tryCatch.getResources().isEmpty()) {
                for (final VariableDeclarationStatement resource : tryCatch.getResources()) {
                    if (StringUtilities.equals(CollectionUtilities.first(resource.getVariables()).getName(), variableName)) {
                        return true;
                    }
                }
            }
        }
        for (AstNode child = statement.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (!(child instanceof BlockStatement) && usesVariable(child, variableName)) {
                if (!hasNestedBlocks(child)) {
                    return false;
                }
                for (AstNode grandChild = child.getFirstChild(); grandChild != null; grandChild = grandChild.getNextSibling()) {
                    if (!(grandChild instanceof BlockStatement) && usesVariable(grandChild, variableName)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
    private static boolean usesVariable(final AstNode node, final String variableName) {
        if (node instanceof AnonymousObjectCreationExpression) {
            for (final Expression argument : ((AnonymousObjectCreationExpression)node).getArguments()) {
                if (usesVariable(argument, variableName)) {
                    return true;
                }
            }
            return false;
        }
        if (node instanceof TypeDeclaration) {
            final TypeDeclaration type = (TypeDeclaration)node;
            for (final FieldDeclaration field : CollectionUtilities.ofType(type.getMembers(), FieldDeclaration.class)) {
                if (!field.getVariables().isEmpty() && usesVariable(CollectionUtilities.first(field.getVariables()), variableName)) {
                    return true;
                }
            }
            for (final MethodDeclaration method : CollectionUtilities.ofType(type.getMembers(), MethodDeclaration.class)) {
                if (usesVariable(method.getBody(), variableName)) {
                    return true;
                }
            }
            return false;
        }
        if (node instanceof IdentifierExpression && StringUtilities.equals(((IdentifierExpression)node).getIdentifier(), variableName)) {
            return true;
        }
        if (node instanceof ForStatement) {
            final ForStatement forLoop = (ForStatement)node;
            for (final Statement statement : forLoop.getInitializers()) {
                if (statement instanceof VariableDeclarationStatement) {
                    final AstNodeCollection<VariableInitializer> variables = ((VariableDeclarationStatement)statement).getVariables();
                    for (final VariableInitializer variable : variables) {
                        if (StringUtilities.equals(variable.getName(), variableName)) {
                            return false;
                        }
                    }
                }
            }
        }
        if (node instanceof TryCatchStatement) {
            final TryCatchStatement tryCatch = (TryCatchStatement)node;
            for (final VariableDeclarationStatement resource : tryCatch.getResources()) {
                if (StringUtilities.equals(CollectionUtilities.first(resource.getVariables()).getName(), variableName)) {
                    return false;
                }
            }
        }
        if (node instanceof ForEachStatement && StringUtilities.equals(((ForEachStatement)node).getVariableName(), variableName)) {
            return false;
        }
        if (node instanceof CatchClause && StringUtilities.equals(((CatchClause)node).getVariableName(), variableName)) {
            return false;
        }
        for (AstNode child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (usesVariable(child, variableName)) {
                return true;
            }
        }
        return false;
    }
    
    private static boolean canRedeclareVariable(final DefiniteAssignmentAnalysis analysis, final BlockStatement block, final AstNode node, final String variableName) {
        if (node instanceof ForStatement) {
            final ForStatement forLoop = (ForStatement)node;
            for (final Statement statement : forLoop.getInitializers()) {
                if (statement instanceof VariableDeclarationStatement) {
                    final AstNodeCollection<VariableInitializer> variables = ((VariableDeclarationStatement)statement).getVariables();
                    for (final VariableInitializer variable : variables) {
                        if (StringUtilities.equals(variable.getName(), variableName)) {
                            return true;
                        }
                    }
                }
                else {
                    if (!(statement instanceof ExpressionStatement) || !(((ExpressionStatement)statement).getExpression() instanceof AssignmentExpression)) {
                        continue;
                    }
                    final AssignmentExpression assignment = (AssignmentExpression)((ExpressionStatement)statement).getExpression();
                    final Expression left = assignment.getLeft();
                    final Expression right = assignment.getRight();
                    if (left instanceof IdentifierExpression && StringUtilities.equals(((IdentifierExpression)left).getIdentifier(), variableName) && !usesVariable(right, variableName)) {
                        return true;
                    }
                    continue;
                }
            }
        }
        if (node instanceof ForEachStatement && StringUtilities.equals(((ForEachStatement)node).getVariableName(), variableName)) {
            return true;
        }
        if (node instanceof TryCatchStatement) {
            final TryCatchStatement tryCatch = (TryCatchStatement)node;
            for (final VariableDeclarationStatement resource : tryCatch.getResources()) {
                if (StringUtilities.equals(CollectionUtilities.first(resource.getVariables()).getName(), variableName)) {
                    return true;
                }
            }
        }
        for (AstNode prev = node.getPreviousSibling(); prev != null && !prev.isNull(); prev = prev.getPreviousSibling()) {
            if (usesVariable(prev, variableName)) {
                final Statement statement = CollectionUtilities.firstOrDefault(CollectionUtilities.ofType(prev.getAncestorsAndSelf(), Statement.class));
                if (statement == null) {
                    return false;
                }
                if (!canMoveVariableIntoSubBlock(analysis, block, statement, variableName, true)) {
                    return false;
                }
            }
        }
        return true;
    }
    
    private static boolean hasNestedBlocks(final AstNode node) {
        return node.getChildByRole(Roles.EMBEDDED_STATEMENT) instanceof BlockStatement || node instanceof TryCatchStatement || node instanceof CatchClause || node instanceof SwitchSection;
    }
    
    private boolean tryConvertAssignmentExpressionIntoVariableDeclaration(final BlockStatement block, final Statement declarationPoint, final AstType type, final String variableName) {
        return declarationPoint instanceof ExpressionStatement && this.tryConvertAssignmentExpressionIntoVariableDeclaration(block, ((ExpressionStatement)declarationPoint).getExpression(), type, variableName);
    }
    
    private boolean tryConvertAssignmentExpressionIntoVariableDeclaration(final BlockStatement block, final Expression expression, final AstType type, final String variableName) {
        if (expression instanceof AssignmentExpression) {
            final AssignmentExpression assignment = (AssignmentExpression)expression;
            if (assignment.getOperator() == AssignmentOperatorType.ASSIGN && assignment.getLeft() instanceof IdentifierExpression) {
                final IdentifierExpression identifier = (IdentifierExpression)assignment.getLeft();
                if (StringUtilities.equals(identifier.getIdentifier(), variableName)) {
                    this.variablesToDeclare.add(new VariableToDeclare(type, variableName, identifier.getUserData(Keys.VARIABLE), assignment, block));
                    return true;
                }
            }
        }
        return false;
    }
    
    private final class IsSingleAssignmentVisitor extends DepthFirstAstVisitor<Void, Boolean>
    {
        private final String _variableName;
        private final AssignmentExpression _replacedAssignment;
        private boolean _abort;
        private int _loopOrTryDepth;
        private int _assignmentCount;
        private static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$UnaryOperatorType;
        
        IsSingleAssignmentVisitor(final String variableName, final AssignmentExpression replacedAssignment) {
            super();
            this._variableName = VerifyArgument.notNull(variableName, "variableName");
            this._replacedAssignment = replacedAssignment;
        }
        
        final boolean isAssigned() {
            return this._assignmentCount > 0 && !this._abort;
        }
        
        final boolean isSingleAssignment() {
            return this._assignmentCount < 2 && !this._abort;
        }
        
        @Override
        protected Boolean visitChildren(final AstNode node, final Void data) {
            if (this._abort) {
                return Boolean.FALSE;
            }
            return super.visitChildren(node, data);
        }
        
        @Override
        public Boolean visitForStatement(final ForStatement node, final Void _) {
            ++this._loopOrTryDepth;
            try {
                return super.visitForStatement(node, _);
            }
            finally {
                --this._loopOrTryDepth;
            }
        }
        
        @Override
        public Boolean visitForEachStatement(final ForEachStatement node, final Void _) {
            ++this._loopOrTryDepth;
            try {
                if (StringUtilities.equals(node.getVariableName(), this._variableName)) {
                    ++this._assignmentCount;
                }
                return super.visitForEachStatement(node, _);
            }
            finally {
                --this._loopOrTryDepth;
            }
        }
        
        @Override
        public Boolean visitDoWhileStatement(final DoWhileStatement node, final Void _) {
            ++this._loopOrTryDepth;
            try {
                return super.visitDoWhileStatement(node, _);
            }
            finally {
                --this._loopOrTryDepth;
            }
        }
        
        @Override
        public Boolean visitWhileStatement(final WhileStatement node, final Void _) {
            ++this._loopOrTryDepth;
            try {
                return super.visitWhileStatement(node, _);
            }
            finally {
                --this._loopOrTryDepth;
            }
        }
        
        @Override
        public Boolean visitTryCatchStatement(final TryCatchStatement node, final Void data) {
            ++this._loopOrTryDepth;
            try {
                return super.visitTryCatchStatement(node, data);
            }
            finally {
                --this._loopOrTryDepth;
            }
        }
        
        @Override
        public Boolean visitAssignmentExpression(final AssignmentExpression node, final Void _) {
            final Expression left = node.getLeft();
            if (left instanceof IdentifierExpression && StringUtilities.equals(((IdentifierExpression)left).getIdentifier(), this._variableName)) {
                if (this._loopOrTryDepth != 0 && this._replacedAssignment != node) {
                    this._abort = true;
                    return Boolean.FALSE;
                }
                ++this._assignmentCount;
            }
            return super.visitAssignmentExpression(node, _);
        }
        
        @Override
        public Boolean visitTypeDeclaration(final TypeDeclaration node, final Void data) {
            return null;
        }
        
        @Override
        public Boolean visitUnaryOperatorExpression(final UnaryOperatorExpression node, final Void _) {
            final Expression operand = node.getExpression();
            switch ($SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$UnaryOperatorType()[node.getOperator().ordinal()]) {
                case 6:
                case 7:
                case 8:
                case 9: {
                    if (!(operand instanceof IdentifierExpression) || !StringUtilities.equals(((IdentifierExpression)operand).getIdentifier(), this._variableName)) {
                        break;
                    }
                    if (this._loopOrTryDepth != 0) {
                        this._abort = true;
                        return Boolean.FALSE;
                    }
                    if (this._assignmentCount == 0) {
                        ++this._assignmentCount;
                    }
                    ++this._assignmentCount;
                    break;
                }
            }
            return super.visitUnaryOperatorExpression(node, _);
        }
        
        static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$UnaryOperatorType() {
            final int[] loc_0 = IsSingleAssignmentVisitor.$SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$UnaryOperatorType;
            if (loc_0 != null) {
                return loc_0;
            }
            final int[] loc_1 = new int[UnaryOperatorType.values().length];
            try {
                loc_1[UnaryOperatorType.ANY.ordinal()] = 1;
            }
            catch (NoSuchFieldError loc_2) {}
            try {
                loc_1[UnaryOperatorType.BITWISE_NOT.ordinal()] = 3;
            }
            catch (NoSuchFieldError loc_3) {}
            try {
                loc_1[UnaryOperatorType.DECREMENT.ordinal()] = 7;
            }
            catch (NoSuchFieldError loc_4) {}
            try {
                loc_1[UnaryOperatorType.INCREMENT.ordinal()] = 6;
            }
            catch (NoSuchFieldError loc_5) {}
            try {
                loc_1[UnaryOperatorType.MINUS.ordinal()] = 4;
            }
            catch (NoSuchFieldError loc_6) {}
            try {
                loc_1[UnaryOperatorType.NOT.ordinal()] = 2;
            }
            catch (NoSuchFieldError loc_7) {}
            try {
                loc_1[UnaryOperatorType.PLUS.ordinal()] = 5;
            }
            catch (NoSuchFieldError loc_8) {}
            try {
                loc_1[UnaryOperatorType.POST_DECREMENT.ordinal()] = 9;
            }
            catch (NoSuchFieldError loc_9) {}
            try {
                loc_1[UnaryOperatorType.POST_INCREMENT.ordinal()] = 8;
            }
            catch (NoSuchFieldError loc_10) {}
            return IsSingleAssignmentVisitor.$SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$UnaryOperatorType = loc_1;
        }
    }
    
    private final class ParameterAssignmentVisitor extends DepthFirstAstVisitor<Void, Boolean>
    {
        private final Set<ParameterDefinition> _unassignedParameters;
        private final Map<String, ParameterDefinition> _parametersByName;
        private static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$UnaryOperatorType;
        
        ParameterAssignmentVisitor(final Set<ParameterDefinition> unassignedParameters, final Map<String, ParameterDefinition> parametersByName) {
            super();
            this._unassignedParameters = unassignedParameters;
            this._parametersByName = parametersByName;
            for (final ParameterDefinition p : unassignedParameters) {
                this._parametersByName.put(p.getName(), p);
            }
        }
        
        @Override
        protected Boolean visitChildren(final AstNode node, final Void data) {
            return super.visitChildren(node, data);
        }
        
        @Override
        public Boolean visitAssignmentExpression(final AssignmentExpression node, final Void _) {
            final Expression left = node.getLeft();
            final Variable variable = left.getUserData(Keys.VARIABLE);
            if (variable != null && variable.isParameter()) {
                this._unassignedParameters.remove(variable.getOriginalParameter());
                return super.visitAssignmentExpression(node, _);
            }
            ParameterDefinition parameter = left.getUserData(Keys.PARAMETER_DEFINITION);
            if (parameter == null && left instanceof IdentifierExpression) {
                parameter = this._parametersByName.get(((IdentifierExpression)left).getIdentifier());
            }
            if (parameter != null) {
                this._unassignedParameters.remove(parameter);
            }
            return super.visitAssignmentExpression(node, _);
        }
        
        @Override
        public Boolean visitTypeDeclaration(final TypeDeclaration node, final Void data) {
            return null;
        }
        
        @Override
        public Boolean visitUnaryOperatorExpression(final UnaryOperatorExpression node, final Void _) {
            final Expression operand = node.getExpression();
            switch ($SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$UnaryOperatorType()[node.getOperator().ordinal()]) {
                case 6:
                case 7:
                case 8:
                case 9: {
                    ParameterDefinition parameter = operand.getUserData(Keys.PARAMETER_DEFINITION);
                    if (parameter == null && operand instanceof IdentifierExpression) {
                        parameter = this._parametersByName.get(((IdentifierExpression)operand).getIdentifier());
                    }
                    if (parameter != null) {
                        this._unassignedParameters.remove(parameter);
                        break;
                    }
                    break;
                }
            }
            return super.visitUnaryOperatorExpression(node, _);
        }
        
        static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$UnaryOperatorType() {
            final int[] loc_0 = ParameterAssignmentVisitor.$SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$UnaryOperatorType;
            if (loc_0 != null) {
                return loc_0;
            }
            final int[] loc_1 = new int[UnaryOperatorType.values().length];
            try {
                loc_1[UnaryOperatorType.ANY.ordinal()] = 1;
            }
            catch (NoSuchFieldError loc_2) {}
            try {
                loc_1[UnaryOperatorType.BITWISE_NOT.ordinal()] = 3;
            }
            catch (NoSuchFieldError loc_3) {}
            try {
                loc_1[UnaryOperatorType.DECREMENT.ordinal()] = 7;
            }
            catch (NoSuchFieldError loc_4) {}
            try {
                loc_1[UnaryOperatorType.INCREMENT.ordinal()] = 6;
            }
            catch (NoSuchFieldError loc_5) {}
            try {
                loc_1[UnaryOperatorType.MINUS.ordinal()] = 4;
            }
            catch (NoSuchFieldError loc_6) {}
            try {
                loc_1[UnaryOperatorType.NOT.ordinal()] = 2;
            }
            catch (NoSuchFieldError loc_7) {}
            try {
                loc_1[UnaryOperatorType.PLUS.ordinal()] = 5;
            }
            catch (NoSuchFieldError loc_8) {}
            try {
                loc_1[UnaryOperatorType.POST_DECREMENT.ordinal()] = 9;
            }
            catch (NoSuchFieldError loc_9) {}
            try {
                loc_1[UnaryOperatorType.POST_INCREMENT.ordinal()] = 8;
            }
            catch (NoSuchFieldError loc_10) {}
            return ParameterAssignmentVisitor.$SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$UnaryOperatorType = loc_1;
        }
    }
    
    private static final class AnalysisResult
    {
        final boolean isSingleAssignment;
        final boolean needsInitializer;
        
        private AnalysisResult(final boolean singleAssignment, final boolean needsInitializer) {
            super();
            this.isSingleAssignment = singleAssignment;
            this.needsInitializer = needsInitializer;
        }
    }
    
    protected static final class VariableToDeclare
    {
        private final AstType _type;
        private final String _name;
        private final Variable _variable;
        private final Statement _insertionPoint;
        private final AssignmentExpression _replacedAssignment;
        private final BlockStatement _block;
        
        public VariableToDeclare(final AstType type, final String name, final Variable variable, final Statement insertionPoint, final BlockStatement block) {
            super();
            this._type = type;
            this._name = name;
            this._variable = variable;
            this._insertionPoint = insertionPoint;
            this._replacedAssignment = null;
            this._block = block;
        }
        
        public VariableToDeclare(final AstType type, final String name, final Variable variable, final AssignmentExpression replacedAssignment, final BlockStatement block) {
            super();
            this._type = type;
            this._name = name;
            this._variable = variable;
            this._insertionPoint = null;
            this._replacedAssignment = replacedAssignment;
            this._block = block;
        }
        
        public BlockStatement getBlock() {
            return this._block;
        }
        
        public AstType getType() {
            return this._type;
        }
        
        public String getName() {
            return this._name;
        }
        
        public Variable getVariable() {
            return this._variable;
        }
        
        public AssignmentExpression getReplacedAssignment() {
            return this._replacedAssignment;
        }
        
        public Statement getInsertionPoint() {
            return this._insertionPoint;
        }
        
        @Override
        public String toString() {
            return "VariableToDeclare{Type=" + this._type + ", Name='" + this._name + '\'' + ", Variable=" + this._variable + ", InsertionPoint=" + this._insertionPoint + ", ReplacedAssignment=" + this._replacedAssignment + '}';
        }
    }
}
