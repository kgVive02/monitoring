package com.kgm.monitoring.model;

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
    
    // TODO: Task 1.2에서 상세 구현 예정
    
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
}