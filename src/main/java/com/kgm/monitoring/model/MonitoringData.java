package com.kgm.monitoring.model;

import java.util.Date;

/**
 * 모니터링 데이터 모델
 * - NBP/DR 세션 수 및 트래픽 정보
 * - 수집 시간
 * - 에러 상태
 */
public class MonitoringData {
    
    private String serviceId;
    private Date timestamp;
    private long nbpSessions;
    private long drSessions;
    private long nbpTraffic;
    private long drTraffic;
    private boolean hasError;
    private String errorMessage;
    
    // TODO: Task 2.3에서 상세 구현 예정
    
    // Getters and Setters
    public String getServiceId() {
        return serviceId;
    }
    
    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }
    
    public Date getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
    
    public long getNbpSessions() {
        return nbpSessions;
    }
    
    public void setNbpSessions(long nbpSessions) {
        this.nbpSessions = nbpSessions;
    }
    
    public long getDrSessions() {
        return drSessions;
    }
    
    public void setDrSessions(long drSessions) {
        this.drSessions = drSessions;
    }
    
    public long getNbpTraffic() {
        return nbpTraffic;
    }
    
    public void setNbpTraffic(long nbpTraffic) {
        this.nbpTraffic = nbpTraffic;
    }
    
    public long getDrTraffic() {
        return drTraffic;
    }
    
    public void setDrTraffic(long drTraffic) {
        this.drTraffic = drTraffic;
    }
    
    public boolean isHasError() {
        return hasError;
    }
    
    public void setHasError(boolean hasError) {
        this.hasError = hasError;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}