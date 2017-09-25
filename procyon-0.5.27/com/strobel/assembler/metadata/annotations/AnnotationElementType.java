package com.strobel.assembler.metadata.annotations;

public enum AnnotationElementType
{
    Constant("Constant", 0), 
    Enum("Enum", 1), 
    Array("Array", 2), 
    Class("Class", 3), 
    Annotation("Annotation", 4);
    
    public static AnnotationElementType forTag(final char tag) {
        switch (tag) {
            case 'B':
            case 'C':
            case 'D':
            case 'F':
            case 'I':
            case 'J':
            case 'S':
            case 'Z':
            case 's': {
                return AnnotationElementType.Constant;
            }
            case 'e': {
                return AnnotationElementType.Enum;
            }
            case '[': {
                return AnnotationElementType.Array;
            }
            case 'c': {
                return AnnotationElementType.Class;
            }
            case '@': {
                return AnnotationElementType.Annotation;
            }
            default: {
                throw new IllegalArgumentException("Invalid annotation element tag: " + tag);
            }
        }
    }
}
