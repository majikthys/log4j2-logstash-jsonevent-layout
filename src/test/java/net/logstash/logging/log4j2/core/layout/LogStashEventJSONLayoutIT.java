package net.logstash.logging.log4j2.core.layout;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.MarkerManager;
import org.testng.annotations.Test;
import org.testng.annotations.DataProvider;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.AfterTest;

public class LogStashEventJSONLayoutIT {
	private static Logger logger = LogManager.getLogger(LogStashEventJSONLayoutIT.class);

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
