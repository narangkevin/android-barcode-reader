package com.example.stmappvod;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.ext.cast.CastPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.ResolvingDataSource;
import com.google.android.exoplayer2.util.Log;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.Util;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaQueueItem;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastState;
import com.google.android.gms.cast.framework.CastStateListener;
import com.google.android.gms.common.images.WebImage;
import com.google.android.material.navigation.NavigationView;
import com.squareup.picasso.Picasso;

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

public class MainActivity extends AppCompatActivity {

    String myResponse;
    ListView listView;
    ArrayList<HashMap<String,String>> arrayList;
    PlayerView playerView;
    ProgressBar progressBar;

    String encrypt;
    ImageView btFullScreen;
    SimpleExoPlayer simpleExoPlayer;
    boolean flag = false;
    ImageView btExoPlay;
    private Player currentPlayer;
    ImageView thumbnail;

    private PlayerControlView castControlView;
    private MenuItem mediaRouteMenuItem;
    private LiveStreamActivity.PlaybackLocation mLocation;
    private CastContext castContext;

    private CastPlayer castPlayer;

    public enum PlaybackLocation {
        LOCAL,
        REMOTE
    }

    ArrayList<Object> arrayList2;
    ListView lv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nav_activity_main);

        arrayList=new ArrayList<>();
        listView = (ListView)findViewById(R.id.listview);

        playerView = findViewById(R.id.player_view_vod);
        progressBar = findViewById(R.id.progress_bar_vod);

        btFullScreen = findViewById(R.id.bt_fullscreen);
        btExoPlay = findViewById(R.id.exo_play);

        thumbnail = (ImageView) findViewById(R.id.thumb);
        NavigationView navigationView = findViewById(R.id.nav_view_vod);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.nav_vod) {
//                    startActivity(new Intent(MainActivity.this, MainActivity.class));
                    Toast.makeText(MainActivity.this, "S-VOD STM Activity", Toast.LENGTH_SHORT).show();
                } else if (item.getItemId() == R.id.nav_live) {
                    Intent intent = new Intent(MainActivity.this, LiveStreamActivity.class);
                    startActivity(intent);

                    Toast.makeText(MainActivity.this, "Live STM Activity", Toast.LENGTH_SHORT).show();
                } else if (item.getItemId() == R.id.nav_tvod){
                    Intent intent = new Intent(MainActivity.this, TVodActivity.class);
                    startActivity(intent);

                    Toast.makeText(MainActivity.this, "T-VOD Activity", Toast.LENGTH_SHORT).show();
                }
                DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

//        videoPlayer("http://cdn094.stm.trueid.net/live1/c03_th_m_auto_tidapp.smil/playlist.m3u8?mpass=JsS4fOmP7Z7G4TR1BtORkuhBtFyharkzakVEOTqD5dA6OmcOIr5IlfIBpkYujWog09g&appid=streamtest&uid=icognito");


        OkHttpClient client = new OkHttpClient();
        String url = "https://cms-fn-dmpapi.trueid.net/cms-fnshelf/v1/wBonymmYLXpR?fields=setting,thumb_list,detail,tvod_flag,is_trailer,trailer,thumb,synopsis,subscription_tiers,ep_items,genres,article_category,content_type&limit=100&lang=en";

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("authorization","Bearer 5aaf9ade15afe0324400bacc4586ff961cbb4b3dbe1c23ed0f06fff1")
                .addHeader("content-type","application/json")
                .addHeader("Cookie","42a3baeb7914c9c6248a28cbfd399fb3=e816f0569b11335ab5fbd7ca9e91d3a")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if(response.isSuccessful()) {
                    myResponse = response.body().string();
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try{
                                JSONObject reader = new JSONObject(myResponse);
                                JSONArray shelfItem = reader.getJSONObject("data").getJSONArray("shelf_items");

                                // Printing Shelf Items
                                System.out.println("Kev's shelf_items  --> : " + shelfItem);

                                for(int i = 0;i<shelfItem.length();i++) {
                                    JSONObject itemChannel = shelfItem.getJSONObject(i);
                                    String id = itemChannel.getString("id");
                                    String title = itemChannel.getString("title");
                                    String thumb = itemChannel.getString("thumb");

                                    HashMap<String,String> data = new HashMap<>();
                                    data.put("id",id);
                                    data.put("title",title);
                                    data.put("thumb",thumb);

                                    arrayList.add(data);


                                    final ListAdapter adapter = new SimpleAdapter(MainActivity.this,arrayList
                                            ,R.layout.listview_layout
                                            ,new String[]{"id","title","thumb"},new int[]{R.id.channelid,R.id.channeltitle,R.id.thumb}
                                            );

                                    // OnItemCLick
                                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                        @Override
                                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                            HashMap<String, String> item = (HashMap<String, String>) adapterView.getItemAtPosition(i);
                                            System.out.println("Kev: Item Click --> " + item.get("id"));
                                            getStreamURL(item.get("id").toString());
                                        }
                                    });

                                    // OnItemCLick
                                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                        @Override
                                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                            HashMap<String, String> item = (HashMap<String, String>) adapterView.getItemAtPosition(i);
                                            System.out.println("Kev: Item Click --> " + item.get("id"));
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
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.browse, menu);
        return true;
    }

    // Using ID to get VOD URL
    public void getStreamURL(String accessId) {
        OkHttpClient client = new OkHttpClient();
        String url = "http://35.244.252.52/pk-streamer/v2/streamer?id="+ accessId +"&lang=&langid=th&fields=setting,allow_chrome_cast,subscriptionoff_requirelogin,subscription_package,subscription_tiers,channel_info,count_views,count_likes,ads,black_out,blackout_start_date,blackout_end_date,blackout_message,mix_no,is_premium,true_vision,teaser_channel,geo_block,time_shift,allow_timeshift,allow_catchup,packages,drm,slug,catchup,allow_catchup,time_shift,allow_timeshift,lang_dual,remove_ads&appid=trueid&visitor=mobile&os=ios&type=live&streamlvl=auto&ep_items=&uid=098123456&access=login&stime=&duration=";

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("authorization","Bearer 5aaf9ade15afe0324400bacc26115aba3ac9493faf4f27ff957620c2")
                .addHeader("content-type","application/json")
//                .addHeader("Cookie","4c11e3180b43875784dcb36bca65ec3c=0b1b561a00cf79cb70ba5121b9994ff7")
                .build();

        final MediaInfo[] mediaInfo = new MediaInfo[1];

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

                final String[] url = {""};
                if(response.isSuccessful()) {
                    myResponse = response.body().string();
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject reader = new JSONObject(myResponse);
                                JSONObject dataItem = reader.getJSONObject("data");
                                JSONObject streamItem = dataItem.getJSONObject("stream");

                                // Printing Stream URL and Stream License for VOD Streaming
                                System.out.println("VOD stream_url --> " + streamItem.get("stream_url"));
                                System.out.println("VOD stream_license --> " + streamItem.get("stream_license"));

                                url[0] = streamItem.get("stream_url").toString();
                                videoPlayer(url[0]);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        });
    }

    public void videoPlayer(String url) {
        //Make activity full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //Video URL
        Uri videoUrl = Uri.parse(url);

        // Initialize Load control
        LoadControl loadControl = new DefaultLoadControl();
        // Initialize band width meter
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        // Initialize track selector
        TrackSelector trackSelector = new DefaultTrackSelector(
                new AdaptiveTrackSelection.Factory(bandwidthMeter)
        );
        // Initialize simple exo player
        simpleExoPlayer = ExoPlayerFactory.newSimpleInstance(
                MainActivity.this,trackSelector,loadControl
        );

        // Initialize Dada source factory
        DefaultHttpDataSourceFactory factory = new DefaultHttpDataSourceFactory("exoplayer_video");

        // Initialize extractors factory
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();

        // Data Source Factory
        DefaultHttpDataSourceFactory dataSourceFactory = new DefaultHttpDataSourceFactory("exoplayer");


        // Call Custom Header
        String timestamp = Long.toString(System.currentTimeMillis() / 1000L);
        String deviceID = "deviceid99999";
        String messageStr = timestamp + "-" + deviceID;
        StmAppPlayerAuthen stmAppPlayerAuthen = new StmAppPlayerAuthen();
        try {
            String encrypted = stmAppPlayerAuthen.encrypt(messageStr);
            // Adding Custom Header into Data Source Factory
            dataSourceFactory.setDefaultRequestProperty("PlayerAuthen", encrypted);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Initialize Media Source
        Handler mainHandler = new Handler();
        MediaSource mediaSource = new HlsMediaSource(videoUrl, dataSourceFactory, mainHandler, null);

        //Set Player
        playerView.setPlayer(simpleExoPlayer);
            //Keep screen on
        playerView.setKeepScreenOn(true);
            // Prepare media
        simpleExoPlayer.prepare(mediaSource);
            // Play video when ready
        simpleExoPlayer.setPlayWhenReady(true);
        simpleExoPlayer.addListener(new Player.EventListener() {
                @Override
            public void onTimelineChanged(Timeline timeline, java.lang.Object manifest, int reason) {

            }

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

            }

            @Override
            public void onLoadingChanged(boolean isLoading) {

            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                // Check condition
                if (playbackState == Player.STATE_BUFFERING){
                    progressBar.setVisibility(View.VISIBLE);
                } else if (playbackState == Player.STATE_READY) {
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onRepeatModeChanged(int repeatMode) {

            }

            @Override
            public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {

            }

            @Override
            public void onPositionDiscontinuity(int reason) {

            }

            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

            }

            @Override
            public void onSeekProcessed() {

            }
        });

        btFullScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (flag) {
                    btFullScreen.setImageDrawable(getResources().getDrawable(R.drawable.ic_fullscreen));

                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

                    flag = false;
                } else {
                    btFullScreen.setImageDrawable(getResources().getDrawable(R.drawable.ic_fullscreen_exit));

                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

                    flag = true;
                }
            }
        });

        btExoPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                simpleExoPlayer.stop();
                simpleExoPlayer.setPlayWhenReady(true);
            }
        });
    }

    private String getCustomHeader() {
        // Call Custom Header Encryption
        String tempHeader = "";
        try {
            String timestamp = Long.toString(System.currentTimeMillis() / 1000L);
            String deviceID = "deviceid99999";
            String messageStr = timestamp + "-" + deviceID;
            StmAppPlayerAuthen stmAppPlayerAuthen = new StmAppPlayerAuthen();
            String encrypted = stmAppPlayerAuthen.encrypt(messageStr);
            tempHeader = encrypted;
            encrypt = encrypted;
            Log.i("Custom Header", encrypted);
            return encrypted;
        } catch (Exception e) {
            System.out.println(e);
            return tempHeader + "Error with the encrypted text";
        }
    }
}
