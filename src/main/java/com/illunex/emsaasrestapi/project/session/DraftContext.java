package com.illunex.emsaasrestapi.project.session;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import org.bson.types.ObjectId;

@Getter
public class DraftContext {
    private final ObjectId sessionId;

    private DraftContext(ObjectId sessionId) {
        this.sessionId = sessionId;
    }

    public static DraftContext from(HttpServletRequest req) {
        String sid = req.getHeader("X-Session-Id");
        ObjectId sessionId = (sid != null && ObjectId.isValid(sid)) ? new ObjectId(sid) : null;
        return new DraftContext(sessionId);
    }

    public void require() {
        if (sessionId == null) throw new IllegalArgumentException("Draft mode requires X-Draft-Mode & X-Session-Id");
    }

    public boolean hasSession() { return sessionId != null; }
}