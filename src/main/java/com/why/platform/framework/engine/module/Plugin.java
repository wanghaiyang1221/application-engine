package com.why.platform.framework.engine.module;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import org.springframework.beans.factory.support.ServiceBeanFactoryPostProcessor;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.context.support.XmlWebApplicationContext;

import com.why.platform.common.utils.servlet3.WebApplicationInitializer;
import com.why.platform.framework.spring.ext.BeanLocator;
import com.why.platform.framework.spring.ext.DispatcherServletRegistry;

public abstract class Plugin extends XmlWebApplicationContext implements Module, WebApplicationInitializer {
	
	public static final String PLUGIN_WEB_PRRFIX = "";
	protected ServletRegistration dispatcher;

	public void setDispatcher(ServletRegistration dispatcher) {
		this.dispatcher = dispatcher;
	}

	public ServletRegistration getDispatcher() {
		return dispatcher;
	}

	public void onStartup(ServletContext servletContext) throws ServletException {
		ConfigurableApplicationContext applicationContext = BeanLocator.getContext();
		setParent(applicationContext);
		setClassLoader(applicationContext.getClassLoader());
		setConfigLocation(String.format("classpath*:META-INF/%s/dispatcher.context.xml", getName()));
		setServletContext(servletContext);
		addBeanFactoryPostProcessor(new ServiceBeanFactoryPostProcessor());
		refresh();
	}
	
	public Plugin() {
		
	}

	public Type getType() {
		return Type.plugin;
	}

	public void init() {
		String path = PLUGIN_WEB_PRRFIX + "/" + getName() + "/*";
		dispatcher = DispatcherServletRegistry.register(getName(), path, this);
	}
}
