package net.logstash.logging.log4j2.core.layout;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.logging.log4j.*;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.SimpleMessage;
import org.testng.annotations.Test;
import org.testng.annotations.DataProvider;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.AfterTest;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.CoreMatchers.*;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.*;

public class LogStashJSONLayoutIT {
    public static final String LOCATION_INFO = "LocationInfo";
    private static Logger logger = LogManager.getLogger(LogStashJSONLayoutIT.class);


  @Test(enabled=false, dataProvider = "dp")
  public void f(Integer n, String s) {
  }

  @DataProvider
  public Object[][] dp() {
    return new Object[][] {
      new Object[] { 1, "a" },
      new Object[] { 2, "b" },
    };
  }
  @BeforeTest
  public void beforeTest() {
  }

  @AfterTest
  public void afterTest() {
  }

  
  ObjectMapper mapper = new ObjectMapper();

  String expectedBasicSimpleTestJSON = "{" +
            "\"@version\":\"1\"," +
            // REMOVE timestamp b/c it'll alwayhs be wrong "\"@timestamp\":\"2014-10-03T09:58:03.391-07:00\"," +
            "\"logger\":\"net.logstash.logging.log4j2.core.layout.LogStashJSONLayoutIT\"," +
            "\"level\":\"DEBUG\"," +
            "\"thread\":\"Test worker\"," +
            "\"message\":\"Test Message\"," +
            "\"log\":\"Test Message\\n\"}";
  
  @Test
  public void BasicSimpleTest() throws JsonParseException, JsonMappingException, IOException{
	  Message simpleMessage = new SimpleMessage("Test Message");
	  LogEvent event = new Log4jLogEvent(logger.getName(),
              null,
              this.getClass().getCanonicalName(),
              Level.DEBUG,
              simpleMessage,
              null);


      LogStashJSONLayout layout = LogStashJSONLayout.createLayout(
              null, //      @PluginAttribute("locationInfo") final String locationInfo,
              null, //      @PluginAttribute("properties") final String properties,
              null, //      @PluginAttribute("complete") final String completeStr,
              null, //      @PluginAttribute("compact") final String compactStr,
              null, //      @PluginAttribute("newline") final String newlineStr,
              null, //      @PluginAttribute("commaAtEventEnd") final String commaAtEventEndStr,
              null, //      @PluginAttribute("charset") final String charsetName,
              null, //      @PluginAttribute("excludeLogger") final String excludeLoggerStr,
              null, //      @PluginAttribute("excludeLevel") final String excludeLevelStr,
              null, //      @PluginAttribute("excludeThread") final String excludeThreadStr,
              null, //      @PluginAttribute("excludeMessage") final String excludeMessageStr,
              null, //      @PluginAttribute("excludeLog") final String excludeLogStr,              null, //      @PluginAttribute("excludeLog") final String excludeLogStr,
              null, //      @PluginAttribute("excludeNDC") final String excludeNDCStr,
              null, //      @PluginAttribute("excludeThrown") final String excludeThrownStr,
              null, //      @PluginAttribute("skipJsonEscapeSubLayout") final String skipJsonEscapeSubLayoutStr,
              null, //      @PluginAttribute("subLayoutAsElement") final String subLayoutAsElementStr,
              null, //      @PluginElement("Layout") Layout<? extends Serializable> subLayout,
              null  //      @PluginElement("Pairs") final KeyValuePair[] pairs
      );

	  String actualJSON = layout.toSerializable(event);

      assertThat(actualJSON, sameJSONAs(expectedBasicSimpleTestJSON)
              .allowingExtraUnexpectedFields()
              .allowingAnyArrayOrdering());

  }

@SuppressWarnings({ "rawtypes", "unchecked" })
@Test
public void TestThrowable() throws IOException {
	  Message simpleMessage = new SimpleMessage("Throw an exception");

	  Throwable nestedThrowable = null;
	  try {
		@SuppressWarnings("unused")
		int divide_by_zero = 12/0;
	  } catch (RuntimeException r) {
		  try {
			  throw new IOException("Test IO Exception", r);
		  }	catch (IOException e) {
			  nestedThrowable = new RuntimeException("Runtime Exception", e);
		  }
	  }

	  LogEvent event = new Log4jLogEvent(logger.getName(),
            null,
            this.getClass().getCanonicalName(),
            Level.DEBUG,
            simpleMessage,
            nestedThrowable);

    LogStashJSONLayout layout = LogStashJSONLayout.createLayout(
            null, //      @PluginAttribute("locationInfo") final String locationInfo,
            null, //      @PluginAttribute("properties") final String properties,
            null, //      @PluginAttribute("complete") final String completeStr,
            null, //      @PluginAttribute("compact") final String compactStr,
            null, //      @PluginAttribute("newline") final String newlineStr,
            null, //      @PluginAttribute("commaAtEventEnd") final String commaAtEventEndStr,
            null, //      @PluginAttribute("charset") final String charsetName,
            null, //      @PluginAttribute("excludeLogger") final String excludeLoggerStr,
            null, //      @PluginAttribute("excludeLevel") final String excludeLevelStr,
            null, //      @PluginAttribute("excludeThread") final String excludeThreadStr,
            null, //      @PluginAttribute("excludeMessage") final String excludeMessageStr,
            null, //      @PluginAttribute("excludeLog") final String excludeLogStr,
            null, //      @PluginAttribute("excludeNDC") final String excludeNDCStr,
            null, //      @PluginAttribute("excludeThrown") final String excludeThrownStr,
            null, //      @PluginAttribute("skipJsonEscapeSubLayout") final String skipJsonEscapeSubLayoutStr,
            null, //      @PluginAttribute("subLayoutAsElement") final String subLayoutAsElementStr,
            null, //      @PluginElement("Layout") Layout<? extends Serializable> subLayout,
            null  //      @PluginElement("Pairs") final KeyValuePair[] pairs
    );

    String layoutJSON = layout.toSerializable(event);

    ObjectNode resultLayout = mapper.readValue(layoutJSON, ObjectNode.class);

    assertThat(resultLayout.has(KEY_THROWABLE), is(true));
    assertThat(resultLayout.get(KEY_THROWABLE).asText(), containsString("/ by zero"));
    assertThat(resultLayout.get(KEY_THROWABLE).asText(), startsWith("java.lang.RuntimeException: Runtime Exception"));

}

    private final static String KEY_THROWABLE = "throwable";
    private final static Map <String,String> MDC = new HashMap<String,String>();

    static {
        MDC.put("CKey1", "CVal1");
        MDC.put("CKey2", "CVal2");
        MDC.put("CKey3", "CVal3");
    }

    private final static Date LOG_DATE = new Date();
    private final static Throwable THROWABLE = null;

    private final static String TEST_THREAD_NAME = "threadname.foo1";

    private final static StackTraceElement STACK_TRACE_ELEMENT = new StackTraceElement("FakeClass", "isFakeMethod", "/fake/file.java", 66);
    private final static String EXPECTED_LOCATION_INFO = "{\"class\":\"FakeClass\",\"method\":\"isFakeMethod\",\"file\":\"/fake/file.java\",\"line\":\"66\"}";

    // ---------------------------------------------------
    // ---------------------------------------------------
    // Below, test to make sure configuration works
    // ---------------------------------------------------
    // ---------------------------------------------------

    @Test
    public void testPluginAttributePropertiesDefault() throws IOException {
        LogStashJSONLayout layout = LogStashJSONLayout.createLayout(
                null, //      @PluginAttribute("locationInfo") final String locationInfo,
                null, // defaultTrue     @PluginAttribute("properties") final String properties,
                null, //      @PluginAttribute("complete") final String completeStr,
                null, //      @PluginAttribute("compact") final String compactStr,
                null, //      @PluginAttribute("newline") final String newlineStr,
                null, //      @PluginAttribute("commaAtEventEnd") final String commaAtEventEndStr,
                null, //      @PluginAttribute("charset") final String charsetName,
                null, //      @PluginAttribute("excludeLogger") final String excludeLoggerStr,
                null, //      @PluginAttribute("excludeLevel") final String excludeLevelStr,
                null, //      @PluginAttribute("excludeThread") final String excludeThreadStr,
                null, //      @PluginAttribute("excludeMessage") final String excludeMessageStr,
                null, //      @PluginAttribute("excludeLog") final String excludeLogStr,
                null, //      @PluginAttribute("excludeNDC") final String excludeNDCStr,
                null, //      @PluginAttribute("excludeThrown") final String excludeThrownStr,
                null, //      @PluginAttribute("skipJsonEscapeSubLayout") final String skipJsonEscapeSubLayoutStr,
                null, //      @PluginAttribute("subLayoutAsElement") final String subLayoutAsElementStr,
                null, //      @PluginElement("Layout") Layout<? extends Serializable> subLayout,
                null  //      @PluginElement("Pairs") final KeyValuePair[] pairs
        );

        Message simpleMessage = new SimpleMessage("testPluginAttributeLocationInfo");


        LogEvent event = new Log4jLogEvent(logger.getName(),
                null, // final Marker marker,
                this.getClass().getCanonicalName(), //final String loggerFQCN,
                Level.DEBUG, //final Level level,
                simpleMessage, //final Message message,
                THROWABLE, //final Throwable t,
                MDC, //final Map<String, String> mdc,
                null, //final ThreadContext.ContextStack ndc,
                TEST_THREAD_NAME, //final String threadName,
                null, // final StackTraceElement location,
                LOG_DATE.getTime()); //final long timestamp);


        String actualJSON = layout.toSerializable(event);

        ObjectNode resultLayout = mapper.readValue(actualJSON, ObjectNode.class);

        String expectedJSON = "{" +
                "\"@version\":\"1\"," +
                // REMOVE timestamp b/c it'll alwayhs be wrong "\"@timestamp\":\"2014-10-03T09:58:03.391-07:00\"," +
                "\"logger\":\"net.logstash.logging.log4j2.core.layout.LogStashJSONLayoutIT\"," +
                "\"level\":\"DEBUG\"," +
                "\"thread\":\"threadname.foo1\"," +
                "\"message\":\"testPluginAttributeLocationInfo\"," +
                "\"Properties\":{\"CKey3\":\"CVal3\",\"CKey2\":\"CVal2\",\"CKey1\":\"CVal1\"}," +
                "\"log\":\"testPluginAttributeLocationInfo\\n\"}";

        System.out.println("--- testPluginAttributeLocationInfo ---");
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(resultLayout));
        System.out.println(mapper.writer().writeValueAsString(resultLayout.get("Properties")));
        System.out.println("--- testPluginAttributeLocationInfo ---");


        assertThat(actualJSON, sameJSONAs(expectedJSON)
                .allowingExtraUnexpectedFields()
                .allowingAnyArrayOrdering());


    }


    @Test
    public void testPluginAttributePropertiesFalse() throws IOException {
        LogStashJSONLayout layout = LogStashJSONLayout.createLayout(
                null, //      @PluginAttribute("locationInfo") final String locationInfo,
                "false", // defaultTrue     @PluginAttribute("properties") final String properties,
                null, //      @PluginAttribute("complete") final String completeStr,
                null, //      @PluginAttribute("compact") final String compactStr,
                null, //      @PluginAttribute("newline") final String newlineStr,
                null, //      @PluginAttribute("commaAtEventEnd") final String commaAtEventEndStr,
                null, //      @PluginAttribute("charset") final String charsetName,
                null, //      @PluginAttribute("excludeLogger") final String excludeLoggerStr,
                null, //      @PluginAttribute("excludeLevel") final String excludeLevelStr,
                null, //      @PluginAttribute("excludeThread") final String excludeThreadStr,
                null, //      @PluginAttribute("excludeMessage") final String excludeMessageStr,
                null, //      @PluginAttribute("excludeLog") final String excludeLogStr,
                null, //      @PluginAttribute("excludeNDC") final String excludeNDCStr,
                null, //      @PluginAttribute("excludeThrown") final String excludeThrownStr,
                null, //      @PluginAttribute("skipJsonEscapeSubLayout") final String skipJsonEscapeSubLayoutStr,
                null, //      @PluginAttribute("subLayoutAsElement") final String subLayoutAsElementStr,
                null, //      @PluginElement("Layout") Layout<? extends Serializable> subLayout,
                null  //      @PluginElement("Pairs") final KeyValuePair[] pairs
        );

        Message simpleMessage = new SimpleMessage("testPluginAttributeLocationInfo");


        LogEvent event = new Log4jLogEvent(logger.getName(),
                null, // final Marker marker,
                this.getClass().getCanonicalName(), //final String loggerFQCN,
                Level.DEBUG, //final Level level,
                simpleMessage, //final Message message,
                THROWABLE, //final Throwable t,
                MDC, //final Map<String, String> mdc,
                null, //final ThreadContext.ContextStack ndc,
                TEST_THREAD_NAME, //final String threadName,
                null, // final StackTraceElement location,
                LOG_DATE.getTime()); //final long timestamp);


        String actualJSON = layout.toSerializable(event);
        ObjectNode resultLayout = mapper.readValue(actualJSON, ObjectNode.class);

        assertThat(resultLayout.has("Properties"), is(false));


    }


    @Test
    public void testPluginAttributeLocationInfoDefault() throws IOException {
        LogStashJSONLayout layout = LogStashJSONLayout.createLayout(
                null, //   true by default   @PluginAttribute("locationInfo") final String locationInfo,
                null, //      @PluginAttribute("properties") final String properties,
                null, //      @PluginAttribute("complete") final String completeStr,
                null, //      @PluginAttribute("compact") final String compactStr,
                null, //      @PluginAttribute("newline") final String newlineStr,
                null, //      @PluginAttribute("commaAtEventEnd") final String commaAtEventEndStr,
                null, //      @PluginAttribute("charset") final String charsetName,
                null, //      @PluginAttribute("excludeLogger") final String excludeLoggerStr,
                null, //      @PluginAttribute("excludeLevel") final String excludeLevelStr,
                null, //      @PluginAttribute("excludeThread") final String excludeThreadStr,
                null, //      @PluginAttribute("excludeMessage") final String excludeMessageStr,
                null, //      @PluginAttribute("excludeLog") final String excludeLogStr,
                null, //      @PluginAttribute("excludeNDC") final String excludeNDCStr,
                null, //      @PluginAttribute("excludeThrown") final String excludeThrownStr,
                null, //      @PluginAttribute("skipJsonEscapeSubLayout") final String skipJsonEscapeSubLayoutStr,
                null, //      @PluginAttribute("subLayoutAsElement") final String subLayoutAsElementStr,
                null, //      @PluginElement("Layout") Layout<? extends Serializable> subLayout,
                null  //      @PluginElement("Pairs") final KeyValuePair[] pairs
        );

        Message simpleMessage = new SimpleMessage("testPluginAttributeLocationInfo");


        LogEvent event = new Log4jLogEvent(logger.getName(),
                null, // final Marker marker,
                this.getClass().getCanonicalName(), //final String loggerFQCN,
                Level.DEBUG, //final Level level,
                simpleMessage, //final Message message,
                THROWABLE, //final Throwable t,
                null, //final Map<String, String> mdc,
                null, //final ThreadContext.ContextStack ndc,
                TEST_THREAD_NAME, //final String threadName,
                STACK_TRACE_ELEMENT, // final StackTraceElement location,
                LOG_DATE.getTime()); //final long timestamp);

        String actualJSON = layout.toSerializable(event);
        ObjectNode resultLayout = mapper.readValue(actualJSON, ObjectNode.class);
//
//        System.out.println("++++++++++++++++++++++++++++++++++");
//        System.out.println(actualJSON);
//        System.out.println("++++++++++++++++++++++++++++++++++");
//

        assertThat(mapper.writeValueAsString(resultLayout.get(LOCATION_INFO)), sameJSONAs(EXPECTED_LOCATION_INFO)
                .allowingAnyArrayOrdering());

//
//        assertThat(resultLayout.has("Properties"), is(false));


    }

    @Test
    public void testPluginAttributeLocationInfoFalse() throws IOException {
        LogStashJSONLayout layout = LogStashJSONLayout.createLayout(
                "false", //   true by default   @PluginAttribute("locationInfo") final String locationInfo,
                null, //      @PluginAttribute("properties") final String properties,
                null, //      @PluginAttribute("complete") final String completeStr,
                null, //      @PluginAttribute("compact") final String compactStr,
                null, //      @PluginAttribute("newline") final String newlineStr,
                null, //      @PluginAttribute("commaAtEventEnd") final String commaAtEventEndStr,
                null, //      @PluginAttribute("charset") final String charsetName,
                null, //      @PluginAttribute("excludeLogger") final String excludeLoggerStr,
                null, //      @PluginAttribute("excludeLevel") final String excludeLevelStr,
                null, //      @PluginAttribute("excludeThread") final String excludeThreadStr,
                null, //      @PluginAttribute("excludeMessage") final String excludeMessageStr,
                null, //      @PluginAttribute("excludeLog") final String excludeLogStr,
                null, //      @PluginAttribute("excludeNDC") final String excludeNDCStr,
                null, //      @PluginAttribute("excludeThrown") final String excludeThrownStr,
                null, //      @PluginAttribute("skipJsonEscapeSubLayout") final String skipJsonEscapeSubLayoutStr,
                null, //      @PluginAttribute("subLayoutAsElement") final String subLayoutAsElementStr,
                null, //      @PluginElement("Layout") Layout<? extends Serializable> subLayout,
                null  //      @PluginElement("Pairs") final KeyValuePair[] pairs
        );

        Message simpleMessage = new SimpleMessage("testPluginAttributeLocationInfo");


        LogEvent event = new Log4jLogEvent(logger.getName(),
                null, // final Marker marker,
                this.getClass().getCanonicalName(), //final String loggerFQCN,
                Level.DEBUG, //final Level level,
                simpleMessage, //final Message message,
                THROWABLE, //final Throwable t,
                null, //final Map<String, String> mdc,
                null, //final ThreadContext.ContextStack ndc,
                TEST_THREAD_NAME, //final String threadName,
                STACK_TRACE_ELEMENT, // final StackTraceElement location,
                LOG_DATE.getTime()); //final long timestamp);

        String actualJSON = layout.toSerializable(event);
        ObjectNode resultLayout = mapper.readValue(actualJSON, ObjectNode.class);


        assertThat(resultLayout.has("Properties"), is(false));


    }

    @Test
    public void testPluginAttributeComplete() {
        LogStashJSONLayout layout = LogStashJSONLayout.createLayout(
                null, //      @PluginAttribute("locationInfo") final String locationInfo,
                null, //      @PluginAttribute("properties") final String properties,
                null, //      @PluginAttribute("complete") final String completeStr,
                null, //      @PluginAttribute("compact") final String compactStr,
                null, //      @PluginAttribute("newline") final String newlineStr,
                null, //      @PluginAttribute("commaAtEventEnd") final String commaAtEventEndStr,
                null, //      @PluginAttribute("charset") final String charsetName,
                null, //      @PluginAttribute("excludeLogger") final String excludeLoggerStr,
                null, //      @PluginAttribute("excludeLevel") final String excludeLevelStr,
                null, //      @PluginAttribute("excludeThread") final String excludeThreadStr,
                null, //      @PluginAttribute("excludeMessage") final String excludeMessageStr,
                null, //      @PluginAttribute("excludeLog") final String excludeLogStr,
                null, //      @PluginAttribute("excludeNDC") final String excludeNDCStr,
                null, //      @PluginAttribute("excludeThrown") final String excludeThrownStr,
                null, //      @PluginAttribute("skipJsonEscapeSubLayout") final String skipJsonEscapeSubLayoutStr,
                null, //      @PluginAttribute("subLayoutAsElement") final String subLayoutAsElementStr,
                null, //      @PluginElement("Layout") Layout<? extends Serializable> subLayout,
                null  //      @PluginElement("Pairs") final KeyValuePair[] pairs
        );
    }

    @Test
    public void testPluginAttributeCompact() {
        LogStashJSONLayout layout = LogStashJSONLayout.createLayout(
                null, //      @PluginAttribute("locationInfo") final String locationInfo,
                null, //      @PluginAttribute("properties") final String properties,
                null, //      @PluginAttribute("complete") final String completeStr,
                null, //      @PluginAttribute("compact") final String compactStr,
                null, //      @PluginAttribute("newline") final String newlineStr,
                null, //      @PluginAttribute("commaAtEventEnd") final String commaAtEventEndStr,
                null, //      @PluginAttribute("charset") final String charsetName,
                null, //      @PluginAttribute("excludeLogger") final String excludeLoggerStr,
                null, //      @PluginAttribute("excludeLevel") final String excludeLevelStr,
                null, //      @PluginAttribute("excludeThread") final String excludeThreadStr,
                null, //      @PluginAttribute("excludeMessage") final String excludeMessageStr,
                null, //      @PluginAttribute("excludeLog") final String excludeLogStr,
                null, //      @PluginAttribute("excludeNDC") final String excludeNDCStr,
                null, //      @PluginAttribute("excludeThrown") final String excludeThrownStr,
                null, //      @PluginAttribute("skipJsonEscapeSubLayout") final String skipJsonEscapeSubLayoutStr,
                null, //      @PluginAttribute("subLayoutAsElement") final String subLayoutAsElementStr,
                null, //      @PluginElement("Layout") Layout<? extends Serializable> subLayout,
                null  //      @PluginElement("Pairs") final KeyValuePair[] pairs
        );
    }

    @Test
    public void testPluginAttributeNewLine() {
        LogStashJSONLayout layout = LogStashJSONLayout.createLayout(
                null, //      @PluginAttribute("locationInfo") final String locationInfo,
                null, //      @PluginAttribute("properties") final String properties,
                null, //      @PluginAttribute("complete") final String completeStr,
                null, //      @PluginAttribute("compact") final String compactStr,
                null, //      @PluginAttribute("newline") final String newlineStr,
                null, //      @PluginAttribute("commaAtEventEnd") final String commaAtEventEndStr,
                null, //      @PluginAttribute("charset") final String charsetName,
                null, //      @PluginAttribute("excludeLogger") final String excludeLoggerStr,
                null, //      @PluginAttribute("excludeLevel") final String excludeLevelStr,
                null, //      @PluginAttribute("excludeThread") final String excludeThreadStr,
                null, //      @PluginAttribute("excludeMessage") final String excludeMessageStr,
                null, //      @PluginAttribute("excludeLog") final String excludeLogStr,
                null, //      @PluginAttribute("excludeNDC") final String excludeNDCStr,
                null, //      @PluginAttribute("excludeThrown") final String excludeThrownStr,
                null, //      @PluginAttribute("skipJsonEscapeSubLayout") final String skipJsonEscapeSubLayoutStr,
                null, //      @PluginAttribute("subLayoutAsElement") final String subLayoutAsElementStr,
                null, //      @PluginElement("Layout") Layout<? extends Serializable> subLayout,
                null  //      @PluginElement("Pairs") final KeyValuePair[] pairs
        );
    }

    @Test
    public void testPluginAttributeCommaAtEventEnd() {
        LogStashJSONLayout layout = LogStashJSONLayout.createLayout(
                null, //      @PluginAttribute("locationInfo") final String locationInfo,
                null, //      @PluginAttribute("properties") final String properties,
                null, //      @PluginAttribute("complete") final String completeStr,
                null, //      @PluginAttribute("compact") final String compactStr,
                null, //      @PluginAttribute("newline") final String newlineStr,
                null, //      @PluginAttribute("commaAtEventEnd") final String commaAtEventEndStr,
                null, //      @PluginAttribute("charset") final String charsetName,
                null, //      @PluginAttribute("excludeLogger") final String excludeLoggerStr,
                null, //      @PluginAttribute("excludeLevel") final String excludeLevelStr,
                null, //      @PluginAttribute("excludeThread") final String excludeThreadStr,
                null, //      @PluginAttribute("excludeMessage") final String excludeMessageStr,
                null, //      @PluginAttribute("excludeLog") final String excludeLogStr,
                null, //      @PluginAttribute("excludeNDC") final String excludeNDCStr,
                null, //      @PluginAttribute("excludeThrown") final String excludeThrownStr,
                null, //      @PluginAttribute("skipJsonEscapeSubLayout") final String skipJsonEscapeSubLayoutStr,
                null, //      @PluginAttribute("subLayoutAsElement") final String subLayoutAsElementStr,
                null, //      @PluginElement("Layout") Layout<? extends Serializable> subLayout,
                null  //      @PluginElement("Pairs") final KeyValuePair[] pairs
        );
    }

    @Test
    public void testPluginAttributeCharSet() {
        LogStashJSONLayout layout = LogStashJSONLayout.createLayout(
                null, //      @PluginAttribute("locationInfo") final String locationInfo,
                null, //      @PluginAttribute("properties") final String properties,
                null, //      @PluginAttribute("complete") final String completeStr,
                null, //      @PluginAttribute("compact") final String compactStr,
                null, //      @PluginAttribute("newline") final String newlineStr,
                null, //      @PluginAttribute("commaAtEventEnd") final String commaAtEventEndStr,
                null, //      @PluginAttribute("charset") final String charsetName,
                null, //      @PluginAttribute("excludeLogger") final String excludeLoggerStr,
                null, //      @PluginAttribute("excludeLevel") final String excludeLevelStr,
                null, //      @PluginAttribute("excludeThread") final String excludeThreadStr,
                null, //      @PluginAttribute("excludeMessage") final String excludeMessageStr,
                null, //      @PluginAttribute("excludeLog") final String excludeLogStr,
                null, //      @PluginAttribute("excludeNDC") final String excludeNDCStr,
                null, //      @PluginAttribute("excludeThrown") final String excludeThrownStr,
                null, //      @PluginAttribute("skipJsonEscapeSubLayout") final String skipJsonEscapeSubLayoutStr,
                null, //      @PluginAttribute("subLayoutAsElement") final String subLayoutAsElementStr,
                null, //      @PluginElement("Layout") Layout<? extends Serializable> subLayout,
                null  //      @PluginElement("Pairs") final KeyValuePair[] pairs
        );
    }

    @Test
    public void testPluginAttributeExcludeLogger() {
        LogStashJSONLayout layout = LogStashJSONLayout.createLayout(
                null, //      @PluginAttribute("locationInfo") final String locationInfo,
                null, //      @PluginAttribute("properties") final String properties,
                null, //      @PluginAttribute("complete") final String completeStr,
                null, //      @PluginAttribute("compact") final String compactStr,
                null, //      @PluginAttribute("newline") final String newlineStr,
                null, //      @PluginAttribute("commaAtEventEnd") final String commaAtEventEndStr,
                null, //      @PluginAttribute("charset") final String charsetName,
                null, //      @PluginAttribute("excludeLogger") final String excludeLoggerStr,
                null, //      @PluginAttribute("excludeLevel") final String excludeLevelStr,
                null, //      @PluginAttribute("excludeThread") final String excludeThreadStr,
                null, //      @PluginAttribute("excludeMessage") final String excludeMessageStr,
                null, //      @PluginAttribute("excludeLog") final String excludeLogStr,
                null, //      @PluginAttribute("excludeNDC") final String excludeNDCStr,
                null, //      @PluginAttribute("excludeThrown") final String excludeThrownStr,
                null, //      @PluginAttribute("skipJsonEscapeSubLayout") final String skipJsonEscapeSubLayoutStr,
                null, //      @PluginAttribute("subLayoutAsElement") final String subLayoutAsElementStr,
                null, //      @PluginElement("Layout") Layout<? extends Serializable> subLayout,
                null  //      @PluginElement("Pairs") final KeyValuePair[] pairs
        );
    }

    @Test
    public void testPluginAttributeExcludeLevel() {
        LogStashJSONLayout layout = LogStashJSONLayout.createLayout(
                null, //      @PluginAttribute("locationInfo") final String locationInfo,
                null, //      @PluginAttribute("properties") final String properties,
                null, //      @PluginAttribute("complete") final String completeStr,
                null, //      @PluginAttribute("compact") final String compactStr,
                null, //      @PluginAttribute("newline") final String newlineStr,
                null, //      @PluginAttribute("commaAtEventEnd") final String commaAtEventEndStr,
                null, //      @PluginAttribute("charset") final String charsetName,
                null, //      @PluginAttribute("excludeLogger") final String excludeLoggerStr,
                null, //      @PluginAttribute("excludeLevel") final String excludeLevelStr,
                null, //      @PluginAttribute("excludeThread") final String excludeThreadStr,
                null, //      @PluginAttribute("excludeMessage") final String excludeMessageStr,
                null, //      @PluginAttribute("excludeLog") final String excludeLogStr,
                null, //      @PluginAttribute("excludeNDC") final String excludeNDCStr,
                null, //      @PluginAttribute("excludeThrown") final String excludeThrownStr,
                null, //      @PluginAttribute("skipJsonEscapeSubLayout") final String skipJsonEscapeSubLayoutStr,
                null, //      @PluginAttribute("subLayoutAsElement") final String subLayoutAsElementStr,
                null, //      @PluginElement("Layout") Layout<? extends Serializable> subLayout,
                null  //      @PluginElement("Pairs") final KeyValuePair[] pairs
        );
    }

    @Test
    public void testPluginAttributeExcludeThread() {
        LogStashJSONLayout layout = LogStashJSONLayout.createLayout(
                null, //      @PluginAttribute("locationInfo") final String locationInfo,
                null, //      @PluginAttribute("properties") final String properties,
                null, //      @PluginAttribute("complete") final String completeStr,
                null, //      @PluginAttribute("compact") final String compactStr,
                null, //      @PluginAttribute("newline") final String newlineStr,
                null, //      @PluginAttribute("commaAtEventEnd") final String commaAtEventEndStr,
                null, //      @PluginAttribute("charset") final String charsetName,
                null, //      @PluginAttribute("excludeLogger") final String excludeLoggerStr,
                null, //      @PluginAttribute("excludeLevel") final String excludeLevelStr,
                null, //      @PluginAttribute("excludeThread") final String excludeThreadStr,
                null, //      @PluginAttribute("excludeMessage") final String excludeMessageStr,
                null, //      @PluginAttribute("excludeLog") final String excludeLogStr,
                null, //      @PluginAttribute("excludeNDC") final String excludeNDCStr,
                null, //      @PluginAttribute("excludeThrown") final String excludeThrownStr,
                null, //      @PluginAttribute("skipJsonEscapeSubLayout") final String skipJsonEscapeSubLayoutStr,
                null, //      @PluginAttribute("subLayoutAsElement") final String subLayoutAsElementStr,
                null, //      @PluginElement("Layout") Layout<? extends Serializable> subLayout,
                null  //      @PluginElement("Pairs") final KeyValuePair[] pairs
        );

    }

    @Test
    public void testPluginAttributeMessage() {
        LogStashJSONLayout layout = LogStashJSONLayout.createLayout(
                null, //      @PluginAttribute("locationInfo") final String locationInfo,
                null, //      @PluginAttribute("properties") final String properties,
                null, //      @PluginAttribute("complete") final String completeStr,
                null, //      @PluginAttribute("compact") final String compactStr,
                null, //      @PluginAttribute("newline") final String newlineStr,
                null, //      @PluginAttribute("commaAtEventEnd") final String commaAtEventEndStr,
                null, //      @PluginAttribute("charset") final String charsetName,
                null, //      @PluginAttribute("excludeLogger") final String excludeLoggerStr,
                null, //      @PluginAttribute("excludeLevel") final String excludeLevelStr,
                null, //      @PluginAttribute("excludeThread") final String excludeThreadStr,
                null, //      @PluginAttribute("excludeMessage") final String excludeMessageStr,
                null, //      @PluginAttribute("excludeLog") final String excludeLogStr,
                null, //      @PluginAttribute("excludeNDC") final String excludeNDCStr,
                null, //      @PluginAttribute("excludeThrown") final String excludeThrownStr,
                null, //      @PluginAttribute("skipJsonEscapeSubLayout") final String skipJsonEscapeSubLayoutStr,
                null, //      @PluginAttribute("subLayoutAsElement") final String subLayoutAsElementStr,
                null, //      @PluginElement("Layout") Layout<? extends Serializable> subLayout,
                null  //      @PluginElement("Pairs") final KeyValuePair[] pairs
        );

    }
    @Test
    public void testPluginAttributeNDC() {
        LogStashJSONLayout layout = LogStashJSONLayout.createLayout(
                null, //      @PluginAttribute("locationInfo") final String locationInfo,
                null, //      @PluginAttribute("properties") final String properties,
                null, //      @PluginAttribute("complete") final String completeStr,
                null, //      @PluginAttribute("compact") final String compactStr,
                null, //      @PluginAttribute("newline") final String newlineStr,
                null, //      @PluginAttribute("commaAtEventEnd") final String commaAtEventEndStr,
                null, //      @PluginAttribute("charset") final String charsetName,
                null, //      @PluginAttribute("excludeLogger") final String excludeLoggerStr,
                null, //      @PluginAttribute("excludeLevel") final String excludeLevelStr,
                null, //      @PluginAttribute("excludeThread") final String excludeThreadStr,
                null, //      @PluginAttribute("excludeMessage") final String excludeMessageStr,
                null, //      @PluginAttribute("excludeLog") final String excludeLogStr,
                null, //      @PluginAttribute("excludeNDC") final String excludeNDCStr,
                null, //      @PluginAttribute("excludeThrown") final String excludeThrownStr,
                null, //      @PluginAttribute("skipJsonEscapeSubLayout") final String skipJsonEscapeSubLayoutStr,
                null, //      @PluginAttribute("subLayoutAsElement") final String subLayoutAsElementStr,
                null, //      @PluginElement("Layout") Layout<? extends Serializable> subLayout,
                null  //      @PluginElement("Pairs") final KeyValuePair[] pairs
        );

    }
    @Test
    public void testPluginAttributeThrown() {
        LogStashJSONLayout layout = LogStashJSONLayout.createLayout(
                null, //      @PluginAttribute("locationInfo") final String locationInfo,
                null, //      @PluginAttribute("properties") final String properties,
                null, //      @PluginAttribute("complete") final String completeStr,
                null, //      @PluginAttribute("compact") final String compactStr,
                null, //      @PluginAttribute("newline") final String newlineStr,
                null, //      @PluginAttribute("commaAtEventEnd") final String commaAtEventEndStr,
                null, //      @PluginAttribute("charset") final String charsetName,
                null, //      @PluginAttribute("excludeLogger") final String excludeLoggerStr,
                null, //      @PluginAttribute("excludeLevel") final String excludeLevelStr,
                null, //      @PluginAttribute("excludeThread") final String excludeThreadStr,
                null, //      @PluginAttribute("excludeMessage") final String excludeMessageStr,
                null, //      @PluginAttribute("excludeLog") final String excludeLogStr,
                null, //      @PluginAttribute("excludeNDC") final String excludeNDCStr,
                null, //      @PluginAttribute("excludeThrown") final String excludeThrownStr,
                null, //      @PluginAttribute("skipJsonEscapeSubLayout") final String skipJsonEscapeSubLayoutStr,
                null, //      @PluginAttribute("subLayoutAsElement") final String subLayoutAsElementStr,
                null, //      @PluginElement("Layout") Layout<? extends Serializable> subLayout,
                null  //      @PluginElement("Pairs") final KeyValuePair[] pairs
        );

    }
    @Test
    public void testPluginAttributeSkipJSONSublayout() {
        LogStashJSONLayout layout = LogStashJSONLayout.createLayout(
                null, //      @PluginAttribute("locationInfo") final String locationInfo,
                null, //      @PluginAttribute("properties") final String properties,
                null, //      @PluginAttribute("complete") final String completeStr,
                null, //      @PluginAttribute("compact") final String compactStr,
                null, //      @PluginAttribute("newline") final String newlineStr,
                null, //      @PluginAttribute("commaAtEventEnd") final String commaAtEventEndStr,
                null, //      @PluginAttribute("charset") final String charsetName,
                null, //      @PluginAttribute("excludeLogger") final String excludeLoggerStr,
                null, //      @PluginAttribute("excludeLevel") final String excludeLevelStr,
                null, //      @PluginAttribute("excludeThread") final String excludeThreadStr,
                null, //      @PluginAttribute("excludeMessage") final String excludeMessageStr,
                null, //      @PluginAttribute("excludeLog") final String excludeLogStr,
                null, //      @PluginAttribute("excludeNDC") final String excludeNDCStr,
                null, //      @PluginAttribute("excludeThrown") final String excludeThrownStr,
                null, //      @PluginAttribute("skipJsonEscapeSubLayout") final String skipJsonEscapeSubLayoutStr,
                null, //      @PluginAttribute("subLayoutAsElement") final String subLayoutAsElementStr,
                null, //      @PluginElement("Layout") Layout<? extends Serializable> subLayout,
                null  //      @PluginElement("Pairs") final KeyValuePair[] pairs
        );

    }
    @Test
    public void testPluginAttributeLayout() {
        LogStashJSONLayout layout = LogStashJSONLayout.createLayout(
                null, //      @PluginAttribute("locationInfo") final String locationInfo,
                null, //      @PluginAttribute("properties") final String properties,
                null, //      @PluginAttribute("complete") final String completeStr,
                null, //      @PluginAttribute("compact") final String compactStr,
                null, //      @PluginAttribute("newline") final String newlineStr,
                null, //      @PluginAttribute("commaAtEventEnd") final String commaAtEventEndStr,
                null, //      @PluginAttribute("charset") final String charsetName,
                null, //      @PluginAttribute("excludeLogger") final String excludeLoggerStr,
                null, //      @PluginAttribute("excludeLevel") final String excludeLevelStr,
                null, //      @PluginAttribute("excludeThread") final String excludeThreadStr,
                null, //      @PluginAttribute("excludeMessage") final String excludeMessageStr,
                null, //      @PluginAttribute("excludeLog") final String excludeLogStr,
                null, //      @PluginAttribute("excludeNDC") final String excludeNDCStr,
                null, //      @PluginAttribute("excludeThrown") final String excludeThrownStr,
                null, //      @PluginAttribute("skipJsonEscapeSubLayout") final String skipJsonEscapeSubLayoutStr,
                null, //      @PluginAttribute("subLayoutAsElement") final String subLayoutAsElementStr,
                null, //      @PluginElement("Layout") Layout<? extends Serializable> subLayout,
                null  //      @PluginElement("Pairs") final KeyValuePair[] pairs
        );
    }
    @Test
    public void testPluginAttributePairs() {
        LogStashJSONLayout layout = LogStashJSONLayout.createLayout(
                null, //      @PluginAttribute("locationInfo") final String locationInfo,
                null, //      @PluginAttribute("properties") final String properties,
                null, //      @PluginAttribute("complete") final String completeStr,
                null, //      @PluginAttribute("compact") final String compactStr,
                null, //      @PluginAttribute("newline") final String newlineStr,
                null, //      @PluginAttribute("commaAtEventEnd") final String commaAtEventEndStr,
                null, //      @PluginAttribute("charset") final String charsetName,
                null, //      @PluginAttribute("excludeLogger") final String excludeLoggerStr,
                null, //      @PluginAttribute("excludeLevel") final String excludeLevelStr,
                null, //      @PluginAttribute("excludeThread") final String excludeThreadStr,
                null, //      @PluginAttribute("excludeMessage") final String excludeMessageStr,
                null, //      @PluginAttribute("excludeLog") final String excludeLogStr,
                null, //      @PluginAttribute("excludeNDC") final String excludeNDCStr,
                null, //      @PluginAttribute("excludeThrown") final String excludeThrownStr,
                null, //      @PluginAttribute("skipJsonEscapeSubLayout") final String skipJsonEscapeSubLayoutStr,
                null, //      @PluginAttribute("subLayoutAsElement") final String subLayoutAsElementStr,
                null, //      @PluginElement("Layout") Layout<? extends Serializable> subLayout,
                null  //      @PluginElement("Pairs") final KeyValuePair[] pairs
        );

    }









  //TODO test variety of objects
  
  //TODO test all message types
 
  //TODO test markers
  
  //TODO demo expansion scenarios like audit log marker 
  
  //TODO demo parameter object like audit
  
  //TODO test context 
  

  
  /**
   * This test requires logstash (installed manually) and makes assumptions
   * about both configuration and operating system...
   * 
   * ... So, you probably ought not run it by default :)
   */
  @Test(groups="integration")
  public void LogToLogStashTest() {
	 System.out.println("&&&&&&&&&&&&&&&&&&&");
	 System.out.println("&&&&&&&&&&&&&&&&&&&");
	 System.out.println("&&&&&&&&&&&&&&&&&&&");
	 System.out.println("&&&&&&&&&&&&&&&&&&&");
	 logger.info("TEST IS WIRED");
	  
  }
  
}
