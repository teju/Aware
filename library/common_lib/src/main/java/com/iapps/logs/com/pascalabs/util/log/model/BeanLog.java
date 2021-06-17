package com.iapps.logs.com.pascalabs.util.log.model;

import com.iapps.logs.com.pascalabs.util.log.model.BeanLogAPI;

import java.io.Serializable;

public class BeanLog
    implements Serializable {
    private static final long serialVersionUID = -1940733474634251659L;
    private String type, event, timestamp, response = "";
    private long timestampMilis;
    private BeanLogAPI beanLogAPI;

    public BeanLog(String type, String event, String timestamp) {
        this.type = type;
        this.event = event;
        this.timestamp = timestamp;
    }

    public BeanLog(String type, String event, String timestamp, String response) {
        this.type = type;
        this.event = event;
        this.timestamp = timestamp;
        this.response = response;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public long getTimestampMilis() {
        return timestampMilis;
    }

    public void setTimestampMilis(long timestampMilis) {
        this.timestampMilis = timestampMilis;
    }

    public BeanLogAPI getBeanLogAPI() {
        return beanLogAPI;
    }

    public void setBeanLogAPI(BeanLogAPI beanLogAPI) {
        this.beanLogAPI = beanLogAPI;
    }
}
