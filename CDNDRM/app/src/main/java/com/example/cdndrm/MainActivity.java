package com.example.cdndrm;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
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
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.Util;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    PlayerView playerView;
    EditText cdnUrl;
    EditText licenseUrl;
    Button playBtn;
    Button stopBtn;
    TextView errorLog;

    private String useragent;

    SimpleExoPlayer simpleExoPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        playerView = findViewById(R.id.video_view);
        cdnUrl = (EditText) findViewById(R.id.urlInput);
        licenseUrl = (EditText) findViewById(R.id.licenseInput);
        playBtn = (Button) findViewById(R.id.play_btn);
        stopBtn = (Button) findViewById(R.id.stop_btn);
        errorLog = (TextView) findViewById(R.id.error_logs);

        useragent = Util.getUserAgent(getApplicationContext().getApplicationContext(), "ExoPlayer");

        if (cdnUrl.getText().toString() != "" && licenseUrl.getText().toString() != ""){
            playBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (simpleExoPlayer != null) {
                        simpleExoPlayer.release();
                    }
                    String cdnURL = cdnUrl.getText().toString();
                    String licenseURL = licenseUrl.getText().toString();
                    drmPlayer(cdnURL, licenseURL);
                }
            });
        } else if (cdnUrl.getText().toString() != "" && licenseUrl.getText().toString() == ""){
            playBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (simpleExoPlayer != null) {
                        errorLog.setText("...");
                        simpleExoPlayer.release();
                    }
                    String cdnURL = cdnUrl.getText().toString();
                    videoPlayer(cdnURL);
                }
            });
        }

        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                errorLog.setText("...");
                simpleExoPlayer.release();
            }
        });
    }

    public void videoPlayer(String url){
        //Video URL
        Uri videoUri = Uri.parse(url);

        MediaItem mediaItem = new MediaItem.Builder()
                .setUri(videoUri)
                .setMimeType(MimeTypes.APPLICATION_M3U8)
                .build();

        simpleExoPlayer = new SimpleExoPlayer.Builder(this).build();

        playerView.setPlayer(simpleExoPlayer);
        //Keep screen on
        playerView.setKeepScreenOn(true);

        simpleExoPlayer.setMediaItem(mediaItem);
        // Prepare the player.
        simpleExoPlayer.prepare();

        // Play video when ready
        simpleExoPlayer.setPlayWhenReady(true);

        simpleExoPlayer.addListener(new Player.EventListener() {
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
                            errorLog.setText("UNDEFINED/NO RESPONSE CDN CODE ERROR --> " + error.getSourceException().toString());
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
//                            errorLog.setText(httpError.getCause().toString());
                        }
                    }
                }
            }
        });
        simpleExoPlayer.play();
    }

    public void drmPlayer(String url, String licenseUrl){
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

        // Play video when ready
        simpleExoPlayer.setPlayWhenReady(true);

        simpleExoPlayer.addListener(new Player.EventListener() {
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
                            errorLog.setText("Http Data Source Exception --> Unable to Connect");
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
//                        errorLog.setText("UNDEFINED ERROR --> " + error.getSourceException().toString());
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