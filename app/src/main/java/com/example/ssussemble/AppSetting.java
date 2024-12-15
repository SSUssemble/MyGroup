package com.example.ssussemble;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.app.AlertDialog;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class AppSetting extends AppCompatActivity {
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_setting);

        mAuth = FirebaseAuth.getInstance();

        ListView listview = findViewById(R.id.listView);

        String[] listviewItem = {"다크모드 설정", "비밀번호 변경", "로그아웃"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listviewItem);
        listview.setAdapter(adapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 0) {// 다크모드 설정 clicked
                    Intent intent = new Intent(AppSetting.this, DarkModeSetting.class);
                    startActivity(intent);
                }else if(i == 1){//비밀번호 변경
                    Intent intent = new Intent(AppSetting.this, EditPassword.class);
                    startActivity(intent);
                }else if(i == 2) {//로그아웃
                    performLogout();
                }
            }
        });
    }

    private void performLogout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("로그아웃 확인")
                .setMessage("정말 로그아웃 하시겠습니까?")
                .setPositiveButton("예", (dialog, which) -> {

                    // Firebase 로그아웃
                    mAuth.signOut();

                    // SharedPreferences에서 로그인 정보 삭제
                    SharedPreferences mPref = getSharedPreferences("LoginData", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = mPref.edit();
                    editor.remove("LoginId");
                    editor.remove("LoginPassword");
                    editor.apply();

                    MainActivity.Login_id = null;
                    MainActivity.Login_password = null;

                    Intent intent = new Intent(AppSetting.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);

                    SharedPreferences sharedPrefs = getSharedPreferences("AppSettings", Context.MODE_PRIVATE);
                    SharedPreferences.Editor appEditor = sharedPrefs.edit();
                    appEditor.putBoolean("isLoggedOut", true);
                    appEditor.apply();

                    finish();
                })
                .setNegativeButton("아니오", (dialog, which) -> dialog.dismiss())
                .show();
    }

}