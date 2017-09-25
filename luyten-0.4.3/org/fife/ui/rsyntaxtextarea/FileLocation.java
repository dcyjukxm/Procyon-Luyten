package org.fife.ui.rsyntaxtextarea;

import java.net.*;
import java.io.*;

public abstract class FileLocation
{
    public static FileLocation create(final String fileFullPath) {
        return new FileFileLocation(new File(fileFullPath));
    }
    
    public static FileLocation create(final File file) {
        return new FileFileLocation(file);
    }
    
    public static FileLocation create(final URL url) {
        if ("file".equalsIgnoreCase(url.getProtocol())) {
            return new FileFileLocation(new File(url.getPath()));
        }
        return new URLFileLocation(url);
    }
    
    protected abstract long getActualLastModified();
    
    public abstract String getFileFullPath();
    
    public abstract String getFileName();
    
    protected abstract InputStream getInputStream() throws IOException;
    
    protected abstract OutputStream getOutputStream() throws IOException;
    
    public abstract boolean isLocal();
    
    public abstract boolean isLocalAndExists();
    
    public boolean isRemote() {
        return !this.isLocal();
    }
}
