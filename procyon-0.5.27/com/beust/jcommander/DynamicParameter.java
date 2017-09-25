package com.beust.jcommander;

import java.lang.annotation.*;
import com.beust.jcommander.validators.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface DynamicParameter {
    String[] names() default {};
    
    boolean required() default false;
    
    String description() default "";
    
    String descriptionKey() default "";
    
    boolean hidden() default false;
    
    Class<? extends IParameterValidator> validateWith() default NoValidator.class;
    
    String assignment() default "=";
    
    Class<? extends IValueValidator> validateValueWith() default NoValueValidator.class;
}
