package pro.sky.telegrambot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

  // УДАЛЯЕМ тесты парсинга, так как эта логика теперь в ReminderParserService
  // @Test
  // void testParseAndSaveTaskValidFormat() { ... }

  // @Test
  // void testParseAndSaveTaskInvalidFormat() { ... }

  @Test
  void testCheckNotifications() {
    // Arrange
    LocalDateTime now = LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MINUTES);
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

    when(notificationTaskRepository.findByNotificationDateTimeAndSentFalse(now))
        .thenReturn(tasks);

    // Act
    notificationTaskService.checkNotifications();

    // Assert
    verify(telegramBot, times(2)).execute(any(SendMessage.class));
    verify(notificationTaskRepository, times(2)).save(any(NotificationTask.class));
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
    assertFalse(savedTask.isSent());
  }

  // ДОБАВЛЯЕМ тест для cleanup
  @Test
  void testCleanupOldNotifications() {
    // Act
    notificationTaskService.cleanupOldNotifications();

    // Assert - проверяем что методы репозитория вызываются
    verify(notificationTaskRepository).deleteBySentTrueAndSentDateTimeBefore(any(LocalDateTime.class));
    verify(notificationTaskRepository).deleteBySentFalseAndNotificationDateTimeBefore(any(LocalDateTime.class));
  }
}