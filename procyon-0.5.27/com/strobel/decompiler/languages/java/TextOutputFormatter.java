package com.strobel.decompiler.languages.java;

import com.strobel.decompiler.*;
import com.strobel.decompiler.languages.*;
import com.strobel.core.*;
import java.util.*;
import com.strobel.decompiler.ast.*;
import com.strobel.decompiler.languages.java.ast.*;
import com.strobel.assembler.metadata.*;

public class TextOutputFormatter implements IOutputFormatter
{
    private final ITextOutput output;
    private final Stack<AstNode> nodeStack;
    private int braceLevelWithinType;
    private boolean inDocumentationComment;
    private boolean firstUsingDeclaration;
    private boolean lastUsingDeclaration;
    private LineNumberMode lineNumberMode;
    private int lastObservedLineNumber;
    private OffsetToLineNumberConverter offset2LineNumber;
    private final List<LineNumberPosition> lineNumberPositions;
    private final Stack<TextLocation> startLocations;
    private static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$decompiler$languages$java$BraceStyle;
    private static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$CommentType;
    
    public TextOutputFormatter(final ITextOutput output, final LineNumberMode lineNumberMode) {
        super();
        this.nodeStack = new Stack<AstNode>();
        this.braceLevelWithinType = -1;
        this.inDocumentationComment = false;
        this.lastObservedLineNumber = -100;
        this.offset2LineNumber = OffsetToLineNumberConverter.NOOP_CONVERTER;
        this.lineNumberPositions = new ArrayList<LineNumberPosition>();
        this.startLocations = new Stack<TextLocation>();
        this.output = VerifyArgument.notNull(output, "output");
        this.lineNumberMode = lineNumberMode;
    }
    
    @Override
    public void startNode(final AstNode node) {
        if (this.nodeStack.isEmpty()) {
            if (this.isImportDeclaration(node)) {
                this.firstUsingDeclaration = !this.isImportDeclaration(node.getPreviousSibling());
                this.lastUsingDeclaration = !this.isImportDeclaration(node.getNextSibling());
            }
            else {
                this.firstUsingDeclaration = false;
                this.lastUsingDeclaration = false;
            }
        }
        this.nodeStack.push(node);
        int offset = -34;
        String prefix = null;
        if (node instanceof Expression) {
            offset = ((Expression)node).getOffset();
            prefix = "/*EL:";
        }
        else if (node instanceof Statement) {
            offset = ((Statement)node).getOffset();
            prefix = "/*SL:";
        }
        if (offset != -34) {
            final int lineNumber = this.offset2LineNumber.getLineForOffset(offset);
            if (lineNumber > this.lastObservedLineNumber) {
                final int lineOfComment = this.output.getRow();
                final int columnOfComment = this.output.getColumn();
                final LineNumberPosition pos = new LineNumberPosition(lineNumber, lineOfComment, columnOfComment);
                this.lineNumberPositions.add(pos);
                this.lastObservedLineNumber = lineNumber;
                if (this.lineNumberMode == LineNumberMode.WITH_DEBUG_LINE_NUMBERS) {
                    final String commentStr = String.valueOf(prefix) + lineNumber + "*/";
                    this.output.writeComment(commentStr);
                }
            }
        }
        this.startLocations.push(new TextLocation(this.output.getRow(), this.output.getColumn()));
        if (node instanceof EntityDeclaration && node.getUserData(Keys.MEMBER_REFERENCE) != null && node.getChildByRole(Roles.IDENTIFIER).isNull()) {
            this.output.writeDefinition("", node.getUserData(Keys.MEMBER_REFERENCE), false);
        }
    }
    
    @Override
    public void endNode(final AstNode node) {
        if (this.nodeStack.pop() != node) {
            throw new IllegalStateException();
        }
        this.startLocations.pop();
    }
    
    @Override
    public void writeLabel(final String label) {
        this.output.writeLabel(label);
    }
    
    @Override
    public void writeIdentifier(final String identifier) {
        Object reference = this.getCurrentLocalReference();
        if (reference != null) {
            this.output.writeReference(identifier, reference, true);
            return;
        }
        reference = this.getCurrentMemberReference();
        if (reference != null) {
            this.output.writeReference(identifier, reference);
            return;
        }
        reference = this.getCurrentTypeReference();
        if (reference != null) {
            this.output.writeReference(identifier, reference);
            return;
        }
        reference = this.getCurrentPackageReference();
        if (reference != null) {
            this.output.writeReference(identifier, reference);
            return;
        }
        Object definition = this.getCurrentDefinition();
        if (definition != null) {
            this.output.writeDefinition(identifier, definition, false);
            return;
        }
        definition = this.getCurrentLocalDefinition();
        if (definition != null) {
            this.output.writeDefinition(identifier, definition);
            return;
        }
        if (this.firstUsingDeclaration) {
            this.output.markFoldStart("", true);
            this.firstUsingDeclaration = false;
        }
        this.output.write(identifier);
    }
    
    @Override
    public void writeKeyword(final String keyword) {
        this.output.writeKeyword(keyword);
    }
    
    @Override
    public void writeOperator(final String token) {
        this.output.writeOperator(token);
    }
    
    @Override
    public void writeDelimiter(final String token) {
        this.output.writeDelimiter(token);
    }
    
    @Override
    public void writeToken(final String token) {
        this.output.write(token);
    }
    
    @Override
    public void writeLiteral(final String value) {
        this.output.writeLiteral(value);
    }
    
    @Override
    public void writeTextLiteral(final String value) {
        this.output.writeTextLiteral(value);
    }
    
    @Override
    public void space() {
        this.output.write(' ');
    }
    
    @Override
    public void openBrace(final BraceStyle style) {
        if (this.braceLevelWithinType >= 0 || this.nodeStack.peek() instanceof TypeDeclaration) {
            ++this.braceLevelWithinType;
        }
        int blockDepth = 0;
        for (final AstNode node : this.nodeStack) {
            if (node instanceof BlockStatement) {
                ++blockDepth;
            }
        }
        if (blockDepth <= 1) {
            this.output.markFoldStart("", this.braceLevelWithinType == 1);
        }
        switch ($SWITCH_TABLE$com$strobel$decompiler$languages$java$BraceStyle()[style.ordinal()]) {
            case 4: {
                this.output.writeLine();
                break;
            }
            case 5: {
                this.output.writeLine();
                this.output.indent();
                break;
            }
            case 6: {
                this.output.writeLine();
                this.output.indent();
                this.output.indent();
                break;
            }
        }
        this.output.writeDelimiter("{");
        if (style != BraceStyle.BannerStyle) {
            this.output.writeLine();
        }
        this.output.indent();
    }
    
    @Override
    public void closeBrace(final BraceStyle style) {
        this.output.unindent();
        this.output.writeDelimiter("}");
        switch ($SWITCH_TABLE$com$strobel$decompiler$languages$java$BraceStyle()[style.ordinal()]) {
            case 5: {
                this.output.unindent();
                break;
            }
            case 6: {
                this.output.unindent();
                this.output.unindent();
                break;
            }
        }
        int blockDepth = 0;
        for (final AstNode node : this.nodeStack) {
            if (node instanceof BlockStatement) {
                ++blockDepth;
            }
        }
        if (blockDepth <= 1) {
            this.output.markFoldEnd();
        }
        if (this.braceLevelWithinType >= 0) {
            --this.braceLevelWithinType;
        }
    }
    
    @Override
    public void indent() {
        this.output.indent();
    }
    
    @Override
    public void unindent() {
        this.output.unindent();
    }
    
    @Override
    public void newLine() {
        if (this.lastUsingDeclaration) {
            this.output.markFoldEnd();
            this.lastUsingDeclaration = false;
        }
        this.output.writeLine();
    }
    
    @Override
    public void writeComment(final CommentType commentType, final String content) {
        switch ($SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$CommentType()[commentType.ordinal()]) {
            case 1: {
                this.output.writeComment("//");
                this.output.writeComment(content);
                this.output.writeLine();
                break;
            }
            case 2: {
                this.output.writeComment("/*");
                this.output.writeComment(content);
                this.output.writeComment("*/");
                break;
            }
            case 3: {
                final boolean isFirstLine = !(this.nodeStack.peek().getPreviousSibling() instanceof Comment);
                final boolean isLastLine = !(this.nodeStack.peek().getNextSibling() instanceof Comment);
                if (!this.inDocumentationComment && isFirstLine) {
                    this.inDocumentationComment = true;
                    String foldedContent = content.replace("\r|\n", " ").trim();
                    if (foldedContent.length() > 80) {
                        foldedContent = String.valueOf(foldedContent.substring(0, 80)) + " (...)";
                    }
                    else if (!isLastLine) {
                        foldedContent = String.valueOf(foldedContent) + " (...)";
                    }
                    this.output.markFoldStart("/** " + foldedContent + " */", true);
                    this.output.writeComment("/**");
                    this.output.writeLine();
                }
                this.output.writeComment(" * ");
                this.output.writeComment(content);
                this.output.writeLine();
                if (this.inDocumentationComment && isLastLine) {
                    this.inDocumentationComment = false;
                    this.output.writeComment(" */");
                    this.output.markFoldEnd();
                    this.output.writeLine();
                    break;
                }
                break;
            }
            default: {
                this.output.write(content);
                break;
            }
        }
    }
    
    private Object getCurrentDefinition() {
        if (this.nodeStack.isEmpty()) {
            return null;
        }
        final AstNode node = this.nodeStack.peek();
        if (isDefinition(node)) {
            Object definition = node.getUserData(Keys.TYPE_DEFINITION);
            if (definition != null) {
                return definition;
            }
            definition = node.getUserData(Keys.METHOD_DEFINITION);
            if (definition != null) {
                return definition;
            }
            definition = node.getUserData(Keys.FIELD_DEFINITION);
            if (definition != null) {
                return definition;
            }
        }
        if (node.getRole() == Roles.IDENTIFIER) {
            AstNode parent = node.getParent();
            if (parent == null) {
                return null;
            }
            if (parent instanceof VariableInitializer) {
                parent = parent.getParent();
            }
            Object definition = parent.getUserData(Keys.TYPE_DEFINITION);
            if (definition != null) {
                return definition;
            }
            definition = parent.getUserData(Keys.METHOD_DEFINITION);
            if (definition != null) {
                return definition;
            }
            definition = parent.getUserData(Keys.FIELD_DEFINITION);
            if (definition != null) {
                return definition;
            }
        }
        return null;
    }
    
    private MemberReference getCurrentTypeReference() {
        final AstNode node = this.nodeStack.peek();
        final TypeReference typeReference = node.getUserData(Keys.TYPE_REFERENCE);
        if (typeReference != null) {
            return typeReference;
        }
        if (node instanceof Identifier) {
            final AstNode parent = node.getParent();
            if (parent instanceof AstType || parent instanceof TypeParameterDeclaration || parent instanceof ImportDeclaration) {
                return parent.getUserData(Keys.TYPE_REFERENCE);
            }
        }
        return null;
    }
    
    private PackageReference getCurrentPackageReference() {
        final AstNode node = this.nodeStack.peek();
        PackageReference pkg = node.getUserData(Keys.PACKAGE_REFERENCE);
        if (pkg == null && node.getParent() instanceof ImportDeclaration) {
            pkg = node.getParent().getUserData(Keys.PACKAGE_REFERENCE);
        }
        return pkg;
    }
    
    private MemberReference getCurrentMemberReference() {
        final AstNode node = this.nodeStack.peek();
        MemberReference member = node.getUserData(Keys.MEMBER_REFERENCE);
        if (member == null && node.getRole() == Roles.TARGET_EXPRESSION && (node.getParent() instanceof InvocationExpression || node.getParent() instanceof ObjectCreationExpression)) {
            member = node.getParent().getUserData(Keys.MEMBER_REFERENCE);
        }
        return member;
    }
    
    private Object getCurrentLocalReference() {
        final AstNode node = this.nodeStack.peek();
        Variable variable = node.getUserData(Keys.VARIABLE);
        if (variable == null && node instanceof Identifier && node.getParent() != null) {
            variable = node.getParent().getUserData(Keys.VARIABLE);
        }
        if (variable == null) {
            return null;
        }
        if (variable.isParameter()) {
            return variable.getOriginalParameter();
        }
        return variable.getOriginalVariable();
    }
    
    private Object getCurrentLocalDefinition() {
        AstNode node = this.nodeStack.peek();
        if (node instanceof Identifier && node.getParent() != null) {
            node = node.getParent();
        }
        final ParameterDefinition parameter = node.getUserData(Keys.PARAMETER_DEFINITION);
        if (parameter != null) {
            return parameter;
        }
        if (node instanceof VariableInitializer || node instanceof CatchClause) {
            Variable variable = node.getUserData(Keys.VARIABLE);
            if (variable == null && node.getParent() instanceof VariableDeclarationStatement) {
                variable = node.getParent().getUserData(Keys.VARIABLE);
            }
            if (variable != null) {
                if (variable.getOriginalParameter() != null) {
                    return variable.getOriginalParameter();
                }
                return variable.getOriginalVariable();
            }
        }
        if (node instanceof LabelStatement) {
            final LabelStatement label = (LabelStatement)node;
            for (int i = this.nodeStack.size() - 1; i >= 0; --i) {
                final AstNode n = this.nodeStack.get(i);
                final MemberReference methodReference = n.getUserData(Keys.MEMBER_REFERENCE);
                if (methodReference instanceof MethodReference) {
                    return methodReference + label.getLabel();
                }
            }
        }
        return null;
    }
    
    private static boolean isDefinition(final AstNode node) {
        return node instanceof EntityDeclaration;
    }
    
    private boolean isImportDeclaration(final AstNode node) {
        return node instanceof ImportDeclaration;
    }
    
    @Override
    public void resetLineNumberOffsets(final OffsetToLineNumberConverter offset2LineNumber) {
        this.lastObservedLineNumber = -100;
        this.offset2LineNumber = offset2LineNumber;
    }
    
    public List<LineNumberPosition> getLineNumberPositions() {
        return this.lineNumberPositions;
    }
    
    static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$decompiler$languages$java$BraceStyle() {
        final int[] loc_0 = TextOutputFormatter.$SWITCH_TABLE$com$strobel$decompiler$languages$java$BraceStyle;
        if (loc_0 != null) {
            return loc_0;
        }
        final int[] loc_1 = new int[BraceStyle.values().length];
        try {
            loc_1[BraceStyle.BannerStyle.ordinal()] = 7;
        }
        catch (NoSuchFieldError loc_2) {}
        try {
            loc_1[BraceStyle.DoNotChange.ordinal()] = 1;
        }
        catch (NoSuchFieldError loc_3) {}
        try {
            loc_1[BraceStyle.EndOfLine.ordinal()] = 2;
        }
        catch (NoSuchFieldError loc_4) {}
        try {
            loc_1[BraceStyle.EndOfLineWithoutSpace.ordinal()] = 3;
        }
        catch (NoSuchFieldError loc_5) {}
        try {
            loc_1[BraceStyle.NextLine.ordinal()] = 4;
        }
        catch (NoSuchFieldError loc_6) {}
        try {
            loc_1[BraceStyle.NextLineShifted.ordinal()] = 5;
        }
        catch (NoSuchFieldError loc_7) {}
        try {
            loc_1[BraceStyle.NextLineShifted2.ordinal()] = 6;
        }
        catch (NoSuchFieldError loc_8) {}
        return TextOutputFormatter.$SWITCH_TABLE$com$strobel$decompiler$languages$java$BraceStyle = loc_1;
    }
    
    static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$CommentType() {
        final int[] loc_0 = TextOutputFormatter.$SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$CommentType;
        if (loc_0 != null) {
            return loc_0;
        }
        final int[] loc_1 = new int[CommentType.values().length];
        try {
            loc_1[CommentType.Documentation.ordinal()] = 3;
        }
        catch (NoSuchFieldError loc_2) {}
        try {
            loc_1[CommentType.MultiLine.ordinal()] = 2;
        }
        catch (NoSuchFieldError loc_3) {}
        try {
            loc_1[CommentType.SingleLine.ordinal()] = 1;
        }
        catch (NoSuchFieldError loc_4) {}
        return TextOutputFormatter.$SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$CommentType = loc_1;
    }
    
    public enum LineNumberMode
    {
        WITH_DEBUG_LINE_NUMBERS("WITH_DEBUG_LINE_NUMBERS", 0), 
        WITHOUT_DEBUG_LINE_NUMBERS("WITHOUT_DEBUG_LINE_NUMBERS", 1);
    }
}
