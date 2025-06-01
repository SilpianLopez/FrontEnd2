package com.example.frontend2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SpaceListActivity extends AppCompatActivity {

    private List<Space> spaceList;
    private SpaceAdapter adapter;
    private int editingPosition = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_space_list);

        Toolbar toolbar = findViewById(R.id.myToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("공간 목록");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        ImageView btnAddSpace = findViewById(R.id.btnAddSpace);
        btnAddSpace.setOnClickListener(v -> {
            Intent intent = new Intent(SpaceListActivity.this, SpaceAddActivity.class);
            startActivityForResult(intent, 101);
        });

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // TODO: 더미 데이터 - 백엔드 연동 시 제거 필요
        spaceList = new ArrayList<>();
        spaceList.add(new Space("거실", "거실", "소파, 테이블"));
        spaceList.add(new Space("화장실", "욕실", "세면대, 변기"));
        spaceList.add(new Space("옷방", "드레스룸", "옷장, 전신거울"));
        //TODO: 끝

        adapter = new SpaceAdapter(this, spaceList);
        recyclerView.setAdapter(adapter);

        adapter.setOnSpaceEditListener((position, space) -> {
            editingPosition = position;
            Intent intent = new Intent(SpaceListActivity.this, SpaceAddActivity.class);
            intent.putExtra("mode", "edit");
            intent.putExtra("spaceName", space.getName());
            intent.putExtra("spaceType", space.getType());
            intent.putExtra("furniture", space.getFurniture());
            startActivityForResult(intent, 102);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            String name = data.getStringExtra("spaceName");
            String type = data.getStringExtra("spaceType");
            String furniture = data.getStringExtra("furniture");

            if (requestCode == 101) {
                Space newSpace = new Space(name, type, furniture);
                spaceList.add(newSpace);
                adapter.notifyItemInserted(spaceList.size() - 1);
            } else if (requestCode == 102 && editingPosition != -1) {
                spaceList.set(editingPosition, new Space(name, type, furniture));
                adapter.notifyItemChanged(editingPosition);
                editingPosition = -1;
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
