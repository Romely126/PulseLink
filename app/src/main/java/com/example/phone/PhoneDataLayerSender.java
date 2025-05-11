package com.example.phone; // 1단계에서 만든 패키지명으로 변경하세요

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull; // NonNull 추가

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeClient;
import com.google.android.gms.wearable.Wearable;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList; // ArrayList 추가
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 휴대폰 앱에서 Wearable Data Layer의 MessageClient를 사용하여
 * 연결된 워치로 데이터를 전송하는 헬퍼 클래스.
 */
public class PhoneDataLayerSender {

    private static final String TAG = "PhoneDataLayerSender"; // 로그 태그
    private final Context context;
    private final MessageClient messageClient;
    private final NodeClient nodeClient;
    private final ExecutorService executorService;

    public PhoneDataLayerSender(Context context) {
        this.context = context.getApplicationContext();
        this.messageClient = Wearable.getMessageClient(this.context);
        this.nodeClient = Wearable.getNodeClient(this.context);
        // 백그라운드 작업을 위한 단일 스레드 Executor 생성
        this.executorService = Executors.newSingleThreadExecutor();
        Log.i(TAG, "PhoneDataLayerSender initialized.");
    }

    /**
     * 연결된 워치 노드를 찾아 메시지를 비동기적으로 전송합니다.
     * 연결된 모든 워치 노드에 메시지를 보냅니다.
     * @param path 메시지 경로 (예: LoginActivity.LOGIN_STATUS_PATH)
     * @param message 전송할 문자열 메시지 (JSON 등)
     */
    public void sendMessageAsync(final String path, final String message) {
        if (message == null || message.isEmpty()) {
            Log.w(TAG, "Message is empty, skipping send for path: " + path);
            return;
        }
        Log.d(TAG, "sendMessageAsync called. Path: " + path + ", Message(start): " + message.substring(0, Math.min(message.length(), 50)) + "...");

        // 백그라운드 스레드에서 노드 검색 및 메시지 전송 실행
        executorService.submit(() -> {
            Log.d(TAG, "Background task started: Finding connected watch nodes and sending message...");
            List<String> targetNodeIds = findConnectedWatchNodeIds(); // 워치 노드 검색

            if (!targetNodeIds.isEmpty()) {
                byte[] data = message.getBytes(StandardCharsets.UTF_8);
                for (String nodeId : targetNodeIds) {
                    Log.i(TAG, "Attempting to send message to Watch Node ID: " + nodeId + " for path: " + path);
                    // 각 노드에 메시지 전송 시도
                    Task<Integer> sendTask = messageClient.sendMessage(nodeId, path, data);

                    // 성공 리스너 (로그 출력)
                    sendTask.addOnSuccessListener(new OnSuccessListener<Integer>() {
                        @Override
                        public void onSuccess(Integer integer) {
                            Log.i(TAG, "✅ Message sent successfully to Node ID: " + nodeId + " for path: " + path);
                        }
                    });

                    // 실패 리스너 (로그 출력)
                    sendTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "❌ Failed to send message to Node ID: " + nodeId + " for path: " + path, e);
                        }
                    });
                    // 주의: sendTask 자체는 비동기이지만, 루프는 다음 노드로 바로 넘어갑니다.
                    // Tasks.await() 등을 사용하여 동기적으로 결과를 기다릴 수도 있지만,
                    // UI 스레드를 차단하지 않기 위해 여기서는 비동기 콜백만 사용합니다.
                }
            } else {
                Log.w(TAG, "❌ No connected watch nodes found to send message for path: " + path);
                // TODO: 사용자에게 "워치가 연결되지 않았습니다" 와 같은 피드백을 줄 필요가 있을 수 있음
                //       (예: Toast 메시지 - 하지만 백그라운드 스레드이므로 Handler 사용 필요)
            }
            Log.d(TAG, "Background task finished for path: " + path);
        });
    }

    /**
     * 연결된 노드 중 워치(Wearable)에 해당하는 모든 노드의 ID 목록을 찾습니다. (동기 방식)
     * @return 찾은 워치 노드 ID 목록 (없으면 빈 리스트)
     */
    private List<String> findConnectedWatchNodeIds() {
        Log.d(TAG, "findConnectedWatchNodeIds: Searching for connected nodes...");
        List<String> watchNodeIds = new ArrayList<>();
        try {
            // getConnectedNodes() Task를 최대 5초간 동기적으로 기다림
            // 주의: 이 메소드는 백그라운드 스레드에서 호출되어야 합니다 (sendMessageAsync 내부).
            Task<List<Node>> connectedNodesTask = nodeClient.getConnectedNodes();
            List<Node> nodes = Tasks.await(connectedNodesTask, 5, TimeUnit.SECONDS); // 5초 타임아웃

            if (nodes == null || nodes.isEmpty()) {
                Log.w(TAG, "findConnectedWatchNodeIds: No connected nodes found.");
                return watchNodeIds; // 빈 리스트 반환
            }

            Log.i(TAG, "findConnectedWatchNodeIds: Found " + nodes.size() + " connected node(s). Checking for wearables...");
            for (Node node : nodes) {
                // isNearby() 는 물리적 근접성을 나타내며, 실제 연결과 항상 같지는 않을 수 있습니다.
                // getDisplayName() 으로 식별하거나, 더 확실하게 하려면 CapabilityClient 사용이 좋습니다.
                // 여기서는 일단 모든 연결된 노드를 워치 후보로 간주합니다 (일반적으로 폰에는 워치만 연결됨).
                Log.d(TAG, "  - Node Found: Name=" + node.getDisplayName() + ", ID=" + node.getId() + ", Nearby=" + node.isNearby());
                watchNodeIds.add(node.getId());
            }
            Log.i(TAG, "findConnectedWatchNodeIds: Returning " + watchNodeIds.size() + " potential watch node ID(s).");
            return watchNodeIds;

        } catch (ExecutionException e) {
            Log.e(TAG, "findConnectedWatchNodeIds: ExecutionException while getting nodes", e);
        } catch (InterruptedException e) {
            Log.e(TAG, "findConnectedWatchNodeIds: InterruptedException while getting nodes", e);
            Thread.currentThread().interrupt(); // 스레드 인터럽트 상태 복원
        } catch (TimeoutException e) {
            Log.e(TAG, "findConnectedWatchNodeIds: TimeoutException (5s) while getting nodes", e);
        } catch (Exception e) { // 기타 예외 처리
            Log.e(TAG, "findConnectedWatchNodeIds: Unexpected error while getting nodes", e);
        }
        return watchNodeIds; // 오류 발생 시 빈 리스트 반환
    }

    /**
     * 서비스 또는 액티비티 종료 시 스레드 풀을 정리합니다.
     */
    public void cleanup() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            Log.i(TAG, "PhoneDataLayerSender background executor shutdown.");
        }
    }
}