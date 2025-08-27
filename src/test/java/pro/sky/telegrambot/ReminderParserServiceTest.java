package pro.sky.telegrambot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import pro.sky.telegrambot.service.ReminderParserService;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class ReminderParserServiceTest {

  private final ReminderParserService reminderParserService = new ReminderParserService();

  @Test
  void testIsValidReminderFormat_ValidFormats() {
    // Valid formats
    assertTrue(reminderParserService.isValidReminderFormat("25.12.2025 15:30 Поздравить маму"));
    assertTrue(reminderParserService.isValidReminderFormat("01.01.2025 00:00 С Новым годом!"));
    assertTrue(reminderParserService.isValidReminderFormat("15.03.2025 09:00 Сходить к врачу"));
    assertTrue(reminderParserService.isValidReminderFormat("31.12.2025 23:59 Последнее напоминание года"));
  }

  @Test
  void testIsValidReminderFormat_InvalidFormats() {
    // Invalid formats
    assertFalse(reminderParserService.isValidReminderFormat(""));
    assertFalse(reminderParserService.isValidReminderFormat("   "));
    assertFalse(reminderParserService.isValidReminderFormat(null));
    assertFalse(reminderParserService.isValidReminderFormat("просто текст"));
    assertFalse(reminderParserService.isValidReminderFormat("25.12.2025"));
    assertFalse(reminderParserService.isValidReminderFormat("15:30 Поздравить маму"));
    assertFalse(reminderParserService.isValidReminderFormat("25.12.2025 15:30"));
    assertFalse(reminderParserService.isValidReminderFormat("2025-12-25 15:30 Поздравить"));
    assertFalse(reminderParserService.isValidReminderFormat("25/12/2025 15:30 Поздравить"));
  }

  @Test
  void testParseReminder_ValidReminder() {
    // Arrange
    String validText = "25.12.2025 15:30 Поздравить маму с праздником";

    // Act
    ReminderParserService.ParsedReminder result = reminderParserService.parseReminder(validText);

    // Assert
    assertNotNull(result);
    assertTrue(result.isValid());
    assertEquals("Поздравить маму с праздником", result.getMessage());

    LocalDateTime expectedDateTime = LocalDateTime.parse("25.12.2025 15:30",
        DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
    assertEquals(expectedDateTime, result.getDateTime());
  }

  @Test
  void testParseReminder_ValidReminderWithSpecialCharacters() {
    // Arrange
    String validText = "31.12.2025 23:59 С Новым 2026 годом! 🎉";

    // Act
    ReminderParserService.ParsedReminder result = reminderParserService.parseReminder(validText);

    // Assert
    assertNotNull(result);
    assertTrue(result.isValid());
    assertEquals("С Новым 2026 годом! 🎉", result.getMessage());

    LocalDateTime expectedDateTime = LocalDateTime.parse("31.12.2025 23:59",
        DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
    assertEquals(expectedDateTime, result.getDateTime());
  }

  @Test
  void testParseReminder_InvalidReminder() {
    // Arrange
    String invalidText = "неправильный формат";

    // Act
    ReminderParserService.ParsedReminder result = reminderParserService.parseReminder(invalidText);

    // Assert
    assertNotNull(result);
    assertFalse(result.isValid());
    assertNull(result.getMessage());
    assertNull(result.getDateTime());
  }

  @Test
  void testParseReminder_MalformedDate() {
    // Arrange - invalid date format
    String malformedText = "32.13.2025 25:61 Некорректная дата";

    // Act
    ReminderParserService.ParsedReminder result = reminderParserService.parseReminder(malformedText);

    // Assert
    assertNotNull(result);
    assertFalse(result.isValid());
    assertNull(result.getMessage());
    assertNull(result.getDateTime());
  }

  @Test
  void testParseReminder_EmptyText() {
    // Arrange
    String emptyText = "";

    // Act
    ReminderParserService.ParsedReminder result = reminderParserService.parseReminder(emptyText);

    // Assert
    assertNotNull(result);
    assertFalse(result.isValid());
    assertNull(result.getMessage());
    assertNull(result.getDateTime());
  }

  @Test
  void testParseReminder_NullText() {
    // Arrange
    String nullText = null;

    // Act
    ReminderParserService.ParsedReminder result = reminderParserService.parseReminder(nullText);

    // Assert
    assertNotNull(result);
    assertFalse(result.isValid());
    assertNull(result.getMessage());
    assertNull(result.getDateTime());
  }

  @Test
  void testIsFutureDateTime_FutureDate() {
    // Arrange
    LocalDateTime futureDateTime = LocalDateTime.now().plusDays(1);

    // Act
    boolean result = reminderParserService.isFutureDateTime(futureDateTime);

    // Assert
    assertTrue(result);
  }

  @Test
  void testIsFutureDateTime_PastDate() {
    // Arrange
    LocalDateTime pastDateTime = LocalDateTime.now().minusDays(1);

    // Act
    boolean result = reminderParserService.isFutureDateTime(pastDateTime);

    // Assert
    assertFalse(result);
  }

  @Test
  void testIsFutureDateTime_CurrentTimeTruncated() {
    // Arrange
    LocalDateTime currentDateTime = LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MINUTES);

    // Act
    boolean result = reminderParserService.isFutureDateTime(currentDateTime);

    // Assert
    // Текущее время (с точностью до минут) считается будущим для напоминаний
    assertTrue(result);
  }

  @Test
  void testIsFutureDateTime_CurrentTimePlusOneMinute() {
    // Arrange
    LocalDateTime futureDateTime = LocalDateTime.now()
        .truncatedTo(java.time.temporal.ChronoUnit.MINUTES)
        .plusMinutes(1);

    // Act
    boolean result = reminderParserService.isFutureDateTime(futureDateTime);

    // Assert
    assertTrue(result);
  }

  @Test
  void testIsFutureDateTime_CurrentTimeMinusOneMinute() {
    // Arrange
    LocalDateTime pastDateTime = LocalDateTime.now()
        .truncatedTo(java.time.temporal.ChronoUnit.MINUTES)
        .minusMinutes(1);

    // Act
    boolean result = reminderParserService.isFutureDateTime(pastDateTime);

    // Assert
    assertFalse(result);
  }

  @Test
  void testParsedReminderConstructorAndGetters() {
    // Arrange
    LocalDateTime testDateTime = LocalDateTime.now();
    String testMessage = "Test message";

    // Act
    ReminderParserService.ParsedReminder validReminder =
        new ReminderParserService.ParsedReminder(testDateTime, testMessage, true);

    ReminderParserService.ParsedReminder invalidReminder =
        new ReminderParserService.ParsedReminder(null, null, false);

    // Assert
    assertEquals(testDateTime, validReminder.getDateTime());
    assertEquals(testMessage, validReminder.getMessage());
    assertTrue(validReminder.isValid());

    assertNull(invalidReminder.getDateTime());
    assertNull(invalidReminder.getMessage());
    assertFalse(invalidReminder.isValid());
  }

  @Test
  void testEdgeCasesWithWhitespace() {
    // Arrange - различные варианты с пробелами
    String withLeadingSpaces = "   25.12.2025 15:30 Поздравить маму";
    String withTrailingSpaces = "25.12.2025 15:30 Поздравить маму   ";
    String withMultipleSpaces = "25.12.2025   15:30   Поздравить   маму";

    // Act & Assert
    assertTrue(reminderParserService.isValidReminderFormat(withLeadingSpaces));
    assertTrue(reminderParserService.isValidReminderFormat(withTrailingSpaces));
    assertTrue(reminderParserService.isValidReminderFormat(withMultipleSpaces));
  }

  @Test
  void testBoundaryDateValues() {
    // Arrange - граничные значения дат
    String minDate = "01.01.2000 00:00 Тест минимальной даты";
    String maxDate = "31.12.2099 23:59 Тест максимальной даты";

    // Act & Assert
    assertTrue(reminderParserService.isValidReminderFormat(minDate));
    assertTrue(reminderParserService.isValidReminderFormat(maxDate));
  }
}