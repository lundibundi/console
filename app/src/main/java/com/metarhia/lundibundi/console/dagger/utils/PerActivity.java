package com.metarhia.lundibundi.console.dagger.utils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Scope;

/**
 * Created by lundibundi on 7/22/16.
 */
@Scope
@Retention(RetentionPolicy.RUNTIME)
public @interface PerActivity { }
