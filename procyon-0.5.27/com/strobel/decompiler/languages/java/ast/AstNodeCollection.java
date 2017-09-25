package com.strobel.decompiler.languages.java.ast;

import com.strobel.util.*;
import com.strobel.decompiler.patterns.*;
import com.strobel.core.*;
import java.util.*;

public final class AstNodeCollection<T extends AstNode> extends AbstractCollection<T>
{
    private final AstNode _node;
    private final Role<T> _role;
    
    public AstNodeCollection(final AstNode node, final Role<T> role) {
        super();
        this._node = VerifyArgument.notNull(node, "node");
        this._role = VerifyArgument.notNull(role, "role");
    }
    
    @Override
    public int size() {
        int count = 0;
        for (AstNode current = this._node.getFirstChild(); current != null; current = current.getNextSibling()) {
            if (current.getRole() == this._role) {
                ++count;
            }
        }
        return count;
    }
    
    @Override
    public boolean isEmpty() {
        for (AstNode current = this._node.getFirstChild(); current != null; current = current.getNextSibling()) {
            if (current.getRole() == this._role) {
                return false;
            }
        }
        return true;
    }
    
    public boolean hasSingleElement() {
        boolean hasElement = false;
        for (AstNode current = this._node.getFirstChild(); current != null; current = current.getNextSibling()) {
            if (current.getRole() == this._role) {
                if (hasElement) {
                    return false;
                }
                hasElement = true;
            }
        }
        return hasElement;
    }
    
    @Override
    public boolean contains(final Object o) {
        return o instanceof AstNode && ((AstNode)o).getParent() == this._node && ((AstNode)o).getRole() == this._role;
    }
    
    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            AstNode position = AstNodeCollection.access$1(AstNodeCollection.this).getFirstChild();
            T next;
            
            private T selectNext() {
                if (this.next != null) {
                    return this.next;
                }
                while (this.position != null) {
                    if (this.position.getRole() == AstNodeCollection.access$2(AstNodeCollection.this)) {
                        this.next = (T)this.position;
                        this.position = this.position.getNextSibling();
                        return this.next;
                    }
                    this.position = this.position.getNextSibling();
                }
                return null;
            }
            
            @Override
            public boolean hasNext() {
                return this.selectNext() != null;
            }
            
            @Override
            public T next() {
                final T next = this.selectNext();
                if (next == null) {
                    throw new NoSuchElementException();
                }
                this.next = null;
                return next;
            }
            
            @Override
            public void remove() {
                throw ContractUtils.unsupported();
            }
        };
    }
    
    @Override
    public Object[] toArray() {
        return this.toArray(new Object[this.size()]);
    }
    
    @Override
    public <T1> T1[] toArray(final T1[] a) {
        int index = 0;
        Object[] destination = a;
        for (final T child : this) {
            if (index >= destination.length) {
                destination = Arrays.copyOf(destination, this.size());
            }
            destination[index++] = child;
        }
        return (T1[])destination;
    }
    
    @Override
    public boolean add(final T t) {
        this._node.addChild(t, this._role);
        return true;
    }
    
    @Override
    public boolean remove(final Object o) {
        if (this.contains(o)) {
            ((AstNode)o).remove();
            return true;
        }
        return false;
    }
    
    @Override
    public void clear() {
        for (final T item : this) {
            item.remove();
        }
    }
    
    public void moveTo(final Collection<T> destination) {
        VerifyArgument.notNull(destination, "destination");
        for (final T node : this) {
            node.remove();
            destination.add(node);
        }
    }
    
    public T firstOrNullObject() {
        return this.firstOrNullObject(null);
    }
    
    public T firstOrNullObject(final Predicate<T> predicate) {
        for (final T item : this) {
            if (predicate == null || predicate.test(item)) {
                return item;
            }
        }
        return this._role.getNullObject();
    }
    
    public T lastOrNullObject() {
        return this.lastOrNullObject(null);
    }
    
    public T lastOrNullObject(final Predicate<T> predicate) {
        T result = this._role.getNullObject();
        for (final T item : this) {
            if (predicate == null || predicate.test(item)) {
                result = item;
            }
        }
        return result;
    }
    
    public void acceptVisitor(final IAstVisitor<? super T, ?> visitor) {
        AstNode next;
        for (AstNode current = this._node.getFirstChild(); current != null; current = next) {
            assert current.getParent() == this._node;
            next = current.getNextSibling();
            if (current.getRole() == this._role) {
                current.acceptVisitor(visitor, (Object)null);
            }
        }
    }
    
    public final boolean matches(final AstNodeCollection<T> other, final Match match) {
        return Pattern.matchesCollection(this._role, this._node.getFirstChild(), VerifyArgument.notNull(other, "other")._node.getFirstChild(), VerifyArgument.notNull(match, "match"));
    }
    
    @Override
    public int hashCode() {
        return this._node.hashCode() ^ this._role.hashCode();
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof AstNodeCollection) {
            final AstNodeCollection<?> other = (AstNodeCollection<?>)obj;
            return other._node == this._node && other._role == this._role;
        }
        return false;
    }
    
    public final void replaceWith(final Iterable<T> nodes) {
        final List<T> nodeList = (nodes != null) ? CollectionUtilities.toList(nodes) : null;
        this.clear();
        if (nodeList == null) {
            return;
        }
        for (final T node : nodeList) {
            this.add(node);
        }
    }
    
    public final void insertAfter(final T existingItem, final T newItem) {
        this._node.insertChildAfter(existingItem, newItem, this._role);
    }
    
    public final void insertBefore(final T existingItem, final T newItem) {
        this._node.insertChildBefore(existingItem, newItem, this._role);
    }
    
    static /* synthetic */ AstNode access$1(final AstNodeCollection param_0) {
        return param_0._node;
    }
    
    static /* synthetic */ Role access$2(final AstNodeCollection param_0) {
        return param_0._role;
    }
}
