package pro.sky.telegrambot.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
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
    notificationTask.setSent(false);
    notificationTaskRepository.save(notificationTask);
  }

  @Scheduled(cron = "0 * * * * *") // Проверка каждую минуту
  public void checkNotifications() {
    LocalDateTime currentDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);

    List<NotificationTask> tasks = notificationTaskRepository
        .findByNotificationDateTime(currentDateTime);

    for (NotificationTask task : tasks) {
      SendMessage sendMessage = new SendMessage(task.getChatId(), "🔔 Напоминание:\n" + task.getMessage())
          .disableWebPagePreview(true);
      telegramBot.execute(sendMessage);

      // Помечаем как отправленное и сохраняем время отправки
      task.setSent(true);
      task.setSentDateTime(LocalDateTime.now());
      notificationTaskRepository.save(task);
    }
  }

  //для очистки старых напоминаний
  @Scheduled(cron = "0 0 3 * * *") // Каждый день в 3:00
  public void cleanupOldNotifications() {
    LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);

    // Удаляем отправленные напоминания старше 24 часов
    notificationTaskRepository.deleteBySentTrueAndSentDateTimeBefore(twentyFourHoursAgo);

    // Удаляем неотправленные напоминания из прошлого
    notificationTaskRepository.deleteBySentFalseAndNotificationDateTimeBefore(LocalDateTime.now());
  }

  public boolean parseAndSaveTask(Long chatId, String text) {
    Pattern pattern = Pattern.compile("(\\d{2}\\.\\d{2}\\.\\d{4} \\d{2}:\\d{2}) (.+)");
    Matcher matcher = pattern.matcher(text);

    if (matcher.matches()) {
      String dateTimeString = matcher.group(1);
      String message = matcher.group(2);

      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
      LocalDateTime dateTime = LocalDateTime.parse(dateTimeString, formatter);

      // Проверяем, что дата не в прошлом
      if (dateTime.isBefore(LocalDateTime.now())) {
        return false;
      }

      saveNotificationTask(chatId, message, dateTime);
      return true;
    }
    return false;
  }
}