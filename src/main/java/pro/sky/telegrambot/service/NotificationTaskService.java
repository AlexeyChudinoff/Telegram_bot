package pro.sky.telegrambot.service;

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

    this.notificationTaskRepository = notificationTaskRepository;
    this.telegramBot = telegramBot;
  }

  public void saveNotificationTask(Long chatId, String message, LocalDateTime notificationDateTime) {
    NotificationTask notificationTask = new NotificationTask();
    notificationTask.setChatId(chatId);
    notificationTask.setMessage(message);
    notificationTask.setNotificationDateTime(notificationDateTime);
    notificationTaskRepository.save(notificationTask);
  }

  @Scheduled(cron = "0 * * * * *")
  public void checkNotifications() {
      LocalDateTime currentDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);

      for (NotificationTask task : tasks) {
          SendMessage sendMessage = new SendMessage(task.getChatId(), "🔔 " + task.getMessage())
              .disableWebPagePreview(true);

    }
  }

  public boolean parseAndSaveTask(Long chatId, String text) {
    Pattern pattern = Pattern.compile("(\\d{2}\\.\\d{2}\\.\\d{4} \\d{2}:\\d{2}) (.+)");
    Matcher matcher = pattern.matcher(text);

    if (matcher.matches()) {
      String dateTimeString = matcher.group(1);
      String message = matcher.group(2);

      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        LocalDateTime dateTime = LocalDateTime.parse(dateTimeString, formatter);

        saveNotificationTask(chatId, message, dateTime);
        return true;
    }
    return false;
  }