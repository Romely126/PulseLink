package com.example.phone; // 모든 관련 클래스가 이 패키지에 있다고 가정

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager; // 키보드 숨기기용 import
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONObject; // JSON 데이터 생성용

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private EditText editTextUserId;
    private Button buttonLogin;
    private ProgressBar progressBarLogin;

    private ApiService apiService;
    private PhoneDataLayerSender phoneDataLayerSender; // PhoneDataLayerSender 클래스가 com.example.phone 패키지에 있다고 가정

    // SharedPreferences 이름 및 키 정의
    public static final String SHARED_PREFS_NAME = "LoginPrefs_MyApp";
    public static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    public static final String KEY_USER_ID = "loggedInUserId";
    public static final String KEY_USER_NAME = "loggedInUserName";

    // 워치로 보낼 메시지 경로 상수
    public static final String LOGIN_STATUS_PATH = "/auth/login_status";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 자동 로그인 체크
        SharedPreferences prefs = getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        if (prefs.getBoolean(KEY_IS_LOGGED_IN, false)) {
            Log.i(TAG, "User already logged in (User ID: " + prefs.getString(KEY_USER_ID, "N/A") + "). Redirecting to main activity.");
            startActivity(new Intent(this, ReceivedMessageActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        editTextUserId = findViewById(R.id.editTextUserId);
        buttonLogin = findViewById(R.id.buttonLogin);
        progressBarLogin = findViewById(R.id.progressBarLogin);

        // Retrofit 서비스 초기화
        try {
            // RetrofitClient 클래스가 com.example.phone 패키지에 있다고 가정
            apiService = RetrofitClient.getClient().create(ApiService.class);
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Retrofit ApiService", e);
            Toast.makeText(this, "네트워크 서비스 초기화 오류", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // PhoneDataLayerSender 초기화 (com.example.phone 패키지에 있다고 가정)
        try {
            phoneDataLayerSender = new PhoneDataLayerSender(this);
            Log.d(TAG, "PhoneDataLayerSender initialized.");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing PhoneDataLayerSender", e);
            Toast.makeText(this, "워치 통신 서비스 초기화 오류", Toast.LENGTH_LONG).show();
            // 로그인 기능은 계속 사용할 수 있게 할지 결정 필요
        }

        Log.d(TAG, "LoginActivity initialized.");

        buttonLogin.setOnClickListener(v -> {
            String userId = editTextUserId.getText().toString().trim();
            if (userId.isEmpty()) {
                Toast.makeText(LoginActivity.this, "사용자 ID를 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }
            hideKeyboard();
            performLogin(userId);
        });
    }

    /**
     * 백엔드 API를 호출하여 로그인을 시도하는 메소드.
     * @param userIdInputValue 사용자가 입력한 ID
     */
    private void performLogin(String userIdInputValue) {
        if (apiService == null) {
            Log.e(TAG, "ApiService is null. Cannot perform login.");
            Toast.makeText(this, "로그인 서비스 오류", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Attempting login for user: " + userIdInputValue);
        progressBarLogin.setVisibility(View.VISIBLE);
        buttonLogin.setEnabled(false);

        // LoginRequest 클래스가 com.example.phone 패키지에 있다고 가정
        LoginRequest loginRequest = new LoginRequest(userIdInputValue);
        Call<LoginResponse> call = apiService.loginUser(loginRequest);

        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                progressBarLogin.setVisibility(View.GONE);
                buttonLogin.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    // LoginResponse 클래스가 com.example.phone 패키지에 있다고 가정
                    LoginResponse loginResponse = response.body();
                    if (loginResponse.isSuccess()) {
                        Log.i(TAG, "Login successful via API for user: " + loginResponse.getUserId() + ", Name: " + loginResponse.getUserName());
                        handleLoginSuccess(loginResponse.getUserId(), loginResponse.getUserName());
                    } else {
                        String errorMessage = loginResponse.getMessage() != null ? loginResponse.getMessage() : "아이디 또는 비밀번호를 확인해주세요.";
                        Log.w(TAG, "Login failed via API: " + errorMessage);
                        Toast.makeText(LoginActivity.this, "로그인 실패: " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                } else {
                    String errorMsg = "로그인 요청 실패 (서버 응답 코드: " + response.code() + ")";
                    if (response.errorBody() != null) {
                        try {
                            String errorBodyStr = response.errorBody().string();
                            Log.e(TAG, "Error body: " + errorBodyStr);
                            errorMsg += " - 서버 메시지 확인 필요";
                        } catch (Exception e) { Log.e(TAG, "Error reading error body", e); }
                    }
                    Log.e(TAG, errorMsg);
                    Toast.makeText(LoginActivity.this, "로그인 요청에 실패했습니다. 서버 상태를 확인해주세요.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                progressBarLogin.setVisibility(View.GONE);
                buttonLogin.setEnabled(true);
                Log.e(TAG, "Login API call failed due to network or other error: " + t.getMessage(), t);
                Toast.makeText(LoginActivity.this, "로그인 요청 중 오류가 발생했습니다. 네트워크 연결을 확인해주세요.", Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * 로그인 성공 시 후속 처리를 담당하는 메소드.
     * @param userId 로그인된 사용자의 ID
     * @param userName 로그인된 사용자의 이름
     */
    private void handleLoginSuccess(String userId, String userName) {
        Toast.makeText(LoginActivity.this, userName + "님, 환영합니다!", Toast.LENGTH_SHORT).show();
        saveLoginInfo(userId, userName);
        sendLoginStatusToWatch(true, userId, userName);

        Intent intent = new Intent(LoginActivity.this, ReceivedMessageActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * SharedPreferences에 로그인 상태 및 사용자 정보를 저장하는 메소드.
     */
    private void saveLoginInfo(String userId, String userName) {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_USER_NAME, userName);
        editor.apply();
        Log.i(TAG, "Logged in user info saved to SharedPreferences.");
    }

    /**
     * 워치로 로그인/로그아웃 상태 메시지를 전송하는 메소드.
     */
    private void sendLoginStatusToWatch(boolean isLoggedIn, String userId, String userName) {
        if (phoneDataLayerSender == null) {
            Log.e(TAG, "PhoneDataLayerSender is not initialized. Cannot send status to watch.");
            // 여기서 Toast를 보여주거나 사용자에게 알릴 수 있습니다.
            // Toast.makeText(this, "워치 통신 서비스가 준비되지 않았습니다.", Toast.LENGTH_SHORT).show();
            return; // 전송 불가
        }

        try {
            JSONObject jsonPayload = new JSONObject();
            jsonPayload.put("isLoggedIn", isLoggedIn);
            if (isLoggedIn) {
                if (userId == null || userName == null) {
                    Log.e(TAG, "Cannot send logged in status without valid userId and userName.");
                    return;
                }
                jsonPayload.put("userId", userId);
                jsonPayload.put("userName", userName);
            }
            jsonPayload.put("timestamp", System.currentTimeMillis());
            String message = jsonPayload.toString();

            Log.i(TAG, "Sending login status to watch via path " + LOGIN_STATUS_PATH + ": " + message);
            phoneDataLayerSender.sendMessageAsync(LOGIN_STATUS_PATH, message);

        } catch (org.json.JSONException e) {
            Log.e(TAG, "Error creating JSON payload for watch login status", e);
        } catch (Exception e) {
            Log.e(TAG, "Error sending login status message to watch", e);
            Toast.makeText(this, "워치로 상태 전송 중 오류 발생", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 로그아웃 처리를 수행하는 정적 메소드.
     */
    public static void performLogout(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, false);
        editor.remove(KEY_USER_ID);
        editor.remove(KEY_USER_NAME);
        editor.apply();
        Log.i(TAG, "User logged out. Cleared SharedPreferences.");

        // 워치로 로그아웃 상태 전송
        try {
            JSONObject jsonPayload = new JSONObject();
            jsonPayload.put("isLoggedIn", false);
            jsonPayload.put("timestamp", System.currentTimeMillis());
            String message = jsonPayload.toString();

            // PhoneDataLayerSender 인스턴스를 얻는 방법 필요 (여기서는 임시로 새로 생성)
            // 싱글톤 또는 다른 관리 방식을 사용하는 것이 더 효율적일 수 있습니다.
            new PhoneDataLayerSender(context.getApplicationContext()).sendMessageAsync(LOGIN_STATUS_PATH, message);
            Log.i(TAG, "Sent logged out status to watch: " + message);
        } catch (org.json.JSONException e) {
            Log.e(TAG, "Error creating JSON for watch logout status", e);
        } catch (Exception e) {
            Log.e(TAG, "Error sending logout status to watch", e);
        }

        // 로그인 화면으로 이동하고 모든 상위 액티비티 종료
        Intent intent = new Intent(context, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);

        if (context instanceof AppCompatActivity) {
            Toast.makeText(context, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 키보드를 숨기는 유틸리티 메소드.
     */
    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // PhoneDataLayerSender 리소스 해제
        if (phoneDataLayerSender != null) {
            phoneDataLayerSender.cleanup();
            Log.d(TAG, "PhoneDataLayerSender cleaned up in onDestroy.");
        }
        Log.d(TAG, "LoginActivity destroyed.");
    }
}