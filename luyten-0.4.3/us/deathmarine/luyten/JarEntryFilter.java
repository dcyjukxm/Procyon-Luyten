package us.deathmarine.luyten;

import java.util.jar.*;
import java.util.*;

public class JarEntryFilter
{
    private JarFile jfile;
    
    public JarEntryFilter() {
        super();
    }
    
    public JarEntryFilter(final JarFile jfile) {
        super();
        this.jfile = jfile;
    }
    
    public List<String> getAllEntriesFromJar() {
        final List<String> mass = new ArrayList<String>();
        final Enumeration<JarEntry> entries = this.jfile.entries();
        while (entries.hasMoreElements()) {
            final JarEntry e = entries.nextElement();
            if (!e.isDirectory()) {
                mass.add(e.getName());
            }
        }
        return mass;
    }
    
    public List<String> getEntriesWithoutInnerClasses() {
        final List<String> mass = new ArrayList<String>();
        final Enumeration<JarEntry> entries = this.jfile.entries();
        final Set<String> possibleInnerClasses = new HashSet<String>();
        final Set<String> baseClasses = new HashSet<String>();
        while (entries.hasMoreElements()) {
            final JarEntry e = entries.nextElement();
            if (!e.isDirectory()) {
                String entryName = e.getName();
                if (entryName == null || entryName.trim().length() <= 0) {
                    continue;
                }
                entryName = entryName.trim();
                if (!entryName.endsWith(".class")) {
                    mass.add(entryName);
                }
                else if (entryName.matches(".*[^(/|\\\\)]+\\$[^(/|\\\\)]+$")) {
                    possibleInnerClasses.add(entryName);
                }
                else {
                    baseClasses.add(entryName);
                    mass.add(entryName);
                }
            }
        }
        for (final String inner : possibleInnerClasses) {
            final String innerWithoutTail = inner.replaceAll("\\$[^(/|\\\\)]+\\.class$", "");
            if (!baseClasses.contains(String.valueOf(innerWithoutTail) + ".class")) {
                mass.add(inner);
            }
        }
        return mass;
    }
    
    public JarFile getJfile() {
        return this.jfile;
    }
    
    public void setJfile(final JarFile jfile) {
        this.jfile = jfile;
    }
}
