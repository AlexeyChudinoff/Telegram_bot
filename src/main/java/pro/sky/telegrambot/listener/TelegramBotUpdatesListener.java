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

  private Keyboard createMainKeyboard() {
    return new ReplyKeyboardMarkup(
        new String[]{"⏰ Напоминание"},
        new String[]{"💵 Курс доллара"},
        new String[]{"🌤️ Погода в Томске"}
  }

  @Override
  public int process(List<Update> updates) {
    updates.forEach(update -> {

      if (update.message() != null && update.message().text() != null) {
        Long chatId = update.message().chat().id();


                .replyMarkup(createMainKeyboard());

    telegramBot.execute(message);

    if (notificationTaskService.parseAndSaveTask(chatId, text)) {
    } else {
          "dd.MM.yyyy HH:mm Текст\n\n" +
    }
  }
}