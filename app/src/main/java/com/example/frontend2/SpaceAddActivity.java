package com.example.frontend2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.frontend2.api.ApiClient;
import com.example.frontend2.api.SpaceApi;
import com.example.frontend2.models.Space;
import com.example.frontend2.models.SpaceRequest;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SpaceAddActivity extends AppCompatActivity {

    private EditText etFurniture, etCustomType, etNickname;
    private Spinner spinnerSpaceType;
    private Button btnSave;
    private TextView tvEmoji;
    private TextInputLayout tilCustomType;

    private boolean isEditMode = false;
    private int editingSpaceId = -1;

    public static final String PREFS_NAME_FOR_APP = "CleanItAppPrefs";
    public static final String KEY_USER_ID_FOR_APP = "logged_in_user_id";

    private ArrayList<String> existingSpaceNames;

    private static final Map<String, String> spaceEmojiMap = new HashMap<>();
    static {
        spaceEmojiMap.put("거실", "🛋️");
        spaceEmojiMap.put("침실", "🛏️");
        spaceEmojiMap.put("부엌", "🍳");
        spaceEmojiMap.put("화장실", "🚽");
        spaceEmojiMap.put("세탁실", "🧺");
        spaceEmojiMap.put("옷방", "👗");
        spaceEmojiMap.put("현관", "🚪");
        spaceEmojiMap.put("서재", "📚");
        spaceEmojiMap.put("다용도실", "🧹");
        spaceEmojiMap.put("베란다", "🌿");
        spaceEmojiMap.put("아이방", "🧸");
        spaceEmojiMap.put("펫룸", "🐶");
        spaceEmojiMap.put("차고", "🚗");
        spaceEmojiMap.put("창고", "📦");
        spaceEmojiMap.put("테라스", "☀️");
        spaceEmojiMap.put("기타", "❓");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_space_add);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("공간 추가");
        }

        etFurniture = findViewById(R.id.etFurniture);
        etCustomType = findViewById(R.id.etCustomType);
        etNickname = findViewById(R.id.etNickname);  // ✅ 별명 필드
        tilCustomType = findViewById(R.id.tilCustomType);
        spinnerSpaceType = findViewById(R.id.spinnerSpaceType);
        btnSave = findViewById(R.id.btnSave);
        tvEmoji = findViewById(R.id.tvEmoji);

        String[] types = {"거실", "침실", "부엌", "화장실", "세탁실", "옷방", "현관", "서재", "다용도실",
                "베란다", "아이방", "펫룸", "차고", "창고", "테라스", "기타"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, types);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSpaceType.setAdapter(adapter);

        spinnerSpaceType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedType = types[position];
                tvEmoji.setText(spaceEmojiMap.getOrDefault(selectedType, "❓"));

                if ("기타".equals(selectedType)) {
                    tilCustomType.setVisibility(View.VISIBLE);
                } else {
                    tilCustomType.setVisibility(View.GONE);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // 기존 공간 이름 리스트 받기
        existingSpaceNames = getIntent().getStringArrayListExtra("existing_spaces");
        if (existingSpaceNames == null) {
            existingSpaceNames = new ArrayList<>();
        }

        // 수정 모드 처리
        Intent intent = getIntent();
        String mode = intent.getStringExtra("mode");
        if ("edit".equals(mode)) {
            isEditMode = true;
            editingSpaceId = intent.getIntExtra("space_id", -1);
            String type = intent.getStringExtra("type");
            String furniture = intent.getStringExtra("furniture");

            etFurniture.setText(furniture);

            boolean matched = false;
            for (int i = 0; i < types.length; i++) {
                if (types[i].equals(type)) {
                    spinnerSpaceType.setSelection(i);
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                spinnerSpaceType.setSelection(adapter.getPosition("기타"));
                tilCustomType.setVisibility(View.VISIBLE);
                etCustomType.setText(type);
            }
            btnSave.setText("수정");
        }

        btnSave.setOnClickListener(v -> saveSpace());
    }

    private void saveSpace() {
        String furniture = etFurniture.getText().toString().trim();
        String type = spinnerSpaceType.getSelectedItem().toString();
        String finalType;

        if ("기타".equals(type)) {
            finalType = etCustomType.getText().toString().trim();
            if (finalType.isEmpty()) {
                Toast.makeText(this, "기타 공간명을 입력하세요.", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            finalType = type;
        }

        String nickname = etNickname.getText().toString().trim();
        String name;

        if (!nickname.isEmpty()) {
            name = nickname;
        } else {
            name = finalType;

            // 중복 방지: "침실", "침실 2", "침실 3" ...
            int count = 1;
            String tempName = name;
            while (existingSpaceNames.contains(tempName)) {
                count++;
                tempName = name + " " + count;
            }
            name = tempName;
        }

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME_FOR_APP, Context.MODE_PRIVATE);
        int userId = prefs.getInt(KEY_USER_ID_FOR_APP, -1);
        if (userId == -1) {
            Toast.makeText(this, "로그인 정보 없음", Toast.LENGTH_SHORT).show();
            return;
        }

        SpaceApi api = ApiClient.getClient().create(SpaceApi.class);
        SpaceRequest request = new SpaceRequest(name, userId, finalType, furniture);

        if (isEditMode && editingSpaceId != -1) {
            api.updateSpace(editingSpaceId, request).enqueue(new Callback<Space>() {
                @Override
                public void onResponse(Call<Space> call, Response<Space> response) {
                    handleResponse(response, "수정");
                }
                @Override
                public void onFailure(Call<Space> call, Throwable t) {
                    handleFailure(t);
                }
            });
        } else {
            api.createSpace(request).enqueue(new Callback<Space>() {
                @Override
                public void onResponse(Call<Space> call, Response<Space> response) {
                    handleResponse(response, "추가");
                }
                @Override
                public void onFailure(Call<Space> call, Throwable t) {
                    handleFailure(t);
                }
            });
        }
    }

    private void handleResponse(Response<Space> response, String mode) {
        if (response.isSuccessful()) {
            Toast.makeText(this, "공간이 " + mode + "되었습니다", Toast.LENGTH_SHORT).show();
            setResult(Activity.RESULT_OK);
            finish();
        } else {
            try {
                String errorBody = response.errorBody() != null ? response.errorBody().string() : "No error body";
                Log.e("SpaceAddActivity", "실패: " + response.code() + " - " + errorBody);
            } catch (IOException e) {
                Log.e("SpaceAddActivity", "Error reading error body", e);
            }
            Toast.makeText(this, "공간 " + mode + " 실패", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleFailure(Throwable t) {
        Log.e("SpaceAddActivity", "API 통신 실패", t);
        Toast.makeText(this, "서버 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
