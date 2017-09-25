package com.strobel.assembler.metadata;

import com.strobel.assembler.*;
import com.strobel.assembler.metadata.annotations.*;
import com.strobel.assembler.ir.attributes.*;
import com.strobel.core.*;
import javax.lang.model.element.*;
import java.util.*;

public class FieldDefinition extends FieldReference implements IMemberDefinition, IConstantValueProvider
{
    private final Collection<CustomAnnotation> _customAnnotations;
    private final Collection<SourceAttribute> _sourceAttributes;
    private final List<CustomAnnotation> _customAnnotationsView;
    private final List<SourceAttribute> _sourceAttributesView;
    private final IMetadataResolver _resolver;
    private String _name;
    private Object _fieldType;
    private TypeDefinition _declaringType;
    private Object _constantValue;
    private long _flags;
    
    protected FieldDefinition(final IMetadataResolver resolver) {
        super();
        this._resolver = resolver;
        this._customAnnotations = new Collection<CustomAnnotation>();
        this._customAnnotationsView = Collections.unmodifiableList((List<? extends CustomAnnotation>)this._customAnnotations);
        this._sourceAttributes = new Collection<SourceAttribute>();
        this._sourceAttributesView = Collections.unmodifiableList((List<? extends SourceAttribute>)this._sourceAttributes);
    }
    
    @Override
    public final List<CustomAnnotation> getAnnotations() {
        return this._customAnnotationsView;
    }
    
    protected final Collection<CustomAnnotation> getAnnotationsInternal() {
        return this._customAnnotations;
    }
    
    public final List<SourceAttribute> getSourceAttributes() {
        return this._sourceAttributesView;
    }
    
    protected final Collection<SourceAttribute> getSourceAttributesInternal() {
        return this._sourceAttributes;
    }
    
    @Override
    public int hashCode() {
        return HashUtilities.hashCode(this.getFullName());
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof FieldDefinition) {
            final FieldDefinition other = (FieldDefinition)obj;
            return StringUtilities.equals(this.getName(), other.getName()) && this.typeNamesMatch(this.getDeclaringType(), other.getDeclaringType());
        }
        return false;
    }
    
    private boolean typeNamesMatch(final TypeReference t1, final TypeReference t2) {
        return t1 != null && t2 != null && StringUtilities.equals(t1.getFullName(), t2.getFullName());
    }
    
    public final boolean isEnumConstant() {
        return Flags.testAny(this.getFlags(), 16384L);
    }
    
    @Override
    public final boolean hasConstantValue() {
        return this._constantValue != null;
    }
    
    @Override
    public final Object getConstantValue() {
        return this._constantValue;
    }
    
    @Override
    public final TypeReference getFieldType() {
        if (this._fieldType instanceof TypeReference) {
            return (TypeReference)this._fieldType;
        }
        if (this._fieldType instanceof String && this._resolver != null) {
            final TypeReference fieldType = this._resolver.lookupType((String)this._fieldType);
            if (fieldType != null) {
                return (TypeReference)(this._fieldType = fieldType);
            }
        }
        return null;
    }
    
    protected final void setFieldType(final TypeReference fieldType) {
        this._fieldType = fieldType;
    }
    
    protected final void setConstantValue(final Object constantValue) {
        this._constantValue = constantValue;
    }
    
    @Override
    public final String getName() {
        return this._name;
    }
    
    protected final void setName(final String name) {
        this._name = name;
    }
    
    @Override
    public final boolean isDefinition() {
        return true;
    }
    
    @Override
    public final TypeDefinition getDeclaringType() {
        return this._declaringType;
    }
    
    protected final void setDeclaringType(final TypeDefinition declaringType) {
        this._declaringType = declaringType;
    }
    
    @Override
    public final long getFlags() {
        return this._flags;
    }
    
    protected final void setFlags(final long flags) {
        this._flags = flags;
    }
    
    @Override
    public final int getModifiers() {
        return Flags.toModifiers(this.getFlags());
    }
    
    @Override
    public final boolean isFinal() {
        return Flags.testAny(this.getFlags(), 16L);
    }
    
    @Override
    public final boolean isNonPublic() {
        return !Flags.testAny(this.getFlags(), 1L);
    }
    
    @Override
    public final boolean isPrivate() {
        return Flags.testAny(this.getFlags(), 2L);
    }
    
    @Override
    public final boolean isProtected() {
        return Flags.testAny(this.getFlags(), 4L);
    }
    
    @Override
    public final boolean isPublic() {
        return Flags.testAny(this.getFlags(), 1L);
    }
    
    @Override
    public final boolean isStatic() {
        return Flags.testAny(this.getFlags(), 8L);
    }
    
    @Override
    public final boolean isSynthetic() {
        return Flags.testAny(this.getFlags(), 4096L);
    }
    
    @Override
    public final boolean isDeprecated() {
        return Flags.testAny(this.getFlags(), 131072L);
    }
    
    @Override
    public final boolean isPackagePrivate() {
        return !Flags.testAny(this.getFlags(), 7L);
    }
    
    @Override
    public String getBriefDescription() {
        return this.appendBriefDescription(new StringBuilder()).toString();
    }
    
    @Override
    public String getDescription() {
        return this.appendDescription(new StringBuilder()).toString();
    }
    
    @Override
    public String getErasedDescription() {
        return this.appendErasedDescription(new StringBuilder()).toString();
    }
    
    @Override
    public String getSimpleDescription() {
        return this.appendSimpleDescription(new StringBuilder()).toString();
    }
    
    @Override
    protected StringBuilder appendName(final StringBuilder sb, final boolean fullName, final boolean dottedName) {
        if (fullName) {
            final TypeDefinition declaringType = this.getDeclaringType();
            if (declaringType != null) {
                return declaringType.appendName(sb, true, false).append('.').append(this.getName());
            }
        }
        return sb.append(this._name);
    }
    
    protected StringBuilder appendDescription(final StringBuilder sb) {
        StringBuilder s = sb;
        for (final Modifier modifier : Flags.asModifierSet(this.getModifiers())) {
            s.append(modifier.toString());
            s.append(' ');
        }
        final TypeReference fieldType = this.getFieldType();
        if (fieldType.isGenericParameter()) {
            s.append(fieldType.getName());
        }
        else {
            s = fieldType.appendBriefDescription(s);
        }
        s.append(' ');
        s.append(this.getName());
        return s;
    }
    
    protected StringBuilder appendBriefDescription(final StringBuilder sb) {
        StringBuilder s = sb;
        for (final Modifier modifier : Flags.asModifierSet(this.getModifiers())) {
            s.append(modifier.toString());
            s.append(' ');
        }
        final TypeReference fieldType = this.getFieldType();
        if (fieldType.isGenericParameter()) {
            s.append(fieldType.getName());
        }
        else {
            s = fieldType.appendBriefDescription(s);
        }
        s.append(' ');
        s.append(this.getName());
        return s;
    }
    
    protected StringBuilder appendErasedDescription(final StringBuilder sb) {
        for (final Modifier modifier : Flags.asModifierSet(this.getModifiers())) {
            sb.append(modifier.toString());
            sb.append(' ');
        }
        final StringBuilder s = this.getFieldType().getRawType().appendErasedDescription(sb);
        s.append(' ');
        s.append(this.getName());
        return s;
    }
    
    protected StringBuilder appendSimpleDescription(final StringBuilder sb) {
        StringBuilder s = sb;
        for (final Modifier modifier : Flags.asModifierSet(this.getModifiers())) {
            s.append(modifier.toString());
            s.append(' ');
        }
        final TypeReference fieldType = this.getFieldType();
        if (fieldType.isGenericParameter()) {
            s.append(fieldType.getName());
        }
        else {
            s = fieldType.appendSimpleDescription(s);
        }
        s.append(' ');
        s.append(this.getName());
        return s;
    }
    
    @Override
    public String toString() {
        return this.getSimpleDescription();
    }
}
