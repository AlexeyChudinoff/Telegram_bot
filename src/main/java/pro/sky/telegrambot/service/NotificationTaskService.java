package pro.sky.telegrambot.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

@Service
public class NotificationTaskService {

  private static final Logger logger = LoggerFactory.getLogger(NotificationTaskService.class);
  private final NotificationTaskRepository notificationTaskRepository;
  private final TelegramBot telegramBot;

  public NotificationTaskService(NotificationTaskRepository notificationTaskRepository,
      TelegramBot telegramBot) {
    this.notificationTaskRepository = notificationTaskRepository;
    this.telegramBot = telegramBot;
  }

  public void saveNotificationTask(Long chatId, String message,
      LocalDateTime notificationDateTime) {
    NotificationTask notificationTask = new NotificationTask();
    notificationTask.setChatId(chatId);
    notificationTask.setMessage(message);
    notificationTask.setNotificationDateTime(notificationDateTime);
    notificationTask.setSent(false);
    notificationTaskRepository.save(notificationTask);

    logger.info("Saved notification for chat {} at {}", chatId, notificationDateTime);
  }

  @Scheduled(cron = "0 * * * * *") // Проверка каждую минуту
  public void checkNotifications() {
    LocalDateTime currentDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);

    try {
      List<NotificationTask> tasks = notificationTaskRepository
          .findByNotificationDateTimeAndSentFalse(currentDateTime);

      for (NotificationTask task : tasks) {
        SendMessage sendMessage = new SendMessage(task.getChatId(),
            "🔔 Напоминание:\n" + task.getMessage())
            .disableWebPagePreview(true);
        telegramBot.execute(sendMessage);

        // Помечаем как отправленное и сохраняем время отправки
        task.setSent(true);
        task.setSentDateTime(LocalDateTime.now());
        notificationTaskRepository.save(task);

        logger.info("Sent notification to chat {}: {}", task.getChatId(), task.getMessage());
      }
    } catch (Exception e) {
      logger.error("Error checking notifications", e);
    }
  }

  // Очистка старых напоминаний
  @Scheduled(cron = "0 0 3 * * *") // Каждый день в 3:00
  public void cleanupOldNotifications() {
    try {
      LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);

      // Удаляем отправленные напоминания старше 24 часов
      int deletedSent = notificationTaskRepository.deleteBySentTrueAndSentDateTimeBefore(
          twentyFourHoursAgo);

      // Удаляем неотправленные напоминания из прошлого
      int deletedUnsent = notificationTaskRepository.deleteBySentFalseAndNotificationDateTimeBefore(
          LocalDateTime.now());

      logger.info("Cleanup completed: deleted {} sent and {} unsent notifications",
          deletedSent, deletedUnsent);
    } catch (Exception e) {
      logger.error("Error cleaning up old notifications", e);
    }
  }

}