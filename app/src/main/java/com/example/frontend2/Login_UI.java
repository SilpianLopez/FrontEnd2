package com.example.frontend2;

import android.content.Context; // Context import 추가
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
import com.example.frontend2.models.User; // LoginResponse 대신 User 모델 사용으로 보입니다.

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

    // **** 🌟🌟🌟 SpaceAddActivity, SpaceListActivity와 동일한 상수를 정의합니다! 🌟🌟🌟 ****
    // 앱 전체에서 공유되는 SharedPreferences 파일 이름
    public static final String PREFS_NAME_FOR_APP = "CleanItAppPrefs";
    // 로그인된 사용자 ID를 저장하는 SharedPreferences 키
    public static final String KEY_USER_ID_FOR_APP = "logged_in_user_id";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_ui); // login_ui.xml 레이아웃 사용

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
            // LoginResponse 대신 User 모델을 사용하고 있음을 확인했습니다.
            Call<User> call = api.login(loginRequest);

            call.enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        User loggedInUser = response.body(); // User 객체로 받음
                        int userId = loggedInUser.getUser_id(); // User 객체에서 user_id 가져옴
                        String userName = loggedInUser.getName();

                        // **** 🌟🌟🌟 SharedPreferences 저장 로직 수정! 🌟🌟🌟 ****
                        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME_FOR_APP, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putInt(KEY_USER_ID_FOR_APP, userId); // 통일된 키 사용
                        editor.putString("user_name", userName); // user_name은 기존과 동일하게 저장
                        editor.apply();

                        // 로그인 성공 로그 및 SharedPreferences 저장 확인 로그 추가
                        Log.d("Login_UI", "▶▶ 서버가 준 user_id = " + userId);
                        Log.d("Login_UI", "User ID " + userId + " saved to SharedPreferences with key '" + KEY_USER_ID_FOR_APP + "'");
                        // **** 🌟🌟🌟 여기까지 수정 완료! 🌟🌟🌟 ****


                        Toast.makeText(Login_UI.this, "환영합니다 " + userName, Toast.LENGTH_SHORT).show();

                        // 로그인 성공 시 MainActivity로 이동 (혹은 공간 목록 액티비티로)
                        Intent intent = new Intent(Login_UI.this, Main_UI.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // 스택 클리어
                        startActivity(intent);
                        finish(); // 현재 액티비티 종료
                    } else {
                        // 로그인 실패 처리
                        String errorMessage = "로그인 실패: ";
                        if (response.errorBody() != null) {
                            try {
                                // 서버에서 보낸 에러 메시지 파싱 시도
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

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    Log.e("Login_UI", "Login API call failed", t);
                    Toast.makeText(Login_UI.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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