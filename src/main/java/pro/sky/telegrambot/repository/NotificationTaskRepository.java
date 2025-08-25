package pro.sky.telegrambot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import pro.sky.telegrambot.model.NotificationTask;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationTaskRepository extends JpaRepository<NotificationTask, Long> {

  // Используется в checkNotifications для поиска неотправленных уведомлений
  List<NotificationTask> findByNotificationDateTimeAndSentFalse(LocalDateTime notificationDateTime);

  // Используется в cleanupOldNotifications для удаления отправленных уведомлений
  @Transactional
  @Modifying
  @Query("DELETE FROM NotificationTask n WHERE n.sent = true AND n.sentDateTime < :dateTime")
  int deleteBySentTrueAndSentDateTimeBefore(@Param("dateTime") LocalDateTime dateTime);

  // Используется в cleanupOldNotifications для удаления старых неотправленных уведомлений
  @Transactional
  @Modifying
  @Query("DELETE FROM NotificationTask n WHERE n.sent = false AND n.notificationDateTime < :dateTime")
  int deleteBySentFalseAndNotificationDateTimeBefore(@Param("dateTime") LocalDateTime dateTime);
}