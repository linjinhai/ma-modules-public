/**
 * @copyright 2017 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.infiniteautomation.mango.rest.v2.exception.AccessDeniedException;
import com.infiniteautomation.mango.rest.v2.exception.NotFoundRestException;
import com.infiniteautomation.mango.rest.v2.model.pointValue.PointValueImportResult;
import com.infiniteautomation.mango.rest.v2.model.pointValue.PointValueTimeStream;
import com.infiniteautomation.mango.rest.v2.model.pointValue.quantize.MultiDataPointStatisticsQuantizerStream;
import com.infiniteautomation.mango.rest.v2.model.pointValue.query.LatestQueryInfo;
import com.infiniteautomation.mango.rest.v2.model.pointValue.query.MultiPointLatestDatabaseStream;
import com.infiniteautomation.mango.rest.v2.model.pointValue.query.MultiPointTimeRangeDatabaseStream;
import com.infiniteautomation.mango.rest.v2.model.pointValue.query.ZonedDateTimeRangeQueryInfo;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.db.dao.PointValueDao;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.permission.Permissions;
import com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.PointValueTimeModel;
import com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.XidPointValueTimeModel;
import com.serotonin.m2m2.web.mvc.rest.v1.model.time.RollupEnum;
import com.serotonin.m2m2.web.mvc.rest.v1.model.time.TimePeriod;
import com.serotonin.m2m2.web.mvc.rest.v1.model.time.TimePeriodType;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

/**
 *
 * @author Terry Packer
 */
@Api(value = "Point Values", description = "Point Values")
@RestController("PointValueV2RestController")
@RequestMapping("/v2/point-values")
public class PointValueRestController extends AbstractMangoRestV2Controller{

    private final PointValueDao dao = Common.databaseProxy.newPointValueDao();
    
    @ApiOperation(
            value = "Get latest values For 1 Data Point in time descending order", 
            notes = "Optionally use memory cached values that are available on Interval Logged data points, < before time and optional limit",
            response = PointValueTimeModel.class, 
            responseContainer = "Array"
            )
    @RequestMapping(method = RequestMethod.GET, value = "/latest/{xid}")
    public ResponseEntity<PointValueTimeStream<PointValueTimeModel, LatestQueryInfo>> getLatestPointValues(
            HttpServletRequest request,
            @ApiParam(value = "Point xid", required = true, allowMultiple = false) 
            @PathVariable String xid,

            @ApiParam(value = "Return rendered value as 'rendered' field", required = false,
                    defaultValue = "false", allowMultiple = false) 
            @RequestParam(required = false, defaultValue = "false") 
            boolean useRendered,
            
            @ApiParam(value = "Date Time format pattern for timestamps as strings, if not included epoch milli number is used",
            required = false, allowMultiple = false) 
            @RequestParam(value = "dateTimeFormat", required = false) 
            String dateTimeFormat,

            @ApiParam(value = "Return values before this time", required = false, allowMultiple = false)
            @RequestParam(value = "before", required = false)
            @DateTimeFormat(iso = ISO.DATE_TIME) 
            ZonedDateTime before,
            
            @ApiParam(value = "Time zone", required = false, allowMultiple = false) 
            @RequestParam(value = "timezone", required = false) 
            String timezone,
            
            @ApiParam(value = "Limit", required = false, allowMultiple = false) 
            @RequestParam(value = "limit", required = false) 
            Integer limit,

            @ApiParam(value = "Use cached intra-interval logging data", required = false, allowMultiple = false) 
            @RequestParam(value = "useCache", required = false, defaultValue="false") 
            boolean useCache,
            
            @AuthenticationPrincipal User user
            ) {

        LatestQueryInfo info = new LatestQueryInfo(request.getServerName(), 
                request.getServerPort(), before, dateTimeFormat, timezone, RollupEnum.NONE, null, limit, 
                true, false, useRendered, false, true, useCache);
        
        return generateLatestStream(user, info, new String[] {xid});
    }    
    
    @ApiOperation(
            value = "Get latest values For 1 or more Data Points in time descending order in a single array", 
            notes = "Optionally use memory cached values that are available on Interval Logged data points, < before time and optional limit",
            response = PointValueTimeModel.class, 
            responseContainer = "Array"
            )
    @RequestMapping(method = RequestMethod.GET, value = "/single-array/latest/{xids}")
    public ResponseEntity<PointValueTimeStream<PointValueTimeModel, LatestQueryInfo>> getLatestPointValuesAsSingleArray(
            HttpServletRequest request,
            @ApiParam(value = "Point xids", required = true, allowMultiple = false) 
            @PathVariable String[] xids,

            @ApiParam(value = "Return rendered value as 'rendered' field", required = false,
                    defaultValue = "false", allowMultiple = false) 
            @RequestParam(required = false, defaultValue = "false") 
            boolean useRendered,
            
            @ApiParam(value = "Date Time format pattern for timestamps as strings, if not included epoch milli number is used",
            required = false, allowMultiple = false) 
            @RequestParam(value = "dateTimeFormat", required = false) 
            String dateTimeFormat,

            @ApiParam(value = "Return values before this time", required = false, allowMultiple = false)
            @RequestParam(value = "before", required = false)
            @DateTimeFormat(iso = ISO.DATE_TIME) 
            ZonedDateTime before,
            
            @ApiParam(value = "Time zone", required = false, allowMultiple = false) 
            @RequestParam(value = "timezone", required = false) 
            String timezone,
            
            @ApiParam(value = "Limit", required = false, allowMultiple = false) 
            @RequestParam(value = "limit", required = false) 
            Integer limit,

            @ApiParam(value = "Use cached intra-interval logging data", required = false, allowMultiple = false) 
            @RequestParam(value = "useCache", required = false, defaultValue="false") 
            boolean useCache,
            
            @AuthenticationPrincipal User user
            ) {

        LatestQueryInfo info = new LatestQueryInfo(request.getServerName(), 
                request.getServerPort(), before, dateTimeFormat, timezone, RollupEnum.NONE, null, limit, 
                true, false, useRendered, true, true, useCache);
        
        return generateLatestStream(user, info, xids);
    }
    
    @ApiOperation(
            value = "Get latest values For 1 or more Data Points in time descending order in multiple arrays", 
            notes = "Optionally use memory cached values that are available on Interval Logged data points, < before time and optional limit",
            response = PointValueTimeModel.class, 
            responseContainer = "Object"
            )
    @RequestMapping(method = RequestMethod.GET, value = "/multiple-arrays/latest/{xids}")
    public ResponseEntity<PointValueTimeStream<PointValueTimeModel, LatestQueryInfo>> getLatestPointValuesAsMultipleArrays(
            HttpServletRequest request,
            @ApiParam(value = "Point xids", required = true, allowMultiple = false) 
            @PathVariable String[] xids,

            @ApiParam(value = "Return rendered value as 'rendered' field", required = false,
                    defaultValue = "false", allowMultiple = false) 
            @RequestParam(required = false, defaultValue = "false") 
            boolean useRendered,
            
            @ApiParam(value = "Date Time format pattern for timestamps as strings, if not included epoch milli number is used",
            required = false, allowMultiple = false) 
            @RequestParam(value = "dateTimeFormat", required = false) 
            String dateTimeFormat,

            @ApiParam(value = "Return values before this time", required = false, allowMultiple = false)
            @RequestParam(value = "before", required = false)
            @DateTimeFormat(iso = ISO.DATE_TIME) 
            ZonedDateTime before,
            
            @ApiParam(value = "Time zone", required = false, allowMultiple = false) 
            @RequestParam(value = "timezone", required = false) 
            String timezone,
            
            @ApiParam(value = "Limit", required = false, allowMultiple = false) 
            @RequestParam(value = "limit", required = false) 
            Integer limit,

            @ApiParam(value = "Use cached intra-interval logging data", required = false, allowMultiple = false) 
            @RequestParam(value = "useCache", required = false, defaultValue="false") 
            boolean useCache,
            
            @AuthenticationPrincipal User user
            ) {

        LatestQueryInfo info = new LatestQueryInfo(request.getServerName(), 
                request.getServerPort(), before, dateTimeFormat, timezone, RollupEnum.NONE, null, limit, 
                true, false, useRendered, false, false, useCache);
        
        return generateLatestStream(user, info, xids);
    } 
    
    @ApiOperation(
            value = "Query Time Range For 1 Data Point, return in time ascending order", 
            notes = "From time inclusive, To time exclusive.  With a bookend value at from and to if possible/necessary.",
            response = PointValueTimeModel.class, 
            responseContainer = "Array"
            )
    @RequestMapping(method = RequestMethod.GET, value = "/time-period/{xid}")
    public ResponseEntity<PointValueTimeStream<PointValueTimeModel, ZonedDateTimeRangeQueryInfo>> getPointValues(
            HttpServletRequest request,
            @ApiParam(value = "Point xid", required = true, allowMultiple = false) 
            @PathVariable String xid,

            @ApiParam(value = "Return rendered value as 'rendered' field", required = false,
                    defaultValue = "false", allowMultiple = false) 
            @RequestParam(required = false, defaultValue = "false") 
            boolean useRendered,
            
            @ApiParam(value = "Date Time format pattern for timestamps as strings, if not included epoch milli number is used",
            required = false, allowMultiple = false) 
            @RequestParam(value = "dateTimeFormat", required = false) 
            String dateTimeFormat,
    
            @ApiParam(value = "From time", required = false, allowMultiple = false)
            @RequestParam(value = "from", required = false)
            @DateTimeFormat(iso = ISO.DATE_TIME) 
            ZonedDateTime from,

            @ApiParam(value = "To time", required = false, allowMultiple = false) 
            @RequestParam(value = "to", required = false)
            @DateTimeFormat(iso = ISO.DATE_TIME) 
            ZonedDateTime to,

            @ApiParam(value = "Time zone", required = false, allowMultiple = false) 
            @RequestParam(value = "timezone", required = false) 
            String timezone,
            
            @ApiParam(value = "Limit", required = false, allowMultiple = false) 
            @RequestParam(value = "limit", required = false) 
            Integer limit,
            
            @ApiParam(value = "Bookend", required = false, allowMultiple = false) 
            @RequestParam(value = "bookend", required = false, defaultValue="false") 
            boolean bookend,
            
            @AuthenticationPrincipal User user
            ) {

        ZonedDateTimeRangeQueryInfo info = new ZonedDateTimeRangeQueryInfo(request.getServerName(), 
                request.getServerPort(), 
                from, to, dateTimeFormat, timezone, RollupEnum.NONE, null, limit, 
                true, bookend, useRendered, false, true, false);
        
        return generateStream(user, info, new String[] {xid});
    }
    
    @ApiOperation(
            value = "Rollup values For 1 Data Point, return in time ascending order", 
            notes = "From time inclusive, To time exclusive.",
            response = PointValueTimeModel.class, 
            responseContainer = "Array"
            )
    @RequestMapping(method = RequestMethod.GET, value = "/time-period/{xid}/{rollup}")
    public ResponseEntity<PointValueTimeStream<PointValueTimeModel, ZonedDateTimeRangeQueryInfo>> getRollupPointValues(
            HttpServletRequest request,
            @ApiParam(value = "Point xid", required = true,  allowMultiple = false) 
            @PathVariable String xid,

            @ApiParam(value = "Rollup type", required = false, allowMultiple = false) 
            @PathVariable(value = "rollup") 
            RollupEnum rollup,
            
            @ApiParam(value = "Return rendered value as 'rendered' field", required = false,
                    defaultValue = "false", allowMultiple = false) 
            @RequestParam(required = false, defaultValue = "false") 
            boolean useRendered,
            
            @ApiParam(value = "Date Time format pattern for timestamps as strings, if not included epoch milli number is used",
            required = false, allowMultiple = false) 
            @RequestParam(value = "dateTimeFormat", required = false) 
            String dateTimeFormat,
    
            @ApiParam(value = "From time", required = false, allowMultiple = false) 
            @RequestParam(value = "from", required = false)
            @DateTimeFormat(iso = ISO.DATE_TIME) 
            ZonedDateTime from,

            @ApiParam(value = "To time", required = false, allowMultiple = false) 
            @RequestParam(value = "to", required = false)
            @DateTimeFormat(iso = ISO.DATE_TIME) 
            ZonedDateTime to,

            @ApiParam(value = "Time zone", required = false, allowMultiple = false) 
            @RequestParam(value = "timezone", required = false) 
            String timezone,
            
            @ApiParam(value = "Time Period Type", required = false, allowMultiple = false) 
            @RequestParam(value = "timePeriodType",required = false) 
            TimePeriodType timePeriodType,

            @ApiParam(value = "Time Periods", required = false,allowMultiple = false) 
            @RequestParam(value = "timePeriods", required = false) 
            Integer timePeriods,
            
            @AuthenticationPrincipal User user
            ) {

        TimePeriod timePeriod = null;
        if ((timePeriodType != null) && (timePeriods != null)) {
            timePeriod = new TimePeriod(timePeriods, timePeriodType);
        }
 
        ZonedDateTimeRangeQueryInfo info = new ZonedDateTimeRangeQueryInfo(request.getServerName(), 
                request.getServerPort(), 
                from, to, dateTimeFormat, timezone, rollup, timePeriod, null, 
                true, true, useRendered, false, true, false);
        
        return generateStream(user, info, new String[] {xid});
    }
    
    @ApiOperation(value = "Query Time Range for multiple data points, return in time ascending order",
            notes = "From time inclusive, To time exclusive. Return in single array with bookends, use limit if provided.",
            response = PointValueTimeModel.class, responseContainer = "Array")
    @RequestMapping(method = RequestMethod.GET, value = "/single-array/time-period/{xids}")
    public ResponseEntity<PointValueTimeStream<PointValueTimeModel, ZonedDateTimeRangeQueryInfo>> getPointValuesAsSingleArray(
            HttpServletRequest request,

            @ApiParam(value = "Point xids", required = true,
                    allowMultiple = true) 
            @PathVariable String[] xids,

            @ApiParam(value = "Return rendered value as 'rendered' field", required = false,
                    defaultValue = "false", allowMultiple = false) 
            @RequestParam(required = false, defaultValue = "false") 
            boolean useRendered,
            
            @ApiParam(value = "From time", required = false, allowMultiple = false) 
            @RequestParam(value = "from", required = false)
            @DateTimeFormat(iso = ISO.DATE_TIME) 
            ZonedDateTime from,

            @ApiParam(value = "To time", required = false, allowMultiple = false) 
            @RequestParam(value = "to", required = false)
            @DateTimeFormat(iso = ISO.DATE_TIME) 
            ZonedDateTime to,

            @ApiParam(value = "Time zone", required = false, allowMultiple = false)
            @RequestParam(value = "timezone", required = false) 
            String timezone,

            @ApiParam(value = "Limit", required = false, allowMultiple = false) 
            @RequestParam(value = "limit", required = false) 
            Integer limit,
            
            @ApiParam(value = "Bookend", required = false, allowMultiple = false) 
            @RequestParam(value = "bookend", required = false, defaultValue="false") 
            boolean bookend,
            
            @ApiParam(value = "Date Time format pattern for timestamps as strings, if not included epoch milli number is used",
                    required = false, allowMultiple = false) 
            @RequestParam(value = "dateTimeFormat", required = false) 
            String dateTimeFormat,
            @AuthenticationPrincipal User user
            ) {
        
        ZonedDateTimeRangeQueryInfo info = new ZonedDateTimeRangeQueryInfo(
                request.getServerName(), request.getServerPort(), 
                from, to, dateTimeFormat, timezone, RollupEnum.NONE, null, limit, 
                true, bookend, useRendered, true, true, false);
        return generateStream(user, info, xids);
    }
    
    @ApiOperation(value = "Rollup values for multiple data points, return in time ascending order",
            notes = "From time inclusive, To time exclusive. Return in single array.",
            response = PointValueTimeModel.class, responseContainer = "Array")
    @RequestMapping(method = RequestMethod.GET, value = "/single-array/time-period/{xids}/{rollup}")
    public ResponseEntity<PointValueTimeStream<PointValueTimeModel, ZonedDateTimeRangeQueryInfo>> getRollupPointValuesAsSingleArray(
            HttpServletRequest request,

            @ApiParam(value = "Point xids", required = true,
                    allowMultiple = true) 
            @PathVariable String[] xids,

            @ApiParam(value = "Rollup type", required = false, allowMultiple = false) 
            @PathVariable(value = "rollup") 
            RollupEnum rollup,
            
            @ApiParam(value = "Return rendered value as 'rendered' field", required = false,
                    defaultValue = "false", allowMultiple = false) 
            @RequestParam(required = false, defaultValue = "false") 
            boolean useRendered,
            
            @ApiParam(value = "From time", required = false, allowMultiple = false) 
            @RequestParam(value = "from", required = false)
            @DateTimeFormat(iso = ISO.DATE_TIME) 
            ZonedDateTime from,

            @ApiParam(value = "To time", required = false, allowMultiple = false) 
            @RequestParam(value = "to", required = false)
            @DateTimeFormat(iso = ISO.DATE_TIME) 
            ZonedDateTime to,

            @ApiParam(value = "Time Period Type", required = false,
                    allowMultiple = false) 
            @RequestParam(value = "timePeriodType", required = false) 
            TimePeriodType timePeriodType,

            @ApiParam(value = "Time Periods", required = false, allowMultiple = false) 
            @RequestParam(value = "timePeriods", required = false) 
            Integer timePeriods,

            @ApiParam(value = "Time zone", required = false, allowMultiple = false)
            @RequestParam(value = "timezone", required = false) 
            String timezone,

            @ApiParam(value = "Date Time format pattern for timestamps as strings, if not included epoch milli number is used",
                    required = false, allowMultiple = false) 
            @RequestParam(value = "dateTimeFormat", required = false) 
            String dateTimeFormat,
            @AuthenticationPrincipal User user
            ) {
        
        TimePeriod timePeriod = null;
        if ((timePeriodType != null) && (timePeriods != null)) {
            timePeriod = new TimePeriod(timePeriods, timePeriodType);
        }
        
        ZonedDateTimeRangeQueryInfo info = new ZonedDateTimeRangeQueryInfo(request.getServerName(), 
                request.getServerPort(), 
                from, to, dateTimeFormat, timezone, rollup, timePeriod, null, true,
                true, useRendered, true, true, false);
        return generateStream(user, info, xids);
    }
    
   
    
    @ApiOperation(value = "Query time range for multiple data points, return in time ascending order",
            notes = "From time inclusive, To time exclusive.  Returns a map of xid to values with optionally limited value arrays with bookends.",
            response = PointValueTimeModel.class, responseContainer = "Object")
    @RequestMapping(method = RequestMethod.GET, value = "/multiple-arrays/time-period/{xids}")
    public ResponseEntity<PointValueTimeStream<Map<String, List<PointValueTime>>, ZonedDateTimeRangeQueryInfo>> getPointValuesForMultiplePointsAsMultipleArrays(
            HttpServletRequest request,

            @ApiParam(value = "Point xids", required = true,
                    allowMultiple = true) @PathVariable String[] xids,

            @ApiParam(value = "Return rendered value as 'rendered' field", required = false,
                    defaultValue = "false", allowMultiple = false) @RequestParam(required = false,
                            defaultValue = "false") boolean useRendered,

            @ApiParam(value = "From time", required = false,
                    allowMultiple = false) @RequestParam(value = "from", required = false)
            @DateTimeFormat(iso = ISO.DATE_TIME) ZonedDateTime from,

            @ApiParam(value = "To time", required = false,
                    allowMultiple = false) @RequestParam(value = "to", required = false)
            @DateTimeFormat(iso = ISO.DATE_TIME) ZonedDateTime to,

            @ApiParam(value = "Time zone", required = false, allowMultiple = false) @RequestParam(
                    value = "timezone", required = false) String timezone,

            @ApiParam(value = "Limit", required = false, allowMultiple = false) @RequestParam(
                    value = "limit", required = false) Integer limit,

            @ApiParam(value = "Bookend", required = false, allowMultiple = false) 
            @RequestParam(value = "bookend", required = false, defaultValue="false") 
            boolean bookend,
            
            @ApiParam(value = "Date Time format pattern for timestamps as strings, if not included epoch milli number is used",
                    required = false, allowMultiple = false) 
            @RequestParam(value = "dateTimeFormat", required = false) String dateTimeFormat,
            @AuthenticationPrincipal User user
            ) {
        
        ZonedDateTimeRangeQueryInfo info = new ZonedDateTimeRangeQueryInfo(
                request.getServerName(), request.getServerPort(), 
                from, to, dateTimeFormat, timezone, RollupEnum.NONE, null, limit, 
                true, bookend, useRendered, false, false, false);
        
        return generateStream(user, info, xids);
    }
    
    @ApiOperation(value = "Rollup values for multiple data points, return in time ascending order",
            notes = "From time inclusive, To time exclusive.  Returns a map of xid to point value time arrays.",
            response = PointValueTimeModel.class, responseContainer = "Object")
    @RequestMapping(method = RequestMethod.GET, value = "/multiple-arrays/time-period/{xids}/{rollup}")
    public ResponseEntity<PointValueTimeStream<Map<String, List<PointValueTime>>, ZonedDateTimeRangeQueryInfo>> getRollupPointValuesAsMultipleArrays(
            HttpServletRequest request,

            @ApiParam(value = "Point xids", required = true,
                    allowMultiple = true) @PathVariable String[] xids,

            @ApiParam(value = "Rollup type", required = false, allowMultiple = false) 
            @PathVariable(value = "rollup") 
            RollupEnum rollup,
            
            @ApiParam(value = "Return rendered value as 'rendered' field", required = false,
                    defaultValue = "false", allowMultiple = false) @RequestParam(required = false,
                            defaultValue = "false") boolean useRendered,

            @ApiParam(value = "From time", required = false,
                    allowMultiple = false) @RequestParam(value = "from", required = false)
            @DateTimeFormat(iso = ISO.DATE_TIME) ZonedDateTime from,

            @ApiParam(value = "To time", required = false,
                    allowMultiple = false) @RequestParam(value = "to", required = false)
            @DateTimeFormat(iso = ISO.DATE_TIME) ZonedDateTime to,

            @ApiParam(value = "Time Period Type", required = false,
                    allowMultiple = false) @RequestParam(value = "timePeriodType",
                            required = false) TimePeriodType timePeriodType,

            @ApiParam(value = "Time Periods", required = false,
                    allowMultiple = false) @RequestParam(value = "timePeriods",
                            required = false) Integer timePeriods,

            @ApiParam(value = "Time zone", required = false, allowMultiple = false) @RequestParam(
                    value = "timezone", required = false) String timezone,

            @ApiParam(value = "Date Time format pattern for timestamps as strings, if not included epoch milli number is used",
                    required = false, allowMultiple = false) 
            @RequestParam(value = "dateTimeFormat", required = false) String dateTimeFormat,
            @AuthenticationPrincipal User user
            ) {
        
        TimePeriod timePeriod = null;
        if ((timePeriodType != null) && (timePeriods != null)) {
            timePeriod = new TimePeriod(timePeriods, timePeriodType);
        }
        
        ZonedDateTimeRangeQueryInfo info = new ZonedDateTimeRangeQueryInfo(
                request.getServerName(), request.getServerPort(), 
                from, to, dateTimeFormat, timezone, rollup, timePeriod, null, 
                true, true, useRendered, false, false, false);
        
        return generateStream(user, info, xids);
    }
    
    @ApiOperation(
            value = "Import Point Values for one or many Data Points",
            notes = "Data Point must exist and user must have write access"
            )
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<Collection<PointValueImportResult>> savePointsValues(HttpServletRequest request,
            @RequestBody(required = true) List<XidPointValueTimeModel> models,
            @AuthenticationPrincipal User user
            ) {

        //Map of XIDs to results
        Map<String, PointValueImportResult> results = new HashMap<String, PointValueImportResult>();
        
        for(XidPointValueTimeModel model : models) {
            PointValueImportResult result = results.get(model.getXid());
            if(result == null) {
                result = new PointValueImportResult(model.getXid(), dao, user);
                results.put(model.getXid(), result);
            }
            //Attempt to save it
            result.saveValue(model);
        }
        
        return ResponseEntity.ok(results.values());
    }
    
    @ApiOperation(
            value = "Delete point values >= from  and < to",
            notes = "The user must have set permission to the data point. If date is not supplied it defaults to now."
            )
    @RequestMapping(method = RequestMethod.DELETE, value = "/{xid}", produces={"application/json", "text/csv", "application/sero-json"})
    public ResponseEntity<Long> deletePointValues(
            @ApiParam(value = "Point xids", required = true)
            @PathVariable 
            String xid,
            
            @ApiParam(value = "From time", required = false,
                allowMultiple = false)
            @RequestParam(value = "from", required = false)
            @DateTimeFormat(iso = ISO.DATE_TIME) 
            ZonedDateTime from,

            @ApiParam(value = "To time", required = false,
                allowMultiple = false) 
            @RequestParam(value = "to", required = false)
            @DateTimeFormat(iso = ISO.DATE_TIME) 
            ZonedDateTime to,

            @ApiParam(value = "Time zone", required = false, allowMultiple = false) 
            @RequestParam(value = "timezone", required = false) 
            String timezone,

            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder, 
            HttpServletRequest request) {
    
        
        DataPointVO vo = DataPointDao.instance.getByXid(xid);
        if (vo == null) {
            throw new NotFoundRestException();
        }else {
            if(!Permissions.hasDataPointSetPermission(user, vo))
                throw new AccessDeniedException();
        }
        
        ZoneId zoneId;
        if (timezone == null) {
            if (from != null) {
                zoneId = from.getZone();
            } else if (to != null)
                zoneId = to.getZone();
            else
                zoneId = TimeZone.getDefault().toZoneId();
        } else {
            zoneId = ZoneId.of(timezone);
        }

        // Set the timezone on the from and to dates
        long current = Common.timer.currentTimeMillis();
        if (from != null)
            from = from.withZoneSameInstant(zoneId);
        else
            from = ZonedDateTime.ofInstant(Instant.ofEpochMilli(current), zoneId);
        if (to != null)
            to = to.withZoneSameInstant(zoneId);
        else
            to = ZonedDateTime.ofInstant(Instant.ofEpochMilli(current), zoneId);

        return ResponseEntity.ok(Common.runtimeManager.purgeDataPointValuesBetween(vo.getId(), from.toInstant().toEpochMilli(), to.toInstant().toEpochMilli()));
    }

    /**
     * The Hard Working Value Generation Logic for Latest Value Queries
     * 
     * @param user
     * @param info
     * @param xids
     * @return
     */
    protected <T, INFO extends LatestQueryInfo> ResponseEntity<PointValueTimeStream<T, INFO>> generateLatestStream(User user, INFO info, String[] xids){
        //Build the map, check permissions
        Map<Integer, DataPointVO> voMap = buildMap(user, xids);
        return ResponseEntity.ok(new MultiPointLatestDatabaseStream<T, INFO>(info, voMap, this.dao));
    }
    
    /**
     * The Hard Working Value Generation Logic for Time Range Queries
     * @param user
     * @param info
     * @param xids
     * @return
     */
    protected <T, INFO extends ZonedDateTimeRangeQueryInfo> ResponseEntity<PointValueTimeStream<T, INFO>> generateStream(User user, INFO info, String[] xids){
        
        //Build the map, check permissions
        Map<Integer, DataPointVO> voMap = buildMap(user, xids);
        
        // Are we using rollup
        if (info.getRollup() != RollupEnum.NONE) {
            return ResponseEntity.ok(new MultiDataPointStatisticsQuantizerStream<T, INFO>(info, voMap, this.dao));
        } else {
            return ResponseEntity.ok(new MultiPointTimeRangeDatabaseStream<T, INFO>(info, voMap, this.dao));
        }
    }
    
    /**
     * Build and validate the map of Requested Data Points
     * @param user
     * @param xids
     * @return
     */
    protected Map<Integer, DataPointVO> buildMap(User user, String[] xids){
        //Build the map, check permissions
        Map<Integer, DataPointVO> voMap = new HashMap<Integer, DataPointVO>();
        for(String xid : xids) {
            DataPointVO vo = DataPointDao.instance.getByXid(xid);
            if (vo == null) {
                throw new NotFoundRestException();
            }else {
                if(!Permissions.hasDataPointReadPermission(user, vo))
                    throw new AccessDeniedException();
            }
            voMap.put(vo.getId(), vo);
        }
        
        //Do we have any points
        if(voMap.isEmpty())
            throw new NotFoundRestException();
        return voMap;
    }
}