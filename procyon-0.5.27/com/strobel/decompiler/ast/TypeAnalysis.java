package com.strobel.decompiler.ast;

import com.strobel.decompiler.*;
import com.strobel.assembler.ir.attributes.*;
import com.strobel.functions.*;
import com.strobel.util.*;
import java.util.*;
import com.strobel.core.*;
import com.strobel.assembler.metadata.*;

public final class TypeAnalysis
{
    private static final int FLAG_BOOLEAN_PROHIBITED = 1;
    private final List<ExpressionToInfer> _allExpressions;
    private final Set<Variable> _singleStoreVariables;
    private final Set<Variable> _singleLoadVariables;
    private final Set<Variable> _allVariables;
    private final Map<Variable, List<ExpressionToInfer>> _assignmentExpressions;
    private final Map<Variable, Set<TypeReference>> _previouslyInferred;
    private final IdentityHashMap<Variable, TypeReference> _inferredVariableTypes;
    private DecompilerContext _context;
    private CoreMetadataFactory _factory;
    private boolean _preserveMetadataTypes;
    private boolean _preserveMetadataGenericTypes;
    private Stack<Expression> _stack;
    private boolean _doneInitializing;
    private static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$assembler$metadata$JvmType;
    private static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$decompiler$ast$AstCode;
    private static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$assembler$metadata$MethodHandleType;
    
    public TypeAnalysis() {
        super();
        this._allExpressions = new ArrayList<ExpressionToInfer>();
        this._singleStoreVariables = new LinkedHashSet<Variable>();
        this._singleLoadVariables = new LinkedHashSet<Variable>();
        this._allVariables = new LinkedHashSet<Variable>();
        this._assignmentExpressions = new LinkedHashMap<Variable, List<ExpressionToInfer>>() {
            @Override
            public List<ExpressionToInfer> get(final Object key) {
                List<ExpressionToInfer> value = super.get(key);
                if (value == null) {
                    if (TypeAnalysis.access$0(TypeAnalysis.this)) {
                        return Collections.emptyList();
                    }
                    ((HashMap<Variable, ArrayList<ExpressionToInfer>>)this).put((Variable)key, value = new ArrayList<ExpressionToInfer>());
                }
                return value;
            }
        };
        this._previouslyInferred = new DefaultMap<Variable, Set<TypeReference>>(CollectionUtilities.setFactory());
        this._inferredVariableTypes = new IdentityHashMap<Variable, TypeReference>();
        this._stack = new Stack<Expression>();
    }
    
    public static void run(final DecompilerContext context, final Block method) {
        final TypeAnalysis ta = new TypeAnalysis();
        final SourceAttribute localVariableTable = SourceAttribute.find("LocalVariableTable", context.getCurrentMethod().getSourceAttributes());
        final SourceAttribute localVariableTypeTable = SourceAttribute.find("LocalVariableTypeTable", context.getCurrentMethod().getSourceAttributes());
        ta._context = context;
        ta._factory = CoreMetadataFactory.make(context.getCurrentType(), context.getCurrentMethod());
        ta._preserveMetadataTypes = (localVariableTable != null);
        ta._preserveMetadataGenericTypes = (localVariableTypeTable != null);
        ta.createDependencyGraph(method);
        ta.identifySingleLoadVariables();
        ta._doneInitializing = true;
        ta.runInference();
    }
    
    public static void reset(final DecompilerContext context, final Block method) {
        final SourceAttribute localVariableTable = SourceAttribute.find("LocalVariableTable", context.getCurrentMethod().getSourceAttributes());
        final SourceAttribute localVariableTypeTable = SourceAttribute.find("LocalVariableTypeTable", context.getCurrentMethod().getSourceAttributes());
        final boolean preserveTypesFromMetadata = localVariableTable != null;
        final boolean preserveGenericTypesFromMetadata = localVariableTypeTable != null;
        for (final Expression e : method.getSelfAndChildrenRecursive(Expression.class)) {
            e.setInferredType(null);
            e.setExpectedType(null);
            final Object operand = e.getOperand();
            if (operand instanceof Variable) {
                final Variable variable = (Variable)operand;
                if (!shouldResetVariableType(variable, preserveTypesFromMetadata, preserveGenericTypesFromMetadata)) {
                    continue;
                }
                variable.setType(null);
            }
        }
    }
    
    private void createDependencyGraph(final Node node) {
        if (node instanceof Condition) {
            ((Condition)node).getCondition().setExpectedType(BuiltinTypes.Boolean);
        }
        else if (node instanceof Loop && ((Loop)node).getCondition() != null) {
            ((Loop)node).getCondition().setExpectedType(BuiltinTypes.Boolean);
        }
        else if (node instanceof CatchBlock) {
            final CatchBlock catchBlock = (CatchBlock)node;
            if (catchBlock.getExceptionVariable() != null && catchBlock.getExceptionType() != null && catchBlock.getExceptionVariable().getType() == null) {
                catchBlock.getExceptionVariable().setType(catchBlock.getExceptionType());
            }
        }
        else if (node instanceof Expression) {
            final Expression expression = (Expression)node;
            final ExpressionToInfer expressionToInfer = new ExpressionToInfer();
            expressionToInfer.expression = expression;
            this._allExpressions.add(expressionToInfer);
            this.findNestedAssignments(expression, expressionToInfer);
            if (expression.getCode().isStore()) {
                if (expression.getOperand() instanceof Variable && this.shouldInferVariableType((Variable)expression.getOperand())) {
                    this._assignmentExpressions.get(expression.getOperand()).add(expressionToInfer);
                    this._allVariables.add((Variable)expression.getOperand());
                }
                else {
                    final StrongBox<Variable> v;
                    if (PatternMatching.matchLoad(expression.getArguments().get(0), v = new StrongBox<Variable>()) && this.shouldInferVariableType(v.value)) {
                        this._assignmentExpressions.get(v.value).add(expressionToInfer);
                        this._allVariables.add(v.value);
                    }
                }
            }
        }
        else if (node instanceof Lambda) {
            final Lambda lambda = (Lambda)node;
            final List<Variable> parameters = lambda.getParameters();
            for (final Variable parameter : parameters) {
                this._assignmentExpressions.get(parameter);
            }
        }
        for (final Node child : node.getChildren()) {
            this.createDependencyGraph(child);
        }
    }
    
    private void findNestedAssignments(final Expression expression, final ExpressionToInfer parent) {
        for (final Expression argument : expression.getArguments()) {
            final Object operand = argument.getOperand();
            if (operand instanceof Variable) {
                this._allVariables.add((Variable)operand);
            }
            if (argument.getCode() == AstCode.Store) {
                final ExpressionToInfer expressionToInfer = new ExpressionToInfer();
                expressionToInfer.expression = argument;
                this._allExpressions.add(expressionToInfer);
                final Variable variable = (Variable)operand;
                if (this.shouldInferVariableType(variable)) {
                    this._assignmentExpressions.get(variable).add(expressionToInfer);
                    this._allVariables.add(variable);
                    ExpressionToInfer.access$0(parent).add(variable);
                }
            }
            else if (argument.getCode() == AstCode.Inc) {
                final ExpressionToInfer expressionToInfer = new ExpressionToInfer();
                expressionToInfer.expression = argument;
                this._allExpressions.add(expressionToInfer);
                final Variable variable = (Variable)operand;
                if (this.shouldInferVariableType(variable)) {
                    this._assignmentExpressions.get(variable).add(expressionToInfer);
                    this._allVariables.add(variable);
                    ExpressionToInfer.access$0(parent).add(variable);
                }
            }
            else if (argument.getCode() == AstCode.PreIncrement || argument.getCode() == AstCode.PostIncrement) {
                final ExpressionToInfer expressionToInfer = new ExpressionToInfer();
                expressionToInfer.expression = argument;
                this._allExpressions.add(expressionToInfer);
                final Expression load = CollectionUtilities.firstOrDefault(argument.getArguments());
                final StrongBox<Variable> variable2 = new StrongBox<Variable>();
                if (load != null && PatternMatching.matchLoadOrRet(load, variable2) && this.shouldInferVariableType(variable2.value)) {
                    this._assignmentExpressions.get(variable2.value).add(expressionToInfer);
                    this._allVariables.add(variable2.value);
                    ExpressionToInfer.access$0(parent).add(variable2.value);
                }
            }
            else {
                final StrongBox<Variable> variable3 = new StrongBox<Variable>();
                if (PatternMatching.matchLoadOrRet(argument, variable3) && this.shouldInferVariableType(variable3.value)) {
                    ExpressionToInfer.access$0(parent).add(variable3.value);
                    this._allVariables.add(variable3.value);
                }
            }
            this.findNestedAssignments(argument, parent);
        }
    }
    
    private boolean isSingleStoreBoolean(final Variable variable) {
        if (this._singleStoreVariables.contains(variable)) {
            final List<ExpressionToInfer> assignments = this._assignmentExpressions.get(variable);
            final ExpressionToInfer e = CollectionUtilities.single(assignments);
            return PatternMatching.matchBooleanConstant(CollectionUtilities.last(e.expression.getArguments())) != null;
        }
        return false;
    }
    
    private void identifySingleLoadVariables() {
        final Map<Variable, List<ExpressionToInfer>> groupedExpressions = new DefaultMap<Variable, List<ExpressionToInfer>>(new Supplier<List<ExpressionToInfer>>() {
            @Override
            public List<ExpressionToInfer> get() {
                return new ArrayList<ExpressionToInfer>();
            }
        });
        for (final ExpressionToInfer expressionToInfer : this._allExpressions) {
            for (final Variable variable : ExpressionToInfer.access$0(expressionToInfer)) {
                groupedExpressions.get(variable).add(expressionToInfer);
            }
        }
        for (final Variable variable2 : groupedExpressions.keySet()) {
            final List<ExpressionToInfer> expressions = groupedExpressions.get(variable2);
            if (expressions.size() == 1) {
                int references = 0;
                for (final Expression expression : expressions.get(0).expression.getSelfAndChildrenRecursive(Expression.class)) {
                    if (expression.getOperand() == variable2 && ++references > 1) {
                        break;
                    }
                }
                if (references != 1) {
                    continue;
                }
                this._singleLoadVariables.add(variable2);
                for (final ExpressionToInfer assignment : this._assignmentExpressions.get(variable2)) {
                    assignment.dependsOnSingleLoad = variable2;
                }
            }
        }
        for (final Variable variable2 : this._assignmentExpressions.keySet()) {
            if (this._assignmentExpressions.get(variable2).size() == 1) {
                this._singleStoreVariables.add(variable2);
            }
        }
    }
    
    private void runInference() {
        this._previouslyInferred.clear();
        this._inferredVariableTypes.clear();
        int numberOfExpressionsAlreadyInferred = 0;
        boolean ignoreSingleLoadDependencies = false;
        boolean assignVariableTypesBasedOnPartialInformation = false;
        final Predicate<Variable> dependentVariableTypesKnown = new Predicate<Variable>() {
            @Override
            public boolean test(final Variable v) {
                return TypeAnalysis.access$2(TypeAnalysis.this, v, null) != null || TypeAnalysis.access$3(TypeAnalysis.this).contains(v);
            }
        };
        while (numberOfExpressionsAlreadyInferred < this._allExpressions.size()) {
            final int oldCount = numberOfExpressionsAlreadyInferred;
            for (final ExpressionToInfer e : this._allExpressions) {
                if (!e.done && trueForAll(ExpressionToInfer.access$0(e), dependentVariableTypesKnown) && (e.dependsOnSingleLoad == null || e.dependsOnSingleLoad.getType() != null || ignoreSingleLoadDependencies)) {
                    this.runInference(e.expression);
                    e.done = true;
                    ++numberOfExpressionsAlreadyInferred;
                }
            }
            if (numberOfExpressionsAlreadyInferred == oldCount) {
                if (!ignoreSingleLoadDependencies) {
                    ignoreSingleLoadDependencies = true;
                    continue;
                }
                if (assignVariableTypesBasedOnPartialInformation) {
                    throw new IllegalStateException("Could not infer any expression.");
                }
                assignVariableTypesBasedOnPartialInformation = true;
            }
            else {
                assignVariableTypesBasedOnPartialInformation = false;
                ignoreSingleLoadDependencies = false;
            }
            this.inferTypesForVariables(assignVariableTypesBasedOnPartialInformation);
        }
        this.verifyResults();
    }
    
    private void verifyResults() {
        final StrongBox<Expression> a = new StrongBox<Expression>();
        for (final Variable variable : this._allVariables) {
            final TypeReference type = variable.getType();
            if (type == null || type == BuiltinTypes.Null) {
                final TypeReference inferredType = this.inferTypeForVariable(variable, BuiltinTypes.Object);
                if (inferredType == null || inferredType == BuiltinTypes.Null) {
                    variable.setType(BuiltinTypes.Object);
                }
                else {
                    variable.setType(inferredType);
                }
            }
            else if (type.isWildcardType()) {
                variable.setType(MetadataHelper.getUpperBound(type));
            }
            else if (type.getSimpleType() == JvmType.Boolean) {
                for (final ExpressionToInfer e : this._assignmentExpressions.get(variable)) {
                    if (PatternMatching.matchStore(e.expression, variable, a)) {
                        final Boolean booleanConstant = PatternMatching.matchBooleanConstant(a.value);
                        if (booleanConstant == null) {
                            continue;
                        }
                        e.expression.setExpectedType(BuiltinTypes.Boolean);
                        e.expression.setInferredType(BuiltinTypes.Boolean);
                        a.value.setExpectedType(BuiltinTypes.Boolean);
                        a.value.setInferredType(BuiltinTypes.Boolean);
                    }
                }
            }
            else {
                if (type.getSimpleType() != JvmType.Character) {
                    continue;
                }
                for (final ExpressionToInfer e : this._assignmentExpressions.get(variable)) {
                    if (PatternMatching.matchStore(e.expression, variable, a)) {
                        final Character characterConstant = PatternMatching.matchCharacterConstant(a.value);
                        if (characterConstant == null) {
                            continue;
                        }
                        e.expression.setExpectedType(BuiltinTypes.Character);
                        e.expression.setInferredType(BuiltinTypes.Character);
                        a.value.setExpectedType(BuiltinTypes.Character);
                        a.value.setInferredType(BuiltinTypes.Character);
                    }
                }
            }
        }
    }
    
    private void inferTypesForVariables(final boolean assignVariableTypesBasedOnPartialInformation) {
        for (final Variable variable : this._allVariables) {
            final List<ExpressionToInfer> expressionsToInfer = this._assignmentExpressions.get(variable);
            boolean inferredFromNull = false;
            TypeReference inferredType = null;
            if (variable.isLambdaParameter()) {
                inferredType = this._inferredVariableTypes.get(variable);
                if (inferredType == null) {
                    continue;
                }
            }
            else {
                if (expressionsToInfer.isEmpty()) {
                    continue;
                }
                if (assignVariableTypesBasedOnPartialInformation) {
                    if (!this.anyDone(expressionsToInfer)) {
                        continue;
                    }
                }
                else if (!this.allDone(expressionsToInfer)) {
                    continue;
                }
                for (final ExpressionToInfer e : expressionsToInfer) {
                    final List<Expression> arguments = e.expression.getArguments();
                    assert e.expression.getCode() == AstCode.PostIncrement;
                    final Expression assignedValue = arguments.get(0);
                    if (assignedValue.getInferredType() == null) {
                        continue;
                    }
                    if (inferredType == null) {
                        inferredType = adjustType(assignedValue.getInferredType(), e.flags);
                        inferredFromNull = PatternMatching.match(assignedValue, AstCode.AConstNull);
                    }
                    else {
                        final TypeReference assigned = this.cleanTypeArguments(assignedValue.getInferredType(), inferredType);
                        final TypeReference commonSuper = adjustType(this.typeWithMoreInformation(inferredType, assigned), e.flags);
                        if (inferredFromNull && assigned != BuiltinTypes.Null && !MetadataHelper.isAssignableFrom(commonSuper, assigned)) {
                            final TypeReference asSubType = MetadataHelper.asSubType(commonSuper, assigned);
                            inferredType = ((asSubType != null) ? asSubType : assigned);
                            inferredFromNull = false;
                        }
                        else {
                            inferredType = commonSuper;
                        }
                    }
                }
            }
            if (inferredType == null) {
                inferredType = variable.getType();
            }
            else if (!inferredType.isUnbounded()) {
                inferredType = (inferredType.hasSuperBound() ? inferredType.getSuperBound() : inferredType.getExtendsBound());
            }
            if (this.shouldInferVariableType(variable) && inferredType != null) {
                variable.setType(inferredType);
                this._inferredVariableTypes.put(variable, inferredType);
                for (final ExpressionToInfer e : this._allExpressions) {
                    if (ExpressionToInfer.access$0(e).contains(variable) || expressionsToInfer.contains(e)) {
                        if (this._stack.contains(e.expression)) {
                            continue;
                        }
                        boolean invalidate = false;
                        for (final Expression c : e.expression.getSelfAndChildrenRecursive(Expression.class)) {
                            if (this._stack.contains(c)) {
                                continue;
                            }
                            c.setExpectedType(null);
                            if ((PatternMatching.matchLoad(c, variable) || PatternMatching.matchStore(c, variable)) && !MetadataHelper.isSameType(c.getInferredType(), inferredType)) {
                                c.setExpectedType(inferredType);
                            }
                            c.setInferredType(null);
                            invalidate = true;
                        }
                        if (!invalidate) {
                            continue;
                        }
                        this.runInference(e.expression, e.flags);
                    }
                }
            }
        }
    }
    
    private boolean shouldInferVariableType(final Variable variable) {
        final VariableDefinition variableDefinition = variable.getOriginalVariable();
        if (variable.isGenerated() || variable.isLambdaParameter()) {
            return true;
        }
        if (!variable.isParameter()) {
            if (variableDefinition != null && variableDefinition.isFromMetadata()) {
                if (variableDefinition.getVariableType().isGenericType()) {
                    if (!this._preserveMetadataGenericTypes) {
                        return true;
                    }
                }
                else if (!this._preserveMetadataTypes) {
                    return true;
                }
                return false;
            }
            return true;
        }
        final ParameterDefinition parameter = variable.getOriginalParameter();
        if (parameter == this._context.getCurrentMethod().getBody().getThisParameter()) {
            return false;
        }
        final TypeReference parameterType = parameter.getParameterType();
        return !this._preserveMetadataGenericTypes && (parameterType.isGenericType() || MetadataHelper.isRawType(parameterType));
    }
    
    private static boolean shouldResetVariableType(final Variable variable, final boolean preserveTypesFromMetadata, final boolean preserveGenericTypesFromMetadata) {
        if (variable.isGenerated() || variable.isLambdaParameter()) {
            return true;
        }
        final VariableDefinition variableDefinition = variable.getOriginalVariable();
        if (variableDefinition != null && variableDefinition.isFromMetadata()) {
            if (variableDefinition.getVariableType().isGenericType()) {
                if (!preserveGenericTypesFromMetadata) {
                    return (variableDefinition != null && variableDefinition.getVariableType() == BuiltinTypes.Integer) || (variableDefinition != null && !variableDefinition.isTypeKnown());
                }
            }
            else if (!preserveTypesFromMetadata) {
                return (variableDefinition != null && variableDefinition.getVariableType() == BuiltinTypes.Integer) || (variableDefinition != null && !variableDefinition.isTypeKnown());
            }
            return false;
        }
        return (variableDefinition != null && variableDefinition.getVariableType() == BuiltinTypes.Integer) || (variableDefinition != null && !variableDefinition.isTypeKnown());
    }
    
    private void runInference(final Expression expression) {
        this.runInference(expression, 0);
    }
    
    private void runInference(final Expression expression, final int flags) {
        final List<Expression> arguments = expression.getArguments();
        Variable changedVariable = null;
        boolean anyArgumentIsMissingExpectedType = false;
        for (final Expression argument : arguments) {
            if (argument.getExpectedType() == null) {
                anyArgumentIsMissingExpectedType = true;
                break;
            }
        }
        if (expression.getInferredType() == null || anyArgumentIsMissingExpectedType) {
            this.inferTypeForExpression(expression, expression.getExpectedType(), anyArgumentIsMissingExpectedType, flags);
        }
        else if (expression.getInferredType() == BuiltinTypes.Integer && expression.getExpectedType() == BuiltinTypes.Boolean) {
            if (expression.getCode() == AstCode.Load || expression.getCode() == AstCode.Store) {
                final Variable variable = (Variable)expression.getOperand();
                expression.setInferredType(BuiltinTypes.Boolean);
                if (variable.getType() == BuiltinTypes.Integer && this.shouldInferVariableType(variable)) {
                    variable.setType(BuiltinTypes.Boolean);
                    changedVariable = variable;
                }
            }
        }
        else if (expression.getInferredType() == BuiltinTypes.Integer && expression.getExpectedType() == BuiltinTypes.Character && (expression.getCode() == AstCode.Load || expression.getCode() == AstCode.Store)) {
            final Variable variable = (Variable)expression.getOperand();
            expression.setInferredType(BuiltinTypes.Character);
            if (variable.getType() == BuiltinTypes.Integer && this.shouldInferVariableType(variable) && this._singleLoadVariables.contains(variable)) {
                variable.setType(BuiltinTypes.Character);
                changedVariable = variable;
            }
        }
        for (final Expression argument : arguments) {
            if (!argument.getCode().isStore()) {
                this.runInference(argument, flags);
            }
        }
        if (changedVariable != null && this._previouslyInferred.get(changedVariable).add(changedVariable.getType())) {
            this.invalidateDependentExpressions(expression, changedVariable);
        }
    }
    
    private void invalidateDependentExpressions(final Expression expression, final Variable variable) {
        final List<ExpressionToInfer> assignments = this._assignmentExpressions.get(variable);
        final TypeReference inferredType = this._inferredVariableTypes.get(variable);
        for (final ExpressionToInfer e : this._allExpressions) {
            if (e.expression != expression && (ExpressionToInfer.access$0(e).contains(variable) || assignments.contains(e))) {
                if (this._stack.contains(e.expression)) {
                    continue;
                }
                boolean invalidate = false;
                for (final Expression c : e.expression.getSelfAndChildrenRecursive(Expression.class)) {
                    if (this._stack.contains(c)) {
                        continue;
                    }
                    c.setExpectedType(null);
                    if ((PatternMatching.matchLoad(c, variable) || PatternMatching.matchStore(c, variable)) && !MetadataHelper.isSameType(c.getInferredType(), inferredType)) {
                        c.setExpectedType(inferredType);
                    }
                    c.setInferredType(null);
                    invalidate = true;
                }
                if (!invalidate) {
                    continue;
                }
                this.runInference(e.expression, e.flags);
            }
        }
    }
    
    private TypeReference inferTypeForExpression(final Expression expression, final TypeReference expectedType) {
        return this.inferTypeForExpression(expression, expectedType, 0);
    }
    
    private TypeReference inferTypeForExpression(final Expression expression, final TypeReference expectedType, final int flags) {
        return this.inferTypeForExpression(expression, expectedType, false, flags);
    }
    
    private TypeReference inferTypeForExpression(final Expression expression, final TypeReference expectedType, final boolean forceInferChildren) {
        return this.inferTypeForExpression(expression, expectedType, forceInferChildren, 0);
    }
    
    private TypeReference inferTypeForExpression(final Expression expression, final TypeReference expectedType, final boolean forceInferChildren, final int flags) {
        boolean actualForceInferChildren = forceInferChildren;
        if (expectedType != null && !this.isSameType(expression.getExpectedType(), expectedType)) {
            expression.setExpectedType(expectedType);
            if (!expression.getCode().isStore()) {
                actualForceInferChildren = true;
            }
        }
        if (actualForceInferChildren || expression.getInferredType() == null) {
            expression.setInferredType(this.doInferTypeForExpression(expression, expectedType, actualForceInferChildren, flags));
        }
        return expression.getInferredType();
    }
    
    private TypeReference doInferTypeForExpression(final Expression expression, final TypeReference expectedType, final boolean forceInferChildren, final int flags) {
        if (this._stack.contains(expression) && !PatternMatching.match(expression, AstCode.LdC)) {
            return expectedType;
        }
        this._stack.push(expression);
        try {
            final AstCode code = expression.getCode();
            final Object operand = expression.getOperand();
            final List<Expression> arguments = expression.getArguments();
            switch ($SWITCH_TABLE$com$strobel$decompiler$ast$AstCode()[code.ordinal()]) {
                case 245: {
                    if (forceInferChildren) {
                        this.inferTypeForExpression(arguments.get(0), BuiltinTypes.Boolean);
                    }
                    return BuiltinTypes.Boolean;
                }
                case 246:
                case 247: {
                    if (forceInferChildren) {
                        this.inferTypeForExpression(arguments.get(0), BuiltinTypes.Boolean);
                        this.inferTypeForExpression(arguments.get(1), BuiltinTypes.Boolean);
                    }
                    return BuiltinTypes.Boolean;
                }
                case 253: {
                    if (forceInferChildren) {
                        this.inferTypeForExpression(arguments.get(0), BuiltinTypes.Boolean);
                    }
                    return this.inferBinaryArguments(arguments.get(1), arguments.get(2), expectedType, forceInferChildren, null, null);
                }
                case 1:
                case 215:
                case 216: {}
                case 88:
                case 89: {}
                case 168:
                case 178:
                case 192:
                case 250:
                case 254:
                case 255: {}
                case 195:
                case 196: {
                    return null;
                }
                case 218: {
                    final Variable v = (Variable)operand;
                    final TypeReference lastInferredType = this._inferredVariableTypes.get(v);
                    if (PatternMatching.matchBooleanConstant(expression.getArguments().get(0)) != null && this.shouldInferVariableType(v) && isBoolean(this.inferTypeForVariable(v, (expectedType != null) ? expectedType : BuiltinTypes.Boolean, true, flags))) {
                        return BuiltinTypes.Boolean;
                    }
                    if (forceInferChildren || (lastInferredType == null && v.getType() == null)) {
                        TypeReference inferredType = this.inferTypeForExpression(expression.getArguments().get(0), this.inferTypeForVariable(v, null, flags), flags);
                        if (inferredType != null && inferredType.isWildcardType()) {
                            inferredType = MetadataHelper.getUpperBound(inferredType);
                        }
                        if (inferredType != null) {
                            return adjustType(inferredType, flags);
                        }
                    }
                    return adjustType((lastInferredType != null) ? lastInferredType : v.getType(), flags);
                }
                case 217: {
                    final Variable v = (Variable)expression.getOperand();
                    final TypeReference inferredType2 = this.inferTypeForVariable(v, expectedType, flags);
                    final TypeDefinition thisType = this._context.getCurrentType();
                    if (v.isParameter() && v.getOriginalParameter() == this._context.getCurrentMethod().getBody().getThisParameter()) {
                        if (this._singleLoadVariables.contains(v) && v.getType() == null) {
                            v.setType(thisType);
                        }
                        return thisType;
                    }
                    TypeReference result = inferredType2;
                    if (expectedType != null && expectedType != BuiltinTypes.Null && this.shouldInferVariableType(v)) {
                        TypeReference tempResult;
                        if (MetadataHelper.isSubType(inferredType2, expectedType)) {
                            tempResult = inferredType2;
                        }
                        else {
                            tempResult = MetadataHelper.asSubType(inferredType2, expectedType);
                        }
                        if (tempResult != null && tempResult.containsGenericParameters()) {
                            final Map<TypeReference, TypeReference> mappings = MetadataHelper.adapt(tempResult, inferredType2);
                            List<TypeReference> mappingsToRemove = null;
                            for (final TypeReference key : mappings.keySet()) {
                                final GenericParameter gp = this._context.getCurrentMethod().findTypeVariable(key.getSimpleName());
                                if (MetadataHelper.isSameType(gp, key, true)) {
                                    if (mappingsToRemove == null) {
                                        mappingsToRemove = new ArrayList<TypeReference>();
                                    }
                                    mappingsToRemove.add(key);
                                }
                            }
                            if (mappingsToRemove != null) {
                                mappings.keySet().removeAll(mappingsToRemove);
                            }
                            if (!mappings.isEmpty()) {
                                tempResult = TypeSubstitutionVisitor.instance().visit(tempResult, mappings);
                            }
                        }
                        if (tempResult == null && v.getType() != null) {
                            tempResult = MetadataHelper.asSubType(v.getType(), expectedType);
                            if (tempResult == null) {
                                tempResult = MetadataHelper.asSubType(MetadataHelper.eraseRecursive(v.getType()), expectedType);
                            }
                        }
                        if (tempResult == null) {
                            tempResult = expectedType;
                        }
                        result = tempResult;
                        if (result.isGenericType()) {
                            if (expectedType.isGenericDefinition() && !result.isGenericDefinition()) {
                                result = result.getUnderlyingType();
                            }
                            if (MetadataHelper.areGenericsSupported(thisType) && MetadataHelper.getUnboundGenericParameterCount(result) > 0) {
                                result = MetadataHelper.substituteGenericArguments(result, inferredType2);
                            }
                        }
                        if (result.isGenericDefinition() && !MetadataHelper.canReferenceTypeVariablesOf(result, this._context.getCurrentType())) {
                            result = new RawType(result.getUnderlyingType());
                        }
                    }
                    final List<ExpressionToInfer> assignments = this._assignmentExpressions.get(v);
                    if (result == null && assignments.isEmpty()) {
                        result = BuiltinTypes.Object;
                    }
                    if (result != null && result.isWildcardType()) {
                        result = MetadataHelper.getUpperBound(result);
                    }
                    result = adjustType(result, flags);
                    if (flags != 0) {
                        for (int i = 0; i < assignments.size(); ++i) {
                            final ExpressionToInfer loc_1 = assignments.get(i);
                            loc_1.flags |= flags;
                        }
                    }
                    this._inferredVariableTypes.put(v, result);
                    if (result != null && !MetadataHelper.isSameType(result, inferredType2) && this._previouslyInferred.get(v).add(result)) {
                        expression.setInferredType(result);
                        this.invalidateDependentExpressions(expression, v);
                    }
                    if (this._singleLoadVariables.contains(v) && v.getType() == null) {
                        v.setType(result);
                    }
                    return result;
                }
                case 187: {
                    return this.inferDynamicCall(expression, expectedType, forceInferChildren);
                }
                case 183:
                case 184:
                case 185:
                case 186: {
                    return this.inferCall(expression, expectedType, forceInferChildren);
                }
                case 181: {
                    final FieldReference field = (FieldReference)operand;
                    if (forceInferChildren) {
                        final FieldDefinition resolvedField = field.resolve();
                        final FieldReference effectiveField = (resolvedField != null) ? resolvedField : field;
                        final TypeReference targetType = this.inferTypeForExpression(arguments.get(0), field.getDeclaringType());
                        if (targetType != null) {
                            final FieldReference asMember = MetadataHelper.asMemberOf(effectiveField, targetType);
                            return asMember.getFieldType();
                        }
                    }
                    return getFieldType((FieldReference)operand);
                }
                case 179: {
                    return getFieldType((FieldReference)operand);
                }
                case 182: {
                    if (forceInferChildren) {
                        this.inferTypeForExpression(arguments.get(0), ((FieldReference)operand).getDeclaringType());
                        this.inferTypeForExpression(arguments.get(1), getFieldType((FieldReference)operand));
                    }
                    return getFieldType((FieldReference)operand);
                }
                case 180: {
                    if (forceInferChildren) {
                        this.inferTypeForExpression(arguments.get(0), getFieldType((FieldReference)operand));
                    }
                    return getFieldType((FieldReference)operand);
                }
                case 188: {
                    return (TypeReference)operand;
                }
                case 257:
                case 258: {
                    final TypeReference inferredType3 = this.inferTypeForExpression(arguments.get(0), null, flags | 0x1);
                    if (inferredType3 != null && inferredType3 != BuiltinTypes.Boolean) {
                        return inferredType3;
                    }
                    final Number n = (Number)operand;
                    if (n instanceof Long) {
                        return BuiltinTypes.Long;
                    }
                    return BuiltinTypes.Integer;
                }
                case 226:
                case 232: {
                    return this.inferTypeForExpression(arguments.get(0), expectedType);
                }
                case 221:
                case 222:
                case 223:
                case 224:
                case 225:
                case 230:
                case 231:
                case 233: {
                    return this.inferBinaryExpression(code, arguments, flags);
                }
                case 227: {
                    if (forceInferChildren) {
                        this.inferTypeForExpression(arguments.get(1), BuiltinTypes.Integer, flags | 0x1);
                    }
                    if (expectedType != null && (expectedType.getSimpleType() == JvmType.Integer || expectedType.getSimpleType() == JvmType.Long)) {
                        return this.numericPromotion(this.inferTypeForExpression(arguments.get(0), expectedType, flags | 0x1));
                    }
                    return this.numericPromotion(this.inferTypeForExpression(arguments.get(0), null, flags | 0x1));
                }
                case 228:
                case 229: {
                    if (forceInferChildren) {
                        this.inferTypeForExpression(arguments.get(1), BuiltinTypes.Integer, flags | 0x1);
                    }
                    final TypeReference type = this.numericPromotion(this.inferTypeForExpression(arguments.get(0), null, flags | 0x1));
                    if (type == null) {
                        return null;
                    }
                    TypeReference expectedInputType = null;
                    switch ($SWITCH_TABLE$com$strobel$assembler$metadata$JvmType()[type.getSimpleType().ordinal()]) {
                        case 5: {
                            expectedInputType = BuiltinTypes.Integer;
                            break;
                        }
                        case 6: {
                            expectedInputType = BuiltinTypes.Long;
                            break;
                        }
                    }
                    if (expectedInputType != null) {
                        this.inferTypeForExpression(arguments.get(0), expectedInputType);
                        return expectedInputType;
                    }
                    return type;
                }
                case 256: {
                    final Expression op = arguments.get(0);
                    final TypeReference targetType2 = this.inferTypeForExpression(op.getArguments().get(0), null);
                    if (forceInferChildren) {
                        this.inferTypeForExpression(arguments.get(0), targetType2);
                    }
                    return targetType2;
                }
                case 2: {
                    if (expectedType != null && !expectedType.isPrimitive()) {
                        return expectedType;
                    }
                    return BuiltinTypes.Null;
                }
                case 19: {
                    if (operand instanceof Boolean && PatternMatching.matchBooleanConstant(expression) != null && !Flags.testAny(flags, 1)) {
                        return BuiltinTypes.Boolean;
                    }
                    if (operand instanceof Character && PatternMatching.matchCharacterConstant(expression) != null) {
                        return BuiltinTypes.Character;
                    }
                    if (operand instanceof Number) {
                        final Number number = (Number)operand;
                        if (number instanceof Integer) {
                            if (expectedType != null) {
                                switch ($SWITCH_TABLE$com$strobel$assembler$metadata$JvmType()[expectedType.getSimpleType().ordinal()]) {
                                    case 1: {
                                        if (number.intValue() == 0 || number.intValue() == 1) {
                                            return adjustType(BuiltinTypes.Boolean, flags);
                                        }
                                        return BuiltinTypes.Integer;
                                    }
                                    case 2: {
                                        if (number.intValue() >= -128 && number.intValue() <= 127) {
                                            return BuiltinTypes.Byte;
                                        }
                                        return BuiltinTypes.Integer;
                                    }
                                    case 3: {
                                        if (number.intValue() >= 0 && number.intValue() <= 65535) {
                                            return BuiltinTypes.Character;
                                        }
                                        return BuiltinTypes.Integer;
                                    }
                                    case 4: {
                                        if (number.intValue() >= -32768 && number.intValue() <= 32767) {
                                            return BuiltinTypes.Short;
                                        }
                                        return BuiltinTypes.Integer;
                                    }
                                }
                            }
                            else if (PatternMatching.matchBooleanConstant(expression) != null) {
                                return adjustType(BuiltinTypes.Boolean, flags);
                            }
                            return BuiltinTypes.Integer;
                        }
                        if (number instanceof Long) {
                            return BuiltinTypes.Long;
                        }
                        if (number instanceof Float) {
                            return BuiltinTypes.Float;
                        }
                        return BuiltinTypes.Double;
                    }
                    else {
                        if (operand instanceof TypeReference) {
                            return this._factory.makeParameterizedType(this._factory.makeNamedType("java.lang.Class"), null, (TypeReference)operand);
                        }
                        return this._factory.makeNamedType("java.lang.String");
                    }
                    break;
                }
                case 189:
                case 190:
                case 243: {
                    if (forceInferChildren) {
                        this.inferTypeForExpression(arguments.get(0), BuiltinTypes.Integer, flags | 0x1);
                    }
                    return ((TypeReference)operand).makeArrayType();
                }
                case 197: {
                    if (forceInferChildren) {
                        for (int j = 0; j < arguments.size(); ++j) {
                            this.inferTypeForExpression(arguments.get(j), BuiltinTypes.Integer, flags | 0x1);
                        }
                    }
                    return (TypeReference)operand;
                }
                case 248: {
                    return this.inferInitObject(expression, expectedType, forceInferChildren, (MethodReference)operand, arguments);
                }
                case 249: {
                    final TypeReference arrayType = (TypeReference)operand;
                    final TypeReference elementType = arrayType.getElementType();
                    if (forceInferChildren) {
                        for (final Expression argument : arguments) {
                            this.inferTypeForExpression(argument, elementType);
                        }
                    }
                    return arrayType;
                }
                case 191: {
                    return BuiltinTypes.Integer;
                }
                case 219: {
                    final TypeReference arrayType = this.inferTypeForExpression(arguments.get(0), null);
                    this.inferTypeForExpression(arguments.get(1), BuiltinTypes.Integer, flags | 0x1);
                    if (arrayType != null && arrayType.isArray()) {
                        return arrayType.getElementType();
                    }
                    return null;
                }
                case 220: {
                    final TypeReference arrayType = this.inferTypeForExpression(arguments.get(0), null);
                    this.inferTypeForExpression(arguments.get(1), BuiltinTypes.Integer, flags | 0x1);
                    TypeReference expectedElementType;
                    if (arrayType != null && arrayType.isArray()) {
                        expectedElementType = arrayType.getElementType();
                    }
                    else {
                        expectedElementType = null;
                    }
                    if (forceInferChildren) {
                        this.inferTypeForExpression(arguments.get(2), expectedElementType);
                    }
                    return expectedElementType;
                }
                case 17:
                case 18: {
                    final Number number = (Number)operand;
                    if (expectedType != null) {
                        if (expectedType.getSimpleType() == JvmType.Boolean && (number.intValue() == 0 || number.intValue() == 1)) {
                            return BuiltinTypes.Boolean;
                        }
                        if (expectedType.getSimpleType() == JvmType.Byte && number.intValue() >= -128 && number.intValue() <= 127) {
                            return BuiltinTypes.Byte;
                        }
                        if (expectedType.getSimpleType() == JvmType.Character && number.intValue() >= 0 && number.intValue() <= 65535) {
                            return BuiltinTypes.Character;
                        }
                        if (expectedType.getSimpleType().isIntegral()) {
                            return expectedType;
                        }
                    }
                    else if (code == AstCode.__BIPush) {
                        return BuiltinTypes.Byte;
                    }
                    return BuiltinTypes.Short;
                }
                case 134:
                case 135:
                case 136:
                case 137:
                case 138:
                case 139:
                case 140:
                case 141:
                case 142:
                case 143:
                case 144:
                case 145:
                case 146:
                case 147:
                case 148: {
                    TypeReference conversionResult = null;
                    TypeReference expectedArgumentType = null;
                    switch ($SWITCH_TABLE$com$strobel$decompiler$ast$AstCode()[code.ordinal()]) {
                        case 134: {
                            conversionResult = BuiltinTypes.Long;
                            expectedArgumentType = BuiltinTypes.Integer;
                            break;
                        }
                        case 135: {
                            conversionResult = BuiltinTypes.Float;
                            expectedArgumentType = BuiltinTypes.Integer;
                            break;
                        }
                        case 136: {
                            conversionResult = BuiltinTypes.Double;
                            expectedArgumentType = BuiltinTypes.Integer;
                            break;
                        }
                        case 137: {
                            conversionResult = BuiltinTypes.Integer;
                            expectedArgumentType = BuiltinTypes.Long;
                            break;
                        }
                        case 138: {
                            conversionResult = BuiltinTypes.Float;
                            expectedArgumentType = BuiltinTypes.Long;
                            break;
                        }
                        case 139: {
                            conversionResult = BuiltinTypes.Double;
                            expectedArgumentType = BuiltinTypes.Long;
                            break;
                        }
                        case 140: {
                            conversionResult = BuiltinTypes.Integer;
                            expectedArgumentType = BuiltinTypes.Float;
                            break;
                        }
                        case 141: {
                            conversionResult = BuiltinTypes.Long;
                            expectedArgumentType = BuiltinTypes.Float;
                            break;
                        }
                        case 142: {
                            conversionResult = BuiltinTypes.Double;
                            expectedArgumentType = BuiltinTypes.Float;
                            break;
                        }
                        case 143: {
                            conversionResult = BuiltinTypes.Integer;
                            expectedArgumentType = BuiltinTypes.Double;
                            break;
                        }
                        case 144: {
                            conversionResult = BuiltinTypes.Long;
                            expectedArgumentType = BuiltinTypes.Double;
                            break;
                        }
                        case 145: {
                            conversionResult = BuiltinTypes.Float;
                            expectedArgumentType = BuiltinTypes.Double;
                            break;
                        }
                        case 146: {
                            conversionResult = BuiltinTypes.Byte;
                            expectedArgumentType = BuiltinTypes.Integer;
                            break;
                        }
                        case 147: {
                            conversionResult = BuiltinTypes.Character;
                            expectedArgumentType = BuiltinTypes.Integer;
                            break;
                        }
                        case 148: {
                            conversionResult = BuiltinTypes.Short;
                            expectedArgumentType = BuiltinTypes.Integer;
                            break;
                        }
                        default: {
                            throw ContractUtils.unsupported();
                        }
                    }
                    arguments.get(0).setExpectedType(expectedArgumentType);
                    return conversionResult;
                }
                case 193:
                case 260: {
                    if (expectedType != null) {
                        final TypeReference castType = (TypeReference)operand;
                        TypeReference inferredType2 = MetadataHelper.asSubType(castType, expectedType);
                        if (forceInferChildren) {
                            inferredType2 = this.inferTypeForExpression(arguments.get(0), (inferredType2 != null) ? inferredType2 : ((TypeReference)operand));
                        }
                        if (inferredType2 != null && MetadataHelper.isSubType(inferredType2, MetadataHelper.eraseRecursive(castType))) {
                            expression.setOperand(inferredType2);
                            return inferredType2;
                        }
                    }
                    return (TypeReference)operand;
                }
                case 259: {
                    final TypeReference type = (TypeReference)operand;
                    if (forceInferChildren) {
                        this.inferTypeForExpression(arguments.get(0), type);
                    }
                    return type.isPrimitive() ? BuiltinTypes.Object : type;
                }
                case 235:
                case 236:
                case 237:
                case 238:
                case 239:
                case 240: {
                    if (forceInferChildren) {
                        return this.inferBinaryExpression(code, arguments, flags);
                    }
                    return BuiltinTypes.Boolean;
                }
                case 149:
                case 150:
                case 151:
                case 152:
                case 153: {
                    if (forceInferChildren) {
                        return this.inferBinaryExpression(code, arguments, flags);
                    }
                    return BuiltinTypes.Integer;
                }
                case 241: {
                    if (forceInferChildren) {
                        this.inferTypeForExpression(arguments.get(0), BuiltinTypes.Boolean, true);
                        return null;
                    }
                    return null;
                }
                case 173:
                case 174:
                case 175:
                case 176:
                case 177:
                case 242: {
                    final Expression lambdaBinding = expression.getUserData(AstKeys.PARENT_LAMBDA_BINDING);
                    if (lambdaBinding == null) {
                        final TypeReference returnType = this._context.getCurrentMethod().getReturnType();
                        if (forceInferChildren && arguments.size() == 1) {
                            this.inferTypeForExpression(arguments.get(0), returnType, true);
                        }
                        return returnType;
                    }
                    final Lambda lambda = (Lambda)lambdaBinding.getOperand();
                    final MethodReference method = lambda.getMethod();
                    if (method == null) {
                        return null;
                    }
                    final TypeReference oldInferredType = lambda.getInferredReturnType();
                    TypeReference inferredType4 = expectedType;
                    TypeReference returnType2 = (oldInferredType != null) ? oldInferredType : expectedType;
                    if (forceInferChildren) {
                        if (returnType2 == null) {
                            returnType2 = lambda.getMethod().getReturnType();
                        }
                        if (returnType2.containsGenericParameters()) {
                            Map<TypeReference, TypeReference> mappings2 = null;
                            TypeReference declaringType = method.getDeclaringType();
                            if (declaringType.isGenericType()) {
                                for (final GenericParameter gp2 : declaringType.getGenericParameters()) {
                                    final GenericParameter inScope = this._context.getCurrentMethod().findTypeVariable(gp2.getName());
                                    if (inScope != null && MetadataHelper.isSameType(gp2, inScope)) {
                                        continue;
                                    }
                                    if (mappings2 == null) {
                                        mappings2 = new HashMap<TypeReference, TypeReference>();
                                    }
                                    if (mappings2.containsKey(gp2)) {
                                        continue;
                                    }
                                    mappings2.put(gp2, MetadataHelper.eraseRecursive(gp2));
                                }
                                if (mappings2 != null) {
                                    declaringType = TypeSubstitutionVisitor.instance().visit(declaringType, mappings2);
                                    if (declaringType != null) {
                                        final MethodReference boundMethod = MetadataHelper.asMemberOf(method, declaringType);
                                        if (boundMethod != null) {
                                            returnType2 = boundMethod.getReturnType();
                                        }
                                    }
                                }
                            }
                        }
                        if (!arguments.isEmpty() && returnType2 != BuiltinTypes.Void) {
                            inferredType4 = this.inferTypeForExpression(arguments.get(0), returnType2);
                        }
                        if (oldInferredType != null && inferredType4 != BuiltinTypes.Void) {
                            final TypeReference newInferredType = MetadataHelper.asSuper(inferredType4, oldInferredType);
                            if (newInferredType != null) {
                                inferredType4 = newInferredType;
                            }
                        }
                    }
                    lambda.setExpectedReturnType(returnType2);
                    lambda.setInferredReturnType(inferredType4);
                    return inferredType4;
                }
                case 252: {
                    final Lambda lambda2 = (Lambda)expression.getOperand();
                    if (lambda2 == null) {
                        return null;
                    }
                    final MethodReference method2 = lambda2.getMethod();
                    final List<Variable> parameters = lambda2.getParameters();
                    TypeReference functionType = lambda2.getFunctionType();
                    if (functionType != null && expectedType != null) {
                        final TypeReference asSubType = MetadataHelper.asSubType(functionType, expectedType);
                        if (asSubType != null) {
                            functionType = asSubType;
                        }
                    }
                    MethodReference boundMethod2 = MetadataHelper.asMemberOf(method2, functionType);
                    if (boundMethod2 == null) {
                        boundMethod2 = method2;
                    }
                    List<ParameterDefinition> methodParameters = boundMethod2.getParameters();
                    final int argumentCount = Math.min(arguments.size(), methodParameters.size());
                    TypeReference inferredReturnType = null;
                    if (forceInferChildren) {
                        for (int k = 0; k < argumentCount; ++k) {
                            final Expression argument2 = arguments.get(k);
                            this.inferTypeForExpression(argument2, methodParameters.get(k).getParameterType());
                        }
                        final List<Variable> lambdaParameters = lambda2.getParameters();
                        for (int l = 0, n2 = lambdaParameters.size(); l < n2; ++l) {
                            this.invalidateDependentExpressions(expression, lambdaParameters.get(l));
                        }
                        for (final Expression e : lambda2.getChildrenAndSelfRecursive(Expression.class)) {
                            if (PatternMatching.match(e, AstCode.Return)) {
                                this.runInference(e);
                                if (e.getInferredType() == null) {
                                    continue;
                                }
                                if (inferredReturnType != null) {
                                    inferredReturnType = MetadataHelper.asSuper(e.getInferredType(), inferredReturnType);
                                }
                                else {
                                    inferredReturnType = e.getInferredType();
                                }
                            }
                        }
                    }
                    final MethodDefinition r = boundMethod2.resolve();
                    if ((functionType.containsGenericParameters() && boundMethod2.containsGenericParameters()) || (r != null && r.getDeclaringType().containsGenericParameters() && r.containsGenericParameters())) {
                        final Map<TypeReference, TypeReference> oldMappings = new HashMap<TypeReference, TypeReference>();
                        final Map<TypeReference, TypeReference> newMappings = new HashMap<TypeReference, TypeReference>();
                        final List<ParameterDefinition> p = boundMethod2.getParameters();
                        final List<ParameterDefinition> rp = (r != null) ? r.getParameters() : method2.getParameters();
                        final TypeReference returnType3 = (r != null) ? r.getReturnType() : method2.getReturnType();
                        if (inferredReturnType != null) {
                            if (returnType3.isGenericParameter()) {
                                final TypeReference boundReturnType = ensureReferenceType(inferredReturnType);
                                if (!MetadataHelper.isSameType(boundReturnType, returnType3)) {
                                    newMappings.put(returnType3, boundReturnType);
                                }
                            }
                            else if (returnType3.containsGenericParameters()) {
                                final Map<TypeReference, TypeReference> returnMappings = new HashMap<TypeReference, TypeReference>();
                                new AddMappingsForArgumentVisitor(returnType3).visit(inferredReturnType, returnMappings);
                                newMappings.putAll(returnMappings);
                            }
                        }
                        for (int m = 0, j2 = Math.max(0, parameters.size() - arguments.size()); m < arguments.size(); ++m, ++j2) {
                            final Expression argument3 = arguments.get(m);
                            final TypeReference rType = rp.get(j2).getParameterType();
                            final TypeReference pType = p.get(j2).getParameterType();
                            final TypeReference aType = argument3.getInferredType();
                            if (pType != null && rType.containsGenericParameters()) {
                                new AddMappingsForArgumentVisitor(pType).visit(rType, oldMappings);
                            }
                            if (aType != null && rType.containsGenericParameters()) {
                                new AddMappingsForArgumentVisitor(aType).visit(rType, newMappings);
                            }
                        }
                        final Map<TypeReference, TypeReference> mappings3 = oldMappings;
                        if (!newMappings.isEmpty()) {
                            for (final TypeReference t : newMappings.keySet()) {
                                final TypeReference oldMapping = oldMappings.get(t);
                                final TypeReference newMapping = newMappings.get(t);
                                if (oldMapping == null || MetadataHelper.isSubType(newMapping, oldMapping)) {
                                    mappings3.put(t, newMapping);
                                }
                            }
                        }
                        if (!mappings3.isEmpty()) {
                            final TypeReference declaringType2 = ((r != null) ? r : method2).getDeclaringType();
                            TypeReference boundDeclaringType = TypeSubstitutionVisitor.instance().visit(declaringType2, mappings3);
                            if (boundDeclaringType != null && boundDeclaringType.isGenericType()) {
                                for (final GenericParameter gp3 : boundDeclaringType.getGenericParameters()) {
                                    final GenericParameter inScope2 = this._context.getCurrentMethod().findTypeVariable(gp3.getName());
                                    if (inScope2 != null && MetadataHelper.isSameType(gp3, inScope2)) {
                                        continue;
                                    }
                                    if (mappings3.containsKey(gp3)) {
                                        continue;
                                    }
                                    mappings3.put(gp3, MetadataHelper.eraseRecursive(gp3));
                                }
                                boundDeclaringType = TypeSubstitutionVisitor.instance().visit(boundDeclaringType, mappings3);
                            }
                            if (boundDeclaringType != null) {
                                functionType = boundDeclaringType;
                            }
                            final MethodReference newBoundMethod = MetadataHelper.asMemberOf(boundMethod2, boundDeclaringType);
                            if (newBoundMethod != null) {
                                boundMethod2 = newBoundMethod;
                                lambda2.setMethod(boundMethod2);
                                methodParameters = boundMethod2.getParameters();
                            }
                        }
                        for (int m = 0; m < methodParameters.size(); ++m) {
                            final Variable variable = parameters.get(m);
                            final TypeReference variableType = methodParameters.get(m).getParameterType();
                            final TypeReference oldVariableType = variable.getType();
                            if (oldVariableType == null || !MetadataHelper.isSameType(variableType, oldVariableType)) {
                                this.invalidateDependentExpressions(expression, variable);
                            }
                        }
                    }
                    return functionType;
                }
                case 169: {
                    return BuiltinTypes.Integer;
                }
                case 170: {
                    if (forceInferChildren) {
                        this.inferTypeForExpression(arguments.get(0), BuiltinTypes.Integer);
                        return null;
                    }
                    return null;
                }
                case 90:
                case 93: {
                    final Expression argument4 = arguments.get(0);
                    final TypeReference result2 = this.inferTypeForExpression(argument4, expectedType);
                    argument4.setExpectedType(result2);
                    return result2;
                }
                case 194: {
                    return BuiltinTypes.Boolean;
                }
                case 133:
                case 213:
                case 234: {
                    final TypeReference inferredType3 = this.inferTypeForVariable((Variable)operand, BuiltinTypes.Integer, flags | 0x1);
                    if (forceInferChildren) {
                        this.inferTypeForExpression(arguments.get(0), inferredType3, true);
                    }
                    return inferredType3;
                }
                case 261: {
                    return (TypeReference)expression.getOperand();
                }
                default: {
                    System.err.printf("Type inference can't handle opcode '%s'.\n", code.getName());
                    return null;
                }
            }
        }
        finally {
            this._stack.pop();
        }
    }
    
    private TypeReference inferInitObject(final Expression expression, final TypeReference expectedType, final boolean forceInferChildren, final MethodReference operand, final List<Expression> arguments) {
        final MethodReference resolvedCtor = (operand instanceof IGenericInstance) ? operand.resolve() : operand;
        final MethodReference constructor = (resolvedCtor != null) ? resolvedCtor : operand;
        final TypeReference type = constructor.getDeclaringType();
        TypeReference inferredType;
        if (expectedType != null && !MetadataHelper.isSameType(expectedType, BuiltinTypes.Object)) {
            final TypeReference asSubType = MetadataHelper.asSubType(type, expectedType);
            inferredType = ((asSubType != null) ? asSubType : type);
        }
        else {
            inferredType = type;
        }
        Map<TypeReference, TypeReference> mappings;
        if (inferredType.isGenericDefinition()) {
            mappings = new HashMap<TypeReference, TypeReference>();
            for (final GenericParameter gp : inferredType.getGenericParameters()) {
                mappings.put(gp, MetadataHelper.eraseRecursive(gp));
            }
        }
        else {
            mappings = Collections.emptyMap();
        }
        if (forceInferChildren) {
            final MethodReference asMember = MetadataHelper.asMemberOf(constructor, TypeSubstitutionVisitor.instance().visit(inferredType, mappings));
            final List<ParameterDefinition> parameters = asMember.getParameters();
            for (int i = 0; i < arguments.size() && i < parameters.size(); ++i) {
                this.inferTypeForExpression(arguments.get(i), parameters.get(i).getParameterType());
            }
            expression.setOperand(asMember);
        }
        if (inferredType == null) {
            return type;
        }
        final List<TypeReference> oldTypeArguments = expression.getUserData(AstKeys.TYPE_ARGUMENTS);
        if (inferredType instanceof IGenericInstance) {
            boolean typeArgumentsChanged = false;
            List<TypeReference> typeArguments = ((IGenericInstance)inferredType).getTypeArguments();
            for (int j = 0; j < typeArguments.size(); ++j) {
                TypeReference t = typeArguments.get(j);
                while (t.isWildcardType()) {
                    t = (t.hasExtendsBound() ? t.getExtendsBound() : MetadataHelper.getUpperBound(t));
                    if (!typeArgumentsChanged) {
                        typeArguments = CollectionUtilities.toList(typeArguments);
                        typeArgumentsChanged = true;
                    }
                    typeArguments.set(j, t);
                }
                while (t.isGenericParameter()) {
                    final GenericParameter inScope = this._context.getCurrentMethod().findTypeVariable(t.getName());
                    if (inScope != null && MetadataHelper.isSameType(t, inScope)) {
                        break;
                    }
                    if (oldTypeArguments != null && oldTypeArguments.size() == typeArguments.size()) {
                        final TypeReference o = oldTypeArguments.get(j);
                        if (!MetadataHelper.isSameType(o, t)) {
                            t = o;
                            if (!typeArgumentsChanged) {
                                typeArguments = CollectionUtilities.toList(typeArguments);
                                typeArgumentsChanged = true;
                            }
                            typeArguments.set(j, t);
                            continue;
                        }
                    }
                    t = (t.hasExtendsBound() ? t.getExtendsBound() : MetadataHelper.getUpperBound(t));
                    if (!typeArgumentsChanged) {
                        typeArguments = CollectionUtilities.toList(typeArguments);
                        typeArgumentsChanged = true;
                    }
                    typeArguments.set(j, t);
                }
            }
            expression.putUserData(AstKeys.TYPE_ARGUMENTS, typeArguments);
            if (typeArgumentsChanged) {
                inferredType = inferredType.makeGenericType(typeArguments);
            }
        }
        return inferredType;
    }
    
    private TypeReference cleanTypeArguments(final TypeReference newType, final TypeReference alternateType) {
        if (!(alternateType instanceof IGenericInstance)) {
            return newType;
        }
        if (!StringUtilities.equals(newType.getInternalName(), alternateType.getInternalName())) {
            return newType;
        }
        final List<TypeReference> alternateTypeArguments = ((IGenericInstance)alternateType).getTypeArguments();
        boolean typeArgumentsChanged = false;
        List<TypeReference> typeArguments;
        if (newType instanceof IGenericInstance) {
            typeArguments = ((IGenericInstance)newType).getTypeArguments();
        }
        else {
            typeArguments = new ArrayList<TypeReference>();
            typeArguments.addAll(newType.getGenericParameters());
        }
        for (int i = 0; i < typeArguments.size(); ++i) {
            TypeReference t = typeArguments.get(i);
            while (t.isGenericParameter()) {
                final GenericParameter inScope = this._context.getCurrentMethod().findTypeVariable(t.getName());
                if (inScope != null && MetadataHelper.isSameType(t, inScope)) {
                    break;
                }
                if (alternateTypeArguments != null && alternateTypeArguments.size() == typeArguments.size()) {
                    final TypeReference o = alternateTypeArguments.get(i);
                    if (!MetadataHelper.isSameType(o, t)) {
                        t = o;
                        if (!typeArgumentsChanged) {
                            typeArguments = CollectionUtilities.toList(typeArguments);
                            typeArgumentsChanged = true;
                        }
                        typeArguments.set(i, t);
                        continue;
                    }
                }
                t = (t.hasExtendsBound() ? t.getExtendsBound() : MetadataHelper.getUpperBound(t));
                if (!typeArgumentsChanged) {
                    typeArguments = CollectionUtilities.toList(typeArguments);
                    typeArgumentsChanged = true;
                }
                typeArguments.set(i, t);
            }
        }
        if (typeArgumentsChanged) {
            return newType.makeGenericType(typeArguments);
        }
        return newType;
    }
    
    private TypeReference inferBinaryExpression(final AstCode code, final List<Expression> arguments, final int flags) {
        final Expression left = arguments.get(0);
        final Expression right = arguments.get(1);
        this.runInference(left);
        this.runInference(right);
        left.setExpectedType(left.getInferredType());
        right.setExpectedType(left.getInferredType());
        left.setInferredType(null);
        right.setInferredType(null);
        int operandFlags = 0;
        switch ($SWITCH_TABLE$com$strobel$decompiler$ast$AstCode()[code.ordinal()]) {
            case 230:
            case 231:
            case 233:
            case 235:
            case 236: {
                if (left.getExpectedType() == BuiltinTypes.Boolean) {
                    if (right.getExpectedType() == BuiltinTypes.Integer && PatternMatching.matchBooleanConstant(right) != null) {
                        right.setExpectedType(BuiltinTypes.Boolean);
                        break;
                    }
                    left.setExpectedType(BuiltinTypes.Integer);
                    break;
                }
                else {
                    if (right.getExpectedType() != BuiltinTypes.Boolean) {
                        break;
                    }
                    if (left.getExpectedType() == BuiltinTypes.Integer && PatternMatching.matchBooleanConstant(left) != null) {
                        left.setExpectedType(BuiltinTypes.Boolean);
                        break;
                    }
                    right.setExpectedType(BuiltinTypes.Integer);
                    break;
                }
                break;
            }
            default: {
                operandFlags |= 0x1;
                if (left.getExpectedType() == BuiltinTypes.Boolean || (left.getExpectedType() == null && PatternMatching.matchBooleanConstant(left) != null)) {
                    left.setExpectedType(BuiltinTypes.Integer);
                }
                if (right.getExpectedType() == BuiltinTypes.Boolean || (right.getExpectedType() == null && PatternMatching.matchBooleanConstant(right) != null)) {
                    right.setExpectedType(BuiltinTypes.Integer);
                    break;
                }
                break;
            }
        }
        if (left.getExpectedType() == BuiltinTypes.Character) {
            if (right.getExpectedType() == BuiltinTypes.Integer && PatternMatching.matchCharacterConstant(right) != null) {
                right.setExpectedType(BuiltinTypes.Character);
            }
        }
        else if (right.getExpectedType() == BuiltinTypes.Character && left.getExpectedType() == BuiltinTypes.Integer && PatternMatching.matchCharacterConstant(left) != null) {
            left.setExpectedType(BuiltinTypes.Character);
        }
        final TypeReference operandType = this.inferBinaryArguments(left, right, this.typeWithMoreInformation(this.doInferTypeForExpression(left, left.getExpectedType(), true, operandFlags), this.doInferTypeForExpression(right, right.getExpectedType(), true, operandFlags)), false, null, null);
        switch ($SWITCH_TABLE$com$strobel$decompiler$ast$AstCode()[code.ordinal()]) {
            case 235:
            case 236:
            case 237:
            case 238:
            case 239:
            case 240: {
                return BuiltinTypes.Boolean;
            }
            default: {
                return adjustType(operandType, flags);
            }
        }
    }
    
    private TypeReference inferDynamicCall(final Expression expression, final TypeReference expectedType, final boolean forceInferChildren) {
        final List<Expression> arguments = expression.getArguments();
        final DynamicCallSite callSite = (DynamicCallSite)expression.getOperand();
        TypeReference inferredType = expression.getInferredType();
        if (inferredType == null) {
            inferredType = callSite.getMethodType().getReturnType();
        }
        TypeReference result = (expectedType == null) ? inferredType : MetadataHelper.asSubType(inferredType, expectedType);
        if (result == null) {
            result = inferredType;
        }
        if (result.isGenericType() || MetadataHelper.isRawType(result)) {
            final MethodReference bootstrapMethod = callSite.getBootstrapMethod();
            if ("java/lang/invoke/LambdaMetafactory".equals(bootstrapMethod.getDeclaringType().getInternalName()) && StringUtilities.equals("metafactory", bootstrapMethod.getName(), StringComparison.OrdinalIgnoreCase) && callSite.getBootstrapArguments().size() == 3 && callSite.getBootstrapArguments().get(1) instanceof MethodHandle) {
                final MethodHandle targetHandle = callSite.getBootstrapArguments().get(1);
                final MethodReference targetMethod = targetHandle.getMethod();
                final Map<TypeReference, TypeReference> expectedMappings = new HashMap<TypeReference, TypeReference>();
                final Map<TypeReference, TypeReference> inferredMappings = new HashMap<TypeReference, TypeReference>();
                MethodReference functionMethod = null;
                final TypeDefinition resolvedType = result.resolve();
                final List<MethodReference> methods = MetadataHelper.findMethods((resolvedType != null) ? resolvedType : result, MetadataFilters.matchName(callSite.getMethodName()));
                for (final MethodReference m : methods) {
                    final MethodDefinition r = m.resolve();
                    if (r != null && r.isAbstract() && !r.isStatic() && !r.isDefault()) {
                        functionMethod = r;
                        break;
                    }
                }
                if (functionMethod == null) {
                    return null;
                }
                boolean firstArgIsTarget = false;
                MethodReference actualMethod = targetMethod;
                switch ($SWITCH_TABLE$com$strobel$assembler$metadata$MethodHandleType()[targetHandle.getHandleType().ordinal()]) {
                    case 1:
                    case 3:
                    case 5:
                    case 7:
                    case 9: {
                        if (arguments.size() <= 0) {
                            break;
                        }
                        final Expression arg = arguments.get(0);
                        final TypeReference expectedArgType = targetMethod.getDeclaringType();
                        if (forceInferChildren) {
                            this.inferTypeForExpression(arg, expectedArgType, true);
                        }
                        final TypeReference targetType = arg.getInferredType();
                        if (targetType == null || !MetadataHelper.isSubType(targetType, expectedArgType)) {
                            break;
                        }
                        firstArgIsTarget = true;
                        final MethodReference asMember = MetadataHelper.asMemberOf(actualMethod, targetType);
                        if (asMember != null) {
                            actualMethod = asMember;
                            break;
                        }
                        break;
                    }
                }
                if (expectedType != null && expectedType.isGenericType() && !expectedType.isGenericDefinition()) {
                    List<GenericParameter> genericParameters;
                    if (resolvedType != null) {
                        genericParameters = resolvedType.getGenericParameters();
                    }
                    else {
                        genericParameters = expectedType.getGenericParameters();
                    }
                    final List<TypeReference> typeArguments = ((IGenericInstance)expectedType).getTypeArguments();
                    if (typeArguments.size() == genericParameters.size()) {
                        for (int i = 0; i < genericParameters.size(); ++i) {
                            final TypeReference typeArgument = typeArguments.get(i);
                            final GenericParameter genericParameter = genericParameters.get(i);
                            if (!MetadataHelper.isSameType(typeArgument, genericParameter, true)) {
                                expectedMappings.put(genericParameter, typeArgument);
                            }
                        }
                    }
                }
                new AddMappingsForArgumentVisitor(actualMethod.isConstructor() ? actualMethod.getDeclaringType() : actualMethod.getReturnType()).visit(functionMethod.getReturnType(), inferredMappings);
                final List<ParameterDefinition> tp = actualMethod.getParameters();
                final List<ParameterDefinition> fp = functionMethod.getParameters();
                if (tp.size() == fp.size()) {
                    for (int i = 0; i < fp.size(); ++i) {
                        new AddMappingsForArgumentVisitor(tp.get(i).getParameterType()).visit(fp.get(i).getParameterType(), inferredMappings);
                    }
                }
                for (final TypeReference key : expectedMappings.keySet()) {
                    final TypeReference expectedMapping = expectedMappings.get(key);
                    final TypeReference inferredMapping = inferredMappings.get(key);
                    if (inferredMapping == null || MetadataHelper.isSubType(expectedMapping, inferredMapping)) {
                        inferredMappings.put(key, expectedMapping);
                    }
                }
                result = TypeSubstitutionVisitor.instance().visit((resolvedType != null) ? resolvedType : result, inferredMappings);
                if (!firstArgIsTarget || expectedType == null) {
                    return result;
                }
                TypeReference declaringType = actualMethod.getDeclaringType();
                if (!declaringType.isGenericDefinition() && !MetadataHelper.isRawType(actualMethod.getDeclaringType())) {
                    return result;
                }
                declaringType = (declaringType.isGenericDefinition() ? declaringType : declaringType.resolve());
                if (declaringType == null) {
                    return result;
                }
                final MethodReference resultMethod = MetadataHelper.asMemberOf(functionMethod, result);
                actualMethod = actualMethod.resolve();
                if (resultMethod == null || actualMethod == null) {
                    return result;
                }
                inferredMappings.clear();
                new AddMappingsForArgumentVisitor(resultMethod.getReturnType()).visit(actualMethod.getReturnType(), inferredMappings);
                final List<ParameterDefinition> ap = actualMethod.getParameters();
                final List<ParameterDefinition> rp = resultMethod.getParameters();
                if (ap.size() == rp.size()) {
                    for (int j = 0, n = ap.size(); j < n; ++j) {
                        new AddMappingsForArgumentVisitor(rp.get(j).getParameterType()).visit(ap.get(j).getParameterType(), inferredMappings);
                    }
                }
                final TypeReference resolvedTargetType = TypeSubstitutionVisitor.instance().visit(declaringType, inferredMappings);
                if (resolvedTargetType != null) {
                    this.inferTypeForExpression(arguments.get(0), resolvedTargetType, true);
                }
            }
        }
        return result;
    }
    
    private TypeReference inferCall(final Expression expression, final TypeReference expectedType, final boolean forceInferChildren) {
        final AstCode code = expression.getCode();
        final List<Expression> arguments = expression.getArguments();
        final MethodReference method = (MethodReference)expression.getOperand();
        final List<ParameterDefinition> parameters = method.getParameters();
        final boolean hasThis = code != AstCode.InvokeStatic && code != AstCode.InvokeDynamic;
        TypeReference targetType = null;
        MethodReference boundMethod = method;
        if (forceInferChildren) {
            final MethodDefinition r = method.resolve();
            MethodReference actualMethod;
            if (hasThis) {
                final Expression thisArg = arguments.get(0);
                final TypeReference expectedTargetType = (thisArg.getInferredType() != null) ? thisArg.getInferredType() : thisArg.getExpectedType();
                if (expectedTargetType != null && expectedTargetType.isGenericType() && !expectedTargetType.isGenericDefinition()) {
                    boundMethod = MetadataHelper.asMemberOf(method, expectedTargetType);
                    targetType = this.inferTypeForExpression(arguments.get(0), expectedTargetType);
                }
                else if (method.isConstructor()) {
                    targetType = method.getDeclaringType();
                }
                else {
                    targetType = this.inferTypeForExpression(arguments.get(0), method.getDeclaringType());
                }
                if (!(targetType instanceof RawType) && MetadataHelper.isRawType(targetType) && !MetadataHelper.canReferenceTypeVariablesOf(targetType, this._context.getCurrentType())) {
                    targetType = MetadataHelper.erase(targetType);
                }
                final MethodReference m = (targetType != null) ? MetadataHelper.asMemberOf((r != null) ? r : method, targetType) : method;
                if (m != null) {
                    actualMethod = m;
                }
                else {
                    actualMethod = ((r != null) ? r : boundMethod);
                }
            }
            else {
                actualMethod = ((r != null) ? r : boundMethod);
            }
            boundMethod = actualMethod;
            expression.setOperand(boundMethod);
            List<ParameterDefinition> p = method.getParameters();
            Map<TypeReference, TypeReference> mappings = null;
            if (actualMethod.containsGenericParameters() || (r != null && r.containsGenericParameters())) {
                final Map<TypeReference, TypeReference> oldMappings = new HashMap<TypeReference, TypeReference>();
                final Map<TypeReference, TypeReference> newMappings = new HashMap<TypeReference, TypeReference>();
                final Map<TypeReference, TypeReference> inferredMappings = new HashMap<TypeReference, TypeReference>();
                if (targetType != null && targetType.isGenericType()) {
                    oldMappings.putAll(MetadataHelper.getGenericSubTypeMappings(targetType.getUnderlyingType(), targetType));
                }
                final List<ParameterDefinition> rp = (r != null) ? r.getParameters() : actualMethod.getParameters();
                final List<ParameterDefinition> cp = boundMethod.getParameters();
                final boolean mapOld = method instanceof IGenericInstance;
                for (int i = 0; i < parameters.size(); ++i) {
                    final TypeReference rType = rp.get(i).getParameterType();
                    final TypeReference pType = p.get(i).getParameterType();
                    final TypeReference cType = cp.get(i).getParameterType();
                    final TypeReference aType = this.inferTypeForExpression(arguments.get(hasThis ? (i + 1) : i), cType);
                    if (mapOld && rType != null && rType.containsGenericParameters()) {
                        new AddMappingsForArgumentVisitor(pType).visit(rType, oldMappings);
                    }
                    if (cType != null && rType.containsGenericParameters()) {
                        new AddMappingsForArgumentVisitor(cType).visit(rType, newMappings);
                    }
                    if (aType != null && rType.containsGenericParameters()) {
                        new AddMappingsForArgumentVisitor(aType).visit(rType, inferredMappings);
                    }
                }
                if (expectedType != null) {
                    final TypeReference returnType = (r != null) ? r.getReturnType() : actualMethod.getReturnType();
                    if (returnType.containsGenericParameters()) {
                        final Map<TypeReference, TypeReference> returnMappings = new HashMap<TypeReference, TypeReference>();
                        new AddMappingsForArgumentVisitor(expectedType).visit(returnType, returnMappings);
                        newMappings.putAll(returnMappings);
                    }
                }
                if (!oldMappings.isEmpty() || !newMappings.isEmpty() || !inferredMappings.isEmpty()) {
                    mappings = oldMappings;
                    for (final TypeReference t : newMappings.keySet()) {
                        final TypeReference oldMapping = mappings.get(t);
                        final TypeReference newMapping = newMappings.get(t);
                        if (oldMapping == null || MetadataHelper.isSubType(newMapping, oldMapping)) {
                            mappings.put(t, newMapping);
                        }
                    }
                    for (final TypeReference t : inferredMappings.keySet()) {
                        final TypeReference oldMapping = mappings.get(t);
                        final TypeReference newMapping = inferredMappings.get(t);
                        if (oldMapping == null || MetadataHelper.isSubType(newMapping, oldMapping)) {
                            mappings.put(t, newMapping);
                        }
                    }
                }
                if (mappings != null) {
                    boundMethod = (actualMethod = TypeSubstitutionVisitor.instance().visitMethod((r != null) ? r : actualMethod, mappings));
                    expression.setOperand(boundMethod);
                    p = boundMethod.getParameters();
                }
                final TypeReference boundDeclaringType = boundMethod.getDeclaringType();
                if (boundDeclaringType.isGenericType()) {
                    if (mappings == null) {
                        mappings = new HashMap<TypeReference, TypeReference>();
                    }
                    for (final GenericParameter gp : boundDeclaringType.getGenericParameters()) {
                        final GenericParameter inScope = this._context.getCurrentMethod().findTypeVariable(gp.getName());
                        if (inScope != null && MetadataHelper.isSameType(gp, inScope)) {
                            continue;
                        }
                        if (mappings.containsKey(gp)) {
                            continue;
                        }
                        mappings.put(gp, MetadataHelper.eraseRecursive(gp));
                    }
                    boundMethod = TypeSubstitutionVisitor.instance().visitMethod(actualMethod, mappings);
                    expression.setOperand(boundMethod);
                    p = boundMethod.getParameters();
                }
                if (boundMethod.isGenericMethod()) {
                    if (mappings == null) {
                        mappings = new HashMap<TypeReference, TypeReference>();
                    }
                    for (final GenericParameter gp : boundMethod.getGenericParameters()) {
                        if (!mappings.containsKey(gp)) {
                            mappings.put(gp, MetadataHelper.eraseRecursive(gp));
                        }
                    }
                    boundMethod = TypeSubstitutionVisitor.instance().visitMethod(actualMethod, mappings);
                    expression.setOperand(boundMethod);
                    p = boundMethod.getParameters();
                }
                if (r != null && method.isGenericMethod()) {
                    final HashMap<TypeReference, TypeReference> tempMappings = new HashMap<TypeReference, TypeReference>();
                    final List<ParameterDefinition> bp = method.getParameters();
                    for (int j = 0, n = bp.size(); j < n; ++j) {
                        new AddMappingsForArgumentVisitor(bp.get(j).getParameterType()).visit(rp.get(j).getParameterType(), tempMappings);
                    }
                    boolean changed = false;
                    if (mappings == null) {
                        mappings = tempMappings;
                        changed = true;
                    }
                    else {
                        for (final TypeReference key : tempMappings.keySet()) {
                            if (!mappings.containsKey(key)) {
                                mappings.put(key, tempMappings.get(key));
                                changed = true;
                            }
                        }
                    }
                    if (changed) {
                        boundMethod = TypeSubstitutionVisitor.instance().visitMethod(actualMethod, mappings);
                        expression.setOperand(boundMethod);
                        p = boundMethod.getParameters();
                    }
                }
            }
            else {
                boundMethod = actualMethod;
            }
            if (hasThis && mappings != null) {
                TypeReference expectedTargetType2;
                if (boundMethod.isConstructor()) {
                    expectedTargetType2 = MetadataHelper.substituteGenericArguments(boundMethod.getDeclaringType(), mappings);
                }
                else {
                    expectedTargetType2 = boundMethod.getDeclaringType();
                }
                if (expectedTargetType2 != null && expectedTargetType2.isGenericDefinition() && arguments.get(0).getInferredType() != null) {
                    expectedTargetType2 = MetadataHelper.asSuper(expectedTargetType2, arguments.get(0).getInferredType());
                }
                final TypeReference inferredTargetType = this.inferTypeForExpression(arguments.get(0), expectedTargetType2, forceInferChildren);
                if (inferredTargetType != null) {
                    targetType = MetadataHelper.substituteGenericArguments(inferredTargetType, mappings);
                    if (MetadataHelper.isRawType(targetType) && !MetadataHelper.canReferenceTypeVariablesOf(targetType, this._context.getCurrentType())) {
                        targetType = MetadataHelper.erase(targetType);
                    }
                    boundMethod = MetadataHelper.asMemberOf(boundMethod, targetType);
                    p = boundMethod.getParameters();
                    expression.setOperand(boundMethod);
                }
            }
            for (int k = 0; k < parameters.size(); ++k) {
                final TypeReference pType2 = p.get(k).getParameterType();
                this.inferTypeForExpression(arguments.get(hasThis ? (k + 1) : k), pType2, forceInferChildren);
            }
        }
        if (hasThis && boundMethod.isConstructor()) {
            return boundMethod.getDeclaringType();
        }
        return boundMethod.getReturnType();
    }
    
    private TypeReference inferTypeForVariable(final Variable v, final TypeReference expectedType) {
        return this.inferTypeForVariable(v, expectedType, false, 0);
    }
    
    private TypeReference inferTypeForVariable(final Variable v, final TypeReference expectedType, final int flags) {
        return this.inferTypeForVariable(v, expectedType, false, flags);
    }
    
    private TypeReference inferTypeForVariable(final Variable v, final TypeReference expectedType, final boolean favorExpectedOverActual, final int flags) {
        final TypeReference lastInferredType = this._inferredVariableTypes.get(v);
        if (lastInferredType != null) {
            return adjustType(lastInferredType, flags);
        }
        if (this.isSingleStoreBoolean(v)) {
            return adjustType(BuiltinTypes.Boolean, flags);
        }
        if (favorExpectedOverActual && expectedType != null) {
            return adjustType(expectedType, flags);
        }
        final TypeReference variableType = v.getType();
        if (variableType != null) {
            return adjustType(variableType, flags);
        }
        if (v.isGenerated()) {
            return adjustType(expectedType, flags);
        }
        return adjustType(v.isParameter() ? v.getOriginalParameter().getParameterType() : v.getOriginalVariable().getVariableType(), flags);
    }
    
    private static TypeReference adjustType(final TypeReference type, final int flags) {
        if (Flags.testAny(flags, 1) && type == BuiltinTypes.Boolean) {
            return BuiltinTypes.Integer;
        }
        return type;
    }
    
    private TypeReference numericPromotion(final TypeReference type) {
        if (type == null) {
            return null;
        }
        switch ($SWITCH_TABLE$com$strobel$assembler$metadata$JvmType()[type.getSimpleType().ordinal()]) {
            case 2:
            case 4: {
                return BuiltinTypes.Integer;
            }
            default: {
                return type;
            }
        }
    }
    
    private TypeReference inferBinaryArguments(final Expression left, final Expression right, final TypeReference expectedType, final boolean forceInferChildren, final TypeReference leftPreferred, final TypeReference rightPreferred) {
        TypeReference actualLeftPreferred = leftPreferred;
        TypeReference actualRightPreferred = rightPreferred;
        if (actualLeftPreferred == null) {
            actualLeftPreferred = this.doInferTypeForExpression(left, expectedType, forceInferChildren, 0);
        }
        if (actualRightPreferred == null) {
            actualRightPreferred = this.doInferTypeForExpression(right, expectedType, forceInferChildren, 0);
        }
        if (actualLeftPreferred == BuiltinTypes.Null) {
            if (actualRightPreferred != null && !actualRightPreferred.isPrimitive()) {
                actualLeftPreferred = actualRightPreferred;
            }
        }
        else if (actualRightPreferred == BuiltinTypes.Null && actualLeftPreferred != null && !actualLeftPreferred.isPrimitive()) {
            actualRightPreferred = actualLeftPreferred;
        }
        if (actualLeftPreferred == BuiltinTypes.Character) {
            if (actualRightPreferred == BuiltinTypes.Integer && PatternMatching.matchCharacterConstant(right) != null) {
                actualRightPreferred = BuiltinTypes.Character;
            }
        }
        else if (actualRightPreferred == BuiltinTypes.Character && actualLeftPreferred == BuiltinTypes.Integer && PatternMatching.matchCharacterConstant(left) != null) {
            actualLeftPreferred = BuiltinTypes.Character;
        }
        if (this.isSameType(actualLeftPreferred, actualRightPreferred)) {
            left.setInferredType(actualLeftPreferred);
            left.setExpectedType(actualLeftPreferred);
            right.setInferredType(actualLeftPreferred);
            right.setExpectedType(actualLeftPreferred);
            return actualLeftPreferred;
        }
        if (this.isSameType(actualRightPreferred, this.doInferTypeForExpression(left, actualRightPreferred, forceInferChildren, 0))) {
            left.setInferredType(actualRightPreferred);
            left.setExpectedType(actualRightPreferred);
            right.setInferredType(actualRightPreferred);
            right.setExpectedType(actualRightPreferred);
            return actualRightPreferred;
        }
        if (this.isSameType(actualLeftPreferred, this.doInferTypeForExpression(right, actualLeftPreferred, forceInferChildren, 0))) {
            left.setInferredType(actualLeftPreferred);
            left.setExpectedType(actualLeftPreferred);
            right.setInferredType(actualLeftPreferred);
            right.setExpectedType(actualLeftPreferred);
            return actualLeftPreferred;
        }
        final TypeReference result = this.typeWithMoreInformation(actualLeftPreferred, actualRightPreferred);
        left.setExpectedType(result);
        right.setExpectedType(result);
        left.setInferredType(this.doInferTypeForExpression(left, result, forceInferChildren, 0));
        right.setInferredType(this.doInferTypeForExpression(right, result, forceInferChildren, 0));
        return result;
    }
    
    private TypeReference typeWithMoreInformation(final TypeReference leftPreferred, final TypeReference rightPreferred) {
        final int left = getInformationAmount(leftPreferred);
        final int right = getInformationAmount(rightPreferred);
        if (left < right) {
            return rightPreferred;
        }
        if (left > right) {
            return leftPreferred;
        }
        if (leftPreferred != null && rightPreferred != null) {
            return MetadataHelper.findCommonSuperType(leftPreferred.isGenericDefinition() ? new RawType(leftPreferred) : leftPreferred, rightPreferred.isGenericDefinition() ? new RawType(rightPreferred) : rightPreferred);
        }
        return leftPreferred;
    }
    
    private static int getInformationAmount(final TypeReference type) {
        if (type == null || type == BuiltinTypes.Null) {
            return 0;
        }
        switch ($SWITCH_TABLE$com$strobel$assembler$metadata$JvmType()[type.getSimpleType().ordinal()]) {
            case 1: {
                return 1;
            }
            case 2: {
                return 8;
            }
            case 3:
            case 4: {
                return 16;
            }
            case 5:
            case 7: {
                return 32;
            }
            case 6:
            case 8: {
                return 64;
            }
            default: {
                return 100;
            }
        }
    }
    
    static TypeReference getFieldType(final FieldReference field) {
        final FieldDefinition resolvedField = field.resolve();
        if (resolvedField != null) {
            final FieldReference asMember = MetadataHelper.asMemberOf(resolvedField, field.getDeclaringType());
            return asMember.getFieldType();
        }
        return substituteTypeArguments(field.getFieldType(), field);
    }
    
    static TypeReference substituteTypeArguments(final TypeReference type, final MemberReference member) {
        if (type instanceof ArrayType) {
            final ArrayType arrayType = (ArrayType)type;
            final TypeReference elementType = substituteTypeArguments(arrayType.getElementType(), member);
            if (!MetadataResolver.areEquivalent(elementType, arrayType.getElementType())) {
                return elementType.makeArrayType();
            }
            return type;
        }
        else {
            if (type instanceof IGenericInstance) {
                final IGenericInstance genericInstance = (IGenericInstance)type;
                final List<TypeReference> newTypeArguments = new ArrayList<TypeReference>();
                boolean isChanged = false;
                for (final TypeReference typeArgument : genericInstance.getTypeArguments()) {
                    final TypeReference newTypeArgument = substituteTypeArguments(typeArgument, member);
                    newTypeArguments.add(newTypeArgument);
                    isChanged |= (newTypeArgument != typeArgument);
                }
                return isChanged ? type.makeGenericType(newTypeArguments) : type;
            }
            if (type instanceof GenericParameter) {
                final GenericParameter genericParameter = (GenericParameter)type;
                final IGenericParameterProvider owner = genericParameter.getOwner();
                if (member.getDeclaringType() instanceof ArrayType) {
                    return member.getDeclaringType().getElementType();
                }
                if (owner instanceof MethodReference && member instanceof MethodReference) {
                    final MethodReference method = (MethodReference)member;
                    final MethodReference ownerMethod = (MethodReference)owner;
                    if (method.isGenericMethod() && MetadataResolver.areEquivalent(ownerMethod.getDeclaringType(), method.getDeclaringType()) && StringUtilities.equals(ownerMethod.getName(), method.getName()) && StringUtilities.equals(ownerMethod.getErasedSignature(), method.getErasedSignature())) {
                        if (method instanceof IGenericInstance) {
                            final List<TypeReference> typeArguments = ((IGenericInstance)member).getTypeArguments();
                            return typeArguments.get(genericParameter.getPosition());
                        }
                        return method.getGenericParameters().get(genericParameter.getPosition());
                    }
                }
                else if (owner instanceof TypeReference) {
                    TypeReference declaringType;
                    if (member instanceof TypeReference) {
                        declaringType = (TypeReference)member;
                    }
                    else {
                        declaringType = member.getDeclaringType();
                    }
                    if (MetadataResolver.areEquivalent((TypeReference)owner, declaringType)) {
                        if (declaringType instanceof IGenericInstance) {
                            final List<TypeReference> typeArguments2 = ((IGenericInstance)declaringType).getTypeArguments();
                            return typeArguments2.get(genericParameter.getPosition());
                        }
                        if (!declaringType.isGenericDefinition()) {
                            declaringType = declaringType.getUnderlyingType();
                        }
                        if (declaringType != null && declaringType.isGenericDefinition()) {
                            return declaringType.getGenericParameters().get(genericParameter.getPosition());
                        }
                    }
                }
            }
            return type;
        }
    }
    
    private boolean isSameType(final TypeReference t1, final TypeReference t2) {
        return MetadataHelper.isSameType(t1, t2, true);
    }
    
    private boolean anyDone(final List<ExpressionToInfer> expressions) {
        for (final ExpressionToInfer expression : expressions) {
            if (expression.done) {
                return true;
            }
        }
        return false;
    }
    
    private boolean allDone(final List<ExpressionToInfer> expressions) {
        for (final ExpressionToInfer expression : expressions) {
            if (!expression.done) {
                return false;
            }
        }
        return true;
    }
    
    public static <T> boolean trueForAll(final Iterable<T> sequence, final Predicate<T> condition) {
        for (final T item : sequence) {
            if (!condition.test(item)) {
                return false;
            }
        }
        return true;
    }
    
    public static boolean isBoolean(final TypeReference type) {
        return type != null && type.getSimpleType() == JvmType.Boolean;
    }
    
    private static TypeReference ensureReferenceType(final TypeReference mappedType) {
        if (mappedType == null) {
            return null;
        }
        if (mappedType.isPrimitive()) {
            switch ($SWITCH_TABLE$com$strobel$assembler$metadata$JvmType()[mappedType.getSimpleType().ordinal()]) {
                case 1: {
                    return CommonTypeReferences.Boolean;
                }
                case 2: {
                    return CommonTypeReferences.Byte;
                }
                case 3: {
                    return CommonTypeReferences.Character;
                }
                case 4: {
                    return CommonTypeReferences.Short;
                }
                case 5: {
                    return CommonTypeReferences.Integer;
                }
                case 6: {
                    return CommonTypeReferences.Long;
                }
                case 7: {
                    return CommonTypeReferences.Float;
                }
                case 8: {
                    return CommonTypeReferences.Double;
                }
            }
        }
        return mappedType;
    }
    
    static /* synthetic */ boolean access$0(final TypeAnalysis param_0) {
        return param_0._doneInitializing;
    }
    
    static /* synthetic */ TypeReference access$1(final TypeReference param_0) {
        return ensureReferenceType(param_0);
    }
    
    static /* synthetic */ TypeReference access$2(final TypeAnalysis param_0, final Variable param_1, final TypeReference param_2) {
        return param_0.inferTypeForVariable(param_1, param_2);
    }
    
    static /* synthetic */ Set access$3(final TypeAnalysis param_0) {
        return param_0._singleLoadVariables;
    }
    
    static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$assembler$metadata$JvmType() {
        final int[] loc_0 = TypeAnalysis.$SWITCH_TABLE$com$strobel$assembler$metadata$JvmType;
        if (loc_0 != null) {
            return loc_0;
        }
        final int[] loc_1 = new int[JvmType.values().length];
        try {
            loc_1[JvmType.Array.ordinal()] = 10;
        }
        catch (NoSuchFieldError loc_2) {}
        try {
            loc_1[JvmType.Boolean.ordinal()] = 1;
        }
        catch (NoSuchFieldError loc_3) {}
        try {
            loc_1[JvmType.Byte.ordinal()] = 2;
        }
        catch (NoSuchFieldError loc_4) {}
        try {
            loc_1[JvmType.Character.ordinal()] = 3;
        }
        catch (NoSuchFieldError loc_5) {}
        try {
            loc_1[JvmType.Double.ordinal()] = 8;
        }
        catch (NoSuchFieldError loc_6) {}
        try {
            loc_1[JvmType.Float.ordinal()] = 7;
        }
        catch (NoSuchFieldError loc_7) {}
        try {
            loc_1[JvmType.Integer.ordinal()] = 5;
        }
        catch (NoSuchFieldError loc_8) {}
        try {
            loc_1[JvmType.Long.ordinal()] = 6;
        }
        catch (NoSuchFieldError loc_9) {}
        try {
            loc_1[JvmType.Object.ordinal()] = 9;
        }
        catch (NoSuchFieldError loc_10) {}
        try {
            loc_1[JvmType.Short.ordinal()] = 4;
        }
        catch (NoSuchFieldError loc_11) {}
        try {
            loc_1[JvmType.TypeVariable.ordinal()] = 11;
        }
        catch (NoSuchFieldError loc_12) {}
        try {
            loc_1[JvmType.Void.ordinal()] = 13;
        }
        catch (NoSuchFieldError loc_13) {}
        try {
            loc_1[JvmType.Wildcard.ordinal()] = 12;
        }
        catch (NoSuchFieldError loc_14) {}
        return TypeAnalysis.$SWITCH_TABLE$com$strobel$assembler$metadata$JvmType = loc_1;
    }
    
    static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$decompiler$ast$AstCode() {
        final int[] loc_0 = TypeAnalysis.$SWITCH_TABLE$com$strobel$decompiler$ast$AstCode;
        if (loc_0 != null) {
            return loc_0;
        }
        final int[] loc_1 = new int[AstCode.values().length];
        try {
            loc_1[AstCode.AConstNull.ordinal()] = 2;
        }
        catch (NoSuchFieldError loc_2) {}
        try {
            loc_1[AstCode.AThrow.ordinal()] = 192;
        }
        catch (NoSuchFieldError loc_3) {}
        try {
            loc_1[AstCode.Add.ordinal()] = 221;
        }
        catch (NoSuchFieldError loc_4) {}
        try {
            loc_1[AstCode.And.ordinal()] = 230;
        }
        catch (NoSuchFieldError loc_5) {}
        try {
            loc_1[AstCode.ArrayLength.ordinal()] = 191;
        }
        catch (NoSuchFieldError loc_6) {}
        try {
            loc_1[AstCode.Bind.ordinal()] = 252;
        }
        catch (NoSuchFieldError loc_7) {}
        try {
            loc_1[AstCode.Box.ordinal()] = 259;
        }
        catch (NoSuchFieldError loc_8) {}
        try {
            loc_1[AstCode.Breakpoint.ordinal()] = 202;
        }
        catch (NoSuchFieldError loc_9) {}
        try {
            loc_1[AstCode.CheckCast.ordinal()] = 193;
        }
        catch (NoSuchFieldError loc_10) {}
        try {
            loc_1[AstCode.CmpEq.ordinal()] = 235;
        }
        catch (NoSuchFieldError loc_11) {}
        try {
            loc_1[AstCode.CmpGe.ordinal()] = 238;
        }
        catch (NoSuchFieldError loc_12) {}
        try {
            loc_1[AstCode.CmpGt.ordinal()] = 239;
        }
        catch (NoSuchFieldError loc_13) {}
        try {
            loc_1[AstCode.CmpLe.ordinal()] = 240;
        }
        catch (NoSuchFieldError loc_14) {}
        try {
            loc_1[AstCode.CmpLt.ordinal()] = 237;
        }
        catch (NoSuchFieldError loc_15) {}
        try {
            loc_1[AstCode.CmpNe.ordinal()] = 236;
        }
        catch (NoSuchFieldError loc_16) {}
        try {
            loc_1[AstCode.CompoundAssignment.ordinal()] = 256;
        }
        catch (NoSuchFieldError loc_17) {}
        try {
            loc_1[AstCode.D2F.ordinal()] = 145;
        }
        catch (NoSuchFieldError loc_18) {}
        try {
            loc_1[AstCode.D2I.ordinal()] = 143;
        }
        catch (NoSuchFieldError loc_19) {}
        try {
            loc_1[AstCode.D2L.ordinal()] = 144;
        }
        catch (NoSuchFieldError loc_20) {}
        try {
            loc_1[AstCode.DefaultValue.ordinal()] = 261;
        }
        catch (NoSuchFieldError loc_21) {}
        try {
            loc_1[AstCode.Div.ordinal()] = 224;
        }
        catch (NoSuchFieldError loc_22) {}
        try {
            loc_1[AstCode.Dup.ordinal()] = 90;
        }
        catch (NoSuchFieldError loc_23) {}
        try {
            loc_1[AstCode.Dup2.ordinal()] = 93;
        }
        catch (NoSuchFieldError loc_24) {}
        try {
            loc_1[AstCode.Dup2X1.ordinal()] = 94;
        }
        catch (NoSuchFieldError loc_25) {}
        try {
            loc_1[AstCode.Dup2X2.ordinal()] = 95;
        }
        catch (NoSuchFieldError loc_26) {}
        try {
            loc_1[AstCode.DupX1.ordinal()] = 91;
        }
        catch (NoSuchFieldError loc_27) {}
        try {
            loc_1[AstCode.DupX2.ordinal()] = 92;
        }
        catch (NoSuchFieldError loc_28) {}
        try {
            loc_1[AstCode.EndFinally.ordinal()] = 216;
        }
        catch (NoSuchFieldError loc_29) {}
        try {
            loc_1[AstCode.F2D.ordinal()] = 142;
        }
        catch (NoSuchFieldError loc_30) {}
        try {
            loc_1[AstCode.F2I.ordinal()] = 140;
        }
        catch (NoSuchFieldError loc_31) {}
        try {
            loc_1[AstCode.F2L.ordinal()] = 141;
        }
        catch (NoSuchFieldError loc_32) {}
        try {
            loc_1[AstCode.GetField.ordinal()] = 181;
        }
        catch (NoSuchFieldError loc_33) {}
        try {
            loc_1[AstCode.GetStatic.ordinal()] = 179;
        }
        catch (NoSuchFieldError loc_34) {}
        try {
            loc_1[AstCode.Goto.ordinal()] = 168;
        }
        catch (NoSuchFieldError loc_35) {}
        try {
            loc_1[AstCode.I2B.ordinal()] = 146;
        }
        catch (NoSuchFieldError loc_36) {}
        try {
            loc_1[AstCode.I2C.ordinal()] = 147;
        }
        catch (NoSuchFieldError loc_37) {}
        try {
            loc_1[AstCode.I2D.ordinal()] = 136;
        }
        catch (NoSuchFieldError loc_38) {}
        try {
            loc_1[AstCode.I2F.ordinal()] = 135;
        }
        catch (NoSuchFieldError loc_39) {}
        try {
            loc_1[AstCode.I2L.ordinal()] = 134;
        }
        catch (NoSuchFieldError loc_40) {}
        try {
            loc_1[AstCode.I2S.ordinal()] = 148;
        }
        catch (NoSuchFieldError loc_41) {}
        try {
            loc_1[AstCode.IfTrue.ordinal()] = 241;
        }
        catch (NoSuchFieldError loc_42) {}
        try {
            loc_1[AstCode.Inc.ordinal()] = 234;
        }
        catch (NoSuchFieldError loc_43) {}
        try {
            loc_1[AstCode.InitArray.ordinal()] = 249;
        }
        catch (NoSuchFieldError loc_44) {}
        try {
            loc_1[AstCode.InitObject.ordinal()] = 248;
        }
        catch (NoSuchFieldError loc_45) {}
        try {
            loc_1[AstCode.InstanceOf.ordinal()] = 194;
        }
        catch (NoSuchFieldError loc_46) {}
        try {
            loc_1[AstCode.InvokeDynamic.ordinal()] = 187;
        }
        catch (NoSuchFieldError loc_47) {}
        try {
            loc_1[AstCode.InvokeInterface.ordinal()] = 186;
        }
        catch (NoSuchFieldError loc_48) {}
        try {
            loc_1[AstCode.InvokeSpecial.ordinal()] = 184;
        }
        catch (NoSuchFieldError loc_49) {}
        try {
            loc_1[AstCode.InvokeStatic.ordinal()] = 185;
        }
        catch (NoSuchFieldError loc_50) {}
        try {
            loc_1[AstCode.InvokeVirtual.ordinal()] = 183;
        }
        catch (NoSuchFieldError loc_51) {}
        try {
            loc_1[AstCode.Jsr.ordinal()] = 169;
        }
        catch (NoSuchFieldError loc_52) {}
        try {
            loc_1[AstCode.L2D.ordinal()] = 139;
        }
        catch (NoSuchFieldError loc_53) {}
        try {
            loc_1[AstCode.L2F.ordinal()] = 138;
        }
        catch (NoSuchFieldError loc_54) {}
        try {
            loc_1[AstCode.L2I.ordinal()] = 137;
        }
        catch (NoSuchFieldError loc_55) {}
        try {
            loc_1[AstCode.LdC.ordinal()] = 19;
        }
        catch (NoSuchFieldError loc_56) {}
        try {
            loc_1[AstCode.Leave.ordinal()] = 215;
        }
        catch (NoSuchFieldError loc_57) {}
        try {
            loc_1[AstCode.Load.ordinal()] = 217;
        }
        catch (NoSuchFieldError loc_58) {}
        try {
            loc_1[AstCode.LoadElement.ordinal()] = 219;
        }
        catch (NoSuchFieldError loc_59) {}
        try {
            loc_1[AstCode.LoadException.ordinal()] = 244;
        }
        catch (NoSuchFieldError loc_60) {}
        try {
            loc_1[AstCode.LogicalAnd.ordinal()] = 246;
        }
        catch (NoSuchFieldError loc_61) {}
        try {
            loc_1[AstCode.LogicalNot.ordinal()] = 245;
        }
        catch (NoSuchFieldError loc_62) {}
        try {
            loc_1[AstCode.LogicalOr.ordinal()] = 247;
        }
        catch (NoSuchFieldError loc_63) {}
        try {
            loc_1[AstCode.LoopContinue.ordinal()] = 255;
        }
        catch (NoSuchFieldError loc_64) {}
        try {
            loc_1[AstCode.LoopOrSwitchBreak.ordinal()] = 254;
        }
        catch (NoSuchFieldError loc_65) {}
        try {
            loc_1[AstCode.MonitorEnter.ordinal()] = 195;
        }
        catch (NoSuchFieldError loc_66) {}
        try {
            loc_1[AstCode.MonitorExit.ordinal()] = 196;
        }
        catch (NoSuchFieldError loc_67) {}
        try {
            loc_1[AstCode.Mul.ordinal()] = 223;
        }
        catch (NoSuchFieldError loc_68) {}
        try {
            loc_1[AstCode.MultiANewArray.ordinal()] = 197;
        }
        catch (NoSuchFieldError loc_69) {}
        try {
            loc_1[AstCode.Neg.ordinal()] = 226;
        }
        catch (NoSuchFieldError loc_70) {}
        try {
            loc_1[AstCode.NewArray.ordinal()] = 243;
        }
        catch (NoSuchFieldError loc_71) {}
        try {
            loc_1[AstCode.Nop.ordinal()] = 1;
        }
        catch (NoSuchFieldError loc_72) {}
        try {
            loc_1[AstCode.Not.ordinal()] = 232;
        }
        catch (NoSuchFieldError loc_73) {}
        try {
            loc_1[AstCode.Or.ordinal()] = 231;
        }
        catch (NoSuchFieldError loc_74) {}
        try {
            loc_1[AstCode.Pop.ordinal()] = 88;
        }
        catch (NoSuchFieldError loc_75) {}
        try {
            loc_1[AstCode.Pop2.ordinal()] = 89;
        }
        catch (NoSuchFieldError loc_76) {}
        try {
            loc_1[AstCode.PostIncrement.ordinal()] = 258;
        }
        catch (NoSuchFieldError loc_77) {}
        try {
            loc_1[AstCode.PreIncrement.ordinal()] = 257;
        }
        catch (NoSuchFieldError loc_78) {}
        try {
            loc_1[AstCode.PutField.ordinal()] = 182;
        }
        catch (NoSuchFieldError loc_79) {}
        try {
            loc_1[AstCode.PutStatic.ordinal()] = 180;
        }
        catch (NoSuchFieldError loc_80) {}
        try {
            loc_1[AstCode.Rem.ordinal()] = 225;
        }
        catch (NoSuchFieldError loc_81) {}
        try {
            loc_1[AstCode.Ret.ordinal()] = 170;
        }
        catch (NoSuchFieldError loc_82) {}
        try {
            loc_1[AstCode.Return.ordinal()] = 242;
        }
        catch (NoSuchFieldError loc_83) {}
        try {
            loc_1[AstCode.Shl.ordinal()] = 227;
        }
        catch (NoSuchFieldError loc_84) {}
        try {
            loc_1[AstCode.Shr.ordinal()] = 228;
        }
        catch (NoSuchFieldError loc_85) {}
        try {
            loc_1[AstCode.Store.ordinal()] = 218;
        }
        catch (NoSuchFieldError loc_86) {}
        try {
            loc_1[AstCode.StoreElement.ordinal()] = 220;
        }
        catch (NoSuchFieldError loc_87) {}
        try {
            loc_1[AstCode.Sub.ordinal()] = 222;
        }
        catch (NoSuchFieldError loc_88) {}
        try {
            loc_1[AstCode.Swap.ordinal()] = 96;
        }
        catch (NoSuchFieldError loc_89) {}
        try {
            loc_1[AstCode.Switch.ordinal()] = 250;
        }
        catch (NoSuchFieldError loc_90) {}
        try {
            loc_1[AstCode.TernaryOp.ordinal()] = 253;
        }
        catch (NoSuchFieldError loc_91) {}
        try {
            loc_1[AstCode.UShr.ordinal()] = 229;
        }
        catch (NoSuchFieldError loc_92) {}
        try {
            loc_1[AstCode.Unbox.ordinal()] = 260;
        }
        catch (NoSuchFieldError loc_93) {}
        try {
            loc_1[AstCode.Wrap.ordinal()] = 251;
        }
        catch (NoSuchFieldError loc_94) {}
        try {
            loc_1[AstCode.Xor.ordinal()] = 233;
        }
        catch (NoSuchFieldError loc_95) {}
        try {
            loc_1[AstCode.__AALoad.ordinal()] = 51;
        }
        catch (NoSuchFieldError loc_96) {}
        try {
            loc_1[AstCode.__AAStore.ordinal()] = 84;
        }
        catch (NoSuchFieldError loc_97) {}
        try {
            loc_1[AstCode.__ALoad.ordinal()] = 26;
        }
        catch (NoSuchFieldError loc_98) {}
        try {
            loc_1[AstCode.__ALoad0.ordinal()] = 43;
        }
        catch (NoSuchFieldError loc_99) {}
        try {
            loc_1[AstCode.__ALoad1.ordinal()] = 44;
        }
        catch (NoSuchFieldError loc_100) {}
        try {
            loc_1[AstCode.__ALoad2.ordinal()] = 45;
        }
        catch (NoSuchFieldError loc_101) {}
        try {
            loc_1[AstCode.__ALoad3.ordinal()] = 46;
        }
        catch (NoSuchFieldError loc_102) {}
        try {
            loc_1[AstCode.__ALoadW.ordinal()] = 207;
        }
        catch (NoSuchFieldError loc_103) {}
        try {
            loc_1[AstCode.__ANewArray.ordinal()] = 190;
        }
        catch (NoSuchFieldError loc_104) {}
        try {
            loc_1[AstCode.__AReturn.ordinal()] = 177;
        }
        catch (NoSuchFieldError loc_105) {}
        try {
            loc_1[AstCode.__AStore.ordinal()] = 59;
        }
        catch (NoSuchFieldError loc_106) {}
        try {
            loc_1[AstCode.__AStore0.ordinal()] = 76;
        }
        catch (NoSuchFieldError loc_107) {}
        try {
            loc_1[AstCode.__AStore1.ordinal()] = 77;
        }
        catch (NoSuchFieldError loc_108) {}
        try {
            loc_1[AstCode.__AStore2.ordinal()] = 78;
        }
        catch (NoSuchFieldError loc_109) {}
        try {
            loc_1[AstCode.__AStore3.ordinal()] = 79;
        }
        catch (NoSuchFieldError loc_110) {}
        try {
            loc_1[AstCode.__AStoreW.ordinal()] = 212;
        }
        catch (NoSuchFieldError loc_111) {}
        try {
            loc_1[AstCode.__BALoad.ordinal()] = 52;
        }
        catch (NoSuchFieldError loc_112) {}
        try {
            loc_1[AstCode.__BAStore.ordinal()] = 85;
        }
        catch (NoSuchFieldError loc_113) {}
        try {
            loc_1[AstCode.__BIPush.ordinal()] = 17;
        }
        catch (NoSuchFieldError loc_114) {}
        try {
            loc_1[AstCode.__CALoad.ordinal()] = 53;
        }
        catch (NoSuchFieldError loc_115) {}
        try {
            loc_1[AstCode.__CAStore.ordinal()] = 86;
        }
        catch (NoSuchFieldError loc_116) {}
        try {
            loc_1[AstCode.__DALoad.ordinal()] = 50;
        }
        catch (NoSuchFieldError loc_117) {}
        try {
            loc_1[AstCode.__DAStore.ordinal()] = 83;
        }
        catch (NoSuchFieldError loc_118) {}
        try {
            loc_1[AstCode.__DAdd.ordinal()] = 100;
        }
        catch (NoSuchFieldError loc_119) {}
        try {
            loc_1[AstCode.__DCmpG.ordinal()] = 153;
        }
        catch (NoSuchFieldError loc_120) {}
        try {
            loc_1[AstCode.__DCmpL.ordinal()] = 152;
        }
        catch (NoSuchFieldError loc_121) {}
        try {
            loc_1[AstCode.__DConst0.ordinal()] = 15;
        }
        catch (NoSuchFieldError loc_122) {}
        try {
            loc_1[AstCode.__DConst1.ordinal()] = 16;
        }
        catch (NoSuchFieldError loc_123) {}
        try {
            loc_1[AstCode.__DDiv.ordinal()] = 112;
        }
        catch (NoSuchFieldError loc_124) {}
        try {
            loc_1[AstCode.__DLoad.ordinal()] = 25;
        }
        catch (NoSuchFieldError loc_125) {}
        try {
            loc_1[AstCode.__DLoad0.ordinal()] = 39;
        }
        catch (NoSuchFieldError loc_126) {}
        try {
            loc_1[AstCode.__DLoad1.ordinal()] = 40;
        }
        catch (NoSuchFieldError loc_127) {}
        try {
            loc_1[AstCode.__DLoad2.ordinal()] = 41;
        }
        catch (NoSuchFieldError loc_128) {}
        try {
            loc_1[AstCode.__DLoad3.ordinal()] = 42;
        }
        catch (NoSuchFieldError loc_129) {}
        try {
            loc_1[AstCode.__DLoadW.ordinal()] = 206;
        }
        catch (NoSuchFieldError loc_130) {}
        try {
            loc_1[AstCode.__DMul.ordinal()] = 108;
        }
        catch (NoSuchFieldError loc_131) {}
        try {
            loc_1[AstCode.__DNeg.ordinal()] = 120;
        }
        catch (NoSuchFieldError loc_132) {}
        try {
            loc_1[AstCode.__DRem.ordinal()] = 116;
        }
        catch (NoSuchFieldError loc_133) {}
        try {
            loc_1[AstCode.__DReturn.ordinal()] = 176;
        }
        catch (NoSuchFieldError loc_134) {}
        try {
            loc_1[AstCode.__DStore.ordinal()] = 58;
        }
        catch (NoSuchFieldError loc_135) {}
        try {
            loc_1[AstCode.__DStore0.ordinal()] = 72;
        }
        catch (NoSuchFieldError loc_136) {}
        try {
            loc_1[AstCode.__DStore1.ordinal()] = 73;
        }
        catch (NoSuchFieldError loc_137) {}
        try {
            loc_1[AstCode.__DStore2.ordinal()] = 74;
        }
        catch (NoSuchFieldError loc_138) {}
        try {
            loc_1[AstCode.__DStore3.ordinal()] = 75;
        }
        catch (NoSuchFieldError loc_139) {}
        try {
            loc_1[AstCode.__DStoreW.ordinal()] = 211;
        }
        catch (NoSuchFieldError loc_140) {}
        try {
            loc_1[AstCode.__DSub.ordinal()] = 104;
        }
        catch (NoSuchFieldError loc_141) {}
        try {
            loc_1[AstCode.__FALoad.ordinal()] = 49;
        }
        catch (NoSuchFieldError loc_142) {}
        try {
            loc_1[AstCode.__FAStore.ordinal()] = 82;
        }
        catch (NoSuchFieldError loc_143) {}
        try {
            loc_1[AstCode.__FAdd.ordinal()] = 99;
        }
        catch (NoSuchFieldError loc_144) {}
        try {
            loc_1[AstCode.__FCmpG.ordinal()] = 151;
        }
        catch (NoSuchFieldError loc_145) {}
        try {
            loc_1[AstCode.__FCmpL.ordinal()] = 150;
        }
        catch (NoSuchFieldError loc_146) {}
        try {
            loc_1[AstCode.__FConst0.ordinal()] = 12;
        }
        catch (NoSuchFieldError loc_147) {}
        try {
            loc_1[AstCode.__FConst1.ordinal()] = 13;
        }
        catch (NoSuchFieldError loc_148) {}
        try {
            loc_1[AstCode.__FConst2.ordinal()] = 14;
        }
        catch (NoSuchFieldError loc_149) {}
        try {
            loc_1[AstCode.__FDiv.ordinal()] = 111;
        }
        catch (NoSuchFieldError loc_150) {}
        try {
            loc_1[AstCode.__FLoad.ordinal()] = 24;
        }
        catch (NoSuchFieldError loc_151) {}
        try {
            loc_1[AstCode.__FLoad0.ordinal()] = 35;
        }
        catch (NoSuchFieldError loc_152) {}
        try {
            loc_1[AstCode.__FLoad1.ordinal()] = 36;
        }
        catch (NoSuchFieldError loc_153) {}
        try {
            loc_1[AstCode.__FLoad2.ordinal()] = 37;
        }
        catch (NoSuchFieldError loc_154) {}
        try {
            loc_1[AstCode.__FLoad3.ordinal()] = 38;
        }
        catch (NoSuchFieldError loc_155) {}
        try {
            loc_1[AstCode.__FLoadW.ordinal()] = 205;
        }
        catch (NoSuchFieldError loc_156) {}
        try {
            loc_1[AstCode.__FMul.ordinal()] = 107;
        }
        catch (NoSuchFieldError loc_157) {}
        try {
            loc_1[AstCode.__FNeg.ordinal()] = 119;
        }
        catch (NoSuchFieldError loc_158) {}
        try {
            loc_1[AstCode.__FRem.ordinal()] = 115;
        }
        catch (NoSuchFieldError loc_159) {}
        try {
            loc_1[AstCode.__FReturn.ordinal()] = 175;
        }
        catch (NoSuchFieldError loc_160) {}
        try {
            loc_1[AstCode.__FStore.ordinal()] = 57;
        }
        catch (NoSuchFieldError loc_161) {}
        try {
            loc_1[AstCode.__FStore0.ordinal()] = 68;
        }
        catch (NoSuchFieldError loc_162) {}
        try {
            loc_1[AstCode.__FStore1.ordinal()] = 69;
        }
        catch (NoSuchFieldError loc_163) {}
        try {
            loc_1[AstCode.__FStore2.ordinal()] = 70;
        }
        catch (NoSuchFieldError loc_164) {}
        try {
            loc_1[AstCode.__FStore3.ordinal()] = 71;
        }
        catch (NoSuchFieldError loc_165) {}
        try {
            loc_1[AstCode.__FStoreW.ordinal()] = 210;
        }
        catch (NoSuchFieldError loc_166) {}
        try {
            loc_1[AstCode.__FSub.ordinal()] = 103;
        }
        catch (NoSuchFieldError loc_167) {}
        try {
            loc_1[AstCode.__GotoW.ordinal()] = 200;
        }
        catch (NoSuchFieldError loc_168) {}
        try {
            loc_1[AstCode.__IALoad.ordinal()] = 47;
        }
        catch (NoSuchFieldError loc_169) {}
        try {
            loc_1[AstCode.__IAStore.ordinal()] = 80;
        }
        catch (NoSuchFieldError loc_170) {}
        try {
            loc_1[AstCode.__IAdd.ordinal()] = 97;
        }
        catch (NoSuchFieldError loc_171) {}
        try {
            loc_1[AstCode.__IAnd.ordinal()] = 127;
        }
        catch (NoSuchFieldError loc_172) {}
        try {
            loc_1[AstCode.__IConst0.ordinal()] = 4;
        }
        catch (NoSuchFieldError loc_173) {}
        try {
            loc_1[AstCode.__IConst1.ordinal()] = 5;
        }
        catch (NoSuchFieldError loc_174) {}
        try {
            loc_1[AstCode.__IConst2.ordinal()] = 6;
        }
        catch (NoSuchFieldError loc_175) {}
        try {
            loc_1[AstCode.__IConst3.ordinal()] = 7;
        }
        catch (NoSuchFieldError loc_176) {}
        try {
            loc_1[AstCode.__IConst4.ordinal()] = 8;
        }
        catch (NoSuchFieldError loc_177) {}
        try {
            loc_1[AstCode.__IConst5.ordinal()] = 9;
        }
        catch (NoSuchFieldError loc_178) {}
        try {
            loc_1[AstCode.__IConstM1.ordinal()] = 3;
        }
        catch (NoSuchFieldError loc_179) {}
        try {
            loc_1[AstCode.__IDiv.ordinal()] = 109;
        }
        catch (NoSuchFieldError loc_180) {}
        try {
            loc_1[AstCode.__IInc.ordinal()] = 133;
        }
        catch (NoSuchFieldError loc_181) {}
        try {
            loc_1[AstCode.__IIncW.ordinal()] = 213;
        }
        catch (NoSuchFieldError loc_182) {}
        try {
            loc_1[AstCode.__ILoad.ordinal()] = 22;
        }
        catch (NoSuchFieldError loc_183) {}
        try {
            loc_1[AstCode.__ILoad0.ordinal()] = 27;
        }
        catch (NoSuchFieldError loc_184) {}
        try {
            loc_1[AstCode.__ILoad1.ordinal()] = 28;
        }
        catch (NoSuchFieldError loc_185) {}
        try {
            loc_1[AstCode.__ILoad2.ordinal()] = 29;
        }
        catch (NoSuchFieldError loc_186) {}
        try {
            loc_1[AstCode.__ILoad3.ordinal()] = 30;
        }
        catch (NoSuchFieldError loc_187) {}
        try {
            loc_1[AstCode.__ILoadW.ordinal()] = 203;
        }
        catch (NoSuchFieldError loc_188) {}
        try {
            loc_1[AstCode.__IMul.ordinal()] = 105;
        }
        catch (NoSuchFieldError loc_189) {}
        try {
            loc_1[AstCode.__INeg.ordinal()] = 117;
        }
        catch (NoSuchFieldError loc_190) {}
        try {
            loc_1[AstCode.__IOr.ordinal()] = 129;
        }
        catch (NoSuchFieldError loc_191) {}
        try {
            loc_1[AstCode.__IRem.ordinal()] = 113;
        }
        catch (NoSuchFieldError loc_192) {}
        try {
            loc_1[AstCode.__IReturn.ordinal()] = 173;
        }
        catch (NoSuchFieldError loc_193) {}
        try {
            loc_1[AstCode.__IShl.ordinal()] = 121;
        }
        catch (NoSuchFieldError loc_194) {}
        try {
            loc_1[AstCode.__IShr.ordinal()] = 123;
        }
        catch (NoSuchFieldError loc_195) {}
        try {
            loc_1[AstCode.__IStore.ordinal()] = 55;
        }
        catch (NoSuchFieldError loc_196) {}
        try {
            loc_1[AstCode.__IStore0.ordinal()] = 60;
        }
        catch (NoSuchFieldError loc_197) {}
        try {
            loc_1[AstCode.__IStore1.ordinal()] = 61;
        }
        catch (NoSuchFieldError loc_198) {}
        try {
            loc_1[AstCode.__IStore2.ordinal()] = 62;
        }
        catch (NoSuchFieldError loc_199) {}
        try {
            loc_1[AstCode.__IStore3.ordinal()] = 63;
        }
        catch (NoSuchFieldError loc_200) {}
        try {
            loc_1[AstCode.__IStoreW.ordinal()] = 208;
        }
        catch (NoSuchFieldError loc_201) {}
        try {
            loc_1[AstCode.__ISub.ordinal()] = 101;
        }
        catch (NoSuchFieldError loc_202) {}
        try {
            loc_1[AstCode.__IUShr.ordinal()] = 125;
        }
        catch (NoSuchFieldError loc_203) {}
        try {
            loc_1[AstCode.__IXor.ordinal()] = 131;
        }
        catch (NoSuchFieldError loc_204) {}
        try {
            loc_1[AstCode.__IfACmpEq.ordinal()] = 166;
        }
        catch (NoSuchFieldError loc_205) {}
        try {
            loc_1[AstCode.__IfACmpNe.ordinal()] = 167;
        }
        catch (NoSuchFieldError loc_206) {}
        try {
            loc_1[AstCode.__IfEq.ordinal()] = 154;
        }
        catch (NoSuchFieldError loc_207) {}
        try {
            loc_1[AstCode.__IfGe.ordinal()] = 157;
        }
        catch (NoSuchFieldError loc_208) {}
        try {
            loc_1[AstCode.__IfGt.ordinal()] = 158;
        }
        catch (NoSuchFieldError loc_209) {}
        try {
            loc_1[AstCode.__IfICmpEq.ordinal()] = 160;
        }
        catch (NoSuchFieldError loc_210) {}
        try {
            loc_1[AstCode.__IfICmpGe.ordinal()] = 163;
        }
        catch (NoSuchFieldError loc_211) {}
        try {
            loc_1[AstCode.__IfICmpGt.ordinal()] = 164;
        }
        catch (NoSuchFieldError loc_212) {}
        try {
            loc_1[AstCode.__IfICmpLe.ordinal()] = 165;
        }
        catch (NoSuchFieldError loc_213) {}
        try {
            loc_1[AstCode.__IfICmpLt.ordinal()] = 162;
        }
        catch (NoSuchFieldError loc_214) {}
        try {
            loc_1[AstCode.__IfICmpNe.ordinal()] = 161;
        }
        catch (NoSuchFieldError loc_215) {}
        try {
            loc_1[AstCode.__IfLe.ordinal()] = 159;
        }
        catch (NoSuchFieldError loc_216) {}
        try {
            loc_1[AstCode.__IfLt.ordinal()] = 156;
        }
        catch (NoSuchFieldError loc_217) {}
        try {
            loc_1[AstCode.__IfNe.ordinal()] = 155;
        }
        catch (NoSuchFieldError loc_218) {}
        try {
            loc_1[AstCode.__IfNonNull.ordinal()] = 199;
        }
        catch (NoSuchFieldError loc_219) {}
        try {
            loc_1[AstCode.__IfNull.ordinal()] = 198;
        }
        catch (NoSuchFieldError loc_220) {}
        try {
            loc_1[AstCode.__JsrW.ordinal()] = 201;
        }
        catch (NoSuchFieldError loc_221) {}
        try {
            loc_1[AstCode.__LALoad.ordinal()] = 48;
        }
        catch (NoSuchFieldError loc_222) {}
        try {
            loc_1[AstCode.__LAStore.ordinal()] = 81;
        }
        catch (NoSuchFieldError loc_223) {}
        try {
            loc_1[AstCode.__LAdd.ordinal()] = 98;
        }
        catch (NoSuchFieldError loc_224) {}
        try {
            loc_1[AstCode.__LAnd.ordinal()] = 128;
        }
        catch (NoSuchFieldError loc_225) {}
        try {
            loc_1[AstCode.__LCmp.ordinal()] = 149;
        }
        catch (NoSuchFieldError loc_226) {}
        try {
            loc_1[AstCode.__LConst0.ordinal()] = 10;
        }
        catch (NoSuchFieldError loc_227) {}
        try {
            loc_1[AstCode.__LConst1.ordinal()] = 11;
        }
        catch (NoSuchFieldError loc_228) {}
        try {
            loc_1[AstCode.__LDiv.ordinal()] = 110;
        }
        catch (NoSuchFieldError loc_229) {}
        try {
            loc_1[AstCode.__LLoad.ordinal()] = 23;
        }
        catch (NoSuchFieldError loc_230) {}
        try {
            loc_1[AstCode.__LLoad0.ordinal()] = 31;
        }
        catch (NoSuchFieldError loc_231) {}
        try {
            loc_1[AstCode.__LLoad1.ordinal()] = 32;
        }
        catch (NoSuchFieldError loc_232) {}
        try {
            loc_1[AstCode.__LLoad2.ordinal()] = 33;
        }
        catch (NoSuchFieldError loc_233) {}
        try {
            loc_1[AstCode.__LLoad3.ordinal()] = 34;
        }
        catch (NoSuchFieldError loc_234) {}
        try {
            loc_1[AstCode.__LLoadW.ordinal()] = 204;
        }
        catch (NoSuchFieldError loc_235) {}
        try {
            loc_1[AstCode.__LMul.ordinal()] = 106;
        }
        catch (NoSuchFieldError loc_236) {}
        try {
            loc_1[AstCode.__LNeg.ordinal()] = 118;
        }
        catch (NoSuchFieldError loc_237) {}
        try {
            loc_1[AstCode.__LOr.ordinal()] = 130;
        }
        catch (NoSuchFieldError loc_238) {}
        try {
            loc_1[AstCode.__LRem.ordinal()] = 114;
        }
        catch (NoSuchFieldError loc_239) {}
        try {
            loc_1[AstCode.__LReturn.ordinal()] = 174;
        }
        catch (NoSuchFieldError loc_240) {}
        try {
            loc_1[AstCode.__LShl.ordinal()] = 122;
        }
        catch (NoSuchFieldError loc_241) {}
        try {
            loc_1[AstCode.__LShr.ordinal()] = 124;
        }
        catch (NoSuchFieldError loc_242) {}
        try {
            loc_1[AstCode.__LStore.ordinal()] = 56;
        }
        catch (NoSuchFieldError loc_243) {}
        try {
            loc_1[AstCode.__LStore0.ordinal()] = 64;
        }
        catch (NoSuchFieldError loc_244) {}
        try {
            loc_1[AstCode.__LStore1.ordinal()] = 65;
        }
        catch (NoSuchFieldError loc_245) {}
        try {
            loc_1[AstCode.__LStore2.ordinal()] = 66;
        }
        catch (NoSuchFieldError loc_246) {}
        try {
            loc_1[AstCode.__LStore3.ordinal()] = 67;
        }
        catch (NoSuchFieldError loc_247) {}
        try {
            loc_1[AstCode.__LStoreW.ordinal()] = 209;
        }
        catch (NoSuchFieldError loc_248) {}
        try {
            loc_1[AstCode.__LSub.ordinal()] = 102;
        }
        catch (NoSuchFieldError loc_249) {}
        try {
            loc_1[AstCode.__LUShr.ordinal()] = 126;
        }
        catch (NoSuchFieldError loc_250) {}
        try {
            loc_1[AstCode.__LXor.ordinal()] = 132;
        }
        catch (NoSuchFieldError loc_251) {}
        try {
            loc_1[AstCode.__LdC2W.ordinal()] = 21;
        }
        catch (NoSuchFieldError loc_252) {}
        try {
            loc_1[AstCode.__LdCW.ordinal()] = 20;
        }
        catch (NoSuchFieldError loc_253) {}
        try {
            loc_1[AstCode.__LookupSwitch.ordinal()] = 172;
        }
        catch (NoSuchFieldError loc_254) {}
        try {
            loc_1[AstCode.__New.ordinal()] = 188;
        }
        catch (NoSuchFieldError loc_255) {}
        try {
            loc_1[AstCode.__NewArray.ordinal()] = 189;
        }
        catch (NoSuchFieldError loc_256) {}
        try {
            loc_1[AstCode.__RetW.ordinal()] = 214;
        }
        catch (NoSuchFieldError loc_257) {}
        try {
            loc_1[AstCode.__Return.ordinal()] = 178;
        }
        catch (NoSuchFieldError loc_258) {}
        try {
            loc_1[AstCode.__SALoad.ordinal()] = 54;
        }
        catch (NoSuchFieldError loc_259) {}
        try {
            loc_1[AstCode.__SAStore.ordinal()] = 87;
        }
        catch (NoSuchFieldError loc_260) {}
        try {
            loc_1[AstCode.__SIPush.ordinal()] = 18;
        }
        catch (NoSuchFieldError loc_261) {}
        try {
            loc_1[AstCode.__TableSwitch.ordinal()] = 171;
        }
        catch (NoSuchFieldError loc_262) {}
        return TypeAnalysis.$SWITCH_TABLE$com$strobel$decompiler$ast$AstCode = loc_1;
    }
    
    static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$assembler$metadata$MethodHandleType() {
        final int[] loc_0 = TypeAnalysis.$SWITCH_TABLE$com$strobel$assembler$metadata$MethodHandleType;
        if (loc_0 != null) {
            return loc_0;
        }
        final int[] loc_1 = new int[MethodHandleType.values().length];
        try {
            loc_1[MethodHandleType.GetField.ordinal()] = 1;
        }
        catch (NoSuchFieldError loc_2) {}
        try {
            loc_1[MethodHandleType.GetStatic.ordinal()] = 2;
        }
        catch (NoSuchFieldError loc_3) {}
        try {
            loc_1[MethodHandleType.InvokeInterface.ordinal()] = 9;
        }
        catch (NoSuchFieldError loc_4) {}
        try {
            loc_1[MethodHandleType.InvokeSpecial.ordinal()] = 7;
        }
        catch (NoSuchFieldError loc_5) {}
        try {
            loc_1[MethodHandleType.InvokeStatic.ordinal()] = 6;
        }
        catch (NoSuchFieldError loc_6) {}
        try {
            loc_1[MethodHandleType.InvokeVirtual.ordinal()] = 5;
        }
        catch (NoSuchFieldError loc_7) {}
        try {
            loc_1[MethodHandleType.NewInvokeSpecial.ordinal()] = 8;
        }
        catch (NoSuchFieldError loc_8) {}
        try {
            loc_1[MethodHandleType.PutField.ordinal()] = 3;
        }
        catch (NoSuchFieldError loc_9) {}
        try {
            loc_1[MethodHandleType.PutStatic.ordinal()] = 4;
        }
        catch (NoSuchFieldError loc_10) {}
        return TypeAnalysis.$SWITCH_TABLE$com$strobel$assembler$metadata$MethodHandleType = loc_1;
    }
    
    private static final class AddMappingsForArgumentVisitor extends DefaultTypeVisitor<Map<TypeReference, TypeReference>, Void>
    {
        private TypeReference argumentType;
        
        AddMappingsForArgumentVisitor(final TypeReference argumentType) {
            super();
            this.argumentType = VerifyArgument.notNull(argumentType, "argumentType");
        }
        
        @Override
        public Void visit(final TypeReference t, final Map<TypeReference, TypeReference> map) {
            final TypeReference a = this.argumentType;
            t.accept((TypeMetadataVisitor<Map<TypeReference, TypeReference>, Object>)this, map);
            this.argumentType = a;
            return null;
        }
        
        @Override
        public Void visitArrayType(final ArrayType t, final Map<TypeReference, TypeReference> map) {
            final TypeReference a = this.argumentType;
            if (a.isArray() && t.isArray()) {
                this.argumentType = a.getElementType();
                this.visit(t.getElementType(), map);
            }
            return null;
        }
        
        @Override
        public Void visitGenericParameter(final GenericParameter t, final Map<TypeReference, TypeReference> map) {
            if (MetadataResolver.areEquivalent(this.argumentType, t)) {
                return null;
            }
            final TypeReference existingMapping = map.get(t);
            TypeReference mappedType = this.argumentType;
            mappedType = TypeAnalysis.access$1(mappedType);
            if (existingMapping == null) {
                if (!(mappedType instanceof RawType) && MetadataHelper.isRawType(mappedType)) {
                    final TypeReference bound = MetadataHelper.getUpperBound(t);
                    final TypeReference asSuper = MetadataHelper.asSuper(mappedType, bound);
                    if (asSuper != null) {
                        if (MetadataHelper.isSameType(MetadataHelper.getUpperBound(t), asSuper)) {
                            return null;
                        }
                        mappedType = asSuper;
                    }
                    else {
                        mappedType = MetadataHelper.erase(mappedType);
                    }
                }
                map.put(t, mappedType);
            }
            else if (!MetadataHelper.isSubType(this.argumentType, existingMapping)) {
                TypeReference commonSuperType = MetadataHelper.asSuper(mappedType, existingMapping);
                if (commonSuperType == null) {
                    commonSuperType = MetadataHelper.asSuper(existingMapping, mappedType);
                }
                if (commonSuperType == null) {
                    commonSuperType = MetadataHelper.findCommonSuperType(existingMapping, mappedType);
                }
                map.put(t, commonSuperType);
            }
            return null;
        }
        
        @Override
        public Void visitWildcard(final WildcardType t, final Map<TypeReference, TypeReference> map) {
            return null;
        }
        
        @Override
        public Void visitCompoundType(final CompoundTypeReference t, final Map<TypeReference, TypeReference> map) {
            return null;
        }
        
        @Override
        public Void visitParameterizedType(final TypeReference t, final Map<TypeReference, TypeReference> map) {
            final TypeReference r = MetadataHelper.asSuper(t.getUnderlyingType(), this.argumentType);
            final TypeReference s = MetadataHelper.asSubType(this.argumentType, (r != null) ? r : t.getUnderlyingType());
            if (s != null && s instanceof IGenericInstance) {
                final List<TypeReference> tArgs = ((IGenericInstance)t).getTypeArguments();
                final List<TypeReference> sArgs = ((IGenericInstance)s).getTypeArguments();
                if (tArgs.size() == sArgs.size()) {
                    for (int i = 0, n = tArgs.size(); i < n; ++i) {
                        this.argumentType = sArgs.get(i);
                        this.visit(tArgs.get(i), map);
                    }
                }
            }
            return null;
        }
        
        @Override
        public Void visitPrimitiveType(final PrimitiveType t, final Map<TypeReference, TypeReference> map) {
            return null;
        }
        
        @Override
        public Void visitClassType(final TypeReference t, final Map<TypeReference, TypeReference> map) {
            return null;
        }
        
        @Override
        public Void visitNullType(final TypeReference t, final Map<TypeReference, TypeReference> map) {
            return null;
        }
        
        @Override
        public Void visitBottomType(final TypeReference t, final Map<TypeReference, TypeReference> map) {
            return null;
        }
        
        @Override
        public Void visitRawType(final RawType t, final Map<TypeReference, TypeReference> map) {
            return null;
        }
    }
    
    static final class ExpressionToInfer
    {
        private final List<Variable> dependencies;
        Expression expression;
        boolean done;
        Variable dependsOnSingleLoad;
        int flags;
        
        ExpressionToInfer() {
            super();
            this.dependencies = new ArrayList<Variable>();
        }
        
        @Override
        public String toString() {
            if (this.done) {
                return "[Done] " + this.expression;
            }
            return this.expression.toString();
        }
        
        static /* synthetic */ List access$0(final ExpressionToInfer param_0) {
            return param_0.dependencies;
        }
    }
}
