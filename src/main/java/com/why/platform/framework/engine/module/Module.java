package com.why.platform.framework.engine.module;

public interface Module {
	Type getType();
	
	String getName();
	
	void init();
	
	enum Type {
		component,
		plugin
	}
}
