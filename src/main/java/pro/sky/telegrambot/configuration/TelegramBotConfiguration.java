package pro.sky.telegrambot.configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.DeleteMyCommands;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TelegramBotConfiguration {

  @Value("${telegram.bot.token}")
  private String token;

  @Bean
  public TelegramBot telegramBot() {
    TelegramBot bot = new TelegramBot(token);
    bot.execute(new DeleteMyCommands()); // Очистка старых команд
    return bot;
  }

  @Bean
  public ObjectMapper objectMapper() {
    ObjectMapper mapper = new ObjectMapper();
    // Отключение проверки на отсутствие полей, API погоды может добавлять новые поля, а приложение продолжит работать
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    // Включение записи дат как строк вместо timestamp
    mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    // Регистрация модуля для работы с Java 8 Date/Time API
    mapper.registerModule(new JavaTimeModule());
    return mapper;
  }
}