/*
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.serotonin.m2m2.web.mvc.rest.v1.publisher;

import com.infiniteautomation.mango.rest.v2.temporaryResource.TemporaryResourceWebSocketHandler;
import com.serotonin.m2m2.module.WebSocketDefinition;
import com.serotonin.m2m2.web.mvc.websocket.MangoWebSocketHandler;

public class TemporaryResourceWebSocketDefinition extends WebSocketDefinition {
    
    public static final String TYPE_NAME = "TEMPORARY_RESOURCE_WEBSOCKET";

    @Override
    protected MangoWebSocketHandler getHandler() {
        return new TemporaryResourceWebSocketHandler();
    }

    @Override
    public String getUrl() {
        return "/v2/websocket/temporary-resources";
    }

    @Override
    public boolean perConnection() {
        return false;
    }

    @Override
    public String getTypeName() {
        return TYPE_NAME;
    }
}