package com.kodexa.client.steps;

import com.kodexa.client.Document;
import com.kodexa.client.pipeline.PipelineContext;

/**
 * An interface that describes the contract for a pipeline step.
 * <p>
 * Each step will recieve one document at a time, and also the pipeline context, it can then work with that document
 * and return it, or create a new document to return.
 */
public interface PipelineStep {

    /**
     * The pipeline will call the process method, passing each document to the step.  The step will then perform
     * and actions and can return either the same document or a new document representing its result.
     * <p>
     * It can also interact with the context if it wishes to work with the stores or other contextual information.
     *
     * @param document The document to process
     * @param context  The pipeline's context
     * @return The document after the steps actions
     */
    Document process(Document document, PipelineContext context);

    /**
     * The name of the pipeline step
     *
     * @return a string representing the name
     */
    String getName();
}
