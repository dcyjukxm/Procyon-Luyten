package com.strobel.decompiler.patterns;

import com.strobel.core.*;
import com.strobel.decompiler.languages.java.ast.*;

public final class SingleOrBinaryAggregateNode extends Pattern
{
    private final INode _pattern;
    private final BinaryOperatorType _operator;
    
    public SingleOrBinaryAggregateNode(final BinaryOperatorType operator, final INode pattern) {
        super();
        this._pattern = VerifyArgument.notNull(pattern, "pattern");
        this._operator = VerifyArgument.notNull(operator, "operator");
    }
    
    @Override
    public boolean matches(final INode other, final Match match) {
        if (this._pattern.matches(other, match)) {
            return true;
        }
        if (other instanceof BinaryOperatorExpression) {
            final BinaryOperatorExpression binary = (BinaryOperatorExpression)other;
            if (this._operator != BinaryOperatorType.ANY && binary.getOperator() != this._operator) {
                return false;
            }
            final int checkPoint = match.getCheckPoint();
            if (this.matches(binary.getLeft(), match) && this.matches(binary.getRight(), match)) {
                return true;
            }
            match.restoreCheckPoint(checkPoint);
        }
        return false;
    }
}
