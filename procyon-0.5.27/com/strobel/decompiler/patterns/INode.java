package com.strobel.decompiler.patterns;

import com.strobel.functions.*;
import java.util.*;
import com.strobel.util.*;

public interface INode
{
    public static final Function<INode, Iterable<INode>> CHILD_ITERATOR = new Function<INode, Iterable<INode>>() {
        @Override
        public Iterable<INode> apply(INode input) {
            return new Iterable<INode>() {
                @Override
                public final Iterator<INode> iterator() {
                    return new Iterator<INode>(input) {
                        INode next = param_1.getFirstChild();
                        
                        @Override
                        public final boolean hasNext() {
                            return this.next != null;
                        }
                        
                        @Override
                        public final INode next() {
                            INode result;
                            result = this.next;
                            if (result == null) {
                                throw new NoSuchElementException();
                            }
                            this.next = result.getNextSibling();
                            return result;
                        }
                        
                        @Override
                        public final void remove() {
                            throw ContractUtils.unsupported();
                        }
                    };
                }
            };
        }
    };
    
    boolean isNull();
    
    Role getRole();
    
    INode getFirstChild();
    
    INode getNextSibling();
    
    boolean matches(INode param_0, Match param_1);
    
    boolean matchesCollection(Role param_0, INode param_1, Match param_2, BacktrackingInfo param_3);
    
    Match match(INode param_0);
    
    boolean matches(INode param_0);
}
