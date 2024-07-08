package com.example.timecraft2;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;

public class AddNewTask extends DialogFragment {
    public static final String TAG = "AddNewTask";
    private TextView setDueDate;
    private TextView setDueTime;
    private EditText mTaskEdit;
    private Button mSave;
    private FirebaseFirestore firestore;
    private Context context;
    private String dueDate;
    private String dueTime;
    private String id="";
    private String dueDateUpdate="";


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.add_new_task, container, false);

        setDueTime = rootView.findViewById(R.id.set_time);
        setDueTime.setOnClickListener(v -> showTimePickerDialog());
        return rootView;
    }

    public static AddNewTask newInstance(){
        return new AddNewTask();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setDueTime = view.findViewById(R.id.set_time);
        setDueDate = view.findViewById(R.id.set_due);
        mTaskEdit = view.findViewById(R.id.task_edit);
        mSave = view.findViewById(R.id.save);

        firestore = FirebaseFirestore.getInstance();

        boolean isUpdate = false;

        final Bundle bundle = getArguments();
        if(bundle != null) {
            isUpdate = true;
            String task = bundle.getString("task");
            id = bundle.getString("id");
            dueDateUpdate = bundle.getString("due");
            String[] dueParts = dueDateUpdate.split(" ");
            if (dueParts.length >= 2) {
                dueDate = dueParts[0];
                dueTime = dueParts[1];
            }
            mTaskEdit.setText(task);
            setDueDate.setText(dueDateUpdate);
            setDueTime.setText(dueTime);
            if(task.length() > 0 && dueDate != null && dueTime != null) {
                mSave.setEnabled(false);
                mSave.setBackgroundColor(Color.GRAY);
                mSave.setTextColor(Color.WHITE);
            }
        }

        mTaskEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().equals("")) {
                    mSave.setEnabled(false);
                    mSave.setTextColor(Color.WHITE);
                    mSave.setBackgroundColor(Color.GRAY);
                } else {
                    mSave.setEnabled(true);
                    mSave.setTextColor(Color.BLACK);
                    mSave.setBackgroundColor(Color.BLUE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        setDueDate.setOnClickListener(v -> showDatePickerDialog());
        setDueTime.setOnClickListener(v -> showTimePickerDialog());
        boolean finalIsUpdate = isUpdate;
        mSave.setOnClickListener(v -> {
            String task = mTaskEdit.getText().toString();
            String updatedDueDateTime;
            if(finalIsUpdate) {
                updatedDueDateTime = dueDate + " " + dueTime; // Default to existing due date and time
                if (dueDate != null && !dueDate.isEmpty() && dueTime != null && !dueTime.isEmpty()) {
                    updatedDueDateTime = dueDate + " " + dueTime; // Use edited due date and time if available
                } else if (dueDate != null && !dueDate.isEmpty() && dueTime == null) {
                    // Use existing due time if only due date is updated
                    String[] existingDueDateTimeParts = dueDateUpdate.split(" ");
                    updatedDueDateTime = dueDate + " " + existingDueDateTimeParts[1];
                } else if (dueDate == null && dueTime != null && !dueTime.isEmpty()) {
                    // Use existing due date if only due time is updated
                    String[] existingDueDateTimeParts = dueDateUpdate.split(" ");
                    updatedDueDateTime = existingDueDateTimeParts[0] + " " + dueTime;
                }

                firestore.collection("task").document(id).update("task", task, "due", dueDate + " " + dueTime);
                Toast.makeText(context, "Task Updated", Toast.LENGTH_SHORT).show();
                dismiss();
            } else {
                if(task.isEmpty() || dueDate == null || dueTime == null) {
                    Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    saveTaskToFirestore(task, dueDate, dueTime);
                }
            }
        });
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int YEAR = calendar.get(Calendar.YEAR);
        int MONTH = calendar.get(Calendar.MONTH);
        int DAY = calendar.get(Calendar.DATE);

        DatePickerDialog datePickerDialog = new DatePickerDialog(context, (view, year, month, dayOfMonth) -> {
            month = month + 1;
            setDueDate.setText(dayOfMonth + "/" + month + "/" + year);
            dueDate = dayOfMonth + "/" + month + "/" + year;
        }, YEAR, MONTH, DAY);
        datePickerDialog.show();
    }
    private void showTimePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int HOUR = calendar.get(Calendar.HOUR_OF_DAY); // Use HOUR_OF_DAY for 24-hour format
        int MINUTE = calendar.get(Calendar.MINUTE);
        boolean is24HourFormat = true; // Set to true for 24-hour format

        TimePickerDialog timePickerDialog = new TimePickerDialog(context, (view, hourOfDay, minute) -> {
            setDueTime(hourOfDay, minute); // Assuming setDueTime is a method to set due time
        }, HOUR, MINUTE, is24HourFormat);
        timePickerDialog.show();
    }

    private void setDueTime(int hourOfDay, int minute) {
        String formattedHour = String.format("%02d", hourOfDay);
        String formattedMinute = String.format("%02d", minute);
        dueTime = formattedHour + ":" + formattedMinute; // Assign the value to the dueTime variable
        setDueTime.setText(dueTime); // Update the text of the TextView if needed
        // Save the dueTime value if needed
    }

    private void saveTaskToFirestore(String taskText, String dueDate, String dueTime) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String dueDateTime = dueDate + " " + dueTime; // Combine dueDate and dueTime
        HashMap<String, Object> taskMap = new HashMap<>();
        taskMap.put("task", taskText);
        taskMap.put("due", dueDateTime); // Store the combined dueDateTime
        taskMap.put("status", 0);

        taskMap.put("userId", userId);

        firestore.collection("task").add(taskMap)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(context, "Task Saved", Toast.LENGTH_SHORT).show();
                    dismiss();
                })
                .addOnFailureListener(e -> Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        Activity activity = getActivity();
        if(activity instanceof  OnDialogCloseListner) {
            ((OnDialogCloseListner)activity).onDialogClose(dialog);
        }
    }
}