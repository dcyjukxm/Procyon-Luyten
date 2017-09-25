package com.strobel.decompiler.languages.java.analysis;

import java.util.*;
import com.strobel.functions.*;
import com.strobel.decompiler.utilities.*;
import com.strobel.assembler.metadata.*;
import com.strobel.decompiler.languages.java.ast.*;
import com.strobel.decompiler.ast.*;

public final class Correlator
{
    public static boolean areCorrelated(final Expression readExpression, final Statement writeStatement) {
        final Set<IMetadataTypeMember> readMembers = new LinkedHashSet<IMetadataTypeMember>();
        final Set<IMetadataTypeMember> writeMembers = new LinkedHashSet<IMetadataTypeMember>();
        collectCorrelations(readExpression, CorrelationMode.Read, readMembers);
        if (readMembers.isEmpty()) {
            return false;
        }
        collectCorrelations(writeStatement, CorrelationMode.Write, writeMembers);
        if (writeMembers.isEmpty()) {
            return false;
        }
        for (final IMetadataTypeMember typeMember : readMembers) {
            if (writeMembers.contains(typeMember)) {
                return true;
            }
        }
        return false;
    }
    
    private static void collectCorrelations(final AstNode node, final CorrelationMode mode, final Collection<IMetadataTypeMember> members) {
        final Iterable<AstNode> traversal = TreeTraversal.postOrder(node, new Function<AstNode, Iterable<AstNode>>() {
            @Override
            public Iterable<AstNode> apply(final AstNode n) {
                return n.getChildren();
            }
        });
        for (final AstNode n : traversal) {
            if (!(n instanceof IdentifierExpression)) {
                continue;
            }
            final IdentifierExpression identifier = (IdentifierExpression)n;
            final UsageType usage = UsageClassifier.getUsageType(identifier);
            if (mode == CorrelationMode.Read) {
                if (usage != UsageType.Read && usage != UsageType.ReadWrite) {
                    continue;
                }
            }
            else if (usage != UsageType.Write && usage != UsageType.ReadWrite) {
                continue;
            }
            IMetadataTypeMember member = identifier.getUserData(Keys.MEMBER_REFERENCE);
            if (member != null) {
                members.add(member);
            }
            else {
                final Variable variable = identifier.getUserData(Keys.VARIABLE);
                if (variable == null) {
                    continue;
                }
                if (variable.isParameter()) {
                    member = variable.getOriginalParameter();
                }
                else if (variable.getOriginalVariable() != null) {
                    member = variable.getOriginalVariable();
                }
                if (member == null) {
                    continue;
                }
                members.add(member);
            }
        }
    }
    
    private enum CorrelationMode
    {
        Read("Read", 0), 
        Write("Write", 1);
    }
}
