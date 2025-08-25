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
  }

  @PostConstruct
  public void init() {
    telegramBot.setUpdatesListener(this);
  }

  private Keyboard createMainKeyboard() {
    return new ReplyKeyboardMarkup(
        new String[]{"⏰ Напоминание"},
        new String[]{"💵 Курс доллара"},
        new String[]{"🌤️ Погода в Томске"}
    ).resizeKeyboard(true);
  }

  @Override
  public int process(List<Update> updates) {
    updates.forEach(update -> {
      if (update.message() != null && update.message().text() != null) {
        Long chatId = update.message().chat().id();
        String text = update.message().text();

        switch (text) {
          case "⏰ Напоминание":
            sendMessage(chatId, "Введите напоминание в формате: dd.MM.yyyy HH:mm Текст напоминания");
            break;
          case "💵 Курс доллара":
            String currencyRate = currencyService.getUsdRate();
            sendMessage(chatId, currencyRate);
            break;
          case "🌤️ Погода в Томске":
            String weather = weatherService.getTomskWeather();
            sendMessage(chatId, weather);
            break;
          default:
            if (notificationTaskService.parseAndSaveTask(chatId, text)) {
              sendMessage(chatId, "Напоминание успешно сохранено!");
            } else {
              SendMessage message = new SendMessage(chatId, "Добро пожаловать! Выберите действие:")
                  .replyMarkup(createMainKeyboard());
              telegramBot.execute(message);
            }
            break;
        }
      }
    });
    return UpdatesListener.CONFIRMED_UPDATES_ALL;
  }

  private void sendMessage(Long chatId, String text) {
    SendMessage message = new SendMessage(chatId, text)
        .replyMarkup(createMainKeyboard());
    telegramBot.execute(message);
  }
}