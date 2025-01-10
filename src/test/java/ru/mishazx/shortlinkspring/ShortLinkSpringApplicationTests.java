package ru.mishazx.shortlinkspring;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import ru.mishazx.shortlinkspring.config.TestSecurityConfig;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class ShortLinkSpringApplicationTests {

    @Test
    void contextLoads() {
    }

}
