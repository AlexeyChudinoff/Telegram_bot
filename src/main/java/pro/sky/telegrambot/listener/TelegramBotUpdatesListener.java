package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.Keyboard;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.List;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.service.CurrencyService;
import pro.sky.telegrambot.service.NotificationTaskService;
import pro.sky.telegrambot.service.ReminderParserService;
import pro.sky.telegrambot.service.WeatherService;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

  private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);
  private final TelegramBot telegramBot;
  private final NotificationTaskService notificationTaskService;
  private final CurrencyService currencyService;
  private final WeatherService weatherService;
  private final ReminderParserService reminderParserService;

  public TelegramBotUpdatesListener(TelegramBot telegramBot,
      NotificationTaskService notificationTaskService,
      CurrencyService currencyService,
      WeatherService weatherService,
      ReminderParserService reminderParserService) {
    this.telegramBot = telegramBot;
    this.notificationTaskService = notificationTaskService;
    this.currencyService = currencyService;
    this.weatherService = weatherService;
    this.reminderParserService = reminderParserService;
  }

  @PostConstruct
  public void init() {
    telegramBot.setUpdatesListener(this);
  }

  private Keyboard createMainKeyboard() {
    return new ReplyKeyboardMarkup(
        new String[]{"⏰ Создать напоминание"},
        new String[]{"💵 Курс доллара", "🌤️ Погода в Томске"},
        new String[]{"❓ Помощь"}
    ).resizeKeyboard(true);
  }

  @Override
  public int process(List<Update> updates) {
    try {
      updates.forEach(update -> {
        if (update.message() != null && update.message().text() != null) {
          Long chatId = update.message().chat().id();
          String text = update.message().text().trim();

          logger.info("Received message from chat {}: {}", chatId, text);

          if (text.isEmpty()) {
            return;
          }

          switch (text) {
            case "/start":
              sendWelcomeMessage(chatId);
              break;
            case "⏰ Создать напоминание":
              sendReminderInstructions(chatId);
              break;
            case "💵 Курс доллара":
              sendCurrencyRate(chatId);
              break;
            case "🌤️ Погода в Томске":
              sendWeather(chatId);
              break;
            case "❓ Помощь":
              sendHelp(chatId);
              break;
            default:
              processReminder(chatId, text);
              break;
          }
        }
      });
      return UpdatesListener.CONFIRMED_UPDATES_ALL;
    } catch (Exception e) {
      logger.error("Error processing updates", e);
      return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }
  }

  private void sendWelcomeMessage(Long chatId) {
    String message = "👋 Привет! Я умный бот-помощник!\n\n" +
        "Я могу помочь тебе:\n" +
        "• ⏰ Создавать напоминания\n" +
        "• 💵 Показывать текущий курс доллара\n" +
        "• 🌤️ Сообщать погоду в Томске\n\n" +
        "Просто выбери нужное действие на клавиатуре или напиши мне напоминание в формате:\n" +
        "`dd.MM.yyyy HH:mm Текст напоминания`\n\n" +
        "Пример: `25.12.2028 15:30 Поздравить маму с праздником`";

    sendMessage(chatId, message);
  }

  private void sendReminderInstructions(Long chatId) {
    String message = "📝 Чтобы создать напоминание, отправь мне сообщение в формате:\n\n" +
        "`dd.MM.yyyy HH:mm Текст напоминания`\n\n" +
        "Примеры:\n" +
        "• `25.12.2028 15:30 Поздравить маму`\n" +
        "• `01.01.2028 00:00 С Новым годом!`\n" +
        "• `15.03.2028 09:00 Сходить к врачу`\n\n" +
        "⚠️ Важно: дата должна быть в будущем!";

    sendMessage(chatId, message);
  }

  private void sendCurrencyRate(Long chatId) {
    try {
      String rate = currencyService.getUsdRate();
      sendMessage(chatId, rate);
    } catch (Exception e) {
      logger.error("Error getting currency rate for chat: {}", chatId, e);
      sendMessage(chatId, "❌ Не удалось получить курс доллара. Попробуйте позже.");
    }
  }

  private void sendWeather(Long chatId) {
    try {
      String weather = weatherService.getTomskWeather();
      sendMessage(chatId, weather);
    } catch (Exception e) {
      logger.error("Error getting weather for chat: {}", chatId, e);
      sendMessage(chatId, "❌ Не удалось получить погоду. Попробуйте позже.");
    }
  }

  private void sendHelp(Long chatId) {
    String message = "❓ **Помощь по использованию бота:**\n\n" +
        "**Создание напоминаний:**\n" +
        "Формат: `dd.MM.yyyy HH:mm Текст`\n" +
        "Пример: `25.12.2028 15:30 Поздравить маму`\n\n" +
        "**Команды:**\n" +
        "• /start - начать работу с ботом\n" +
        "• ⏰ Создать напоминание - инструкция\n" +
        "• 💵 Курс доллара - текущий курс\n" +
        "• 🌤️ Погода в Томске - погода\n" +
        "• ❓ Помощь - это сообщение\n\n" +
        "Если что-то не работает, попробуйте позже или проверьте формат вводимых данных.";

    sendMessage(chatId, message);
  }

  private void processReminder(Long chatId, String text) {
    if (reminderParserService.isValidReminderFormat(text)) {
      ReminderParserService.ParsedReminder parsed = reminderParserService.parseReminder(text);

      if (parsed.isValid() && reminderParserService.isFutureDateTime(parsed.getDateTime())) {
        notificationTaskService.saveNotificationTask(chatId, parsed.getMessage(), parsed.getDateTime());
        sendMessage(chatId, "✅ Напоминание успешно создано!\nЯ напомню вам в указанное время.");
      } else {
        sendReminderError(chatId, text);
      }
    } else {
      sendUnknownCommand(chatId);
    }
  }

  private void sendReminderError(Long chatId, String text) {
    String message = "❌ Не удалось создать напоминание.\n\n" +
        "**Возможные причины:**\n" +
        "• Неверный формат даты/времени\n" +
        "• Дата в прошлом\n" +
        "• Отсутствует текст напоминания\n\n" +
        "Правильный формат: `dd.MM.yyyy HH:mm Текст`\n" +
        "Пример: `25.12.2028 15:30 Ваше напоминание`\n\n" +
        "Исправьте и попробуйте снова!";

    sendMessage(chatId, message);
  }

  private void sendUnknownCommand(Long chatId) {
    String message = "🤔 Я не понял вашу команду.\n\n" +
        "Я могу:\n" +
        "• Создавать напоминания\n" +
        "• Показывать курс доллара\n" +
        "• Сообщать погоду в Томске\n\n" +
        "Используйте кнопки меню или напишите напоминание в формате:\n" +
        "`dd.MM.yyyy HH:mm Текст напоминания`";

    sendMessage(chatId, message);
  }

  private void sendMessage(Long chatId, String text) {
    try {
      SendMessage message = new SendMessage(chatId, text)
          .replyMarkup(createMainKeyboard())
          .disableWebPagePreview(true);
      telegramBot.execute(message);
    } catch (Exception e) {
      logger.error("Error sending message to chat {}", chatId, e);
    }
  }
}