package com.kodexa.client.remote;

import com.kodexa.client.Document;
import com.kodexa.client.pipeline.Options;
import com.kodexa.client.pipeline.PipelineContext;
import com.kodexa.client.steps.PipelineStep;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * A step that is hosted in the Kodexa Cloud
 */
@Slf4j
public class RemoteAction extends AbstractKodexaSession implements PipelineStep {

    private Options options;
    private Map<String, Object> parameters;

    public RemoteAction(String organizationSlug, String serviceSlug) {
        this(organizationSlug, serviceSlug, new Options());
    }

    public RemoteAction(String ref) {
        super(ref);
    }

    public RemoteAction(String organizationSlug, String serviceSlug, Options options) {
        super(organizationSlug + "/" + serviceSlug);
        this.options = options;
    }

    public RemoteAction options(Options options) {
        this.options = options;
        return this;
    }

    public RemoteAction parameters(Map<String, Object> parameters) {
        this.parameters = parameters;
        return this;
    }

    @Override
    public Document process(Document document, PipelineContext pipelineContext) {

        CloudSession session = this.createSession(CloudSessionType.service);
        CloudExecution execution = executeService(session, document, pipelineContext, options, parameters);
        execution = waitForExecution(session, execution);
        mergeStores(session, execution, pipelineContext);
        document = getOutputDocument(session, execution);
        return document;
    }

    @Override
    public String getName() {
        return "Kodexa Service [" + KodexaPlatform.getUrl() + "/" + this.getRef() + "]";
    }

}
