package pro.sky.telegrambot.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Entity
public class NotificationTask {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private Long chatId;
  private String message;
  private LocalDateTime notificationDateTime;
  private boolean sent = false; // Флаг отправки
  private LocalDateTime sentDateTime; // Когда было отправлено


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

  public boolean isSent() { return sent; }
  public void setSent(boolean sent) { this.sent = sent; }
  public LocalDateTime getSentDateTime() { return sentDateTime; }
  public void setSentDateTime(LocalDateTime sentDateTime) { this.sentDateTime = sentDateTime; }

}