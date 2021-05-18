package com.hmc.nativetoflutter;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import io.flutter.embedding.android.FlutterView;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.embedding.engine.dart.DartExecutor;
import io.flutter.plugin.common.MethodChannel;

public class TransitionActivity extends AppCompatActivity {
    private static final String BATTERY_CHANNEL = "samples.flutter.io/battery";
    private  MethodChannel methodChannel;
    FlutterEngine flutterEngine;
    String routeName = "";//路由名
    String arguments; //参数数据

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transition);
        getData();
        initFlutterView();

    }

    private void getData() {
        // 获取由上一个页面传过来的routeName
        Intent intent = getIntent();
        if (intent != null && intent.getExtras() != null) {
            routeName = intent.getStringExtra("routeName");
            arguments = intent.getStringExtra("arguments");
        }
    }

    private void initFlutterView() {
        // 通过FlutterView引入Flutter编写的页面
        FlutterView flutterView = new FlutterView(this);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        FrameLayout flContainer = findViewById(R.id.fl_container);
        flContainer.addView(flutterView, lp);
        //flutterEngine = FlutterEngineCache.getInstance().get("my_engine_id");
        if(flutterEngine == null) {
            flutterEngine = new FlutterEngine(this);
        }

        // 设置初始路由并将参数拼接后传递
        flutterEngine.getNavigationChannel().setInitialRoute(routeName+arguments);
        // 开始执行dart代码来pre-warm FlutterEngine
        flutterEngine.getDartExecutor().executeDartEntrypoint(
                DartExecutor.DartEntrypoint.createDefault()
        );
        //将flutter引擎缓存起来
        //FlutterEngineCache.getInstance().put("my_engine_id", flutterEngine);
        // 关键代码，将Flutter页面显示到FlutterView中
        flutterView.attachToFlutterEngine(this.flutterEngine);
        //创建methodChannel对象
        methodChannel = new MethodChannel(this.flutterEngine.getDartExecutor(), BATTERY_CHANNEL);
        methodChannel.setMethodCallHandler((call, result) -> {
            switch (call.method){
                case "getBatteryLevel":
                    int batteryLevel = getBattery();
                    if (batteryLevel != -1) {
                        result.success(batteryLevel);
                    } else {
                        result.error("UNAVAILABLE", "Battery level not available.", null);
                    }
                    break;
                case "activityGoBack":
                    // 返回上一页，并携带数据
                    Intent backIntent = new Intent();
                    backIntent.putExtra("message", (String) call.argument("flutterBack"));
                    setResult(RESULT_OK, backIntent);
                    finish();
                    break;
                case "jumpToNative":
                    //flutter跳转到原生页面
                    Intent intent  = new Intent(TransitionActivity.this,NativeActivity.class);
                    intent.putExtra("name", (String) call.argument("name"));
                    startActivityForResult(intent,101);
                    break;
                default:
                    result.notImplemented();
                    break;
            }
        });
    }

    @Override
    public void onBackPressed() {
        methodChannel.invokeMethod("goBack", null);
    }

    private int getBattery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            BatteryManager batteryManager = (BatteryManager) getSystemService(BATTERY_SERVICE);
            return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        } else {
            Intent intent = new ContextWrapper(getApplicationContext()).
                    registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            return (intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) * 100) /
                    intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        flutterEngine.getLifecycleChannel().appIsResumed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        flutterEngine.getLifecycleChannel().appIsInactive();
    }

    @Override
    protected void onStop() {
        super.onStop();
        flutterEngine.getLifecycleChannel().appIsPaused();
    }

    public static void openFlutter(Context context,Bundle bundle){
        Intent intent = new Intent(context, TransitionActivity.class);
        intent.putExtras(bundle);
        context.startActivity(intent);

    }

    //当原生页面返回flutter时
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 101:
                if (data != null) {
                    // NativePageActivity返回的数据
                    String message = data.getStringExtra("message");
                    Map<String, Object> result = new HashMap<>();
                    result.put("message", message);
                    // 调用Flutter端定义的方法
                    methodChannel.invokeMethod("onActivityResult", result);
                }
                break;
            default:
                break;
        }
    }
}
