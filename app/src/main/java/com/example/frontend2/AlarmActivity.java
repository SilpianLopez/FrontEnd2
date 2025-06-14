package com.example.frontend2;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AlarmActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private AlarmAdapter adapter;
    private Button btnSaveAlarm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        Toolbar toolbar = findViewById(R.id.myToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recyclerView);
        btnSaveAlarm = findViewById(R.id.btnSaveAlarm);

        // TODO: 백엔드 연동 전 테스트용 더미 데이터 (연동 시 삭제 예정)
        List<AlarmRoom> roomList = new ArrayList<>();
        List<AlarmTask> taskList1 = new ArrayList<>();
        taskList1.add(new AlarmTask("청소 항목1", "알람시간1", true));
        taskList1.add(new AlarmTask("청소 항목2", "알람시간2", true));
        taskList1.add(new AlarmTask("청소 항목3", "알람시간3", true));
        roomList.add(new AlarmRoom("거실", taskList1));

        List<AlarmTask> taskList2 = new ArrayList<>();
        roomList.add(new AlarmRoom("화장실", taskList2));
        //TODO: 끝
        adapter = new AlarmAdapter(this, roomList);// TODO: 위 더미 데이터 제거 시 adapter 초기화도 백엔드 데이터로 대체 필요
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        btnSaveAlarm.setOnClickListener(v -> finish());
    }
}
