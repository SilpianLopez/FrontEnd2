package com.example.frontend;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.frontend.api.ApiClient;
import com.example.frontend.api.SpaceApi;
import com.example.frontend.models.Space;
import com.example.frontend.models.SpaceRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SpaceAddActivity extends AppCompatActivity {

    private EditText etSpaceName, etFurniture, etCustomType;
    private Spinner spinnerSpaceType;
    private Button btnSave;
    private ImageView ivIcon;
    private int selectedIconResId = R.drawable.ic_default;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_space_add);

        // 툴바 설정
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("공간 추가");
        }

        etSpaceName = findViewById(R.id.etSpaceName);
        etFurniture = findViewById(R.id.etFurniture);
        etCustomType = findViewById(R.id.etCustomType);
        spinnerSpaceType = findViewById(R.id.spinnerSpaceType);
        btnSave = findViewById(R.id.btnSave);
        ivIcon = findViewById(R.id.ivIcon);

        String[] types = {"거실", "침실", "화장실", "옷방", "기타"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, types);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSpaceType.setAdapter(adapter);

        spinnerSpaceType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if ("기타".equals(parent.getItemAtPosition(position))) {
                    etCustomType.setVisibility(View.VISIBLE);
                } else {
                    etCustomType.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        ivIcon.setOnClickListener(v -> showIconPicker());

        btnSave.setOnClickListener(v -> {
            String name = etSpaceName.getText().toString().trim();
            String furniture = etFurniture.getText().toString().trim();
            String type = spinnerSpaceType.getSelectedItem().toString();

            if ("기타".equals(type)) {
                type = etCustomType.getText().toString().trim();
            }

            if (name.isEmpty() || type.isEmpty()) {
                Toast.makeText(this, "모든 항목을 입력하세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            // SharedPreferences에서 user_id 가져오기
            SharedPreferences prefs = getSharedPreferences("CleanItPrefs", MODE_PRIVATE);
            int userId = prefs.getInt("user_id", -1);
            if (userId == -1) {
                Toast.makeText(this, "로그인 정보 없음", Toast.LENGTH_SHORT).show();
                return;
            }

            // 🔹 서버로 보낼 요청 객체 생성
            SpaceRequest request = new SpaceRequest(name, userId);

            SpaceApi api = ApiClient.getClient().create(SpaceApi.class);
            api.createSpace(request).enqueue(new Callback<Space>() {
                @Override
                public void onResponse(Call<Space> call, Response<Space> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(SpaceAddActivity.this, "공간이 추가되었습니다", Toast.LENGTH_SHORT).show();
                        setResult(Activity.RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(SpaceAddActivity.this, "공간 추가 실패", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Space> call, Throwable t) {
                    Toast.makeText(SpaceAddActivity.this, "서버 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void showIconPicker() {
        int[] iconResIds = {
                R.drawable.ic_livingroom,
                R.drawable.ic_room,
                R.drawable.ic_toilet,
                R.drawable.ic_wardrobe
        };

        String[] iconLabels = {"거실", "침실", "화장실", "옷방"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("아이콘 선택");

        builder.setItems(iconLabels, (dialog, which) -> {
            selectedIconResId = iconResIds[which];
            ivIcon.setImageResource(selectedIconResId);
        });

        builder.setNegativeButton("취소", null);
        builder.show();
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
