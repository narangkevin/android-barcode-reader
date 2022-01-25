package com.example.cdnerror;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.media.browse.MediaBrowser;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
//import android.support.v4.media.MediaBrowserCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

//import com.google.android.exoplayer2.*;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;

import com.google.android.exoplayer2.drm.DefaultDrmSessionManager;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.drm.FrameworkMediaDrm;
import com.google.android.exoplayer2.drm.HttpMediaDrmCallback;
import com.google.android.exoplayer2.drm.MediaDrmCallback;
import com.google.android.exoplayer2.drm.UnsupportedDrmException;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.DefaultMediaSourceEventListener;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.DefaultHlsDataSourceFactory;
import com.google.android.exoplayer2.source.hls.HlsDataSourceFactory;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Util;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    PlayerView playerView;
    EditText cdnUrl;
    EditText licenseUrl;
    Button playBtn;
    TextView errorLog;

    private String useragent;
    private String drmuseragent;

    SimpleExoPlayer simpleExoPlayer;

    private FrameworkMediaDrm mediaDrm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        playerView = findViewById(R.id.video_view);
        cdnUrl = (EditText) findViewById(R.id.urlInput);
        licenseUrl = (EditText) findViewById(R.id.licenseInput);
        errorLog = (TextView) findViewById(R.id.error_logs);
        playBtn = (Button) findViewById(R.id.play_btn);

        useragent = Util.getUserAgent(getApplicationContext().getApplicationContext(), "ExoPlayer");
        drmuseragent = Util.getUserAgent(getApplicationContext().getApplicationContext(), "ExoPlayer1");

        if (cdnUrl.getText().toString() != "" && licenseUrl.getText().toString() != ""){
            playBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String cdnURL = cdnUrl.getText().toString();
                    String licenseURL = cdnUrl.getText().toString();
                    videoPlayer2(cdnURL, licenseURL);
                }
            });
        } else if (cdnUrl.getText().toString() != "" && licenseUrl.getText().toString() == ""){
            playBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String cdnURL = cdnUrl.getText().toString();
                    videoPlayer(cdnURL);
                }
            });
        }
    }

    private DrmSessionManager<FrameworkMediaCrypto> buildDrmSessionManager(String licenseUrl) throws UnsupportedDrmException {
        HttpDataSource.Factory httpDataSourceFactory = new DefaultHttpDataSourceFactory(useragent);
        MediaDrmCallback drmCallback = new HttpMediaDrmCallback(licenseUrl, httpDataSourceFactory);
        return  DefaultDrmSessionManager.newWidevineInstance(drmCallback,null);
    }

    private SimpleExoPlayer createPlayer(String url_license) throws UnsupportedDrmException {
        DrmSessionManager<FrameworkMediaCrypto> drmSessionManager = buildDrmSessionManager(url_license);
        return ExoPlayerFactory.newSimpleInstance(this, new DefaultRenderersFactory(this),new DefaultTrackSelector(), drmSessionManager);
    }

    public void videoPlayer2(String url, String licenseUrl){
        //Video URL
        Uri videoUrl = Uri.parse(url);
        try {
            simpleExoPlayer = createPlayer(licenseUrl);
        } catch(UnsupportedDrmException e) {
            Log.e("ERROR", "Create Player",e);
        }

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

        // Initialize Data Source Factory
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this, useragent);
        // Create a DASH media source pointing to a DASH manifest uri.
        DashMediaSource dashMediaSource = new DashMediaSource.Factory(new DefaultDashChunkSource.Factory(dataSourceFactory), dataSourceFactory).createMediaSource(videoUrl);
        // Create a player instance.

        //Set Player
        playerView.setPlayer(simpleExoPlayer);
        //Keep screen on
        playerView.setKeepScreenOn(true);
        // Prepare media
        simpleExoPlayer.prepare(dashMediaSource);
        // Play video when ready
        simpleExoPlayer.setPlayWhenReady(true);
        simpleExoPlayer.addListener(new Player.EventListener() {
            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                if (isPlaying) {
                    // Active Player
                    errorLog.setText("Active Player (No Error)");
                }
                else{
                    errorLog.setText("CDN has no content");
                    String log = simpleExoPlayer.getPlaybackError().toString();
                    System.out.print("Error Log ---> " + log);
                }
            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {
                if (error.type == ExoPlaybackException.TYPE_SOURCE) {
                    IOException cause = error.getSourceException();
                    if (cause instanceof HttpDataSource.HttpDataSourceException) {
                        // An HTTP error occurred.
                        errorLog.setText("HTTP Data-Source Error / CDN does not exists");
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
                        }
                    }
                }
            }
        });
    }

    public void videoPlayer(String url) {
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

        // DASH Media Source
        // Initialize Data Source Factory
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this, useragent);
        // Create a DASH media source pointing to a DASH manifest uri.
        DashMediaSource dashMediaSource = new DashMediaSource.Factory(new DefaultDashChunkSource.Factory(dataSourceFactory), dataSourceFactory).createMediaSource(videoUrl);
        // Create a player instance.


        //Set Player
        playerView.setPlayer(simpleExoPlayer);
        //Keep screen on
        playerView.setKeepScreenOn(true);
        // Prepare media
        simpleExoPlayer.prepare(dashMediaSource);
        // Play video when ready
        simpleExoPlayer.setPlayWhenReady(true);
        simpleExoPlayer.addListener(new Player.EventListener() {

            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                if (isPlaying) {
                    // Active Player
                    errorLog.setText("Active Player (No Error)");
                }
                else{
                    errorLog.setText("CDN has no content");
                    String log = simpleExoPlayer.getPlaybackError().toString();
                    System.out.print("Error Log ---> " + log);
                }
            }

            @Override
            public void onPlayerError(ExoPlaybackException error){
                if (error.type == ExoPlaybackException.TYPE_SOURCE) {
                    IOException cause = error.getSourceException();
                    if (cause instanceof HttpDataSource.HttpDataSourceException) {
                        // An HTTP error occurred.
                        errorLog.setText("HTTP Data-Source Error / CDN does not exists");
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
                        }
                    }
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
}