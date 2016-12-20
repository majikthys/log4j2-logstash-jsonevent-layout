/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache license, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the license for the specific language governing permissions and
 * limitations under the license.
 */
package org.apache.logging.log4j.core.jackson;

import org.apache.logging.log4j.core.JsonLogEvent;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Extends LogEventMixIn but adds two elements
 */
@JsonSerialize(converter = JsonLogEvent.LogEventToLogStashLogEventConverter.class)
@JsonRootName(XmlConstants.ELT_EVENT)
@JsonFilter("org.apache.logging.log4j.core.impl.Log4jLogEvent")
@JsonPropertyOrder({"version", "timestamp", "timeMillis", "threadName", "level", "loggerName", "marker", "message", "thrown", XmlConstants.ELT_CONTEXT_MAP,
        JsonConstants.ELT_CONTEXT_STACK, "loggerFQCN", "Source", "endOfBatch" })
abstract class CustomLogEventMixIn extends LogEventMixIn {

    @JsonProperty("timestamp")
    public abstract String getTimestamp();

    private static final long serialVersionUID = 1L;

    @JsonProperty("version")
    public abstract String getVersion();


}
