package com.strobel.decompiler.ast;

import com.strobel.collections.*;
import com.strobel.decompiler.*;
import com.strobel.assembler.metadata.*;
import java.util.*;
import com.strobel.core.*;
import com.strobel.componentmodel.*;
import com.strobel.annotations.*;

public final class Expression extends Node implements Cloneable, UserDataStore
{
    public static final Object ANY_OPERAND;
    public static final int MYSTERY_OFFSET = -34;
    private final SmartList<Expression> _arguments;
    private final SmartList<Range> _ranges;
    private AstCode _code;
    private Object _operand;
    private int _offset;
    private TypeReference _expectedType;
    private TypeReference _inferredType;
    private UserDataStoreBase _userData;
    
    static {
        ANY_OPERAND = new Object();
    }
    
    public Expression(final AstCode code, final Object operand, final int offset, final List<Expression> arguments) {
        super();
        this._arguments = new SmartList<Expression>();
        this._ranges = new SmartList<Range>() {
            @Override
            public boolean add(final Range range) {
                return !this.contains(range) && super.add(range);
            }
            
            @Override
            public void add(final int index, final Range element) {
                if (this.contains(element)) {
                    return;
                }
                super.add(index, element);
            }
        };
        this._code = VerifyArgument.notNull(code, "code");
        this._operand = VerifyArgument.notInstanceOf(Expression.class, operand, "operand");
        this._offset = offset;
        if (arguments != null) {
            this._arguments.addAll((Collection<?>)arguments);
        }
    }
    
    public Expression(final AstCode code, final Object operand, final int offset, final Expression... arguments) {
        super();
        this._arguments = new SmartList<Expression>();
        this._ranges = new SmartList<Range>() {
            @Override
            public boolean add(final Range range) {
                return !this.contains(range) && super.add(range);
            }
            
            @Override
            public void add(final int index, final Range element) {
                if (this.contains(element)) {
                    return;
                }
                super.add(index, element);
            }
        };
        this._code = VerifyArgument.notNull(code, "code");
        this._operand = VerifyArgument.notInstanceOf(Expression.class, operand, "operand");
        this._offset = offset;
        if (arguments != null) {
            Collections.addAll(this._arguments, arguments);
        }
    }
    
    public final List<Expression> getArguments() {
        return this._arguments;
    }
    
    public final AstCode getCode() {
        return this._code;
    }
    
    public final void setCode(final AstCode code) {
        this._code = code;
    }
    
    public final Object getOperand() {
        return this._operand;
    }
    
    public final void setOperand(final Object operand) {
        this._operand = operand;
    }
    
    public final int getOffset() {
        return this._offset;
    }
    
    public final TypeReference getExpectedType() {
        return this._expectedType;
    }
    
    public final void setExpectedType(final TypeReference expectedType) {
        this._expectedType = expectedType;
    }
    
    public final TypeReference getInferredType() {
        return this._inferredType;
    }
    
    public final void setInferredType(final TypeReference inferredType) {
        this._inferredType = inferredType;
    }
    
    public final boolean isBranch() {
        return this._operand instanceof Label || this._operand instanceof Label[];
    }
    
    public final List<Label> getBranchTargets() {
        if (this._operand instanceof Label) {
            return Collections.singletonList(this._operand);
        }
        if (this._operand instanceof Label[]) {
            return ArrayUtilities.asUnmodifiableList((Label[])this._operand);
        }
        return Collections.emptyList();
    }
    
    public final List<Range> getRanges() {
        return this._ranges;
    }
    
    @Override
    public final List<Node> getChildren() {
        final ArrayList<Node> childrenCopy = new ArrayList<Node>();
        childrenCopy.addAll(this._arguments);
        if (this._operand instanceof Lambda) {
            childrenCopy.add((Node)this._operand);
        }
        return childrenCopy;
    }
    
    public final boolean containsReferenceTo(final Variable variable) {
        if (this._operand == variable) {
            return true;
        }
        for (int i = 0; i < this._arguments.size(); ++i) {
            if (this._arguments.get(i).containsReferenceTo(variable)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public final void writeTo(final ITextOutput output) {
        final AstCode code = this._code;
        final Object operand = this._operand;
        final TypeReference inferredType = this._inferredType;
        final TypeReference expectedType = this._expectedType;
        if (operand instanceof Variable) {
            if (AstCodeHelpers.isLocalStore(code)) {
                output.write(((Variable)operand).getName());
                output.write(" = ");
                this.getArguments().get(0).writeTo(output);
                return;
            }
            if (AstCodeHelpers.isLocalLoad(code)) {
                output.write(((Variable)operand).getName());
                if (inferredType != null) {
                    output.write(':');
                    DecompilerHelpers.writeType(output, inferredType, NameSyntax.SHORT_TYPE_NAME);
                    if (expectedType != null && !Comparer.equals(expectedType.getInternalName(), inferredType.getInternalName())) {
                        output.write("[expected:");
                        DecompilerHelpers.writeType(output, expectedType, NameSyntax.SHORT_TYPE_NAME);
                        output.write(']');
                    }
                }
                return;
            }
        }
        output.writeReference(code.name().toLowerCase(), code);
        if (inferredType != null) {
            output.write(':');
            DecompilerHelpers.writeType(output, inferredType, NameSyntax.SHORT_TYPE_NAME);
            if (expectedType != null && !Comparer.equals(expectedType.getInternalName(), inferredType.getInternalName())) {
                output.write("[expected:");
                DecompilerHelpers.writeType(output, expectedType, NameSyntax.SHORT_TYPE_NAME);
                output.write(']');
            }
        }
        else if (expectedType != null) {
            output.write("[expected:");
            DecompilerHelpers.writeType(output, expectedType, NameSyntax.SHORT_TYPE_NAME);
            output.write(']');
        }
        output.write('(');
        boolean first = true;
        if (operand != null) {
            if (operand instanceof Label) {
                output.writeReference(((Label)operand).getName(), operand);
            }
            else if (operand instanceof Label[]) {
                final Label[] labels = (Label[])operand;
                for (int i = 0; i < labels.length; ++i) {
                    if (i != 0) {
                        output.write(", ");
                    }
                    output.writeReference(labels[i].getName(), labels[i]);
                }
            }
            else if (operand instanceof MethodReference || operand instanceof FieldReference) {
                final MemberReference member = (MemberReference)operand;
                final TypeReference declaringType = member.getDeclaringType();
                if (declaringType != null) {
                    DecompilerHelpers.writeType(output, declaringType, NameSyntax.SHORT_TYPE_NAME);
                    output.write("::");
                }
                output.writeReference(member.getName(), member);
            }
            else if (operand instanceof Node) {
                ((Node)operand).writeTo(output);
            }
            else {
                DecompilerHelpers.writeOperand(output, operand);
            }
            first = false;
        }
        for (final Expression argument : this.getArguments()) {
            if (!first) {
                output.write(", ");
            }
            argument.writeTo(output);
            first = false;
        }
        output.write(')');
    }
    
    public final Expression clone() {
        final Expression clone = new Expression(this._code, this._operand, this._offset, new Expression[0]);
        clone._code = this._code;
        clone._expectedType = this._expectedType;
        clone._inferredType = this._inferredType;
        clone._operand = this._operand;
        clone._userData = ((this._userData != null) ? this._userData.clone() : null);
        clone._offset = this._offset;
        for (final Expression argument : this._arguments) {
            clone._arguments.add(argument.clone());
        }
        return clone;
    }
    
    public boolean isEquivalentTo(final Expression e) {
        if (e == null || this._code != e._code) {
            return false;
        }
        if (this._operand instanceof FieldReference) {
            if (!(e._operand instanceof FieldReference)) {
                return false;
            }
            final FieldReference f1 = (FieldReference)this._operand;
            final FieldReference f2 = (FieldReference)e._operand;
            if (!StringUtilities.equals(f1.getFullName(), f2.getFullName())) {
                return false;
            }
        }
        else if (this._operand instanceof MethodReference) {
            if (!(e._operand instanceof MethodReference)) {
                return false;
            }
            final MethodReference f3 = (MethodReference)this._operand;
            final MethodReference f4 = (MethodReference)e._operand;
            if (!StringUtilities.equals(f3.getFullName(), f4.getFullName()) || !StringUtilities.equals(f3.getErasedSignature(), f4.getErasedSignature())) {
                return false;
            }
        }
        else if (!Comparer.equals(e._operand, this._operand)) {
            return false;
        }
        if (this._arguments.size() != e._arguments.size()) {
            return false;
        }
        for (int i = 0, n = this._arguments.size(); i < n; ++i) {
            final Expression a1 = this._arguments.get(i);
            final Expression a2 = e._arguments.get(i);
            if (!a1.isEquivalentTo(a2)) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public <T> T getUserData(@NotNull final Key<T> key) {
        if (this._userData == null) {
            return null;
        }
        return this._userData.getUserData(key);
    }
    
    @Override
    public <T> void putUserData(@NotNull final Key<T> key, @Nullable final T value) {
        if (this._userData == null) {
            this._userData = new UserDataStoreBase();
        }
        this._userData.putUserData(key, value);
    }
    
    @Override
    public <T> T putUserDataIfAbsent(@NotNull final Key<T> key, @Nullable final T value) {
        if (this._userData == null) {
            this._userData = new UserDataStoreBase();
        }
        return this._userData.putUserDataIfAbsent(key, value);
    }
    
    @Override
    public <T> boolean replace(@NotNull final Key<T> key, @Nullable final T oldValue, @Nullable final T newValue) {
        if (this._userData == null) {
            this._userData = new UserDataStoreBase();
        }
        return this._userData.replace(key, oldValue, newValue);
    }
}
