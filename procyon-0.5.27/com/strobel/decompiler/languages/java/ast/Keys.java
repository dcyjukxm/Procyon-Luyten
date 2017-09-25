package com.strobel.decompiler.languages.java.ast;

import com.strobel.componentmodel.*;
import com.strobel.decompiler.ast.*;
import com.strobel.assembler.metadata.*;
import java.util.*;
import com.strobel.core.*;
import java.lang.reflect.*;

public final class Keys
{
    public static final Key<Variable> VARIABLE;
    public static final Key<VariableDefinition> VARIABLE_DEFINITION;
    public static final Key<ParameterDefinition> PARAMETER_DEFINITION;
    public static final Key<MemberReference> MEMBER_REFERENCE;
    public static final Key<PackageReference> PACKAGE_REFERENCE;
    public static final Key<FieldDefinition> FIELD_DEFINITION;
    public static final Key<MethodDefinition> METHOD_DEFINITION;
    public static final Key<TypeDefinition> TYPE_DEFINITION;
    public static final Key<TypeReference> TYPE_REFERENCE;
    public static final Key<TypeReference> ANONYMOUS_BASE_TYPE_REFERENCE;
    public static final Key<DynamicCallSite> DYNAMIC_CALL_SITE;
    public static final Key<AstBuilder> AST_BUILDER;
    public static final Key<Object> CONSTANT_VALUE;
    public static final List<Key<?>> ALL_KEYS;
    
    static {
        VARIABLE = Key.create("Variable");
        VARIABLE_DEFINITION = Key.create("VariableDefinition");
        PARAMETER_DEFINITION = Key.create("ParameterDefinition");
        MEMBER_REFERENCE = Key.create("MemberReference");
        PACKAGE_REFERENCE = Key.create("PackageReference");
        FIELD_DEFINITION = Key.create("FieldDefinition");
        METHOD_DEFINITION = Key.create("MethodDefinition");
        TYPE_DEFINITION = Key.create("TypeDefinition");
        TYPE_REFERENCE = Key.create("TypeReference");
        ANONYMOUS_BASE_TYPE_REFERENCE = Key.create("AnonymousBaseTypeReference");
        DYNAMIC_CALL_SITE = Key.create("DynamicCallSite");
        AST_BUILDER = Key.create("AstBuilder");
        CONSTANT_VALUE = Key.create("ConstantValue");
        final ArrayList<Key<?>> keys = new ArrayList<Key<?>>();
        try {
            Field[] loc_1;
            for (int loc_0 = (loc_1 = Keys.class.getDeclaredFields()).length, loc_2 = 0; loc_2 < loc_0; ++loc_2) {
                final Field field = loc_1[loc_2];
                if (field.getType() == Key.class) {
                    keys.add((Key)field.get(null));
                }
            }
            ALL_KEYS = ArrayUtilities.asUnmodifiableList((Key<?>[])keys.toArray((T[])new Key[keys.size()]));
        }
        catch (Throwable t) {
            throw ExceptionUtilities.asRuntimeException(t);
        }
    }
}
