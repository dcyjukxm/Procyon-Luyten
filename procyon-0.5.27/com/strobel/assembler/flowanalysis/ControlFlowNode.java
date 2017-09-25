package com.strobel.assembler.flowanalysis;

import com.strobel.assembler.*;
import com.strobel.assembler.ir.*;
import com.strobel.annotations.*;
import com.strobel.functions.*;
import com.strobel.decompiler.*;
import java.util.*;
import com.strobel.util.*;
import com.strobel.core.*;

public final class ControlFlowNode implements Comparable<ControlFlowNode>
{
    private final int _blockIndex;
    private final int _offset;
    private final ControlFlowNodeType _nodeType;
    private final ControlFlowNode _endFinallyNode;
    private final List<ControlFlowNode> _dominatorTreeChildren;
    private final Set<ControlFlowNode> _dominanceFrontier;
    private final List<ControlFlowEdge> _incoming;
    private final List<ControlFlowEdge> _outgoing;
    private boolean _visited;
    private ControlFlowNode _copyFrom;
    private ControlFlowNode _immediateDominator;
    private Instruction _start;
    private Instruction _end;
    private ExceptionHandler _exceptionHandler;
    private Object _userData;
    public static final Predicate<ControlFlowNode> REACHABLE_PREDICATE;
    private static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$assembler$flowanalysis$ControlFlowNodeType;
    
    static {
        REACHABLE_PREDICATE = new Predicate<ControlFlowNode>() {
            @Override
            public boolean test(final ControlFlowNode node) {
                return node.isReachable();
            }
        };
    }
    
    public ControlFlowNode(final int blockIndex, final int offset, final ControlFlowNodeType nodeType) {
        super();
        this._dominatorTreeChildren = new Collection<ControlFlowNode>();
        this._dominanceFrontier = new LinkedHashSet<ControlFlowNode>();
        this._incoming = new Collection<ControlFlowEdge>();
        this._outgoing = new Collection<ControlFlowEdge>();
        this._blockIndex = blockIndex;
        this._offset = offset;
        this._nodeType = VerifyArgument.notNull(nodeType, "nodeType");
        this._endFinallyNode = null;
        this._start = null;
        this._end = null;
    }
    
    public ControlFlowNode(final int blockIndex, final Instruction start, final Instruction end) {
        super();
        this._dominatorTreeChildren = new Collection<ControlFlowNode>();
        this._dominanceFrontier = new LinkedHashSet<ControlFlowNode>();
        this._incoming = new Collection<ControlFlowEdge>();
        this._outgoing = new Collection<ControlFlowEdge>();
        this._blockIndex = blockIndex;
        this._start = VerifyArgument.notNull(start, "start");
        this._end = VerifyArgument.notNull(end, "end");
        this._offset = start.getOffset();
        this._nodeType = ControlFlowNodeType.Normal;
        this._endFinallyNode = null;
    }
    
    public ControlFlowNode(final int blockIndex, final ExceptionHandler exceptionHandler, final ControlFlowNode endFinallyNode) {
        super();
        this._dominatorTreeChildren = new Collection<ControlFlowNode>();
        this._dominanceFrontier = new LinkedHashSet<ControlFlowNode>();
        this._incoming = new Collection<ControlFlowEdge>();
        this._outgoing = new Collection<ControlFlowEdge>();
        this._blockIndex = blockIndex;
        this._exceptionHandler = VerifyArgument.notNull(exceptionHandler, "exceptionHandler");
        this._nodeType = (exceptionHandler.isFinally() ? ControlFlowNodeType.FinallyHandler : ControlFlowNodeType.CatchHandler);
        this._endFinallyNode = endFinallyNode;
        final InstructionBlock handlerBlock = exceptionHandler.getHandlerBlock();
        this._start = null;
        this._end = null;
        this._offset = handlerBlock.getFirstInstruction().getOffset();
    }
    
    public final int getBlockIndex() {
        return this._blockIndex;
    }
    
    public final int getOffset() {
        return this._offset;
    }
    
    public final ControlFlowNodeType getNodeType() {
        return this._nodeType;
    }
    
    public final ControlFlowNode getEndFinallyNode() {
        return this._endFinallyNode;
    }
    
    public final List<ControlFlowNode> getDominatorTreeChildren() {
        return this._dominatorTreeChildren;
    }
    
    public final Set<ControlFlowNode> getDominanceFrontier() {
        return this._dominanceFrontier;
    }
    
    public final List<ControlFlowEdge> getIncoming() {
        return this._incoming;
    }
    
    public final List<ControlFlowEdge> getOutgoing() {
        return this._outgoing;
    }
    
    public final boolean isVisited() {
        return this._visited;
    }
    
    public final boolean isReachable() {
        return this._immediateDominator != null || this._nodeType == ControlFlowNodeType.EntryPoint;
    }
    
    public final ControlFlowNode getCopyFrom() {
        return this._copyFrom;
    }
    
    public final ControlFlowNode getImmediateDominator() {
        return this._immediateDominator;
    }
    
    public final Instruction getStart() {
        return this._start;
    }
    
    public final Instruction getEnd() {
        return this._end;
    }
    
    public final ExceptionHandler getExceptionHandler() {
        return this._exceptionHandler;
    }
    
    public final Object getUserData() {
        return this._userData;
    }
    
    public final void setVisited(final boolean visited) {
        this._visited = visited;
    }
    
    public final void setCopyFrom(final ControlFlowNode copyFrom) {
        this._copyFrom = copyFrom;
    }
    
    public final void setImmediateDominator(final ControlFlowNode immediateDominator) {
        this._immediateDominator = immediateDominator;
    }
    
    public final void setStart(final Instruction start) {
        this._start = start;
    }
    
    public final void setEnd(final Instruction end) {
        this._end = end;
    }
    
    public final void setExceptionHandler(final ExceptionHandler exceptionHandler) {
        this._exceptionHandler = exceptionHandler;
    }
    
    public final void setUserData(final Object userData) {
        this._userData = userData;
    }
    
    public final boolean succeeds(final ControlFlowNode other) {
        if (other == null) {
            return false;
        }
        for (int i = 0; i < this._incoming.size(); ++i) {
            if (this._incoming.get(i).getSource() == other) {
                return true;
            }
        }
        return false;
    }
    
    public final boolean precedes(final ControlFlowNode other) {
        if (other == null) {
            return false;
        }
        for (int i = 0; i < this._outgoing.size(); ++i) {
            if (this._outgoing.get(i).getTarget() == other) {
                return true;
            }
        }
        return false;
    }
    
    public final Iterable<ControlFlowNode> getPredecessors() {
        return new Iterable<ControlFlowNode>() {
            @NotNull
            @Override
            public final Iterator<ControlFlowNode> iterator() {
                return new PredecessorIterator((PredecessorIterator)null);
            }
        };
    }
    
    public final Iterable<ControlFlowNode> getSuccessors() {
        return new Iterable<ControlFlowNode>() {
            @NotNull
            @Override
            public final Iterator<ControlFlowNode> iterator() {
                return new SuccessorIterator((SuccessorIterator)null);
            }
        };
    }
    
    public final Iterable<Instruction> getInstructions() {
        return new Iterable<Instruction>() {
            @NotNull
            @Override
            public final Iterator<Instruction> iterator() {
                return new InstructionIterator((InstructionIterator)null);
            }
        };
    }
    
    public final void traversePreOrder(final Function<ControlFlowNode, Iterable<ControlFlowNode>> children, final Block<ControlFlowNode> visitAction) {
        if (this._visited) {
            return;
        }
        this._visited = true;
        visitAction.accept(this);
        for (final ControlFlowNode child : children.apply(this)) {
            child.traversePreOrder(children, visitAction);
        }
    }
    
    public final void traversePostOrder(final Function<ControlFlowNode, Iterable<ControlFlowNode>> children, final Block<ControlFlowNode> visitAction) {
        if (this._visited) {
            return;
        }
        this._visited = true;
        for (final ControlFlowNode child : children.apply(this)) {
            child.traversePostOrder(children, visitAction);
        }
        visitAction.accept(this);
    }
    
    public final boolean dominates(final ControlFlowNode node) {
        for (ControlFlowNode current = node; current != null; current = current._immediateDominator) {
            if (current == this) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public final String toString() {
        final PlainTextOutput output = new PlainTextOutput();
        switch ($SWITCH_TABLE$com$strobel$assembler$flowanalysis$ControlFlowNodeType()[this._nodeType.ordinal()]) {
            case 1: {
                output.write("Block #%d", this._blockIndex);
                if (this._start != null) {
                    output.write(": %d to %d", this._start.getOffset(), this._end.getEndOffset());
                    break;
                }
                break;
            }
            case 5:
            case 6: {
                output.write("Block #%d: %s: ", this._blockIndex, this._nodeType);
                DecompilerHelpers.writeExceptionHandler(output, this._exceptionHandler);
                break;
            }
            default: {
                output.write("Block #%d: %s", this._blockIndex, this._nodeType);
                break;
            }
        }
        output.indent();
        if (!this._dominanceFrontier.isEmpty()) {
            output.writeLine();
            output.write("DominanceFrontier: ");
            final int[] blockIndexes = new int[this._dominanceFrontier.size()];
            int i = 0;
            for (final ControlFlowNode node : this._dominanceFrontier) {
                blockIndexes[i++] = node._blockIndex;
            }
            Arrays.sort(blockIndexes);
            output.write(StringUtilities.join(", ", new Iterable<String>() {
                @NotNull
                @Override
                public Iterator<String> iterator() {
                    return new Iterator<String>() {
                        private int _position = 0;
                        
                        @Override
                        public boolean hasNext() {
                            return this._position < blockIndexes.length;
                        }
                        
                        @Override
                        public String next() {
                            if (!this.hasNext()) {
                                throw new NoSuchElementException();
                            }
                            return String.valueOf(blockIndexes[this._position++]);
                        }
                        
                        @Override
                        public void remove() {
                            throw ContractUtils.unreachable();
                        }
                    };
                }
            }));
        }
        for (final Instruction instruction : this.getInstructions()) {
            output.writeLine();
            DecompilerHelpers.writeInstruction(output, instruction);
        }
        final Object userData = this._userData;
        if (userData != null) {
            output.writeLine();
            output.write(String.valueOf(userData));
        }
        output.unindent();
        return output.toString();
    }
    
    @Override
    public int compareTo(final ControlFlowNode o) {
        return Integer.compare(this._blockIndex, o._blockIndex);
    }
    
    static /* synthetic */ List access$1(final ControlFlowNode param_0) {
        return param_0._incoming;
    }
    
    static /* synthetic */ List access$2(final ControlFlowNode param_0) {
        return param_0._outgoing;
    }
    
    static /* synthetic */ Instruction access$3(final ControlFlowNode param_0) {
        return param_0._start;
    }
    
    static /* synthetic */ Instruction access$4(final ControlFlowNode param_0) {
        return param_0._end;
    }
    
    static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$assembler$flowanalysis$ControlFlowNodeType() {
        final int[] loc_0 = ControlFlowNode.$SWITCH_TABLE$com$strobel$assembler$flowanalysis$ControlFlowNodeType;
        if (loc_0 != null) {
            return loc_0;
        }
        final int[] loc_1 = new int[ControlFlowNodeType.values().length];
        try {
            loc_1[ControlFlowNodeType.CatchHandler.ordinal()] = 5;
        }
        catch (NoSuchFieldError loc_2) {}
        try {
            loc_1[ControlFlowNodeType.EndFinally.ordinal()] = 7;
        }
        catch (NoSuchFieldError loc_3) {}
        try {
            loc_1[ControlFlowNodeType.EntryPoint.ordinal()] = 2;
        }
        catch (NoSuchFieldError loc_4) {}
        try {
            loc_1[ControlFlowNodeType.ExceptionalExit.ordinal()] = 4;
        }
        catch (NoSuchFieldError loc_5) {}
        try {
            loc_1[ControlFlowNodeType.FinallyHandler.ordinal()] = 6;
        }
        catch (NoSuchFieldError loc_6) {}
        try {
            loc_1[ControlFlowNodeType.Normal.ordinal()] = 1;
        }
        catch (NoSuchFieldError loc_7) {}
        try {
            loc_1[ControlFlowNodeType.RegularExit.ordinal()] = 3;
        }
        catch (NoSuchFieldError loc_8) {}
        return ControlFlowNode.$SWITCH_TABLE$com$strobel$assembler$flowanalysis$ControlFlowNodeType = loc_1;
    }
    
    private final class InstructionIterator implements Iterator<Instruction>
    {
        private Instruction _next;
        
        private InstructionIterator() {
            super();
            this._next = ControlFlowNode.access$3(ControlFlowNode.this);
        }
        
        @Override
        public final boolean hasNext() {
            return this._next != null && this._next.getOffset() <= ControlFlowNode.access$4(ControlFlowNode.this).getOffset();
        }
        
        @Override
        public final Instruction next() {
            final Instruction next = this._next;
            if (next == null || next.getOffset() > ControlFlowNode.access$4(ControlFlowNode.this).getOffset()) {
                throw new NoSuchElementException();
            }
            this._next = next.getNext();
            return next;
        }
        
        @Override
        public final void remove() {
            throw ContractUtils.unsupported();
        }
    }
    
    private final class PredecessorIterator implements Iterator<ControlFlowNode>
    {
        private Iterator<ControlFlowEdge> _innerIterator;
        
        @Override
        public final boolean hasNext() {
            if (this._innerIterator == null) {
                this._innerIterator = (Iterator<ControlFlowEdge>)ControlFlowNode.access$1(ControlFlowNode.this).listIterator();
            }
            return this._innerIterator.hasNext();
        }
        
        @Override
        public final ControlFlowNode next() {
            if (this._innerIterator == null) {
                this._innerIterator = (Iterator<ControlFlowEdge>)ControlFlowNode.access$1(ControlFlowNode.this).listIterator();
            }
            return this._innerIterator.next().getSource();
        }
        
        @Override
        public final void remove() {
            throw ContractUtils.unsupported();
        }
    }
    
    private final class SuccessorIterator implements Iterator<ControlFlowNode>
    {
        private Iterator<ControlFlowEdge> _innerIterator;
        
        @Override
        public final boolean hasNext() {
            if (this._innerIterator == null) {
                this._innerIterator = (Iterator<ControlFlowEdge>)ControlFlowNode.access$2(ControlFlowNode.this).listIterator();
            }
            return this._innerIterator.hasNext();
        }
        
        @Override
        public final ControlFlowNode next() {
            if (this._innerIterator == null) {
                this._innerIterator = (Iterator<ControlFlowEdge>)ControlFlowNode.access$2(ControlFlowNode.this).listIterator();
            }
            return this._innerIterator.next().getTarget();
        }
        
        @Override
        public final void remove() {
            throw ContractUtils.unsupported();
        }
    }
}
