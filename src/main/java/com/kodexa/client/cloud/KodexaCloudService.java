package com.kodexa.client.cloud;

import com.kodexa.client.Document;
import com.kodexa.client.pipeline.PipelineContext;
import com.kodexa.client.steps.PipelineStep;
import lombok.extern.slf4j.Slf4j;

/**
 * A step that is hosted in the Kodexa Cloud
 */
@Slf4j
public class KodexaCloudService extends AbstractKodexaSession implements PipelineStep {

    private final Options options;

    public KodexaCloudService(String organizationSlug, String serviceSlug) {
        this(organizationSlug, serviceSlug, new Options());
    }

    public KodexaCloudService(String organizationSlug, String serviceSlug, Options options) {
        super(organizationSlug, serviceSlug);
        this.options = options;
    }

    @Override
    public Document process(Document document, PipelineContext pipelineContext) {

        CloudSession session = this.createSession(CloudSessionType.service);
        CloudExecution execution = executeService(session, document, pipelineContext, options);
        execution = waitForExecution(session, execution);
        mergeStores(session,execution,pipelineContext);
        document = getOutputDocument(session,execution);
        return document;
    }

    @Override
    public String getName() {
        return "Kodexa Service [" + KodexaCloud.getUrl() + "/" + organizationSlug + "/" + serviceSlug + "]";
    }

}
