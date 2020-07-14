package com.kodexa.client.remote;

import com.kodexa.client.Document;
import com.kodexa.client.connectors.Connector;
import com.kodexa.client.connectors.FolderConnector;
import com.kodexa.client.connectors.InputStreamConnector;
import com.kodexa.client.pipeline.Pipeline;
import com.kodexa.client.pipeline.PipelineContext;
import com.kodexa.client.sink.Sink;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.util.Map;

/**
 * A remote-hosted Pipeline
 */
@Slf4j
public class RemotePipeline extends Pipeline {

    private final RemotePipelineSession session;
    private Options options = Options.start();
    private Sink sink;
    private Map<String, Object> parameters;

    public RemotePipeline(String ref, Connector connector) {
        super(connector);
        session = new RemotePipelineSession(ref);
    }

    public RemotePipeline(String ref, Document document) {
        super(document);
        session = new RemotePipelineSession(ref);
    }

    public RemotePipeline parameters(Map<String, Object> parameters) {
        this.parameters = parameters;
        return this;
    }

    public RemotePipeline options(Options options) {
        this.options = options;
        return this;
    }

    @Override
    public PipelineContext run() {

        log.info("Starting pipeline");
        PipelineContext pipelineContext = new PipelineContext();

        connector.forEachRemaining(document -> {
            long startTime = System.currentTimeMillis();

            CloudSession session = this.session.createSession(CloudSessionType.pipeline);
            CloudExecution execution = this.session.executeService(session, document, pipelineContext, options, parameters);
            execution = this.session.waitForExecution(session, execution);
            this.session.mergeStores(session, execution, pipelineContext);
            pipelineContext.setOutputDocument(this.session.getOutputDocument(session, execution));
            long endTime = System.currentTimeMillis();
            log.info("Pipeline processed in " + (endTime - startTime) + " ms");

            if (sink != null) {
                log.info("Writing to sink " + sink.getName());
                sink.sink(document);
            }
        });

        log.info("Pipeline completed");
        return pipelineContext;

    }

    /**
     * Set the sink
     *
     * @param sink The sink to use for the documents
     */
    public void setSink(Sink sink) {
        this.sink = sink;
    }

    /**
     * Create a new pipeline based on a remote reference and a text document
     *
     * @param ref  the reference (slug) to the pipeline
     * @param text The text to use to create a new document
     * @return A new remote pipeline
     */
    public static Pipeline fromText(String ref, String text) {
        return new RemotePipeline(ref, Document.fromText(text));
    }

    public static Pipeline fromUrl(String ref, String url) {
        return new RemotePipeline(ref, Document.fromUrl(url));
    }

    public static Pipeline fromInputStream(String ref, InputStream inputStream) {
        return new RemotePipeline(ref, new InputStreamConnector(inputStream));
    }

    public static Pipeline fromFolder(String ref, String folderPath, String filenameFilter, boolean recursive) {
        return new RemotePipeline(ref, new FolderConnector(folderPath, filenameFilter, recursive));
    }
}
