package com.strobel.decompiler.ast;

import com.strobel.assembler.*;
import com.strobel.assembler.metadata.*;
import com.strobel.decompiler.*;
import java.util.*;

public class Lambda extends Node
{
    private final Collection<Variable> _parameters;
    private DynamicCallSite _callSite;
    private MethodReference _method;
    private TypeReference _functionType;
    private Block _body;
    private TypeReference _expectedReturnType;
    private TypeReference _inferredReturnType;
    
    public Lambda() {
        super();
        this._parameters = new Collection<Variable>();
    }
    
    public Lambda(final Block body) {
        super();
        this._parameters = new Collection<Variable>();
        this._body = body;
    }
    
    public Lambda(final Block body, final TypeReference functionType) {
        super();
        this._parameters = new Collection<Variable>();
        this._body = body;
        this._functionType = functionType;
    }
    
    public final List<Variable> getParameters() {
        return this._parameters;
    }
    
    public final DynamicCallSite getCallSite() {
        return this._callSite;
    }
    
    public final void setCallSite(final DynamicCallSite callSite) {
        this._callSite = callSite;
    }
    
    public final Block getBody() {
        return this._body;
    }
    
    public final void setBody(final Block body) {
        this._body = body;
    }
    
    public final TypeReference getFunctionType() {
        return this._functionType;
    }
    
    public final void setFunctionType(final TypeReference functionType) {
        this._functionType = functionType;
    }
    
    public final MethodReference getMethod() {
        return this._method;
    }
    
    public final void setMethod(final MethodReference method) {
        this._method = method;
    }
    
    public final TypeReference getExpectedReturnType() {
        return this._expectedReturnType;
    }
    
    public final void setExpectedReturnType(final TypeReference expectedReturnType) {
        this._expectedReturnType = expectedReturnType;
    }
    
    public final TypeReference getInferredReturnType() {
        return this._inferredReturnType;
    }
    
    public final void setInferredReturnType(final TypeReference inferredReturnType) {
        this._inferredReturnType = inferredReturnType;
    }
    
    @Override
    public List<Node> getChildren() {
        return (List<Node>)((this._body != null) ? Collections.singletonList(this._body) : Collections.emptyList());
    }
    
    @Override
    public final void writeTo(final ITextOutput output) {
        output.write("(");
        boolean comma = false;
        for (final Variable parameter : this._parameters) {
            if (comma) {
                output.write(", ");
            }
            DecompilerHelpers.writeOperand(output, parameter);
            if (parameter.getType() != null) {
                output.writeDelimiter(":");
                DecompilerHelpers.writeType(output, parameter.getType(), NameSyntax.SHORT_TYPE_NAME);
            }
            comma = true;
        }
        output.write(") -> ");
        if (this._body != null) {
            final List<Node> body = this._body.getBody();
            if (body.size() == 1 && body.get(0) instanceof Expression) {
                body.get(0).writeTo(output);
            }
            else {
                output.writeLine("{");
                output.indent();
                this._body.writeTo(output);
                output.unindent();
                output.write("}");
            }
        }
        else {
            output.write("{}");
        }
    }
}
