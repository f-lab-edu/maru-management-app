package com.maru.service.dev;

// TODO: 테스트용. 프로덕션에서는 반드시 삭제할 것.

import com.maru.domain.tenant.Dojang;
import com.maru.domain.tenant.Tenant;
import com.maru.domain.user.OnboardingStep;
import com.maru.domain.user.User;
import com.maru.domain.user.UserRole;
import com.maru.repository.tenant.DojangRepository;
import com.maru.repository.tenant.TenantRepository;
import com.maru.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@Profile({"dev", "local"})
@RequiredArgsConstructor
public class DevDojangSeeder {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final DojangRepository dojangRepository;

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

    @Transactional
    public int seedDojangs() {
        List<DojangCsvRow> rows = parseCsv();
        log.info("CSV 파싱 완료: {}개 도장", rows.size());

        List<User> users = new ArrayList<>();
        List<Tenant> tenants = new ArrayList<>();
        List<Dojang> dojangs = new ArrayList<>();

        for (DojangCsvRow row : rows) {
            String ownerName = generateRandomKoreanName();
            User owner = createOwner(ownerName);
            users.add(owner);
        }

        List<User> savedUsers = userRepository.saveAll(users);
        log.info("관장 {} 명 저장 완료", savedUsers.size());

        for (User owner : savedUsers) {
            Tenant tenant = Tenant.create(owner.getId());
            tenants.add(tenant);
        }

        List<Tenant> savedTenants = tenantRepository.saveAll(tenants);
        log.info("테넌트 {} 개 저장 완료", savedTenants.size());

        for (int i = 0; i < rows.size(); i++) {
            DojangCsvRow row = rows.get(i);
            User owner = savedUsers.get(i);
            Tenant tenant = savedTenants.get(i);

            Dojang dojang = Dojang.create(tenant.getId(), owner.getId(), row.name(), row.address(), null);
            dojangs.add(dojang);
        }

        List<Dojang> savedDojangs = dojangRepository.saveAll(dojangs);
        log.info("도장 {} 개 저장 완료", savedDojangs.size());

        return savedDojangs.size();
    }

    private List<DojangCsvRow> parseCsv() {
        List<DojangCsvRow> rows = new ArrayList<>();

        try {
            ClassPathResource resource = new ClassPathResource("dojang_list.csv");
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {

                String line;
                boolean isHeader = true;

                while ((line = reader.readLine()) != null) {
                    if (isHeader) {
                        isHeader = false;
                        continue;
                    }

                    String[] parts = parseCsvLine(line);
                    if (parts.length >= 2) {
                        rows.add(new DojangCsvRow(parts[0].trim(), parts[1].trim()));
                    }
                }
            }
        } catch (Exception e) {
            log.error("CSV 파싱 실패", e);
            throw new RuntimeException("CSV 파싱 실패", e);
        }

        return rows;
    }

    private String[] parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        result.add(current.toString());

        return result.toArray(new String[0]);
    }

    private User createOwner(String name) {
        User user = User.createWithoutRole(name, null, null);
        user.updateRole(UserRole.OWNER);
        user.updateOnboardingStep(OnboardingStep.COMPLETED);
        return user;
    }

    private String generateRandomKoreanName() {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        String first = FIRST_SYLLABLES[random.nextInt(FIRST_SYLLABLES.length)];
        String middle = MIDDLE_SYLLABLES[random.nextInt(MIDDLE_SYLLABLES.length)];
        String last = LAST_SYLLABLES[random.nextInt(LAST_SYLLABLES.length)];

        return first + middle + last;
    }

    private record DojangCsvRow(String name, String address) {}
}
