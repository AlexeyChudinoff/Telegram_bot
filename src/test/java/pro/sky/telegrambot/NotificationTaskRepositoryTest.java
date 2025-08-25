package pro.sky.telegrambot;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import pro.sky.telegrambot.model.NotificationTask;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class NotificationTaskRepositoryTest {

  @Autowired
  private Environment environment;

  @Autowired
  private NotificationTaskRepository repository;

  @Test
  void testProfile() {
    String[] activeProfiles = environment.getActiveProfiles();
    assertTrue(Arrays.asList(activeProfiles).contains("test"));
    System.out.println("Active profiles: " + Arrays.toString(activeProfiles));
  }

  @Test
  void testFindByNotificationDateTimeAndSentFalse() {
    // Arrange
    LocalDateTime dateTime = LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MINUTES);
    NotificationTask task = new NotificationTask();
    task.setChatId(123L);
    task.setMessage("Test message");
    task.setNotificationDateTime(dateTime);
    task.setSent(false);

    repository.save(task);

    // Act
    List<NotificationTask> result = repository.findByNotificationDateTimeAndSentFalse(dateTime);

    // Assert
    assertEquals(1, result.size());
    assertEquals("Test message", result.get(0).getMessage());
    assertFalse(result.get(0).isSent());
  }

  @Test
  void testDeleteBySentTrueAndSentDateTimeBefore() {
    // Arrange
    LocalDateTime oldDate = LocalDateTime.now().minusDays(10);

    // Act
    int deletedCount = repository.deleteBySentTrueAndSentDateTimeBefore(oldDate);

    // Assert
    assertEquals(0, deletedCount);
  }

  @Test
  void testDeleteBySentFalseAndNotificationDateTimeBefore() {
    // Arrange
    LocalDateTime oldDate = LocalDateTime.now().minusMonths(2);

    // Act
    int deletedCount = repository.deleteBySentFalseAndNotificationDateTimeBefore(oldDate);

    // Assert
    assertEquals(0, deletedCount);
  }
}