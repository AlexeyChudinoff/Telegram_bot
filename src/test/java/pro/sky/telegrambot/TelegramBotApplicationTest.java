package pro.sky.telegrambot;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@TestPropertySource(properties = "spring.main.allow-bean-definition-overriding=true")
class TelegramBotApplicationTest {

  @Test
  void contextLoads() {
    // Test checks only context loading without web server
    // Если контекст не загружается из-за конфликта бинов,
    // этот тест может пропустить исключение
  }
}