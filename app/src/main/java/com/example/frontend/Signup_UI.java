package com.example.frontend;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class Signup_UI extends AppCompatActivity {
    Button btn_complete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_ui);

        btn_complete = findViewById(R.id.btn_complete);
        //완료 버튼 클릭 시 로그인 화면으로 전환
        btn_complete.setOnClickListener(item -> {
            Intent intent = new Intent(Signup_UI.this, Login_UI.class);
            startActivity(intent);
        });
    }
}
