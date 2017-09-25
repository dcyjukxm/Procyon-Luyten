package com.strobel.assembler.metadata;

import java.util.*;
import com.strobel.assembler.metadata.annotations.*;

public interface IAnnotationsProvider
{
    boolean hasAnnotations();
    
    List<CustomAnnotation> getAnnotations();
}
