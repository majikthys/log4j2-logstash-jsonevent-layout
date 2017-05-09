log4j2 CustomJSONLayout
=======================

log4j2 JsonisedLayout

# Overview

This is a log4j2 layout that produces json log events.
JSON produced is a serialization of a given log4j2 LogEvent and is intentionally very similar to 
that produced by the default log4j2 [JSONLayout](http://logging.apache.org/log4j/2.x/manual/layouts.html). 

(see http://logging.apache.org/log4j/2.x/manual/layouts.html and  http://logstash.net/) 


### Example log events

```json
{
  "@timestamp": "2017-05-07T02:27:46.906-07:00",
  "logger_name": "org.apache.logging.log4j.core.layout.CustomJSONLayoutJacksonUnitSpecs",
  "eventSourceId": "UUID-01",
  "timeTaken": 28,
  "unit": "millis",
  "name": "prayagupd",
  "eventType": "SomethingHappened",
  "level": "DEBUG",
  "Foo": "Bar"
}
```

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

### Building from source
We use gradle to build. Don't worry, it's embedded.

To generate a jar file, use:

```
./gradlew clean install

or 

./gradlew clean build 
```


```xml

<dependency>
    <groupId>org.log4j2.jsonised</groupId>
    <artifactId>log4j2-jsonised</artifactId>
    <version>1.0.0</version>
</dependency>

```

### Maven Publishing
We're using gradle maven-publish plugin, which is currently incubating and requires a settings.xml
