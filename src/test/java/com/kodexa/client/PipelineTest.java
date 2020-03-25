package com.kodexa.client;

import com.kodexa.client.cloud.KodexaCloud;
import com.kodexa.client.cloud.KodexaCloudPipeline;
import com.kodexa.client.cloud.KodexaCloudService;
import com.kodexa.client.cloud.Options;
import com.kodexa.client.connectors.FolderConnector;
import com.kodexa.client.pipeline.Pipeline;
import com.kodexa.client.pipeline.PipelineContext;
import com.kodexa.client.registry.SourceRegistry;
import com.kodexa.client.sink.InMemorySink;
import com.kodexa.client.store.MsgPackDocumentStore;
import com.kodexa.client.store.TableStore;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

@Slf4j
public class PipelineTest {

    @Ignore
    @Test
    public void kodexaPipeline() throws IOException {
        KodexaCloud.setUrl("http://localhost:8080");
        SourceRegistry.getInstance().addConnector(new FolderConnector());

        FolderConnector connector = new FolderConnector("/Users/pdodds/Dropbox/Kodexa Demo/Excel Examples", "*.xlsx");
        MsgPackDocumentStore msgPackDocumentStore = new MsgPackDocumentStore("/tmp/msgpack", true);
        KodexaCloudPipeline pipeline = new KodexaCloudPipeline("kodexa", "excel", connector, Options.start().attachSource());
        pipeline.setSink(msgPackDocumentStore);
        PipelineContext context = pipeline.run();

        Assert.assertTrue(msgPackDocumentStore.getCount() == 1);
    }

    @Ignore
    @Test
    public void kodexaService() throws IOException {
        KodexaCloud.setUrl("http://localhost:8080");
        SourceRegistry.getInstance().addConnector(new FolderConnector());

        MsgPackDocumentStore msgPackDocumentStore = new MsgPackDocumentStore("/tmp/msgpack", true);
        Pipeline pipeline = new Pipeline(new FolderConnector("/Users/pdodds/tmp", "form*.pdf"));
        pipeline.addStep(new KodexaCloudService("kodexa", "pdf-parse", Options.start().attachSource()));
        pipeline.setSink(msgPackDocumentStore);
        PipelineContext context = pipeline.run();

        Assert.assertTrue(msgPackDocumentStore.getCount() == 1);

    }

    @Ignore
    @Test
    public void pipelineToLayout() throws IOException {
        KodexaCloud.setUrl("http://localhost:8080");
        SourceRegistry.getInstance().addConnector(new FolderConnector());

        InMemorySink sink = new InMemorySink();
        MsgPackDocumentStore msgPackDocumentStore = new MsgPackDocumentStore("/tmp/msgpack.backup");
        Pipeline pipeline = new Pipeline(msgPackDocumentStore);
        pipeline.addStep(new KodexaCloudService("kodexa", "node-tagger",
                Options.start()
                        .set("type_re", "line")
                        .set("content_re", "^(F.?itchConnect|feeds)$|^Technical.*3\\.1.$")
                        .set("tag_name", "page_header")));

        pipeline.addStep(new KodexaCloudService("kodexa", "pattern-table-tag",
                Options.start()
                        .set("table_tag_name", "Chapter9")
                        .set("page_start_re", "^Chapter 9.*Layout$")
                        .set("page_end_re", "^Section 11.*Files$")
                        .set("table_header_re", "^Name Definition .* Lookup$")
                        .set("table_end_re", "^\\d+$")
                        .set("tags_to_ignore_re", "page_(header|footer)|ignore")
                        .set("col_space_multiplier", 2.0)));

        pipeline.addStep(new KodexaCloudService("kodexa", "tagged-table-extract",
                Options.start()
                        .set("store_name", "chapter9")
                        .set("table_tag_name", "Chapter9")
                        .set("table_header_re", "^Name Definition .* Lookup$")
                        .set("header_lines_count", 2)
                        .set("first_col_has_text", true)
                        .set("tables_in_page_count", 1)));

        pipeline.setSink(sink);
        PipelineContext context = pipeline.run();

        Assert.assertTrue(msgPackDocumentStore.getCount() == 1);
        Assert.assertTrue(context.getStoreNames().size() == 1);
        Assert.assertTrue(context.getStore("chapter9") != null);
        Assert.assertTrue(context.getStore("chapter9") instanceof TableStore);
        Assert.assertTrue(((TableStore) context.getStore("chapter9")).getRows().size() == 110);

//        Document document = msgPackDocumentStore.getDocument(0);

    }
}
