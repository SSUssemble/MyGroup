package com.example.ssussemble;

import static android.Manifest.permission.POST_NOTIFICATIONS;

import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.List;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String CHANNEL_ID = "chat_notifications";
    private static final String TAG = "FCM_Service";

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "New token: " + token);

        if (MainActivity.Login_id != null) {
            String userKey = MainActivity.Login_id.replace(".", "_dot_").replace("@", "_at_");
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                    .child("users")
                    .child(userKey);
            userRef.child("fcmToken").setValue(token);
        }
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d(TAG, "Message received from: " + remoteMessage.getFrom());

        String currentRoomId = NavigationManager.getInstance().getCurrentRoomId();
        Map<String, String> data = remoteMessage.getData();

        if (currentRoomId != null && currentRoomId.equals(data.get("chatRoomId"))) {
            return;
        }

        if (MainActivity.Login_id.equals(data.get("senderId"))) {
            return;
        }

        createNotificationChannel();
        showNotification(data);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Chat Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void showNotification(Map<String, String> data) {
        // MainActivity로 이동하는 인텐트 생성
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("chatRoomId", data.get("chatRoomId"));

        // SharedPreferences에 채팅방 ID 저장
        getSharedPreferences("chat_prefs", MODE_PRIVATE)
                .edit()
                .putString("pending_chat_room", data.get("chatRoomId"))
                .apply();

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(data.get("roomName"))
                .setContentText(data.get("message"))
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(0, builder.build());
        }
    }
}