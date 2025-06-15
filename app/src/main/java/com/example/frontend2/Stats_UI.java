package com.example.frontend2;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.frontend2.api.ApiClient;
import com.example.frontend2.api.CleaningLogApi;
import com.example.frontend2.models.MonthlyLogStat;
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
    private CleaningLogApi api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stats_ui);  // ✅ xml 파일명 유지


        Toolbar toolbar = findViewById(R.id.toolbar_stats);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        barChart = findViewById(R.id.barchart);
        encouragementTextView = findViewById(R.id.encouragementTextView); // ✅ 응원멘트 TextView 연결
        api = ApiClient.getClient().create(CleaningLogApi.class);

        SharedPreferences prefs = getSharedPreferences(Login_UI.PREFS_NAME_FOR_APP, MODE_PRIVATE);
        int userId = prefs.getInt(Login_UI.KEY_USER_ID_FOR_APP, -1);

        if (userId != -1) {
            fetchMonthlyStats(userId);   // ✅ 바차트 호출
            fetchSpaceStats(userId);     // ✅ 공간별 통계 호출
        } else {
            Log.e("통계", "로그인 정보 없음");
        }
    }

    // ✅ 월별 통계 호출 (바차트용)
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

    // ✅ 공간별 통계 호출 (응원멘트용)
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

    // ✅ 바차트 생성
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

    // ✅ 응원멘트 생성
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
}
