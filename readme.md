<div align="center">
  <h1>🚀 To U+</h1>
  <p><strong>대용량 정산 및 메시지 발송 관리 시스템</strong></p>
  <p>
    <img src="https://img.shields.io/badge/Java%2017-007396?style=flat-square&logo=openjdk&logoColor=white"/>
    <img src="https://img.shields.io/badge/Spring_Boot%203.x-6DB33F?style=flat-square&logo=spring-boot&logoColor=white"/>
    <img src="https://img.shields.io/badge/Spring_Batch-6DB33F?style=flat-square&logo=spring&logoColor=white"/>
    <img src="https://img.shields.io/badge/Apache_Kafka-231F20?style=flat-square&logo=apache-kafka&logoColor=white"/>
    <img src="https://img.shields.io/badge/Redis-DC382D?style=flat-square&logo=redis&logoColor=white"/>
    <img src="https://img.shields.io/badge/MySQL%208.0-4479A1?style=flat-square&logo=mysql&logoColor=white"/>
    <img src="https://img.shields.io/badge/Docker-2496ED?style=flat-square&logo=docker&logoColor=white"/>
    <img src="https://img.shields.io/badge/AWS_EC2-FF9900?style=flat-square&logo=amazon-ec2&logoColor=white"/>
  </p>
  <p>
    <img src="https://img.shields.io/badge/상태-배포_중단_(재개_가능)-yellow?style=flat-square"/>
    <img src="https://img.shields.io/badge/플랫폼-AWS_EC2-FF9900?style=flat-square&logo=amazon-aws&logoColor=white"/>
  </p>
</div>

---

## 📌 목차

1. [프로젝트 소개](#-프로젝트-소개)
2. [배포 정보](#-배포-정보)
3. [시작하기](#-시작하기)
4. [환경 변수](#-환경-변수)
5. [멀티 모듈 빌드](#️-멀티-모듈-빌드)
6. [기술 스택](#-기술-스택)
7. [주요 기능](#-주요-기능)
8. [프로젝트 구조](#-프로젝트-구조)
9. [아키텍처 및 워크플로우](#-아키텍처-및-워크플로우)
10. [ERD](#️-erd)
11. [핵심 처리 흐름](#-핵심-처리-흐름)
12. [설계 포인트](#-설계-포인트)
13. [트러블슈팅](#-트러블슈팅)
14. [기대 효과](#-기대-효과)
15. [향후 개선 방향](#-향후-개선-방향)
16. [Demo](#-demo)

---

## 📌 프로젝트 소개

**To U+** 는 대규모 사용자 데이터를 기반으로 정산을 수행하고,
그 결과를 바탕으로 메시지를 생성·발송하는 과정을 통합 관리할 수 있도록 설계한 **백엔드 중심 프로젝트**입니다.

이 프로젝트는 단순한 관리자 페이지 구현이 아니라,
**정산 배치**, **이벤트 기반 메시지 처리**, **운영 모니터링**을 하나의 흐름으로 연결하여
대용량 처리 환경에서도 안정적으로 동작하는 시스템을 설계하는 데 초점을 맞추었습니다.

> 관리자는 웹 대시보드를 통해 정산 현황, 배치 진행 상태, Kafka 전송 상태, 메시지 생성 및 발송 상태를 확인할 수 있으며,
> 템플릿 관리, 사용자 정보 조회, Audit 로그 확인 등을 통해 전체 운영 흐름을 추적할 수 있습니다.

---

## 🌐 배포 정보

| 항목 | 내용 |
| :--- | :--- |
| **플랫폼** | AWS EC2 |
| **현재 상태** | 💤 비용 절감을 위해 현재 배포 중단 상태 |
| **배포 시 접속** | `http://<EC2-IP>:8080` (배포 재개 시 주소 업데이트 예정) |

> 배포를 재개하면 위 주소에서 관리자 대시보드를 직접 확인할 수 있습니다.
> 로컬 환경에서는 아래 **시작하기** 섹션을 참고해 동일한 환경을 구성할 수 있습니다.

---

## 🚀 시작하기

### 사전 요구사항

- Java 17+
- Docker & Docker Compose
- MySQL 8.0 (외부 연결, 포트 `13306` 사용)

> **주의:** `docker-compose.yml`의 MySQL 서비스는 주석 처리되어 있습니다.
> 로컬 MySQL을 **13306 포트**로 직접 실행하거나, `docker-compose.yml`에서 MySQL 주석을 해제한 뒤 사용하세요.

### 실행 순서

**1. 환경 변수 설정**

```bash
cp .env.example .env
# .env에 DB_USER, DB_PASSWORD, CRYPTO_AES_SECRET_KEY, CRYPTO_AES_IV 등을 채워넣으세요
```

**2. 인프라 실행 (Redis · Zookeeper · Kafka)**

```bash
docker-compose up -d redis zookeeper kafka
```

**3. 애플리케이션 빌드 및 실행**

```bash
./gradlew build
docker-compose up -d billing-api billing-batch billing-message
```

### 서비스 포트

| 서비스 | 포트 | 설명 |
| :--- | :---: | :--- |
| `billing-api` | `8080` | 관리자 API 및 대시보드 |
| `billing-batch` | `8081` | 정산 배치 서버 |
| `billing-message` | `8082` | Kafka 메시지 처리 서버 |
| `kafka` | `9092` | 메시지 브로커 (외부 접속) |
| `kafka` | `29092` | 메시지 브로커 (컨테이너 내부) |
| `redis` | `6379` | 캐시 |

---

## 🔐 환경 변수

`.env.example`을 복사해 `.env`를 생성한 후 아래 항목을 채워넣으세요.

| 변수명 | 설명 |
| :--- | :--- |
| `DB_USER` | MySQL 접속 계정 |
| `DB_PASSWORD` | MySQL 접속 비밀번호 |
| `CRYPTO_AES_SECRET_KEY` | 사용자 민감정보 AES-256 암호화 키 (32바이트) |
| `CRYPTO_AES_IV` | AES 암호화 초기화 벡터 IV (16바이트) |
| `KAFKA_USERNAME` | Kafka 인증 계정 |
| `KAFKA_PASSWORD` | Kafka 인증 비밀번호 |

---

## 🏗️ 멀티 모듈 빌드

이 프로젝트는 Gradle 멀티 모듈 구조로 이루어져 있습니다.

```bash
# 전체 빌드
./gradlew build

# 모듈별 개별 빌드
./gradlew :billing_api:build
./gradlew :billing_batch:build
./gradlew :billing_message:build
```

---

## 🛠 기술 스택

### Backend

| 기술 | 버전 | 용도 |
| :--- | :---: | :--- |
| Java | 17 | 메인 언어 |
| Spring Boot | 3.x | 애플리케이션 프레임워크 |
| Spring Batch | 5.x | 대용량 정산 배치 처리 |
| Gradle | - | 멀티 모듈 빌드 도구 |

### Infrastructure & Messaging

| 기술 | 버전 | 용도 |
| :--- | :---: | :--- |
| MySQL | 8.0 | 정산·메시지 데이터 저장 |
| Redis | 7 | 캐시 및 세션 관리 |
| Apache Kafka | 7.5.0 (Confluent) | 비동기 메시지 브로커 |
| Docker | - | 컨테이너화 및 환경 통일 |

### DevOps

| 기술 | 용도 |
| :--- | :--- |
| AWS EC2 | 프로덕션 배포 서버 |
| Git / GitHub | 버전 관리 및 협업 |

---

## ✨ 주요 기능

### 1. 대용량 정산 배치 처리

- 대규모 사용자 데이터를 대상으로 정산 배치 실행
- 정산 대상 월 기준으로 배치 작업 수행 및 결과 생성
- 정산 결과 저장 후 다음 단계로 연계 가능한 처리 흐름 구성
- Chunk 기반 처리, 단계별 상태 관리 등 대용량 환경을 고려한 구조 반영

### 2. Kafka 기반 메시지 처리

- 정산 결과를 기반으로 Kafka 전송 단계 수행
- 메시지 생성과 발송 단계를 분리하여 비동기 처리 흐름 구성
- 메시지 처리 상태를 단계별로 추적 가능
- 중복 처리 상황을 고려한 안정적인 메시지 발송 구조 설계

### 3. 관리자 대시보드

- 정산 월, 전체 대상 데이터 건수, 전체 진행 현황 확인
- Batch Job 진행 상태 모니터링
- Kafka 전송 상태, 메시지 생성 상태, 메시지 발송 상태 확인
- 운영자가 전체 처리 흐름을 한눈에 파악할 수 있도록 구성

### 4. Audit 로그 관리

- 주요 작업 이력 및 운영 로그 조회
- 장애 상황 발생 시 원인 추적을 위한 기반 제공
- 에러 로그 상세 확인 및 `.txt` 파일 다운로드 지원

### 5. 사용자 및 템플릿 관리

- 정산 및 발송 대상 사용자 정보 조회
- 메시지 템플릿 생성, 수정, 삭제 기능 제공
- 템플릿 기반 메시지 관리로 운영 효율성 향상

---

## 📂 프로젝트 구조

```bash
ToUPlus/
├── billing_api/        # 관리자 요청 처리 및 조회 API       → :8080
├── billing_batch/      # 대용량 정산 배치 서버              → :8081
├── billing_message/    # Kafka 기반 메시지 생성 및 발송 서버 → :8082
├── init/               # DB 초기화 스크립트
├── logs/billing_audit/ # Audit 로그 파일
├── .env.example        # 환경 변수 템플릿
├── docker-compose.yml  # 전체 서비스 컨테이너 구성
└── README.md
```

---

## 📊 아키텍처 및 워크플로우

<details>
<summary><b>🔵 전체 플로우차트 보기</b></summary>

<br/>
<img width="100%" src="https://github.com/user-attachments/assets/abc79424-3a09-4c4f-9102-95bcdab73a24" />

</details>

<details>
<summary><b>🟢 Batch 서버 워크플로우 보기</b></summary>

<br/>
<img width="100%" src="https://github.com/user-attachments/assets/eccbc497-6822-42de-b4c9-c3a25de4d469" />

</details>

<details>
<summary><b>🟡 Message 서버 워크플로우 보기</b></summary>

<br/>
<img width="100%" src="https://github.com/user-attachments/assets/686d9aa5-a6bd-40d4-b605-06f3d57c944f" />

</details>

<details>
<summary><b>🔴 전체 서버 구조 보기</b></summary>

<br/>
<img width="100%" src="https://github.com/user-attachments/assets/d69788d7-6d25-4c46-8195-c52c32412d90" />

</details>

---

## 🗄️ ERD

<details>
<summary><b>ERD (Entity Relationship Diagram) 보기</b></summary>

<br/>
<img width="100%" src="https://github.com/user-attachments/assets/57e06716-58f8-42f7-9b75-85fdf0891bf0" alt="ERD Diagram" />
<img width="100%" src="https://github.com/user-attachments/assets/a8b64e00-5fa4-4425-b3c1-9c55d1ba2e93" alt="ERD Diagram" />

</details>

---

## 🔄 핵심 처리 흐름

```
1. 정산 대상 월 확인 및 전체 대상 데이터 집계
        ↓
2. Spring Batch Job 실행 → Chunk 기반 정산 처리
        ↓
3. 정산 결과 DB 저장 → Kafka 토픽 전송
        ↓
4. billing-message 서버 Kafka 컨슈머 수신 → 메시지 생성
        ↓
5. 메시지 발송 단계 처리 (발송 상태 추적)
        ↓
6. 운영자: 대시보드 + Audit 로그로 전체 이력 확인
        ↓
7. 필요 시 템플릿 수정 및 재발송
```

---

## 🧩 설계 포인트

### 대용량 배치 처리 중심 설계

- 대량 데이터를 한 번에 처리해야 하는 정산 시나리오를 전제로 구성
- 배치 실행 상태와 처리 진행률을 운영 관점에서 확인할 수 있도록 설계
- 단순 조회 시스템이 아니라 실제 대용량 처리 흐름을 관리하는 데 목적을 둠

### 비동기 메시지 발송 흐름 분리

- 정산과 메시지 발송을 하나의 동기 처리로 묶지 않고 단계적으로 분리
- Kafka를 통해 정산 이후 메시지 처리 단계를 유연하게 연결
- 메시지 생성과 발송 단계를 나누어 운영 상태 추적이 가능하도록 구성

### 운영 가시성 확보

- 정산, Kafka 전송, 메시지 생성, 메시지 발송까지의 흐름을 시각적으로 확인 가능
- Audit 로그와 상태 대시보드를 통해 운영 이력과 현재 상태를 함께 관리
- 장애 분석과 운영 추적을 고려한 구조 반영

### 템플릿 기반 운영 효율화

- 반복적으로 사용하는 메시지를 템플릿 형태로 관리
- 메시지 일관성을 유지하고 운영자의 수작업 부담을 줄일 수 있도록 구성

---

## 🐛 트러블슈팅

개발 과정에서 Docker 환경 구성 시 발생한 주요 문제와 해결 방법을 기록합니다.

---

<details>
<summary><b>1. 로컬 MySQL과 Docker 포트 충돌</b></summary>

**문제**

로컬에 이미 MySQL이 `3306` 포트로 실행 중인 상태에서 Docker MySQL 컨테이너를 같은 포트로 올리면 바인딩 충돌이 발생합니다.

```
Error: Bind for 0.0.0.0:3306 failed: port is already allocated
```

**원인**

`docker-compose.yml`의 MySQL 포트가 `3306:3306`으로 설정되어 로컬 MySQL과 충돌.

**해결**

Docker MySQL의 외부 포트를 `3307`로 변경하거나, 본 프로젝트처럼 Docker MySQL을 주석 처리하고 로컬 MySQL을 `13306` 포트로 직접 연결합니다.

```yaml
# 변경 전
ports:
  - "3306:3306"

# 변경 후
ports:
  - "3307:3306"
```

</details>

---

<details>
<summary><b>2. Kafka 내부/외부 리스너 포트 충돌</b></summary>

**문제**

Kafka 컨테이너 기동 시 내부 통신용 리스너와 외부(호스트) 접속용 리스너가 모두 `9092` 포트를 사용해 충돌이 발생했습니다.

```
ERROR: Duplicate listeners found: PLAINTEXT:9092
```

**원인**

`KAFKA_ADVERTISED_LISTENERS`에 `PLAINTEXT://kafka:9092`와 `PLAINTEXT_HOST://localhost:9092`가 같은 포트를 공유.

**해결**

내부 통신 포트를 `29092`로 분리하고, 외부 접속은 `9092`로 유지합니다.
애플리케이션의 `SPRING_KAFKA_BOOTSTRAP_SERVERS`도 컨테이너 내부 포트인 `29092`로 변경합니다.

```yaml
# 변경 전
KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:9092,PLAINTEXT_HOST://localhost:9092

# 변경 후
KAFKA_LISTENERS: PLAINTEXT://0.0.0.0:29092,PLAINTEXT_HOST://0.0.0.0:9092
KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:29092,PLAINTEXT_HOST://localhost:9092
```

```yaml
# billing-batch, billing-message 환경변수 수정
SPRING_KAFKA_BOOTSTRAP_SERVERS: kafka:29092   # 변경 전: kafka:9092
```

</details>

---

<details>
<summary><b>3. Docker 빌드 컨텍스트에 MySQL 소켓 파일 포함으로 인한 빌드 오류</b></summary>

**문제**

`docker build` 실행 시 MySQL 데이터 디렉토리(`data/mysql/mysql.sock`) 등 불필요한 파일이 빌드 컨텍스트에 포함되어 오류가 발생했습니다.

```
error checking context: can't stat '/path/data/mysql/mysql.sock'
```

**원인**

`.dockerignore` 파일이 없어 프로젝트 루트의 모든 파일이 빌드 컨텍스트에 포함됨.

**해결**

`.dockerignore` 파일을 신규 생성해 빌드에 불필요한 경로를 제외합니다.

```
data/
.git/
.idea/
*.log
build/
.gradle/
```

</details>

---

<details>
<summary><b>4. 앱 컨테이너가 DB/Kafka 준비 전에 먼저 기동되는 문제</b></summary>

**문제**

`docker-compose up` 실행 시 MySQL과 Kafka가 완전히 준비되기 전에 애플리케이션 컨테이너가 먼저 기동되어 DB 연결 실패 또는 Kafka 연결 오류가 발생했습니다.

```
com.mysql.cj.jdbc.exceptions.CommunicationsException: Communications link failure
org.apache.kafka.common.errors.TimeoutException: Failed to update metadata
```

**원인**

`depends_on`이 컨테이너 **시작** 여부만 확인하고 서비스 **준비 완료(healthy)** 여부는 확인하지 않음.

**해결**

MySQL에 `healthcheck`를 추가하고, 각 애플리케이션의 `depends_on`에 `condition: service_healthy`를 설정합니다.

```yaml
mysql:
  healthcheck:
    test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
    interval: 10s
    timeout: 5s
    retries: 5

billing-api:
  depends_on:
    mysql:
      condition: service_healthy
    redis:
      condition: service_started

billing-batch:
  depends_on:
    mysql:
      condition: service_healthy
    kafka:
      condition: service_healthy

billing-message:
  depends_on:
    mysql:
      condition: service_healthy
    kafka:
      condition: service_healthy
    redis:
      condition: service_started
```

</details>

---

<details>
<summary><b>5. Docker 환경에서 Spring 설정 파일 미적용 문제</b></summary>

**문제**

로컬에서는 정상 동작하던 DB URL, Kafka 주소 등이 Docker 컨테이너 환경에서는 적용되지 않아 연결 실패가 발생했습니다.

**원인**

Docker 환경용 `application-docker.yml` 설정이 있음에도 `SPRING_PROFILES_ACTIVE`가 설정되지 않아 기본 `application.yml`이 적용됨.

**해결**

`docker-compose.yml`의 각 애플리케이션 환경변수에 `SPRING_PROFILES_ACTIVE: docker`를 명시합니다.

```yaml
environment:
  SPRING_PROFILES_ACTIVE: docker
```

**적용 대상:** `billing-api`, `billing-batch`, `billing-message`

</details>

---

## 🎯 기대 효과

| 효과 | 설명 |
| :--- | :--- |
| **운영 효율성 향상** | 정산부터 메시지 발송까지의 흐름을 하나의 관리자 시스템에서 통합 관리 |
| **대용량 처리 가시성 확보** | 각 단계의 진행 상황을 실시간으로 확인하여 현재 처리 상태를 빠르게 파악 |
| **운영 안정성 강화** | 상태 모니터링과 Audit 로그를 통해 장애 대응과 운영 추적이 용이 |
| **반복 업무 자동화 기반 마련** | 템플릿 관리와 메시지 처리 흐름을 통해 반복적인 운영 업무를 절감 |

---

## 📈 향후 개선 방향

- [ ] 발송 채널 확장 (SMS / EMAIL / PUSH 등)
- [ ] 사용자별 발송 정책 설정 기능 추가
- [ ] 통계 및 리포트 기능 고도화
- [ ] 장애 알림 및 운영 모니터링 기능 강화 (Slack 연동 등)

---

## 🎥 Demo

GIF를 통해 프로젝트의 실제 동작 과정 및 화면 구성을 확인할 수 있습니다.

<table>
  <tr>
    <td align="center" width="50%">
      <img width="100%" src="https://github.com/user-attachments/assets/e32ce8ea-1b49-4c5c-99ec-759cbf9cfee7" alt="메인 대시보드" />
      <br/><br/>
      <b>메인 대시보드</b><br/>
      <sub>전체 진행사항을 요약해서 전달합니다.</sub>
    </td>
    <td align="center" width="50%">
      <img width="100%" src="https://github.com/user-attachments/assets/70450ae5-a2d8-46cf-8cf4-ab35250fd564" alt="배치 대시보드" />
      <br/><br/>
      <b>배치 대시보드</b><br/>
      <sub>정산 배치의 진행 상태 및 결과를 모니터링합니다.</sub>
    </td>
  </tr>
  <tr>
    <td align="center" width="50%">
      <img width="100%" src="https://github.com/user-attachments/assets/3b33ce2d-facf-48c0-928d-86b061fefc8a" alt="메시지 대시보드" />
      <br/><br/>
      <b>메시지 대시보드</b><br/>
      <sub>Kafka 기반 배치 서버에서 전송된 내역의 수신 및 발송 상태를 실시간으로 확인합니다.</sub>
    </td>
    <td align="center" width="50%">
      <img width="100%" src="https://github.com/user-attachments/assets/a770ee01-2e64-4d1a-b650-296c6bdefe3d" alt="관리자 로그 관리" />
      <br/><br/>
      <b>관리자 로그 관리</b><br/>
      <sub>시스템 내 주요 작업 이력 및 Audit 로그를 조회합니다. 에러 로그를 상세 확인하거나 .txt 파일로 다운로드할 수 있습니다.</sub>
    </td>
  </tr>
  <tr>
    <td align="center" width="50%">
      <img width="100%" src="https://github.com/user-attachments/assets/df9e7d35-8e77-44ea-8762-d9b08f585a8d" alt="메시지 템플릿 관리" />
      <br/><br/>
      <b>메시지 템플릿 관리</b><br/>
      <sub>발송할 메시지의 템플릿을 생성, 수정, 삭제 및 관리합니다.</sub>
    </td>
    <td align="center" width="50%">
      <img width="100%" src="https://github.com/user-attachments/assets/19fea02a-7ffd-4a90-be27-f72269a698c0" alt="사용자 대시보드" />
      <br/><br/>
      <b>사용자 대시보드</b><br/>
      <sub>사용자별 정산 및 메시지 발송 현황을 조회합니다. 사용자가 자주 쓰는 요금제 등 데이터를 그래프로 확인할 수 있습니다.</sub>
    </td>
  </tr>
</table>

---

<div align="center">
  <sub>Developed & Maintained by To U+ Team</sub>
</div>
