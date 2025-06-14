package com.example.frontend2;

import android.app.AlertDialog;
import android.content.Context; // Context import 추가
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.frontend2.api.ApiClient;
import com.example.frontend2.api.SpaceApi;
import com.example.frontend2.models.Space;

import java.io.IOException; // IOException import 추가
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SpaceListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private List<Space> spaceList = new ArrayList<>();
    private SpaceAdapter spaceAdapter;

    // **** 🌟🌟🌟 SpaceAddActivity 및 Login_UI와 동일한 SharedPreferences 상수를 정의합니다! 🌟🌟🌟 ****
    public static final String PREFS_NAME_FOR_APP = "CleanItAppPrefs";
    public static final String KEY_USER_ID_FOR_APP = "logged_in_user_id";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_space_list);

        // 툴바 설정
        Toolbar toolbar = findViewById(R.id.myToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("공간 목록");
        }

        toolbar.setNavigationOnClickListener(v -> finish());

        ImageView btnAddSpace = toolbar.findViewById(R.id.btnAddSpace);
        if (btnAddSpace != null) { // NullPointerException 방지를 위한 체크
            btnAddSpace.setOnClickListener(v -> {
                Intent intent = new Intent(SpaceListActivity.this, SpaceAddActivity.class);
                startActivityForResult(intent, 101);
            });
        } else {
            Log.e("SpaceListActivity", "btnAddSpace ImageView (R.id.btnAddSpace) not found in Toolbar. Please check your XML.");
        }


        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        spaceAdapter = new SpaceAdapter(this, spaceList);
        recyclerView.setAdapter(spaceAdapter);

        // 바텀시트 콜백 연결
        spaceAdapter.setOnSpaceEditListener(new SpaceAdapter.OnSpaceEditListener() {
            @Override
            public void onEditRequested(int position, Space space) {
                Intent intent = new Intent(SpaceListActivity.this, SpaceAddActivity.class);
                intent.putExtra("mode", "edit");
                intent.putExtra("space_id", space.getSpace_id());
                intent.putExtra("name", space.getName());
                intent.putExtra("type", space.getType());
                intent.putExtra("furniture", space.getFurniture());
                startActivityForResult(intent, 101);
            }

            @Override
            public void onDeleteRequested(int position, Space space) {
                new AlertDialog.Builder(SpaceListActivity.this)
                        .setTitle("삭제 확인")
                        .setMessage("정말 삭제하시겠습니까?")
                        .setPositiveButton("삭제", (dialog, which) -> {
                            deleteSpaceFromServer(space.getSpace_id());
                        })
                        .setNegativeButton("취소", null)
                        .show();
            }
        });

        // 사용자 ID 확인
        // **** 🌟🌟🌟 여기서부터 수정 시작! 🌟🌟🌟 ****
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME_FOR_APP, Context.MODE_PRIVATE);
        int userId = prefs.getInt(KEY_USER_ID_FOR_APP, -1);
        if (userId == -1) {
            Toast.makeText(this, "로그인 정보가 없습니다. 다시 로그인해주세요.", Toast.LENGTH_LONG).show();
            Log.e("SpaceListActivity", "User ID is -1 on onCreate. Not logged in or SharedPreferences issue. Navigating to login.");
            // 로그인 화면으로 강제 이동 (선택 사항)
            Intent loginIntent = new Intent(this, Login_UI.class);
            loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(loginIntent);
            finish();
            return;
        }
        // **** 🌟🌟🌟 여기까지 수정 완료! 🌟🌟🌟 ****

        // 공간 목록 불러오기
        fetchSpacesFromServer(userId);
    }

    private void fetchSpacesFromServer(int userId) {
        Log.d("SpaceListActivity", "fetchSpacesFromServer 호출됨, userId = " + userId);

        SpaceApi api = ApiClient.getClient().create(SpaceApi.class);
        api.getSpacesByUserId(userId).enqueue(new Callback<List<Space>>() {
            @Override
            public void onResponse(Call<List<Space>> call, Response<List<Space>> response) {
                Log.d("SpaceListActivity", "응답 도착 - 성공 여부: " + response.isSuccessful());
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("SpaceListActivity", "응답 받은 공간 수: " + response.body().size());
                    spaceList.clear();
                    spaceList.addAll(response.body());
                    spaceAdapter.notifyDataSetChanged();
                } else {
                    // 에러 바디를 로그에 출력하여 서버 응답 상세 확인
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "No error body";
                        Log.e("SpaceListActivity", "공간 목록 불러오기 실패 - code: " + response.code() + ", message: " + response.message() + ", body: " + errorBody);
                    } catch (IOException e) {
                        Log.e("SpaceListActivity", "Error reading error body for fetchSpacesFromServer", e);
                    }
                    Toast.makeText(SpaceListActivity.this, "공간 목록 불러오기 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Space>> call, Throwable t) {
                Log.e("SpaceListActivity", "서버 연결 오류 (fetchSpacesFromServer): " + t.getMessage(), t);
                Toast.makeText(SpaceListActivity.this, "서버 연결 오류: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void deleteSpaceFromServer(int spaceId) {
        SpaceApi api = ApiClient.getClient().create(SpaceApi.class);
        api.deleteSpace(spaceId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(SpaceListActivity.this, "공간 삭제 성공", Toast.LENGTH_SHORT).show();
                    // **** 🌟🌟🌟 여기서부터 수정 시작! 🌟🌟🌟 ****
                    SharedPreferences prefs = getSharedPreferences(PREFS_NAME_FOR_APP, Context.MODE_PRIVATE);
                    int userId = prefs.getInt(KEY_USER_ID_FOR_APP, -1);
                    if (userId != -1) {
                        fetchSpacesFromServer(userId); // 삭제 후 목록 갱신
                    } else {
                        Log.e("SpaceListActivity", "User ID is -1 after space deletion. Cannot refresh space list.");
                        Toast.makeText(SpaceListActivity.this, "삭제 후 목록 갱신 실패: 로그인 정보 없음", Toast.LENGTH_SHORT).show();
                    }
                    // **** 🌟🌟🌟 여기까지 수정 완료! 🌟🌟🌟 ****
                } else {
                    // 에러 바디를 로그에 출력하여 서버 응답 상세 확인
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "No error body";
                        Log.e("SpaceListActivity", "공간 삭제 실패 - code: " + response.code() + ", message: " + response.message() + ", body: " + errorBody);
                    } catch (IOException e) {
                        Log.e("SpaceListActivity", "Error reading error body for deleteSpaceFromServer", e);
                    }
                    Toast.makeText(SpaceListActivity.this, "공간 삭제 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("SpaceListActivity", "서버 연결 오류 (deleteSpaceFromServer): " + t.getMessage(), t);
                Toast.makeText(SpaceListActivity.this, "서버 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == 101) {
            // 공간 추가/수정 후 목록을 갱신할 때도 동일한 userId 사용
            // **** 🌟🌟🌟 여기서부터 수정 시작! 🌟🌟🌟 ****
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME_FOR_APP, Context.MODE_PRIVATE);
            int userId = prefs.getInt(KEY_USER_ID_FOR_APP, -1);
            if (userId != -1) {
                fetchSpacesFromServer(userId);
            } else {
                Log.e("SpaceListActivity", "User ID is -1 on activity result. Cannot refresh space list.");
                Toast.makeText(SpaceListActivity.this, "목록 갱신 실패: 로그인 정보 없음", Toast.LENGTH_SHORT).show();
            }
            // **** 🌟🌟🌟 여기까지 수정 완료! 🌟🌟🌟 ****
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}