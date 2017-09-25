package com.strobel.assembler.metadata;

import com.strobel.core.*;
import java.util.*;

public class PackageReference
{
    public static final PackageReference GLOBAL;
    private final PackageReference _parent;
    private final String _name;
    private String _fullName;
    
    static {
        GLOBAL = new PackageReference();
    }
    
    private PackageReference() {
        super();
        this._parent = null;
        this._name = "";
    }
    
    public PackageReference(final String name) {
        super();
        this._parent = null;
        this._name = VerifyArgument.notNull(name, "name");
    }
    
    public PackageReference(final PackageReference parent, final String name) {
        super();
        this._parent = parent;
        this._name = VerifyArgument.notNull(name, "name");
    }
    
    public final boolean isGlobal() {
        return this._name.length() == 0;
    }
    
    public final String getName() {
        return this._name;
    }
    
    public final String getFullName() {
        if (this._fullName == null) {
            if (this._parent == null || this._parent.equals(PackageReference.GLOBAL)) {
                this._fullName = this.getName();
            }
            else {
                this._fullName = String.valueOf(this._parent.getFullName()) + "." + this.getName();
            }
        }
        return this._fullName;
    }
    
    public final PackageReference getParent() {
        return this._parent;
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof PackageReference) {
            final PackageReference that = (PackageReference)o;
            if (this._name.equals(that._name)) {
                if (this._parent == null) {
                    if (that._parent != null) {
                        return false;
                    }
                }
                else if (!this._parent.equals(that._parent)) {
                    return false;
                }
                return true;
            }
            return false;
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        int result = (this._parent != null) ? this._parent.hashCode() : 0;
        result = 31 * result + this._name.hashCode();
        return result;
    }
    
    public static PackageReference parse(final String qualifiedName) {
        VerifyArgument.notNull(qualifiedName, "qualifiedName");
        final List<String> parts = StringUtilities.split(qualifiedName, '.', '/');
        if (parts.isEmpty()) {
            return PackageReference.GLOBAL;
        }
        PackageReference current = new PackageReference(parts.get(0));
        for (int i = 1; i < parts.size(); ++i) {
            current = new PackageReference(current, parts.get(i));
        }
        return current;
    }
}
