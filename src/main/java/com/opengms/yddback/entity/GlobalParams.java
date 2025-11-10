package com.opengms.yddback.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "global_params",
        uniqueConstraints = @UniqueConstraint(columnNames = {"year"}))
public class GlobalParams {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "year", nullable = false)
    private Integer year;

    /**
     * 总体gdp增长率
     */
    @Column(name = "total_gdp_growth_rate")
    private BigDecimal totalGdpGrowthRate;
}
