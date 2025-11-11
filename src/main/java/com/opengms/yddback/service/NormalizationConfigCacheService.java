package com.opengms.yddback.service;

import com.opengms.yddback.entity.IndicatorNormalizationConfig;
import com.opengms.yddback.repository.IndicatorNormalizationConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 归一化配置缓存服务
 * 用于缓存归一化配置，避免频繁查询数据库
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NormalizationConfigCacheService {

    private final IndicatorNormalizationConfigRepository configRepository;

    /**
     * 内存缓存
     * key: 指标代码
     * value: 归一化配置
     */
    private final Map<String, IndicatorNormalizationConfig> configCache = new HashMap<>();

    /**
     * 应用启动时加载所有配置到缓存
     */
    @PostConstruct
    public void init() {
        refreshCache();
    }

    /**
     * 刷新缓存
     */
    public void refreshCache() {
        log.info("开始加载归一化配置到缓存...");

        List<IndicatorNormalizationConfig> configs = configRepository.findAll();

        configCache.clear();
        for (IndicatorNormalizationConfig config : configs) {
            configCache.put(config.getIndicatorCode(), config);
        }

        log.info("归一化配置加载完成，共{}条配置", configCache.size());
    }

    /**
     * 根据指标代码获取配置
     *
     * @param indicatorCode 指标代码
     * @return 归一化配置
     */
    public Optional<IndicatorNormalizationConfig> getConfig(String indicatorCode) {
        // 先从缓存获取
        IndicatorNormalizationConfig config = configCache.get(indicatorCode);

        if (config != null) {
            return Optional.of(config);
        }

        // 缓存中没有，从数据库查询并更新缓存
        log.warn("指标{}的归一化配置未在缓存中，从数据库查询", indicatorCode);
        Optional<IndicatorNormalizationConfig> dbConfig = configRepository.findByIndicatorCode(indicatorCode);

        dbConfig.ifPresent(c -> configCache.put(indicatorCode, c));

        return dbConfig;
    }

    /**
     * 获取所有配置
     */
    public Map<String, IndicatorNormalizationConfig> getAllConfigs() {
        return new HashMap<>(configCache);
    }
}