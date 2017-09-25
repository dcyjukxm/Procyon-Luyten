package com.strobel.decompiler.languages.java.ast;

import com.strobel.decompiler.patterns.*;

public final class Roles
{
    public static final Role<AstNode> Root;
    public static final Role<AstType> TYPE;
    public static final Role<AstType> BASE_TYPE;
    public static final Role<AstType> IMPLEMENTED_INTERFACE;
    public static final Role<AstType> TYPE_ARGUMENT;
    public static final Role<AstType> EXTENDS_BOUND;
    public static final Role<AstType> SUPER_BOUND;
    public static final Role<TypeParameterDeclaration> TYPE_PARAMETER;
    public static final Role<Expression> ARGUMENT;
    public static final Role<ParameterDeclaration> PARAMETER;
    public static final Role<Expression> EXPRESSION;
    public static final Role<Expression> TARGET_EXPRESSION;
    public static final Role<Expression> CONDITION;
    public static final Role<Comment> COMMENT;
    public static final Role<Identifier> LABEL;
    public static final Role<Identifier> IDENTIFIER;
    public static final Role<Statement> EMBEDDED_STATEMENT;
    public static final Role<BlockStatement> BODY;
    public static final Role<Annotation> ANNOTATION;
    public static final Role<VariableInitializer> VARIABLE;
    public static final Role<EntityDeclaration> TYPE_MEMBER;
    public static final Role<TypeDeclaration> LOCAL_TYPE_DECLARATION;
    public static final Role<AstType> THROWN_TYPE;
    public static final Role<PackageDeclaration> PACKAGE;
    public static final Role<NewLineNode> NEW_LINE;
    public static final Role<TextNode> TEXT;
    public static final TokenRole LEFT_PARENTHESIS;
    public static final TokenRole RIGHT_PARENTHESIS;
    public static final TokenRole LEFT_BRACKET;
    public static final TokenRole RIGHT_BRACKET;
    public static final TokenRole LEFT_BRACE;
    public static final TokenRole RIGHT_BRACE;
    public static final TokenRole LEFT_CHEVRON;
    public static final TokenRole RIGHT_CHEVRON;
    public static final TokenRole COMMA;
    public static final TokenRole DOT;
    public static final TokenRole SEMICOLON;
    public static final TokenRole COLON;
    public static final TokenRole DOUBLE_COLON;
    public static final TokenRole ASSIGN;
    public static final TokenRole PIPE;
    public static final TokenRole VARARGS;
    public static final TokenRole DEFAULT_KEYWORD;
    public static final TokenRole PACKAGE_KEYWORD;
    public static final TokenRole ENUM_KEYWORD;
    public static final TokenRole INTERFACE_KEYWORD;
    public static final TokenRole CLASS_KEYWORD;
    public static final TokenRole ANNOTATION_KEYWORD;
    public static final TokenRole EXTENDS_KEYWORD;
    public static final TokenRole IMPLEMENTS_KEYWORD;
    
    static {
        Root = AstNode.ROOT_ROLE;
        TYPE = new Role<AstType>("Type", AstType.class, AstType.NULL);
        BASE_TYPE = new Role<AstType>("BaseType", AstType.class, AstType.NULL);
        IMPLEMENTED_INTERFACE = new Role<AstType>("ImplementedInterface", AstType.class, AstType.NULL);
        TYPE_ARGUMENT = new Role<AstType>("TypeArgument", AstType.class, AstType.NULL);
        EXTENDS_BOUND = new Role<AstType>("ExtendsBound", AstType.class, AstType.NULL);
        SUPER_BOUND = new Role<AstType>("SuperBound", AstType.class, AstType.NULL);
        TYPE_PARAMETER = new Role<TypeParameterDeclaration>("TypeParameter", TypeParameterDeclaration.class);
        ARGUMENT = new Role<Expression>("Argument", Expression.class, Expression.NULL);
        PARAMETER = new Role<ParameterDeclaration>("Parameter", ParameterDeclaration.class);
        EXPRESSION = new Role<Expression>("Expression", Expression.class, Expression.NULL);
        TARGET_EXPRESSION = new Role<Expression>("Target", Expression.class, Expression.NULL);
        CONDITION = new Role<Expression>("Condition", Expression.class, Expression.NULL);
        COMMENT = new Role<Comment>("Comment", Comment.class);
        LABEL = new Role<Identifier>("Label", Identifier.class, Identifier.NULL);
        IDENTIFIER = new Role<Identifier>("Identifier", Identifier.class, Identifier.NULL);
        EMBEDDED_STATEMENT = new Role<Statement>("EmbeddedStatement", Statement.class, Statement.NULL);
        BODY = new Role<BlockStatement>("Body", BlockStatement.class, BlockStatement.NULL);
        ANNOTATION = new Role<Annotation>("Annotation", Annotation.class);
        VARIABLE = new Role<VariableInitializer>("Variable", VariableInitializer.class, VariableInitializer.NULL);
        TYPE_MEMBER = new Role<EntityDeclaration>("TypeMember", EntityDeclaration.class);
        LOCAL_TYPE_DECLARATION = new Role<TypeDeclaration>("LocalTypeDeclaration", TypeDeclaration.class, TypeDeclaration.NULL);
        THROWN_TYPE = new Role<AstType>("ThrownType", AstType.class, AstType.NULL);
        PACKAGE = new Role<PackageDeclaration>("Package", PackageDeclaration.class, PackageDeclaration.NULL);
        NEW_LINE = new Role<NewLineNode>("NewLine", NewLineNode.class);
        TEXT = new Role<TextNode>("Text", TextNode.class);
        LEFT_PARENTHESIS = new TokenRole("(", 4);
        RIGHT_PARENTHESIS = new TokenRole(")", 4);
        LEFT_BRACKET = new TokenRole("[", 4);
        RIGHT_BRACKET = new TokenRole("]", 4);
        LEFT_BRACE = new TokenRole("{", 4);
        RIGHT_BRACE = new TokenRole("}", 4);
        LEFT_CHEVRON = new TokenRole("<", 4);
        RIGHT_CHEVRON = new TokenRole(">", 4);
        COMMA = new TokenRole(",", 4);
        DOT = new TokenRole(".", 4);
        SEMICOLON = new TokenRole(";", 4);
        COLON = new TokenRole(":", 4);
        DOUBLE_COLON = new TokenRole("::", 4);
        ASSIGN = new TokenRole("=", 2);
        PIPE = new TokenRole("|", 2);
        VARARGS = new TokenRole("...", 4);
        DEFAULT_KEYWORD = new TokenRole("default", 1);
        PACKAGE_KEYWORD = new TokenRole("package", 1);
        ENUM_KEYWORD = new TokenRole("enum", 1);
        INTERFACE_KEYWORD = new TokenRole("interface", 1);
        CLASS_KEYWORD = new TokenRole("class", 1);
        ANNOTATION_KEYWORD = new TokenRole("@interface", 1);
        EXTENDS_KEYWORD = new TokenRole("extends", 1);
        IMPLEMENTS_KEYWORD = new TokenRole("implements", 1);
    }
}
