package us.deathmarine.luyten;

import org.fife.ui.rtextarea.*;
import com.strobel.assembler.metadata.*;
import java.util.*;
import org.fife.ui.rsyntaxtextarea.*;
import java.awt.*;

public class OpenFile implements SyntaxConstants
{
    public static final HashSet<String> WELL_KNOWN_TEXT_FILE_EXTENSIONS;
    RTextScrollPane scrollPane;
    Panel image_pane;
    RSyntaxTextArea textArea;
    String name;
    private String path;
    private TypeReference type;
    private boolean isContentValid;
    
    static {
        WELL_KNOWN_TEXT_FILE_EXTENSIONS = new HashSet<String>(Arrays.asList(".java", ".xml", ".rss", ".project", ".classpath", ".h", ".sql", ".js", ".php", ".php5", ".phtml", ".html", ".htm", ".xhtm", ".xhtml", ".lua", ".bat", ".pl", ".sh", ".css", ".json", ".txt", ".rb", ".make", ".mak", ".py", ".properties", ".prop"));
    }
    
    public OpenFile(final TypeReference type, final String name, final String path, final String content, final Theme theme) {
        this(name, path, content, theme);
        this.type = type;
    }
    
    public OpenFile(final String name, final String path, final String contents, final Theme theme) {
        super();
        this.type = null;
        this.isContentValid = false;
        this.name = name;
        this.path = path;
        (this.textArea = new RSyntaxTextArea(25, 70)).setCaretPosition(0);
        this.textArea.requestFocusInWindow();
        this.textArea.setMarkOccurrences(true);
        this.textArea.setClearWhitespaceLinesEnabled(false);
        this.textArea.setEditable(false);
        this.textArea.setAntiAliasingEnabled(true);
        this.textArea.setCodeFoldingEnabled(true);
        if (name.toLowerCase().endsWith(".class") || name.toLowerCase().endsWith(".java")) {
            this.textArea.setSyntaxEditingStyle("text/java");
        }
        else if (name.toLowerCase().endsWith(".xml") || name.toLowerCase().endsWith(".rss") || name.toLowerCase().endsWith(".project") || name.toLowerCase().endsWith(".classpath")) {
            this.textArea.setSyntaxEditingStyle("text/xml");
        }
        else if (name.toLowerCase().endsWith(".h")) {
            this.textArea.setSyntaxEditingStyle("text/c");
        }
        else if (name.toLowerCase().endsWith(".sql")) {
            this.textArea.setSyntaxEditingStyle("text/sql");
        }
        else if (name.toLowerCase().endsWith(".js")) {
            this.textArea.setSyntaxEditingStyle("text/javascript");
        }
        else if (name.toLowerCase().endsWith(".php") || name.toLowerCase().endsWith(".php5") || name.toLowerCase().endsWith(".phtml")) {
            this.textArea.setSyntaxEditingStyle("text/php");
        }
        else if (name.toLowerCase().endsWith(".html") || name.toLowerCase().endsWith(".htm") || name.toLowerCase().endsWith(".xhtm") || name.toLowerCase().endsWith(".xhtml")) {
            this.textArea.setSyntaxEditingStyle("text/html");
        }
        else if (name.toLowerCase().endsWith(".js")) {
            this.textArea.setSyntaxEditingStyle("text/javascript");
        }
        else if (name.toLowerCase().endsWith(".lua")) {
            this.textArea.setSyntaxEditingStyle("text/lua");
        }
        else if (name.toLowerCase().endsWith(".bat")) {
            this.textArea.setSyntaxEditingStyle("text/bat");
        }
        else if (name.toLowerCase().endsWith(".pl")) {
            this.textArea.setSyntaxEditingStyle("text/perl");
        }
        else if (name.toLowerCase().endsWith(".sh")) {
            this.textArea.setSyntaxEditingStyle("text/unix");
        }
        else if (name.toLowerCase().endsWith(".css")) {
            this.textArea.setSyntaxEditingStyle("text/css");
        }
        else if (name.toLowerCase().endsWith(".json")) {
            this.textArea.setSyntaxEditingStyle("text/json");
        }
        else if (name.toLowerCase().endsWith(".txt")) {
            this.textArea.setSyntaxEditingStyle("text/plain");
        }
        else if (name.toLowerCase().endsWith(".rb")) {
            this.textArea.setSyntaxEditingStyle("text/ruby");
        }
        else if (name.toLowerCase().endsWith(".make") || name.toLowerCase().endsWith(".mak")) {
            this.textArea.setSyntaxEditingStyle("text/makefile");
        }
        else if (name.toLowerCase().endsWith(".py")) {
            this.textArea.setSyntaxEditingStyle("text/python");
        }
        else {
            this.textArea.setSyntaxEditingStyle("text/properties");
        }
        (this.scrollPane = new RTextScrollPane(this.textArea, true)).setIconRowHeaderEnabled(true);
        this.textArea.setText(contents);
        theme.apply(this.textArea);
    }
    
    public void setContent(final String content) {
        this.textArea.setText(content);
    }
    
    public String getPath() {
        return this.path;
    }
    
    public void setPath(final String path) {
        this.path = path;
    }
    
    public TypeReference getType() {
        return this.type;
    }
    
    public void setType(final TypeReference type) {
        this.type = type;
    }
    
    public boolean isContentValid() {
        return this.isContentValid;
    }
    
    public void setContentValid(final boolean isContentValid) {
        this.isContentValid = isContentValid;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = 31 * result + ((this.name == null) ? 0 : this.name.hashCode());
        return result;
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        final OpenFile other = (OpenFile)obj;
        if (this.name == null) {
            if (other.name != null) {
                return false;
            }
        }
        else if (!this.name.equals(other.name)) {
            return false;
        }
        return true;
    }
}
