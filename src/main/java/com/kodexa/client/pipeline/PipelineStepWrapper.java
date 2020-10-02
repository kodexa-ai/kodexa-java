package com.kodexa.client.pipeline;

import com.kodexa.client.Document;
import com.kodexa.client.steps.PipelineStep;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class PipelineStepWrapper {

    private final Pattern pattern = Pattern.compile("\\$\\{(.*)\\}");

    private final PipelineStep step;

    private Options options;

    public PipelineStepWrapper(PipelineStep step, Options options) {
        this.step = step;
        this.options = options;
    }

    private void replaceParameters(Map<String, Object> options, Map<String, Object> parameters) {
        options.forEach((k, v) -> {
            if (v instanceof String) {
                String stringValue = (String) v;
                Matcher matcher = pattern.matcher(stringValue);
                if (matcher.find()) {
                    String paramName = matcher.group(1);
                    options.put(k, parameters.get(paramName));
                }
            }
        });
    }

    public Document process(Document document, PipelineContext context) {
        log.info("Starting step " + step.getName());

        if (!this.options.isEnabled()) {
            return document;
        }

        Options finalOptions = this.options.createClone();
        if (this.options.isParameterized()) {
            replaceParameters(finalOptions.getMap(), context.getParameterMap());
        }

        if (this.step instanceof OptionDrivenStep) {
            ((OptionDrivenStep) this.step).setOptions(finalOptions);
        }

        document = this.step.process(document, context);

        return document;
    }
}
