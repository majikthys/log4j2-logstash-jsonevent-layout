package org.apache.logging.log4j.core.layout

import java.io.{IOException, PrintWriter, StringWriter}
import java.nio.charset.Charset
import java.util

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.logging.log4j.core.config.DefaultConfiguration
import org.apache.logging.log4j.core.impl.Log4jLogEvent
import org.apache.logging.log4j.core.util.KeyValuePair
import org.apache.logging.log4j.core.{AppLogEvent, LogEvent}
import org.apache.logging.log4j.message.{Message, SimpleMessage}
import org.apache.logging.log4j.{Level, LogManager, Logger}
import org.scalatest.testng.TestNGSuite
import org.testng.annotations.{AfterTest, BeforeTest, Test}


class JsonisedLogsSpecs extends TestNGSuite  {

  val LOCATION_INFO: String = "LocationInfo"
  private val logger: Logger = LogManager.getLogger(classOf[JsonisedLogsSpecs])

//  @Test(enabled = false, dataProvider = "dp")
//  def f(n: Integer, s: String): Unit = {
//    //no-ops
//  }
//
//  @DataProvider
//  def dp: Array[Array[AnyRef]] = Array[Array[AnyRef]](
//    Array[AnyRef](1, "a"),
//    Array[AnyRef](2, "b")
//  )

  @BeforeTest def beforeTest() {
  }

  @AfterTest def afterTest() {
  }

  val mapper: ObjectMapper = new ObjectMapper

  @Test
  @throws[Exception]
  def hasTimestampAndVersionInLogMessages() {
    val simpleMessage: Message = new SimpleMessage(new AppLogEvent().eventType("SomethingHappened")
      .eventSourceId("UUID-01").metrics("name", "prayagupd")
      .metrics("timeTaken", 28)
      .metrics("unit", "millis").toJson)

    val mdc: util.Map[String, String] = new util.HashMap[String, String]
    mdc.put("A", "B") //Already some threadcontext
    val event: LogEvent = new Log4jLogEvent(logger.getName, null, this.getClass.getCanonicalName,
      Level.DEBUG, simpleMessage, null, mdc, null, Thread.currentThread.getName, null, System.currentTimeMillis)
    val layout = CustomJSONLayout.createLayout(new DefaultConfiguration, true, //location
      true, //properties
      true, //complete
      false, //compact
      false, //eventEol
      Charset.defaultCharset, Array[KeyValuePair](new KeyValuePair("Foo", "Bar")))
    val actualJSON: String = layout.toSerializable(event)
    System.out.println("Actual = " + actualJSON)
    val expectedBasicSimpleTestJSON: String = "{\"@timestamp\":\"2017-05-07T01:15:14.397-07:00\",\"Foo\":\"Bar\",\"name\":\"urayagppd\",\"age\":\"1000\"}"
    //assertThat(actualJSON, expectedBasicSimpleTestJSON);
  }

  @Test
  @throws[Exception]
  def hasLogMessageAsItIs() {
    val exception: Exception = new Exception(new IOException(new IOException("something happened")))
    val stringWriter: StringWriter = new StringWriter
    val printWriter: PrintWriter = new PrintWriter(stringWriter)
    exception.printStackTrace(printWriter)

    val simpleMessage: Message = new SimpleMessage(new AppLogEvent()
      .metrics("name", "urayagppd")
      .metrics("error", stringWriter.toString).toString)

    val mdc: util.Map[String, String] = new util.HashMap[String, String]
    mdc.put("A", "B")
    val event: LogEvent = new Log4jLogEvent(logger.getName, null, this.getClass.getCanonicalName,
      Level.DEBUG, simpleMessage, null, mdc, null, Thread.currentThread.getName, null, System.currentTimeMillis)

    val layout = CustomJSONLayout.createLayout(new DefaultConfiguration, true, //location
      true, //properties
      true, //complete
      false, //compact
      false, //eventEol
      Charset.defaultCharset, Array[KeyValuePair](new KeyValuePair("Foo", "Bar")))

    val actualJSON: String = layout.toSerializable(event)

    System.out.println("=========================")
    System.out.println("Actual = " + actualJSON)
    System.out.println("=========================")
  }

  @Test
  @throws[Exception]
  def hasError() {
    val simpleMessage: Message = new SimpleMessage(new AppLogEvent().eventSourceId("sj").toJson)
    val mdc: util.Map[String, String] = new util.HashMap[String, String]
    mdc.put("A", "B")
    val event: LogEvent = new Log4jLogEvent(logger.getName, null, this.getClass.getCanonicalName,
      Level.ERROR, simpleMessage, null, mdc, null, Thread.currentThread.getName, null, System.currentTimeMillis)

    val layout = CustomJSONLayout.createLayout(new DefaultConfiguration, true, //location
      true, //properties
      true, //complete
      false, //compact
      false, //eventEol
      Charset.defaultCharset, Array[KeyValuePair](new KeyValuePair("Foo", "Bar")))
    val actualJSON: String = layout.toSerializable(event)

    System.out.println("Actual = " + actualJSON)
  }
}