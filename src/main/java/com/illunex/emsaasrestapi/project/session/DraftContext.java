package com.illunex.emsaasrestapi.project.session;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import org.bson.types.ObjectId;

@Getter
public class DraftContext {
    private final boolean draft;
    private final ObjectId sessionId;

    private DraftContext(boolean draft, ObjectId sessionId) {
        this.draft = draft;
        this.sessionId = sessionId;
    }

    public static DraftContext from(HttpServletRequest req) {
        String m = req.getHeader("X-Draft-Mode");
        boolean draft = "1".equals(m) || "true".equalsIgnoreCase(m);
        String sid = req.getHeader("X-Session-Id");
        ObjectId sessionId = (sid != null && ObjectId.isValid(sid)) ? new ObjectId(sid) : null;
        return new DraftContext(draft, sessionId);
    }

    public void require() {
        if (!draft || sessionId == null) throw new IllegalArgumentException("Draft mode requires X-Draft-Mode & X-Session-Id");
    }
}