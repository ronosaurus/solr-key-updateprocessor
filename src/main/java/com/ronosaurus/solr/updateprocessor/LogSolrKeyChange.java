package com.ronosaurus.solr.updateprocessor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.StringUtils;
import org.apache.solr.core.SolrCore;
import org.apache.solr.search.SolrIndexSearcher;
import org.apache.solr.update.AddUpdateCommand;
import org.apache.solr.update.processor.UpdateRequestProcessor;
import org.apache.solr.util.RefCounted;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

// inspired by https://github.com/oreillymedia/ifpress-solr-plugin/blob/master/src/main/java/com/ifactory/press/db/solr/processor/UpdateDocValuesProcessor.java
public class LogSolrKeyChange extends UpdateRequestProcessor  {
    private final static Logger LOG = LoggerFactory.getLogger(LogSolrKeyChange.class);

    // https://stackoverflow.com/questions/3907929/should-i-declare-jacksons-objectmapper-as-a-static-field
    private final static ObjectWriter WRITER = new ObjectMapper().writer();

    private final SolrCore core;

    public LogSolrKeyChange(SolrCore core, UpdateRequestProcessor next) {
        super(next);
        this.core = core;
    }

    @Override
    public void processAdd(AddUpdateCommand cmd) throws IOException {
        String ip = getRemoteAddr(cmd);
        String name = (String)cmd.getSolrInputDocument().getFieldValue("name");
        String previousEndpoint = getPreviousEndpoint(cmd, name);
        String newEndpoint = (String)cmd.getSolrInputDocument().getFieldValue("endpoint");
        String json = WRITER.writeValueAsString(new SolrEndpoint(ip, name, previousEndpoint, newEndpoint));
        LOG.info("processAdd - {}", json);

        if (next != null) {
            next.processAdd(cmd);
        }
    }

    private String getRemoteAddr(AddUpdateCommand cmd) {
        HttpServletRequest servletRequest = cmd.getReq().getHttpSolrCall().getReq();
        String forwardedFor = servletRequest.getHeader("X-Forwarded-For");
        return StringUtils.isEmpty(forwardedFor) ? servletRequest.getRemoteAddr() : forwardedFor;
    }

    // http://useof.org/java-open-source/org.apache.solr.search.SolrIndexSearcher
    private String getPreviousEndpoint(AddUpdateCommand cmd, String name) {
        String result = null;
        RefCounted<SolrIndexSearcher> searcherRef = core.getSearcher();
        try {
            SolrIndexSearcher searcher = searcherRef.get();
            TermQuery query = new TermQuery(new Term("name", name));
            ScoreDoc scoreDoc = searcher.search(query, 1).scoreDocs[0];
            result = searcher.doc(scoreDoc.doc).get("endpoint");
        } catch (IOException e) {
            LOG.warn("getPreviousEndpoint - " + e.getMessage());
        } finally {
            searcherRef.decref();
        }
        return result;
    }
}