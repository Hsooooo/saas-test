package com.illunex.emsaasrestapi.query;

import com.illunex.emsaasrestapi.query.dto.RequestQueryDTO;
import com.mongodb.MongoException;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.bson.json.JsonParseException;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class QueryComponent {
    private final MongoTemplate mongoTemplate;


//    public Object executeAggregate(RequestQueryDTO.ExecuteQuery executeQuery) {
//        // Logic to execute an aggregate operation
//        // This is a placeholder for actual implementation
//        System.out.println("Executing aggregate query: " + executeQuery.getQuery() + " for project: " + executeQuery.getProjectIdx());
//        return null; // Return appropriate response or object
//    }
//
//
//    public Object executeInsert(RequestQueryDTO.ExecuteQuery executeQuery) {
//        // Logic to execute an insert operation
//        // This is a placeholder for actual implementation
//        System.out.println("Executing insert query: " + executeQuery.getQuery() + " for project: " + executeQuery.getProjectIdx());
//        return null; // Return appropriate response or object
//    }
}
