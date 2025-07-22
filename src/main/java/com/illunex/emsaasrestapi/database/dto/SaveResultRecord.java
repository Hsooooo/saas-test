package com.illunex.emsaasrestapi.database.dto;

public record SaveResultRecord(Object action, Object id, String type) {
    public static SaveResultRecord created(Object id, String type) {
        return new SaveResultRecord(SaveAction.CREATED, id, type);
    }

    public static SaveResultRecord updated(Object id, String type) {
        return new SaveResultRecord(SaveAction.UPDATED, id, type);
    }

    public static SaveResultRecord failed(Object id, String type) {
        return new SaveResultRecord(SaveAction.FAILED, id, type);
    }

    public static SaveResultRecord skipped(Object id, String type) {
        return new SaveResultRecord(SaveAction.SKIPPED, id, type);
    }

    public enum SaveAction { CREATED, UPDATED, FAILED, SKIPPED }
}
