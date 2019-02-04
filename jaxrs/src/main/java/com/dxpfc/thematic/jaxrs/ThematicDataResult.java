package com.dxpfc.thematic.jaxrs;

import org.onehippo.cms7.crisp.core.resource.jackson.JacksonResource;

public class ThematicDataResult {

    private int numFound;
    private JacksonResource searchResult;

    public int getNumFound() {
        return numFound;
    }

    public void setNumFound(int numFound) {
        this.numFound = numFound;
    }

    public JacksonResource getSearchResult() {
        return searchResult;
    }

    public void setSearchResult(JacksonResource searchResult) {
        this.searchResult = searchResult;
    }

}
