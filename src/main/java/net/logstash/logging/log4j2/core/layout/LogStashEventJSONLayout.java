package net.logstash.logging.log4j2.core.layout;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import net.logstash.logging.event.LogStashEvent;
import net.logstash.logging.event.LogStashEventBuilder;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
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

	// This exists only for debugging
	private boolean prettyPrint = false;
	private String[] tags = new String[]{};
	
	

	// Null is significant value for the getters, meaning try to discover.  
	protected String hostNameProperty = null;
	protected String applicationNameProperty = null;
	protected String localhostAddressProperty = null;
	protected Properties properties = System.getProperties();
	
	protected String[] getTags() {
		return tags;
	}

	protected void setTags(String[] tags) {
		this.tags = tags;
	}

	protected String[] getFieldProperties() {
		return fieldProperties;
	}

	protected void setFieldProperties(String[] fieldProperties) {
		this.fieldProperties = fieldProperties;
	}

	private String[] fieldProperties = new String[]{};

	protected static ObjectMapper objectMapper;
	
	
	static {
		objectMapper = new ObjectMapper();
		//TODO configure objectMapper		
	}
	
	protected LogStashEventBuilder logStashEventBuilder = new LogStashEventBuilder();

		
	@Override
	public  String toSerializable(LogEvent event) {
		//TODO clear or oneshot builder?
		synchronized (this) {
			handleLocalInfo();
			handleLogEvent(event);
			handleMessage(event.getMessage());
			handleTags(tags);
			handleFieldProperties(fieldProperties);
			
			// TODO Auto-generated method stub
			try {
				return LogStashEvent.marshallToJSON(logStashEventBuilder.buildLogStashEvent(), prettyPrint);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}

	protected void handleFieldProperties(String[] fieldProperties) {
		if (ArrayUtils.isNotEmpty(fieldProperties)) {
			for (String fieldProperty : fieldProperties) {
				if (properties.containsKey(fieldProperty)) {
					logStashEventBuilder.addField(fieldProperty, properties.getProperty(fieldProperty));
				}
			}
		}
 	}

	protected void handleTags(String[] tags) {
		if (ArrayUtils.isNotEmpty(tags)) {
			logStashEventBuilder.addTags(tags);
		}
	}

	protected void handleLocalInfo() {
		String hostName = getHostName();
		String type = getLogStashType();
		String applicationName = getApplicationName();
		String localAddress = getLocalhostAddress();
		
		logStashEventBuilder.setType(type);
		logStashEventBuilder.setSourceHost(hostName);
		logStashEventBuilder.setSource(type  + ";" + hostName + ";" + applicationName);

		logStashEventBuilder.addField("source_address", localAddress);
		logStashEventBuilder.addField("source_application", applicationName);
	}
	
	protected String getLogStashType() {
		return getClass().getSimpleName();
	}
	
	protected void handleLogEvent(LogEvent event) {
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
	protected void handleMessage(Message message) {
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

	
	
	
	protected void setHostNameProperty(String hostNameProperty) {
		this.hostNameProperty = hostNameProperty;
	}

	protected void setApplicationNameProperty(String applicationNameProperty) {
		this.applicationNameProperty = applicationNameProperty;
	}

	protected void setLocalHostAddressProperty(String localHostAddressProperty) {
		this.localhostAddressProperty = localHostAddressProperty;
	}

	protected void setProperties(Properties properties) {
		this.properties = properties;
	}
	
	protected Properties getProperties() {
		return properties;
	}
	
	protected String getHostName() {
		String hostName = null;
		if (null != hostNameProperty) {
			hostName = getProperties().getProperty(hostNameProperty);
		} else {
			try {
				hostName = InetAddress.getLocalHost().getHostName();
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return hostName; 
	}

	protected String getApplicationName() {
		String applicationName = null;
		if (null != applicationNameProperty) {
			applicationName = getProperties().getProperty(applicationNameProperty);
		} 		
		return applicationName;		
	}

	protected String getLocalhostAddress() {
		String hostAddress = null;
		if (null != localhostAddressProperty) {
			hostAddress = getProperties().getProperty(localhostAddressProperty);
		} else {
			try {
				hostAddress = InetAddress.getLocalHost().getHostAddress();
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return hostAddress; 
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
			@PluginAttr("prettyprint") String prettyprint,
			@PluginAttr("tags") String tags,
			@PluginAttr("fieldProperties") String fieldProperties,
			@PluginAttr("hostNameProperty") String hostNameProperty,
			@PluginAttr("applicationNameProperty") String applicationNameProperty,
			@PluginAttr("localhostAddressProperty") String localhostAddressProperty

			) {
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
		if (StringUtils.isNotBlank(hostNameProperty)) {
			logStashEventJSONLayout.setHostNameProperty(hostNameProperty);
		}
		if (StringUtils.isNotBlank(applicationNameProperty)) {
			logStashEventJSONLayout.setApplicationNameProperty(applicationNameProperty);
		}
		if (StringUtils.isNotBlank(localhostAddressProperty)) {
			logStashEventJSONLayout.setLocalHostAddressProperty(localhostAddressProperty);
		}

		
		if (StringUtils.isNotBlank(tags)) {
			logStashEventJSONLayout.setTags(tags.split(","));
		}

		if (StringUtils.isNotBlank(fieldProperties)) {
			logStashEventJSONLayout.setFieldProperties(fieldProperties.split(","));
		}

		
		return logStashEventJSONLayout;
	}

	
	
	
}
