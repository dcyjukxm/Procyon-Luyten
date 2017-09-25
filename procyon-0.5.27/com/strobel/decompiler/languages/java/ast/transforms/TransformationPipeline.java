package com.strobel.decompiler.languages.java.ast.transforms;

import com.strobel.decompiler.*;
import com.strobel.decompiler.languages.java.ast.*;
import com.strobel.core.*;
import java.util.logging.*;

public final class TransformationPipeline
{
    private static final Logger LOG;
    
    static {
        LOG = Logger.getLogger(TransformationPipeline.class.getSimpleName());
    }
    
    public static IAstTransform[] createPipeline(final DecompilerContext context) {
        return new IAstTransform[] { new EnumRewriterTransform(context), new EnumSwitchRewriterTransform(context), new EclipseEnumSwitchRewriterTransform(context), new AssertStatementTransform(context), new RemoveImplicitBoxingTransform(context), new RemoveRedundantCastsTransform(context), new ConvertLoopsTransform(context), new BreakTargetRelocation(context), new LabelCleanupTransform(context), new TryWithResourcesTransform(context), new DeclareVariablesTransform(context), new StringSwitchRewriterTransform(context), new EclipseStringSwitchRewriterTransform(context), new SimplifyAssignmentsTransform(context), new EliminateSyntheticAccessorsTransform(context), new LambdaTransform(context), new RewriteNewArrayLambdas(context), new RewriteLocalClassesTransform(context), new IntroduceOuterClassReferencesTransform(context), new RewriteInnerClassConstructorCalls(context), new RemoveRedundantInitializersTransform(context), new FlattenElseIfStatementsTransform(context), new FlattenSwitchBlocksTransform(context), new IntroduceInitializersTransform(context), new MarkReferencedSyntheticsTransform(context), new RemoveRedundantCastsTransform(context), new InsertNecessaryConversionsTransform(context), new IntroduceStringConcatenationTransform(context), new SimplifyAssignmentsTransform(context), new InlineEscapingAssignmentsTransform(context), new VarArgsTransform(context), new InsertConstantReferencesTransform(context), new SimplifyArithmeticExpressionsTransform(context), new DeclareLocalClassesTransform(context), new InsertOverrideAnnotationsTransform(context), new AddReferenceQualifiersTransform(context), new RemoveHiddenMembersTransform(context), new CollapseImportsTransform(context) };
    }
    
    public static void runTransformationsUntil(final AstNode node, final Predicate<IAstTransform> abortCondition, final DecompilerContext context) {
        if (node == null) {
            return;
        }
        IAstTransform[] loc_1;
        for (int loc_0 = (loc_1 = createPipeline(context)).length, loc_2 = 0; loc_2 < loc_0; ++loc_2) {
            final IAstTransform transform = loc_1[loc_2];
            if (abortCondition != null && abortCondition.test(transform)) {
                return;
            }
            if (TransformationPipeline.LOG.isLoggable(Level.FINE)) {
                TransformationPipeline.LOG.fine("Running Java AST transform: " + transform.getClass().getSimpleName() + "...");
            }
            transform.run(node);
        }
    }
}
