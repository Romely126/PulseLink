package com.example.phone; // 실제 패키지 이름으로 변경하세요

import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.WearableListenerService;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

/**
 * 휴대폰 측 Wearable Data Layer 메시지 및 데이터 변경 수신 서비스 (진입 로그 추가 버전)
 */
public class DataLayerListenerService extends WearableListenerService {

    private static final String TAG = "DataLayerListener";

    // 브로드캐스트 액션 정의
    public static final String ACTION_MESSAGE_RECEIVED = "com.example.phone.SENSOR_DATA_RECEIVED";
    public static final String ACTION_CONNECTION_STATUS_CHANGED = "com.example.phone.CONNECTION_STATUS_CHANGED";

    // 워치에서 정의한 메시지 경로 상수들 (public)
    public static final String HEART_RATE_PATH = "/heartrate";
    public static final String SPO2_PATH = "/spo2";
    public static final String SKIN_TEMP_PATH = "/skintemp";
    public static final String LOCATION_PATH = "/location";
    public static final String FALL_DETECT_PATH = "/fall_detect";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "✅ Service onCreate - 서비스 생성됨.");
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "⚠️ Service onDestroy - 서비스 종료됨.");
        super.onDestroy();
    }

    // 연결된 노드 상태 변경 시 호출
    @Override
    public void onPeerConnected(Node peer) {
        // --- ⬇️ 메소드 진입 로그 추가 ---
        Log.i(TAG, ">>> onPeerConnected CALLED! Node ID=" + peer.getId());
        // --- ⬆️ 메소드 진입 로그 추가 ---
        super.onPeerConnected(peer);
        Log.i(TAG, "  Peer Connected: " + peer.getDisplayName() + " (" + peer.getId() + "), Nearby=" + peer.isNearby());
        sendConnectionStatusBroadcast(true, peer.getDisplayName());
    }

    @Override
    public void onPeerDisconnected(Node peer) {
        // --- ⬇️ 메소드 진입 로그 추가 ---
        Log.w(TAG, ">>> onPeerDisconnected CALLED! Node ID=" + peer.getId());
        // --- ⬆️ 메소드 진입 로그 추가 ---
        super.onPeerDisconnected(peer);
        Log.w(TAG, "  Peer Disconnected: " + peer.getDisplayName() + " (" + peer.getId() + ")");
        sendConnectionStatusBroadcast(false, peer.getDisplayName());
    }

    // 워치에서 MessageClient.sendMessage()로 보낸 메시지 수신
    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        // --- ⬇️ 메소드 진입 로그 추가 ---
        Log.i(TAG, ">>> onMessageReceived CALLED! Path=" + messageEvent.getPath() + ", SourceNodeId=" + messageEvent.getSourceNodeId());
        // --- ⬆️ 메소드 진입 로그 추가 ---
        super.onMessageReceived(messageEvent);

        String path = messageEvent.getPath();
        String payload = new String(messageEvent.getData(), StandardCharsets.UTF_8);
        String sourceNodeId = messageEvent.getSourceNodeId();

        Log.i(TAG, "⬇️ Message Received from Node: " + sourceNodeId);
        Log.i(TAG, "  Path: " + path);
        Log.i(TAG, "  Payload: " + payload.substring(0, Math.min(payload.length(), 150)));

        try {
            JSONObject json = new JSONObject(payload);
            long timestamp = json.optLong("timestamp", System.currentTimeMillis());

            Intent intent = new Intent(ACTION_MESSAGE_RECEIVED);
            intent.putExtra("path", path);
            intent.putExtra("timestamp", timestamp);
            intent.putExtra("payload", payload);
            intent.putExtra("sourceNodeId", sourceNodeId);

            switch (path) {
                case HEART_RATE_PATH:
                case SPO2_PATH:
                case SKIN_TEMP_PATH:
                case LOCATION_PATH:
                case FALL_DETECT_PATH:
                    Log.d(TAG, "   -> Known sensor data received for path: " + path);
                    LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                    Log.d(TAG, "   -> Broadcast sent for path: " + path);
                    break;
                default:
                    Log.w(TAG, "   -> Unknown message path received: " + path + ". Ignoring.");
                    break;
            }

        } catch (JSONException e) {
            Log.e(TAG, "❌ Failed to parse JSON payload for path: " + path, e);
        } catch (Exception e) {
            Log.e(TAG, "❌ Error handling message for path: " + path, e);
        }
    }

    // (참고) DataClient 사용 시 데이터 변경 이벤트 처리
    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        // --- ⬇️ 메소드 진입 로그 추가 ---
        Log.d(TAG, ">>> onDataChanged CALLED! Event count: " + dataEvents.getCount());
        // --- ⬆️ 메소드 진입 로그 추가 ---
        super.onDataChanged(dataEvents);
        dataEvents.release();
    }

    // 연결 상태 브로드캐스트
    private void sendConnectionStatusBroadcast(boolean isConnected, String nodeName) {
        Log.d(TAG, "sendConnectionStatusBroadcast 호출: isConnected=" + isConnected + ", nodeName=" + nodeName);
        Intent intent = new Intent(ACTION_CONNECTION_STATUS_CHANGED);
        intent.putExtra("connected", isConnected);
        intent.putExtra("nodeName", nodeName);
        boolean sent = LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        Log.d(TAG, "Connection status broadcast " + (sent ? "sent successfully." : "failed to send (no active receivers?)."));
    }
}