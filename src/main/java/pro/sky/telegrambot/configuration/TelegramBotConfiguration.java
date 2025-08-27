package pro.sky.telegrambot.configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.DeleteMyCommands;
import java.text.SimpleDateFormat;
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
    // Установка формата даты и времени, чтобы не было ошибок при парсинге даты
    mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
    return mapper;
  }

}
