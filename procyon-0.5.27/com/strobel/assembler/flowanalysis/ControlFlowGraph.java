package com.strobel.assembler.flowanalysis;

import java.util.regex.*;
import java.util.concurrent.*;
import com.strobel.functions.*;
import com.strobel.decompiler.*;
import java.util.*;
import com.strobel.core.*;
import java.io.*;

public final class ControlFlowGraph
{
    private final List<ControlFlowNode> _nodes;
    private static final Pattern SAFE_PATTERN;
    private static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$assembler$flowanalysis$JumpType;
    
    static {
        SAFE_PATTERN = Pattern.compile("^[\\w\\d]+$");
    }
    
    public final ControlFlowNode getEntryPoint() {
        return this._nodes.get(0);
    }
    
    public final ControlFlowNode getRegularExit() {
        return this._nodes.get(1);
    }
    
    public final ControlFlowNode getExceptionalExit() {
        return this._nodes.get(2);
    }
    
    public final List<ControlFlowNode> getNodes() {
        return this._nodes;
    }
    
    public ControlFlowGraph(final ControlFlowNode... nodes) {
        super();
        this._nodes = ArrayUtilities.asUnmodifiableList((ControlFlowNode[])VerifyArgument.noNullElements((T[])nodes, "nodes"));
        assert nodes.length >= 3;
        assert this.getEntryPoint().getNodeType() == ControlFlowNodeType.EntryPoint;
        assert this.getRegularExit().getNodeType() == ControlFlowNodeType.RegularExit;
        assert this.getExceptionalExit().getNodeType() == ControlFlowNodeType.ExceptionalExit;
    }
    
    public final void resetVisited() {
        for (final ControlFlowNode node : this._nodes) {
            node.setVisited(false);
        }
    }
    
    public final void computeDominance() {
        this.computeDominance(new BooleanBox());
    }
    
    public final void computeDominance(final BooleanBox cancelled) {
        final ControlFlowNode entryPoint = this.getEntryPoint();
        entryPoint.setImmediateDominator(entryPoint);
        final BooleanBox changed = new BooleanBox(true);
        while (changed.get()) {
            changed.set(false);
            this.resetVisited();
            if (cancelled.get()) {
                throw new CancellationException();
            }
            entryPoint.traversePreOrder(new Function<ControlFlowNode, Iterable<ControlFlowNode>>() {
                @Override
                public final Iterable<ControlFlowNode> apply(final ControlFlowNode input) {
                    return input.getSuccessors();
                }
            }, new Block<ControlFlowNode>() {
                @Override
                public final void accept(final ControlFlowNode b) {
                    if (b == entryPoint) {
                        return;
                    }
                    ControlFlowNode newImmediateDominator = null;
                    for (final ControlFlowNode p : b.getPredecessors()) {
                        if (p.isVisited() && p != b) {
                            newImmediateDominator = p;
                            break;
                        }
                    }
                    if (newImmediateDominator == null) {
                        throw new IllegalStateException("Could not compute new immediate dominator!");
                    }
                    for (final ControlFlowNode p : b.getPredecessors()) {
                        if (p != b && p.getImmediateDominator() != null) {
                            newImmediateDominator = ControlFlowGraph.findCommonDominator(p, newImmediateDominator);
                        }
                    }
                    if (b.getImmediateDominator() != newImmediateDominator) {
                        b.setImmediateDominator(newImmediateDominator);
                        changed.set(true);
                    }
                }
            });
        }
        entryPoint.setImmediateDominator(null);
        for (final ControlFlowNode node : this._nodes) {
            final ControlFlowNode immediateDominator = node.getImmediateDominator();
            if (immediateDominator != null) {
                immediateDominator.getDominatorTreeChildren().add(node);
            }
        }
    }
    
    public final void computeDominanceFrontier() {
        this.resetVisited();
        this.getEntryPoint().traversePostOrder(new Function<ControlFlowNode, Iterable<ControlFlowNode>>() {
            @Override
            public final Iterable<ControlFlowNode> apply(final ControlFlowNode input) {
                return input.getDominatorTreeChildren();
            }
        }, new Block<ControlFlowNode>() {
            @Override
            public void accept(final ControlFlowNode n) {
                final Set<ControlFlowNode> dominanceFrontier = n.getDominanceFrontier();
                dominanceFrontier.clear();
                for (final ControlFlowNode s : n.getSuccessors()) {
                    if (s.getImmediateDominator() != n) {
                        dominanceFrontier.add(s);
                    }
                }
                for (final ControlFlowNode child : n.getDominatorTreeChildren()) {
                    for (final ControlFlowNode p : child.getDominanceFrontier()) {
                        if (p.getImmediateDominator() != n) {
                            dominanceFrontier.add(p);
                        }
                    }
                }
            }
        });
    }
    
    public static ControlFlowNode findCommonDominator(final ControlFlowNode a, final ControlFlowNode b) {
        final Set<ControlFlowNode> path1 = new LinkedHashSet<ControlFlowNode>();
        ControlFlowNode node1 = a;
        ControlFlowNode node2 = b;
        while (node1 != null) {
            if (!path1.add(node1)) {
                break;
            }
            node1 = node1.getImmediateDominator();
        }
        while (node2 != null) {
            if (path1.contains(node2)) {
                return node2;
            }
            node2 = node2.getImmediateDominator();
        }
        throw new IllegalStateException("No common dominator found!");
    }
    
    public final void export(final File path) {
        final PlainTextOutput output = new PlainTextOutput();
        output.writeLine("digraph g {");
        output.indent();
        final Set<ControlFlowEdge> edges = new LinkedHashSet<ControlFlowEdge>();
        for (final ControlFlowNode node : this._nodes) {
            output.writeLine("\"%s\" [", nodeName(node));
            output.indent();
            output.writeLine("label = \"%s\\l\"", escapeGraphViz(node.toString()));
            output.writeLine(", shape = \"box\"");
            output.unindent();
            output.writeLine("];");
            edges.addAll(node.getIncoming());
            edges.addAll(node.getOutgoing());
            final ControlFlowNode endFinallyNode = node.getEndFinallyNode();
            if (endFinallyNode != null) {
                output.writeLine("\"%s\" [", nodeName(endFinallyNode));
                output.indent();
                output.writeLine("label = \"%s\"", escapeGraphViz(endFinallyNode.toString()));
                output.writeLine("shape = \"box\"");
                output.unindent();
                output.writeLine("];");
                edges.addAll(endFinallyNode.getIncoming());
                edges.addAll(endFinallyNode.getOutgoing());
            }
        }
        for (final ControlFlowEdge edge : edges) {
            final ControlFlowNode from = edge.getSource();
            final ControlFlowNode to = edge.getTarget();
            output.writeLine("\"%s\" -> \"%s\" [", nodeName(from), nodeName(to));
            output.indent();
            switch ($SWITCH_TABLE$com$strobel$assembler$flowanalysis$JumpType()[edge.getType().ordinal()]) {
                case 1: {
                    break;
                }
                case 3: {
                    output.writeLine("color = \"blue\"");
                    break;
                }
                case 4: {
                    output.writeLine("color = \"red\"");
                    break;
                }
                case 2: {
                    output.writeLine("color = \"gray\"");
                    break;
                }
                default: {
                    output.writeLine("label = \"%s\"", edge.getType());
                    break;
                }
            }
            output.unindent();
            output.writeLine("];");
        }
        output.unindent();
        output.writeLine("}");
        try {
            Throwable loc_2 = null;
            try {
                final OutputStreamWriter out = new FileWriter(path);
                try {
                    out.write(output.toString());
                }
                finally {
                    if (out != null) {
                        out.close();
                    }
                }
            }
            finally {
                if (loc_2 == null) {
                    final Throwable loc_3;
                    loc_2 = loc_3;
                }
                else {
                    final Throwable loc_3;
                    if (loc_2 != loc_3) {
                        loc_2.addSuppressed(loc_3);
                    }
                }
            }
        }
        catch (IOException e) {
            throw ExceptionUtilities.asRuntimeException(e);
        }
    }
    
    private static String nodeName(final ControlFlowNode node) {
        String name = "node" + node.getBlockIndex();
        if (node.getNodeType() == ControlFlowNodeType.EndFinally) {
            name = String.valueOf(name) + "_ef";
        }
        return name;
    }
    
    private static String escapeGraphViz(final String text) {
        return escapeGraphViz(text, false);
    }
    
    private static String escapeGraphViz(final String text, final boolean quote) {
        if (ControlFlowGraph.SAFE_PATTERN.matcher(text).matches()) {
            return quote ? ("\"" + text + "\"") : text;
        }
        return String.valueOf(quote ? "\"" : "") + text.replace("\\", "\\\\").replace("\r", "").replace("\n", "\\l").replace("|", "\\|").replace("{", "\\{").replace("}", "\\}").replace("<", "\\<").replace(">", "\\>").replace("\"", "\\\"") + (quote ? "\"" : "");
    }
    
    static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$assembler$flowanalysis$JumpType() {
        final int[] loc_0 = ControlFlowGraph.$SWITCH_TABLE$com$strobel$assembler$flowanalysis$JumpType;
        if (loc_0 != null) {
            return loc_0;
        }
        final int[] loc_1 = new int[JumpType.values().length];
        try {
            loc_1[JumpType.EndFinally.ordinal()] = 4;
        }
        catch (NoSuchFieldError loc_2) {}
        try {
            loc_1[JumpType.JumpToExceptionHandler.ordinal()] = 2;
        }
        catch (NoSuchFieldError loc_3) {}
        try {
            loc_1[JumpType.LeaveTry.ordinal()] = 3;
        }
        catch (NoSuchFieldError loc_4) {}
        try {
            loc_1[JumpType.Normal.ordinal()] = 1;
        }
        catch (NoSuchFieldError loc_5) {}
        return ControlFlowGraph.$SWITCH_TABLE$com$strobel$assembler$flowanalysis$JumpType = loc_1;
    }
}
