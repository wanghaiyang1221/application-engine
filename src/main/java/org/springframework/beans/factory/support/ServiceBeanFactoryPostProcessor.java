package org.springframework.beans.factory.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import com.why.platform.common.utils.AppSettings;
import com.why.platform.components.backagent.export.InterfaceExporterImpl;
import com.why.platform.framework.engine.service.Exporter;

public class ServiceBeanFactoryPostProcessor implements BeanFactoryPostProcessor {
	private static final Logger logger = LoggerFactory.getLogger(ServiceBeanPostProcessor.class);
	
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		ServiceBeanPostProcessor a = new ServiceBeanPostProcessor(beanFactory);
		beanFactory.addBeanPostProcessor(a);
	}
	
	public static class ServiceBeanPostProcessor implements BeanPostProcessor {
		
		private DefaultListableBeanFactory beanFactory;
		
		public ServiceBeanPostProcessor(ConfigurableListableBeanFactory beanFactory) {
			this.beanFactory = (DefaultListableBeanFactory) beanFactory;
		}

		public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
			Exporter exporter = bean.getClass().getAnnotation(Exporter.class);
			
			if(exporter != null) {
				RootBeanDefinition beanDefinition = (RootBeanDefinition)BeanDefinitionBuilder.rootBeanDefinition(InterfaceExporterImpl.class)
						.setScope(BeanDefinition.SCOPE_SINGLETON)
						.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_NO)
						.setInitMethodName("init")
						.addPropertyValue("gateway_uri", AppSettings.getInstance().get("why.gateway.uri"))
						.addPropertyValue("service_regist_host", AppSettings.getInstance().get("why.gateway.service-regist.host"))
						.addPropertyValue("service_regist_port", AppSettings.getInstance().get("why.gateway.service-regist.port"))
						.addPropertyValue("regist", AppSettings.getInstance().get("why.gateway.service-regist.auto_regist"))
						.addPropertyValue("webRoot",AppSettings.getInstance().get("server.http.webapp.default.contextPath"))
	                    .addPropertyValue("provider", AppSettings.getInstance().getInt("oy.app.id"))
	                    .addPropertyValue("serviceName", exporter.name())
	                    .addPropertyValue("version", exporter.version())
	                    .addPropertyValue("serviceInterface", exporter.serviceInterface())
	                    .addPropertyReference("service", beanName)
	                    .addDependsOn(beanName)
	                    .getBeanDefinition();
				beanFactory.createBean(String.format("exporter.%s", beanName), beanDefinition, null);
				logger.info("regist exporter service:[{}] for bean",
	                    String.format("exporter.%s", beanName),
	                    bean);
			}
			
			return bean;
		}

		public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
			return bean;
		}
	}
}
