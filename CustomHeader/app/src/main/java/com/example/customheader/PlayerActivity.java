package com.example.customheader;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

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
import com.google.android.exoplayer2.source.ExtractorMediaSource;
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
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.upstream.ResolvingDataSource;
import com.google.android.exoplayer2.util.Util;

import java.io.File;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class PlayerActivity extends AppCompatActivity {

    private PlayerView playerView;
    private SimpleExoPlayer player;
    private boolean playWhenReady = true;
    private int currentWindow = 0;
    private long playbackPosition = 0;

    // TVOD Assets
    private String userAgent;
    private String dashuseragent;
    private byte[] offlineLicenseKeySetId;
    SharedPreferences sharedPreferences;
    public static final String storeKey = "storekey";
    public static final String storageId =  "storageid";

    HashMap<String,String> headerMap = new HashMap<String,String>();

    //Logs
    final private String TAG = "PlayerActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        playerView = findViewById(R.id.video_view);

        // TVOD Assets
        sharedPreferences = getSharedPreferences(storeKey, Context.MODE_PRIVATE);
        userAgent = Util.getUserAgent(this,"Offlinelicensetesting");

        final EditText editText = (EditText)findViewById(R.id.urlInput);
        final EditText licenseURL = (EditText)findViewById(R.id.licenseInput);

        // Click play AES Custom Header
        findViewById(R.id.playBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                videoPlayer(editText.getText().toString());
            }
        });

        // CLick Play Custom Header TVOD
        findViewById(R.id.customTVODBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        // Click Play Custom Header SVOD
        findViewById(R.id.customSVODBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        //Click Play Regular AES
        findViewById(R.id.playRegBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playRegular(editText.getText().toString());
            }
        });

        // Click play Regular TVOD
        findViewById(R.id.playTVOD).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playTVOD("https://cdn120.stm.trueid.net/live8/ht111_th_m_auto_tidapp.smil/manifest.mpd?appid=trueid&type=live&visitor=mobile&uid=000000001&did=MDAwMDAwMDAw",
                        "https://kd.stm.trueid.net/charybdis/drmdecrypt?appid=trueid&type=live&visitor=mobile&uid=000000001&did=MDAwMDAwMDAw&mpass=9WKhT4DrTtZACPMgYX4YZW86NAa2TJT9SDyHBvr94yk6On0kN67AvcAm4dI71PPU63k");
            }
        });

        // Click Play Regular SVOD
        findViewById(R.id.playSVOD).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        // Click Clear
        findViewById(R.id.clearBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editText.setText("");
                licenseURL.setText("");
            }
        });
    }

    private void playRegular(String url) {
//
    }


    // Play Regular TVOD
    private void playTVOD (String url, String license){

        try {
            player = createPlayer(license);
        } catch(UnsupportedDrmException e) {
            Log.e("ERROR", "Create Player",e);
        }

        playerView.setPlayer(player);
        Uri uri = Uri.parse(url);
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this, userAgent);
        DashChunkSource.Factory dashChunkSource = new DefaultDashChunkSource.Factory(dataSourceFactory);
        DashMediaSource mediaSource = new DashMediaSource.Factory(dashChunkSource,dataSourceFactory).createMediaSource(uri);
        player.prepare(mediaSource);
        player.setPlayWhenReady(true);
        player.addListener(new Player.EventListener() {
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
    }

    // Regular TVOD Assets
    private byte[] loadKeySetId() {
        String encodedKeySetId = sharedPreferences.getString(storageId,null);
        if (encodedKeySetId == null) {
            return null;
        }
        return Base64.decode(encodedKeySetId,0);
    }

    private DrmSessionManager<FrameworkMediaCrypto> buildDrmSessionManager(String licenseUrl) throws UnsupportedDrmException {
        HttpDataSource.Factory httpDataSourceFactory = new DefaultHttpDataSourceFactory(userAgent);
        MediaDrmCallback drmCallback = new HttpMediaDrmCallback(licenseUrl, httpDataSourceFactory);
        return  DefaultDrmSessionManager.newWidevineInstance(drmCallback,null);
    }
    private DefaultDrmSessionManager<FrameworkMediaCrypto> drmContruction(String licenseUrl) throws UnsupportedDrmException{
        HttpDataSource.Factory httpDataSource = new DefaultHttpDataSourceFactory(userAgent);
        MediaDrmCallback mediaDrmCallback = new HttpMediaDrmCallback(licenseUrl,httpDataSource);
        return DefaultDrmSessionManager.newWidevineInstance(mediaDrmCallback,null);
    }
    private SimpleExoPlayer createPlayer(String url_license) throws UnsupportedDrmException {
        DrmSessionManager<FrameworkMediaCrypto> drmSessionManager = buildDrmSessionManager(url_license);
        return ExoPlayerFactory.newSimpleInstance(this, new DefaultRenderersFactory(this), new DefaultTrackSelector(), drmSessionManager);
    }

    private File getPrivateStorageDir(Context context, String folderName){
        File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_MOVIES), folderName);
        if (!file.mkdirs()) {
            Log.e("File","Directory is not created");
        }
        return file;
    }

    public void videoPlayer(String url) {
//        if (player != null) {
//            releasePlayer();
//        }
//
//        Uri uri = Uri.parse(url);
//
//        // Build HLS Media Source with Factory inside of it
//        HlsMediaSource hlsMediaSource = new HlsMediaSource.Factory(
//                new ResolvingDataSource.Factory(
//                        new DefaultHttpDataSourceFactory(Util.getUserAgent(this, getString(R.string.app_name))),
//                        // Provide just-in-time request headers.
//                        (DataSpec dataSpec) ->
//                                dataSpec.withRequestHeaders(getCustomHeaders())
//                ))
//                .createMediaSource(uri);
//
//        // Prepare Player
//        player = new SimpleExoPlayer.Builder(this).build();
//        playerView.setPlayer(player);
//        player.setPlayWhenReady(playWhenReady);
//        player.prepare(hlsMediaSource);
    }

    @Override
    protected void onPause() {
        super.onPause();
        player.setPlayWhenReady(false);
        player.getPlaybackState();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        player.setPlayWhenReady(true);
        player.getPlaybackState();
    }

    public HashMap getCustomHeaders(){
        headerMap = new HashMap<String, String>();
        String playerAuthen = "Null";
        String deviceID = "deviceid999999";

        try {
            StmAppPlayerAuthen stmAppPlayerAuthen = new StmAppPlayerAuthen();
            playerAuthen = stmAppPlayerAuthen.encrypt(Long.toString(System.currentTimeMillis() / 1000L) + "|deviceid999999");
            System.out.println("Kevin Custom Header --> " + playerAuthen);
        } catch (Exception e){
            Log.d(TAG,"StmAppSecurity Error " + e);
            return headerMap;
        }

        headerMap.put("PlayerService", playerAuthen);
        return headerMap;
    }

    // Release Player
    private void releasePlayer() {
        if (player != null) {
            playWhenReady = player.getPlayWhenReady();
            playbackPosition = player.getCurrentPosition();
            currentWindow = player.getCurrentWindowIndex();
            player.release();
            player = null;
        }
    }
}