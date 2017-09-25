package com.strobel.assembler.metadata;

import java.util.*;
import java.util.logging.*;
import java.util.zip.*;
import com.strobel.core.*;
import java.util.jar.*;
import java.io.*;
import com.strobel.assembler.ir.*;

public class JarTypeLoader implements ITypeLoader
{
    private static final Logger LOG;
    private final JarFile _jarFile;
    private final Map<String, String> _knownMappings;
    
    static {
        LOG = Logger.getLogger(JarTypeLoader.class.getSimpleName());
    }
    
    public JarTypeLoader(final JarFile jarFile) {
        super();
        this._jarFile = VerifyArgument.notNull(jarFile, "jarFile");
        this._knownMappings = new HashMap<String, String>();
    }
    
    @Override
    public boolean tryLoadType(final String internalName, final Buffer buffer) {
        try {
            if (JarTypeLoader.LOG.isLoggable(Level.FINE)) {
                JarTypeLoader.LOG.fine("Attempting to load type: " + internalName + "...");
            }
            final JarEntry entry = this._jarFile.getJarEntry(String.valueOf(internalName) + ".class");
            if (entry == null) {
                final String mappedName = this._knownMappings.get(internalName);
                return mappedName != null && !mappedName.equals(internalName) && this.tryLoadType(mappedName, buffer);
            }
            final InputStream inputStream = this._jarFile.getInputStream(entry);
            int remainingBytes = inputStream.available();
            buffer.reset(remainingBytes);
            while (remainingBytes > 0) {
                final int bytesRead = inputStream.read(buffer.array(), buffer.position(), remainingBytes);
                if (bytesRead < 0) {
                    break;
                }
                buffer.position(buffer.position() + bytesRead);
                remainingBytes -= bytesRead;
            }
            buffer.position(0);
            final String actualName = getInternalNameFromClassFile(buffer);
            if (actualName != null && !actualName.equals(internalName)) {
                this._knownMappings.put(actualName, internalName);
            }
            if (JarTypeLoader.LOG.isLoggable(Level.FINE)) {
                JarTypeLoader.LOG.fine("Type loaded from " + this._jarFile.getName() + "!" + entry.getName() + ".");
            }
            return true;
        }
        catch (IOException e) {
            throw ExceptionUtilities.asRuntimeException(e);
        }
    }
    
    private static String getInternalNameFromClassFile(final Buffer b) {
        final long magic = b.readInt() & 0xFFFFFFFFL;
        if (magic != 0xCAFEBABEL) {
            return null;
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
