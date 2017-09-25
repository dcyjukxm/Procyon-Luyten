package com.strobel.decompiler.languages.java.ast.transforms;

import com.strobel.decompiler.*;
import com.strobel.functions.*;
import com.strobel.decompiler.languages.java.ast.*;
import com.strobel.core.*;
import java.util.*;

public final class BreakTargetRelocation extends ContextTrackingVisitor<Void>
{
    public BreakTargetRelocation(final DecompilerContext context) {
        super(context);
    }
    
    @Override
    public Void visitMethodDeclaration(final MethodDeclaration node, final Void _) {
        super.visitMethodDeclaration(node, _);
        this.runForMethod(node);
        return null;
    }
    
    @Override
    public Void visitConstructorDeclaration(final ConstructorDeclaration node, final Void _) {
        super.visitConstructorDeclaration(node, _);
        this.runForMethod(node);
        return null;
    }
    
    private void runForMethod(final AstNode node) {
        final Map<String, LabelInfo> labels = new LinkedHashMap<String, LabelInfo>();
        for (final AstNode n : node.getDescendantsAndSelf()) {
            if (n instanceof LabelStatement) {
                final LabelStatement label = (LabelStatement)n;
                final LabelInfo labelInfo = labels.get(label.getLabel());
                if (labelInfo == null) {
                    labels.put(label.getLabel(), new LabelInfo(label));
                }
                else {
                    labelInfo.label = label;
                    labelInfo.labelTarget = label.getNextSibling();
                    labelInfo.labelIsLast = true;
                }
            }
            else {
                if (!(n instanceof GotoStatement)) {
                    continue;
                }
                final GotoStatement gotoStatement = (GotoStatement)n;
                LabelInfo labelInfo = labels.get(gotoStatement.getLabel());
                if (labelInfo == null) {
                    labels.put(gotoStatement.getLabel(), labelInfo = new LabelInfo(gotoStatement.getLabel()));
                }
                else {
                    labelInfo.labelIsLast = false;
                }
                labelInfo.gotoStatements.add(gotoStatement);
            }
        }
        for (final LabelInfo labelInfo2 : labels.values()) {
            this.run(labelInfo2);
        }
    }
    
    private void run(final LabelInfo labelInfo) {
        assert labelInfo != null;
        final LabelStatement label = labelInfo.label;
        if (label == null || labelInfo.gotoStatements.isEmpty()) {
            return;
        }
        final List<Stack<AstNode>> paths = new ArrayList<Stack<AstNode>>();
        for (final GotoStatement gotoStatement : labelInfo.gotoStatements) {
            paths.add(this.buildPath(gotoStatement));
        }
        paths.add(this.buildPath(label));
        final Statement commonAncestor = this.findLowestCommonAncestor(paths);
        if (commonAncestor instanceof SwitchStatement && labelInfo.gotoStatements.size() == 1 && label.getParent() instanceof BlockStatement && label.getParent().getParent() instanceof SwitchSection && label.getParent().getParent().getParent() == commonAncestor) {
            final GotoStatement s = labelInfo.gotoStatements.get(0);
            if (s.getParent() instanceof BlockStatement && s.getParent().getParent() instanceof SwitchSection && s.getParent().getParent().getParent() == commonAncestor) {
                final SwitchStatement parentSwitch = (SwitchStatement)commonAncestor;
                final SwitchSection targetSection = (SwitchSection)label.getParent().getParent();
                final BlockStatement fallThroughBlock = (BlockStatement)s.getParent();
                final SwitchSection fallThroughSection = (SwitchSection)fallThroughBlock.getParent();
                if (fallThroughSection.getNextSibling() != targetSection) {
                    fallThroughSection.remove();
                    parentSwitch.getSwitchSections().insertBefore(targetSection, fallThroughSection);
                }
                final BlockStatement parentBlock = (BlockStatement)label.getParent();
                s.remove();
                label.remove();
                if (fallThroughBlock.getStatements().isEmpty()) {
                    fallThroughBlock.remove();
                }
                if (parentBlock.getStatements().isEmpty()) {
                    parentBlock.remove();
                }
                return;
            }
        }
        paths.clear();
        for (final GotoStatement gotoStatement2 : labelInfo.gotoStatements) {
            paths.add(this.buildPath(gotoStatement2));
        }
        paths.add(this.buildPath(label));
        final BlockStatement parent = this.findLowestCommonAncestorBlock(paths);
        if (parent == null) {
            return;
        }
        if (this.convertToContinue(parent, labelInfo, paths)) {
            return;
        }
        final Set<AstNode> remainingNodes = new LinkedHashSet<AstNode>();
        final LinkedList<AstNode> orderedNodes = new LinkedList<AstNode>();
        final AstNode startNode = paths.get(0).peek();
        assert startNode != null;
        for (final Stack<AstNode> path : paths) {
            if (path.isEmpty()) {
                return;
            }
            remainingNodes.add(path.peek());
        }
        AstNode current = startNode;
        while (lookAhead(current, remainingNodes)) {
            while (current != null && !remainingNodes.isEmpty()) {
                if (current instanceof Statement) {
                    orderedNodes.addLast(current);
                }
                if (remainingNodes.remove(current)) {
                    break;
                }
                current = current.getNextSibling();
            }
        }
        if (!remainingNodes.isEmpty()) {
            current = startNode.getPreviousSibling();
            while (lookBehind(current, remainingNodes)) {
                while (current != null && !remainingNodes.isEmpty()) {
                    if (current instanceof Statement) {
                        orderedNodes.addFirst(current);
                    }
                    if (remainingNodes.remove(current)) {
                        break;
                    }
                    current = current.getPreviousSibling();
                }
            }
        }
        if (!remainingNodes.isEmpty()) {
            return;
        }
        final AstNode insertBefore = orderedNodes.getLast().getNextSibling();
        final AstNode insertAfter = orderedNodes.getFirst().getPreviousSibling();
        final BlockStatement newBlock = new BlockStatement();
        final AstNodeCollection<Statement> blockStatements = newBlock.getStatements();
        final AssessForLoopResult loopData = this.assessForLoop(commonAncestor, paths, label, labelInfo.gotoStatements);
        final boolean rewriteAsLoop = !loopData.continueStatements.isEmpty();
        for (final AstNode node : orderedNodes) {
            node.remove();
            blockStatements.add((Statement)node);
        }
        label.remove();
        Statement insertedStatement;
        if (rewriteAsLoop) {
            final WhileStatement loop = new WhileStatement(new PrimitiveExpression(-34, true));
            loop.setEmbeddedStatement(newBlock);
            if (!AstNode.isUnconditionalBranch(CollectionUtilities.lastOrDefault(newBlock.getStatements()))) {
                newBlock.getStatements().add(new BreakStatement(-34));
            }
            if (loopData.needsLabel) {
                insertedStatement = (labelInfo.newLabeledStatement = new LabeledStatement(label.getLabel(), loop));
            }
            else {
                insertedStatement = loop;
            }
        }
        else if (newBlock.getStatements().hasSingleElement() && AstNode.isLoop(newBlock.getStatements().firstOrNullObject())) {
            final Statement loop2 = newBlock.getStatements().firstOrNullObject();
            loop2.remove();
            insertedStatement = (labelInfo.newLabeledStatement = new LabeledStatement(label.getLabel(), loop2));
        }
        else {
            insertedStatement = (labelInfo.newLabeledStatement = new LabeledStatement(label.getLabel(), newBlock));
        }
        if (parent.getParent() instanceof LabelStatement) {
            AstNode insertionPoint;
            for (insertionPoint = parent; insertionPoint != null && insertionPoint.getParent() instanceof LabelStatement; insertionPoint = CollectionUtilities.firstOrDefault(insertionPoint.getAncestors(BlockStatement.class))) {}
            if (insertionPoint == null) {
                return;
            }
            insertionPoint.addChild(insertedStatement, BlockStatement.STATEMENT_ROLE);
        }
        else if (insertBefore != null) {
            parent.insertChildBefore(insertBefore, insertedStatement, BlockStatement.STATEMENT_ROLE);
        }
        else if (insertAfter != null) {
            parent.insertChildAfter(insertAfter, insertedStatement, BlockStatement.STATEMENT_ROLE);
        }
        else {
            parent.getStatements().add(insertedStatement);
        }
        for (final GotoStatement gotoStatement3 : labelInfo.gotoStatements) {
            if (loopData.continueStatements.contains(gotoStatement3)) {
                final ContinueStatement continueStatement = new ContinueStatement(-34);
                if (loopData.needsLabel) {
                    continueStatement.setLabel(gotoStatement3.getLabel());
                }
                gotoStatement3.replaceWith(continueStatement);
            }
            else {
                final BreakStatement breakStatement = new BreakStatement(-34);
                breakStatement.setLabel(gotoStatement3.getLabel());
                gotoStatement3.replaceWith(breakStatement);
            }
        }
        if (rewriteAsLoop && !loopData.preexistingContinueStatements.isEmpty()) {
            final AstNode existingLoop = CollectionUtilities.firstOrDefault(insertedStatement.getAncestors(), new Predicate<AstNode>() {
                @Override
                public boolean test(final AstNode node) {
                    return AstNode.isLoop(node);
                }
            });
            if (existingLoop != null) {
                final String loopLabel = String.valueOf(label.getLabel()) + "_Outer";
                existingLoop.replaceWith((Function<? super AstNode, ? extends AstNode>)new Function<AstNode, AstNode>() {
                    @Override
                    public AstNode apply(final AstNode input) {
                        return new LabeledStatement(loopLabel, (Statement)existingLoop);
                    }
                });
                for (final ContinueStatement statement : loopData.preexistingContinueStatements) {
                    statement.setLabel(loopLabel);
                }
            }
        }
    }
    
    private boolean convertToContinue(final BlockStatement parent, final LabelInfo labelInfo, final List<Stack<AstNode>> paths) {
        if (!AstNode.isLoop(parent.getParent())) {
            return false;
        }
        final AstNode loop = parent.getParent();
        final AstNode nextAfterLoop = loop.getNextNode();
        AstNode n;
        for (n = labelInfo.label; n.getNextSibling() == null; n = n.getParent()) {}
        n = n.getNextSibling();
        final boolean isContinue = n == nextAfterLoop || (loop instanceof ForStatement && n.getRole() == ForStatement.ITERATOR_ROLE && n.getParent() == loop);
        if (!isContinue) {
            return false;
        }
        boolean loopNeedsLabel = false;
        for (final AstNode node : loop.getDescendantsAndSelf()) {
            if (node instanceof ContinueStatement && StringUtilities.equals(((ContinueStatement)node).getLabel(), labelInfo.name)) {
                loopNeedsLabel = true;
            }
            else {
                if (!(node instanceof BreakStatement) || !StringUtilities.equals(((BreakStatement)node).getLabel(), labelInfo.name)) {
                    continue;
                }
                loopNeedsLabel = true;
            }
        }
        for (final Stack<AstNode> path : paths) {
            final AstNode start = path.firstElement();
            boolean continueNeedsLabel = false;
            if (start instanceof GotoStatement) {
                for (AstNode node2 = start; node2 != null && node2 != loop; node2 = node2.getParent()) {
                    if (AstNode.isLoop(node2)) {
                        continueNeedsLabel = (loopNeedsLabel = true);
                        break;
                    }
                }
                final int offset = ((GotoStatement)start).getOffset();
                if (continueNeedsLabel) {
                    start.replaceWith(new ContinueStatement(offset, labelInfo.name));
                }
                else {
                    start.replaceWith(new ContinueStatement(offset));
                }
            }
        }
        labelInfo.label.remove();
        if (loopNeedsLabel) {
            loop.replaceWith((Function<? super AstNode, ? extends AstNode>)new Function<AstNode, AstNode>() {
                @Override
                public AstNode apply(final AstNode input) {
                    return new LabeledStatement(labelInfo.name, (Statement)input);
                }
            });
        }
        return true;
    }
    
    private AssessForLoopResult assessForLoop(final AstNode commonAncestor, final List<Stack<AstNode>> paths, final LabelStatement label, final List<GotoStatement> statements) {
        final Set<GotoStatement> gotoStatements = new HashSet<GotoStatement>(statements);
        final Set<GotoStatement> continueStatements = new HashSet<GotoStatement>();
        final Set<ContinueStatement> preexistingContinueStatements = new HashSet<ContinueStatement>();
        boolean labelSeen = false;
        boolean loopEncountered = false;
        for (final Stack<AstNode> path : paths) {
            if (CollectionUtilities.firstOrDefault(path) == label) {
                continue;
            }
            loopEncountered = CollectionUtilities.any(path, new Predicate<AstNode>() {
                @Override
                public boolean test(final AstNode node) {
                    return AstNode.isLoop(node);
                }
            });
            if (loopEncountered) {
                break;
            }
        }
        for (final AstNode node : commonAncestor.getDescendantsAndSelf()) {
            if (node == label) {
                labelSeen = true;
            }
            else if (labelSeen && node instanceof GotoStatement && gotoStatements.contains(node)) {
                continueStatements.add((GotoStatement)node);
            }
            else {
                if (!(node instanceof ContinueStatement) || !StringUtilities.isNullOrEmpty(((ContinueStatement)node).getLabel())) {
                    continue;
                }
                preexistingContinueStatements.add((ContinueStatement)node);
            }
        }
        return new AssessForLoopResult(loopEncountered, continueStatements, preexistingContinueStatements, null);
    }
    
    private static boolean lookAhead(final AstNode start, final Set<AstNode> targets) {
        for (AstNode current = start; current != null && !targets.isEmpty(); current = current.getNextSibling()) {
            if (targets.contains(current)) {
                return true;
            }
        }
        return false;
    }
    
    private static boolean lookBehind(final AstNode start, final Set<AstNode> targets) {
        for (AstNode current = start; current != null && !targets.isEmpty(); current = current.getPreviousSibling()) {
            if (targets.contains(current)) {
                return true;
            }
        }
        return false;
    }
    
    private BlockStatement findLowestCommonAncestorBlock(final List<Stack<AstNode>> paths) {
        if (paths.isEmpty()) {
            return null;
        }
        AstNode current = null;
        BlockStatement match = null;
        final Stack<AstNode> sinceLastMatch = new Stack<AstNode>();
    Label_0220:
        while (true) {
            for (final Stack<AstNode> path : paths) {
                if (path.isEmpty()) {
                    break Label_0220;
                }
                if (current == null) {
                    current = path.peek();
                }
                else {
                    if (path.peek() != current) {
                        break Label_0220;
                    }
                    continue;
                }
            }
            for (final Stack<AstNode> path : paths) {
                path.pop();
            }
            if (current instanceof BlockStatement) {
                sinceLastMatch.clear();
                match = (BlockStatement)current;
            }
            else {
                sinceLastMatch.push(current);
            }
            current = null;
        }
        while (!sinceLastMatch.isEmpty()) {
            for (int i = 0, n = paths.size(); i < n; ++i) {
                paths.get(i).push(sinceLastMatch.peek());
            }
            sinceLastMatch.pop();
        }
        return match;
    }
    
    private Statement findLowestCommonAncestor(final List<Stack<AstNode>> paths) {
        if (paths.isEmpty()) {
            return null;
        }
        AstNode current = null;
        Statement match = null;
        final Stack<AstNode> sinceLastMatch = new Stack<AstNode>();
    Label_0220:
        while (true) {
            for (final Stack<AstNode> path : paths) {
                if (path.isEmpty()) {
                    break Label_0220;
                }
                if (current == null) {
                    current = path.peek();
                }
                else {
                    if (path.peek() != current) {
                        break Label_0220;
                    }
                    continue;
                }
            }
            for (final Stack<AstNode> path : paths) {
                path.pop();
            }
            if (current instanceof Statement) {
                sinceLastMatch.clear();
                match = (Statement)current;
            }
            else {
                sinceLastMatch.push(current);
            }
            current = null;
        }
        while (!sinceLastMatch.isEmpty()) {
            for (int i = 0, n = paths.size(); i < n; ++i) {
                paths.get(i).push(sinceLastMatch.peek());
            }
            sinceLastMatch.pop();
        }
        return match;
    }
    
    private Stack<AstNode> buildPath(final AstNode node) {
        assert node != null;
        final Stack<AstNode> path = new Stack<AstNode>();
        path.push(node);
        for (AstNode current = node; current != null; current = current.getParent()) {
            path.push(current);
            if (current instanceof MethodDeclaration) {
                break;
            }
        }
        return path;
    }
    
    private static final class LabelInfo
    {
        final String name;
        final List<GotoStatement> gotoStatements;
        boolean labelIsLast;
        LabelStatement label;
        AstNode labelTarget;
        LabeledStatement newLabeledStatement;
        
        LabelInfo(final String name) {
            super();
            this.gotoStatements = new ArrayList<GotoStatement>();
            this.name = name;
        }
        
        LabelInfo(final LabelStatement label) {
            super();
            this.gotoStatements = new ArrayList<GotoStatement>();
            this.label = label;
            this.labelTarget = label.getNextSibling();
            this.name = label.getLabel();
        }
    }
    
    private static final class AssessForLoopResult
    {
        final boolean needsLabel;
        final Set<GotoStatement> continueStatements;
        final Set<ContinueStatement> preexistingContinueStatements;
        
        private AssessForLoopResult(final boolean needsLabel, final Set<GotoStatement> continueStatements, final Set<ContinueStatement> preexistingContinueStatements) {
            super();
            this.needsLabel = needsLabel;
            this.continueStatements = continueStatements;
            this.preexistingContinueStatements = preexistingContinueStatements;
        }
    }
}
