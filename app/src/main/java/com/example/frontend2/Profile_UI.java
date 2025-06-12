package com.example.frontend2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.frontend2.api.ApiClient;
import com.example.frontend2.api.UserApi;
import com.example.frontend2.models.User;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Profile_UI extends AppCompatActivity {
    TextView tvWelcome, tvFamily, tvPet;
    Button btnStats;
    ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_ui);

        tvWelcome = findViewById(R.id.tv_welcome);
        tvFamily = findViewById(R.id.text_family);
        tvPet = findViewById(R.id.text_pet);
        btnStats = findViewById(R.id.btn_stats);
        btnBack = findViewById(R.id.btn_back);

        LinearLayout btnEditProfile = findViewById(R.id.btn_edit_profile);
        btnEditProfile.setOnClickListener(v -> {
            startActivity(new Intent(Profile_UI.this, Profile_Edit_UI.class));
        });

        btnStats.setOnClickListener(v -> {
            startActivity(new Intent(Profile_UI.this, Stats_UI.class));
        });

        btnBack.setOnClickListener(v -> {
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences prefs = getSharedPreferences("CleanItPrefs", MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);

        if (userId == -1) {
            Log.e("Profile", "사용자 ID가 없습니다.");
            return;
        }

        UserApi userApi = ApiClient.getClient().create(UserApi.class);
        userApi.getUserById(userId).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();

                    tvWelcome.setText(user.getName() + "님, 환영합니다!");
                    tvFamily.setText("가족 구성원: " + user.getFamily_count() + "명");

                    String petText = (user.isHas_pet() && user.getPet_count() > 0)
                            ? "반려동물: " + user.getPet_count() + "마리"
                            : "반려동물: 없음";

                    tvPet.setText(petText);
                } else {
                    Log.e("Profile", "사용자 정보 불러오기 실패");
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e("Profile", "API 호출 오류: " + t.getMessage());
            }
        });
    }
}
