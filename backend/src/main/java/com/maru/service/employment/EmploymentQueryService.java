package com.maru.service.employment;

import com.maru.common.exception.BusinessException;
import com.maru.controller.employment.dto.EmploymentRes;
import com.maru.controller.employment.dto.PendingApprovalRes;
import com.maru.controller.user.dto.MyDojangRes;
import com.maru.domain.employment.EmploymentStatus;
import com.maru.domain.employment.exception.EmploymentErrorCode;
import com.maru.domain.user.UserRole;
import com.maru.repository.employment.EmploymentRepository;
import com.maru.repository.employment.view.EmploymentDetailView;
import com.maru.repository.employment.view.MyDojangView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmploymentQueryService {

    private final EmploymentRepository employmentRepository;

    public EmploymentRes getEmployment(String employmentId) {
        EmploymentDetailView view = employmentRepository.findViewById(employmentId)
                .orElseThrow(() -> new BusinessException(EmploymentErrorCode.NOT_FOUND));
        return toEmploymentRes(view);
    }

    public List<PendingApprovalRes> getPendingRequests(String dojangId) {
        return employmentRepository.findViewByDojangIdAndStatus(dojangId, EmploymentStatus.PENDING)
                .stream()
                .map(this::toPendingApprovalRes)
                .toList();
    }

    public List<EmploymentRes> getMyRequests(String userId) {
        return employmentRepository.findViewByUserId(userId)
                .stream()
                .map(this::toEmploymentRes)
                .toList();
    }

    public List<MyDojangRes> getMyDojangs(String userId) {
        return employmentRepository.findMyDojangViewByUserIdAndStatus(userId, EmploymentStatus.ACTIVE)
                .stream()
                .map(this::toMyDojangRes)
                .toList();
    }

    public static UserRole resolveRole(String userId, String dojangOwnerId) {
        return dojangOwnerId.equals(userId) ? UserRole.OWNER : UserRole.INSTRUCTOR;
    }

    private EmploymentRes toEmploymentRes(EmploymentDetailView view) {
        return EmploymentRes.builder()
                .id(view.getId())
                .userId(view.getUserId())
                .dojangId(view.getDojangId())
                .dojangName(view.getDojangName())
                .status(view.getStatus())
                .requestedAt(view.getCreatedAt())
                .approvedAt(view.getStatus() == EmploymentStatus.ACTIVE ? view.getJoinedAt() : null)
                .rejectedAt(view.getStatus() == EmploymentStatus.REJECTED ? view.getEndedAt() : null)
                .build();
    }

    private PendingApprovalRes toPendingApprovalRes(EmploymentDetailView view) {
        return PendingApprovalRes.builder()
                .id(view.getId())
                .userId(view.getUserId())
                .userName(view.getUserName())
                .userEmail(view.getUserEmail())
                .userPhone(view.getUserPhone())
                .requestedAt(view.getCreatedAt())
                .status(view.getStatus())
                .build();
    }

    private MyDojangRes toMyDojangRes(MyDojangView view) {
        return MyDojangRes.builder()
                .dojangId(view.getDojangId())
                .dojangName(view.getDojangName())
                .tenantId(view.getTenantId())
                .role(resolveRole(view.getUserId(), view.getDojangOwnerId()))
                .build();
    }
}
