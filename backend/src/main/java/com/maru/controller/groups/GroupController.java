package com.maru.controller.groups;

import com.maru.common.exception.BusinessException;
import com.maru.common.exception.CommonErrorCode;
import com.maru.controller.groups.dto.GroupCreateReq;
import com.maru.controller.groups.dto.GroupDetailRes;
import com.maru.controller.groups.dto.GroupListRes;
import com.maru.controller.groups.dto.GroupRes;
import com.maru.controller.groups.dto.GroupUpdateReq;
import com.maru.service.group.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    /**
     * 수련반 생성
     *
     * @param dojangId 도장 ID
     * @param request 수련반 생성 정보
     * @return 생성된 수련반 정보
     */
    @PostMapping
    public ResponseEntity<GroupRes> createGroup(
            @RequestParam String dojangId,
            @Valid @RequestBody GroupCreateReq request
    ) {
        throw new BusinessException(CommonErrorCode.NOT_IMPLEMENTED);
    }

    /**
     * 수련반 목록 조회
     *
     * @param dojangId 도장 ID
     * @param sectionId 수련부 ID (선택, null이면 전체)
     * @return 수련반 목록 (section.displayOrder, displayOrder 순)
     */
    @GetMapping
    public ResponseEntity<GroupListRes> getGroups(
            @RequestParam String dojangId,
            @RequestParam(required = false) String sectionId
    ) {
        throw new BusinessException(CommonErrorCode.NOT_IMPLEMENTED);
    }

    /**
     * 수련반 상세 조회
     *
     * @param dojangId 도장 ID
     * @param groupId 수련반 ID
     * @return 수련반 상세 정보
     */
    @GetMapping("/{groupId}")
    public ResponseEntity<GroupDetailRes> getGroupDetail(
            @RequestParam String dojangId,
            @PathVariable String groupId
    ) {
        throw new BusinessException(CommonErrorCode.NOT_IMPLEMENTED);
    }

    /**
     * 수련반 수정
     *
     * @param dojangId 도장 ID
     * @param groupId 수련반 ID
     * @param request 수정 정보
     * @return 수정된 수련반 정보
     */
    @PatchMapping("/{groupId}")
    public ResponseEntity<GroupRes> updateGroup(
            @RequestParam String dojangId,
            @PathVariable String groupId,
            @Valid @RequestBody GroupUpdateReq request
    ) {
        throw new BusinessException(CommonErrorCode.NOT_IMPLEMENTED);
    }

    /**
     * 수련반 삭제
     *
     * @param dojangId 도장 ID
     * @param groupId 수련반 ID
     * @return 204 No Content
     */
    @DeleteMapping("/{groupId}")
    public ResponseEntity<Void> deleteGroup(
            @RequestParam String dojangId,
            @PathVariable String groupId
    ) {
        throw new BusinessException(CommonErrorCode.NOT_IMPLEMENTED);
    }
}
