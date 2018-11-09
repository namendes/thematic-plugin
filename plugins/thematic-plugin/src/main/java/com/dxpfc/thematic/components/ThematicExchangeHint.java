package com.dxpfc.thematic.components;

import org.onehippo.cms7.crisp.api.exchange.ExchangeHint;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ThematicExchangeHint implements ExchangeHint {
    private String methodName;
    /**
     * @deprecated
     */
    @Deprecated
    private Object request;
    private Map<String, List<String>> requestHeaders;
    private Map<String, List<String>> unmodifiableRequestHeaders = Collections.emptyMap();
    private Object requestBody;
    private String theme;

    ThematicExchangeHint() {
    }

    public String getMethodName() {
        return this.methodName;
    }

    void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    /**
     * @deprecated
     */
    @Deprecated
    public Object getRequest() {
        return this.request;
    }

    /**
     * @deprecated
     */
    @Deprecated
    void setRequest(Object request) {
        this.request = request;
    }

    void setTheme(String theme) {
        this.theme = theme;
    }

    public Map<String, List<String>> getRequestHeaders() {
        return this.unmodifiableRequestHeaders;
    }

    void setRequestHeaders(Map<String, List<String>> requestHeaders) {
        this.requestHeaders = new LinkedHashMap();
        this.unmodifiableRequestHeaders = Collections.unmodifiableMap(this.requestHeaders);
        if (requestHeaders != null) {
            this.requestHeaders.putAll(requestHeaders);
        }

    }

    public Object getRequestBody() {
        return this.requestBody;
    }

    void setRequestBody(Object requestBody) {
        this.requestBody = requestBody;
    }

    public Object getCacheKey() {
        StringBuilder sb = new StringBuilder(10);
        if (this.methodName != null) {
            sb.append("method=").append(this.methodName);
        }
        if (this.theme != null) {
            sb.append(",theme=").append(this.theme);
        }
        return sb.toString();
    }
}
