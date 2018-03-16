package com.why.platform.framework.engine.jetty;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PACKAGE)
@Retention(RetentionPolicy.RUNTIME)
public @interface WebAppRegistry {
	
	@Target(ElementType.ANNOTATION_TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface WebApp {
		String dir();
		String contextPath();
		String[] welcomeFiles() default "index.html";
	}

	WebApp[] value();
}
