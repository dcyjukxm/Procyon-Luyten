package com.strobel.decompiler.patterns;

import com.strobel.core.*;
import com.strobel.decompiler.languages.java.ast.*;
import java.util.*;

public abstract class Pattern implements INode
{
    public static final String ANY_STRING = "$any$";
    
    public static boolean matchString(final String pattern, final String text) {
        return "$any$".equals(pattern) || StringUtilities.equals(pattern, text);
    }
    
    public final AstNode toNode() {
        return AstNode.forPattern(this);
    }
    
    public final Expression toExpression() {
        return Expression.forPattern(this);
    }
    
    public final Statement toStatement() {
        return Statement.forPattern(this);
    }
    
    public final BlockStatement toBlockStatement() {
        return BlockStatement.forPattern(this);
    }
    
    public final CatchClause toCatchClause() {
        return CatchClause.forPattern(this);
    }
    
    public final VariableInitializer toVariableInitializer() {
        return VariableInitializer.forPattern(this);
    }
    
    public final ParameterDeclaration toParameterDeclaration() {
        return ParameterDeclaration.forPattern(this);
    }
    
    public final AstType toType() {
        return AstType.forPattern(this);
    }
    
    @Override
    public boolean isNull() {
        return false;
    }
    
    @Override
    public Role getRole() {
        return null;
    }
    
    @Override
    public INode getFirstChild() {
        return null;
    }
    
    @Override
    public INode getNextSibling() {
        return null;
    }
    
    @Override
    public abstract boolean matches(final INode param_0, final Match param_1);
    
    @Override
    public boolean matchesCollection(final Role role, final INode position, final Match match, final BacktrackingInfo backtrackingInfo) {
        return this.matches(position, match);
    }
    
    @Override
    public final Match match(final INode other) {
        final Match match = Match.createNew();
        return this.matches(other, match) ? match : Match.failure();
    }
    
    @Override
    public final boolean matches(final INode other) {
        return this.matches(other, Match.createNew());
    }
    
    public static boolean matchesCollection(final Role<?> role, final INode firstPatternChild, final INode firstOtherChild, final Match match) {
        final BacktrackingInfo backtrackingInfo = new BacktrackingInfo();
        final Stack<INode> patternStack = new Stack<INode>();
        final Stack<PossibleMatch> stack = backtrackingInfo.stack;
        patternStack.push(firstPatternChild);
        stack.push(new PossibleMatch(firstOtherChild, match.getCheckPoint()));
        while (!stack.isEmpty()) {
            INode current1 = patternStack.pop();
            INode current2 = stack.peek().nextOther;
            match.restoreCheckPoint(stack.pop().checkPoint);
            boolean success = true;
            while (current1 != null) {
                if (!success) {
                    break;
                }
                while (current1 != null) {
                    if (current1.getRole() == role) {
                        break;
                    }
                    current1 = current1.getNextSibling();
                }
                while (current2 != null && current2.getRole() != role) {
                    current2 = current2.getNextSibling();
                }
                if (current1 == null) {
                    break;
                }
                assert stack.size() == patternStack.size();
                success = current1.matchesCollection(role, current2, match, backtrackingInfo);
                assert stack.size() >= patternStack.size();
                while (stack.size() > patternStack.size()) {
                    patternStack.push(current1.getNextSibling());
                }
                current1 = current1.getNextSibling();
                if (current2 == null) {
                    continue;
                }
                current2 = current2.getNextSibling();
            }
            while (current2 != null && current2.getRole() != role) {
                current2 = current2.getNextSibling();
            }
            if (success && current2 == null) {
                return true;
            }
        }
        return false;
    }
}
