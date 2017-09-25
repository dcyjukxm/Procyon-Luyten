package com.strobel.assembler.metadata;

import com.strobel.core.*;
import java.util.regex.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.logging.*;
import java.io.*;
import sun.misc.*;

public final class ClasspathTypeLoader implements ITypeLoader
{
    private static final Logger LOG;
    private final URLClassPath _classPath;
    
    static {
        LOG = Logger.getLogger(ClasspathTypeLoader.class.getSimpleName());
    }
    
    public ClasspathTypeLoader() {
        this(StringUtilities.join(System.getProperty("path.separator"), System.getProperty("java.class.path"), System.getProperty("sun.boot.class.path")));
    }
    
    public ClasspathTypeLoader(final String classPath) {
        super();
        final String[] parts = VerifyArgument.notNull(classPath, "classPath").split(Pattern.quote(System.getProperty("path.separator")));
        final URL[] urls = new URL[parts.length];
        for (int i = 0; i < parts.length; ++i) {
            try {
                urls[i] = new File(parts[i]).toURI().toURL();
            }
            catch (MalformedURLException e) {
                throw new UndeclaredThrowableException(e);
            }
        }
        this._classPath = new URLClassPath(urls);
    }
    
    @Override
    public boolean tryLoadType(final String internalName, final Buffer buffer) {
        if (ClasspathTypeLoader.LOG.isLoggable(Level.FINE)) {
            ClasspathTypeLoader.LOG.fine("Attempting to load type: " + internalName + "...");
        }
        final String path = internalName.concat(".class");
        final Resource resource = this._classPath.getResource(path, false);
        if (resource == null) {
            return false;
        }
        byte[] data;
        try {
            data = resource.getBytes();
            assert data.length == resource.getContentLength();
        }
        catch (IOException e) {
            return false;
        }
        buffer.reset(data.length);
        System.arraycopy(data, 0, buffer.array(), 0, data.length);
        if (ClasspathTypeLoader.LOG.isLoggable(Level.FINE)) {
            ClasspathTypeLoader.LOG.fine("Type loaded from " + resource.getURL() + ".");
        }
        return true;
    }
}
