package net.logstash.logging.log4j2.core.layout;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import net.logstash.logging.event.LogStashEvent;
import net.logstash.logging.event.LogStashEventBuilder;

import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.ThreadContext.ContextStack;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttr;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;
import org.apache.logging.log4j.message.Message;

import com.fasterxml.jackson.databind.ObjectMapper;

@Plugin(name = "LogStashEventJSONLayout", category = "Core", elementType = "layout", printObject = true)
public class LogStashEventJSONLayout extends AbstractStringLayout {
	//TODO add boolean to control introspections
	protected LogStashEventJSONLayout(Charset charset, boolean prettyPrint) {
		super(charset);
		this.prettyPrint = prettyPrint;
	}

	private boolean prettyPrint = false;
	
	protected static ObjectMapper objectMapper;
	
	
	static {
		objectMapper = new ObjectMapper();
		//TODO configure objectMapper		
	}
	
		
	@Override
	public String toSerializable(LogEvent event) {
		LogStashEventBuilder logStashEventBuilder = new LogStashEventBuilder();
		handleLogEvent(event, logStashEventBuilder);
		handleMessage(event.getMessage(), logStashEventBuilder);

		
		// TODO Auto-generated method stub
		try {
			return LogStashEvent.marshallToJSON(logStashEventBuilder.buildLogStashEvent(), prettyPrint);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	protected void handleLogEvent(LogEvent event, LogStashEventBuilder logStashEventBuilder) {
		//Level
		logStashEventBuilder.addField("logger_name", event.getLoggerName());
		logStashEventBuilder.addField("thread_name", event.getThreadName());
		if (null != event.getLevel()) {
			logStashEventBuilder.addField("level", event.getLevel());
			logStashEventBuilder.addField("level_value", event.getLevel().intLevel());		
		}
		logStashEventBuilder.addField("logger_fqcn", event.getFQCN());
		
		StackTraceElement stackTrace = event.getSource();
		if (null != stackTrace) {
			logStashEventBuilder.addField("caller_class_name", stackTrace.getClassName());
			logStashEventBuilder.addField("caller_method_name", stackTrace.getMethodName());
			logStashEventBuilder.addField("caller_method_is_native", stackTrace.isNativeMethod());
			logStashEventBuilder.addField("caller_file_name", stackTrace.getFileName());
			logStashEventBuilder.addField("caller_line_number", stackTrace.getLineNumber());
		}
		
		
		Marker marker = event.getMarker();
		if (null != marker) {
			logStashEventBuilder.addField("marker", marker.getName());
			//Decend Marker Hierarchy TODO ???
			//event.getMarker().getParent()
		}
	
		logStashEventBuilder.setTimestamp(event.getMillis());

		//Apparently this can be different than event.getMessage().getThrowable... but should check for equality TODO
		Throwable thrown = event.getThrown();
		if (null != thrown) {
			logStashEventBuilder.addField("thrown", thrown);
		}
		
		ContextStack nestedDiagnosticContext = event.getContextStack();
		if (null != nestedDiagnosticContext && null != nestedDiagnosticContext.asList()) {
			logStashEventBuilder.addField("nested_diagnostic_context", nestedDiagnosticContext.asList());
		}
		
		//Map Diagnostic Context (all kv pairs)
		Map<String,String> contextMap = event.getContextMap();
		if (null != contextMap && !contextMap.isEmpty()) {
			logStashEventBuilder.addField("context_map", contextMap);
		}
			
		//TODO is LogEvent.isEndOfBatch rela to this layout (assuming no)	
	}
	
	/**
	 * Implemented as opportunity to override with Message subclass specific behavior
	 * 
	 * @param message
	 */
	protected void handleMessage(Message message, LogStashEventBuilder logStashEventBuilder) {
		//preflight abort
		if (message == null) {
			return;
		}
		
		//Event Message Type
		logStashEventBuilder.addField("message_type", message.getClass().getSimpleName());
		//TODO special handling for different message types?
		
		//Message Objects Parsing (each object) TODO
		
		if(null != message.getParameters() && message.getParameters().length > 0) {
			logStashEventBuilder.addField("message_parameters", message.getParameters());
		}
		
		Throwable message_throwable = message.getThrowable();
		if (null != message_throwable) {
			logStashEventBuilder.addField("message_throwable", message_throwable);
		}
		
		String message_formatted = message.getFormattedMessage();
		if (null !=message_formatted) {
			logStashEventBuilder.addField("message_formatted", message_formatted);
		}
	}
	
	
	MessageObjectsHandler messageObjectsHandler = null; //TODO add setting messageObjectsHandler to constructor
	
	public void setMessageObjectsHandler(MessageObjectsHandler messageObjectsHandler) {
		this.messageObjectsHandler = messageObjectsHandler;
	}
	
	protected String getHostName() {
		return null; //TODO
	}

	protected String getApplicationName() {
		return null; //TODO		
	}

	protected String getLocalIP() {
		return null; //TODO		
	}


	
	
	@Override
	public Map<String, String> getContentFormat() {
		return new HashMap<String,String>();
	}

	
	@Override 
	public String getContentType() {
		return "application/json; charset=" + this.getCharset(); //TODO is there an handy constant in core libs?
	}
	
	@PluginFactory
	public static LogStashEventJSONLayout createLayout(
			@PluginAttr("charset") String charset,
			@PluginAttr("prettyprint") String prettyprint) {
		Charset c = Charset.isSupported("UTF-8") ? Charset.forName("UTF-8")
				: Charset.defaultCharset();
		if (charset != null) {
			if (Charset.isSupported(charset)) {
				c = Charset.forName(charset);
			} else {
				LOGGER.error("Charset " + charset
						+ " is not supported for layout, using "
						+ c.displayName());
			}
		}

		
		boolean pp = (prettyprint != null && Boolean.parseBoolean(prettyprint)) ? true : false;

		LogStashEventJSONLayout logStashEventJSONLayout = new LogStashEventJSONLayout(c, pp);
		

		
		return logStashEventJSONLayout;
	}

	
	
	
}
