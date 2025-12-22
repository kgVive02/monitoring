package com.kgm.monitoring.model;

import java.util.List;
import java.util.Map;

/**
 * 서비스 정보 모델
 * - 서비스 ID, 서비스명
 * - 모니터링 타입 (L4/GLB)
 * - VIP, Port 정보
 * - 서버 목록 (NBP/DR)
 * - SNMP 설정 정보
 */
public class ServiceInfo {
    
    private String serviceId;
    private String serviceName;
    private String monitoringType; // L4 or GLB
    private String vip;
    private int port;
    private boolean enabled;
    
    // NBP 서버 정보
    private List<ServerInfo> nbpServers;
    
    // DR 서버 정보
    private List<ServerInfo> drServers;
    
    // SNMP 설정 정보
    private SnmpConfig snmpConfig;
    
    /**
     * 서버 정보 내부 클래스
     */
    public static class ServerInfo {
        private String ip;
        private String name;
        private boolean enabled;
        
        public ServerInfo() {}
        
        public ServerInfo(String ip, String name) {
            this.ip = ip;
            this.name = name;
            this.enabled = true;
        }
        
        // Getters and Setters
        public String getIp() { return ip; }
        public void setIp(String ip) { this.ip = ip; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }
    
    /**
     * SNMP 설정 정보 내부 클래스
     */
    public static class SnmpConfig {
        private String community;
        private int port;
        private String version;
        private String sessionsOid;
        private String trafficOid;
        private int timeout;
        private int retries;
        
        public SnmpConfig() {
            this.port = 161;
            this.version = "2c";
            this.timeout = 3000;
            this.retries = 2;
        }
        
        // Getters and Setters
        public String getCommunity() { return community; }
        public void setCommunity(String community) { this.community = community; }
        
        public int getPort() { return port; }
        public void setPort(int port) { this.port = port; }
        
        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }
        
        public String getSessionsOid() { return sessionsOid; }
        public void setSessionsOid(String sessionsOid) { this.sessionsOid = sessionsOid; }
        
        public String getTrafficOid() { return trafficOid; }
        public void setTrafficOid(String trafficOid) { this.trafficOid = trafficOid; }
        
        public int getTimeout() { return timeout; }
        public void setTimeout(int timeout) { this.timeout = timeout; }
        
        public int getRetries() { return retries; }
        public void setRetries(int retries) { this.retries = retries; }
    }
    
    // 기본 생성자
    public ServiceInfo() {}
    
    // Getters and Setters
    public String getServiceId() {
        return serviceId;
    }
    
    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }
    
    public String getServiceName() {
        return serviceName;
    }
    
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
    
    public String getMonitoringType() {
        return monitoringType;
    }
    
    public void setMonitoringType(String monitoringType) {
        this.monitoringType = monitoringType;
    }
    
    public String getVip() {
        return vip;
    }
    
    public void setVip(String vip) {
        this.vip = vip;
    }
    
    public int getPort() {
        return port;
    }
    
    public void setPort(int port) {
        this.port = port;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public List<ServerInfo> getNbpServers() {
        return nbpServers;
    }
    
    public void setNbpServers(List<ServerInfo> nbpServers) {
        this.nbpServers = nbpServers;
    }
    
    public List<ServerInfo> getDrServers() {
        return drServers;
    }
    
    public void setDrServers(List<ServerInfo> drServers) {
        this.drServers = drServers;
    }
    
    public SnmpConfig getSnmpConfig() {
        return snmpConfig;
    }
    
    public void setSnmpConfig(SnmpConfig snmpConfig) {
        this.snmpConfig = snmpConfig;
    }
    
    /**
     * 전체 서버 수 반환 (NBP + DR)
     */
    public int getTotalServerCount() {
        int count = 0;
        if (nbpServers != null) count += nbpServers.size();
        if (drServers != null) count += drServers.size();
        return count;
    }
    
    /**
     * 활성화된 NBP 서버 수 반환
     */
    public int getActiveNbpServerCount() {
        if (nbpServers == null) return 0;
        int count = 0;
        for (ServerInfo server : nbpServers) {
            if (server.isEnabled()) count++;
        }
        return count;
    }
    
    /**
     * 활성화된 DR 서버 수 반환
     */
    public int getActiveDrServerCount() {
        if (drServers == null) return 0;
        int count = 0;
        for (ServerInfo server : drServers) {
            if (server.isEnabled()) count++;
        }
        return count;
    }
}