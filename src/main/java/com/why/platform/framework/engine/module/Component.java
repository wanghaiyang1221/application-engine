package com.why.platform.framework.engine.module;

import com.why.platform.framework.engine.spring.EngineApplicationContext;

public abstract class Component implements Module {
	
	protected EngineApplicationContext context;
	
	public Component(EngineApplicationContext context) {
		this.context = context;
	}
	
	public Type getType() {
		return Type.component;
	}

	public void init() {
		context.appendConfigLocations(getName());
	}

}
