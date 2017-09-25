package com.strobel.assembler.metadata.signatures;

import com.strobel.assembler.metadata.*;
import java.util.*;

public final class Reifier implements TypeTreeVisitor<TypeReference>
{
    private final MetadataFactory factory;
    private TypeReference resultType;
    
    private Reifier(final MetadataFactory f) {
        super();
        this.factory = f;
    }
    
    public static Reifier make(final MetadataFactory f) {
        return new Reifier(f);
    }
    
    private MetadataFactory getFactory() {
        return this.factory;
    }
    
    private TypeReference[] reifyTypeArguments(final TypeArgument[] tas) {
        final TypeReference[] ts = new TypeReference[tas.length];
        for (int i = 0; i < tas.length; ++i) {
            tas[i].accept(this);
            ts[i] = this.resultType;
            if (ts[i] == null) {
                System.err.println("BAD TYPE ARGUMENTS: " + Arrays.toString(tas) + "; " + Arrays.toString(ts));
            }
            assert ts[i] != null;
        }
        return ts;
    }
    
    @Override
    public TypeReference getResult() {
        assert this.resultType != null;
        return this.resultType;
    }
    
    @Override
    public void visitFormalTypeParameter(final FormalTypeParameter ftp) {
        final FieldTypeSignature[] bounds = ftp.getBounds();
        this.resultType = this.getFactory().makeTypeVariable(ftp.getName(), bounds);
    }
    
    @Override
    public void visitClassTypeSignature(final ClassTypeSignature ct) {
        final List<SimpleClassTypeSignature> scts = ct.getPath();
        assert !scts.isEmpty();
        final Iterator<SimpleClassTypeSignature> iter = scts.iterator();
        SimpleClassTypeSignature sc = iter.next();
        final StringBuilder n = new StringBuilder(sc.getName());
        while (iter.hasNext() && sc.getTypeArguments().length == 0) {
            sc = iter.next();
            final boolean dollar = sc.useDollar();
            n.append(dollar ? "$" : ".").append(sc.getName());
        }
        assert sc.getTypeArguments().length > 0;
        TypeReference c = this.getFactory().makeNamedType(n.toString());
        if (sc.getTypeArguments().length == 0) {
            assert !iter.hasNext();
            this.resultType = c;
        }
        else {
            assert sc.getTypeArguments().length > 0;
            TypeReference[] pts = this.reifyTypeArguments(sc.getTypeArguments());
            TypeReference owner = this.getFactory().makeParameterizedType(c, null, pts);
            while (iter.hasNext()) {
                sc = iter.next();
                final boolean dollar = sc.useDollar();
                n.append(dollar ? "$" : ".").append(sc.getName());
                c = this.getFactory().makeNamedType(n.toString());
                pts = this.reifyTypeArguments(sc.getTypeArguments());
                owner = this.getFactory().makeParameterizedType(c, owner, pts);
            }
            this.resultType = owner;
        }
    }
    
    @Override
    public void visitArrayTypeSignature(final ArrayTypeSignature a) {
        a.getComponentType().accept(this);
        final TypeReference ct = this.resultType;
        assert ct != null;
        this.resultType = this.getFactory().makeArrayType(ct);
    }
    
    @Override
    public void visitTypeVariableSignature(final TypeVariableSignature tv) {
        this.resultType = this.getFactory().findTypeVariable(tv.getName());
    }
    
    @Override
    public void visitWildcard(final Wildcard w) {
        this.resultType = this.getFactory().makeWildcard(w.getSuperBound(), w.getExtendsBound());
    }
    
    @Override
    public void visitSimpleClassTypeSignature(final SimpleClassTypeSignature sct) {
        this.resultType = this.getFactory().makeNamedType(sct.getName());
    }
    
    @Override
    public void visitBottomSignature(final BottomSignature b) {
        this.resultType = null;
    }
    
    @Override
    public void visitByteSignature(final ByteSignature b) {
        this.resultType = this.getFactory().makeByte();
    }
    
    @Override
    public void visitBooleanSignature(final BooleanSignature b) {
        this.resultType = this.getFactory().makeBoolean();
    }
    
    @Override
    public void visitShortSignature(final ShortSignature s) {
        this.resultType = this.getFactory().makeShort();
    }
    
    @Override
    public void visitCharSignature(final CharSignature c) {
        this.resultType = this.getFactory().makeChar();
    }
    
    @Override
    public void visitIntSignature(final IntSignature i) {
        this.resultType = this.getFactory().makeInt();
    }
    
    @Override
    public void visitLongSignature(final LongSignature l) {
        this.resultType = this.getFactory().makeLong();
    }
    
    @Override
    public void visitFloatSignature(final FloatSignature f) {
        this.resultType = this.getFactory().makeFloat();
    }
    
    @Override
    public void visitDoubleSignature(final DoubleSignature d) {
        this.resultType = this.getFactory().makeDouble();
    }
    
    @Override
    public void visitVoidSignature(final VoidSignature v) {
        this.resultType = this.getFactory().makeVoid();
    }
}
