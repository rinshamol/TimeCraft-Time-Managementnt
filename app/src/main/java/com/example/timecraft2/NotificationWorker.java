package com.example.timecraft2;

import static com.example.timecraft2.Adapter.ToDoAdapter.REQUEST_CODE_NOTIFICATION_PERMISSION;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.timecraft2.R;

public class NotificationWorker extends Worker {

    private static final String CHANNEL_ID = "ToDoListChannel";
    private static final int NOTIFICATION_ID_PREFIX = 1000;

    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        // Retrieve data from input data
        String taskName = getInputData().getString("taskName");
        long notificationTimeMillis = getInputData().getLong("notificationTimeMillis", 0);
        boolean isOverdue = getInputData().getBoolean("isOverdue", false);

        // Check if it's a reminder or overdue notification
        if (isOverdue) {
            // It's an overdue notification
            showOverdueNotification(taskName);
        } else {
            // It's a reminder notification
            showReminderNotification(taskName, notificationTimeMillis);
        }

        return Result.success();
    }

    private void showReminderNotification(String taskName, long notificationTimeMillis) {
        // Calculate time difference in milliseconds
        long currentTimeMillis = System.currentTimeMillis();
        long timeDifferenceMillis = notificationTimeMillis - currentTimeMillis;

        // Calculate hours left
        long hoursLeft = timeDifferenceMillis / (1000 * 60 * 60);

        // Create notification content for reminder with hours left
        String contentTitle = "Task Reminder";
        String contentText = taskName + " is due in " + hoursLeft + " hours";

        // Create notification builder for reminder
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.baseline_access_time_24) // Replace with your app icon
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true); // Automatically remove the notification when clicked

        // Show reminder notification
        showNotification(builder);
    }

    private void showOverdueNotification(String taskName) {
        // Create notification content for overdue
        String contentTitle = "Task Overdue";
        String contentText = taskName + " is overdue";

        // Create notification builder for overdue
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.baseline_access_time_24) // Replace with your app icon
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true); // Automatically remove the notification when clicked

        // Show overdue notification
        showNotification(builder);
    }

    private void showNotification(NotificationCompat.Builder builder) {
        // Show the notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());

        // Check if the permission is granted
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions((Activity)getApplicationContext(), new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_CODE_NOTIFICATION_PERMISSION);
            return;
        }

        // Permission is granted, show the notification
        notificationManager.notify(NOTIFICATION_ID_PREFIX, builder.build());
    }
}
