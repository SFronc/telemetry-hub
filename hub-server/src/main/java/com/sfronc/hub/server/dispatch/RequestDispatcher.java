package com.sfronc.hub.server.dispatch;

import com.sfronc.hub.common.Ids;
import com.sfronc.hub.common.Json;
import com.sfronc.hub.common.messages.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RequestDispatcher {
    private static final Logger log = LoggerFactory.getLogger(RequestDispatcher.class);

    private final CommandFactory factory;
}
