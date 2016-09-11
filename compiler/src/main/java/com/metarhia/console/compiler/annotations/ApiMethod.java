package com.metarhia.console.compiler.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Created by lundibundi on 8/31/16.
 */
@Target(ElementType.METHOD)
public @interface ApiMethod {
    String value() default "";
}
