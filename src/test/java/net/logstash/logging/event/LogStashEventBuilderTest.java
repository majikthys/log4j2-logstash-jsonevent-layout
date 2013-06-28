package net.logstash.logging.event;

import java.io.IOException;

import net.logstash.logging.event.LogStashEvent;
import net.logstash.logging.event.LogStashEventBuilder;
import net.logstash.logging.event.SimplifiedThrowable;

import org.apache.commons.lang3.RandomStringUtils;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.testng.annotations.DataProvider;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.CoreMatchers.*;

/**
 * 
 * @author jeremyfranklin-ross 
 *
 */
public class LogStashEventBuilderTest {
//TODO test ISO timezone has colon
//TODO test choosed right timezone
//TODO test event with null stuff
//TODO test/demo override of message handler
//TODO test handlemessage with all basic message types from log4j2
	
	LogStashEventBuilder eBuilder = new LogStashEventBuilder();

	@BeforeTest
	public void setUpTest() {
		eBuilder = new LogStashEventBuilder();
	}

	@Test(dataProvider = "dp", enabled = false)
	public void f(Integer n, String s) {
	}

	@DataProvider
	public Object[][] dp() {
		return new Object[][] { new Object[] { 1, "a" },
				new Object[] { 2, "b" }, };
	}

	@Test
	public void LogStashEventBuilder() {
		LogStashEventBuilder eBuilder = new LogStashEventBuilder();
		assertThat(eBuilder, isA(LogStashEventBuilder.class));

	}

	@Test(enabled = false)
	public void addField() {
		throw new RuntimeException("Test not implemented");
	}

	@Test(enabled = false)
	public void addTags() {
		throw new RuntimeException("Test not implemented");
	}

	@Test
  public void buildLogStashEventJSON() throws IOException {
	assertThat(eBuilder.buildLogStashEvent(), notNullValue());
	SimplifiedThrowable t = new SimplifiedThrowable();
	t.setException_class("esasda");
	t.setException_message("SAdas");
	t.setStacktrace("asdsa");
	String message = RandomStringUtils.randomAlphanumeric(22);
	eBuilder.setMessage(message);
	eBuilder.addField("throwable", t);
	String jsonStr = LogStashEvent.marshallToJSON(eBuilder.buildLogStashEvent());
	System.out.println("---------------");
	System.out.println("---------------");
	System.out.println("---------------");
	System.out.println("---------------");	
	System.out.println(jsonStr);
	System.out.println("---------------");
	System.out.println("---------------");
	System.out.println("---------------");
	System.out.println("---------------");



	
  }

	@Test(enabled = false)
	public void setTimestamp() {
		throw new RuntimeException("Test not implemented");
	}
}
