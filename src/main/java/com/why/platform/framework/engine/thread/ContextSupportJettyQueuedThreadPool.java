package com.why.platform.framework.engine.thread;

import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContextSupportJettyQueuedThreadPool extends QueuedThreadPool {
	private final static Logger logger = LoggerFactory.getLogger(ContextSupportJettyQueuedThreadPool.class);

	@Override
	protected void runJob(Runnable job) {
		try {
			super.runJob(job);
		} finally {
			ThreadContext.clear();
			logger.trace("clear thread:[{}] context", Thread.currentThread());
		}
	}
	
}
