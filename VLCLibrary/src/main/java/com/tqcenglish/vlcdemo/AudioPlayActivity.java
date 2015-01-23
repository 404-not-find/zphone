package com.tqcenglish.vlcdemo;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.fourmob.panningview.library.PanningView;

public class AudioPlayActivity extends SherlockActivity implements View.OnClickListener, ServiceConnection,
        SeekBar.OnSeekBarChangeListener, Runnable {
    public final static String TAG = AudioPlayActivity.class.getCanonicalName();
    private static final int JUMP_OFFSET = 3000;
    private Handler handler;
    private IAudioPlayerService service;
    private TextView currentTime;
    private TextView totalTime;
    private ImageButton previousButton;
    private ImageButton nextButton;

    private RelativeLayout playPauseButtonBackground;
    private ImageButton playPauseButton;
    private PanningView panningView;
    private SeekBar seekBar;
    private ProgressBar progressBar;
    String path = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        if (intent != null) {
            path = intent.getStringExtra("path");
        }
        if (null == path) {
            //path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/n.mp3";
            //path = "http://192.168.1.49:4242/download_monitor_file?type=recording&extension=804&file_name=20150105-120601-809-804-1420430761.210-15.wav";
            Toast.makeText(this, "path is null", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "path is null");
            finish();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_play);
        panningView = (PanningView) findViewById(R.id.panningView);
        playPauseButtonBackground = (RelativeLayout) findViewById(R.id.playPauseButtonBackground);
        playPauseButton = (ImageButton) findViewById(R.id.playPauseButton);
        playPauseButtonBackground.setOnClickListener(this);
        playPauseButton.setOnClickListener(this);
        seekBar = (SeekBar) findViewById(R.id.nowPlayingSeekBar);
        seekBar.setOnSeekBarChangeListener(this);
        currentTime = (TextView) findViewById(R.id.current_play_time);
        totalTime = (TextView) findViewById(R.id.total_play_time);
        previousButton = (ImageButton) findViewById(R.id.previousButton);
        previousButton.setOnClickListener(this);
        nextButton = (ImageButton) findViewById(R.id.nextButton);
        nextButton.setOnClickListener(this);
        //progressBar = (ProgressBar) findViewById(R.id.startingStreamProgressBar);
        this.handler = new Handler();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    /*
    @Override
    public void onStop() {
        super.onStop();
        mLibVLC.closeAout();
        mLibVLC.destroy();
        mLibVLC = null;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Log.d(TAG, "Setting item selected.");
                return true;
            case R.id.action_refresh:
                //mAdapter.refresh();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }*/


    public void play(View v) throws RemoteException {
        Log.d(TAG, "play()");
        this.service.play(path);
        this.handler.postDelayed(this, 500);
        panningView.startPanning();
        playPauseButton.setImageResource(R.drawable.pause_light);
    }

    public void pause(View v) throws RemoteException {
        Log.d(TAG, "pause()");
        this.service.pause();
        playPauseButton.setImageResource(R.drawable.play_light);
        panningView.stopPanning();
    }


    @Override
    public void onClick(View v) {
        try {
            this.wasPlayingBeforeSeeking = this.service.isPlaying();
        } catch (RemoteException e) {
            Log.wtf(TAG, "Failed to talk to the service", e);
            return;
        }
        int i = v.getId();
        if (i == R.id.nextButton) {
            seekBar.setProgress(seekBar.getProgress() + JUMP_OFFSET);
            onStopTrackingTouch(seekBar);

        } else if (i == R.id.previousButton) {
            seekBar.setProgress(seekBar.getProgress() - JUMP_OFFSET);
            onStopTrackingTouch(seekBar);

        } else if (i == R.id.playPauseButton) {
        } else if (i == R.id.playPauseButtonBackground) {
            try {
                if (
                        this.service.isPlaying()) {
                    pause(null);
                } else {
                    play(null);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "Binding to service...");
        if (super.bindService(new Intent(this, AudioPlayerService.class), this, BIND_AUTO_CREATE)) {
            Log.d(TAG, " done");
        } else {
            Log.e(TAG, " failed");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "Unbinding from service...");
        this.unbindService(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        try {
            this.service.stop();
        } catch (RemoteException e) {
            Log.wtf(TAG, "Failed", e);
        }
        this.handler.removeCallbacks(this);
        this.seekBar.setEnabled(false);
        this.seekBar.setProgress(0);
        this.panningView.stopPanning();
    }

    public void onServiceConnected(ComponentName name, IBinder service) {
        Log.d(TAG, "Connected to service " + name);
        this.service = IAudioPlayerService.Stub.asInterface(service);
        try {
            boolean playing = this.service.isPlaying();
            if (playing) {
                this.handler.post(this);
            }
        } catch (RemoteException e) {
            Log.wtf(TAG, "Failed", e);
        }
    }

    public void onServiceDisconnected(ComponentName name) {
        Log.d(TAG, "Disconnected from service " + name);
        this.service = null;
    }

    public void goToBeginning(View v) throws RemoteException {
        Log.d(TAG, "goToBeginning()");
        this.service.seek(0);
    }

    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        // ignored
    }

    private boolean wasPlayingBeforeSeeking = false;

    public void onStartTrackingTouch(SeekBar seekBar) {
        try {
            this.wasPlayingBeforeSeeking = this.service.isPlaying();
            if (this.wasPlayingBeforeSeeking) {
                this.service.pause();
            }
            this.handler.removeCallbacks(this);
        } catch (RemoteException e) {
            Log.wtf(TAG, "Failed to talk to the service", e);
        }
    }

    public void onStopTrackingTouch(SeekBar seekBar) {
        try {
            this.service.seek(seekBar.getProgress());
            if (this.wasPlayingBeforeSeeking) {
                this.service.play(path);
                panningView.startPanning();
                playPauseButton.setImageResource(R.drawable.pause_light);
            }
            boolean result = this.handler.postDelayed(this, 200);
            if (!result) {
                Log.d(TAG, "postDelayed result" + result);
                //try again
                this.handler.postDelayed(this, 200);
            }
        } catch (RemoteException e) {
            Log.wtf(TAG, "Failed to talk to the service", e);
        }
    }

    private static String formatAsTime(long milliseconds) {
        long seconds = milliseconds / 1000;
        return String.format("%02d:%02d", seconds / 60, seconds % 60);
    }

    public void run() {
        try {
            if (this.service == null || this.service.isStopped()) {
                //this.progressBar.setProgress(0);
                this.seekBar.setEnabled(false);
                this.currentTime.setText("00:00");
            } else {
                long position = this.service.position();

                this.totalTime.setText(formatAsTime(this.service.duration()));
                // update the seekbar
                this.seekBar.setMax((int) this.service.duration());
                if (0.00 != position) {
                    // update the status
                    this.currentTime.setText(formatAsTime(position));
                    this.seekBar.setProgress((int) position);
                }
                this.seekBar.setEnabled(true);
                for (int i = 1; i < 10; i++) {
                    if (this.service.isPlaying()) {
                        // schedule a callback of this method in 500 ms
                        this.handler.postDelayed(this, 500);
                        break;
                    } else if (Math.abs(service.position() - service.duration()) <= 800) {
                        playPauseButton.setImageResource(R.drawable.play_light);
                        panningView.stopPanning();
                        this.seekBar.setProgress((int)service.duration());
                        break;
                    }
                    SystemClock.sleep(500);
                }
            }
        } catch (RemoteException e) {
            Log.wtf(TAG, "Failed to talk to the service", e);
        }
    }
}
