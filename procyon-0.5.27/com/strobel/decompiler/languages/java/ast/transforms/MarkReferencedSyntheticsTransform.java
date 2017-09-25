package com.strobel.decompiler.languages.java.ast.transforms;

import com.strobel.decompiler.*;
import com.strobel.decompiler.languages.java.ast.*;
import com.strobel.assembler.metadata.*;

public class MarkReferencedSyntheticsTransform extends ContextTrackingVisitor<Void>
{
    public MarkReferencedSyntheticsTransform(final DecompilerContext context) {
        super(context);
    }
    
    @Override
    public Void visitMemberReferenceExpression(final MemberReferenceExpression node, final Void data) {
        super.visitMemberReferenceExpression(node, data);
        if (this.isCurrentMemberVisible()) {
            MemberReference member = node.getUserData(Keys.MEMBER_REFERENCE);
            if (member == null && node.getParent() != null) {
                member = node.getParent().getUserData(Keys.MEMBER_REFERENCE);
            }
            if (member != null) {
                IMemberDefinition resolvedMember;
                if (member instanceof FieldReference) {
                    resolvedMember = ((FieldReference)member).resolve();
                }
                else {
                    resolvedMember = ((MethodReference)member).resolve();
                }
                if (resolvedMember != null && resolvedMember.isSynthetic() && !Flags.testAny(resolvedMember.getFlags(), 2147483648L)) {
                    this.context.getForcedVisibleMembers().add(resolvedMember);
                }
            }
        }
        return null;
    }
    
    private boolean isCurrentMemberVisible() {
        final MethodDefinition currentMethod = this.context.getCurrentMethod();
        if (currentMethod != null && AstBuilder.isMemberHidden(currentMethod, this.context)) {
            return false;
        }
        final TypeDefinition currentType = this.context.getCurrentType();
        return currentType == null || !AstBuilder.isMemberHidden(currentType, this.context);
    }
}
