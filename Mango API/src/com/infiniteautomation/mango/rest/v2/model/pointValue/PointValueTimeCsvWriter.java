/**
 * @copyright 2017 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2.model.pointValue;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerator;
import com.infiniteautomation.mango.rest.v2.model.pointValue.quantize.DataPointStatisticsGenerator;
import com.infiniteautomation.mango.rest.v2.model.pointValue.query.LatestQueryInfo;
import com.serotonin.ShouldNeverHappenException;
import com.serotonin.m2m2.rt.dataImage.AnnotatedIdPointValueTime;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.vo.DataPointVO;

/**
 *
 * @author Terry Packer
 */
public class PointValueTimeCsvWriter extends PointValueTimeJsonWriter{

    /**
     * @param info
     * @param jgen
     */
    public PointValueTimeCsvWriter(LatestQueryInfo info, JsonGenerator jgen) {
        super(info, jgen);
    }

    @Override
    public void writePointValueTime(DataPointVOPointValueTimeBookend value) throws IOException {
        this.jgen.writeStartObject();
        DataPointVO vo = value.getVo();
        PointValueTime pvt = value.getPvt();
        if(info.isMultiplePointsPerArray()) {
            writeStringField("xid", vo.getXid());
            if(pvt == null) {
                writeStringField(ANNOTATION, info.getNoDataMessage());
            }else {
                writeTimestamp(pvt.getTime());
                if(pvt.isAnnotated())
                    writeStringField(ANNOTATION, ((AnnotatedIdPointValueTime) pvt).getAnnotation(translations));
                if(value.isBookend())
                    writeBooleanField(BOOKEND, true);
                if(value.isCached())
                    writeBooleanField(CACHED, true);
                writeDataValue(VALUE, vo, pvt.getValue(), pvt.getTime());
                if(info.isUseRendered())
                    writeStringField(RENDERED, info.getRenderedString(vo, pvt));
            }
        }else {
            if(info.isSingleArray()) {
                if(pvt == null) {
                    writeStringField(ANNOTATION, info.getNoDataMessage());
                }else {
                    writeTimestamp(pvt.getTime());
                    if(pvt.isAnnotated())
                        writeStringField(ANNOTATION, ((AnnotatedIdPointValueTime) pvt).getAnnotation(translations));
                    if(value.isBookend())
                        writeBooleanField(BOOKEND, true);
                    if(value.isCached())
                        writeBooleanField(CACHED, true);
                    writeDataValue(VALUE, vo, pvt.getValue(), pvt.getTime());
                    if(info.isUseRendered())
                        writeStringField(RENDERED, info.getRenderedString(vo, pvt));
                }
            }else {
                //Use XIDs
                writeStringField("xid", vo.getXid());
                if(pvt == null) {
                    writeStringField(ANNOTATION, info.getNoDataMessage());
                }else {
                    writeTimestamp(pvt.getTime());
                    if(pvt.isAnnotated())
                        writeStringField(ANNOTATION, ((AnnotatedIdPointValueTime) pvt).getAnnotation(translations));
                    if(value.isBookend())
                        writeBooleanField(BOOKEND, true);
                    if(value.isCached())
                        writeBooleanField(CACHED, true);
                    writeDataValue(VALUE, vo, pvt.getValue(), pvt.getTime());
                    if(info.isUseRendered())
                        writeStringField(RENDERED, info.getRenderedString(vo, pvt));
                }
            }
        }
        this.jgen.writeEndObject();
    }
    
    /* (non-Javadoc)
     * @see com.infiniteautomation.mango.rest.v2.model.pointValue.query.PointValueTimeWriter#writeMultipleStatsAsObject(java.util.List)
     */
    @Override
    public void writeStatsAsObject(DataPointStatisticsGenerator periodStats) throws IOException{
        this.writeStartObject();
        writeTimestamp(periodStats.getGenerator().getPeriodStartTime());
        DataPointVO vo = periodStats.getVo();
        if(info.isMultiplePointsPerArray()) {
            writeStringField("xid", vo.getXid());
            writeStatistic(vo.getXid() + "." + info.getRollup().name(), periodStats.getGenerator(), periodStats.getVo());
        }else {
            if(!info.isSingleArray())
                writeStringField("xid", vo.getXid());
            writeStatistic(info.getRollup().name(), periodStats.getGenerator(), periodStats.getVo());
                
        }
        this.writeEndObject();
    }
    
    @Override
    public void writeMultiplePointValuesAtSameTime(List<DataPointVOPointValueTimeBookend> currentValues, long timestamp)  throws IOException{
        this.jgen.writeStartObject();
        writeTimestamp(timestamp);
        for(DataPointVOPointValueTimeBookend value : currentValues) {
            String xid = value.getVo().getXid();
            if(info.isMultiplePointsPerArray()) {
                //XID columns
                writeDataValue(xid, value.getVo(), value.getPvt().getValue(), value.getPvt().getTime());
                if(value.isBookend())
                    writeBooleanField(xid + ".bookend", value.isBookend());
                if(value.isCached())
                    writeBooleanField(xid + ".cached", value.isCached());
                if(info.isUseRendered())
                    writeStringField(xid + ".rendered", info.getRenderedString(value.getVo(), value.getPvt()));
            }else {
                
            }
        
        }
        this.jgen.writeEndObject();
    }
    
    @Override
    public void writeMultiplePointStatsAtSameTime(List<DataPointStatisticsGenerator> periodStats, long timestamp) throws IOException{
        this.jgen.writeStartObject();
        writeTimestamp(timestamp);
        for(DataPointStatisticsGenerator gen : periodStats) {
            DataPointVO vo = gen.getVo();
            if(info.isMultiplePointsPerArray()) {
                writeStatistic(vo.getXid() + "." + info.getRollup(), gen.getGenerator(), gen.getVo());
            }else {
                throw new ShouldNeverHappenException("Implement me?");
            }
        }
        this.jgen.writeEndObject();
    }
    
    /* (non-Javadoc)
     * @see com.infiniteautomation.mango.rest.v2.model.pointValue.query.PointValueTimeWriter#writeIntegral(java.lang.String, com.serotonin.m2m2.vo.DataPointVO, java.lang.Double)
     */
    @Override
    public void writeIntegral(String name, DataPointVO vo, Double integral) throws IOException {
        if (integral == null) {
            writeNullField(name);
        } else {
            writeDoubleField(name, integral);
            if(info.isUseRendered())
                writeStringField(vo.getXid() + ".rendered", info.getIntegralString(vo, integral));
        }
    }
    
    /* (non-Javadoc)
     * @see com.infiniteautomation.mango.rest.v2.model.pointValue.query.PointValueTimeWriter#writeAnalogStatistic(java.lang.String, com.serotonin.m2m2.vo.DataPointVO, java.lang.Double)
     */
    @Override
    public void writeAnalogStatistic(String name, DataPointVO vo, Double value) throws IOException {
        if (value == null) {
            writeNullField(name);
        } else {
            writeDoubleField(name, value);
            if(info.isUseRendered())
                writeStringField(vo.getXid() + ".rendered", info.getRenderedString(vo, value));
        }
    }
    
    /* (non-Javadoc)
     * @see com.infiniteautomation.mango.rest.v2.model.pointValue.query.PointValueTimeWriter#startWriteArray()
     */
    @Override
    public void writeStartArray(String name) throws IOException {
        jgen.writeStartArray();
    }

    /* (non-Javadoc)
     * @see com.infiniteautomation.mango.rest.v2.model.pointValue.query.PointValueTimeWriter#endWriteArray()
     */
    @Override
    public void writeEndArray() throws IOException {
        jgen.writeEndArray();
    }



    /* (non-Javadoc)
     * @see com.infiniteautomation.mango.rest.v2.model.pointValue.query.PointValueTimeWriter#writeStartArray()
     */
    @Override
    public void writeStartArray() throws IOException {
        this.jgen.writeStartArray();
    }



    /* (non-Javadoc)
     * @see com.infiniteautomation.mango.rest.v2.model.pointValue.query.PointValueTimeWriter#writeStartObject()
     */
    @Override
    public void writeStartObject() throws IOException {
        this.jgen.writeStartObject();
    }



    /* (non-Javadoc)
     * @see com.infiniteautomation.mango.rest.v2.model.pointValue.query.PointValueTimeWriter#writeEndObject()
     */
    @Override
    public void writeEndObject() throws IOException {
        this.jgen.writeEndObject();       
    }
    
}