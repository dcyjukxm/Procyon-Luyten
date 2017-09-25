package com.strobel.decompiler.languages.java.ast.transforms;

import com.strobel.decompiler.*;
import com.strobel.functions.*;
import com.strobel.decompiler.semantics.*;
import com.strobel.decompiler.languages.java.analysis.*;
import java.util.*;
import com.strobel.decompiler.ast.*;
import javax.lang.model.element.*;
import com.strobel.decompiler.patterns.*;
import com.strobel.decompiler.languages.java.ast.*;
import com.strobel.assembler.metadata.*;
import com.strobel.core.*;

public final class ConvertLoopsTransform extends ContextTrackingVisitor<AstNode>
{
    private static final ExpressionStatement ARRAY_INIT_PATTERN;
    private static final ForStatement FOR_ARRAY_PATTERN_1;
    private static final ForStatement FOR_ARRAY_PATTERN_2;
    private static final ForStatement FOR_ARRAY_PATTERN_3;
    private static final ExpressionStatement GET_ITERATOR_PATTERN;
    private static final WhileStatement FOR_EACH_PATTERN;
    private static final WhileStatement DO_WHILE_PATTERN;
    private static final WhileStatement CONTINUE_OUTER_PATTERN;
    private static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$UnaryOperatorType;
    
    static {
        ARRAY_INIT_PATTERN = new ExpressionStatement(new AssignmentExpression(new NamedNode("array", new IdentifierExpression(-34, "$any$")).toExpression(), new AnyNode("initializer").toExpression()));
        final ForStatement forArrayPattern1 = new ForStatement(-34);
        final VariableDeclarationStatement declaration1 = new VariableDeclarationStatement();
        final SimpleType variableType1 = new SimpleType("int");
        variableType1.putUserData(Keys.TYPE_REFERENCE, BuiltinTypes.Integer);
        declaration1.setType(variableType1);
        declaration1.getVariables().add(new VariableInitializer("$any$", new NamedNode("array", new IdentifierExpression(-34, "$any$")).toExpression().member("length")));
        declaration1.getVariables().add(new VariableInitializer("$any$", new PrimitiveExpression(-34, 0)));
        forArrayPattern1.getInitializers().add(new NamedNode("declaration", declaration1).toStatement());
        forArrayPattern1.setCondition(new BinaryOperatorExpression(new NamedNode("index", new IdentifierExpression(-34, "$any$")).toExpression(), BinaryOperatorType.LESS_THAN, new NamedNode("length", new IdentifierExpression(-34, "$any$")).toExpression()));
        forArrayPattern1.getIterators().add(new ExpressionStatement(new UnaryOperatorExpression(UnaryOperatorType.INCREMENT, new BackReference("index").toExpression())));
        final BlockStatement embeddedStatement1 = new BlockStatement();
        embeddedStatement1.add(new ExpressionStatement(new AssignmentChain(new NamedNode("item", new IdentifierExpression(-34, "$any$")).toExpression(), new IndexerExpression(-34, new BackReference("array").toExpression(), new BackReference("index").toExpression())).toExpression()));
        embeddedStatement1.add(new Repeat(new AnyNode("statement")).toStatement());
        forArrayPattern1.setEmbeddedStatement(embeddedStatement1);
        FOR_ARRAY_PATTERN_1 = forArrayPattern1;
        final ForStatement forArrayPattern2 = new ForStatement(-34);
        final VariableDeclarationStatement declaration2 = new VariableDeclarationStatement();
        final SimpleType variableType2 = new SimpleType("int");
        variableType2.putUserData(Keys.TYPE_REFERENCE, BuiltinTypes.Integer);
        declaration2.setType(variableType2);
        declaration2.getVariables().add(new VariableInitializer("$any$", new PrimitiveExpression(-34, 0)));
        forArrayPattern2.getInitializers().add(new NamedNode("declaration", declaration2).toStatement());
        forArrayPattern2.setCondition(new BinaryOperatorExpression(new NamedNode("index", new IdentifierExpression(-34, "$any$")).toExpression(), BinaryOperatorType.LESS_THAN, new NamedNode("length", new IdentifierExpression(-34, "$any$")).toExpression()));
        forArrayPattern2.getIterators().add(new ExpressionStatement(new UnaryOperatorExpression(UnaryOperatorType.INCREMENT, new BackReference("index").toExpression())));
        final BlockStatement embeddedStatement2 = new BlockStatement();
        embeddedStatement2.add(new ExpressionStatement(new AssignmentChain(new NamedNode("item", new IdentifierExpression(-34, "$any$")).toExpression(), new IndexerExpression(-34, new NamedNode("array", new IdentifierExpression(-34, "$any$")).toExpression(), new BackReference("index").toExpression())).toExpression()));
        embeddedStatement2.add(new Repeat(new AnyNode("statement")).toStatement());
        forArrayPattern2.setEmbeddedStatement(embeddedStatement2);
        FOR_ARRAY_PATTERN_2 = forArrayPattern2;
        final ForStatement altForArrayPattern = new ForStatement(-34);
        altForArrayPattern.getInitializers().add(new ExpressionStatement(new AssignmentExpression(new NamedNode("length", new IdentifierExpression(-34, "$any$")).toExpression(), AssignmentOperatorType.ASSIGN, new NamedNode("array", new IdentifierExpression(-34, "$any$")).toExpression().member("length"))));
        altForArrayPattern.getInitializers().add(new ExpressionStatement(new AssignmentExpression(new NamedNode("index", new IdentifierExpression(-34, "$any$")).toExpression(), AssignmentOperatorType.ASSIGN, new PrimitiveExpression(-34, 0))));
        altForArrayPattern.setCondition(new BinaryOperatorExpression(new BackReference("index").toExpression(), BinaryOperatorType.LESS_THAN, new BackReference("length").toExpression()));
        altForArrayPattern.getIterators().add(new ExpressionStatement(new UnaryOperatorExpression(UnaryOperatorType.INCREMENT, new BackReference("index").toExpression())));
        final BlockStatement altEmbeddedStatement = new BlockStatement();
        altEmbeddedStatement.add(new ExpressionStatement(new AssignmentChain(new NamedNode("item", new IdentifierExpression(-34, "$any$")).toExpression(), new IndexerExpression(-34, new BackReference("array").toExpression(), new BackReference("index").toExpression())).toExpression()));
        altEmbeddedStatement.add(new Repeat(new AnyNode("statement")).toStatement());
        altForArrayPattern.setEmbeddedStatement(altEmbeddedStatement);
        FOR_ARRAY_PATTERN_3 = altForArrayPattern;
        GET_ITERATOR_PATTERN = new ExpressionStatement(new AssignmentExpression(new NamedNode("left", new AnyNode()).toExpression(), new AnyNode("collection").toExpression().invoke("iterator", new Expression[0])));
        final WhileStatement forEachPattern = new WhileStatement(-34);
        forEachPattern.setCondition(new InvocationExpression(-34, new MemberReferenceExpression(-34, new NamedNode("iterator", new IdentifierExpression(-34, "$any$")).toExpression(), "hasNext", new AstType[0]), new Expression[0]));
        final BlockStatement embeddedStatement3 = new BlockStatement();
        embeddedStatement3.add(new NamedNode("next", new ExpressionStatement(new AssignmentChain(new NamedNode("item", new IdentifierExpression(-34, "$any$")), new Choice(new INode[] { new InvocationExpression(-34, new MemberReferenceExpression(-34, new BackReference("iterator").toExpression(), "next", new AstType[0]), new Expression[0]), new CastExpression(new AnyNode("castType").toType(), new InvocationExpression(-34, new MemberReferenceExpression(-34, new BackReference("iterator").toExpression(), "next", new AstType[0]), new Expression[0])) })).toExpression())).toStatement());
        embeddedStatement3.add(new Repeat(new AnyNode("statement")).toStatement());
        forEachPattern.setEmbeddedStatement(embeddedStatement3);
        FOR_EACH_PATTERN = forEachPattern;
        final WhileStatement doWhile = new WhileStatement(-34);
        doWhile.setCondition(new PrimitiveExpression(-34, true));
        doWhile.setEmbeddedStatement(new Choice(new INode[] { new BlockStatement(new Statement[] { new Repeat(new AnyNode("statement")).toStatement(), new IfElseStatement(-34, new AnyNode("breakCondition").toExpression(), new BlockStatement(new Statement[] { new BreakStatement(-34) })) }), new BlockStatement(new Statement[] { new Repeat(new AnyNode("statement")).toStatement(), new IfElseStatement(-34, new AnyNode("continueCondition").toExpression(), new BlockStatement(new Statement[] { new NamedNode("continueStatement", new ContinueStatement(-34)).toStatement() })), new NamedNode("breakStatement", new BreakStatement(-34)).toStatement() }) }).toBlockStatement());
        DO_WHILE_PATTERN = doWhile;
        final WhileStatement continueOuter = new WhileStatement(-34);
        continueOuter.setCondition(new AnyNode().toExpression());
        continueOuter.setEmbeddedStatement(new BlockStatement(new Statement[] { new NamedNode("label", new LabelStatement(-34, "$any$")).toStatement(), new Repeat(new AnyNode("statement")).toStatement() }));
        CONTINUE_OUTER_PATTERN = continueOuter;
    }
    
    public ConvertLoopsTransform(final DecompilerContext context) {
        super(context);
    }
    
    @Override
    protected AstNode visitChildren(final AstNode node, final Void data) {
        AstNode next;
        for (AstNode child = node.getFirstChild(); child != null; child = next) {
            next = child.getNextSibling();
            final AstNode childResult = child.acceptVisitor((IAstVisitor<? super Void, ? extends AstNode>)this, data);
            if (childResult != null && childResult != child) {
                next = childResult;
            }
        }
        return node;
    }
    
    @Override
    public AstNode visitExpressionStatement(final ExpressionStatement node, final Void data) {
        final AstNode n = super.visitExpressionStatement(node, data);
        if (!this.context.getSettings().getDisableForEachTransforms() && n instanceof ExpressionStatement) {
            final AstNode result = this.transformForEach((ExpressionStatement)n);
            if (result != null) {
                return result.acceptVisitor((IAstVisitor<? super Void, ? extends AstNode>)this, data);
            }
        }
        return n;
    }
    
    @Override
    public AstNode visitWhileStatement(final WhileStatement node, final Void data) {
        final ForStatement forLoop = this.transformFor(node);
        if (forLoop != null) {
            if (!this.context.getSettings().getDisableForEachTransforms()) {
                final AstNode forEachInArray = this.transformForEachInArray(forLoop);
                if (forEachInArray != null) {
                    return forEachInArray.acceptVisitor((IAstVisitor<? super Void, ? extends AstNode>)this, data);
                }
            }
            return forLoop.acceptVisitor((IAstVisitor<? super Void, ? extends AstNode>)this, data);
        }
        final DoWhileStatement doWhile = this.transformDoWhile(node);
        if (doWhile != null) {
            return doWhile.acceptVisitor((IAstVisitor<? super Void, ? extends AstNode>)this, data);
        }
        return this.visitChildren(this.transformContinueOuter(node), data);
    }
    
    public final ForStatement transformFor(final WhileStatement node) {
        final Expression condition = node.getCondition();
        if (condition == null || condition.isNull() || condition instanceof PrimitiveExpression) {
            return null;
        }
        if (!(node.getEmbeddedStatement() instanceof BlockStatement)) {
            return null;
        }
        final BlockStatement body = (BlockStatement)node.getEmbeddedStatement();
        final ControlFlowGraphBuilder graphBuilder = new ControlFlowGraphBuilder();
        final List<ControlFlowNode> nodes = graphBuilder.buildControlFlowGraph(node, new JavaResolver(this.context));
        if (nodes.size() < 2) {
            return null;
        }
        final ControlFlowNode conditionNode = CollectionUtilities.firstOrDefault(nodes, new Predicate<ControlFlowNode>() {
            @Override
            public boolean test(final ControlFlowNode n) {
                return n.getType() == ControlFlowNodeType.LoopCondition;
            }
        });
        if (conditionNode == null) {
            return null;
        }
        final List<ControlFlowNode> bodyNodes = new ArrayList<ControlFlowNode>();
        for (final ControlFlowEdge edge : conditionNode.getIncoming()) {
            final ControlFlowNode from = edge.getFrom();
            final Statement statement = from.getPreviousStatement();
            if (statement != null && body.isAncestorOf(statement)) {
                bodyNodes.add(from);
            }
        }
        if (bodyNodes.size() != 1) {
            return null;
        }
        final Set<Statement> incoming = new LinkedHashSet<Statement>();
        final Set<ControlFlowEdge> visited = new HashSet<ControlFlowEdge>();
        final ArrayDeque<ControlFlowEdge> agenda = new ArrayDeque<ControlFlowEdge>();
        agenda.addAll((Collection<?>)conditionNode.getIncoming());
        visited.addAll(conditionNode.getIncoming());
        while (!agenda.isEmpty()) {
            final ControlFlowEdge edge2 = agenda.removeFirst();
            final ControlFlowNode from2 = edge2.getFrom();
            if (from2 == null) {
                continue;
            }
            if (edge2.getType() == ControlFlowEdgeType.Jump) {
                final Statement jump = from2.getNextStatement();
                if (jump.getPreviousStatement() != null) {
                    incoming.add(jump.getPreviousStatement());
                }
                else {
                    incoming.add(jump);
                }
            }
            else {
                final Statement previousStatement = from2.getPreviousStatement();
                if (previousStatement == null) {
                    continue;
                }
                if (from2.getType() != ControlFlowNodeType.EndNode) {
                    continue;
                }
                if (previousStatement instanceof TryCatchStatement) {
                    incoming.add(previousStatement);
                }
                else if (previousStatement instanceof BlockStatement || hasNestedBlocks(previousStatement)) {
                    for (final ControlFlowEdge e : from2.getIncoming()) {
                        if (visited.add(e)) {
                            agenda.addLast(e);
                        }
                    }
                }
                else {
                    incoming.add(previousStatement);
                }
            }
        }
        if (incoming.isEmpty()) {
            return null;
        }
        final Statement[] iteratorSites = incoming.toArray(new Statement[incoming.size()]);
        final List<Statement> iterators = new ArrayList<Statement>();
        final Set<Statement> iteratorCopies = new HashSet<Statement>();
        final Map<Statement, List<Statement>> iteratorCopyMap = new DefaultMap<Statement, List<Statement>>(CollectionUtilities.listFactory());
    Label_0702:
        while (true) {
            final Statement s = iteratorSites[0];
            if (s == null || s.isNull() || !s.isEmbeddable() || !isSimpleIterator(s)) {
                break;
            }
            for (int i = 1; i < iteratorSites.length; ++i) {
                final Statement o = iteratorSites[i];
                if (o == null) {
                    break Label_0702;
                }
                if (!s.matches(o)) {
                    break Label_0702;
                }
            }
            iterators.add(s);
            for (int i = 0; i < iteratorSites.length; ++i) {
                iteratorCopies.add(iteratorSites[i]);
                iteratorCopyMap.get(s).add(iteratorSites[i]);
                iteratorSites[i] = iteratorSites[i].getPreviousStatement();
            }
        }
        Collections.reverse(iterators);
        while (!iterators.isEmpty()) {
            final Statement iterator = CollectionUtilities.first(iterators);
            if (Correlator.areCorrelated(condition, iterator)) {
                break;
            }
            for (final Statement copy : iteratorCopyMap.get(iterator)) {
                iteratorCopies.remove(copy);
            }
            iterators.remove(0);
        }
        if (iterators.isEmpty()) {
            return null;
        }
        final ForStatement forLoop = new ForStatement(node.getOffset());
        final Stack<Statement> initializers = new Stack<Statement>();
        for (Statement s2 = node.getPreviousStatement(); s2 instanceof ExpressionStatement; s2 = s2.getPreviousStatement()) {
            final Statement fs = s2;
            final Expression e2 = ((ExpressionStatement)s2).getExpression();
            final Expression left;
            final boolean canExtract = e2 instanceof AssignmentExpression && (left = e2.getChildByRole(AssignmentExpression.LEFT_ROLE)) instanceof IdentifierExpression && (Correlator.areCorrelated(condition, s2) || CollectionUtilities.any(iterators, new Predicate<Statement>() {
                @Override
                public boolean test(final Statement i) {
                    return (i instanceof ExpressionStatement && Correlator.areCorrelated(((ExpressionStatement)i).getExpression(), fs)) || Correlator.areCorrelated(left, i);
                }
            }));
            if (!canExtract) {
                break;
            }
            initializers.add(s2);
        }
        if (initializers.isEmpty()) {
            return null;
        }
        condition.remove();
        body.remove();
        forLoop.setCondition(condition);
        if (body instanceof BlockStatement) {
            for (final Statement copy2 : iteratorCopies) {
                copy2.remove();
            }
            forLoop.setEmbeddedStatement(body);
        }
        forLoop.getIterators().addAll((Collection<?>)iterators);
        while (!initializers.isEmpty()) {
            final Statement initializer = initializers.pop();
            initializer.remove();
            forLoop.getInitializers().add(initializer);
        }
        node.replaceWith(forLoop);
        final Statement firstInlinableInitializer = this.canInlineInitializerDeclarations(forLoop);
        if (firstInlinableInitializer != null) {
            final BlockStatement parent = (BlockStatement)forLoop.getParent();
            final VariableDeclarationStatement newDeclaration = new VariableDeclarationStatement();
            final List<Statement> forInitializers = new ArrayList<Statement>(forLoop.getInitializers());
            final int firstInlinableInitializerIndex = forInitializers.indexOf(firstInlinableInitializer);
            forLoop.getInitializers().clear();
            forLoop.getInitializers().add(newDeclaration);
            for (int j = 0; j < forInitializers.size(); ++j) {
                final Statement initializer2 = forInitializers.get(j);
                if (j < firstInlinableInitializerIndex) {
                    parent.insertChildBefore(forLoop, initializer2, BlockStatement.STATEMENT_ROLE);
                }
                else {
                    final AssignmentExpression assignment = (AssignmentExpression)((ExpressionStatement)initializer2).getExpression();
                    final IdentifierExpression variable = (IdentifierExpression)assignment.getLeft();
                    final String variableName = variable.getIdentifier();
                    final VariableDeclarationStatement declaration = findVariableDeclaration(forLoop, variableName);
                    final Expression initValue = assignment.getRight();
                    initValue.remove();
                    newDeclaration.getVariables().add(new VariableInitializer(variableName, initValue));
                    final AstType newDeclarationType = newDeclaration.getType();
                    if (newDeclarationType == null || newDeclarationType.isNull()) {
                        newDeclaration.setType(declaration.getType().clone());
                    }
                }
            }
        }
        return forLoop;
    }
    
    private static boolean hasNestedBlocks(final AstNode node) {
        return AstNode.isLoop(node) || node instanceof TryCatchStatement || node instanceof CatchClause || node instanceof LabeledStatement || node instanceof SynchronizedStatement || node instanceof IfElseStatement || node instanceof SwitchSection;
    }
    
    private static boolean isSimpleIterator(final Statement statement) {
        if (!(statement instanceof ExpressionStatement)) {
            return false;
        }
        final Expression e = ((ExpressionStatement)statement).getExpression();
        if (e instanceof AssignmentExpression) {
            return true;
        }
        if (!(e instanceof UnaryOperatorExpression)) {
            return false;
        }
        switch ($SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$UnaryOperatorType()[((UnaryOperatorExpression)e).getOperator().ordinal()]) {
            case 6:
            case 7:
            case 8:
            case 9: {
                return true;
            }
            default: {
                return false;
            }
        }
    }
    
    private Statement canInlineInitializerDeclarations(final ForStatement forLoop) {
        TypeReference variableType = null;
        final BlockStatement tempOuter = new BlockStatement();
        final BlockStatement temp = new BlockStatement();
        final Statement[] initializers = forLoop.getInitializers().toArray(new Statement[forLoop.getInitializers().size()]);
        final Set<String> variableNames = new HashSet<String>();
        Statement firstInlinableInitializer = null;
        forLoop.getParent().insertChildBefore(forLoop, tempOuter, BlockStatement.STATEMENT_ROLE);
        forLoop.remove();
        Statement[] loc_1;
        for (int loc_0 = (loc_1 = initializers).length, loc_2 = 0; loc_2 < loc_0; ++loc_2) {
            final Statement initializer = loc_1[loc_2];
            initializer.remove();
            temp.getStatements().add(initializer);
        }
        temp.getStatements().add(forLoop);
        tempOuter.getStatements().add(temp);
        try {
            Statement[] loc_4;
            for (int loc_3 = (loc_4 = initializers).length, loc_5 = 0; loc_5 < loc_3; ++loc_5) {
                final Statement initializer = loc_4[loc_5];
                final AssignmentExpression assignment = (AssignmentExpression)((ExpressionStatement)initializer).getExpression();
                final IdentifierExpression variable = (IdentifierExpression)assignment.getLeft();
                final String variableName = variable.getIdentifier();
                final VariableDeclarationStatement declaration = findVariableDeclaration(forLoop, variableName);
                if (declaration == null) {
                    firstInlinableInitializer = null;
                }
                else {
                    final Variable underlyingVariable = declaration.getUserData(Keys.VARIABLE);
                    if (underlyingVariable == null || underlyingVariable.isParameter()) {
                        firstInlinableInitializer = null;
                    }
                    else if (!variableNames.add(underlyingVariable.getName())) {
                        firstInlinableInitializer = null;
                    }
                    else {
                        if (variableType == null) {
                            variableType = underlyingVariable.getType();
                        }
                        else if (!variableType.equals(underlyingVariable.getType())) {
                            variableType = underlyingVariable.getType();
                            firstInlinableInitializer = null;
                        }
                        if (!(declaration.getParent() instanceof BlockStatement)) {
                            firstInlinableInitializer = null;
                        }
                        else {
                            final Statement declarationPoint = canMoveVariableDeclarationIntoStatement(this.context, declaration, forLoop);
                            if (declarationPoint != tempOuter) {
                                variableType = null;
                                firstInlinableInitializer = null;
                            }
                            else if (firstInlinableInitializer == null) {
                                firstInlinableInitializer = initializer;
                            }
                        }
                    }
                }
            }
            return firstInlinableInitializer;
        }
        finally {
            forLoop.remove();
            tempOuter.replaceWith(forLoop);
            Statement[] loc_7;
            for (int loc_6 = (loc_7 = initializers).length, loc_8 = 0; loc_8 < loc_6; ++loc_8) {
                final Statement initializer2 = loc_7[loc_8];
                initializer2.remove();
                forLoop.getInitializers().add(initializer2);
            }
        }
    }
    
    public final ForEachStatement transformForEachInArray(final ForStatement loop) {
        Match m = ConvertLoopsTransform.FOR_ARRAY_PATTERN_1.match(loop);
        if (!m.success()) {
            m = ConvertLoopsTransform.FOR_ARRAY_PATTERN_2.match(loop);
            if (!m.success()) {
                m = ConvertLoopsTransform.FOR_ARRAY_PATTERN_3.match(loop);
                if (!m.success()) {
                    return null;
                }
            }
        }
        final IdentifierExpression array = CollectionUtilities.first(m.get("array"));
        final IdentifierExpression item = CollectionUtilities.last(m.get("item"));
        final IdentifierExpression index = CollectionUtilities.first(m.get("index"));
        final VariableDeclarationStatement itemDeclaration = findVariableDeclaration(loop, item.getIdentifier());
        if (itemDeclaration == null || !(itemDeclaration.getParent() instanceof BlockStatement)) {
            return null;
        }
        final Statement declarationPoint = canMoveVariableDeclarationIntoStatement(this.context, itemDeclaration, loop);
        if (declarationPoint != loop) {
            return null;
        }
        final BlockStatement loopBody = (BlockStatement)loop.getEmbeddedStatement();
        final Statement secondStatement = CollectionUtilities.getOrDefault(loopBody.getStatements(), 1);
        if (secondStatement != null && !secondStatement.isNull()) {
            final DefiniteAssignmentAnalysis analysis = new DefiniteAssignmentAnalysis(this.context, loopBody);
            analysis.setAnalyzedRange(secondStatement, loopBody);
            analysis.analyze(array.getIdentifier(), DefiniteAssignmentStatus.DEFINITELY_NOT_ASSIGNED);
            if (analysis.getStatusAfter(loopBody) != DefiniteAssignmentStatus.DEFINITELY_NOT_ASSIGNED) {
                return null;
            }
            analysis.analyze(index.getIdentifier(), DefiniteAssignmentStatus.DEFINITELY_NOT_ASSIGNED);
            if (analysis.getStatusAfter(loopBody) != DefiniteAssignmentStatus.DEFINITELY_NOT_ASSIGNED) {
                return null;
            }
            if (!analysis.getUnassignedVariableUses().isEmpty()) {
                return null;
            }
        }
        final ForEachStatement forEach = new ForEachStatement(loop.getOffset());
        forEach.setVariableType(itemDeclaration.getType().clone());
        forEach.setVariableName(item.getIdentifier());
        forEach.putUserData(Keys.VARIABLE, (Variable)itemDeclaration.getVariables().firstOrNullObject().getUserData(Keys.VARIABLE));
        final BlockStatement body = new BlockStatement();
        final BlockStatement parent = (BlockStatement)loop.getParent();
        forEach.setEmbeddedStatement(body);
        parent.getStatements().insertBefore(loop, forEach);
        loop.remove();
        body.add(loop);
        loop.remove();
        body.add(loop);
        array.remove();
        forEach.setInExpression(array);
        final AstNodeCollection<Statement> bodyStatements = body.getStatements();
        bodyStatements.clear();
        final AstNode itemParent = item.getParent();
        if (itemParent.getParent() instanceof AssignmentExpression && ((AssignmentExpression)itemParent.getParent()).getRight() == itemParent) {
            final Statement itemStatement = CollectionUtilities.firstOrDefault(itemParent.getParent().getAncestors(Statement.class));
            item.remove();
            itemParent.replaceWith(item);
            if (itemStatement != null) {
                itemStatement.remove();
                bodyStatements.add(itemStatement);
            }
        }
        for (final Statement statement : m.get("statement")) {
            statement.remove();
            bodyStatements.add(statement);
        }
        Statement previous;
        for (previous = forEach.getPreviousStatement(); previous instanceof LabelStatement; previous = previous.getPreviousStatement()) {}
        if (previous != null) {
            final Match m2 = ConvertLoopsTransform.ARRAY_INIT_PATTERN.match(previous);
            if (m2.success()) {
                final Expression initializer = m2.get("initializer").iterator().next();
                final IdentifierExpression array2 = m2.get("array").iterator().next();
                if (StringUtilities.equals(array2.getIdentifier(), array.getIdentifier())) {
                    final BlockStatement tempOuter = new BlockStatement();
                    final BlockStatement temp = new BlockStatement();
                    boolean restorePrevious = true;
                    parent.insertChildBefore(forEach, tempOuter, BlockStatement.STATEMENT_ROLE);
                    previous.remove();
                    forEach.remove();
                    temp.add(previous);
                    temp.add(forEach);
                    tempOuter.add(temp);
                    try {
                        final VariableDeclarationStatement arrayDeclaration = findVariableDeclaration(forEach, array.getIdentifier());
                        if (arrayDeclaration != null && arrayDeclaration.getParent() instanceof BlockStatement) {
                            final Statement arrayDeclarationPoint = canMoveVariableDeclarationIntoStatement(this.context, arrayDeclaration, forEach);
                            if (arrayDeclarationPoint == tempOuter) {
                                initializer.remove();
                                array.replaceWith(initializer);
                                restorePrevious = false;
                            }
                        }
                    }
                    finally {
                        previous.remove();
                        forEach.remove();
                        if (restorePrevious) {
                            parent.insertChildBefore(tempOuter, previous, BlockStatement.STATEMENT_ROLE);
                        }
                        parent.insertChildBefore(tempOuter, forEach, BlockStatement.STATEMENT_ROLE);
                        tempOuter.remove();
                    }
                    previous.remove();
                    forEach.remove();
                    if (restorePrevious) {
                        parent.insertChildBefore(tempOuter, previous, BlockStatement.STATEMENT_ROLE);
                    }
                    parent.insertChildBefore(tempOuter, forEach, BlockStatement.STATEMENT_ROLE);
                    tempOuter.remove();
                }
            }
        }
        final DefiniteAssignmentAnalysis analysis2 = new DefiniteAssignmentAnalysis(this.context, body);
        final Statement firstStatement = CollectionUtilities.firstOrDefault(body.getStatements());
        final Statement lastStatement = CollectionUtilities.lastOrDefault(body.getStatements());
        analysis2.setAnalyzedRange(firstStatement, lastStatement);
        analysis2.analyze(item.getIdentifier(), DefiniteAssignmentStatus.DEFINITELY_NOT_ASSIGNED);
        if (!analysis2.isPotentiallyAssigned()) {
            forEach.addVariableModifier(Modifier.FINAL);
        }
        return forEach;
    }
    
    public final ForEachStatement transformForEach(final ExpressionStatement node) {
        final Match m1 = ConvertLoopsTransform.GET_ITERATOR_PATTERN.match(node);
        if (!m1.success()) {
            return null;
        }
        AstNode next;
        for (next = node.getNextSibling(); next instanceof LabelStatement; next = next.getNextSibling()) {}
        final Match m2 = ConvertLoopsTransform.FOR_EACH_PATTERN.match(next);
        if (!m2.success()) {
            return null;
        }
        final IdentifierExpression iterator = m2.get("iterator").iterator().next();
        final IdentifierExpression item = CollectionUtilities.lastOrDefault(m2.get("item"));
        final WhileStatement loop = (WhileStatement)next;
        if (!iterator.matches(m1.get("left").iterator().next())) {
            return null;
        }
        final VariableDeclarationStatement iteratorDeclaration = findVariableDeclaration(loop, iterator.getIdentifier());
        if (iteratorDeclaration == null || !(iteratorDeclaration.getParent() instanceof BlockStatement)) {
            return null;
        }
        final VariableDeclarationStatement itemDeclaration = findVariableDeclaration(loop, item.getIdentifier());
        if (itemDeclaration == null || !(itemDeclaration.getParent() instanceof BlockStatement)) {
            return null;
        }
        Statement declarationPoint = canMoveVariableDeclarationIntoStatement(this.context, itemDeclaration, loop);
        if (declarationPoint != loop) {
            return null;
        }
        final BlockStatement loopBody = (BlockStatement)loop.getEmbeddedStatement();
        final Statement secondStatement = CollectionUtilities.getOrDefault(loopBody.getStatements(), 1);
        if (secondStatement != null && !secondStatement.isNull()) {
            final DefiniteAssignmentAnalysis analysis = new DefiniteAssignmentAnalysis(this.context, loopBody);
            analysis.setAnalyzedRange(secondStatement, loopBody);
            analysis.analyze(iterator.getIdentifier(), DefiniteAssignmentStatus.DEFINITELY_NOT_ASSIGNED);
            if (!analysis.getUnassignedVariableUses().isEmpty()) {
                return null;
            }
        }
        final ForEachStatement forEach = new ForEachStatement(node.getOffset());
        forEach.setVariableType(itemDeclaration.getType().clone());
        forEach.setVariableName(item.getIdentifier());
        forEach.putUserData(Keys.VARIABLE, (Variable)itemDeclaration.getVariables().firstOrNullObject().getUserData(Keys.VARIABLE));
        final BlockStatement body = new BlockStatement();
        forEach.setEmbeddedStatement(body);
        ((BlockStatement)node.getParent()).getStatements().insertBefore(loop, forEach);
        node.remove();
        body.add(node);
        loop.remove();
        body.add(loop);
        declarationPoint = canMoveVariableDeclarationIntoStatement(this.context, iteratorDeclaration, forEach);
        if (declarationPoint != forEach) {
            node.remove();
            ((BlockStatement)forEach.getParent()).getStatements().insertBefore(forEach, node);
            forEach.replaceWith(loop);
            return null;
        }
        final Expression collection = m1.get("collection").iterator().next();
        collection.remove();
        if (collection instanceof SuperReferenceExpression) {
            final ThisReferenceExpression self = new ThisReferenceExpression(collection.getOffset());
            self.putUserData(Keys.TYPE_REFERENCE, (TypeReference)collection.getUserData(Keys.TYPE_REFERENCE));
            self.putUserData(Keys.VARIABLE, (Variable)collection.getUserData(Keys.VARIABLE));
            forEach.setInExpression(self);
        }
        else {
            forEach.setInExpression(collection);
        }
        final AstNodeCollection<Statement> bodyStatements = body.getStatements();
        bodyStatements.clear();
        final AstNode itemParent = item.getParent();
        if (itemParent.getParent() instanceof AssignmentExpression && ((AssignmentExpression)itemParent.getParent()).getRight() == itemParent) {
            final Statement itemStatement = CollectionUtilities.firstOrDefault(itemParent.getParent().getAncestors(Statement.class));
            item.remove();
            itemParent.replaceWith(item);
            if (itemStatement != null) {
                itemStatement.remove();
                bodyStatements.add(itemStatement);
            }
        }
        for (final Statement statement : m2.get("statement")) {
            statement.remove();
            bodyStatements.add(statement);
        }
        final Statement firstStatement = CollectionUtilities.firstOrDefault(body.getStatements());
        final Statement lastStatement = CollectionUtilities.lastOrDefault(body.getStatements());
        if (firstStatement != null && lastStatement != null) {
            final DefiniteAssignmentAnalysis analysis2 = new DefiniteAssignmentAnalysis(this.context, body);
            analysis2.setAnalyzedRange(firstStatement, lastStatement);
            analysis2.analyze(item.getIdentifier(), DefiniteAssignmentStatus.DEFINITELY_NOT_ASSIGNED);
            if (!analysis2.isPotentiallyAssigned()) {
                forEach.addVariableModifier(Modifier.FINAL);
            }
        }
        return forEach;
    }
    
    public final DoWhileStatement transformDoWhile(final WhileStatement loop) {
        final Match m = ConvertLoopsTransform.DO_WHILE_PATTERN.match(loop);
        if (!m.success() || !this.canConvertWhileToDoWhile(loop, CollectionUtilities.firstOrDefault(m.get("continueStatement")))) {
            return null;
        }
        final DoWhileStatement doWhile = new DoWhileStatement(loop.getOffset());
        Expression condition = CollectionUtilities.firstOrDefault(m.get("continueCondition"));
        final boolean hasContinueCondition = condition != null;
        if (hasContinueCondition) {
            condition.remove();
            CollectionUtilities.first(m.get("breakStatement")).remove();
        }
        else {
            condition = CollectionUtilities.firstOrDefault(m.get("breakCondition"));
            condition.remove();
            if (condition instanceof UnaryOperatorExpression && ((UnaryOperatorExpression)condition).getOperator() == UnaryOperatorType.NOT) {
                condition = ((UnaryOperatorExpression)condition).getExpression();
                condition.remove();
            }
            else {
                condition = new UnaryOperatorExpression(UnaryOperatorType.NOT, condition);
            }
        }
        doWhile.setCondition(condition);
        final BlockStatement block = (BlockStatement)loop.getEmbeddedStatement();
        CollectionUtilities.lastOrDefault(block.getStatements()).remove();
        block.remove();
        doWhile.setEmbeddedStatement(block);
        loop.replaceWith(doWhile);
        for (final Statement statement : block.getStatements()) {
            if (statement instanceof VariableDeclarationStatement) {
                final VariableDeclarationStatement declaration = (VariableDeclarationStatement)statement;
                final VariableInitializer v = CollectionUtilities.firstOrDefault(declaration.getVariables());
                for (final AstNode node : condition.getDescendantsAndSelf()) {
                    if (node instanceof IdentifierExpression && StringUtilities.equals(v.getName(), ((IdentifierExpression)node).getIdentifier())) {
                        final Expression initializer = v.getInitializer();
                        initializer.remove();
                        final AssignmentExpression assignment = new AssignmentExpression(new IdentifierExpression(statement.getOffset(), v.getName()), initializer);
                        assignment.putUserData(Keys.MEMBER_REFERENCE, (MemberReference)initializer.getUserData(Keys.MEMBER_REFERENCE));
                        assignment.putUserData(Keys.VARIABLE, (Variable)initializer.getUserData(Keys.VARIABLE));
                        v.putUserData(Keys.MEMBER_REFERENCE, null);
                        v.putUserData(Keys.VARIABLE, null);
                        assignment.putUserData(Keys.MEMBER_REFERENCE, (MemberReference)declaration.getUserData(Keys.MEMBER_REFERENCE));
                        assignment.putUserData(Keys.VARIABLE, (Variable)declaration.getUserData(Keys.VARIABLE));
                        declaration.replaceWith(new ExpressionStatement(assignment));
                        declaration.putUserData(Keys.MEMBER_REFERENCE, null);
                        declaration.putUserData(Keys.VARIABLE, null);
                        doWhile.getParent().insertChildBefore(doWhile, declaration, BlockStatement.STATEMENT_ROLE);
                    }
                }
            }
        }
        return doWhile;
    }
    
    private boolean canConvertWhileToDoWhile(final WhileStatement loop, final ContinueStatement continueStatement) {
        final List<ContinueStatement> continueStatements = new ArrayList<ContinueStatement>();
        for (final AstNode node : loop.getDescendantsAndSelf()) {
            if (node instanceof ContinueStatement) {
                continueStatements.add((ContinueStatement)node);
            }
        }
        if (continueStatements.isEmpty()) {
            return true;
        }
        for (final ContinueStatement cs : continueStatements) {
            final String label = cs.getLabel();
            if (StringUtilities.isNullOrEmpty(label) && cs != continueStatement) {
                return false;
            }
            final Statement previousStatement = loop.getPreviousStatement();
            if (previousStatement instanceof LabelStatement) {
                return !StringUtilities.equals(((LabelStatement)previousStatement).getLabel(), label);
            }
            if (loop.getParent() instanceof LabeledStatement) {
                return !StringUtilities.equals(((LabeledStatement)loop.getParent()).getLabel(), label);
            }
        }
        return true;
    }
    
    public final WhileStatement transformContinueOuter(final WhileStatement loop) {
        final Match m = ConvertLoopsTransform.CONTINUE_OUTER_PATTERN.match(loop);
        if (!m.success()) {
            return loop;
        }
        final LabelStatement label = m.get("label").iterator().next();
        label.remove();
        loop.getParent().insertChildBefore(loop, label, BlockStatement.STATEMENT_ROLE);
        return loop;
    }
    
    static VariableDeclarationStatement findVariableDeclaration(final AstNode node, final String identifier) {
        for (AstNode current = node; current != null; current = current.getParent()) {
            while (current.getPreviousSibling() != null) {
                current = current.getPreviousSibling();
                if (current instanceof VariableDeclarationStatement) {
                    final VariableDeclarationStatement variableDeclaration = (VariableDeclarationStatement)current;
                    final Variable variable = variableDeclaration.getUserData(Keys.VARIABLE);
                    if (variable != null && StringUtilities.equals(variable.getName(), identifier)) {
                        return variableDeclaration;
                    }
                    if (variableDeclaration.getVariables().size() == 1 && StringUtilities.equals(variableDeclaration.getVariables().firstOrNullObject().getName(), identifier)) {
                        return variableDeclaration;
                    }
                    continue;
                }
            }
        }
        return null;
    }
    
    static Statement canMoveVariableDeclarationIntoStatement(final DecompilerContext context, final VariableDeclarationStatement declaration, final Statement targetStatement) {
        if (declaration == null) {
            return null;
        }
        final BlockStatement parent = (BlockStatement)declaration.getParent();
        assert CollectionUtilities.contains(targetStatement.getAncestors(), parent);
        final ArrayList<BlockStatement> blocks = new ArrayList<BlockStatement>();
        for (final AstNode block : targetStatement.getAncestors()) {
            if (block == parent) {
                break;
            }
            if (!(block instanceof BlockStatement)) {
                continue;
            }
            blocks.add((BlockStatement)block);
        }
        blocks.add(parent);
        Collections.reverse(blocks);
        final StrongBox<Statement> declarationPoint = new StrongBox<Statement>();
        final DefiniteAssignmentAnalysis analysis = new DefiniteAssignmentAnalysis(context, blocks.get(0));
        Statement result = null;
        for (final BlockStatement block2 : blocks) {
            if (!DeclareVariablesTransform.findDeclarationPoint(analysis, declaration, block2, declarationPoint, null)) {
                break;
            }
            result = declarationPoint.get();
        }
        return result;
    }
    
    static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$UnaryOperatorType() {
        final int[] loc_0 = ConvertLoopsTransform.$SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$UnaryOperatorType;
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
        return ConvertLoopsTransform.$SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$UnaryOperatorType = loc_1;
    }
}
