package com.illunex.emsaasrestapi.project;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.illunex.emsaasrestapi.common.CustomException;
import com.illunex.emsaasrestapi.common.CustomResponse;
import com.illunex.emsaasrestapi.common.ErrorCode;
import com.illunex.emsaasrestapi.common.code.EnumCode;
import com.illunex.emsaasrestapi.partnership.mapper.PartnershipMapper;
import com.illunex.emsaasrestapi.partnership.vo.PartnershipVO;
import com.illunex.emsaasrestapi.partnership.dto.ResponsePartnershipDTO;
import com.illunex.emsaasrestapi.partnership.mapper.PartnershipMemberMapper;
import com.illunex.emsaasrestapi.partnership.vo.PartnershipMemberPreviewVO;
import com.illunex.emsaasrestapi.project.document.Project;
import com.illunex.emsaasrestapi.project.document.ProjectId;
import com.illunex.emsaasrestapi.project.dto.RequestProjectDTO;
import com.illunex.emsaasrestapi.project.dto.ResponseProjectDTO;
import com.illunex.emsaasrestapi.project.mapper.ProjectCategoryMapper;
import com.illunex.emsaasrestapi.project.mapper.ProjectMapper;
import com.illunex.emsaasrestapi.project.mapper.ProjectMemberMapper;
import com.illunex.emsaasrestapi.project.vo.ProjectCategoryVO;
import com.illunex.emsaasrestapi.project.vo.ProjectMemberVO;
import com.illunex.emsaasrestapi.project.vo.ProjectVO;
import com.mongodb.client.result.UpdateResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.BeanUtils;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProjectService {
    private final ProjectMapper projectMapper;
    private final ProjectCategoryMapper projectCategoryMapper;
    private final ProjectMemberMapper projectMemberMapper;
    private final PartnershipMapper partnershipMapper;
    private final PartnershipMemberMapper partnershipMemberMapper;

    private final MongoTemplate mongoTemplate;
    private final ModelMapper modelMapper;

    /**
     * 프로젝트 생성
     * @param project
     * @return
     */
    public CustomResponse<?> createProject(RequestProjectDTO.Project project) throws CustomException {
        // RDB 처리 부분
        ProjectVO projectVO = ProjectVO.builder()
                .projectCategoryIdx(project.getProjectId().getProjectCategoryIdx())
                .title(project.getTitle())
                .description(project.getDescription())
                .statusCd(EnumCode.Project.StatusCd.MongoDB.getCode())
                .build();
        // 프로젝트 저장
        int insertCnt = projectMapper.insertProjectVO(projectVO);

        if(insertCnt > 0) {
            // MongoDB 처리 부분
//            mongoTemplate.find(Query.query(Criteria.where("idx").is(1)), RequestProjectDTO.Project.class);
            // Document 맵핑
            Project mappingProject = modelMapper.map(project, Project.class);
            // projectIdx 업데이트
            mappingProject.getProjectId().setProjectIdx(projectVO.getIdx());

            mongoTemplate.insert(mappingProject);

//            List<Project> result = mongoTemplate.find(Query.query(Criteria.where("nodeList").elemMatch(Criteria.where("nodeType").is("Company"))), Project.class);

            Project result = mongoTemplate.findOne(Query.query(Criteria.where("projectId").is(mappingProject.getProjectId())), Project.class);

            if (result == null) {
                throw new CustomException(ErrorCode.COMMON_INTERNAL_SERVER_ERROR);
            }

            return CustomResponse.builder()
                    .data(modelMapper.map(result, ResponseProjectDTO.Project.class))
                    .build();
        }
        throw new CustomException(ErrorCode.COMMON_INTERNAL_SERVER_ERROR);
    }

    /**
     * 단일 엑셀 파일 업로드
     * @param projectIdx
     * @param excelFile
     * @return
     */
    public CustomResponse<?> uploadSingleExcelFile(Integer projectIdx, MultipartFile excelFile) throws CustomException, IOException {
        // 확장자 체크
        String ext = FilenameUtils.getExtension(excelFile.getOriginalFilename());
        if(ext == null || !ext.equals("xlsx") && !ext.equals("xls")) {
            throw new CustomException(ErrorCode.PROJECT_INVALID_FILE_EXTENSION);
        }

        Workbook workbook = null;
        if (ext.equals("xlsx")) {
            workbook = new XSSFWorkbook(excelFile.getInputStream());
        } else {
            workbook = new HSSFWorkbook(excelFile.getInputStream());
        }

        ResponseProjectDTO.ExcelData excelData = ResponseProjectDTO.ExcelData.builder()
                .sheetList(new ArrayList<>())
                .build();
        // 시트명 목록
//        List<String> sheetList = new ArrayList<>();

        // 탭 읽기
        for(int sheetCnt = 0; sheetCnt < workbook.getNumberOfSheets(); sheetCnt++) {
            // Cell 목록
            List<String> cellList = new ArrayList<>();
            // Row 데이터 정보
            List<LinkedHashMap<String, Object>> rowList = new ArrayList<>();

            Sheet workSheet = workbook.getSheetAt(sheetCnt);
            // 데이터 개수 체크
            if(workSheet.getLastRowNum() <= 1) {
                // row 데이터 없음
                workbook.close();
                throw new CustomException(ErrorCode.PROJECT_INVALID_FILE_DATA_ROW_EMPTY);
            }

            // 첫번째 행에서 컬럼명 추출
            Row firstRow = workSheet.getRow(0);
            if(firstRow == null) {
                // column 데이터 없음
                workbook.close();
                throw new CustomException(ErrorCode.PROJECT_INVALID_FILE_DATA_COLUMN_EMPTY);
            }

            // Cell, Row 최대 개수 저장
            int totalCellCnt = firstRow.getLastCellNum();
            int totalRowCnt = workSheet.getLastRowNum();

            // 첫번째 행에 컬럼 수 만큼 컬럼명 추출
            for(int cellCnt = 0; cellCnt < totalCellCnt; cellCnt++) {
                if(firstRow.getCell(cellCnt).getStringCellValue().isEmpty()) {
                    // 셀에 빈값이면 컬럼 총개수 감소
                    totalCellCnt--;
                } else {
                    cellList.add(firstRow.getCell(cellCnt).getStringCellValue());
                }
            }

            // 컬럼명과 1:1로 매칭하여 데이터 정제
            // 행별 컬럼 수만큼 반복적으로
            for(int rowCnt = 1; rowCnt < totalRowCnt; rowCnt++) {
                Row row = workSheet.getRow(rowCnt);
                if(row == null) {
                    // 열 데이터가 없으면 종료
                    break;
                }
                LinkedHashMap<String, Object> dataMap = new LinkedHashMap<>();
                for (int cellCnt = 0; cellCnt < cellList.size(); cellCnt++) {
                    dataMap.put(cellList.get(cellCnt), getColumnData(row.getCell(cellCnt)));
                }
                rowList.add(dataMap);
            }

            // 엑셀 데이터에 시트별로 저장
            excelData.getSheetList()
                    .add(ResponseProjectDTO.Sheet.builder()
                            .sheetName(workSheet.getSheetName())
                            .cellList(cellList)
                            .rowList(rowList)
                            .build());
        }

        return CustomResponse.builder()
                .data(excelData)
                .build();
    }

    /**
     * 프로젝트 삭제
     * @param projectId
     * @return
     */
    public CustomResponse<?> deleteProject(RequestProjectDTO.ProjectId projectId) throws CustomException {
        // 프로젝트 조회
        Project findProject = mongoTemplate.findById(projectId, Project.class);
        if(findProject == null) {
            throw new CustomException(ErrorCode.COMMON_EMPTY);
        }

        //maria삭제
        projectMapper.deleteByIdx(projectId.getProjectIdx());

        return CustomResponse.builder()
                .data(mongoTemplate.remove(findProject))
                .build();
    }

    /**
     * 프로젝트 조회
     * @param projectIdx
     * @param partnershipIdx
     * @return
     */
    public CustomResponse<?> getProject(Integer projectIdx, Integer partnershipIdx) throws CustomException {
        // 프로젝트 조회
        Query query = Query.query(Criteria.where("projectId.projectIdx").is(projectIdx).and("projectId.partnershipIdx").is(partnershipIdx));
        Project findProject = mongoTemplate.findOne(query, Project.class);
        if(findProject == null) {
            throw new CustomException(ErrorCode.COMMON_EMPTY);
        }

        ResponseProjectDTO.Project response = modelMapper.map(findProject, ResponseProjectDTO.Project.class);

        return CustomResponse.builder()
                .data(response)
                .build();
    }

    /**
     * 프로젝트 한번에 수정
     * @param project
     * @return
     */
    public CustomResponse<?> replaceProject(RequestProjectDTO.Project project) throws CustomException {
        // 프로젝트 조회
        Project targetProject = mongoTemplate.findById(project.getProjectId(), Project.class);
        if(targetProject == null) {
            throw new CustomException(ErrorCode.COMMON_EMPTY);
        }

        // 데이터 맵핑
        Project replaceProject = modelMapper.map(project, Project.class);

        // 데이터 덮어쓰기
        UpdateResult result = mongoTemplate.replace(Query.query(Criteria.where("projectId").is(project.getProjectId())), replaceProject);

        if(result.getModifiedCount() == 0) {
            throw new CustomException(ErrorCode.COMMON_INTERNAL_SERVER_ERROR);
        }

        return CustomResponse.builder()
                .data(result)
                .build();
    }

    /**
     * cell 타입에 맞게 데이터 반환
     * @param cell
     * @return
     * @throws CustomException
     */
    private Object getColumnData(Cell cell) throws CustomException {
        if(cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING -> {
                return cell.getStringCellValue();
            }
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue();
                } else {
                    return cell.getNumericCellValue();
                }
            }
            case BOOLEAN -> {
                return cell.getBooleanCellValue();
            }
            // 수식 셀은 getCellFormula() 또는 evaluate 사용
            case FORMULA -> {
                return cell.getCellFormula();
            }
            case BLANK -> {
                return "";
            }
            case ERROR -> {
                return cell.getErrorCellValue();
            }
            default -> throw new CustomException(ErrorCode.COMMON_INVALID_FILE_EXTENSION);
        }
    }

    /**
     * 프로젝트 카테고리 조회
     * @param user
     * @return
     * @throws CustomException
     */
    public CustomResponse<?> getProjectCategory(User user) throws CustomException {
        PartnershipVO partnershipVO = getPartnershipVOFromUser(user);

        Integer partnershipIdx = partnershipVO.getIdx();
        List<ProjectCategoryVO> projectCategoryVOList = projectCategoryMapper.selectAllByPartnershipIdx(partnershipIdx);

        List<ResponseProjectDTO.ProjectCategory> result = modelMapper.map(projectCategoryVOList, new TypeToken<List<ResponseProjectDTO.ProjectCategory>>() {}.getType());

        // 카테고리별 프로젝트 개수 세팅
        for(ResponseProjectDTO.ProjectCategory res : result){
            Integer cnt = projectMapper.countByProjectCategoryIdx(res.getIdx());
            res.setProjectCnt(cnt);
        }

        return CustomResponse.builder()
                .data(result)
                .build();
    }

    public CustomResponse<?> updateProjectCategory(RequestProjectDTO.ProjectCategoryModify projectCategoryModify, User user) throws CustomException {
        PartnershipVO partnershipVO = getPartnershipVOFromUser(user);

        List<ProjectCategoryVO> projectCategoryVOList = modelMapper.map(projectCategoryModify.getProjectCategoryList(), new TypeToken<List<ProjectCategoryVO>>() {}.getType());

        // 카테고리 삭제
        if (!CollectionUtils.isEmpty(projectCategoryModify.getDeleteCategoryIds())) {
            for (Integer idx : projectCategoryModify.getDeleteCategoryIds()) {
                // 삭제되는 카테고리에 포함된 프로젝트는 기본 카테고리로 넣어줌
                List<ProjectVO> projectVOList = projectMapper.selectAllByProjectCategoryIdx(idx);
                if (!CollectionUtils.isEmpty(projectVOList)) {
                    // TODO[JCW] : 기본 카테고리를 어떻게 잡을지 정해야됨
                    ProjectCategoryVO defaultCategory = projectCategoryVOList.stream()
                            .filter(category -> category.getName().equals("미분류")).findFirst()
                            .orElseThrow(() -> new CustomException(ErrorCode.COMMON_EMPTY));
                    for (ProjectVO projectVO : projectVOList) {
                        projectVO.setProjectCategoryIdx(defaultCategory.getIdx());
                        projectMapper.updateProjectCategoryIdxByProjectVO(projectVO);
                    }
                }
                projectCategoryMapper.deleteByIdx(idx);
            }
        }

        // 카테고리 추가 & 수정
        for (ProjectCategoryVO projectCategoryVO : projectCategoryVOList) {
            if (projectCategoryVO.getIdx() == null) {
                projectCategoryVO.setPartnershipIdx(partnershipVO.getIdx());
                projectCategoryMapper.insertByProjectCategoryVO(projectCategoryVO);
            } else {
                projectCategoryMapper.updateByProjectCategoryVO(projectCategoryVO);
            }
        }

        // 추가 및 수정 완료된 카테고리 리스트를 재조회
        List<ProjectCategoryVO> result = projectCategoryMapper.selectAllByPartnershipIdx(partnershipVO.getIdx());

        // response DTO 맵핑
        List<ResponseProjectDTO.ProjectCategory> response = modelMapper
                .map(result, new TypeToken<List<ResponseProjectDTO.ProjectCategory>>() {}.getType());

        // 카테고리별 프로젝트 개수 세팅
        for (ResponseProjectDTO.ProjectCategory projectCategory : response) {
            Integer cnt = projectMapper.countByProjectCategoryIdx(projectCategory.getIdx());
            projectCategory.setProjectCnt(cnt);
        }

        return CustomResponse.builder()
                .data(response)
                .build();
    }



    /**
     * 프로젝트 카테고리 이동
     * @param projectId
     * @return
     * @throws CustomException
     */
    public CustomResponse<?> moveProject(List<RequestProjectDTO.ProjectId> projectId) throws CustomException {
        for(RequestProjectDTO.ProjectId dto : projectId){
            //maraiDB
            ProjectVO projectVO = ProjectVO.builder()
                    .idx(dto.getProjectIdx())
                    .projectCategoryIdx(dto.getProjectCategoryIdx())
                    .build();

            projectMapper.updateProjectCategoryIdxByProjectVO(projectVO);
        }

        return CustomResponse.builder()
                .data(null)
                .message("카테고리 이동 되었습니다.")
                .build();
    }



    /**
     * 카테고리별 프로젝트 단순 내용 조회
     * @param projectId
     * @return
     * @throws CustomException
     */
    public CustomResponse<?> selectProject(RequestProjectDTO.ProjectId projectId) throws CustomException {

        //TODO[pyj]: 파트너쉽 정보 필요
        Integer partnershipIdx = 1;

        // 프로젝트 조회
        List<ProjectVO> projectList = projectMapper.selectAllByProjectCategoryIdx(projectId.getProjectCategoryIdx());

        List<ResponseProjectDTO.ProjectPreview> result = projectList.stream()
                .map(vo -> modelMapper.map(vo, ResponseProjectDTO.ProjectPreview.class))
                .toList();

        //구성원 조회
        for(ResponseProjectDTO.ProjectPreview dto : result){
            List<PartnershipMemberPreviewVO> memberList = partnershipMemberMapper.selectAllByProjectIdx(dto.getProjectIdx());
            List<ResponsePartnershipDTO.MemberPreview> memberPreview = memberList.stream()
                    .map(vo -> ResponsePartnershipDTO.MemberPreview.builder()
                            .memberIdx(vo.getIdx())
                            .name(vo.getName())
                            .profileImageUrl(vo.getProfileImageUrl())
                            .profileImagePath(vo.getProfileImagePath())
                            .build()
                    ).toList();
            dto.setMember(memberPreview);
        }


        return CustomResponse.builder()
                .data(result)
                .build();
    }

    @Transactional(rollbackFor = Exception.class)
    public CustomResponse<?> copyProject(List<RequestProjectDTO.ProjectId> projectIds) throws CustomException, JsonProcessingException {
        for(RequestProjectDTO.ProjectId projectId : projectIds){
            // 프로젝트 조회
            ProjectVO projectVO = projectMapper.selectByIdx(projectId.getProjectIdx())
                    .orElseThrow(() -> new CustomException(ErrorCode.COMMON_EMPTY));

            // Maria 프로젝트 복제
            projectVO.setIdx(null);
            int insertCnt = projectMapper.insertProjectVO(projectVO);
            if (insertCnt == 0) {
                throw new CustomException(ErrorCode.COMMON_INTERNAL_SERVER_ERROR);
            }

            // Maria 프로젝트 멤버 복제
            List<ProjectMemberVO> projectMemberList = projectMemberMapper.selectAllByProjectIdx(projectId.getProjectIdx());
            for (ProjectMemberVO projectMemberVO : projectMemberList) {
                projectMemberVO.setIdx(null);
                int insertMemberCnt = projectMemberMapper.insertByProjectMemberVO(projectMemberVO);
                if (insertMemberCnt == 0) {
                    throw new CustomException(ErrorCode.COMMON_INTERNAL_SERVER_ERROR);
                }
            }

            // TODO[JCW] : 노드 및 엣지 추가 필요

            // MongoDB 조회
            Project findProject = mongoTemplate.findById(projectId, Project.class);
            if(findProject == null) {
                throw new CustomException(ErrorCode.COMMON_EMPTY);
            }

            // MongoDB 복제
            ObjectMapper mapper = new ObjectMapper();
            Project copiedProject = mapper.readValue(mapper.writeValueAsString(findProject), Project.class);

            // 복제된 프로젝트 Idx 삽입
            copiedProject.getProjectId().setProjectIdx(projectVO.getIdx());

            // MongoDB 저장
            mongoTemplate.save(copiedProject);
        }

        return CustomResponse.builder()
                .data(null)
                .build();
    }

    private PartnershipVO getPartnershipVOFromUser(User user) throws CustomException {
        // TODO[JCW] : partnershipId를 어디서 가져올 것인지 정해야함. parameter or userDetailService
//        if (user == null) {
//            throw new CustomException(ErrorCode.COMMON_FAIL_AUTHENTICATION);
//        }
//
//        String email = user.getUsername();
//        String domain = email.split("@")[1];

        // 테스트를 위한 임시 선언
        String domain = "1";

        return partnershipMapper.selectByDomain(domain)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMON_EMPTY));
    }
}
