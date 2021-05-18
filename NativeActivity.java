package com.hmc.nativetoflutter;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class NativeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_native);
        TextView textView = findViewById(R.id.tv_text);
        textView.setText(getIntent().getStringExtra("name"));
    }

    //返回
    public void backFlutter(View view) {
        Intent intent = new Intent();
        intent.putExtra("message", "这是原生页面返回的数据");
        setResult(RESULT_OK, intent);
        finish();
    }
}
