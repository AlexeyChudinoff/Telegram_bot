package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.Keyboard;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.service.NotificationTaskService;
import pro.sky.telegrambot.service.CurrencyService;
import pro.sky.telegrambot.service.WeatherService;

import javax.annotation.PostConstruct;

import java.util.List;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

  private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);
  private final TelegramBot telegramBot;
  private final NotificationTaskService notificationTaskService;
  private final CurrencyService currencyService;
  private final WeatherService weatherService;

  public TelegramBotUpdatesListener(TelegramBot telegramBot,
      NotificationTaskService notificationTaskService,
      CurrencyService currencyService,
      WeatherService weatherService) {
    this.telegramBot = telegramBot;
    this.notificationTaskService = notificationTaskService;
    this.currencyService = currencyService;
    this.weatherService = weatherService;
    // Инициализация в конструкторе
    this.telegramBot.setUpdatesListener(this);
    logger.info("Telegram bot initialized with token: {}",
        telegramBot.getToken() != null ? "SET" : "NOT SET");
  }
// перенес в конструктор
//  @PostConstruct
//  public void init() {
//    telegramBot.setUpdatesListener(this);
//    logger.info("Telegram bot initialized with token: {}",
//        telegramBot.getToken() != null ? "SET" : "NOT SET");
//  }

  private Keyboard createMainKeyboard() {
    return new ReplyKeyboardMarkup(
        new String[]{"⏰ Напоминание"},
        new String[]{"💵 Курс доллара"},
        new String[]{"🌤️ Погода в Томске"}
    ).resizeKeyboard(true).oneTimeKeyboard(false).selective(true);
  }

  @Override
  public int process(List<Update> updates) {
    updates.forEach(update -> {
      logger.info("Процесс запущен: {}", update);

      if (update.message() != null && update.message().text() != null) {
        Long chatId = update.message().chat().id();
        String text = update.message().text().trim();

        if (text.isEmpty()) {
          logger.debug("Skipping empty message from chatId: {}", chatId);
          return;
        }

        switch (text.toLowerCase()) {
          case "/start":
            SendMessage welcomeMessage = new SendMessage(chatId,
                "Привет! Я умный бот. Выбери что тебя интересует: " +
                    "установить напоминание, узнать курс доллара или погоду в Томске.")
                .replyMarkup(createMainKeyboard());
            telegramBot.execute(welcomeMessage);
            break;
          case "⏰ напоминание":
            sendMessage(chatId, "Отправь мне напоминание в формате:\n"
                + "dd.MM.yyyy HH:mm Текст напоминания\n\n"
                + "Пример: 25.12.2025 15:30 Поздравить маму с днем рождения");
            break;
          case "💵 курс доллара":
            sendMessage(chatId, currencyService.getUsdRate());
            break;
          case "🌤️ погода в томске":
            sendMessage(chatId, weatherService.getTomskWeather());
            break;
          default:
            handleDefaultMessage(chatId, text);
        }
      }
    });
    return UpdatesListener.CONFIRMED_UPDATES_ALL;
  }

  private void sendMessage(Long chatId, String text) {
    SendMessage message = new SendMessage(chatId, text)
        .replyMarkup(createMainKeyboard()); // Добавляем клавиатуру ко всем сообщениям
    telegramBot.execute(message);
  }

  private void handleDefaultMessage(Long chatId, String text) {
    if (notificationTaskService.parseAndSaveTask(chatId, text)) {
      sendMessage(chatId, "✅ Напоминание успешно запланировано!");
    } else {
      sendMessage(chatId, "❌ Неверный формат или дата. Используй:\n" +
          "dd.MM.yyyy HH:mm Текст\n\n" +
          "Пример: 25.12.2025 15:30 Поздравить маму\n\n" +
          "Убедись что дата корректна и в будущем!");
    }
  }
}