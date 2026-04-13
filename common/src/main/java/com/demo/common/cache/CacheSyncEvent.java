package com.demo.common.cache;

import java.util.Map;

public class CacheSyncEvent {

    private String eventId;
    private String db;
    private String table;
    private String opType;
    private Map<String, Object> pk;
    private Map<String, Object> before;
    private Map<String, Object> after;
    private Long ts;
    private String sourcePos;
    private String traceId;

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getDb() {
        return db;
    }

    public void setDb(String db) {
        this.db = db;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getOpType() {
        return opType;
    }

    public void setOpType(String opType) {
        this.opType = opType;
    }

    public Map<String, Object> getPk() {
        return pk;
    }

    public void setPk(Map<String, Object> pk) {
        this.pk = pk;
    }

    public Map<String, Object> getBefore() {
        return before;
    }

    public void setBefore(Map<String, Object> before) {
        this.before = before;
    }

    public Map<String, Object> getAfter() {
        return after;
    }

    public void setAfter(Map<String, Object> after) {
        this.after = after;
    }

    public Long getTs() {
        return ts;
    }

    public void setTs(Long ts) {
        this.ts = ts;
    }

    public String getSourcePos() {
        return sourcePos;
    }

    public void setSourcePos(String sourcePos) {
        this.sourcePos = sourcePos;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }
}
