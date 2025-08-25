package pro.sky.telegrambot;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;
import pro.sky.telegrambot.service.NotificationTaskService;


@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class NotificationTaskServiceTest {

  @Mock
  private NotificationTaskRepository notificationTaskRepository;

  @Mock
  private TelegramBot telegramBot;

  @InjectMocks
  private NotificationTaskService notificationTaskService;

  @Test
  void testParseAndSaveTaskValidFormat() {
    // Arrange
    Long chatId = 123L;
    String validText = "25.12.2025 15:30 Поздравить маму";

    // Act
    boolean result = notificationTaskService.parseAndSaveTask(chatId, validText);

    // Assert
    assertTrue(result);
    verify(notificationTaskRepository, times(1)).save(any(NotificationTask.class));
  }

  @Test
  void testParseAndSaveTaskInvalidFormat() {
    // Arrange
    Long chatId = 123L;
    String invalidText = "неправильный формат";

    // Act
    boolean result = notificationTaskService.parseAndSaveTask(chatId, invalidText);

    // Assert
    assertFalse(result);
    verify(notificationTaskRepository, never()).save(any(NotificationTask.class));
  }

  @Test
  void testCheckNotifications() {
    // Arrange
    LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
    NotificationTask task1 = new NotificationTask();
    task1.setChatId(123L);
    task1.setMessage("Тестовое напоминание 1");
    task1.setNotificationDateTime(now);
    task1.setSent(false);

    NotificationTask task2 = new NotificationTask();
    task2.setChatId(456L);
    task2.setMessage("Тестовое напоминание 2");
    task2.setNotificationDateTime(now);
    task2.setSent(false);

    List<NotificationTask> tasks = Arrays.asList(task1, task2);

    // Создаем мок успешного ответа от Telegram
    SendResponse successResponse = mock(SendResponse.class);
    when(successResponse.isOk()).thenReturn(true);

    when(notificationTaskRepository.findByNotificationDateTimeAndSentFalse(now))
        .thenReturn(tasks);
    when(telegramBot.execute(any(SendMessage.class))).thenReturn(successResponse);

    // Act
    notificationTaskService.checkNotifications();

    // Assert
    verify(telegramBot, times(2)).execute(any(SendMessage.class));
    verify(notificationTaskRepository, times(2)).save(any(NotificationTask.class));
  }

  @Test
  void testCheckNotificationsWithSentTasks() {
    // Arrange
    LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);

    // Создаем задачу, которая уже отправлена - она не должна обрабатываться
    NotificationTask sentTask = new NotificationTask();
    sentTask.setChatId(123L);
    sentTask.setMessage("Уже отправленное напоминание");
    sentTask.setNotificationDateTime(now);
    sentTask.setSent(true);

    List<NotificationTask> emptyList = List.of();

    when(notificationTaskRepository.findByNotificationDateTimeAndSentFalse(now))
        .thenReturn(emptyList);

    // Act
    notificationTaskService.checkNotifications();

    // Assert
    verify(telegramBot, never()).execute(any(SendMessage.class));
    verify(notificationTaskRepository, never()).save(any(NotificationTask.class));
  }

  @Test
  void testSaveNotificationTask() {
    // Arrange
    Long chatId = 123L;
    String message = "Тестовое сообщение";
    LocalDateTime dateTime = LocalDateTime.now();

    // Act
    notificationTaskService.saveNotificationTask(chatId, message, dateTime);

    // Assert
    ArgumentCaptor<NotificationTask> captor = ArgumentCaptor.forClass(NotificationTask.class);
    verify(notificationTaskRepository).save(captor.capture());

    NotificationTask savedTask = captor.getValue();
    assertEquals(chatId, savedTask.getChatId());
    assertEquals(message, savedTask.getMessage());
    assertEquals(dateTime, savedTask.getNotificationDateTime());
    assertFalse(savedTask.isSent()); // Проверяем, что по умолчанию не отправлено
    assertNull(savedTask.getSentDateTime()); // И время отправки null
  }

  @Test
  void testCleanupOldNotifications() {
    // Arrange - не нужно создавать конкретные даты

    // Act
    notificationTaskService.cleanupOldNotifications();

    // Assert - проверяем что методы были вызваны с любыми LocalDateTime
    verify(notificationTaskRepository).deleteBySentTrueAndSentDateTimeBefore(any(LocalDateTime.class));
    verify(notificationTaskRepository).deleteBySentFalseAndNotificationDateTimeBefore(any(LocalDateTime.class));
  }

  @Test
  void testCheckNotificationsWithException() {
    // Arrange
    LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);

    when(notificationTaskRepository.findByNotificationDateTimeAndSentFalse(now))
        .thenThrow(new RuntimeException("Test exception"));

    // Act & Assert - не должно быть исключения, только логирование
    assertDoesNotThrow(() -> notificationTaskService.checkNotifications());
  }
}