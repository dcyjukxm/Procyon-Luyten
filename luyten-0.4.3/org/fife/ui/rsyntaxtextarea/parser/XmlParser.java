package org.fife.ui.rsyntaxtextarea.parser;

import org.fife.ui.rsyntaxtextarea.*;
import org.fife.io.*;
import org.xml.sax.helpers.*;
import javax.swing.text.*;
import javax.xml.parsers.*;
import java.io.*;
import org.xml.sax.*;

public class XmlParser extends AbstractParser
{
    private SAXParserFactory spf;
    private DefaultParseResult result;
    private EntityResolver entityResolver;
    
    public XmlParser() {
        this(null);
    }
    
    public XmlParser(final EntityResolver resolver) {
        super();
        this.entityResolver = resolver;
        this.result = new DefaultParseResult(this);
        try {
            this.spf = SAXParserFactory.newInstance();
        }
        catch (FactoryConfigurationError fce) {
            fce.printStackTrace();
        }
    }
    
    public boolean isValidating() {
        return this.spf.isValidating();
    }
    
    public ParseResult parse(final RSyntaxDocument doc, final String style) {
        this.result.clearNotices();
        final Element root = doc.getDefaultRootElement();
        this.result.setParsedLines(0, root.getElementCount() - 1);
        if (this.spf == null || doc.getLength() == 0) {
            return this.result;
        }
        try {
            final SAXParser sp = this.spf.newSAXParser();
            final Handler handler = new Handler((Document)doc);
            final DocumentReader r = new DocumentReader(doc);
            final InputSource input = new InputSource(r);
            sp.parse(input, handler);
            r.close();
        }
        catch (SAXParseException spe) {}
        catch (Exception e) {
            this.result.addNotice(new DefaultParserNotice(this, "Error parsing XML: " + e.getMessage(), 0, -1, -1));
        }
        return this.result;
    }
    
    public void setValidating(final boolean validating) {
        this.spf.setValidating(validating);
    }
    
    static /* synthetic */ DefaultParseResult access$100(final XmlParser x0) {
        return x0.result;
    }
    
    static /* synthetic */ EntityResolver access$200(final XmlParser x0) {
        return x0.entityResolver;
    }
    
    private class Handler extends DefaultHandler
    {
        private Document doc;
        
        private Handler(final Document doc) {
            super();
            this.doc = doc;
        }
        
        private void doError(final SAXParseException e, final ParserNotice.Level level) {
            final int line = e.getLineNumber() - 1;
            final Element root = this.doc.getDefaultRootElement();
            final Element elem = root.getElement(line);
            final int offs = elem.getStartOffset();
            int len = elem.getEndOffset() - offs;
            if (line == root.getElementCount() - 1) {
                ++len;
            }
            final DefaultParserNotice pn = new DefaultParserNotice(XmlParser.this, e.getMessage(), line, offs, len);
            pn.setLevel(level);
            XmlParser.access$100(XmlParser.this).addNotice(pn);
        }
        
        public void error(final SAXParseException e) {
            this.doError(e, ParserNotice.Level.ERROR);
        }
        
        public void fatalError(final SAXParseException e) {
            this.doError(e, ParserNotice.Level.ERROR);
        }
        
        public InputSource resolveEntity(final String publicId, final String systemId) throws IOException, SAXException {
            if (XmlParser.access$200(XmlParser.this) != null) {
                return XmlParser.access$200(XmlParser.this).resolveEntity(publicId, systemId);
            }
            return super.resolveEntity(publicId, systemId);
        }
        
        public void warning(final SAXParseException e) {
            this.doError(e, ParserNotice.Level.WARNING);
        }
    }
}
