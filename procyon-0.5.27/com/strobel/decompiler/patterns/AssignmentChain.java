package com.strobel.decompiler.patterns;

import com.strobel.core.*;
import java.util.*;
import com.strobel.decompiler.languages.java.ast.*;

public class AssignmentChain extends Pattern
{
    private final INode _valuePattern;
    private final INode _targetPattern;
    
    public AssignmentChain(final INode targetPattern, final INode valuePattern) {
        super();
        this._targetPattern = VerifyArgument.notNull(targetPattern, "targetPattern");
        this._valuePattern = VerifyArgument.notNull(valuePattern, "valuePattern");
    }
    
    public final INode getTargetPattern() {
        return this._targetPattern;
    }
    
    public final INode getValuePattern() {
        return this._valuePattern;
    }
    
    @Override
    public boolean matches(final INode other, final Match match) {
        if (!(other instanceof AssignmentExpression)) {
            return false;
        }
        final ArrayDeque<AssignmentExpression> assignments = new ArrayDeque<AssignmentExpression>();
        INode current = other;
        final int checkPoint = match.getCheckPoint();
        while (current instanceof AssignmentExpression && ((AssignmentExpression)current).getOperator() == AssignmentOperatorType.ASSIGN) {
            final AssignmentExpression assignment = (AssignmentExpression)current;
            final Expression target = assignment.getLeft();
            if (!this._targetPattern.matches(target, match)) {
                assignments.clear();
                match.restoreCheckPoint(checkPoint);
                break;
            }
            assignments.addLast(assignment);
            current = assignment.getRight();
        }
        if (assignments.isEmpty() || !this._valuePattern.matches(assignments.getLast().getRight(), match)) {
            match.restoreCheckPoint(checkPoint);
            return false;
        }
        return true;
    }
}
