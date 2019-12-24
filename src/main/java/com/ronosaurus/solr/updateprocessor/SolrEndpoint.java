package com.ronosaurus.solr.updateprocessor;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

// https://stackoverflow.com/questions/2435527/use-class-name-as-root-key-for-json-jackson-serialization
@JsonTypeName("solrEndpoint")
@JsonTypeInfo(include = JsonTypeInfo.As.WRAPPER_OBJECT ,use = JsonTypeInfo.Id.NAME)
public class SolrEndpoint {
    public String ip;
    public String key;
    public String previousValue;
    public String newValue;

    public SolrEndpoint(String ip, String key, String previousValue, String newValue) {
        this.ip = ip;
        this.key = key;
        this.previousValue = previousValue;
        this.newValue = newValue;
    }
}
