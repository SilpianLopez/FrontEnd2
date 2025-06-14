package com.example.frontend2;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.frontend2.api.ApiClient;
import com.example.frontend2.api.StatsApi;
import com.example.frontend2.models.StatsResponse;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Stats_UI extends AppCompatActivity {
    Toolbar toolbar;
    BarChart barchart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stats_ui);
        // 청소 통계 툴바
        toolbar = findViewById(R.id.toolbar_stats);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("청소 통계");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);  // <- 버튼 표시
        }
        // 통계 그래프
        barchart = findViewById(R.id.barchart);

        StatsApi api = ApiClient.getClient().create(StatsApi.class);
        api.getSpaceCleaningCounts().enqueue(new Callback<List<StatsResponse>>() {
            @Override
            public void onResponse(Call<List<StatsResponse>> call, Response<List<StatsResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<StatsResponse> statslist = response.body();

                    List<BarEntry> entries = new ArrayList<>();
                    List<String> labels = new ArrayList<>();

                    for (int i = 0; i < statslist.size(); i++) {
                        StatsResponse item = statslist.get(i);
                        entries.add(new BarEntry(i, item.getTotal_cleaning_count()));
                        labels.add(item.getSpace_name());
                    }
                    BarDataSet dataSet = new BarDataSet(entries, "청소 횟수");
                    dataSet.setColor(Color.parseColor("#4A90E2"));
                    dataSet.setValueTextColor(Color.BLACK);
                    dataSet.setValueTextSize(14f);

                    BarData barData = new BarData(dataSet);
                    barchart.setData(barData);

                    XAxis xAxis = barchart.getXAxis();
                    xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
                    xAxis.setGranularity(1f);
                    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                    xAxis.setDrawGridLines(false);

                    YAxis yAxis = barchart.getAxisLeft();
                    yAxis.setAxisMaximum(10f);
                    yAxis.setAxisMinimum(0f);
                    yAxis.setGranularity(2f);
                    yAxis.setLabelCount(6, true);
                    yAxis.setDrawGridLines(true);

                    barchart.getDescription().setEnabled(false);
                    barchart.getLegend().setEnabled(false);
                    barchart.getAxisRight().setEnabled(false);
                    barchart.invalidate();
                }
            }

            @Override
            public void onFailure(Call<List<StatsResponse>> call, Throwable t) {
                Log.e("API", "통계 조회 실퍠", t);
            }
        });
    }
    // 툴바 <- 버튼 기능 구현
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return  true;
    }
}
