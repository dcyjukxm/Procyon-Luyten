package org.fife.ui.rsyntaxtextarea.folding;

import org.fife.ui.rsyntaxtextarea.*;
import java.util.*;

public class FoldParserManager implements SyntaxConstants
{
    private Map<String, FoldParser> foldParserMap;
    private static final FoldParserManager INSTANCE;
    
    private FoldParserManager() {
        super();
        this.foldParserMap = this.createFoldParserMap();
    }
    
    public void addFoldParserMapping(final String syntaxStyle, final FoldParser parser) {
        this.foldParserMap.put(syntaxStyle, parser);
    }
    
    private Map<String, FoldParser> createFoldParserMap() {
        final Map<String, FoldParser> map = new HashMap<String, FoldParser>();
        map.put("text/c", new CurlyFoldParser());
        map.put("text/cpp", new CurlyFoldParser());
        map.put("text/cs", new CurlyFoldParser());
        map.put("text/clojure", new LispFoldParser());
        map.put("text/css", new CurlyFoldParser());
        map.put("text/groovy", new CurlyFoldParser());
        map.put("text/htaccess", new XmlFoldParser());
        map.put("text/html", new HtmlFoldParser(-1));
        map.put("text/java", new CurlyFoldParser(true, true));
        map.put("text/javascript", new CurlyFoldParser());
        map.put("text/json", new JsonFoldParser());
        map.put("text/jsp", new HtmlFoldParser(1));
        map.put("text/latex", new LatexFoldParser());
        map.put("text/lisp", new LispFoldParser());
        map.put("text/mxml", new XmlFoldParser());
        map.put("text/nsis", new NsisFoldParser());
        map.put("text/perl", new CurlyFoldParser());
        map.put("text/php", new HtmlFoldParser(0));
        map.put("text/scala", new CurlyFoldParser());
        map.put("text/xml", new XmlFoldParser());
        return map;
    }
    
    public static FoldParserManager get() {
        return FoldParserManager.INSTANCE;
    }
    
    public FoldParser getFoldParser(final String syntaxStyle) {
        return this.foldParserMap.get(syntaxStyle);
    }
    
    static {
        INSTANCE = new FoldParserManager();
    }
}
