package com.example.frontend2;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.frontend2.api.ApiClient;
import com.example.frontend2.api.CleaningLogApi;
import com.example.frontend2.api.CleaningRoutineApi;
import com.example.frontend2.models.CleaningLogRecommendation;
import com.example.frontend2.models.MonthlyLogStat;
import com.example.frontend2.models.RecommendationRoutineRequest;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Stats_UI extends AppCompatActivity {

    private BarChart barChart;
    private TextView encouragementTextView;
    private LinearLayout recommendationContainer;

    private CleaningLogApi api;
    private CleaningRoutineApi routineApi;

    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stats_ui);

        Toolbar toolbar = findViewById(R.id.toolbar_stats);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        barChart = findViewById(R.id.barchart);
        encouragementTextView = findViewById(R.id.encouragementTextView);
        recommendationContainer = findViewById(R.id.recommendationContainer);

        api = ApiClient.getClient().create(CleaningLogApi.class);
        routineApi = ApiClient.getClient().create(CleaningRoutineApi.class);

        SharedPreferences prefs = getSharedPreferences(Login_UI.PREFS_NAME_FOR_APP, MODE_PRIVATE);
        userId = prefs.getInt(Login_UI.KEY_USER_ID_FOR_APP, -1);

        if (userId != -1) {
            fetchMonthlyStats(userId);
            fetchSpaceStats(userId);
            fetchRecommendations(userId);
        } else {
            Log.e("통계", "로그인 정보 없음");
        }
    }

    private void fetchMonthlyStats(int userId) {
        api.getMonthlyLogs(userId).enqueue(new Callback<List<MonthlyLogStat>>() {
            @Override
            public void onResponse(Call<List<MonthlyLogStat>> call, Response<List<MonthlyLogStat>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    showBarChart(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<MonthlyLogStat>> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void showBarChart(List<MonthlyLogStat> stats) {
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        for (int i = 0; i < stats.size(); i++) {
            MonthlyLogStat stat = stats.get(i);
            entries.add(new BarEntry(i, stat.getCount()));
            labels.add(stat.getMonth());
        }

        BarDataSet dataSet = new BarDataSet(entries, "월별 청소 횟수");
        dataSet.setColor(getResources().getColor(R.color.teal_200));
        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.4f);
        barChart.setData(barData);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(labels.size(), true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);

        barChart.setFitBars(true);
        barChart.getDescription().setEnabled(false);
        barChart.getAxisRight().setEnabled(false);
        barChart.animateY(1000);
        barChart.invalidate();
    }

    private void fetchSpaceStats(int userId) {
        api.getSpaceLogs(userId).enqueue(new Callback<Map<String, Integer>>() {
            @Override
            public void onResponse(Call<Map<String, Integer>> call, Response<Map<String, Integer>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    generateEncouragement(response.body());
                }
            }

            @Override
            public void onFailure(Call<Map<String, Integer>> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void generateEncouragement(Map<String, Integer> spaceStats) {
        String leastCleanedSpace = null;
        int minCount = Integer.MAX_VALUE;

        for (Map.Entry<String, Integer> entry : spaceStats.entrySet()) {
            if (entry.getValue() < minCount) {
                minCount = entry.getValue();
                leastCleanedSpace = entry.getKey();
            }
        }

        if (leastCleanedSpace != null) {
            String message = String.format("이번달에는 '%s' 청소가 가장 적어요! 한 번 정리해보는 건 어떨까요? 🧹", leastCleanedSpace);
            encouragementTextView.setText(message);
        }
    }

    private void fetchRecommendations(int userId) {
        api.getRecommendations(userId).enqueue(new Callback<List<CleaningLogRecommendation>>() {
            @Override
            public void onResponse(Call<List<CleaningLogRecommendation>> call, Response<List<CleaningLogRecommendation>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("RECOMMEND_API", "추천 응답: " + response.body().size() + "개");

                    for (CleaningLogRecommendation rec : response.body()) {
                        Log.d("RECOMMEND_API", "추천 아이템: "
                                + "space_id=" + rec.getSpace_id()
                                + ", space_name=" + rec.getSpace_name());
                    }

                    showRecommendations(response.body());
                } else {
                    Log.e("RECOMMEND_API", "응답은 왔으나 body가 null이거나 실패");
                }
            }

            @Override
            public void onFailure(Call<List<CleaningLogRecommendation>> call, Throwable t) {
                Log.e("RECOMMEND_API", "API 호출 실패", t);
            }
        });
    }


    private void showRecommendations(List<CleaningLogRecommendation> list) {
        recommendationContainer.removeAllViews();

        for (CleaningLogRecommendation rec : list) {
            TextView tv = new TextView(this);
            tv.setText(rec.getSpace_name() + " 청소를 다시 해보는 건 어떨까요?");
            tv.setPadding(8, 16, 8, 16);
            tv.setTextSize(16f);
            tv.setOnClickListener(v -> createRoutineFromRecommendation(rec));
            recommendationContainer.addView(tv);
        }
    }

    private void createRoutineFromRecommendation(CleaningLogRecommendation rec) {
        RecommendationRoutineRequest request = new RecommendationRoutineRequest();
        request.setUser_id(userId);
        request.setSpace_id(rec.getSpace_id());
        request.setTitle(rec.getSpace_name() + " 청소");
        request.setRepeat_unit("WEEK");
        request.setRepeat_interval(1);

        // ✅ 여기서 이걸로 호출
        routineApi.createRecommendationRoutine(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                Toast.makeText(Stats_UI.this, "루틴에 추가되었습니다!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(Stats_UI.this, "추가 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }
}