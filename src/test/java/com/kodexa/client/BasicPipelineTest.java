package com.kodexa.client;

import com.kodexa.client.pipeline.Pipeline;
import com.kodexa.client.pipeline.PipelineContext;
import org.junit.Test;

public class BasicPipelineTest {

    @Test
    public void basicTest() {
        Pipeline pipeline = Pipeline.fromText("Hello World");
        PipelineContext context = pipeline.run();
    }

    @Test
    public void basicChaining() {
        PipelineContext context = Pipeline.fromText("Hello World").run();
    }

    public void testEnabled() {

    }

}
