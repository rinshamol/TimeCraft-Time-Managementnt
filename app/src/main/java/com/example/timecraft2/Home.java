package com.example.timecraft2;





import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.timecraft2.Adapter.ToDoAdapter;
import com.example.timecraft2.Model.ToDoModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class Home extends AppCompatActivity implements OnDialogCloseListner {
    private static final int REQUEST_CODE_NOTIFICATION_PERMISSION = 123;
    private RecyclerView recyclerView;
    private FloatingActionButton mfab;
    private FirebaseFirestore firestore;
    private ToDoAdapter adapter;
    private List<ToDoModel> mList;
    private  Query query;
    ListenerRegistration listenerRegistration;
    FirebaseAuth auth;
    StorageReference storageReference;
    Button logoutbtn;
    String taskName,documentId,imageUrl,duedate;
    Home home;

    FirebaseUser user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        recyclerView=findViewById(R.id.recycularview);
        mfab=findViewById(R.id.floatingActionButton);
        firestore=FirebaseFirestore.getInstance();
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(Home.this));

        mList=new ArrayList<>();
        adapter=new ToDoAdapter(Home.this,mList);
        ItemTouchHelper itemTouchHelper=new ItemTouchHelper(new TouchHelper(adapter));
        itemTouchHelper.attachToRecyclerView(recyclerView);
        recyclerView.setAdapter(adapter);
        showData();
        mfab.setOnClickListener(v -> AddNewTask.newInstance().show(getSupportFragmentManager(),AddNewTask.TAG));
        auth=FirebaseAuth.getInstance();
        logoutbtn=findViewById(R.id.logout);

        user=auth.getCurrentUser();
        if(user==null){
            Intent i= new Intent(Home.this, Login.class);
            startActivity(i);
            finish();
        }


        logoutbtn.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent i= new Intent(Home.this, Login.class);
            startActivity(i);
            finish();
        });

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_NOTIFICATION_PERMISSION) {
            // Check if the permission is granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted, you can proceed to display the notification
                // Notify the adapter to update the notifications
                adapter.notifyDataSetChanged();
            } else {
                // Permission is denied, handle accordingly (e.g., show a message to the user)
                Toast.makeText(Home.this, "Notification permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
    public void viewTask(View v) {


    }

    private void showData() {
        // Get the UID of the currently authenticated user
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Query Firestore to retrieve tasks associated with the current user's UID
        query=firestore.collection("task").whereEqualTo("userId", userId);
                listenerRegistration=query.addSnapshotListener((value, error) -> {
                    if (error != null) {
                        // Handle errors
                        Toast.makeText(Home.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    mList.clear(); // Clear the existing list before adding new data
                    for (DocumentChange documentChange : value.getDocumentChanges()) {
                        if (documentChange.getType() == DocumentChange.Type.ADDED) {
                            String id = documentChange.getDocument().getId();
                            ToDoModel toDoModel = documentChange.getDocument().toObject(ToDoModel.class).withId(id);
                            mList.add(toDoModel);

                        }
                    }
                    adapter.notifyDataSetChanged();

                    // Notify the adapter that the dataset has changed
                    // Reverse the list to display most recent tasks first
                    listenerRegistration.remove();
                });
    }



    @Override
    public void onDialogClose(DialogInterface dialogInterface) {
        mList.clear();
        showData();
        adapter.notifyDataSetChanged();
    }



}
