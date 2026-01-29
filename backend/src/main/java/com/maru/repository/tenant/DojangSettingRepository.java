package com.maru.repository.tenant;

import com.maru.domain.tenant.DojangSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DojangSettingRepository extends JpaRepository<DojangSetting, String> {

    Optional<DojangSetting> findByDojangId(String dojangId);
}
