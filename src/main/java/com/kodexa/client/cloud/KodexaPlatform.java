package com.kodexa.client.cloud;

import lombok.Data;

/**
 * The configuration object (static) for the connection to Kodexa
 */
@Data
public class KodexaPlatform {

    private static String accessToken = System.getenv("KODEXA_ACCESS_TOKEN");

    public static String getAccessToken() {
        return KodexaPlatform.accessToken;
    }

    public static void setAccessToken(String accessToken) {
        KodexaPlatform.accessToken = accessToken;
    }

    private static String url = "https://platform.kodexa.com";

    public static String getUrl() {
        return KodexaPlatform.url;
    }

    public static void setUrl(String url) {
        KodexaPlatform.url = url;
    }

}
