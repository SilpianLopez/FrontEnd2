package com.example.frontend2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.frontend2.api.ApiClient;
import com.example.frontend2.api.RoutineApi;
import com.example.frontend2.models.CleaningRoutine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CleaningList_UI extends AppCompatActivity implements CleaningListAdapter.OnCleaningEditListener {

    private static final String TAG = "CleaningList_UI";
    private Toolbar toolbar;
    private int spaceIdFromIntent = -1;
    private String spaceNameFromIntent;
    private int currentUserId = -1;

    private RecyclerView recyclerView;
    private CleaningListAdapter adapter;
    private ImageView btnAddRoutine;

    private RoutineApi routineApiService;

    private static final String PREFS_NAME = "UserPrefs";  // 여기 기존 유지
    private static final String KEY_USER_ID = "user_id";

    private ActivityResultLauncher<Intent> addOrEditRoutineLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cleaning_list_ui);

        initIntentAndUser();
        initToolbar();
        initRecyclerView();
        initRetrofit();
        initResultLauncher();
        initAddButton();

        fetchRoutinesForSpace(currentUserId, spaceIdFromIntent);
    }

    private void initIntentAndUser() {
        Intent intent = getIntent();
        if (intent != null) {
            spaceNameFromIntent = intent.getStringExtra("space_name");
            spaceIdFromIntent = intent.getIntExtra("space_id", -1);
            currentUserId = intent.getIntExtra("userId", -1);
        }
        if (currentUserId == -1) {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            currentUserId = prefs.getInt(KEY_USER_ID, -1);
        }
        if (currentUserId == -1 || spaceIdFromIntent == -1) {
            Toast.makeText(this, "사용자 또는 공간 정보가 없습니다.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initToolbar() {
        toolbar = findViewById(R.id.toolbar_clist);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(spaceNameFromIntent + " 루틴 목록");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initRecyclerView() {
        recyclerView = findViewById(R.id.rv_clist);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CleaningListAdapter(this, new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);
    }

    private void initRetrofit() {
        routineApiService = ApiClient.getClient().create(RoutineApi.class);
    }

    private void initResultLauncher() {
        addOrEditRoutineLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        fetchRoutinesForSpace(currentUserId, spaceIdFromIntent);
                    }
                }
        );
    }

    private void initAddButton() {
        btnAddRoutine = findViewById(R.id.im_cadd);
        btnAddRoutine.setOnClickListener(v -> {
            Intent addIntent = new Intent(this, CleaningAdd_UI.class);
            addIntent.putExtra("userId", currentUserId);
            addIntent.putExtra("spaceId", spaceIdFromIntent);
            addOrEditRoutineLauncher.launch(addIntent);
        });
    }

    private void fetchRoutinesForSpace(int userId, int spaceId) {
        routineApiService.getRoutinesByUserAndSpace(userId, spaceId).enqueue(new Callback<List<CleaningRoutine>>() {
            @Override
            public void onResponse(@NonNull Call<List<CleaningRoutine>> call, @NonNull Response<List<CleaningRoutine>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setItems(response.body());
                } else {
                    adapter.setItems(new ArrayList<>());
                    Toast.makeText(CleaningList_UI.this, "루틴 불러오기 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<CleaningRoutine>> call, @NonNull Throwable t) {
                adapter.setItems(new ArrayList<>());
                Toast.makeText(CleaningList_UI.this, "네트워크 오류", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onEditRequested(CleaningRoutine routineToEdit) {
        Intent editIntent = new Intent(this, CleaningAdd_UI.class);
        editIntent.putExtra("mode", "edit");
        editIntent.putExtra("userId", currentUserId);
        editIntent.putExtra("spaceId", routineToEdit.getSpace_id());
        editIntent.putExtra("routineIdToEdit", routineToEdit.getRoutine_id());
        editIntent.putExtra("currentTitle", routineToEdit.getTitle());
        editIntent.putExtra("currentDescription", routineToEdit.getDescription());
        editIntent.putExtra("currentRepeatUnit", routineToEdit.getRepeat_unit());
        if (routineToEdit.getRepeat_interval() != null) {
            editIntent.putExtra("currentRepeatInterval", routineToEdit.getRepeat_interval());
        }
        addOrEditRoutineLauncher.launch(editIntent);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
