package com.strobel.decompiler.languages.java.ast;

import com.strobel.decompiler.patterns.*;
import java.util.*;
import com.strobel.util.*;

public class CompilationUnit extends AstNode
{
    public static final Role<AstNode> MEMBER_ROLE;
    public static final Role<ImportDeclaration> IMPORT_ROLE;
    private AstNode _topExpression;
    private String _fileName;
    
    static {
        MEMBER_ROLE = new Role<AstNode>("Member", AstNode.class, AstNode.NULL);
        IMPORT_ROLE = new Role<ImportDeclaration>("Import", ImportDeclaration.class, ImportDeclaration.NULL);
    }
    
    public final AstNodeCollection<ImportDeclaration> getImports() {
        return this.getChildrenByRole(CompilationUnit.IMPORT_ROLE);
    }
    
    public final PackageDeclaration getPackage() {
        return this.getChildByRole(Roles.PACKAGE);
    }
    
    public final void setPackage(final PackageDeclaration value) {
        this.setChildByRole(Roles.PACKAGE, value);
    }
    
    public final String getFileName() {
        return this._fileName;
    }
    
    public final void setFileName(final String fileName) {
        this.verifyNotFrozen();
        this._fileName = fileName;
    }
    
    public final AstNode getTopExpression() {
        return this._topExpression;
    }
    
    final void setTopExpression(final AstNode topExpression) {
        this._topExpression = topExpression;
    }
    
    public final AstNodeCollection<AstNode> getMembers() {
        return this.getChildrenByRole(CompilationUnit.MEMBER_ROLE);
    }
    
    @Override
    public NodeType getNodeType() {
        return NodeType.UNKNOWN;
    }
    
    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return (R)visitor.visitCompilationUnit(this, (Object)data);
    }
    
    @Override
    public boolean matches(final INode other, final Match match) {
        return other instanceof CompilationUnit && !other.isNull() && this.getMembers().matches(((CompilationUnit)other).getMembers(), match);
    }
    
    public Iterable<TypeDeclaration> getTypes() {
        return this.getTypes(false);
    }
    
    public Iterable<TypeDeclaration> getTypes(final boolean includeInnerTypes) {
        return new Iterable<TypeDeclaration>() {
            final /* synthetic */ CompilationUnit this$0;
            
            @Override
            public final Iterator<TypeDeclaration> iterator() {
                return new Iterator<TypeDeclaration>() {
                    final Stack<AstNode> nodeStack = new Stack<AstNode>();
                    TypeDeclaration next = null;
                    
                    {
                        this.nodeStack.push(CompilationUnit$1.access$0(Iterable.this));
                    }
                    
                    private TypeDeclaration selectNext() {
                        if (this.next != null) {
                            return this.next;
                        }
                        while (!this.nodeStack.isEmpty()) {
                            final AstNode current = this.nodeStack.pop();
                            if (current instanceof TypeDeclaration) {
                                this.next = (TypeDeclaration)current;
                                break;
                            }
                            for (final AstNode child : current.getChildren()) {
                                if (!(child instanceof Statement) && !(child instanceof Expression) && (child.getRole() != Roles.TYPE_MEMBER || (child instanceof TypeDeclaration && includeInnerTypes))) {
                                    this.nodeStack.push(child);
                                }
                            }
                        }
                        return null;
                    }
                    
                    @Override
                    public final boolean hasNext() {
                        return this.selectNext() != null;
                    }
                    
                    @Override
                    public final TypeDeclaration next() {
                        final TypeDeclaration next = this.selectNext();
                        if (next == null) {
                            throw new NoSuchElementException();
                        }
                        this.next = null;
                        return next;
                    }
                    
                    @Override
                    public final void remove() {
                        throw ContractUtils.unsupported();
                    }
                };
            }
            
            static /* synthetic */ CompilationUnit access$0(final CompilationUnit$1 param_0) {
                return param_0.this$0;
            }
        };
    }
}
