package com.strobel.assembler;

import com.strobel.core.*;
import java.util.*;

public class Collection<E> extends AbstractList<E> implements IFreezable
{
    private final ArrayList<E> _items;
    private boolean _isFrozen;
    
    public Collection() {
        super();
        this._items = new ArrayList<E>();
    }
    
    @Override
    public final int size() {
        return this._items.size();
    }
    
    @Override
    public final E get(final int index) {
        return this._items.get(index);
    }
    
    @Override
    public final boolean add(final E e) {
        this.verifyNotFrozen();
        this.add(this.size(), e);
        return true;
    }
    
    @Override
    public final E set(final int index, final E element) {
        this.verifyNotFrozen();
        VerifyArgument.notNull(element, "element");
        this.beforeSet(index, element);
        return this._items.set(index, element);
    }
    
    @Override
    public void add(final int index, final E element) {
        this.verifyNotFrozen();
        VerifyArgument.notNull(element, "element");
        this.addCore(index, element);
    }
    
    protected final void addCore(final int index, final E element) {
        final boolean append = index == this.size();
        this._items.add(index, element);
        this.afterAdd(index, element, append);
    }
    
    @Override
    public final E remove(final int index) {
        this.verifyNotFrozen();
        final E e = this._items.remove(index);
        if (e != null) {
            this.afterRemove(index, e);
        }
        return e;
    }
    
    @Override
    public final void clear() {
        this.verifyNotFrozen();
        this.beforeClear();
        this._items.clear();
    }
    
    @Override
    public final boolean remove(final Object o) {
        this.verifyNotFrozen();
        final int index = this._items.indexOf(o);
        return index >= 0 && this.remove(index) != null;
    }
    
    protected void afterAdd(final int index, final E e, final boolean appended) {
    }
    
    protected void beforeSet(final int index, final E e) {
    }
    
    protected void afterRemove(final int index, final E e) {
    }
    
    protected void beforeClear() {
    }
    
    @Override
    public boolean canFreeze() {
        return !this.isFrozen();
    }
    
    @Override
    public final boolean isFrozen() {
        return this._isFrozen;
    }
    
    @Override
    public final void freeze() {
        this.freeze(true);
    }
    
    public final void freeze(final boolean freezeContents) {
        if (!this.canFreeze()) {
            throw new IllegalStateException("Collection cannot be frozen.  Be sure to check canFreeze() before calling freeze(), or use the tryFreeze() method instead.");
        }
        this.freezeCore(freezeContents);
        this._isFrozen = true;
    }
    
    protected void freezeCore(final boolean freezeContents) {
        if (freezeContents) {
            for (final E item : this._items) {
                if (item instanceof IFreezable) {
                    ((IFreezable)item).freezeIfUnfrozen();
                }
            }
        }
    }
    
    protected final void verifyNotFrozen() {
        if (this.isFrozen()) {
            throw new IllegalStateException("Frozen collections cannot be modified.");
        }
    }
    
    protected final void verifyFrozen() {
        if (!this.isFrozen()) {
            throw new IllegalStateException("Collection must be frozen before performing this operation.");
        }
    }
    
    @Override
    public final boolean tryFreeze() {
        if (!this.canFreeze()) {
            return false;
        }
        try {
            this.freeze();
            return true;
        }
        catch (Throwable t) {
            return false;
        }
    }
    
    @Override
    public final void freezeIfUnfrozen() throws IllegalStateException {
        if (this.isFrozen()) {
            return;
        }
        this.freeze();
    }
}
