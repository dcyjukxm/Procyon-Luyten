package com.strobel.io;

import com.strobel.util.*;
import java.io.*;
import com.strobel.core.*;

public final class PathHelper
{
    public static final char DirectorySeparator;
    public static final char AlternateDirectorySeparator;
    public static final char VolumeSeparator;
    private static final int maxPath = 260;
    private static final int maxDirectoryLength = 255;
    private static final char[] invalidPathCharacters;
    private static final char[] invalidFileNameCharacters;
    private static final char[] trimEndChars;
    private static final boolean isWindows;
    
    static {
        invalidPathCharacters = new char[] { '\"', '<', '>', '|', '\0', '\u0001', '\u0002', '\u0003', '\u0004', '\u0005', '\u0006', '\u0007', '\b', '\t', '\n', '\u000b', '\f', '\r', '\u000e', '\u000f', '\u0010', '\u0011', '\u0012', '\u0013', '\u0014', '\u0015', '\u0016', '\u0017', '\u0018', '\u0019', '\u001a', '\u001b', '\u001c', '\u001d', '\u001e', '\u001f' };
        invalidFileNameCharacters = new char[] { '\"', '<', '>', '|', '\0', '\u0001', '\u0002', '\u0003', '\u0004', '\u0005', '\u0006', '\u0007', '\b', '\t', '\n', '\u000b', '\f', '\r', '\u000e', '\u000f', '\u0010', '\u0011', '\u0012', '\u0013', '\u0014', '\u0015', '\u0016', '\u0017', '\u0018', '\u0019', '\u001a', '\u001b', '\u001c', '\u001d', '\u001e', '\u001f', ':', '*', '?', '\\', '/' };
        trimEndChars = new char[] { '\t', '\n', '\u000b', '\f', '\r', ' ', '\u0085', ' ' };
        final String osName = System.getProperty("os.name");
        isWindows = (osName != null && StringUtilities.startsWithIgnoreCase(osName, "windows"));
        if (PathHelper.isWindows) {
            DirectorySeparator = '\\';
            AlternateDirectorySeparator = '/';
            VolumeSeparator = ':';
        }
        else {
            DirectorySeparator = '/';
            AlternateDirectorySeparator = '\\';
            VolumeSeparator = '/';
        }
    }
    
    private PathHelper() {
        super();
        throw ContractUtils.unreachable();
    }
    
    public static char[] getInvalidPathCharacters() {
        return PathHelper.invalidPathCharacters.clone();
    }
    
    public static char[] getInvalidFileNameCharacters() {
        return PathHelper.invalidFileNameCharacters;
    }
    
    public static boolean isPathRooted(final String path) {
        if (StringUtilities.isNullOrEmpty(path)) {
            return false;
        }
        final int length = path.length();
        return path.charAt(0) == PathHelper.DirectorySeparator || path.charAt(0) == PathHelper.AlternateDirectorySeparator || (PathHelper.isWindows && length >= 2 && path.charAt(1) == PathHelper.VolumeSeparator);
    }
    
    public static String combine(final String path1, final String path2) {
        if (path1 == null) {
            return (path2 != null) ? path2 : "";
        }
        if (path2 == null) {
            return path1;
        }
        checkInvalidPathChars(path1);
        checkInvalidPathChars(path2);
        return combineUnsafe(path1, path2);
    }
    
    public static String combine(final String path1, final String path2, final String path3) {
        return combine(combine(path1, path2), path3);
    }
    
    public static String combine(final String... paths) {
        if (ArrayUtilities.isNullOrEmpty(paths)) {
            return "";
        }
        int finalSize = 0;
        int firstComponent = 0;
        for (int i = 0; i < paths.length; ++i) {
            final String path = paths[i];
            if (!StringUtilities.isNullOrEmpty(path)) {
                checkInvalidPathChars(path);
                final int length = path.length();
                if (isPathRooted(path)) {
                    firstComponent = i;
                    finalSize = length;
                }
                else {
                    finalSize += length;
                }
                final char ch = path.charAt(length - 1);
                if (ch != PathHelper.DirectorySeparator && ch != PathHelper.AlternateDirectorySeparator && ch != PathHelper.VolumeSeparator) {
                    ++finalSize;
                }
            }
        }
        if (finalSize == 0) {
            return "";
        }
        final StringBuilder finalPath = new StringBuilder(finalSize);
        for (int j = firstComponent; j < paths.length; ++j) {
            final String path2 = paths[j];
            if (!StringUtilities.isNullOrEmpty(path2)) {
                final int length2 = finalPath.length();
                if (length2 == 0) {
                    finalPath.append(path2);
                }
                else {
                    final char ch2 = finalPath.charAt(length2 - 1);
                    if (ch2 != PathHelper.DirectorySeparator && ch2 != PathHelper.AlternateDirectorySeparator && ch2 != PathHelper.VolumeSeparator) {
                        finalPath.append(PathHelper.DirectorySeparator);
                    }
                    finalPath.append(path2);
                }
            }
        }
        return finalPath.toString();
    }
    
    public static String getDirectoryName(final String path) {
        if (StringUtilities.isNullOrEmpty(path)) {
            return "";
        }
        checkInvalidPathChars(path);
        final String normalizedPath = normalizePath(path, false, 260);
        final int root = getRootLength(normalizedPath);
        int i = normalizedPath.length();
        if (i <= root) {
            return normalizedPath;
        }
        i = normalizedPath.length();
        if (i == root) {
            return null;
        }
        while (i > root && !isDirectorySeparator(normalizedPath.charAt(--i))) {}
        return normalizedPath.substring(0, i);
    }
    
    public static String getFileName(final String path) {
        if (StringUtilities.isNullOrEmpty(path)) {
            return "";
        }
        checkInvalidPathChars(path);
        int i;
        final int length = i = path.length();
        while (--i >= 0) {
            final char ch = path.charAt(i);
            if (isDirectorySeparator(ch) || ch == PathHelper.VolumeSeparator) {
                return path.substring(i + 1, length);
            }
        }
        return path;
    }
    
    public static String getFileNameWithoutExtension(final String path) {
        final String fileName = getFileName(path);
        if (StringUtilities.isNullOrEmpty(fileName)) {
            return fileName;
        }
        if (fileName == null) {
            return null;
        }
        final int dotPosition = fileName.lastIndexOf(46);
        if (dotPosition == -1) {
            return fileName;
        }
        return fileName.substring(0, dotPosition);
    }
    
    public static String getFullPath(final String path) {
        if (StringUtilities.isNullOrEmpty(path)) {
            return "";
        }
        return normalizePath(path, true, 260);
    }
    
    public static String getTempPath() {
        return getFullPath(System.getProperty("java.io.tmpdir"));
    }
    
    private static String combineUnsafe(final String path1, final String path2) {
        if (path2.length() == 0) {
            return path1;
        }
        if (path1.length() == 0) {
            return path2;
        }
        if (isPathRooted(path2)) {
            return path2;
        }
        final char ch = path1.charAt(path1.length() - 1);
        if (ch != PathHelper.DirectorySeparator && ch != PathHelper.AlternateDirectorySeparator && ch != PathHelper.VolumeSeparator) {
            return String.valueOf(path1) + PathHelper.DirectorySeparator + path2;
        }
        return String.valueOf(path1) + path2;
    }
    
    private static void checkInvalidPathChars(final String path) {
        if (!PathHelper.isWindows && path.length() >= 2 && path.charAt(0) == '\\' && path.charAt(1) == '\\') {
            throw Error.invalidPathCharacters();
        }
        for (int i = 0; i < path.length(); ++i) {
            final int ch = path.charAt(i);
            if (ch == 34 || ch == 60 || ch == 62 || ch == 124 || ch < 32) {
                throw Error.invalidPathCharacters();
            }
        }
    }
    
    private static boolean isDirectorySeparator(final char ch) {
        return ch == PathHelper.DirectorySeparator || ch == PathHelper.AlternateDirectorySeparator;
    }
    
    private static int getRootLength(final String path) {
        checkInvalidPathChars(path);
        int i = 0;
        final int length = path.length();
        if (PathHelper.isWindows) {
            if (length >= 1 && isDirectorySeparator(path.charAt(0))) {
                i = 1;
                if (length >= 2 && isDirectorySeparator(path.charAt(1))) {
                    i = 2;
                    int n = 2;
                    while (i < length) {
                        if (isDirectorySeparator(path.charAt(i)) && --n <= 0) {
                            break;
                        }
                        ++i;
                    }
                }
            }
            else if (length >= 2 && path.charAt(1) == PathHelper.VolumeSeparator) {
                i = 2;
                if (length >= 3 && isDirectorySeparator(path.charAt(2))) {
                    ++i;
                }
            }
        }
        else if (length >= 1 && isDirectorySeparator(path.charAt(0))) {
            i = 1;
        }
        return i;
    }
    
    private static String normalizePath(final String p, final boolean fullCheck, final int maxPathLength) {
        String path;
        if (fullCheck) {
            path = StringUtilities.trimAndRemoveRight(p, PathHelper.trimEndChars);
            checkInvalidPathChars(path);
        }
        else {
            path = p;
        }
        int index = 0;
        final StringBuilder newBuffer = new StringBuilder(path.length() + 260);
        int spaceCount = 0;
        int dotCount = 0;
        boolean fixupDirectorySeparator = false;
        int significantCharCount = 0;
        int lastSignificantChar = -1;
        boolean startedWithVolumeSeparator = false;
        boolean firstSegment = true;
        int lastSeparatorPosition = 0;
        if (PathHelper.isWindows && path.length() > 0 && (path.charAt(0) == PathHelper.DirectorySeparator || path.charAt(0) == PathHelper.AlternateDirectorySeparator)) {
            newBuffer.append('\\');
            ++index;
            lastSignificantChar = 0;
        }
        while (index < path.length()) {
            final char currentChar = path.charAt(index);
            if (currentChar == PathHelper.DirectorySeparator || currentChar == PathHelper.AlternateDirectorySeparator) {
                if (significantCharCount == 0) {
                    if (dotCount > 0) {
                        final int start = lastSignificantChar + 1;
                        if (path.charAt(start) != '.') {
                            throw Error.illegalPath();
                        }
                        if (dotCount >= 2) {
                            if (startedWithVolumeSeparator && dotCount > 2) {
                                throw Error.illegalPath();
                            }
                            if (path.charAt(start + 1) == '.') {
                                for (int i = start + 2; i < start + dotCount; ++i) {
                                    if (path.charAt(i) != '.') {
                                        throw Error.illegalPath();
                                    }
                                }
                                dotCount = 2;
                            }
                            else {
                                if (dotCount > 1) {
                                    throw Error.illegalPath();
                                }
                                dotCount = 1;
                            }
                        }
                        if (dotCount == 2) {
                            newBuffer.append('.');
                        }
                        newBuffer.append('.');
                        fixupDirectorySeparator = false;
                    }
                    if (spaceCount > 0 && firstSegment && index + 1 < path.length() && (path.charAt(index + 1) == PathHelper.DirectorySeparator || path.charAt(index + 1) == PathHelper.AlternateDirectorySeparator)) {
                        newBuffer.append(PathHelper.DirectorySeparator);
                    }
                }
                dotCount = 0;
                spaceCount = 0;
                if (!fixupDirectorySeparator) {
                    fixupDirectorySeparator = true;
                    newBuffer.append(PathHelper.DirectorySeparator);
                }
                significantCharCount = 0;
                lastSignificantChar = index;
                startedWithVolumeSeparator = false;
                firstSegment = false;
                final int thisPos = newBuffer.length() - 1;
                if (thisPos - lastSeparatorPosition > 255) {
                    throw Error.pathTooLong();
                }
                lastSeparatorPosition = thisPos;
            }
            else if (currentChar == '.') {
                ++dotCount;
            }
            else if (currentChar == ' ') {
                ++spaceCount;
            }
            else {
                fixupDirectorySeparator = false;
                if (PathHelper.isWindows && firstSegment && currentChar == PathHelper.VolumeSeparator) {
                    final char driveLetter = (index > 0) ? path.charAt(index - 1) : ' ';
                    final boolean validPath = dotCount == 0 && significantCharCount >= 1 && driveLetter != ' ';
                    if (!validPath) {
                        throw Error.illegalPath();
                    }
                    startedWithVolumeSeparator = true;
                    if (significantCharCount > 1) {
                        int tempSpaceCount;
                        for (tempSpaceCount = 0; tempSpaceCount < newBuffer.length() && newBuffer.charAt(tempSpaceCount) == ' '; ++tempSpaceCount) {}
                        if (significantCharCount - tempSpaceCount == 1) {
                            newBuffer.setLength(0);
                            newBuffer.append(driveLetter);
                        }
                    }
                    significantCharCount = 0;
                }
                else {
                    significantCharCount += 1 + dotCount + spaceCount;
                }
                if (dotCount > 0 || spaceCount > 0) {
                    final int copyLength = (lastSignificantChar >= 0) ? (index - lastSignificantChar - 1) : index;
                    if (copyLength > 0) {
                        for (int i = 0; i < copyLength; ++i) {
                            newBuffer.append(path.charAt(lastSignificantChar + 1 + i));
                        }
                    }
                    dotCount = 0;
                    spaceCount = 0;
                }
                newBuffer.append(currentChar);
                lastSignificantChar = index;
            }
            ++index;
        }
        if (newBuffer.length() - 1 - lastSeparatorPosition > 255) {
            throw Error.pathTooLong();
        }
        if (significantCharCount == 0 && dotCount > 0) {
            final int start2 = lastSignificantChar + 1;
            if (path.charAt(start2) != '.') {
                throw Error.illegalPath();
            }
            if (dotCount >= 2) {
                if (startedWithVolumeSeparator && dotCount > 2) {
                    throw Error.illegalPath();
                }
                if (path.charAt(start2 + 1) == '.') {
                    for (int j = start2 + 2; j < start2 + dotCount; ++j) {
                        if (path.charAt(j) != '.') {
                            throw Error.illegalPath();
                        }
                    }
                    dotCount = 2;
                }
                else {
                    if (dotCount > 1) {
                        throw Error.illegalPath();
                    }
                    dotCount = 1;
                }
            }
            if (dotCount == 2) {
                newBuffer.append("..");
            }
            else if (start2 == 0) {
                newBuffer.append('.');
            }
        }
        if (newBuffer.length() == 0) {
            throw Error.illegalPath();
        }
        if (fullCheck && (StringUtilities.startsWith(newBuffer, "http:") || StringUtilities.startsWith(newBuffer, "file:"))) {
            throw Error.pathUriFormatNotSupported();
        }
        int normalizedLength = newBuffer.length();
        if (normalizedLength > 1 && newBuffer.charAt(0) == '\\' && newBuffer.charAt(1) == '\\') {
            int startIndex;
            for (startIndex = 2; startIndex < normalizedLength; ++startIndex) {
                if (newBuffer.charAt(startIndex) == '\\') {
                    ++startIndex;
                    break;
                }
            }
            if (startIndex == normalizedLength) {
                throw Error.illegalUncPath();
            }
        }
        if (fullCheck) {
            final String temp = newBuffer.toString();
            newBuffer.setLength(0);
            try {
                newBuffer.append(new File(temp).getCanonicalPath());
            }
            catch (IOException e) {
                throw Error.canonicalizationError(e);
            }
            normalizedLength = newBuffer.length();
        }
        if (newBuffer.length() >= maxPathLength) {
            throw Error.pathTooLong();
        }
        if (normalizedLength == 0) {
            return "";
        }
        String returnVal = newBuffer.toString();
        if (StringUtilities.equals(returnVal, path, StringComparison.Ordinal)) {
            returnVal = path;
        }
        return returnVal;
    }
}
