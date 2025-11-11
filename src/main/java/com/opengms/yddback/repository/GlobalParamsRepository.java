package com.opengms.yddback.repository;

import com.opengms.yddback.entity.GlobalParams;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GlobalParamsRepository extends JpaRepository<GlobalParams, Long> {
    /**
     * 查询某年的全局参数
     */
    Optional<GlobalParams> findByYear(Integer year);
}
