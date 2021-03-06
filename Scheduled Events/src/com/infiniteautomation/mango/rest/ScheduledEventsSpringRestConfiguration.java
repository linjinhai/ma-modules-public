/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.infiniteautomation.mango.rest.v2.model.ScheduledEventTypeModel;
import com.infiniteautomation.mango.spring.MangoRuntimeContextConfiguration;
import com.serotonin.m2m2.scheduledEvents.ScheduledEventType;

/**
 * @author Terry Packer
 *
 */
@Configuration
public class ScheduledEventsSpringRestConfiguration {

    @Autowired
    public ScheduledEventsSpringRestConfiguration(
            @Autowired
            @Qualifier(MangoRuntimeContextConfiguration.REST_OBJECT_MAPPER_NAME)
            ObjectMapper mapper) {
        mapper.registerSubtypes(new NamedType(ScheduledEventTypeModel.class, ScheduledEventType.TYPE_NAME));
    }
}
