package com.strobel.decompiler.languages.java.ast;

import com.strobel.decompiler.patterns.*;

public interface IAstVisitor<T, R>
{
    R visitComment(Comment param_0, T param_1);
    
    R visitPatternPlaceholder(AstNode param_0, Pattern param_1, T param_2);
    
    R visitInvocationExpression(InvocationExpression param_0, T param_1);
    
    R visitTypeReference(TypeReferenceExpression param_0, T param_1);
    
    R visitJavaTokenNode(JavaTokenNode param_0, T param_1);
    
    R visitMemberReferenceExpression(MemberReferenceExpression param_0, T param_1);
    
    R visitIdentifier(Identifier param_0, T param_1);
    
    R visitNullReferenceExpression(NullReferenceExpression param_0, T param_1);
    
    R visitThisReferenceExpression(ThisReferenceExpression param_0, T param_1);
    
    R visitSuperReferenceExpression(SuperReferenceExpression param_0, T param_1);
    
    R visitClassOfExpression(ClassOfExpression param_0, T param_1);
    
    R visitBlockStatement(BlockStatement param_0, T param_1);
    
    R visitExpressionStatement(ExpressionStatement param_0, T param_1);
    
    R visitBreakStatement(BreakStatement param_0, T param_1);
    
    R visitContinueStatement(ContinueStatement param_0, T param_1);
    
    R visitDoWhileStatement(DoWhileStatement param_0, T param_1);
    
    R visitEmptyStatement(EmptyStatement param_0, T param_1);
    
    R visitIfElseStatement(IfElseStatement param_0, T param_1);
    
    R visitLabelStatement(LabelStatement param_0, T param_1);
    
    R visitLabeledStatement(LabeledStatement param_0, T param_1);
    
    R visitReturnStatement(ReturnStatement param_0, T param_1);
    
    R visitSwitchStatement(SwitchStatement param_0, T param_1);
    
    R visitSwitchSection(SwitchSection param_0, T param_1);
    
    R visitCaseLabel(CaseLabel param_0, T param_1);
    
    R visitThrowStatement(ThrowStatement param_0, T param_1);
    
    R visitCatchClause(CatchClause param_0, T param_1);
    
    R visitAnnotation(Annotation param_0, T param_1);
    
    R visitNewLine(NewLineNode param_0, T param_1);
    
    R visitVariableDeclaration(VariableDeclarationStatement param_0, T param_1);
    
    R visitVariableInitializer(VariableInitializer param_0, T param_1);
    
    R visitText(TextNode param_0, T param_1);
    
    R visitImportDeclaration(ImportDeclaration param_0, T param_1);
    
    R visitSimpleType(SimpleType param_0, T param_1);
    
    R visitMethodDeclaration(MethodDeclaration param_0, T param_1);
    
    R visitInitializerBlock(InstanceInitializer param_0, T param_1);
    
    R visitConstructorDeclaration(ConstructorDeclaration param_0, T param_1);
    
    R visitTypeParameterDeclaration(TypeParameterDeclaration param_0, T param_1);
    
    R visitParameterDeclaration(ParameterDeclaration param_0, T param_1);
    
    R visitFieldDeclaration(FieldDeclaration param_0, T param_1);
    
    R visitTypeDeclaration(TypeDeclaration param_0, T param_1);
    
    R visitCompilationUnit(CompilationUnit param_0, T param_1);
    
    R visitPackageDeclaration(PackageDeclaration param_0, T param_1);
    
    R visitArraySpecifier(ArraySpecifier param_0, T param_1);
    
    R visitComposedType(ComposedType param_0, T param_1);
    
    R visitWhileStatement(WhileStatement param_0, T param_1);
    
    R visitPrimitiveExpression(PrimitiveExpression param_0, T param_1);
    
    R visitCastExpression(CastExpression param_0, T param_1);
    
    R visitBinaryOperatorExpression(BinaryOperatorExpression param_0, T param_1);
    
    R visitInstanceOfExpression(InstanceOfExpression param_0, T param_1);
    
    R visitIndexerExpression(IndexerExpression param_0, T param_1);
    
    R visitIdentifierExpression(IdentifierExpression param_0, T param_1);
    
    R visitUnaryOperatorExpression(UnaryOperatorExpression param_0, T param_1);
    
    R visitConditionalExpression(ConditionalExpression param_0, T param_1);
    
    R visitArrayInitializerExpression(ArrayInitializerExpression param_0, T param_1);
    
    R visitObjectCreationExpression(ObjectCreationExpression param_0, T param_1);
    
    R visitArrayCreationExpression(ArrayCreationExpression param_0, T param_1);
    
    R visitAssignmentExpression(AssignmentExpression param_0, T param_1);
    
    R visitForStatement(ForStatement param_0, T param_1);
    
    R visitForEachStatement(ForEachStatement param_0, T param_1);
    
    R visitTryCatchStatement(TryCatchStatement param_0, T param_1);
    
    R visitGotoStatement(GotoStatement param_0, T param_1);
    
    R visitParenthesizedExpression(ParenthesizedExpression param_0, T param_1);
    
    R visitSynchronizedStatement(SynchronizedStatement param_0, T param_1);
    
    R visitAnonymousObjectCreationExpression(AnonymousObjectCreationExpression param_0, T param_1);
    
    R visitWildcardType(WildcardType param_0, T param_1);
    
    R visitMethodGroupExpression(MethodGroupExpression param_0, T param_1);
    
    R visitEnumValueDeclaration(EnumValueDeclaration param_0, T param_1);
    
    R visitAssertStatement(AssertStatement param_0, T param_1);
    
    R visitLambdaExpression(LambdaExpression param_0, T param_1);
    
    R visitLocalTypeDeclarationStatement(LocalTypeDeclarationStatement param_0, T param_1);
}
