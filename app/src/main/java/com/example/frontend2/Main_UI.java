package com.example.frontend2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.frontend2.api.ApiClient;
import com.example.frontend2.api.SpaceApi;
import com.example.frontend2.models.Space;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Main_UI extends AppCompatActivity {

    private GridLayout spaceGrid;
    private LinearLayout todoListLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_ui);

        spaceGrid = findViewById(R.id.spaceGrid);
        todoListLayout = findViewById(R.id.todoListLayout);

        // ✅ 공간 서버에서 불러오기
        SharedPreferences prefs = getSharedPreferences("CleanItPrefs", MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);
        if (userId != -1) {
            fetchSpacesFromServer(userId);
        }

        // 할 일 추가 (더미)
        addTodoItem("청소 항목1");
        addTodoItem("청소 항목2");

        // 버튼 클릭 처리
        findViewById(R.id.btnAddSpace).setOnClickListener(v -> {
            Intent intent = new Intent(Main_UI.this, SpaceListActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.btnAlarm).setOnClickListener(v -> {
            Intent intent = new Intent(Main_UI.this, AlarmActivity.class);
            startActivity(intent);
        });

        // 하단 네비게이션 클릭 처리
        LinearLayout navProfile = findViewById(R.id.navProfile);
        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navCalendar = findViewById(R.id.navCalendar);
        LinearLayout navAi = findViewById(R.id.navAi);

// 텍스트 뷰 ID도 연결
        TextView tvProfile = findViewById(R.id.navProfileText);
        TextView tvHome = findViewById(R.id.navHomeText);
        TextView tvCalendar = findViewById(R.id.navCalendarText);
        TextView tvAi = findViewById(R.id.navAiText);

// 색 초기화 함수
        Runnable resetTabColors = () -> {
            int gray = getResources().getColor(android.R.color.darker_gray);
            tvProfile.setTextColor(gray);
            tvHome.setTextColor(gray);
            tvCalendar.setTextColor(gray);
            tvAi.setTextColor(gray);
        };

// 처음엔 홈을 선택된 상태로
        resetTabColors.run();
        tvHome.setTextColor(getResources().getColor(android.R.color.black));

// 클릭 이벤트
        navProfile.setOnClickListener(v -> {
            resetTabColors.run();
            tvProfile.setTextColor(getResources().getColor(android.R.color.black));
            startActivity(new Intent(this, Profile_UI.class));
        });

        navHome.setOnClickListener(v -> {
            resetTabColors.run();
            tvHome.setTextColor(getResources().getColor(android.R.color.black));
            // 현재 페이지이므로 이동 없음
        });

        navCalendar.setOnClickListener(v -> {
            resetTabColors.run();
            tvCalendar.setTextColor(getResources().getColor(android.R.color.black));
            startActivity(new Intent(this, CalendarActivity.class));
        });

        navAi.setOnClickListener(v -> {
            resetTabColors.run();
            tvAi.setTextColor(getResources().getColor(android.R.color.black));
            startActivity(new Intent(this, RoutineMainActivity.class));
        });


        // 현재 페이지가 홈이므로 navHome 클릭 이벤트 없음
    }

    // 🔹 공간 불러오기
    private void fetchSpacesFromServer(int userId) {
        SpaceApi api = ApiClient.getClient().create(SpaceApi.class);
        api.getSpacesByUser(userId).enqueue(new Callback<List<Space>>() {
            @Override
            public void onResponse(Call<List<Space>> call, Response<List<Space>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    spaceGrid.removeAllViews();
                    for (Space space : response.body()) {
                        addSpaceCard(space.getName(), R.drawable.ic_room); // 아이콘은 임의로
                    }
                } else {
                    Toast.makeText(Main_UI.this, "공간을 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Space>> call, Throwable t) {
                Toast.makeText(Main_UI.this, "서버 연결 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 🔹 공간 카드 생성
    private void addSpaceCard(String name, int imageResId) {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setGravity(Gravity.CENTER);
        container.setBackgroundColor(0xFFDADADA);
        container.setPadding(16, 24, 16, 24);

        ImageView icon = new ImageView(this);
        icon.setImageResource(imageResId);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(96, 96);
        icon.setLayoutParams(iconParams);

        TextView tv = new TextView(this);
        tv.setText(name);
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(0, 8, 0, 0);

        container.addView(icon);
        container.addView(tv);

        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        params.setMargins(8, 8, 8, 8);
        container.setLayoutParams(params);

        container.setOnClickListener(v -> {
            Intent intent = new Intent(Main_UI.this, CleaningList_UI.class);
            intent.putExtra("space_name", name);
            startActivity(intent);
        });

        spaceGrid.addView(container);
    }

    // 🔹 할 일 추가
    private void addTodoItem(String content) {
        TextView tv = new TextView(this);
        tv.setText("· " + content);
        tv.setTextSize(14);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 8, 0, 8);
        tv.setLayoutParams(params);
        todoListLayout.addView(tv);
    }
}