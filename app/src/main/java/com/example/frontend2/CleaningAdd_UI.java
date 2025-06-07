package com.example.frontend2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.frontend2.api.ApiClient;
import com.example.frontend2.api.CleaningRoutineApi;
import com.example.frontend2.CleaningList;
import com.example.frontend2.models.CleaningRoutine;
import com.example.frontend2.models.RoutineRequest;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CleaningAdd_UI extends AppCompatActivity {
    private Toolbar toolbar;
    private Spinner spUnit, spValue;
    private Button btnSave;
    private EditText etTitle, etDescription;

    private boolean isEditMode = false;
    private int editingRoutineId = -1;
    private int spaceId, userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cleaning_add_ui);

        // Toolbar
        toolbar = findViewById(R.id.toolbar_cadd);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Views
        etTitle = findViewById(R.id.et_title);
        etDescription = findViewById(R.id.et_description);
        spUnit = findViewById(R.id.sp_unit);
        spValue = findViewById(R.id.sp_value);
        btnSave = findViewById(R.id.btn_save);

        // Spinner setup
        String[] unitItems = {"매일", "매주", "매달"};
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, unitItems);
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spUnit.setAdapter(unitAdapter);

        String[] valueItems = new String[10];
        for (int i = 0; i < 10; i++) {
            valueItems[i] = String.valueOf(i + 1);
        }
        ArrayAdapter<String> valueAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, valueItems);
        valueAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spValue.setAdapter(valueAdapter);

        // Intent data
        Intent intent = getIntent();
        spaceId = intent.getIntExtra("space_id", -1);
        userId = getSharedPreferences("CleanItPrefs", MODE_PRIVATE).getInt("user_id", -1);

        String mode = intent.getStringExtra("mode");
        if ("edit".equals(mode)) {
            isEditMode = true;
            editingRoutineId = intent.getIntExtra("routine_id", -1);
            getSupportActionBar().setTitle("청소 항목 수정");
            btnSave.setText("수정");
            // Prefill fields
            etTitle.setText(intent.getStringExtra("title"));
            etDescription.setText(intent.getStringExtra("comment"));
            spUnit.setSelection(getSpinnerIndex(unitItems, intent.getStringExtra("cycle")));
            spValue.setSelection(valueAdapter.getPosition(extractNumber(intent.getStringExtra("cycle"))));
        } else {
            getSupportActionBar().setTitle("청소 항목 추가");
            btnSave.setText("추가");
        }

        // Save button
        btnSave.setOnClickListener(v -> saveRoutine());
    }

    private void saveRoutine() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String repeatUnit = spUnit.getSelectedItem().toString();
        int repeatInterval = Integer.parseInt(spValue.getSelectedItem().toString());
        String firstDueDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        if (title.isEmpty()) {
            Toast.makeText(this, "제목을 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        RoutineRequest request = new RoutineRequest(
                spaceId, userId, title, description,
                repeatUnit, repeatInterval, firstDueDate
        );

        CleaningRoutineApi api = ApiClient.getClient().create(CleaningRoutineApi.class);
        if (isEditMode && editingRoutineId != -1) {
            api.updateRoutine(editingRoutineId, request)
                    .enqueue(new Callback<CleaningRoutine>() {
                        @Override
                        public void onResponse(Call<CleaningRoutine> call, Response<CleaningRoutine> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(CleaningAdd_UI.this, "수정되었습니다.", Toast.LENGTH_SHORT).show();
                                setResult(RESULT_OK);
                                finish();
                            } else {
                                Toast.makeText(CleaningAdd_UI.this, "수정 실패: " + response.code(), Toast.LENGTH_SHORT).show();
                            }
                        }
                        @Override
                        public void onFailure(Call<CleaningRoutine> call, Throwable t) {
                            Log.e("ROUTINE_API", "수정 오류", t);
                            Toast.makeText(CleaningAdd_UI.this, "통신 오류", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            api.createRoutine(request)
                    .enqueue(new Callback<CleaningList>() {
                        @Override
                        public void onResponse(Call<CleaningList> call, Response<CleaningList> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(CleaningAdd_UI.this, "추가 성공", Toast.LENGTH_SHORT).show();
                                setResult(RESULT_OK);
                                finish();
                            } else {
                                Toast.makeText(CleaningAdd_UI.this, "추가 실패: " + response.code(), Toast.LENGTH_SHORT).show();
                            }
                        }
                        @Override
                        public void onFailure(Call<CleaningList> call, Throwable t) {
                            Log.e("ROUTINE_API", "추가 오류", t);
                            Toast.makeText(CleaningAdd_UI.this, "통신 오류", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private int getSpinnerIndex(String[] items, String value) {
        for (int i = 0; i < items.length; i++) {
            if (value != null && value.startsWith(items[i])) return i;
        }
        return 0;
    }

    private String extractNumber(String cycle) {
        if (cycle == null) return "1";
        // "2일" -> "2"
        return cycle.replaceAll("\\D+", "");
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
