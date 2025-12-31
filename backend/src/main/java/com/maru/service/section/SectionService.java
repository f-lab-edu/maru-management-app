package com.maru.service.section;

import com.maru.common.exception.BusinessException;
import com.maru.common.exception.CommonErrorCode;
import com.maru.controller.section.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SectionService {

    /**
     * 수련부 생성
     *
     * @param dojangId 도장 ID
     * @param request  생성 요청
     * @return 생성된 수련부 정보
     * @throws BusinessException 중복 이름인 경우
     */
    public SectionRes createSection(String dojangId, SectionCreateReq request) {
        throw new BusinessException(CommonErrorCode.NOT_IMPLEMENTED);
    }

    /**
     * 수련부 목록 조회
     *
     * @param dojangId 도장 ID
     * @return 수련부 목록 (displayOrder 순)
     */
    public SectionListRes getSections(String dojangId) {
        throw new BusinessException(CommonErrorCode.NOT_IMPLEMENTED);
    }

    /**
     * 수련부 수정
     *
     * @param dojangId  도장 ID
     * @param sectionId 수련부 ID
     * @param request   수정 요청
     * @return 수정된 수련부 정보
     * @throws BusinessException 수련부를 찾을 수 없거나 중복 이름인 경우
     */
    public SectionRes updateSection(String dojangId, String sectionId, SectionUpdateReq request) {
        throw new BusinessException(CommonErrorCode.NOT_IMPLEMENTED);
    }

    /**
     * 수련부 삭제
     *
     * @param dojangId  도장 ID
     * @param sectionId 수련부 ID
     * @throws BusinessException 수련부를 찾을 수 없거나 소속 수련반이 있는 경우
     */
    public void deleteSection(String dojangId, String sectionId) {
        throw new BusinessException(CommonErrorCode.NOT_IMPLEMENTED);
    }

    /**
     * 수련부 순서 변경
     *
     * @param dojangId 도장 ID
     * @param request  순서 변경 요청 (ID 목록 순서대로 displayOrder 부여)
     */
    public void reorderSections(String dojangId, SectionReorderReq request) {
        throw new BusinessException(CommonErrorCode.NOT_IMPLEMENTED);
    }
}
