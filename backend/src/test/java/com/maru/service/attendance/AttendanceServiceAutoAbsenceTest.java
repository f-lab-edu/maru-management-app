package com.maru.service.attendance;

import com.maru.domain.attendance.Attendance;
import com.maru.domain.student.Student;
import com.maru.domain.tenant.Dojang;
import com.maru.domain.tenant.Tenant;
import com.maru.repository.attendance.AttendanceRepository;
import com.maru.repository.student.StudentRepository;
import com.maru.repository.tenant.DojangRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("AttendanceService 자동 결석 처리")
class AttendanceServiceAutoAbsenceTest {

    @Mock
    StudentRepository studentRepository;

    @Mock
    AttendanceRepository attendanceRepository;

    @SuppressWarnings("unused")
    @Mock
    DojangRepository dojangRepository;

    @SuppressWarnings("unused")
    @Mock
    ApplicationEventPublisher eventPublisher;

    @InjectMocks
    AttendanceService attendanceService;

    @Captor
    ArgumentCaptor<List<Attendance>> attendanceListCaptor;

    private static final LocalDate TARGET_DATE = LocalDate.of(2025, 12, 17);

    @Nested
    @DisplayName("processAutoAbsence 메서드는")
    class ProcessAutoAbsence {

        @Test
        @DisplayName("대상 원생이 있으면 결석 레코드를 생성하고 저장한다")
        void createsAbsenceRecordsWhenStudentsExist() {
            // given
            List<Student> students = List.of(
                    createMockStudent("student1", "홍길동"),
                    createMockStudent("student2", "김철수")
            );
            given(studentRepository.findAllActiveStudentsWithoutAttendance(TARGET_DATE))
                    .willReturn(students);

            // when
            int result = attendanceService.processAutoAbsence(TARGET_DATE);

            // then
            assertThat(result).isEqualTo(2);
            then(attendanceRepository).should().saveAll(attendanceListCaptor.capture());

            List<Attendance> savedAttendances = attendanceListCaptor.getValue();
            assertThat(savedAttendances).hasSize(2);
        }

        @Test
        @DisplayName("대상 원생이 없으면 0을 리턴하고 저장하지 않는다")
        void returnsZeroWhenNoStudents() {
            // given
            given(studentRepository.findAllActiveStudentsWithoutAttendance(TARGET_DATE))
                    .willReturn(Collections.emptyList());

            // when
            int result = attendanceService.processAutoAbsence(TARGET_DATE);

            // then
            assertThat(result).isEqualTo(0);
            then(attendanceRepository).should(never()).saveAll(any());
        }

        @Test
        @DisplayName("100명 원생도 일괄 처리된다")
        void handlesBulkProcessing() {
            // given
            List<Student> students = createMockStudents(100);
            given(studentRepository.findAllActiveStudentsWithoutAttendance(TARGET_DATE))
                    .willReturn(students);

            // when
            int result = attendanceService.processAutoAbsence(TARGET_DATE);

            // then
            assertThat(result).isEqualTo(100);
            then(attendanceRepository).should().saveAll(attendanceListCaptor.capture());
            assertThat(attendanceListCaptor.getValue()).hasSize(100);
        }
    }

    private Student createMockStudent(String id, String name) {
        Student student = mock(Student.class);
        Dojang dojang = mock(Dojang.class);
        Tenant tenant = mock(Tenant.class);

        given(student.getId()).willReturn(id);
        given(student.getName()).willReturn(name);
        given(student.getDojang()).willReturn(dojang);
        given(dojang.getId()).willReturn("dojang1");
        given(dojang.getTenant()).willReturn(tenant);
        given(tenant.getId()).willReturn("tenant1");

        return student;
    }

    private List<Student> createMockStudents(int count) {
        return IntStream.rangeClosed(1, count)
                .mapToObj(i -> createMockStudent("student" + i, "원생" + i))
                .toList();
    }
}