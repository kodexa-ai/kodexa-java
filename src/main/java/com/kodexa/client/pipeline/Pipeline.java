package com.kodexa.client.pipeline;

import com.kodexa.client.Document;
import com.kodexa.client.connectors.Connector;
import com.kodexa.client.connectors.FolderConnector;
import com.kodexa.client.connectors.InputStreamConnector;
import com.kodexa.client.remote.RemoteAction;
import com.kodexa.client.sink.Sink;
import com.kodexa.client.steps.PipelineStep;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * A Pipeline allows you to put together steps from a connector to a sink
 * to enable re-use and encapsulation of steps.
 */
@Slf4j
public class Pipeline {

    protected final Connector connector;
    private final PipelineContext context;
    private Sink sink;
    private List<PipelineStepWrapper> steps = new ArrayList<>();
    protected List<PipelineParameter> parameters = new ArrayList<>();

    public Pipeline(Connector connector) {
        this.connector = connector;
        this.context = new PipelineContext();
    }

    public Pipeline(Document document) {
        this.connector = new DocumentConnector(document);
        this.context = new PipelineContext();
    }

    public Pipeline addStep(Class stepClass, Options options) {
        steps.add(new PipelineStepWrapper(new ClassBasedStep(stepClass), options));
        return this;
    }

    public Pipeline addStep(String actionSlug, Options options) {
        steps.add(new PipelineStepWrapper(new RemoteAction(actionSlug), options));
        return this;
    }

    public Pipeline addStep(PipelineStep step) {
        steps.add(new PipelineStepWrapper(step, new Options()));
        return this;
    }

    public Pipeline setSink(Sink sink) {
        this.sink = sink;
        return this;
    }

    public Pipeline setParameters(List<PipelineParameter> parameters) {
        this.parameters = parameters;
        return this;
    }

    public PipelineContext run() {

        log.info("Starting pipeline");

        connector.forEachRemaining(document -> {
            for (PipelineStepWrapper step : steps) {
                long startTime = System.currentTimeMillis();
                document = step.process(document, context);
                long endTime = System.currentTimeMillis();
                log.info("Step processed in " + (endTime - startTime) + " ms");
            }

            if (sink != null) {
                log.info("Writing to sink " + sink.getName());
                sink.sink(document);
            }

            context.setOutputDocument(document);
        });

        log.info("Pipeline completed");
        return context;

    }

    public static Pipeline fromText(String text) {
        return new Pipeline(Document.fromText(text));
    }

    public static Pipeline fromUrl(String url) {
        return new Pipeline(Document.fromUrl(url));
    }

    public static Pipeline fromInputStream(InputStream inputStream) {
        return new Pipeline(new InputStreamConnector(inputStream));
    }

    public static Pipeline fromFolder(String folderPath, String filenameFilter, boolean recursive) {
        return new Pipeline(new FolderConnector(folderPath, filenameFilter, recursive));
    }

}
