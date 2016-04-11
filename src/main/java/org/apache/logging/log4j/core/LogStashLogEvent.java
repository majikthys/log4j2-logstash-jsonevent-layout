package org.apache.logging.log4j.core;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import com.fasterxml.jackson.databind.util.StdConverter;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.impl.ThrowableProxy;
import org.apache.logging.log4j.message.Message;

/**
 * To serialize with mixin AND add two properties (@version and @timestamp) we
 * need to convert from LogEvent to a POJO with these new accessor methods.
 *
 * All LogEvent methods pass through to wrappedLogEvent used to instantiate object.
 *
 * Created by jeremyfranklin-ross on 7/28/15.
 */
public class LogStashLogEvent implements LogEvent{

    static final String LOG_STASH_ISO8601_TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
    static final DateFormat iso8601DateFormat = new SimpleDateFormat(LOG_STASH_ISO8601_TIMESTAMP_FORMAT);

    private LogEvent wrappedLogEvent;

    public LogStashLogEvent(LogEvent wrappedLogEvent) {
        this.wrappedLogEvent = wrappedLogEvent;
    }

    public String getVersion() {
        return "1";//LOGSTASH VERSION
    }

    public String getTimestamp() {
        return iso8601DateFormat.format(new Date(this.getTimeMillis()));
    }

    @Override
    public Map<String, String> getContextMap() {
        return wrappedLogEvent.getContextMap();
    }

    @Override
    public ThreadContext.ContextStack getContextStack() {
        return wrappedLogEvent.getContextStack();
    }

    @Override
    public String getLoggerFqcn() {
        return wrappedLogEvent.getLoggerFqcn();
    }

    @Override
    public Level getLevel() {
        return wrappedLogEvent.getLevel();
    }

    @Override
    public String getLoggerName() {
        return wrappedLogEvent.getLoggerName();
    }

    @Override
    public Marker getMarker() {
        return wrappedLogEvent.getMarker();
    }

    @Override
    public Message getMessage() {
        return wrappedLogEvent.getMessage();
    }

    @Override
    public long getTimeMillis() {
        return wrappedLogEvent.getTimeMillis();
    }

    @Override
    public StackTraceElement getSource() {
        return wrappedLogEvent.getSource();
    }

    @Override
    public String getThreadName() {
        return wrappedLogEvent.getThreadName();
    }

    @Override
    public Throwable getThrown() {
        return wrappedLogEvent.getThrown();
    }

    @Override
    public ThrowableProxy getThrownProxy() {
        return wrappedLogEvent.getThrownProxy();
    }

    @Override
    public boolean isEndOfBatch() {
        return wrappedLogEvent.isEndOfBatch();
    }

    @Override
    public boolean isIncludeLocation() {
        return wrappedLogEvent.isIncludeLocation();
    }

    @Override
    public void setEndOfBatch(boolean endOfBatch) {
        wrappedLogEvent.setEndOfBatch(endOfBatch);

    }

    @Override
    public void setIncludeLocation(boolean locationRequired) {
        wrappedLogEvent.setIncludeLocation(locationRequired);
    }

    @Override
    public long getNanoTime() {
        return wrappedLogEvent.getNanoTime();
    }

    /**
     * Converter used by JsonSerilize annotation on mixin.
     *
     * Created by jeremyfranklin-ross on 7/28/15.
     */
    public static class LogEventToLogStashLogEventConverter extends StdConverter<LogEvent, LogStashLogEvent> {

        @Override
        public LogStashLogEvent convert(LogEvent value) {
            return new LogStashLogEvent(value);
        }
    }
}
