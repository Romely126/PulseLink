package com.example.phone; // 실제 패키지 이름으로 변경하세요

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;

/**
 * 앱의 시작점. ReceivedMessageActivity를 실행합니다.
 * (BLE 관련 코드는 제거되었습니다.)
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.activity_main); // activity_main.xml이 필요 없다면 이 줄도 제거

        Log.d(TAG, "MainActivity onCreate - Launching ReceivedMessageActivity");

        // 바로 ReceivedMessageActivity 실행
        Intent intent = new Intent(this, ReceivedMessageActivity.class);
        startActivity(intent);

        // MainActivity는 바로 종료
        finish();
    }
}