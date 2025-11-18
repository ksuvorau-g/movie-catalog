# Test Database Configuration

## Overview
The project now uses **Testcontainers** with MongoDB for integration tests, ensuring complete isolation from production databases. Each test run uses a fresh, isolated MongoDB instance that is automatically started and cleaned up.

## Implementation

### 1. Dependencies Added (pom.xml)
- `org.testcontainers:testcontainers:1.19.3`
- `org.testcontainers:mongodb:1.19.3`
- `org.testcontainers:junit-jupiter:1.19.3`

### 2. Test Configuration (application-test.properties)
```properties
# MongoDB URI dynamically set by Testcontainers
spring.data.mongodb.auto-index-creation=true

# Disable scheduled tasks during tests
scheduler.cron.season-check=-

# Test-specific logging
logging.level.org.testcontainers=INFO

# Test image storage
image.storage.path=target/test-images
```

### 3. Base Test Class (AbstractIntegrationTest.java)
All integration tests extend `AbstractIntegrationTest`, which:
- Starts a reusable MongoDB 7.0 container (shared across tests in same JVM)
- Dynamically configures `spring.data.mongodb.uri` via `@DynamicPropertySource`
- Activates the `test` profile with `@ActiveProfiles("test")`
- Uses `@Testcontainers` annotation for automatic lifecycle management

### 4. Updated Test Classes
- `CatalogControllerIntegrationTest` - extends AbstractIntegrationTest
- `MovieControllerIntegrationTest` - extends AbstractIntegrationTest
- `RecommendationControllerIntegrationTest` - extends AbstractIntegrationTest
- `MovieCatalogApplicationTests` - has its own container setup

## Benefits

1. **Complete Isolation**: Tests never touch production MongoDB
2. **No Manual Setup**: Docker pulls and starts MongoDB automatically
3. **Clean State**: Each test suite gets a fresh database
4. **CI/CD Ready**: Works in any environment with Docker
5. **Fast Execution**: Container reuse optimizes test speed

## Running Tests

### Local Development
```bash
mvn test                    # Run all tests with isolated MongoDB
mvn test -Dtest=ClassName  # Run specific test class
make test                   # Same as mvn test (convenience command)
```

**Note**: Tests run locally using Maven. The `make test` command runs `mvn test` directly on your machine (not in Docker) to avoid Mockito agent attachment issues in containers.

## Requirements
- Docker must be running on your system
- Testcontainers will automatically pull `mongo:7.0` image if not present
- No need to start MongoDB manually

## Troubleshooting

### Docker Not Running
**Error**: `Could not find a valid Docker environment`
**Solution**: Start Docker Desktop or Docker daemon

### Port Conflicts
Testcontainers automatically assigns random ports, avoiding conflicts with local MongoDB instances.

### Slow First Run
First test execution downloads the MongoDB Docker image (~150MB). Subsequent runs are faster as the image is cached.

### Container Cleanup
Containers are automatically removed after test execution. Use `docker ps -a` to verify cleanup.

## Test Execution Flow

1. JUnit detects `@Testcontainers` annotation
2. Testcontainers starts MongoDB container on random port
3. `@DynamicPropertySource` injects connection URL
4. Spring Boot test context loads with test configuration
5. Tests execute against isolated database
6. Container stops and removes itself (or reused for next test class)

## Migration Notes

### Before (Production DB Risk)
```java
@SpringBootTest
@TestPropertySource(properties = {
    "spring.data.mongodb.uri=mongodb://localhost:27017/moviecat-test"
})
```
❌ Tests used local MongoDB, risking data corruption

### After (Isolated Testcontainers)
```java
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
class MyTest extends AbstractIntegrationTest {
    // Tests run in isolated container
}
```
✅ Each test run gets fresh, isolated MongoDB instance

## Future Improvements

1. Add `@DirtiesContext` for tests that modify shared state
2. Implement test data builders for complex domain objects
3. Add performance benchmarks for recommendation algorithm
4. Consider using MongoDB in-memory mode for unit tests

## Related Documentation
- [Testcontainers MongoDB Module](https://www.testcontainers.org/modules/databases/mongodb/)
- [Spring Boot Testing](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
