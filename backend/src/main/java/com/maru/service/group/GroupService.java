package com.maru.service.group;

import com.maru.common.exception.BusinessException;
import com.maru.controller.groups.dto.GroupCreateReq;
import com.maru.controller.groups.dto.GroupDetailRes;
import com.maru.controller.groups.dto.GroupListRes;
import com.maru.controller.groups.dto.GroupRes;
import com.maru.controller.groups.dto.GroupUpdateReq;
import com.maru.domain.group.Group;
import com.maru.domain.group.exception.GroupErrorCode;
import com.maru.domain.section.Section;
import com.maru.domain.section.exception.SectionErrorCode;
import com.maru.domain.tenant.Dojang;
import com.maru.domain.tenant.exception.DojangErrorCode;
import com.maru.repository.group.GroupRepository;
import com.maru.repository.section.SectionRepository;
import com.maru.repository.tenant.DojangRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupService {

    private final GroupRepository groupRepository;
    private final SectionRepository sectionRepository;
    private final DojangRepository dojangRepository;

    /**
     * 수련반 생성
     *
     * @param dojangId 도장 ID
     * @param request 생성 요청
     * @return 생성된 수련반 정보
     * @throws BusinessException 수련부를 찾을 수 없거나 중복 이름인 경우
     */
    @Transactional
    public GroupRes createGroup(String dojangId, GroupCreateReq request) {
        Dojang dojang = findDojangById(dojangId);
        Section section = findSectionByIdAndDojangId(request.sectionId(), dojangId);

        validateDuplicateName(request.sectionId(), request.name());

        int nextDisplayOrder = groupRepository.findMaxDisplayOrderBySectionId(request.sectionId()) + 1;
        Group group = Group.create(dojang, section, request.name(), nextDisplayOrder);

        if (request.dayOfWeek() != null) {
            group.updateSchedule(request.dayOfWeek(), request.startTime(), request.endTime());
        }

        groupRepository.save(group);

        return toGroupRes(group);
    }

    /**
     * 수련반 목록 조회
     *
     * @param dojangId 도장 ID
     * @param sectionId 수련부 ID (선택, null이면 전체)
     * @return 수련반 목록 (section.displayOrder, displayOrder 순)
     */
    public GroupListRes getGroups(String dojangId, String sectionId) {
        List<Group> groups = (sectionId == null)
                ? groupRepository.findAllWithSectionByDojangId(dojangId)
                : groupRepository.findAllWithSectionByDojangIdAndSectionId(dojangId, sectionId);

        List<GroupRes> groupResList = groups.stream()
                .map(this::toGroupRes)
                .toList();

        return GroupListRes.from(groupResList);
    }

    /**
     * 수련반 상세
     *
     * @param dojangId 도장 ID
     * @param groupId 수련반 ID
     * @return 수련반 상세 정보
     * @throws BusinessException 수련반을 찾을 수 없는 경우
     */
    public GroupDetailRes getGroupDetail(String dojangId, String groupId) {
        Group group = findGroupByIdAndDojangId(groupId, dojangId);
        return toGroupDetailRes(group);
    }

    /**
     * 수련반 정보 수정
     *
     * @param dojangId 도장 ID
     * @param groupId 수련반 ID
     * @param request 수정 요청
     * @return 수정된 수련반 정보
     * @throws BusinessException 수련반을 찾을 수 없거나 중복 이름인 경우
     */
    @Transactional
    public GroupRes updateGroup(String dojangId, String groupId, GroupUpdateReq request) {
        Group group = findGroupByIdAndDojangId(groupId, dojangId);

        validateDuplicateNameExcludingSelf(group.getSection().getId(), request.name(), groupId);

        group.updateName(request.name());
        group.updateSchedule(request.dayOfWeek(), request.startTime(), request.endTime());

        return toGroupRes(group);
    }

    /**
     * 수련반 삭제
     *
     * @param dojangId 도장 ID
     * @param groupId 수련반 ID
     * @throws BusinessException 수련반을 찾을 수 없는 경우
     */
    @Transactional
    public void deleteGroup(String dojangId, String groupId) {
        Group group = findGroupByIdAndDojangId(groupId, dojangId);
        group.markAsDeleted();
    }

    private Dojang findDojangById(String dojangId) {
        return dojangRepository.findById(dojangId)
                .orElseThrow(() -> new BusinessException(DojangErrorCode.NOT_FOUND));
    }

    private Section findSectionByIdAndDojangId(String sectionId, String dojangId) {
        return sectionRepository.findByIdAndDojangId(sectionId, dojangId)
                .orElseThrow(() -> new BusinessException(SectionErrorCode.NOT_FOUND));
    }

    private Group findGroupByIdAndDojangId(String groupId, String dojangId) {
        return groupRepository.findByIdAndDojangIdWithSection(groupId, dojangId)
                .orElseThrow(() -> new BusinessException(GroupErrorCode.NOT_FOUND));
    }

    private void validateDuplicateName(String sectionId, String name) {
        if (groupRepository.existsBySectionIdAndName(sectionId, name)) {
            throw new BusinessException(GroupErrorCode.DUPLICATE_NAME);
        }
    }

    private void validateDuplicateNameExcludingSelf(String sectionId, String name, String excludeId) {
        if (groupRepository.existsBySectionIdAndNameAndIdNot(sectionId, name, excludeId)) {
            throw new BusinessException(GroupErrorCode.DUPLICATE_NAME);
        }
    }

    private GroupRes toGroupRes(Group group) {
        return GroupRes.builder()
                .id(group.getId())
                .sectionId(group.getSection().getId())
                .sectionName(group.getSection().getName())
                .name(group.getName())
                .displayOrder(group.getDisplayOrder())
                .dayOfWeek(group.getDayOfWeek())
                .startTime(group.getStartTime())
                .endTime(group.getEndTime())
                .studentCount(0)  // TODO: Enrollment 구현 후 추가
                .build();
    }

    private GroupDetailRes toGroupDetailRes(Group group) {
        return GroupDetailRes.builder()
                .id(group.getId())
                .sectionId(group.getSection().getId())
                .sectionName(group.getSection().getName())
                .name(group.getName())
                .displayOrder(group.getDisplayOrder())
                .dayOfWeek(group.getDayOfWeek())
                .startTime(group.getStartTime())
                .endTime(group.getEndTime())
                .studentCount(0)  // TODO: Enrollment 구현 후 추가
                .build();
    }
}
