package com.iapps.logs.com.pascalabs.util.log.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class BeanLogAPI
    implements Serializable {

    private static final long serialVersionUID = 8130623756360893148L;
    private String params;
    private String method;
    private String header;
    private String fileparam;
    private String byteparam;
    private String url;
    private String statuscode;
    private String content;

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }

    private String exception;
    private Class shortenClassName;

    private String parameters = "";
    private HashMap<String, String> mHeaderParams = new HashMap<String, String>();

    public BeanLogAPI() {}

    public Class getShortenClassName() {
        return shortenClassName;
    }

    public void setShortenClassName(Class shortenClassName) {
        this.shortenClassName = shortenClassName;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getFileparam() {
        return fileparam;
    }

    public void setFileparam(String fileparam) {
        this.fileparam = fileparam;
    }

    public String getByteparam() {
        return byteparam;
    }

    public void setByteparam(String byteparam) {
        this.byteparam = byteparam;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getStatuscode() {
        return statuscode;
    }

    public void setStatuscode(String statuscode) {
        this.statuscode = statuscode;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    public HashMap<String, String> getmHeaderParams() {
        return mHeaderParams;
    }

    public void setmHeaderParams(HashMap<String, String> mHeaderParams) {
        this.mHeaderParams = mHeaderParams;
    }
}
