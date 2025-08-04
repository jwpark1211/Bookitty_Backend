# CLAUDE-KO.md

이 파일은 Claude Code (claude.ai/code)가 이 저장소에서 코드 작업을 할 때 가이드를 제공합니다.

**중요**: 이 파일은 CLAUDE.md (영문 원본)와 항상 동기화되어야 합니다. CLAUDE.md 파일이 업데이트되면 이 한국어 번역본도 함께 업데이트해야 합니다.

## 프로젝트 개요

Bookitty는 코사인 유사도 알고리즘을 사용하여 개인화된 도서 추천을 제공하는 Spring Boot 기반 도서 추천 서비스입니다. 이 애플리케이션은 유사도 계산을 위한 Spring Batch 처리와 함께 이중 데이터베이스 아키텍처를 특징으로 합니다.

## 빌드 및 개발 명령어

### 빌드 및 실행
```bash
# 프로젝트 빌드
./gradlew build

# 로컬에서 애플리케이션 실행
./gradlew bootRun

# 테스트 실행
./gradlew test

# 빌드 아티팩트 정리
./gradlew clean
```

### 테스트
```bash
# 모든 테스트 실행
./gradlew test

# 특정 테스트 클래스 실행
./gradlew test --tests "capstone.bookitty.domain.member.*"

# 커버리지 포함 테스트 실행
./gradlew test jacocoTestReport
```

### Docker 운영
```bash
# Docker 이미지 빌드
docker build -t bookitty .

# Docker Compose로 실행 (사용 가능한 경우)
docker-compose up -d

# Blue-green 배포
./scripts/blue-green-deploy.sh bookitty:latest
```

## 아키텍처 개요

### 다중 데이터베이스 구성
- **Data DB**: 주요 애플리케이션 데이터 (도서, 사용자, 평점, 댓글)
- **Meta DB**: Spring Batch 메타데이터 및 작업 실행 기록
- **Redis**: 성능 최적화를 위한 캐싱 레이어

### 도메인 구조
애플리케이션은 다음과 같은 주요 도메인으로 도메인 주도 설계를 따릅니다:
- `book`: 외부 API 통합 (알라딘 API) 및 도서 데이터 관리
- `bookSimilarity`: 코사인 유사도 계산 및 배치 처리
- `bookState`: 사용자 독서 상태 및 통계 추적
- `comment`: 사용자 리뷰 및 소셜 상호작용
- `member`: 사용자 인증 및 프로필 관리
- `star`: 평점 시스템 및 사용자 선호도

### 배치 처리
Spring Batch는 다음 용도로 사용됩니다:
- 코사인 유사도 알고리즘을 사용한 도서 유사도 계산
- 추천 벡터 업데이트를 위한 스케줄된 배치 작업
- 성능 격리를 위해 실시간 API 운영과 분리

### 주요 기술
- Java 17과 Spring Boot 3.2.5
- Spring Security + JWT 인증
- 백그라운드 처리를 위한 Spring Batch
- 데이터 접근을 위한 Spring Data JPA + QueryDSL
- 캐싱 및 성능 최적화를 위한 Redis
- MySQL 데이터베이스 (이중 구성)
- Docker + Blue-Green 배포 전략

## 구성 프로필

### 환경 프로필
- `local`: 로컬 MySQL/Redis를 사용한 개발 환경
- `dev`: 개발 서버 구성
- `prod`: 외부 서비스를 사용한 프로덕션 환경

### 데이터베이스 구성
두 개의 별도 데이터소스가 구성됩니다:
- Data DB: 주요 애플리케이션 엔티티
- Meta DB: Spring Batch 메타데이터

### 외부 통합
- 도서 데이터 검색을 위한 알라딘 오픈 API
- 오류 알림 및 모니터링을 위한 Slack 웹훅
- 메트릭 및 모니터링을 위한 Prometheus/Grafana

## 개발 참고사항

### 인증
- 액세스/리프레시 토큰 패턴을 사용한 JWT 기반 인증
- 사용자 정의 UserDetails 구현
- API 우선 접근 방식을 지원하는 보안 구성

### 배치 작업
- 스케줄 기반으로 실행되는 도서 유사도 계산
- 배치와 애플리케이션 데이터를 위한 별도 트랜잭션 매니저
- 배치 성능 모니터링을 위한 사용자 정의 스텝 리스너

### 모니터링 및 관찰 가능성
- Spring Boot Actuator 엔드포인트 노출
- Prometheus 메트릭 수집
- Redis를 위한 사용자 정의 헬스 인디케이터
- 컨트롤러 및 서비스를 위한 AOP 기반 로깅
- 중요한 오류에 대한 Slack 알림

### 테스트 구조

#### 테스트 철학
- **통합 테스트 중심**: 이 프로젝트는 단위 테스트보다 통합 테스트를 강력히 선호합니다
- **실제 의존성 선호**: Mock보다는 실제 Spring 빈, 실제 데이터베이스, 실제 서비스를 사용합니다
- **Mock 최소화**: Mock 사용을 가능한 한 피하며, 절대적으로 필요한 경우에만 사용합니다

#### 테스트 유형 및 선택 기준
- **통합 테스트**: 실제 Spring 컨텍스트를 사용하는 `@SpringBootTest` 기반의 주요하고 선호되는 테스트 방식
- **Mock 기반 테스트**: 학습 목적으로만 존재하며 실제 실행에서는 `@Disabled` 처리됨
- **선택 규칙**: 서비스 레이어 테스트에서는 항상 통합 테스트를 사용하여 실제 의존성들이 올바르게 함께 작동하는지 확인

#### 테스트 환경 구성
- **테스트 프로필**: 항상 `@ActiveProfiles("test")` 사용
- **트랜잭션 관리**: 자동 롤백과 테스트 격리를 위해 `@Transactional` 사용
- **데이터베이스**: 실제 MySQL 연결을 사용하는 별도 테스트 데이터베이스 (`data_test_db`, `meta_test_db`)
- **스키마**: 깨끗한 테스트 격리를 위해 `ddl-auto: create-drop` 사용
- **실제 빈 주입**: Mock이 아닌 실제 Spring 빈을 주입하기 위해 `@Autowired` 사용

#### 테스트 구조 및 네이밍
- **Nested 테스트 클래스**: 관련된 테스트 시나리오를 그룹화하기 위해 `@Nested` 사용
- **한국어 DisplayName**: 테스트 메서드에 설명적인 한국어 이름 사용
  ```java
  @DisplayName("로그인 성공 시 JWT 토큰을 발급합니다.")
  ```
- **BDD 구조**: given-when-then 패턴을 일관되게 따름

#### 제한적인 Mock 사용
- **SecurityUtil만 예외**: Static 클래스 특성상 Mock을 사용하는 유일한 예외 케이스
  ```java
  try (MockedStatic<SecurityUtil> mocked = mockSecurityUtil(email)) {
      // 테스트 실행 - SecurityUtil에만 적용
  }
  ```
- **헬퍼 메서드**: 재사용 가능한 SecurityUtil Mock 설정 메서드 생성
- **서비스 Mock 금지**: 서비스 의존성은 절대 Mock하지 않고 실제 주입된 서비스 사용

#### 테스트 데이터 관리
- **TestFixture 패턴**: 테스트 데이터 생성을 위한 전용 픽스처 클래스 사용
- **빌더 패턴**: 유연한 테스트 데이터 설정을 위한 빌더 패턴 활용
- **기본값**: 재정의 가능한 합리적인 기본값 제공
- **실제 데이터베이스 연산**: 테스트 데이터베이스에 실제 테스트 데이터를 생성하고 저장

#### Assertion 표준
- **AssertJ**: 유창한 assertion을 위해 AssertJ 사용
- **예외 테스트**: 예외 시나리오에 `assertThatThrownBy()` 사용
- **커버리지**: 성공 및 실패 시나리오, 엣지 케이스 모두 테스트
- **실제 데이터 검증**: 실제 데이터베이스 상태와 실제 서비스 응답을 검증

#### 테스트 구조화
- **파일 구조**: 활성 서비스 테스트는 `src/test/java/.../application/`에 위치
- **Mock 테스트**: 비활성화된 Mock 테스트는 `MockTest/` 하위 디렉토리에 분리 (학습용만)
- **픽스처**: 공유 테스트 픽스처는 `fixture/` 패키지에 위치