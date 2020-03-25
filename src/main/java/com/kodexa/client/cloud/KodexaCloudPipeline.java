package com.kodexa.client.cloud;

import com.kodexa.client.Document;
import com.kodexa.client.connectors.Connector;
import com.kodexa.client.pipeline.PipelineContext;
import com.kodexa.client.sink.Sink;
import lombok.extern.slf4j.Slf4j;

/**
 * A Kodexa-hosted Pipeline
 */
@Slf4j
public class KodexaCloudPipeline extends AbstractKodexaSession {

    private final Options options;
    private final Connector connector;
    private Sink sink;

    /**
     * Create a pipeline connected to the Kodexa Cloud using the organization / action url.
     *
     * @param organizationSlug the slug for the organization
     * @param serviceSlug      the slug for the action/service
     * @param connector        the connector for feeding documents into the pipeline
     */
    public KodexaCloudPipeline(String organizationSlug, String serviceSlug, Connector connector) {
        this(organizationSlug, serviceSlug, connector, new Options());
    }

    /**
     * Create a pipeline connected to the Kodexa Cloud using the organization / action url.
     *
     * @param organizationSlug the slug for the organization
     * @param serviceSlug      the slug for the action/service
     * @param connector        the connector for feeding documents into the pipeline
     * @param options          the options object for the pipeline
     */
    public KodexaCloudPipeline(String organizationSlug, String serviceSlug, Connector connector, Options options) {
        super(organizationSlug, serviceSlug);
        this.options = options;
        this.connector = connector;
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
     * Run the pipeline.
     * <p>
     * This will start the connector and feed each document through the steps in the pipeline referenced
     *
     * @return The final {@link PipelineContext} after all documents have been processed
     */
    public PipelineContext run() {

        log.info("Starting pipeline");
        CloudSession session = this.createSession(CloudSessionType.pipeline);
        PipelineContext pipelineContext = new PipelineContext();
        connector.forEachRemaining(document -> {
            CloudExecution execution = executeService(session, document, pipelineContext, options);
            execution = waitForExecution(session, execution);

            mergeStores(session,execution,pipelineContext);
            document = getOutputDocument(session,execution);

            if (sink != null) {
                log.info("Writing to sink " + sink.getName());
                sink.sink(document);
            }

        });

        log.info("Pipeline completed");
        return pipelineContext;
    }


}
