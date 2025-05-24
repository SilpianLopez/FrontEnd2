package com.example.frontend;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.frontend.api.ApiClient;
import com.example.frontend.api.UserApi;
import com.example.frontend.models.RegisterRequest;
import com.example.frontend.models.User;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Signup_UI extends AppCompatActivity {

    EditText editName, editEmail, editPassword;
    Button btnComplete;
    TextView textSignup;  // 🔹 로그인 화면으로 가는 버튼

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_ui);

        editName = findViewById(R.id.edit_name);
        editEmail = findViewById(R.id.edit_email);
        editPassword = findViewById(R.id.edit_password);
        btnComplete = findViewById(R.id.btn_complete);
        textSignup = findViewById(R.id.text_signup); // 🔹 여기 추가

        // 🔹 로그인 화면으로 돌아가기
        textSignup.setOnClickListener(view -> {
            Intent intent = new Intent(Signup_UI.this, Login_UI.class);
            startActivity(intent);
            finish();
        });

        // 🔸 회원가입 버튼 클릭 시 서버 요청
        btnComplete.setOnClickListener(view -> {
            String name = editName.getText().toString().trim();
            String email = editEmail.getText().toString().trim();
            String password = editPassword.getText().toString().trim();

            int family_count = 1;
            boolean has_pet = false;

            RegisterRequest request = new RegisterRequest(name, email, password, family_count, has_pet);
            UserApi api = ApiClient.getClient().create(UserApi.class);
            Call<User> call = api.register(request);

            call.enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(Signup_UI.this, "회원가입 성공!", Toast.LENGTH_SHORT).show();
                        finish(); // 로그인 화면으로 돌아가기
                    } else {
                        Toast.makeText(Signup_UI.this, "회원가입 실패 (이메일 중복 등)", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    Toast.makeText(Signup_UI.this, "서버 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
