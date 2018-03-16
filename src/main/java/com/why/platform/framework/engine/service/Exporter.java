package com.why.platform.framework.engine.service;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Exporter {
	String name();
	
	String type() default "rest";
	
	String version() default "SNAPSHOT";
	
	Class<?> serviceInterface();
}
