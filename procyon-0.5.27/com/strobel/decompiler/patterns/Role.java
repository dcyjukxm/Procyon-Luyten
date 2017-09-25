package com.strobel.decompiler.patterns;

import java.util.concurrent.atomic.*;
import com.strobel.core.*;

public class Role<T>
{
    public static final int ROLE_INDEX_BITS = 9;
    static final Role[] ROLES;
    static final AtomicInteger NEXT_ROLE_INDEX;
    final int index;
    final String name;
    final Class<T> nodeType;
    final T nullObject;
    
    static {
        ROLES = new Role[512];
        NEXT_ROLE_INDEX = new AtomicInteger();
    }
    
    public Role(final String name, final Class<T> nodeType) {
        this(name, nodeType, null);
    }
    
    public Role(final String name, final Class<T> nodeType, final T nullObject) {
        super();
        VerifyArgument.notNull(nodeType, "nodeType");
        this.index = Role.NEXT_ROLE_INDEX.getAndIncrement();
        if (this.index >= Role.ROLES.length) {
            throw new IllegalStateException("Too many roles created!");
        }
        this.name = name;
        this.nodeType = nodeType;
        this.nullObject = nullObject;
        Role.ROLES[this.index] = this;
    }
    
    public final T getNullObject() {
        return this.nullObject;
    }
    
    public final Class<T> getNodeType() {
        return this.nodeType;
    }
    
    public final int getIndex() {
        return this.index;
    }
    
    public boolean isValid(final Object node) {
        return this.nodeType.isInstance(node);
    }
    
    public static Role get(final int index) {
        return Role.ROLES[index];
    }
    
    @Override
    public String toString() {
        return this.name;
    }
}
