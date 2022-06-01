package com.kodexa.client;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * A content exception
 */
@Data
public class ContentException {

    private String id;

    private String tag;

    private String message;

    private String exceptionDetails;

    private String groupUuid;

    private String tagUuid;

}
