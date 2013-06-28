package net.logstash.logging.log4j2.core.layout;

import java.util.List;
/**
 * 
 * Convert from given objects to simplified DTO forms
 * 
 * @author jeremyfranklin-ross
 *
 */
public interface MessageObjectsHandler {
	public List<Object> handleMessageObjects(Object ... objects);	
}
