package com.hmc.nativetoflutter;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * 原生跳转到flutter过渡的页面
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


    }

    public void enterFlutter(View view) {
        Bundle bundle = new Bundle();
        bundle.putString("routeName", "/first");
        bundle.putString("arguments", "?{\"name\":\"" + "第一页数据11" + "\"}");
        TransitionActivity.openFlutter(MainActivity.this,bundle);
    }

    public void enterFlutter2(View view) {
        Intent intent = new Intent(this, TransitionActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("routeName", "/second");
        bundle.putString("arguments", "?{\"name\":\"" + "第二页数据22" + "\"}");
        intent.putExtras(bundle);
        startActivityForResult(intent,100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 100 ) {
            Log.e("TAG", "====flutter页面返回的数据===="+data.getStringExtra("message"));
        }
    }
}
