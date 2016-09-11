package com.metarhia.lundibundi.console.dagger.utils;

import javax.inject.Scope;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by lundibundi on 8/31/16.
 */
@Scope
@Retention(RetentionPolicy.RUNTIME)
public @interface PerActivitySub { }
