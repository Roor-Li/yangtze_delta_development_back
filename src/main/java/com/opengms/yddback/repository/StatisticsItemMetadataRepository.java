package com.opengms.yddback.repository;

import com.opengms.yddback.entity.StatisticsItemMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StatisticsItemMetadataRepository extends JpaRepository<StatisticsItemMetadata, Long> {

    Optional<StatisticsItemMetadata> findByItemCode(String itemCode);

    Optional<StatisticsItemMetadata> findByItemName(String itemName);

    List<StatisticsItemMetadata> findByIsActiveTrue();

    boolean existsByItemCode(String itemCode);
}