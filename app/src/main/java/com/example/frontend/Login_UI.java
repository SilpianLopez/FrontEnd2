package com.example.frontend;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.frontend.api.ApiClient;
import com.example.frontend.api.UserApi;
import com.example.frontend.models.LoginRequest;
import com.example.frontend.models.User;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Login_UI extends AppCompatActivity {

    Button btn_login;
    TextView text_signup;
    EditText input_email, input_password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_ui);

        btn_login = findViewById(R.id.btn_login);
        text_signup = findViewById(R.id.text_signup);
        input_email = findViewById(R.id.edit_email);
        input_password = findViewById(R.id.edit_password);

        // 🔹 로그인 버튼 클릭 시 서버 요청
        btn_login.setOnClickListener(view -> {
            String email = input_email.getText().toString();
            String password = input_password.getText().toString();

            LoginRequest loginRequest = new LoginRequest(email, password);
            UserApi api = ApiClient.getClient().create(UserApi.class);
            Call<User> call = api.login(loginRequest);

            call.enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        // ✅ 로그인 성공 시 SharedPreferences 저장
                        SharedPreferences prefs = getSharedPreferences("CleanItPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putInt("user_id", response.body().getUser_id());
                        editor.apply();

                        Toast.makeText(Login_UI.this, "환영합니다 " + response.body().getName(), Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(Login_UI.this, Main_UI.class);
                        startActivity(intent);
                    } else {
                        Toast.makeText(Login_UI.this, "로그인 실패: 이메일/비밀번호 확인", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    Toast.makeText(Login_UI.this, "서버 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        // 🔹 회원가입 텍스트 클릭 시 회원가입 화면으로 전환
        text_signup.setOnClickListener(view -> {
            Intent intent = new Intent(Login_UI.this, Signup_UI.class);
            startActivity(intent);
        });
    }
}
