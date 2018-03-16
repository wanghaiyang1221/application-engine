package com.why.platform.framework.engine;

import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.beans.factory.support.ServiceBeanFactoryPostProcessor;
import org.springframework.context.ApplicationListener;

import com.why.platform.common.utils.AppSettings;
import com.why.platform.common.utils.servlet3.EngineEnvironment;
import com.why.platform.framework.engine.event.PluginInitEvent;
import com.why.platform.framework.engine.module.Component;
import com.why.platform.framework.engine.module.Plugin;
import com.why.platform.framework.engine.spring.EngineApplicationContext;
import com.why.platform.framework.spring.ext.ApplicationContextLauncher;
import com.why.platform.framework.spring.ext.BeanLocator;

public class ApplicationEngine {
	private static final Logger logger = LoggerFactory.getLogger(ApplicationEngine.class);
	
	private static final Set<Component> COMPONENTS = new LinkedHashSet<Component>();
	private static final Map<String, Plugin> PLUGINS = new LinkedHashMap<String, Plugin>();
	
	static {
		SLF4JBridgeHandler.removeHandlersForRootLogger();
		SLF4JBridgeHandler.install();
	}
	
	public static void main(String[] args) throws Exception {
		AppSettings.init(ApplicationEngine.class.getResource(getSettingPath(args)).getPath());
		ApplicationEngine applicationEngine = new ApplicationEngine();
		applicationEngine.start();
		logger.info("application engine start completed");
		EngineEnvironment.setServletContext(EngineEnvironment.getServletContext());
		Thread.currentThread().join();
	}
	
	public static String getSettingPath(String []args) {
		String settingPath = args != null && args.length > 1 && StringUtils.isNotBlank(args[1]) ? args[1] : Constans.DEFAULT_SETTING;
		if(!settingPath.startsWith("/")) {
			settingPath  = "/" + settingPath;
		}
		return settingPath;
	}
	
	public void start() throws PluginEngineException {
		createApplicationContext();
		initCompontents();
		initPlugin();
		ApplicationContextLauncher.start();
	}
	
	protected void createApplicationContext() {
		EngineApplicationContext applicationContext = null;
		
		if(ApplicationEngine.class.getResource("/" + Constans.MAIN_XML_SETTTING) == null) {
			logger.info("can not find the main xml: /[{}], use:[/META-INF/{}]", Constans.MAIN_XML_SETTTING, Constans.MAIN_XML_SETTTING);
			applicationContext = new EngineApplicationContext("/META-INF/" + Constans.MAIN_XML_SETTTING);
		} else {
			applicationContext = new EngineApplicationContext("/" + Constans.MAIN_XML_SETTTING);
		}
		
		applicationContext.addBeanFactoryPostProcessor(new ServiceBeanFactoryPostProcessor());
		
		BeanLocator.setContext(applicationContext);
	}
	
	protected void initCompontents() throws PluginEngineException {
		try {
			Enumeration<URL> componentsUrls = ClassLoader.getSystemResources("META-INF/components");
			while(componentsUrls.hasMoreElements()) {
				URL url = componentsUrls.nextElement();
				logger.info("find modules declaration: [{}]", url);
				List<String> lines = IOUtils.readLines(url.openStream());
				for(String line : lines) {
					logger.info("### try to create module: [{}] ###", line);
					Component component = (Component) Class.forName(line).getConstructor(EngineApplicationContext.class).newInstance(BeanLocator.getContext());
					component.init();
					logger.info("### create module finished: [{}] ###", component.getName());
					COMPONENTS.add(component);
				}
			}
		} catch (Exception e) {
			throw new PluginEngineException(e);
		}
	}
	
	protected void initPlugin() {
		BeanLocator.getContext().addApplicationListener(new ApplicationListener<PluginInitEvent>() {

			public void onApplicationEvent(PluginInitEvent event) {
				Plugin plugin = (Plugin) event.getSource();
				PLUGINS.put(plugin.getName(), plugin);
			}
		});
	}
}
