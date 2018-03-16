/*
 * Copyright © 2010 www.myctu.cn. All rights reserved.
 */

package com.why.platform.framework.engine.event;

import org.springframework.context.ApplicationEvent;

import javax.servlet.ServletContext;

/**
 * User: pippo
 * Date: 13-12-16-20:26
 */
public class PluginsInitEvent extends ApplicationEvent {

    private static final long serialVersionUID = 5208655107052400366L;

    public PluginsInitEvent(ServletContext ctx) {
        super(ctx);
    }
}
