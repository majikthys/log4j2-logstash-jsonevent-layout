/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache license, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the license for the specific language governing permissions and
 * limitations under the license.
 */
package net.logstash.logging.log4j2.core.layout;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.text.DateFormat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map.Entry;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.util.Charsets;
import org.apache.logging.log4j.core.util.KeyValuePair;
import org.apache.logging.log4j.core.util.Throwables;
import org.apache.logging.log4j.core.util.Transform;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.MultiformatMessage;

/**
 *
 * Appends a series of JSON events as strings serialized as bytes.
 *
 * <b>Complete well-formed JSON vs. fragment JSON</b>
 * <p>
 * If you configure {@code complete="true"}, the appender outputs a well-formed JSON document.
 * By default, with {@code complete="false"}, you should include the
 * output as an <em>external file</em> in a separate file to form a well-formed JSON document.
 * </p>
 * <p>
 * A {@code complete="true} {@code compact=false} well-formed JSON document follows this pattern:
 * </p>
 * <pre>[
 *{
 *               "@version" =&gt; "1",
 *             "@timestamp" =&gt; "2014-05-19T16:09:26.239-04:00",
 *                 "logger" =&gt; "com.liaison.service.resource.examples.LogStashExampleTest",
 *                  "level" =&gt; "ERROR",
 *                 "thread" =&gt; "Test worker",
 *                "message" =&gt; "Going on right here1",
 *           "LocationInfo" =&gt; {
 *         "class" =&gt; "com.liaison.service.resource.examples.LogStashExampleTest",
 *        "method" =&gt; "testLogStashLogs",
 *          "file" =&gt; "LogStashExampleTest.java",
 *          "line" =&gt; "16"
 *    },
 *                    "log" =&gt; "THIS BLOCK IS ARBITRARY FORMAT 13:09:26.239 [Test worker] ERROR com.liaison.service.resource.examples.LogStashExampleTest - Going on right here1",
 *       "environment_type" =&gt; "${sys:deploy_env}",
 *           "cluster_name" =&gt; "example cluster name",
 *       "cluster_location" =&gt; "${sys:cluster_location}",
 *       "application_name" =&gt; "${sys:application.name}",
 *       "application_user" =&gt; "jeremyfranklin-ross",
 *    "application_version" =&gt; "${sys:application.version}",
 *               "hostname" =&gt; "${sys:hostname}",
 *       "environment_user" =&gt; "jeremyfranklin-ross",
 *                "host_ip" =&gt; "${sys:host_ip}",
 *                   "host" =&gt; "10.10.87.16:54027"
 *},
 *{
 *               "@version" =&gt; "1",
 *             "@timestamp" =&gt; "2014-05-19T16:09:26.256-04:00",
 *                 "logger" =&gt; "com.liaison.service.resource.examples.LogStashExampleTest",
 *                  "level" =&gt; "ERROR",
 *                 "thread" =&gt; "Test worker",
 *                "message" =&gt; "Going on right here2",
 *           "LocationInfo" =&gt; {
 *         "class" =&gt; "com.liaison.service.resource.examples.LogStashExampleTest",
 *        "method" =&gt; "testLogStashLogs",
 *          "file" =&gt; "LogStashExampleTest.java",
 *          "line" =&gt; "18"
 *    },
 *                    "log" =&gt; "THIS BLOCK IS ARBITRARY FORMAT 13:09:26.256 [Test worker] ERROR com.liaison.service.resource.examples.LogStashExampleTest - Going on right here2",
 *       "environment_type" =&gt; "${sys:deploy_env}",
 *           "cluster_name" =&gt; "example cluster name",
 *       "cluster_location" =&gt; "${sys:cluster_location}",
 *       "application_name" =&gt; "${sys:application.name}",
 *       "application_user" =&gt; "jeremyfranklin-ross",
 *    "application_version" =&gt; "${sys:application.version}",
 *               "hostname" =&gt; "${sys:hostname}",
 *       "environment_user" =&gt; "jeremyfranklin-ross",
 *                "host_ip" =&gt; "${sys:host_ip}",
 *                   "host" =&gt; "10.10.87.16:54027"
 *}
 * ]</pre>
 * <p>
 * If {@code complete="false"}, the appender does not write the JSON open array character "[" at the start of the document.
 * and "]" and the end.
 * </p>
 * <p>
 * This approach enforces the independence of the JSONLayout and the appender where you embed it.
 * </p>
 * <b>Encoding</b>
 * <p>
 * Appenders using this layout should have their {@code charset} set to {@code UTF-8} or {@code UTF-16}, otherwise
 * events containing non ASCII characters could result in corrupted log files.
 * </p>
 * <b>Pretty vs. compact XML</b>
 * <p>
 * By default, the JSON layout is not compact (a.k.a. not "pretty") with {@code compact="false"}, which means the
 * appender uses end-of-line characters and indents lines to format the text. If {@code compact="true"}, then no
 * end-of-line or indentation is used. Message content may contain, of course, escaped end-of-lines.
 *
 *
 *
 * locationInfo  If "true", includes the location information in the generated JSON, defaults to true.
 * properties If "true", includes the thread context in the generated JSON, defaults to true.
 * complete If "true", includes the JSON header and footer, defaults to "false".
 *
 * compact If "true", does not use end-of-lines and indentation, defaults to "true".
 * newline If "true", adds newline after each event, only applicable when compact=true, defaults to "true".
 * commaAtEventEnd If "true", adds comma after event. If new line is also true, comma appears before newline. Defaults to "false"
 *
 * charset The character set to use, if {@code null}, uses "UTF-8", must be same as subLayout charset.
 *
 * excludeLogger If "true" excludes logger element, defaults to false;
 * excludeLevel If "true" excludes level element, defaults to false;
 * excludeThread If "true" excludes thread element, defaults to false;
 * excludeMessage If "true" excludes message element, defaults to false;
 * excludeLog If "true" excludes log element, defaults to false;
 * excludeNDC If "true" excludes context stack aka NDC element, defaults to false;
 * excludeThrown If "true" excludes logger element, defaults to false;
 * skipJsonEscapeSubLayout If "true" doesn't escape product of layout,
 * 			only use if layout already produces escaped ing. Defaults to false;
 *  		Setting subLayoutAsElement to true implies skipJsonEscapeSubLayout is "true" and overrides contradiction.
 * subLayoutAsElement If "true" doesn't escape product of layout and assumes product will be an element bracketed with curly braces,
 * 			only use if layout already produces escaped ing. Defaults to false;
 *  		Setting to true implies skipJsonEscapeSubLayout is "true" and overrides contradiction.
 * subLayout If omitted uses default pattern layout
 *
 *
 * </p>
 *
 * Code here derived from Apache's log4j2 JsonLayout {@link org.apache.logging.log4j.core.layout.JsonLayout}
 * and all licensing is carried forward.
 *
 * @author jeremyfranklin-ross
 *
 */

@Plugin(name = "LogStashJSONLayout", category = "Core", elementType = "layout", printObject = true)
public class LogStashJSONLayout extends AbstractStringLayout {
    private static final String COMMA = ",";
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
    private final String eventSeparator;
    private final String eventEnd;
    private volatile boolean firstLayoutDone;

    private final boolean excludeLogger;
    private final boolean excludeLevel;
    private final boolean excludeThread;
    private final boolean excludeMessage;
    private final boolean excludeLog;
    private final boolean excludeNDC;
    private final boolean excludeThrown;

    private final String subLayoutBegin;
    private final String subLayoutEnd;

    private final boolean jsonEscapeSubLayout;
    private final Map<String, String> additionalLogAttributes;

    private Layout<? extends Serializable> subLayout;

    /**
     *
     */
	protected LogStashJSONLayout(final boolean locationInfo,
			final boolean properties,
			final boolean complete,
			final boolean compact,
			final boolean newlineAtEventEnd,
			final boolean commaAtEventEnd,
            final Charset charset,
			final boolean excludeLogger,
			final boolean excludeLevel,
			final boolean excludeThread,
			final boolean excludeMessage,
			final boolean excludeLog,
			final boolean excludeNDC,
			final boolean excludeThrown,
			final boolean jsonEscapeSubLayout,
			final boolean subLayoutAsElement,
			final DateFormat iso8601DatePrinter,
			final Layout<? extends Serializable> subLayout,
			final Map<String, String> additionalLogAttributes) {

        super(charset);
        this.locationInfo = locationInfo;
        this.properties = properties;
        this.complete = complete;
        this.eol = compact ? COMPACT_EOL : DEFAULT_EOL;
        this.indent1 = compact ? COMPACT_INDENT : DEFAULT_INDENT;
        this.indent2 = this.indent1 + this.indent1;
        this.indent3 = this.indent2 + this.indent1;
        this.indent4 = this.indent3 + this.indent1;

        // Ahoy: Complications to pay attention to! eventSplit and eventEnd are
        // modal by complete, comma, and newline! This is because in the non-bracketed
        // list scenario, EOL is taken to be the termination of event.
        //
        //Complete means we're bracketing elements with open and close brackets.
        if (complete) {
        	//When complete we:
        	// 1: Render comma following all but last event
        	// 2: Render newline after commas if newlineAtEventEnd is or compact is false
        	// 3: Render no characters between final event and closing bracket
            this.eventSeparator = newlineAtEventEnd ? COMMA + DEFAULT_EOL : COMMA +  this.eol;
            this.eventEnd = "";
        } else {
        	//When complete is false we:
        	//1: Render after EVERY event if commaAtEventEnd is true
        	//2: Render newline after EVERY event (following comma if present) if
        	//   newlineAteventEnd is true or compact is false
        	if (newlineAtEventEnd) {
                this.eventEnd = commaAtEventEnd ? COMMA + DEFAULT_EOL : DEFAULT_EOL;
        	} else {
                this.eventEnd = commaAtEventEnd ? COMMA +  this.eol :  this.eol;
        	}
            this.eventSeparator = "";
        }

        this.excludeLogger= excludeLogger;
        this.excludeLevel = excludeLevel;
        this.excludeThread = excludeThread;
        this.excludeMessage = excludeMessage;
        this.excludeLog = excludeLog;
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
                    buf.append(this.eventSeparator);
                }
            }
        } else {
            buf.append(this.eventSeparator);
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
        buf.append(iso8601DateFormat.format(new Date(event.getTimeMillis())));
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
            buf.append(COMMA);
            buf.append(this.eol);
            buf.append(this.indent2);
            buf.append("\"ndc\":[");
            final ListIterator<String> ndc = event.getContextStack().asList().listIterator();
            buf.append(this.eol);
            buf.append(this.indent3);
            buf.append("\"");
            buf.append(Transform.escapeJsonControlCharacters(ndc.next()));
            buf.append("\"");
            while (ndc.hasNext()) {
                buf.append(COMMA);
                buf.append(this.eol);
                buf.append(this.indent3);
                buf.append("\"");
                buf.append(Transform.escapeJsonControlCharacters(ndc.next()));
                buf.append("\"");
            }
            buf.append(this.eol);
            buf.append(this.indent2);
            buf.append("]");
        }

        //Thrown
        final Throwable throwable = event.getThrown();
        if (!excludeThrown && throwable != null) {
            buf.append(COMMA);
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
        if (this.locationInfo && null != event.getSource()) {
            final StackTraceElement element = event.getSource();
            buf.append(COMMA);
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
            buf.append(COMMA);
            buf.append(this.eol);
            buf.append(this.indent2);
            buf.append("\"Properties\":{");
            buf.append(this.eol);
            final Iterator<Entry<String,String>> entrySet = event.getContextMap().entrySet().iterator();
            Map.Entry<String, String> entry = entrySet.next();
            buf.append(this.indent3);
            buf.append("\"");
            buf.append(Transform.escapeJsonControlCharacters(entry.getKey()));
            buf.append("\":\"");
            buf.append(Transform.escapeJsonControlCharacters(String.valueOf(entry.getValue())));
            buf.append("\"");

            while(entrySet.hasNext()) {
                entry = entrySet.next();
                buf.append(COMMA);
                buf.append(this.eol);
                buf.append(this.indent3);
                buf.append("\"");
                buf.append(Transform.escapeJsonControlCharacters(entry.getKey()));
                buf.append("\":\"");
                buf.append(Transform.escapeJsonControlCharacters(String.valueOf(entry.getValue())));
                buf.append("\"");
            }
            buf.append(this.eol);
            buf.append(this.indent2);
            buf.append("}");
        }

        //Log (the sublayout)
        if (!excludeLog) {
            buf.append(COMMA);
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
        }

        for (Entry<String,String> attributeEntry : additionalLogAttributes.entrySet()) {
            buf.append(COMMA);
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
        buf.append(this.eventEnd);

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
     * <p>
     * Key: "dtd" Value: "log4j-events.dtd"
     * </p><p>
     * Key: "version" Value: "2.0"
     * </p>
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
     * @param locationInfo  If "true", includes the location information in the generated JSON, defaults to true.
     * @param properties If "true", includes the thread context in the generated JSON, defaults to true.
     * @param completeStr If "true", includes the JSON header and footer, defaults to "false".
     *
     * @param compactStr  If "true", does not use end-of-lines and indentation, defaults to "true".
     * @param newlineStr  If "true", adds newline after each event, only applicable when compact=true, defaults to "true".
     * @param commaAtEventEndStr If "true", adds comma after event. If new line is also true, comma appears before newline. Defaults to "false"
     *
     * @param charsetName The character set to use, if {@code null}, uses "UTF-8", must be same as subLayout charset.
     *
     * @param excludeLoggerStr If "true" excludes logger element, defaults to false;
     * @param excludeLevelStr If "true" excludes level element, defaults to false;
     * @param excludeThreadStr If "true" excludes thread element, defaults to false;
     * @param excludeMessageStr If "true" excludes message element, defaults to false;
     * @param excludeLogStr If "true" excludes log element, defaults to false;
     * @param excludeNDCStr If "true" excludes context stack aka NDC element, defaults to false;
     * @param excludeThrownStr If "true" excludes logger element, defaults to false;
     * @param skipJsonEscapeSubLayoutStr If "true" doesn't escape product of layout,
     * 			only use if layout already produces escaped string. Defaults to false;
     *  		Setting subLayoutAsElement to true implies skipJsonEscapeSubLayoutStr is "true" and overrides contradiction.
     * @param subLayoutAsElementStr If "true" doesn't escape product of layout and assumes product will be an element bracketed with curly braces,
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
            @PluginAttribute("commaAtEventEnd") final String commaAtEventEndStr,

            @PluginAttribute("charset") final String charsetName,

            @PluginAttribute("excludeLogger") final String excludeLoggerStr,
            @PluginAttribute("excludeLevel") final String excludeLevelStr,
            @PluginAttribute("excludeThread") final String excludeThreadStr,

            @PluginAttribute("excludeMessage") final String excludeMessageStr,
            @PluginAttribute("excludeLog") final String excludeLogStr,

            @PluginAttribute("excludeNDC") final String excludeNDCStr,
            @PluginAttribute("excludeThrown") final String excludeThrownStr,


            @PluginAttribute("skipJsonEscapeSubLayout") final String skipJsonEscapeSubLayoutStr,
            @PluginAttribute("subLayoutAsElement") final String subLayoutAsElementStr,

            @PluginElement("Layout") Layout<? extends Serializable> subLayout,
            @PluginElement("Pairs") final KeyValuePair[] pairs

    		) {
        final Charset charset = Charsets.getSupportedCharset(charsetName, Charsets.UTF_8);


        final boolean info = (null == locationInfo) ? true : Boolean.parseBoolean(locationInfo);
        final boolean props = (null == properties) ? true: Boolean.parseBoolean(properties);
        final boolean complete = Boolean.parseBoolean(completeStr);

        final boolean compact = (null == compactStr) ? true : Boolean.parseBoolean(compactStr);
        final boolean newline = (null == newlineStr) ? true : Boolean.parseBoolean(newlineStr);

        final boolean commaAtEventEnd = Boolean.parseBoolean(commaAtEventEndStr);
        final boolean excludeLogger = Boolean.parseBoolean(excludeLoggerStr);
        final boolean excludeLevel = Boolean.parseBoolean(excludeLevelStr);
        final boolean excludeThread = Boolean.parseBoolean(excludeThreadStr);
        final boolean excludeMessage = Boolean.parseBoolean(excludeMessageStr);
        final boolean excludeLog = Boolean.parseBoolean(excludeLogStr);

        final boolean excludeNDC = Boolean.parseBoolean(excludeNDCStr);
		final boolean excludeThrown = Boolean.parseBoolean(excludeThrownStr);
        final boolean jsonEscapeSubLayout = !Boolean.parseBoolean(skipJsonEscapeSubLayoutStr);
        final boolean subLayoutAsElement = Boolean.parseBoolean(subLayoutAsElementStr);

        //Note, FasterDateFormat does not support XXX timezone yet.
    	final DateFormat datePrinter = new SimpleDateFormat(LOG_STASH_ISO8601_TIMESTAMP_FORMAT);
    	 //when no layout is offered we'll go with the ol' favorite pattern
        if (subLayout == null) {
        	subLayout = PatternLayout.createDefaultLayout();
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

        /**
         * Give some helpful feedback about funciton of configurations scenarios that may not be intuitive or
         * are internally contradictory
         */
        if (complete && !commaAtEventEnd) {
        	LOGGER.warn("Because complete is true commas will be present between each element even though commaAtEventEnd is false");
        }

        if (complete && !compact && !newline) {
        	LOGGER.warn("Because complete is true and compact is false, there will be newlines between each event element even through newline is false");
        }

        if (!complete && commaAtEventEnd && !newline) {
        	LOGGER.warn("comma will terminate every event element, including the last one (it does not necessarily signify another element will follow)");
        }

        if (!complete && commaAtEventEnd && newline) {
        	LOGGER.warn("comma + newline will terminate every event element, including the last one (it does not necessarily signify another element will follow)");
        }


        return new LogStashJSONLayout(info, props, complete,
        		compact, newline, commaAtEventEnd,
        		charset,
        		excludeLogger,
                excludeLevel,
                excludeThread,
                excludeMessage,
                excludeLog,
    			excludeNDC,
    			excludeThrown,
        		jsonEscapeSubLayout,
        		subLayoutAsElement,
        		datePrinter,
        		subLayout,
        		additionalLogAttributes);
    }



}
