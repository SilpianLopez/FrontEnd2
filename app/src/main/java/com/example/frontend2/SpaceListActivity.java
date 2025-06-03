package com.example.frontend2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.frontend2.api.ApiClient;
import com.example.frontend2.api.SpaceApi;
import com.example.frontend2.models.Space;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SpaceListActivity extends AppCompatActivity {

    private LinearLayout spaceListContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_space_list);

        // 툴바 설정
        Toolbar toolbar = findViewById(R.id.myToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("공간 목록");
        }

        toolbar.setNavigationOnClickListener(v -> finish());

        ImageView btnAddSpace = toolbar.findViewById(R.id.btnAddSpace);
        btnAddSpace.setOnClickListener(v -> {
            Intent intent = new Intent(SpaceListActivity.this, SpaceAddActivity.class);
            startActivityForResult(intent, 101);
        });

        spaceListContainer = findViewById(R.id.spaceListContainer);

        // SharedPreferences에서 userId 가져오기
        SharedPreferences prefs = getSharedPreferences("CleanItPrefs", MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);

        if (userId == -1) {
            Toast.makeText(this, "로그인 정보가 없습니다", Toast.LENGTH_SHORT).show();
            return;
        }

        // 서버에서 공간 목록 가져오기
        fetchSpacesFromServer(userId);
    }

    private void fetchSpacesFromServer(int userId) {
        SpaceApi api = ApiClient.getClient().create(SpaceApi.class);
        api.getSpacesByUserId(userId).enqueue(new Callback<List<Space>>() {
            @Override
            public void onResponse(Call<List<Space>> call, Response<List<Space>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    spaceListContainer.removeAllViews();
                    for (Space space : response.body()) {
                        addSpaceItemToView(space);
                    }
                } else {
                    Toast.makeText(SpaceListActivity.this, "공간 목록 불러오기 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Space>> call, Throwable t) {
                Toast.makeText(SpaceListActivity.this, "서버 연결 오류: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void addSpaceItemToView(Space space) {
        View itemView = getLayoutInflater().inflate(R.layout.item_space, spaceListContainer, false);

        TextView tvSpaceName = itemView.findViewById(R.id.tvSpaceName);
        TextView tvSpaceType = itemView.findViewById(R.id.tvSpaceType);
        TextView tvFurniture = itemView.findViewById(R.id.tvFurniture);

        tvSpaceName.setText(space.getName());

        // 🔹 type, furniture도 실제 데이터로 표시
        tvSpaceType.setText("종류: " + (space.getType() != null ? space.getType() : "-"));
        tvFurniture.setText("가구: " + (space.getFurniture() != null ? space.getFurniture() : "-"));

        spaceListContainer.addView(itemView);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == 101) {
            SharedPreferences prefs = getSharedPreferences("CleanItPrefs", MODE_PRIVATE);
            int userId = prefs.getInt("user_id", -1);
            if (userId != -1) {
                fetchSpacesFromServer(userId);
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
