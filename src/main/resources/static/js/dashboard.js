/**
 * NBP/DR 모니터링 대시보드 JavaScript
 */

class MonitoringDashboard {
    constructor() {
        this.services = [];
        this.selectedService = null;
        this.updateInterval = 5000; // 5초마다 업데이트
        this.init();
    }

    init() {
        this.loadServices();
        this.startAutoUpdate();
        this.bindEvents();
    }

    async loadServices() {
        try {
            const response = await fetch('/api/services');
            this.services = await response.json();
            this.renderServiceList();
            this.updateConnectionStatus(true);
        } catch (error) {
            console.error('서비스 목록 로드 실패:', error);
            this.updateConnectionStatus(false);
        }
    }

    renderServiceList() {
        const serviceList = document.getElementById('service-list');
        serviceList.innerHTML = '';

        this.services.forEach(service => {
            const serviceCard = this.createServiceCard(service);
            serviceList.appendChild(serviceCard);
        });
    }

    createServiceCard(service) {
        const card = document.createElement('div');
        card.className = 'service-card';
        card.dataset.serviceId = service.id;
        
        card.innerHTML = `
            <h3>${service.name}</h3>
            <p>타입: ${service.type}</p>
            <div class="ratio-display">
                <div class="ratio-bar">
                    <div class="ratio-nbp" style="width: ${service.nbpRatio}%"></div>
                    <div class="ratio-dr" style="width: ${service.drRatio}%"></div>
                </div>
                <p>NBP: ${service.nbpRatio}% | DR: ${service.drRatio}%</p>
            </div>
            <p class="status ${service.status}">상태: ${this.getStatusText(service.status)}</p>
        `;

        card.addEventListener('click', () => {
            this.selectService(service.id);
        });

        return card;
    }

    getStatusText(status) {
        switch(status) {
            case 'ok': return '정상';
            case 'warning': return '경고';
            case 'error': return '오류';
            default: return '알 수 없음';
        }
    }

    selectService(serviceId) {
        this.selectedService = serviceId;
        this.loadServiceDetail(serviceId);
        document.getElementById('service-detail').style.display = 'block';
    }

    async loadServiceDetail(serviceId) {
        try {
            const response = await fetch(`/api/services/${serviceId}/ratio`);
            const data = await response.json();
            this.renderServiceDetail(data);
        } catch (error) {
            console.error('서비스 상세 정보 로드 실패:', error);
        }
    }

    renderServiceDetail(data) {
        // TODO: Task 4.2, 4.3에서 차트 구현 예정
        console.log('서비스 상세 데이터:', data);
    }

    startAutoUpdate() {
        setInterval(() => {
            this.loadServices();
            if (this.selectedService) {
                this.loadServiceDetail(this.selectedService);
            }
        }, this.updateInterval);
    }

    updateConnectionStatus(connected) {
        const statusElement = document.getElementById('connection-status');
        const lastUpdateElement = document.getElementById('last-update');
        
        if (connected) {
            statusElement.textContent = '연결됨';
            statusElement.className = 'status-ok';
        } else {
            statusElement.textContent = '연결 끊김';
            statusElement.className = 'status-error';
        }
        
        lastUpdateElement.textContent = `마지막 업데이트: ${new Date().toLocaleTimeString()}`;
    }

    bindEvents() {
        // 추가 이벤트 바인딩
        // TODO: Task 4.4에서 구현 예정
    }
}

// 페이지 로드 시 대시보드 초기화
document.addEventListener('DOMContentLoaded', () => {
    new MonitoringDashboard();
});

// TODO: Task 4.1, 4.2, 4.3에서 상세 기능 구현 예정