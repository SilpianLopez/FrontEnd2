package com.example.frontend2;

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
        // 청소 항목 목록 툴바(Main_UI에서 공간명(space_name)을 전달받아 툴바 타이틀로 사용)
        space_name = getIntent().getStringExtra("space_name");
        toolbar = findViewById(R.id.toolbar_clist);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(space_name);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);  // <- 버튼 표시
        }

        recyclerView = findViewById(R.id.rv_clist);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // TODO: 백엔드 연동 전 테스트용 더미 데이터 (연동 시 삭제 예정)
        List<CleaningList> itemList = new ArrayList<>();
        itemList.add(new CleaningList("창틀 먼지 제거", "2일", "창문 2개의 창틀 먼지 제거"));
        itemList.add(new CleaningList("책상 정리", "3일", "책상 위 물건 정리하고 먼지 제거"));
        itemList.add(new CleaningList("침대 청소", "1주", "이불 털고 돌돌이로 머리카락 제거"));
        // TODO 끝: 위 더미 데이터는 추후 백엔드에서 받은 데이터로 대체 필요
        adapter = new CleaningListAdapter(itemList);
        recyclerView.setAdapter(adapter);

        adapter.setOnCleaningEditListener((position, item) -> {
            Intent intent = new Intent(CleaningList_UI.this, CleaningAdd_UI.class);
            intent.putExtra("mode", "edit");
            intent.putExtra("name", item.name);
            intent.putExtra("cycle", item.cycle);
            intent.putExtra("comment", item.comment);
            startActivity(intent);
        });

        // + 아이콘 클릭 시 청소 항목 추가 화면으로 전환
        cadd = findViewById(R.id.im_cadd);

        cadd.setOnClickListener(item -> {
            Intent intent = new Intent(CleaningList_UI.this, CleaningAdd_UI.class);
            startActivity(intent);
        });


    }
    // 툴바 <- 버튼 기능 구현
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return  true;
    }
}
