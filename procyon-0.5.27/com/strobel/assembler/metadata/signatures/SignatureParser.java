package com.strobel.assembler.metadata.signatures;

import java.lang.reflect.*;
import java.util.*;

public final class SignatureParser
{
    private static final boolean DEBUG;
    private static final TypeArgument[] EMPTY_TYPE_ARGUMENTS;
    private static final char EOI = ':';
    private char[] input;
    private int index;
    
    static {
        DEBUG = Boolean.getBoolean("DEBUG");
        EMPTY_TYPE_ARGUMENTS = new TypeArgument[0];
    }
    
    private SignatureParser() {
        super();
        this.index = 0;
    }
    
    public static SignatureParser make() {
        return new SignatureParser();
    }
    
    private char current() {
        assert this.index <= this.input.length;
        try {
            return this.input[this.index];
        }
        catch (ArrayIndexOutOfBoundsException e) {
            return ':';
        }
    }
    
    private void advance() {
        assert this.index <= this.input.length;
        ++this.index;
    }
    
    private Error error(final String errorMsg) {
        if (SignatureParser.DEBUG) {
            System.out.println("Parse error:" + errorMsg);
        }
        return new GenericSignatureFormatError();
    }
    
    public ClassSignature parseClassSignature(final String s) {
        if (SignatureParser.DEBUG) {
            System.out.println("Parsing class sig:" + s);
        }
        this.input = s.toCharArray();
        this.index = 0;
        return this.parseClassSignature();
    }
    
    public MethodTypeSignature parseMethodSignature(final String s) {
        if (SignatureParser.DEBUG) {
            System.out.println("Parsing method sig:" + s);
        }
        this.input = s.toCharArray();
        this.index = 0;
        return this.parseMethodTypeSignature();
    }
    
    public TypeSignature parseTypeSignature(final String s) {
        if (SignatureParser.DEBUG) {
            System.out.println("Parsing type sig:" + s);
        }
        this.input = s.toCharArray();
        this.index = 0;
        return this.parseTypeSignature();
    }
    
    private ClassSignature parseClassSignature() {
        assert this.index == 0;
        return ClassSignature.make(this.parseZeroOrMoreFormalTypeParameters(), this.parseClassTypeSignature(), this.parseSuperInterfaces());
    }
    
    private FormalTypeParameter[] parseZeroOrMoreFormalTypeParameters() {
        if (this.current() == '<') {
            return this.parseFormalTypeParameters();
        }
        return new FormalTypeParameter[0];
    }
    
    private FormalTypeParameter[] parseFormalTypeParameters() {
        final Collection<FormalTypeParameter> ftps = new ArrayList<FormalTypeParameter>(3);
        assert this.current() == '<';
        if (this.current() != '<') {
            throw this.error("expected <");
        }
        this.advance();
        ftps.add(this.parseFormalTypeParameter());
        while (this.current() != '>') {
            ftps.add(this.parseFormalTypeParameter());
        }
        this.advance();
        final FormalTypeParameter[] formalTypeParameters = new FormalTypeParameter[ftps.size()];
        return ftps.toArray(formalTypeParameters);
    }
    
    private FormalTypeParameter parseFormalTypeParameter() {
        return FormalTypeParameter.make(this.parseIdentifier(), this.parseZeroOrMoreBounds());
    }
    
    private String parseIdentifier() {
        final StringBuilder result = new StringBuilder();
        while (!Character.isWhitespace(this.current())) {
            final char c = this.current();
            switch (c) {
                case '.':
                case '/':
                case ':':
                case ';':
                case '<':
                case '>': {
                    return result.toString();
                }
                default: {
                    result.append(c);
                    this.advance();
                    continue;
                }
            }
        }
        return result.toString();
    }
    
    private FieldTypeSignature parseFieldTypeSignature() {
        switch (this.current()) {
            case 'L': {
                return this.parseClassTypeSignature();
            }
            case 'T': {
                return this.parseTypeVariableSignature();
            }
            case '[': {
                return this.parseArrayTypeSignature();
            }
            default: {
                throw this.error("Expected Field Type Signature");
            }
        }
    }
    
    private ClassTypeSignature parseClassTypeSignature() {
        assert this.current() == 'L';
        if (this.current() != 'L') {
            throw this.error("expected a class type");
        }
        this.advance();
        final List<SimpleClassTypeSignature> typeSignatures = new ArrayList<SimpleClassTypeSignature>(5);
        typeSignatures.add(this.parseSimpleClassTypeSignature(false));
        this.parseClassTypeSignatureSuffix(typeSignatures);
        if (this.current() != ';') {
            throw this.error("expected ';' got '" + this.current() + "'");
        }
        this.advance();
        return ClassTypeSignature.make(typeSignatures);
    }
    
    private SimpleClassTypeSignature parseSimpleClassTypeSignature(final boolean dollar) {
        final String id = this.parseIdentifier();
        final int position = this.index;
        final char c = this.current();
        switch (c) {
            case '$':
            case '.':
            case '/':
            case ';': {
                return SimpleClassTypeSignature.make(id, dollar, new TypeArgument[0]);
            }
            case '<': {
                return SimpleClassTypeSignature.make(id, dollar, this.parseTypeArguments());
            }
            default: {
                throw this.error(String.valueOf(position) + ": expected < or ; or /");
            }
        }
    }
    
    private void parseClassTypeSignatureSuffix(final List<SimpleClassTypeSignature> typeSignatures) {
        while (this.current() == '/' || this.current() == '.') {
            final boolean dollar = this.current() == '.';
            this.advance();
            typeSignatures.add(this.parseSimpleClassTypeSignature(dollar));
        }
    }
    
    private TypeArgument[] parseTypeArguments() {
        final Collection<TypeArgument> tas = new ArrayList<TypeArgument>(3);
        assert this.current() == '<';
        if (this.current() != '<') {
            throw this.error("expected <");
        }
        this.advance();
        tas.add(this.parseTypeArgument());
        while (this.current() != '>') {
            tas.add(this.parseTypeArgument());
        }
        this.advance();
        final TypeArgument[] taa = new TypeArgument[tas.size()];
        return tas.toArray(taa);
    }
    
    private TypeArgument parseTypeArgument() {
        final char c = this.current();
        switch (c) {
            case '+': {
                this.advance();
                return Wildcard.make(BottomSignature.make(), this.parseFieldTypeSignature());
            }
            case '*': {
                this.advance();
                return Wildcard.make(BottomSignature.make(), SimpleClassTypeSignature.make("java.lang.Object", false, SignatureParser.EMPTY_TYPE_ARGUMENTS));
            }
            case '-': {
                this.advance();
                return Wildcard.make(this.parseFieldTypeSignature(), SimpleClassTypeSignature.make("java.lang.Object", false, SignatureParser.EMPTY_TYPE_ARGUMENTS));
            }
            default: {
                return this.parseFieldTypeSignature();
            }
        }
    }
    
    private TypeVariableSignature parseTypeVariableSignature() {
        assert this.current() == 'T';
        if (this.current() != 'T') {
            throw this.error("expected a type variable usage");
        }
        this.advance();
        final TypeVariableSignature ts = TypeVariableSignature.make(this.parseIdentifier());
        if (this.current() != ';') {
            throw this.error("; expected in signature of type variable named" + ts.getName());
        }
        this.advance();
        return ts;
    }
    
    private ArrayTypeSignature parseArrayTypeSignature() {
        if (this.current() != '[') {
            throw this.error("expected array type signature");
        }
        this.advance();
        return ArrayTypeSignature.make(this.parseTypeSignature());
    }
    
    private TypeSignature parseTypeSignature() {
        switch (this.current()) {
            case 'B':
            case 'C':
            case 'D':
            case 'F':
            case 'I':
            case 'J':
            case 'S':
            case 'V':
            case 'Z': {
                return this.parseBaseType();
            }
            default: {
                return this.parseFieldTypeSignature();
            }
        }
    }
    
    private BaseType parseBaseType() {
        switch (this.current()) {
            case 'B': {
                this.advance();
                return ByteSignature.make();
            }
            case 'C': {
                this.advance();
                return CharSignature.make();
            }
            case 'D': {
                this.advance();
                return DoubleSignature.make();
            }
            case 'F': {
                this.advance();
                return FloatSignature.make();
            }
            case 'I': {
                this.advance();
                return IntSignature.make();
            }
            case 'J': {
                this.advance();
                return LongSignature.make();
            }
            case 'S': {
                this.advance();
                return ShortSignature.make();
            }
            case 'Z': {
                this.advance();
                return BooleanSignature.make();
            }
            case 'V': {
                this.advance();
                return VoidSignature.make();
            }
            default: {
                throw this.error("expected primitive type");
            }
        }
    }
    
    private FieldTypeSignature[] parseZeroOrMoreBounds() {
        final List<FieldTypeSignature> fts = new ArrayList<FieldTypeSignature>(3);
        if (this.current() == ':') {
            this.advance();
            switch (this.current()) {
                case ':': {
                    fts.add(BottomSignature.make());
                    break;
                }
                default: {
                    fts.add(this.parseFieldTypeSignature());
                    break;
                }
            }
            while (this.current() == ':') {
                this.advance();
                fts.add(this.parseFieldTypeSignature());
            }
        }
        return fts.toArray(new FieldTypeSignature[fts.size()]);
    }
    
    private ClassTypeSignature[] parseSuperInterfaces() {
        final Collection<ClassTypeSignature> cts = new ArrayList<ClassTypeSignature>(5);
        while (this.current() == 'L') {
            cts.add(this.parseClassTypeSignature());
        }
        final ClassTypeSignature[] cta = new ClassTypeSignature[cts.size()];
        return cts.toArray(cta);
    }
    
    private MethodTypeSignature parseMethodTypeSignature() {
        assert this.index == 0;
        return MethodTypeSignature.make(this.parseZeroOrMoreFormalTypeParameters(), this.parseFormalParameters(), this.parseReturnType(), this.parseZeroOrMoreThrowsSignatures());
    }
    
    private TypeSignature[] parseFormalParameters() {
        if (this.current() != '(') {
            throw this.error("expected (");
        }
        this.advance();
        final TypeSignature[] pts = this.parseZeroOrMoreTypeSignatures();
        if (this.current() != ')') {
            throw this.error("expected )");
        }
        this.advance();
        return pts;
    }
    
    private TypeSignature[] parseZeroOrMoreTypeSignatures() {
        final Collection<TypeSignature> ts = new ArrayList<TypeSignature>();
        boolean stop = false;
        while (!stop) {
            switch (this.current()) {
                case 'B':
                case 'C':
                case 'D':
                case 'F':
                case 'I':
                case 'J':
                case 'L':
                case 'S':
                case 'T':
                case 'Z':
                case '[': {
                    ts.add(this.parseTypeSignature());
                    continue;
                }
                default: {
                    stop = true;
                    continue;
                }
            }
        }
        final TypeSignature[] ta = new TypeSignature[ts.size()];
        return ts.toArray(ta);
    }
    
    private ReturnType parseReturnType() {
        if (this.current() == 'V') {
            this.advance();
            return VoidSignature.make();
        }
        return this.parseTypeSignature();
    }
    
    private FieldTypeSignature[] parseZeroOrMoreThrowsSignatures() {
        final Collection<FieldTypeSignature> ets = new ArrayList<FieldTypeSignature>(3);
        while (this.current() == '^') {
            ets.add(this.parseThrowsSignature());
        }
        final FieldTypeSignature[] eta = new FieldTypeSignature[ets.size()];
        return ets.toArray(eta);
    }
    
    private FieldTypeSignature parseThrowsSignature() {
        assert this.current() == '^';
        if (this.current() != '^') {
            throw this.error("expected throws signature");
        }
        this.advance();
        return this.parseFieldTypeSignature();
    }
}
