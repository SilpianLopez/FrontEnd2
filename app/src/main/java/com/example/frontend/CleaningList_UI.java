package com.example.frontend;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

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

        // 아이콘 클릭 시 청소 항목 추가 화면으로 전환
        cadd = findViewById(R.id.im_cadd);

        cadd.setOnClickListener(item -> {
            Intent intent = new Intent(CleaningList_UI.this, CleaningAdd_UI.class);
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
