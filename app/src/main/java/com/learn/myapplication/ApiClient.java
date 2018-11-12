package com.learn.myapplication;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static Retrofit retrofit = null;

    public static Retrofit getClient(){

        if (retrofit==null) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(interceptor)
                    .connectTimeout(3, TimeUnit.MINUTES)
                    .readTimeout(2, TimeUnit.MINUTES)
                    .retryOnConnectionFailure(true)
                    .build();

            Gson gsonBuilder = new GsonBuilder()
                    .setLenient()
                    .create();

            retrofit = new Retrofit.Builder()
                    .client(client)
                    .baseUrl(ConstantValues.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gsonBuilder))
                    .build();
        }
        return retrofit;
    }

   /* public static Retrofit getClientWithProgress(){
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .addNetworkInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {

                        Response originalResponse = chain.proceed(chain.request());
                        return originalResponse.newBuilder()
                                .body(new ProgressResponseBody(originalResponse.body(), progressListener))
                                .build();

                        return null;
                    }
                })
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();

        Gson gsonBuilder = new GsonBuilder()
                .setLenient()
                .create();

        return new Retrofit.Builder()
                .client(client)
                .baseUrl(ConstantValues.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gsonBuilder))
                .build();
    }*/

    public static Retrofit getRetrofitClient(){
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();

        Gson gsonBuilder = new GsonBuilder()
                .setLenient()
                .create();

        return new Retrofit.Builder()
                .client(client)
                .baseUrl(ConstantValues.BASE_URL)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gsonBuilder))
                .build();

    }

}