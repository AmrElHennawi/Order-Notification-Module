package org.fcai.OrderNotificationModule.Controllers;

import org.fcai.OrderNotificationModule.Enums.NotificationChannel;
import org.fcai.OrderNotificationModule.Enums.NotificationLanguage;
import org.fcai.OrderNotificationModule.Enums.OrderStatus;
import org.fcai.OrderNotificationModule.Models.Notification;
import org.fcai.OrderNotificationModule.Helpers.NotificationFactory;
import org.fcai.OrderNotificationModule.Models.NotificationSpecs;
import org.fcai.OrderNotificationModule.Models.Order;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.Queue;

@EnableScheduling
@RestController
@RequestMapping("/notifications")
public class NotificationController {
    private final Queue<Notification> notificationQueue;

    public NotificationController() {
        this.notificationQueue = new LinkedList<>();
    }

    @PostMapping("/send-notification/{orderStatus}")
    public void sendNotification(@RequestBody Order order,
                                 @RequestBody NotificationLanguage language,
                                 @RequestBody NotificationChannel channel,
                                 @PathVariable OrderStatus orderStatus) {
        try {
            NotificationSpecs specs = new NotificationSpecs(order.getId(),
                    order.getUser().getName(), order.getTotalPrice(), 7, channel, language);
            Notification notification = NotificationFactory.createNotification(specs, orderStatus);
            notificationQueue.add(notification);
        } catch (NullPointerException | IllegalArgumentException e) {
            System.err.println("Cannot send notification: " + e.getMessage());
            throw e;
        }
    }

    @Scheduled(fixedDelay = 1000)
    public void sendNotification() throws IOException {
        try {
            System.out.println("Sending notification");
            File file = new File("target/Logging/SendingNotificationLoggingFile.txt");
            FileOutputStream outputStream = new FileOutputStream(file, true);
            if (notificationQueue.isEmpty()) {
                outputStream.write("No notifications to send\n".getBytes());
            } else {
                String notificationLoggerMessage = notificationQueue.poll().toString();
                notificationLoggerMessage = notificationLoggerMessage
                        .concat(String.format(", Sent at: %s\n", LocalDateTime.now().toString()));
                outputStream.write(notificationLoggerMessage.getBytes());
            }
            outputStream.close();
        }
        catch (IOException e) {
            System.err.println("Cannot send notification: " + e.getMessage());
            throw e;
        }
    }
}
