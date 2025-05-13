package com.example.frontend;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class Login_UI extends AppCompatActivity {
    Button btn_login;
    TextView text_signup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_ui);

        btn_login = findViewById(R.id.btn_login);
        text_signup = findViewById(R.id.text_signup);
        //로그인 버튼 클릭 시 메인화면으로 전환
        btn_login.setOnClickListener(item -> {
            Intent intent = new Intent(Login_UI.this, Main_UI.class);
            startActivity(intent);
        });
        //회원가입 클릭 시 회원가입 화면으로 전환
        text_signup.setOnClickListener(item -> {
            Intent intent = new Intent(Login_UI.this, Signup_UI.class);
            startActivity(intent);
        });

    }
}
