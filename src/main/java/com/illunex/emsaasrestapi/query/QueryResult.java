package com.illunex.emsaasrestapi.query;

import org.springframework.data.mongodb.core.query.Query;

public record QueryResult(Query query, String collection) {
}
