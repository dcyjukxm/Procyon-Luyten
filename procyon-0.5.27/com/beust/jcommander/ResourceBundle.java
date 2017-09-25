package com.beust.jcommander;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface ResourceBundle {
    String value();
}
