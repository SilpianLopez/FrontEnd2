package com.example.frontend2;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;


public class RoutineDetailActivity extends AppCompatActivity {
    Button btnApply, btnRetry;
    RecyclerView recyclerRoutine;
    RoutineAdapter routineAdapter;
    List<String> routineList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routine_detail);

        String roomName = getIntent().getStringExtra("roomName");

        TextView tvRoomName = findViewById(R.id.tvRoomName);
        tvRoomName.setText(roomName + " 루틴 추천");

        Toolbar toolbar = findViewById(R.id.myToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // 뒤로가기 아이콘 활성화
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> finish()); // 아이콘 클릭 시 뒤로가기


        Button btnApply = findViewById(R.id.btnApplyRoutine);
        Button btnRetry = findViewById(R.id.btnRetryRoutine);
        recyclerRoutine = findViewById(R.id.recyclerRoutine);
        recyclerRoutine.setLayoutManager(new LinearLayoutManager(this));
        routineAdapter = new RoutineAdapter(routineList);
        recyclerRoutine.setAdapter(routineAdapter);

        btnApply.setOnClickListener(v -> {
            Toast.makeText(this, "루틴이 반영되었습니다!", Toast.LENGTH_SHORT).show();
            // 실제 반영 로직이 있다면 여기에 추가
        });

        btnRetry.setOnClickListener(v -> {
            Toast.makeText(this, "루틴을 다시 추천합니다.", Toast.LENGTH_SHORT).show();
            // 추천 로직 새로 불러오기 등 추가 가능
        });

        // 테스트용 더미 루틴 추가 (Node.js 연동 전 임시)
        routineList.add("바닥 청소하기");
        routineList.add("창문 닦기");
        routineList.add("쓰레기통 비우기");
        routineAdapter.notifyDataSetChanged();

    }
}
