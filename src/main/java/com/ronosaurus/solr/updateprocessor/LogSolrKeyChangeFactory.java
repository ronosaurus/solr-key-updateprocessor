package com.ronosaurus.solr.updateprocessor;

import org.apache.solr.core.SolrCore;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.update.processor.UpdateRequestProcessor;
import org.apache.solr.update.processor.UpdateRequestProcessorFactory;
import org.apache.solr.util.plugin.SolrCoreAware;

public class LogSolrKeyChangeFactory extends UpdateRequestProcessorFactory implements SolrCoreAware {
    private SolrCore core;

    @Override
    public LogSolrKeyChange getInstance(SolrQueryRequest req, SolrQueryResponse rsp, UpdateRequestProcessor next) {
        return new LogSolrKeyChange(core, next);
    }

    public void inform(SolrCore core) {
        this.core = core;
    }
}