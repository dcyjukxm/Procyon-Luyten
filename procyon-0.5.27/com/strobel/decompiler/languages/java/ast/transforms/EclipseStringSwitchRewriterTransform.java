package com.strobel.decompiler.languages.java.ast.transforms;

import com.strobel.decompiler.*;
import com.strobel.core.*;
import com.strobel.assembler.metadata.*;
import com.strobel.decompiler.patterns.*;
import com.strobel.decompiler.languages.java.ast.*;
import java.util.*;

public class EclipseStringSwitchRewriterTransform extends ContextTrackingVisitor<Void>
{
    private static final Pattern HASH_CODE_PATTERN;
    private static final BlockStatement CASE_BODY_PATTERN;
    
    static {
        HASH_CODE_PATTERN = new NamedNode("hashCodeCall", new InvocationExpression(-34, new MemberReferenceExpression(-34, new AnyNode("target").toExpression(), "hashCode", new AstType[0]), new Expression[0]));
        final BlockStatement caseBody = new BlockStatement();
        final IfElseStatement test = new IfElseStatement(-34, new UnaryOperatorExpression(UnaryOperatorType.NOT, new SingleOrBinaryAggregateNode(BinaryOperatorType.LOGICAL_OR, new InvocationExpression(-34, new MemberReferenceExpression(-34, new NamedNode("input", new IdentifierExpression(-34, "$any$")).toExpression(), "equals", new AstType[0]), new Expression[] { new NamedNode("stringValue", new PrimitiveExpression(-34, "$any$")).toExpression() })).toExpression()), new BlockStatement(new Statement[] { new Choice(new INode[] { new NamedNode("defaultBreak", new BreakStatement(-34, "$any$")), new ReturnStatement(-34) }).toStatement() }));
        caseBody.add(new NamedNode("test", test).toStatement());
        caseBody.add(new Repeat(new AnyNode("statements")).toStatement());
        CASE_BODY_PATTERN = caseBody;
    }
    
    public EclipseStringSwitchRewriterTransform(final DecompilerContext context) {
        super(context);
    }
    
    @Override
    public Void visitSwitchStatement(final SwitchStatement node, final Void data) {
        super.visitSwitchStatement(node, data);
        final Expression input = node.getExpression();
        if (input == null || input.isNull()) {
            return null;
        }
        Match m2 = EclipseStringSwitchRewriterTransform.HASH_CODE_PATTERN.match(input);
        if (!m2.success()) {
            return null;
        }
        final InvocationExpression hashCodeCall = CollectionUtilities.first(m2.get("hashCodeCall"));
        final MemberReference hashCodeMethod = hashCodeCall.getUserData(Keys.MEMBER_REFERENCE);
        if (!(hashCodeMethod instanceof MethodReference) || !"java/lang/String".equals(hashCodeMethod.getDeclaringType().getInternalName())) {
            return null;
        }
        final List<Match> matches = new ArrayList<Match>();
        final AstNodeCollection<SwitchSection> sections = node.getSwitchSections();
        for (final SwitchSection section : sections) {
            final AstNodeCollection<CaseLabel> caseLabels = section.getCaseLabels();
            if (caseLabels.isEmpty() || (caseLabels.hasSingleElement() && caseLabels.firstOrNullObject().isNull())) {
                return null;
            }
            m2 = EclipseStringSwitchRewriterTransform.CASE_BODY_PATTERN.match(section.getStatements().firstOrNullObject());
            if (!m2.success()) {
                return null;
            }
            matches.add(m2);
        }
        int matchIndex = 0;
        BreakStatement defaultBreak = null;
        for (final SwitchSection section2 : sections) {
            final Match i = matches.get(matchIndex++);
            final IfElseStatement test = CollectionUtilities.first(i.get("test"));
            final List<PrimitiveExpression> stringValues = CollectionUtilities.toList(i.get("stringValue"));
            final AstNodeCollection<CaseLabel> caseLabels2 = section2.getCaseLabels();
            if (defaultBreak == null) {
                defaultBreak = CollectionUtilities.firstOrDefault(i.get("defaultBreak"));
            }
            caseLabels2.clear();
            test.remove();
            for (int j = 0; j < stringValues.size(); ++j) {
                final PrimitiveExpression stringValue = stringValues.get(j);
                stringValue.remove();
                caseLabels2.add(new CaseLabel(stringValue));
            }
        }
        if (defaultBreak != null) {
            final SwitchSection defaultSection = new SwitchSection();
            defaultBreak.remove();
            defaultSection.getCaseLabels().add(new CaseLabel());
            defaultSection.getStatements().add(defaultBreak);
            sections.add(defaultSection);
        }
        final AstNode newInput = CollectionUtilities.first(m2.get("target"));
        newInput.remove();
        node.getExpression().replaceWith(newInput);
        return null;
    }
}
