package pro.sky.telegrambot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import pro.sky.telegrambot.listener.TelegramBotUpdatesListener;
import pro.sky.telegrambot.service.CurrencyService;
import pro.sky.telegrambot.service.NotificationTaskService;
import pro.sky.telegrambot.service.ReminderParserService;
import pro.sky.telegrambot.service.WeatherService;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class TelegramBotUpdatesListenerTest {

  @Mock
  private TelegramBot telegramBot;

  @Mock
  private NotificationTaskService notificationTaskService;

  @Mock
  private CurrencyService currencyService;

  @Mock
  private WeatherService weatherService;

  @Mock
  private ReminderParserService reminderParserService;

  @InjectMocks
  private TelegramBotUpdatesListener listener;

  private Update createUpdate(Long chatId, String text) {
    Update update = mock(Update.class);
    Message message = mock(Message.class);
    Chat chat = mock(Chat.class);

    when(update.message()).thenReturn(message);
    when(message.chat()).thenReturn(chat);
    when(chat.id()).thenReturn(chatId);
    when(message.text()).thenReturn(text);

    return update;
  }

  @Test
  void process_StartCommand_ShouldSendWelcomeMessage() {
    // Arrange
    Update update = createUpdate(123L, "/start");
    ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);

    // Act
    listener.process(List.of(update));

    // Assert
    verify(telegramBot).execute(captor.capture());
    SendMessage actualMessage = captor.getValue();

    assertEquals(123L, actualMessage.getParameters().get("chat_id"));
    String responseText = actualMessage.getParameters().get("text").toString();

    // ДЕБАГ: выведем реальный текст
    System.out.println("=== REAL RESPONSE TEXT ===");
    System.out.println(responseText);
    System.out.println("==========================");

    // Проверяем более общие фразы
    assertTrue(responseText.contains("Привет"), "Должно содержать 'Привет'");
    assertTrue(responseText.contains("бот"), "Должно содержать 'бот'");
    assertTrue(responseText.contains("помощник"), "Должно содержать 'помощник'");
  }

  @Test
  void process_ReminderButton_ShouldSendInstructions() {
    // Arrange - обновленный текст кнопки
    Update update = createUpdate(123L, "⏰ Создать напоминание");
    ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);

    // Act
    listener.process(List.of(update));

    // Assert
    verify(telegramBot).execute(captor.capture());
    SendMessage actualMessage = captor.getValue();

    assertEquals(123L, actualMessage.getParameters().get("chat_id"));
    String responseText = actualMessage.getParameters().get("text").toString();
    assertTrue(responseText.contains("dd.MM.yyyy HH:mm"));
    assertTrue(responseText.contains("Примеры:"));
  }

  @Test
  void process_CurrencyButton_ShouldCallCurrencyService() {
    // Arrange
    Update update = createUpdate(123L, "💵 Курс доллара");
    when(currencyService.getUsdRate()).thenReturn("Курс доллара: 75.50 руб.");
    ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);

    // Act
    listener.process(List.of(update));

    // Assert
    verify(currencyService).getUsdRate();
    verify(telegramBot).execute(captor.capture());

    SendMessage actualMessage = captor.getValue();
    assertEquals("Курс доллара: 75.50 руб.", actualMessage.getParameters().get("text"));
  }

  @Test
  void process_WeatherButton_ShouldCallWeatherService() {
    // Arrange
    Update update = createUpdate(123L, "🌤️ Погода в Томске");
    when(weatherService.getTomskWeather()).thenReturn("Погода в Томске: +20°C");
    ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);

    // Act
    listener.process(List.of(update));

    // Assert
    verify(weatherService).getTomskWeather();
    verify(telegramBot).execute(captor.capture());

    SendMessage actualMessage = captor.getValue();
    assertEquals("Погода в Томске: +20°C", actualMessage.getParameters().get("text"));
  }

  @Test
  void process_HelpButton_ShouldSendHelpMessage() {
    // Arrange - новая кнопка помощи
    Update update = createUpdate(123L, "❓ Помощь");
    ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);

    // Act
    listener.process(List.of(update));

    // Assert
    verify(telegramBot).execute(captor.capture());
    SendMessage actualMessage = captor.getValue();

    String responseText = actualMessage.getParameters().get("text").toString();
    assertTrue(responseText.contains("Помощь по использованию бота"));
    assertTrue(responseText.contains("Создание напоминаний"));
    assertTrue(responseText.contains("Команды:"));
  }

  @Test
  void process_ValidReminder_ShouldSaveAndConfirm() {
    // Arrange
    Update update = createUpdate(123L, "25.12.2030 15:30 Поздравить маму");

    // Мокаем ReminderParserService
    when(reminderParserService.isValidReminderFormat("25.12.2030 15:30 Поздравить маму"))
        .thenReturn(true);

    ReminderParserService.ParsedReminder parsedReminder =
        new ReminderParserService.ParsedReminder(
            LocalDateTime.of(2030, 12, 25, 15, 30),
            "Поздравить маму",
            true
        );

    when(reminderParserService.parseReminder("25.12.2030 15:30 Поздравить маму"))
        .thenReturn(parsedReminder);

    when(reminderParserService.isFutureDateTime(any(LocalDateTime.class)))
        .thenReturn(true);

    ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);

    // Act
    listener.process(List.of(update));

    // Assert
    verify(notificationTaskService).saveNotificationTask(123L, "Поздравить маму",
        LocalDateTime.of(2030, 12, 25, 15, 30));
    verify(telegramBot).execute(captor.capture());

    SendMessage actualMessage = captor.getValue();
    String responseText = actualMessage.getParameters().get("text").toString();
    assertTrue(responseText.contains("✅ Напоминание успешно создано"));
  }

  @Test
  void process_InvalidReminderFormat_ShouldSendUnknownCommand() {
    // Arrange
    Update update = createUpdate(123L, "неправильный формат");

    when(reminderParserService.isValidReminderFormat("неправильный формат"))
        .thenReturn(false);

    ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);

    // Act
    listener.process(List.of(update));

    // Assert
    verify(notificationTaskService, never()).saveNotificationTask(any(), any(), any());
    verify(telegramBot).execute(captor.capture());

    SendMessage actualMessage = captor.getValue();
    String responseText = actualMessage.getParameters().get("text").toString();
    assertTrue(responseText.contains("Я не понял вашу команду"));
  }

  @Test
  void process_ValidFormatButPastDate_ShouldSendError() {
    // Arrange
    Update update = createUpdate(123L, "01.01.2020 15:30 Поздравить маму");

    when(reminderParserService.isValidReminderFormat("01.01.2020 15:30 Поздравить маму"))
        .thenReturn(true);

    ReminderParserService.ParsedReminder parsedReminder =
        new ReminderParserService.ParsedReminder(
            LocalDateTime.of(2020, 1, 1, 15, 30),
            "Поздравить маму",
            true
        );

    when(reminderParserService.parseReminder("01.01.2020 15:30 Поздравить маму"))
        .thenReturn(parsedReminder);

    when(reminderParserService.isFutureDateTime(any(LocalDateTime.class)))
        .thenReturn(false);

    ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);

    // Act
    listener.process(List.of(update));

    // Assert
    verify(notificationTaskService, never()).saveNotificationTask(any(), any(), any());
    verify(telegramBot).execute(captor.capture());

    SendMessage actualMessage = captor.getValue();
    String responseText = actualMessage.getParameters().get("text").toString();
    assertTrue(responseText.contains("❌ Не удалось создать напоминание"));
  }

  @Test
  void process_ParsedReminderInvalid_ShouldSendError() {
    // Arrange
    Update update = createUpdate(123L, "25.12.2030 15:30 Поздравить маму");

    when(reminderParserService.isValidReminderFormat("25.12.2030 15:30 Поздравить маму"))
        .thenReturn(true);

    ReminderParserService.ParsedReminder parsedReminder =
        new ReminderParserService.ParsedReminder(null, null, false);

    when(reminderParserService.parseReminder("25.12.2030 15:30 Поздравить маму"))
        .thenReturn(parsedReminder);

    ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);

    // Act
    listener.process(List.of(update));

    // Assert
    verify(notificationTaskService, never()).saveNotificationTask(any(), any(), any());
    verify(telegramBot).execute(captor.capture());

    SendMessage actualMessage = captor.getValue();
    String responseText = actualMessage.getParameters().get("text").toString();
    assertTrue(responseText.contains("❌ Не удалось создать напоминание"));
  }

  @Test
  void process_EmptyMessage_ShouldNotProcess() {
    // Arrange
    Update update = createUpdate(123L, "   ");

    // Act
    int result = listener.process(List.of(update));

    // Assert
    assertEquals(-1, result);
    verify(notificationTaskService, never()).saveNotificationTask(any(), any(), any());
  }

  @Test
  void process_MultipleUpdates_ShouldProcessAll() {
    // Arrange
    Update update1 = createUpdate(123L, "/start");
    Update update2 = createUpdate(456L, "💵 Курс доллара");
    when(currencyService.getUsdRate()).thenReturn("Курс доллара: 75.50 руб.");

    // Act
    listener.process(List.of(update1, update2));

    // Assert
    verify(telegramBot, times(2)).execute(any(SendMessage.class));
    verify(currencyService).getUsdRate();
  }

}