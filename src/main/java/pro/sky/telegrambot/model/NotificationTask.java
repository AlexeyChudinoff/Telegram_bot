package pro.sky.telegrambot.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
public class NotificationTask {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private Long chatId;
  private String message;
  private LocalDateTime notificationDateTime;
  private Boolean sent = false; // ДОБАВЛЕНО
  private LocalDateTime sentDateTime; // ДОБАВЛЕНО


  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getChatId() {
    return chatId;
  }

  public void setChatId(Long chatId) {
    this.chatId = chatId;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public LocalDateTime getNotificationDateTime() {
    return notificationDateTime;
  }

  public void setNotificationDateTime(LocalDateTime notificationDateTime) {
    this.notificationDateTime = notificationDateTime;
  }

  public Boolean getSent() {
    return sent;
  }

  public void setSent(Boolean sent) {
    this.sent = sent;
  }

  public LocalDateTime getSentDateTime() {
    return sentDateTime;
  }

  public void setSentDateTime(LocalDateTime sentDateTime) {
    this.sentDateTime = sentDateTime;
  }
}