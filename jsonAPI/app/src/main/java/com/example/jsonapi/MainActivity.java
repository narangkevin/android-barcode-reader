package com.example.jsonapi;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        OkHttpClient client = new OkHttpClient().newBuilder().build();
        Request request = new Request.Builder()
                .url("https://cms-fn-dmpapi.trueid.net/cms-fnshelf/v1/vdd78mEQYEv?fields=channel_code," +
                        "thumb,channel_info,subscription_package,subscription_tiers,subscriptionoff_requirelogin," +
                        "drm,is_premium,true_vision,ads_webapp,lang_dual,subtitle,catch_up," +
                        "allow_catchup,time_shift,allow_timeshift,epg_flag")
                .method("GET", null)
                .addHeader("authorization", "Bearer 5aaf9ade15afe0324400bacc26115aba3ac9493faf4f27ff957620c2")
                .addHeader("content-type", "application/json")
                .addHeader("Cookie", "42a3baeb7914c9c6248a28cbfd399fb3=12cb01084916017e201e9cccb170b93e")
                .build();

        try {
            Response response = client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}