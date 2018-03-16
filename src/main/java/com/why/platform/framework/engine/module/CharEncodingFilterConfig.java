package com.why.platform.framework.engine.module;

import java.util.Arrays;
import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.CharEncoding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.CharacterEncodingFilter;

import com.why.platform.common.utils.AppSettings;

public class CharEncodingFilterConfig {
	private static final Logger logger = LoggerFactory.getLogger(CharEncodingFilterConfig.class);
	protected FilterRegistration.Dynamic encodingFilter;
	
	public CharEncodingFilterConfig(ServletContext context) {
		String enableEncodingFilter = AppSettings.getInstance().get("server.http.encoding.enable");
		String enableEncodingCharset = AppSettings.getInstance().get("server.http.encoding.charset");
		if(BooleanUtils.toBoolean(enableEncodingFilter.trim())) {
			encodingFilter = context.addFilter("encoding", new CharacterEncodingFilter());
			encodingFilter.setInitParameter("encoding", StringUtils.isNotBlank(enableEncodingCharset) ? enableEncodingCharset : CharEncoding.UTF_8);
			encodingFilter.setInitParameter("forceEncoding", "true");
			encodingFilter.setAsyncSupported(true);
		}
	}
	
	public void addMapping(ServletRegistration registration) {
		for(String patter : registration.getMappings()) {
			addMapping(patter);
		}
	}
	
	public void addMapping(String ...urlPattern) {
		if(encodingFilter == null) {
			return;
		}
		
		logger.info("enable encoding filter for url pattern: [{}]", Arrays.toString(urlPattern));
		encodingFilter.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, urlPattern);
	}
}
