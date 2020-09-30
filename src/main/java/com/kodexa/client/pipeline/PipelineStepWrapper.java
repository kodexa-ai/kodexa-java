package com.kodexa.client.pipeline;

import com.kodexa.client.Document;
import com.kodexa.client.steps.PipelineStep;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PipelineStepWrapper {

    private final PipelineStep step;

    private Options options;

    public PipelineStepWrapper(PipelineStep step, Options options) {
        this.step = step;
        this.options = options;
    }

    public Document process(Document document, PipelineContext context) {
        log.info("Starting step " + step.getName());

        if (!this.options.isEnabled()) {
            return document;
        }

        // TODO We need to handle the use of parameterized options

        if (this.step instanceof OptionDrivenStep) {
            ((OptionDrivenStep) this.step).setOptions(this.options);
        }

        document = this.step.process(document, context);

        return document;
    }
}
