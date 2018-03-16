package com.why.platform.framework.engine.module;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextAttributeEvent;
import javax.servlet.ServletContextAttributeListener;

import com.why.platform.framework.engine.event.PluginInitEvent;
import com.why.platform.framework.engine.event.PluginsInitEvent;

public abstract class WebPluginInitLinstener implements ServletContextAttributeListener {
	
	 public static final String PLUGIN_INIT_EVENT = "why.plugin.init";
	 public static final String PLUGINS_INIT_EVENT = "why.plugins.init";

	public void attributeAdded(ServletContextAttributeEvent event) {
		if(event.getName().startsWith(PLUGIN_INIT_EVENT)) {
			PluginInitEvent pluginInitEvent = (PluginInitEvent) event.getValue();
			onPluginInit(event.getServletContext(), (Plugin)pluginInitEvent.getSource());
		}
		
		if (event.getName().equals(PLUGINS_INIT_EVENT)) {
            PluginsInitEvent pluginsInitEvent = (PluginsInitEvent) event.getValue();
            onPluginsInit((ServletContext) pluginsInitEvent.getSource());
            return;
        }
	}

	public void attributeRemoved(ServletContextAttributeEvent event) {
	}

	public void attributeReplaced(ServletContextAttributeEvent event) {
	}
	
	protected void onPluginInit(ServletContext servletContext,Plugin plugin) {

    }

    protected void onPluginsInit(ServletContext ctx) {

    }

}
