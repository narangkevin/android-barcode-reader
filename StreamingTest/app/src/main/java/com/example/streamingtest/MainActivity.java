package com.example.streamingtest;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.exoplayer2.ui.PlayerView;

public class MainActivity extends AppCompatActivity {

    private PlayerView playerView;
//    private ListView listView;
    public static TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        playerView = findViewById(R.id.player_view_live);
//        listView = (ListView) findViewById(R.id.list_view);
        textView = (TextView) findViewById(R.id.text);

        FetchData process = new FetchData();
        process.execute();
    }
}
