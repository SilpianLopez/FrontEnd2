package com.example.frontend2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class Profile_Edit_UI extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    ImageView im_prfimg, im_imgedit;
    Toolbar toolbar;
    Button btn_save;
    EditText edit_name, edit_family, edit_pet;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_edit_ui);
        // 프로필 편집 툴바
        toolbar = findViewById(R.id.toolbar_prfedit);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("프로필 편집");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);  // <- 버튼 표시
        }

        im_prfimg = findViewById(R.id.im_prfimg);
        im_imgedit = findViewById(R.id.im_imgedit);
        // 앱 실행 시 저장된 이미지가 있다면 로드
        File file = new File(getFilesDir(), "im_user_prfimg");
        if (file.exists()) {
            im_prfimg.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));
        }
        // 사진 편집 아이콘 클릭 시 갤러리 열기
        im_imgedit.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        btn_save = findViewById(R.id.btn_save);
        edit_name = findViewById(R.id.edit_name);
        edit_family = findViewById(R.id.edit_family);
        edit_pet = findViewById(R.id.edit_pet);

        // 기존 값 불러오기
        SharedPreferences prefs = getSharedPreferences("UserProfile", MODE_PRIVATE);
        edit_name.setText(prefs.getString("name", ""));
        edit_family.setText(prefs.getString("family", ""));
        edit_pet.setText(prefs.getString("pet", ""));
        // 저장 버튼 기능 구현
        btn_save.setOnClickListener(v -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("name", edit_name.getText().toString());
            editor.putString("family", edit_family.getText().toString());
            editor.putString("pet", edit_pet.getText().toString());
            editor.apply();

            Toast.makeText(this, "저장 완료", Toast.LENGTH_SHORT).show();
        });
    }

    // 툴바 <- 버튼 기능 구현
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return  true;
    }
    // 이미지 처음 저장 시 'im_user_prfimg'로 저장하고 그 후부터는 덮어쓰게 함
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();

            try {
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                File file = new File(getFilesDir(), "im_user_prfimg");
                OutputStream outputStream = new FileOutputStream(file);

                byte[] buffer = new byte[1024];
                int len;
                while ((len = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, len);
                }
                inputStream.close();
                outputStream.close();

                // 프로필 사진에 표시
                im_prfimg.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));
            }
            catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "이미지 저장 실패", Toast.LENGTH_SHORT). show();
            }
        }
    }
}
