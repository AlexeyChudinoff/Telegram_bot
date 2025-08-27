package pro.sky.telegrambot.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ReminderParserService {

  private static final Logger logger = LoggerFactory.getLogger(ReminderParserService.class);
  private static final Pattern REMINDER_PATTERN =
      Pattern.compile("(\\d{2}\\.\\d{2}\\.\\d{4} \\d{2}:\\d{2}) (.+)");

  public boolean isValidReminderFormat(String text) {
    if (text == null || text.trim().isEmpty()) {
      return false;
    }
    return REMINDER_PATTERN.matcher(text.trim()).matches();
  }

  public ParsedReminder parseReminder(String text) {
    try {
      if (text == null) {
        return new ParsedReminder(null, null, false);
      }

      String trimmedText = text.trim();
      if (trimmedText.isEmpty()) {
        return new ParsedReminder(null, null, false);
      }

      Matcher matcher = REMINDER_PATTERN.matcher(trimmedText);

      if (matcher.matches()) {
        String dateTimeString = matcher.group(1);
        String message = matcher.group(2);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        LocalDateTime dateTime = LocalDateTime.parse(dateTimeString, formatter);

        return new ParsedReminder(dateTime, message, true);
      }
      return new ParsedReminder(null, null, false);
    } catch (Exception e) {
      logger.error("Error parsing reminder: {}", text, e);
      return new ParsedReminder(null, null, false);
    }
  }

  public boolean isFutureDateTime(LocalDateTime dateTime) {
    if (dateTime == null) {
      return false;
    }
    return !dateTime.isBefore(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
  }

  public static class ParsedReminder {
    private final LocalDateTime dateTime;
    private final String message;
    private final boolean valid;

    public ParsedReminder(LocalDateTime dateTime, String message, boolean valid) {
      this.dateTime = dateTime;
      this.message = message;
      this.valid = valid;
    }

    public LocalDateTime getDateTime() { return dateTime; }
    public String getMessage() { return message; }
    public boolean isValid() { return valid; }
  }
}