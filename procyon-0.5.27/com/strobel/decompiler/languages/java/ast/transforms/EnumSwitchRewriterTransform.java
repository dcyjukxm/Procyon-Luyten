package com.strobel.decompiler.languages.java.ast.transforms;

import com.strobel.decompiler.*;
import com.strobel.core.*;
import java.util.*;
import com.strobel.decompiler.languages.java.ast.*;
import com.strobel.assembler.metadata.*;

public class EnumSwitchRewriterTransform implements IAstTransform
{
    private final DecompilerContext _context;
    
    public EnumSwitchRewriterTransform(final DecompilerContext context) {
        super();
        this._context = VerifyArgument.notNull(context, "context");
    }
    
    @Override
    public void run(final AstNode compilationUnit) {
        compilationUnit.acceptVisitor((IAstVisitor<? super Object, ?>)new Visitor(this._context), (Object)null);
    }
    
    private static final class Visitor extends ContextTrackingVisitor<Void>
    {
        private final Map<String, SwitchMapInfo> _switchMaps;
        private boolean _isSwitchMapWrapper;
        
        protected Visitor(final DecompilerContext context) {
            super(context);
            this._switchMaps = new LinkedHashMap<String, SwitchMapInfo>();
        }
        
        @Override
        public Void visitTypeDeclaration(final TypeDeclaration typeDeclaration, final Void _) {
            final boolean oldIsSwitchMapWrapper = this._isSwitchMapWrapper;
            final TypeDefinition typeDefinition = typeDeclaration.getUserData(Keys.TYPE_DEFINITION);
            final boolean isSwitchMapWrapper = isSwitchMapWrapper(typeDefinition);
            if (isSwitchMapWrapper) {
                final String internalName = typeDefinition.getInternalName();
                SwitchMapInfo info = this._switchMaps.get(internalName);
                if (info == null) {
                    this._switchMaps.put(internalName, info = new SwitchMapInfo(internalName));
                }
                info.enclosingTypeDeclaration = typeDeclaration;
            }
            this._isSwitchMapWrapper = isSwitchMapWrapper;
            try {
                super.visitTypeDeclaration(typeDeclaration, _);
            }
            finally {
                this._isSwitchMapWrapper = oldIsSwitchMapWrapper;
            }
            this._isSwitchMapWrapper = oldIsSwitchMapWrapper;
            this.rewrite();
            return null;
        }
        
        @Override
        public Void visitSwitchStatement(final SwitchStatement node, final Void data) {
            final Expression test = node.getExpression();
            if (test instanceof IndexerExpression) {
                final IndexerExpression indexer = (IndexerExpression)test;
                final Expression array = indexer.getTarget();
                final Expression argument = indexer.getArgument();
                if (!(array instanceof MemberReferenceExpression)) {
                    return super.visitSwitchStatement(node, data);
                }
                final MemberReferenceExpression arrayAccess = (MemberReferenceExpression)array;
                final Expression arrayOwner = arrayAccess.getTarget();
                final String mapName = arrayAccess.getMemberName();
                if (mapName == null || !mapName.startsWith("$SwitchMap$") || !(arrayOwner instanceof TypeReferenceExpression)) {
                    return super.visitSwitchStatement(node, data);
                }
                final TypeReferenceExpression enclosingTypeExpression = (TypeReferenceExpression)arrayOwner;
                final TypeReference enclosingType = enclosingTypeExpression.getType().getUserData(Keys.TYPE_REFERENCE);
                if (!isSwitchMapWrapper(enclosingType) || !(argument instanceof InvocationExpression)) {
                    return super.visitSwitchStatement(node, data);
                }
                final InvocationExpression invocation = (InvocationExpression)argument;
                final Expression invocationTarget = invocation.getTarget();
                if (!(invocationTarget instanceof MemberReferenceExpression)) {
                    return super.visitSwitchStatement(node, data);
                }
                final MemberReferenceExpression memberReference = (MemberReferenceExpression)invocationTarget;
                if (!"ordinal".equals(memberReference.getMemberName())) {
                    return super.visitSwitchStatement(node, data);
                }
                final String enclosingTypeName = enclosingType.getInternalName();
                SwitchMapInfo info = this._switchMaps.get(enclosingTypeName);
                if (info == null) {
                    this._switchMaps.put(enclosingTypeName, info = new SwitchMapInfo(enclosingTypeName));
                    final TypeDefinition resolvedType = enclosingType.resolve();
                    if (resolvedType != null) {
                        AstBuilder astBuilder = this.context.getUserData(Keys.AST_BUILDER);
                        if (astBuilder == null) {
                            astBuilder = new AstBuilder(this.context);
                        }
                        final TypeDeclaration declaration = astBuilder.createType(resolvedType);
                        declaration.acceptVisitor((IAstVisitor<? super Void, ?>)this, data);
                    }
                }
                List<SwitchStatement> switches = info.switches.get(mapName);
                if (switches == null) {
                    info.switches.put(mapName, switches = new ArrayList<SwitchStatement>());
                }
                switches.add(node);
            }
            return super.visitSwitchStatement(node, data);
        }
        
        @Override
        public Void visitAssignmentExpression(final AssignmentExpression node, final Void data) {
            final TypeDefinition currentType = this.context.getCurrentType();
            final MethodDefinition currentMethod = this.context.getCurrentMethod();
            if (this._isSwitchMapWrapper && currentType != null && currentMethod != null && currentMethod.isTypeInitializer()) {
                final Expression left = node.getLeft();
                final Expression right = node.getRight();
                if (left instanceof IndexerExpression && right instanceof PrimitiveExpression) {
                    String mapName = null;
                    final Expression array = ((IndexerExpression)left).getTarget();
                    final Expression argument = ((IndexerExpression)left).getArgument();
                    if (array instanceof MemberReferenceExpression) {
                        mapName = ((MemberReferenceExpression)array).getMemberName();
                    }
                    else if (array instanceof IdentifierExpression) {
                        mapName = ((IdentifierExpression)array).getIdentifier();
                    }
                    if (mapName == null || !mapName.startsWith("$SwitchMap$")) {
                        return super.visitAssignmentExpression(node, data);
                    }
                    if (!(argument instanceof InvocationExpression)) {
                        return super.visitAssignmentExpression(node, data);
                    }
                    final InvocationExpression invocation = (InvocationExpression)argument;
                    final Expression invocationTarget = invocation.getTarget();
                    if (!(invocationTarget instanceof MemberReferenceExpression)) {
                        return super.visitAssignmentExpression(node, data);
                    }
                    final MemberReferenceExpression memberReference = (MemberReferenceExpression)invocationTarget;
                    final Expression memberTarget = memberReference.getTarget();
                    if (!(memberTarget instanceof MemberReferenceExpression) || !"ordinal".equals(memberReference.getMemberName())) {
                        return super.visitAssignmentExpression(node, data);
                    }
                    final MemberReferenceExpression outerMemberReference = (MemberReferenceExpression)memberTarget;
                    final Expression outerMemberTarget = outerMemberReference.getTarget();
                    if (!(outerMemberTarget instanceof TypeReferenceExpression)) {
                        return super.visitAssignmentExpression(node, data);
                    }
                    final String enclosingType = currentType.getInternalName();
                    SwitchMapInfo info = this._switchMaps.get(enclosingType);
                    if (info == null) {
                        this._switchMaps.put(enclosingType, info = new SwitchMapInfo(enclosingType));
                        AstBuilder astBuilder = this.context.getUserData(Keys.AST_BUILDER);
                        if (astBuilder == null) {
                            astBuilder = new AstBuilder(this.context);
                        }
                        info.enclosingTypeDeclaration = astBuilder.createType(currentType);
                    }
                    final PrimitiveExpression value = (PrimitiveExpression)right;
                    assert value.getValue() instanceof Integer;
                    Map<Integer, Expression> mapping = info.mappings.get(mapName);
                    if (mapping == null) {
                        info.mappings.put(mapName, mapping = new LinkedHashMap<Integer, Expression>());
                    }
                    final IdentifierExpression enumValue = new IdentifierExpression(-34, outerMemberReference.getMemberName());
                    enumValue.putUserData(Keys.MEMBER_REFERENCE, (MemberReference)outerMemberReference.getUserData(Keys.MEMBER_REFERENCE));
                    mapping.put(((Number)value.getValue()).intValue(), enumValue);
                }
            }
            return super.visitAssignmentExpression(node, data);
        }
        
        private void rewrite() {
            if (this._switchMaps.isEmpty()) {
                return;
            }
            for (final SwitchMapInfo info : this._switchMaps.values()) {
                this.rewrite(info);
            }
        Label_0169:
            for (final SwitchMapInfo info : this._switchMaps.values()) {
                for (final String mapName : info.switches.keySet()) {
                    final List<SwitchStatement> switches = info.switches.get(mapName);
                    if (switches != null && !switches.isEmpty()) {
                        continue Label_0169;
                    }
                }
                final TypeDeclaration enclosingTypeDeclaration = info.enclosingTypeDeclaration;
                if (enclosingTypeDeclaration != null) {
                    enclosingTypeDeclaration.remove();
                }
            }
        }
        
        private void rewrite(final SwitchMapInfo info) {
            if (info.switches.isEmpty()) {
                return;
            }
            for (final String mapName : info.switches.keySet()) {
                final List<SwitchStatement> switches = info.switches.get(mapName);
                final Map<Integer, Expression> mappings = info.mappings.get(mapName);
                if (switches != null && mappings != null) {
                    for (int i = 0; i < switches.size(); ++i) {
                        if (this.rewriteSwitch(switches.get(i), mappings)) {
                            switches.remove(i--);
                        }
                    }
                }
            }
        }
        
        private boolean rewriteSwitch(final SwitchStatement s, final Map<Integer, Expression> mappings) {
            final Map<Expression, Expression> replacements = new IdentityHashMap<Expression, Expression>();
            for (final SwitchSection section : s.getSwitchSections()) {
                for (final CaseLabel caseLabel : section.getCaseLabels()) {
                    final Expression expression = caseLabel.getExpression();
                    if (expression != null) {
                        if (expression.isNull()) {
                            continue;
                        }
                        if (expression instanceof PrimitiveExpression) {
                            final Object value = ((PrimitiveExpression)expression).getValue();
                            if (value instanceof Integer) {
                                final Expression replacement = mappings.get(value);
                                if (replacement != null) {
                                    replacements.put(expression, replacement);
                                    continue;
                                }
                            }
                        }
                        return false;
                    }
                }
            }
            final IndexerExpression indexer = (IndexerExpression)s.getExpression();
            final InvocationExpression argument = (InvocationExpression)indexer.getArgument();
            final MemberReferenceExpression memberReference = (MemberReferenceExpression)argument.getTarget();
            final Expression newTest = memberReference.getTarget();
            newTest.remove();
            indexer.replaceWith(newTest);
            for (final Map.Entry<Expression, Expression> entry : replacements.entrySet()) {
                entry.getKey().replaceWith(entry.getValue().clone());
            }
            return true;
        }
        
        private static boolean isSwitchMapWrapper(final TypeReference type) {
            if (type == null) {
                return false;
            }
            final TypeDefinition definition = (TypeDefinition)((type instanceof TypeDefinition) ? type : type.resolve());
            if (definition == null || !definition.isSynthetic() || !definition.isInnerClass()) {
                return false;
            }
            for (final FieldDefinition field : definition.getDeclaredFields()) {
                if (field.getName().startsWith("$SwitchMap$") && BuiltinTypes.Integer.makeArrayType().equals(field.getFieldType())) {
                    return true;
                }
            }
            return false;
        }
        
        private static final class SwitchMapInfo
        {
            final String enclosingType;
            final Map<String, List<SwitchStatement>> switches;
            final Map<String, Map<Integer, Expression>> mappings;
            TypeDeclaration enclosingTypeDeclaration;
            
            SwitchMapInfo(final String enclosingType) {
                super();
                this.switches = new LinkedHashMap<String, List<SwitchStatement>>();
                this.mappings = new LinkedHashMap<String, Map<Integer, Expression>>();
                this.enclosingType = enclosingType;
            }
        }
    }
}
