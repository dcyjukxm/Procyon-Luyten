package com.strobel.decompiler.languages.java.ast;

import com.strobel.decompiler.patterns.*;
import com.strobel.assembler.metadata.*;
import com.strobel.core.*;

public final class TokenRole extends Role<JavaTokenNode>
{
    public static final byte FLAG_KEYWORD = 1;
    public static final byte FLAG_OPERATOR = 2;
    public static final byte FLAG_DELIMITER = 4;
    private final String _token;
    private final int _length;
    private final byte _flags;
    
    public final String getToken() {
        return this._token;
    }
    
    public final int getLength() {
        return this._length;
    }
    
    public final boolean isKeyword() {
        return Flags.testAny(this._flags, 1);
    }
    
    public final boolean isOperator() {
        return Flags.testAny(this._flags, 2);
    }
    
    public final boolean isDelimiter() {
        return Flags.testAny(this._flags, 4);
    }
    
    public TokenRole(final String token) {
        this(token, 0);
    }
    
    public TokenRole(final String token, final int flags) {
        super(token, JavaTokenNode.class, JavaTokenNode.NULL);
        this._token = VerifyArgument.notNull(token, "token");
        this._length = token.length();
        this._flags = (byte)(flags & 0xFF);
    }
}
