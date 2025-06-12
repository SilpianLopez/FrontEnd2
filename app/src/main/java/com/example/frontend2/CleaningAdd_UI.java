package com.example.frontend2;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.frontend2.api.ApiClient;
import com.example.frontend2.api.CleaningRoutineApi;
import com.example.frontend2.models.CleaningRoutine;
import com.example.frontend2.models.RoutineRequest;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.gson.Gson;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CleaningAdd_UI extends AppCompatActivity {

    private static final String TAG = "CleaningAdd_UI";
    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_USER_ID = "current_user_id";

    private EditText etTitle, etDescription;
    private ChipGroup chipGroupWeekdays;
    private ImageView btnOpenCalendar;
    private TextView tvSelectedRepeatInfo;
    private Button btnSave;

    private int currentUserId = -1;
    private int spaceId = -1;
    private boolean isEditMode = false;
    private int routineId = -1;

    private List<Integer> selectedWeekdays = new ArrayList<>();
    private String selectedDate = null;

    private final String[] weekdayNames = {"일", "월", "화", "수", "목", "금", "토"};
    private final int[] chipIds = {
            R.id.chipSun, R.id.chipMon, R.id.chipTue, R.id.chipWed,
            R.id.chipThu, R.id.chipFri, R.id.chipSat
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cleaning_add_ui);

        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        chipGroupWeekdays = findViewById(R.id.chipGroupWeekdays);
        btnOpenCalendar = findViewById(R.id.btnOpenCalendar);
        tvSelectedRepeatInfo = findViewById(R.id.tvSelectedRepeatInfo);
        btnSave = findViewById(R.id.btnSaveRoutine);

        Toolbar toolbar = findViewById(R.id.toolbar_cadd);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }


        Intent intent = getIntent();
        currentUserId = intent.getIntExtra("userId", -1);
        if (currentUserId == -1) {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            currentUserId = prefs.getInt(KEY_USER_ID, -1);
        }

        spaceId = intent.getIntExtra("spaceId", -1);
        isEditMode = intent.getStringExtra("mode") != null &&
                intent.getStringExtra("mode").equals("edit");
        routineId = intent.getIntExtra("routineIdToEdit", -1);

        if (isEditMode) {
            etTitle.setText(intent.getStringExtra("currentTitle"));
            etDescription.setText(intent.getStringExtra("currentDescription"));

            List<Integer> weekdays = intent.getIntegerArrayListExtra("weekdays");
            String date = intent.getStringExtra("dates");
            if (weekdays != null && !weekdays.isEmpty()) {
                for (int i : weekdays) {
                    Chip chip = findViewById(chipIds[i]);
                    chip.setChecked(true);
                    selectedWeekdays.add(i);
                }
            } else if (date != null) {
                selectedDate = date;
            }
        }

        // 요일 Chip 체크 리스너
        for (int i = 0; i < chipIds.length; i++) {
            final int weekday = i;
            Chip chip = findViewById(chipIds[i]);
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedWeekdays.add(weekday);
                    selectedDate = null;  // 상호배타
                } else {
                    selectedWeekdays.remove((Integer) weekday);
                }
                updateRepeatInfoText();
            });
        }

        btnOpenCalendar.setOnClickListener(v -> showDatePicker());

        btnSave.setOnClickListener(v -> saveRoutine());

        updateRepeatInfoText();
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(year, month, dayOfMonth);

                    SimpleDateFormat sendFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREAN);
                    selectedDate = sendFormat.format(selected.getTime());
                    selectedWeekdays.clear();  // 상호배타
                    for (int id : chipIds) {
                        Chip chip = findViewById(id);
                        chip.setChecked(false);
                    }
                    updateRepeatInfoText();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        dialog.setButton(DatePickerDialog.BUTTON_POSITIVE, "완료", dialog);
        dialog.setButton(DatePickerDialog.BUTTON_NEGATIVE, "취소", (d, which) -> dialog.dismiss());
        dialog.show();
    }

    private void updateRepeatInfoText() {
        if (selectedDate != null) {
            Calendar c = Calendar.getInstance();
            try {
                c.setTime(new SimpleDateFormat("yyyy-MM-dd", Locale.KOREAN).parse(selectedDate));
                String day = new SimpleDateFormat("M월 d일 (E)", Locale.KOREAN).format(c.getTime());
                Calendar today = Calendar.getInstance();
                if (c.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                        c.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) + 1) {
                    tvSelectedRepeatInfo.setText("내일 - " + day);
                } else {
                    tvSelectedRepeatInfo.setText(day);
                }
            } catch (Exception e) {
                tvSelectedRepeatInfo.setText("선택된 날짜: " + selectedDate);
            }
        } else if (!selectedWeekdays.isEmpty()) {
            Collections.sort(selectedWeekdays);
            if (selectedWeekdays.size() == 7) {
                tvSelectedRepeatInfo.setText("매일");
            } else {
                List<String> label = new ArrayList<>();
                for (int i : selectedWeekdays) label.add(weekdayNames[i]);
                tvSelectedRepeatInfo.setText("매주 " + TextUtils.join(", ", label));
            }
        } else {
            tvSelectedRepeatInfo.setText("선택된 반복 정보 없음");
        }
    }


    private void saveRoutine() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        if (TextUtils.isEmpty(title)) {
            Toast.makeText(this, "제목을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (spaceId <= 0 || currentUserId <= 0) {
            Toast.makeText(this, "유효한 공간/사용자 정보가 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Integer> weekdays = selectedWeekdays.isEmpty() ? null : new ArrayList<>(selectedWeekdays);
        List<String> dates = selectedDate == null ? null : Collections.singletonList(selectedDate);

        Gson gson = new Gson();
        String weekdaysJson = gson.toJson(selectedWeekdays);  // 요일 숫자 배열
        String datesJson = gson.toJson(selectedDate);

        RoutineRequest req = new RoutineRequest(
                spaceId,
                currentUserId,
                title,
                description,
                "NONE", null, null,
                weekdaysJson,
                datesJson
        );

        CleaningRoutineApi api = ApiClient.getClient().create(CleaningRoutineApi.class);
        Call<CleaningRoutine> call = isEditMode ?
                api.updateRoutine(routineId, req) :
                api.createRoutine(req);

        call.enqueue(new Callback<CleaningRoutine>() {
            @Override
            public void onResponse(@NonNull Call<CleaningRoutine> call, @NonNull Response<CleaningRoutine> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(CleaningAdd_UI.this, "루틴이 저장되었습니다.", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(CleaningAdd_UI.this, "저장 실패: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<CleaningRoutine> call, @NonNull Throwable t) {
                Toast.makeText(CleaningAdd_UI.this, "서버 통신 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "통신 오류", t);
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        setResult(RESULT_CANCELED);
        finish();
        return true;
    }

}
