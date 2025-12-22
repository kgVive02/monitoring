package com.kgm.monitoring.service;

import com.kgm.monitoring.config.SystemPropertiesConfig;
import com.kgm.monitoring.model.ServiceInfo;
import com.kgm.monitoring.util.ConfigValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 설정 관리 서비스
 * - 서비스 설정 정보 제공
 * - 설정 유효성 검증
 * - 설정 변경 이벤트 처리
 */
@Service
public class ConfigService implements SystemPropertiesConfig.ConfigChangeListener {
    
    @Autowired
    private SystemPropertiesConfig systemPropertiesConfig;
    
    // 설정 변경 리스너 목록
    private final List<ServiceConfigChangeListener> serviceChangeListeners = 
            new ArrayList<ServiceConfigChangeListener>();
    
    /**
     * 서비스 설정 변경 리스너 인터페이스
     */
    public interface ServiceConfigChangeListener {
        void onServiceConfigChanged(Map<String, ServiceInfo> newServiceConfig);
    }
    
    @PostConstruct
    public void initialize() {
        // 설정 변경 리스너 등록
        systemPropertiesConfig.addConfigChangeListener(this);
        
        // 초기 설정 검증
        validateAllServices();
    }
    
    /**
     * 모든 서비스 정보 반환
     */
    public Map<String, ServiceInfo> getAllServices() {
        return systemPropertiesConfig.getAllServices();
    }
    
    /**
     * 활성화된 서비스 목록 반환
     */
    public List<ServiceInfo> getEnabledServices() {
        List<ServiceInfo> enabledServices = new ArrayList<ServiceInfo>();
        
        Map<String, ServiceInfo> allServices = systemPropertiesConfig.getAllServices();
        for (ServiceInfo service : allServices.values()) {
            if (service.isEnabled()) {
                enabledServices.add(service);
            }
        }
        
        return enabledServices;
    }
    
    /**
     * 특정 서비스 정보 반환
     */
    public ServiceInfo getService(String serviceId) {
        return systemPropertiesConfig.getService(serviceId);
    }
    
    /**
     * 서비스 존재 여부 확인
     */
    public boolean hasService(String serviceId) {
        return systemPropertiesConfig.getService(serviceId) != null;
    }
    
    /**
     * 전역 설정값 반환
     */
    public String getGlobalConfig(String key) {
        return systemPropertiesConfig.getGlobalConfig(key);
    }
    
    /**
     * 전역 설정값 반환 (기본값 포함)
     */
    public String getGlobalConfig(String key, String defaultValue) {
        return systemPropertiesConfig.getGlobalConfig(key, defaultValue);
    }
    
    /**
     * 전역 설정값 반환 (정수형)
     */
    public int getGlobalConfigInt(String key, int defaultValue) {
        return systemPropertiesConfig.getGlobalConfigInt(key, defaultValue);
    }
    
    /**
     * SNMP 폴링 간격 반환 (밀리초)
     */
    public int getPollingInterval() {
        return getGlobalConfigInt("global.polling.interval", 5000);
    }
    
    /**
     * Ring Buffer 크기 반환
     */
    public int getRingBufferSize() {
        return getGlobalConfigInt("global.ringbuffer.size", 120);
    }
    
    /**
     * SNMP 기본 타임아웃 반환 (밀리초)
     */
    public int getDefaultSnmpTimeout() {
        return getGlobalConfigInt("global.snmp.timeout", 3000);
    }
    
    /**
     * SNMP 기본 재시도 횟수 반환
     */
    public int getDefaultSnmpRetries() {
        return getGlobalConfigInt("global.snmp.retries", 2);
    }
    
    /**
     * 설정 파일 수동 리로드
     */
    public void reloadConfiguration() {
        systemPropertiesConfig.reloadConfiguration();
    }
    
    /**
     * 모든 서비스 설정 검증
     */
    public List<String> validateAllServices() {
        List<String> allErrors = new ArrayList<String>();
        
        Map<String, ServiceInfo> allServices = systemPropertiesConfig.getAllServices();
        
        for (Map.Entry<String, ServiceInfo> entry : allServices.entrySet()) {
            String serviceId = entry.getKey();
            ServiceInfo serviceInfo = entry.getValue();
            
            ConfigValidator.ValidationResult result = ConfigValidator.validateServiceInfo(serviceInfo);
            
            if (result.hasErrors()) {
                String errorMsg = "서비스 [" + serviceId + "] 설정 오류: " + result.getErrorMessage();
                allErrors.add(errorMsg);
                System.err.println(errorMsg);
            }
            
            if (result.hasWarnings()) {
                String warningMsg = "서비스 [" + serviceId + "] 설정 경고: " + result.getWarningMessage();
                System.out.println(warningMsg);
            }
        }
        
        if (allErrors.isEmpty()) {
            System.out.println("모든 서비스 설정 검증 완료: " + allServices.size() + "개 서비스");
        } else {
            System.err.println("설정 검증 실패: " + allErrors.size() + "개 오류 발견");
        }
        
        return allErrors;
    }
    
    /**
     * 특정 서비스 설정 검증
     */
    public ConfigValidator.ValidationResult validateService(String serviceId) {
        ServiceInfo serviceInfo = getService(serviceId);
        if (serviceInfo == null) {
            ConfigValidator.ValidationResult result = new ConfigValidator.ValidationResult();
            result.addError("서비스를 찾을 수 없습니다: " + serviceId);
            return result;
        }
        
        return ConfigValidator.validateServiceInfo(serviceInfo);
    }
    
    /**
     * 서비스 설정 변경 리스너 등록
     */
    public void addServiceConfigChangeListener(ServiceConfigChangeListener listener) {
        serviceChangeListeners.add(listener);
    }
    
    /**
     * 설정 변경 이벤트 처리 (SystemPropertiesConfig.ConfigChangeListener 구현)
     */
    public void onConfigChanged(Map<String, ServiceInfo> newServiceConfig) {
        System.out.println("서비스 설정 변경 감지: " + newServiceConfig.size() + "개 서비스");
        
        // 새로운 설정 검증
        List<String> errors = validateAllServices();
        
        if (!errors.isEmpty()) {
            System.err.println("새로운 설정에 오류가 있습니다. 이전 설정을 유지합니다.");
            return;
        }
        
        // 서비스 변경 리스너들에게 알림
        for (ServiceConfigChangeListener listener : serviceChangeListeners) {
            try {
                listener.onServiceConfigChanged(newServiceConfig);
            } catch (Exception e) {
                System.err.println("서비스 설정 변경 리스너 호출 중 오류: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * 서비스 통계 정보 반환
     */
    public ServiceStats getServiceStats() {
        Map<String, ServiceInfo> allServices = getAllServices();
        
        int totalServices = allServices.size();
        int enabledServices = 0;
        int totalServers = 0;
        int l4Services = 0;
        int glbServices = 0;
        
        for (ServiceInfo service : allServices.values()) {
            if (service.isEnabled()) {
                enabledServices++;
            }
            
            totalServers += service.getTotalServerCount();
            
            if ("L4".equals(service.getMonitoringType())) {
                l4Services++;
            } else if ("GLB".equals(service.getMonitoringType())) {
                glbServices++;
            }
        }
        
        return new ServiceStats(totalServices, enabledServices, totalServers, l4Services, glbServices);
    }
    
    /**
     * 서비스 통계 정보 클래스
     */
    public static class ServiceStats {
        private final int totalServices;
        private final int enabledServices;
        private final int totalServers;
        private final int l4Services;
        private final int glbServices;
        
        public ServiceStats(int totalServices, int enabledServices, int totalServers, 
                           int l4Services, int glbServices) {
            this.totalServices = totalServices;
            this.enabledServices = enabledServices;
            this.totalServers = totalServers;
            this.l4Services = l4Services;
            this.glbServices = glbServices;
        }
        
        public int getTotalServices() { return totalServices; }
        public int getEnabledServices() { return enabledServices; }
        public int getTotalServers() { return totalServers; }
        public int getL4Services() { return l4Services; }
        public int getGlbServices() { return glbServices; }
        
        public String toString() {
            return String.format("ServiceStats{total=%d, enabled=%d, servers=%d, L4=%d, GLB=%d}",
                    totalServices, enabledServices, totalServers, l4Services, glbServices);
        }
    }
}