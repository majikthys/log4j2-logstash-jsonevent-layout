package net.logstash.logging.event;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonInclude(value=JsonInclude.Include.NON_EMPTY)
public abstract class LogStashEvent {

	/**
	 * @source: The source of the event which includes the plugin that generated
	 *          it and the hostname that produced it.
	 */
	protected String source = null;

	/**
	 * @tags: Tags on the event.
	 */
	protected List<String> tags = new ArrayList<String>();

	/**
	 * @fields: A set of fields.
	 */
	protected Map<String, Object> fields = new LinkedHashMap<String, Object>();

	/**
	 * @timestamp: An ISO8601 timestamp.
	 */
	protected String timestamp = null;

	/**
	 * @source_host: Host of the event.
	 */
	protected String source_host = null;

	/**
	 * @source_path: Path of source
	 */
	protected String source_path = null;

	/**
	 * @message: The message
	 */
	protected String message = null;

	/**
	 * @type: The value of the type configuration option we set.
	 */
	protected String type = null;

	@JsonProperty("@source")
	public String getSource() {
		return source;
	}

	protected void setSource(String source) {
		this.source = source;
	}

	@JsonProperty("@tags")
	public List<String> getTags() {
		return tags;
	}

	protected void addTags(String tag) {
		addTags(tag);
	}

	protected void addTags(String... tags) {
		if (tags != null && tags.length > 0) {
			this.tags.addAll(Arrays.asList(tags));
		}
	}

	@JsonProperty("@fields")
	public Map<String, Object> getFields() {
		return fields;
	}

	protected void addFields(String key, Object value) {
		this.fields.put(key, value);
	}

	@JsonProperty("@timestamp")
	public String getTimestamp() {
		return timestamp;
	}

	protected void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	@JsonProperty("@source_host")
	public String getSource_host() {
		return source_host;
	}

	protected void setSource_host(String source_host) {
		this.source_host = source_host;
	}

	@JsonProperty("@source_path")
	public String getSource_path() {
		return source_path;
	}

	protected void setSource_path(String source_path) {
		this.source_path = source_path;
	}

	@JsonProperty("@message")
	public String getMessage() {
		return message;
	}

	protected void setMessage(String message) {
		this.message = message;
	}

	@JsonProperty("@type")
	public String getType() {
		return type;
	}

	protected void setType(String type) {
		this.type = type;
	}
	
	public static final String LOG_STASH_ISO8601_TIMESTAMP_FORMAT ="yyyy-MM-dd'T'HH:mm:ss.SSSXXX";


	private static ObjectMapper mapper; 
	static {
		mapper = new ObjectMapper();
		mapper.setDateFormat(new SimpleDateFormat(LOG_STASH_ISO8601_TIMESTAMP_FORMAT));//Ahoy, X introduced in java 7
	}
	
	public static String marshallToJSON(LogStashEvent event) throws IOException {
		return marshallToJSON(event, false);
	}

	public static String marshallToJSON(LogStashEvent event, boolean prettyPrint) throws IOException {
		if (prettyPrint) {
			return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(event);
		} else {
			return mapper.writeValueAsString(event);
		}
	}
	
}
