package com.example.frontend2;

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
import android.widget.Spinner;
import android.widget.Toast;
// import android.widget.TextView; // 공간 이름 표시용 TextView가 XML에 없다면 필요 없음

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.frontend2.api.ApiClient;
import com.example.frontend2.api.CleaningRoutineApi;
// SpaceApi, Space 모델은 이 Activity에서 공간 목록을 직접 가져오지 않으므로 제거 가능 (단, spaceId는 받아야 함)
import com.example.frontend2.models.CleaningRoutine;
import com.example.frontend2.models.RoutineRequest;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CleaningAdd_UI extends AppCompatActivity {

    private static final String TAG = "CleaningAdd_UI";
    private Toolbar toolbar;
    private EditText etTitle, etDescription;
    // private Spinner spSpace; // XML에 공간 선택 Spinner가 없으므로 이 변수 및 관련 로직 제거
    private Spinner spRepeatUnit, spRepeatInterval;
    private Button btnSave;

    private int currentLocalUserId = -1;
    private int spaceIdToSave = -1; // 루틴을 저장/수정할 공간 ID (Intent로 받아옴)

    private boolean isEditMode = false;
    private int routineIdToEdit = -1;

    public static final String PREFS_NAME = "UserPrefs";
    public static final String KEY_USER_ID = "current_user_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cleaning_add_ui); // cleaning_add_ui.xml (공간 Spinner 없음)

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
            finish(); return;
        }

        String mode = intent.getStringExtra("mode");
        boolean isFromAiRecommendation = intent.getBooleanExtra("isFromAiRecommendation", false);

        // spaceIdToSave 설정: AI추천/수정/일반추가 모드에 따라 Intent에서 가져옴
        if (isFromAiRecommendation) {
            spaceIdToSave = intent.getIntExtra("preselected_space_id", -1);
        } else { // 일반 추가 또는 수정 모드
            spaceIdToSave = intent.getIntExtra("spaceId", -1);
        }

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
        } else { // 일반 신규 생성 모드 (예: CleaningList_UI에서 + 버튼 클릭)
            if (getSupportActionBar() != null) getSupportActionBar().setTitle("새 루틴 추가");
            Log.d(TAG, "일반 생성 모드, spaceId: " + spaceIdToSave);
            // 일반 생성 시에도 spaceIdToSave는 이전 화면에서 전달받아야 함
            if (spaceIdToSave == -1) {
                Toast.makeText(this, "루틴을 추가할 공간 정보가 없습니다.", Toast.LENGTH_LONG).show();
                finish(); return;
            }
        }
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // 반복 단위 Spinner 설정
        String[] repeatUnitsDisplay = {"반복 안함", "매일", "매주", "매월", "매년"};
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, repeatUnitsDisplay);
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spRepeatUnit.setAdapter(unitAdapter);

        // 반복 간격 Spinner 설정
        String[] intervalItems = new String[30];
        for (int i = 0; i < 30; i++) { intervalItems[i] = String.valueOf(i + 1); }
        ArrayAdapter<String> intervalAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, intervalItems);
        intervalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spRepeatInterval.setAdapter(intervalAdapter);

        if (!isEditMode) { // 생성 모드 (AI 기반 또는 일반) 일 때 기본값 설정
            spRepeatUnit.setSelection(0); // "반복 안함"
            spRepeatInterval.setEnabled(false);
            spRepeatInterval.setSelection(0); // 간격 1
        }

        spRepeatUnit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                spRepeatInterval.setEnabled(!repeatUnitsDisplay[position].equals("반복 안함"));
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { spRepeatInterval.setEnabled(false); }
        });

        btnSave.setOnClickListener(v -> saveOrUpdateRoutine());
    }

    private void populateUiWithAiRecommendation(Intent intent) {
        String suggestedTitle = intent.getStringExtra("suggestedTitle");
        String suggestedDescription = intent.getStringExtra("suggestedDescription");
        // preselected_space_id는 이미 spaceIdToSave에 반영됨

        if (suggestedTitle != null) etTitle.setText(suggestedTitle);
        if (suggestedDescription != null) etDescription.setText(suggestedDescription);

        // AI 추천 기반일 경우, 반복 주기는 사용자가 직접 설정하도록 기본값("반복 안함") 유지
        spRepeatUnit.setSelection(0); // "반복 안함"
        spRepeatInterval.setEnabled(false);
        spRepeatInterval.setSelection(0); // 간격 1
    }

    private void loadRoutineDataForEdit(Intent intent) {
        // TODO: 실제로는 routineIdToEdit로 서버에서 최신 루틴 정보를 API 호출로 가져오는 것이 더 정확.
        etTitle.setText(intent.getStringExtra("currentTitle"));
        etDescription.setText(intent.getStringExtra("currentDescription"));
        // spaceIdToSave는 이미 onCreate에서 설정됨 (수정 시에는 보통 공간 변경 안 함)

        String currentRepeatUnit = intent.getStringExtra("currentRepeatUnit");
        Integer currentRepeatInterval = intent.hasExtra("currentRepeatInterval") ? intent.getIntExtra("currentRepeatInterval", 1) : null;

        String[] repeatUnitsApiValues = {"NONE", "DAY", "WEEK", "MONTH", "YEAR"};
        int unitSelection = 0;
        if (currentRepeatUnit != null) {
            for (int i = 0; i < repeatUnitsApiValues.length; i++) {
                if (currentRepeatUnit.equals(repeatUnitsApiValues[i])) {
                    unitSelection = i; break;
                }
            }
        }
        spRepeatUnit.setSelection(unitSelection);

        if (currentRepeatInterval != null && currentRepeatInterval > 0 && unitSelection != 0) {
            if (currentRepeatInterval <= spRepeatInterval.getCount()) {
                spRepeatInterval.setSelection(currentRepeatInterval - 1);
            } else { spRepeatInterval.setSelection(0); }
            spRepeatInterval.setEnabled(true);
        } else {
            spRepeatInterval.setSelection(0);
            spRepeatInterval.setEnabled(false);
        }
        // TODO: first_due_date도 있다면 로드하여 DatePicker 등에 설정
    }

    private void saveOrUpdateRoutine() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            Toast.makeText(this, "루틴 제목을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        // spaceIdToSave는 onCreate에서 이미 설정됨
        if (spaceIdToSave <= 0) {
            Toast.makeText(this, "루틴을 저장할 유효한 공간 정보가 없습니다. 이전 화면에서 공간을 선택하거나 추가해주세요.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "Invalid spaceIdToSave: " + spaceIdToSave + " during save/update attempt.");
            return;
        }

        String[] repeatUnitsApiValues = {"NONE", "DAY", "WEEK", "MONTH", "YEAR"};
        String repeatUnitForApi = repeatUnitsApiValues[spRepeatUnit.getSelectedItemPosition()];

        Integer finalRepeatInterval = null;
        if (!repeatUnitForApi.equals("NONE")) {
            String intervalStr = spRepeatInterval.getSelectedItem().toString();
            try {
                int interval = Integer.parseInt(intervalStr);
                if (interval > 0) {
                    finalRepeatInterval = interval;
                } else { Toast.makeText(this, "반복 간격은 1 이상이어야 합니다.", Toast.LENGTH_SHORT).show(); return; }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "유효한 반복 간격을 입력해주세요.", Toast.LENGTH_SHORT).show(); return;
            }
        }

        String firstDueDate = null;
        if (!repeatUnitForApi.equals("NONE")) {
            // TODO: 사용자가 DatePicker 등으로 첫 시작일을 선택했다면 그 값을 사용
            // SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            // firstDueDate = sdf.format(new Date()); // 임시로 오늘 날짜
        }

        RoutineRequest requestDto = new RoutineRequest(
                spaceIdToSave, currentLocalUserId, title, description,
                repeatUnitForApi, finalRepeatInterval, firstDueDate
        );

        CleaningRoutineApi api = ApiClient.getClient().create(CleaningRoutineApi.class);
        Call<CleaningRoutine> call;

        if (isEditMode && routineIdToEdit != -1) {
            Log.d(TAG, "루틴 수정 요청 API 호출: ID " + routineIdToEdit);
            call = api.updateRoutine(routineIdToEdit, requestDto);
        } else {
            Log.d(TAG, "새 루틴 생성 API 호출 (AI 반영 또는 일반 추가)");
            call = api.createRoutine(requestDto);
        }

        Toast.makeText(this, "저장 중...", Toast.LENGTH_SHORT).show();

        call.enqueue(new Callback<CleaningRoutine>() {
            @Override
            public void onResponse(@NonNull Call<CleaningRoutine> call, @NonNull Response<CleaningRoutine> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String action = isEditMode ? "수정" : "추가";
                    Toast.makeText(CleaningAdd_UI.this, "루틴이 성공적으로 " + action + "되었습니다!", Toast.LENGTH_SHORT).show();
                    Intent resultIntent = new Intent();
                    setResult(RESULT_OK, resultIntent);
                    finish();
                } else {
                    handleApiError(response, "루틴 " + (isEditMode ? "수정" : "추가") + " 실패");
                }
            }
            @Override
            public void onFailure(@NonNull Call<CleaningRoutine> call, @NonNull Throwable t) {
                handleApiFailure(t, "루틴 " + (isEditMode ? "수정" : "추가") + " 통신 오류");
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        setResult(RESULT_CANCELED);
        finish();
        return true;
    }

    private void handleApiError(Response<?> response, String defaultMessage) {
        String errorMessage = defaultMessage + " (코드: " + response.code() + ")";
        if (response.errorBody() != null) {
            try {
                errorMessage += "\n 내용: " + response.errorBody().string();
            } catch (IOException e) { Log.e(TAG, "Error body parsing error", e); }
        }
        Log.e(TAG, errorMessage);
        Toast.makeText(this, defaultMessage, Toast.LENGTH_LONG).show();
    }

    private void handleApiFailure(Throwable t, String defaultMessage) {
        String failMessage = defaultMessage + ": " + t.getMessage();
        Log.e(TAG, failMessage, t);
        Toast.makeText(this, defaultMessage, Toast.LENGTH_LONG).show();
    }
}