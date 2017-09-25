package com.strobel.decompiler.languages.java.analysis;

import com.strobel.functions.*;
import com.strobel.decompiler.semantics.*;
import java.util.*;
import com.strobel.util.*;
import com.strobel.core.*;
import com.strobel.decompiler.languages.java.ast.*;

public class ControlFlowGraphBuilder
{
    private Statement rootStatement;
    private Function<AstNode, ResolveResult> resolver;
    private ArrayList<ControlFlowNode> nodes;
    private HashMap<String, ControlFlowNode> labels;
    private ArrayList<ControlFlowNode> gotoStatements;
    private boolean _evaluateOnlyPrimitiveConstants;
    
    protected ControlFlowNode createNode(final Statement previousStatement, final Statement nextStatement, final ControlFlowNodeType type) {
        return new ControlFlowNode(previousStatement, nextStatement, type);
    }
    
    protected ControlFlowNode createStartNode(final Statement statement) {
        final ControlFlowNode node = this.createNode(null, statement, ControlFlowNodeType.StartNode);
        this.nodes.add(node);
        return node;
    }
    
    protected ControlFlowNode createSpecialNode(final Statement statement, final ControlFlowNodeType type) {
        return this.createSpecialNode(statement, type, true);
    }
    
    protected ControlFlowNode createSpecialNode(final Statement statement, final ControlFlowNodeType type, final boolean addNodeToList) {
        final ControlFlowNode node = this.createNode(null, statement, type);
        if (addNodeToList) {
            this.nodes.add(node);
        }
        return node;
    }
    
    protected ControlFlowNode createEndNode(final Statement statement) {
        return this.createEndNode(statement, true);
    }
    
    protected ControlFlowNode createEndNode(final Statement statement, final boolean addNodeToList) {
        Statement nextStatement = null;
        if (statement == this.rootStatement) {
            nextStatement = null;
        }
        else {
            AstNode next = statement;
            do {
                next = next.getNextSibling();
            } while (next != null && next.getRole() != statement.getRole());
            if (next instanceof Statement) {
                nextStatement = (Statement)next;
            }
        }
        final ControlFlowNodeType type = (nextStatement != null) ? ControlFlowNodeType.BetweenStatements : ControlFlowNodeType.EndNode;
        final ControlFlowNode node = this.createNode(statement, nextStatement, type);
        if (addNodeToList) {
            this.nodes.add(node);
        }
        return node;
    }
    
    protected ControlFlowEdge createEdge(final ControlFlowNode from, final ControlFlowNode to, final ControlFlowEdgeType type) {
        return new ControlFlowEdge(from, to, type);
    }
    
    public List<ControlFlowNode> buildControlFlowGraph(final Statement statement, final Function<AstNode, ResolveResult> resolver) {
        final NodeCreationVisitor nodeCreationVisitor = new NodeCreationVisitor();
        try {
            this.nodes = new ArrayList<ControlFlowNode>();
            this.labels = new HashMap<String, ControlFlowNode>();
            this.gotoStatements = new ArrayList<ControlFlowNode>();
            this.rootStatement = statement;
            this.resolver = resolver;
            final ControlFlowNode entryPoint = this.createStartNode(statement);
            statement.acceptVisitor((IAstVisitor<? super ControlFlowNode, ?>)nodeCreationVisitor, entryPoint);
            for (final ControlFlowNode gotoStatement : this.gotoStatements) {
                String label;
                if (gotoStatement.getNextStatement() instanceof BreakStatement) {
                    label = ((BreakStatement)gotoStatement.getNextStatement()).getLabel();
                }
                else if (gotoStatement.getNextStatement() instanceof ContinueStatement) {
                    label = ((ContinueStatement)gotoStatement.getNextStatement()).getLabel();
                }
                else {
                    label = ((GotoStatement)gotoStatement.getNextStatement()).getLabel();
                }
                final ControlFlowNode labelNode = this.labels.get(label);
                if (labelNode != null) {
                    nodeCreationVisitor.connect(gotoStatement, labelNode, ControlFlowEdgeType.Jump);
                }
            }
            this.annotateLeaveEdgesWithTryFinallyBlocks();
            return this.nodes;
        }
        finally {
            this.nodes = null;
            this.labels = null;
            this.gotoStatements = null;
            this.rootStatement = null;
            this.resolver = null;
        }
    }
    
    final void annotateLeaveEdgesWithTryFinallyBlocks() {
        for (final ControlFlowNode n : this.nodes) {
            for (final ControlFlowEdge edge : n.getOutgoing()) {
                if (edge.getType() != ControlFlowEdgeType.Jump) {
                    continue;
                }
                final Statement gotoStatement = edge.getFrom().getNextStatement();
                assert gotoStatement instanceof GotoStatement || gotoStatement instanceof ContinueStatement;
                final Statement targetStatement = (edge.getTo().getPreviousStatement() != null) ? edge.getTo().getPreviousStatement() : edge.getTo().getNextStatement();
                if (gotoStatement.getParent() == targetStatement.getParent()) {
                    continue;
                }
                final Set<TryCatchStatement> targetParentTryCatch = new LinkedHashSet<TryCatchStatement>();
                for (final AstNode ancestor : targetStatement.getAncestors()) {
                    if (ancestor instanceof TryCatchStatement) {
                        targetParentTryCatch.add((TryCatchStatement)ancestor);
                    }
                }
                for (AstNode node = gotoStatement.getParent(); node != null; node = node.getParent()) {
                    if (node instanceof TryCatchStatement) {
                        final TryCatchStatement leftTryCatch = (TryCatchStatement)node;
                        if (targetParentTryCatch.contains(leftTryCatch)) {
                            break;
                        }
                        if (!leftTryCatch.getFinallyBlock().isNull()) {
                            edge.AddJumpOutOfTryFinally(leftTryCatch);
                        }
                    }
                }
            }
        }
    }
    
    public final boolean isEvaluateOnlyPrimitiveConstants() {
        return this._evaluateOnlyPrimitiveConstants;
    }
    
    public final void setEvaluateOnlyPrimitiveConstants(final boolean evaluateOnlyPrimitiveConstants) {
        this._evaluateOnlyPrimitiveConstants = evaluateOnlyPrimitiveConstants;
    }
    
    protected ResolveResult evaluateConstant(final Expression e) {
        if (this._evaluateOnlyPrimitiveConstants && !(e instanceof PrimitiveExpression) && !(e instanceof NullReferenceExpression)) {
            return null;
        }
        return this.resolver.apply(e);
    }
    
    private boolean areEqualConstants(final ResolveResult c1, final ResolveResult c2) {
        return c1 != null && c2 != null && c1.isCompileTimeConstant() && c2.isCompileTimeConstant() && Comparer.equals(c1.getConstantValue(), c2.getConstantValue());
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
    
    static /* synthetic */ HashMap access$0(final ControlFlowGraphBuilder param_0) {
        return param_0.labels;
    }
    
    static /* synthetic */ boolean access$1(final ControlFlowGraphBuilder param_0, final ResolveResult param_1, final ResolveResult param_2) {
        return param_0.areEqualConstants(param_1, param_2);
    }
    
    static /* synthetic */ ArrayList access$2(final ControlFlowGraphBuilder param_0) {
        return param_0.nodes;
    }
    
    static /* synthetic */ ArrayList access$3(final ControlFlowGraphBuilder param_0) {
        return param_0.gotoStatements;
    }
    
    final class NodeCreationVisitor extends DepthFirstAstVisitor<ControlFlowNode, ControlFlowNode>
    {
        final Stack<ControlFlowNode> breakTargets;
        final Stack<ControlFlowNode> continueTargets;
        final Stack<ControlFlowNode> gotoTargets;
        
        NodeCreationVisitor() {
            super();
            this.breakTargets = new Stack<ControlFlowNode>();
            this.continueTargets = new Stack<ControlFlowNode>();
            this.gotoTargets = new Stack<ControlFlowNode>();
        }
        
        final ControlFlowEdge connect(final ControlFlowNode from, final ControlFlowNode to) {
            return this.connect(from, to, ControlFlowEdgeType.Normal);
        }
        
        final ControlFlowEdge connect(final ControlFlowNode from, final ControlFlowNode to, final ControlFlowEdgeType type) {
            final ControlFlowEdge edge = ControlFlowGraphBuilder.this.createEdge(from, to, type);
            from.getOutgoing().add(edge);
            to.getIncoming().add(edge);
            return edge;
        }
        
        final ControlFlowNode createConnectedEndNode(final Statement statement, final ControlFlowNode from) {
            final ControlFlowNode newNode = ControlFlowGraphBuilder.this.createEndNode(statement);
            this.connect(from, newNode);
            return newNode;
        }
        
        final ControlFlowNode handleStatementList(final AstNodeCollection<Statement> statements, final ControlFlowNode source) {
            ControlFlowNode childNode = null;
            for (final Statement statement : statements) {
                if (childNode == null) {
                    childNode = ControlFlowGraphBuilder.this.createStartNode(statement);
                    if (source != null) {
                        this.connect(source, childNode);
                    }
                }
                assert childNode.getNextStatement() == statement;
                childNode = statement.acceptVisitor((IAstVisitor<? super ControlFlowNode, ? extends ControlFlowNode>)this, childNode);
                assert childNode.getPreviousStatement() == statement;
            }
            return (childNode != null) ? childNode : source;
        }
        
        @Override
        protected ControlFlowNode visitChildren(final AstNode node, final ControlFlowNode data) {
            throw ContractUtils.unreachable();
        }
        
        @Override
        public ControlFlowNode visitBlockStatement(final BlockStatement node, final ControlFlowNode data) {
            final ControlFlowNode childNode = this.handleStatementList(node.getStatements(), data);
            return this.createConnectedEndNode(node, childNode);
        }
        
        @Override
        public ControlFlowNode visitEmptyStatement(final EmptyStatement node, final ControlFlowNode data) {
            return this.createConnectedEndNode(node, data);
        }
        
        @Override
        public ControlFlowNode visitLabelStatement(final LabelStatement node, final ControlFlowNode data) {
            final ControlFlowNode end = this.createConnectedEndNode(node, data);
            ControlFlowGraphBuilder.access$0(ControlFlowGraphBuilder.this).put(node.getLabel(), end);
            return end;
        }
        
        @Override
        public ControlFlowNode visitLabeledStatement(final LabeledStatement node, final ControlFlowNode data) {
            final ControlFlowNode end = this.createConnectedEndNode(node, data);
            ControlFlowGraphBuilder.access$0(ControlFlowGraphBuilder.this).put(node.getLabel(), end);
            this.connect(end, node.getStatement().acceptVisitor((IAstVisitor<? super ControlFlowNode, ? extends ControlFlowNode>)this, data));
            return end;
        }
        
        @Override
        public ControlFlowNode visitVariableDeclaration(final VariableDeclarationStatement node, final ControlFlowNode data) {
            return this.createConnectedEndNode(node, data);
        }
        
        @Override
        public ControlFlowNode visitExpressionStatement(final ExpressionStatement node, final ControlFlowNode data) {
            return this.createConnectedEndNode(node, data);
        }
        
        @Override
        public ControlFlowNode visitIfElseStatement(final IfElseStatement node, final ControlFlowNode data) {
            final Boolean condition = ControlFlowGraphBuilder.this.evaluateCondition(node.getCondition());
            final ControlFlowNode trueBegin = ControlFlowGraphBuilder.this.createStartNode(node.getTrueStatement());
            if (!Boolean.FALSE.equals(condition)) {
                this.connect(data, trueBegin, ControlFlowEdgeType.ConditionTrue);
            }
            final ControlFlowNode trueEnd = node.getTrueStatement().acceptVisitor((IAstVisitor<? super ControlFlowNode, ? extends ControlFlowNode>)this, trueBegin);
            ControlFlowNode falseEnd;
            if (node.getFalseStatement().isNull()) {
                falseEnd = null;
            }
            else {
                final ControlFlowNode falseBegin = ControlFlowGraphBuilder.this.createStartNode(node.getFalseStatement());
                if (!Boolean.TRUE.equals(condition)) {
                    this.connect(data, falseBegin, ControlFlowEdgeType.ConditionFalse);
                }
                falseEnd = node.getFalseStatement().acceptVisitor((IAstVisitor<? super ControlFlowNode, ? extends ControlFlowNode>)this, falseBegin);
            }
            final ControlFlowNode end = ControlFlowGraphBuilder.this.createEndNode(node);
            if (trueEnd != null) {
                this.connect(trueEnd, end);
            }
            if (falseEnd != null) {
                this.connect(falseEnd, end);
            }
            else if (!Boolean.TRUE.equals(condition)) {
                this.connect(data, end, ControlFlowEdgeType.ConditionFalse);
            }
            return end;
        }
        
        @Override
        public ControlFlowNode visitAssertStatement(final AssertStatement node, final ControlFlowNode data) {
            return this.createConnectedEndNode(node, data);
        }
        
        @Override
        public ControlFlowNode visitSwitchStatement(final SwitchStatement node, final ControlFlowNode data) {
            final ResolveResult constant = ControlFlowGraphBuilder.this.evaluateConstant(node.getExpression());
            SwitchSection defaultSection = null;
            SwitchSection sectionMatchedByConstant = null;
            for (final SwitchSection section : node.getSwitchSections()) {
                for (final CaseLabel label : section.getCaseLabels()) {
                    if (label.getExpression().isNull()) {
                        defaultSection = section;
                    }
                    else {
                        if (constant == null || !constant.isCompileTimeConstant()) {
                            continue;
                        }
                        final ResolveResult labelConstant = ControlFlowGraphBuilder.this.evaluateConstant(label.getExpression());
                        if (!ControlFlowGraphBuilder.access$1(ControlFlowGraphBuilder.this, constant, labelConstant)) {
                            continue;
                        }
                        sectionMatchedByConstant = section;
                    }
                }
            }
            if (constant != null && constant.isCompileTimeConstant() && sectionMatchedByConstant == null) {
                sectionMatchedByConstant = defaultSection;
            }
            final ControlFlowNode end = ControlFlowGraphBuilder.this.createEndNode(node, false);
            this.breakTargets.push(end);
            for (final SwitchSection section2 : node.getSwitchSections()) {
                assert section2 != null;
                if (constant == null || !constant.isCompileTimeConstant() || section2 == sectionMatchedByConstant) {
                    this.handleStatementList(section2.getStatements(), data);
                }
                else {
                    this.handleStatementList(section2.getStatements(), null);
                }
            }
            this.breakTargets.pop();
            if (defaultSection == null || sectionMatchedByConstant == null) {
                this.connect(data, end);
            }
            ControlFlowGraphBuilder.access$2(ControlFlowGraphBuilder.this).add(end);
            return end;
        }
        
        @Override
        public ControlFlowNode visitWhileStatement(final WhileStatement node, final ControlFlowNode data) {
            final ControlFlowNode end = ControlFlowGraphBuilder.this.createEndNode(node, false);
            final ControlFlowNode conditionNode = ControlFlowGraphBuilder.this.createSpecialNode(node, ControlFlowNodeType.LoopCondition);
            this.breakTargets.push(end);
            this.continueTargets.push(conditionNode);
            this.connect(data, conditionNode);
            final Boolean condition = ControlFlowGraphBuilder.this.evaluateCondition(node.getCondition());
            final ControlFlowNode bodyStart = ControlFlowGraphBuilder.this.createStartNode(node.getEmbeddedStatement());
            if (!Boolean.FALSE.equals(condition)) {
                this.connect(conditionNode, bodyStart, ControlFlowEdgeType.ConditionTrue);
            }
            final ControlFlowNode bodyEnd = node.getEmbeddedStatement().acceptVisitor((IAstVisitor<? super ControlFlowNode, ? extends ControlFlowNode>)this, bodyStart);
            this.connect(bodyEnd, conditionNode);
            if (!Boolean.TRUE.equals(condition)) {
                this.connect(conditionNode, end, ControlFlowEdgeType.ConditionFalse);
            }
            this.breakTargets.pop();
            this.continueTargets.pop();
            ControlFlowGraphBuilder.access$2(ControlFlowGraphBuilder.this).add(end);
            return end;
        }
        
        @Override
        public ControlFlowNode visitDoWhileStatement(final DoWhileStatement node, final ControlFlowNode data) {
            final ControlFlowNode end = ControlFlowGraphBuilder.this.createEndNode(node, false);
            final ControlFlowNode conditionNode = ControlFlowGraphBuilder.this.createSpecialNode(node, ControlFlowNodeType.LoopCondition, false);
            this.breakTargets.push(end);
            this.continueTargets.push(conditionNode);
            final ControlFlowNode bodyStart = ControlFlowGraphBuilder.this.createStartNode(node.getEmbeddedStatement());
            this.connect(data, bodyStart);
            final ControlFlowNode bodyEnd = node.getEmbeddedStatement().acceptVisitor((IAstVisitor<? super ControlFlowNode, ? extends ControlFlowNode>)this, bodyStart);
            this.connect(bodyEnd, conditionNode);
            final Boolean condition = ControlFlowGraphBuilder.this.evaluateCondition(node.getCondition());
            if (!Boolean.FALSE.equals(condition)) {
                this.connect(conditionNode, bodyStart, ControlFlowEdgeType.ConditionTrue);
            }
            if (!Boolean.TRUE.equals(condition)) {
                this.connect(conditionNode, end, ControlFlowEdgeType.ConditionFalse);
            }
            this.breakTargets.pop();
            this.continueTargets.pop();
            ControlFlowGraphBuilder.access$2(ControlFlowGraphBuilder.this).add(conditionNode);
            ControlFlowGraphBuilder.access$2(ControlFlowGraphBuilder.this).add(end);
            return end;
        }
        
        @Override
        public ControlFlowNode visitForStatement(final ForStatement node, final ControlFlowNode data) {
            final ControlFlowNode newData = this.handleStatementList(node.getInitializers(), data);
            final ControlFlowNode end = ControlFlowGraphBuilder.this.createEndNode(node, false);
            final ControlFlowNode conditionNode = ControlFlowGraphBuilder.this.createSpecialNode(node, ControlFlowNodeType.LoopCondition);
            this.connect(newData, conditionNode);
            final int iteratorStartNodeId = ControlFlowGraphBuilder.access$2(ControlFlowGraphBuilder.this).size();
            final ControlFlowNode iteratorEnd = this.handleStatementList(node.getIterators(), null);
            ControlFlowNode iteratorStart;
            if (iteratorEnd != null) {
                iteratorStart = ControlFlowGraphBuilder.access$2(ControlFlowGraphBuilder.this).get(iteratorStartNodeId);
            }
            else {
                iteratorStart = conditionNode;
            }
            this.breakTargets.push(end);
            this.continueTargets.push(iteratorStart);
            final ControlFlowNode bodyStart = ControlFlowGraphBuilder.this.createStartNode(node.getEmbeddedStatement());
            final ControlFlowNode bodyEnd = node.getEmbeddedStatement().acceptVisitor((IAstVisitor<? super ControlFlowNode, ? extends ControlFlowNode>)this, bodyStart);
            if (bodyEnd != null) {
                this.connect(bodyEnd, iteratorStart);
            }
            this.breakTargets.pop();
            this.continueTargets.pop();
            final Boolean condition = node.getCondition().isNull() ? Boolean.TRUE : ControlFlowGraphBuilder.this.evaluateCondition(node.getCondition());
            if (!Boolean.FALSE.equals(condition)) {
                this.connect(conditionNode, bodyStart, ControlFlowEdgeType.ConditionTrue);
            }
            if (!Boolean.TRUE.equals(condition)) {
                this.connect(conditionNode, end, ControlFlowEdgeType.ConditionFalse);
            }
            ControlFlowGraphBuilder.access$2(ControlFlowGraphBuilder.this).add(end);
            return end;
        }
        
        final ControlFlowNode handleEmbeddedStatement(final Statement embeddedStatement, final ControlFlowNode source) {
            if (embeddedStatement == null || embeddedStatement.isNull()) {
                return source;
            }
            final ControlFlowNode bodyStart = ControlFlowGraphBuilder.this.createStartNode(embeddedStatement);
            if (source != null) {
                this.connect(source, bodyStart);
            }
            return embeddedStatement.acceptVisitor((IAstVisitor<? super ControlFlowNode, ? extends ControlFlowNode>)this, bodyStart);
        }
        
        @Override
        public ControlFlowNode visitForEachStatement(final ForEachStatement node, final ControlFlowNode data) {
            final ControlFlowNode end = ControlFlowGraphBuilder.this.createEndNode(node, false);
            final ControlFlowNode conditionNode = ControlFlowGraphBuilder.this.createSpecialNode(node, ControlFlowNodeType.LoopCondition);
            this.connect(data, conditionNode);
            this.breakTargets.push(end);
            this.continueTargets.push(conditionNode);
            final ControlFlowNode bodyEnd = this.handleEmbeddedStatement(node.getEmbeddedStatement(), conditionNode);
            this.connect(bodyEnd, conditionNode);
            this.breakTargets.pop();
            this.continueTargets.pop();
            this.connect(conditionNode, end);
            ControlFlowGraphBuilder.access$2(ControlFlowGraphBuilder.this).add(end);
            return end;
        }
        
        @Override
        public ControlFlowNode visitGotoStatement(final GotoStatement node, final ControlFlowNode data) {
            ControlFlowGraphBuilder.access$3(ControlFlowGraphBuilder.this).add(data);
            return ControlFlowGraphBuilder.this.createEndNode(node);
        }
        
        @Override
        public ControlFlowNode visitBreakStatement(final BreakStatement node, final ControlFlowNode data) {
            if (!StringUtilities.isNullOrEmpty(node.getLabel())) {
                ControlFlowGraphBuilder.access$3(ControlFlowGraphBuilder.this).add(data);
                return ControlFlowGraphBuilder.this.createEndNode(node);
            }
            if (!this.breakTargets.isEmpty()) {
                this.connect(data, this.breakTargets.peek(), ControlFlowEdgeType.Jump);
            }
            return ControlFlowGraphBuilder.this.createEndNode(node);
        }
        
        @Override
        public ControlFlowNode visitContinueStatement(final ContinueStatement node, final ControlFlowNode data) {
            if (!StringUtilities.isNullOrEmpty(node.getLabel())) {
                ControlFlowGraphBuilder.access$3(ControlFlowGraphBuilder.this).add(data);
                return ControlFlowGraphBuilder.this.createEndNode(node);
            }
            if (!this.continueTargets.isEmpty()) {
                this.connect(data, this.continueTargets.peek(), ControlFlowEdgeType.Jump);
            }
            return ControlFlowGraphBuilder.this.createEndNode(node);
        }
        
        @Override
        public ControlFlowNode visitReturnStatement(final ReturnStatement node, final ControlFlowNode data) {
            return ControlFlowGraphBuilder.this.createEndNode(node);
        }
        
        @Override
        public ControlFlowNode visitThrowStatement(final ThrowStatement node, final ControlFlowNode data) {
            return ControlFlowGraphBuilder.this.createEndNode(node);
        }
        
        @Override
        public ControlFlowNode visitTryCatchStatement(final TryCatchStatement node, final ControlFlowNode data) {
            final boolean hasFinally = !node.getFinallyBlock().isNull();
            final ControlFlowNode end = ControlFlowGraphBuilder.this.createEndNode(node, false);
            ControlFlowEdge edge = this.connect(this.handleEmbeddedStatement(node.getTryBlock(), data), end);
            if (hasFinally) {
                edge.AddJumpOutOfTryFinally(node);
            }
            for (final CatchClause cc : node.getCatchClauses()) {
                edge = this.connect(this.handleEmbeddedStatement(cc.getBody(), data), end);
                if (hasFinally) {
                    edge.AddJumpOutOfTryFinally(node);
                }
            }
            if (hasFinally) {
                this.handleEmbeddedStatement(node.getFinallyBlock(), data);
            }
            ControlFlowGraphBuilder.access$2(ControlFlowGraphBuilder.this).add(end);
            return end;
        }
        
        @Override
        public ControlFlowNode visitSynchronizedStatement(final SynchronizedStatement node, final ControlFlowNode data) {
            final ControlFlowNode bodyEnd = this.handleEmbeddedStatement(node.getEmbeddedStatement(), data);
            return this.createConnectedEndNode(node, bodyEnd);
        }
    }
}
