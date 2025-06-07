package com.example.frontend2;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.frontend2.api.ApiClient;
import com.example.frontend2.api.SpaceApi;
import com.example.frontend2.models.Space;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SpaceListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private List<Space> spaceList = new ArrayList<>();
    private SpaceAdapter spaceAdapter;

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

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        spaceAdapter = new SpaceAdapter(this, spaceList);
        recyclerView.setAdapter(spaceAdapter);

        // 바텀시트 콜백 연결
        spaceAdapter.setOnSpaceEditListener(new SpaceAdapter.OnSpaceEditListener() {
            @Override
            public void onEditRequested(int position, Space space) {
                Intent intent = new Intent(SpaceListActivity.this, SpaceAddActivity.class);
                intent.putExtra("mode", "edit");
                intent.putExtra("space_id", space.getSpace_id());
                intent.putExtra("name", space.getName());
                intent.putExtra("type", space.getType());
                intent.putExtra("furniture", space.getFurniture());
                startActivityForResult(intent, 101);
            }

            @Override
            public void onDeleteRequested(int position, Space space) {
                new AlertDialog.Builder(SpaceListActivity.this)
                        .setTitle("삭제 확인")
                        .setMessage("정말 삭제하시겠습니까?")
                        .setPositiveButton("삭제", (dialog, which) -> {
                            deleteSpaceFromServer(space.getSpace_id());
                        })
                        .setNegativeButton("취소", null)
                        .show();
            }
        });

        // 사용자 ID 확인
        SharedPreferences prefs = getSharedPreferences("CleanItPrefs", MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);
        if (userId == -1) {
            Toast.makeText(this, "로그인 정보가 없습니다", Toast.LENGTH_SHORT).show();
            return;
        }

        // 공간 목록 불러오기
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
                    spaceList.clear();
                    spaceList.addAll(response.body());
                    spaceAdapter.notifyDataSetChanged();
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

    private void deleteSpaceFromServer(int spaceId) {
        SpaceApi api = ApiClient.getClient().create(SpaceApi.class);
        api.deleteSpace(spaceId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(SpaceListActivity.this, "삭제 성공", Toast.LENGTH_SHORT).show();
                    SharedPreferences prefs = getSharedPreferences("CleanItPrefs", MODE_PRIVATE);
                    int userId = prefs.getInt("user_id", -1);
                    fetchSpacesFromServer(userId);
                } else {
                    Toast.makeText(SpaceListActivity.this, "삭제 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(SpaceListActivity.this, "서버 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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
