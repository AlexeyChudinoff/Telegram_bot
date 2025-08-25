package pro.sky.telegrambot;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class TelegramBotApplicationTest {

  @Test
  void contextLoads() {
    // Test checks only context loading without web server
  }
}