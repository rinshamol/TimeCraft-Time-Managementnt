package com.example.timecraft2;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;

public class PushNotificationService extends FirebaseMessagingService {
    NotificationManager notificationManager;
    Notification notification;
    Uri defaultSoundUri;
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (remoteMessage != null) {
            Map<String,String> dataMap = new HashMap<>();
            String noteType="";
            if (remoteMessage.getData().size() > 0) {
                noteType = remoteMessage.getData().get("type");
                dataMap = remoteMessage.getData();

            }
        }
    }
    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
    }
}
