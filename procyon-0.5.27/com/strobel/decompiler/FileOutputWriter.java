package com.strobel.decompiler;

import java.nio.charset.*;
import java.io.*;

final class FileOutputWriter extends OutputStreamWriter
{
    private final File file;
    
    FileOutputWriter(final File file, final DecompilerSettings settings) throws IOException {
        super(new FileOutputStream(file), settings.isUnicodeOutputEnabled() ? Charset.forName("UTF-8") : Charset.defaultCharset());
        this.file = file;
    }
    
    public File getFile() {
        return this.file;
    }
}
