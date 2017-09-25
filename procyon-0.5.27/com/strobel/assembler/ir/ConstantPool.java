package com.strobel.assembler.ir;

import java.util.*;
import com.strobel.assembler.metadata.*;
import com.strobel.core.*;
import java.io.*;

public final class ConstantPool extends Freezable implements Iterable<Entry>
{
    private final ArrayList<Entry> _pool;
    private final HashMap<Key, Entry> _entryMap;
    private final Key _lookupKey;
    private final Key _newKey;
    private int _size;
    private static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$assembler$ir$ConstantPool$Tag;
    
    public ConstantPool() {
        super();
        this._pool = new ArrayList<Entry>();
        this._entryMap = new HashMap<Key, Entry>();
        this._lookupKey = new Key(null);
        this._newKey = new Key(null);
    }
    
    @Override
    public Iterator<Entry> iterator() {
        return this._pool.iterator();
    }
    
    public void accept(final Visitor visitor) {
        VerifyArgument.notNull(visitor, "visitor");
        for (final Entry entry : this._pool) {
            if (entry != null) {
                visitor.visit(entry);
            }
        }
    }
    
    public void write(final Buffer stream) {
        stream.writeShort(this._size + 1);
        this.accept(new Writer(stream, null));
    }
    
    public <T extends Entry> T getEntry(final int index) {
        VerifyArgument.inRange(0, this._size + 1, index, "index");
        final Entry info = this._pool.get(index - 1);
        if (info == null) {
            throw new IndexOutOfBoundsException();
        }
        return (T)info;
    }
    
    public Entry get(final int index) {
        VerifyArgument.inRange(0, this._size + 1, index, "index");
        final Entry info = this._pool.get(index - 1);
        if (info == null) {
            throw new IndexOutOfBoundsException();
        }
        return info;
    }
    
    public Entry get(final int index, final Tag expectedType) {
        VerifyArgument.inRange(0, this._size + 1, index, "index");
        final Entry entry = this.get(index);
        final Tag actualType = entry.getTag();
        if (actualType != expectedType) {
            throw new IllegalStateException(String.format("Expected type '%s' but found type '%s'.", expectedType, actualType));
        }
        return entry;
    }
    
    public String lookupStringConstant(final int index) {
        final StringConstantEntry entry = (StringConstantEntry)this.get(index, Tag.StringConstant);
        return entry.getValue();
    }
    
    public String lookupUtf8Constant(final int index) {
        final Utf8StringConstantEntry entry = (Utf8StringConstantEntry)this.get(index, Tag.Utf8StringConstant);
        return entry.value;
    }
    
    public <T> T lookupConstant(final int index) {
        final ConstantEntry entry = (ConstantEntry)this.get(index);
        return (T)entry.getConstantValue();
    }
    
    public int lookupIntegerConstant(final int index) {
        final IntegerConstantEntry entry = (IntegerConstantEntry)this.get(index, Tag.IntegerConstant);
        return entry.value;
    }
    
    public long lookupLongConstant(final int index) {
        final LongConstantEntry entry = (LongConstantEntry)this.get(index, Tag.LongConstant);
        return entry.value;
    }
    
    public float lookupFloatConstant(final int index) {
        final FloatConstantEntry entry = (FloatConstantEntry)this.get(index, Tag.FloatConstant);
        return entry.value;
    }
    
    public double lookupDoubleConstant(final int index) {
        final DoubleConstantEntry entry = (DoubleConstantEntry)this.get(index, Tag.DoubleConstant);
        return entry.value;
    }
    
    public Utf8StringConstantEntry getUtf8StringConstant(final String value) {
        this._lookupKey.set(value);
        Entry entry = this._entryMap.get(this._lookupKey);
        if (entry == null) {
            if (this.isFrozen()) {
                return null;
            }
            entry = new Utf8StringConstantEntry(this, value);
        }
        this._lookupKey.clear();
        return (Utf8StringConstantEntry)entry;
    }
    
    public StringConstantEntry getStringConstant(final String value) {
        final Utf8StringConstantEntry utf8Constant = this.getUtf8StringConstant(value);
        this._lookupKey.set(Tag.StringConstant, utf8Constant.index);
        Entry entry = this._entryMap.get(this._lookupKey);
        if (entry == null) {
            if (this.isFrozen()) {
                return null;
            }
            entry = new StringConstantEntry(this, utf8Constant.index);
        }
        this._lookupKey.clear();
        return (StringConstantEntry)entry;
    }
    
    public IntegerConstantEntry getIntegerConstant(final int value) {
        this._lookupKey.set(value);
        Entry entry = this._entryMap.get(this._lookupKey);
        if (entry == null) {
            if (this.isFrozen()) {
                return null;
            }
            entry = new IntegerConstantEntry(this, value);
        }
        this._lookupKey.clear();
        return (IntegerConstantEntry)entry;
    }
    
    public FloatConstantEntry getFloatConstant(final float value) {
        this._lookupKey.set(value);
        Entry entry = this._entryMap.get(this._lookupKey);
        if (entry == null) {
            if (this.isFrozen()) {
                return null;
            }
            entry = new FloatConstantEntry(this, value);
        }
        this._lookupKey.clear();
        return (FloatConstantEntry)entry;
    }
    
    public LongConstantEntry getLongConstant(final long value) {
        this._lookupKey.set(value);
        Entry entry = this._entryMap.get(this._lookupKey);
        if (entry == null) {
            if (this.isFrozen()) {
                return null;
            }
            entry = new LongConstantEntry(this, value);
        }
        this._lookupKey.clear();
        return (LongConstantEntry)entry;
    }
    
    public DoubleConstantEntry getDoubleConstant(final double value) {
        this._lookupKey.set(value);
        Entry entry = this._entryMap.get(this._lookupKey);
        if (entry == null) {
            if (this.isFrozen()) {
                return null;
            }
            entry = new DoubleConstantEntry(this, value);
        }
        this._lookupKey.clear();
        return (DoubleConstantEntry)entry;
    }
    
    public TypeInfoEntry getTypeInfo(final TypeReference type) {
        final Utf8StringConstantEntry name = this.getUtf8StringConstant(type.getInternalName());
        this._lookupKey.set(Tag.TypeInfo, name.index);
        Entry entry = this._entryMap.get(this._lookupKey);
        if (entry == null) {
            if (this.isFrozen()) {
                return null;
            }
            entry = new TypeInfoEntry(this, name.index);
        }
        this._lookupKey.clear();
        return (TypeInfoEntry)entry;
    }
    
    public FieldReferenceEntry getFieldReference(final FieldReference field) {
        final TypeInfoEntry typeInfo = this.getTypeInfo(field.getDeclaringType());
        final NameAndTypeDescriptorEntry nameAndDescriptor = this.getNameAndTypeDescriptor(field.getName(), field.getErasedSignature());
        this._lookupKey.set(Tag.FieldReference, typeInfo.index, nameAndDescriptor.index);
        Entry entry = this._entryMap.get(this._lookupKey);
        if (entry == null) {
            if (this.isFrozen()) {
                return null;
            }
            entry = new FieldReferenceEntry(this, typeInfo.index, nameAndDescriptor.index);
        }
        this._lookupKey.clear();
        return (FieldReferenceEntry)entry;
    }
    
    public MethodReferenceEntry getMethodReference(final MethodReference method) {
        final TypeInfoEntry typeInfo = this.getTypeInfo(method.getDeclaringType());
        final NameAndTypeDescriptorEntry nameAndDescriptor = this.getNameAndTypeDescriptor(method.getName(), method.getErasedSignature());
        this._lookupKey.set(Tag.MethodReference, typeInfo.index, nameAndDescriptor.index);
        Entry entry = this._entryMap.get(this._lookupKey);
        if (entry == null) {
            if (this.isFrozen()) {
                return null;
            }
            entry = new MethodReferenceEntry(this, typeInfo.index, nameAndDescriptor.index);
        }
        this._lookupKey.clear();
        return (MethodReferenceEntry)entry;
    }
    
    public InterfaceMethodReferenceEntry getInterfaceMethodReference(final MethodReference method) {
        final TypeInfoEntry typeInfo = this.getTypeInfo(method.getDeclaringType());
        final NameAndTypeDescriptorEntry nameAndDescriptor = this.getNameAndTypeDescriptor(method.getName(), method.getErasedSignature());
        this._lookupKey.set(Tag.InterfaceMethodReference, typeInfo.index, nameAndDescriptor.index);
        Entry entry = this._entryMap.get(this._lookupKey);
        if (entry == null) {
            if (this.isFrozen()) {
                return null;
            }
            entry = new InterfaceMethodReferenceEntry(this, typeInfo.index, nameAndDescriptor.index);
        }
        this._lookupKey.clear();
        return (InterfaceMethodReferenceEntry)entry;
    }
    
    NameAndTypeDescriptorEntry getNameAndTypeDescriptor(final String name, final String typeDescriptor) {
        final Utf8StringConstantEntry utf8Name = this.getUtf8StringConstant(name);
        final Utf8StringConstantEntry utf8Descriptor = this.getUtf8StringConstant(typeDescriptor);
        this._lookupKey.set(Tag.NameAndTypeDescriptor, utf8Name.index, utf8Descriptor.index);
        Entry entry = this._entryMap.get(this._lookupKey);
        if (entry == null) {
            if (this.isFrozen()) {
                return null;
            }
            entry = new NameAndTypeDescriptorEntry(this, utf8Name.index, utf8Descriptor.index);
        }
        this._lookupKey.clear();
        return (NameAndTypeDescriptorEntry)entry;
    }
    
    MethodHandleEntry getMethodHandle(final ReferenceKind referenceKind, final int referenceIndex) {
        this._lookupKey.set(Tag.MethodHandle, referenceIndex, referenceKind);
        Entry entry = this._entryMap.get(this._lookupKey);
        if (entry == null) {
            if (this.isFrozen()) {
                return null;
            }
            entry = new MethodHandleEntry(this, referenceKind, referenceIndex);
        }
        this._lookupKey.clear();
        return (MethodHandleEntry)entry;
    }
    
    MethodTypeEntry getMethodType(final int descriptorIndex) {
        this._lookupKey.set(Tag.MethodType, descriptorIndex);
        Entry entry = this._entryMap.get(this._lookupKey);
        if (entry == null) {
            if (this.isFrozen()) {
                return null;
            }
            entry = new MethodTypeEntry(this, descriptorIndex);
        }
        this._lookupKey.clear();
        return (MethodTypeEntry)entry;
    }
    
    InvokeDynamicInfoEntry getInvokeDynamicInfo(final int bootstrapMethodAttributeIndex, final int nameAndTypeDescriptorIndex) {
        this._lookupKey.set(Tag.InvokeDynamicInfo, bootstrapMethodAttributeIndex, nameAndTypeDescriptorIndex);
        Entry entry = this._entryMap.get(this._lookupKey);
        if (entry == null) {
            if (this.isFrozen()) {
                return null;
            }
            entry = new InvokeDynamicInfoEntry(this, bootstrapMethodAttributeIndex, nameAndTypeDescriptorIndex);
        }
        this._lookupKey.clear();
        return (InvokeDynamicInfoEntry)entry;
    }
    
    public static ConstantPool read(final Buffer b) {
        boolean skipOne = false;
        final ConstantPool pool = new ConstantPool();
        final int size = b.readUnsignedShort();
        final Key key = new Key(null);
        for (int i = 1; i < size; ++i) {
            if (skipOne) {
                skipOne = false;
            }
            else {
                key.clear();
                final Tag tag = Tag.fromValue(b.readUnsignedByte());
                switch ($SWITCH_TABLE$com$strobel$assembler$ir$ConstantPool$Tag()[tag.ordinal()]) {
                    case 1: {
                        new Utf8StringConstantEntry(pool, b.readUtf8());
                        break;
                    }
                    case 2: {
                        new IntegerConstantEntry(pool, b.readInt());
                        break;
                    }
                    case 3: {
                        new FloatConstantEntry(pool, b.readFloat());
                        break;
                    }
                    case 4: {
                        new LongConstantEntry(pool, b.readLong());
                        skipOne = true;
                        break;
                    }
                    case 5: {
                        new DoubleConstantEntry(pool, b.readDouble());
                        skipOne = true;
                        break;
                    }
                    case 6: {
                        new TypeInfoEntry(pool, b.readUnsignedShort());
                        break;
                    }
                    case 7: {
                        new StringConstantEntry(pool, b.readUnsignedShort());
                        break;
                    }
                    case 8: {
                        new FieldReferenceEntry(pool, b.readUnsignedShort(), b.readUnsignedShort());
                        break;
                    }
                    case 9: {
                        new MethodReferenceEntry(pool, b.readUnsignedShort(), b.readUnsignedShort());
                        break;
                    }
                    case 10: {
                        new InterfaceMethodReferenceEntry(pool, b.readUnsignedShort(), b.readUnsignedShort());
                        break;
                    }
                    case 11: {
                        new NameAndTypeDescriptorEntry(pool, b.readUnsignedShort(), b.readUnsignedShort());
                        break;
                    }
                    case 12: {
                        new MethodHandleEntry(pool, ReferenceKind.fromTag(b.readUnsignedByte()), b.readUnsignedShort());
                        break;
                    }
                    case 13: {
                        new MethodTypeEntry(pool, b.readUnsignedShort());
                        break;
                    }
                    case 14: {
                        new InvokeDynamicInfoEntry(pool, b.readUnsignedShort(), b.readUnsignedShort());
                        break;
                    }
                }
            }
        }
        return pool;
    }
    
    static /* synthetic */ int access$0(final ConstantPool param_0) {
        return param_0._size;
    }
    
    static /* synthetic */ ArrayList access$1(final ConstantPool param_0) {
        return param_0._pool;
    }
    
    static /* synthetic */ void access$2(final ConstantPool param_0, final int param_1) {
        param_0._size = param_1;
    }
    
    static /* synthetic */ Key access$3(final ConstantPool param_0) {
        return param_0._newKey;
    }
    
    static /* synthetic */ HashMap access$4(final ConstantPool param_0) {
        return param_0._entryMap;
    }
    
    static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$assembler$ir$ConstantPool$Tag() {
        final int[] loc_0 = ConstantPool.$SWITCH_TABLE$com$strobel$assembler$ir$ConstantPool$Tag;
        if (loc_0 != null) {
            return loc_0;
        }
        final int[] loc_1 = new int[Tag.values().length];
        try {
            loc_1[Tag.DoubleConstant.ordinal()] = 5;
        }
        catch (NoSuchFieldError loc_2) {}
        try {
            loc_1[Tag.FieldReference.ordinal()] = 8;
        }
        catch (NoSuchFieldError loc_3) {}
        try {
            loc_1[Tag.FloatConstant.ordinal()] = 3;
        }
        catch (NoSuchFieldError loc_4) {}
        try {
            loc_1[Tag.IntegerConstant.ordinal()] = 2;
        }
        catch (NoSuchFieldError loc_5) {}
        try {
            loc_1[Tag.InterfaceMethodReference.ordinal()] = 10;
        }
        catch (NoSuchFieldError loc_6) {}
        try {
            loc_1[Tag.InvokeDynamicInfo.ordinal()] = 14;
        }
        catch (NoSuchFieldError loc_7) {}
        try {
            loc_1[Tag.LongConstant.ordinal()] = 4;
        }
        catch (NoSuchFieldError loc_8) {}
        try {
            loc_1[Tag.MethodHandle.ordinal()] = 12;
        }
        catch (NoSuchFieldError loc_9) {}
        try {
            loc_1[Tag.MethodReference.ordinal()] = 9;
        }
        catch (NoSuchFieldError loc_10) {}
        try {
            loc_1[Tag.MethodType.ordinal()] = 13;
        }
        catch (NoSuchFieldError loc_11) {}
        try {
            loc_1[Tag.NameAndTypeDescriptor.ordinal()] = 11;
        }
        catch (NoSuchFieldError loc_12) {}
        try {
            loc_1[Tag.StringConstant.ordinal()] = 7;
        }
        catch (NoSuchFieldError loc_13) {}
        try {
            loc_1[Tag.TypeInfo.ordinal()] = 6;
        }
        catch (NoSuchFieldError loc_14) {}
        try {
            loc_1[Tag.Utf8StringConstant.ordinal()] = 1;
        }
        catch (NoSuchFieldError loc_15) {}
        return ConstantPool.$SWITCH_TABLE$com$strobel$assembler$ir$ConstantPool$Tag = loc_1;
    }
    
    private static final class Key
    {
        private Tag _tag;
        private int _intValue;
        private long _longValue;
        private String _stringValue1;
        private String _stringValue2;
        private int _refIndex1;
        private int _refIndex2;
        private int _hashCode;
        private static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$assembler$ir$ConstantPool$Tag;
        
        private Key() {
            super();
            this._refIndex1 = -1;
            this._refIndex2 = -1;
        }
        
        public void clear() {
            this._tag = null;
            this._intValue = 0;
            this._longValue = 0L;
            this._stringValue1 = null;
            this._stringValue2 = null;
            this._refIndex1 = -1;
            this._refIndex2 = -1;
        }
        
        public void set(final int intValue) {
            this._tag = Tag.IntegerConstant;
            this._intValue = intValue;
            this._hashCode = (Integer.MAX_VALUE & this._tag.value + this._intValue);
        }
        
        public void set(final long longValue) {
            this._tag = Tag.LongConstant;
            this._longValue = longValue;
            this._hashCode = (Integer.MAX_VALUE & this._tag.value + (int)longValue);
        }
        
        public void set(final float floatValue) {
            this._tag = Tag.FloatConstant;
            this._intValue = Float.floatToIntBits(floatValue);
            this._hashCode = (Integer.MAX_VALUE & this._tag.value + this._intValue);
        }
        
        public void set(final double doubleValue) {
            this._tag = Tag.DoubleConstant;
            this._longValue = Double.doubleToLongBits(doubleValue);
            this._hashCode = (Integer.MAX_VALUE & this._tag.value + (int)this._longValue);
        }
        
        public void set(final String utf8Value) {
            this._tag = Tag.Utf8StringConstant;
            this._stringValue1 = utf8Value;
            this._hashCode = HashUtilities.combineHashCodes(this._tag, utf8Value);
        }
        
        public void set(final Tag tag, final int refIndex1, final ReferenceKind refKind) {
            this._tag = tag;
            this._refIndex1 = refIndex1;
            this._refIndex2 = refKind.tag;
            this._hashCode = HashUtilities.combineHashCodes(tag, refIndex1);
        }
        
        public void set(final Tag tag, final int refIndex1) {
            this._tag = tag;
            this._refIndex1 = refIndex1;
            this._hashCode = HashUtilities.combineHashCodes(tag, refIndex1);
        }
        
        public void set(final Tag tag, final int refIndex1, final int refIndex2) {
            this._tag = tag;
            this._refIndex1 = refIndex1;
            this._refIndex2 = refIndex2;
            this._hashCode = HashUtilities.combineHashCodes(tag, refIndex1, refIndex2);
        }
        
        public void set(final Tag tag, final String stringValue1) {
            this._tag = tag;
            this._stringValue1 = stringValue1;
            this._hashCode = HashUtilities.combineHashCodes(tag, stringValue1);
        }
        
        public void set(final Tag tag, final String stringValue1, final String stringValue2) {
            this._tag = tag;
            this._stringValue1 = stringValue1;
            this._stringValue2 = stringValue2;
            this._hashCode = HashUtilities.combineHashCodes(tag, stringValue1, stringValue2);
        }
        
        @Override
        protected Key clone() {
            final Key key = new Key();
            key._tag = this._tag;
            key._hashCode = this._hashCode;
            key._intValue = this._intValue;
            key._longValue = this._longValue;
            key._stringValue1 = this._stringValue1;
            key._stringValue2 = this._stringValue2;
            key._refIndex1 = this._refIndex1;
            key._refIndex2 = this._refIndex2;
            return key;
        }
        
        @Override
        public int hashCode() {
            return this._hashCode;
        }
        
        @Override
        public boolean equals(final Object obj) {
            if (!(obj instanceof Key)) {
                return false;
            }
            final Key key = (Key)obj;
            if (key._tag != this._tag) {
                return false;
            }
            switch ($SWITCH_TABLE$com$strobel$assembler$ir$ConstantPool$Tag()[this._tag.ordinal()]) {
                case 1: {
                    return StringUtilities.equals(key._stringValue1, this._stringValue1);
                }
                case 2:
                case 3: {
                    return key._intValue == this._intValue;
                }
                case 4:
                case 5: {
                    return key._longValue == this._longValue;
                }
                case 6:
                case 7:
                case 13: {
                    return key._refIndex1 == this._refIndex1;
                }
                case 8:
                case 9:
                case 10:
                case 11:
                case 12:
                case 14: {
                    return key._refIndex1 == this._refIndex1 && key._refIndex2 == this._refIndex2;
                }
                default: {
                    return false;
                }
            }
        }
        
        static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$assembler$ir$ConstantPool$Tag() {
            final int[] loc_0 = Key.$SWITCH_TABLE$com$strobel$assembler$ir$ConstantPool$Tag;
            if (loc_0 != null) {
                return loc_0;
            }
            final int[] loc_1 = new int[Tag.values().length];
            try {
                loc_1[Tag.DoubleConstant.ordinal()] = 5;
            }
            catch (NoSuchFieldError loc_2) {}
            try {
                loc_1[Tag.FieldReference.ordinal()] = 8;
            }
            catch (NoSuchFieldError loc_3) {}
            try {
                loc_1[Tag.FloatConstant.ordinal()] = 3;
            }
            catch (NoSuchFieldError loc_4) {}
            try {
                loc_1[Tag.IntegerConstant.ordinal()] = 2;
            }
            catch (NoSuchFieldError loc_5) {}
            try {
                loc_1[Tag.InterfaceMethodReference.ordinal()] = 10;
            }
            catch (NoSuchFieldError loc_6) {}
            try {
                loc_1[Tag.InvokeDynamicInfo.ordinal()] = 14;
            }
            catch (NoSuchFieldError loc_7) {}
            try {
                loc_1[Tag.LongConstant.ordinal()] = 4;
            }
            catch (NoSuchFieldError loc_8) {}
            try {
                loc_1[Tag.MethodHandle.ordinal()] = 12;
            }
            catch (NoSuchFieldError loc_9) {}
            try {
                loc_1[Tag.MethodReference.ordinal()] = 9;
            }
            catch (NoSuchFieldError loc_10) {}
            try {
                loc_1[Tag.MethodType.ordinal()] = 13;
            }
            catch (NoSuchFieldError loc_11) {}
            try {
                loc_1[Tag.NameAndTypeDescriptor.ordinal()] = 11;
            }
            catch (NoSuchFieldError loc_12) {}
            try {
                loc_1[Tag.StringConstant.ordinal()] = 7;
            }
            catch (NoSuchFieldError loc_13) {}
            try {
                loc_1[Tag.TypeInfo.ordinal()] = 6;
            }
            catch (NoSuchFieldError loc_14) {}
            try {
                loc_1[Tag.Utf8StringConstant.ordinal()] = 1;
            }
            catch (NoSuchFieldError loc_15) {}
            return Key.$SWITCH_TABLE$com$strobel$assembler$ir$ConstantPool$Tag = loc_1;
        }
    }
    
    public enum ReferenceKind
    {
        GetField("GetField", 0, 1, "getfield"), 
        GetStatic("GetStatic", 1, 2, "getstatic"), 
        PutField("PutField", 2, 3, "putfield"), 
        PutStatic("PutStatic", 3, 4, "putstatic"), 
        InvokeVirtual("InvokeVirtual", 4, 5, "invokevirtual"), 
        InvokeStatic("InvokeStatic", 5, 6, "invokestatic"), 
        InvokeSpecial("InvokeSpecial", 6, 7, "invokespecial"), 
        NewInvokeSpecial("NewInvokeSpecial", 7, 8, "newinvokespecial"), 
        InvokeInterface("InvokeInterface", 8, 9, "invokeinterface");
        
        public final int tag;
        public final String name;
        
        private ReferenceKind(final String param_0, final int param_1, final int tag, final String name) {
            this.tag = tag;
            this.name = name;
        }
        
        static ReferenceKind fromTag(final int tag) {
            switch (tag) {
                case 1: {
                    return ReferenceKind.GetField;
                }
                case 2: {
                    return ReferenceKind.GetStatic;
                }
                case 3: {
                    return ReferenceKind.PutField;
                }
                case 4: {
                    return ReferenceKind.PutStatic;
                }
                case 5: {
                    return ReferenceKind.InvokeVirtual;
                }
                case 6: {
                    return ReferenceKind.InvokeStatic;
                }
                case 7: {
                    return ReferenceKind.InvokeSpecial;
                }
                case 8: {
                    return ReferenceKind.NewInvokeSpecial;
                }
                case 9: {
                    return ReferenceKind.InvokeInterface;
                }
                default: {
                    return null;
                }
            }
        }
    }
    
    public enum Tag
    {
        Utf8StringConstant("Utf8StringConstant", 0, 1), 
        IntegerConstant("IntegerConstant", 1, 3), 
        FloatConstant("FloatConstant", 2, 4), 
        LongConstant("LongConstant", 3, 5), 
        DoubleConstant("DoubleConstant", 4, 6), 
        TypeInfo("TypeInfo", 5, 7), 
        StringConstant("StringConstant", 6, 8), 
        FieldReference("FieldReference", 7, 9), 
        MethodReference("MethodReference", 8, 10), 
        InterfaceMethodReference("InterfaceMethodReference", 9, 11), 
        NameAndTypeDescriptor("NameAndTypeDescriptor", 10, 12), 
        MethodHandle("MethodHandle", 11, 15), 
        MethodType("MethodType", 12, 16), 
        InvokeDynamicInfo("InvokeDynamicInfo", 13, 18);
        
        public final int value;
        private static final Tag[] lookup;
        
        static {
            final Tag[] values = values();
            lookup = new Tag[Tag.InvokeDynamicInfo.value + 1];
            Tag[] loc_1;
            for (int loc_0 = (loc_1 = values).length, loc_2 = 0; loc_2 < loc_0; ++loc_2) {
                final Tag tag = loc_1[loc_2];
                Tag.lookup[tag.value] = tag;
            }
        }
        
        private Tag(final String param_0, final int param_1, final int value) {
            this.value = value;
        }
        
        public static Tag fromValue(final int value) {
            VerifyArgument.inRange(Tag.Utf8StringConstant.value, Tag.InvokeDynamicInfo.value, value, "value");
            return Tag.lookup[value];
        }
    }
    
    public abstract static class Entry
    {
        public final int index;
        protected final ConstantPool owner;
        
        Entry(final ConstantPool owner) {
            super();
            this.owner = owner;
            this.index = ConstantPool.access$0(owner) + 1;
            ConstantPool.access$1(owner).add(this);
            ConstantPool.access$2(owner, ConstantPool.access$0(owner) + this.size());
            for (int i = 1; i < this.size(); ++i) {
                ConstantPool.access$1(owner).add(null);
            }
        }
        
        abstract void fixupKey(final Key param_0);
        
        public abstract Tag getTag();
        
        public int size() {
            return 1;
        }
        
        public abstract int byteLength();
        
        public abstract void accept(final Visitor param_0);
    }
    
    public abstract static class ConstantEntry extends Entry
    {
        ConstantEntry(final ConstantPool owner) {
            super(owner);
        }
        
        public abstract Object getConstantValue();
    }
    
    public interface Visitor
    {
        public static final Visitor EMPTY = new Visitor() {
            @Override
            public void visit(Entry entry) {
            }
            
            @Override
            public void visitTypeInfo(TypeInfoEntry info) {
            }
            
            @Override
            public void visitDoubleConstant(DoubleConstantEntry info) {
            }
            
            @Override
            public void visitFieldReference(FieldReferenceEntry info) {
            }
            
            @Override
            public void visitFloatConstant(FloatConstantEntry info) {
            }
            
            @Override
            public void visitIntegerConstant(IntegerConstantEntry info) {
            }
            
            @Override
            public void visitInterfaceMethodReference(InterfaceMethodReferenceEntry info) {
            }
            
            @Override
            public void visitInvokeDynamicInfo(InvokeDynamicInfoEntry info) {
            }
            
            @Override
            public void visitLongConstant(LongConstantEntry info) {
            }
            
            @Override
            public void visitNameAndTypeDescriptor(NameAndTypeDescriptorEntry info) {
            }
            
            @Override
            public void visitMethodReference(MethodReferenceEntry info) {
            }
            
            @Override
            public void visitMethodHandle(MethodHandleEntry info) {
            }
            
            @Override
            public void visitMethodType(MethodTypeEntry info) {
            }
            
            @Override
            public void visitStringConstant(StringConstantEntry info) {
            }
            
            @Override
            public void visitUtf8StringConstant(Utf8StringConstantEntry info) {
            }
            
            @Override
            public void visitEnd() {
            }
        };
        
        void visit(Entry param_0);
        
        void visitTypeInfo(TypeInfoEntry param_0);
        
        void visitDoubleConstant(DoubleConstantEntry param_0);
        
        void visitFieldReference(FieldReferenceEntry param_0);
        
        void visitFloatConstant(FloatConstantEntry param_0);
        
        void visitIntegerConstant(IntegerConstantEntry param_0);
        
        void visitInterfaceMethodReference(InterfaceMethodReferenceEntry param_0);
        
        void visitInvokeDynamicInfo(InvokeDynamicInfoEntry param_0);
        
        void visitLongConstant(LongConstantEntry param_0);
        
        void visitNameAndTypeDescriptor(NameAndTypeDescriptorEntry param_0);
        
        void visitMethodReference(MethodReferenceEntry param_0);
        
        void visitMethodHandle(MethodHandleEntry param_0);
        
        void visitMethodType(MethodTypeEntry param_0);
        
        void visitStringConstant(StringConstantEntry param_0);
        
        void visitUtf8StringConstant(Utf8StringConstantEntry param_0);
        
        void visitEnd();
    }
    
    private static final class Writer implements Visitor
    {
        private final Buffer codeStream;
        
        private Writer(final Buffer codeStream) {
            super();
            this.codeStream = VerifyArgument.notNull(codeStream, "codeStream");
        }
        
        @Override
        public void visit(final Entry entry) {
            entry.accept(this);
        }
        
        @Override
        public void visitTypeInfo(final TypeInfoEntry info) {
            this.codeStream.writeByte(info.getTag().value);
            this.codeStream.writeShort(info.nameIndex);
        }
        
        @Override
        public void visitDoubleConstant(final DoubleConstantEntry info) {
            this.codeStream.writeByte(info.getTag().value);
            this.codeStream.writeDouble(info.value);
        }
        
        @Override
        public void visitFieldReference(final FieldReferenceEntry info) {
            this.codeStream.writeByte(info.getTag().value);
            this.codeStream.writeShort(info.typeInfoIndex);
            this.codeStream.writeShort(info.nameAndTypeDescriptorIndex);
        }
        
        @Override
        public void visitFloatConstant(final FloatConstantEntry info) {
            this.codeStream.writeByte(info.getTag().value);
            this.codeStream.writeFloat(info.value);
        }
        
        @Override
        public void visitIntegerConstant(final IntegerConstantEntry info) {
            this.codeStream.writeByte(info.getTag().value);
            this.codeStream.writeInt(info.value);
        }
        
        @Override
        public void visitInterfaceMethodReference(final InterfaceMethodReferenceEntry info) {
            this.codeStream.writeByte(info.getTag().value);
            this.codeStream.writeShort(info.typeInfoIndex);
            this.codeStream.writeShort(info.nameAndTypeDescriptorIndex);
        }
        
        @Override
        public void visitInvokeDynamicInfo(final InvokeDynamicInfoEntry info) {
            this.codeStream.writeByte(info.getTag().value);
            this.codeStream.writeShort(info.bootstrapMethodAttributeIndex);
            this.codeStream.writeShort(info.nameAndTypeDescriptorIndex);
        }
        
        @Override
        public void visitLongConstant(final LongConstantEntry info) {
            this.codeStream.writeByte(info.getTag().value);
            this.codeStream.writeLong(info.value);
        }
        
        @Override
        public void visitNameAndTypeDescriptor(final NameAndTypeDescriptorEntry info) {
            this.codeStream.writeByte(info.getTag().value);
            this.codeStream.writeShort(info.nameIndex);
            this.codeStream.writeShort(info.typeDescriptorIndex);
        }
        
        @Override
        public void visitMethodReference(final MethodReferenceEntry info) {
            this.codeStream.writeByte(info.getTag().value);
            this.codeStream.writeShort(info.typeInfoIndex);
            this.codeStream.writeShort(info.nameAndTypeDescriptorIndex);
        }
        
        @Override
        public void visitMethodHandle(final MethodHandleEntry info) {
            this.codeStream.writeByte(info.getTag().value);
            this.codeStream.writeShort(info.referenceKind.ordinal());
            this.codeStream.writeShort(info.referenceIndex);
        }
        
        @Override
        public void visitMethodType(final MethodTypeEntry info) {
            this.codeStream.writeByte(info.getTag().value);
            this.codeStream.writeShort(info.descriptorIndex);
        }
        
        @Override
        public void visitStringConstant(final StringConstantEntry info) {
            this.codeStream.writeByte(info.getTag().value);
            this.codeStream.writeShort(info.stringIndex);
        }
        
        @Override
        public void visitUtf8StringConstant(final Utf8StringConstantEntry info) {
            this.codeStream.writeByte(info.getTag().value);
            this.codeStream.writeUtf8(info.value);
        }
        
        @Override
        public void visitEnd() {
        }
    }
    
    public static final class TypeInfoEntry extends Entry
    {
        public final int nameIndex;
        
        public TypeInfoEntry(final ConstantPool owner, final int nameIndex) {
            super(owner);
            this.nameIndex = nameIndex;
            ConstantPool.access$3(owner).set(this.getTag(), nameIndex);
            ConstantPool.access$4(owner).put(ConstantPool.access$3(owner).clone(), this);
            ConstantPool.access$3(owner).clear();
        }
        
        public String getName() {
            return ((Utf8StringConstantEntry)this.owner.get(this.nameIndex, Tag.Utf8StringConstant)).value;
        }
        
        @Override
        void fixupKey(final Key key) {
            key.set(Tag.TypeInfo, this.nameIndex);
        }
        
        @Override
        public Tag getTag() {
            return Tag.TypeInfo;
        }
        
        @Override
        public int byteLength() {
            return 3;
        }
        
        @Override
        public void accept(final Visitor visitor) {
            visitor.visitTypeInfo(this);
        }
        
        @Override
        public String toString() {
            return "TypeIndex[index: " + this.index + ", nameIndex: " + this.nameIndex + "]";
        }
    }
    
    public static final class MethodTypeEntry extends Entry
    {
        public final int descriptorIndex;
        
        public MethodTypeEntry(final ConstantPool owner, final int descriptorIndex) {
            super(owner);
            this.descriptorIndex = descriptorIndex;
            ConstantPool.access$3(owner).set(this.getTag(), descriptorIndex);
            ConstantPool.access$4(owner).put(ConstantPool.access$3(owner).clone(), this);
            ConstantPool.access$3(owner).clear();
        }
        
        public String getType() {
            return ((Utf8StringConstantEntry)this.owner.get(this.descriptorIndex, Tag.Utf8StringConstant)).value;
        }
        
        @Override
        void fixupKey(final Key key) {
            key.set(Tag.MethodType, this.descriptorIndex);
        }
        
        @Override
        public Tag getTag() {
            return Tag.MethodType;
        }
        
        @Override
        public int byteLength() {
            return 3;
        }
        
        @Override
        public void accept(final Visitor visitor) {
            visitor.visitMethodType(this);
        }
        
        @Override
        public String toString() {
            return "MethodTypeEntry[index: " + this.index + ", descriptorIndex: " + this.descriptorIndex + "]";
        }
    }
    
    public abstract static class ReferenceEntry extends Entry
    {
        public final Tag tag;
        public final int typeInfoIndex;
        public final int nameAndTypeDescriptorIndex;
        
        protected ReferenceEntry(final ConstantPool cp, final Tag tag, final int typeInfoIndex, final int nameAndTypeDescriptorIndex) {
            super(cp);
            this.tag = tag;
            this.typeInfoIndex = typeInfoIndex;
            this.nameAndTypeDescriptorIndex = nameAndTypeDescriptorIndex;
            ConstantPool.access$3(this.owner).set(tag, typeInfoIndex, nameAndTypeDescriptorIndex);
            ConstantPool.access$4(this.owner).put(ConstantPool.access$3(this.owner).clone(), this);
            ConstantPool.access$3(this.owner).clear();
        }
        
        @Override
        public Tag getTag() {
            return this.tag;
        }
        
        @Override
        public int byteLength() {
            return 5;
        }
        
        public TypeInfoEntry getClassInfo() {
            return (TypeInfoEntry)this.owner.get(this.typeInfoIndex, Tag.TypeInfo);
        }
        
        public String getClassName() {
            return this.getClassInfo().getName();
        }
        
        public NameAndTypeDescriptorEntry getNameAndTypeInfo() {
            return (NameAndTypeDescriptorEntry)this.owner.get(this.nameAndTypeDescriptorIndex, Tag.NameAndTypeDescriptor);
        }
        
        @Override
        public String toString() {
            return String.valueOf(this.getClass().getSimpleName()) + "[index: " + this.index + ", typeInfoIndex: " + this.typeInfoIndex + ", nameAndTypeDescriptorIndex: " + this.nameAndTypeDescriptorIndex + "]";
        }
    }
    
    public static final class FieldReferenceEntry extends ReferenceEntry
    {
        public FieldReferenceEntry(final ConstantPool owner, final int typeIndex, final int nameAndTypeDescriptorIndex) {
            super(owner, Tag.FieldReference, typeIndex, nameAndTypeDescriptorIndex);
        }
        
        @Override
        void fixupKey(final Key key) {
            key.set(Tag.FieldReference, this.typeInfoIndex, this.nameAndTypeDescriptorIndex);
        }
        
        @Override
        public void accept(final Visitor visitor) {
            visitor.visitFieldReference(this);
        }
    }
    
    public static final class MethodReferenceEntry extends ReferenceEntry
    {
        public MethodReferenceEntry(final ConstantPool owner, final int typeIndex, final int nameAndTypeDescriptorIndex) {
            super(owner, Tag.MethodReference, typeIndex, nameAndTypeDescriptorIndex);
        }
        
        @Override
        void fixupKey(final Key key) {
            key.set(Tag.MethodReference, this.typeInfoIndex, this.nameAndTypeDescriptorIndex);
        }
        
        @Override
        public void accept(final Visitor visitor) {
            visitor.visitMethodReference(this);
        }
    }
    
    public static final class InterfaceMethodReferenceEntry extends ReferenceEntry
    {
        public InterfaceMethodReferenceEntry(final ConstantPool owner, final int typeIndex, final int nameAndTypeDescriptorIndex) {
            super(owner, Tag.InterfaceMethodReference, typeIndex, nameAndTypeDescriptorIndex);
        }
        
        @Override
        void fixupKey(final Key key) {
            key.set(Tag.InterfaceMethodReference, this.typeInfoIndex, this.nameAndTypeDescriptorIndex);
        }
        
        @Override
        public void accept(final Visitor visitor) {
            visitor.visitInterfaceMethodReference(this);
        }
    }
    
    public static class MethodHandleEntry extends Entry
    {
        public final ReferenceKind referenceKind;
        public final int referenceIndex;
        private static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$assembler$ir$ConstantPool$Tag;
        
        public MethodHandleEntry(final ConstantPool owner, final ReferenceKind referenceKind, final int referenceIndex) {
            super(owner);
            this.referenceKind = referenceKind;
            this.referenceIndex = referenceIndex;
            ConstantPool.access$3(owner).set(this.getTag(), referenceIndex, referenceKind);
            ConstantPool.access$4(owner).put(ConstantPool.access$3(owner).clone(), this);
            ConstantPool.access$3(owner).clear();
        }
        
        public ReferenceEntry getReference() {
            final Tag actual = this.owner.get(this.referenceIndex).getTag();
            Tag expected = Tag.MethodReference;
            switch ($SWITCH_TABLE$com$strobel$assembler$ir$ConstantPool$Tag()[actual.ordinal()]) {
                case 8:
                case 10: {
                    expected = actual;
                    break;
                }
            }
            return (ReferenceEntry)this.owner.get(this.referenceIndex, expected);
        }
        
        @Override
        void fixupKey(final Key key) {
            key.set(Tag.MethodHandle, this.referenceIndex, this.referenceKind);
        }
        
        @Override
        public Tag getTag() {
            return Tag.MethodHandle;
        }
        
        @Override
        public int byteLength() {
            return 4;
        }
        
        @Override
        public void accept(final Visitor visitor) {
            visitor.visitMethodHandle(this);
        }
        
        static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$assembler$ir$ConstantPool$Tag() {
            final int[] loc_0 = MethodHandleEntry.$SWITCH_TABLE$com$strobel$assembler$ir$ConstantPool$Tag;
            if (loc_0 != null) {
                return loc_0;
            }
            final int[] loc_1 = new int[Tag.values().length];
            try {
                loc_1[Tag.DoubleConstant.ordinal()] = 5;
            }
            catch (NoSuchFieldError loc_2) {}
            try {
                loc_1[Tag.FieldReference.ordinal()] = 8;
            }
            catch (NoSuchFieldError loc_3) {}
            try {
                loc_1[Tag.FloatConstant.ordinal()] = 3;
            }
            catch (NoSuchFieldError loc_4) {}
            try {
                loc_1[Tag.IntegerConstant.ordinal()] = 2;
            }
            catch (NoSuchFieldError loc_5) {}
            try {
                loc_1[Tag.InterfaceMethodReference.ordinal()] = 10;
            }
            catch (NoSuchFieldError loc_6) {}
            try {
                loc_1[Tag.InvokeDynamicInfo.ordinal()] = 14;
            }
            catch (NoSuchFieldError loc_7) {}
            try {
                loc_1[Tag.LongConstant.ordinal()] = 4;
            }
            catch (NoSuchFieldError loc_8) {}
            try {
                loc_1[Tag.MethodHandle.ordinal()] = 12;
            }
            catch (NoSuchFieldError loc_9) {}
            try {
                loc_1[Tag.MethodReference.ordinal()] = 9;
            }
            catch (NoSuchFieldError loc_10) {}
            try {
                loc_1[Tag.MethodType.ordinal()] = 13;
            }
            catch (NoSuchFieldError loc_11) {}
            try {
                loc_1[Tag.NameAndTypeDescriptor.ordinal()] = 11;
            }
            catch (NoSuchFieldError loc_12) {}
            try {
                loc_1[Tag.StringConstant.ordinal()] = 7;
            }
            catch (NoSuchFieldError loc_13) {}
            try {
                loc_1[Tag.TypeInfo.ordinal()] = 6;
            }
            catch (NoSuchFieldError loc_14) {}
            try {
                loc_1[Tag.Utf8StringConstant.ordinal()] = 1;
            }
            catch (NoSuchFieldError loc_15) {}
            return MethodHandleEntry.$SWITCH_TABLE$com$strobel$assembler$ir$ConstantPool$Tag = loc_1;
        }
    }
    
    public static class NameAndTypeDescriptorEntry extends Entry
    {
        public final int nameIndex;
        public final int typeDescriptorIndex;
        
        public NameAndTypeDescriptorEntry(final ConstantPool owner, final int nameIndex, final int typeDescriptorIndex) {
            super(owner);
            this.nameIndex = nameIndex;
            this.typeDescriptorIndex = typeDescriptorIndex;
            ConstantPool.access$3(owner).set(this.getTag(), nameIndex, typeDescriptorIndex);
            ConstantPool.access$4(owner).put(ConstantPool.access$3(owner).clone(), this);
            ConstantPool.access$3(owner).clear();
        }
        
        @Override
        void fixupKey(final Key key) {
            key.set(Tag.NameAndTypeDescriptor, this.nameIndex, this.typeDescriptorIndex);
        }
        
        @Override
        public Tag getTag() {
            return Tag.NameAndTypeDescriptor;
        }
        
        @Override
        public int byteLength() {
            return 5;
        }
        
        public String getName() {
            return ((Utf8StringConstantEntry)this.owner.get(this.nameIndex, Tag.Utf8StringConstant)).value;
        }
        
        public String getType() {
            return ((Utf8StringConstantEntry)this.owner.get(this.typeDescriptorIndex, Tag.Utf8StringConstant)).value;
        }
        
        @Override
        public void accept(final Visitor visitor) {
            visitor.visitNameAndTypeDescriptor(this);
        }
        
        @Override
        public String toString() {
            return "NameAndTypeDescriptorEntry[index: " + this.index + ", descriptorIndex: " + this.nameIndex + ", typeDescriptorIndex: " + this.typeDescriptorIndex + "]";
        }
    }
    
    public static class InvokeDynamicInfoEntry extends Entry
    {
        public final int bootstrapMethodAttributeIndex;
        public final int nameAndTypeDescriptorIndex;
        
        public InvokeDynamicInfoEntry(final ConstantPool owner, final int bootstrapMethodAttributeIndex, final int nameAndTypeDescriptorIndex) {
            super(owner);
            this.bootstrapMethodAttributeIndex = bootstrapMethodAttributeIndex;
            this.nameAndTypeDescriptorIndex = nameAndTypeDescriptorIndex;
            ConstantPool.access$3(owner).set(this.getTag(), bootstrapMethodAttributeIndex, nameAndTypeDescriptorIndex);
            ConstantPool.access$4(owner).put(ConstantPool.access$3(owner).clone(), this);
            ConstantPool.access$3(owner).clear();
        }
        
        @Override
        void fixupKey(final Key key) {
            key.set(Tag.InvokeDynamicInfo, this.bootstrapMethodAttributeIndex, this.nameAndTypeDescriptorIndex);
        }
        
        @Override
        public Tag getTag() {
            return Tag.InvokeDynamicInfo;
        }
        
        @Override
        public int byteLength() {
            return 5;
        }
        
        public NameAndTypeDescriptorEntry getNameAndTypeDescriptor() {
            return (NameAndTypeDescriptorEntry)this.owner.get(this.nameAndTypeDescriptorIndex, Tag.NameAndTypeDescriptor);
        }
        
        @Override
        public void accept(final Visitor visitor) {
            visitor.visitInvokeDynamicInfo(this);
        }
        
        @Override
        public String toString() {
            return "InvokeDynamicInfoEntry[bootstrapMethodAttributeIndex: " + this.bootstrapMethodAttributeIndex + ", nameAndTypeDescriptorIndex: " + this.nameAndTypeDescriptorIndex + "]";
        }
    }
    
    public static final class DoubleConstantEntry extends ConstantEntry
    {
        public final double value;
        
        public DoubleConstantEntry(final ConstantPool owner, final double value) {
            super(owner);
            this.value = value;
            ConstantPool.access$3(owner).set(value);
            ConstantPool.access$4(owner).put(ConstantPool.access$3(owner).clone(), this);
            ConstantPool.access$3(owner).clear();
        }
        
        @Override
        void fixupKey(final Key key) {
            key.set(this.value);
        }
        
        @Override
        public Tag getTag() {
            return Tag.DoubleConstant;
        }
        
        @Override
        public int size() {
            return 2;
        }
        
        @Override
        public int byteLength() {
            return 9;
        }
        
        @Override
        public void accept(final Visitor visitor) {
            visitor.visitDoubleConstant(this);
        }
        
        @Override
        public String toString() {
            return "DoubleConstantEntry[index: " + this.index + ", value: " + this.value + "]";
        }
        
        @Override
        public Object getConstantValue() {
            return this.value;
        }
    }
    
    public static final class FloatConstantEntry extends ConstantEntry
    {
        public final float value;
        
        public FloatConstantEntry(final ConstantPool owner, final float value) {
            super(owner);
            this.value = value;
            ConstantPool.access$3(owner).set(value);
            ConstantPool.access$4(owner).put(ConstantPool.access$3(owner).clone(), this);
            ConstantPool.access$3(owner).clear();
        }
        
        @Override
        void fixupKey(final Key key) {
            key.set(this.value);
        }
        
        @Override
        public Tag getTag() {
            return Tag.FloatConstant;
        }
        
        @Override
        public int byteLength() {
            return 5;
        }
        
        @Override
        public void accept(final Visitor visitor) {
            visitor.visitFloatConstant(this);
        }
        
        @Override
        public String toString() {
            return "FloatConstantEntry[index: " + this.index + ", value: " + this.value + "]";
        }
        
        @Override
        public Object getConstantValue() {
            return this.value;
        }
    }
    
    public static final class IntegerConstantEntry extends ConstantEntry
    {
        public final int value;
        
        public IntegerConstantEntry(final ConstantPool owner, final int value) {
            super(owner);
            this.value = value;
            ConstantPool.access$3(owner).set(value);
            ConstantPool.access$4(owner).put(ConstantPool.access$3(owner).clone(), this);
            ConstantPool.access$3(owner).clear();
        }
        
        @Override
        void fixupKey(final Key key) {
            key.set(this.value);
        }
        
        @Override
        public Tag getTag() {
            return Tag.IntegerConstant;
        }
        
        @Override
        public int byteLength() {
            return 5;
        }
        
        @Override
        public void accept(final Visitor visitor) {
            visitor.visitIntegerConstant(this);
        }
        
        @Override
        public String toString() {
            return "IntegerConstantEntry[index: " + this.index + ", value: " + this.value + "]";
        }
        
        @Override
        public Object getConstantValue() {
            return this.value;
        }
    }
    
    public static final class LongConstantEntry extends ConstantEntry
    {
        public final long value;
        
        public LongConstantEntry(final ConstantPool owner, final long value) {
            super(owner);
            this.value = value;
            ConstantPool.access$3(owner).set(value);
            ConstantPool.access$4(owner).put(ConstantPool.access$3(owner).clone(), this);
            ConstantPool.access$3(owner).clear();
        }
        
        @Override
        void fixupKey(final Key key) {
            key.set(this.value);
        }
        
        @Override
        public Tag getTag() {
            return Tag.LongConstant;
        }
        
        @Override
        public int byteLength() {
            return 9;
        }
        
        @Override
        public int size() {
            return 2;
        }
        
        @Override
        public void accept(final Visitor visitor) {
            visitor.visitLongConstant(this);
        }
        
        @Override
        public String toString() {
            return "LongConstantEntry[index: " + this.index + ", value: " + this.value + "]";
        }
        
        @Override
        public Object getConstantValue() {
            return this.value;
        }
    }
    
    public static final class StringConstantEntry extends ConstantEntry
    {
        public final int stringIndex;
        
        public StringConstantEntry(final ConstantPool owner, final int stringIndex) {
            super(owner);
            this.stringIndex = stringIndex;
            ConstantPool.access$3(owner).set(this.getTag(), stringIndex);
            ConstantPool.access$4(owner).put(ConstantPool.access$3(owner).clone(), this);
            ConstantPool.access$3(owner).clear();
        }
        
        public String getValue() {
            return ((Utf8StringConstantEntry)this.owner.get(this.stringIndex)).value;
        }
        
        @Override
        void fixupKey(final Key key) {
            key.set(Tag.StringConstant, this.stringIndex);
        }
        
        @Override
        public Tag getTag() {
            return Tag.StringConstant;
        }
        
        @Override
        public int byteLength() {
            return 3;
        }
        
        @Override
        public void accept(final Visitor visitor) {
            visitor.visitStringConstant(this);
        }
        
        @Override
        public String toString() {
            return "StringConstantEntry[index: " + this.index + ", stringIndex: " + this.stringIndex + "]";
        }
        
        @Override
        public Object getConstantValue() {
            return this.getValue();
        }
    }
    
    public static final class Utf8StringConstantEntry extends ConstantEntry
    {
        public final String value;
        
        public Utf8StringConstantEntry(final ConstantPool owner, final String value) {
            super(owner);
            this.value = value;
            ConstantPool.access$3(owner).set(this.getTag(), value);
            ConstantPool.access$4(owner).put(ConstantPool.access$3(owner).clone(), this);
            ConstantPool.access$3(owner).clear();
        }
        
        @Override
        void fixupKey(final Key key) {
            key.set(this.value);
        }
        
        @Override
        public Tag getTag() {
            return Tag.Utf8StringConstant;
        }
        
        @Override
        public int byteLength() {
            class SizeOutputStream extends OutputStream
            {
                private int size;
                
                @Override
                public void write(final int b) {
                    ++this.size;
                }
                
                static /* synthetic */ int access$0(final SizeOutputStream param_0) {
                    return param_0.size;
                }
            }
            final SizeOutputStream sizeOut = new SizeOutputStream();
            final DataOutputStream out = new DataOutputStream(sizeOut);
            try {
                out.writeUTF(this.value);
            }
            catch (IOException loc_0) {}
            return 1 + SizeOutputStream.access$0(sizeOut);
        }
        
        @Override
        public void accept(final Visitor visitor) {
            visitor.visitUtf8StringConstant(this);
        }
        
        @Override
        public String toString() {
            return "Utf8StringConstantEntry[index: " + this.index + ", value: " + this.value + "]";
        }
        
        @Override
        public Object getConstantValue() {
            return this.value;
        }
    }
}
