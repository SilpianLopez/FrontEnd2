package com.example.frontend2;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.frontend2.api.CleaningRoutineApi;
import com.example.frontend2.api.ApiClient;
import com.example.frontend2.models.CleaningRoutine;
import com.example.frontend2.models.RoutineRequest;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CleaningAdd_UI extends AppCompatActivity {
    private static final String TAG = "CleaningAdd_UI";
    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_USER_ID = "current_user_id";

    private Toolbar toolbar;
    private EditText etTitle, etDescription;
    private Spinner spRepeatUnit, spRepeatInterval;
    private Button btnSave;
    private ImageView calendarIcon;


    private int currentLocalUserId = -1;
    private int spaceIdToSave = -1;
    private boolean isEditMode = false;
    private int routineIdToEdit = -1;

    private String firstDueDateString = null;  // yyyy-MM-dd 형식으로 저장


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cleaning_add_ui);

        toolbar = findViewById(R.id.toolbar_cadd);
        setSupportActionBar(toolbar);

        etTitle = findViewById(R.id.et_name);
        etDescription = findViewById(R.id.et_comment);
        spRepeatUnit = findViewById(R.id.sp_unit);
        spRepeatInterval = findViewById(R.id.sp_value);
        btnSave = findViewById(R.id.btn_save);

        Intent intent = getIntent();
        currentLocalUserId = intent.getIntExtra("userId", -1);
        if (currentLocalUserId == -1) {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            currentLocalUserId = prefs.getInt(KEY_USER_ID, -1);
        }
        if (currentLocalUserId == -1) {
            Toast.makeText(this, "사용자 정보가 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        spaceIdToSave = intent.getIntExtra("spaceId", -1);
        boolean isFromAiRecommendation = intent.getBooleanExtra("isFromAiRecommendation", false);
        if (isFromAiRecommendation) {
            spaceIdToSave = intent.getIntExtra("preselected_space_id", -1);
        }

        String mode = intent.getStringExtra("mode");
        if ("edit".equals(mode)) {
            isEditMode = true;
            routineIdToEdit = intent.getIntExtra("routineIdToEdit", -1);
            if (getSupportActionBar() != null) getSupportActionBar().setTitle("루틴 수정");
            Log.d(TAG, "수정 모드, routineId: " + routineIdToEdit + ", spaceId: " + spaceIdToSave);
            loadRoutineDataForEdit(intent);
        } else if (isFromAiRecommendation) {
            if (getSupportActionBar() != null) getSupportActionBar().setTitle("AI 추천 루틴 반영");
            Log.d(TAG, "AI 추천 기반 생성 모드, spaceId: " + spaceIdToSave);
            populateUiWithAiRecommendation(intent);
        } else {
            if (getSupportActionBar() != null) getSupportActionBar().setTitle("새 루틴 추가");
            Log.d(TAG, "일반 생성 모드, spaceId: " + spaceIdToSave);
            if (spaceIdToSave <= 0) {
                Toast.makeText(this, "저장할 공간 정보가 없습니다.", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
        }
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String[] repeatUnitsDisplay = {"반복 안함", "매일", "매주", "매월", "매년"};
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, repeatUnitsDisplay);
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spRepeatUnit.setAdapter(unitAdapter);

        String[] intervalItems = new String[30];
        for (int i = 0; i < 30; i++) intervalItems[i] = String.valueOf(i + 1);
        ArrayAdapter<String> intervalAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, intervalItems);
        intervalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spRepeatInterval.setAdapter(intervalAdapter);

        if (!isEditMode) {
            spRepeatUnit.setSelection(0);
            spRepeatInterval.setEnabled(false);
            spRepeatInterval.setSelection(0);
        }

        spRepeatUnit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                spRepeatInterval.setEnabled(position != 0);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {
                spRepeatInterval.setEnabled(false);
            }
        });

        calendarIcon = findViewById(R.id.calendarIcon);  // XML에 calendarIcon 있어야 함
        calendarIcon.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePicker = new DatePickerDialog(
                    CleaningAdd_UI.this,
                    (view, selectedYear, selectedMonth, selectedDayOfMonth) -> {
                        Calendar selectedCal = Calendar.getInstance();
                        selectedCal.set(selectedYear, selectedMonth, selectedDayOfMonth);

                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        firstDueDateString = format.format(selectedCal.getTime());

                        Toast.makeText(CleaningAdd_UI.this, "선택된 날짜: " + firstDueDateString, Toast.LENGTH_SHORT).show();
                    },
                    year, month, day
            );

            datePicker.setOnCancelListener(dialog -> {
                // 취소 시 아무 처리 안 함 (달력만 닫힘)
            });

            datePicker.show();
        });


        btnSave.setOnClickListener(v -> saveOrUpdateRoutine());
    }

    @Override
    public boolean onSupportNavigateUp() {
        setResult(RESULT_CANCELED);
        finish();
        return true;
    }

    private void populateUiWithAiRecommendation(Intent intent) {
        String suggestedTitle = intent.getStringExtra("suggestedTitle");
        String suggestedDescription = intent.getStringExtra("suggestedDescription");
        if (suggestedTitle != null) etTitle.setText(suggestedTitle);
        if (suggestedDescription != null) etDescription.setText(suggestedDescription);

        spRepeatUnit.setSelection(0);
        spRepeatInterval.setEnabled(false);
        spRepeatInterval.setSelection(0);
    }

    private void loadRoutineDataForEdit(Intent intent) {
        etTitle.setText(intent.getStringExtra("currentTitle"));
        etDescription.setText(intent.getStringExtra("currentDescription"));
        String currentRepeatUnit = intent.getStringExtra("currentRepeatUnit");
        int unitIndex = 0;
        String[] repeatUnitsApi = {"NONE","DAY","WEEK","MONTH","YEAR"};
        for (int i=0;i<repeatUnitsApi.length;i++) {
            if (repeatUnitsApi[i].equals(currentRepeatUnit)) { unitIndex = i; break; }
        }
        spRepeatUnit.setSelection(unitIndex);
        int intervalIndex = 0;
        if (unitIndex != 0 && intent.hasExtra("currentRepeatInterval")) {
            intervalIndex = intent.getIntExtra("currentRepeatInterval", 1) - 1;
            if (intervalIndex < 0 || intervalIndex >= spRepeatInterval.getCount()) intervalIndex = 0;
        }
        spRepeatInterval.setSelection(intervalIndex);
        spRepeatInterval.setEnabled(unitIndex != 0);
    }

    private void saveOrUpdateRoutine() {
        String title = etTitle.getText().toString().trim();
        if (TextUtils.isEmpty(title)) {
            Toast.makeText(this, "루틴 제목을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (spaceIdToSave <= 0) {
            Toast.makeText(this, "저장할 공간 정보가 없습니다.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "Invalid spaceId: " + spaceIdToSave);
            return;
        }
        String[] repeatUnitsApi = {"NONE","DAY","WEEK","MONTH","YEAR"};
        String repeatUnitApi = repeatUnitsApi[spRepeatUnit.getSelectedItemPosition()];
        Integer repeatInterval = null;
        if (!repeatUnitApi.equals("NONE")) {
            try {
                int val = Integer.parseInt(spRepeatInterval.getSelectedItem().toString());
                if (val > 0) repeatInterval = val;
                else { Toast.makeText(this, "간격은 1 이상이어야 합니다.", Toast.LENGTH_SHORT).show(); return; }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "유효한 간격을 입력하세요.", Toast.LENGTH_SHORT).show(); return;
            }
        }
        String firstDue = firstDueDateString;
        RoutineRequest dto = new RoutineRequest(
                spaceIdToSave,
                currentLocalUserId,
                title,
                etDescription.getText().toString(),
                repeatUnitApi,
                repeatInterval,
                firstDue
        );
        CleaningRoutineApi api = ApiClient.getClient().create(CleaningRoutineApi.class);
        Call<CleaningRoutine> call;
        if (isEditMode && routineIdToEdit > 0) {
            call = api.updateRoutine(routineIdToEdit, dto);
        } else {
            call = api.createRoutine(dto);
        }
        Toast.makeText(this, "저장 중...", Toast.LENGTH_SHORT).show();
        call.enqueue(new Callback<CleaningRoutine>() {
            @Override public void onResponse(@NonNull Call<CleaningRoutine> call, @NonNull Response<CleaningRoutine> resp) {
                if (resp.isSuccessful() && resp.body() != null) {
                    String act = isEditMode ? "수정" : "추가";
                    Toast.makeText(CleaningAdd_UI.this, "루틴이 성공적으로 " + act + "되었습니다!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    String msg = (isEditMode ? "수정" : "추가") + " 실패 (코드:" + resp.code() + ")";
                    Log.e(TAG, msg);
                    Toast.makeText(CleaningAdd_UI.this, msg, Toast.LENGTH_LONG).show();
                    try {
                        if (resp.errorBody() != null) Log.e(TAG, "Error body:" + resp.errorBody().string());
                    } catch (IOException e) { Log.e(TAG, "Error parsing errorBody", e); }
                }
            }
            @Override public void onFailure(@NonNull Call<CleaningRoutine> call, @NonNull Throwable t) {
                String msg = (isEditMode ? "수정" : "추가") + " 통신 오류: " + t.getMessage();
                Log.e(TAG, msg, t);
                Toast.makeText(CleaningAdd_UI.this, msg, Toast.LENGTH_LONG).show();
            }
        });
    }
}