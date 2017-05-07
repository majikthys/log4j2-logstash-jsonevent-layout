package org.apache.logging.log4j.core;

import org.json.JSONObject;

/**
 * Created by prayagupd
 * on 5/7/17.
 */

public class AppLog extends JSONObject {

    public AppLog(){

    }

    public AppLog eventSourceId(Object eventSourceId) {
        this.put("eventSourceId", eventSourceId);
        return this;
    }

    public AppLog eventType(String eventType) {
        this.put("eventType", eventType);
        return this;
    }

    public AppLog metrics(String metricKey, Object metricValue) {
        this.put(metricKey, metricValue);
        return this;
    }

    public String toJson() {
        return super.toString();
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
