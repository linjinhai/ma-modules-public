/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.reports;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.infiniteautomation.mango.rest.v2.model.event.handlers.AbstractEventHandlerModel;
import com.serotonin.m2m2.module.ModuleRegistry;
import com.serotonin.m2m2.reports.ReportDao;
import com.serotonin.m2m2.reports.handler.ReportEventHandlerDefinition;
import com.serotonin.m2m2.reports.handler.ReportEventHandlerVO;
import com.serotonin.m2m2.vo.event.AbstractEventHandlerVO;

import io.swagger.annotations.ApiModel;

/**
 * @author Terry Packer
 *
 */
@ApiModel(value="REPORT", parent=AbstractEventHandlerModel.class)
@JsonTypeName("REPORT")
public class ReportEventHandlerModel extends AbstractEventHandlerModel {

    private String activeReportXid;
    private String inactiveReportXid;
    
    public ReportEventHandlerModel() {
        super();
    }
    
    public ReportEventHandlerModel(ReportEventHandlerVO vo) {
        super(vo);
    }
    
    /**
     * @return the activeReportXid
     */
    public String getActiveReportXid() {
        return activeReportXid;
    }

    /**
     * @param activeReportXid the activeReportXid to set
     */
    public void setActiveReportXid(String activeReportXid) {
        this.activeReportXid = activeReportXid;
    }

    /**
     * @return the inactiveReportXid
     */
    public String getInactiveReportXid() {
        return inactiveReportXid;
    }

    /**
     * @param inactiveReportXid the inactiveReportXid to set
     */
    public void setInactiveReportXid(String inactiveReportXid) {
        this.inactiveReportXid = inactiveReportXid;
    }

    @Override
    public AbstractEventHandlerVO<?> toVO() {
        ReportEventHandlerVO vo = (ReportEventHandlerVO)super.toVO();
        Integer id =  ReportDao.getInstance().getIdByXid(activeReportXid);
        if(id != null)
            vo.setActiveReportId(id);
        id = ReportDao.getInstance().getIdByXid(inactiveReportXid);
        if(id != null)
            vo.setInactiveReportId(id);
        return vo;
    }
    
    @Override
    public void fromVO(AbstractEventHandlerVO<?> vo) {
        super.fromVO(vo);
        ReportEventHandlerVO hVo = (ReportEventHandlerVO)vo;
        this.activeReportXid = ReportDao.getInstance().getXidById(hVo.getActiveReportId());
        this.inactiveReportXid = ReportDao.getInstance().getXidById(hVo.getInactiveReportId());
                
    }
    
    @Override
    protected AbstractEventHandlerVO<?> newVO() {
        ReportEventHandlerVO vo = new ReportEventHandlerVO();
        vo.setDefinition(ModuleRegistry.getEventHandlerDefinition(ReportEventHandlerDefinition.TYPE_NAME));
        return vo;
    }

}
