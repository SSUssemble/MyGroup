package com.example.ssussemble;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import androidx.core.app.NotificationCompat;

public class NotificationHelper {
    private static final String TAG = "NotificationHelper";
    private static final String CHANNEL_ID = "chat_messages";
    private static final String CHANNEL_NAME = "Chat Messages";
    private static final String CHANNEL_DESC = "채팅 메시지 알림";
    private static final int NOTIFICATION_ID = 1001;

    private final Context context;
    private final NotificationManager notificationManager;

    public NotificationHelper(Context context) {
        this.context = context;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
    }

    public void showChatNotification(String chatRoomId, String senderId, String message) {
        if (!isCurrentChatRoom(chatRoomId)) {
            PendingIntent pendingIntent = createPendingIntent(chatRoomId);
            NotificationCompat.Builder builder = createNotificationBuilder(senderId, message, pendingIntent);
            notificationManager.notify(NOTIFICATION_ID, builder.build());
            Log.d(TAG, "Showing notification for message from: " + senderId);
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(CHANNEL_DESC);
            channel.enableVibration(true);
            channel.enableLights(true);
            notificationManager.createNotificationChannel(channel);
            Log.d(TAG, "Notification channel created");
        }
    }

    private PendingIntent createPendingIntent(String chatRoomId) {
        Intent intent = new Intent(context, MainActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .putExtra("chatRoomId", chatRoomId);

        return PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }

    private NotificationCompat.Builder createNotificationBuilder(
            String senderId,
            String message,
            PendingIntent pendingIntent) {
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        return new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(senderId)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);
    }

    private boolean isCurrentChatRoom(String chatRoomId) {
        String currentRoomId = NavigationManager.getInstance().getCurrentRoomId();
        return currentRoomId != null && currentRoomId.equals(chatRoomId);
    }
}