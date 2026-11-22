package com.redpulse.app.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.redpulse.app.utils.Constants;
import java.util.HashMap;
import java.util.Map;

public class RedPulseMessagingService extends FirebaseMessagingService {
    private static final String CHANNEL_ID = "red_pulse_alerts";

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        if (FirebaseAuth.getInstance().getUid() == null) return;
        Map<String, Object> map = new HashMap<>();
        map.put("token", token);
        map.put("updatedAt", com.google.firebase.Timestamp.now());
        FirebaseFirestore.getInstance()
                .collection(Constants.USERS)
                .document(FirebaseAuth.getInstance().getUid())
                .collection("devices")
                .document(token)
                .set(map);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        createChannel();
        String title = remoteMessage.getNotification() != null ? remoteMessage.getNotification().getTitle() : "Red Pulse";
        String body = remoteMessage.getNotification() != null ? remoteMessage.getNotification().getBody() : "New notification";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat.from(this).notify((int) System.currentTimeMillis(), builder.build());
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Red Pulse Alerts", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Emergency blood alerts and app notifications");
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }
}
