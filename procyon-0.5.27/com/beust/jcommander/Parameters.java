package com.beust.jcommander;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface Parameters {
    public static final String DEFAULT_OPTION_PREFIXES = "-";
    
    String resourceBundle() default "";
    
    String separators() default " ";
    
    String optionPrefixes() default "-";
    
    String commandDescription() default "";
    
    String commandDescriptionKey() default "";
    
    String[] commandNames() default {};
}
