/*
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.gviews;

import com.serotonin.m2m2.module.DwrDefinition;
import com.serotonin.m2m2.web.dwr.ModuleDwr;

public class GraphicalViewsDwrDefinition extends DwrDefinition {
    @Override
    public Class<? extends ModuleDwr> getDwrClass() {
        return GraphicalViewDwr.class;
    }
}
