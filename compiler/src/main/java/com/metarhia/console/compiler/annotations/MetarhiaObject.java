package com.metarhia.console.compiler.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Created by lundibundi on 8/31/16.
 */
public @interface MetarhiaObject {
    Class[] contracts() default {};

    String hierarchy() default "MetarhiaObject";
}
