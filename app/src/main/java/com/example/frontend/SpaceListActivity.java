package com.example.frontend;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;

public class SpaceListActivity extends AppCompatActivity {

    private LinearLayout spaceListContainer;
    private List<Space> spaceList;
    private int editingPosition = -1;

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

        // 뒤로가기 동작 처리
        toolbar.setNavigationOnClickListener(v -> finish());

        // 툴바 내부의 공간 추가 버튼
        ImageView btnAddSpace = toolbar.findViewById(R.id.btnAddSpace);
        btnAddSpace.setOnClickListener(v -> {
            Intent intent = new Intent(SpaceListActivity.this, SpaceAddActivity.class);
            startActivityForResult(intent, 101); // 추가 요청
        });

        // 공간 목록 초기화 및 추가
        spaceListContainer = findViewById(R.id.spaceListContainer);
        spaceList = new ArrayList<>();

        spaceList.add(new Space("거실", "거실", "소파, 테이블"));
        spaceList.add(new Space("화장실", "욕실", "세면대, 변기"));
        spaceList.add(new Space("옷방", "드레스룸", "옷장, 전신거울"));

        for (Space space : spaceList) {
            addSpaceItemToView(space);
        }

    }

    private void addSpaceItemToView(Space space) {
        View itemView = getLayoutInflater().inflate(R.layout.item_space, spaceListContainer, false);

        TextView tvSpaceName = itemView.findViewById(R.id.tvSpaceName);
        TextView tvSpaceType = itemView.findViewById(R.id.tvSpaceType);
        TextView tvFurniture = itemView.findViewById(R.id.tvFurniture);

        tvSpaceName.setText(space.getName());
        tvSpaceType.setText("종류: " + space.getType());
        tvFurniture.setText("가구: " + space.getFurniture());

        spaceListContainer.addView(itemView);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            String name = data.getStringExtra("spaceName");
            String type = data.getStringExtra("spaceType");
            String furniture = data.getStringExtra("furniture");

            if (requestCode == 101) {
                Space newSpace = new Space(name, type, furniture);
                spaceList.add(newSpace);
                addSpaceItemToView(newSpace);
            }
            // 수정 로직은 필요시 이어서 구현 가능
        }
    }
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

}
