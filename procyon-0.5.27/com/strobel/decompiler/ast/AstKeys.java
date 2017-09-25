package com.strobel.decompiler.ast;

import com.strobel.componentmodel.*;
import java.util.*;
import com.strobel.assembler.metadata.*;
import com.strobel.util.*;

public final class AstKeys
{
    public static final Key<SwitchInfo> SWITCH_INFO;
    public static final Key<Expression> PARENT_LAMBDA_BINDING;
    public static final Key<List<TypeReference>> TYPE_ARGUMENTS;
    
    static {
        SWITCH_INFO = Key.create("SwitchInfo");
        PARENT_LAMBDA_BINDING = Key.create("ParentLambdaBinding");
        TYPE_ARGUMENTS = Key.create("TypeArguments");
    }
    
    private AstKeys() {
        super();
        throw ContractUtils.unreachable();
    }
}
