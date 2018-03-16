package com.why.platform.framework.engine.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.core.Ordered;

import com.why.platform.common.utils.servlet3.EngineEnvironment;
import com.why.platform.framework.engine.jetty.Servlet3JettyInbound;

/**
 * Created by zhouxl on 2016/11/17 0017.
 */
public class HttpExporterServer extends Servlet3JettyInbound implements ExporterServer,
        ApplicationListener<ApplicationContextEvent>, Ordered {

    private final static Logger logger = LoggerFactory.getLogger(HttpExporterServer.class);

    @Override
    public void onApplicationEvent(ApplicationContextEvent event) {
        if(event instanceof ContextStartedEvent){
            start();
        }
        if(event instanceof ContextClosedEvent){
            stop();
        }
    }

    /* 该应用应该在所有bean之后初始化 */
    @Override
    public int getOrder() {
        return Integer.MAX_VALUE;
    }
    @Override
    public void start() {
        /**
         * 如果已经在serverlet容器内，不能再启动
         */
        if (EngineEnvironment.isServletContainer()) {
            logger.warn(
                    "current application in servlet3 container, ignore start embedded jetty server, the real export service path is:[{}]",
                    EngineEnvironment.getServletContext().getContextPath());
            return;
        }
        setJmxDomain(String.format("jetty-%s", System.currentTimeMillis()));
        super.start();
    }


}
