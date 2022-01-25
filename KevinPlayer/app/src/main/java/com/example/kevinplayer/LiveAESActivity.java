package com.example.kevinplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.HttpDataSource;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LiveAESActivity extends AppCompatActivity {

    // UI Elements
    ListView listView;
    PlayerView playerView;
    TextView errorLog;
    ImageView thumbnail;

    // ExoPlayer
    SimpleExoPlayer simpleExoPlayer;

    // Declaration Global Elements
    String myResponse;
    ArrayList<HashMap<String,String>> arrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_a_e_s);

        // Initializing UI Elements
        arrayList = new ArrayList<>();
        listView = (ListView) findViewById(R.id.aes_listview);
        errorLog = (TextView) findViewById(R.id.aes_cdn_info);
        playerView = findViewById(R.id.aes_player_view);
        thumbnail = (ImageView) findViewById(R.id.thumb);

        // Call OkHTTPClient
        OkHttpClient client = new OkHttpClient();

        String url = "https://dmpapi2.trueid-preprod.net/cms-fnshelf/v1/vdd78mEQYEv?fields=channel_code%2Cthumb%2Cthumb_list%2Cchannel_info%2Csubscription_package%2Csubscription_tiers%2Csubscriptionoff_requirelogin%2Cdrm%2Cslug%2Cschedule_code%2Callow_chrome_cast%2Cmix_no%2Cgeo_block%2Ccatch_up%2Ccontent_type%2Cmovie_type%2Ccount_views%2Csetting%2Ccount_likes%2Cis_premium%2Cads%2Cremove_ads%2Ctrue_vision%2Cepg_flag%2Clang_dual%2Csubtitle%2Ccatch_up%2Callow_catchup%2Ctime_shift%2Callow_timeshift%2Cembed%2Cchannel_name%2Ctvod_flag%2Cep_items%2Callow_app%2Ccontent_rights%2Cdigital_no&lang=th&limit=200";
        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("Authorization", "5b18e526f0463656f7c4329f395402da04944983a2396966beab29c3")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                errorLog.setText("Unable to retrieve channel list");
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if(response.isSuccessful()){
                    myResponse = response.body().string();
                    LiveAESActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject reader = new JSONObject(myResponse);
                                JSONArray shelfItem = reader.getJSONObject("data").getJSONArray("shelf_items");

                                System.out.println("Live Stream shelf_items  --> : " + shelfItem);
                                for(int i = 0;i<shelfItem.length();i++)
                                {
                                    JSONObject itemChannel = shelfItem.getJSONObject(i);
                                    String id = itemChannel.getString("id");
                                    String title = itemChannel.getString("title");
                                    String thumb = itemChannel.getString("thumb");

                                    HashMap<String,String> data = new HashMap<>();

                                    data.put("id",id);
                                    data.put("title",title);
                                    data.put("thumb",thumb);

                                    arrayList.add(data);
                                    System.out.print("Kevin Array List --> " + arrayList);

                                    final ListAdapter adapter = new SimpleAdapter(LiveAESActivity.this,arrayList
                                            ,R.layout.listview_layout
                                            ,new String[]{"id","title","thumb"},new int[]{R.id.channelid,R.id.channeltitle,R.id.thumb});

                                    // OnItemCLick
                                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                        @Override
                                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                            HashMap<String, String> item = (HashMap<String, String>) adapterView.getItemAtPosition(i);
                                            getStreamURL(item.get("id").toString());
                                        }
                                    });

                                    listView.setAdapter(adapter);
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
                else {
                    errorLog.setText("Unable to retrieve channels");
                }
            }
        });
    }

    // Request Stream URL / CDN Url
    public void getStreamURL(String accessId) {
        OkHttpClient client = new OkHttpClient();

        System.out.print("Access ID ----> " + accessId);
        String url = "https://cms-streamer-dmpapi.trueid.net/pk-streamer/v2/streamer?access=nonlogin&appid=trueid&device_id=FE2B0454-2EC4-4264-B92C-9C5DD2740E34&ep_items=no&fields=channel_code%2Cthumb%2Cchannel_info%2Csubscription_package%2Csubscription_tiers%2Csubscriptionoff_requirelogin%2Cdrm%2Cslug%2Cschedule_code%2Callow_chrome_cast%2Cmix_no%2Cgeo_block%2Ccatch_up%2Ccontent_type%2Cmovie_type%2Ccount_views%2Cblack_out%2Cblackout_start_date%2Cblackout_end_date%2Cblackout_message%2Cremove_ads%2Cads%2Csetting%2Ctrue_vision%2Cads_webapp%2Callow_catchup&id="+accessId+"&os=ios&streamlvl=auto&type=live&uid=&visitor=mobile";

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("authorization", "Bearer 5aaf9ade15afe0324400bacc77392774a9a049908de8128dbc961654")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                errorLog.setText("Unable to play. No Access to Stream URL, possibly due to Network Error)");
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String[] url = {""};
                final String[] license = {""};

                // Release SimpleExoplayer;
                if (simpleExoPlayer != null) {
                    simpleExoPlayer.release();
                }

                if(response.isSuccessful()){
                    myResponse = response.body().string();
                    LiveAESActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            try {
                                JSONObject reader = new JSONObject(myResponse);
                                JSONObject dataItem = reader.getJSONObject("data");
                                JSONObject streamItem = dataItem.getJSONObject("stream");

                                System.out.print("Live stream_url --> " + streamItem.get("stream_url"));
                                System.out.println("Live stream_license --> " + streamItem.get("stream_license"));

                                // Changing CDN info state
//                                errorLog.setText(streamItem.get("stream_url").toString());

                                url[0] = streamItem.get("stream_url").toString();
                                license[0] = streamItem.get("stream_license").toString();

                                videoPlayer(url[0], license[0]);

                            } catch (JSONException e) {
                                e.printStackTrace();
                                errorLog.setText("Unable to play. Need to Rent/Buy package");
                            }
                        }
                    });
                } else {
                    errorLog.setText("Unable to play. No Access to Stream URL");
                }
            }
        });
    }

    // Setting up Video Player
    public void videoPlayer(String url, String licenseUrl){
        //Video URL
        Uri videoUri = Uri.parse(url);
        Uri licenseUri = Uri.parse(licenseUrl);

        MediaItem mediaItem = new MediaItem.Builder()
                .setUri(videoUri)
                .setDrmUuid(C.WIDEVINE_UUID)
                .setDrmLicenseUri(licenseUri)
                .setDrmMultiSession(true)
                .build();

        simpleExoPlayer = new SimpleExoPlayer.Builder(this).build();

        playerView.setPlayer(simpleExoPlayer);
        //Keep screen on
        playerView.setKeepScreenOn(true);

        simpleExoPlayer.setMediaItem(mediaItem);
        // Prepare the player.
        simpleExoPlayer.prepare();

//        // Set Volume of Device
//        simpleExoPlayer.setDeviceVolume(5);
//
//        // Set Volume using Float Value
//        simpleExoPlayer.setVolume(5);

        // Play video when ready
        simpleExoPlayer.setPlayWhenReady(true);

        simpleExoPlayer.addListener(new Player.EventListener() {
            public void onTimelineChanged(Timeline timeline, int reason) {

            }
            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                if (isPlaying) {
                    // Active Player
                    errorLog.setText("Active Player (No Error)");
                }
            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {
                if (error.type == ExoPlaybackException.TYPE_SOURCE) {
                    IOException cause = error.getSourceException();
                    if (cause instanceof HttpDataSource.HttpDataSourceException) {
                        // An HTTP error occurred.
                        if (error.getSourceException().toString().contains("404")){
                            errorLog.setText("404 ERROR, CDN Content not found");
                        } else if (error.getSourceException().toString().contains("401")){
                            errorLog.setText("401 ERROR, CDN Unauthorized");
                        } else if (error.getSourceException().toString().contains("400")) {
                            errorLog.setText("400 ERROR, CDN Bad Request");
                        } else {
                            errorLog.setText("UNDEFINED CDN ERROR --> " + error.getSourceException().toString());
                        }
                        HttpDataSource.HttpDataSourceException httpError = (HttpDataSource.HttpDataSourceException) cause;
                        // This is the request for which the error occurred.
                        DataSpec requestDataSpec = httpError.dataSpec;
                        // It's possible to find out more about the error both by casting and by
                        // querying the cause.
                        if (httpError instanceof HttpDataSource.InvalidResponseCodeException) {
                            // Cast to InvalidResponseCodeException and retrieve the response code,
                            // message and headers.
                        } else {
                            // Try calling httpError.getCause() to retrieve the underlying cause,
                            // although note that it may be null.
                            httpError.getCause();
                            errorLog.setText(httpError.getCause().toString());
                        }
                    }
                    // For Checking Error
                    if (error.getSourceException().toString().contains("HttpDataSource$InvalidResponseCodeException") && !error.getSourceException().toString().contains("DrmSession$DrmSessionException")){
                        if (error.getSourceException().toString().contains("404")){
                            errorLog.setText("404 ERROR, Content not found");
                        } else if (error.getSourceException().toString().contains("401")){
                            errorLog.setText("401 ERROR, CDN Unauthorized");
                        } else if (error.getSourceException().toString().contains("400")) {
                            errorLog.setText("400 ERROR, CDN Bad Request");
                        }  else if (error.getSourceException().toString().contains("403")){
                            errorLog.setText("403 ERROR, CDN Forbidden, server is refusing action");
                        } else if (error.getSourceException().toString().contains("402")){
                            errorLog.setText("402 ERROR, CDN Payment Required");
                        }
                        // Remaining of the UNDEFINED Exception ERRORS
                        else {
                            errorLog.setText("UNDEFINED ERROR --> " + error.getSourceException().toString());
//                        errorLog.setText("UNDEFINED ERROR --> " + error.getSourceException().toString());
                        }
                    }
                    // HttpDataSource$HttpDataSourceException
                    else if (error.getSourceException().toString().contains("HttpDataSource$HttpDataSourceException")) {
                        if (error.getSourceException().toString().contains("Unable to connect")) {
                            errorLog.setText("Http Data Source Exception --> Unable to Connect (No Network)");
                        } else if (error.getSourceException().toString().contains("Failed to handle key response")) {
                            errorLog.setText("Http Data Source Exception --> Failed to handle key (Most likely wrong mPass key)");
                        } else {
                            errorLog.setText("UNDEFINED Http Data Source Exception --> " + error.getSourceException().toString());
                        }
                    }
                    // Unexpected Loader Exception Clause
                    else if (error.getSourceException().toString().contains("Loader$UnexpectedLoaderException")) {
                        if (error.getSourceException().toString().contains("Empty key")) {
                            errorLog.setText("Unexpected Loader Exception --> Empty Key (Most likely wrong mPass key)");
                        } else {
                            errorLog.setText("UNDEFINED Loader Exception --> " + error.getSourceException().toString());
                        }
                    }
                    // HttpDataSource$InvalidResponseCodeException && DrmSession$DrmSessionException
                    else if (error.getSourceException().toString().contains("HttpDataSource$InvalidResponseCodeException") && error.getSourceException().toString().contains("DrmSession$DrmSessionException")) {
                        if (error.getSourceException().toString().contains("404")){
                            errorLog.setText("404 ERROR, DRM not found");
                        } else if (error.getSourceException().toString().contains("401")){
                            errorLog.setText("401 ERROR, DRM Unauthorized");
                        } else if (error.getSourceException().toString().contains("400")) {
                            errorLog.setText("400 ERROR, DRM Bad Request");
                        }  else if (error.getSourceException().toString().contains("403")){
                            errorLog.setText("403 ERROR, DRM Forbidden, server is refusing action");
                        } else if (error.getSourceException().toString().contains("402")){
                            errorLog.setText("402 ERROR, DRM Payment Required");
                        }
                        // Remaining of the UNDEFINED Exception ERRORS
                        else {
                            // DRM Session Exception Clauses
                            if (error.getSourceException().toString().contains("DrmSession$DrmSessionException")) {
                                if (error.getSourceException().toString().contains("Unable to connect")) {
                                    errorLog.setText("DRM Session Exception --> Unable to Connect");
                                } else if (error.getSourceException().toString().contains("Failed to handle key response")) {
                                    errorLog.setText("DRM Session Exception --> Failed to handle key (Most likely wrong mPass key)");
                                } else {
                                    errorLog.setText("UNDEFINED Drm Session Exception --> " + error.getSourceException().toString());
                                }
                            } else if (error.getSourceException().toString().contains("Loader$UnexpectedLoaderException")) {
                                if (error.getSourceException().toString().contains("Empty key")) {
                                    errorLog.setText("Unexpected Loader Exception --> Empty Key (Most likely wrong mPass key)");
                                } else {
                                    errorLog.setText("UNDEFINED Loader Exception --> " + error.getSourceException().toString());
                                }
                            }
                            else {
                                errorLog.setText("UNDEFINED ERROR --> " + error.getSourceException().toString());
                            }
                        }
                    }
                    // !HttpDataSource$InvalidResponseCodeException && DrmSession$DrmSessionException
                    else if (!error.getSourceException().toString().contains("HttpDataSource$InvalidResponseCodeException") && error.getSourceException().toString().contains("DrmSession$DrmSessionException")) {
                        if (error.getSourceException().toString().contains("404")){
                            errorLog.setText("404 ERROR, DRM not found");
                        } else if (error.getSourceException().toString().contains("401")){
                            errorLog.setText("401 ERROR, DRM Unauthorized");
                        } else if (error.getSourceException().toString().contains("400")) {
                            errorLog.setText("400 ERROR, DRM Bad Request");
                        }  else if (error.getSourceException().toString().contains("403")){
                            errorLog.setText("403 ERROR, DRM Forbidden, server is refusing action");
                        } else if (error.getSourceException().toString().contains("402")){
                            errorLog.setText("402 ERROR, DRM Payment Required");
                        }
                        // Remaining of the UNDEFINED Exception ERRORS
                        else {
                            // DRM Session Exception Clauses
                            if (error.getSourceException().toString().contains("DrmSession$DrmSessionException")) {
                                if (error.getSourceException().toString().contains("Unable to connect")) {
                                    errorLog.setText("DRM Session Exception --> Unable to Connect");
                                } else if (error.getSourceException().toString().contains("Failed to handle key response")) {
                                    errorLog.setText("DRM Session Exception --> Failed to handle key (Most likely wrong mPass key)");
                                } else {
                                    errorLog.setText("UNDEFINED Drm Session Exception --> " + error.getSourceException().toString());
                                }
                                // Http Data Source Exception Clause
                            }  else if (error.getSourceException().toString().contains("Loader$UnexpectedLoaderException")) {
                                if (error.getSourceException().toString().contains("Empty key")) {
                                    errorLog.setText("Unexpected Loader Exception --> Empty Key (Most likely wrong mPass key)");
                                } else {
                                    errorLog.setText("UNDEFINED Loader Exception --> " + error.getSourceException().toString());
                                }
                            }
                            else {
                                errorLog.setText("UNDEFINED ERROR --> " + error.getSourceException().toString());
                            }
                        }
                    }
                    else {
                        errorLog.setText("UNDEFINED ERROR --> " + error.getSourceException().toString());
                    }
                }
            }
        });
        simpleExoPlayer.play();
    }

    @Override
    public void onPause(){
        super.onPause();
        simpleExoPlayer.pause();
    }

    @Override
    public void onStop(){
        super.onStop();
        simpleExoPlayer.pause();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        simpleExoPlayer.release();
    }

    @Override
    protected void onResume(){
        super.onResume();
    }
}