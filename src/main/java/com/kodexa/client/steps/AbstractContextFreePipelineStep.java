package com.kodexa.client.steps;

import com.kodexa.client.Document;
import com.kodexa.client.pipeline.PipelineContext;

public abstract class AbstractContextFreePipelineStep implements PipelineStep {

    public abstract Document process(Document document);

    @Override
    public Document process(Document document, PipelineContext context) {
        return process(document);
    }
}
