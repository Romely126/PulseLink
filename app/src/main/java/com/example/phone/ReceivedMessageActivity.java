package com.example.phone; // 실제 패키지 이름으로 변경하세요

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Displays data received from the watch via Wearable Data Layer. (Entry Logging Added)
 */
public class ReceivedMessageActivity extends AppCompatActivity {

    private static final String TAG = "ReceivedMessageActivity";

    // UI Elements
    private TextView tvHeartRate;
    private TextView tvSpo2;
    private TextView tvSkinTemp;
    private TextView tvLocation;
    private TextView tvFallDetect;
    private TextView tvLastUpdate;
    private TextView tvConnectionStatus;

    // Data Layer 데이터 수신 브로드캐스트 리시버
    private final BroadcastReceiver dataLayerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // --- ⬇️ 리시버 호출 확인 로그 ---
            Log.i(TAG, ">>> dataLayerReceiver onReceive CALLED!");
            // --- ⬆️ 리시버 호출 확인 로그 ---
            String action = intent.getAction();
            Log.d(TAG, "  Received local broadcast: " + action);

            if (DataLayerListenerService.ACTION_MESSAGE_RECEIVED.equals(action)) {
                String path = intent.getStringExtra("path");
                String payload = intent.getStringExtra("payload");
                long timestamp = intent.getLongExtra("timestamp", System.currentTimeMillis());
                String sourceNodeId = intent.getStringExtra("sourceNodeId");
                Log.i(TAG, "  ➡️ Data Received: Path=" + path + ", From Node=" + sourceNodeId);

                updateLastUpdateTime(timestamp);

                if (payload != null) {
                    try {
                        JSONObject json = new JSONObject(payload);
                        handleSensorData(path, json);
                    } catch (JSONException e) {
                        Log.e(TAG, "❌ Failed to parse JSON from broadcast: " + payload, e);
                    }
                } else {
                    Log.w(TAG,"  Received broadcast with null payload for path: " + path);
                }

            } else if (DataLayerListenerService.ACTION_CONNECTION_STATUS_CHANGED.equals(action)) {
                boolean isConnected = intent.getBooleanExtra("connected", false);
                String nodeName = intent.getStringExtra("nodeName");
                Log.i(TAG, "  🔄 Connection Status Changed: Connected=" + isConnected + ", Node=" + nodeName);
                updateConnectionStatusUI(isConnected, nodeName);
            } else {
                Log.w(TAG, "  Received unknown broadcast action: " + action);
            }
        }
    };

    /**
     * 수신된 센서 데이터를 경로에 따라 처리하고 UI를 업데이트합니다.
     */
    private void handleSensorData(String path, JSONObject json) {
        if (path == null || json == null) return;
        Log.d(TAG, "  handleSensorData for path: " + path);

        runOnUiThread(() -> {
            try {
                switch (path) {
                    case DataLayerListenerService.HEART_RATE_PATH:
                        double hrValue = json.optDouble("value", -1.0);
                        int hrStatus = json.optInt("status", -1);
                        if (tvHeartRate != null) tvHeartRate.setText(String.format(Locale.getDefault(), "심박수: %.0f (상태: %d)", hrValue, hrStatus));
                        break;
                    case DataLayerListenerService.SPO2_PATH:
                        double spo2Value = json.optDouble("value", -1.0);
                        if (tvSpo2 != null) tvSpo2.setText(String.format(Locale.getDefault(), "SpO2: %.0f %%", spo2Value));
                        break;
                    case DataLayerListenerService.SKIN_TEMP_PATH:
                        double skinValue = json.optDouble("skinValue", -999.0);
                        double ambientValue = json.optDouble("ambientValue", -999.0);
                        int tempStatus = json.optInt("status", -1);
                        if (tvSkinTemp != null) tvSkinTemp.setText(String.format(Locale.getDefault(), "피부: %.1f°C / 주변: %.1f°C (상태: %d)", skinValue, ambientValue, tempStatus));
                        break;
                    case DataLayerListenerService.LOCATION_PATH:
                        double latitude = json.optDouble("latitude", 0.0);
                        double longitude = json.optDouble("longitude", 0.0);
                        double altitude = json.optDouble("altitude", 0.0);
                        double speed = json.optDouble("speed", 0.0);
                        if (tvLocation != null) tvLocation.setText(String.format(Locale.getDefault(), "위치: %.4f, %.4f\n고도: %.1f m, 속도: %.1f km/h", latitude, longitude, altitude, speed));
                        break;
                    case DataLayerListenerService.FALL_DETECT_PATH:
                        String fallMessage = json.optString("message", "알 수 없는 낙상 이벤트");
                        if (tvFallDetect != null) {
                            tvFallDetect.setText("낙상 감지: " + fallMessage);
                            tvFallDetect.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
                        }
                        break;
                    default:
                        break;
                }
            } catch (Exception e) { Log.e(TAG, "❌ Error updating UI for path: " + path, e); }
        });
    }

    /**
     * 마지막 업데이트 시간 UI를 갱신합니다.
     */
    private void updateLastUpdateTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String timeStr = sdf.format(new Date(timestamp));
        if (tvLastUpdate != null) {
            runOnUiThread(() -> tvLastUpdate.setText("마지막 업데이트: " + timeStr));
        }
    }

    /**
     * Data Layer 연결 상태 UI를 업데이트합니다.
     */
    private void updateConnectionStatusUI(boolean isConnected, String nodeName) {
        runOnUiThread(() -> {
            if (tvConnectionStatus != null) {
                final String statusText;
                final int colorRes;
                if (isConnected) {
                    statusText = "워치 상태: 연결됨" + (nodeName != null ? " ("+nodeName+")" : "");
                    colorRes = android.R.color.holo_green_dark;
                } else {
                    statusText = "워치 상태: 연결 끊김";
                    colorRes = android.R.color.holo_red_dark;
                }
                tvConnectionStatus.setText(statusText);
                tvConnectionStatus.setTextColor(ContextCompat.getColor(this, colorRes));
                Log.i(TAG, "  UI Updated: " + statusText);
            } else { Log.w(TAG, "tvConnectionStatus is null."); }
        });
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "Activity onCreate");
        setContentView(R.layout.activity_received_message);

        // UI 요소 초기화
        try {
            tvHeartRate = findViewById(R.id.tvHeartRate);
            tvSpo2 = findViewById(R.id.tvSpo2);
            tvSkinTemp = findViewById(R.id.tvSkinTemp);
            tvLocation = findViewById(R.id.tvLocation);
            tvFallDetect = findViewById(R.id.tvFallDetect);
            tvLastUpdate = findViewById(R.id.tvLastUpdate);
            tvConnectionStatus = findViewById(R.id.tvConnectionStatus);

            if (tvHeartRate == null || tvSpo2 == null || tvSkinTemp == null ||
                    tvLocation == null || tvFallDetect == null || tvLastUpdate == null ||
                    tvConnectionStatus == null) {
                Log.e(TAG, "One or more TextView IDs not found in activity_received_message.xml!");
                Toast.makeText(this, "UI 초기화 오류.", Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            Log.e(TAG, "Error during findViewById", e);
            Toast.makeText(this, "UI 요소 초기화 오류 발생", Toast.LENGTH_LONG).show();
        }

        // 초기 텍스트 설정
        if (tvHeartRate != null) tvHeartRate.setText("심박수: 대기 중...");
        if (tvSpo2 != null) tvSpo2.setText("SpO2: 대기 중...");
        if (tvSkinTemp != null) tvSkinTemp.setText("온도: 대기 중...");
        if (tvLocation != null) tvLocation.setText("위치: 대기 중...");
        if (tvFallDetect != null) tvFallDetect.setText("낙상 감지: 없음");
        if (tvLastUpdate != null) tvLastUpdate.setText("마지막 업데이트: -");
        updateConnectionStatusUI(false, null); // 초기 상태: 연결 끊김
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "Activity onResume - Registering Data Layer receiver");
        // 로컬 브로드캐스트 리시버 등록
        IntentFilter filter = new IntentFilter();
        filter.addAction(DataLayerListenerService.ACTION_MESSAGE_RECEIVED);
        filter.addAction(DataLayerListenerService.ACTION_CONNECTION_STATUS_CHANGED);
        LocalBroadcastManager.getInstance(this).registerReceiver(dataLayerReceiver, filter);
        Log.i(TAG,"LocalBroadcastReceiver registered.");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "Activity onPause - Unregistering Data Layer receiver");
        // 로컬 브로드캐스트 리시버 등록 해제
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(dataLayerReceiver);
            Log.i(TAG,"LocalBroadcastReceiver unregistered.");
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "Receiver not registered or already unregistered.", e);
        }
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "Activity onDestroy");
        super.onDestroy();
    }
}