package org.apache.logging.log4j.core.layout;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.AppLog;
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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class CustomJSONLayoutJacksonUnitSpecs {
    public static final String LOCATION_INFO = "LocationInfo";
    private static Logger logger = LogManager.getLogger(CustomJSONLayoutJacksonUnitSpecs.class);


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
        Message simpleMessage = new SimpleMessage(new AppLog()
                .eventType("SomethingHappened")
                .eventSourceId("UUID-01")
                .metrics("name", "prayagupd")
                .metrics("timeTaken", 28)
                .metrics("unit", "millis").toJson());

        Map<String, String> mdc = new HashMap<String, String>();
        mdc.put("A", "B");//Already some threadcontext

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

        String expectedBasicSimpleTestJSON = "{\"@timestamp\":\"2017-05-07T01:15:14.397-07:00\",\"Foo\":\"Bar\",\"name\":\"urayagppd\",\"age\":\"1000\"}";

        //assertThat(actualJSON, expectedBasicSimpleTestJSON);

    }

    @Test
    public void hasLogMessageAsItIs() throws Exception {
        Exception exception = new Exception(new IOException(new IOException("something happened")));

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        exception.printStackTrace(printWriter);

        Message simpleMessage = new SimpleMessage(new AppLog()
                .put("name", "urayagppd")
                .put("error", stringWriter.toString()).toString());

        Map<String, String> mdc = new HashMap<String, String>();
        mdc.put("A", "B");//Already some threadcontext

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
        System.out.println("=========================");
        System.out.println("Actual = " + actualJSON);
        System.out.println("=========================");

//        assertThat(actualJSON, sameJSONAs("{\"version\":\"1\"," +
//                // "\"timestamp\":\"2015-07-28T11:31:18.492-07:00\",\"timeMillis\":1438108278492," +
//                "\"thread\":\""+ Thread.currentThread().getName() +"\"," +
//                "\"level\":\"DEBUG\"," +
//                "\"loggerName\":\"org.apache.logging.log4j.core.layout.CustomJSONLayoutJacksonUnitSpecs\"," +
//                "\"message\":\"key1=value1,key2=value2\"," +
//                "\"endOfBatch\":false," +
//                "\"loggerFqcn\":\"org.apache.logging.log4j.core.layout.CustomJSONLayoutJacksonUnitSpecs\","+
//                "\"contextMap\":[{\"key\":\"Foo\",\"value\":\"Bar\"},{\"key\":\"A\",\"value\":\"B\"}]}")
//                .allowingExtraUnexpectedFields()
//                .allowingAnyArrayOrdering());
    }

    @Test
    public void hasError() throws Exception {
        Message simpleMessage = new SimpleMessage(new AppLog().eventSourceId("sj").toJson());

        Map<String, String> mdc = new HashMap<String, String>();
        mdc.put("A", "B");//Already some threadcontext

        LogEvent event = new Log4jLogEvent(
                logger.getName(),
                null,
                this.getClass().getCanonicalName(),
                Level.ERROR,
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
//                "\"loggerName\":\"org.apache.logging.log4j.core.layout.CustomJSONLayoutJacksonUnitSpecs\"," +
//                "\"message\":\"key1=value1,key2=value2\"," +
//                "\"endOfBatch\":false," +
//                "\"loggerFqcn\":\"org.apache.logging.log4j.core.layout.CustomJSONLayoutJacksonUnitSpecs\","+
//                "\"contextMap\":[{\"key\":\"Foo\",\"value\":\"Bar\"},{\"key\":\"A\",\"value\":\"B\"}]}")
//                .allowingExtraUnexpectedFields()
//                .allowingAnyArrayOrdering());
    }

}
