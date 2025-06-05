package com.example.frontend2.api;

import okhttp3.OkHttpClient; // OkHttpClient 사용 권장 (선택 사항)
import okhttp3.logging.HttpLoggingInterceptor; // 로깅 인터셉터 사용 권장 (선택 사항)
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit; // 타임아웃 사용 시

public class ApiClient {
    private static final String BASE_URL = "http://10.0.2.2:3002/"; // 포트 번호 확인!
    private static Retrofit retrofitInstance = null; // 변수명 변경 (선택 사항)
    private static OkHttpClient okHttpClientInstance = null; // OkHttpClient 인스턴스 추가

    // OkHttpClient 인스턴스를 생성하거나 기존 인스턴스를 반환 (로깅 및 타임아웃 설정용)
    private static OkHttpClient getOkHttpClient() {
        if (okHttpClientInstance == null) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            // 개발 중에는 BODY 레벨로 설정하여 모든 요청/응답 내용을 Logcat에서 확인
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            okHttpClientInstance = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .addInterceptor(loggingInterceptor) // 로깅 인터셉터 추가
                    .build();
        }
        return okHttpClientInstance;
    }

    /**
     * Retrofit 인스턴스를 반환합니다. (싱글톤)
     * @return Retrofit 인스턴스
     */
    public static Retrofit getClient() { // 또는 getRetrofitInstance()
        if (retrofitInstance == null) {
            retrofitInstance = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(getOkHttpClient()) // 👈 커스텀 OkHttpClient 사용
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofitInstance;
    }

    // 🔽🔽🔽 이 메소드를 추가합니다! 🔽🔽🔽
    /**
     * SpaceApi 인터페이스의 구현체를 반환합니다.
     * @return SpaceApi 구현체
     */
    public static SpaceApi getSpaceApi() {
        return getClient().create(SpaceApi.class);
    }

    // 🔽🔽🔽 다른 API 인터페이스가 있다면 유사하게 추가할 수 있습니다. 🔽🔽🔽
    /**
     * AiRoutineApi 인터페이스의 구현체를 반환합니다.
     * @return AiRoutineApi 구현체
     */
    public static AiRoutineApi getAiRoutineApi() {
        return getClient().create(AiRoutineApi.class);
    }

    /**
     * RoutineApi (또는 CleaningRoutineApi) 인터페이스의 구현체를 반환합니다.
     * @return RoutineApi 구현체
     */
    public static RoutineApi getRoutineApi() { // 인터페이스 이름이 RoutineApi라고 가정
        return getClient().create(RoutineApi.class);
    }
}