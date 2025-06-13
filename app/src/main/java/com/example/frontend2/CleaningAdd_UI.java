package com.example.frontend2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class CleaningAdd_UI extends AppCompatActivity {
    Toolbar toolbar;
    Spinner spunit, spvalue;
    Button btnsave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cleaning_add_ui);
        // 청소 항목 추가 툴바
        toolbar = findViewById(R.id.toolbar_cadd);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("청소 항목 추가");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);  // <- 버튼 표시
        }
        // 청소 주기 스피너
        spunit = findViewById(R.id.sp_unit);
        spvalue = findViewById(R.id.sp_value);
        // 단위: 매일, 매주, 매달
        String[] unitItems = {"매일", "매주", "매달"};
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(this, R.layout.spinner_item_white, unitItems);
        unitAdapter.setDropDownViewResource(R.layout.spinner_item_white);
        spunit.setAdapter(unitAdapter);
        // 숫자: 1 ~ 10일
        String[] valueitems = new String[10];
        for (int i = 0; i < 10; i++) {
            valueitems[i] = String.valueOf(i + 1);
        }
        ArrayAdapter<String> valueAdapter = new ArrayAdapter<>(this, R.layout.spinner_item_white, valueitems);
        unitAdapter.setDropDownViewResource(R.layout.spinner_item_white);
        spvalue.setAdapter(valueAdapter);

        //TODO: 수정 기능
        EditText etName = findViewById(R.id.et_name);
        EditText etComment = findViewById(R.id.et_comment);

        Intent intent = getIntent();
        if (intent != null && "edit".equals(intent.getStringExtra("mode"))) {
            etName.setText(intent.getStringExtra("name"));
            etComment.setText(intent.getStringExtra("comment"));

            String cycle = intent.getStringExtra("cycle");
            if (cycle != null && cycle.length() >= 2) {
                String valuePart = cycle.substring(0, cycle.length() - 1);
                String unitPart = cycle.substring(cycle.length() - 1);

                try {
                    int valueIndex = Integer.parseInt(valuePart) - 1;
                    if (valueIndex >= 0 && valueIndex < spvalue.getCount()) {
                        spvalue.setSelection(valueIndex);
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }

                switch (unitPart) {
                    case "일": spunit.setSelection(0); break;
                    case "주": spunit.setSelection(1); break;
                    case "달": spunit.setSelection(2); break;
                }
            }
        }//TODO: 여기까지 수정 기능



        // 저장 버튼('저장 완료' 메시지만 뜨게 함)
        btnsave = findViewById(R.id.btn_save);
        btnsave.setOnClickListener(view -> {
            String name = etName.getText().toString();
            String comment = etComment.getText().toString();
            String unit = spunit.getSelectedItem().toString();  // 매일, 매주, 매달
            String value = spvalue.getSelectedItem().toString(); // 1 ~ 10
            String cycle = value + (unit.equals("매일") ? "일" : unit.equals("매주") ? "주" : "달");

            Intent resultIntent = new Intent();
            resultIntent.putExtra("name", name);
            resultIntent.putExtra("cycle", cycle);
            resultIntent.putExtra("comment", comment);
            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
