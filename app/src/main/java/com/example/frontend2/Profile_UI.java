package com.example.frontend2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

public class Profile_UI extends AppCompatActivity {
    ImageView im_prfimg, im_edit;
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

        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navCalendar = findViewById(R.id.navCalendar);
        LinearLayout navAi = findViewById(R.id.navAi);
        LinearLayout navProfile = findViewById(R.id.navProfile);

        // 프로필 아이콘 클릭 시 프로필 화면으로 전환
        navProfile.setOnClickListener(item -> {

        });

        // 홈 아이콘 클릭 시 메인화면으로 전환
        navHome.setOnClickListener(item -> {
            Intent intent = new Intent(Profile_UI.this, Main_UI.class);
            startActivity(intent);
        });

        // 캘린더 아이콘 클릭 시 캘린더 화면으로 전환
        navCalendar.setOnClickListener(item -> {
            Intent intent = new Intent(Profile_UI.this, CalendarActivity.class);
            startActivity(intent);
        });

        // AI 아이콘 클릭 시 AI 화면으로 전환
        navAi.setOnClickListener(item -> {
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
