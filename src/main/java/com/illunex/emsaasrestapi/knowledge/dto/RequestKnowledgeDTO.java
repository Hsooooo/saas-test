package com.illunex.emsaasrestapi.knowledge.dto;

import com.illunex.emsaasrestapi.common.code.EnumCode;
import com.illunex.emsaasrestapi.common.validation.ValidEnumCode;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class RequestKnowledgeDTO {
    @Getter
    @Setter
    public static class CreateNode {
        @NotNull
        private Integer partnershipIdx;
        @ValidEnumCode(enumClass = EnumCode.KnowledgeGardenNode.TypeCd.class, message = "Invalid type code")
        private String typeCd;
        @NotNull
        private String label;
        private String content;
        private String noteStatusCd;
        private Integer chatHistoryIdx;
        private Integer parentNodeIdx;
    }

    @Getter
    @Setter
    public static class UpdateNode {
        private Integer partnershipIdx;
        private Integer nodeIdx;
        private String label;
        private String content;
        private String noteStatusCd;
        private List<Integer> keywordNodeIdxList;
        private List<Integer> referenceNodeIdxList;
    }

    @Getter
    @Setter
    public static class NodeSearch {
        @NotNull
        private Integer partnershipIdx;
        private String searchStr = "";
        private String[] includeTypes;
        private Integer limit = 50;
    }

    @Getter
    @Setter
    public static class ExtendSearch {
        @NotNull
        private Integer partnershipIdx;
        @NotNull
        private Integer nodeIdx;
        @NotNull
        private Integer depth;
    }

    @Getter
    @Setter
    public static class TreePosition {
        @NotNull
        private Integer partnershipIdx;
        @NotNull
        private Integer nodeIdx;
        @NotNull
        private Integer targetNodeIdx;
        @NotNull
        private DropPosition position;

        public enum DropPosition {
            INTO,
            BEFORE,
            AFTER
        }
    }

    @Getter
    @Setter
    public static class TrashSearch {
        @NotNull
        private Integer partnershipIdx;
        private String searchStr = "";
        private String[] includeTypes;
        private Integer limit = 50;
    }
}
