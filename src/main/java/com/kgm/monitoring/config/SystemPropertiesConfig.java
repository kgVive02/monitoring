package com.kgm.monitoring.config;

import com.kgm.monitoring.model.ServiceInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * system.properties 파일 기반 설정 관리
 * - 서비스 설정 정보 로딩
 * - 파일 변경 감지 및 리로드
 * - 설정 정보 메모리 캐시
 */
@Configuration
@Component
public class SystemPropertiesConfig {
    
    @Value("${monitoring.system.properties.path:system.properties}")
    private String propertiesFilePath;
    
    @Value("${monitoring.system.properties.watch:true}")
    private boolean enableFileWatch;
    
    // 서비스 설정 정보 캐시 (Thread-Safe)
    private final Map<String, ServiceInfo> serviceInfoCache = new ConcurrentHashMap<String, ServiceInfo>();
    
    // 전역 설정 캐시
    private final Properties globalConfig = new Properties();
    
    // 파일 변경 감지를 위한 스케줄러
    private ScheduledExecutorService fileWatchScheduler;
    private long lastModified = 0L;
    
    // 설정 변경 리스너 목록
    private final List<ConfigChangeListener> changeListeners = new ArrayList<ConfigChangeListener>();
    
    /**
     * 설정 변경 리스너 인터페이스
     */
    public interface ConfigChangeListener {
        void onConfigChanged(Map<String, ServiceInfo> newServiceConfig);
    }
    
    @PostConstruct
    public void initialize() {
        loadConfiguration();
        
        if (enableFileWatch) {
            startFileWatcher();
        }
    }
    
    @PreDestroy
    public void cleanup() {
        if (fileWatchScheduler != null && !fileWatchScheduler.isShutdown()) {
            fileWatchScheduler.shutdown();
        }
    }
    
    /**
     * 설정 파일 로딩
     */
    public synchronized void loadConfiguration() {
        try {
            File configFile = new File(propertiesFilePath);
            if (!configFile.exists()) {
                throw new FileNotFoundException("설정 파일을 찾을 수 없습니다: " + propertiesFilePath);
            }
            
            Properties props = new Properties();
            FileInputStream fis = new FileInputStream(configFile);
            try {
                props.load(fis);
            } finally {
                fis.close();
            }
            
            // 전역 설정 로딩
            loadGlobalConfig(props);
            
            // 서비스 설정 로딩
            Map<String, ServiceInfo> newServiceConfig = loadServiceConfig(props);
            
            // 캐시 업데이트
            serviceInfoCache.clear();
            serviceInfoCache.putAll(newServiceConfig);
            
            // 파일 수정 시간 업데이트
            lastModified = configFile.lastModified();
            
            // 변경 리스너 호출
            notifyConfigChanged(newServiceConfig);
            
            System.out.println("설정 파일 로딩 완료: " + serviceInfoCache.size() + "개 서비스");
            
        } catch (Exception e) {
            System.err.println("설정 파일 로딩 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 전역 설정 로딩
     */
    private void loadGlobalConfig(Properties props) {
        globalConfig.clear();
        
        // 전역 설정 키들
        String[] globalKeys = {
            "global.snmp.timeout",
            "global.snmp.retries", 
            "global.polling.interval",
            "global.ringbuffer.size"
        };
        
        for (String key : globalKeys) {
            String value = props.getProperty(key);
            if (value != null) {
                globalConfig.setProperty(key, value);
            }
        }
    }
    
    /**
     * 서비스 설정 로딩
     */
    private Map<String, ServiceInfo> loadServiceConfig(Properties props) {
        Map<String, ServiceInfo> serviceMap = new HashMap<String, ServiceInfo>();
        
        // 서비스 목록 가져오기
        String servicesStr = props.getProperty("services");
        if (servicesStr == null || servicesStr.trim().isEmpty()) {
            return serviceMap;
        }
        
        String[] serviceIds = servicesStr.split(",");
        
        for (String serviceId : serviceIds) {
            serviceId = serviceId.trim();
            ServiceInfo serviceInfo = loadSingleServiceConfig(props, serviceId);
            if (serviceInfo != null) {
                serviceMap.put(serviceId, serviceInfo);
            }
        }
        
        return serviceMap;
    }
    
    /**
     * 단일 서비스 설정 로딩
     */
    private ServiceInfo loadSingleServiceConfig(Properties props, String serviceId) {
        String prefix = "service." + serviceId + ".";
        
        ServiceInfo serviceInfo = new ServiceInfo();
        
        // 기본 정보
        serviceInfo.setServiceId(props.getProperty(prefix + "id"));
        serviceInfo.setServiceName(props.getProperty(prefix + "name"));
        serviceInfo.setMonitoringType(props.getProperty(prefix + "type"));
        serviceInfo.setVip(props.getProperty(prefix + "vip"));
        
        String portStr = props.getProperty(prefix + "port");
        if (portStr != null) {
            serviceInfo.setPort(Integer.parseInt(portStr));
        }
        
        String enabledStr = props.getProperty(prefix + "enabled", "true");
        serviceInfo.setEnabled(Boolean.parseBoolean(enabledStr));
        
        // NBP 서버 목록 로딩
        List<ServiceInfo.ServerInfo> nbpServers = loadServerList(props, prefix + "nbp");
        serviceInfo.setNbpServers(nbpServers);
        
        // DR 서버 목록 로딩
        List<ServiceInfo.ServerInfo> drServers = loadServerList(props, prefix + "dr");
        serviceInfo.setDrServers(drServers);
        
        // SNMP 설정 로딩
        ServiceInfo.SnmpConfig snmpConfig = loadSnmpConfig(props, prefix + "snmp");
        serviceInfo.setSnmpConfig(snmpConfig);
        
        return serviceInfo;
    }
    
    /**
     * 서버 목록 로딩
     */
    private List<ServiceInfo.ServerInfo> loadServerList(Properties props, String prefix) {
        List<ServiceInfo.ServerInfo> servers = new ArrayList<ServiceInfo.ServerInfo>();
        
        String serversStr = props.getProperty(prefix + ".servers");
        if (serversStr == null || serversStr.trim().isEmpty()) {
            return servers;
        }
        
        String[] serverIps = serversStr.split(",");
        
        for (String ip : serverIps) {
            ip = ip.trim();
            String serverName = props.getProperty(prefix + ".server." + ip + ".name", ip);
            
            ServiceInfo.ServerInfo serverInfo = new ServiceInfo.ServerInfo(ip, serverName);
            servers.add(serverInfo);
        }
        
        return servers;
    }
    
    /**
     * SNMP 설정 로딩
     */
    private ServiceInfo.SnmpConfig loadSnmpConfig(Properties props, String prefix) {
        ServiceInfo.SnmpConfig snmpConfig = new ServiceInfo.SnmpConfig();
        
        snmpConfig.setCommunity(props.getProperty(prefix + ".community", "public"));
        
        String portStr = props.getProperty(prefix + ".port", "161");
        snmpConfig.setPort(Integer.parseInt(portStr));
        
        snmpConfig.setVersion(props.getProperty(prefix + ".version", "2c"));
        snmpConfig.setSessionsOid(props.getProperty(prefix + ".oid.sessions"));
        snmpConfig.setTrafficOid(props.getProperty(prefix + ".oid.traffic"));
        
        // 전역 설정에서 기본값 가져오기
        String timeoutStr = props.getProperty(prefix + ".timeout", 
                                           globalConfig.getProperty("global.snmp.timeout", "3000"));
        snmpConfig.setTimeout(Integer.parseInt(timeoutStr));
        
        String retriesStr = props.getProperty(prefix + ".retries",
                                            globalConfig.getProperty("global.snmp.retries", "2"));
        snmpConfig.setRetries(Integer.parseInt(retriesStr));
        
        return snmpConfig;
    }
    
    /**
     * 파일 변경 감지 시작
     */
    private void startFileWatcher() {
        fileWatchScheduler = Executors.newSingleThreadScheduledExecutor();
        
        fileWatchScheduler.scheduleWithFixedDelay(new Runnable() {
            public void run() {
                checkFileChanges();
            }
        }, 5, 5, TimeUnit.SECONDS); // 5초마다 체크
    }
    
    /**
     * 파일 변경 체크
     */
    private void checkFileChanges() {
        try {
            File configFile = new File(propertiesFilePath);
            if (configFile.exists() && configFile.lastModified() > lastModified) {
                System.out.println("설정 파일 변경 감지, 리로드 중...");
                loadConfiguration();
            }
        } catch (Exception e) {
            System.err.println("파일 변경 감지 중 오류: " + e.getMessage());
        }
    }
    
    /**
     * 설정 변경 리스너 등록
     */
    public void addConfigChangeListener(ConfigChangeListener listener) {
        changeListeners.add(listener);
    }
    
    /**
     * 설정 변경 알림
     */
    private void notifyConfigChanged(Map<String, ServiceInfo> newConfig) {
        for (ConfigChangeListener listener : changeListeners) {
            try {
                listener.onConfigChanged(newConfig);
            } catch (Exception e) {
                System.err.println("설정 변경 리스너 호출 중 오류: " + e.getMessage());
            }
        }
    }
    
    // Public API 메서드들
    
    /**
     * 모든 서비스 정보 반환
     */
    public Map<String, ServiceInfo> getAllServices() {
        return new HashMap<String, ServiceInfo>(serviceInfoCache);
    }
    
    /**
     * 특정 서비스 정보 반환
     */
    public ServiceInfo getService(String serviceId) {
        return serviceInfoCache.get(serviceId);
    }
    
    /**
     * 전역 설정값 반환
     */
    public String getGlobalConfig(String key) {
        return globalConfig.getProperty(key);
    }
    
    /**
     * 전역 설정값 반환 (기본값 포함)
     */
    public String getGlobalConfig(String key, String defaultValue) {
        return globalConfig.getProperty(key, defaultValue);
    }
    
    /**
     * 전역 설정값 반환 (정수형)
     */
    public int getGlobalConfigInt(String key, int defaultValue) {
        String value = globalConfig.getProperty(key);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                System.err.println("설정값 파싱 오류 (" + key + "): " + value);
            }
        }
        return defaultValue;
    }
    
    /**
     * 설정 파일 수동 리로드
     */
    public void reloadConfiguration() {
        loadConfiguration();
    }
}