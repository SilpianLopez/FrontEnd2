package com.example.frontend2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class Profile_UI extends AppCompatActivity {
    TextView tvWelcome, tvFamily, tvPet;
    Button btnStats;
    // 하단 네비게이션 아이콘
    // (임시로 뷰만 바인딩해 두고, 클릭 리스너는 기존과 동일하게 설정합니다)
    LinearLayout imProfileIcon, imHomeIcon, imCalendarIcon, imAiIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_ui);

        // 1) 뷰 바인딩
        tvWelcome   = findViewById(R.id.tv_welcome);
        tvFamily    = findViewById(R.id.text_family);
        tvPet       = findViewById(R.id.text_pet);
        btnStats    = findViewById(R.id.btn_stats);

        imProfileIcon  = findViewById(R.id.im_profile);
        imHomeIcon     = findViewById(R.id.im_home);
        imCalendarIcon = findViewById(R.id.im_calendar);
        imAiIcon       = findViewById(R.id.im_ai);

        // 2) SharedPreferences에서 저장된 사용자 이름, 가족, 반려동물 정보 불러오기
        SharedPreferences prefs = getSharedPreferences("UserProfile", MODE_PRIVATE);
        String name   = prefs.getString("name", "사용자");
        String family = prefs.getString("family", "정보 없음");
        String pet    = prefs.getString("pet", "정보 없음");

        // 3) “[이름]님, 환영합니다!” 텍스트 설정
        tvWelcome.setText(name + "님, 환영합니다!");

        // 4) 가족 구성원, 반려동물 텍스트 설정
        tvFamily.setText(family);
        tvPet.setText(pet);

        // 5) “청소 통계 보기” 버튼 클릭 시 Stats_UI로 이동
        btnStats.setOnClickListener(v -> {
            Intent intent = new Intent(Profile_UI.this, Stats_UI.class);
            startActivity(intent);
        });

        // 6) 하단 네비게이션 버튼 클릭 처리 (프로필 / 홈 / 캘린더 / AI)
        imProfileIcon.setOnClickListener(v -> {
            // 현재 프로필 화면이므로 별도 이동 없음
        });
        imHomeIcon.setOnClickListener(v -> {
            startActivity(new Intent(Profile_UI.this, Main_UI.class));
        });
        imCalendarIcon.setOnClickListener(v -> {
            startActivity(new Intent(Profile_UI.this, CalendarActivity.class));
        });
        imAiIcon.setOnClickListener(v -> {
            startActivity(new Intent(Profile_UI.this, RoutineMainActivity.class));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Profile_Edit_UI에서 저장된 값이 변경되었을 수 있으니 다시 불러와서 화면 갱신
        SharedPreferences prefs = getSharedPreferences("UserProfile", MODE_PRIVATE);
        String name   = prefs.getString("name", "사용자");
        String family = prefs.getString("family", "정보 없음");
        String pet    = prefs.getString("pet", "정보 없음");

        tvWelcome.setText(name + "님, 환영합니다!");
        tvFamily.setText(family);
        tvPet.setText(pet);
    }
}
