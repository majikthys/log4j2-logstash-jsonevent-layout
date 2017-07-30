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
package org.apache.logging.log4j.core.layout;


import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.DefaultConfiguration;
import org.apache.logging.log4j.core.LogStashLogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginConfiguration;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.util.KeyValuePair;
import org.apache.logging.log4j.util.Strings;
import org.apache.logging.log4j.util.SortedArrayStringMap;
/**
 * Copy Pasta version of JsonLayout that uses a different JSON writer which adds
 * required logstash "@version" and "@timestamp" fields to the default serialized form.
 *
 * @see org.apache.logging.log4j.core.layout.JsonLayout
 */
@Plugin(name = "LogStashJSONLayout", category = "Core", elementType = "layout", printObject = true)
public class LogStashJSONLayout extends AbstractJacksonLayout {

    private static final String DEFAULT_FOOTER = "]";

    private static final String DEFAULT_HEADER = "[";

    static final String CONTENT_TYPE = "application/json";

    private static final SortedArrayStringMap additionalLogAttributes = new SortedArrayStringMap();

    protected LogStashJSONLayout(final Configuration config, final boolean locationInfo, final boolean properties,
            final boolean encodeThreadContextAsList,
            final boolean complete, final boolean compact, final boolean eventEol, final String headerPattern,
            final String footerPattern, final Charset charset, final SortedArrayStringMap additionalLogAttributes,final boolean includeStackTrace) {
        super(config, new LogStashJacksonFactory.JSON(encodeThreadContextAsList,includeStackTrace).newWriter(locationInfo, properties, compact),
                charset, compact, complete, eventEol,
                PatternLayout.createSerializer(config, null, headerPattern, DEFAULT_HEADER, null, false, false),
                PatternLayout.createSerializer(config, null, footerPattern, DEFAULT_FOOTER, null, false, false));

        this.additionalLogAttributes.putAll(additionalLogAttributes);
    }

    /**
     * Returns appropriate JSON header.
     *
     * @return a byte array containing the header, opening the JSON array.
     */
    @Override
    public byte[] getHeader() {
        if (!this.complete) {
            return null;
        }
        final StringBuilder buf = new StringBuilder();
        final String str = serializeToString(getHeaderSerializer());
        if (str != null) {
            buf.append(str);
        }
        buf.append(this.eol);
        return getBytes(buf.toString());
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
        final StringBuilder buf = new StringBuilder();
        buf.append(this.eol);
        final String str = serializeToString(getFooterSerializer());
        if (str != null) {
            buf.append(str);
        }
        buf.append(this.eol);
        return getBytes(buf.toString());
    }

    @Override
    public Map<String, String> getContentFormat() {
        final Map<String, String> result = new HashMap<>();
        result.put("version", "2.0");
        return result;
    }

    @Override
    /**
     * @return The content type.
     */
    public String getContentType() {
        return CONTENT_TYPE + "; charset=" + this.getCharset();
    }

    /**
     * Creates a JSON Layout.
     * @param config
     *           The plugin configuration.
     * @param locationInfo
     *            If "true", includes the location information in the generated JSON.
     * @param properties
     *            If "true", includes the thread context map in the generated JSON.
     * @param propertiesAsList
     *            If true, the thread context map is included as a list of map entry objects, where each entry has
     *            a "key" attribute (whose value is the key) and a "value" attribute (whose value is the value).
     *            Defaults to false, in which case the thread context map is included as a simple map of key-value
     *            pairs.
     * @param complete
     *            If "true", includes the JSON header and footer, and comma between records.
     * @param compact
     *            If "true", does not use end-of-lines and indentation, defaults to "false".
     * @param eventEol
     *            If "true", forces an EOL after each log event (even if compact is "true"), defaults to "false". This
     *            allows one even per line, even in compact mode.
     * @param headerPattern
     *            The header pattern, defaults to {@code "["} if null.
     * @param footerPattern
     *            The header pattern, defaults to {@code "]"} if null.
     * @param charset
     *            The character set to use, if {@code null}, uses "UTF-8".
     * @param includeStacktrace
     *            If "true", includes the stacktrace of any Throwable in the generated JSON, defaults to "true".
     * @param pairs
     *            Additional KeyValue Pairs
     * @return A JSON Layout.
     */
    @PluginFactory
    public static AbstractJacksonLayout createLayout(
            // @formatter:off
            @PluginConfiguration final Configuration config,
            @PluginAttribute(value = "locationInfo", defaultBoolean = false) final boolean locationInfo,
            @PluginAttribute(value = "properties", defaultBoolean = false) final boolean properties,
            @PluginAttribute(value = "propertiesAsList", defaultBoolean = false) final boolean propertiesAsList,
            @PluginAttribute(value = "complete", defaultBoolean = false) final boolean complete,
            @PluginAttribute(value = "compact", defaultBoolean = false) final boolean compact,
            @PluginAttribute(value = "eventEol", defaultBoolean = false) final boolean eventEol,
            @PluginAttribute(value = "header", defaultString = DEFAULT_HEADER) final String headerPattern,
            @PluginAttribute(value = "footer", defaultString = DEFAULT_FOOTER) final String footerPattern,
            @PluginAttribute(value = "charset", defaultString = "UTF-8") final Charset charset,
            @PluginAttribute(value = "includeStacktrace", defaultBoolean = true) boolean includeStacktrace,
            @PluginElement("Pairs") final KeyValuePair[] pairs
            // @formatter:on
    ) {
        final boolean encodeThreadContextAsList = properties && propertiesAsList;

        //Unpacke the pairs list
        final SortedArrayStringMap additionalLogAttributes = new SortedArrayStringMap();
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
                additionalLogAttributes.putValue(key, value);
            }

        }

        return new LogStashJSONLayout(config, locationInfo, properties, encodeThreadContextAsList, complete, compact, eventEol,
                headerPattern, footerPattern, charset, additionalLogAttributes, includeStacktrace);
    }

    /**
     * Creates a JSON Layout using the default settings. Useful for testing.
     *
     * @return A JSON Layout.
     */
    public static AbstractJacksonLayout createDefaultLayout() {
        return new LogStashJSONLayout(new DefaultConfiguration(), false, false, false, false, false, false,
                DEFAULT_HEADER, DEFAULT_FOOTER, StandardCharsets.UTF_8, new SortedArrayStringMap(), true);
    }

    /**
     * Formats a {@link org.apache.logging.log4j.core.LogEvent}.
     *
     * @param event The LogEvent.
     * @return The XML representation of the LogEvent.
     */
    @Override
    public String toSerializable(final LogEvent event) {
        //convert event to a LogStashLogEvent, if needed, so that we know we can write to contextMap
        LogStashLogEvent logStashLogEvent;
        if (event instanceof LogStashLogEvent) {
            logStashLogEvent = (LogStashLogEvent) event;
        } else {
            logStashLogEvent = new LogStashLogEvent(event);
        }
        //SortedArrayStringMap data = new SortedArrayStringMap();
        ((SortedArrayStringMap)logStashLogEvent.getContextData()).putAll(additionalLogAttributes);

        try {
            return this.objectWriter.writeValueAsString(logStashLogEvent) + eol;
        } catch (final JsonProcessingException e) {
            // Should this be an ISE or IAE?
            LOGGER.error(e);
            return Strings.EMPTY;
        }
    }
}
