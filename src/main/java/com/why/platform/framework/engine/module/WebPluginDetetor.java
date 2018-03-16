package com.why.platform.framework.engine.module;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.HandlesTypes;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.why.platform.common.utils.servlet3.Servlet3ContainerDetector;
import com.why.platform.common.utils.servlet3.WebApplicationInitializer;
import com.why.platform.framework.engine.event.PluginInitEvent;
import com.why.platform.framework.engine.event.PluginsInitEvent;
import com.why.platform.framework.spring.ext.BeanLocator;

@HandlesTypes(Plugin.class)
public class WebPluginDetetor extends Servlet3ContainerDetector {
	private static final Logger logger = LoggerFactory.getLogger(WebPluginDetetor.class);
	
	protected CharEncodingFilterConfig encodingFilterConfig;

	@Override
	protected void beforInit(ServletContext context) {
		Validate.notNull(BeanLocator.getContext(), "the parent applicationContext can not be null");
		BeanLocator.getContext().getBeanFactory().freezeConfiguration();
		encodingFilterConfig = new CharEncodingFilterConfig(context);
	}

	@Override
	protected void afterInit(ServletContext context) {
		PluginsInitEvent pluginsInitEvent = new PluginsInitEvent(context);
		BeanLocator.getContext().publishEvent(pluginsInitEvent);
		context.setAttribute(WebPluginInitLinstener.PLUGINS_INIT_EVENT, pluginsInitEvent);
	}

	@Override
	protected void init(ServletContext context, WebApplicationInitializer initializer) throws ServletException {
		Plugin plugin = (Plugin) initializer;
		logger.info("### try to init plugin: [{}] ###", plugin.getName());
		plugin.onStartup(context);
		plugin.init();
		encodingFilterConfig.addMapping(plugin.getDispatcher());
		logger.info("### plugin:[{}] init finished ###", plugin.getName());
		
		PluginInitEvent pluginInitEvent = new PluginInitEvent(plugin);
		BeanLocator.getContext().publishEvent(pluginInitEvent);
		context.setAttribute(WebPluginInitLinstener.PLUGIN_INIT_EVENT + "." + plugin.getName(), pluginInitEvent);
	}

}
