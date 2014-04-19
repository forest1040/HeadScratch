package com.headscratch;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayer.Provider;
import com.google.android.youtube.player.YouTubePlayerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Toast;

@SuppressLint("NewApi")
public class FullActivity extends YouTubeBaseActivity 
		implements YouTubePlayer.OnInitializedListener {

    //API key
    private static final String DEVELOPER_KEY = DeveloperKey.DEVELOPER_KEY;
    //Youtube のビデオID
    private static String videoId = "JW7y1gzWISA";
    private YouTubePlayerView youTubeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.youtube_view);

        youTubeView = (YouTubePlayerView) findViewById(R.id.youtube_view);
        //Youtubeビューの初期化
        youTubeView.initialize(DEVELOPER_KEY, this);
    }

    //初期化失敗
    @Override
    public void onInitializationFailure(Provider provider,
            YouTubeInitializationResult errorReason) {
        String errorMessage = String.format("ERR", errorReason.toString());
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
    }

    //初期化成功
    @Override
    public void onInitializationSuccess(Provider provider, YouTubePlayer player,
            boolean wasRestored) {
        if (!wasRestored) {
            player.loadVideo(videoId);
        }
    }
}
