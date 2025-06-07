package com.example.frontend2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.frontend2.api.ApiClient;
import com.example.frontend2.api.CleaningRoutineApi;
import com.example.frontend2.CleaningList;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CleaningList_UI extends AppCompatActivity {
    private Toolbar toolbar;
    private String space_name;
    private RecyclerView recyclerView;
    private CleaningListAdapter adapter;
    private ImageView cadd;
    private int spaceId, userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cleaning_list_ui);

        // intent, prefs
        spaceId = getIntent().getIntExtra("space_id", -1);
        userId  = getSharedPreferences("CleanItPrefs", MODE_PRIVATE)
                .getInt("user_id", -1);
        space_name = getIntent().getStringExtra("space_name");
        Log.d("CHECK", "CleaningList_UI: spaceId=" + spaceId + ", userId=" + userId);

        // toolbar
        toolbar = findViewById(R.id.toolbar_clist);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(space_name);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // RecyclerView + Adapter
        recyclerView = findViewById(R.id.rv_clist);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CleaningListAdapter(this, new ArrayList<CleaningList>());
        recyclerView.setAdapter(adapter);

        // 수정/삭제 콜백 연결
        adapter.setOnRoutineActionListener(new CleaningListAdapter.OnRoutineActionListener() {
            @Override
            public void onEditRequested(int position, CleaningList item) {
                Intent intent = new Intent(CleaningList_UI.this, CleaningAdd_UI.class);
                intent.putExtra("mode", "edit");
                intent.putExtra("routine_id", item.getRoutine_id());
                intent.putExtra("space_id", spaceId);
                intent.putExtra("title", item.getName());
                intent.putExtra("comment", item.getComment());
                intent.putExtra("cycle", item.getCycle());
                startActivity(intent);
            }

            @Override
            public void onDeleteRequested(int position, CleaningList item) {
                new AlertDialog.Builder(CleaningList_UI.this)
                        .setTitle("삭제 확인")
                        .setMessage("정말 삭제하시겠습니까?")
                        .setPositiveButton("삭제", (dialog, which) ->
                                deleteRoutine(item.getRoutine_id(), position))
                        .setNegativeButton("취소", null)
                        .show();
            }
        });

        // 데이터 로드
        if (spaceId != -1) fetchRoutines();

        // 추가 버튼
        cadd = findViewById(R.id.im_cadd);
        cadd.setOnClickListener(v -> {
            Intent intent = new Intent(CleaningList_UI.this, CleaningAdd_UI.class);
            intent.putExtra("space_id", spaceId);
            intent.putExtra("user_id", userId);
            startActivity(intent);
        });
    }

    private void fetchRoutines() {
        CleaningRoutineApi api = ApiClient.getClient().create(CleaningRoutineApi.class);
        api.getRoutinesBySpace(spaceId).enqueue(new Callback<List<CleaningList>>() {
            @Override
            public void onResponse(Call<List<CleaningList>> call, Response<List<CleaningList>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<CleaningList> items = response.body();
                    // adapter 내부 리스트 교체
                    adapter.getItems().clear();
                    adapter.getItems().addAll(items);
                    adapter.notifyDataSetChanged();
                } else {
                    Log.e("루틴", "불러오기 실패: code=" + response.code());
                    Toast.makeText(CleaningList_UI.this, "루틴 불러오기 실패", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<List<CleaningList>> call, Throwable t) {
                Log.e("루틴", "서버 오류: " + t.getMessage(), t);
                Toast.makeText(CleaningList_UI.this, "서버 오류", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteRoutine(int routineId, int position) {
        CleaningRoutineApi api = ApiClient.getClient().create(CleaningRoutineApi.class);
        api.deleteRoutine(routineId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    adapter.removeItem(position);
                    Toast.makeText(CleaningList_UI.this, "삭제되었습니다", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(CleaningList_UI.this, "삭제 실패", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(CleaningList_UI.this, "서버 오류", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
