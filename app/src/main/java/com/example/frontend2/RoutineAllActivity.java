package com.example.frontend2;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class RoutineAllActivity extends AppCompatActivity {

    Button btnApply, btnRetry;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routine_all);

        Toolbar toolbar = findViewById(R.id.myToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> finish());

        Button btnApply = findViewById(R.id.btnApplyRoutine);
        Button btnRetry = findViewById(R.id.btnRetryRoutine);

        btnApply.setOnClickListener(v -> {
            Toast.makeText(this, "루틴이 반영되었습니다!", Toast.LENGTH_SHORT).show();
            // 실제 반영 로직이 있다면 여기에 추가
        });

        btnRetry.setOnClickListener(v -> {
            Toast.makeText(this, "루틴을 다시 추천합니다.", Toast.LENGTH_SHORT).show();
            // 추천 로직 새로 불러오기 등 추가 가능
        });

    }
}
