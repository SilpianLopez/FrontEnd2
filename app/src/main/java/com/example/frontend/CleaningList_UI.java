package com.example.frontend;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.frontend.api.ApiClient;
import com.example.frontend.api.CleaningRoutineApi;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CleaningList_UI extends AppCompatActivity {
    Toolbar toolbar;
    String space_name;
    RecyclerView recyclerView;
    CleaningListAdapter adapter;
    ImageView cadd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cleaning_list_ui);

        int spaceId = getIntent().getIntExtra("space_id", -1);
        int userId = getSharedPreferences("CleanItPrefs", MODE_PRIVATE).getInt("user_id", -1);
        space_name = getIntent().getStringExtra("space_name");
        toolbar = findViewById(R.id.toolbar_clist);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(space_name);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);  // <- 버튼 표시
        }

        recyclerView = findViewById(R.id.rv_clist);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<CleaningList> itemList = new ArrayList<>();
        itemList.add(new CleaningList("창틀 먼지 제거", "2일", "창문 2개의 창틀 먼지 제거"));
        itemList.add(new CleaningList("책상 정리", "3일", "책상 위 물건 정리하고 먼지 제거"));
        itemList.add(new CleaningList("침대 청소", "1주", "이불 털고 돌돌이로 머리카락 제거"));

        adapter = new CleaningListAdapter(itemList);
        recyclerView.setAdapter(adapter);

        //신도현 Retrofit 요청
        if (spaceId != -1) {
            CleaningRoutineApi routineApi = ApiClient.getClient().create(CleaningRoutineApi.class);
            routineApi.getRoutinesBySpace(spaceId).enqueue(new Callback<List<CleaningList>>() {
                @Override
                public void onResponse(Call<List<CleaningList>> call, Response<List<CleaningList>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<CleaningList> items = response.body();
                        adapter = new CleaningListAdapter(items);
                        recyclerView.setAdapter(adapter);
                    }
                }

                @Override
                public void onFailure(Call<List<CleaningList>> call, Throwable t) {
                    Log.e("루틴", "루틴 목록 불러오기 실패: " + t.getMessage());
                }
            });
        }

        // 아이콘 클릭 시 청소 항목 추가 화면으로 전환
        cadd = findViewById(R.id.im_cadd);

        cadd.setOnClickListener(item -> {
            Intent intent = new Intent(CleaningList_UI.this, CleaningAdd_UI.class);
            intent.putExtra("space_id", spaceId);  // space_id 넘겨주기
            intent.putExtra("user_id", userId);    // user_id도 같이 넘겨주기
            startActivity(intent);
        });
    }

    // <- 버튼 기능 구현
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return  true;
    }
}
