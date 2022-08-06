package com.example.Interface;

import java.lang.annotation.*;

@Target ({ElementType.FIELD})
@Retention (RetentionPolicy.RUNTIME)
@Documented
public @interface NotNull {
    boolean value() default true;
}
