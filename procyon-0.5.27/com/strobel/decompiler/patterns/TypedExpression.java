package com.strobel.decompiler.patterns;

import com.strobel.functions.*;
import com.strobel.decompiler.semantics.*;
import com.strobel.core.*;
import com.strobel.decompiler.languages.java.ast.*;
import com.strobel.assembler.metadata.*;

public class TypedExpression extends Pattern
{
    public static final int OPTION_EXACT = 1;
    public static final int OPTION_STRICT = 2;
    public static final int OPTION_ALLOW_UNCHECKED = 3;
    private final TypeReference _expressionType;
    private final String _groupName;
    private final Function<AstNode, ResolveResult> _resolver;
    private final int _options;
    
    public TypedExpression(final TypeReference expressionType, final Function<AstNode, ResolveResult> resolver) {
        this(expressionType, resolver, 0);
    }
    
    public TypedExpression(final TypeReference expressionType, final Function<AstNode, ResolveResult> resolver, final int options) {
        super();
        this._groupName = null;
        this._expressionType = VerifyArgument.notNull(expressionType, "expressionType");
        this._resolver = VerifyArgument.notNull(resolver, "resolver");
        this._options = options;
    }
    
    public TypedExpression(final String groupName, final TypeReference expressionType, final Function<AstNode, ResolveResult> resolver) {
        this(groupName, expressionType, resolver, 0);
    }
    
    public TypedExpression(final String groupName, final TypeReference expressionType, final Function<AstNode, ResolveResult> resolver, final int options) {
        super();
        this._groupName = groupName;
        this._expressionType = VerifyArgument.notNull(expressionType, "expressionType");
        this._resolver = VerifyArgument.notNull(resolver, "resolver");
        this._options = options;
    }
    
    public final TypeReference getExpressionType() {
        return this._expressionType;
    }
    
    public final String getGroupName() {
        return this._groupName;
    }
    
    @Override
    public final boolean matches(final INode other, final Match match) {
        if (!(other instanceof Expression) || other.isNull()) {
            return false;
        }
        final ResolveResult result = this._resolver.apply((Expression)other);
        if (result == null || result.getType() == null) {
            return false;
        }
        boolean isMatch;
        if (Flags.testAny(this._options, 1)) {
            isMatch = MetadataHelper.isSameType(this._expressionType, result.getType(), Flags.testAny(this._options, 2));
        }
        else {
            isMatch = MetadataHelper.isAssignableFrom(this._expressionType, result.getType(), Flags.testAny(this._options, 3));
        }
        if (isMatch) {
            match.add(this._groupName, other);
            return true;
        }
        return false;
    }
}
