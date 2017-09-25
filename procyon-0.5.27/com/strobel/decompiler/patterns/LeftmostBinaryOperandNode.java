package com.strobel.decompiler.patterns;

import com.strobel.core.*;
import com.strobel.decompiler.languages.java.ast.*;

public class LeftmostBinaryOperandNode extends Pattern
{
    private final boolean _matchWithoutOperator;
    private final BinaryOperatorType _operatorType;
    private final INode _operandPattern;
    
    public LeftmostBinaryOperandNode(final INode pattern) {
        this(pattern, BinaryOperatorType.ANY, false);
    }
    
    public LeftmostBinaryOperandNode(final INode pattern, final BinaryOperatorType type, final boolean matchWithoutOperator) {
        super();
        this._matchWithoutOperator = matchWithoutOperator;
        this._operatorType = VerifyArgument.notNull(type, "type");
        this._operandPattern = VerifyArgument.notNull(pattern, "pattern");
    }
    
    public final INode getOperandPattern() {
        return this._operandPattern;
    }
    
    @Override
    public boolean matches(final INode other, final Match match) {
        if (this._matchWithoutOperator || other instanceof BinaryOperatorExpression) {
            INode current;
            for (current = other; current instanceof BinaryOperatorExpression && (this._operatorType == BinaryOperatorType.ANY || ((BinaryOperatorExpression)current).getOperator() == this._operatorType); current = ((BinaryOperatorExpression)current).getLeft()) {}
            return current != null && this._operandPattern.matches(current, match);
        }
        return false;
    }
}
