package com.kgm.monitoring.util;

import com.kgm.monitoring.model.ServiceInfo;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * 설정 정보 검증 유틸리티
 * - 서비스 설정 유효성 검사
 * - IP 주소 형식 검증
 * - SNMP 설정 검증
 */
public class ConfigValidator {
    
    /**
     * 서비스 정보 전체 검증
     */
    public static ValidationResult validateServiceInfo(ServiceInfo serviceInfo) {
        ValidationResult result = new ValidationResult();
        
        if (serviceInfo == null) {
            result.addError("서비스 정보가 null입니다.");
            return result;
        }
        
        // 기본 정보 검증
        validateBasicInfo(serviceInfo, result);
        
        // 서버 목록 검증
        validateServerList(serviceInfo, result);
        
        // SNMP 설정 검증
        validateSnmpConfig(serviceInfo, result);
        
        return result;
    }
    
    /**
     * 기본 정보 검증
     */
    private static void validateBasicInfo(ServiceInfo serviceInfo, ValidationResult result) {
        // 서비스 ID 검증
        if (isNullOrEmpty(serviceInfo.getServiceId())) {
            result.addError("서비스 ID가 비어있습니다.");
        }
        
        // 서비스명 검증
        if (isNullOrEmpty(serviceInfo.getServiceName())) {
            result.addError("서비스명이 비어있습니다.");
        }
        
        // 모니터링 타입 검증
        String type = serviceInfo.getMonitoringType();
        if (isNullOrEmpty(type)) {
            result.addError("모니터링 타입이 비어있습니다.");
        } else if (!"L4".equals(type) && !"GLB".equals(type)) {
            result.addError("모니터링 타입은 L4 또는 GLB여야 합니다: " + type);
        }
        
        // VIP 검증
        if (isNullOrEmpty(serviceInfo.getVip())) {
            result.addError("VIP가 비어있습니다.");
        } else if (!isValidIpAddress(serviceInfo.getVip())) {
            result.addError("VIP 형식이 올바르지 않습니다: " + serviceInfo.getVip());
        }
        
        // 포트 검증
        int port = serviceInfo.getPort();
        if (port <= 0 || port > 65535) {
            result.addError("포트 번호가 올바르지 않습니다: " + port);
        }
    }
    
    /**
     * 서버 목록 검증
     */
    private static void validateServerList(ServiceInfo serviceInfo, ValidationResult result) {
        // NBP 서버 검증
        List<ServiceInfo.ServerInfo> nbpServers = serviceInfo.getNbpServers();
        if (nbpServers == null || nbpServers.isEmpty()) {
            result.addError("NBP 서버 목록이 비어있습니다.");
        } else {
            for (int i = 0; i < nbpServers.size(); i++) {
                ServiceInfo.ServerInfo server = nbpServers.get(i);
                validateServerInfo(server, "NBP 서버[" + i + "]", result);
            }
        }
        
        // DR 서버 검증
        List<ServiceInfo.ServerInfo> drServers = serviceInfo.getDrServers();
        if (drServers == null || drServers.isEmpty()) {
            result.addError("DR 서버 목록이 비어있습니다.");
        } else {
            for (int i = 0; i < drServers.size(); i++) {
                ServiceInfo.ServerInfo server = drServers.get(i);
                validateServerInfo(server, "DR 서버[" + i + "]", result);
            }
        }
    }
    
    /**
     * 개별 서버 정보 검증
     */
    private static void validateServerInfo(ServiceInfo.ServerInfo server, String prefix, ValidationResult result) {
        if (server == null) {
            result.addError(prefix + "가 null입니다.");
            return;
        }
        
        // IP 주소 검증
        if (isNullOrEmpty(server.getIp())) {
            result.addError(prefix + " IP가 비어있습니다.");
        } else if (!isValidIpAddress(server.getIp())) {
            result.addError(prefix + " IP 형식이 올바르지 않습니다: " + server.getIp());
        }
        
        // 서버명 검증
        if (isNullOrEmpty(server.getName())) {
            result.addWarning(prefix + " 서버명이 비어있습니다.");
        }
    }
    
    /**
     * SNMP 설정 검증
     */
    private static void validateSnmpConfig(ServiceInfo serviceInfo, ValidationResult result) {
        ServiceInfo.SnmpConfig snmpConfig = serviceInfo.getSnmpConfig();
        if (snmpConfig == null) {
            result.addError("SNMP 설정이 null입니다.");
            return;
        }
        
        // Community 검증
        if (isNullOrEmpty(snmpConfig.getCommunity())) {
            result.addError("SNMP Community가 비어있습니다.");
        }
        
        // 포트 검증
        int port = snmpConfig.getPort();
        if (port <= 0 || port > 65535) {
            result.addError("SNMP 포트 번호가 올바르지 않습니다: " + port);
        }
        
        // 버전 검증
        String version = snmpConfig.getVersion();
        if (isNullOrEmpty(version)) {
            result.addError("SNMP 버전이 비어있습니다.");
        } else if (!"1".equals(version) && !"2c".equals(version) && !"3".equals(version)) {
            result.addError("SNMP 버전이 올바르지 않습니다: " + version);
        }
        
        // OID 검증
        if (isNullOrEmpty(snmpConfig.getSessionsOid())) {
            result.addError("세션 수 OID가 비어있습니다.");
        } else if (!isValidOid(snmpConfig.getSessionsOid())) {
            result.addError("세션 수 OID 형식이 올바르지 않습니다: " + snmpConfig.getSessionsOid());
        }
        
        if (isNullOrEmpty(snmpConfig.getTrafficOid())) {
            result.addError("트래픽 OID가 비어있습니다.");
        } else if (!isValidOid(snmpConfig.getTrafficOid())) {
            result.addError("트래픽 OID 형식이 올바르지 않습니다: " + snmpConfig.getTrafficOid());
        }
        
        // 타임아웃 검증
        if (snmpConfig.getTimeout() <= 0) {
            result.addError("SNMP 타임아웃이 올바르지 않습니다: " + snmpConfig.getTimeout());
        }
        
        // 재시도 횟수 검증
        if (snmpConfig.getRetries() < 0) {
            result.addError("SNMP 재시도 횟수가 올바르지 않습니다: " + snmpConfig.getRetries());
        }
    }
    
    /**
     * IP 주소 형식 검증
     */
    public static boolean isValidIpAddress(String ip) {
        if (isNullOrEmpty(ip)) {
            return false;
        }
        
        try {
            InetAddress.getByName(ip);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * OID 형식 검증 (간단한 형식 체크)
     */
    public static boolean isValidOid(String oid) {
        if (isNullOrEmpty(oid)) {
            return false;
        }
        
        // OID는 숫자와 점으로만 구성되어야 함
        return oid.matches("^[0-9]+(\\.[0-9]+)*$");
    }
    
    /**
     * 문자열이 null이거나 비어있는지 확인
     */
    private static boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
    
    /**
     * 검증 결과 클래스
     */
    public static class ValidationResult {
        private final List<String> errors = new ArrayList<String>();
        private final List<String> warnings = new ArrayList<String>();
        
        public void addError(String error) {
            errors.add(error);
        }
        
        public void addWarning(String warning) {
            warnings.add(warning);
        }
        
        public boolean hasErrors() {
            return !errors.isEmpty();
        }
        
        public boolean hasWarnings() {
            return !warnings.isEmpty();
        }
        
        public List<String> getErrors() {
            return new ArrayList<String>(errors);
        }
        
        public List<String> getWarnings() {
            return new ArrayList<String>(warnings);
        }
        
        public boolean isValid() {
            return !hasErrors();
        }
        
        public String getErrorMessage() {
            if (errors.isEmpty()) {
                return null;
            }
            
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < errors.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(errors.get(i));
            }
            return sb.toString();
        }
        
        public String getWarningMessage() {
            if (warnings.isEmpty()) {
                return null;
            }
            
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < warnings.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(warnings.get(i));
            }
            return sb.toString();
        }
    }
}