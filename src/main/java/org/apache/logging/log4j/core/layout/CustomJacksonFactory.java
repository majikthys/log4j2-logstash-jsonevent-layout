package org.apache.logging.log4j.core.layout;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.core.jackson.CustomLog4jJsonObjectMapper;

import java.util.HashSet;
import java.util.Set;

/**
 * Cribbed from log4j core JacksonFactory, this merely introduces CustomLog4jJsonObjectMapper
 *
 * Created by jeremyfranklin-ross on 7/27/15.
 */
abstract  class CustomJacksonFactory extends JacksonFactory {
        static class JSON extends JacksonFactory.JSON {

            public JSON(boolean encodeThreadContextAsList, boolean stackTrace) {
                super(encodeThreadContextAsList, stackTrace);
            }

            @Override
            protected ObjectMapper newObjectMapper() {
                return new CustomLog4jJsonObjectMapper();
            }

        }

    ObjectWriter newWriter(final boolean locationInfo, final boolean properties, final boolean compact) {
        final SimpleFilterProvider filters = new SimpleFilterProvider();
        final Set<String> except = new HashSet<String>(2);
        if (!locationInfo) {
            except.add(this.getPropertNameForSource());
        }
        if (!properties) {
            except.add(this.getPropertNameForContextMap());
        }
        filters.addFilter(Log4jLogEvent.class.getName(), SimpleBeanPropertyFilter.serializeAllExcept(except));
        final ObjectWriter writer = this.newObjectMapper().writer(compact ? this.newCompactPrinter() : this.newPrettyPrinter());
        return writer.with(filters);
    }

}
