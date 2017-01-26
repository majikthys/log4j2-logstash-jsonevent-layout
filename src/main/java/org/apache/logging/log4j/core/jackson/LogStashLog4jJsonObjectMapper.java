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

    public LogStashLog4jJsonObjectMapper(final boolean encodeThreadContextAsList) {
        this.registerModule(new LogStashLog4jJsonModule(encodeThreadContextAsList));
        this.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    }

}
