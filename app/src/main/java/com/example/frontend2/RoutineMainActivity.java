package com.example.frontend2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

public class RoutineMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routine_main);

        Button btnAllRoutine = findViewById(R.id.btnAllRoutine);
        Button btnRoom = findViewById(R.id.btnRoom);
        Button btnLivingRoom = findViewById(R.id.btnLivingRoom);
        Button btnBathroom = findViewById(R.id.btnBathroom);
        Button btnWardrobe = findViewById(R.id.btnWardrobe);

        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navCalendar = findViewById(R.id.navCalendar);
        LinearLayout navAi = findViewById(R.id.navAi);
        LinearLayout navProfile = findViewById(R.id.navProfile);

        navHome.setOnClickListener(v -> {
            Intent intent = new Intent(RoutineMainActivity.this, Main_UI.class);
            startActivity(intent);
            finish();  // 현재 액티비티 종료 (원하면)
        });

        navAi.setOnClickListener(v -> {
            // 현재 페이지 → 아무 작업 안함
        });

        navCalendar.setOnClickListener(v -> {
            Intent intent = new Intent(RoutineMainActivity.this, CalendarActivity.class);
            startActivity(intent);
            finish();  // 현재 액티비티 종료 (원하면)
        });

        navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(RoutineMainActivity.this, Profile_UI.class);
            startActivity(intent);
            finish();  // 현재 액티비티 종료 (원하면)
        });

        btnAllRoutine.setOnClickListener(v -> {
            Intent intent = new Intent(this, RoutineAllActivity.class);
            startActivity(intent);
        });

        View.OnClickListener spaceClickListener = view -> {
            Intent intent = new Intent(this, RoutineDetailActivity.class);

            int id = view.getId();
            if (id == R.id.btnRoom) {
                intent.putExtra("roomName", "침실");
            } else if (id == R.id.btnLivingRoom) {
                intent.putExtra("roomName", "거실");
            } else if (id == R.id.btnBathroom) {
                intent.putExtra("roomName", "화장실");
            } else if (id == R.id.btnWardrobe) {
                intent.putExtra("roomName", "옷방");
            }

            startActivity(intent);
        };


        btnRoom.setOnClickListener(spaceClickListener);
        btnLivingRoom.setOnClickListener(spaceClickListener);
        btnBathroom.setOnClickListener(spaceClickListener);
        btnWardrobe.setOnClickListener(spaceClickListener);
    }
}
