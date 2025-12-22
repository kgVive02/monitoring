package com.kgm.monitoring.config;

import com.kgm.monitoring.model.ServiceInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * SystemPropertiesConfig 테스트
 */
@RunWith(SpringRunner.class)
public class SystemPropertiesConfigTest {
    
    private SystemPropertiesConfig config;
    private File testPropertiesFile;
    
    @Before
    public void setUp() throws IOException {
        // 테스트용 임시 설정 파일 생성
        testPropertiesFile = File.createTempFile("test-system", ".properties");
        testPropertiesFile.deleteOnExit();
        
        // 테스트 설정 내용 작성
        FileWriter writer = new FileWriter(testPropertiesFile);
        writer.write("# 테스트 설정 파일\n");
        writer.write("services=TEST_SERVICE\n");
        writer.write("service.TEST_SERVICE.id=TEST_SERVICE\n");
        writer.write("service.TEST_SERVICE.name=테스트 서비스\n");
        writer.write("service.TEST_SERVICE.type=L4\n");
        writer.write("service.TEST_SERVICE.vip=192.168.1.100\n");
        writer.write("service.TEST_SERVICE.port=80\n");
        writer.write("service.TEST_SERVICE.enabled=true\n");
        writer.write("service.TEST_SERVICE.nbp.servers=192.168.1.10,192.168.1.11\n");
        writer.write("service.TEST_SERVICE.dr.servers=192.168.2.10,192.168.2.11\n");
        writer.write("service.TEST_SERVICE.snmp.community=public\n");
        writer.write("service.TEST_SERVICE.snmp.oid.sessions=1.3.6.1.2.1.6.9.0\n");
        writer.write("service.TEST_SERVICE.snmp.oid.traffic=1.3.6.1.2.1.2.2.1.10.1\n");
        writer.write("global.snmp.timeout=3000\n");
        writer.write("global.polling.interval=5000\n");
        writer.close();
        
        // SystemPropertiesConfig 인스턴스 생성 및 설정
        config = new SystemPropertiesConfig();
        // 리플렉션을 사용하여 private 필드 설정 (실제로는 @Value 어노테이션으로 주입됨)
        setPrivateField(config, "propertiesFilePath", testPropertiesFile.getAbsolutePath());
        setPrivateField(config, "enableFileWatch", false);
    }
    
    @Test
    public void testLoadConfiguration() {
        // 설정 로딩
        config.loadConfiguration();
        
        // 서비스 정보 확인
        Map<String, ServiceInfo> services = config.getAllServices();
        assertNotNull("서비스 목록이 null이면 안됩니다", services);
        assertEquals("서비스 개수가 1개여야 합니다", 1, services.size());
        
        ServiceInfo testService = config.getService("TEST_SERVICE");
        assertNotNull("TEST_SERVICE가 존재해야 합니다", testService);
        assertEquals("서비스 ID가 일치해야 합니다", "TEST_SERVICE", testService.getServiceId());
        assertEquals("서비스명이 일치해야 합니다", "테스트 서비스", testService.getServiceName());
        assertEquals("모니터링 타입이 L4여야 합니다", "L4", testService.getMonitoringType());
        assertEquals("VIP가 일치해야 합니다", "192.168.1.100", testService.getVip());
        assertEquals("포트가 80이어야 합니다", 80, testService.getPort());
        assertTrue("서비스가 활성화되어야 합니다", testService.isEnabled());
    }
    
    @Test
    public void testServerListLoading() {
        config.loadConfiguration();
        
        ServiceInfo testService = config.getService("TEST_SERVICE");
        assertNotNull("TEST_SERVICE가 존재해야 합니다", testService);
        
        // NBP 서버 목록 확인
        assertNotNull("NBP 서버 목록이 null이면 안됩니다", testService.getNbpServers());
        assertEquals("NBP 서버가 2개여야 합니다", 2, testService.getNbpServers().size());
        assertEquals("첫 번째 NBP 서버 IP", "192.168.1.10", testService.getNbpServers().get(0).getIp());
        assertEquals("두 번째 NBP 서버 IP", "192.168.1.11", testService.getNbpServers().get(1).getIp());
        
        // DR 서버 목록 확인
        assertNotNull("DR 서버 목록이 null이면 안됩니다", testService.getDrServers());
        assertEquals("DR 서버가 2개여야 합니다", 2, testService.getDrServers().size());
        assertEquals("첫 번째 DR 서버 IP", "192.168.2.10", testService.getDrServers().get(0).getIp());
        assertEquals("두 번째 DR 서버 IP", "192.168.2.11", testService.getDrServers().get(1).getIp());
    }
    
    @Test
    public void testSnmpConfigLoading() {
        config.loadConfiguration();
        
        ServiceInfo testService = config.getService("TEST_SERVICE");
        assertNotNull("TEST_SERVICE가 존재해야 합니다", testService);
        
        ServiceInfo.SnmpConfig snmpConfig = testService.getSnmpConfig();
        assertNotNull("SNMP 설정이 null이면 안됩니다", snmpConfig);
        assertEquals("SNMP Community", "public", snmpConfig.getCommunity());
        assertEquals("SNMP 포트", 161, snmpConfig.getPort());
        assertEquals("SNMP 버전", "2c", snmpConfig.getVersion());
        assertEquals("세션 OID", "1.3.6.1.2.1.6.9.0", snmpConfig.getSessionsOid());
        assertEquals("트래픽 OID", "1.3.6.1.2.1.2.2.1.10.1", snmpConfig.getTrafficOid());
        assertEquals("타임아웃", 3000, snmpConfig.getTimeout());
        assertEquals("재시도 횟수", 2, snmpConfig.getRetries());
    }
    
    @Test
    public void testGlobalConfig() {
        config.loadConfiguration();
        
        assertEquals("전역 SNMP 타임아웃", "3000", config.getGlobalConfig("global.snmp.timeout"));
        assertEquals("전역 폴링 간격", "5000", config.getGlobalConfig("global.polling.interval"));
        assertEquals("기본값 반환", "default", config.getGlobalConfig("nonexistent.key", "default"));
        assertEquals("정수형 전역 설정", 3000, config.getGlobalConfigInt("global.snmp.timeout", 1000));
        assertEquals("정수형 기본값", 1000, config.getGlobalConfigInt("nonexistent.key", 1000));
    }
    
    /**
     * 리플렉션을 사용하여 private 필드 설정
     */
    private void setPrivateField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("필드 설정 실패: " + fieldName, e);
        }
    }
}