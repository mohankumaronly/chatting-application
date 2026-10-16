package com.rockranger.chat.common.error;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {

    private String type;
    private String title;
    private int status;
    private String code;
    private String detail;
    private String traceId;
    private Instant timestamp;
    private List<FieldErrorItem> fieldErrors;
    private Map<String, Object> extensions;

    public ApiError() {
        this.timestamp = Instant.now();
    }

    public ApiError(String type, String title, int status, String code, String detail, String traceId) {
        this.type = type;
        this.title = title;
        this.status = status;
        this.code = code;
        this.detail = detail;
        this.traceId = traceId;
        this.timestamp = Instant.now();
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }
    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    public List<FieldErrorItem> getFieldErrors() { return fieldErrors; }
    public void setFieldErrors(List<FieldErrorItem> fieldErrors) { this.fieldErrors = fieldErrors; }
    public Map<String, Object> getExtensions() { return extensions; }
    public void setExtensions(Map<String, Object> extensions) { this.extensions = extensions; }

    public static class FieldErrorItem {
        private String field;
        private String code;
        private String message;

        public FieldErrorItem() {}

        public FieldErrorItem(String field, String code, String message) {
            this.field = field;
            this.code = code;
            this.message = message;
        }

        public String getField() { return field; }
        public void setField(String field) { this.field = field; }
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}
