package net.logstash.logging.log4j2.core.layout;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.text.DateFormat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.helpers.Charsets;
import org.apache.logging.log4j.core.helpers.KeyValuePair;
import org.apache.logging.log4j.core.helpers.Throwables;
import org.apache.logging.log4j.core.helpers.Transform;
import org.apache.logging.log4j.core.layout.JSONLayout;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.MultiformatMessage;
//TODO JER document, include provenance. 
@Plugin(name = "LogStashJSONLayout", category = "Core", elementType = "layout", printObject = true)
public class LogStashJSONLayout extends JSONLayout {
    private static final int DEFAULT_SIZE = 256;
    //Ahoy, X introduced in java 7
    public static final String LOG_STASH_ISO8601_TIMESTAMP_FORMAT ="yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
	protected final DateFormat iso8601DateFormat;


    // We yield to \r\n for the default.
    private static final String DEFAULT_EOL = "\r\n";
    private static final String COMPACT_EOL = "";
    private static final String DEFAULT_INDENT = "  ";
    private static final String COMPACT_INDENT = "";

    private static final String[] FORMATS = new String[] { "json" };

    private final boolean locationInfo;
    private final boolean properties;
    private final boolean complete;
    private final String eol;
    private final String indent1;
    private final String indent2;
    private final String indent3;
    private final String indent4;
    private final String eventEol;
    private volatile boolean firstLayoutDone;
    
    private final boolean excludeLogger;
    private final boolean excludeLevel;
    private final boolean excludeThread; 
    private final boolean excludeMessage; 
    private final boolean excludeNDC; 
    private final boolean excludeThrown; 
    
    private final String subLayoutBegin;
    private final String subLayoutEnd;

    private final boolean jsonEscapeSubLayout;
    private final Map<String, String> additionalLogAttributes;
    
    private Layout<? extends Serializable> subLayout;
    
	protected LogStashJSONLayout(final boolean locationInfo, 
			final boolean properties, 
			final boolean complete, 
			boolean compact,
			final boolean newline,
            final Charset charset,
			final boolean excludeLogger, 
			final boolean excludeLevel, 
			final boolean excludeThread, 
			final boolean excludeMessage, 
			final boolean excludeNDC,
			final boolean excludeThrown,
			final boolean jsonEscapeSubLayout,
			final boolean subLayoutAsElement,
			final DateFormat iso8601DatePrinter,
			final Layout<? extends Serializable> subLayout,
			final Map<String, String> additionalLogAttributes) {
		super(locationInfo, properties, complete, compact, charset);
        this.locationInfo = locationInfo;
        this.properties = properties;
        this.complete = complete;
        this.eol = compact ? COMPACT_EOL : DEFAULT_EOL;
        this.indent1 = compact ? COMPACT_INDENT : DEFAULT_INDENT;
        this.indent2 = this.indent1 + this.indent1;
        this.indent3 = this.indent2 + this.indent1;
        this.indent4 = this.indent3 + this.indent1;
        this.eventEol = newline ? DEFAULT_EOL : this.eol;
        this.excludeLogger= excludeLogger;
        this.excludeLevel = excludeLevel;
        this.excludeThread = excludeThread;
        this.excludeMessage = excludeMessage;
        this.excludeNDC = excludeNDC;
        this.excludeThrown = excludeThrown;
        if (subLayoutAsElement) {
        	this.subLayoutBegin = "{" + this.eol + this.indent3;
        	this.subLayoutEnd = this.indent2+"}";
            this.jsonEscapeSubLayout = false;
        } else {
        	this.subLayoutBegin = "\"";
        	this.subLayoutEnd = "\"";     	
            this.jsonEscapeSubLayout = jsonEscapeSubLayout;
        }
        this.iso8601DateFormat = iso8601DatePrinter;
        this.subLayout = subLayout;
        this.additionalLogAttributes = additionalLogAttributes;
	}
    /**
     * Formats a {@link org.apache.logging.log4j.core.LogEvent} in conformance with the log4j.dtd.
     * 
     * @param event
     *            The LogEvent.
     * @return The XML representation of the LogEvent.
     */
    @Override
    public String toSerializable(final LogEvent event) {
        final StringBuilder buf = new StringBuilder(DEFAULT_SIZE);
        // DC locking to avoid synchronizing the whole layout.
        boolean check = this.firstLayoutDone; 
        if (!this.firstLayoutDone) {
            synchronized(this) {
                check = this.firstLayoutDone;
                if (!check) {
                    this.firstLayoutDone = true;
                } else {
                    buf.append(',');
                    buf.append(this.eventEol);
                }
            }
        } else {
            buf.append(',');
            buf.append(this.eventEol);
        }
        buf.append(this.indent1);
        buf.append('{');
        //**************************************
        //************ LogStash Message Elements
        // yes, it's now a very simple format, 
        // everything else is optional.
        buf.append(this.eol);
        buf.append(this.indent2);
        buf.append("\"@version\":\"");
        buf.append("1"); //LogStash message version is now 1
        buf.append("\",");
        buf.append(this.eol);
        buf.append(this.indent2);
        buf.append("\"@timestamp\":\"");
        buf.append(iso8601DateFormat.format(new Date(event.getMillis()))); 
        //************ LogStash Message Elements
        //**************************************
        
        
        //Logger
        if (!excludeLogger) {
	        buf.append("\",");
	        buf.append(this.eol);
	        buf.append(this.indent2);
	        buf.append("\"logger\":\"");
	        String name = event.getLoggerName();
	        if (name.isEmpty()) {
	            name = "root";
	        }
	        buf.append(Transform.escapeJsonControlCharacters(name));
        }
        
        //Level
        if(!excludeLevel) {
	        buf.append("\",");
	        buf.append(this.eol);
	        buf.append(this.indent2);
	        buf.append("\"level\":\"");
	        buf.append(Transform.escapeJsonControlCharacters(String.valueOf(event.getLevel())));
        }
        
        //Thread
        if (!excludeThread) {
	        buf.append("\",");
	        buf.append(this.eol);
	        buf.append(this.indent2);
	        buf.append("\"thread\":\"");
	        buf.append(Transform.escapeJsonControlCharacters(event.getThreadName()));
        }
        
        //Message
        if (!excludeMessage) {
	        buf.append("\",");
	        buf.append(this.eol);
	
	        final Message msg = event.getMessage();
	        if (msg != null) {
	            boolean jsonSupported = false;
	            if (msg instanceof MultiformatMessage) {
	                final String[] formats = ((MultiformatMessage) msg).getFormats();
	                for (final String format : formats) {
	                    if (format.equalsIgnoreCase("JSON")) {
	                        jsonSupported = true;
	                        break;
	                    }
	                }
	            }
	            buf.append(this.indent2);
	            buf.append("\"message\":\"");
	            if (jsonSupported) {
	                buf.append(((MultiformatMessage) msg).getFormattedMessage(FORMATS));
	            } else {
	                buf.append(Transform.escapeJsonControlCharacters(event.getMessage().getFormattedMessage()));
	            }
	            buf.append('\"');
	        }
        }
        
        //NDC
        if (!excludeNDC && event.getContextStack().getDepth() > 0) {
            buf.append(",");
            buf.append(this.eol);
            buf.append("\"ndc\":");
            buf.append(Transform.escapeJsonControlCharacters(event.getContextStack().toString()));
            buf.append("\"");
        }

        //Thrown
        final Throwable throwable = event.getThrown();
        if (!excludeThrown && throwable != null) {
            buf.append(",");
            buf.append(this.eol);
            buf.append(this.indent2);
            buf.append("\"throwable\":\"");
            final List<String> list = Throwables.toStringList(throwable);
            for (final String str : list) {
                buf.append(Transform.escapeJsonControlCharacters(str));
                buf.append("\\\\n");
            }
            buf.append("\"");
        }

        //LocationInfo
        if (this.locationInfo) {
            final StackTraceElement element = event.getSource();
            buf.append(",");
            buf.append(this.eol);
            buf.append(this.indent2);
            buf.append("\"LocationInfo\":{");
            buf.append(this.eol);
            buf.append(this.indent3);
            buf.append("\"class\":\"");
            buf.append(Transform.escapeJsonControlCharacters(element.getClassName()));
            buf.append("\",");
            buf.append(this.eol);
            buf.append(this.indent3);
            buf.append("\"method\":\"");
            buf.append(Transform.escapeJsonControlCharacters(element.getMethodName()));
            buf.append("\",");
            buf.append(this.eol);
            buf.append(this.indent3);
            buf.append("\"file\":\"");
            buf.append(Transform.escapeJsonControlCharacters(element.getFileName()));
            buf.append("\",");
            buf.append(this.eol);
            buf.append(this.indent3);
            buf.append("\"line\":\"");
            buf.append(element.getLineNumber());
            buf.append("\"");
            buf.append(this.eol);
            buf.append(this.indent2);
            buf.append("}");
        }

        //Properties ContextMap
        if (this.properties && event.getContextMap().size() > 0) {
            buf.append(",");
            buf.append(this.eol);
            buf.append(this.indent2);
            buf.append("\"Properties\":[");
            buf.append(this.eol);
            final Set<Entry<String, String>> entrySet = event.getContextMap().entrySet();
            int i = 1;
            for (final Map.Entry<String, String> entry : entrySet) {
                buf.append(this.indent3);
                buf.append('{');
                buf.append(this.eol);
                buf.append(this.indent4);
                buf.append("\"name\":\"");
                buf.append(Transform.escapeJsonControlCharacters(entry.getKey()));
                buf.append("\",");
                buf.append(this.eol);
                buf.append(this.indent4);
                buf.append("\"value\":\"");
                buf.append(Transform.escapeJsonControlCharacters(String.valueOf(entry.getValue())));
                buf.append("\"");
                buf.append(this.eol);
                buf.append(this.indent3);
                buf.append("}");
                if (i < entrySet.size()) {
                    buf.append(",");
                }
                buf.append(this.eol);
                i++;
            }
            buf.append(this.indent2);
            buf.append("]");
        }
        
        //Log (the sublayout)
        buf.append(",");
        buf.append(this.eol);
        buf.append(this.indent2);
        buf.append("\"log\":");
        buf.append(this.subLayoutBegin);
        
        if (jsonEscapeSubLayout) {
        	Serializable serializedLayoutProduct = subLayout.toSerializable(event);
	        if (serializedLayoutProduct instanceof CharSequence) {
	        	buf.append(Transform.escapeJsonControlCharacters(serializedLayoutProduct.toString()));
	        } else {
	        	buf.append(Transform.escapeJsonControlCharacters(new String(subLayout.toByteArray(event), this.getCharset())));        	
	        }
        } else {
        	Serializable serializedLayoutProduct = subLayout.toSerializable(event);
	        if (serializedLayoutProduct instanceof CharSequence) {
	        	buf.append(serializedLayoutProduct.toString());
	        } else {
	        	buf.append(new String(subLayout.toByteArray(event), this.getCharset()));        	
	        }
        }
        buf.append(this.subLayoutEnd);
        
        for (Entry<String,String> attributeEntry : additionalLogAttributes.entrySet()) {
            buf.append(",");
            buf.append(this.eol);
	        buf.append(this.indent2);
            buf.append("\"");
            buf.append(attributeEntry.getKey());
            buf.append("\":\"");
            buf.append(attributeEntry.getValue());
            buf.append("\"");
        }

        buf.append(this.eol);    
        buf.append(this.indent1);
        buf.append("}");

        return buf.toString();
    }

    /**
     * Returns appropriate JSON headers.
     * 
     * @return a byte array containing the header, opening the JSON array.
     */
    @Override
    public byte[] getHeader() {
        if (!this.complete) {
            return null;
        }
        final StringBuilder buf = new StringBuilder();
        buf.append('[');
        buf.append(this.eol);
        return buf.toString().getBytes(this.getCharset());
    }

    /**
     * Returns appropriate JSON footer.
     * 
     * @return a byte array containing the footer, closing the JSON array.
     */
    @Override
    public byte[] getFooter() {
        if (!this.complete) {
            return null;
        }
        return (this.eol + "]" + this.eol).getBytes(this.getCharset());
    }

    /**
     * XMLLayout's content format is specified by:
     * <p/>
     * Key: "dtd" Value: "log4j-events.dtd"
     * <p/>
     * Key: "version" Value: "2.0"
     * 
     * @return Map of content format keys supporting XMLLayout
     */
    @Override
    public Map<String, String> getContentFormat() {
        final Map<String, String> result = new HashMap<String, String>();
        result.put("version", "2.0");
        return result;
    }

    @Override
    /**
     * @return The content type.
     */
    public String getContentType() {
        return "application/json; charset=" + this.getCharset();
    }

    /**
     * Creates an XML Layout.
     * 
     * @param locationInfo
     *            If "true", includes the location information in the generated JSON.
     * @param properties
     *            If "true", includes the thread context in the generated JSON.
     * @param completeStr
     *            If "true", includes the JSON header and footer, defaults to "false".
     * @param compactStr
     *            If "true", does not use end-of-lines and indentation, defaults to "false".
     * @param charsetName
     *            The character set to use, if {@code null}, uses "UTF-8".
     *            //TODO JER DOCUMENT NEW PARAMS
     * @return An XML Layout.
     */
    /**
     * 
     * @param locationInfo  If "true", includes the location information in the generated JSON.
     * @param properties If "true", includes the thread context in the generated JSON.
     * @param completeStr If "true", includes the JSON header and footer, defaults to "false".
     * @param compactStr  If "true", does not use end-of-lines and indentation, defaults to "false".
     * @param newlineStr  If "true", adds newline after each event, only applicable when compact=true, defaults to "false".
     * @param charsetName The character set to use, if {@code null}, uses "UTF-8", must be same as subLayout charset.
     * 
     * 
     * @param excludeLoggerStr If "true" excludes logger element, defaults to false;
     * @param excludeLevelStr If "true" excludes level element, defaults to false;
     * @param excludeThreadStr If "true" excludes thread element, defaults to false;
     * @param excludeMessageStr If "true" excludes message element, defaults to false;
     * @param excludeNDCStr If "true" excludes context stack aka NDC element, defaults to false;
     * @param excludeThrownStr If "true" excludes logger element, defaults to false;
     * @param skipJsonEscapeSubLayoutStr If "true" doesn't escape product of layout, 
     * 			only use if layout already produces escaped string. Defaults to false; 
     *  		Setting subLayoutAsElement to true implies skipJsonEscapeSubLayoutStr is "true" and overrides contradiction.
     * @param subLayoutAsElement If "true" doesn't escape product of layout and assumes product will be an element bracketed with curly braces, 
     * 			only use if layout already produces escaped string. Defaults to false;
     *  		Setting to true implies skipJsonEscapeSubLayoutStr is "true" and overrides contradiction.
     * @param subLayout If omitted uses default pattern layout
     * @return JSON layout of logevent, with modification to conform to logstash event json schema and element produced by subLayout
     */
    @PluginFactory
    public static LogStashJSONLayout createLayout(
            @PluginAttribute("locationInfo") final String locationInfo,
            @PluginAttribute("properties") final String properties, 
            @PluginAttribute("complete") final String completeStr,
            @PluginAttribute("compact") final String compactStr, 
            @PluginAttribute("newline") final String newlineStr,
            @PluginAttribute("charset") final String charsetName,
            
            @PluginAttribute("excludeLogger") final String excludeLoggerStr,
            @PluginAttribute("excludeLevel") final String excludeLevelStr,
            @PluginAttribute("excludeThread") final String excludeThreadStr,
            @PluginAttribute("excludeMessage") final String excludeMessageStr,

            @PluginAttribute("excludeNDC") final String excludeNDCStr,
            @PluginAttribute("excludeThrown") final String excludeThrownStr,
            
            
            @PluginAttribute("skipJsonEscapeSubLayout") final String skipJsonEscapeSubLayoutStr,
            @PluginAttribute("subLayoutAsElement") final String subLayoutAsElementStr,

            @PluginElement("Layout") Layout<? extends Serializable> subLayout,
            @PluginElement("Pairs") final KeyValuePair[] pairs

    		) {
        final Charset charset = Charsets.getSupportedCharset(charsetName, Charsets.UTF_8);
        final boolean info = Boolean.parseBoolean(locationInfo);
        final boolean props = Boolean.parseBoolean(properties);
        final boolean complete = Boolean.parseBoolean(completeStr);
        final boolean compact = Boolean.parseBoolean(compactStr);
        final boolean newline = Boolean.parseBoolean(newlineStr);

        final boolean excludeLogger = Boolean.parseBoolean(excludeLoggerStr);
        final boolean excludeLevel = Boolean.parseBoolean(excludeLevelStr);
        final boolean excludeThread = Boolean.parseBoolean(excludeThreadStr);
        final boolean excludeMessage = Boolean.parseBoolean(excludeMessageStr);
        final boolean excludeNDC = Boolean.parseBoolean(excludeNDCStr);
		final boolean excludeThrown = Boolean.parseBoolean(excludeThrownStr);
        final boolean jsonEscapeSubLayout = !Boolean.parseBoolean(skipJsonEscapeSubLayoutStr);
        final boolean subLayoutAsElement = Boolean.parseBoolean(subLayoutAsElementStr);
        
        //Note, FasterDateFormat does not support XXX timezone yet.
    	final DateFormat datePrinter = new SimpleDateFormat(LOG_STASH_ISO8601_TIMESTAMP_FORMAT);
    	 //when no layout is offered we'll go with the ol' favorite pattern 
        if (subLayout == null) {
        	subLayout = PatternLayout.createLayout(null, null, null, null, null, null);
        }
        
        //Unpacke the pairs list
        final Map<String, String> additionalLogAttributes = new HashMap<String, String>();
        if (pairs != null && pairs.length > 0) {
	        for (final KeyValuePair pair : pairs) {
	            final String key = pair.getKey();
	            if (key == null) {
	                LOGGER.error("A null key is not valid in MapFilter");
	                continue;
	            }
	            final String value = pair.getValue();
	            if (value == null) {
	                LOGGER.error("A null value for key " + key + " is not allowed in MapFilter");
	                continue;
	            }
	            if (additionalLogAttributes.containsKey(key)) {
	            	LOGGER.error("Duplicate entry for key: {} is forbidden!", key);
	            }
                additionalLogAttributes.put(key, value);
	        }
	        
	        
	        
        }
        return new LogStashJSONLayout(info, props, complete, compact, newline, charset,
        		excludeLogger, excludeLevel, excludeThread, excludeMessage,
    			excludeNDC,
    			excludeThrown,
        		jsonEscapeSubLayout, 
        		subLayoutAsElement,
        		datePrinter, 
        		subLayout,
        		additionalLogAttributes);
    }
    
   
  
}
