package com.example.stmappvod;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.media.browse.MediaBrowser;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.media.MediaBrowserCompat;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.android.volley.toolbox.NetworkImageView;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
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
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaLoadRequestData;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.IntroductoryOverlay;
import com.google.android.gms.cast.framework.SessionManagerListener;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.google.android.gms.common.images.WebImage;
import com.google.android.material.navigation.NavigationView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LiveStreamActivity extends AppCompatActivity {

    String myResponse;
    ListView listView;
    ArrayList<HashMap<String,String>> arrayList;
    PlayerView playerView;
    ProgressBar progressBar;
    private SeekBar mSeekbar;

    ImageView btFullScreen;
    SimpleExoPlayer simpleExoPlayer;
    boolean flag = false;
    ImageView btExoPlay;
    private View mControllers;

    private Timer mSeekbarTimer;
    private Timer mControllersTimer;

    private PlayerControlView castControlView;
    private CastContext castContext;

    private MenuItem mediaRouteMenuItem;
    private IntroductoryOverlay mIntroductoryOverlay;
    private PlaybackLocation mLocation;
    private PlaybackState mPlaybackState;

    private CastContext mCastContext;

    private MediaBrowser.MediaItem mSelectedMedia;
    private final Handler mHandler = new Handler();
    private boolean mControllersVisible;

    private SessionManagerListener<CastSession> mSessionManagerListener;

    public enum PlaybackLocation {
        LOCAL,
        REMOTE
    }

    private CastSession mCastSession;
    private SessionManagerListener<CastSession> mSessionManagerListener;

    // Cast Session Managements

    // Back to Normal...

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nav_activity_livestm);

        arrayList=new ArrayList<>();
        listView = (ListView)findViewById(R.id.listview);

        playerView = findViewById(R.id.player_view_live);
        progressBar = findViewById(R.id.progress_bar_live);

        btFullScreen = findViewById(R.id.bt_fullscreen);
        btExoPlay = findViewById(R.id.exo_play);

        castControlView = findViewById(R.id.cast_control_view_live);

        mCastContext = CastContext.getSharedInstance(this);

        // Cast Session Management
        private void setupCastListener() {
            mSessionManagerListener = new SessionManagerListener<CastSession>() {
                @Override
                public void onSessionStarting(CastSession castSession) {

                }

                @Override
                public void onSessionStarted(CastSession castSession, String s) {

                }

                @Override
                public void onSessionStartFailed(CastSession castSession, int i) {

                }

                @Override
                public void onSessionEnding(CastSession castSession) {

                }

                @Override
                public void onSessionEnded(CastSession castSession, int i) {

                }

                @Override
                public void onSessionResuming(CastSession castSession, String s) {

                }

                @Override
                public void onSessionResumed(CastSession castSession, boolean b) {

                }

                @Override
                public void onSessionResumeFailed(CastSession castSession, int i) {

                }

                @Override
                public void onSessionSuspended(CastSession castSession, int i) {

                }
            };
        }

        // End of Cast Session Management

        NavigationView navigationView = findViewById(R.id.nav_view_live);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.nav_live) {
//                    startActivity(new Intent(LiveStreamActivity.this, LiveStreamActivity.class));
                    Toast.makeText(LiveStreamActivity.this, "Live STM Activity", Toast.LENGTH_SHORT).show();
                } else if (item.getItemId() == R.id.nav_vod) {
                    startActivity(new Intent(LiveStreamActivity.this, MainActivity.class));
                    Toast.makeText(LiveStreamActivity.this, "S-VOD STM Activity", Toast.LENGTH_SHORT).show();
                } else if (item.getItemId() == R.id.nav_tvod){
                    Intent intent = new Intent(LiveStreamActivity.this, TVodActivity.class);
                    startActivity(intent);

                    Toast.makeText(LiveStreamActivity.this, "T-VOD Activity", Toast.LENGTH_SHORT).show();
                }
                DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        OkHttpClient client = new OkHttpClient();
        String url = "https://cms-fn-dmpapi.trueid.net/cms-fnshelf/v1/vdd78mEQYEv?fields=channel_code," +
                "thumb,channel_info,subscription_package,subscription_tiers,subscriptionoff_requirelogin," +
                "drm,is_premium,true_vision,ads_webapp,lang_dual,subtitle,catch_up,allow_catchup,time_shift,allow_timeshift,epg_flag";

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("authorization", "Bearer 5aaf9ade15afe0324400bacc26115aba3ac9493faf4f27ff957620c2")
                .addHeader("content-type", "application/json")
                .addHeader("cache-control", "no-cache")
                .addHeader("Postman-Token", "8d4f27f9-5913-4bdb-8fd0-1239c06d7c0e")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if(response.isSuccessful())
                {
                    myResponse = response.body().string();
                    LiveStreamActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            try {
                                JSONObject reader = new JSONObject(myResponse);
                                JSONArray shelfItem = reader.getJSONObject("data").getJSONArray("shelf_items");

                                System.out.println("Live Stream shelf_items  --> : " + shelfItem);
//                                System.out.println("Jojoe shelf_items --> json size is : " + shelfItem.length());

                                for(int i = 0;i<shelfItem.length();i++)
                                {
                                    JSONObject itemChannel = shelfItem.getJSONObject(i);
                                    String id = itemChannel.getString("id");
                                    String title = itemChannel.getString("title");
                                    String thumb = itemChannel.getString("thumb");

//                                    System.out.println("Jojoe --> "+ i +" id: "+id +" title : "+title + " thumb : " + thumb);

                                    HashMap<String,String> data = new HashMap<>();

                                    data.put("id",id);
                                    data.put("title",title);
                                    data.put("thumb",thumb);

                                    arrayList.add(data);

                                    final ListAdapter adapter = new SimpleAdapter(LiveStreamActivity.this,arrayList
                                            ,R.layout.listview_layout
                                            ,new String[]{"id","title","thumb"},new int[]{R.id.channelid,R.id.channeltitle,R.id.thumb});

                                    // OnItemCLick
                                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                        @Override
                                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                            HashMap<String, String> item = (HashMap<String, String>) adapterView.getItemAtPosition(i);
//                                            System.out.println("Jojoe --> Click " + item.get("id"));
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

    private void showIntroductoryOverlay() {
        if (mIntroductoryOverlay != null) {
            mIntroductoryOverlay.remove();
        }
        if ((mediaRouteMenuItem != null) && mediaRouteMenuItem.isVisible()) {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    mIntroductoryOverlay = new IntroductoryOverlay.Builder(
                            LiveStreamActivity.this, mediaRouteMenuItem)
                            .setTitleText("Introducing Cast")
                            .setSingleTime()
                            .setOnOverlayDismissedListener(
                                    new IntroductoryOverlay.OnOverlayDismissedListener() {
                                        @Override
                                        public void onOverlayDismissed() {
                                            mIntroductoryOverlay = null;
                                        }
                                    })
                            .build();
                    mIntroductoryOverlay.show();
                }
            });
        }
    }

    public void getStreamURL(String accessId) {

        OkHttpClient client = new OkHttpClient();
        String url = "http://35.244.252.52/pk-streamer/v2/streamer?id="
                +accessId+"&lang=&langid=th&fields=setting,allow_chrome_cast,subscriptionoff_requirelogin," +
                "subscription_package,subscription_tiers,channel_info,count_views," +
                "count_likes,ads,black_out,blackout_start_date," +
                "blackout_end_date,blackout_message,mix_no,is_premium,true_vision," +
                "teaser_channel,geo_block,time_shift,allow_timeshift,allow_catchup,packages,drm,slug," +
                "catchup,allow_catchup,time_shift,allow_timeshift,lang_dual," +
                "remove_ads&appid=trueid&visitor=mobile&os=ios&type=live&streamlvl=auto&ep_items=&uid=098123456&access=login&stime=&duration=";

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("authorization", "Bearer 5aaf9ade15afe0324400bacc26115aba3ac9493faf4f27ff957620c2")
                .addHeader("content-type", "application/json")
                .addHeader("cache-control", "no-cache")
                .addHeader("Postman-Token", "c07c5344-f5b5-4095-9ee2-65b6aceee9fb")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

                final String[] url = {""};

                if(response.isSuccessful())
                {
                    myResponse = response.body().string();
                    LiveStreamActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            try {
                                JSONObject reader = new JSONObject(myResponse);
                                JSONObject dataItem = reader.getJSONObject("data");
                                JSONObject streamItem = dataItem.getJSONObject("stream");

                                System.out.println("Live stream_url --> " + streamItem.get("stream_url"));
                                System.out.println("Live stream_license --> " + streamItem.get("stream_license"));

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
    /*... Class Media source under here */

    // Setting up Video Player
    public void videoPlayer(String url){
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
                LiveStreamActivity.this,trackSelector,loadControl
        );
        // Initialize Dada source factory
        DefaultHttpDataSourceFactory factory = new DefaultHttpDataSourceFactory(
                "exoplayer_video"
        );
        // Initialize extractors factory
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();

        // Data Source Factory
        DefaultHttpDataSourceFactory dataSourceFactory = new DefaultHttpDataSourceFactory("exoplayer");

        MediaSource mediaSource = new HlsMediaSource(videoUrl, dataSourceFactory, null, null);

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
}