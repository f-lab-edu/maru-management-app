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

@Entity
@Table(
    name = "invoice",
    indexes = {
        @Index(name = "idx_invoice_tenant_status_due", columnList = "tenant_id, status, due_date"),
        @Index(name = "idx_invoice_student_status", columnList = "student_id, status"),
        @Index(name = "idx_invoice_dojang_issue", columnList = "dojang_id, issue_date")
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

    @Column(name = "issued_by")
    private Long issuedBy;

    @Column(name = "issue_date", nullable = false)
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

    private Invoice(Student student, BigDecimal amount, LocalDate dueDate, String note) {
        DomainAssert.notNull(student, InvoiceErrorCode.STUDENT_REQUIRED);
        DomainAssert.notNull(dueDate, InvoiceErrorCode.DUE_DATE_REQUIRED);
        DomainAssert.notNull(amount, InvoiceErrorCode.AMOUNT_REQUIRED);

        this.tenantId = student.getTenantId();
        this.dojangId = student.getDojang().getId();
        this.student = student;
        this.issueDate = LocalDate.now();
        this.dueDate = dueDate;
        this.status = InvoiceStatus.DRAFT;
        this.amount = amount;
        this.paidAmount = BigDecimal.ZERO;
        this.note = note;
    }

    public static Invoice create(Student student, BigDecimal amount, LocalDate dueDate, String note) {
        return new Invoice(student, amount, dueDate, note);
    }

    public void issue(Long issuedBy) {
        if (this.status != InvoiceStatus.DRAFT) {
            throw new BusinessException(InvoiceErrorCode.INVALID_STATUS_TRANSITION);
        }
        this.status = InvoiceStatus.OPEN;
        this.issuedBy = issuedBy;
    }

    public void markAsVoid() {
        if (this.status == InvoiceStatus.PAID) {
            throw new BusinessException(InvoiceErrorCode.CANNOT_VOID_PAID_INVOICE);
        }
        if (this.status != InvoiceStatus.DRAFT && this.status != InvoiceStatus.OPEN) {
            throw new BusinessException(InvoiceErrorCode.INVALID_STATUS_TRANSITION);
        }
        this.status = InvoiceStatus.VOID;
    }

    public void addPayment(BigDecimal paymentAmount) {
        this.paidAmount = this.paidAmount.add(paymentAmount);
        this.status = calculateStatus();
    }

    public void subtractPayment(BigDecimal paymentAmount) {
        this.paidAmount = this.paidAmount.subtract(paymentAmount);
        this.status = calculateStatus();
    }

    public BigDecimal getRemainingAmount() {
        return this.amount.subtract(this.paidAmount);
    }

    private InvoiceStatus calculateStatus() {
        int comparison = this.paidAmount.compareTo(this.amount);
        if (comparison >= 0) {
            return InvoiceStatus.PAID;
        } else if (this.paidAmount.compareTo(BigDecimal.ZERO) > 0) {
            return InvoiceStatus.PARTIAL;
        }
        return this.status;
    }

    public void update(BigDecimal amount, LocalDate dueDate, String note) {
        if (this.status != InvoiceStatus.DRAFT) {
            throw new BusinessException(InvoiceErrorCode.CANNOT_UPDATE_NON_DRAFT);
        }
        if (amount != null) {
            this.amount = amount;
        }
        if (dueDate != null) {
            this.dueDate = dueDate;
        }
        if (note != null) {
            this.note = note;
        }
    }
}
