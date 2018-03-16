package com.why.platform.framework.engine.jetty;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.server.session.AbstractSession;
import org.eclipse.jetty.server.session.AbstractSessionManager;

public class EmptySessionManager extends AbstractSessionManager {
	
	private static EmptySession default_session;

	public EmptySessionManager() {
		_sessionIdManager = new EmptySessionIdManager();
		if(default_session == null) {
			default_session = new EmptySession(this, null);
		}
	}
	
	@Override
	protected void addSession(AbstractSession arg0) {

	}

	@Override
	public AbstractSession getSession(String arg0) {
		return default_session;
	}

	@Override
	protected AbstractSession newSession(HttpServletRequest arg0) {
		return default_session;
	}

	@Override
	protected boolean removeSession(String arg0) {
		return true;
	}

	@Override
	protected void shutdownSessions() throws Exception {

	}

}
