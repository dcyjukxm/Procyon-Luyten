package com.beust.jcommander;

import java.lang.annotation.*;
import com.beust.jcommander.validators.*;
import com.beust.jcommander.converters.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.METHOD })
public @interface Parameter {
    String[] names() default {};
    
    String description() default "";
    
    boolean required() default false;
    
    String descriptionKey() default "";
    
    int arity() default -1;
    
    boolean password() default false;
    
    Class<? extends IStringConverter<?>> converter() default NoConverter.class;
    
    Class<? extends IStringConverter<?>> listConverter() default NoConverter.class;
    
    boolean hidden() default false;
    
    Class<? extends IParameterValidator> validateWith() default NoValidator.class;
    
    Class<? extends IValueValidator> validateValueWith() default NoValueValidator.class;
    
    boolean variableArity() default false;
    
    Class<? extends IParameterSplitter> splitter() default CommaParameterSplitter.class;
    
    boolean echoInput() default false;
    
    boolean help() default false;
}
