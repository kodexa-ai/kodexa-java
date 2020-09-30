package com.kodexa.client.pipeline;

import com.kodexa.client.Document;
import com.kodexa.client.steps.PipelineStep;

public class ClassBasedStep implements PipelineStep {

    private final Class stepClass;

    public ClassBasedStep(Class stepClass) {
        this.stepClass = stepClass;
    }

    @Override
    public Document process(Document document, PipelineContext context) {
        return document;
    }

    @Override
    public String getName() {
        return "Step for class " + stepClass.getName();
    }
}
