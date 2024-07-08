package com.example.timecraft2.Adapter;
import android.os.Build;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.timecraft2.AddNewTask;
import com.example.timecraft2.NotificationWorker;
import com.example.timecraft2.Home;
import com.example.timecraft2.Model.ToDoModel;
import com.example.timecraft2.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class ToDoAdapter extends RecyclerView.Adapter<ToDoAdapter.MyViewHolder> {
    private Timer timer;
    private List<ToDoModel> todoList;
    private Home activity;
    private FirebaseFirestore firestore;
    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "task_states";
    private ToDoAdapter adapter;
    private static final String CHANNEL_ID = "ToDoListChannel";
    private static final int NOTIFICATION_ID_PREFIX = 1000;

    private Context context;
    public static final int REQUEST_CODE_NOTIFICATION_PERMISSION = 123;
    private static final String TAG_NOTIFICATION_WORK = "notification_work";


    public ToDoAdapter(Home home, List<ToDoModel> todoList) {
        this.activity = home; // Assign 'home' to 'activity'
        this.todoList = todoList;
        this.context = home; // Assign 'home' to 'context'
        firestore = FirebaseFirestore.getInstance();
        sharedPreferences = activity.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.each_task, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return todoList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView mTaskNameTv;
        TextView mDueDateTv;
        Button mButton;
        ConstraintLayout taskClick;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            mTaskNameTv = itemView.findViewById(R.id.thistask);
            mDueDateTv = itemView.findViewById(R.id.due_date);
            mButton = itemView.findViewById(R.id.viewtask);
            taskClick = itemView.findViewById(R.id.taskClick);
            // Set click listener for the button
            mButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        ToDoModel toDoModel = todoList.get(position);

                        // Update button text and disable it
                        mButton.setText("Completed");
                        mButton.setEnabled(false);

                        // Change button color
                        mButton.setBackgroundColor(Color.GRAY);
                        taskClick.setBackgroundColor(Color.GREEN);
                        // Update status in Firestore to 1
                        updateStatusInFirestore(toDoModel.getTaskId(), 1);

                    }
                }
            });
        }
    }

    public void deleteTask(int position) {
        ToDoModel toDoModel = todoList.get(position);
        firestore.collection("task").document(toDoModel.TaskId).delete();
        todoList.remove(position);
        notifyItemRemoved(position);
    }

    public Context getContext() {
        return activity;
    }

    public void editTask(int position) {
        ToDoModel toDoModel = todoList.get(position);

        Bundle bundle = new Bundle();
        bundle.putString("task", toDoModel.getTask());
        bundle.putString("due", toDoModel.getDue());
        bundle.putString("id", toDoModel.TaskId);

        AddNewTask addNewTask = new AddNewTask();
        addNewTask.setArguments(bundle);
        addNewTask.show(activity.getSupportFragmentManager(), addNewTask.getTag());
    }

    private void updateStatusInFirestore(String taskId, int newStatus) {

        firestore.collection("task").document(taskId)
                .update("status", newStatus)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(activity, "Status successfully Updated", Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle failure to update status in Firestore
                        Log.e("ToDoAdapter", "Error updating status: " + e.getMessage());
                    }
                });
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        ToDoModel toDoModel = todoList.get(position);
        holder.mTaskNameTv.setText(toDoModel.getTask());
        holder.mDueDateTv.setText("Due On " + toDoModel.getDue());
        boolean isPastDueTime = isPastDueTime(toDoModel.getDue());
        int savedStatus = sharedPreferences.getInt(toDoModel.getTaskId(), 0);
        boolean isCompleted = savedStatus == 1;

        for (int i = 0; i < todoList.size(); i++) {
            ToDoModel currentToDo = todoList.get(i);
            int value=currentToDo.getStatus();
            if (!isPastDueTime) {
                scheduleNotification(currentToDo);
            }
            Log.d("currr", String.valueOf(todoList.get(i)));
            Log.d("Test iteration", String.valueOf(i));
        }

        // Restore button state and background color from SharedPreferences

        // Check if the due date is today
        boolean isToday = isToday(toDoModel.getDue());

        // Check if the due time is past due


        if (isToday && !isPastDueTime && !isCompleted) {
            // Due date is today and task is not past due time and incomplete, set background color to yellow
            holder.taskClick.setBackgroundColor(Color.YELLOW);
            holder.mButton.setTextColor(Color.BLACK);
            holder.mButton.setEnabled(true);
            holder.mButton.setText("Not Complete");
        } else if (!isCompleted && isTomorrow(toDoModel.getDue()) && !isPastDueTime) {
            // Due date is tomorrow and task is not past due time and incomplete, set background color to yellow
            holder.taskClick.setBackgroundColor(Color.YELLOW);
            holder.mButton.setTextColor(Color.BLACK);
            holder.mButton.setEnabled(true);
            holder.mButton.setText("Not Complete");
        } else if (!isCompleted && isPastDueTime) {
            // Due time is past due and task is incomplete, set background color to red
            holder.taskClick.setBackgroundColor(Color.RED);
            holder.mButton.setEnabled(false);
            holder.mButton.setText("Not Completed");


        } else if (isCompleted) {
            // Completed task, set background color to green
            holder.taskClick.setBackgroundColor(Color.GREEN);
            holder.mButton.setTextColor(Color.BLACK);
            holder.mButton.setEnabled(false);
            holder.mButton.setText("Completed");
        }
        else {
            // Incomplete task, set background color to transparent
            holder.taskClick.setBackgroundColor(Color.TRANSPARENT);
            holder.mButton.setTextColor(Color.WHITE);
            holder.mButton.setEnabled(true);
            holder.mButton.setText("Not Completed");
        }

        // Set click listener for the button
        holder.mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Update button text and disable it
                holder.mButton.setEnabled(false);
                holder.mButton.setText("Completed");
                holder.taskClick.setBackgroundColor(Color.GREEN);

                // Save the updated status locally
                saveStatusLocally(toDoModel.getTaskId(), 1);

                // Update status in Firestore
                updateStatusInFirestore(toDoModel.getTaskId(), 1);
            }
        });
    }
    private void saveStatusLocally(String taskId, int status) {
        // Save the status to SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(taskId, status);
        editor.apply();
    }


    private boolean isToday(String dueDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date today = sdf.parse(sdf.format(new Date()));
            Date due = sdf.parse(dueDate);
            return due.compareTo(today) == 0;
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean isTomorrow(String dueDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            Date tomorrow = sdf.parse(sdf.format(calendar.getTime()));
            Date due = sdf.parse(dueDate);
            return due.compareTo(tomorrow) == 0;
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean isPastDueTime(String dueDateTime) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            Date currentDateTime = new Date();
            Date due = sdf.parse(dueDateTime);
            return currentDateTime.compareTo(due) > 0;
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }
    // Declare Timer as a class member



    private void scheduleNotification(ToDoModel toDoModel) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(); // Create notification channel if Android version is Oreo or above
        }

        // Initialize the timer
        timer = new Timer();

        // Schedule a task to continuously check if any task's due time and date is reached
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                checkDueTime(toDoModel);
            }
        }, 0, 1000); // Check every second
    }

    private void checkDueTime(ToDoModel toDoModel) {
        // Parse due date and time
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        try {
            Date dueDateTime = sdf.parse(toDoModel.getDue());
            if (dueDateTime == null) {
                throw new ParseException("Failed to parse due date and time", 0);
            }
            long dueTimeMillis = dueDateTime.getTime();

            // Get current time
            long currentTimeMillis = System.currentTimeMillis();

            // Calculate time difference in milliseconds
            long timeDifferenceMillis = dueTimeMillis - currentTimeMillis;

            if ((timeDifferenceMillis >= 0 && timeDifferenceMillis <= 1000)) {
                // Due time is reached, create and display overdue notification immediately
                showOverdueNotification(toDoModel);
                timer.cancel(); // Cancel the timer as the notification is shown
            } else if ((timeDifferenceMillis <= 24 * 60 * 60 * 1000)&& timeDifferenceMillis>0 ) {
                // Task is due within the specified interval, schedule reminder notification
                scheduleReminderNotification(toDoModel, dueTimeMillis);
                timer.cancel(); // Cancel the timer as the notification is scheduled
            }


        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(context, "Failed to parse due date and time", Toast.LENGTH_SHORT).show();
        }
    }





    private void scheduleReminderNotification(ToDoModel toDoModel, long notificationTimeMillis) {
        // Calculate time difference in milliseconds
        long currentTimeMillis = System.currentTimeMillis();
        long timeDifferenceMillis = notificationTimeMillis - currentTimeMillis;

        // Calculate remaining hours and minutes
        long hoursLeft = timeDifferenceMillis / (1000 * 60 * 60);
        long minutesLeft = (timeDifferenceMillis / (1000 * 60)) % 60;

        // Create notification content for reminder with hours and minutes left
        String contentTitle = "Task Reminder";
        String contentText = toDoModel.getTask() + " is due in " + hoursLeft + " hours and " + minutesLeft + " minutes";

        // Create notification intent for reminder
        Intent intent = new Intent(context, NotificationWorker.class);
        intent.putExtra("task", toDoModel.getTask());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, NOTIFICATION_ID_PREFIX + toDoModel.getTaskId().hashCode(), intent, PendingIntent.FLAG_IMMUTABLE);

        // Create notification builder for reminder
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.baseline_access_time_24) // Replace with your app icon
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true); // Automatically remove the notification when clicked

        // Show reminder notification
        showNotification(builder, toDoModel);
    }


    private void showOverdueNotification(ToDoModel toDoModel) {
        // Create notification content for overdue
        String contentTitle = "Task Overdue";
        String contentText = toDoModel.getTask() + " is overdue";

        // Create notification intent for overdue
        Intent intent = new Intent(context, NotificationWorker.class);
        intent.putExtra("task", toDoModel.getTask());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, NOTIFICATION_ID_PREFIX + toDoModel.getTaskId().hashCode(), intent, PendingIntent.FLAG_IMMUTABLE);

        // Create notification builder for overdue
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.baseline_access_time_24) // Replace with your app icon
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true); // Automatically remove the notification when clicked

        // Show overdue notification
        showNotification(builder, toDoModel);
    }


    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Channel Name", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    private void showNotification(NotificationCompat.Builder builder, ToDoModel toDoModel) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_NOTIFICATION_POLICY) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_NOTIFICATION_POLICY}, REQUEST_CODE_NOTIFICATION_PERMISSION);
        } else {
            // Permission is already granted, proceed to display the notification
            notificationManager.notify(NOTIFICATION_ID_PREFIX + toDoModel.getTaskId().hashCode(), builder.build());
            return; // Exit the method after showing the notification
        }
        OneTimeWorkRequest notificationWorkRequest = new OneTimeWorkRequest.Builder(NotificationWorker.class)
                .setInitialDelay(0, TimeUnit.MILLISECONDS) // Start immediately
                .addTag(TAG_NOTIFICATION_WORK)
                .build();

        WorkManager.getInstance(context).enqueueUniqueWork(TAG_NOTIFICATION_WORK, ExistingWorkPolicy.REPLACE, notificationWorkRequest);
        // Create a data object to hold task name, notification time, and type of notification

    }

}