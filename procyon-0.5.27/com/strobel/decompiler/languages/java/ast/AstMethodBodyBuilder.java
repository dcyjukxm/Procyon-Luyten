package com.strobel.decompiler.languages.java.ast;

import com.strobel.decompiler.languages.*;
import com.strobel.annotations.*;
import java.util.*;
import com.strobel.core.*;
import com.strobel.assembler.metadata.annotations.*;
import javax.lang.model.element.*;
import com.strobel.util.*;
import com.strobel.decompiler.patterns.*;
import com.strobel.decompiler.ast.*;
import com.strobel.decompiler.semantics.*;
import com.strobel.decompiler.*;
import com.strobel.assembler.metadata.*;

public class AstMethodBodyBuilder
{
    private final AstBuilder _astBuilder;
    private final MethodDefinition _method;
    private final MetadataParser _parser;
    private final DecompilerContext _context;
    private final Set<Variable> _localVariablesToDefine;
    private static final INode LAMBDA_BODY_PATTERN;
    private static final INode EMPTY_LAMBDA_BODY_PATTERN;
    private static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$assembler$metadata$JvmType;
    private static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$assembler$metadata$MethodHandleType;
    private static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$decompiler$ast$AstCode;
    
    static {
        LAMBDA_BODY_PATTERN = new Choice(new INode[] { new BlockStatement(new Statement[] { new ExpressionStatement(new AnyNode("body").toExpression()), new OptionalNode(new ReturnStatement(-34)).toStatement() }), new BlockStatement(new Statement[] { new ReturnStatement(-34, new AnyNode("body").toExpression()) }), new AnyNode("body").toBlockStatement() });
        EMPTY_LAMBDA_BODY_PATTERN = new BlockStatement(new Statement[] { new ReturnStatement(-34) });
    }
    
    public static BlockStatement createMethodBody(final AstBuilder astBuilder, final MethodDefinition method, final DecompilerContext context, final Iterable<ParameterDeclaration> parameters) {
        VerifyArgument.notNull(astBuilder, "astBuilder");
        VerifyArgument.notNull(method, "method");
        VerifyArgument.notNull(context, "context");
        final MethodDefinition oldCurrentMethod = context.getCurrentMethod();
        context.setCurrentMethod(method);
        try {
            final AstMethodBodyBuilder builder = new AstMethodBodyBuilder(astBuilder, method, context);
            return builder.createMethodBody(parameters);
        }
        catch (Throwable t) {
            return createErrorBlock(astBuilder, context, method, t);
        }
        finally {
            context.setCurrentMethod(oldCurrentMethod);
        }
    }
    
    private static BlockStatement createErrorBlock(final AstBuilder astBuilder, final DecompilerContext context, final MethodDefinition method, final Throwable t) {
        final BlockStatement block = new BlockStatement();
        final List<String> lines = StringUtilities.split(ExceptionUtilities.getStackTraceString(t), true, '\r', '\n');
        block.addChild(new Comment(" ", CommentType.SingleLine), Roles.COMMENT);
        block.addChild(new Comment(" This method could not be decompiled.", CommentType.SingleLine), Roles.COMMENT);
        block.addChild(new Comment(" ", CommentType.SingleLine), Roles.COMMENT);
        try {
            final PlainTextOutput bytecodeOutput = new PlainTextOutput();
            final DecompilationOptions bytecodeOptions = new DecompilationOptions();
            bytecodeOptions.getSettings().setIncludeLineNumbersInBytecode(false);
            Languages.bytecode().decompileMethod(method, bytecodeOutput, bytecodeOptions);
            final List<String> bytecodeLines = StringUtilities.split(bytecodeOutput.toString(), true, '\r', '\n');
            block.addChild(new Comment(" Original Bytecode:", CommentType.SingleLine), Roles.COMMENT);
            block.addChild(new Comment(" ", CommentType.SingleLine), Roles.COMMENT);
            for (int i = 4; i < bytecodeLines.size(); ++i) {
                final String line = StringUtilities.removeLeft(bytecodeLines.get(i), "      ");
                block.addChild(new Comment(line.replace("\t", "  "), CommentType.SingleLine), Roles.COMMENT);
            }
            block.addChild(new Comment(" ", CommentType.SingleLine), Roles.COMMENT);
        }
        catch (Throwable ignored) {
            block.addChild(new Comment(" Could not show original bytecode.", CommentType.SingleLine), Roles.COMMENT);
            block.addChild(new Comment(" ", CommentType.SingleLine), Roles.COMMENT);
        }
        if (context.getSettings().getIncludeErrorDiagnostics()) {
            block.addChild(new Comment(" The error that occurred was:", CommentType.SingleLine), Roles.COMMENT);
            block.addChild(new Comment(" ", CommentType.SingleLine), Roles.COMMENT);
            for (final String line2 : lines) {
                block.addChild(new Comment(" " + line2.replace("\t", "    "), CommentType.SingleLine), Roles.COMMENT);
            }
            block.addChild(new Comment(" ", CommentType.SingleLine), Roles.COMMENT);
        }
        try {
            final TypeDefinition currentType = astBuilder.getContext().getCurrentType();
            final IMetadataResolver resolver = (currentType != null) ? currentType.getResolver() : MetadataSystem.instance();
            final MetadataParser parser = new MetadataParser(resolver);
            block.add(new ThrowStatement(new ObjectCreationExpression(-34, astBuilder.convertType(parser.parseTypeDescriptor("java/lang/IllegalStateException")), new Expression[] { new PrimitiveExpression(-34, "An error occurred while decompiling this method.") })));
        }
        catch (Throwable ignored) {
            block.add(new EmptyStatement());
        }
        return block;
    }
    
    private AstMethodBodyBuilder(final AstBuilder astBuilder, final MethodDefinition method, final DecompilerContext context) {
        super();
        this._localVariablesToDefine = new LinkedHashSet<Variable>();
        this._astBuilder = astBuilder;
        this._method = method;
        this._context = context;
        this._parser = new MetadataParser(method.getDeclaringType());
    }
    
    private BlockStatement createMethodBody(final Iterable<ParameterDeclaration> parameters) {
        final MethodBody body = this._method.getBody();
        if (body == null) {
            return null;
        }
        final Block method = new Block();
        method.getBody().addAll(com.strobel.decompiler.ast.AstBuilder.build(body, true, this._context));
        AstOptimizer.optimize(this._context, method);
        final Set<ParameterDefinition> unmatchedParameters = new LinkedHashSet<ParameterDefinition>(this._method.getParameters());
        final Set<Variable> methodParameters = new LinkedHashSet<Variable>();
        final Set<Variable> localVariables = new LinkedHashSet<Variable>();
        final List<com.strobel.decompiler.ast.Expression> expressions = method.getSelfAndChildrenRecursive(com.strobel.decompiler.ast.Expression.class);
        for (final com.strobel.decompiler.ast.Expression e : expressions) {
            final Object operand = e.getOperand();
            if (operand instanceof Variable) {
                final Variable variable = (Variable)operand;
                if (variable.isParameter()) {
                    methodParameters.add(variable);
                    unmatchedParameters.remove(variable.getOriginalParameter());
                }
                else {
                    localVariables.add(variable);
                }
            }
        }
        final List<Variable> orderedParameters = new ArrayList<Variable>();
        for (final ParameterDefinition p : unmatchedParameters) {
            final Variable v = new Variable();
            v.setName(p.getName());
            v.setOriginalParameter(p);
            v.setType(p.getParameterType());
            orderedParameters.add(v);
        }
        for (final Variable parameter : methodParameters) {
            orderedParameters.add(parameter);
        }
        Collections.sort(orderedParameters, new Comparator<Variable>() {
            @Override
            public int compare(@NotNull final Variable p1, @NotNull final Variable p2) {
                return Integer.compare(p1.getOriginalParameter().getSlot(), p2.getOriginalParameter().getSlot());
            }
        });
        final List<CatchBlock> catchBlocks = method.getSelfAndChildrenRecursive(CatchBlock.class);
        for (final CatchBlock catchBlock : catchBlocks) {
            final Variable exceptionVariable = catchBlock.getExceptionVariable();
            if (exceptionVariable != null) {
                localVariables.add(exceptionVariable);
            }
        }
        NameVariables.assignNamesToVariables(this._context, orderedParameters, localVariables, method);
        for (final Variable p2 : orderedParameters) {
            final ParameterDeclaration declaration = CollectionUtilities.firstOrDefault(parameters, new Predicate<ParameterDeclaration>() {
                @Override
                public boolean test(final ParameterDeclaration pd) {
                    return pd.getUserData(Keys.PARAMETER_DEFINITION) == p2.getOriginalParameter();
                }
            });
            if (declaration != null) {
                declaration.setName(p2.getName());
            }
        }
        final BlockStatement astBlock = this.transformBlock(method);
        CommentStatement.replaceAll(astBlock);
        final AstNodeCollection<Statement> statements = astBlock.getStatements();
        final Statement insertionPoint = CollectionUtilities.firstOrDefault(statements);
        for (final Variable v2 : this._localVariablesToDefine) {
            TypeReference variableType = v2.getType();
            final TypeDefinition resolvedType = variableType.resolve();
            if (resolvedType != null && resolvedType.isAnonymous()) {
                if (resolvedType.getExplicitInterfaces().isEmpty()) {
                    variableType = resolvedType.getBaseType();
                }
                else {
                    variableType = resolvedType.getExplicitInterfaces().get(0);
                }
            }
            final AstType type = this._astBuilder.convertType(variableType);
            final VariableDeclarationStatement declaration2 = new VariableDeclarationStatement(type, v2.getName(), -34);
            declaration2.putUserData(Keys.VARIABLE, v2);
            statements.insertBefore(insertionPoint, declaration2);
        }
        return astBlock;
    }
    
    private BlockStatement transformBlock(final Block block) {
        final BlockStatement astBlock = new BlockStatement();
        if (block != null) {
            final List<Node> children = block.getChildren();
            for (int i = 0; i < children.size(); ++i) {
                final Node node = children.get(i);
                final Statement statement = this.transformNode(node, (i < children.size() - 1) ? children.get(i + 1) : null);
                astBlock.getStatements().add(statement);
                if (statement instanceof SynchronizedStatement) {
                    ++i;
                }
            }
        }
        return astBlock;
    }
    
    private Statement transformNode(final Node node, final Node next) {
        if (node instanceof Label) {
            return new LabelStatement(-34, ((Label)node).getName());
        }
        if (node instanceof Block) {
            return this.transformBlock((Block)node);
        }
        if (node instanceof com.strobel.decompiler.ast.Expression) {
            final com.strobel.decompiler.ast.Expression expression = (com.strobel.decompiler.ast.Expression)node;
            if (expression.getCode() == AstCode.MonitorEnter && next instanceof TryCatchBlock) {
                final TryCatchBlock tryCatch = (TryCatchBlock)next;
                final Block finallyBlock = tryCatch.getFinallyBlock();
                if (finallyBlock != null && finallyBlock.getBody().size() == 1) {
                    final Node finallyNode = finallyBlock.getBody().get(0);
                    if (finallyNode instanceof com.strobel.decompiler.ast.Expression && ((com.strobel.decompiler.ast.Expression)finallyNode).getCode() == AstCode.MonitorExit) {
                        return this.transformSynchronized(expression, tryCatch);
                    }
                }
            }
            final List<Range> ranges = new ArrayList<Range>();
            final List<com.strobel.decompiler.ast.Expression> childExpressions = node.getSelfAndChildrenRecursive(com.strobel.decompiler.ast.Expression.class);
            for (final com.strobel.decompiler.ast.Expression e : childExpressions) {
                ranges.addAll(e.getRanges());
            }
            final List<Range> orderedAndJoinedRanges = Range.orderAndJoint(ranges);
            final AstNode codeExpression = this.transformExpression((com.strobel.decompiler.ast.Expression)node, true);
            if (codeExpression != null) {
                if (codeExpression instanceof Expression) {
                    return new ExpressionStatement((Expression)codeExpression);
                }
                return (Statement)codeExpression;
            }
        }
        if (node instanceof Loop) {
            final Loop loop = (Loop)node;
            final com.strobel.decompiler.ast.Expression loopCondition = loop.getCondition();
            Statement loopStatement;
            if (loopCondition != null) {
                if (loop.getLoopType() == LoopType.PostCondition) {
                    final DoWhileStatement doWhileStatement = new DoWhileStatement(loopCondition.getOffset());
                    doWhileStatement.setCondition((Expression)this.transformExpression(loopCondition, false));
                    loopStatement = doWhileStatement;
                }
                else {
                    final WhileStatement whileStatement = new WhileStatement(loopCondition.getOffset());
                    whileStatement.setCondition((Expression)this.transformExpression(loopCondition, false));
                    loopStatement = whileStatement;
                }
            }
            else {
                final WhileStatement whileStatement = (WhileStatement)(loopStatement = new WhileStatement(-34));
                whileStatement.setCondition(new PrimitiveExpression(-34, true));
            }
            loopStatement.setChildByRole(Roles.EMBEDDED_STATEMENT, this.transformBlock(loop.getBody()));
            return loopStatement;
        }
        if (node instanceof Condition) {
            final Condition condition = (Condition)node;
            final com.strobel.decompiler.ast.Expression testCondition = condition.getCondition();
            final Block trueBlock = condition.getTrueBlock();
            final Block falseBlock = condition.getFalseBlock();
            final boolean hasFalseBlock = falseBlock.getEntryGoto() != null || !falseBlock.getBody().isEmpty();
            return new IfElseStatement(testCondition.getOffset(), (Expression)this.transformExpression(testCondition, false), this.transformBlock(trueBlock), hasFalseBlock ? this.transformBlock(falseBlock) : null);
        }
        if (node instanceof Switch) {
            final Switch switchNode = (Switch)node;
            final com.strobel.decompiler.ast.Expression testCondition = switchNode.getCondition();
            if (TypeAnalysis.isBoolean(testCondition.getInferredType())) {
                testCondition.setExpectedType(BuiltinTypes.Integer);
            }
            final List<CaseBlock> caseBlocks = switchNode.getCaseBlocks();
            final SwitchStatement switchStatement = new SwitchStatement((Expression)this.transformExpression(testCondition, false));
            for (final CaseBlock caseBlock : caseBlocks) {
                final SwitchSection section = new SwitchSection();
                final AstNodeCollection<CaseLabel> caseLabels = section.getCaseLabels();
                if (caseBlock.getValues().isEmpty()) {
                    caseLabels.add(new CaseLabel());
                }
                else {
                    TypeReference referenceType;
                    if (testCondition.getExpectedType() != null) {
                        referenceType = testCondition.getExpectedType();
                    }
                    else {
                        referenceType = testCondition.getInferredType();
                    }
                    for (final Integer value : caseBlock.getValues()) {
                        final CaseLabel caseLabel = new CaseLabel();
                        caseLabel.setExpression(AstBuilder.makePrimitive(value, referenceType));
                        caseLabels.add(caseLabel);
                    }
                }
                section.getStatements().add(this.transformBlock(caseBlock));
                switchStatement.getSwitchSections().add(section);
            }
            return switchStatement;
        }
        if (node instanceof TryCatchBlock) {
            final TryCatchBlock tryCatchNode = (TryCatchBlock)node;
            final Block finallyBlock2 = tryCatchNode.getFinallyBlock();
            final List<CatchBlock> catchBlocks = tryCatchNode.getCatchBlocks();
            final TryCatchStatement tryCatch2 = new TryCatchStatement(-34);
            tryCatch2.setTryBlock(this.transformBlock(tryCatchNode.getTryBlock()));
            for (final CatchBlock catchBlock : catchBlocks) {
                final CatchClause catchClause = new CatchClause(this.transformBlock(catchBlock));
                for (final TypeReference caughtType : catchBlock.getCaughtTypes()) {
                    catchClause.getExceptionTypes().add(this._astBuilder.convertType(caughtType));
                }
                final Variable exceptionVariable = catchBlock.getExceptionVariable();
                if (exceptionVariable != null) {
                    catchClause.setVariableName(exceptionVariable.getName());
                    catchClause.putUserData(Keys.VARIABLE, exceptionVariable);
                }
                tryCatch2.getCatchClauses().add(catchClause);
            }
            if (finallyBlock2 != null && (!finallyBlock2.getBody().isEmpty() || catchBlocks.isEmpty())) {
                tryCatch2.setFinallyBlock(this.transformBlock(finallyBlock2));
            }
            return tryCatch2;
        }
        throw new IllegalArgumentException("Unknown node type: " + node);
    }
    
    private SynchronizedStatement transformSynchronized(final com.strobel.decompiler.ast.Expression expression, final TryCatchBlock tryCatch) {
        final SynchronizedStatement s = new SynchronizedStatement(expression.getOffset());
        s.setExpression((Expression)this.transformExpression(expression.getArguments().get(0), false));
        if (tryCatch.getCatchBlocks().isEmpty()) {
            s.setEmbeddedStatement(this.transformBlock(tryCatch.getTryBlock()));
        }
        else {
            tryCatch.setFinallyBlock(null);
            s.setEmbeddedStatement(new BlockStatement(new Statement[] { this.transformNode(tryCatch, null) }));
        }
        return s;
    }
    
    private AstNode transformExpression(final com.strobel.decompiler.ast.Expression e, final boolean isTopLevel) {
        return this.transformByteCode(e, isTopLevel);
    }
    
    private AstNode transformByteCode(final com.strobel.decompiler.ast.Expression byteCode, final boolean isTopLevel) {
        final Object operand = byteCode.getOperand();
        final Label label = (operand instanceof Label) ? ((Label)operand) : null;
        final AstType operandType = (operand instanceof TypeReference) ? this._astBuilder.convertType((TypeReference)operand) : AstType.NULL;
        final Variable variableOperand = (operand instanceof Variable) ? ((Variable)operand) : null;
        final FieldReference fieldOperand = (operand instanceof FieldReference) ? ((FieldReference)operand) : null;
        final List<Expression> arguments = new ArrayList<Expression>();
        for (final com.strobel.decompiler.ast.Expression e : byteCode.getArguments()) {
            arguments.add((Expression)this.transformExpression(e, false));
        }
        final Expression arg1 = (arguments.size() >= 1) ? arguments.get(0) : null;
        final Expression arg2 = (arguments.size() >= 2) ? arguments.get(1) : null;
        final Expression arg3 = (arguments.size() >= 3) ? arguments.get(2) : null;
        switch ($SWITCH_TABLE$com$strobel$decompiler$ast$AstCode()[byteCode.getCode().ordinal()]) {
            case 1: {
                return null;
            }
            case 2: {
                return new NullReferenceExpression(byteCode.getOffset());
            }
            case 19: {
                if (operand instanceof TypeReference) {
                    operandType.getChildrenByRole(Roles.TYPE_ARGUMENT).clear();
                    return new ClassOfExpression(byteCode.getOffset(), operandType);
                }
                final TypeReference type = (byteCode.getInferredType() != null) ? byteCode.getInferredType() : byteCode.getExpectedType();
                if (type == null) {
                    return new PrimitiveExpression(byteCode.getOffset(), operand);
                }
                switch ($SWITCH_TABLE$com$strobel$assembler$metadata$JvmType()[type.getSimpleType().ordinal()]) {
                    case 2:
                    case 4: {
                        return new PrimitiveExpression(byteCode.getOffset(), JavaPrimitiveCast.cast(JvmType.Integer, operand));
                    }
                    default: {
                        return new PrimitiveExpression(byteCode.getOffset(), JavaPrimitiveCast.cast(type.getSimpleType(), operand));
                    }
                }
                break;
            }
            case 88:
            case 89:
            case 90:
            case 91:
            case 92:
            case 93:
            case 94:
            case 95: {
                return arg1;
            }
            case 96: {
                return arg1;
            }
            case 134: {
                return new CastExpression(this._astBuilder.convertType(BuiltinTypes.Long), arg1);
            }
            case 135: {
                return new CastExpression(this._astBuilder.convertType(BuiltinTypes.Float), arg1);
            }
            case 136: {
                return new CastExpression(this._astBuilder.convertType(BuiltinTypes.Double), arg1);
            }
            case 137: {
                return new CastExpression(this._astBuilder.convertType(BuiltinTypes.Integer), arg1);
            }
            case 138: {
                return new CastExpression(this._astBuilder.convertType(BuiltinTypes.Float), arg1);
            }
            case 139: {
                return new CastExpression(this._astBuilder.convertType(BuiltinTypes.Double), arg1);
            }
            case 140: {
                return new CastExpression(this._astBuilder.convertType(BuiltinTypes.Integer), arg1);
            }
            case 141: {
                return new CastExpression(this._astBuilder.convertType(BuiltinTypes.Long), arg1);
            }
            case 142: {
                return new CastExpression(this._astBuilder.convertType(BuiltinTypes.Double), arg1);
            }
            case 143: {
                return new CastExpression(this._astBuilder.convertType(BuiltinTypes.Integer), arg1);
            }
            case 144: {
                return new CastExpression(this._astBuilder.convertType(BuiltinTypes.Long), arg1);
            }
            case 145: {
                return new CastExpression(this._astBuilder.convertType(BuiltinTypes.Float), arg1);
            }
            case 146: {
                return new CastExpression(this._astBuilder.convertType(BuiltinTypes.Byte), arg1);
            }
            case 147: {
                return new CastExpression(this._astBuilder.convertType(BuiltinTypes.Character), arg1);
            }
            case 148: {
                return new CastExpression(this._astBuilder.convertType(BuiltinTypes.Short), arg1);
            }
            case 168: {
                return new GotoStatement(byteCode.getOffset(), ((Label)operand).getName());
            }
            case 179: {
                final ConvertTypeOptions options = new ConvertTypeOptions();
                options.setIncludeTypeParameterDefinitions(false);
                final MemberReferenceExpression fieldReference = this._astBuilder.convertType(fieldOperand.getDeclaringType(), options).member(fieldOperand.getName());
                fieldReference.putUserData(Keys.MEMBER_REFERENCE, fieldOperand);
                return fieldReference;
            }
            case 180: {
                final ConvertTypeOptions options = new ConvertTypeOptions();
                options.setIncludeTypeParameterDefinitions(false);
                final FieldDefinition resolvedField = fieldOperand.resolve();
                Expression fieldReference2;
                if (resolvedField != null && resolvedField.isFinal() && StringUtilities.equals(resolvedField.getDeclaringType().getInternalName(), this._context.getCurrentType().getInternalName())) {
                    fieldReference2 = new IdentifierExpression(byteCode.getOffset(), fieldOperand.getName());
                }
                else {
                    fieldReference2 = this._astBuilder.convertType(fieldOperand.getDeclaringType(), options).member(fieldOperand.getName());
                }
                fieldReference2.putUserData(Keys.MEMBER_REFERENCE, fieldOperand);
                return new AssignmentExpression(fieldReference2, arg1);
            }
            case 181: {
                MemberReferenceExpression fieldReference3;
                if (arg1 instanceof ThisReferenceExpression && MetadataHelper.isSubType(this._context.getCurrentType(), fieldOperand.getDeclaringType()) && !StringUtilities.equals(fieldOperand.getDeclaringType().getInternalName(), this._context.getCurrentType().getInternalName())) {
                    fieldReference3 = new SuperReferenceExpression(arg1.getOffset()).member(fieldOperand.getName());
                }
                else {
                    fieldReference3 = arg1.member(fieldOperand.getName());
                }
                fieldReference3.putUserData(Keys.MEMBER_REFERENCE, fieldOperand);
                return fieldReference3;
            }
            case 182: {
                MemberReferenceExpression fieldReference3;
                if (arg1 instanceof ThisReferenceExpression && MetadataHelper.isSubType(this._context.getCurrentType(), fieldOperand.getDeclaringType()) && !StringUtilities.equals(fieldOperand.getDeclaringType().getInternalName(), this._context.getCurrentType().getInternalName())) {
                    fieldReference3 = new SuperReferenceExpression(arg1.getOffset()).member(fieldOperand.getName());
                }
                else {
                    fieldReference3 = arg1.member(fieldOperand.getName());
                }
                fieldReference3.putUserData(Keys.MEMBER_REFERENCE, fieldOperand);
                return new AssignmentExpression(fieldReference3, arg2);
            }
            case 183: {
                return this.transformCall(true, byteCode, arguments);
            }
            case 184:
            case 185: {
                return this.transformCall(false, byteCode, arguments);
            }
            case 186: {
                return this.transformCall(false, byteCode, arguments);
            }
            case 187: {
                final DynamicCallSite callSite = (DynamicCallSite)operand;
                final MethodReference bootstrapMethod = callSite.getBootstrapMethod();
                if ("java/lang/invoke/LambdaMetafactory".equals(bootstrapMethod.getDeclaringType().getInternalName()) && (StringUtilities.equals("metafactory", bootstrapMethod.getName(), StringComparison.OrdinalIgnoreCase) || StringUtilities.equals("altMetafactory", bootstrapMethod.getName(), StringComparison.OrdinalIgnoreCase)) && callSite.getBootstrapArguments().size() >= 3 && callSite.getBootstrapArguments().get(1) instanceof MethodHandle) {
                    final MethodHandle targetMethodHandle = callSite.getBootstrapArguments().get(1);
                    final MethodReference targetMethod = targetMethodHandle.getMethod();
                    final TypeReference declaringType = targetMethod.getDeclaringType();
                    final String methodName = targetMethod.isConstructor() ? "new" : targetMethod.getName();
                    boolean hasInstanceArgument = false;
                    switch ($SWITCH_TABLE$com$strobel$assembler$metadata$MethodHandleType()[targetMethodHandle.getHandleType().ordinal()]) {
                        case 1:
                        case 3:
                        case 5:
                        case 7:
                        case 9: {
                            hasInstanceArgument = (arg1 != null);
                            break;
                        }
                        default: {
                            hasInstanceArgument = false;
                            break;
                        }
                    }
                    final MethodGroupExpression methodGroup = new MethodGroupExpression(byteCode.getOffset(), hasInstanceArgument ? arg1 : new TypeReferenceExpression(byteCode.getOffset(), this._astBuilder.convertType(declaringType)), methodName);
                    methodGroup.getClosureArguments().addAll((Collection<?>)(hasInstanceArgument ? arguments.subList(1, arguments.size()) : arguments));
                    methodGroup.putUserData(Keys.DYNAMIC_CALL_SITE, callSite);
                    methodGroup.putUserData(Keys.MEMBER_REFERENCE, targetMethod);
                    if (byteCode.getInferredType() != null) {
                        methodGroup.putUserData(Keys.TYPE_REFERENCE, byteCode.getInferredType());
                    }
                    return methodGroup;
                }
                break;
            }
            case 252: {
                final Lambda lambda = (Lambda)byteCode.getOperand();
                final LambdaExpression lambdaExpression = new LambdaExpression(byteCode.getOffset());
                final AstNodeCollection<ParameterDeclaration> declarations = lambdaExpression.getParameters();
                for (final Variable v : lambda.getParameters()) {
                    final ParameterDefinition p = v.getOriginalParameter();
                    final ParameterDeclaration d = new ParameterDeclaration(v.getName(), null);
                    d.putUserData(Keys.PARAMETER_DEFINITION, p);
                    d.putUserData(Keys.VARIABLE, v);
                    for (final CustomAnnotation annotation : p.getAnnotations()) {
                        d.getAnnotations().add(this._astBuilder.createAnnotation(annotation));
                    }
                    declarations.add(d);
                    if (p.isFinal()) {
                        EntityDeclaration.addModifier(d, Modifier.FINAL);
                    }
                }
                final BlockStatement body = this.transformBlock(lambda.getBody());
                final Match m = AstMethodBodyBuilder.LAMBDA_BODY_PATTERN.match(body);
                if (m.success()) {
                    final AstNode bodyNode = CollectionUtilities.first(m.get("body"));
                    bodyNode.remove();
                    lambdaExpression.setBody(bodyNode);
                    if (AstMethodBodyBuilder.EMPTY_LAMBDA_BODY_PATTERN.matches(bodyNode)) {
                        bodyNode.getChildrenByRole(BlockStatement.STATEMENT_ROLE).clear();
                    }
                }
                else {
                    lambdaExpression.setBody(body);
                }
                lambdaExpression.putUserData(Keys.TYPE_REFERENCE, byteCode.getInferredType());
                final DynamicCallSite callSite2 = lambda.getCallSite();
                if (callSite2 != null) {
                    lambdaExpression.putUserData(Keys.DYNAMIC_CALL_SITE, callSite2);
                }
                return lambdaExpression;
            }
            case 191: {
                final MemberReferenceExpression length = arg1.member("length");
                final TypeReference arrayType = CollectionUtilities.single(byteCode.getArguments()).getInferredType();
                if (arrayType != null) {
                    length.putUserData(Keys.MEMBER_REFERENCE, this._parser.parseField(arrayType, "length", "I"));
                }
                return length;
            }
            case 192: {
                return new ThrowStatement(arg1);
            }
            case 193: {
                return new CastExpression(operandType, arg1);
            }
            case 194: {
                return new InstanceOfExpression(byteCode.getOffset(), arg1, operandType);
            }
            case 197: {
                final ArrayCreationExpression arrayCreation = new ArrayCreationExpression(byteCode.getOffset());
                int rank = 0;
                AstType elementType;
                for (elementType = operandType; elementType instanceof ComposedType; elementType = ((ComposedType)elementType).getBaseType()) {
                    rank += ((ComposedType)elementType).getArraySpecifiers().size();
                }
                arrayCreation.setType(elementType.clone());
                for (int i = 0; i < arguments.size(); ++i) {
                    arrayCreation.getDimensions().add(arguments.get(i));
                    --rank;
                }
                for (int i = 0; i < rank; ++i) {
                    arrayCreation.getAdditionalArraySpecifiers().add(new ArraySpecifier());
                }
                return arrayCreation;
            }
            case 202: {
                return null;
            }
            case 217: {
                if (!variableOperand.isParameter()) {
                    this._localVariablesToDefine.add(variableOperand);
                }
                if (variableOperand.isParameter() && variableOperand.getOriginalParameter().getPosition() < 0) {
                    final ThisReferenceExpression self = new ThisReferenceExpression(byteCode.getOffset());
                    self.putUserData(Keys.TYPE_REFERENCE, this._context.getCurrentType());
                    return self;
                }
                final IdentifierExpression name = new IdentifierExpression(byteCode.getOffset(), variableOperand.getName());
                name.putUserData(Keys.VARIABLE, variableOperand);
                return name;
            }
            case 218: {
                if (!variableOperand.isParameter()) {
                    this._localVariablesToDefine.add(variableOperand);
                }
                final IdentifierExpression name = new IdentifierExpression(byteCode.getOffset(), variableOperand.getName());
                name.putUserData(Keys.VARIABLE, variableOperand);
                return new AssignmentExpression(name, arg1);
            }
            case 219: {
                return new IndexerExpression(byteCode.getOffset(), arg1, arg2);
            }
            case 220: {
                return new AssignmentExpression(new IndexerExpression(byteCode.getOffset(), arg1, arg2), arg3);
            }
            case 221: {
                return new BinaryOperatorExpression(arg1, BinaryOperatorType.ADD, arg2);
            }
            case 222: {
                return new BinaryOperatorExpression(arg1, BinaryOperatorType.SUBTRACT, arg2);
            }
            case 223: {
                return new BinaryOperatorExpression(arg1, BinaryOperatorType.MULTIPLY, arg2);
            }
            case 224: {
                return new BinaryOperatorExpression(arg1, BinaryOperatorType.DIVIDE, arg2);
            }
            case 225: {
                return new BinaryOperatorExpression(arg1, BinaryOperatorType.MODULUS, arg2);
            }
            case 226: {
                return new UnaryOperatorExpression(UnaryOperatorType.MINUS, arg1);
            }
            case 227: {
                return new BinaryOperatorExpression(arg1, BinaryOperatorType.SHIFT_LEFT, arg2);
            }
            case 228: {
                return new BinaryOperatorExpression(arg1, BinaryOperatorType.SHIFT_RIGHT, arg2);
            }
            case 229: {
                return new BinaryOperatorExpression(arg1, BinaryOperatorType.UNSIGNED_SHIFT_RIGHT, arg2);
            }
            case 230: {
                return new BinaryOperatorExpression(arg1, BinaryOperatorType.BITWISE_AND, arg2);
            }
            case 231: {
                return new BinaryOperatorExpression(arg1, BinaryOperatorType.BITWISE_OR, arg2);
            }
            case 232: {
                return new UnaryOperatorExpression(UnaryOperatorType.NOT, arg1);
            }
            case 233: {
                return new BinaryOperatorExpression(arg1, BinaryOperatorType.EXCLUSIVE_OR, arg2);
            }
            case 234: {
                if (!variableOperand.isParameter()) {
                    this._localVariablesToDefine.add(variableOperand);
                }
                final IdentifierExpression name = new IdentifierExpression(byteCode.getOffset(), variableOperand.getName());
                name.getIdentifierToken().putUserData(Keys.VARIABLE, variableOperand);
                name.putUserData(Keys.VARIABLE, variableOperand);
                final PrimitiveExpression deltaExpression = (PrimitiveExpression)arg1;
                final int delta = (int)JavaPrimitiveCast.cast(JvmType.Integer, deltaExpression.getValue());
                switch (delta) {
                    case -1: {
                        return new UnaryOperatorExpression(UnaryOperatorType.DECREMENT, name);
                    }
                    case 1: {
                        return new UnaryOperatorExpression(UnaryOperatorType.INCREMENT, name);
                    }
                    default: {
                        return new AssignmentExpression(name, AssignmentOperatorType.ADD, arg1);
                    }
                }
                break;
            }
            case 235: {
                return new BinaryOperatorExpression(arg1, BinaryOperatorType.EQUALITY, arg2);
            }
            case 236: {
                return new BinaryOperatorExpression(arg1, BinaryOperatorType.INEQUALITY, arg2);
            }
            case 237: {
                return new BinaryOperatorExpression(arg1, BinaryOperatorType.LESS_THAN, arg2);
            }
            case 238: {
                return new BinaryOperatorExpression(arg1, BinaryOperatorType.GREATER_THAN_OR_EQUAL, arg2);
            }
            case 239: {
                return new BinaryOperatorExpression(arg1, BinaryOperatorType.GREATER_THAN, arg2);
            }
            case 240: {
                return new BinaryOperatorExpression(arg1, BinaryOperatorType.LESS_THAN_OR_EQUAL, arg2);
            }
            case 242: {
                return new ReturnStatement(byteCode.getOffset(), arg1);
            }
            case 243: {
                final ArrayCreationExpression arrayCreation = new ArrayCreationExpression(byteCode.getOffset());
                TypeReference elementType2;
                for (elementType2 = operandType.getUserData(Keys.TYPE_REFERENCE); elementType2.isArray(); elementType2 = elementType2.getElementType()) {
                    arrayCreation.getAdditionalArraySpecifiers().add(new ArraySpecifier());
                }
                arrayCreation.setType(this._astBuilder.convertType(elementType2));
                arrayCreation.getDimensions().add(arg1);
                return arrayCreation;
            }
            case 245: {
                return new UnaryOperatorExpression(UnaryOperatorType.NOT, arg1);
            }
            case 246: {
                return new BinaryOperatorExpression(arg1, BinaryOperatorType.LOGICAL_AND, arg2);
            }
            case 247: {
                return new BinaryOperatorExpression(arg1, BinaryOperatorType.LOGICAL_OR, arg2);
            }
            case 248: {
                return this.transformCall(false, byteCode, arguments);
            }
            case 249: {
                final ArrayCreationExpression arrayCreation = new ArrayCreationExpression(byteCode.getOffset());
                TypeReference elementType2;
                for (elementType2 = operandType.getUserData(Keys.TYPE_REFERENCE); elementType2.isArray(); elementType2 = elementType2.getElementType()) {
                    arrayCreation.getAdditionalArraySpecifiers().add(new ArraySpecifier());
                }
                arrayCreation.setType(this._astBuilder.convertType(elementType2));
                arrayCreation.setInitializer(new ArrayInitializerExpression(arguments));
                return arrayCreation;
            }
            case 251: {
                return null;
            }
            case 253: {
                return new ConditionalExpression(arg1, arg2, arg3);
            }
            case 254: {
                return (label != null) ? new GotoStatement(byteCode.getOffset(), label.getName()) : new BreakStatement(byteCode.getOffset());
            }
            case 255: {
                return (label != null) ? new ContinueStatement(byteCode.getOffset(), label.getName()) : new ContinueStatement(byteCode.getOffset());
            }
            case 256: {
                throw ContractUtils.unreachable();
            }
            case 257: {
                final Integer incrementAmount = (Integer)operand;
                if (incrementAmount < 0) {
                    return new UnaryOperatorExpression(UnaryOperatorType.DECREMENT, arg1);
                }
                return new UnaryOperatorExpression(UnaryOperatorType.INCREMENT, arg1);
            }
            case 258: {
                final Integer incrementAmount = (Integer)operand;
                if (incrementAmount < 0) {
                    return new UnaryOperatorExpression(UnaryOperatorType.POST_DECREMENT, arg1);
                }
                return new UnaryOperatorExpression(UnaryOperatorType.POST_INCREMENT, arg1);
            }
            case 259:
            case 260: {
                throw ContractUtils.unreachable();
            }
            case 215:
            case 216: {
                return null;
            }
            case 261: {
                return AstBuilder.makeDefaultValue((TypeReference)operand);
            }
        }
        final Expression inlinedAssembly = inlineAssembly(byteCode, arguments);
        if (isTopLevel) {
            return new CommentStatement(" " + inlinedAssembly.toString());
        }
        return inlinedAssembly;
    }
    
    private Expression transformCall(final boolean isVirtual, final com.strobel.decompiler.ast.Expression byteCode, final List<Expression> arguments) {
        final MethodReference methodReference = (MethodReference)byteCode.getOperand();
        final boolean hasThis = byteCode.getCode() == AstCode.InvokeVirtual || byteCode.getCode() == AstCode.InvokeInterface || byteCode.getCode() == AstCode.InvokeSpecial;
        final TypeReference declaringType = methodReference.getDeclaringType();
        Expression target;
        if (hasThis) {
            target = arguments.remove(0);
            if (target instanceof NullReferenceExpression) {
                target = new CastExpression(this._astBuilder.convertType(declaringType), target);
            }
        }
        else {
            final MethodDefinition resolvedMethod;
            if (byteCode.getCode() == AstCode.InvokeStatic && declaringType.isEquivalentTo(this._context.getCurrentType()) && (!this._context.getSettings().getForceExplicitTypeArguments() || (resolvedMethod = methodReference.resolve()) == null || !resolvedMethod.isGenericMethod())) {
                target = Expression.NULL;
            }
            else {
                final ConvertTypeOptions options = new ConvertTypeOptions();
                options.setIncludeTypeArguments(false);
                options.setIncludeTypeParameterDefinitions(false);
                options.setAllowWildcards(false);
                target = new TypeReferenceExpression(byteCode.getOffset(), this._astBuilder.convertType(declaringType, options));
            }
        }
        if (target instanceof ThisReferenceExpression) {
            if (!isVirtual && !declaringType.isEquivalentTo(this._method.getDeclaringType())) {
                target = new SuperReferenceExpression(byteCode.getOffset());
                target.putUserData(Keys.TYPE_REFERENCE, declaringType);
            }
        }
        else if (methodReference.isConstructor()) {
            final TypeDefinition resolvedType = declaringType.resolve();
            ObjectCreationExpression creation;
            if (resolvedType != null) {
                TypeReference instantiatedType;
                if (resolvedType.isAnonymous()) {
                    if (resolvedType.getExplicitInterfaces().isEmpty()) {
                        instantiatedType = resolvedType.getBaseType();
                    }
                    else {
                        instantiatedType = resolvedType.getExplicitInterfaces().get(0);
                    }
                }
                else {
                    instantiatedType = resolvedType;
                }
                final List<TypeReference> typeArguments = byteCode.getUserData(AstKeys.TYPE_ARGUMENTS);
                if (typeArguments != null && resolvedType.isGenericDefinition() && typeArguments.size() == resolvedType.getGenericParameters().size()) {
                    instantiatedType = instantiatedType.makeGenericType(typeArguments);
                }
                final AstType declaredType = this._astBuilder.convertType(instantiatedType);
                if (resolvedType.isAnonymous()) {
                    creation = new AnonymousObjectCreationExpression(byteCode.getOffset(), this._astBuilder.createType(resolvedType).clone(), declaredType);
                }
                else {
                    creation = new ObjectCreationExpression(byteCode.getOffset(), declaredType);
                }
            }
            else {
                final ConvertTypeOptions options2 = new ConvertTypeOptions();
                options2.setIncludeTypeParameterDefinitions(false);
                creation = new ObjectCreationExpression(byteCode.getOffset(), this._astBuilder.convertType(declaringType, options2));
            }
            creation.getArguments().addAll((Collection<?>)this.adjustArgumentsForMethodCall(methodReference, arguments));
            creation.putUserData(Keys.MEMBER_REFERENCE, methodReference);
            return creation;
        }
        InvocationExpression invocation;
        if (methodReference.isConstructor()) {
            invocation = new InvocationExpression(byteCode.getOffset(), target, this.adjustArgumentsForMethodCall(methodReference, arguments));
        }
        else {
            invocation = target.invoke(methodReference.getName(), this.convertTypeArguments(methodReference), this.adjustArgumentsForMethodCall(methodReference, arguments));
        }
        invocation.putUserData(Keys.MEMBER_REFERENCE, methodReference);
        return invocation;
    }
    
    private List<AstType> convertTypeArguments(final MethodReference methodReference) {
        if (this._context.getSettings().getForceExplicitTypeArguments() && methodReference instanceof IGenericInstance) {
            final List<TypeReference> typeArguments = ((IGenericInstance)methodReference).getTypeArguments();
            if (!typeArguments.isEmpty()) {
                final List<AstType> astTypeArguments = new ArrayList<AstType>();
                for (final TypeReference type : typeArguments) {
                    astTypeArguments.add(this._astBuilder.convertType(type));
                }
                return astTypeArguments;
            }
        }
        return Collections.emptyList();
    }
    
    private List<Expression> adjustArgumentsForMethodCall(final MethodReference method, final List<Expression> arguments) {
        if (!arguments.isEmpty() && method.isConstructor()) {
            final TypeReference declaringType = method.getDeclaringType();
            if (declaringType.isNested()) {
                final TypeDefinition resolvedType = declaringType.resolve();
                if (resolvedType != null) {
                    if (resolvedType.isLocalClass()) {
                        return arguments;
                    }
                    if (resolvedType.isInnerClass()) {
                        final MethodDefinition resolvedMethod = method.resolve();
                        if (resolvedMethod != null && resolvedMethod.isSynthetic() && (resolvedMethod.getFlags() & 0x7L) == 0x0L) {
                            final List<ParameterDefinition> parameters = resolvedMethod.getParameters();
                            int start = 0;
                            int end = arguments.size();
                            for (int i = parameters.size() - 1; i >= 0; --i) {
                                final TypeReference parameterType = parameters.get(i).getParameterType();
                                final TypeDefinition resolvedParameterType = parameterType.resolve();
                                if (resolvedParameterType == null || !resolvedParameterType.isAnonymous()) {
                                    break;
                                }
                                --end;
                            }
                            if (!resolvedType.isStatic() && !this._context.getSettings().getShowSyntheticMembers()) {
                                ++start;
                            }
                            if (start > end) {
                                return Collections.emptyList();
                            }
                            return this.adjustArgumentsForMethodCallCore(method.getParameters().subList(start, end), arguments.subList(start, end));
                        }
                    }
                }
            }
        }
        return this.adjustArgumentsForMethodCallCore(method.getParameters(), arguments);
    }
    
    private List<Expression> adjustArgumentsForMethodCallCore(final List<ParameterDefinition> parameters, final List<Expression> arguments) {
        final int parameterCount = parameters.size();
        assert parameterCount == arguments.size();
        final JavaResolver resolver = new JavaResolver(this._context);
        final ConvertTypeOptions options = new ConvertTypeOptions();
        options.setAllowWildcards(false);
        for (int i = 0, n = arguments.size(); i < n; ++i) {
            final Expression argument = arguments.get(i);
            final ResolveResult resolvedArgument = resolver.apply((AstNode)argument);
            if (resolvedArgument != null) {
                if (!(argument instanceof LambdaExpression)) {
                    final ParameterDefinition p = parameters.get(i);
                    final TypeReference aType = resolvedArgument.getType();
                    final TypeReference pType = p.getParameterType();
                    if (this.isCastRequired(pType, aType, true)) {
                        arguments.set(i, new CastExpression(this._astBuilder.convertType(pType, options), argument));
                    }
                }
            }
        }
        int first = 0;
        int last = parameterCount - 1;
        while (first < parameterCount) {
            if (!parameters.get(first).isSynthetic()) {
                break;
            }
            ++first;
        }
        while (last >= 0 && parameters.get(last).isSynthetic()) {
            --last;
        }
        if (first >= parameterCount || last < 0) {
            return Collections.emptyList();
        }
        if (first == 0 && last == parameterCount - 1) {
            return arguments;
        }
        return arguments.subList(first, last + 1);
    }
    
    private boolean isCastRequired(final TypeReference targetType, final TypeReference sourceType, final boolean exactMatch) {
        if (targetType == null || sourceType == null) {
            return false;
        }
        if (targetType.isPrimitive()) {
            return sourceType.getSimpleType() != targetType.getSimpleType();
        }
        if (exactMatch) {
            return !MetadataHelper.isSameType(targetType, sourceType, true);
        }
        return !MetadataHelper.isAssignableFrom(targetType, sourceType);
    }
    
    private static Expression inlineAssembly(final com.strobel.decompiler.ast.Expression byteCode, final List<Expression> arguments) {
        if (byteCode.getOperand() != null) {
            arguments.add(0, new IdentifierExpression(byteCode.getOffset(), formatByteCodeOperand(byteCode.getOperand())));
        }
        return new IdentifierExpression(byteCode.getOffset(), byteCode.getCode().getName()).invoke(arguments);
    }
    
    private static String formatByteCodeOperand(final Object operand) {
        if (operand == null) {
            return "";
        }
        final PlainTextOutput output = new PlainTextOutput();
        DecompilerHelpers.writeOperand(output, operand);
        return output.toString();
    }
    
    static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$assembler$metadata$JvmType() {
        final int[] loc_0 = AstMethodBodyBuilder.$SWITCH_TABLE$com$strobel$assembler$metadata$JvmType;
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
        return AstMethodBodyBuilder.$SWITCH_TABLE$com$strobel$assembler$metadata$JvmType = loc_1;
    }
    
    static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$assembler$metadata$MethodHandleType() {
        final int[] loc_0 = AstMethodBodyBuilder.$SWITCH_TABLE$com$strobel$assembler$metadata$MethodHandleType;
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
        return AstMethodBodyBuilder.$SWITCH_TABLE$com$strobel$assembler$metadata$MethodHandleType = loc_1;
    }
    
    static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$decompiler$ast$AstCode() {
        final int[] loc_0 = AstMethodBodyBuilder.$SWITCH_TABLE$com$strobel$decompiler$ast$AstCode;
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
        return AstMethodBodyBuilder.$SWITCH_TABLE$com$strobel$decompiler$ast$AstCode = loc_1;
    }
}
