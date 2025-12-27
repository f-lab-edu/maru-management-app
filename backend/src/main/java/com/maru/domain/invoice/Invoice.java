package com.maru.domain.invoice;

import com.maru.common.exception.BusinessException;
import com.maru.common.exception.DomainAssert;
import com.maru.common.exception.InvoiceErrorCode;
import com.maru.domain.common.BaseEntity;
import com.maru.domain.student.Student;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Entity
@Table(
    name = "invoice",
    indexes = {
        @Index(name = "idx_invoice_tenant_status_due", columnList = "tenant_id, status, due_date"),
        @Index(name = "idx_invoice_student_status", columnList = "student_id, status"),
        @Index(name = "idx_invoice_tenant_dojang_issue", columnList = "tenant_id, dojang_id, issue_date"),
        @Index(name = "idx_invoice_billing", columnList = "tenant_id, dojang_id, billing_year, billing_month")
    },
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_invoice_billing",
            columnNames = {"tenant_id", "dojang_id", "student_id", "billing_year", "billing_month"}
        )
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Invoice extends BaseEntity {

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "dojang_id", nullable = false)
    private Long dojangId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "billing_year", nullable = false)
    private int billingYear;

    @Column(name = "billing_month", nullable = false)
    private int billingMonth;

    @Column(name = "issued_by")
    private Long issuedBy;

    @Column(name = "issue_date")
    private LocalDate issueDate;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private InvoiceStatus status;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "paid_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal paidAmount;

    @Column(name = "note", length = 500)
    private String note;

    private Invoice(Student student, int billingYear, int billingMonth,
                    BigDecimal amount, LocalDate dueDate, String note) {
        DomainAssert.notNull(student, InvoiceErrorCode.STUDENT_REQUIRED);
        DomainAssert.notNull(dueDate, InvoiceErrorCode.DUE_DATE_REQUIRED);
        DomainAssert.notNull(amount, InvoiceErrorCode.AMOUNT_REQUIRED);
        validateBillingMonth(billingMonth);

        this.tenantId = student.getTenantId();
        this.dojangId = student.getDojang().getId();
        this.student = student;
        this.billingYear = billingYear;
        this.billingMonth = billingMonth;
        this.issueDate = null;
        this.dueDate = dueDate;
        this.status = InvoiceStatus.DRAFT;
        this.amount = amount;
        this.paidAmount = BigDecimal.ZERO;
        this.note = note;
    }

    public static Invoice create(Student student, int billingYear, int billingMonth,
                                 BigDecimal amount, LocalDate dueDate, String note) {
        return new Invoice(student, billingYear, billingMonth, amount, dueDate, note);
    }

    private void validateBillingMonth(int billingMonth) {
        if (billingMonth < 1 || billingMonth > 12) {
            throw new BusinessException(InvoiceErrorCode.INVALID_BILLING_MONTH);
        }
    }

    public void issue(Long issuedBy) {
        if (this.status != InvoiceStatus.DRAFT) {
            throw new BusinessException(InvoiceErrorCode.INVALID_STATUS_TRANSITION);
        }
        this.status = InvoiceStatus.OPEN;
        this.issuedBy = issuedBy;
        this.issueDate = LocalDate.now();
    }

    public void markAsVoid() {
        if (this.status == InvoiceStatus.PAID) {
            throw new BusinessException(InvoiceErrorCode.CANNOT_VOID_PAID_INVOICE);
        }
        if (this.status != InvoiceStatus.DRAFT && this.status != InvoiceStatus.OPEN && this.status != InvoiceStatus.PARTIAL) {
            throw new BusinessException(InvoiceErrorCode.INVALID_STATUS_TRANSITION);
        }
        this.status = InvoiceStatus.VOID;
    }

    public void restore() {
        if (this.status != InvoiceStatus.VOID) {
            throw new BusinessException(InvoiceErrorCode.CANNOT_RESTORE_NON_VOID);
        }
        this.status = InvoiceStatus.DRAFT;
        this.issueDate = null;
        this.issuedBy = null;
    }

    public void addPayment(BigDecimal paymentAmount) {
        if (this.status == InvoiceStatus.VOID) {
            throw new BusinessException(InvoiceErrorCode.CANNOT_PAY_VOID_INVOICE);
        }
        if (this.status == InvoiceStatus.DRAFT) {
            throw new BusinessException(InvoiceErrorCode.CANNOT_PAY_DRAFT_INVOICE);
        }
        this.paidAmount = this.paidAmount.add(paymentAmount);
        this.status = calculateStatus();
    }

    public void subtractPayment(BigDecimal paymentAmount) {
        if (this.status == InvoiceStatus.VOID) {
            throw new BusinessException(InvoiceErrorCode.CANNOT_REFUND_VOID_INVOICE);
        }
        this.paidAmount = this.paidAmount.subtract(paymentAmount);
        this.status = calculateStatus();
    }

    public BigDecimal getRemainingAmount() {
        return this.amount.subtract(this.paidAmount);
    }

    private InvoiceStatus calculateStatus() {
        if (this.status == InvoiceStatus.VOID) {
            return InvoiceStatus.VOID;
        }

        int comparison = this.paidAmount.compareTo(this.amount);
        if (comparison >= 0) {
            return InvoiceStatus.PAID;
        } else if (this.paidAmount.compareTo(BigDecimal.ZERO) > 0) {
            return InvoiceStatus.PARTIAL;
        }
        return InvoiceStatus.OPEN;
    }

    public void update(BigDecimal amount, LocalDate dueDate, String note) {
        // DRAFT, OPEN만 수정 가능 (OPEN = 아직 수납 없음)
        if (this.status != InvoiceStatus.DRAFT && this.status != InvoiceStatus.OPEN) {
            throw new BusinessException(InvoiceErrorCode.CANNOT_UPDATE_ISSUED_INVOICE);
        }
        Optional.ofNullable(amount).ifPresent(a -> this.amount = a);
        Optional.ofNullable(dueDate).ifPresent(d -> this.dueDate = d);
        Optional.ofNullable(note).ifPresent(n -> this.note = n);
    }


    public boolean isEditable() {
        return this.status == InvoiceStatus.DRAFT || this.status == InvoiceStatus.OPEN;
    }
}
