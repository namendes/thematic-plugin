package com.dxpfc.thematic.model.impl;

import com.dxpfc.thematic.model.ThematicConfigProperties;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ThematicConfigPropertiesImpl implements Serializable, Cloneable, ThematicConfigProperties {

    public static final String AUTH_KEY = "auth_key";
    public static final String THEMATIC_PAGE_SITEMAP_ITEM = "thematicPageSitemapPath";
    public static final String ACCOUNT_ID = "account_id";
    public static final String ROWS = "rows";
    public static final String SORT_BY = "sort_by";

    private String authKey;
    private String thematicPageSitemapPath;
    private String accountId;
    private String rows;
    private String sortBy;

    @JsonProperty(AUTH_KEY)
    public String getAuthKey() {
        return authKey;
    }

    public void setAuthKey(final String authKey) {
        this.authKey = authKey;
    }

    @JsonProperty(THEMATIC_PAGE_SITEMAP_ITEM)
    @Override
    public String getThematicPageSitemapPath() {
        return thematicPageSitemapPath;
    }

    public void setThematicPageSitemapPath(String thematicPageSitemapPath) {
        this.thematicPageSitemapPath = thematicPageSitemapPath;
    }

    @JsonProperty(ACCOUNT_ID)
    @Override
    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    @JsonProperty(ROWS)
    @Override
    public String getRows() {
        return rows;
    }

    public void setRows(String rows) {
        this.rows = rows;
    }

    @JsonProperty(SORT_BY)
    @Override
    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }
}
