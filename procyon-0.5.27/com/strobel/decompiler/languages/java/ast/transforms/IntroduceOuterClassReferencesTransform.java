package com.strobel.decompiler.languages.java.ast.transforms;

import com.strobel.decompiler.*;
import java.util.*;
import com.strobel.decompiler.ast.*;
import com.strobel.assembler.metadata.*;
import com.strobel.core.*;
import com.strobel.decompiler.languages.java.ast.*;

public class IntroduceOuterClassReferencesTransform extends ContextTrackingVisitor<Void>
{
    private final List<AstNode> _nodesToRemove;
    private final Set<String> _outerClassFields;
    private final Set<ParameterReference> _parametersToRemove;
    
    public IntroduceOuterClassReferencesTransform(final DecompilerContext context) {
        super(context);
        this._nodesToRemove = new ArrayList<AstNode>();
        this._parametersToRemove = new HashSet<ParameterReference>();
        this._outerClassFields = new HashSet<String>();
    }
    
    @Override
    public void run(final AstNode compilationUnit) {
        new PhaseOneVisitor((PhaseOneVisitor)null).run(compilationUnit);
        super.run(compilationUnit);
        for (final AstNode node : this._nodesToRemove) {
            node.remove();
        }
    }
    
    @Override
    public Void visitInvocationExpression(final InvocationExpression node, final Void data) {
        super.visitInvocationExpression(node, data);
        final Expression target = node.getTarget();
        final AstNodeCollection<Expression> arguments = node.getArguments();
        if (target instanceof MemberReferenceExpression && arguments.size() == 1) {
            final MemberReferenceExpression memberReference = (MemberReferenceExpression)target;
            MemberReference reference = memberReference.getUserData(Keys.MEMBER_REFERENCE);
            if (reference == null) {
                reference = node.getUserData(Keys.MEMBER_REFERENCE);
            }
            if (reference instanceof MethodReference) {
                final MethodReference method = (MethodReference)reference;
                if (method.isConstructor()) {
                    final MethodDefinition resolvedMethod = method.resolve();
                    if (resolvedMethod != null) {
                        final TypeDefinition declaringType = resolvedMethod.getDeclaringType();
                        if (declaringType.isInnerClass() || declaringType.isLocalClass()) {
                            for (final ParameterDefinition p : resolvedMethod.getParameters()) {
                                if (this._parametersToRemove.contains(p)) {
                                    final int parameterIndex = p.getPosition();
                                    final Expression argumentToRemove = CollectionUtilities.getOrDefault(arguments, parameterIndex);
                                    if (argumentToRemove == null) {
                                        continue;
                                    }
                                    this._nodesToRemove.add(argumentToRemove);
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
    
    @Override
    public Void visitMemberReferenceExpression(final MemberReferenceExpression node, final Void data) {
        this.tryIntroduceOuterClassReference(node, node.getTarget() instanceof ThisReferenceExpression);
        return super.visitMemberReferenceExpression(node, data);
    }
    
    private boolean tryIntroduceOuterClassReference(final MemberReferenceExpression node, final boolean hasThisOnLeft) {
        final TypeDefinition currentType = this.context.getCurrentType();
        if (!currentType.isInnerClass()) {
            return false;
        }
        final MemberReference reference = node.getUserData(Keys.MEMBER_REFERENCE);
        FieldReference field;
        FieldDefinition resolvedField;
        if (reference instanceof FieldReference) {
            field = (FieldReference)reference;
            resolvedField = field.resolve();
        }
        else {
            field = null;
            resolvedField = null;
        }
        if (resolvedField != null && !this._outerClassFields.contains(resolvedField.getFullName())) {
            return false;
        }
        if (!hasThisOnLeft || currentType.isStatic() || (node.getParent() instanceof AssignmentExpression && node.getRole() == AssignmentExpression.LEFT_ROLE) || resolvedField == null || !resolvedField.isSynthetic()) {
            return this.tryInsertOuterClassReference(node, reference);
        }
        if (node.getParent() instanceof MemberReferenceExpression && this.tryIntroduceOuterClassReference((MemberReferenceExpression)node.getParent(), hasThisOnLeft)) {
            return true;
        }
        final TypeReference outerTypeReference = field.getFieldType();
        final TypeDefinition resolvedOuterType = outerTypeReference.resolve();
        SimpleType outerType;
        if (resolvedOuterType != null && resolvedOuterType.isAnonymous()) {
            if (resolvedOuterType.getExplicitInterfaces().isEmpty()) {
                outerType = new SimpleType(resolvedOuterType.getBaseType().getSimpleName());
                outerType.putUserData(Keys.ANONYMOUS_BASE_TYPE_REFERENCE, resolvedOuterType.getBaseType());
            }
            else {
                outerType = new SimpleType(resolvedOuterType.getExplicitInterfaces().get(0).getSimpleName());
                outerType.putUserData(Keys.ANONYMOUS_BASE_TYPE_REFERENCE, resolvedOuterType.getExplicitInterfaces().get(0));
            }
        }
        else if (resolvedOuterType != null) {
            outerType = new SimpleType(resolvedOuterType.getSimpleName());
        }
        else {
            outerType = new SimpleType(outerTypeReference.getSimpleName());
        }
        outerType.putUserData(Keys.TYPE_REFERENCE, outerTypeReference);
        final ThisReferenceExpression replacement = new ThisReferenceExpression(node.getOffset());
        replacement.setTarget(new TypeReferenceExpression(node.getOffset(), outerType));
        replacement.putUserData(Keys.TYPE_REFERENCE, outerTypeReference);
        node.replaceWith(replacement);
        return true;
    }
    
    @Override
    public Void visitIdentifierExpression(final IdentifierExpression node, final Void data) {
        final Variable variable = node.getUserData(Keys.VARIABLE);
        if (variable != null && variable.isParameter() && this._parametersToRemove.contains(variable.getOriginalParameter())) {
            final TypeReference parameterType = variable.getOriginalParameter().getParameterType();
            if (!MetadataResolver.areEquivalent(this.context.getCurrentType(), parameterType) && this.isContextWithinTypeInstance(parameterType)) {
                final TypeDefinition resolvedType = parameterType.resolve();
                TypeReference declaredType;
                if (resolvedType != null && resolvedType.isAnonymous()) {
                    if (resolvedType.getExplicitInterfaces().isEmpty()) {
                        declaredType = resolvedType.getBaseType();
                    }
                    else {
                        declaredType = resolvedType.getExplicitInterfaces().get(0);
                    }
                }
                else {
                    declaredType = parameterType;
                }
                final SimpleType outerType = new SimpleType(declaredType.getSimpleName());
                outerType.putUserData(Keys.TYPE_REFERENCE, declaredType);
                final ThisReferenceExpression thisReference = new ThisReferenceExpression(node.getOffset());
                thisReference.setTarget(new TypeReferenceExpression(node.getOffset(), outerType));
                node.replaceWith(thisReference);
                return null;
            }
        }
        return super.visitIdentifierExpression(node, data);
    }
    
    private boolean tryInsertOuterClassReference(final MemberReferenceExpression node, final MemberReference reference) {
        if (node == null || reference == null) {
            return false;
        }
        if (!(node.getTarget() instanceof ThisReferenceExpression)) {
            return false;
        }
        if (!node.getChildByRole(Roles.TARGET_EXPRESSION).isNull()) {
            return false;
        }
        final TypeReference declaringType = reference.getDeclaringType();
        if (MetadataResolver.areEquivalent(this.context.getCurrentType(), declaringType) || !this.isContextWithinTypeInstance(declaringType)) {
            return false;
        }
        final TypeDefinition resolvedType = declaringType.resolve();
        TypeReference declaredType;
        if (resolvedType != null && resolvedType.isAnonymous()) {
            if (resolvedType.getExplicitInterfaces().isEmpty()) {
                declaredType = resolvedType.getBaseType();
            }
            else {
                declaredType = resolvedType.getExplicitInterfaces().get(0);
            }
        }
        else {
            declaredType = declaringType;
        }
        final SimpleType outerType = new SimpleType(declaredType.getSimpleName());
        outerType.putUserData(Keys.TYPE_REFERENCE, declaredType);
        if (node.getTarget() instanceof ThisReferenceExpression) {
            final ThisReferenceExpression thisReference = (ThisReferenceExpression)node.getTarget();
            thisReference.setTarget(new TypeReferenceExpression(node.getOffset(), outerType));
        }
        return true;
    }
    
    private boolean isContextWithinTypeInstance(final TypeReference type) {
        final MethodReference method = this.context.getCurrentMethod();
        if (method != null) {
            final MethodDefinition resolvedMethod = method.resolve();
            if (resolvedMethod != null && resolvedMethod.isStatic()) {
                return false;
            }
        }
        TypeReference current;
        for (TypeReference scope = current = this.context.getCurrentType(); current != null; current = current.getDeclaringType()) {
            if (MetadataResolver.areEquivalent(current, type)) {
                return true;
            }
            final TypeDefinition resolved = current.resolve();
            if (resolved != null && resolved.isLocalClass()) {
                final MethodReference declaringMethod = resolved.getDeclaringMethod();
                if (declaringMethod != null) {
                    final MethodDefinition resolvedDeclaringMethod = declaringMethod.resolve();
                    if (resolvedDeclaringMethod != null && resolvedDeclaringMethod.isStatic()) {
                        break;
                    }
                }
            }
        }
        return false;
    }
    
    static /* synthetic */ DecompilerContext access$3(final IntroduceOuterClassReferencesTransform param_0) {
        return param_0.context;
    }
    
    static /* synthetic */ Set access$4(final IntroduceOuterClassReferencesTransform param_0) {
        return param_0._outerClassFields;
    }
    
    static /* synthetic */ Set access$5(final IntroduceOuterClassReferencesTransform param_0) {
        return param_0._parametersToRemove;
    }
    
    static /* synthetic */ List access$6(final IntroduceOuterClassReferencesTransform param_0) {
        return param_0._nodesToRemove;
    }
    
    private class PhaseOneVisitor extends ContextTrackingVisitor<Void>
    {
        private PhaseOneVisitor() {
            super(IntroduceOuterClassReferencesTransform.access$3(IntroduceOuterClassReferencesTransform.this));
        }
        
        @Override
        public Void visitAssignmentExpression(final AssignmentExpression node, final Void _) {
            super.visitAssignmentExpression(node, _);
            final TypeDefinition currentType = this.context.getCurrentType();
            if (this.context.getSettings().getShowSyntheticMembers() || this.context.getCurrentMethod() == null || !this.context.getCurrentMethod().isConstructor() || (!currentType.isInnerClass() && !currentType.isLocalClass())) {
                return null;
            }
            final Expression left = node.getLeft();
            final Expression right = node.getRight();
            if (left instanceof MemberReferenceExpression && right instanceof IdentifierExpression) {
                final Variable variable = right.getUserData(Keys.VARIABLE);
                if (variable == null || !variable.isParameter()) {
                    return null;
                }
                final MemberReferenceExpression memberReference = (MemberReferenceExpression)left;
                final MemberReference member = memberReference.getUserData(Keys.MEMBER_REFERENCE);
                if (member instanceof FieldReference && memberReference.getTarget() instanceof ThisReferenceExpression) {
                    final FieldDefinition resolvedField = ((FieldReference)member).resolve();
                    if (resolvedField != null && resolvedField.isSynthetic() && MetadataResolver.areEquivalent(resolvedField.getFieldType(), currentType.getDeclaringType())) {
                        final ParameterDefinition parameter = variable.getOriginalParameter();
                        IntroduceOuterClassReferencesTransform.access$4(IntroduceOuterClassReferencesTransform.this).add(resolvedField.getFullName());
                        IntroduceOuterClassReferencesTransform.access$5(IntroduceOuterClassReferencesTransform.this).add(parameter);
                        final ConstructorDeclaration constructorDeclaration = CollectionUtilities.firstOrDefault(node.getAncestorsAndSelf(), Predicates.instanceOf(ConstructorDeclaration.class));
                        if (constructorDeclaration != null && !constructorDeclaration.isNull()) {
                            final ParameterDeclaration parameterToRemove = CollectionUtilities.getOrDefault(constructorDeclaration.getParameters(), parameter.getPosition());
                            if (parameterToRemove != null) {
                                IntroduceOuterClassReferencesTransform.access$6(IntroduceOuterClassReferencesTransform.this).add(parameterToRemove);
                            }
                        }
                        if (node.getParent() instanceof ExpressionStatement) {
                            IntroduceOuterClassReferencesTransform.access$6(IntroduceOuterClassReferencesTransform.this).add(node.getParent());
                        }
                        else {
                            final TypeReference fieldType = resolvedField.getFieldType();
                            final ThisReferenceExpression replacement = new ThisReferenceExpression(left.getOffset());
                            final SimpleType type = new SimpleType(fieldType.getSimpleName());
                            type.putUserData(Keys.TYPE_REFERENCE, fieldType);
                            replacement.putUserData(Keys.TYPE_REFERENCE, fieldType);
                            replacement.setTarget(new TypeReferenceExpression(left.getOffset(), type));
                            right.replaceWith(replacement);
                        }
                    }
                }
            }
            return null;
        }
    }
}
