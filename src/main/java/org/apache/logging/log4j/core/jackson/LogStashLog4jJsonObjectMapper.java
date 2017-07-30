package org.apache.logging.log4j.core.jackson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Class cribbed from core Log4jJsonObjectMapper
 *
 * This merely registers LogStashLog4JsonModule
 *
 * Created by jeremyfranklin-ross on 7/27/15.
 */
public class LogStashLog4jJsonObjectMapper extends ObjectMapper {

    private static final long serialVersionUID = 1L;

    /**
     * Create a new instance using the {@link LogStashLog4jJsonModule}.
     */
    public LogStashLog4jJsonObjectMapper() {
        this(false, true);
    }

    /**
     * Create a new instance using the {@link LogStashLog4jJsonModule}.
     *
     * @param encodeThreadContextAsList
     *            when true, make ThreadContext map to be a list of map entries where each entry has a "key" attribute with the key value and a "value" attribute with the value value (old behavior), instead of "natural" JSON/YAML map
     * @param includeStackTrace
     *            If "true", includes the stacktrace of any Throwable in the generated JSON, defaults to "true".
     */
    public LogStashLog4jJsonObjectMapper(final boolean encodeThreadContextAsList, final boolean includeStackTrace) {
        this.registerModule(new LogStashLog4jJsonModule(encodeThreadContextAsList,includeStackTrace));
        this.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    }

}
