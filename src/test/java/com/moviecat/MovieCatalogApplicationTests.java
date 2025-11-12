package com.moviecat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Basic test to verify Spring Boot application context loads successfully.
 */
@SpringBootTest
@TestPropertySource(properties = {
    "spring.data.mongodb.uri=mongodb://localhost:27017/moviecat-test"
})
class MovieCatalogApplicationTests {

    @Test
    void contextLoads() {
        // This test will pass if the application context loads successfully
    }

}
