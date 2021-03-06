/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.infiniteautomation.mango.rest.v2.JsonEmportV2Controller.ImportStatusProvider;
import com.infiniteautomation.mango.rest.v2.genericcsv.CsvJacksonModule;
import com.infiniteautomation.mango.rest.v2.genericcsv.GenericCSVMessageConverter;
import com.infiniteautomation.mango.rest.v2.mapping.PointValueTimeStreamCsvMessageConverter;
import com.infiniteautomation.mango.rest.v2.model.event.AuditEventTypeModel;
import com.infiniteautomation.mango.rest.v2.model.event.DataPointEventTypeModel;
import com.infiniteautomation.mango.rest.v2.model.event.DataSourceEventTypeModel;
import com.infiniteautomation.mango.rest.v2.model.event.MissingEventTypeModel;
import com.infiniteautomation.mango.rest.v2.model.event.PublisherEventTypeModel;
import com.infiniteautomation.mango.rest.v2.model.event.SystemEventTypeModel;
import com.infiniteautomation.mango.rest.v2.model.event.handlers.EmailEventHandlerModel;
import com.infiniteautomation.mango.rest.v2.model.event.handlers.ProcessEventHandlerModel;
import com.infiniteautomation.mango.rest.v2.model.event.handlers.SetPointEventHandlerModel;
import com.infiniteautomation.mango.rest.v2.patch.PartialUpdateArgumentResolver;
import com.infiniteautomation.mango.rest.v2.util.MangoRestTemporaryResourceContainer;
import com.infiniteautomation.mango.spring.MangoRuntimeContextConfiguration;
import com.serotonin.m2m2.web.mvc.rest.v1.CsvObjectStreamMessageConverter;

/**
 * Class to configure spring for any module specific REST components.
 *
 * Can also be useful to add functionality that will make it into the core on the next minor release.
 *
 * @author Terry Packer
 *
 */
@Configuration
public class MangoRestModuleSpringConfiguration implements WebMvcConfigurer {

    final ObjectMapper mapper;
    final PartialUpdateArgumentResolver resolver;
    
    @Autowired
    public MangoRestModuleSpringConfiguration(
            @Qualifier(MangoRuntimeContextConfiguration.REST_OBJECT_MAPPER_NAME)
            ObjectMapper mapper,
            PartialUpdateArgumentResolver resolver) {
        this.mapper = mapper;
        this.resolver = resolver;
        mapper.registerSubtypes(
                    //Event Handlers
                    new NamedType(EmailEventHandlerModel.class, "EMAIL"),
                    new NamedType(ProcessEventHandlerModel.class, "PROCESS"),
                    new NamedType(SetPointEventHandlerModel.class, "SET_POINT"),
                    new NamedType(AuditEventTypeModel.class, "AUDIT"),
                    //Event Types
                    new NamedType(DataPointEventTypeModel.class, "DATA_POINT"),
                    new NamedType(DataSourceEventTypeModel.class, "DATA_SOURCE"),
                    new NamedType(MissingEventTypeModel.class, "MISSING"),
                    new NamedType(PublisherEventTypeModel.class, "PUBLISHER"),
                    new NamedType(SystemEventTypeModel.class, "SYSTEM")
                );
        
    }

    @Bean("csvObjectMapper")
    public ObjectMapper csvObjectMapper() {
        return mapper.copy()
                .setDateFormat(GenericCSVMessageConverter.EXCEL_DATE_FORMAT)
                .registerModule(new CsvJacksonModule());
    }

    /**
     * Configure the Message Converters for the API for now only JSON
     */
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(new PointValueTimeStreamCsvMessageConverter());
        converters.add(new CsvObjectStreamMessageConverter());
        converters.add(new GenericCSVMessageConverter(csvObjectMapper()));
    }

    @Bean
    public MangoRestTemporaryResourceContainer<ImportStatusProvider> importStatusResources() {
        return new MangoRestTemporaryResourceContainer<ImportStatusProvider>("IMPORT_");
    }
    
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(resolver);
    }
}
