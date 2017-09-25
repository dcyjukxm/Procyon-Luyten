package com.strobel.assembler.metadata;

import com.strobel.annotations.*;
import java.util.*;
import com.strobel.core.*;
import java.util.logging.*;
import com.strobel.assembler.ir.*;

public final class ArrayTypeLoader implements ITypeLoader
{
    private static final Logger LOG;
    private final Buffer _buffer;
    private Throwable _parseError;
    private boolean _parsed;
    private String _className;
    
    static {
        LOG = Logger.getLogger(ArrayTypeLoader.class.getSimpleName());
    }
    
    public ArrayTypeLoader(@NotNull final byte[] bytes) {
        super();
        VerifyArgument.notNull(bytes, "bytes");
        this._buffer = new Buffer(Arrays.copyOf(bytes, bytes.length));
    }
    
    public String getClassNameFromArray() {
        this.ensureParsed(true);
        return this._className;
    }
    
    @Override
    public boolean tryLoadType(final String internalName, final Buffer buffer) {
        this.ensureParsed(false);
        if (StringUtilities.equals(internalName, this._className)) {
            buffer.reset(this._buffer.size());
            buffer.putByteArray(this._buffer.array(), 0, this._buffer.size());
            buffer.position(0);
            return true;
        }
        return false;
    }
    
    private void ensureParsed(final boolean throwOnError) {
        if (!this._parsed) {
            if (ArrayTypeLoader.LOG.isLoggable(Level.FINE)) {
                ArrayTypeLoader.LOG.log(Level.FINE, "Parsing classfile header from user-provided buffer...");
            }
            try {
                this._className = getInternalNameFromClassFile(this._buffer);
                if (ArrayTypeLoader.LOG.isLoggable(Level.FINE)) {
                    ArrayTypeLoader.LOG.log(Level.FINE, "Parsed header for class: " + this._className);
                }
            }
            catch (Throwable t) {
                this._parseError = t;
                if (ArrayTypeLoader.LOG.isLoggable(Level.FINE)) {
                    ArrayTypeLoader.LOG.log(Level.FINE, "Error parsing classfile header.", t);
                }
                if (throwOnError) {
                    throw new IllegalStateException("Error parsing classfile header.", t);
                }
                return;
            }
            finally {
                this._parsed = true;
            }
            this._parsed = true;
            return;
        }
        if (throwOnError && this._parseError != null) {
            throw new IllegalStateException("Error parsing classfile header.", this._parseError);
        }
    }
    
    private static String getInternalNameFromClassFile(final Buffer b) {
        final long magic = b.readInt() & 0xFFFFFFFFL;
        if (magic != 0xCAFEBABEL) {
            throw new IllegalStateException("Bad magic number: 0x" + Long.toHexString(magic));
        }
        b.readUnsignedShort();
        b.readUnsignedShort();
        final ConstantPool constantPool = ConstantPool.read(b);
        b.readUnsignedShort();
        final ConstantPool.TypeInfoEntry thisClass = constantPool.getEntry(b.readUnsignedShort());
        b.position(0);
        return thisClass.getName();
    }
}
