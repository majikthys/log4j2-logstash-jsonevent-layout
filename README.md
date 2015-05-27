log4j2-logstash-jsonevent-layout
================================

Log4J2 Layout as a Logstash "json_event"

Note: Currently alpha: under active development and review. Input and contributions most welcome!

# Overview

This is a log4j2 layout that produces json that is compliant to logstash v1 spec. JSON produced is a serialization of a given log4j2 LogEvent and is intentionally very similar to that produced by the default log4j2 [JSONLayout](http://logging.apache.org/log4j/2.x/manual/layouts.html). You may use this layout out of the box to connect your java application to a logstash server with maximal speed and minimal redundant processing.

This layout is fast, flexible, and other superlatives starting with f. 

(see http://logging.apache.org/log4j/2.x/manual/layouts.html and  http://logstash.net/) 

# Download
Sorry! Until we have a maven repository you'll have to build it yourself! I'm working on this presently!

# Getting Started

You'll need an application that uses [log4j2](http://logging.apache.org/) and an install of [logstash](http://logstash.net/). Explaining these perquisites is an exercise left for the reader, but we'll provide you a configuration sample.

(see http://logging.apache.org/ and http://logstash.net/)

## Simple Configuration: Log4j2 SocketAppender to Logstash TCP Input

This is the simplest form of the intended use. Once you have this configuration working, you should be able to customize the configuration to your specific needs.

Log4j2 is configured to connect to logstash via a TCP by using a standard SocketAppender with our LogStashJSONLayout in order to produce logstash v1 spec json. Logstash is configure to receive TCP connections and expect logstash v1 spec json. 


Example Log4j2 log4j2.xml:

    <?xml version="1.0" encoding="UTF-8"?>
    <configuration status="DEBUG" packages="net.logstash.logging.log4j2.core.layout" verbose="false">
       <appenders>
          <!-- logstash tcp stocket example, replace host value -->
          <Socket name="LogStashSocket" host="REPLACE_HOST_NAME" port="4560" protocol="tcp">
    	      <LogStashJSONLayout>
    
    			<!-- Provide ANY type of layout to expose the message, being mindful that if -->
    			<!-- subLayoutAsElement="true", your (likely custom) Layout will need to     -->
    			<!-- produce a valid and escaped json element -->
    			<PatternLayout pattern="THIS BLOCK IS ARBITRARY FORMAT %d{HH:mm:ss.SSS} [%t] %level %logger{36} - %msg"/>
    			
    			<!-- Example of what you might do to add fields, warning values should be known to be json escaped strings -->
    		    <KeyValuePair key="application_name" value="${sys:application.name}"/>
    		    <KeyValuePair key="application_version" value="${sys:application.version}"/>
    		    <KeyValuePair key="environment_type" value="${sys:deploy_env}"/>
    		    <KeyValuePair key="cluster_location" value="${sys:cluster_location}"/>
    		    <KeyValuePair key="cluster_name" value="${sys:cluster_name}"/>
    		    <KeyValuePair key="hostname" value="${sys:hostname}"/>
    		    <KeyValuePair key="host_ip" value="${sys:host_ip}"/>
    	
    		    <!--Example of using system property substitution -->
    		    <KeyValuePair key="application_user" value="${sys:user.name}"/>
    		    
    		    <!--Example of using environment property substitution  env:USERNAME on windows-->
    		    <KeyValuePair key="environment_user" value="${env:USER}"/> 
      	      </LogStashJSONLayout>    	  
    	  </Socket>
       </appenders>
       
       <loggers>
          <root level="DEBUG">
             <!-- Example of logstash json layout and configuration -->
             <appender-ref ref="LogStashSocket" />   
          </root>
       </loggers>
    </configuration>


Example logstash configuration (later we refer to this as file tcp-logstash.conf):

    input {
      tcp {
        codec => json_lines { charset => "UTF-8" }
        # 4560 is default log4j socket appender port
        port => 4560
      }
    }
    output {
      stdout { codec => rubydebug }
    }
    

Note, this example configuration expects port 4560 to be accessible, so make sure your firewall rules allow for this. You may of course opt to change the port to a value of your choosing, just make sure to update both logstash and log4j2 configuration.
Note, tcp input has buffer underrun and overrun conditions that prevent use with non-line delineated codecs and therefore json codec does not work.

### Example output
You should see in your logstash console a message like:

    {
                   "@version" => "1",
                 "@timestamp" => "2014-04-29T14:21:14.988-07:00",
                     "logger" => "com.liaison.service.resource.examples.LogStashExampleTest",
                      "level" => "ERROR",
                     "thread" => "Test worker",
                    "message" => "Going on right here",
               "LocationInfo" => {
             "class" => "com.liaison.service.resource.examples.LogStashExampleTest",
            "method" => "testLogStashLogs",
              "file" => "LogStashExampleTest.java",
              "line" => "15"
        },
                        "log" => "THIS BLOCK IS ARBITRARY FORMAT 14:21:14.988 [Test worker] ERROR com.liaison.service.resource.examples.LogStashExampleTest - Going on right here",
           "environment_type" => "${sys:deploy_env}",
               "cluster_name" => "example cluster name",
           "cluster_location" => "${sys:cluster_location}",
           "application_name" => "${sys:application.name}",
           "application_user" => "jeremyfranklin-ross",
        "application_version" => "${sys:application.version}",
                   "hostname" => "${sys:hostname}",
           "environment_user" => "jeremyfranklin-ross",
                    "host_ip" => "${sys:host_ip}",
                       "host" => "10.211.55.2:53051"
    }

#### Look out for malformed messages

Something is wrong with your configuration if you see something like the above but escaped and wedged into the message element, example:

    {
           "message" => "{\"@version\":\"1\",\"@timestamp\":\"2014-04-29T16:21:03.554-07:00\",\"logger\":\"com.liaison.service.resource.examples.LogStashExampleTest\",\"level\":\"ERROR\",\"thread\":\"Test worker\",\"message\":\"Going on right here\",\"LocationInfo\":{\"class\":\"com.liaison.service.resource.examples.LogStashExampleTest\",\"method\":\"testLogStashLogs\",\"file\":\"LogStashExampleTest.java\",\"line\":\"15\"},\"log\":\"THIS BLOCK IS ARBITRARY FORMAT 16:21:03.554 [Test worker] ERROR com.liaison.service.resource.examples.LogStashExampleTest - Going on right here\",\"environment_type\":\"${sys:deploy_env}\",\"cluster_name\":\"example cluster name\",\"cluster_location\":\"${sys:cluster_location}\",\"application_name\":\"${sys:application.name}\",\"application_user\":\"jeremyfranklin-ross\",\"application_version\":\"${sys:application.version}\",\"hostname\":\"${sys:hostname}\",\"environment_user\":\"jeremyfranklin-ross\",\"host_ip\":\"${sys:host_ip}\"}\r",
          "@version" => "1",
        "@timestamp" => "2014-04-29T23:21:03.099Z",
              "host" => "10.211.55.2:53807"
    }

If you see this behavior it either you have specified the wrong codec in your logstash conf or log4j2 layout is producing malformed json (typically due to incorrect parameters set in log4j2 or unescaped json characters in keypair values).


## Log4j2 Logstash Layout In More Detail

### Logstash LogEvent JSON Layout:
Conceptually this is a mashup between log4j2 logevent json schema and logstash event schema. The only required elements are @version and @timestamp which, as you'll note, is in logstash's native format so no modification needs to happen here. 
 
#### Log4j2 LogEvent Elements

Log4j2 LogEvent elements have been expanded and a means to omit/include via configuration included. Those elements are:

 * logger: logger name 
 * level: log level
 * thread: thread name
 * message: log message
 * ndc: the thread context stack
 * throwable: the thrown exception
 * LocationInfo (element): where the log originated
 * Properties (element): thread context map
 * log : product of sublayout, element or attribute (see below)
 * * : anything... see discussion on Arbitrary KeyValues
 
#### SubLayout:
Central to this implementation is the facility to use any layout as a sublayout, which is demonstrated by a patternlayout in the log4j2.xml example, above. By default, a sublayout will render as a string value, which will then be json escaped. It is possible to configure this so that it will skip the escaping, which you would do as a speed enhancement if you had a custom (sub)layout that provided pre-escaped content. It's also possible, in the realm custom layouts, to make a sublayout that provides a json element, in which case you can configure Log4j2 Logstash Layout to treat the sublayout as a element node. 

** Most likely, for your purposes the pattern logger will be sufficient as a sublayout. **
 
#### Arbitrary Key Values Pairs
This is a very important mechanism that allows you to include arbitrary key values. This is the means by which you should insert application/server context such as application name, host name, cluster name, cluster location, etc. These can be hardcoded in log4j2.xml or you can use log4j2 [Lookups](http://logging.apache.org/log4j/2.x/manual/lookups.html) to render them from configuration values. Examples are provided of both environment and system lookups are given in the sample log4j2.xml, above.

### UDP Caveat
UDP can have strict size limits and ambiguous over-sized functionality. This layout does not provide a means to truncate long messages in a manner that produces valid JSON. Therefore, UDP is not appropriate for applications that are able to produce long messages such as an exception containing a stacktrace. 

## Developer Discussion

The implementation is fast, using StringBuilder (as opposed to a general use object serializer such as GSON or Jackson). The code is a branch of the log4j2 JSONLayout (http://logging.apache.org/log4j/2.x/log4j-core/apidocs/index.html) and so should be maintained in close parallel (review changeset of JSONLayout when upgrading log4j2 dependency version).

Project Needs: YOU CAN HELP :)

	* Step by step example
	* Tests need to be fleshed out.
	* CI needs to be wired.
	* Maven Artifact needs to be published.


### Building from source
We use gradle to build. Don't worry, it's embedded.

To generate a jar file, use:
./gradlew clean build 

This will produce a jar file that you may import into your current project. For example:
build/libs/log4j2-logstash-jsonevent-layout-1.0.2-SNAPSHOT.jar


### Maven Publishing
We're using gradle maven-publish plugin, which is currently incubating and requires a settings.xml


