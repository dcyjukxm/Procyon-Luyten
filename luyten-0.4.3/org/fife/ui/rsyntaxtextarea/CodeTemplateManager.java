package org.fife.ui.rsyntaxtextarea;

import org.fife.ui.rsyntaxtextarea.templates.*;
import javax.swing.text.*;
import java.util.*;
import java.beans.*;
import java.io.*;

public class CodeTemplateManager
{
    private int maxTemplateIDLength;
    private List<CodeTemplate> templates;
    private Segment s;
    private TemplateComparator comparator;
    private File directory;
    
    public CodeTemplateManager() {
        super();
        this.s = new Segment();
        this.comparator = new TemplateComparator();
        this.templates = new ArrayList<CodeTemplate>();
    }
    
    public synchronized void addTemplate(final CodeTemplate template) {
        if (template == null) {
            throw new IllegalArgumentException("template cannot be null");
        }
        this.templates.add(template);
        this.sortTemplates();
    }
    
    public synchronized CodeTemplate getTemplate(final RSyntaxTextArea textArea) {
        final int caretPos = textArea.getCaretPosition();
        final int charsToGet = Math.min(caretPos, this.maxTemplateIDLength);
        try {
            final Document doc = textArea.getDocument();
            doc.getText(caretPos - charsToGet, charsToGet, this.s);
            final int index = Collections.binarySearch(this.templates, this.s, this.comparator);
            return (index >= 0) ? this.templates.get(index) : null;
        }
        catch (BadLocationException ble) {
            ble.printStackTrace();
            throw new InternalError("Error in CodeTemplateManager");
        }
    }
    
    public synchronized int getTemplateCount() {
        return this.templates.size();
    }
    
    public synchronized CodeTemplate[] getTemplates() {
        final CodeTemplate[] temp = new CodeTemplate[this.templates.size()];
        return this.templates.toArray(temp);
    }
    
    public static final boolean isValidChar(final char ch) {
        return RSyntaxUtilities.isLetterOrDigit(ch) || ch == '_';
    }
    
    public synchronized boolean removeTemplate(final CodeTemplate template) {
        if (template == null) {
            throw new IllegalArgumentException("template cannot be null");
        }
        return this.templates.remove(template);
    }
    
    public synchronized CodeTemplate removeTemplate(final String id) {
        if (id == null) {
            throw new IllegalArgumentException("id cannot be null");
        }
        final Iterator<CodeTemplate> i = this.templates.iterator();
        while (i.hasNext()) {
            final CodeTemplate template = i.next();
            if (id.equals(template.getID())) {
                i.remove();
                return template;
            }
        }
        return null;
    }
    
    public synchronized void replaceTemplates(final CodeTemplate[] newTemplates) {
        this.templates.clear();
        if (newTemplates != null) {
            for (int i = 0; i < newTemplates.length; ++i) {
                this.templates.add(newTemplates[i]);
            }
        }
        this.sortTemplates();
    }
    
    public synchronized boolean saveTemplates() {
        if (this.templates == null) {
            return true;
        }
        if (this.directory == null || !this.directory.isDirectory()) {
            return false;
        }
        final File[] oldXMLFiles = this.directory.listFiles(new XMLFileFilter());
        if (oldXMLFiles == null) {
            return false;
        }
        for (int count = oldXMLFiles.length, i = 0; i < count; ++i) {
            oldXMLFiles[i].delete();
        }
        boolean wasSuccessful = true;
        for (final CodeTemplate template : this.templates) {
            final File xmlFile = new File(this.directory, template.getID() + ".xml");
            try {
                final XMLEncoder e = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(xmlFile)));
                e.writeObject(template);
                e.close();
            }
            catch (IOException ioe) {
                ioe.printStackTrace();
                wasSuccessful = false;
            }
        }
        return wasSuccessful;
    }
    
    public synchronized int setTemplateDirectory(final File dir) {
        if (dir != null && dir.isDirectory()) {
            this.directory = dir;
            final File[] files = dir.listFiles(new XMLFileFilter());
            final int newCount = (files == null) ? 0 : files.length;
            final int oldCount = this.templates.size();
            final List<CodeTemplate> temp = new ArrayList<CodeTemplate>(oldCount + newCount);
            temp.addAll(this.templates);
            for (int i = 0; i < newCount; ++i) {
                try {
                    final XMLDecoder d = new XMLDecoder(new BufferedInputStream(new FileInputStream(files[i])));
                    final Object obj = d.readObject();
                    if (!(obj instanceof CodeTemplate)) {
                        throw new IOException("Not a CodeTemplate: " + files[i].getAbsolutePath());
                    }
                    temp.add((CodeTemplate)obj);
                    d.close();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            this.templates = temp;
            this.sortTemplates();
            return this.getTemplateCount();
        }
        return -1;
    }
    
    private synchronized void sortTemplates() {
        this.maxTemplateIDLength = 0;
        final Iterator<CodeTemplate> i = this.templates.iterator();
        while (i.hasNext()) {
            final CodeTemplate temp = i.next();
            if (temp == null || temp.getID() == null) {
                i.remove();
            }
            else {
                this.maxTemplateIDLength = Math.max(this.maxTemplateIDLength, temp.getID().length());
            }
        }
        Collections.sort(this.templates);
    }
    
    private static class TemplateComparator implements Comparator, Serializable
    {
        public int compare(final Object template, final Object segment) {
            final CodeTemplate t = (CodeTemplate)template;
            final char[] templateArray = t.getID().toCharArray();
            int i = 0;
            final int len1 = templateArray.length;
            final Segment s = (Segment)segment;
            final char[] segArray = s.array;
            int len2 = s.count;
            int j;
            for (j = s.offset + len2 - 1; j >= s.offset && CodeTemplateManager.isValidChar(segArray[j]); --j) {}
            final int segShift = ++j - s.offset;
            len2 -= segShift;
            int n = Math.min(len1, len2);
            while (n-- != 0) {
                final char c1 = templateArray[i++];
                final char c2 = segArray[j++];
                if (c1 != c2) {
                    return c1 - c2;
                }
            }
            return len1 - len2;
        }
    }
    
    private static class XMLFileFilter implements FileFilter
    {
        public boolean accept(final File f) {
            return f.getName().toLowerCase().endsWith(".xml");
        }
    }
}
