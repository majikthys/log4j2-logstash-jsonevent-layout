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
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.util.KeyValuePair;
import org.apache.logging.log4j.util.Strings;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Copy Pasta version of JsonLayout that uses a different JSON writer which adds
 * required logstash "@version" and "@timestamp" fields to the default serialized form.
 *
 * @see org.apache.logging.log4j.core.layout.JsonLayout
 */
@Plugin(name = "LogStashJSONLayout", category = "Core", elementType = "layout", printObject = true)
public class LogStashJSONLayout extends AbstractJacksonLayout {

    static final String CONTENT_TYPE = "application/json";

    private static final Map<String, String> additionalLogAttributes = new HashMap<String, String>();

    protected LogStashJSONLayout(final boolean locationInfo, final boolean properties, final boolean complete, final boolean compact,
                                 boolean eventEol, final Charset charset, final Map<String, String> additionalLogAttributes) {

        super(new LogStashJacksonFactory.JSON().newWriter(locationInfo, properties, compact), charset, compact, complete, eventEol);
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
        buf.append('[');
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
        return getBytes(this.eol + ']' + this.eol);
    }

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
        return CONTENT_TYPE + "; charset=" + this.getCharset();
    }

    /**
     * Creates a JSON Layout.
     *
     * @param locationInfo
     *        If "true", includes the location information in the generated JSON.
     * @param properties
     *        If "true", includes the thread context in the generated JSON.
     * @param complete
     *        If "true", includes the JSON header and footer, defaults to "false".
     * @param compact
     *        If "true", does not use end-of-lines and indentation, defaults to "false".
     * @param eventEol
     *        If "true", forces an EOL after each log event (even if compact is "true"), defaults to "false". This
     *        allows one even per line, even in compact mode.
     * @param charset
     *        The character set to use, if {@code null}, uses "UTF-8".
     * @return A JSON Layout.
     */
    @PluginFactory
    public static AbstractJacksonLayout createLayout(
            // @formatter:off
            @PluginAttribute(value = "locationInfo", defaultBoolean = false) final boolean locationInfo,
            @PluginAttribute(value = "properties", defaultBoolean = false) final boolean properties,
            @PluginAttribute(value = "complete", defaultBoolean = false) final boolean complete,
            @PluginAttribute(value = "compact", defaultBoolean = false) final boolean compact,
            @PluginAttribute(value = "eventEol", defaultBoolean = false) final boolean eventEol,
            @PluginAttribute(value = "charset", defaultString = "UTF-8") final Charset charset,
            @PluginElement("Pairs") final KeyValuePair[] pairs
            // @formatter:on
    ) {


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


        return new LogStashJSONLayout(locationInfo, properties, complete, compact, eventEol, charset, additionalLogAttributes);

    }

    /**
     * Creates a JSON Layout using the default settings.
     *
     * @return A JSON Layout.
     */
    public static AbstractJacksonLayout createDefaultLayout() {
        return new LogStashJSONLayout(false, false, false, false, false, UTF_8, new HashMap<>());
    }

    /**
     * Formats a {@link org.apache.logging.log4j.core.LogEvent}.
     *
     * @param event The LogEvent.
     * @return The XML representation of the LogEvent.
     */
    @Override
    public String toSerializable(final LogEvent event) {
        event.getContextMap().putAll(additionalLogAttributes);
        try {
            return this.objectWriter.writeValueAsString(event) + eol;
        } catch (final JsonProcessingException e) {
            // Should this be an ISE or IAE?
            LOGGER.error(e);
            return Strings.EMPTY;
        }
    }



}
