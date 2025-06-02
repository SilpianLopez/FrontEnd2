package com.example.frontend;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.frontend.api.ApiClient;
import com.example.frontend.api.SpaceApi;
import com.example.frontend.models.Space;

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
        Log.d("SpaceListActivity", "fetchSpacesFromServer 호출됨, userId = " + userId);

        SpaceApi api = ApiClient.getClient().create(SpaceApi.class);
        api.getSpacesByUser(userId).enqueue(new Callback<List<Space>>() {
            @Override
            public void onResponse(Call<List<Space>> call, Response<List<Space>> response) {
                Log.d("SpaceListActivity", "응답 도착 - 성공 여부: " + response.isSuccessful());
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("SpaceListActivity", "응답 받은 공간 수: " + response.body().size());

                    spaceListContainer.removeAllViews();
                    for (Space space : response.body()) {
                        Log.d("SpaceListActivity", "공간 추가: " + space.getName() + " (ID: " + space.getSpace_id() + ")");
                        addSpaceItemToView(space);
                    }
                } else {
                    Log.e("SpaceListActivity", "공간 목록 불러오기 실패 - code: " + response.code());
                    Toast.makeText(SpaceListActivity.this, "공간 목록 불러오기 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Space>> call, Throwable t) {
                Log.e("SpaceListActivity", "서버 연결 오류: " + t.getMessage(), t);
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

        // 🔻 로그 추가
        Log.d("SPACE_LIST", "space_id: " + space.getSpace_id() + ", name: " + space.getName());

        //신도현 클릭 리스너 추가
        itemView.setOnClickListener(v -> {
            Intent intent = new Intent(SpaceListActivity.this, CleaningList_UI.class);
            intent.putExtra("space_id", space.getSpace_id());
            intent.putExtra("space_name", space.getName()); // 공간 이름 넘기기
            startActivity(intent);
        });

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
