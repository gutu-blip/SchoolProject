package com.example.schoolproject.Mpesa.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.example.schoolproject.Mpesa.Mode;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.schedulers.Schedulers;

/**
 * @author Fredrick Ochieng on 02/02/2018.
 */

public class RetroClient {

    private static final String PRODUCTION_URL = "https://api.safaricom.co.ke/";
    private static final String SANDBOX_URL = "https://sandbox.safaricom.co.ke/";

    // https://sandbox.safaricom.co.ke/oauth/v1/generate?grant_type=client_credentials
//    https://sandbox.safaricom.co.ke/mpesa/c2b/v1/simulate
    /**
     * Get Retrofit Instance
     */
    private static Retrofit getRetrofitInstance(String url) {

        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                .create();

        RxJavaCallAdapterFactory rxAdapter = RxJavaCallAdapterFactory.createWithScheduler(Schedulers.io());

        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        clientBuilder.addInterceptor(loggingInterceptor);

        return new Retrofit.Builder()
                .baseUrl(url)
                .addCallAdapterFactory(rxAdapter)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(clientBuilder.build())
                .build();
    }

    /**
     * Get API Service
     *
     * @return MpesaApi Service
     */
    public static MpesaApi getApiService(Mode mode) {
        String url = SANDBOX_URL;

        if (mode == Mode.PRODUCTION)
            url = PRODUCTION_URL;

        return getRetrofitInstance(url).create(MpesaApi.class);
    }

}
