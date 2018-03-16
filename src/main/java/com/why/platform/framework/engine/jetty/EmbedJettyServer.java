package com.why.platform.framework.engine.jetty;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.jmx.MBeanContainer;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.StatisticsHandler;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import com.why.platform.framework.engine.thread.ContextSupportJettyQueuedThreadPool;
import com.why.platform.framework.spring.ext.ClassScanner;
import com.why.platform.framework.spring.ext.ClassScanner.Handler;

public class EmbedJettyServer {
	private static final Logger logger = LoggerFactory.getLogger(EmbedJettyServer.class);
	
	protected static Server server = null;
	
	public static Server getServer() {
		return server;
	}
	
	public void start() {
		if(server != null) {
			logger.warn("the jetty server:[{}] is exists, ignore start operation", server);
			return;
		}
		
		try {
			createServer();
			createJmxSupport();
			createStatistics();
			if(server != null) {
				server.start();
			}
		} catch (Throwable t){
            throw new RuntimeException(t);
        }
	}
	
	public void stop() {
		if(server == null) {
			logger.debug("the jetty server not exists, ignore stop operation");
			return;
		}
		
		try {
			server.stop();
			server = null;
		} catch (Throwable t){
            throw new RuntimeException(t);
        }
	}
	
	protected void createServer() {
		server = new Server(createThreadPool());
		server.setDumpAfterStart(false);
		server.setDumpBeforeStop(false);
		server.setStopAtShutdown(true);
		server.addConnector(createConnector());
		server.setHandler(createWebAppHandler());
		logger.debug("create jetty server");
	}
	
	protected QueuedThreadPool createThreadPool() {
		QueuedThreadPool queuedThreadPool = new ContextSupportJettyQueuedThreadPool();
		queuedThreadPool.setName("jetty inbound thread pool");
		queuedThreadPool.setIdleTimeout(1000 * 60);
		if(maxThreads < minThreads) {
			maxThreads = minThreads;
		}
		queuedThreadPool.setMaxThreads(maxThreads);
		queuedThreadPool.setMinThreads(minThreads);
		logger.debug("create jetty thread pool:[{}]", queuedThreadPool);
		return queuedThreadPool;
	}
	
	protected Connector createConnector(){
        ServerConnector connector = new ServerConnector(server);
        connector.setHost(host);
        connector.setPort(port);
        logger.debug("create jetty connector:[{}]", connector);
        return connector;
    }
	
	protected HandlerList createWebAppHandler() {
		final HandlerList handlerList = new HandlerList();
		ClassScanner classScanner = new ClassScanner();
		classScanner.addIncludeFilters(new AnnotationTypeFilter(WebAppRegistry.class));
		classScanner.scan(webAppScanPackage, "**/package-info.class", new Handler() {

			public void handle(MetadataReader metadataReader) {
				AnnotationAttributes[] webapps = (AnnotationAttributes[]) metadataReader.getAnnotationMetadata().
						getAnnotationAttributes(WebAppRegistry.class.getName()).get("value");
				
				for(AnnotationAttributes attributes : webapps) {
					String dir = attributes.getString("dir");
					String contextPath = attributes.getString("contextPath");
					String[] welcomeFiles = attributes.getStringArray("welcomeFiles");
					
					WebAppContext appContext = createWebAppContext(dir, contextPath, welcomeFiles);
					if(appContext != null) {
						handlerList.addHandler(appContext);
					}
				}
			}
		});
		
		defaultWebAppContext = createDefaultWebAppContext();
		if(defaultWebAppContext != null) {
			handlerList.addHandler(defaultWebAppContext);
		}
		
		return handlerList;
	}
	
	protected WebAppContext defaultWebAppContext = null;

    protected WebAppContext createDefaultWebAppContext() {
        defaultWebAppContext = createWebAppContext(defaultWebAppDir,
                defaultWebAppContextPath,
                new String[] { "index.html" });

        return defaultWebAppContext;
    }
	
	protected Map<String, WebAppContext> existContextPath = new HashMap<String, WebAppContext>();
	protected WebAppContext createWebAppContext(String dir, String contextPath, String[] welcomeFiles) {
		File webapp = null;
		if(EmbedJettyServer.class.getResource(dir) != null) {
			webapp = new File(EmbedJettyServer.class.getResource(dir).getFile());
		} else {
			webapp = new File(dir);
		}
		
		if(!webapp.exists()) {
			logger.warn("invalid webapp dir:[{}], ignore it", dir);
			return null;
		}
		
		if(!contextPath.startsWith("/")) {
			contextPath = "/" + contextPath;
		}
		
		if(existContextPath.containsKey(contextPath)) {
			logger.warn("conflict contextPath:[{}] mapping:[{}] and [{}], ignore mapping:[{}]",
                    contextPath,
                    existContextPath.get(contextPath),
                    webapp,
                    webapp);

            return existContextPath.get(contextPath);
		}
		
		WebAppContext appContext = new WebAppContext(webapp.getAbsolutePath(), contextPath);
		appContext.setInitParameter("org.eclipse.jetty.servlet.Default.dirAllowed", "true");
		appContext.setWelcomeFiles(welcomeFiles);
		
		if(!useSession) {
			appContext.setSessionHandler(new SessionHandler(new EmptySessionManager()));
		}
		
		existContextPath.put(contextPath, appContext);
		logger.debug("create webapp:[{}] with contextPath:[{}]",appContext,contextPath);
		return appContext;
	}
	
	protected void createJmxSupport() {
		MBeanContainer mBeanContainer = new MBeanContainer(ManagementFactory.getPlatformMBeanServer());
		
		if(StringUtils.isBlank(jmxDomain)) {
			jmxDomain = String.format("jetty-%s", System.currentTimeMillis());
		}
		
		mBeanContainer.setDomain(jmxDomain);
		server.addBean(mBeanContainer);
	}
	
	protected void createStatistics() {
		StatisticsHandler handler = new StatisticsHandler();
		handler.setHandler(server.getHandler());
		server.setHandler(handler);
	}
	
	protected int minThreads= Math.max(4, Runtime.getRuntime().availableProcessors());
    protected int maxThreads = minThreads * 40 + 1;
    protected String host;
    protected int port;
    protected String jmxDomain;
    protected boolean useSession = false;
    protected String defaultWebAppDir = "/";
    protected String defaultWebAppContextPath = "/";
    protected String webAppScanPackage = "com.why";

    public void setMinThreads(int minThreads) {
        if (minThreads > 4) {
            this.minThreads = minThreads;
        }
    }

    public void setMaxThreads(int maxThreads) {
        if (maxThreads > 4) {
            this.maxThreads = maxThreads;
        }
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setJmxDomain(String jmxDomain) {
        this.jmxDomain = jmxDomain;
    }

    public void setUseSession(boolean useSession) {
        this.useSession = useSession;
    }

    public void setDefaultWebAppDir(String defaultWebAppDir) {
        this.defaultWebAppDir = defaultWebAppDir;
    }

    public void setDefaultWebAppContextPath(String defaultWebAppContextPath) {
        this.defaultWebAppContextPath = defaultWebAppContextPath;
    }

    public void setWebAppScanPackage(String webAppScanPackage) {
        this.webAppScanPackage = webAppScanPackage;
    }
    
    public static void main(String[] args) {
		EmbedJettyServer embedJettyServer = new EmbedJettyServer() {

			@Override
			public WebAppContext createDefaultWebAppContext() {
				return createWebAppContext("/", "/", new String[] {"index.html"});
			}
			
		};
		
		embedJettyServer.setHost("127.0.0.1");
		embedJettyServer.setPort(8080);
		embedJettyServer.start();
		
	}
}
