package com.strobel.decompiler.languages.java.ast;

import com.strobel.functions.*;
import com.strobel.decompiler.semantics.*;
import com.strobel.decompiler.*;
import java.util.*;
import com.strobel.core.*;
import com.strobel.util.*;
import com.strobel.decompiler.languages.java.analysis.*;

public class DefiniteAssignmentAnalysis
{
    private final DefiniteAssignmentVisitor visitor;
    private final ArrayList<DefiniteAssignmentNode> allNodes;
    private final LinkedHashMap<Statement, DefiniteAssignmentNode> beginNodeMap;
    private final LinkedHashMap<Statement, DefiniteAssignmentNode> endNodeMap;
    private final LinkedHashMap<Statement, DefiniteAssignmentNode> conditionNodeMap;
    private final LinkedHashMap<ControlFlowEdge, DefiniteAssignmentStatus> edgeStatus;
    private final ArrayList<IdentifierExpression> unassignedVariableUses;
    private final List<IdentifierExpression> unassignedVariableUsesView;
    private final ArrayDeque<DefiniteAssignmentNode> nodesWithModifiedInput;
    private Function<AstNode, ResolveResult> resolver;
    private String variableName;
    private int analyzedRangeStart;
    private int analyzedRangeEnd;
    private static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$DefiniteAssignmentStatus;
    private static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$decompiler$languages$java$analysis$ControlFlowNodeType;
    
    public DefiniteAssignmentAnalysis(final DecompilerContext context, final Statement rootStatement) {
        this(rootStatement, new JavaResolver(context));
    }
    
    public DefiniteAssignmentAnalysis(final Statement rootStatement, final Function<AstNode, ResolveResult> resolver) {
        super();
        this.visitor = new DefiniteAssignmentVisitor();
        this.allNodes = new ArrayList<DefiniteAssignmentNode>();
        this.beginNodeMap = new LinkedHashMap<Statement, DefiniteAssignmentNode>();
        this.endNodeMap = new LinkedHashMap<Statement, DefiniteAssignmentNode>();
        this.conditionNodeMap = new LinkedHashMap<Statement, DefiniteAssignmentNode>();
        this.edgeStatus = new LinkedHashMap<ControlFlowEdge, DefiniteAssignmentStatus>();
        this.unassignedVariableUses = new ArrayList<IdentifierExpression>();
        this.unassignedVariableUsesView = Collections.unmodifiableList((List<? extends IdentifierExpression>)this.unassignedVariableUses);
        this.nodesWithModifiedInput = new ArrayDeque<DefiniteAssignmentNode>();
        VerifyArgument.notNull(rootStatement, "rootStatement");
        VerifyArgument.notNull(resolver, "resolver");
        this.resolver = resolver;
        final DerivedControlFlowGraphBuilder builder = new DerivedControlFlowGraphBuilder();
        builder.setEvaluateOnlyPrimitiveConstants(true);
        for (final ControlFlowNode node : builder.buildControlFlowGraph(rootStatement, resolver)) {
            this.allNodes.add((DefiniteAssignmentNode)node);
        }
        for (int i = 0; i < this.allNodes.size(); ++i) {
            final DefiniteAssignmentNode node2 = this.allNodes.get(i);
            node2.setIndex(i);
            if (node2.getType() == ControlFlowNodeType.StartNode || node2.getType() == ControlFlowNodeType.BetweenStatements) {
                this.beginNodeMap.put(node2.getNextStatement(), node2);
            }
            if (node2.getType() == ControlFlowNodeType.BetweenStatements || node2.getType() == ControlFlowNodeType.EndNode) {
                this.endNodeMap.put(node2.getPreviousStatement(), node2);
            }
            if (node2.getType() == ControlFlowNodeType.LoopCondition) {
                this.conditionNodeMap.put(node2.getNextStatement(), node2);
            }
        }
        this.analyzedRangeStart = 0;
        this.analyzedRangeEnd = this.allNodes.size() - 1;
    }
    
    public List<IdentifierExpression> getUnassignedVariableUses() {
        return this.unassignedVariableUsesView;
    }
    
    public void setAnalyzedRange(final Statement start, final Statement end) {
        this.setAnalyzedRange(start, end, true, true);
    }
    
    public void setAnalyzedRange(final Statement start, final Statement end, final boolean startInclusive, final boolean endInclusive) {
        final Map<Statement, DefiniteAssignmentNode> startMap = startInclusive ? this.beginNodeMap : this.endNodeMap;
        final Map<Statement, DefiniteAssignmentNode> endMap = endInclusive ? this.endNodeMap : this.beginNodeMap;
        assert startMap.containsKey(start) && endMap.containsKey(end);
        final int startIndex = startMap.get(start).getIndex();
        final int endIndex = endMap.get(end).getIndex();
        if (startIndex > endIndex) {
            throw new IllegalStateException("The start statement must lexically precede the end statement.");
        }
        this.analyzedRangeStart = startIndex;
        this.analyzedRangeEnd = endIndex;
    }
    
    public void analyze(final String variable) {
        this.analyze(variable, DefiniteAssignmentStatus.POTENTIALLY_ASSIGNED);
    }
    
    public void analyze(final String variable, final DefiniteAssignmentStatus initialStatus) {
        this.variableName = variable;
        try {
            this.unassignedVariableUses.clear();
            for (final DefiniteAssignmentNode node : this.allNodes) {
                node.setNodeStatus(DefiniteAssignmentStatus.CODE_UNREACHABLE);
                for (final ControlFlowEdge edge : node.getOutgoing()) {
                    this.edgeStatus.put(edge, DefiniteAssignmentStatus.CODE_UNREACHABLE);
                }
            }
            this.changeNodeStatus(this.allNodes.get(this.analyzedRangeStart), initialStatus);
            while (!this.nodesWithModifiedInput.isEmpty()) {
                final DefiniteAssignmentNode node = this.nodesWithModifiedInput.poll();
                DefiniteAssignmentStatus inputStatus = DefiniteAssignmentStatus.CODE_UNREACHABLE;
                for (final ControlFlowEdge edge : node.getIncoming()) {
                    inputStatus = this.mergeStatus(inputStatus, this.edgeStatus.get(edge));
                }
                this.changeNodeStatus(node, inputStatus);
            }
        }
        finally {
            this.variableName = null;
        }
        this.variableName = null;
    }
    
    public boolean isPotentiallyAssigned() {
        for (final DefiniteAssignmentNode node : this.allNodes) {
            final DefiniteAssignmentStatus status = node.getNodeStatus();
            if (status == null) {
                return true;
            }
            switch ($SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$DefiniteAssignmentStatus()[status.ordinal()]) {
                case 2:
                case 3:
                case 4:
                case 5: {
                    return true;
                }
                default: {
                    continue;
                }
            }
        }
        return false;
    }
    
    public DefiniteAssignmentStatus getStatusBefore(final Statement statement) {
        return this.beginNodeMap.get(statement).getNodeStatus();
    }
    
    public DefiniteAssignmentStatus getStatusAfter(final Statement statement) {
        return this.endNodeMap.get(statement).getNodeStatus();
    }
    
    public DefiniteAssignmentStatus getBeforeLoopCondition(final Statement statement) {
        return this.conditionNodeMap.get(statement).getNodeStatus();
    }
    
    private DefiniteAssignmentStatus cleanSpecialValues(final DefiniteAssignmentStatus status) {
        if (status == null) {
            return null;
        }
        switch ($SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$DefiniteAssignmentStatus()[status.ordinal()]) {
            case 4:
            case 5: {
                return DefiniteAssignmentStatus.POTENTIALLY_ASSIGNED;
            }
            default: {
                return status;
            }
        }
    }
    
    private DefiniteAssignmentStatus mergeStatus(final DefiniteAssignmentStatus a, final DefiniteAssignmentStatus b) {
        if (a == b) {
            return a;
        }
        if (a == DefiniteAssignmentStatus.CODE_UNREACHABLE) {
            return b;
        }
        if (b == DefiniteAssignmentStatus.CODE_UNREACHABLE) {
            return a;
        }
        return DefiniteAssignmentStatus.POTENTIALLY_ASSIGNED;
    }
    
    private void changeNodeStatus(final DefiniteAssignmentNode node, final DefiniteAssignmentStatus inputStatus) {
        if (node.getNodeStatus() == inputStatus) {
            return;
        }
        node.setNodeStatus(inputStatus);
        DefiniteAssignmentStatus outputStatus = null;
        Label_0103: {
            switch ($SWITCH_TABLE$com$strobel$decompiler$languages$java$analysis$ControlFlowNodeType()[node.getType().ordinal()]) {
                case 2:
                case 3: {
                    if (node.getNextStatement() instanceof IfElseStatement) {
                        break Label_0103;
                    }
                    if (inputStatus == DefiniteAssignmentStatus.DEFINITELY_ASSIGNED) {
                        outputStatus = DefiniteAssignmentStatus.DEFINITELY_ASSIGNED;
                        break;
                    }
                    outputStatus = this.cleanSpecialValues(node.getNextStatement().acceptVisitor((IAstVisitor<? super DefiniteAssignmentStatus, ? extends DefiniteAssignmentStatus>)this.visitor, inputStatus));
                    break;
                }
                case 5: {
                    if (node.getNextStatement() instanceof ForEachStatement) {
                        final ForEachStatement forEach = (ForEachStatement)node.getNextStatement();
                        outputStatus = this.cleanSpecialValues(forEach.getInExpression().acceptVisitor((IAstVisitor<? super DefiniteAssignmentStatus, ? extends DefiniteAssignmentStatus>)this.visitor, inputStatus));
                        if (StringUtilities.equals(forEach.getVariableName(), this.variableName)) {
                            outputStatus = DefiniteAssignmentStatus.DEFINITELY_ASSIGNED;
                            break;
                        }
                        break;
                    }
                    else {
                        assert node.getNextStatement() instanceof IfElseStatement || node.getNextStatement() instanceof ForStatement;
                        final Expression condition = node.getNextStatement().getChildByRole(Roles.CONDITION);
                        if (condition.isNull()) {
                            outputStatus = inputStatus;
                        }
                        else {
                            outputStatus = condition.acceptVisitor((IAstVisitor<? super DefiniteAssignmentStatus, ? extends DefiniteAssignmentStatus>)this.visitor, inputStatus);
                        }
                        for (final ControlFlowEdge edge : node.getOutgoing()) {
                            if (edge.getType() == ControlFlowEdgeType.ConditionTrue && outputStatus == DefiniteAssignmentStatus.ASSIGNED_AFTER_TRUE_EXPRESSION) {
                                this.changeEdgeStatus(edge, DefiniteAssignmentStatus.DEFINITELY_ASSIGNED);
                            }
                            else if (edge.getType() == ControlFlowEdgeType.ConditionFalse && outputStatus == DefiniteAssignmentStatus.ASSIGNED_AFTER_FALSE_EXPRESSION) {
                                this.changeEdgeStatus(edge, DefiniteAssignmentStatus.DEFINITELY_ASSIGNED);
                            }
                            else {
                                this.changeEdgeStatus(edge, this.cleanSpecialValues(outputStatus));
                            }
                        }
                        return;
                    }
                    break;
                }
                case 4: {
                    outputStatus = inputStatus;
                    if (node.getPreviousStatement().getRole() == TryCatchStatement.FINALLY_BLOCK_ROLE && (outputStatus == DefiniteAssignmentStatus.DEFINITELY_ASSIGNED || outputStatus == DefiniteAssignmentStatus.POTENTIALLY_ASSIGNED)) {
                        final TryCatchStatement tryFinally = (TryCatchStatement)node.getPreviousStatement().getParent();
                        for (final DefiniteAssignmentNode n : this.allNodes) {
                            for (final ControlFlowEdge edge2 : n.getOutgoing()) {
                                if (edge2.isLeavingTryFinally() && CollectionUtilities.contains(edge2.getTryFinallyStatements(), tryFinally)) {
                                    final DefiniteAssignmentStatus s = this.edgeStatus.get(edge2);
                                    if (s != DefiniteAssignmentStatus.POTENTIALLY_ASSIGNED) {
                                        continue;
                                    }
                                    this.changeEdgeStatus(edge2, outputStatus);
                                }
                            }
                        }
                        break;
                    }
                    break;
                }
                default: {
                    throw ContractUtils.unreachable();
                }
            }
        }
        for (final ControlFlowEdge edge3 : node.getOutgoing()) {
            this.changeEdgeStatus(edge3, outputStatus);
        }
    }
    
    private void changeEdgeStatus(final ControlFlowEdge edge, final DefiniteAssignmentStatus newStatus) {
        final DefiniteAssignmentStatus oldStatus = this.edgeStatus.get(edge);
        if (oldStatus == newStatus) {
            return;
        }
        if (oldStatus == DefiniteAssignmentStatus.DEFINITELY_ASSIGNED) {
            return;
        }
        if (newStatus == DefiniteAssignmentStatus.CODE_UNREACHABLE || newStatus == DefiniteAssignmentStatus.ASSIGNED_AFTER_FALSE_EXPRESSION || newStatus == DefiniteAssignmentStatus.ASSIGNED_AFTER_TRUE_EXPRESSION) {
            throw new IllegalStateException("Illegal edge output status:" + newStatus);
        }
        this.edgeStatus.put(edge, newStatus);
        final DefiniteAssignmentNode targetNode = (DefiniteAssignmentNode)edge.getTo();
        if (this.analyzedRangeStart <= targetNode.getIndex() && targetNode.getIndex() <= this.analyzedRangeEnd) {
            this.nodesWithModifiedInput.add(targetNode);
        }
    }
    
    protected ResolveResult evaluateConstant(final Expression e) {
        return this.resolver.apply(e);
    }
    
    protected Boolean evaluateCondition(final Expression e) {
        final ResolveResult result = this.evaluateConstant(e);
        if (result == null || !result.isCompileTimeConstant()) {
            return null;
        }
        final Object constantValue = result.getConstantValue();
        if (constantValue instanceof Boolean) {
            return (Boolean)constantValue;
        }
        return null;
    }
    
    static /* synthetic */ DefiniteAssignmentStatus access$0(final DefiniteAssignmentAnalysis param_0, final DefiniteAssignmentStatus param_1) {
        return param_0.cleanSpecialValues(param_1);
    }
    
    static /* synthetic */ String access$1(final DefiniteAssignmentAnalysis param_0) {
        return param_0.variableName;
    }
    
    static /* synthetic */ DefiniteAssignmentVisitor access$2(final DefiniteAssignmentAnalysis param_0) {
        return param_0.visitor;
    }
    
    static /* synthetic */ DefiniteAssignmentStatus access$3(final DefiniteAssignmentAnalysis param_0, final DefiniteAssignmentStatus param_1, final DefiniteAssignmentStatus param_2) {
        return param_0.mergeStatus(param_1, param_2);
    }
    
    static /* synthetic */ ArrayList access$4(final DefiniteAssignmentAnalysis param_0) {
        return param_0.unassignedVariableUses;
    }
    
    static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$DefiniteAssignmentStatus() {
        final int[] loc_0 = DefiniteAssignmentAnalysis.$SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$DefiniteAssignmentStatus;
        if (loc_0 != null) {
            return loc_0;
        }
        final int[] loc_1 = new int[DefiniteAssignmentStatus.values().length];
        try {
            loc_1[DefiniteAssignmentStatus.ASSIGNED_AFTER_FALSE_EXPRESSION.ordinal()] = 5;
        }
        catch (NoSuchFieldError loc_2) {}
        try {
            loc_1[DefiniteAssignmentStatus.ASSIGNED_AFTER_TRUE_EXPRESSION.ordinal()] = 4;
        }
        catch (NoSuchFieldError loc_3) {}
        try {
            loc_1[DefiniteAssignmentStatus.CODE_UNREACHABLE.ordinal()] = 6;
        }
        catch (NoSuchFieldError loc_4) {}
        try {
            loc_1[DefiniteAssignmentStatus.DEFINITELY_ASSIGNED.ordinal()] = 3;
        }
        catch (NoSuchFieldError loc_5) {}
        try {
            loc_1[DefiniteAssignmentStatus.DEFINITELY_NOT_ASSIGNED.ordinal()] = 1;
        }
        catch (NoSuchFieldError loc_6) {}
        try {
            loc_1[DefiniteAssignmentStatus.POTENTIALLY_ASSIGNED.ordinal()] = 2;
        }
        catch (NoSuchFieldError loc_7) {}
        return DefiniteAssignmentAnalysis.$SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$DefiniteAssignmentStatus = loc_1;
    }
    
    static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$decompiler$languages$java$analysis$ControlFlowNodeType() {
        final int[] loc_0 = DefiniteAssignmentAnalysis.$SWITCH_TABLE$com$strobel$decompiler$languages$java$analysis$ControlFlowNodeType;
        if (loc_0 != null) {
            return loc_0;
        }
        final int[] loc_1 = new int[ControlFlowNodeType.values().length];
        try {
            loc_1[ControlFlowNodeType.BetweenStatements.ordinal()] = 3;
        }
        catch (NoSuchFieldError loc_2) {}
        try {
            loc_1[ControlFlowNodeType.EndNode.ordinal()] = 4;
        }
        catch (NoSuchFieldError loc_3) {}
        try {
            loc_1[ControlFlowNodeType.LoopCondition.ordinal()] = 5;
        }
        catch (NoSuchFieldError loc_4) {}
        try {
            loc_1[ControlFlowNodeType.None.ordinal()] = 1;
        }
        catch (NoSuchFieldError loc_5) {}
        try {
            loc_1[ControlFlowNodeType.StartNode.ordinal()] = 2;
        }
        catch (NoSuchFieldError loc_6) {}
        return DefiniteAssignmentAnalysis.$SWITCH_TABLE$com$strobel$decompiler$languages$java$analysis$ControlFlowNodeType = loc_1;
    }
    
    final class DefiniteAssignmentVisitor extends DepthFirstAstVisitor<DefiniteAssignmentStatus, DefiniteAssignmentStatus>
    {
        @Override
        protected DefiniteAssignmentStatus visitChildren(final AstNode node, final DefiniteAssignmentStatus data) {
            assert data == DefiniteAssignmentAnalysis.access$0(DefiniteAssignmentAnalysis.this, data);
            DefiniteAssignmentStatus status = data;
            for (AstNode child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
                assert !(child instanceof Statement);
                if (!(child instanceof TypeDeclaration)) {
                    status = child.acceptVisitor((IAstVisitor<? super DefiniteAssignmentStatus, ? extends DefiniteAssignmentStatus>)this, status);
                    status = DefiniteAssignmentAnalysis.access$0(DefiniteAssignmentAnalysis.this, status);
                }
            }
            return status;
        }
        
        @Override
        public DefiniteAssignmentStatus visitLabeledStatement(final LabeledStatement node, final DefiniteAssignmentStatus data) {
            return node.getStatement().acceptVisitor((IAstVisitor<? super DefiniteAssignmentStatus, ? extends DefiniteAssignmentStatus>)this, data);
        }
        
        @Override
        public DefiniteAssignmentStatus visitBlockStatement(final BlockStatement node, final DefiniteAssignmentStatus data) {
            return data;
        }
        
        @Override
        public DefiniteAssignmentStatus visitTypeDeclaration(final TypeDeclaration node, final DefiniteAssignmentStatus data) {
            return data;
        }
        
        @Override
        public DefiniteAssignmentStatus visitLocalTypeDeclarationStatement(final LocalTypeDeclarationStatement node, final DefiniteAssignmentStatus data) {
            return data;
        }
        
        @Override
        public DefiniteAssignmentStatus visitVariableInitializer(final VariableInitializer node, final DefiniteAssignmentStatus data) {
            if (node.getInitializer().isNull()) {
                return data;
            }
            final DefiniteAssignmentStatus status = node.getInitializer().acceptVisitor((IAstVisitor<? super DefiniteAssignmentStatus, ? extends DefiniteAssignmentStatus>)this, data);
            if (StringUtilities.equals(DefiniteAssignmentAnalysis.access$1(DefiniteAssignmentAnalysis.this), node.getName())) {
                return DefiniteAssignmentStatus.DEFINITELY_ASSIGNED;
            }
            return status;
        }
        
        @Override
        public DefiniteAssignmentStatus visitSwitchStatement(final SwitchStatement node, final DefiniteAssignmentStatus data) {
            return node.getExpression().acceptVisitor((IAstVisitor<? super DefiniteAssignmentStatus, ? extends DefiniteAssignmentStatus>)this, data);
        }
        
        @Override
        public DefiniteAssignmentStatus visitDoWhileStatement(final DoWhileStatement node, final DefiniteAssignmentStatus data) {
            return data;
        }
        
        @Override
        public DefiniteAssignmentStatus visitWhileStatement(final WhileStatement node, final DefiniteAssignmentStatus data) {
            return data;
        }
        
        @Override
        public DefiniteAssignmentStatus visitForStatement(final ForStatement node, final DefiniteAssignmentStatus data) {
            return data;
        }
        
        @Override
        public DefiniteAssignmentStatus visitTryCatchStatement(final TryCatchStatement node, final DefiniteAssignmentStatus data) {
            return data;
        }
        
        @Override
        public DefiniteAssignmentStatus visitForEachStatement(final ForEachStatement node, final DefiniteAssignmentStatus data) {
            return data;
        }
        
        @Override
        public DefiniteAssignmentStatus visitSynchronizedStatement(final SynchronizedStatement node, final DefiniteAssignmentStatus data) {
            return node.getExpression().acceptVisitor((IAstVisitor<? super DefiniteAssignmentStatus, ? extends DefiniteAssignmentStatus>)this, data);
        }
        
        @Override
        public DefiniteAssignmentStatus visitAssignmentExpression(final AssignmentExpression node, final DefiniteAssignmentStatus data) {
            if (node.getOperator() == AssignmentOperatorType.ASSIGN) {
                return this.handleAssignment(node.getLeft(), node.getRight(), data);
            }
            return this.visitChildren(node, data);
        }
        
        final DefiniteAssignmentStatus handleAssignment(final Expression left, final Expression right, final DefiniteAssignmentStatus initialStatus) {
            if (left instanceof IdentifierExpression) {
                final IdentifierExpression identifier = (IdentifierExpression)left;
                if (StringUtilities.equals(DefiniteAssignmentAnalysis.access$1(DefiniteAssignmentAnalysis.this), identifier.getIdentifier())) {
                    if (right != null) {
                        right.acceptVisitor((IAstVisitor<? super DefiniteAssignmentStatus, ?>)this, initialStatus);
                    }
                    return DefiniteAssignmentStatus.DEFINITELY_ASSIGNED;
                }
            }
            DefiniteAssignmentStatus status = left.acceptVisitor((IAstVisitor<? super DefiniteAssignmentStatus, ? extends DefiniteAssignmentStatus>)this, initialStatus);
            if (right != null) {
                status = right.acceptVisitor((IAstVisitor<? super DefiniteAssignmentStatus, ? extends DefiniteAssignmentStatus>)this, status);
            }
            return DefiniteAssignmentAnalysis.access$0(DefiniteAssignmentAnalysis.this, status);
        }
        
        @Override
        public DefiniteAssignmentStatus visitParenthesizedExpression(final ParenthesizedExpression node, final DefiniteAssignmentStatus data) {
            return node.getExpression().acceptVisitor((IAstVisitor<? super DefiniteAssignmentStatus, ? extends DefiniteAssignmentStatus>)DefiniteAssignmentAnalysis.access$2(DefiniteAssignmentAnalysis.this), data);
        }
        
        @Override
        public DefiniteAssignmentStatus visitBinaryOperatorExpression(final BinaryOperatorExpression node, final DefiniteAssignmentStatus data) {
            final BinaryOperatorType operator = node.getOperator();
            if (operator == BinaryOperatorType.LOGICAL_AND) {
                final Boolean condition = DefiniteAssignmentAnalysis.this.evaluateCondition(node.getLeft());
                if (Boolean.TRUE.equals(condition)) {
                    return node.getRight().acceptVisitor((IAstVisitor<? super DefiniteAssignmentStatus, ? extends DefiniteAssignmentStatus>)this, data);
                }
                if (Boolean.FALSE.equals(condition)) {
                    return data;
                }
                final DefiniteAssignmentStatus afterLeft = node.getLeft().acceptVisitor((IAstVisitor<? super DefiniteAssignmentStatus, ? extends DefiniteAssignmentStatus>)this, data);
                DefiniteAssignmentStatus beforeRight;
                if (afterLeft == DefiniteAssignmentStatus.ASSIGNED_AFTER_TRUE_EXPRESSION) {
                    beforeRight = DefiniteAssignmentStatus.DEFINITELY_ASSIGNED;
                }
                else if (afterLeft == DefiniteAssignmentStatus.ASSIGNED_AFTER_FALSE_EXPRESSION) {
                    beforeRight = DefiniteAssignmentStatus.POTENTIALLY_ASSIGNED;
                }
                else {
                    beforeRight = afterLeft;
                }
                final DefiniteAssignmentStatus afterRight = node.getRight().acceptVisitor((IAstVisitor<? super DefiniteAssignmentStatus, ? extends DefiniteAssignmentStatus>)this, beforeRight);
                if (afterLeft == DefiniteAssignmentStatus.DEFINITELY_ASSIGNED) {
                    return DefiniteAssignmentStatus.DEFINITELY_ASSIGNED;
                }
                if (afterRight == DefiniteAssignmentStatus.DEFINITELY_ASSIGNED && afterLeft == DefiniteAssignmentStatus.ASSIGNED_AFTER_FALSE_EXPRESSION) {
                    return DefiniteAssignmentStatus.DEFINITELY_ASSIGNED;
                }
                if (afterRight == DefiniteAssignmentStatus.DEFINITELY_ASSIGNED || afterRight == DefiniteAssignmentStatus.ASSIGNED_AFTER_TRUE_EXPRESSION) {
                    return DefiniteAssignmentStatus.ASSIGNED_AFTER_TRUE_EXPRESSION;
                }
                if (afterLeft == DefiniteAssignmentStatus.ASSIGNED_AFTER_FALSE_EXPRESSION && afterRight == DefiniteAssignmentStatus.ASSIGNED_AFTER_FALSE_EXPRESSION) {
                    return DefiniteAssignmentStatus.ASSIGNED_AFTER_FALSE_EXPRESSION;
                }
                if (afterLeft == DefiniteAssignmentStatus.DEFINITELY_NOT_ASSIGNED && afterRight == DefiniteAssignmentStatus.DEFINITELY_NOT_ASSIGNED) {
                    return DefiniteAssignmentStatus.DEFINITELY_NOT_ASSIGNED;
                }
                return DefiniteAssignmentStatus.POTENTIALLY_ASSIGNED;
            }
            else {
                if (operator != BinaryOperatorType.LOGICAL_OR) {
                    return this.visitChildren(node, data);
                }
                final Boolean condition = DefiniteAssignmentAnalysis.this.evaluateCondition(node.getLeft());
                if (Boolean.FALSE.equals(condition)) {
                    return node.getRight().acceptVisitor((IAstVisitor<? super DefiniteAssignmentStatus, ? extends DefiniteAssignmentStatus>)this, data);
                }
                if (Boolean.TRUE.equals(condition)) {
                    return data;
                }
                final DefiniteAssignmentStatus afterLeft = node.getLeft().acceptVisitor((IAstVisitor<? super DefiniteAssignmentStatus, ? extends DefiniteAssignmentStatus>)this, data);
                DefiniteAssignmentStatus beforeRight;
                if (afterLeft == DefiniteAssignmentStatus.ASSIGNED_AFTER_TRUE_EXPRESSION) {
                    beforeRight = DefiniteAssignmentStatus.POTENTIALLY_ASSIGNED;
                }
                else if (afterLeft == DefiniteAssignmentStatus.ASSIGNED_AFTER_FALSE_EXPRESSION) {
                    beforeRight = DefiniteAssignmentStatus.DEFINITELY_ASSIGNED;
                }
                else {
                    beforeRight = afterLeft;
                }
                final DefiniteAssignmentStatus afterRight = node.getRight().acceptVisitor((IAstVisitor<? super DefiniteAssignmentStatus, ? extends DefiniteAssignmentStatus>)this, beforeRight);
                if (afterLeft == DefiniteAssignmentStatus.DEFINITELY_ASSIGNED) {
                    return DefiniteAssignmentStatus.DEFINITELY_ASSIGNED;
                }
                if (afterRight == DefiniteAssignmentStatus.DEFINITELY_ASSIGNED && afterLeft == DefiniteAssignmentStatus.ASSIGNED_AFTER_TRUE_EXPRESSION) {
                    return DefiniteAssignmentStatus.DEFINITELY_ASSIGNED;
                }
                if (afterRight == DefiniteAssignmentStatus.DEFINITELY_ASSIGNED || afterRight == DefiniteAssignmentStatus.ASSIGNED_AFTER_FALSE_EXPRESSION) {
                    return DefiniteAssignmentStatus.ASSIGNED_AFTER_FALSE_EXPRESSION;
                }
                if (afterLeft == DefiniteAssignmentStatus.ASSIGNED_AFTER_TRUE_EXPRESSION && afterRight == DefiniteAssignmentStatus.ASSIGNED_AFTER_TRUE_EXPRESSION) {
                    return DefiniteAssignmentStatus.ASSIGNED_AFTER_TRUE_EXPRESSION;
                }
                if (afterLeft == DefiniteAssignmentStatus.DEFINITELY_NOT_ASSIGNED && afterRight == DefiniteAssignmentStatus.DEFINITELY_NOT_ASSIGNED) {
                    return DefiniteAssignmentStatus.DEFINITELY_NOT_ASSIGNED;
                }
                return DefiniteAssignmentStatus.POTENTIALLY_ASSIGNED;
            }
        }
        
        @Override
        public DefiniteAssignmentStatus visitUnaryOperatorExpression(final UnaryOperatorExpression node, final DefiniteAssignmentStatus data) {
            if (node.getOperator() != UnaryOperatorType.NOT) {
                return this.visitChildren(node, data);
            }
            final DefiniteAssignmentStatus status = node.getExpression().acceptVisitor((IAstVisitor<? super DefiniteAssignmentStatus, ? extends DefiniteAssignmentStatus>)this, data);
            if (status == DefiniteAssignmentStatus.ASSIGNED_AFTER_FALSE_EXPRESSION) {
                return DefiniteAssignmentStatus.ASSIGNED_AFTER_TRUE_EXPRESSION;
            }
            if (status == DefiniteAssignmentStatus.ASSIGNED_AFTER_TRUE_EXPRESSION) {
                return DefiniteAssignmentStatus.ASSIGNED_AFTER_FALSE_EXPRESSION;
            }
            return status;
        }
        
        @Override
        public DefiniteAssignmentStatus visitConditionalExpression(final ConditionalExpression node, final DefiniteAssignmentStatus data) {
            final Boolean condition = DefiniteAssignmentAnalysis.this.evaluateCondition(node.getCondition());
            if (Boolean.TRUE.equals(condition)) {
                return node.getTrueExpression().acceptVisitor((IAstVisitor<? super DefiniteAssignmentStatus, ? extends DefiniteAssignmentStatus>)this, data);
            }
            if (Boolean.FALSE.equals(condition)) {
                return node.getFalseExpression().acceptVisitor((IAstVisitor<? super DefiniteAssignmentStatus, ? extends DefiniteAssignmentStatus>)this, data);
            }
            final DefiniteAssignmentStatus afterCondition = node.getCondition().acceptVisitor((IAstVisitor<? super DefiniteAssignmentStatus, ? extends DefiniteAssignmentStatus>)this, data);
            DefiniteAssignmentStatus beforeTrue;
            DefiniteAssignmentStatus beforeFalse;
            if (afterCondition == DefiniteAssignmentStatus.ASSIGNED_AFTER_TRUE_EXPRESSION) {
                beforeTrue = DefiniteAssignmentStatus.DEFINITELY_ASSIGNED;
                beforeFalse = DefiniteAssignmentStatus.DEFINITELY_ASSIGNED;
            }
            else if (afterCondition == DefiniteAssignmentStatus.ASSIGNED_AFTER_FALSE_EXPRESSION) {
                beforeTrue = DefiniteAssignmentStatus.POTENTIALLY_ASSIGNED;
                beforeFalse = DefiniteAssignmentStatus.DEFINITELY_ASSIGNED;
            }
            else {
                beforeTrue = afterCondition;
                beforeFalse = afterCondition;
            }
            final DefiniteAssignmentStatus afterTrue = node.getTrueExpression().acceptVisitor((IAstVisitor<? super DefiniteAssignmentStatus, ? extends DefiniteAssignmentStatus>)this, beforeTrue);
            final DefiniteAssignmentStatus afterFalse = node.getTrueExpression().acceptVisitor((IAstVisitor<? super DefiniteAssignmentStatus, ? extends DefiniteAssignmentStatus>)this, beforeFalse);
            return DefiniteAssignmentAnalysis.access$3(DefiniteAssignmentAnalysis.this, DefiniteAssignmentAnalysis.access$0(DefiniteAssignmentAnalysis.this, afterTrue), DefiniteAssignmentAnalysis.access$0(DefiniteAssignmentAnalysis.this, afterFalse));
        }
        
        @Override
        public DefiniteAssignmentStatus visitIdentifierExpression(final IdentifierExpression node, final DefiniteAssignmentStatus data) {
            if (data != DefiniteAssignmentStatus.DEFINITELY_ASSIGNED && StringUtilities.equals(node.getIdentifier(), DefiniteAssignmentAnalysis.access$1(DefiniteAssignmentAnalysis.this)) && node.getTypeArguments().isEmpty()) {
                DefiniteAssignmentAnalysis.access$4(DefiniteAssignmentAnalysis.this).add(node);
            }
            return data;
        }
    }
    
    final class DefiniteAssignmentNode extends ControlFlowNode
    {
        private int _index;
        private DefiniteAssignmentStatus _nodeStatus;
        
        public DefiniteAssignmentNode(final Statement previousStatement, final Statement nextStatement, final ControlFlowNodeType type) {
            super(previousStatement, nextStatement, type);
        }
        
        public int getIndex() {
            return this._index;
        }
        
        public void setIndex(final int index) {
            this._index = index;
        }
        
        public DefiniteAssignmentStatus getNodeStatus() {
            return this._nodeStatus;
        }
        
        public void setNodeStatus(final DefiniteAssignmentStatus nodeStatus) {
            this._nodeStatus = nodeStatus;
        }
        
        @Override
        public String toString() {
            return "[" + this._index + "] " + this._nodeStatus;
        }
    }
    
    final class DerivedControlFlowGraphBuilder extends ControlFlowGraphBuilder
    {
        @Override
        protected ControlFlowNode createNode(final Statement previousStatement, final Statement nextStatement, final ControlFlowNodeType type) {
            return new DefiniteAssignmentNode(previousStatement, nextStatement, type);
        }
    }
}
