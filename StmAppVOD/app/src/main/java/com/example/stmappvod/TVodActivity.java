package com.example.stmappvod;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
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
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.drm.DefaultDrmSessionManager;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.drm.HttpMediaDrmCallback;
import com.google.android.exoplayer2.drm.MediaDrmCallback;
import com.google.android.exoplayer2.drm.UnsupportedDrmException;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.dash.DashChunkSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.NoOpCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.google.android.exoplayer2.util.Util;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.material.navigation.NavigationView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class TVodActivity extends AppCompatActivity {

    String myResponse;
    ListView listView;
    ArrayList<HashMap<String,String>> arrayList;
    PlayerView playerView;
    ProgressBar progressBar;

    ImageView btFullScreen;
    SimpleExoPlayer simpleExoPlayer;
    boolean flag = false;
    ImageView btExoPlay;

    private byte[] offlineLicenseKeySetId;
    SharedPreferences sharedPreferences;
    public static final String storeKey = "storekey";
    public static final String storageId =  "storageid";

    private String userAgent;
    CoordinatorLayout coordinatorLayout;

    SimpleCache cache;
    DefaultHttpDataSourceFactory factory;

    private PlayerControlView castControlView;
    private MenuItem mediaRouteMenuItem;
    private LiveStreamActivity.PlaybackLocation mLocation;

    public enum PlaybackLocation {
        LOCAL,
        REMOTE
    }

    private String dashuseragent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nav_activity_tvod);

        sharedPreferences = getSharedPreferences(storeKey, Context.MODE_PRIVATE);
        userAgent = Util.getUserAgent(this,"Offlinelicensetesting");

        arrayList=new ArrayList<>();
        listView = (ListView)findViewById(R.id.listview_tvod);

        playerView = findViewById(R.id.player_view_tvod);
        progressBar = findViewById(R.id.progress_bar_tvod);

        btFullScreen = findViewById(R.id.bt_fullscreen);
        btExoPlay = findViewById(R.id.exo_play);

        castControlView = findViewById(R.id.cast_control_view_tvod);

        dashuseragent = Util.getUserAgent(getApplicationContext().getApplicationContext(), "Kevin User Agent");


        NavigationView navigationView = findViewById(R.id.nav_view_tvod);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.nav_tvod) {
                    Toast.makeText(TVodActivity.this, "T-VOD Activity", Toast.LENGTH_SHORT).show();
                } else if (item.getItemId() == R.id.nav_vod) {
                    startActivity(new Intent(TVodActivity.this, MainActivity.class));
                    Toast.makeText(TVodActivity.this, "VOD STM Activity", Toast.LENGTH_SHORT).show();
                } else if (item.getItemId() == R.id.nav_live){
                    Intent intent = new Intent(TVodActivity.this, LiveStreamActivity.class);
                    startActivity(intent);

                    Toast.makeText(TVodActivity.this, "Live STM Activity", Toast.LENGTH_SHORT).show();
                }
                DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
        File tempFolder = getPrivateStorageDir(this ,"Movie_01");
        Log.d("File",tempFolder.getAbsolutePath());
        cache = new SimpleCache(tempFolder, new NoOpCacheEvictor());
        factory = new DefaultHttpDataSourceFactory(userAgent,null);

        OkHttpClient client = new OkHttpClient();
        String url = "https://cms-fn-dmpapi.trueid.net/cms-fnshelf/v1/wBonymmYLXpR?fields=setting," +
                "thumb_list,detail,tvod_flag,is_trailer,trailer,thumb,synopsis,subscription_tiers," +
                "ep_items,genres,article_category,content_type&limit=100&lang=en";

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("authorization","Bearer 5aaf9ade15afe0324400bacc26115aba3ac9493faf4f27ff957620c2")
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
                    TVodActivity.this.runOnUiThread(new Runnable() {
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

                                    final ListAdapter adapter = new SimpleAdapter(TVodActivity.this,arrayList
                                            ,R.layout.listview_layout
                                            ,new String[]{"id","title","thumb"},new int[]{R.id.channelid,R.id.channeltitle,R.id.thumb});

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
        mediaRouteMenuItem = CastButtonFactory.setUpMediaRouteButton(getApplicationContext(), menu,
                R.id.media_route_menu_item);
        return true;
    }

    // Using ID to get VOD URL
    public void getStreamURL(String accessId){
        OkHttpClient client = new OkHttpClient();
        String url = "http://35.244.252.52/pk-streamer/v2/streamer?id="+ accessId +"&lang=&langid=th&fields=setting," +
                "allow_chrome_cast,subscriptionoff_requirelogin,subscription_package,subscription_tiers," +
                "channel_info,count_views,count_likes,ads,black_out,blackout_start_date,blackout_end_date," +
                "blackout_message,mix_no,is_premium,true_vision,teaser_channel,geo_block,time_shift," +
                "allow_timeshift,allow_catchup,packages,drm,slug,catchup,allow_catchup,time_shift," +
                "allow_timeshift,lang_dual,remove_ads&appid=trueid&visitor=mobile&os=android&type=live&streamlvl=auto&ep_items=&uid=098123456&access=login&stime=&duration=";

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("authorization","Bearer 5aaf9ade15afe0324400bacc26115aba3ac9493faf4f27ff957620c2")
                .addHeader("content-type","application/json")
//                .addHeader("Cookie","4c11e3180b43875784dcb36bca65ec3c=0b1b561a00cf79cb70ba5121b9994ff7")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

                final String[] url = {""};
                final String[] license = {""};
                if(response.isSuccessful()) {
                    myResponse = response.body().string();
                    TVodActivity.this.runOnUiThread(new Runnable() {
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
                                license[0] = streamItem.get("stream_license").toString();
                                videoPlayer(url[0], license[0]);
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
    protected void onPause() {
        super.onPause();
        simpleExoPlayer.setPlayWhenReady(false);
        simpleExoPlayer.getPlaybackState();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        simpleExoPlayer.setPlayWhenReady(true);
        simpleExoPlayer.getPlaybackState();
    }

    private byte[] loadKeySetId() {
        String encodedKeySetId = sharedPreferences.getString(storageId,null);
        if (encodedKeySetId == null) {
            return null;
        }
        return Base64.decode(encodedKeySetId,0);
    }

    private DrmSessionManager<FrameworkMediaCrypto> buildDrmSessionManager(String licenseUrl) throws UnsupportedDrmException {
        HttpDataSource.Factory httpDataSourceFactory = new DefaultHttpDataSourceFactory(dashuseragent);
        MediaDrmCallback drmCallback = new HttpMediaDrmCallback(licenseUrl, httpDataSourceFactory);
        return  DefaultDrmSessionManager.newWidevineInstance(drmCallback,null);
        //return DefaultDrmSessionManager.newWidevineInstance(drmCallback, null, null, null);
    }
    private DefaultDrmSessionManager<FrameworkMediaCrypto> drmContruction(String licenseUrl) throws UnsupportedDrmException{
        HttpDataSource.Factory httpDataSource = new DefaultHttpDataSourceFactory(userAgent);
        MediaDrmCallback mediaDrmCallback = new HttpMediaDrmCallback(licenseUrl,httpDataSource);
        return DefaultDrmSessionManager.newWidevineInstance(mediaDrmCallback,null);
    }

    private SimpleExoPlayer createPlayer(String url_license) throws UnsupportedDrmException {
        DrmSessionManager<FrameworkMediaCrypto> drmSessionManager = buildDrmSessionManager(url_license);
        //return ExoPlayerFactory.newSimpleInstance(this, new DefaultRenderersFactory(this), new DefaultTrackSelector(), drmSessionManager);
        //return ExoPlayerFactory.newSimpleInstance(new DefaultRenderersFactory(this, drmSessionManager), new DefaultTrackSelector(new AdaptiveTrackSelection.Factory(bandwidthMeter)));
        return ExoPlayerFactory.newSimpleInstance(new DefaultRenderersFactory(this),new DefaultTrackSelector(), drmSessionManager);
    }

        public void videoPlayer(String url, String license) {
//        //Make activity full screen
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);
//
//        //Video URL
//        Uri videoUrl = Uri.parse(url);
//
//        // Initialize Load control
//        LoadControl loadControl = new DefaultLoadControl();
//        // Initialize band width meter
//        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
//        // Initialize track selector
//        TrackSelector trackSelector = new DefaultTrackSelector(
//                new AdaptiveTrackSelection.Factory(bandwidthMeter)
//        );
//        // Initialize simple exo player
//        simpleExoPlayer = ExoPlayerFactory.newSimpleInstance(
//                TVodActivity.this,trackSelector,loadControl
//        );
//        // Initialize Dada source factory
//        DefaultHttpDataSourceFactory factory = new DefaultHttpDataSourceFactory("exoplayer_video");
//        // Initialize extractors factory
//        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
//
//        // Data Source Factory
//        DefaultHttpDataSourceFactory dataSourceFactory = new DefaultHttpDataSourceFactory("exoplayer");
//
//        // Initialize Media Source
//        Handler mainHandler = new Handler();
//        MediaSource mediaSource = new HlsMediaSource(videoUrl, dataSourceFactory, mainHandler, null);
//
//        //Set Player
//        playerView.setPlayer(simpleExoPlayer);
//        //Keep screen on
//        playerView.setKeepScreenOn(true);
//        // Prepare media
//        simpleExoPlayer.prepare(mediaSource);
//        // Play video when ready
//        simpleExoPlayer.setPlayWhenReady(true);
//        simpleExoPlayer.addListener(new Player.EventListener() {
//            @Override
//            public void onTimelineChanged(Timeline timeline, java.lang.Object manifest, int reason) {
//
//            }
//
//            @Override
//            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
//
//            }
//
//            @Override
//            public void onLoadingChanged(boolean isLoading) {
//
//            }
//
//            @Override
//            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
//                // Check condition
//                if (playbackState == Player.STATE_BUFFERING){
//                    progressBar.setVisibility(View.VISIBLE);
//                } else if (playbackState == Player.STATE_READY) {
//                    progressBar.setVisibility(View.GONE);
//                }
//            }
//
//            @Override
//            public void onRepeatModeChanged(int repeatMode) {
//
//            }
//
//            @Override
//            public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
//
//            }
//
//            @Override
//            public void onPlayerError(ExoPlaybackException error) {
//
//            }
//
//            @Override
//            public void onPositionDiscontinuity(int reason) {
//
//            }
//
//            @Override
//            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
//
//            }
//
//            @Override
//            public void onSeekProcessed() {
//
//            }
//        });
        try {
            simpleExoPlayer = createPlayer(license);
        } catch(UnsupportedDrmException e) {
            Log.e("ERROR", "Create Player",e);
        }

        playerView.setPlayer(simpleExoPlayer);
        Uri uri = Uri.parse(url);
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this, dashuseragent);
        DashChunkSource.Factory dashChunkSource = new DefaultDashChunkSource.Factory(dataSourceFactory);
        DashMediaSource mediaSource = new DashMediaSource.Factory(dashChunkSource,dataSourceFactory).createMediaSource(uri);
        simpleExoPlayer.prepare(mediaSource);
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

    private File getPrivateStorageDir(Context context, String folderName){
        File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_MOVIES), folderName);
        if (!file.mkdirs()) {
            Log.e("File","Directory is not created");
        }
        return file;
    }
}