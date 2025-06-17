package com.example.frontend2;

import android.content.Context;
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

    public static final String PREFS_NAME_FOR_APP = "CleanItAppPrefs";
    public static final String KEY_USER_ID_FOR_APP = "logged_in_user_id";
    public static final String KEY_USER_NAME_FOR_APP = "user_name"; // 상수 분리

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_ui);

        btn_login = findViewById(R.id.btn_login);
        text_signup = findViewById(R.id.text_signup);
        input_email = findViewById(R.id.edit_email);
        input_password = findViewById(R.id.edit_password);

        // ✅ 비밀번호 마스킹 (이 부분은 수아님 코드 그대로 유지)
        input_password.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isUpdating) return;

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

                isUpdating = true;
                StringBuilder display = new StringBuilder();
                for (int i = 0; i < realPassword.length(); i++) display.append("●");
                if (realPassword.length() > 0)
                    display.setCharAt(realPassword.length() - 1, realPassword.charAt(realPassword.length() - 1));
                input_password.setText(display.toString());
                input_password.setSelection(display.length());
                isUpdating = false;
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // 🔹 로그인 버튼 클릭 시 서버 요청
        btn_login.setOnClickListener(view -> {
            String email = input_email.getText().toString();
            String password = realPassword.toString();

            LoginRequest loginRequest = new LoginRequest(email, password);
            UserApi api = ApiClient.getClient().create(UserApi.class);
            Call<User> call = api.login(loginRequest);

            call.enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        User loggedInUser = response.body();
                        int userId = loggedInUser.getUser_id();
                        String userName = loggedInUser.getName();

                        // ✅ SharedPreferences에 새 로그인 정보 저장
                        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME_FOR_APP, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putInt(KEY_USER_ID_FOR_APP, userId);
                        editor.putString(KEY_USER_NAME_FOR_APP, userName);
                        editor.apply();

                        Log.d("Login_UI", "▶▶ 새로 저장된 user_id = " + userId);

                        Toast.makeText(Login_UI.this, "환영합니다 " + userName, Toast.LENGTH_SHORT).show();

                        // ✅ Main_UI로 이동 (스택 클리어 유지)
                        Intent intent = new Intent(Login_UI.this, Main_UI.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        handleLoginFailure(response);
                    }
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    Log.e("Login_UI", "Login API call failed", t);
                    Toast.makeText(Login_UI.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        // 🔹 회원가입 클릭 → 회원가입 화면 이동
        text_signup.setOnClickListener(view -> {
            Intent intent = new Intent(Login_UI.this, Signup_UI.class);
            startActivity(intent);
        });
    }

    private void handleLoginFailure(Response<User> response) {
        String errorMessage = "로그인 실패: ";
        if (response.errorBody() != null) {
            try {
                String errorBody = response.errorBody().string();
                Log.e("Login_UI", "Login Error Body: " + errorBody);
                if (errorBody.contains("Invalid credentials")) {
                    errorMessage += "이메일 또는 비밀번호가 올바르지 않습니다.";
                } else {
                    errorMessage += "서버 응답 오류: " + errorBody;
                }
            } catch (Exception e) {
                Log.e("Login_UI", "Error parsing error body", e);
                errorMessage += "서버 응답 오류. 상세 내용 확인 불가.";
            }
        } else {
            errorMessage += "서버 응답 없음";
        }
        Log.e("Login_UI", "Login failed: " + response.code() + " " + response.message());
        Toast.makeText(Login_UI.this, errorMessage, Toast.LENGTH_LONG).show();
    }
}
