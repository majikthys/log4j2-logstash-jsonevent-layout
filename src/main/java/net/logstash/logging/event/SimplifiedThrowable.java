package net.logstash.logging.event;

import org.apache.commons.lang3.exception.ExceptionUtils;

public class SimplifiedThrowable {

	protected String exception_class = null;	
	protected String exception_message = null;
	protected String stacktrace = null;

	protected SimplifiedThrowable(Throwable throwable) {
		this.setException_class(throwable.getClass().getSimpleName());
		this.setException_message(throwable.getMessage());
		this.setStacktrace(ExceptionUtils.getStackTrace(throwable));
	}

	/**
	 * Really only for tests
	 * TODO should this be killed?
	 */
	protected SimplifiedThrowable() {
	}
	
	public String getException_class() {
		return exception_class;
	}
	public String getException_message() {
		return exception_message;
	}
	public String getStacktrace() {
		return stacktrace;
	}

	protected void setException_class(String exception_class) {
		this.exception_class = exception_class;
	}
	protected void setException_message(String exception_message) {
		this.exception_message = exception_message;
	}
	protected void setStacktrace(String stacktrace) {
		this.stacktrace = stacktrace;
	}


}
