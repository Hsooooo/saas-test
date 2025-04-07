package com.illunex.emsaasrestapi.common.code.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "code", indexes = {@Index(columnList = "firstCode"), @Index(columnList = "secondCode"), @Index(columnList = "thirdCode"), @Index(columnList = "seq")})
public class Code {
    @Id
    @Column(nullable = false, length = 7, name = "code", unique = true)
    private String code;

    @Column(nullable = false, length = 3)
    private String firstCode;

    @Column(nullable = false, length = 2)
    private String secondCode;

    @Column(nullable = false, length = 2)
    private String thirdCode;

    @Column(nullable = false)
    private String codeValue;

    @Column(nullable = false)
    private Integer seq;
}
