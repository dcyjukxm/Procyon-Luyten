package com.strobel.decompiler.languages.java.ast.transforms;

import com.strobel.decompiler.*;
import com.strobel.core.*;
import com.strobel.decompiler.languages.java.ast.*;
import com.strobel.assembler.metadata.*;
import java.util.*;

public final class InsertOverrideAnnotationsTransform extends ContextTrackingVisitor<Void>
{
    private static final String OVERRIDE_ANNOTATION_NAME = "java/lang/Override";
    private final AstBuilder _astBuilder;
    
    public InsertOverrideAnnotationsTransform(final DecompilerContext context) {
        super(context);
        this._astBuilder = context.getUserData(Keys.AST_BUILDER);
    }
    
    @Override
    public Void visitMethodDeclaration(final MethodDeclaration node, final Void _) {
        this.tryAddOverrideAnnotation(node);
        return super.visitMethodDeclaration(node, _);
    }
    
    private void tryAddOverrideAnnotation(final MethodDeclaration node) {
        boolean foundOverride = false;
        for (final Annotation annotation : node.getAnnotations()) {
            final TypeReference annotationType = annotation.getType().getUserData(Keys.TYPE_REFERENCE);
            if (StringUtilities.equals(annotationType.getInternalName(), "java/lang/Override")) {
                foundOverride = true;
                break;
            }
        }
        if (foundOverride) {
            return;
        }
        final MethodDefinition method = node.getUserData(Keys.METHOD_DEFINITION);
        if (method.isStatic() || method.isConstructor() || method.isTypeInitializer()) {
            return;
        }
        final TypeDefinition declaringType = method.getDeclaringType();
        if (declaringType.getCompilerMajorVersion() < CompilerTarget.JDK1_6.majorVersion) {
            return;
        }
        final TypeReference annotationType = new MetadataParser(declaringType).parseTypeDescriptor("java/lang/Override");
        final List<MethodReference> candidates = MetadataHelper.findMethods(declaringType, new Predicate<MethodReference>() {
            @Override
            public boolean test(final MethodReference reference) {
                return StringUtilities.equals(reference.getName(), method.getName());
            }
        }, false, true);
        for (final MethodReference candidate : candidates) {
            if (MetadataHelper.isOverride(method, candidate)) {
                final Annotation annotation2 = new Annotation();
                if (this._astBuilder != null) {
                    annotation2.setType(this._astBuilder.convertType(annotationType));
                }
                else {
                    annotation2.setType(new SimpleType(annotationType.getSimpleName()));
                }
                node.getAnnotations().add(annotation2);
                break;
            }
        }
    }
}
