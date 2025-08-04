# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

**Important**: This file must be kept in sync with CLAUDE-KO.md (Korean translation). When updating this file, always update the Korean version as well.

## Project Overview

Bookitty is a Spring Boot-based book recommendation service that uses cosine similarity algorithms to provide
personalized book recommendations. The application features a dual-database architecture with Spring Batch processing
for similarity calculations.

## Build and Development Commands

### Build and Run

```bash
# Build the project
./gradlew build

# Run the application locally
./gradlew bootRun

# Run tests
./gradlew test

# Clean build artifacts
./gradlew clean
```

### Testing

```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests "capstone.bookitty.domain.member.*"

# Run tests with coverage
./gradlew test jacocoTestReport
```

### Docker Operations

```bash
# Build Docker image
docker build -t bookitty .

# Run with Docker Compose (if available)
docker-compose up -d

# Blue-green deployment
./scripts/blue-green-deploy.sh bookitty:latest
```

## Architecture Overview

### Multi-Database Configuration

- **Data DB**: Primary application data (books, users, ratings, comments)
- **Meta DB**: Spring Batch metadata and job execution history
- **Redis**: Caching layer for performance optimization

### Domain Structure

The application follows Domain-Driven Design with these key domains:

- `book`: External API integration (Aladin API) and book data management
- `bookSimilarity`: Cosine similarity calculations and batch processing
- `bookState`: User reading status and statistics tracking
- `comment`: User reviews and social interactions
- `member`: User authentication and profile management
- `star`: Rating system and user preferences

### Batch Processing

Spring Batch is used for:

- Book similarity calculations using cosine similarity algorithm
- Scheduled batch jobs for recommendation vector updates
- Separate from real-time API operations for performance isolation

### Key Technologies

- Spring Boot 3.2.5 with Java 17
- Spring Security + JWT authentication
- Spring Batch for background processing
- Spring Data JPA + QueryDSL for data access
- Redis for caching and performance optimization
- MySQL databases (dual configuration)
- Docker + Blue-Green deployment strategy

## Configuration Profiles

### Environment Profiles

- `local`: Development environment with local MySQL/Redis
- `dev`: Development server configuration
- `prod`: Production environment with external services

### Database Configuration

Two separate datasources are configured:

- Data DB: Main application entities
- Meta DB: Spring Batch metadata

### External Integrations

- Aladin Open API for book data retrieval
- Slack webhook for error notifications and monitoring
- Prometheus/Grafana for metrics and monitoring

## Development Notes

### Authentication

- JWT-based authentication with access/refresh token pattern
- Custom UserDetails implementation
- Security configuration supports API-first approach

### Batch Jobs

- Book similarity calculations run on scheduled basis
- Separate transaction managers for batch vs application data
- Custom step listeners for monitoring batch performance

### Monitoring and Observability

- Spring Boot Actuator endpoints exposed
- Prometheus metrics collection
- Custom health indicators for Redis
- AOP-based logging for controllers and services
- Slack notifications for critical errors

### Testing Structure

#### Testing Philosophy
- **Integration Test Centric**: This project strongly favors integration tests over unit tests
- **Real Dependencies Preferred**: Use actual Spring beans, real databases, and real services rather than mocks
- **Mock Minimization**: Mock usage is avoided whenever possible; mocks are only used when absolutely necessary

#### Test Types and Selection Criteria
- **Integration Tests**: Primary and preferred testing approach using `@SpringBootTest` with actual Spring context
- **Mock-Based Tests**: Exist only for educational purposes and are `@Disabled` in actual execution
- **Selection Rule**: Always use integration tests for service layer testing to ensure real dependencies work together properly

#### Test Environment Configuration
- **Test Profile**: Always use `@ActiveProfiles("test")`
- **Transaction Management**: Use `@Transactional` for automatic rollback and test isolation
- **Database**: Separate test databases (`data_test_db`, `meta_test_db`) with real MySQL connections
- **Schema**: `ddl-auto: create-drop` for clean test isolation
- **Real Bean Injection**: Use `@Autowired` to inject actual Spring beans, not mocks

#### Test Structure and Naming
- **Nested Test Classes**: Use `@Nested` to group related test scenarios
- **Korean DisplayNames**: Use descriptive Korean names for test methods
  ```java
  @DisplayName("로그인 성공 시 JWT 토큰을 발급합니다.")
  ```
- **BDD Structure**: Follow given-when-then pattern consistently

#### Limited Mock Usage
- **SecurityUtil Only**: The only exception where mocking is used due to static nature
  ```java
  try (MockedStatic<SecurityUtil> mocked = mockSecurityUtil(email)) {
      // test execution - only for SecurityUtil
  }
  ```
- **Helper Methods**: Create reusable SecurityUtil mock setup methods
- **Avoid Service Mocking**: Never mock service dependencies; use real injected services

#### Test Data Management
- **TestFixture Pattern**: Use dedicated fixture classes for test data creation
- **Builder Pattern**: Leverage builder pattern for flexible test data setup
- **Default Values**: Provide sensible defaults with ability to override
- **Real Database Operations**: Create and persist actual test data in test databases

#### Assertion Standards
- **AssertJ**: Use AssertJ for fluent assertions
- **Exception Testing**: Use `assertThatThrownBy()` for exception scenarios
- **Coverage**: Test both success and failure scenarios, including edge cases
- **Real Data Verification**: Verify actual database state and real service responses

#### Test Organization
- **File Structure**: Active service tests in `src/test/java/.../application/`
- **Mock Tests**: Disabled mock tests in `MockTest/` subdirectory (educational only)
- **Fixtures**: Shared test fixtures in `fixture/` package