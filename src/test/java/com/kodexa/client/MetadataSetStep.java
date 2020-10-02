package com.kodexa.client;

import com.kodexa.client.pipeline.OptionDrivenStep;
import com.kodexa.client.pipeline.Options;
import com.kodexa.client.pipeline.PipelineContext;
import com.kodexa.client.steps.PipelineStep;

public class MetadataSetStep implements PipelineStep, OptionDrivenStep {

    private Options options;

    @Override
    public Document process(Document document, PipelineContext context) {

        if (options.get().containsKey("key")) {
            document.getMetadata().put(String.valueOf(options.get().get("key")), options.get().get("value"));
        }

        return document;
    }

    @Override
    public String getName() {
        return "Metadata Set Step";
    }

    @Override
    public void setOptions(Options options) {
        this.options = options;
    }
}
