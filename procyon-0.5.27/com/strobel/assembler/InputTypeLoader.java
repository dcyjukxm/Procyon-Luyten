package com.strobel.assembler;

import com.strobel.assembler.metadata.*;
import java.util.logging.*;
import com.strobel.io.*;
import java.util.*;
import java.io.*;
import com.strobel.core.*;
import com.strobel.assembler.ir.*;

public class InputTypeLoader implements ITypeLoader
{
    private static final Logger LOG;
    private final ITypeLoader _defaultTypeLoader;
    private final Map<String, LinkedHashSet<File>> _packageLocations;
    private final Map<String, File> _knownFiles;
    
    static {
        LOG = Logger.getLogger(InputTypeLoader.class.getSimpleName());
    }
    
    public InputTypeLoader() {
        this(new ClasspathTypeLoader());
    }
    
    public InputTypeLoader(final ITypeLoader defaultTypeLoader) {
        super();
        this._defaultTypeLoader = VerifyArgument.notNull(defaultTypeLoader, "defaultTypeLoader");
        this._packageLocations = new LinkedHashMap<String, LinkedHashSet<File>>();
        this._knownFiles = new LinkedHashMap<String, File>();
    }
    
    @Override
    public boolean tryLoadType(final String typeNameOrPath, final Buffer buffer) {
        VerifyArgument.notNull(typeNameOrPath, "typeNameOrPath");
        VerifyArgument.notNull(buffer, "buffer");
        if (InputTypeLoader.LOG.isLoggable(Level.FINE)) {
            InputTypeLoader.LOG.fine("Attempting to load type: " + typeNameOrPath + "...");
        }
        final boolean hasExtension = StringUtilities.endsWithIgnoreCase(typeNameOrPath, ".class");
        if (hasExtension && this.tryLoadFile(null, typeNameOrPath, buffer, true)) {
            return true;
        }
        if (PathHelper.isPathRooted(typeNameOrPath)) {
            if (InputTypeLoader.LOG.isLoggable(Level.FINER)) {
                InputTypeLoader.LOG.finer("Failed to load type: " + typeNameOrPath + ".");
            }
            return false;
        }
        String internalName = hasExtension ? typeNameOrPath.substring(0, typeNameOrPath.length() - 6) : typeNameOrPath.replace('.', '/');
        if (this.tryLoadTypeFromName(internalName, buffer)) {
            return true;
        }
        if (hasExtension) {
            if (InputTypeLoader.LOG.isLoggable(Level.FINER)) {
                InputTypeLoader.LOG.finer("Failed to load type: " + typeNameOrPath + ".");
            }
            return false;
        }
        for (int lastDelimiter = internalName.lastIndexOf(47); lastDelimiter != -1; lastDelimiter = internalName.lastIndexOf(47)) {
            internalName = String.valueOf(internalName.substring(0, lastDelimiter)) + "$" + internalName.substring(lastDelimiter + 1);
            if (this.tryLoadTypeFromName(internalName, buffer)) {
                return true;
            }
        }
        if (InputTypeLoader.LOG.isLoggable(Level.FINER)) {
            InputTypeLoader.LOG.finer("Failed to load type: " + typeNameOrPath + ".");
        }
        return false;
    }
    
    private boolean tryLoadTypeFromName(final String internalName, final Buffer buffer) {
        if (this.tryLoadFromKnownLocation(internalName, buffer)) {
            return true;
        }
        if (this._defaultTypeLoader.tryLoadType(internalName, buffer)) {
            return true;
        }
        final String filePath = String.valueOf(internalName.replace('/', File.separatorChar)) + ".class";
        if (this.tryLoadFile(internalName, filePath, buffer, false)) {
            return true;
        }
        final int lastSeparatorIndex = filePath.lastIndexOf(File.separatorChar);
        return lastSeparatorIndex >= 0 && this.tryLoadFile(internalName, filePath.substring(lastSeparatorIndex + 1), buffer, true);
    }
    
    private boolean tryLoadFromKnownLocation(final String internalName, final Buffer buffer) {
        final File knownFile = this._knownFiles.get(internalName);
        if (knownFile != null && this.tryLoadFile(knownFile, buffer)) {
            if (InputTypeLoader.LOG.isLoggable(Level.FINE)) {
                InputTypeLoader.LOG.fine("Type loaded from " + knownFile.getAbsolutePath() + ".");
            }
            return true;
        }
        final int packageEnd = internalName.lastIndexOf(47);
        String head;
        String tail;
        if (packageEnd < 0 || packageEnd >= internalName.length()) {
            head = "";
            tail = internalName;
        }
        else {
            head = internalName.substring(0, packageEnd);
            tail = internalName.substring(packageEnd + 1);
        }
        while (true) {
            final LinkedHashSet<File> directories = this._packageLocations.get(head);
            if (directories != null) {
                for (final File directory : directories) {
                    if (this.tryLoadFile(internalName, new File(directory, String.valueOf(tail) + ".class").getAbsolutePath(), buffer, true)) {
                        return true;
                    }
                }
            }
            final int split = head.lastIndexOf(47);
            if (split <= 0) {
                return false;
            }
            tail = String.valueOf(head.substring(split + 1)) + '/' + tail;
            head = head.substring(0, split);
        }
    }
    
    private boolean tryLoadFile(final File file, final Buffer buffer) {
        if (InputTypeLoader.LOG.isLoggable(Level.FINER)) {
            InputTypeLoader.LOG.finer("Probing for file: " + file.getAbsolutePath() + "...");
        }
        if (!file.exists() || file.isDirectory()) {
            return false;
        }
        try {
            Throwable loc_0 = null;
            try {
                final FileInputStream in = new FileInputStream(file);
                try {
                    int remainingBytes = in.available();
                    buffer.position(0);
                    buffer.reset(remainingBytes);
                    while (remainingBytes > 0) {
                        final int bytesRead = in.read(buffer.array(), buffer.position(), remainingBytes);
                        if (bytesRead < 0) {
                            break;
                        }
                        remainingBytes -= bytesRead;
                        buffer.advance(bytesRead);
                    }
                    buffer.position(0);
                    return true;
                }
                finally {
                    if (in != null) {
                        in.close();
                    }
                }
            }
            finally {
                if (loc_0 == null) {
                    final Throwable loc_1;
                    loc_0 = loc_1;
                }
                else {
                    final Throwable loc_1;
                    if (loc_0 != loc_1) {
                        loc_0.addSuppressed(loc_1);
                    }
                }
            }
        }
        catch (IOException e) {
            return false;
        }
    }
    
    private boolean tryLoadFile(final String internalName, final String typeNameOrPath, final Buffer buffer, final boolean trustName) {
        final File file = new File(typeNameOrPath);
        if (!this.tryLoadFile(file, buffer)) {
            return false;
        }
        final String actualName = getInternalNameFromClassFile(buffer);
        final String name = trustName ? ((internalName != null) ? internalName : actualName) : actualName;
        if (name == null) {
            return false;
        }
        final boolean nameMatches = StringUtilities.equals(actualName, internalName);
        final boolean pathMatchesName = typeNameOrPath.endsWith(String.valueOf(name.replace('/', File.separatorChar)) + ".class");
        final boolean result = internalName == null || pathMatchesName || nameMatches;
        if (result) {
            final int packageEnd = name.lastIndexOf(47);
            String packageName;
            if (packageEnd < 0 || packageEnd >= name.length()) {
                packageName = "";
            }
            else {
                packageName = name.substring(0, packageEnd);
            }
            this.registerKnownPath(packageName, file.getParentFile(), pathMatchesName);
            this._knownFiles.put(actualName, file);
            if (InputTypeLoader.LOG.isLoggable(Level.FINE)) {
                InputTypeLoader.LOG.fine("Type loaded from " + file.getAbsolutePath() + ".");
            }
        }
        else {
            buffer.reset(0);
        }
        return result;
    }
    
    private void registerKnownPath(final String packageName, final File directory, final boolean recursive) {
        if (directory == null || !directory.exists()) {
            return;
        }
        LinkedHashSet<File> directories = this._packageLocations.get(packageName);
        if (directories == null) {
            this._packageLocations.put(packageName, directories = new LinkedHashSet<File>());
        }
        if (!directories.add(directory) || !recursive) {
            return;
        }
        try {
            final String directoryPath = StringUtilities.removeRight(directory.getCanonicalPath(), new char[] { PathHelper.DirectorySeparator, PathHelper.AlternateDirectorySeparator }).replace('\\', '/');
            String currentPackage = packageName;
            File currentDirectory = new File(directoryPath);
            int delimiterIndex;
            while ((delimiterIndex = currentPackage.lastIndexOf(47)) >= 0 && currentDirectory.exists()) {
                if (delimiterIndex >= currentPackage.length() - 1) {
                    break;
                }
                final String segmentName = currentPackage.substring(delimiterIndex + 1);
                if (!StringUtilities.equals(currentDirectory.getName(), segmentName, StringComparison.OrdinalIgnoreCase)) {
                    break;
                }
                currentPackage = currentPackage.substring(0, delimiterIndex);
                currentDirectory = currentDirectory.getParentFile();
                directories = this._packageLocations.get(currentPackage);
                if (directories == null) {
                    this._packageLocations.put(currentPackage, directories = new LinkedHashSet<File>());
                }
                if (!directories.add(currentDirectory)) {
                    break;
                }
            }
        }
        catch (IOException loc_0) {}
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
