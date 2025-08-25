package pro.sky.telegrambot;

import com.pengrad.telegrambot.TelegramBot;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import pro.sky.telegrambot.configuration.TelegramBotConfiguration;

import static org.junit.jupiter.api.Assertions.assertNotNull;


@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@TestPropertySource(properties = "telegram.bot.token=test:token")
class TelegramBotConfigurationTest {

  @Value("${telegram.bot.token}")
  private String token;

  @Test
  void testTelegramBotCreation() {
    // Arrange
    TelegramBotConfiguration configuration = new TelegramBotConfiguration();

    // Используем рефлексию для установки значения token
    try {
      var field = TelegramBotConfiguration.class.getDeclaredField("token");
      field.setAccessible(true);
      field.set(configuration, token);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    // Act
    TelegramBot bot = configuration.telegramBot();

    // Assert
    assertNotNull(bot);
  }
}