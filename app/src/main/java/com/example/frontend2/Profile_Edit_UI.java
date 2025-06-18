package com.example.frontend2;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.frontend2.api.ApiClient;
import com.example.frontend2.api.UserApi;
import com.example.frontend2.models.User;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Profile_Edit_UI extends AppCompatActivity {

    private static final String TAG = "Profile_Edit_UI";
    private static final String PREFS_NAME = "CleanItAppPrefs";  // ✅ 통일
    private static final String KEY_USER_ID = "logged_in_user_id";

    Toolbar toolbar;
    Button btn_save;
    EditText edit_name, edit_family, edit_pet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_edit_ui);

        toolbar = findViewById(R.id.toolbar_prfedit);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("프로필 편집");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        edit_name = findViewById(R.id.edit_name);
        edit_family = findViewById(R.id.edit_family);
        edit_pet = findViewById(R.id.edit_pet);
        btn_save = findViewById(R.id.btn_save);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int userId = prefs.getInt(KEY_USER_ID, -1);
        if (userId == -1) {
            Toast.makeText(this, "로그인 정보가 없습니다", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadUserInfo(userId);
        setupSaveButton(userId);
    }

    private void loadUserInfo(int userId) {
        UserApi userApi = ApiClient.getClient().create(UserApi.class);
        userApi.getUserById(userId).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    edit_name.setText(user.getName());
                    edit_family.setText(String.valueOf(user.getFamily_count()));
                    edit_pet.setText(String.valueOf(user.getPet_count()));
                } else {
                    Log.e(TAG, "사용자 정보 응답 실패: " + response.code());
                    Toast.makeText(Profile_Edit_UI.this, "사용자 정보를 불러오지 못했습니다", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e(TAG, "사용자 정보 불러오기 실패: " + t.getMessage());
                Toast.makeText(Profile_Edit_UI.this, "서버 오류", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSaveButton(int userId) {
        btn_save.setOnClickListener(v -> {
            String name = edit_name.getText().toString().trim();
            int familyCount = safeParseInt(edit_family.getText().toString());
            int petCount = safeParseInt(edit_pet.getText().toString());
            boolean hasPet = petCount > 0;

            User updatedUser = new User();
            updatedUser.setName(name);
            updatedUser.setFamily_count(familyCount);
            updatedUser.setPet_count(petCount);
            updatedUser.setHas_pet(hasPet);

            UserApi userApi = ApiClient.getClient().create(UserApi.class);
            userApi.updateUser(userId, updatedUser).enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(Profile_Edit_UI.this, "저장 완료", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Log.e(TAG, "수정 실패: " + response.code());
                        Toast.makeText(Profile_Edit_UI.this, "수정 실패", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    Log.e(TAG, "수정 요청 실패: " + t.getMessage());
                    Toast.makeText(Profile_Edit_UI.this, "요청 실패", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private int safeParseInt(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
