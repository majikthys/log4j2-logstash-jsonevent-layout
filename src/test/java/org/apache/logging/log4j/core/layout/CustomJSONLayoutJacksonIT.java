package org.apache.logging.log4j.core.layout;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.DefaultConfiguration;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.core.util.KeyValuePair;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.SimpleMessage;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class CustomJSONLayoutJacksonIT {
    public static final String LOCATION_INFO = "LocationInfo";
    private static Logger logger = LogManager.getLogger(CustomJSONLayoutJacksonIT.class);


    @Test(enabled = false, dataProvider = "dp")
    public void f(Integer n, String s) {
    }

    @DataProvider
    public Object[][] dp() {
        return new Object[][]{
                new Object[]{1, "a"},
                new Object[]{2, "b"},
        };
    }

    @BeforeTest
    public void beforeTest() {
    }

    @AfterTest
    public void afterTest() {
    }


    ObjectMapper mapper = new ObjectMapper();

    @Test
    public void hasTimestampAndVersionInLogMessages() throws Exception {
        Message simpleMessage = new SimpleMessage("Test Message");

        Map<String,String>  mdc =     new HashMap<String,String>();
        mdc.put("A","B");//Already some threadcontext

        LogEvent event = new Log4jLogEvent(
                logger.getName(),
                null,
                this.getClass().getCanonicalName(),
                Level.DEBUG,
                simpleMessage,
                null,
                mdc,
                null,
                Thread.currentThread().getName(),
                null,
                System.currentTimeMillis()
                );


        AbstractJacksonLayout layout = CustomJSONLayout.createLayout(new DefaultConfiguration(),
                true, //location
                true, //properties
                true, //complete
                false, //compact
                false, //eventEol
                Charset.defaultCharset(),
                new KeyValuePair[]{new KeyValuePair("Foo", "Bar")}
        );

        String actualJSON = layout.toSerializable(event);
        System.out.println("Actual = "+actualJSON);

        String expectedBasicSimpleTestJSON = "{\"version\":\"1\"," +
                // "\"timestamp\":\"2015-07-28T11:31:18.492-07:00\",\"timeMillis\":1438108278492," +
                "\"thread\":\""+ Thread.currentThread().getName() +"\"," +
                "\"level\":\"DEBUG\"," +
                "\"loggerName\":\"org.apache.logging.log4j.core.layout.CustomJSONLayoutJacksonIT\"," +
                "\"message\":\"Test Message\"," +
                "\"endOfBatch\":false," +
                "\"loggerFqcn\":\"org.apache.logging.log4j.core.layout.CustomJSONLayoutJacksonIT\","+
                "\"contextMap\":[{\"key\":\"Foo\",\"value\":\"Bar\"},{\"key\":\"A\",\"value\":\"B\"}]}";
//
//        assertThat(actualJSON, sameJSONAs(expectedBasicSimpleTestJSON)
//                .allowingExtraUnexpectedFields()
//                .allowingAnyArrayOrdering());

    }

    @Test
    public void hasLogMessageAsItIs() throws Exception {
        Message simpleMessage = new SimpleMessage("key1=value1,key2=value2");

        Map<String,String>  mdc =     new HashMap<String,String>();
        mdc.put("A","B");//Already some threadcontext

        LogEvent event = new Log4jLogEvent(
                logger.getName(),
                null,
                this.getClass().getCanonicalName(),
                Level.DEBUG,
                simpleMessage,
                null,
                mdc,
                null,
                Thread.currentThread().getName(),
                null,
                System.currentTimeMillis()
        );


        AbstractJacksonLayout layout = CustomJSONLayout.createLayout(new DefaultConfiguration(),
                true, //location
                true, //properties
                true, //complete
                false, //compact
                false, //eventEol
                Charset.defaultCharset(),
                new KeyValuePair[]{new KeyValuePair("Foo", "Bar")}
        );

        String actualJSON = layout.toSerializable(event);
        System.out.println("Actual = " + actualJSON);

//        assertThat(actualJSON, sameJSONAs("{\"version\":\"1\"," +
//                // "\"timestamp\":\"2015-07-28T11:31:18.492-07:00\",\"timeMillis\":1438108278492," +
//                "\"thread\":\""+ Thread.currentThread().getName() +"\"," +
//                "\"level\":\"DEBUG\"," +
//                "\"loggerName\":\"org.apache.logging.log4j.core.layout.CustomJSONLayoutJacksonIT\"," +
//                "\"message\":\"key1=value1,key2=value2\"," +
//                "\"endOfBatch\":false," +
//                "\"loggerFqcn\":\"org.apache.logging.log4j.core.layout.CustomJSONLayoutJacksonIT\","+
//                "\"contextMap\":[{\"key\":\"Foo\",\"value\":\"Bar\"},{\"key\":\"A\",\"value\":\"B\"}]}")
//                .allowingExtraUnexpectedFields()
//                .allowingAnyArrayOrdering());
    }

}
