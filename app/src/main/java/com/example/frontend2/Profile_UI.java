package com.example.frontend2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

public class Profile_UI extends AppCompatActivity {
    ImageView im_prfimg, im_edit;
    ImageView im_profile, im_home, im_calendar, im_ai;
    TextView text_name, text_family, text_pet;
    Button btn_stats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_ui);

        im_edit = findViewById(R.id.im_edit);
        im_prfimg = findViewById(R.id.im_prfimg);
        // 저장된 이미지가 있으면 불러오기
        File file = new File(getFilesDir(), "im_user_prfimg");
        if (file.exists()) {
            im_prfimg.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));
        }
        //이미지 클릭 시 프로필 편집 화면으로 전환
        im_edit.setOnClickListener(item -> {
            Intent intent = new Intent(Profile_UI.this, Profile_Edit_UI.class);
            startActivity(intent);
        });

        btn_stats = findViewById(R.id.btn_stats);
        // 청소 통계 버튼 클릭 시 청소 통계 화면으로 전환
        btn_stats.setOnClickListener(item -> {
            Intent intent = new Intent(Profile_UI.this, Stats_UI.class);
            startActivity(intent);
        });

        im_profile = findViewById(R.id.im_profile);
        // 프로필 아이콘 클릭 시 프로필 화면으로 전환
        im_profile.setOnClickListener(item -> {
            Intent intent = new Intent(Profile_UI.this, Profile_UI.class);
            startActivity(intent);
        });

        im_home = findViewById(R.id.im_home);
        // 홈 아이콘 클릭 시 메인화면으로 전환
        im_home.setOnClickListener(item -> {
            Intent intent = new Intent(Profile_UI.this, Main_UI.class);
            startActivity(intent);
        });

        im_calendar = findViewById(R.id.im_calendar);
        // 캘린더 아이콘 클릭 시 캘린더 화면으로 전환
        im_calendar.setOnClickListener(item -> {
            Intent intent = new Intent(Profile_UI.this, CalendarActivity.class);
            startActivity(intent);
        });

        im_ai = findViewById(R.id.im_ai);
        // AI 아이콘 클릭 시 AI 화면으로 전환
        im_ai.setOnClickListener(item -> {
            Intent intent = new Intent(Profile_UI.this, RoutineMainActivity.class);
            startActivity(intent);
        });

        text_name = findViewById(R.id.text_name);
        text_family = findViewById(R.id.text_family);
        text_pet = findViewById(R.id.text_pet);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // 프로필 편집에서 저장된 값 불러오기
        SharedPreferences prefs = getSharedPreferences("UserProfile", MODE_PRIVATE);
        text_name.setText(prefs.getString("name", "사용자"));
        text_family.setText(prefs.getString("family", "정보 없음"));
        text_pet.setText(prefs.getString("pet", "정보 없음"));

        File file = new File(getFilesDir(), "im_user_prfimg");
        if (file.exists()) {
            im_prfimg.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));
        }
    }
}
