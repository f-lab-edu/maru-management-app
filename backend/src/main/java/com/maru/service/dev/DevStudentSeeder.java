package com.maru.service.dev;

import com.maru.common.exception.BusinessException;
import com.maru.domain.student.Student;
import com.maru.domain.tenant.Dojang;
import com.maru.repository.student.StudentRepository;
import com.maru.repository.tenant.DojangRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static com.maru.common.exception.ErrorCode.DOJANG_NOT_FOUND;
import static com.maru.common.exception.ErrorCode.UNAUTHORIZED_DOJANG_ACCESS;

@Slf4j
@Service
@Profile({"dev", "local"})
@RequiredArgsConstructor
public class DevStudentSeeder {

    private static final int DEFAULT_STUDENT_COUNT = 200;
    private static final int BIRTH_YEAR_MIN = 2005;
    private static final int BIRTH_YEAR_MAX = 2018;

    private static final String[] FIRST_SYLLABLES = {
        "김", "이", "박", "최", "정", "강", "조", "윤", "장", "임",
        "한", "오", "서", "신", "권", "황", "안", "송", "류", "전",
        "홍", "고", "문", "양", "손", "배", "백", "허", "유", "남",
        "심", "노", "하", "곽", "성", "차", "주", "우", "구", "민"
    };

    private static final String[] MIDDLE_SYLLABLES = {
        "민", "서", "예", "지", "현", "수", "영", "진", "은", "준",
        "도", "하", "윤", "주", "성", "재", "혁", "태", "승", "우",
        "건", "호", "상", "석", "동", "원", "용", "철", "광", "종"
    };

    private static final String[] LAST_SYLLABLES = {
        "호", "준", "우", "혁", "진", "석", "훈", "영", "수", "민",
        "기", "성", "현", "환", "철", "규", "원", "태", "욱", "빈",
        "", "", "", "", ""
    };

    private final StudentRepository studentRepository;
    private final DojangRepository dojangRepository;

    /**
     * 지정된 도장에 테스트용 학생 200명 생성
     *
     * @param tenantId 테넌트 ID
     * @param dojangId 도장 ID
     * @return 생성된 학생 수
     */
    @Transactional
    public int seedStudents(Long tenantId, Long dojangId) {
        Dojang dojang = dojangRepository.findById(dojangId)
                .orElseThrow(() -> new BusinessException(DOJANG_NOT_FOUND));

        if (!dojang.getTenant().getId().equals(tenantId)) {
            throw new BusinessException(UNAUTHORIZED_DOJANG_ACCESS);
        }

        List<Student> students = new ArrayList<>();
        for (int i = 0; i < DEFAULT_STUDENT_COUNT; i++) {
            String name = generateRandomKoreanName();
            LocalDate birth = generateRandomBirth();
            Student student = Student.create(dojang, name, birth, null, null);
            students.add(student);
        }

        studentRepository.saveAll(students);
        log.info("[DEV] 테스트 학생 생성 완료 - dojangId: {}, count: {}", dojangId, students.size());

        return students.size();
    }

    private String generateRandomKoreanName() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        String first = FIRST_SYLLABLES[random.nextInt(FIRST_SYLLABLES.length)];
        String middle = MIDDLE_SYLLABLES[random.nextInt(MIDDLE_SYLLABLES.length)];
        String last = LAST_SYLLABLES[random.nextInt(LAST_SYLLABLES.length)];
        return first + middle + last;
    }

    private LocalDate generateRandomBirth() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int year = random.nextInt(BIRTH_YEAR_MIN, BIRTH_YEAR_MAX + 1);
        int month = random.nextInt(1, 13);
        int day = random.nextInt(1, 29);
        return LocalDate.of(year, month, day);
    }
}
