package com.example.frontend2;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;

import com.example.frontend.api.ApiClient;
import com.example.frontend.api.CleaningRoutineApi;
import com.example.frontend.models.RoutineRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CleaningAdd_UI extends AppCompatActivity {
    Toolbar toolbar;
    Spinner spunit, spvalue;
    Button btnsave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cleaning_add_ui);
        // 청소 항목 추가 툴바
        toolbar = findViewById(R.id.toolbar_cadd);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("청소 항목 추가");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);  // <- 버튼 표시
        }
        // 청소 주기 스피너
        spunit = findViewById(R.id.sp_unit);
        spvalue = findViewById(R.id.sp_value);
        // 단위: 매일, 매주, 매달
        String[] unitItems = {"매일", "매주", "매달"};
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(this, R.layout.spinner_item_white, unitItems);
        unitAdapter.setDropDownViewResource(R.layout.spinner_item_white);
        spunit.setAdapter(unitAdapter);
        // 숫자: 1 ~ 10일
        String[] valueitems = new String[10];
        for (int i = 0; i < 10; i++) {
            valueitems[i] = String.valueOf(i + 1);
        }
        ArrayAdapter<String> valueAdapter = new ArrayAdapter<>(this, R.layout.spinner_item_white, valueitems);
        unitAdapter.setDropDownViewResource(R.layout.spinner_item_white);
        spvalue.setAdapter(valueAdapter);

        // 저장 버튼('저장 완료' 메시지만 뜨게 함)
        btnsave = findViewById(R.id.btn_save);
        btnsave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // EditText에서 값 읽어오기
                EditText etTitle = findViewById(R.id.et_title);
                EditText etDescription = findViewById(R.id.et_description);
                String title = etTitle.getText().toString();
                String description = etDescription.getText().toString();

                // 기타 값들
                int spaceId = getIntent().getIntExtra("space_id", -1);
                int userId = getSharedPreferences("CleanItPrefs", MODE_PRIVATE).getInt("user_id", -1);

                Spinner spUnit = findViewById(R.id.sp_unit);
                Spinner spValue = findViewById(R.id.sp_value);
                String repeatUnit = spUnit.getSelectedItem().toString();
                int repeatInterval = Integer.parseInt(spValue.getSelectedItem().toString());

                String firstDueDate = "2024-06-01"; // 날짜 입력은 추후 DatePicker로

                RoutineRequest request = new RoutineRequest(spaceId, userId, title, description,
                        repeatUnit, repeatInterval, firstDueDate);

                CleaningRoutineApi api = ApiClient.getClient().create(CleaningRoutineApi.class);
                api.createRoutine(request).enqueue(new Callback<CleaningList>() {
                    @Override
                    public void onResponse(Call<CleaningList> call, Response<CleaningList> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(CleaningAdd_UI.this, "추가 성공", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e("ROUTINE_API", "응답 실패: code = " + response.code());
                            Toast.makeText(CleaningAdd_UI.this, "추가 실패", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<CleaningList> call, Throwable t) {
                        Log.e("ROUTINE_API", "통신 오류", t);
                        Toast.makeText(CleaningAdd_UI.this, "통신 실패", Toast.LENGTH_SHORT).show();
                    }
                });

                Log.d("DEBUG", "넘겨받은 title: " + title);
                Log.d("DEBUG", "넘겨받은 description: " + description);
            }
        });



    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
