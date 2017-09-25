package com.strobel.decompiler.languages.java;

import com.strobel.decompiler.*;
import com.strobel.decompiler.patterns.*;
import com.strobel.decompiler.languages.*;
import com.strobel.util.*;
import com.strobel.assembler.ir.attributes.*;
import java.util.*;
import com.strobel.assembler.metadata.*;
import com.strobel.decompiler.languages.java.utilities.*;
import com.strobel.decompiler.languages.java.ast.*;
import com.strobel.core.*;

public final class JavaOutputVisitor implements IAstVisitor<Void, Void>
{
    final TextOutputFormatter formatter;
    final DecompilerSettings settings;
    final JavaFormattingOptions policy;
    final Stack<AstNode> containerStack;
    final Stack<AstNode> positionStack;
    final ITextOutput output;
    private LastWritten lastWritten;
    private static final String[] KEYWORDS;
    static final /* synthetic */ boolean $assertionsDisabled;
    private static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$decompiler$languages$java$BraceEnforcement;
    private static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$ClassType;
    private static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$BinaryOperatorType;
    private static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$UnaryOperatorType;
    
    static {
        KEYWORDS = new String[] { "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const", "continue", "default", "do", "double", "else", "enum", "extends", "final", "finally", "float", "for", "goto", "if", "implements", "import", "instanceof", "int", "interface", "long", "native", "new", "package", "private", "protected", "public", "return", "short", "static", "strictfp", "super", "switch", "synchronized", "this", "throw", "throws", "transient", "try", "void", "volatile", "while" };
    }
    
    public JavaOutputVisitor(final ITextOutput output, final DecompilerSettings settings) {
        super();
        this.containerStack = new Stack<AstNode>();
        this.positionStack = new Stack<AstNode>();
        this.output = output;
        this.settings = VerifyArgument.notNull(settings, "settings");
        this.formatter = new TextOutputFormatter(output, settings.getShowDebugLineNumbers() ? TextOutputFormatter.LineNumberMode.WITH_DEBUG_LINE_NUMBERS : TextOutputFormatter.LineNumberMode.WITHOUT_DEBUG_LINE_NUMBERS);
        final JavaFormattingOptions formattingOptions = settings.getFormattingOptions();
        this.policy = ((formattingOptions != null) ? formattingOptions : JavaFormattingOptions.createDefault());
    }
    
    public List<LineNumberPosition> getLineNumberPositions() {
        return this.formatter.getLineNumberPositions();
    }
    
    void startNode(final AstNode node) {
        if (!JavaOutputVisitor.$assertionsDisabled && !this.containerStack.isEmpty() && node.getParent() != this.containerStack.peek() && this.containerStack.peek().getNodeType() != NodeType.PATTERN) {
            throw new AssertionError();
        }
        if (this.positionStack.size() > 0) {
            this.writeSpecialsUpToNode(node);
        }
        this.containerStack.push(node);
        this.positionStack.push(node.getFirstChild());
        this.formatter.startNode(node);
    }
    
    void endNode(final AstNode node) {
        assert node == this.containerStack.peek();
        final AstNode position = this.positionStack.pop();
        assert position.getParent() == node;
        this.writeSpecials(position, null);
        this.containerStack.pop();
        this.formatter.endNode(node);
    }
    
    private void writeSpecials(final AstNode start, final AstNode end) {
        for (AstNode current = start; current != end; current = current.getNextSibling()) {
            if (current.getRole() == Roles.COMMENT || current.getRole() == Roles.NEW_LINE) {
                current.acceptVisitor((IAstVisitor<? super Object, ?>)this, (Object)null);
            }
        }
    }
    
    private void writeSpecialsUpToRole(final Role<?> role) {
        this.writeSpecialsUpToRole(role, null);
    }
    
    private void writeSpecialsUpToRole(final Role<?> role, final AstNode nextNode) {
        if (this.positionStack.isEmpty()) {
            return;
        }
        for (AstNode current = this.positionStack.peek(); current != null && current != nextNode; current = current.getNextSibling()) {
            if (current.getRole() == role) {
                this.writeSpecials(this.positionStack.pop(), current);
                this.positionStack.push(current.getNextSibling());
                break;
            }
        }
    }
    
    private void writeSpecialsUpToNode(final AstNode node) {
        if (this.positionStack.isEmpty()) {
            return;
        }
        for (AstNode current = this.positionStack.peek(); current != null; current = current.getNextSibling()) {
            if (current == node) {
                this.writeSpecials(this.positionStack.pop(), current);
                this.positionStack.push(current.getNextSibling());
                break;
            }
        }
    }
    
    void leftParenthesis() {
        this.writeToken(Roles.LEFT_PARENTHESIS);
    }
    
    void rightParenthesis() {
        this.writeToken(Roles.RIGHT_PARENTHESIS);
    }
    
    void space() {
        this.formatter.space();
        this.lastWritten = LastWritten.Whitespace;
    }
    
    void space(final boolean addSpace) {
        if (addSpace) {
            this.space();
        }
    }
    
    void newLine() {
        this.formatter.newLine();
        this.lastWritten = LastWritten.Whitespace;
    }
    
    void openBrace(final BraceStyle style) {
        this.writeSpecialsUpToRole(Roles.LEFT_BRACE);
        this.space((style == BraceStyle.EndOfLine || style == BraceStyle.BannerStyle) && this.lastWritten != LastWritten.Whitespace && this.lastWritten != LastWritten.LeftParenthesis);
        this.formatter.openBrace(style);
        this.lastWritten = ((style == BraceStyle.BannerStyle) ? LastWritten.Other : LastWritten.Whitespace);
    }
    
    void closeBrace(final BraceStyle style) {
        this.writeSpecialsUpToRole(Roles.RIGHT_BRACE);
        this.formatter.closeBrace(style);
        this.lastWritten = LastWritten.Other;
    }
    
    void writeIdentifier(final String identifier) {
        this.writeIdentifier(identifier, null);
    }
    
    void writeIdentifier(final String identifier, final Role<Identifier> identifierRole) {
        this.writeSpecialsUpToRole((identifierRole != null) ? identifierRole : Roles.IDENTIFIER);
        if (isKeyword(identifier, this.containerStack.peek())) {
            if (this.lastWritten == LastWritten.KeywordOrIdentifier) {
                this.space();
            }
        }
        else if (this.lastWritten == LastWritten.KeywordOrIdentifier) {
            this.formatter.space();
        }
        if (identifierRole == Roles.LABEL) {
            this.formatter.writeLabel(identifier);
        }
        else {
            this.formatter.writeIdentifier(identifier);
        }
        this.lastWritten = LastWritten.KeywordOrIdentifier;
    }
    
    void writeToken(final TokenRole tokenRole) {
        this.writeToken(tokenRole.getToken(), tokenRole);
    }
    
    void writeToken(final String token, final Role role) {
        this.writeSpecialsUpToRole(role);
        if ((this.lastWritten == LastWritten.Plus && token.charAt(0) == '+') || (this.lastWritten == LastWritten.Minus && token.charAt(0) == '-') || (this.lastWritten == LastWritten.Ampersand && token.charAt(0) == '&') || (this.lastWritten == LastWritten.QuestionMark && token.charAt(0) == '?') || (this.lastWritten == LastWritten.Division && token.charAt(0) == '*')) {
            this.formatter.space();
        }
        if (role instanceof TokenRole) {
            final TokenRole tokenRole = (TokenRole)role;
            if (tokenRole.isKeyword()) {
                this.formatter.writeKeyword(token);
                this.lastWritten = LastWritten.KeywordOrIdentifier;
                return;
            }
            if (tokenRole.isOperator()) {
                this.formatter.writeOperator(token);
                this.lastWritten = LastWritten.Operator;
                return;
            }
            if (tokenRole.isDelimiter()) {
                this.formatter.writeDelimiter(token);
                this.lastWritten = ("(".equals(token) ? LastWritten.LeftParenthesis : LastWritten.Delimiter);
                return;
            }
        }
        this.formatter.writeToken(token);
        switch (token) {
            case "&": {
                this.lastWritten = LastWritten.Ampersand;
                return;
            }
            case "(": {
                this.lastWritten = LastWritten.LeftParenthesis;
                return;
            }
            case "+": {
                this.lastWritten = LastWritten.Plus;
                return;
            }
            case "-": {
                this.lastWritten = LastWritten.Minus;
                return;
            }
            case "/": {
                this.lastWritten = LastWritten.Division;
                return;
            }
            case "?": {
                this.lastWritten = LastWritten.QuestionMark;
                return;
            }
            default:
                break;
        }
        this.lastWritten = LastWritten.Other;
    }
    
    void comma(final AstNode nextNode) {
        this.comma(nextNode, false);
    }
    
    void comma(final AstNode nextNode, final boolean noSpaceAfterComma) {
        this.writeSpecialsUpToRole(Roles.COMMA, nextNode);
        this.space(this.policy.SpaceBeforeBracketComma);
        this.formatter.writeDelimiter(",");
        this.lastWritten = LastWritten.Other;
        this.space(!noSpaceAfterComma && this.policy.SpaceAfterBracketComma);
    }
    
    void optionalComma() {
        AstNode position;
        for (position = this.positionStack.peek(); position != null && position.getNodeType() == NodeType.WHITESPACE; position = position.getNextSibling()) {}
        if (position != null && position.getRole() == Roles.COMMA) {
            this.comma(null, true);
        }
    }
    
    void semicolon() {
        final Role role = this.containerStack.peek().getRole();
        if (role != ForStatement.INITIALIZER_ROLE && role != ForStatement.ITERATOR_ROLE) {
            this.writeToken(Roles.SEMICOLON);
            this.newLine();
        }
    }
    
    private void optionalSemicolon() {
        AstNode pos;
        for (pos = this.positionStack.peek(); pos != null && pos.getNodeType() == NodeType.WHITESPACE; pos = pos.getNextSibling()) {}
        if (pos != null && pos.getRole() == Roles.SEMICOLON) {
            this.semicolon();
        }
    }
    
    private void writeCommaSeparatedList(final Iterable<? extends AstNode> list) {
        boolean isFirst = true;
        for (final AstNode node : list) {
            if (isFirst) {
                isFirst = false;
            }
            else {
                this.comma(node);
            }
            node.acceptVisitor((IAstVisitor<? super Object, ?>)this, (Object)null);
        }
    }
    
    private void writePipeSeparatedList(final Iterable<? extends AstNode> list) {
        boolean isFirst = true;
        for (final AstNode node : list) {
            if (isFirst) {
                isFirst = false;
            }
            else {
                this.space();
                this.writeToken(Roles.PIPE);
                this.space();
            }
            node.acceptVisitor((IAstVisitor<? super Object, ?>)this, (Object)null);
        }
    }
    
    private void writeCommaSeparatedListInParenthesis(final Iterable<? extends AstNode> list, final boolean spaceWithin) {
        this.leftParenthesis();
        if (CollectionUtilities.any(list)) {
            this.space(spaceWithin);
            this.writeCommaSeparatedList(list);
            this.space(spaceWithin);
        }
        this.rightParenthesis();
    }
    
    private void writeTypeArguments(final Iterable<AstType> typeArguments) {
        if (CollectionUtilities.any(typeArguments)) {
            this.writeToken(Roles.LEFT_CHEVRON);
            this.writeCommaSeparatedList(typeArguments);
            this.writeToken(Roles.RIGHT_CHEVRON);
        }
    }
    
    public void writeTypeParameters(final Iterable<TypeParameterDeclaration> typeParameters) {
        if (CollectionUtilities.any(typeParameters)) {
            this.writeToken(Roles.LEFT_CHEVRON);
            this.writeCommaSeparatedList(typeParameters);
            this.writeToken(Roles.RIGHT_CHEVRON);
        }
    }
    
    private void writeModifiers(final Iterable<JavaModifierToken> modifierTokens) {
        for (final JavaModifierToken modifier : modifierTokens) {
            modifier.acceptVisitor((IAstVisitor<? super Object, ?>)this, (Object)null);
        }
    }
    
    private void writeQualifiedIdentifier(final Iterable<Identifier> identifiers) {
        boolean first = true;
        for (final Identifier identifier : identifiers) {
            if (first) {
                first = false;
                if (this.lastWritten == LastWritten.KeywordOrIdentifier) {
                    this.formatter.space();
                }
            }
            else {
                this.writeSpecialsUpToRole(Roles.DOT, identifier);
                this.formatter.writeToken(".");
                this.lastWritten = LastWritten.Other;
            }
            this.writeSpecialsUpToNode(identifier);
            this.formatter.writeIdentifier(identifier.getName());
            this.lastWritten = LastWritten.KeywordOrIdentifier;
        }
    }
    
    void writeEmbeddedStatement(final Statement embeddedStatement) {
        if (embeddedStatement.isNull()) {
            this.newLine();
            return;
        }
        if (embeddedStatement instanceof BlockStatement) {
            this.visitBlockStatement((BlockStatement)embeddedStatement, (Void)null);
        }
        else {
            this.newLine();
            this.formatter.indent();
            embeddedStatement.acceptVisitor((IAstVisitor<? super Object, ?>)this, (Object)null);
            this.formatter.unindent();
        }
    }
    
    void writeMethodBody(final AstNodeCollection<TypeDeclaration> declaredTypes, final BlockStatement body) {
        if (body.isNull()) {
            this.semicolon();
            return;
        }
        this.startNode(body);
        final AstNode parent = body.getParent();
        BraceStyle style;
        BraceEnforcement braceEnforcement;
        if (parent instanceof ConstructorDeclaration) {
            style = this.policy.ConstructorBraceStyle;
            braceEnforcement = BraceEnforcement.AddBraces;
        }
        else if (parent instanceof MethodDeclaration) {
            style = this.policy.MethodBraceStyle;
            braceEnforcement = BraceEnforcement.AddBraces;
        }
        else {
            style = this.policy.StatementBraceStyle;
            if (parent instanceof IfElseStatement) {
                braceEnforcement = this.policy.IfElseBraceEnforcement;
            }
            else if (parent instanceof WhileStatement) {
                braceEnforcement = this.policy.WhileBraceEnforcement;
            }
            else {
                braceEnforcement = BraceEnforcement.AddBraces;
            }
        }
        final AstNodeCollection<Statement> statements = body.getStatements();
        boolean addBraces = false;
        switch ($SWITCH_TABLE$com$strobel$decompiler$languages$java$BraceEnforcement()[braceEnforcement.ordinal()]) {
            case 2: {
                addBraces = false;
                break;
            }
            default: {
                addBraces = true;
                break;
            }
        }
        if (addBraces) {
            this.openBrace(style);
        }
        boolean needNewLine = false;
        if (declaredTypes != null && !declaredTypes.isEmpty()) {
            for (final TypeDeclaration declaredType : declaredTypes) {
                if (needNewLine) {
                    this.newLine();
                }
                declaredType.acceptVisitor((IAstVisitor<? super Object, ?>)new JavaOutputVisitor(this.output, this.settings), (Object)null);
                needNewLine = true;
            }
        }
        if (needNewLine) {
            this.newLine();
        }
        for (final AstNode statement : statements) {
            statement.acceptVisitor((IAstVisitor<? super Object, ?>)this, (Object)null);
        }
        if (addBraces) {
            this.closeBrace(style);
        }
        if (!(parent instanceof Expression)) {
            this.newLine();
        }
        this.endNode(body);
    }
    
    void writeAnnotations(final Iterable<Annotation> annotations, final boolean newLineAfter) {
        for (final Annotation annotation : annotations) {
            annotation.acceptVisitor((IAstVisitor<? super Object, ?>)this, (Object)null);
            if (newLineAfter) {
                this.newLine();
            }
            else {
                this.space();
            }
        }
    }
    
    void writePrivateImplementationType(final AstType privateImplementationType) {
        if (!privateImplementationType.isNull()) {
            privateImplementationType.acceptVisitor((IAstVisitor<? super Object, ?>)this, (Object)null);
            this.writeToken(Roles.DOT);
        }
    }
    
    void writeKeyword(final TokenRole tokenRole) {
        this.writeKeyword(tokenRole.getToken(), tokenRole);
    }
    
    void writeKeyword(final String token) {
        this.writeKeyword(token, null);
    }
    
    void writeKeyword(final String token, final Role tokenRole) {
        if (tokenRole != null) {
            this.writeSpecialsUpToRole(tokenRole);
        }
        if (this.lastWritten == LastWritten.KeywordOrIdentifier) {
            this.formatter.space();
        }
        this.formatter.writeKeyword(token);
        this.lastWritten = LastWritten.KeywordOrIdentifier;
    }
    
    void visitNodeInPattern(final INode childNode) {
        if (childNode instanceof AstNode) {
            ((AstNode)childNode).acceptVisitor((IAstVisitor<? super Object, ?>)this, (Object)null);
        }
        else if (childNode instanceof IdentifierExpressionBackReference) {
            this.visitIdentifierExpressionBackReference((IdentifierExpressionBackReference)childNode);
        }
        else if (childNode instanceof Choice) {
            this.visitChoice((Choice)childNode);
        }
        else if (childNode instanceof AnyNode) {
            this.visitAnyNode((AnyNode)childNode);
        }
        else if (childNode instanceof BackReference) {
            this.visitBackReference((BackReference)childNode);
        }
        else if (childNode instanceof NamedNode) {
            this.visitNamedNode((NamedNode)childNode);
        }
        else if (childNode instanceof OptionalNode) {
            this.visitOptionalNode((OptionalNode)childNode);
        }
        else if (childNode instanceof Repeat) {
            this.visitRepeat((Repeat)childNode);
        }
        else if (childNode instanceof MemberReferenceTypeNode) {
            this.visitMemberReferenceTypeNode((MemberReferenceTypeNode)childNode);
        }
        else if (childNode instanceof TypedNode) {
            this.visitTypedNode((TypedNode)childNode);
        }
        else if (childNode instanceof ParameterReferenceNode) {
            this.visitParameterReferenceNode((ParameterReferenceNode)childNode);
        }
        else {
            this.writePrimitiveValue(childNode);
        }
    }
    
    private void visitTypedNode(final TypedNode node) {
        this.writeKeyword("anyOf");
        this.leftParenthesis();
        this.writeIdentifier(node.getNodeType().getSimpleName());
        this.rightParenthesis();
    }
    
    private void visitParameterReferenceNode(final ParameterReferenceNode node) {
        this.writeKeyword("parameterAt");
        this.leftParenthesis();
        this.writePrimitiveValue(node.getParameterPosition());
        this.rightParenthesis();
    }
    
    private void visitIdentifierExpressionBackReference(final IdentifierExpressionBackReference node) {
        this.writeKeyword("identifierBackReference");
        this.leftParenthesis();
        this.writeIdentifier(node.getReferencedGroupName());
        this.rightParenthesis();
    }
    
    private void visitChoice(final Choice choice) {
        this.writeKeyword("choice");
        this.space();
        this.leftParenthesis();
        this.newLine();
        this.formatter.indent();
        final INode last = CollectionUtilities.lastOrDefault((Iterable<INode>)choice);
        for (final INode alternative : choice) {
            this.visitNodeInPattern(alternative);
            if (alternative != last) {
                this.writeToken(Roles.COMMA);
            }
            this.newLine();
        }
        this.formatter.unindent();
        this.rightParenthesis();
    }
    
    private void visitMemberReferenceTypeNode(final MemberReferenceTypeNode node) {
        this.writeKeyword("memberReference");
        this.writeToken(Roles.LEFT_BRACKET);
        this.writeIdentifier(node.getReferenceType().getSimpleName());
        this.writeToken(Roles.RIGHT_BRACKET);
        this.leftParenthesis();
        this.visitNodeInPattern(node.getTarget());
        this.rightParenthesis();
    }
    
    private void visitAnyNode(final AnyNode anyNode) {
        if (!StringUtilities.isNullOrEmpty(anyNode.getGroupName())) {
            this.writeIdentifier(anyNode.getGroupName());
            this.writeToken(Roles.COLON);
            this.writeIdentifier("*");
        }
    }
    
    private void visitBackReference(final BackReference backReference) {
        this.writeKeyword("backReference");
        this.leftParenthesis();
        this.writeIdentifier(backReference.getReferencedGroupName());
        this.rightParenthesis();
    }
    
    private void visitNamedNode(final NamedNode namedNode) {
        if (!StringUtilities.isNullOrEmpty(namedNode.getGroupName())) {
            this.writeIdentifier(namedNode.getGroupName());
            this.writeToken(Roles.COLON);
        }
        this.visitNodeInPattern(namedNode.getNode());
    }
    
    private void visitOptionalNode(final OptionalNode optionalNode) {
        this.writeKeyword("optional");
        this.leftParenthesis();
        this.visitNodeInPattern(optionalNode.getNode());
        this.rightParenthesis();
    }
    
    private void visitRepeat(final Repeat repeat) {
        this.writeKeyword("repeat");
        this.leftParenthesis();
        if (repeat.getMinCount() != 0 || repeat.getMaxCount() != Integer.MAX_VALUE) {
            this.writeIdentifier(String.valueOf(repeat.getMinCount()));
            this.writeToken(Roles.COMMA);
            this.writeIdentifier(String.valueOf(repeat.getMaxCount()));
            this.writeToken(Roles.COMMA);
        }
        this.visitNodeInPattern(repeat.getNode());
        this.rightParenthesis();
    }
    
    @Override
    public Void visitComment(final Comment comment, final Void ignored) {
        if (this.lastWritten == LastWritten.Division) {
            this.formatter.space();
        }
        this.formatter.startNode(comment);
        this.formatter.writeComment(comment.getCommentType(), comment.getContent());
        this.formatter.endNode(comment);
        this.lastWritten = LastWritten.Whitespace;
        return null;
    }
    
    @Override
    public Void visitPatternPlaceholder(final AstNode node, final Pattern pattern, final Void ignored) {
        this.startNode(node);
        this.visitNodeInPattern(pattern);
        this.endNode(node);
        return null;
    }
    
    @Override
    public Void visitInvocationExpression(final InvocationExpression node, final Void ignored) {
        this.startNode(node);
        node.getTarget().acceptVisitor((IAstVisitor<? super Object, ?>)this, (Object)null);
        this.space(this.policy.SpaceBeforeMethodCallParentheses);
        this.writeCommaSeparatedListInParenthesis(node.getArguments(), this.policy.SpaceWithinMethodCallParentheses);
        this.endNode(node);
        return null;
    }
    
    @Override
    public Void visitTypeReference(final TypeReferenceExpression node, final Void ignored) {
        this.startNode(node);
        node.getType().acceptVisitor((IAstVisitor<? super Object, ?>)this, (Object)null);
        this.endNode(node);
        return null;
    }
    
    @Override
    public Void visitJavaTokenNode(final JavaTokenNode node, final Void ignored) {
        node.setStartLocation(new TextLocation(this.output.getRow(), this.output.getColumn()));
        if (node instanceof JavaModifierToken) {
            final JavaModifierToken modifierToken = (JavaModifierToken)node;
            this.startNode(modifierToken);
            this.writeKeyword(JavaModifierToken.getModifierName(modifierToken.getModifier()));
            this.endNode(modifierToken);
            return null;
        }
        throw ContractUtils.unsupported();
    }
    
    @Override
    public Void visitMemberReferenceExpression(final MemberReferenceExpression node, final Void ignored) {
        this.startNode(node);
        final Expression target = node.getTarget();
        if (!target.isNull()) {
            target.acceptVisitor((IAstVisitor<? super Object, ?>)this, (Object)null);
            this.writeToken(Roles.DOT);
        }
        this.writeTypeArguments(node.getTypeArguments());
        this.writeIdentifier(node.getMemberName());
        this.endNode(node);
        return null;
    }
    
    @Override
    public Void visitIdentifier(final Identifier node, final Void ignored) {
        node.setStartLocation(new TextLocation(this.output.getRow(), this.output.getColumn()));
        this.startNode(node);
        this.writeIdentifier(node.getName());
        this.endNode(node);
        return null;
    }
    
    @Override
    public Void visitNullReferenceExpression(final NullReferenceExpression node, final Void ignored) {
        node.setStartLocation(new TextLocation(this.output.getRow(), this.output.getColumn()));
        this.startNode(node);
        this.writeKeyword("null", node.getRole());
        this.endNode(node);
        return null;
    }
    
    @Override
    public Void visitThisReferenceExpression(final ThisReferenceExpression node, final Void ignored) {
        node.setStartLocation(new TextLocation(this.output.getRow(), this.output.getColumn()));
        this.startNode(node);
        final Expression target = node.getTarget();
        if (target != null && !target.isNull()) {
            target.acceptVisitor((IAstVisitor<? super Void, ?>)this, ignored);
            this.writeToken(Roles.DOT);
        }
        this.writeKeyword("this", node.getRole());
        this.endNode(node);
        return null;
    }
    
    @Override
    public Void visitSuperReferenceExpression(final SuperReferenceExpression node, final Void ignored) {
        node.setStartLocation(new TextLocation(this.output.getRow(), this.output.getColumn()));
        this.startNode(node);
        final Expression target = node.getTarget();
        if (target != null && !target.isNull()) {
            target.acceptVisitor((IAstVisitor<? super Void, ?>)this, ignored);
            this.writeToken(Roles.DOT);
        }
        this.writeKeyword("super", node.getRole());
        this.endNode(node);
        return null;
    }
    
    @Override
    public Void visitClassOfExpression(final ClassOfExpression node, final Void ignored) {
        this.startNode(node);
        node.getType().acceptVisitor((IAstVisitor<? super Void, ?>)this, ignored);
        this.writeToken(Roles.DOT);
        this.writeKeyword("class", node.getRole());
        this.endNode(node);
        return null;
    }
    
    @Override
    public Void visitBlockStatement(final BlockStatement node, final Void ignored) {
        this.startNode(node);
        final AstNode parent = node.getParent();
        final Iterable<AstNode> children = node.getChildren();
        BraceStyle style;
        BraceEnforcement braceEnforcement;
        if (parent instanceof ConstructorDeclaration) {
            style = this.policy.ConstructorBraceStyle;
            braceEnforcement = BraceEnforcement.AddBraces;
        }
        else if (parent instanceof MethodDeclaration) {
            style = this.policy.MethodBraceStyle;
            braceEnforcement = BraceEnforcement.AddBraces;
        }
        else if (this.policy.StatementBraceStyle == BraceStyle.EndOfLine && !CollectionUtilities.any(children)) {
            style = BraceStyle.BannerStyle;
            braceEnforcement = BraceEnforcement.AddBraces;
        }
        else {
            style = this.policy.StatementBraceStyle;
            if (parent instanceof IfElseStatement) {
                braceEnforcement = this.policy.IfElseBraceEnforcement;
            }
            else if (parent instanceof WhileStatement) {
                braceEnforcement = this.policy.WhileBraceEnforcement;
            }
            else {
                braceEnforcement = BraceEnforcement.AddBraces;
            }
        }
        boolean addBraces = false;
        switch ($SWITCH_TABLE$com$strobel$decompiler$languages$java$BraceEnforcement()[braceEnforcement.ordinal()]) {
            case 2: {
                addBraces = false;
                break;
            }
            default: {
                addBraces = true;
                break;
            }
        }
        if (addBraces) {
            this.openBrace(style);
        }
        for (final AstNode child : children) {
            if (child instanceof Statement || child instanceof TypeDeclaration) {
                child.acceptVisitor((IAstVisitor<? super Object, ?>)this, (Object)null);
            }
        }
        if (addBraces) {
            this.closeBrace(style);
        }
        if (!(parent instanceof Expression) && !(parent instanceof DoWhileStatement)) {
            this.newLine();
        }
        this.endNode(node);
        return null;
    }
    
    @Override
    public Void visitExpressionStatement(final ExpressionStatement node, final Void ignored) {
        this.startNode(node);
        node.getExpression().acceptVisitor((IAstVisitor<? super Object, ?>)this, (Object)null);
        this.semicolon();
        this.endNode(node);
        return null;
    }
    
    @Override
    public Void visitBreakStatement(final BreakStatement node, final Void ignored) {
        this.startNode(node);
        this.writeKeyword("break");
        final String label = node.getLabel();
        if (!StringUtilities.isNullOrEmpty(label)) {
            this.writeIdentifier(label, Roles.LABEL);
        }
        this.semicolon();
        this.endNode(node);
        return null;
    }
    
    @Override
    public Void visitContinueStatement(final ContinueStatement node, final Void ignored) {
        this.startNode(node);
        this.writeKeyword("continue");
        final String label = node.getLabel();
        if (!StringUtilities.isNullOrEmpty(label)) {
            this.writeIdentifier(label, Roles.LABEL);
        }
        this.semicolon();
        this.endNode(node);
        return null;
    }
    
    @Override
    public Void visitDoWhileStatement(final DoWhileStatement node, final Void ignored) {
        this.startNode(node);
        this.writeKeyword(DoWhileStatement.DO_KEYWORD_ROLE);
        this.writeEmbeddedStatement(node.getEmbeddedStatement());
        this.space(this.lastWritten != LastWritten.Whitespace);
        this.writeKeyword(DoWhileStatement.WHILE_KEYWORD_ROLE);
        this.space(this.policy.SpaceBeforeWhileParentheses);
        this.leftParenthesis();
        this.space(this.policy.SpacesWithinWhileParentheses);
        node.getCondition().acceptVisitor((IAstVisitor<? super Object, ?>)this, (Object)null);
        this.space(this.policy.SpacesWithinWhileParentheses);
        this.rightParenthesis();
        this.semicolon();
        this.endNode(node);
        return null;
    }
    
    @Override
    public Void visitEmptyStatement(final EmptyStatement node, final Void ignored) {
        this.startNode(node);
        this.semicolon();
        this.endNode(node);
        return null;
    }
    
    @Override
    public Void visitIfElseStatement(final IfElseStatement node, final Void ignored) {
        this.startNode(node);
        this.writeKeyword(IfElseStatement.IF_KEYWORD_ROLE);
        this.space(this.policy.SpaceBeforeIfParentheses);
        this.leftParenthesis();
        this.space(this.policy.SpacesWithinIfParentheses);
        node.getCondition().acceptVisitor((IAstVisitor<? super Object, ?>)this, (Object)null);
        this.space(this.policy.SpacesWithinIfParentheses);
        this.rightParenthesis();
        this.writeEmbeddedStatement(node.getTrueStatement());
        final Statement falseStatement = node.getFalseStatement();
        if (!falseStatement.isNull()) {
            this.writeKeyword(IfElseStatement.ELSE_KEYWORD_ROLE);
            if (falseStatement instanceof IfElseStatement) {
                falseStatement.acceptVisitor((IAstVisitor<? super Void, ?>)this, ignored);
            }
            else {
                this.writeEmbeddedStatement(falseStatement);
            }
        }
        this.endNode(node);
        return null;
    }
    
    @Override
    public Void visitLabelStatement(final LabelStatement node, final Void ignored) {
        this.startNode(node);
        this.writeIdentifier(node.getLabel(), Roles.LABEL);
        this.writeToken(Roles.COLON);
        boolean foundLabelledStatement = false;
        for (AstNode sibling = node.getNextSibling(); sibling != null; sibling = sibling.getNextSibling()) {
            if (sibling.getRole() == node.getRole()) {
                foundLabelledStatement = true;
            }
        }
        if (!foundLabelledStatement) {
            this.writeToken(Roles.SEMICOLON);
        }
        this.newLine();
        this.endNode(node);
        return null;
    }
    
    @Override
    public Void visitLabeledStatement(final LabeledStatement node, final Void ignored) {
        final boolean isLoop = AstNode.isLoop(node.getStatement());
        this.startNode(node);
        if (isLoop) {
            this.formatter.unindent();
        }
        this.writeIdentifier(node.getLabel(), Roles.LABEL);
        this.writeToken(Roles.COLON);
        if (isLoop) {
            this.formatter.indent();
            this.newLine();
        }
        node.getStatement().acceptVisitor((IAstVisitor<? super Void, ?>)this, ignored);
        this.endNode(node);
        return null;
    }
    
    @Override
    public Void visitReturnStatement(final ReturnStatement node, final Void ignored) {
        this.startNode(node);
        this.writeKeyword(ReturnStatement.RETURN_KEYWORD_ROLE);
        if (!node.getExpression().isNull()) {
            this.space();
            node.getExpression().acceptVisitor((IAstVisitor<? super Object, ?>)this, (Object)null);
        }
        this.semicolon();
        this.endNode(node);
        return null;
    }
    
    @Override
    public Void visitSwitchStatement(final SwitchStatement node, final Void ignored) {
        this.startNode(node);
        this.writeKeyword(SwitchStatement.SWITCH_KEYWORD_ROLE);
        this.space(this.policy.SpaceBeforeSwitchParentheses);
        this.leftParenthesis();
        this.space(this.policy.SpacesWithinSwitchParentheses);
        node.getExpression().acceptVisitor((IAstVisitor<? super Void, ?>)this, ignored);
        this.space(this.policy.SpacesWithinSwitchParentheses);
        this.rightParenthesis();
        this.openBrace(this.policy.StatementBraceStyle);
        if (this.policy.IndentSwitchBody) {
            this.formatter.indent();
        }
        for (final SwitchSection section : node.getSwitchSections()) {
            section.acceptVisitor((IAstVisitor<? super Void, ?>)this, ignored);
        }
        if (this.policy.IndentSwitchBody) {
            this.formatter.unindent();
        }
        this.closeBrace(this.policy.StatementBraceStyle);
        this.newLine();
        this.endNode(node);
        return null;
    }
    
    @Override
    public Void visitSwitchSection(final SwitchSection node, final Void ignored) {
        this.startNode(node);
        boolean first = true;
        for (final CaseLabel label : node.getCaseLabels()) {
            if (!first) {
                this.newLine();
            }
            label.acceptVisitor((IAstVisitor<? super Void, ?>)this, ignored);
            first = false;
        }
        final boolean isBlock = node.getStatements().size() == 1 && CollectionUtilities.firstOrDefault(node.getStatements()) instanceof BlockStatement;
        if (this.policy.IndentCaseBody && !isBlock) {
            this.formatter.indent();
        }
        if (!isBlock) {
            this.newLine();
        }
        for (final Statement statement : node.getStatements()) {
            statement.acceptVisitor((IAstVisitor<? super Void, ?>)this, ignored);
        }
        if (this.policy.IndentCaseBody && !isBlock) {
            this.formatter.unindent();
        }
        this.endNode(node);
        return null;
    }
    
    @Override
    public Void visitCaseLabel(final CaseLabel node, final Void ignored) {
        this.startNode(node);
        if (node.getExpression().isNull()) {
            this.writeKeyword(CaseLabel.DEFAULT_KEYWORD_ROLE);
        }
        else {
            this.writeKeyword(CaseLabel.CASE_KEYWORD_ROLE);
            this.space();
            node.getExpression().acceptVisitor((IAstVisitor<? super Void, ?>)this, ignored);
        }
        this.writeToken(Roles.COLON);
        this.endNode(node);
        return null;
    }
    
    @Override
    public Void visitThrowStatement(final ThrowStatement node, final Void ignored) {
        this.startNode(node);
        this.writeKeyword(ThrowStatement.THROW_KEYWORD_ROLE);
        if (!node.getExpression().isNull()) {
            this.space();
            node.getExpression().acceptVisitor((IAstVisitor<? super Void, ?>)this, ignored);
        }
        this.semicolon();
        this.endNode(node);
        return null;
    }
    
    @Override
    public Void visitCatchClause(final CatchClause node, final Void ignored) {
        this.startNode(node);
        this.writeKeyword(CatchClause.CATCH_KEYWORD_ROLE);
        if (!node.getExceptionTypes().isEmpty()) {
            this.space(this.policy.SpaceBeforeCatchParentheses);
            this.leftParenthesis();
            this.space(this.policy.SpacesWithinCatchParentheses);
            this.writePipeSeparatedList(node.getExceptionTypes());
            if (!StringUtilities.isNullOrEmpty(node.getVariableName())) {
                this.space();
                node.getVariableNameToken().acceptVisitor((IAstVisitor<? super Void, ?>)this, ignored);
            }
            this.space(this.policy.SpacesWithinCatchParentheses);
            this.rightParenthesis();
        }
        node.getBody().acceptVisitor((IAstVisitor<? super Void, ?>)this, ignored);
        this.endNode(node);
        return null;
    }
    
    @Override
    public Void visitAnnotation(final Annotation node, final Void ignored) {
        this.startNode(node);
        this.startNode(node.getType());
        this.formatter.writeIdentifier("@");
        this.endNode(node.getType());
        node.getType().acceptVisitor((IAstVisitor<? super Void, ?>)this, ignored);
        final AstNodeCollection<Expression> arguments = node.getArguments();
        if (!arguments.isEmpty()) {
            this.writeCommaSeparatedListInParenthesis(arguments, false);
        }
        this.endNode(node);
        return null;
    }
    
    @Override
    public Void visitNewLine(final NewLineNode node, final Void ignored) {
        this.formatter.startNode(node);
        this.formatter.newLine();
        this.formatter.endNode(node);
        return null;
    }
    
    @Override
    public Void visitVariableDeclaration(final VariableDeclarationStatement node, final Void ignored) {
        return this.writeVariableDeclaration(node, true);
    }
    
    private Void writeVariableDeclaration(final VariableDeclarationStatement node, final boolean semicolon) {
        this.startNode(node);
        this.writeModifiers(node.getChildrenByRole(VariableDeclarationStatement.MODIFIER_ROLE));
        node.getType().acceptVisitor((IAstVisitor<? super Object, ?>)this, (Object)null);
        this.space();
        this.writeCommaSeparatedList(node.getVariables());
        if (semicolon) {
            this.semicolon();
        }
        this.endNode(node);
        return null;
    }
    
    @Override
    public Void visitVariableInitializer(final VariableInitializer node, final Void ignored) {
        this.startNode(node);
        node.getNameToken().acceptVisitor((IAstVisitor<? super Void, ?>)this, ignored);
        if (!node.getInitializer().isNull()) {
            this.space(this.policy.SpaceAroundAssignment);
            this.writeToken(Roles.ASSIGN);
            this.space(this.policy.SpaceAroundAssignment);
            node.getInitializer().acceptVisitor((IAstVisitor<? super Void, ?>)this, ignored);
        }
        this.endNode(node);
        return null;
    }
    
    @Override
    public Void visitText(final TextNode node, final Void ignored) {
        return null;
    }
    
    @Override
    public Void visitImportDeclaration(final ImportDeclaration node, final Void ignored) {
        this.startNode(node);
        this.writeKeyword(ImportDeclaration.IMPORT_KEYWORD_RULE);
        node.getImportIdentifier().acceptVisitor((IAstVisitor<? super Void, ?>)this, ignored);
        this.semicolon();
        this.endNode(node);
        if (!(node.getNextSibling() instanceof ImportDeclaration)) {
            this.newLine();
        }
        return null;
    }
    
    @Override
    public Void visitSimpleType(final SimpleType node, final Void ignored) {
        this.startNode(node);
        final TypeReference typeReference = node.getUserData(Keys.TYPE_REFERENCE);
        if (typeReference != null && typeReference.isPrimitive()) {
            this.writeKeyword(typeReference.getSimpleName());
        }
        else {
            this.writeIdentifier(node.getIdentifier());
        }
        this.writeTypeArguments(node.getTypeArguments());
        this.endNode(node);
        return null;
    }
    
    @Override
    public Void visitMethodDeclaration(final MethodDeclaration node, final Void ignored) {
        this.startNode(node);
        this.formatter.resetLineNumberOffsets(OffsetToLineNumberConverter.NOOP_CONVERTER);
        this.writeAnnotations(node.getAnnotations(), true);
        final MethodDefinition definition = node.getUserData(Keys.METHOD_DEFINITION);
        if (definition != null && definition.isDefault()) {
            this.writeKeyword(Roles.DEFAULT_KEYWORD);
        }
        this.writeModifiers(node.getModifiers());
        if (definition != null && (definition.isSynthetic() || definition.isBridgeMethod())) {
            this.space(this.lastWritten != LastWritten.Whitespace);
            this.formatter.writeComment(CommentType.MultiLine, definition.isBridgeMethod() ? " bridge " : " synthetic ");
            this.space();
        }
        if (definition == null || !definition.isTypeInitializer()) {
            final LineNumberTableAttribute lineNumberTable = SourceAttribute.find("LineNumberTable", (definition != null) ? definition.getSourceAttributes() : Collections.emptyList());
            if (lineNumberTable != null) {
                this.formatter.resetLineNumberOffsets(new LineNumberTableConverter(lineNumberTable));
            }
            final AstNodeCollection<TypeParameterDeclaration> typeParameters = node.getTypeParameters();
            if (CollectionUtilities.any(typeParameters)) {
                this.space();
                this.writeTypeParameters(typeParameters);
                this.space();
            }
            node.getReturnType().acceptVisitor((IAstVisitor<? super Void, ?>)this, ignored);
            this.space();
            this.writePrivateImplementationType(node.getPrivateImplementationType());
            node.getNameToken().acceptVisitor((IAstVisitor<? super Void, ?>)this, ignored);
            this.space(this.policy.SpaceBeforeMethodDeclarationParentheses);
            this.writeCommaSeparatedListInParenthesis(node.getParameters(), this.policy.SpaceWithinMethodDeclarationParentheses);
        }
        final AstNodeCollection<AstType> thrownTypes = node.getThrownTypes();
        if (!thrownTypes.isEmpty()) {
            this.space();
            this.writeKeyword(MethodDeclaration.THROWS_KEYWORD);
            this.writeCommaSeparatedList(thrownTypes);
        }
        final Expression defaultValue = node.getDefaultValue();
        if (defaultValue != null && !defaultValue.isNull()) {
            this.space();
            this.writeKeyword(MethodDeclaration.DEFAULT_KEYWORD);
            this.space();
            defaultValue.acceptVisitor((IAstVisitor<? super Void, ?>)this, ignored);
        }
        final AstNodeCollection<TypeDeclaration> declaredTypes = node.getDeclaredTypes();
        this.writeMethodBody(declaredTypes, node.getBody());
        this.endNode(node);
        return null;
    }
    
    @Override
    public Void visitInitializerBlock(final InstanceInitializer node, final Void ignored) {
        this.startNode(node);
        this.writeMethodBody(node.getDeclaredTypes(), node.getBody());
        this.endNode(node);
        return null;
    }
    
    @Override
    public Void visitConstructorDeclaration(final ConstructorDeclaration node, final Void ignored) {
        this.startNode(node);
        this.writeAnnotations(node.getAnnotations(), true);
        this.writeModifiers(node.getModifiers());
        final AstNode parent = node.getParent();
        final TypeDeclaration type = (parent instanceof TypeDeclaration) ? ((TypeDeclaration)parent) : null;
        this.startNode(node.getNameToken());
        this.writeIdentifier((type != null) ? type.getName() : node.getName());
        this.endNode(node.getNameToken());
        this.space(this.policy.SpaceBeforeConstructorDeclarationParentheses);
        this.writeCommaSeparatedListInParenthesis(node.getParameters(), this.policy.SpaceWithinMethodDeclarationParentheses);
        final AstNodeCollection<AstType> thrownTypes = node.getThrownTypes();
        if (!thrownTypes.isEmpty()) {
            this.space();
            this.writeKeyword(MethodDeclaration.THROWS_KEYWORD);
            this.writeCommaSeparatedList(thrownTypes);
        }
        this.writeMethodBody(null, node.getBody());
        this.endNode(node);
        return null;
    }
    
    @Override
    public Void visitTypeParameterDeclaration(final TypeParameterDeclaration node, final Void ignored) {
        this.startNode(node);
        this.writeAnnotations(node.getAnnotations(), false);
        node.getNameToken().acceptVisitor((IAstVisitor<? super Void, ?>)this, ignored);
        final AstType extendsBound = node.getExtendsBound();
        if (extendsBound != null && !extendsBound.isNull()) {
            this.writeKeyword(Roles.EXTENDS_KEYWORD);
            extendsBound.acceptVisitor((IAstVisitor<? super Void, ?>)this, ignored);
        }
        this.endNode(node);
        return null;
    }
    
    @Override
    public Void visitParameterDeclaration(final ParameterDeclaration node, final Void ignored) {
        final boolean hasType = !node.getType().isNull();
        this.startNode(node);
        this.writeAnnotations(node.getAnnotations(), false);
        if (hasType) {
            this.writeModifiers(node.getModifiers());
            node.getType().acceptVisitor((IAstVisitor<? super Void, ?>)this, ignored);
            if (!StringUtilities.isNullOrEmpty(node.getName())) {
                this.space();
            }
        }
        if (!StringUtilities.isNullOrEmpty(node.getName())) {
            node.getNameToken().acceptVisitor((IAstVisitor<? super Void, ?>)this, ignored);
        }
        this.endNode(node);
        return null;
    }
    
    @Override
    public Void visitFieldDeclaration(final FieldDeclaration node, final Void ignored) {
        this.startNode(node);
        this.writeAnnotations(node.getAnnotations(), true);
        this.writeModifiers(node.getModifiers());
        final FieldDefinition field = node.getUserData(Keys.FIELD_DEFINITION);
        if (field != null && field.isSynthetic()) {
            this.space(this.lastWritten != LastWritten.Whitespace);
            this.formatter.writeComment(CommentType.MultiLine, " synthetic ");
            this.space();
        }
        node.getReturnType().acceptVisitor((IAstVisitor<? super Void, ?>)this, ignored);
        this.space();
        this.writeCommaSeparatedList(node.getVariables());
        this.semicolon();
        this.endNode(node);
        return null;
    }
    
    @Override
    public Void visitLocalTypeDeclarationStatement(final LocalTypeDeclarationStatement node, final Void data) {
        this.startNode(node);
        node.getTypeDeclaration().acceptVisitor((IAstVisitor<? super Void, ?>)this, data);
        this.endNode(node);
        return data;
    }
    
    @Override
    public Void visitTypeDeclaration(final TypeDeclaration node, final Void ignored) {
        this.startNode(node);
        final TypeDefinition type = node.getUserData(Keys.TYPE_DEFINITION);
        final boolean isTrulyAnonymous = type != null && type.isAnonymous() && node.getParent() instanceof AnonymousObjectCreationExpression;
        if (!isTrulyAnonymous) {
            this.writeAnnotations(node.getAnnotations(), true);
            this.writeModifiers(node.getModifiers());
            switch ($SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$ClassType()[node.getClassType().ordinal()]) {
                case 4: {
                    this.writeKeyword(Roles.ENUM_KEYWORD);
                    break;
                }
                case 2: {
                    this.writeKeyword(Roles.INTERFACE_KEYWORD);
                    break;
                }
                case 3: {
                    this.writeKeyword(Roles.ANNOTATION_KEYWORD);
                    break;
                }
                default: {
                    this.writeKeyword(Roles.CLASS_KEYWORD);
                    break;
                }
            }
            node.getNameToken().acceptVisitor((IAstVisitor<? super Void, ?>)this, ignored);
            this.writeTypeParameters(node.getTypeParameters());
            if (!node.getBaseType().isNull()) {
                this.space();
                this.writeKeyword(Roles.EXTENDS_KEYWORD);
                this.space();
                node.getBaseType().acceptVisitor((IAstVisitor<? super Void, ?>)this, ignored);
            }
            if (CollectionUtilities.any(node.getInterfaces())) {
                Collection<AstType> interfaceTypes;
                if (node.getClassType() == ClassType.ANNOTATION) {
                    interfaceTypes = new ArrayList<AstType>();
                    for (final AstType t : node.getInterfaces()) {
                        final TypeReference r = t.getUserData(Keys.TYPE_REFERENCE);
                        if (r != null && "java/lang/annotation/Annotation".equals(r.getInternalName())) {
                            continue;
                        }
                        interfaceTypes.add(t);
                    }
                }
                else {
                    interfaceTypes = node.getInterfaces();
                }
                if (CollectionUtilities.any(interfaceTypes)) {
                    this.space();
                    if (node.getClassType() == ClassType.INTERFACE || node.getClassType() == ClassType.ANNOTATION) {
                        this.writeKeyword(Roles.EXTENDS_KEYWORD);
                    }
                    else {
                        this.writeKeyword(Roles.IMPLEMENTS_KEYWORD);
                    }
                    this.space();
                    this.writeCommaSeparatedList(node.getInterfaces());
                }
            }
        }
        final AstNodeCollection<EntityDeclaration> members = node.getMembers();
        BraceStyle braceStyle = null;
        switch ($SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$ClassType()[node.getClassType().ordinal()]) {
            case 4: {
                braceStyle = this.policy.EnumBraceStyle;
                break;
            }
            case 2: {
                braceStyle = this.policy.InterfaceBraceStyle;
                break;
            }
            case 3: {
                braceStyle = this.policy.AnnotationBraceStyle;
                break;
            }
            default: {
                if (type != null && type.isAnonymous()) {
                    braceStyle = (members.isEmpty() ? BraceStyle.BannerStyle : this.policy.AnonymousClassBraceStyle);
                    break;
                }
                braceStyle = this.policy.ClassBraceStyle;
                break;
            }
        }
        this.openBrace(braceStyle);
        boolean first = true;
        EntityDeclaration lastMember = null;
        for (final EntityDeclaration member : members) {
            if (first) {
                first = false;
            }
            else {
                int blankLines;
                if (member instanceof FieldDeclaration && lastMember instanceof FieldDeclaration) {
                    blankLines = this.policy.BlankLinesBetweenFields;
                }
                else {
                    blankLines = this.policy.BlankLinesBetweenMembers;
                }
                for (int i = 0; i < blankLines; ++i) {
                    this.formatter.newLine();
                }
            }
            member.acceptVisitor((IAstVisitor<? super Void, ?>)this, ignored);
            lastMember = member;
        }
        this.closeBrace(braceStyle);
        if (type == null || !type.isAnonymous()) {
            this.optionalSemicolon();
            this.newLine();
        }
        this.endNode(node);
        return null;
    }
    
    @Override
    public Void visitCompilationUnit(final CompilationUnit node, final Void ignored) {
        for (final AstNode child : node.getChildren()) {
            child.acceptVisitor((IAstVisitor<? super Void, ?>)this, ignored);
        }
        return null;
    }
    
    @Override
    public Void visitPackageDeclaration(final PackageDeclaration node, final Void ignored) {
        this.startNode(node);
        this.writeKeyword(Roles.PACKAGE_KEYWORD);
        this.writeQualifiedIdentifier(node.getIdentifiers());
        this.semicolon();
        this.newLine();
        for (int i = 0; i < this.policy.BlankLinesAfterPackageDeclaration; ++i) {
            this.newLine();
        }
        this.endNode(node);
        return null;
    }
    
    @Override
    public Void visitArraySpecifier(final ArraySpecifier node, final Void ignored) {
        this.startNode(node);
        this.writeToken(Roles.LEFT_BRACKET);
        for (final JavaTokenNode comma : node.getChildrenByRole((Role<AstNode>)Roles.COMMA)) {
            this.writeSpecialsUpToNode(comma);
            this.formatter.writeToken(",");
            this.lastWritten = LastWritten.Other;
        }
        this.writeToken(Roles.RIGHT_BRACKET);
        this.endNode(node);
        return null;
    }
    
    @Override
    public Void visitComposedType(final ComposedType node, final Void ignored) {
        this.startNode(node);
        node.getBaseType().acceptVisitor((IAstVisitor<? super Void, ?>)this, ignored);
        boolean isVarArgs = false;
        if (node.getParent() instanceof ParameterDeclaration) {
            final ParameterDefinition parameter = node.getParent().getUserData(Keys.PARAMETER_DEFINITION);
            if (parameter.getPosition() == parameter.getMethod().getParameters().size() - 1 && parameter.getParameterType().isArray() && parameter.getMethod() instanceof MethodReference) {
                final MethodReference method = (MethodReference)parameter.getMethod();
                final MethodDefinition resolvedMethod = method.resolve();
                if (resolvedMethod != null && Flags.testAny(resolvedMethod.getFlags(), 17179869312L)) {
                    isVarArgs = true;
                }
            }
        }
        final AstNodeCollection<ArraySpecifier> arraySpecifiers = node.getArraySpecifiers();
        final int arraySpecifierCount = arraySpecifiers.size();
        int i = 0;
        for (final ArraySpecifier specifier : arraySpecifiers) {
            if (isVarArgs && ++i == arraySpecifierCount) {
                this.writeToken(Roles.VARARGS);
            }
            else {
                specifier.acceptVisitor((IAstVisitor<? super Void, ?>)this, ignored);
            }
        }
        this.endNode(node);
        return null;
    }
    
    @Override
    public Void visitWhileStatement(final WhileStatement node, final Void ignored) {
        this.startNode(node);
        this.writeKeyword(WhileStatement.WHILE_KEYWORD_ROLE);
        this.space(this.policy.SpaceBeforeWhileParentheses);
        this.leftParenthesis();
        this.space(this.policy.SpacesWithinWhileParentheses);
        node.getCondition().acceptVisitor((IAstVisitor<? super Void, ?>)this, ignored);
        this.space(this.policy.SpacesWithinWhileParentheses);
        this.rightParenthesis();
        this.writeEmbeddedStatement(node.getEmbeddedStatement());
        this.endNode(node);
        return null;
    }
    
    @Override
    public Void visitPrimitiveExpression(final PrimitiveExpression node, final Void ignored) {
        node.setStartLocation(new TextLocation(this.output.getRow(), this.output.getColumn()));
        this.startNode(node);
        if (!StringUtilities.isNullOrEmpty(node.getLiteralValue())) {
            this.formatter.writeLiteral(node.getLiteralValue());
        }
        else if (node.getValue() instanceof Number) {
            final long longValue = ((Number)node.getValue()).longValue();
            if (longValue != -1L && this.isBitwiseContext(node.getParent(), node)) {
                this.formatter.writeLiteral(String.format((node.getValue() instanceof Long) ? "0x%1$XL" : "0x%1$X", node.getValue()));
            }
            else {
                this.writePrimitiveValue(node.getValue());
            }
        }
        else {
            this.writePrimitiveValue(node.getValue());
        }
        this.endNode(node);
        return null;
    }
    
    private boolean isBitwiseContext(AstNode parent, AstNode node) {
        parent = ((parent != null) ? TypeUtilities.skipParenthesesUp(parent) : null);
        node = ((node != null) ? TypeUtilities.skipParenthesesUp(node) : null);
        if (parent instanceof BinaryOperatorExpression || parent instanceof AssignmentExpression) {
            BinaryOperatorType operator;
            if (parent instanceof BinaryOperatorExpression) {
                operator = ((BinaryOperatorExpression)parent).getOperator();
            }
            else {
                operator = AssignmentExpression.getCorrespondingBinaryOperator(((AssignmentExpression)parent).getOperator());
            }
            if (operator == null) {
                return false;
            }
            switch ($SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$BinaryOperatorType()[operator.ordinal()]) {
                case 2:
                case 3:
                case 4: {
                    return true;
                }
                case 11:
                case 12: {
                    if (node != null) {
                        final BinaryOperatorExpression binary = (BinaryOperatorExpression)parent;
                        final AstNode comparand = (node == binary.getLeft()) ? binary.getRight() : binary.getLeft();
                        return this.isBitwiseContext(TypeUtilities.skipParenthesesDown(comparand), null);
                    }
                    break;
                }
            }
            return false;
        }
        else {
            if (!(parent instanceof UnaryOperatorExpression)) {
                return false;
            }
            switch ($SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$UnaryOperatorType()[((UnaryOperatorExpression)parent).getOperator().ordinal()]) {
                case 3: {
                    return true;
                }
                default: {
                    return false;
                }
            }
        }
    }
    
    void writePrimitiveValue(final Object val) {
        if (val == null) {
            this.writeKeyword("null");
            return;
        }
        if (val instanceof Boolean) {
            if (val) {
                this.writeKeyword("true");
            }
            else {
                this.writeKeyword("false");
            }
            return;
        }
        if (val instanceof String) {
            this.formatter.writeTextLiteral(StringUtilities.escape(val.toString(), true, this.settings.isUnicodeOutputEnabled()));
            this.lastWritten = LastWritten.Other;
        }
        else if (val instanceof Character) {
            this.formatter.writeTextLiteral(StringUtilities.escape((char)val, true, this.settings.isUnicodeOutputEnabled()));
            this.lastWritten = LastWritten.Other;
        }
        else if (val instanceof Float) {
            final float f = (float)val;
            if (Float.isInfinite(f) || Float.isNaN(f)) {
                this.writeKeyword("Float");
                this.writeToken(Roles.DOT);
                if (f == Float.POSITIVE_INFINITY) {
                    this.writeIdentifier("POSITIVE_INFINITY");
                }
                else if (f == Float.NEGATIVE_INFINITY) {
                    this.writeIdentifier("NEGATIVE_INFINITY");
                }
                else {
                    this.writeIdentifier("NaN");
                }
                return;
            }
            this.formatter.writeLiteral(String.valueOf(Float.toString(f)) + "f");
            this.lastWritten = LastWritten.Other;
        }
        else if (val instanceof Double) {
            final double d = (double)val;
            if (Double.isInfinite(d) || Double.isNaN(d)) {
                this.writeKeyword("Double");
                this.writeToken(Roles.DOT);
                if (d == Double.POSITIVE_INFINITY) {
                    this.writeIdentifier("POSITIVE_INFINITY");
                }
                else if (d == Double.NEGATIVE_INFINITY) {
                    this.writeIdentifier("NEGATIVE_INFINITY");
                }
                else {
                    this.writeIdentifier("NaN");
                }
                return;
            }
            String number = Double.toString(d);
            if (number.indexOf(46) < 0 && number.indexOf(69) < 0) {
                number = String.valueOf(number) + "d";
            }
            this.formatter.writeLiteral(number);
            this.lastWritten = LastWritten.KeywordOrIdentifier;
        }
        else if (val instanceof Number) {
            final long longValue = ((Number)val).longValue();
            boolean writeHex = longValue == 0xBADBADBADBADL || longValue == 0xBADC0FFEE0DDF00DL;
            if (!writeHex) {
                final long msb = longValue & 0xFFFFFFFF00000000L;
                final long lsb = longValue & 0xFFFFFFFFL;
                if (msb == 0L) {
                    switch ((int)lsb) {
                        case -1414812757:
                        case -1414677826:
                        case -1414673666:
                        case -1159869698:
                        case -1146241297:
                        case -1091581234:
                        case -889275714:
                        case -889270259:
                        case -889262164:
                        case -559039810:
                        case -559038737:
                        case -559038242:
                        case -559026163:
                        case -553727763:
                        case -86057299:
                        case 0x1BADB002: {
                            writeHex = true;
                            break;
                        }
                    }
                }
            }
            final String stringValue = writeHex ? String.format("0x%1$X", longValue) : String.valueOf(val);
            this.formatter.writeLiteral((val instanceof Long) ? (String.valueOf(stringValue) + "L") : stringValue);
            this.lastWritten = LastWritten.Other;
        }
        else {
            this.formatter.writeLiteral(String.valueOf(val));
            this.lastWritten = LastWritten.Other;
        }
    }
    
    @Override
    public Void visitCastExpression(final CastExpression node, final Void ignored) {
        this.startNode(node);
        this.leftParenthesis();
        this.space(this.policy.SpacesWithinCastParentheses);
        node.getType().acceptVisitor((IAstVisitor<? super Void, ?>)this, ignored);
        this.space(this.policy.SpacesWithinCastParentheses);
        this.rightParenthesis();
        this.space(this.policy.SpaceAfterTypecast);
        node.getExpression().acceptVisitor((IAstVisitor<? super Void, ?>)this, ignored);
        this.endNode(node);
        return null;
    }
    
    @Override
    public Void visitBinaryOperatorExpression(final BinaryOperatorExpression node, final Void ignored) {
        this.startNode(node);
        node.getLeft().acceptVisitor((IAstVisitor<? super Void, ?>)this, ignored);
        boolean spacePolicy = false;
        switch ($SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$BinaryOperatorType()[node.getOperator().ordinal()]) {
            case 2:
            case 3:
            case 4: {
                spacePolicy = this.policy.SpaceAroundBitwiseOperator;
                break;
            }
            case 5:
            case 6: {
                spacePolicy = this.policy.SpaceAroundLogicalOperator;
                break;
            }
            case 7:
            case 8:
            case 9:
            case 10: {
                spacePolicy = this.policy.SpaceAroundRelationalOperator;
                break;
            }
            case 11:
            case 12: {
                spacePolicy = this.policy.SpaceAroundEqualityOperator;
                break;
            }
            case 13:
            case 14: {
                spacePolicy = this.policy.SpaceAroundAdditiveOperator;
                break;
            }
            case 15:
            case 16:
            case 17: {
                spacePolicy = this.policy.SpaceAroundMultiplicativeOperator;
                break;
            }
            case 18:
            case 19:
            case 20: {
                spacePolicy = this.policy.SpaceAroundShiftOperator;
                break;
            }
            default: {
                spacePolicy = true;
                break;
            }
        }
        this.space(spacePolicy);
        this.writeToken(BinaryOperatorExpression.getOperatorRole(node.getOperator()));
        this.space(spacePolicy);
        node.getRight().acceptVisitor((IAstVisitor<? super Void, ?>)this, ignored);
        this.endNode(node);
        return null;
    }
    
    @Override
    public Void visitInstanceOfExpression(final InstanceOfExpression node, final Void ignored) {
        this.startNode(node);
        node.getExpression().acceptVisitor((IAstVisitor<? super Void, ?>)this, ignored);
        this.space();
        this.writeKeyword(InstanceOfExpression.INSTANCE_OF_KEYWORD_ROLE);
        node.getType().acceptVisitor((IAstVisitor<? super Void, ?>)this, ignored);
        this.endNode(node);
        return null;
    }
    
    @Override
    public Void visitIndexerExpression(final IndexerExpression node, final Void ignored) {
        this.startNode(node);
        node.getTarget().acceptVisitor((IAstVisitor<? super Void, ?>)this, ignored);
        this.space(this.policy.SpaceBeforeMethodCallParentheses);
        this.writeToken(Roles.LEFT_BRACKET);
        node.getArgument().acceptVisitor((IAstVisitor<? super Void, ?>)this, ignored);
        this.writeToken(Roles.RIGHT_BRACKET);
        this.endNode(node);
        return null;
    }
    
    @Override
    public Void visitIdentifierExpression(final IdentifierExpression node, final Void ignored) {
        this.startNode(node);
        this.writeIdentifier(node.getIdentifier());
        this.writeTypeArguments(node.getTypeArguments());
        this.endNode(node);
        return null;
    }
    
    @Override
    public Void visitUnaryOperatorExpression(final UnaryOperatorExpression node, final Void ignored) {
        this.startNode(node);
        final UnaryOperatorType operator = node.getOperator();
        final TokenRole symbol = UnaryOperatorExpression.getOperatorRole(operator);
        if (operator != UnaryOperatorType.POST_INCREMENT && operator != UnaryOperatorType.POST_DECREMENT) {
            this.writeToken(symbol);
        }
        node.getExpression().acceptVisitor((IAstVisitor<? super Void, ?>)this, ignored);
        if (operator == UnaryOperatorType.POST_INCREMENT || operator == UnaryOperatorType.POST_DECREMENT) {
            this.writeToken(symbol);
        }
        this.endNode(node);
        return null;
    }
    
    @Override
    public Void visitConditionalExpression(final ConditionalExpression node, final Void ignored) {
        this.startNode(node);
        node.getCondition().acceptVisitor((IAstVisitor<? super Void, ?>)this, ignored);
        this.space(this.policy.SpaceBeforeConditionalOperatorCondition);
        this.writeToken(ConditionalExpression.QUESTION_MARK_ROLE);
        this.space(this.policy.SpaceAfterConditionalOperatorCondition);
        node.getTrueExpression().acceptVisitor((IAstVisitor<? super Void, ?>)this, ignored);
        this.space(this.policy.SpaceBeforeConditionalOperatorSeparator);
        this.writeToken(ConditionalExpression.COLON_ROLE);
        this.space(this.policy.SpaceAfterConditionalOperatorSeparator);
        node.getFalseExpression().acceptVisitor((IAstVisitor<? super Void, ?>)this, ignored);
        this.endNode(node);
        return null;
    }
    
    @Override
    public Void visitArrayInitializerExpression(final ArrayInitializerExpression node, final Void ignored) {
        this.startNode(node);
        this.writeInitializerElements(node.getElements());
        this.endNode(node);
        return null;
    }
    
    private void writeInitializerElements(final AstNodeCollection<Expression> elements) {
        if (elements.isEmpty()) {
            this.writeToken(Roles.LEFT_BRACE);
            this.writeToken(Roles.RIGHT_BRACE);
            return;
        }
        final boolean wrapElements = this.policy.ArrayInitializerWrapping == Wrapping.WrapAlways;
        final BraceStyle style = wrapElements ? BraceStyle.NextLine : BraceStyle.BannerStyle;
        this.openBrace(style);
        boolean isFirst = true;
        for (final AstNode node : elements) {
            if (isFirst) {
                if (style == BraceStyle.BannerStyle) {
                    this.space();
                }
                isFirst = false;
            }
            else {
                this.comma(node, wrapElements);
                if (wrapElements) {
                    this.newLine();
                }
            }
            node.acceptVisitor((IAstVisitor<? super Object, ?>)this, (Object)null);
        }
        this.optionalComma();
        if (wrapElements) {
            this.newLine();
        }
        else if (!isFirst && style == BraceStyle.BannerStyle) {
            this.space();
        }
        this.closeBrace(style);
    }
    
    @Override
    public Void visitObjectCreationExpression(final ObjectCreationExpression node, final Void ignored) {
        this.startNode(node);
        final Expression target = node.getTarget();
        if (target != null && !target.isNull()) {
            target.acceptVisitor((IAstVisitor<? super Void, ?>)this, ignored);
            this.writeToken(Roles.DOT);
        }
        this.writeKeyword(ObjectCreationExpression.NEW_KEYWORD_ROLE);
        node.getType().acceptVisitor((IAstVisitor<? super Void, ?>)this, ignored);
        this.space(this.policy.SpaceBeforeMethodCallParentheses);
        this.writeCommaSeparatedListInParenthesis(node.getArguments(), this.policy.SpaceWithinMethodCallParentheses);
        this.endNode(node);
        return null;
    }
    
    @Override
    public Void visitAnonymousObjectCreationExpression(final AnonymousObjectCreationExpression node, final Void ignored) {
        this.startNode(node);
        final Expression target = node.getTarget();
        if (target != null && !target.isNull()) {
            target.acceptVisitor((IAstVisitor<? super Void, ?>)this, ignored);
            this.writeToken(Roles.DOT);
        }
        this.writeKeyword(ObjectCreationExpression.NEW_KEYWORD_ROLE);
        node.getType().acceptVisitor((IAstVisitor<? super Void, ?>)this, ignored);
        this.space(this.policy.SpaceBeforeMethodCallParentheses);
        this.writeCommaSeparatedListInParenthesis(node.getArguments(), this.policy.SpaceWithinMethodCallParentheses);
        node.getTypeDeclaration().acceptVisitor((IAstVisitor<? super Void, ?>)new JavaOutputVisitor(this.output, this.settings), ignored);
        this.endNode(node);
        return null;
    }
    
    @Override
    public Void visitWildcardType(final WildcardType node, final Void ignored) {
        this.startNode(node);
        this.writeToken(WildcardType.WILDCARD_TOKEN_ROLE);
        final AstNodeCollection<AstType> extendsBounds = node.getExtendsBounds();
        if (!extendsBounds.isEmpty()) {
            this.space();
            this.writeKeyword(WildcardType.EXTENDS_KEYWORD_ROLE);
            this.writePipeSeparatedList(extendsBounds);
        }
        else {
            final AstNodeCollection<AstType> superBounds = node.getSuperBounds();
            if (!superBounds.isEmpty()) {
                this.space();
                this.writeKeyword(WildcardType.SUPER_KEYWORD_ROLE);
                this.writePipeSeparatedList(superBounds);
            }
        }
        this.endNode(node);
        return null;
    }
    
    @Override
    public Void visitMethodGroupExpression(final MethodGroupExpression node, final Void ignored) {
        this.startNode(node);
        node.getTarget().acceptVisitor((IAstVisitor<? super Void, ?>)this, ignored);
        this.writeToken(MethodGroupExpression.DOUBLE_COLON_ROLE);
        if (isKeyword(node.getMethodName())) {
            this.writeKeyword(node.getMethodName());
        }
        else {
            this.writeIdentifier(node.getMethodName());
        }
        this.endNode(node);
        return null;
    }
    
    @Override
    public Void visitEnumValueDeclaration(final EnumValueDeclaration node, final Void ignored) {
        this.startNode(node);
        this.writeAnnotations(node.getAnnotations(), true);
        this.writeIdentifier(node.getName());
        final AstNodeCollection<Expression> arguments = node.getArguments();
        if (!arguments.isEmpty()) {
            this.writeCommaSeparatedListInParenthesis(arguments, this.policy.SpaceWithinEnumDeclarationParentheses);
        }
        final AstNodeCollection<EntityDeclaration> members = node.getMembers();
        final TypeDefinition enumType = node.getUserData(Keys.TYPE_DEFINITION);
        if ((enumType != null && enumType.isAnonymous()) || !members.isEmpty()) {
            final BraceStyle braceStyle = this.policy.AnonymousClassBraceStyle;
            this.openBrace(braceStyle);
            boolean first = true;
            EntityDeclaration lastMember = null;
            for (final EntityDeclaration member : node.getMembers()) {
                if (first) {
                    first = false;
                }
                else {
                    int blankLines;
                    if (member instanceof FieldDeclaration && lastMember instanceof FieldDeclaration) {
                        blankLines = this.policy.BlankLinesBetweenFields;
                    }
                    else {
                        blankLines = this.policy.BlankLinesBetweenMembers;
                    }
                    for (int i = 0; i < blankLines; ++i) {
                        this.formatter.newLine();
                    }
                }
                member.acceptVisitor((IAstVisitor<? super Void, ?>)this, ignored);
                lastMember = member;
            }
            this.closeBrace(braceStyle);
        }
        boolean isLast = true;
        AstNode next = node.getNextSibling();
        while (next != null) {
            if (next.getRole() == Roles.TYPE_MEMBER) {
                if (next instanceof EnumValueDeclaration) {
                    isLast = false;
                    break;
                }
                break;
            }
            else {
                next = next.getNextSibling();
            }
        }
        if (isLast) {
            this.semicolon();
        }
        else {
            this.comma(node.getNextSibling());
        }
        this.endNode(node);
        return null;
    }
    
    @Override
    public Void visitAssertStatement(final AssertStatement node, final Void ignored) {
        this.startNode(node);
        this.writeKeyword(AssertStatement.ASSERT_KEYWORD_ROLE);
        this.space();
        node.getCondition().acceptVisitor((IAstVisitor<? super Void, ?>)this, ignored);
        final Expression message = node.getMessage();
        if (message != null && !message.isNull()) {
            this.space();
            this.writeToken(Roles.COLON);
            this.space();
            message.acceptVisitor((IAstVisitor<? super Void, ?>)this, ignored);
        }
        this.semicolon();
        this.endNode(node);
        return null;
    }
    
    @Override
    public Void visitLambdaExpression(final LambdaExpression node, final Void ignored) {
        this.startNode(node);
        if (lambdaNeedsParenthesis(node)) {
            this.writeCommaSeparatedListInParenthesis(node.getParameters(), this.policy.SpaceWithinMethodDeclarationParentheses);
        }
        else {
            node.getParameters().firstOrNullObject().acceptVisitor((IAstVisitor<? super Void, ?>)this, ignored);
        }
        this.space();
        this.writeToken(LambdaExpression.ARROW_ROLE);
        if (!(node.getBody() instanceof BlockStatement)) {
            this.space();
        }
        node.getBody().acceptVisitor((IAstVisitor<? super Void, ?>)this, ignored);
        this.endNode(node);
        return null;
    }
    
    private static boolean lambdaNeedsParenthesis(final LambdaExpression lambda) {
        return lambda.getParameters().size() != 1 || !lambda.getParameters().firstOrNullObject().getType().isNull();
    }
    
    @Override
    public Void visitArrayCreationExpression(final ArrayCreationExpression node, final Void ignored) {
        this.startNode(node);
        boolean needType = true;
        if (node.getDimensions().isEmpty() && node.getType() != null && (node.getParent() instanceof ArrayInitializerExpression || node.getParent() instanceof VariableInitializer)) {
            needType = false;
        }
        if (needType) {
            this.writeKeyword(ArrayCreationExpression.NEW_KEYWORD_ROLE);
            node.getType().acceptVisitor((IAstVisitor<? super Void, ?>)this, ignored);
            for (final Expression dimension : node.getDimensions()) {
                this.writeToken(Roles.LEFT_BRACKET);
                dimension.acceptVisitor((IAstVisitor<? super Void, ?>)this, ignored);
                this.writeToken(Roles.RIGHT_BRACKET);
            }
            for (final ArraySpecifier specifier : node.getAdditionalArraySpecifiers()) {
                specifier.acceptVisitor((IAstVisitor<? super Void, ?>)this, ignored);
            }
            if (node.getInitializer() != null && !node.getInitializer().isNull()) {
                this.space();
            }
        }
        node.getInitializer().acceptVisitor((IAstVisitor<? super Void, ?>)this, ignored);
        this.endNode(node);
        return null;
    }
    
    @Override
    public Void visitAssignmentExpression(final AssignmentExpression node, final Void ignored) {
        this.startNode(node);
        node.getLeft().acceptVisitor((IAstVisitor<? super Void, ?>)this, ignored);
        this.space(this.policy.SpaceAroundAssignment);
        this.writeToken(AssignmentExpression.getOperatorRole(node.getOperator()));
        this.space(this.policy.SpaceAroundAssignment);
        node.getRight().acceptVisitor((IAstVisitor<? super Void, ?>)this, ignored);
        this.endNode(node);
        return null;
    }
    
    @Override
    public Void visitForStatement(final ForStatement node, final Void ignored) {
        this.startNode(node);
        this.writeKeyword(ForStatement.FOR_KEYWORD_ROLE);
        this.space(this.policy.SpaceBeforeForParentheses);
        this.leftParenthesis();
        this.space(this.policy.SpacesWithinForParentheses);
        this.writeCommaSeparatedList(node.getInitializers());
        this.space(this.policy.SpaceBeforeForSemicolon);
        this.writeToken(Roles.SEMICOLON);
        this.space(this.policy.SpaceAfterForSemicolon);
        node.getCondition().acceptVisitor((IAstVisitor<? super Void, ?>)this, ignored);
        this.space(this.policy.SpaceBeforeForSemicolon);
        this.writeToken(Roles.SEMICOLON);
        if (CollectionUtilities.any(node.getIterators())) {
            this.space(this.policy.SpaceAfterForSemicolon);
            this.writeCommaSeparatedList(node.getIterators());
        }
        this.space(this.policy.SpacesWithinForParentheses);
        this.rightParenthesis();
        this.writeEmbeddedStatement(node.getEmbeddedStatement());
        this.endNode(node);
        return null;
    }
    
    @Override
    public Void visitForEachStatement(final ForEachStatement node, final Void ignored) {
        this.startNode(node);
        this.writeKeyword(ForEachStatement.FOR_KEYWORD_ROLE);
        this.space(this.policy.SpaceBeforeForeachParentheses);
        this.leftParenthesis();
        this.space(this.policy.SpacesWithinForeachParentheses);
        this.writeModifiers(node.getChildrenByRole(EntityDeclaration.MODIFIER_ROLE));
        node.getVariableType().acceptVisitor((IAstVisitor<? super Void, ?>)this, ignored);
        this.space();
        node.getVariableNameToken().acceptVisitor((IAstVisitor<? super Void, ?>)this, ignored);
        this.space();
        this.writeToken(ForEachStatement.COLON_ROLE);
        this.space();
        node.getInExpression().acceptVisitor((IAstVisitor<? super Void, ?>)this, ignored);
        this.space(this.policy.SpacesWithinForeachParentheses);
        this.rightParenthesis();
        this.writeEmbeddedStatement(node.getEmbeddedStatement());
        this.endNode(node);
        return null;
    }
    
    @Override
    public Void visitTryCatchStatement(final TryCatchStatement node, final Void ignored) {
        this.startNode(node);
        this.writeKeyword(TryCatchStatement.TRY_KEYWORD_ROLE);
        final AstNodeCollection<VariableDeclarationStatement> resources = node.getResources();
        if (!resources.isEmpty()) {
            this.space();
            this.leftParenthesis();
            VariableDeclarationStatement resource;
            for (VariableDeclarationStatement firstResource = resource = resources.firstOrNullObject(); resource != null; resource = resource.getNextSibling(TryCatchStatement.TRY_RESOURCE_ROLE)) {
                if (resource != firstResource) {
                    this.semicolon();
                    this.space();
                    this.space();
                    this.space();
                    this.space();
                    this.space();
                }
                this.writeVariableDeclaration(resource, false);
            }
            this.rightParenthesis();
        }
        node.getTryBlock().acceptVisitor((IAstVisitor<? super Void, ?>)this, ignored);
        for (final CatchClause catchClause : node.getCatchClauses()) {
            catchClause.acceptVisitor((IAstVisitor<? super Void, ?>)this, ignored);
        }
        if (!node.getFinallyBlock().isNull()) {
            this.writeKeyword(TryCatchStatement.FINALLY_KEYWORD_ROLE);
            node.getFinallyBlock().acceptVisitor((IAstVisitor<? super Void, ?>)this, ignored);
        }
        this.endNode(node);
        return null;
    }
    
    @Override
    public Void visitGotoStatement(final GotoStatement node, final Void ignored) {
        this.startNode(node);
        this.writeKeyword(GotoStatement.GOTO_KEYWORD_ROLE);
        this.writeIdentifier(node.getLabel(), Roles.LABEL);
        this.semicolon();
        this.endNode(node);
        return null;
    }
    
    @Override
    public Void visitParenthesizedExpression(final ParenthesizedExpression node, final Void ignored) {
        this.startNode(node);
        this.leftParenthesis();
        this.space(this.policy.SpacesWithinParentheses);
        node.getExpression().acceptVisitor((IAstVisitor<? super Void, ?>)this, ignored);
        this.space(this.policy.SpacesWithinParentheses);
        this.rightParenthesis();
        this.endNode(node);
        return null;
    }
    
    @Override
    public Void visitSynchronizedStatement(final SynchronizedStatement node, final Void ignored) {
        this.startNode(node);
        this.writeKeyword(SynchronizedStatement.SYNCHRONIZED_KEYWORD_ROLE);
        this.space(this.policy.SpaceBeforeSynchronizedParentheses);
        this.leftParenthesis();
        this.space(this.policy.SpacesWithinSynchronizedParentheses);
        node.getExpression().acceptVisitor((IAstVisitor<? super Void, ?>)this, ignored);
        this.space(this.policy.SpacesWithinSynchronizedParentheses);
        this.rightParenthesis();
        this.writeEmbeddedStatement(node.getEmbeddedStatement());
        this.endNode(node);
        return null;
    }
    
    public static String convertCharacter(final char ch) {
        switch (ch) {
            case '\\': {
                return "\\\\";
            }
            case '\0': {
                return "\u0000";
            }
            case '\b': {
                return "\\b";
            }
            case '\f': {
                return "\\f";
            }
            case '\n': {
                return "\\n";
            }
            case '\r': {
                return "\\r";
            }
            case '\t': {
                return "\\t";
            }
            case '\"': {
                return "\\\"";
            }
            default: {
                if (ch >= '\u00c0' || Character.isISOControl(ch) || Character.isSurrogate(ch) || (Character.isWhitespace(ch) && ch != ' ')) {
                    return String.format("\\u%1$04x", (int)ch);
                }
                return String.valueOf(ch);
            }
        }
    }
    
    public static String escapeUnicode(final String s) {
        StringBuilder sb = null;
        for (int i = 0, n = s.length(); i < n; ++i) {
            final char ch = s.charAt(i);
            if (ch >= '\u00c0' || Character.isISOControl(ch) || Character.isSurrogate(ch) || (Character.isWhitespace(ch) && ch != ' ')) {
                if (sb == null) {
                    sb = new StringBuilder(Math.max(16, s.length()));
                    if (i > 0) {
                        sb.append(s, 0, i);
                    }
                }
                sb.append(String.format("\\u%1$04x", (int)ch));
            }
            else if (sb != null) {
                sb.append(ch);
            }
        }
        return (sb != null) ? sb.toString() : s;
    }
    
    public static boolean isKeyword(final String identifier) {
        return ArrayUtilities.contains(JavaOutputVisitor.KEYWORDS, identifier);
    }
    
    public static boolean isKeyword(final String identifier, final AstNode context) {
        return ArrayUtilities.contains(JavaOutputVisitor.KEYWORDS, identifier);
    }
    
    static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$decompiler$languages$java$BraceEnforcement() {
        final int[] loc_0 = JavaOutputVisitor.$SWITCH_TABLE$com$strobel$decompiler$languages$java$BraceEnforcement;
        if (loc_0 != null) {
            return loc_0;
        }
        final int[] loc_1 = new int[BraceEnforcement.values().length];
        try {
            loc_1[BraceEnforcement.AddBraces.ordinal()] = 3;
        }
        catch (NoSuchFieldError loc_2) {}
        try {
            loc_1[BraceEnforcement.DoNotChange.ordinal()] = 1;
        }
        catch (NoSuchFieldError loc_3) {}
        try {
            loc_1[BraceEnforcement.RemoveBraces.ordinal()] = 2;
        }
        catch (NoSuchFieldError loc_4) {}
        return JavaOutputVisitor.$SWITCH_TABLE$com$strobel$decompiler$languages$java$BraceEnforcement = loc_1;
    }
    
    static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$ClassType() {
        final int[] loc_0 = JavaOutputVisitor.$SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$ClassType;
        if (loc_0 != null) {
            return loc_0;
        }
        final int[] loc_1 = new int[ClassType.values().length];
        try {
            loc_1[ClassType.ANNOTATION.ordinal()] = 3;
        }
        catch (NoSuchFieldError loc_2) {}
        try {
            loc_1[ClassType.CLASS.ordinal()] = 1;
        }
        catch (NoSuchFieldError loc_3) {}
        try {
            loc_1[ClassType.ENUM.ordinal()] = 4;
        }
        catch (NoSuchFieldError loc_4) {}
        try {
            loc_1[ClassType.INTERFACE.ordinal()] = 2;
        }
        catch (NoSuchFieldError loc_5) {}
        return JavaOutputVisitor.$SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$ClassType = loc_1;
    }
    
    static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$BinaryOperatorType() {
        final int[] loc_0 = JavaOutputVisitor.$SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$BinaryOperatorType;
        if (loc_0 != null) {
            return loc_0;
        }
        final int[] loc_1 = new int[BinaryOperatorType.values().length];
        try {
            loc_1[BinaryOperatorType.ADD.ordinal()] = 13;
        }
        catch (NoSuchFieldError loc_2) {}
        try {
            loc_1[BinaryOperatorType.ANY.ordinal()] = 1;
        }
        catch (NoSuchFieldError loc_3) {}
        try {
            loc_1[BinaryOperatorType.BITWISE_AND.ordinal()] = 2;
        }
        catch (NoSuchFieldError loc_4) {}
        try {
            loc_1[BinaryOperatorType.BITWISE_OR.ordinal()] = 3;
        }
        catch (NoSuchFieldError loc_5) {}
        try {
            loc_1[BinaryOperatorType.DIVIDE.ordinal()] = 16;
        }
        catch (NoSuchFieldError loc_6) {}
        try {
            loc_1[BinaryOperatorType.EQUALITY.ordinal()] = 11;
        }
        catch (NoSuchFieldError loc_7) {}
        try {
            loc_1[BinaryOperatorType.EXCLUSIVE_OR.ordinal()] = 4;
        }
        catch (NoSuchFieldError loc_8) {}
        try {
            loc_1[BinaryOperatorType.GREATER_THAN.ordinal()] = 7;
        }
        catch (NoSuchFieldError loc_9) {}
        try {
            loc_1[BinaryOperatorType.GREATER_THAN_OR_EQUAL.ordinal()] = 8;
        }
        catch (NoSuchFieldError loc_10) {}
        try {
            loc_1[BinaryOperatorType.INEQUALITY.ordinal()] = 12;
        }
        catch (NoSuchFieldError loc_11) {}
        try {
            loc_1[BinaryOperatorType.LESS_THAN.ordinal()] = 9;
        }
        catch (NoSuchFieldError loc_12) {}
        try {
            loc_1[BinaryOperatorType.LESS_THAN_OR_EQUAL.ordinal()] = 10;
        }
        catch (NoSuchFieldError loc_13) {}
        try {
            loc_1[BinaryOperatorType.LOGICAL_AND.ordinal()] = 5;
        }
        catch (NoSuchFieldError loc_14) {}
        try {
            loc_1[BinaryOperatorType.LOGICAL_OR.ordinal()] = 6;
        }
        catch (NoSuchFieldError loc_15) {}
        try {
            loc_1[BinaryOperatorType.MODULUS.ordinal()] = 17;
        }
        catch (NoSuchFieldError loc_16) {}
        try {
            loc_1[BinaryOperatorType.MULTIPLY.ordinal()] = 15;
        }
        catch (NoSuchFieldError loc_17) {}
        try {
            loc_1[BinaryOperatorType.SHIFT_LEFT.ordinal()] = 18;
        }
        catch (NoSuchFieldError loc_18) {}
        try {
            loc_1[BinaryOperatorType.SHIFT_RIGHT.ordinal()] = 19;
        }
        catch (NoSuchFieldError loc_19) {}
        try {
            loc_1[BinaryOperatorType.SUBTRACT.ordinal()] = 14;
        }
        catch (NoSuchFieldError loc_20) {}
        try {
            loc_1[BinaryOperatorType.UNSIGNED_SHIFT_RIGHT.ordinal()] = 20;
        }
        catch (NoSuchFieldError loc_21) {}
        return JavaOutputVisitor.$SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$BinaryOperatorType = loc_1;
    }
    
    static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$UnaryOperatorType() {
        final int[] loc_0 = JavaOutputVisitor.$SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$UnaryOperatorType;
        if (loc_0 != null) {
            return loc_0;
        }
        final int[] loc_1 = new int[UnaryOperatorType.values().length];
        try {
            loc_1[UnaryOperatorType.ANY.ordinal()] = 1;
        }
        catch (NoSuchFieldError loc_2) {}
        try {
            loc_1[UnaryOperatorType.BITWISE_NOT.ordinal()] = 3;
        }
        catch (NoSuchFieldError loc_3) {}
        try {
            loc_1[UnaryOperatorType.DECREMENT.ordinal()] = 7;
        }
        catch (NoSuchFieldError loc_4) {}
        try {
            loc_1[UnaryOperatorType.INCREMENT.ordinal()] = 6;
        }
        catch (NoSuchFieldError loc_5) {}
        try {
            loc_1[UnaryOperatorType.MINUS.ordinal()] = 4;
        }
        catch (NoSuchFieldError loc_6) {}
        try {
            loc_1[UnaryOperatorType.NOT.ordinal()] = 2;
        }
        catch (NoSuchFieldError loc_7) {}
        try {
            loc_1[UnaryOperatorType.PLUS.ordinal()] = 5;
        }
        catch (NoSuchFieldError loc_8) {}
        try {
            loc_1[UnaryOperatorType.POST_DECREMENT.ordinal()] = 9;
        }
        catch (NoSuchFieldError loc_9) {}
        try {
            loc_1[UnaryOperatorType.POST_INCREMENT.ordinal()] = 8;
        }
        catch (NoSuchFieldError loc_10) {}
        return JavaOutputVisitor.$SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$UnaryOperatorType = loc_1;
    }
    
    enum LastWritten
    {
        Whitespace("Whitespace", 0), 
        Other("Other", 1), 
        KeywordOrIdentifier("KeywordOrIdentifier", 2), 
        Plus("Plus", 3), 
        Minus("Minus", 4), 
        Ampersand("Ampersand", 5), 
        QuestionMark("QuestionMark", 6), 
        Division("Division", 7), 
        Operator("Operator", 8), 
        Delimiter("Delimiter", 9), 
        LeftParenthesis("LeftParenthesis", 10);
    }
}
