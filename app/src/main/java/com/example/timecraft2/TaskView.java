package com.example.timecraft2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.timecraft2.Model.TaskId;
import com.example.timecraft2.Model.ToDoModel;
import com.example.timecraft2.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

public class TaskView extends AppCompatActivity {

    private ImageView imageView;
    private TextView taskTextView;
    private TextView dueDateTextView;
    private ToggleButton completedToggleButton;
    FirebaseFirestore firestore;
    String task,TaskId;
    private  Query query;
    ListenerRegistration listenerRegistration;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_view);


        imageView = findViewById(R.id.imageView);
        taskTextView = findViewById(R.id.taskTextView);
        dueDateTextView = findViewById(R.id.dueDateTextView);
        completedToggleButton = findViewById(R.id.completedToggleButton);
        firestore =FirebaseFirestore.getInstance();

        firestore.collection("task")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String taskId = document.getId();
                            // Now you have the document ID (taskId) for each task
                            // You can use this taskId to reference individual tasks
                        }
                    }
                });

    }
        }
        // Retrieve data from intent extras



