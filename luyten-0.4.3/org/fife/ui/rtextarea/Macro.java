package org.fife.ui.rtextarea;

import org.fife.io.*;
import org.xml.sax.*;
import javax.xml.parsers.*;
import java.io.*;
import java.util.*;
import javax.xml.transform.stream.*;
import javax.xml.transform.dom.*;
import org.w3c.dom.*;
import javax.xml.transform.*;

public class Macro
{
    private String name;
    private ArrayList<MacroRecord> macroRecords;
    private static final String ROOT_ELEMENT = "macro";
    private static final String MACRO_NAME = "macroName";
    private static final String ACTION = "action";
    private static final String ID = "id";
    private static final String UNTITLED_MACRO_NAME = "<Untitled>";
    private static final String FILE_ENCODING = "UTF-8";
    
    public Macro() {
        this("<Untitled>");
    }
    
    public Macro(final File file) throws FileNotFoundException, IOException {
        super();
        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = null;
        Document doc = null;
        try {
            db = dbf.newDocumentBuilder();
            final InputSource is = new InputSource(new UnicodeReader(new FileInputStream(file), "UTF-8"));
            is.setEncoding("UTF-8");
            doc = db.parse(is);
        }
        catch (Exception e) {
            e.printStackTrace();
            String desc = e.getMessage();
            if (desc == null) {
                desc = e.toString();
            }
            throw new IOException("Error parsing XML: " + desc);
        }
        this.macroRecords = new ArrayList<MacroRecord>();
        final boolean parsedOK = this.initializeFromXMLFile(doc.getDocumentElement());
        if (!parsedOK) {
            this.name = null;
            this.macroRecords.clear();
            this.macroRecords = null;
            throw new IOException("Error parsing XML!");
        }
    }
    
    public Macro(final String name) {
        this(name, null);
    }
    
    public Macro(final String name, final List<MacroRecord> records) {
        super();
        this.name = name;
        if (records != null) {
            this.macroRecords = new ArrayList<MacroRecord>(records.size());
            for (final MacroRecord record : records) {
                this.macroRecords.add(record);
            }
        }
        else {
            this.macroRecords = new ArrayList<MacroRecord>(10);
        }
    }
    
    public void addMacroRecord(final MacroRecord record) {
        if (record != null) {
            this.macroRecords.add(record);
        }
    }
    
    public List<MacroRecord> getMacroRecords() {
        return this.macroRecords;
    }
    
    public String getName() {
        return this.name;
    }
    
    private boolean initializeFromXMLFile(final Element root) {
        final NodeList childNodes = root.getChildNodes();
        for (int count = childNodes.getLength(), i = 0; i < count; ++i) {
            Node node = childNodes.item(i);
            final int type = node.getNodeType();
            switch (type) {
                case 1: {
                    final String nodeName = node.getNodeName();
                    if (nodeName.equals("macroName")) {
                        final NodeList childNodes2 = node.getChildNodes();
                        this.name = "<Untitled>";
                        if (childNodes2.getLength() > 0) {
                            node = childNodes2.item(0);
                            final int type2 = node.getNodeType();
                            if (type2 != 4 && type2 != 3) {
                                return false;
                            }
                            this.name = node.getNodeValue().trim();
                        }
                        break;
                    }
                    if (!nodeName.equals("action")) {
                        break;
                    }
                    final NamedNodeMap attributes = node.getAttributes();
                    if (attributes == null || attributes.getLength() != 1) {
                        return false;
                    }
                    final Node node2 = attributes.item(0);
                    final MacroRecord macroRecord = new MacroRecord();
                    if (!node2.getNodeName().equals("id")) {
                        return false;
                    }
                    macroRecord.id = node2.getNodeValue();
                    final NodeList childNodes3 = node.getChildNodes();
                    final int length = childNodes3.getLength();
                    if (length == 0) {
                        macroRecord.actionCommand = "";
                        this.macroRecords.add(macroRecord);
                        break;
                    }
                    node = childNodes3.item(0);
                    final int type3 = node.getNodeType();
                    if (type3 != 4 && type3 != 3) {
                        return false;
                    }
                    macroRecord.actionCommand = node.getNodeValue();
                    this.macroRecords.add(macroRecord);
                    break;
                }
            }
        }
        return true;
    }
    
    public void saveToFile(final File file) throws IOException {
        this.saveToFile(file.getAbsolutePath());
    }
    
    public void saveToFile(final String fileName) throws IOException {
        try {
            final DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            final DOMImplementation impl = db.getDOMImplementation();
            final Document doc = impl.createDocument(null, "macro", null);
            final Element rootElement = doc.getDocumentElement();
            final Element nameElement = doc.createElement("macroName");
            rootElement.appendChild(nameElement);
            for (final MacroRecord record : this.macroRecords) {
                final Element actionElement = doc.createElement("action");
                actionElement.setAttribute("id", record.id);
                if (record.actionCommand != null && record.actionCommand.length() > 0) {
                    String command = record.actionCommand;
                    for (int j = 0; j < command.length(); ++j) {
                        if (command.charAt(j) < ' ') {
                            command = command.substring(0, j);
                            if (j < command.length() - 1) {
                                command += command.substring(j + 1);
                            }
                        }
                    }
                    final Node n = doc.createCDATASection(command);
                    actionElement.appendChild(n);
                }
                rootElement.appendChild(actionElement);
            }
            final StreamResult result = new StreamResult(new File(fileName));
            final DOMSource source = new DOMSource(doc);
            final TransformerFactory transFac = TransformerFactory.newInstance();
            final Transformer transformer = transFac.newTransformer();
            transformer.setOutputProperty("indent", "yes");
            transformer.setOutputProperty("encoding", "UTF-8");
            transformer.transform(source, result);
        }
        catch (RuntimeException re) {
            throw re;
        }
        catch (Exception e) {
            throw new IOException("Error generating XML!");
        }
    }
    
    public void setName(final String name) {
        this.name = name;
    }
    
    static class MacroRecord
    {
        public String id;
        public String actionCommand;
        
        public MacroRecord() {
            this(null, null);
        }
        
        public MacroRecord(final String id, final String actionCommand) {
            super();
            this.id = id;
            this.actionCommand = actionCommand;
        }
    }
}
