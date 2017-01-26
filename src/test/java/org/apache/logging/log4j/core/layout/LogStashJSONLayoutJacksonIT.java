package org.apache.logging.log4j.core.layout;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.core.util.KeyValuePair;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.SimpleMessage;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

public class LogStashJSONLayoutJacksonIT {
    public static final String LOCATION_INFO = "LocationInfo";
    private static Logger logger = LogManager.getLogger(LogStashJSONLayoutJacksonIT.class);


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


    String expectedBasicSimpleTestJSON = "{\"@version\":\"1\"," +
                   // "\"@timestamp\":\"2015-07-28T11:31:18.492-07:00\",\"timeMillis\":1438108278492," +
                    "\"thread\":\""+ Thread.currentThread().getName() +"\"," +
                    "\"level\":\"DEBUG\"," +
                    "\"loggerName\":\"org.apache.logging.log4j.core.layout.LogStashJSONLayoutJacksonIT\"," +
                    "\"message\":\"Test Message\"," +
                    "\"endOfBatch\":false," +
                    "\"loggerFqcn\":\"org.apache.logging.log4j.core.layout.LogStashJSONLayoutJacksonIT\","+
                    "\"contextMap\":[{\"key\":\"A\",\"value\":\"B\"}]," +
                    "\"Foo\":\"Bar\"," +
                    "\"threadId\":"+ Thread.currentThread().getId() +"," +
                    "\"threadPriority\":"+ Thread.currentThread().getPriority() +"}";

    @Test
    public void BasicSimpleTest() throws Exception {
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


        AbstractJacksonLayout layout = LogStashJSONLayout.createLayout(
                null, //config
                true, //location
                true, //properties
                true, //propertiesAsList
                true, //complete
                false, //compact
                false, //eventEol
                "[", //header
                "]", //footer
                Charset.defaultCharset(),
                new KeyValuePair[]{new KeyValuePair("Foo", "Bar")}
        );

        String actualJSON = layout.toSerializable(event);
        System.out.println(actualJSON);
        assertThat(actualJSON, sameJSONAs(expectedBasicSimpleTestJSON)
                .allowingExtraUnexpectedFields()
                .allowingAnyArrayOrdering());

    }

}
