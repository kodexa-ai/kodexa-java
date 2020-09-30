package com.kodexa.client.pipeline;

import com.kodexa.client.Document;
import com.kodexa.client.KodexaException;
import com.kodexa.client.steps.PipelineStep;

public class ClassBasedStep implements PipelineStep, OptionDrivenStep {

    private final Class stepClass;
    private Options options;

    public ClassBasedStep(Class stepClass) {
        this.stepClass = stepClass;
    }

    @Override
    public Document process(Document document, PipelineContext context) {

        Object classInstance = null;
        try {
            classInstance = this.stepClass.newInstance();
            if (classInstance instanceof OptionDrivenStep) {
                ((OptionDrivenStep) classInstance).setOptions(this.options);
            }
            if (classInstance instanceof PipelineStep) {
                return ((PipelineStep) classInstance).process(document, context);
            } else {
                throw new KodexaException("Class is not an instance of PipelineStep?");
            }
        } catch (Exception e) {
            throw new KodexaException("Unable to create instance of class for step", e);
        }


    }

    @Override
    public String getName() {
        return "Step for class " + stepClass.getName();
    }

    @Override
    public void setOptions(Options options) {
        this.options = options;
    }
}
