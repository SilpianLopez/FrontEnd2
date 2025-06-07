package com.example.frontend2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.frontend2.api.ApiClient;
import com.example.frontend2.api.UserApi;
import com.example.frontend2.models.LoginRequest;
import com.example.frontend2.models.User;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Login_UI extends AppCompatActivity {

    Button btn_login;
    TextView text_signup;
    EditText input_email, input_password;

    private final StringBuilder realPassword = new StringBuilder();
    private final Handler handler = new Handler();
    private final SparseArray<Runnable> maskRunnables = new SparseArray<>();
    private boolean isUpdating = false;
    private final int MASK_DELAY = 1500; // 1.5초 뒤 마스킹

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_ui);

        btn_login = findViewById(R.id.btn_login);
        text_signup = findViewById(R.id.text_signup);
        input_email = findViewById(R.id.edit_email);
        input_password = findViewById(R.id.edit_password);

        // ✅ 비밀번호 입력 마스킹 처리
        input_password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // 무시
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isUpdating) return;

                // 삭제 처리
                if (before > 0 && count == 0) {
                    for (int i = start; i < start + before; i++) {
                        if (i < realPassword.length()) {
                            realPassword.deleteCharAt(i);
                            Runnable r = maskRunnables.get(i);
                            if (r != null) handler.removeCallbacks(r);
                            maskRunnables.remove(i);
                        }
                    }
                }

                // 추가 처리
                if (count > 0) {
                    for (int i = 0; i < count; i++) {
                        char c = s.charAt(start + i);
                        realPassword.insert(start + i, c);

                        final int index = start + i;

                        Runnable r = () -> {
                            isUpdating = true;
                            StringBuilder masked = new StringBuilder();
                            for (int j = 0; j < realPassword.length(); j++) {
                                masked.append("●");
                            }
                            input_password.setText(masked.toString());
                            input_password.setSelection(masked.length());
                            isUpdating = false;
                        };

                        handler.postDelayed(r, MASK_DELAY);
                        maskRunnables.put(index, r);
                    }
                }

                // 화면에 보여주기: 마지막 글자만 평문으로
                isUpdating = true;
                StringBuilder display = new StringBuilder();
                for (int i = 0; i < realPassword.length(); i++) {
                    display.append("●");
                }
                if (realPassword.length() > 0) {
                    display.setCharAt(realPassword.length() - 1, realPassword.charAt(realPassword.length() - 1));
                }
                input_password.setText(display.toString());
                input_password.setSelection(display.length());
                isUpdating = false;
            }

            @Override
            public void afterTextChanged(Editable s) {
                // 무시
            }
        });

        // 🔹 로그인 버튼 클릭 시 서버 요청
        btn_login.setOnClickListener(view -> {
            String email = input_email.getText().toString();
            String password = realPassword.toString(); // 실제 비밀번호 사용

            LoginRequest loginRequest = new LoginRequest(email, password);
            UserApi api = ApiClient.getClient().create(UserApi.class);
            Call<User> call = api.login(loginRequest);

            call.enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putInt("user_id", response.body().getUser_id());
                        editor.putString("user_name", response.body().getName());
                        editor.apply();

                        int returnedId = response.body().getUser_id();
                        Log.d("Login_UI", "▶▶ 서버가 준 user_id = " + returnedId);

                        Toast.makeText(Login_UI.this, "환영합니다 " + response.body().getName(), Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(Login_UI.this, Main_UI.class);
                        startActivity(intent);
                        finish(); // 로그인 화면 종료
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