package com.illunex.emsaasrestapi.project;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.illunex.emsaasrestapi.common.CustomException;
import com.illunex.emsaasrestapi.common.CustomResponse;
import com.illunex.emsaasrestapi.common.ErrorCode;
import com.illunex.emsaasrestapi.common.code.EnumCode;
import com.illunex.emsaasrestapi.partnership.dto.ResponsePartnershipDTO;
import com.illunex.emsaasrestapi.partnership.mapper.PartnershipMapper;
import com.illunex.emsaasrestapi.partnership.mapper.PartnershipMemberMapper;
import com.illunex.emsaasrestapi.partnership.vo.PartnershipMemberPreviewVO;
import com.illunex.emsaasrestapi.partnership.vo.PartnershipVO;
import com.illunex.emsaasrestapi.project.document.data.Data;
import com.illunex.emsaasrestapi.project.document.data.DataRow;
import com.illunex.emsaasrestapi.project.document.data.DataRowId;
import com.illunex.emsaasrestapi.project.document.data.DataSheet;
import com.illunex.emsaasrestapi.project.document.project.Project;
import com.illunex.emsaasrestapi.project.dto.RequestProjectDTO;
import com.illunex.emsaasrestapi.project.dto.ResponseProjectDTO;
import com.illunex.emsaasrestapi.project.mapper.ProjectCategoryMapper;
import com.illunex.emsaasrestapi.project.mapper.ProjectMapper;
import com.illunex.emsaasrestapi.project.mapper.ProjectMemberMapper;
import com.illunex.emsaasrestapi.project.vo.ProjectMemberVO;
import com.illunex.emsaasrestapi.project.vo.ProjectVO;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.modelmapper.ModelMapper;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    private final ProjectComponent projectComponent;

    /**
     * 프로젝트 생성
     * @param project
     * @return
     */
    @Transactional
    public CustomResponse<?> createProject(RequestProjectDTO.Project project) throws CustomException {
        // TODO : 파트너쉽에 속한 회원 여부 체크
        // TODO : 해당 프로젝트 권한 여부 체크
        // RDB 처리 부분
        ProjectVO projectVO = ProjectVO.builder()
                .partnershipIdx(project.getProjectId().getPartnershipIdx())
                .projectCategoryIdx(project.getProjectId().getProjectCategoryIdx())
                .title(project.getTitle())
                .description(project.getDescription())
                .statusCd(EnumCode.Project.StatusCd.MongoDB.getCode())
                .build();
        // 프로젝트 저장
        int insertCnt = projectMapper.insertByProjectVO(projectVO);

        if(insertCnt > 0) {
            // MongoDB 처리 부분
//            mongoTemplate.find(Query.query(Criteria.where("idx").is(1)), RequestProjectDTO.Project.class);
            // Document 맵핑
            Project mappingProject = modelMapper.map(project, Project.class);
            // projectIdx 업데이트
            mappingProject.getProjectId().setProjectIdx(projectVO.getIdx());

            // 기존 데이터 확인
            Project result = mongoTemplate.findOne(Query.query(Criteria.where("projectId").is(mappingProject.getProjectId())), Project.class);
            if (result == null) {
                // 없을 경우 추가
                mongoTemplate.insert(mappingProject);
            } else {
                // 있을 경우 업데이트
                mongoTemplate.replace(Query.query(Criteria.where("projectId").is(mappingProject.getProjectId())), mappingProject);
            }
            // 변경 된 데이터 조회
            result = mongoTemplate.findOne(Query.query(Criteria.where("projectId").is(mappingProject.getProjectId())), Project.class);
//            List<Project> result = mongoTemplate.find(Query.query(Criteria.where("nodeList").elemMatch(Criteria.where("nodeType").is("Company"))), Project.class);

            return CustomResponse.builder()
                    .data(modelMapper.map(result, ResponseProjectDTO.Project.class))
                    .build();
        }
        throw new CustomException(ErrorCode.COMMON_INTERNAL_SERVER_ERROR);
    }

    /**
     * 프로젝트 조회
     * @param projectIdx
     * @param partnershipIdx
     * @return
     */
    public CustomResponse<?> getProject(Integer projectIdx, Integer partnershipIdx) throws CustomException {
        // TODO : 파트너쉽에 속한 회원 여부 체크
        // TODO : 해당 프로젝트 권한 여부 체크
        // 3. 프로젝트 조회
        Query query = Query.query(Criteria.where("_id.projectIdx").is(projectIdx).and("_id.partnershipIdx").is(partnershipIdx));
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
        // TODO : 파트너쉽에 속한 회원 여부 체크
        // TODO : 해당 프로젝트 권한 여부 체크
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
     * 프로젝트 삭제
     * @param projectId
     * @return
     */
    public CustomResponse<?> deleteProject(RequestProjectDTO.ProjectId projectId) throws CustomException {
        // TODO : 파트너쉽에 속한 회원 여부 체크
        // TODO : 해당 프로젝트 권한 여부 체크
        // RDB 프로젝트 조회
        ProjectVO projectVO = projectMapper.selectByProjectCategoryIdxAndProjectIdx(projectId.getProjectCategoryIdx(), projectId.getProjectIdx());
        // MongoDB 프로젝트 조회
        Project findProject = mongoTemplate.findById(projectId, Project.class);
        if(projectVO == null || findProject == null) {
            throw new CustomException(ErrorCode.COMMON_EMPTY);
        }
        // RDB 삭제
        int deleteCnt = projectMapper.deleteByProjectCategoryIdxAndProjectIdx(projectId.getProjectCategoryIdx(), projectId.getProjectIdx());
        // MongoDB 삭제
        DeleteResult deleteResult = mongoTemplate.remove(findProject);

        //maria삭제
        projectMapper.deleteByIdx(projectId.getProjectIdx());

        return CustomResponse.builder()
                .data(deleteResult)
                .build();
    }

    /**
     * 단일 엑셀 파일 업로드
     * @param projectIdx
     * @param excelFile
     * @return
     */
    @Transactional
    public CustomResponse<?> uploadSingleExcelFile(Integer projectIdx, MultipartFile excelFile) throws CustomException, IOException {
        // TODO : 파트너쉽에 속한 회원 여부 체크
        // TODO : 해당 프로젝트 권한 여부 체크
        // 프로젝트 생성 여부 확인
        ProjectVO projectVO = projectMapper.selectByIdx(projectIdx)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMON_EMPTY));

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

        Data data = Data.builder()
                .projectIdx(projectVO.getIdx())
                .dataSheet(new ArrayList<>())
                .build();


        // DataRow 데이터 삭제
        mongoTemplate.findAllAndRemove(Query.query(Criteria.where("_id.projectIdx").is(projectVO.getIdx())), DataRow.class);

        // Sheet 읽기
        for(int sheetIdx = 0; sheetIdx < workbook.getNumberOfSheets(); sheetIdx++) {
            // Cell 목록
            List<String> cellList = new ArrayList<>();

            Sheet workSheet = workbook.getSheetAt(sheetIdx);
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

            // Cell, Row 최대 개수 추출
            int totalCellCnt = firstRow.getLastCellNum();
            int totalRowCnt = workSheet.getLastRowNum();

            // 첫번째 행에 컬럼 수 만큼 컬럼명 추출
            for(int cellIdx = 0; cellIdx < totalCellCnt; cellIdx++) {
                if(firstRow.getCell(cellIdx).getStringCellValue().isEmpty()) {
                    // 셀에 빈값이면 컬럼 총개수 감소
                    totalCellCnt--;
                } else {
                    cellList.add(firstRow.getCell(cellIdx).getStringCellValue());
                }
            }

            // 행 개수 만큼 데이터 추출
            for(int rowIdx = 1; rowIdx < totalRowCnt; rowIdx++) {
                Row row = workSheet.getRow(rowIdx);
                if(row == null) {
                    // 열 데이터가 없으면 종료
                    totalRowCnt = rowIdx;
                    break;
                }
                // 데이터 속성 생성
                DataRow dataRow = DataRow.builder()
                        .dataRowId(DataRowId.builder()
                                .projectIdx(projectIdx)
                                .sheetIdx(sheetIdx + 1)
                                .rowIdx(rowIdx)
                                .build())
                        .build();
                LinkedHashMap<String, Object> dataMap = new LinkedHashMap<>();
                // 열 개수 만큼 데이터 추출
                for (int cellCnt = 0; cellCnt < cellList.size(); cellCnt++) {
                    dataMap.put(cellList.get(cellCnt), projectComponent.getExcelColumnData(row.getCell(cellCnt)));
                }
                dataRow.setDataRow(dataMap);
                // 엑셀 데이터 속성 저장
                mongoTemplate.insert(dataRow);
            }

            // 엑셀 시트별 데이터 행 추가
            data.getDataSheet()
                    .add(DataSheet.builder()
                            .sheetIdx(sheetIdx + 1)
                            .sheetName(workSheet.getSheetName())
                            .cellList(cellList)
                            .totalRowCnt(totalRowCnt - 1)
                            .build());
        }

        // 엑셀 데이터 정보 삭제
        mongoTemplate.findAndRemove(Query.query(Criteria.where("_id").is(projectVO.getIdx())), Data.class);

        // 엑셀 데이터 정보 저장
        mongoTemplate.insert(data);

        // 응답 데이터 조회
        ResponseProjectDTO.Data response = projectComponent.responseProjectData(projectIdx);

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
        List<ProjectVO> projectList = projectMapper.selectAllByProjectCategoryIdxAndPartnerShipIdx(projectId.getProjectCategoryIdx(), projectId.getPartnershipIdx());

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
            int insertCnt = projectMapper.insertByProjectVO(projectVO);
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
