package com.why.platform.framework.engine.spring;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.core.io.Resource;

public class EngineApplicationContext extends AbstractXmlApplicationContext {

	public EngineApplicationContext(ApplicationContext context) {
		super(context);
		registerShutdownHook();
	}
	
	public EngineApplicationContext(String ...locations) {
		setConfigLocations(locations);
		registerShutdownHook();
	}
	
	private Set<ConfigurableApplicationContext> childrenApplication = new LinkedHashSet<ConfigurableApplicationContext>();
	private Set<Resource> configLocations = new LinkedHashSet<Resource>();
	
	public EngineApplicationContext addChildrenApplication(ConfigurableApplicationContext configurableApplicationContext) {
		childrenApplication.add(configurableApplicationContext);
		configurableApplicationContext.setParent(this);
		return this;
	}
	
	public EngineApplicationContext appendConfigLocations(String location) {
		String[] locations = getConfigLocations();
		setConfigLocations(ArrayUtils.add(locations, location));
		return this;
	}
	
	public EngineApplicationContext appendConfigLocations(Resource resource) {
		configLocations.add(resource);
		return this;
	}

	@Override
	protected Resource[] getConfigResources() {
		return configLocations.toArray(new Resource[0]);
	}
	
}
