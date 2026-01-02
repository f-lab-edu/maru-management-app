package com.maru.service.group;

import com.maru.common.exception.BusinessException;
import com.maru.common.exception.CommonErrorCode;
import com.maru.controller.groups.dto.GroupCreateReq;
import com.maru.controller.groups.dto.GroupDetailRes;
import com.maru.controller.groups.dto.GroupListRes;
import com.maru.controller.groups.dto.GroupRes;
import com.maru.controller.groups.dto.GroupUpdateReq;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GroupService {

    /**
     * 수련반을 생성합니다.
     *
     * @param dojangId 도장 ID
     * @param request 생성 요청
     * @return 생성된 수련반 정보
     * @throws BusinessException 수련부를 찾을 수 없거나 중복 이름인 경우
     */
    public GroupRes createGroup(String dojangId, GroupCreateReq request) {
        throw new BusinessException(CommonErrorCode.NOT_IMPLEMENTED);
    }

    /**
     * 수련반 목록을 조회합니다.
     *
     * @param dojangId 도장 ID
     * @param sectionId 수련부 ID (선택, null이면 전체)
     * @return 수련반 목록 (section.displayOrder, displayOrder 순)
     */
    public GroupListRes getGroups(String dojangId, String sectionId) {
        throw new BusinessException(CommonErrorCode.NOT_IMPLEMENTED);
    }

    /**
     * 수련반 상세를 조회합니다.
     *
     * @param dojangId 도장 ID
     * @param groupId 수련반 ID
     * @return 수련반 상세 정보
     * @throws BusinessException 수련반을 찾을 수 없는 경우
     */
    public GroupDetailRes getGroupDetail(String dojangId, String groupId) {
        throw new BusinessException(CommonErrorCode.NOT_IMPLEMENTED);
    }

    /**
     * 수련반 정보를 수정합니다.
     *
     * @param dojangId 도장 ID
     * @param groupId 수련반 ID
     * @param request 수정 요청
     * @return 수정된 수련반 정보
     * @throws BusinessException 수련반을 찾을 수 없거나 중복 이름인 경우
     */
    public GroupRes updateGroup(String dojangId, String groupId, GroupUpdateReq request) {
        throw new BusinessException(CommonErrorCode.NOT_IMPLEMENTED);
    }

    /**
     * 수련반을 삭제합니다.
     *
     * @param dojangId 도장 ID
     * @param groupId 수련반 ID
     * @throws BusinessException 수련반을 찾을 수 없는 경우
     */
    public void deleteGroup(String dojangId, String groupId) {
        throw new BusinessException(CommonErrorCode.NOT_IMPLEMENTED);
    }
}
