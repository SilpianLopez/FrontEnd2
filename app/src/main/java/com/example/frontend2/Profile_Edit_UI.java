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
    Toolbar toolbar;
    Button btn_save;
    EditText edit_name, edit_family, edit_pet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_edit_ui);

        // ✅ 툴바 설정
        toolbar = findViewById(R.id.toolbar_prfedit);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("프로필 편집");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // ✅ 입력 필드 초기화
        edit_name = findViewById(R.id.edit_name);
        edit_family = findViewById(R.id.edit_family);
        edit_pet = findViewById(R.id.edit_pet);
        btn_save = findViewById(R.id.btn_save);

        // ✅ 유저 정보 불러오기
        SharedPreferences prefs = getSharedPreferences("CleanItPrefs", MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);

        if (userId != -1) {
            UserApi userApi = ApiClient.getClient().create(UserApi.class);
            userApi.getUserById(userId).enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        User user = response.body();
                        edit_name.setText(user.getName());
                        edit_family.setText(String.valueOf(user.getFamily_count()));
                        edit_pet.setText(String.valueOf(user.getPet_count()));
                    }
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    Log.e("EditProfile", "사용자 정보 불러오기 실패: " + t.getMessage());
                }
            });
        }

        // ✅ 저장 버튼 클릭 처리
        btn_save.setOnClickListener(v -> {
            String name = edit_name.getText().toString().trim();
            int familyCount = Integer.parseInt(edit_family.getText().toString());
            int petCount = Integer.parseInt(edit_pet.getText().toString());
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
                        Toast.makeText(Profile_Edit_UI.this, "수정 실패", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    Log.e("EditProfile", "수정 요청 실패: " + t.getMessage());
                    Toast.makeText(Profile_Edit_UI.this, "요청 실패", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    // ✅ 툴바 뒤로가기 버튼 동작
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
