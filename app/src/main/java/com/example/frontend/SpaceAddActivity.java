package com.example.frontend;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

public class SpaceAddActivity extends AppCompatActivity {

    private EditText etSpaceName, etFurniture;
    private Spinner spinnerSpaceType;
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_space_add);

        etSpaceName = findViewById(R.id.etSpaceName);
        etFurniture = findViewById(R.id.etFurniture);
        spinnerSpaceType = findViewById(R.id.spinnerSpaceType);
        btnSave = findViewById(R.id.btnSave);

        // 공간 종류 Spinner 세팅
        String[] types = {"거실", "침실", "화장실", "옷방"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, types);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSpaceType.setAdapter(adapter);

        Intent intent = getIntent();
        if ("edit".equals(intent.getStringExtra("mode"))) {
            etSpaceName.setText(intent.getStringExtra("spaceName"));
            etFurniture.setText(intent.getStringExtra("furniture"));

            String type = intent.getStringExtra("spaceType");
            ArrayAdapter<String> spinnerAdapter = (ArrayAdapter<String>) spinnerSpaceType.getAdapter();
            int index = spinnerAdapter.getPosition(type);
            spinnerSpaceType.setSelection(index);
        }


        btnSave.setOnClickListener(v -> {
            String name = etSpaceName.getText().toString();
            String type = spinnerSpaceType.getSelectedItem().toString();
            String furniture = etFurniture.getText().toString();

            if (name.isEmpty() || furniture.isEmpty()) {
                Toast.makeText(this, "모든 항목을 입력하세요.", Toast.LENGTH_SHORT).show();
                return;
            }


            Intent resultIntent = new Intent();
            resultIntent.putExtra("spaceName", name);
            resultIntent.putExtra("spaceType", type);
            resultIntent.putExtra("furniture", furniture);
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        });
    }
}
