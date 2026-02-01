#  Async Settlement System
> **Large-Scale Billing & Notification System with Spring Batch & Kafka**

본 프로젝트는 **100만 사용자 및 500만 사용 이력**을 대상으로 대규모 정산을 수행하고, 결과를 **Kafka 기반 비동기 메시지**로 송신하는 고성능 정산 시스템입니다.

---

## 1. 소개 및 요구사항 (Introduction & Requirements)

이 프로젝트는 대규모 트래픽 환경에서 대량의 데이터를 안정적으로 정산하고, 통신 요금 명세서를 비동기적으로 발송하는 인프라를 구축하는 데 중점을 둡니다.

###  핵심 요구사항
- **대규모 데이터 정산**: 100만 사용자 데이터를 Chunk 기반으로 안정적 처리
- **비동기 이벤트 기반**: Kafka를 활용한 정산 결과 송신 및 알림 발송 분리
- **성능 최적화**: Redis 캐싱을 통한 DB 부하 분산 및 트래픽 병목 현상 해소
- **데이터 정합성**: 장애 발생 시 재처리 가능(Restartable)한 배치 설계 및 중복 발송 방지

---

## 2.  주요 기능 (Key Features)

### 🧾 정산 관리 (Settlement - Spring Batch)
- **정산 데이터 확정**: 요금 계산 후 DB에 정상 커밋된 건에 한해서만 Kafka "정산 완료" 이벤트 발행
- **배치 전략**: 실패 시 중단 지점부터 재시도 가능한 **Restart/Skip/Retry** 정책 적용

### 메시지 발송 (Messaging - Kafka & Redis)
- **비동기 알림**: Kafka 이벤트를 소비하여 실제 이메일, 문자, 푸시 메시지 발송
- **중복 방지**: Redis 분산 락 및 메시지 DB 기록을 통해 동일 이벤트 중복 처리 원천 차단
- **발송 기록 관리**: 모든 메시지 발송 결과를 기록하며 실패 건에 대한 운영자 재시도 지원

###  관리자 제어 (Admin Console)
- **상태 모니터링**: 배치 작업의 진행 상태와 메시지 큐 상태 실시간 조회
- **명령 제어**: 명령 ID 기반으로 중복 요청을 방지하며 작업 시작/중지/재시도 제어

---

## 3.  기술 스택 및 배포 인프라 (Tech Stack & Infrastructure)

본 시스템은 안정적인 서비스 운영을 위해 분산된 클라우드 인프라를 활용합니다.

### Backend & Middleware
- **Language/Framework**: Java 17 / Spring Boot 3.x
- **Batch Processing**: Spring Batch
- **Message Broker**: **Apache Kafka (GCP Cloud 호스팅)**
- **Caching & Delay Queue**: Redis (분산 락 및 임시 저장)
- **Database**: **MySQL 8.0 (Cafe24 호스팅)**

### Deployment Environment
- **Application Server**: AWS EC2 / Docker & Docker Compose
- **Main Database**: **Cafe24 MySQL** (사용자 및 정산 이력 영속화)
- **Messaging Cluster**: **GCP (Google Cloud Platform)** (Kafka & Zookeeper 운영)

---

## 4.  디렉토리 구조 (Directory Structure)

본 프로젝트는 **모노레포(Monorepo)** 구조로 관리되며, 각 모듈은 독립적인 책임을 가집니다.

```text
Ureka_plus
├── billing_api (Dashboard & Admin API)
│   ├── controller      # 모니터링 및 관리자 제어 API
│   ├── domain          # 정산/메시지 도메인 엔티티 및 DTO
│   └── resources       # 대시보드 UI (HTML/CSS) 및 설정
│
├── billing_batch (Settlement Engine)
│   ├── config          # Batch Job & Kafka Producer 설정
│   ├── domain/dto      # 요금 계산 및 정책(할인/추가요금) 모델
│   ├── repository      # 정산 데이터 영속화 레이어
│   └── common          # 배치 실행 리스너 및 로깅 유틸
│
├── billing_message (Messaging Consumer)
│   ├── listener        # Kafka Message Consumer (정산 완료 이벤트 구독)
│   ├── service         # 채널별 메시지 발송 로직
│   └── config          # Kafka Consumer & Redis 분산 락 설정
│
└── billing_common (Shared)
    └── common          # 암호화(Crypto), 마스킹 유틸리티 등 공통 로직

```
---
## 5. DB 스키마
** Batch Meta**

<img width="520" height="443" alt="image" src="https://github.com/user-attachments/assets/2696a54b-3fb6-4c66-ad4f-9fb5880472b1" />

** Batch origin **

<img width="744" height="432" alt="image" src="https://github.com/user-attachments/assets/dbd6eac5-4bb4-4c05-99c7-4c5f15806679" />

** Message **

<img width="511" height="445" alt="image" src="https://github.com/user-attachments/assets/3b8c103e-41f0-4402-bac9-4b5893837d41" />

---
## 6. 모듈 구조
<img width="563" height="414" alt="image" src="https://github.com/user-attachments/assets/917f5553-157a-4924-879e-31a17e5ea326" />

--- 
## 7. 플로우 차트
<img width="810" height="404" alt="image" src="https://github.com/user-attachments/assets/21dfe35e-a979-47d0-a79f-5c1a38f0a3c8" />

<img width="698" height="394" alt="image" src="https://github.com/user-attachments/assets/091f334b-9a80-4021-954a-880f99274e0d" />

<img width="604" height="381" alt="image" src="https://github.com/user-attachments/assets/94743639-041e-47f3-9c2a-8e49ac034be4" />






