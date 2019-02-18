package com.gdou.security.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class HttpUtil {

    public static OkHttpClient client;

    public static final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/*");

    //public static String IP = "http://120.77.149.103:1234";

    public static String IP = "http://192.168.2.125:1234";

    public static void sendLocationRequest(long id,String latitude,String longitude,
                                         okhttp3.Callback callback){

        String address = IP + "/api/sendStation";

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

    public static void loginRequest(String account,String psw,
                                    okhttp3.Callback callback){

        String address = IP + "/api/login";

        MultipartBody body = new MultipartBody.Builder()
                .addFormDataPart("account", account)
                .addFormDataPart("psw", psw)
                .build();

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

    public static void getInformation(String account,
                                      okhttp3.Callback callback){

        String address = IP + "/api/getGuardInfo" + "?account=" + account;

        Request request = new Request.Builder()
                .url(address)
                .get()
                .build();

        client.newCall(request).enqueue(callback);
    }

    public static void sendPic(File file, String account, Callback callback) {

        String address = IP + "/api/sendPic";

        MultipartBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(), RequestBody.create(MEDIA_TYPE_PNG, file))
                .addFormDataPart("name",account)
                .build();

        Request request = new Request.Builder()
                .url(address)
                .post(body)
                .build();

        client.newCall(request).enqueue(callback);
    }

    public static void getLocation(Callback callback) {

        String address = IP + "/api/getAllPosition";

        Request request = new Request.Builder()
                .url(address)
                .get()
                .build();

        client.newCall(request).enqueue(callback);
    }

    public static void logout(Callback callback) {

        String address = IP + "/logout";

        Request request = new Request.Builder()
                .url(address)
                .get()
                .build();

        client.newCall(request).enqueue(callback);
    }
}
