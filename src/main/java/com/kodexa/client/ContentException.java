package com.kodexa.client;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A content exception
 */
@Data
public class ContentException implements Serializable {

    private String id;

    private String tag;

    private String message;

    private String exceptionDetails;

    private String groupUuid;

    private String tagUuid;

    private String exceptionType;

    private String severity;

    private String nodeUuid;

}
