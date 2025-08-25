package pro.sky.telegrambot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import pro.sky.telegrambot.listener.TelegramBotUpdatesListener;
import pro.sky.telegrambot.service.NotificationTaskService;
import pro.sky.telegrambot.service.CurrencyService;
import pro.sky.telegrambot.service.WeatherService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


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
    assertTrue(actualMessage.getParameters().get("text").toString().contains("Привет! Я умный бот"));
  }

  @Test
  void process_ReminderButton_ShouldSendInstructions() {
    // Arrange
    Update update = createUpdate(123L, "⏰ напоминание");
    ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);

    // Act
    listener.process(List.of(update));

    // Assert
    verify(telegramBot).execute(captor.capture());
    SendMessage actualMessage = captor.getValue();

    assertEquals(123L, actualMessage.getParameters().get("chat_id"));
    assertTrue(actualMessage.getParameters().get("text").toString().contains("Отправь мне напоминание"));
  }

  @Test
  void process_CurrencyButton_ShouldCallCurrencyService() {
    // Arrange
    Update update = createUpdate(123L, "💵 курс доллара");
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
    Update update = createUpdate(123L, "🌤️ погода в томске");
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
  void process_ValidReminder_ShouldSaveAndConfirm() {
    // Arrange
    Update update = createUpdate(123L, "25.12.2024 15:30 Поздравить маму");
    when(notificationTaskService.parseAndSaveTask(123L, "25.12.2024 15:30 Поздравить маму"))
        .thenReturn(true);
    ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);

    // Act
    listener.process(List.of(update));

    // Assert
    verify(notificationTaskService).parseAndSaveTask(123L, "25.12.2024 15:30 Поздравить маму");
    verify(telegramBot).execute(captor.capture());

    SendMessage actualMessage = captor.getValue();
    assertTrue(actualMessage.getParameters().get("text").toString().contains("✅ Напоминание успешно запланировано"));
  }

  @Test
  void process_InvalidReminder_ShouldSendError() {
    // Arrange
    Update update = createUpdate(123L, "неправильный формат");
    when(notificationTaskService.parseAndSaveTask(123L, "неправильный формат"))
        .thenReturn(false);
    ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);

    // Act
    listener.process(List.of(update));

    // Assert
    verify(notificationTaskService).parseAndSaveTask(123L, "неправильный формат");
    verify(telegramBot).execute(captor.capture());

    SendMessage actualMessage = captor.getValue();
    assertTrue(actualMessage.getParameters().get("text").toString().contains("Неверный формат"));
  }

  @Test
  void process_EmptyMessage_ShouldNotProcess() {
    // Arrange
    Update update = createUpdate(123L, "");

    // Act
    int result = listener.process(List.of(update));

    // Assert
    assertEquals(-1, result); // Возвращает id последнего обработанного обновления
    verifyNoInteractions(notificationTaskService, currencyService, weatherService);
  }

  @Test
  void process_NullMessage_ShouldNotProcess() {
    // Arrange
    Update update = mock(Update.class);
    Message message = mock(Message.class);

    when(update.message()).thenReturn(message);
    when(message.text()).thenReturn(null);

    // Act
    int result = listener.process(List.of(update));

    // Assert
    assertEquals(-1, result); // Возвращает id последнего обработанного обновления
    verifyNoInteractions(notificationTaskService, currencyService, weatherService);
  }

  @Test
  void process_MultipleUpdates_ShouldProcessAll() {
    // Arrange
    Update update1 = createUpdate(123L, "/start");
    Update update2 = createUpdate(456L, "💵 курс доллара");
    when(currencyService.getUsdRate()).thenReturn("Курс доллара: 75.50 руб.");

    // Act
    listener.process(List.of(update1, update2));

    // Assert
    verify(telegramBot, times(2)).execute(any(SendMessage.class));
    verify(currencyService).getUsdRate();
  }
}
