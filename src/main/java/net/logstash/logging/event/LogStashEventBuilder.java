package net.logstash.logging.event;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class LogStashEventBuilder {
	LogStashEventI logStashEvent = new LogStashEventI();
	protected final DateFormat iso8601DateFormat = new SimpleDateFormat(LogStashEvent.LOG_STASH_ISO8601_TIMESTAMP_FORMAT);
	
	public LogStashEventBuilder() {}
	
	public void addField(String key, Object value) {
		logStashEvent.addFields(key, value);
	}
	
	public void addField(String key, Throwable value) {
		addField(key, new SimplifiedThrowable(value));
	}
	
	public void addTag(String tag) {
		logStashEvent.addTags(tag);
	}

	public void addTags(String ... tags) {
		logStashEvent.addTags(tags);
	}

	public void setMessage(String message) {
		logStashEvent.setMessage(message);
	}
	
	public void setSource(String source) {
		logStashEvent.setSource(source);
	}

	public void setSourcePath(String sourcePath) {
		logStashEvent.setSource_path(sourcePath);
	}
	
	public void setSourceHost(String sourceHost) {
		logStashEvent.setSource_host(sourceHost);
	}
	
	public void setType(String type) {
		logStashEvent.setType(type);
	}
	
	
	/**
	 * @param timestamp milliseconds since the standard base time known as "the epoch", namely January 1, 1970, 00:00:00 GMT.
	 */
	public void setTimestamp(long timestamp) {
		setTimestamp(new Date(timestamp));
	}

	public void setTimestamp(Date timestamp) {
		setTimestamp(iso8601DateFormat.format(timestamp));
	}
	
	public void setTimestamp(String timestamp) {
		logStashEvent.setTimestamp(timestamp);
	}
	
	public LogStashEvent buildLogStashEvent() {
		return logStashEvent;
	}
	
	protected class LogStashEventI extends LogStashEvent {
		protected LogStashEventI() {}
	}
}
