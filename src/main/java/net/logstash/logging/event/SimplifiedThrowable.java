package net.logstash.logging.event;

import org.apache.commons.lang3.exception.ExceptionUtils;

public class SimplifiedThrowable {

	protected String exception_class = null;	
	protected String exception_message = null;
	protected String stacktrace = null;
	protected SimplifiedThrowable root_cause_throwable = null;



	//TODO bug in beta of log4j2 prevents original Throwable from being retrieved from ProxyThrowable so name is wrong.
	protected SimplifiedThrowable(Throwable throwable) {
		this.setException_class(throwable.getClass().getSimpleName());
		this.setException_message(throwable.getMessage());
		this.setStacktrace(ExceptionUtils.getStackTrace(throwable));
		
		Throwable rootThrowable = ExceptionUtils.getRootCause(throwable); //FEAR NOT! getRootCause() does the right thing.
		if (null != rootThrowable) {
			this.setRoot_cause_throwable(new SimplifiedThrowable(rootThrowable));
		}

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

	public SimplifiedThrowable getRoot_cause_throwable() {
		return root_cause_throwable;
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

	protected void setRoot_cause_throwable(SimplifiedThrowable root_cause_throwable) {
		this.root_cause_throwable = root_cause_throwable;
	}


}
