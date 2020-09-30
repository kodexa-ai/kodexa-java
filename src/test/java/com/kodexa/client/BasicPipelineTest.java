package com.kodexa.client;

import com.kodexa.client.pipeline.Options;
import com.kodexa.client.pipeline.Pipeline;
import com.kodexa.client.pipeline.PipelineContext;
import org.junit.Assert;
import org.junit.Test;

/**
 * Basic set of test cases to validate the pipeline functionality is in place
 */
public class BasicPipelineTest {

    @Test
    public void basicTest() {
        Pipeline pipeline = Pipeline.fromText("Hello World");
        PipelineContext context = pipeline.run();
        Assert.assertNotNull(context.getOutputDocument());
    }

    @Test
    public void basicChaining() {
        PipelineContext context = Pipeline.fromText("Hello World").run();
        Assert.assertNotNull(context.getOutputDocument());
    }

    @Test
    public void testEnabled() {
        Pipeline pipeline = Pipeline.fromText("Hello World");
        pipeline.addStep(MetadataSetStep.class, Options.start().set("key", "foo").set("value", "bar"));
        PipelineContext context = pipeline.run();
        Assert.assertTrue(context.getOutputDocument().getMetadata().containsKey("foo"));
        Assert.assertSame("bar", context.getOutputDocument().getMetadata().get("foo"));

    }

    @Test
    public void testDisabled() {
        Pipeline pipeline = Pipeline.fromText("Hello World");
        pipeline.addStep(MetadataSetStep.class, Options.start().set("key", "foo").set("value", "bar").enabled(false));
        PipelineContext context = pipeline.run();
        Assert.assertFalse(context.getOutputDocument().getMetadata().containsKey("foo"));
    }

}
