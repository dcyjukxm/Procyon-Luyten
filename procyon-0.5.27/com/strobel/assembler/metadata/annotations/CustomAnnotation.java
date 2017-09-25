package com.strobel.assembler.metadata.annotations;

import com.strobel.assembler.metadata.*;
import java.util.*;
import com.strobel.core.*;

public final class CustomAnnotation
{
    private final TypeReference _annotationType;
    private final List<AnnotationParameter> _parameters;
    
    public CustomAnnotation(final TypeReference annotationType, final List<AnnotationParameter> parameters) {
        super();
        this._annotationType = VerifyArgument.notNull(annotationType, "annotationType");
        this._parameters = VerifyArgument.notNull(parameters, "parameters");
    }
    
    public TypeReference getAnnotationType() {
        return this._annotationType;
    }
    
    public boolean hasParameters() {
        return !this._parameters.isEmpty();
    }
    
    public List<AnnotationParameter> getParameters() {
        return this._parameters;
    }
}
