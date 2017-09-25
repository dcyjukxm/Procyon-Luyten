package com.strobel.decompiler.languages.java.ast;

import com.strobel.componentmodel.*;
import java.lang.reflect.*;
import java.util.*;
import com.strobel.util.*;
import com.strobel.annotations.*;
import com.strobel.functions.*;
import com.strobel.decompiler.utilities.*;
import com.strobel.decompiler.patterns.*;
import com.strobel.decompiler.languages.*;
import com.strobel.decompiler.languages.java.*;
import com.strobel.decompiler.*;
import com.strobel.core.*;

public abstract class AstNode extends Freezable implements INode, UserDataStore, Cloneable
{
    static final Role<AstNode> ROOT_ROLE;
    static final int ROLE_INDEX_MASK = 511;
    static final int FROZEN_BIT = 512;
    protected static final int AST_NODE_USED_FLAGS = 10;
    protected int flags;
    private AstNode _parent;
    private AstNode _previousSibling;
    private AstNode _nextSibling;
    private AstNode _firstChild;
    private AstNode _lastChild;
    public static final AstNode NULL;
    private final UserDataStore _dataStore;
    
    static {
        ROOT_ROLE = new Role<AstNode>("Root", AstNode.class);
        NULL = new NullAstNode(null);
    }
    
    protected AstNode() {
        super();
        this.flags = AstNode.ROOT_ROLE.getIndex();
        this._dataStore = new UserDataStoreBase();
        if (this.isNull()) {
            this.freeze();
        }
    }
    
    protected static boolean matchString(final String pattern, final String text) {
        return Pattern.matchString(pattern, text);
    }
    
    public static boolean isLoop(final AstNode statement) {
        return statement instanceof ForStatement || statement instanceof ForEachStatement || statement instanceof WhileStatement || statement instanceof DoWhileStatement;
    }
    
    public static boolean isUnconditionalBranch(final AstNode statement) {
        return statement instanceof GotoStatement || statement instanceof ReturnStatement || statement instanceof BreakStatement || statement instanceof ContinueStatement;
    }
    
    final void setRoleUnsafe(final Role<?> role) {
        this.flags = ((this.flags & 0xFFFFFE00) | role.getIndex());
    }
    
    public abstract <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> param_0, final T param_1);
    
    public AstNode clone() {
        try {
            final AstNode clone = (AstNode)super.clone();
            clone._parent = null;
            clone._firstChild = null;
            clone._lastChild = null;
            clone._previousSibling = null;
            clone._nextSibling = null;
            final AstNode loc_0 = clone;
            loc_0.flags &= 0xFFFFFDFF;
            for (final Key<?> key : Keys.ALL_KEYS) {
                copyKey(this, clone, key);
            }
            for (AstNode current = this._firstChild; current != null; current = current._nextSibling) {
                clone.addChildUnsafe(current.clone(), current.getRole());
            }
            return clone;
        }
        catch (CloneNotSupportedException e) {
            throw new UndeclaredThrowableException(e);
        }
    }
    
    public void copyUserDataFrom(final AstNode source) {
        VerifyArgument.notNull(source, "source");
        for (final Key<?> key : Keys.ALL_KEYS) {
            copyKey(source, this, key);
        }
    }
    
    private static <T> void copyKey(final AstNode source, final AstNode target, final Key<T> key) {
        target._dataStore.putUserDataIfAbsent(key, (T)source._dataStore.getUserData(key));
    }
    
    public final AstNode getParent() {
        return this._parent;
    }
    
    public final AstNode getPreviousSibling() {
        return this._previousSibling;
    }
    
    public final AstNode getLastChild() {
        return this._lastChild;
    }
    
    @Override
    public final AstNode getFirstChild() {
        return this._firstChild;
    }
    
    @Override
    public final AstNode getNextSibling() {
        return this._nextSibling;
    }
    
    public final <T extends AstNode> T getPreviousSibling(final Role<T> role) {
        for (AstNode current = this._previousSibling; current != null; current = current.getPreviousSibling()) {
            if (current.getRole() == role) {
                return (T)current;
            }
        }
        return null;
    }
    
    public final <T extends AstNode> T getNextSibling(final Role<T> role) {
        for (AstNode current = this._nextSibling; current != null; current = current.getNextSibling()) {
            if (current.getRole() == role) {
                return (T)current;
            }
        }
        return null;
    }
    
    public final boolean hasChildren() {
        return this._firstChild != null;
    }
    
    public final AstNode getNextNode() {
        final AstNode nextSibling = this.getNextSibling();
        if (nextSibling != null) {
            return nextSibling;
        }
        final AstNode parent = this.getParent();
        if (parent != null) {
            return parent.getNextNode();
        }
        return null;
    }
    
    public final AstNode getPreviousNode() {
        final AstNode previousSibling = this.getPreviousSibling();
        if (previousSibling != null) {
            return previousSibling;
        }
        final AstNode parent = this.getParent();
        if (parent != null) {
            return parent.getPreviousNode();
        }
        return null;
    }
    
    public final Iterable<AstNode> getChildren() {
        return new Iterable<AstNode>() {
            final /* synthetic */ AstNode this$0;
            
            @NotNull
            @Override
            public final Iterator<AstNode> iterator() {
                return new Iterator<AstNode>() {
                    AstNode next = AstNode.access$3(AstNode$1.access$0(Iterable.this));
                    
                    @Override
                    public final boolean hasNext() {
                        return this.next != null;
                    }
                    
                    @Override
                    public final AstNode next() {
                        final AstNode result = this.next;
                        if (result == null) {
                            throw new NoSuchElementException();
                        }
                        this.next = AstNode.access$4(result);
                        return result;
                    }
                    
                    @Override
                    public final void remove() {
                        throw ContractUtils.unsupported();
                    }
                };
            }
            
            static /* synthetic */ AstNode access$0(final AstNode$1 param_0) {
                return param_0.this$0;
            }
        };
    }
    
    public final boolean isAncestorOf(final AstNode node) {
        for (AstNode n = node; n != null; n = n._parent) {
            if (n == this) {
                return true;
            }
        }
        return false;
    }
    
    public final boolean isDescendantOf(final AstNode node) {
        return node != null && node.isAncestorOf(this);
    }
    
    public final <T extends AstNode> Iterable<T> getAncestors(@NotNull final Class<T> type) {
        VerifyArgument.notNull(type, "type");
        return CollectionUtilities.ofType(this.getAncestors(), type);
    }
    
    public final Iterable<AstNode> getAncestors() {
        return new Iterable<AstNode>() {
            final /* synthetic */ AstNode this$0;
            
            @NotNull
            @Override
            public final Iterator<AstNode> iterator() {
                return new Iterator<AstNode>() {
                    AstNode next = AstNode.access$5(AstNode$2.access$0(Iterable.this));
                    
                    @Override
                    public final boolean hasNext() {
                        return this.next != null;
                    }
                    
                    @Override
                    public final AstNode next() {
                        final AstNode result = this.next;
                        if (result == null) {
                            throw new NoSuchElementException();
                        }
                        this.next = AstNode.access$5(result);
                        return result;
                    }
                    
                    @Override
                    public final void remove() {
                        throw ContractUtils.unsupported();
                    }
                };
            }
            
            static /* synthetic */ AstNode access$0(final AstNode$2 param_0) {
                return param_0.this$0;
            }
        };
    }
    
    public final Iterable<AstNode> getAncestorsAndSelf() {
        return new Iterable<AstNode>() {
            final /* synthetic */ AstNode this$0;
            
            @NotNull
            @Override
            public final Iterator<AstNode> iterator() {
                return new Iterator<AstNode>() {
                    AstNode next = AstNode$3.access$0(Iterable.this);
                    
                    @Override
                    public final boolean hasNext() {
                        return this.next != null;
                    }
                    
                    @Override
                    public final AstNode next() {
                        final AstNode result = this.next;
                        if (result == null) {
                            throw new NoSuchElementException();
                        }
                        this.next = AstNode.access$5(result);
                        return result;
                    }
                    
                    @Override
                    public final void remove() {
                        throw ContractUtils.unsupported();
                    }
                };
            }
            
            static /* synthetic */ AstNode access$0(final AstNode$3 param_0) {
                return param_0.this$0;
            }
        };
    }
    
    public final Iterable<AstNode> getDescendants() {
        return TreeTraversal.preOrder(this.getChildren(), new Function<AstNode, Iterable<AstNode>>() {
            @Override
            public Iterable<AstNode> apply(final AstNode n) {
                return n.getChildren();
            }
        });
    }
    
    public final Iterable<AstNode> getDescendantsAndSelf() {
        return TreeTraversal.preOrder(this, new Function<AstNode, Iterable<AstNode>>() {
            @Override
            public Iterable<AstNode> apply(final AstNode n) {
                return n.getChildren();
            }
        });
    }
    
    @NotNull
    public final <T extends AstNode> T getChildByRole(final Role<T> role) {
        VerifyArgument.notNull(role, "role");
        final int roleIndex = role.getIndex();
        for (AstNode current = this._firstChild; current != null; current = current._nextSibling) {
            if ((current.flags & 0x1FF) == roleIndex) {
                return (T)current;
            }
        }
        return role.getNullObject();
    }
    
    @NotNull
    public final <T extends AstNode> AstNodeCollection<T> getChildrenByRole(final Role<T> role) {
        return new AstNodeCollection<T>(this, role);
    }
    
    protected final <T extends AstNode> void setChildByRole(final Role<T> role, final T newChild) {
        final T oldChild = this.getChildByRole(role);
        if (oldChild.isNull()) {
            this.addChild(newChild, role);
        }
        else {
            oldChild.replaceWith(newChild);
        }
    }
    
    public final <T extends AstNode> T getParent(final Class<T> nodeType) {
        for (final AstNode node : this.getAncestors()) {
            if (nodeType.isInstance(node)) {
                return (T)node;
            }
        }
        return null;
    }
    
    public final <T extends AstNode> void addChild(final T child, final Role<T> role) {
        VerifyArgument.notNull(role, "role");
        if (child == null || child.isNull()) {
            return;
        }
        this.verifyNotFrozen();
        if (child._parent != null) {
            throw new IllegalArgumentException("Node belongs to another tree.");
        }
        if (child.isFrozen()) {
            throw new IllegalArgumentException("Cannot add a frozen node.");
        }
        this.addChildUnsafe(child, role);
    }
    
    final void addChildUnsafe(final AstNode child, final Role<?> role) {
        child._parent = this;
        child.setRoleUnsafe(role);
        if (this._firstChild == null) {
            this._firstChild = child;
            this._lastChild = child;
        }
        else {
            this._lastChild._nextSibling = child;
            child._previousSibling = this._lastChild;
            this._lastChild = child;
        }
    }
    
    @SafeVarargs
    public final <T extends AstNode> void insertChildrenBefore(final AstNode nextSibling, final Role<T> role, final T... children) {
        VerifyArgument.notNull(children, "children");
        for (final T child : children) {
            this.insertChildBefore(nextSibling, child, role);
        }
    }
    
    public final <T extends AstNode> void insertChildBefore(final AstNode nextSibling, final T child, final Role<T> role) {
        VerifyArgument.notNull(role, "role");
        if (nextSibling == null || nextSibling.isNull()) {
            this.addChild(child, role);
            return;
        }
        if (child == null || child.isNull()) {
            return;
        }
        this.verifyNotFrozen();
        if (child._parent != null) {
            throw new IllegalArgumentException("Node belongs to another tree.");
        }
        if (child.isFrozen()) {
            throw new IllegalArgumentException("Cannot add a frozen node.");
        }
        if (nextSibling._parent != this) {
            throw new IllegalArgumentException("Next sibling is not a child of this node.");
        }
        this.insertChildBeforeUnsafe(nextSibling, child, role);
    }
    
    @SafeVarargs
    public final <T extends AstNode> void insertChildrenAfter(final AstNode nextSibling, final Role<T> role, final T... children) {
        VerifyArgument.notNull(children, "children");
        for (final T child : children) {
            this.insertChildAfter(nextSibling, child, role);
        }
    }
    
    public final <T extends AstNode> void insertChildAfter(final AstNode previousSibling, final T child, final Role<T> role) {
        this.insertChildBefore((previousSibling == null || previousSibling.isNull()) ? this._firstChild : previousSibling._nextSibling, (AstNode)child, role);
    }
    
    final void insertChildBeforeUnsafe(final AstNode nextSibling, final AstNode child, final Role<?> role) {
        child._parent = this;
        child.setRole(role);
        child._nextSibling = nextSibling;
        child._previousSibling = nextSibling._previousSibling;
        if (nextSibling._previousSibling != null) {
            assert nextSibling._previousSibling._nextSibling == nextSibling;
            nextSibling._previousSibling._nextSibling = child;
        }
        else {
            assert this._firstChild == nextSibling;
            this._firstChild = child;
        }
        nextSibling._previousSibling = child;
    }
    
    public final void remove() {
        if (this._parent == null) {
            return;
        }
        this.verifyNotFrozen();
        if (this._previousSibling != null) {
            assert this._previousSibling._nextSibling == this;
            this._previousSibling._nextSibling = this._nextSibling;
        }
        else {
            assert this._parent._firstChild == this;
            this._parent._firstChild = this._nextSibling;
        }
        if (this._nextSibling != null) {
            assert this._nextSibling._previousSibling == this;
            this._nextSibling._previousSibling = this._previousSibling;
        }
        else {
            assert this._parent._lastChild == this;
            this._parent._lastChild = this._previousSibling;
        }
        this._parent = null;
        this._previousSibling = null;
        this._nextSibling = null;
    }
    
    public final void replaceWith(final AstNode newNode) {
        if (newNode == null || newNode.isNull()) {
            this.remove();
            return;
        }
        if (newNode == this) {
            return;
        }
        if (this._parent == null) {
            throw new IllegalStateException(this.isNull() ? "Cannot replace null nodes." : "Cannot replace the root node.");
        }
        this.verifyNotFrozen();
        final Role role = this.getRole();
        if (!role.isValid(newNode)) {
            throw new IllegalArgumentException(String.format("The new node '%s' is not valid for role '%s'.", newNode.getClass().getName(), role.toString()));
        }
        if (newNode._parent != null) {
            if (!CollectionUtilities.contains(newNode.getAncestors(), this)) {
                throw new IllegalArgumentException("Node belongs to another tree.");
            }
            newNode.remove();
        }
        if (newNode.isFrozen()) {
            throw new IllegalArgumentException("Node belongs to another tree.");
        }
        newNode._parent = this._parent;
        newNode.setRoleUnsafe(role);
        newNode._previousSibling = this._previousSibling;
        newNode._nextSibling = this._nextSibling;
        if (this._parent != null) {
            if (this._previousSibling != null) {
                assert this._previousSibling._nextSibling == this;
                this._previousSibling._nextSibling = newNode;
            }
            else {
                assert this._parent._firstChild == this;
                this._parent._firstChild = newNode;
            }
            if (this._nextSibling != null) {
                assert this._nextSibling._previousSibling == this;
                this._nextSibling._previousSibling = newNode;
            }
            else {
                assert this._parent._lastChild == this;
                this._parent._lastChild = newNode;
            }
            this._parent = null;
            this._previousSibling = null;
            this._nextSibling = null;
        }
    }
    
    public final <T extends AstNode> T replaceWith(final Function<? super AstNode, ? extends T> replaceFunction) {
        VerifyArgument.notNull(replaceFunction, "replaceFunction");
        if (this._parent == null) {
            throw new IllegalStateException(this.isNull() ? "Cannot replace null nodes." : "Cannot replace the root node.");
        }
        final AstNode oldParent = this._parent;
        final AstNode oldSuccessor = this._nextSibling;
        final Role oldRole = this.getRole();
        this.remove();
        final T replacement = (T)replaceFunction.apply(this);
        if (oldSuccessor != null && oldSuccessor._parent != oldParent) {
            throw new IllegalStateException("Replace function changed next sibling of node being replaced.");
        }
        if (replacement != null && !replacement.isNull()) {
            if (replacement.getParent() != null) {
                throw new IllegalStateException("replace function must return the root of a tree");
            }
            if (!oldRole.isValid(replacement)) {
                throw new IllegalStateException(String.format("The new node '%s' is not valid in the role %s.", replacement.getClass().getSimpleName(), oldRole));
            }
            if (oldSuccessor != null) {
                oldParent.insertChildBeforeUnsafe(oldSuccessor, replacement, oldRole);
            }
            else {
                oldParent.addChildUnsafe(replacement, oldRole);
            }
        }
        return replacement;
    }
    
    @Override
    protected void freezeCore() {
        for (AstNode child = this._firstChild; child != null; child = child._nextSibling) {
            child.freezeIfUnfrozen();
        }
        this.flags |= 0x200;
    }
    
    public abstract NodeType getNodeType();
    
    public boolean isReference() {
        return false;
    }
    
    @Override
    public boolean isNull() {
        return false;
    }
    
    @Override
    public final Role getRole() {
        return Role.get(this.flags & 0x1FF);
    }
    
    public final void setRole(final Role<?> role) {
        VerifyArgument.notNull(role, "role");
        if (!role.isValid(this)) {
            throw new IllegalArgumentException("This node is not valid for the specified role.");
        }
        this.verifyNotFrozen();
        this.setRoleUnsafe(role);
    }
    
    @Override
    public abstract boolean matches(final INode param_0, final Match param_1);
    
    @Override
    public boolean matchesCollection(final Role role, final INode position, final Match match, final BacktrackingInfo backtrackingInfo) {
        return (position == null || position instanceof AstNode) && this.matches(position, match);
    }
    
    @Override
    public final Match match(final INode other) {
        final Match match = Match.createNew();
        return this.matches(other, match) ? match : Match.failure();
    }
    
    @Override
    public final boolean matches(final INode other) {
        return this.matches(other, Match.createNew());
    }
    
    public static AstNode forPattern(final Pattern pattern) {
        return new PatternPlaceholder(VerifyArgument.notNull(pattern, "pattern"));
    }
    
    public TextLocation getStartLocation() {
        final AstNode child = this._firstChild;
        return (child != null) ? child.getStartLocation() : TextLocation.EMPTY;
    }
    
    public TextLocation getEndLocation() {
        final AstNode child = this._lastChild;
        return (child != null) ? child.getEndLocation() : TextLocation.EMPTY;
    }
    
    public Region getRegion() {
        return new Region(this.getStartLocation(), this.getEndLocation());
    }
    
    public final boolean contains(final int line, final int column) {
        return this.contains(new TextLocation(line, column));
    }
    
    public final boolean contains(final TextLocation location) {
        if (location == null || location.isEmpty()) {
            return false;
        }
        final TextLocation startLocation = this.getStartLocation();
        final TextLocation endLocation = this.getEndLocation();
        return startLocation != null && endLocation != null && location.compareTo(startLocation) >= 0 && location.compareTo(endLocation) < 0;
    }
    
    public final boolean isInside(final int line, final int column) {
        return this.isInside(new TextLocation(line, column));
    }
    
    public final boolean isInside(final TextLocation location) {
        if (location == null || location.isEmpty()) {
            return false;
        }
        final TextLocation startLocation = this.getStartLocation();
        final TextLocation endLocation = this.getEndLocation();
        return startLocation != null && endLocation != null && location.compareTo(startLocation) >= 0 && location.compareTo(endLocation) <= 0;
    }
    
    public String getText() {
        return this.getText(null);
    }
    
    public String getText(final JavaFormattingOptions options) {
        if (this.isNull()) {
            return "";
        }
        final ITextOutput output = new PlainTextOutput();
        final JavaOutputVisitor visitor = new JavaOutputVisitor(output, DecompilerSettings.javaDefaults());
        this.acceptVisitor((IAstVisitor<? super Object, ?>)visitor, (Object)null);
        return output.toString();
    }
    
    String debugToString() {
        if (this.isNull()) {
            return "Null";
        }
        final String text = StringUtilities.trimRight(this.getText());
        return (text.length() > 1000) ? (String.valueOf(text.substring(0, 97)) + "...") : text;
    }
    
    @Override
    public String toString() {
        return this.debugToString();
    }
    
    @Override
    public final <T> T getUserData(final Key<T> key) {
        return this._dataStore.getUserData(key);
    }
    
    @Override
    public final <T> void putUserData(final Key<T> key, final T value) {
        this._dataStore.putUserData(key, value);
    }
    
    @Override
    public final <T> T putUserDataIfAbsent(final Key<T> key, final T value) {
        return this._dataStore.putUserDataIfAbsent(key, value);
    }
    
    @Override
    public final <T> boolean replace(final Key<T> key, final T oldValue, final T newValue) {
        return this._dataStore.replace(key, oldValue, newValue);
    }
    
    static /* synthetic */ AstNode access$3(final AstNode param_0) {
        return param_0._firstChild;
    }
    
    static /* synthetic */ AstNode access$4(final AstNode param_0) {
        return param_0._nextSibling;
    }
    
    static /* synthetic */ AstNode access$5(final AstNode param_0) {
        return param_0._parent;
    }
    
    private static final class NullAstNode extends AstNode
    {
        @Override
        public boolean isNull() {
            return true;
        }
        
        @Override
        public boolean matches(final INode other, final Match match) {
            return other == null || other.isNull();
        }
        
        @Override
        public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
            return null;
        }
        
        @Override
        public NodeType getNodeType() {
            return NodeType.UNKNOWN;
        }
    }
    
    private static final class PatternPlaceholder extends AstNode
    {
        final Pattern child;
        
        PatternPlaceholder(final Pattern child) {
            super();
            this.child = child;
        }
        
        @Override
        public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
            return (R)visitor.visitPatternPlaceholder((AstNode)this, this.child, (Object)data);
        }
        
        @Override
        public final NodeType getNodeType() {
            return NodeType.PATTERN;
        }
        
        @Override
        public boolean matches(final INode other, final Match match) {
            return this.child.matches(other, match);
        }
        
        @Override
        public boolean matchesCollection(final Role role, final INode position, final Match match, final BacktrackingInfo backtrackingInfo) {
            return this.child.matchesCollection(role, position, match, backtrackingInfo);
        }
    }
}
