package pro.sky.telegrambot.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import java.time.format.DateTimeParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class NotificationTaskService {
  private final NotificationTaskRepository notificationTaskRepository;
  private final TelegramBot telegramBot;
  private final Logger logger = LoggerFactory.getLogger(NotificationTaskService.class);

  public NotificationTaskService(NotificationTaskRepository notificationTaskRepository,
      TelegramBot telegramBot) {
    this.notificationTaskRepository = notificationTaskRepository;
    this.telegramBot = telegramBot;
  }

  public void saveNotificationTask(Long chatId, String message, LocalDateTime notificationDateTime) {
    NotificationTask notificationTask = new NotificationTask();
    notificationTask.setChatId(chatId);
    notificationTask.setMessage(message);
    notificationTask.setNotificationDateTime(notificationDateTime);
    notificationTaskRepository.save(notificationTask);
    logger.info("Напоминание сохранено: chatId={}, time={}", chatId, notificationDateTime);
  }

  // Проверка напоминаний Каждую минуту, в 0-ю секунду этой минуты.
  @Scheduled(cron = "0 * * * * *")
  public void checkNotifications() {
    try {
      LocalDateTime currentDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
      List<NotificationTask> tasks = notificationTaskRepository
          .findByNotificationDateTimeAndSentFalse(currentDateTime);

      logger.debug("Найдено напоминаний для времени {}: {}", currentDateTime, tasks.size());

      for (NotificationTask task : tasks) {
        try {
          SendMessage sendMessage = new SendMessage(task.getChatId(), "🔔 " + task.getMessage())
             .disableNotification(false)
              .disableWebPagePreview(true);

          var response = telegramBot.execute(sendMessage);

          if (response.isOk()) {
            // Отмечаем как отправленное
            task.setSent(true);
            task.setSentDateTime(LocalDateTime.now());
            notificationTaskRepository.save(task);
            logger.info("Уведомление отправлено: chatId={}", task.getChatId());
          }
        } catch (Exception e) {
          logger.error("Ошибка отправки уведомления chatId={}", task.getChatId(), e);
        }
      }
    } catch (Exception e) {
      logger.error("Ошибка в checkNotifications", e);
    }
  }

  // Очистка старых уведомлений (раз в день)
  @Scheduled(cron = "0 0 2 * * ?") // Каждый день в 2:00
  public void cleanupOldNotifications() {
    LocalDateTime weekAgo = LocalDateTime.now().minusWeeks(1);
    LocalDateTime monthAgo = LocalDateTime.now().minusMonths(1);
    // Удаляем отправленные уведомления старше недели
    notificationTaskRepository.deleteBySentTrueAndSentDateTimeBefore(weekAgo);
    // Удаляем неотправленные уведомления из далекого прошлого
    notificationTaskRepository.deleteBySentFalseAndNotificationDateTimeBefore(monthAgo);

    logger.info("Очистка старых уведомлений выполнена");
  }

  public boolean parseAndSaveTask(Long chatId, String text) {
    Pattern pattern = Pattern.compile("(\\d{2}\\.\\d{2}\\.\\d{4} \\d{2}:\\d{2}) (.+)");
    Matcher matcher = pattern.matcher(text);

    if (matcher.matches()) {
      String dateTimeString = matcher.group(1);
      String message = matcher.group(2);

      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

      try {
        LocalDateTime dateTime = LocalDateTime.parse(dateTimeString, formatter);

        // Validate month (1-12)
        if (dateTime.getMonthValue() < 1 || dateTime.getMonthValue() > 12) {
          return false;
        }

        // Validate day of month
        if (dateTime.getDayOfMonth() < 1 || dateTime.getDayOfMonth() > dateTime.getMonth().maxLength()) {
          return false;
        }

        // Validate time
        if (dateTime.getHour() < 0 || dateTime.getHour() > 23 ||
            dateTime.getMinute() < 0 || dateTime.getMinute() > 59) {
          return false;
        }

        // Validate future date
        if (dateTime.isBefore(LocalDateTime.now())) {
          return false;
        }

        saveNotificationTask(chatId, message, dateTime);
        return true;

      } catch (DateTimeParseException e) {
        return false;
      }
    }
    return false;
  }
}//
