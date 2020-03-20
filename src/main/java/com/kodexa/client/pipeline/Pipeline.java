package com.kodexa.client.pipeline;

import com.kodexa.client.connectors.Connector;
import com.kodexa.client.sink.Sink;
import com.kodexa.client.steps.PipelineStep;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * A Pipeline allows you to put together steps from a connector to a sink
 * to enable re-use and encapsulation of steps.
 */
@Slf4j
public class Pipeline {

    private final Connector connector;
    private final PipelineContext context;
    private Sink sink;
    private List<PipelineStep> steps = new ArrayList<>();

    public Pipeline(Connector connector) {
        this.connector = connector;
        this.context = new PipelineContext();
    }

    public void addStep(PipelineStep step) {
        steps.add(step);
    }

    public void setSink(Sink sink) {
        this.sink = sink;
    }

    public PipelineContext run() {

        log.info("Starting pipeline");

        connector.forEachRemaining(document -> {
            for (PipelineStep step : steps) {
                log.info("Starting step " + step.getName());
                long startTime = System.currentTimeMillis();
                document = step.process(document, context);
                long endTime = System.currentTimeMillis();
                log.info("Step processed in " + (endTime - startTime) + " ms");
            }

            if (sink != null) {
                log.info("Writing to sink " + sink.getName());
                sink.sink(document);
            }
        });

        log.info("Pipeline completed");
        return context;

    }
}
