package com.kodexa.client.remote;

import lombok.Data;

/**
 * The configuration object (static) for the connection to Kodexa
 */
@Data
public class KodexaPlatform {

    private static String accessToken = System.getenv("KODEXA_ACCESS_TOKEN");
    private static String url = System.getenv("KODEXA_URL") != null ? System.getenv("KODEXA_URL") : "https://platform.kodexa.com";

    public static String getAccessToken() {
        return KodexaPlatform.accessToken;
    }

    public static void setAccessToken(String accessToken) {
        KodexaPlatform.accessToken = accessToken;
    }

    public static String getUrl() {
        return KodexaPlatform.url;
    }

    public static void setUrl(String url) {
        KodexaPlatform.url = url;
    }

}
