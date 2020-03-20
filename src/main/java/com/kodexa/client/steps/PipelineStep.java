package com.kodexa.client.steps;

import com.kodexa.client.Document;
import com.kodexa.client.pipeline.PipelineContext;

public interface PipelineStep {

    Document process(Document document, PipelineContext context);

    String getName();
}
