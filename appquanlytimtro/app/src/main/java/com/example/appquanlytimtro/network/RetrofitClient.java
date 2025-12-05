//class: quản lý kết nối Retrofit
// Mục đích file: File này dùng để quản lý kết nối Retrofit và cấu hình API client
// function: 
// - getInstance(): Lấy instance duy nhất của RetrofitClient
// - getApiService(): Lấy ApiService interface
// - getToken(): Lấy token từ SharedPreferences
// - setToken(): Lưu token vào SharedPreferences
// - getUserData(): Lấy dữ liệu user từ SharedPreferences
// - setUserData(): Lưu dữ liệu user vào SharedPreferences
// - clearUserData(): Xóa dữ liệu user khỏi SharedPreferences
// - isLoggedIn(): Kiểm tra trạng thái đăng nhập
// - logout(): Đăng xuất và xóa dữ liệu
package com.example.appquanlytimtro.network;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.appquanlytimtro.BuildConfig;
import com.example.appquanlytimtro.utils.Constants;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class RetrofitClient {
    private static final String BASE_URL = BuildConfig.BASE_URL;
    
    private static RetrofitClient instance;
    private ApiService apiService;
    private Context context;
    
    private RetrofitClient(Context context) {
        this.context = context.getApplicationContext();
        createRetrofitInstance();
    }
    
    public static synchronized RetrofitClient getInstance(Context context) {
        if (instance == null) {
            instance = new RetrofitClient(context);
        }
        return instance;
    }
    
    private void createRetrofitInstance() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        
        Interceptor authInterceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request originalRequest = chain.request();
                String token = getToken();
                
                if (token != null && !token.isEmpty()) {
                    Request newRequest = originalRequest.newBuilder()
                            .header("Authorization", "Bearer " + token)
                            .build();
                    return chain.proceed(newRequest);
                }
                
                return chain.proceed(originalRequest);
            }
        };
        
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .addInterceptor(loggingInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        
        apiService = retrofit.create(ApiService.class);
    }
    
    public ApiService getApiService() {
        return apiService;
    }
    
    public void saveToken(String token) {
        SharedPreferences prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(Constants.TOKEN_KEY, token).apply();
    }
    
    public String getToken() {
        SharedPreferences prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(Constants.TOKEN_KEY, null);
    }
    
    public void clearToken() {
        SharedPreferences prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove(Constants.TOKEN_KEY).apply();
    }
    
    public boolean isLoggedIn() {
        return getToken() != null && !getToken().isEmpty();
    }
    
    public void saveUserData(String userJson) {
        SharedPreferences prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(Constants.USER_DATA_KEY, userJson).apply();
    }
    
    public String getUserData() {
        SharedPreferences prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(Constants.USER_DATA_KEY, null);
    }
    
    public void clearUserData() {
        SharedPreferences prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove(Constants.USER_DATA_KEY).apply();
    }
    
    public void logout() {
        clearToken();
        clearUserData();
    }
}
