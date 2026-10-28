package com.example.midmad;

import android.database.Cursor;
import android.os.Bundle;

import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private EditText etStudentId, etStudentName;
    private Spinner spMajor;
    private Button btnAdd, btnEdit, btnDelete, btnView;
    private ListView lvStudents;

    private DatabaseHelper dbHelper;
    private ArrayList<Student> studentList;
    private ArrayAdapter<Student> studentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Views
        etStudentId = findViewById(R.id.etStudentId);
        etStudentName = findViewById(R.id.etStudentName);
        spMajor = findViewById(R.id.spMajor);
        btnAdd = findViewById(R.id.btnAdd);
        btnEdit = findViewById(R.id.btnEdit);
        btnDelete = findViewById(R.id.btnDelete);
        btnView = findViewById(R.id.btnView);
        lvStudents = findViewById(R.id.lvStudents);

        // Initialize Database Helper
        dbHelper = new DatabaseHelper(this);

        // Initialize Spinner with options: Informatics and Information Systems
        ArrayAdapter<CharSequence> majorAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.majors_array,
                android.R.layout.simple_spinner_item
        );
        majorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spMajor.setAdapter(majorAdapter);

        // Load records into ListView
        loadStudentData();

        // Button Listeners
        btnAdd.setOnClickListener(v -> addStudent());
        btnEdit.setOnClickListener(v -> editStudent());
        btnDelete.setOnClickListener(v -> deleteStudent());
        btnView.setOnClickListener(v -> viewStudent());

        // ListView item click to select student for easy editing/deleting
        lvStudents.setOnItemClickListener((parent, view, position, id) -> {
            Student selectedStudent = studentList.get(position);
            etStudentId.setText(selectedStudent.getId());
            etStudentName.setText(selectedStudent.getName());
            setSpinnerValue(selectedStudent.getMajor());
            Toast.makeText(MainActivity.this, "Selected: " + selectedStudent.getName(), Toast.LENGTH_SHORT).show();
        });
    }

    // CREATE operation
    private void addStudent() {
        String id = etStudentId.getText().toString().trim();
        String name = etStudentName.getText().toString().trim();
        String major = spMajor.getSelectedItem().toString();

        if (id.isEmpty() || name.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Student ID must be unique
        if (dbHelper.checkStudentExists(id)) {
            Toast.makeText(this, "Error: Student ID already exists!", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean success = dbHelper.insertStudent(id, name, major);
        if (success) {
            Toast.makeText(this, "Student added successfully!", Toast.LENGTH_SHORT).show();
            clearInputs();
            loadStudentData();
        } else {
            Toast.makeText(this, "Failed to add student!", Toast.LENGTH_SHORT).show();
        }
    }

    // UPDATE operation
    private void editStudent() {
        String id = etStudentId.getText().toString().trim();
        String name = etStudentName.getText().toString().trim();
        String major = spMajor.getSelectedItem().toString();

        if (id.isEmpty()) {
            Toast.makeText(this, "Please enter Student ID to edit!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter Student Name!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!dbHelper.checkStudentExists(id)) {
            Toast.makeText(this, "Error: Student ID not found!", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean success = dbHelper.updateStudent(id, name, major);
        if (success) {
            Toast.makeText(this, "Student updated successfully!", Toast.LENGTH_SHORT).show();
            clearInputs();
            loadStudentData();
        } else {
            Toast.makeText(this, "Failed to update student!", Toast.LENGTH_SHORT).show();
        }
    }

    // DELETE operation
    private void deleteStudent() {
        String id = etStudentId.getText().toString().trim();

        if (id.isEmpty()) {
            Toast.makeText(this, "Please enter Student ID to delete!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!dbHelper.checkStudentExists(id)) {
            Toast.makeText(this, "Error: Student ID not found!", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean success = dbHelper.deleteStudent(id);
        if (success) {
            Toast.makeText(this, "Student deleted successfully!", Toast.LENGTH_SHORT).show();
            clearInputs();
            loadStudentData();
        } else {
            Toast.makeText(this, "Failed to delete student!", Toast.LENGTH_SHORT).show();
        }
    }

    // READ operation / Search or View All
    private void viewStudent() {
        String id = etStudentId.getText().toString().trim();

        if (!id.isEmpty()) {
            // Search specific student by ID
            Cursor cursor = dbHelper.getStudentById(id);
            if (cursor != null && cursor.moveToFirst()) {
                String name = cursor.getString(1);
                String major = cursor.getString(2);
                cursor.close();

                etStudentName.setText(name);
                setSpinnerValue(major);
                Toast.makeText(this, "Student record loaded!", Toast.LENGTH_SHORT).show();
            } else {
                if (cursor != null) cursor.close();
                Toast.makeText(this, "No student found with ID: " + id, Toast.LENGTH_SHORT).show();
            }
        } else {
            // Refresh and display all records
            loadStudentData();
            if (studentList.isEmpty()) {
                Toast.makeText(this, "No student records found in database.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Displaying all student records.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Load data from SQLite and update ListView
    private void loadStudentData() {
        studentList = dbHelper.getAllStudents();
        studentAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, studentList);
        lvStudents.setAdapter(studentAdapter);
    }

    // Helper to select major in Spinner
    private void setSpinnerValue(String major) {
        if (major.equalsIgnoreCase("Informatics")) {
            spMajor.setSelection(0);
        } else if (major.equalsIgnoreCase("Information Systems")) {
            spMajor.setSelection(1);
        }
    }

    // Clear input fields
    private void clearInputs() {
        etStudentId.setText("");
        etStudentName.setText("");
        spMajor.setSelection(0);
    }
}
