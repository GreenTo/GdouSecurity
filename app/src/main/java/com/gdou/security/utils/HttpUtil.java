package com.gdou.security.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class HttpUtil {

    public static OkHttpClient client;

    public static void sendLocationRequest(String address,long id,String latitude,String longitude,
                                         okhttp3.Callback callback){

        MultipartBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("id", String.valueOf(id))
                .addFormDataPart("x", latitude)
                .addFormDataPart("y", longitude)
                .build();

        Request request = new Request.Builder()
                .url(address)
                .post(body)
                .build();

        client.newCall(request).enqueue(callback);
        
    }

    public static void loginRequest(String address,String account,String psw,
                                    okhttp3.Callback callback){

        MultipartBody body = new MultipartBody.Builder()
                .addFormDataPart("account", account)
                .addFormDataPart("psw", psw)
                .build();

        //OkHttpClient client = new OkHttpClient.Builder()
        //        //.cookieJar(new CookieJarImpl(new MemoryCookieStore()))
        //        .build();


        client = new OkHttpClient.Builder()
                .cookieJar(new CookieJar() {

                    private final HashMap<String, List<Cookie>> cookieStore = new HashMap<>();

                    @Override
                    public void saveFromResponse(HttpUrl httpUrl, List<Cookie> list) {
                        cookieStore.put(httpUrl.host(), list);
                    }

                    @Override
                    public List<Cookie> loadForRequest(HttpUrl httpUrl) {
                        List<Cookie> cookies = cookieStore.get(httpUrl.host());
                        return cookies != null ? cookies : new ArrayList<Cookie>();
                    }
                })
                .build();

        Request request = new Request.Builder()
                .url(address)
                .post(body)
                .build();

        client.newCall(request).enqueue(callback);
    }

    public static void getInformation(String address,String account,
                                      okhttp3.Callback callback){

        //String url = address + "?" + "account=" + account;

        String url = address;

        //OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(callback);
    }

}
