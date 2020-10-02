package com.kodexa.client;

import com.kodexa.client.pipeline.Options;
import com.kodexa.client.pipeline.Pipeline;
import com.kodexa.client.pipeline.PipelineContext;
import org.junit.Assert;
import org.junit.Test;

public class ParameterizedPipelineTest {

    @Test
    public void basicParameterTest() {
        Pipeline pipeline = Pipeline.fromText("Hello World");
        pipeline.addParameter("burger", "bar");
        pipeline.addStep(MetadataSetStep.class, Options.start().set("key", "cheese").set("value", "${burger}").parameterized(true));
        PipelineContext context = pipeline.run();
        Assert.assertTrue(context.getOutputDocument().getMetadata().containsKey("cheese"));
        Assert.assertSame("bar", context.getOutputDocument().getMetadata().get("cheese"));
    }

}
