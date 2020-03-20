package com.kodexa.client.connectors;

import com.kodexa.client.Document;
import com.kodexa.client.KodexaException;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Slf4j
public class FolderConnector implements Connector {

    private final String path;
    private final String filter;
    private final File[] files;
    private int position;

    public FolderConnector() {
        this.path = null;
        this.filter = null;
        this.files = new File[]{};
        this.position = 0;
    }

    public FolderConnector(String path, String filter) {
        this.path = path;
        this.filter = filter;

        try {
            File f = new File(path);
            files = f.listFiles(new WildcardMatcher(filter));
            position = 0;
        } catch (Exception e) {
            throw new RuntimeException("Unable to build folder connector", e);
        }
    }

    @Override
    public boolean hasNext() {
        return files != null && position + 1 <= files.length;
    }

    @Override
    public Document next() {
        File file = files[position];
        position++;
        log.info("Found file "+file.getAbsolutePath());
        Document document = new Document();
        document.getMetadata().put("source_path", file.getAbsolutePath());
        document.getMetadata().put("connector", getName());
        Map<String, String> options = new HashMap<>();
        options.put("path", path);
        options.put("file_filter", filter);
        document.getMetadata().put("connector_options", options);
        return document;
    }

    @Override
    public String getName() {
        return "folder";
    }

    @Override
    public InputStream getSource(Document document) {
        try {
            return new FileInputStream(String.valueOf(document.getMetadata().get("source_path")));
        } catch (FileNotFoundException e) {
            throw new KodexaException("Unable to find source file", e);
        }
    }

    public static class WildcardMatcher implements FilenameFilter {

        private final Pattern pattern;

        /**
         * Creates a new matcher with the given expression.
         *
         * @param expression wildcard expressions
         */
        public WildcardMatcher(final String expression) {
            final String[] parts = expression.split("\\:");
            final StringBuilder regex = new StringBuilder(expression.length() * 2);
            boolean next = false;
            for (final String part : parts) {
                if (next) {
                    regex.append('|');
                }
                regex.append('(').append(toRegex(part)).append(')');
                next = true;
            }
            pattern = Pattern.compile(regex.toString());
        }

        private static CharSequence toRegex(final String expression) {
            final StringBuilder regex = new StringBuilder(expression.length() * 2);
            for (final char c : expression.toCharArray()) {
                switch (c) {
                    case '?':
                        regex.append(".");
                        break;
                    case '*':
                        regex.append(".*");
                        break;
                    default:
                        regex.append(Pattern.quote(String.valueOf(c)));
                        break;
                }
            }
            return regex;
        }

        @Override
        public boolean accept(File dir, String name) {
            return pattern.matcher(name).matches();
        }
    }
}
