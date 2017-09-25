package com.strobel.decompiler.languages.java.ast.transforms;

import com.strobel.decompiler.*;
import com.strobel.assembler.metadata.*;
import com.strobel.core.*;
import com.strobel.decompiler.languages.java.ast.*;
import com.strobel.decompiler.patterns.*;
import java.util.*;

public class StringSwitchRewriterTransform extends ContextTrackingVisitor<Void>
{
    private static final VariableDeclarationStatement TABLE_SWITCH_INPUT;
    private static final Pattern HASH_CODE_PATTERN;
    private static final BlockStatement CASE_BODY_PATTERN;
    
    static {
        final SimpleType intType = new SimpleType("int");
        intType.putUserData(Keys.TYPE_REFERENCE, BuiltinTypes.Integer);
        TABLE_SWITCH_INPUT = new VariableDeclarationStatement(intType, "$any$", new PrimitiveExpression(-34, -1));
        HASH_CODE_PATTERN = new NamedNode("hashCodeCall", new InvocationExpression(-34, new MemberReferenceExpression(-34, new AnyNode("target").toExpression(), "hashCode", new AstType[0]), new Expression[0]));
        final BlockStatement caseBody = new BlockStatement();
        final IfElseStatement test = new IfElseStatement(-34, new InvocationExpression(-34, new MemberReferenceExpression(-34, new NamedNode("input", new IdentifierExpression(-34, "$any$")).toExpression(), "equals", new AstType[0]), new Expression[] { new NamedNode("stringValue", new PrimitiveExpression(-34, "$any$")).toExpression() }), new BlockStatement(new Statement[] { new ExpressionStatement(new AssignmentExpression(new NamedNode("tableSwitchInput", new IdentifierExpression(-34, "$any$")).toExpression(), new NamedNode("tableSwitchCaseValue", new PrimitiveExpression(-34, PrimitiveExpression.ANY_VALUE)).toExpression())), new OptionalNode(new BreakStatement(-34)).toStatement() }));
        final IfElseStatement additionalTest = new IfElseStatement(-34, new InvocationExpression(-34, new MemberReferenceExpression(-34, new IdentifierExpressionBackReference("input").toExpression(), "equals", new AstType[0]), new Expression[] { new NamedNode("stringValue", new PrimitiveExpression(-34, "$any$")).toExpression() }), new BlockStatement(new Statement[] { new ExpressionStatement(new AssignmentExpression(new IdentifierExpressionBackReference("tableSwitchInput").toExpression(), new NamedNode("tableSwitchCaseValue", new PrimitiveExpression(-34, PrimitiveExpression.ANY_VALUE)).toExpression())), new OptionalNode(new BreakStatement(-34)).toStatement() }));
        caseBody.add(test);
        caseBody.add(new Repeat(additionalTest).toStatement());
        caseBody.add(new BreakStatement(-34));
        CASE_BODY_PATTERN = caseBody;
    }
    
    public StringSwitchRewriterTransform(final DecompilerContext context) {
        super(context);
    }
    
    @Override
    public Void visitSwitchStatement(final SwitchStatement node, final Void data) {
        super.visitSwitchStatement(node, data);
        final Statement previous = node.getPreviousStatement();
        if (previous == null || previous.isNull()) {
            return null;
        }
        Statement next = node.getNextStatement();
        if (next == null || next.isNull()) {
            return null;
        }
        if (!(next instanceof SwitchStatement)) {
            next = next.getNextStatement();
            if (next == null || next.isNull()) {
                return null;
            }
        }
        if (!(next instanceof SwitchStatement)) {
            return null;
        }
        final Match m1 = StringSwitchRewriterTransform.TABLE_SWITCH_INPUT.match(previous);
        if (!m1.success()) {
            return null;
        }
        final Expression input = node.getExpression();
        if (input == null || input.isNull()) {
            return null;
        }
        final Match m2 = StringSwitchRewriterTransform.HASH_CODE_PATTERN.match(input);
        if (!m2.success()) {
            return null;
        }
        final InvocationExpression hashCodeCall = CollectionUtilities.first(m2.get("hashCodeCall"));
        final MemberReference hashCodeMethod = hashCodeCall.getUserData(Keys.MEMBER_REFERENCE);
        if (!(hashCodeMethod instanceof MethodReference) || !"java/lang/String".equals(hashCodeMethod.getDeclaringType().getInternalName())) {
            return null;
        }
        final Map<Integer, List<String>> tableInputMap = new LinkedHashMap<Integer, List<String>>();
        IdentifierExpression tableSwitchInput = null;
        for (final SwitchSection section : node.getSwitchSections()) {
            final Match m3 = StringSwitchRewriterTransform.CASE_BODY_PATTERN.match(section.getStatements().firstOrNullObject());
            if (!m3.success()) {
                return null;
            }
            if (tableSwitchInput == null) {
                tableSwitchInput = CollectionUtilities.first(m3.get("tableSwitchInput"));
                assert tableSwitchInput != null;
            }
            final List<PrimitiveExpression> stringValues = CollectionUtilities.toList(m3.get("stringValue"));
            final List<PrimitiveExpression> tableSwitchCaseValues = CollectionUtilities.toList(m3.get("tableSwitchCaseValue"));
            if (stringValues.isEmpty() || stringValues.size() != tableSwitchCaseValues.size()) {
                return null;
            }
            for (int i = 0; i < stringValues.size(); ++i) {
                final PrimitiveExpression stringValue = stringValues.get(i);
                final PrimitiveExpression tableSwitchCaseValue = tableSwitchCaseValues.get(i);
                if (!(tableSwitchCaseValue.getValue() instanceof Integer)) {
                    return null;
                }
                final Integer k = (Integer)tableSwitchCaseValue.getValue();
                final String v = (String)stringValue.getValue();
                List<String> list = tableInputMap.get(k);
                if (list == null) {
                    tableInputMap.put(k, list = new ArrayList<String>());
                }
                list.add(v);
            }
        }
        if (tableSwitchInput == null) {
            return null;
        }
        final SwitchStatement tableSwitch = (SwitchStatement)next;
        if (!tableSwitchInput.matches(tableSwitch.getExpression())) {
            return null;
        }
        final boolean allCasesFound = CollectionUtilities.all(tableSwitch.getSwitchSections(), new Predicate<SwitchSection>() {
            @Override
            public boolean test(final SwitchSection s) {
                return !s.getCaseLabels().isEmpty() && CollectionUtilities.all(s.getCaseLabels(), new Predicate<CaseLabel>() {
                    @Override
                    public boolean test(final CaseLabel c) {
                        return c.getExpression().isNull() || (c.getExpression() instanceof PrimitiveExpression && ((PrimitiveExpression)c.getExpression()).getValue() instanceof Integer && tableInputMap.containsKey(((PrimitiveExpression)c.getExpression()).getValue()));
                    }
                });
            }
        });
        if (!allCasesFound) {
            return null;
        }
        final AstNode newInput = CollectionUtilities.first(m2.get("target"));
        newInput.remove();
        tableSwitch.getExpression().replaceWith(newInput);
        for (final SwitchSection s : tableSwitch.getSwitchSections()) {
            for (final CaseLabel c : s.getCaseLabels()) {
                if (c.getExpression() != null) {
                    if (c.getExpression().isNull()) {
                        continue;
                    }
                    final PrimitiveExpression test = (PrimitiveExpression)c.getExpression();
                    final Integer testValue = (Integer)test.getValue();
                    final List<String> stringValues2 = tableInputMap.get(testValue);
                    assert stringValues2 != null && !stringValues2.isEmpty();
                    test.setValue(stringValues2.get(0));
                    CaseLabel insertionPoint = c;
                    for (int j = 1; j < stringValues2.size(); ++j) {
                        final CaseLabel newLabel = new CaseLabel(new PrimitiveExpression(-34, stringValues2.get(j)));
                        s.getCaseLabels().insertAfter(insertionPoint, newLabel);
                        insertionPoint = newLabel;
                    }
                }
            }
        }
        node.remove();
        previous.remove();
        return null;
    }
}
