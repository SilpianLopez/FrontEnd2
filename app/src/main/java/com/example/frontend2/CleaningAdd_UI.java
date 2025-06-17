package com.example.frontend2;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.frontend2.api.ApiClient;
import com.example.frontend2.api.CleaningRoutineApi;
import com.example.frontend2.models.CleaningRoutine;
import com.example.frontend2.models.RoutineRequest;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CleaningAdd_UI extends AppCompatActivity {

    private static final String TAG = "CleaningAdd_UI";
    private static final String PREFS_NAME = "CleanItAppPrefs";
    private static final String KEY_USER_ID = "logged_in_user_id";

    private Toolbar toolbar;
    private EditText etTitle, etDescription, etStartDate;
    private Spinner spRepeatUnit, spRepeatInterval;
    private Button btnSave;

    private int currentLocalUserId = -1;
    private int spaceIdToSave = -1;
    private boolean isEditMode = false;
    private int routineIdToEdit = -1;
    private String firstDueDateString = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cleaning_add_ui);

        toolbar = findViewById(R.id.toolbar_cadd);
        setSupportActionBar(toolbar);

        etTitle = findViewById(R.id.et_name);
        etDescription = findViewById(R.id.et_comment);
        etStartDate = findViewById(R.id.et_start_date);
        spRepeatUnit = findViewById(R.id.sp_unit);
        spRepeatInterval = findViewById(R.id.sp_value);
        btnSave = findViewById(R.id.btn_save);

        loadUserId();
        receiveIntentData();
        initToolbarTitle();
        initRepeatSpinners();
        initStartDatePicker();

        btnSave.setOnClickListener(v -> saveOrUpdateRoutine());
    }

    private void loadUserId() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        currentLocalUserId = prefs.getInt(KEY_USER_ID, -1);
        if (currentLocalUserId == -1) {
            Toast.makeText(this, "사용자 정보가 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void receiveIntentData() {
        Intent intent = getIntent();
        spaceIdToSave = intent.getIntExtra("spaceId", -1);
        boolean isFromAi = intent.getBooleanExtra("isFromAiRecommendation", false);
        if (isFromAi) {
            spaceIdToSave = intent.getIntExtra("preselected_space_id", -1);
        }

        String mode = intent.getStringExtra("mode");
        if ("edit".equals(mode)) {
            isEditMode = true;
            routineIdToEdit = intent.getIntExtra("routineIdToEdit", -1);
            loadRoutineDataForEdit(intent);
        } else if (isFromAi) {
            populateUiWithAiRecommendation(intent);
        }
    }

    private void initToolbarTitle() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(isEditMode ? "루틴 수정" : "새 루틴 추가");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initRepeatSpinners() {
        // ✅ 반복 주기에 "기타" 추가
        String[] repeatUnitsDisplay = {"반복 안함", "매일", "매주", "매월", "매년", "기타"};
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, repeatUnitsDisplay);
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spRepeatUnit.setAdapter(unitAdapter);

        String[] intervalItems = new String[30];
        for (int i = 0; i < 30; i++) intervalItems[i] = String.valueOf(i + 1);
        ArrayAdapter<String> intervalAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, intervalItems);
        intervalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spRepeatInterval.setAdapter(intervalAdapter);

        // ✅ 기타(인덱스 5) 선택 시에만 간격 활성화
        spRepeatUnit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                spRepeatInterval.setEnabled(position == 5);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { spRepeatInterval.setEnabled(false); }
        });

        spRepeatUnit.setSelection(0);
        spRepeatInterval.setEnabled(false);
        spRepeatInterval.setSelection(0);
    }

    private void initStartDatePicker() {
        etStartDate.setFocusable(false);
        etStartDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePicker = new DatePickerDialog(this, (view, y, m, d) -> {
                Calendar selectedCal = Calendar.getInstance();
                selectedCal.set(y, m, d);
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                firstDueDateString = format.format(selectedCal.getTime());
                etStartDate.setText(firstDueDateString);
            }, year, month, day);
            datePicker.show();
        });
    }

    private void populateUiWithAiRecommendation(Intent intent) {
        etTitle.setText(intent.getStringExtra("suggestedTitle"));
        etDescription.setText(intent.getStringExtra("suggestedDescription"));
    }

    private void loadRoutineDataForEdit(Intent intent) {
        etTitle.setText(intent.getStringExtra("currentTitle"));
        etDescription.setText(intent.getStringExtra("currentDescription"));
        etStartDate.setText(intent.getStringExtra("currentFirstDueDate"));
        firstDueDateString = intent.getStringExtra("currentFirstDueDate");

        String currentRepeatUnit = intent.getStringExtra("currentRepeatUnit");
        int unitIndex = 0;
        String[] repeatUnitsApi = {"NONE","DAY","WEEK","MONTH","YEAR","OTHER"};
        for (int i=0;i<repeatUnitsApi.length;i++) {
            if (repeatUnitsApi[i].equals(currentRepeatUnit)) { unitIndex = i; break; }
        }
        spRepeatUnit.setSelection(unitIndex);

        int currentInterval = intent.getIntExtra("currentRepeatInterval", 1);
        spRepeatInterval.setSelection(Math.max(0, currentInterval - 1));
        spRepeatInterval.setEnabled(unitIndex == 5);
    }

    private void saveOrUpdateRoutine() {
        String title = etTitle.getText().toString().trim();
        if (TextUtils.isEmpty(title)) {
            Toast.makeText(this, "루틴 제목을 입력하세요.", Toast.LENGTH_SHORT).show(); return;
        }
        if (spaceIdToSave <= 0) {
            Toast.makeText(this, "저장할 공간 정보가 없습니다.", Toast.LENGTH_SHORT).show(); return;
        }

        // ✅ 기타 포함하여 API 매핑
        String[] repeatUnitsApi = {"NONE","DAY","WEEK","MONTH","YEAR","OTHER"};
        String repeatUnitApi = repeatUnitsApi[spRepeatUnit.getSelectedItemPosition()];
        Integer repeatInterval = null;

        if (repeatUnitApi.equals("OTHER")) {
            repeatInterval = Integer.parseInt(spRepeatInterval.getSelectedItem().toString());
        } else if (!repeatUnitApi.equals("NONE")) {
            repeatInterval = 1;  // 🔥 핵심 추가 부분: 기본 반복은 1로 세팅
        }

        RoutineRequest dto = new RoutineRequest(
                spaceIdToSave, currentLocalUserId, title, etDescription.getText().toString(),
                repeatUnitApi, repeatInterval, firstDueDateString
        );

        CleaningRoutineApi api = ApiClient.getClient().create(CleaningRoutineApi.class);
        Call<CleaningRoutine> call = (isEditMode && routineIdToEdit > 0)
                ? api.updateRoutine(routineIdToEdit, dto)
                : api.createRoutine(dto);

        call.enqueue(new Callback<CleaningRoutine>() {
            @Override public void onResponse(@NonNull Call<CleaningRoutine> call, @NonNull Response<CleaningRoutine> resp) {
                if (resp.isSuccessful()) {
                    Toast.makeText(CleaningAdd_UI.this, "루틴 저장 완료!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(CleaningAdd_UI.this, "저장 실패: " + resp.code(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "서버 에러: " + resp.code());
                }
            }
            @Override public void onFailure(@NonNull Call<CleaningRoutine> call, @NonNull Throwable t) {
                Toast.makeText(CleaningAdd_UI.this, "통신 오류: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override public boolean onSupportNavigateUp() { setResult(RESULT_CANCELED); finish(); return true; }
}
