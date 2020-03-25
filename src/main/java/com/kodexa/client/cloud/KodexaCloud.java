package com.kodexa.client.cloud;

import lombok.Data;

/**
 * The configuration object (static) for the connection to Kodexa
 */
@Data
public class KodexaCloud {

    private static String accessToken = System.getenv("KODEXA_ACCESS_TOKEN");

    public static String getAccessToken() {
        return KodexaCloud.accessToken;
    }

    public static void setAccessToken(String accessToken) {
        KodexaCloud.accessToken = accessToken;
    }

    private static String url = "https://cloud.kodexa.com";

    public static String getUrl() {
        return KodexaCloud.url;
    }

    public static void setUrl(String url) {
        KodexaCloud.url = url;
    }

}
