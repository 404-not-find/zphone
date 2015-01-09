package com.tqcenglish.vlcdemo;

import java.lang.ref.WeakReference;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.util.Log;
import android.widget.Toast;


import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.LibVlcException;
import org.videolan.libvlc.MediaList;

public class IAudioPlayerServiceImpl extends IAudioPlayerService.Stub implements
        OnPreparedListener, OnErrorListener, OnAudioFocusChangeListener, OnCompletionListener {
    private static final String TAG = "IAudioPlayerServiceImpl";
    private final IntentFilter AUDIO_BECOMING_NOISY_INTENT_FILTER = new IntentFilter(
            AudioManager.ACTION_AUDIO_BECOMING_NOISY);
    private final WeakReference<AudioPlayerService> audioPlayerService;
    private final Context context;
    private LibVLC mLibVLC = null;
    private long rTime = 0;
    private AudioManager audioManager;
    private NoisyAudioReceiver noisyAudioReceiver;
    private ComponentName remoteControlReceiverName;
    private String mFilePath;
    private boolean muted = false;

    public IAudioPlayerServiceImpl(AudioPlayerService audioPlayerService) {
        this.context = audioPlayerService.getApplicationContext();
        this.audioPlayerService = new WeakReference<AudioPlayerService>(audioPlayerService);
        this.audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        this.noisyAudioReceiver = new NoisyAudioReceiver();
        this.remoteControlReceiverName = new ComponentName(context, RemoteControlReceiver.class);
    }

    @Override
    public synchronized void play(String filePath) {
        mFilePath = filePath;
        if (this.mLibVLC == null) {
            Log.d(TAG, "Initializing playback");
            // Initialize the LibVLC multimedia framework.
            // This is required before doing anything with LibVLC.
            try {
                mLibVLC = LibVLC.getInstance();
                mLibVLC.init(this.context);
                //this.mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                /* mLibVLC.getMediaList().add(new Media(mLibVLC, LibVLC.PathToURI(filePath)));
                mLibVLC.play();*/
                // LibVLC manages playback with media lists.
                // Let's get the primary default list that comes with it.
                MediaList list = mLibVLC.getMediaList();
                // Clear the list for demonstration purposes.
                list.clear();
                if (filePath.startsWith("http")) {
                    list.insert(0, filePath);
                } else {
                    list.insert(0, LibVLC.PathToURI(filePath));
                }
                onPrepared(null);
            } catch (LibVlcException e) {
                Toast.makeText(this.context,
                        "Error initializing the libVLC multimedia framework!",
                        Toast.LENGTH_LONG).show();
                return;
            }
        }
        mLibVLC.playIndex(0);
        if (!this.mLibVLC.isPlaying()) {
            Log.d(TAG, "Resuming playback.");
            if (rTime > 0) {
                mLibVLC.setTime(rTime);
                rTime = 0;
            }
        } else {
            Log.d(TAG, "Going back to full volume.");
            this.mLibVLC.setVolume(3);
        }

            /*try
            {
				AssetFileDescriptor afd = context.getResources().openRawResourceFd(R.raw.test_cbr);
				try
				{
					this.mediaPlayer.setDataSource(afd.getFileDescriptor());
					Log.d(TAG, "Successfully set the data source");
				}
				finally
				{
					afd.close();
				}
			}
			catch (Exception e)
			{
				Log.wtf(TAG, "Failed to initialize audio stream", e);
				this.stop();
			}
            try {
                this.mediaPlayer.setDataSource(mFilePath);
            } catch (IllegalArgumentException | SecurityException | IllegalStateException
                    | IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            this.mediaPlayer.setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK);
            this.mediaPlayer.setOnErrorListener(this);
            this.mediaPlayer.setOnPreparedListener(this);
            this.mediaPlayer.prepareAsync(); // calls onPrepared when finished
            Log.d(TAG, "Waiting for prepare to finish");
        } else if (!this.mediaPlayer.isPlaying()) {
            Log.d(TAG, "Resuming playback.");
            this.mediaPlayer.start();
        } else {
            Log.d(TAG, "Going back to full volume.");
            this.mediaPlayer.setVolume(1.0f, 1.0f);
        }
        */
    }

    public synchronized boolean pause() {
        if (this.mLibVLC != null && this.mLibVLC.isPlaying()) {
            Log.d(TAG, "Pausing playback.");
            rTime = mLibVLC.getTime();
            this.mLibVLC.pause();
            return true;
        } else {
            Log.d(TAG, "Not playing. Nothing to pause.");
            return false;
        }
    }

    public synchronized void stop() {
        if (this.mLibVLC == null) {
            Log.d(TAG, "No media player. Nothing to release");
        } else {
            if (this.mLibVLC.isPlaying()) {
                Log.d(TAG, "Stopping playback.");
                this.mLibVLC.stop();
            }
            Log.d(TAG, "Releasing audio player.");
            mLibVLC.closeAout();
            mLibVLC.destroy();
            this.mLibVLC = null;
            Log.d(TAG, "Abandoning audio focus.");
            this.audioManager.abandonAudioFocus(this);
            Log.d(TAG, "Unregistering noisy audio receiver.");
            context.unregisterReceiver(this.noisyAudioReceiver);
            Log.d(TAG, "Unregistering remote audio control receiver.");
            this.audioManager.unregisterMediaButtonEventReceiver(this.remoteControlReceiverName);
            Log.d(TAG, "Stopping service.");
            this.audioPlayerService.get().stopForeground(true);
            this.audioPlayerService.get().stopSelf();
        }
    }

    public synchronized long duration() {
        return this.isStopped() ? 0l : this.mLibVLC.getLength();  //  getDuration();
    }

    public synchronized long position() {
        //this.mLibVLC.getPosition() error
        return this.isStopped() ? 0l : this.mLibVLC.getTime();    //getCurrentPosition();
    }

    public synchronized int seek(int position) {
        if (this.isStopped()) {
            return 0;
        } else {
            rTime = position;
            //this.mLibVLC.setPosition(position);
            //return (int) this.mLibVLC.getPosition();
            return (int) position;
        }
    }

    public synchronized boolean isPlaying() {
        return this.mLibVLC != null && this.mLibVLC.isPlaying();
    }

    public synchronized boolean isPaused() {
        return this.mLibVLC != null && !this.mLibVLC.isPlaying();
    }

    public synchronized boolean isStopped() {
        return this.mLibVLC == null;
    }

    public synchronized void mute() {
        if (this.mLibVLC != null) {
            this.mLibVLC.setVolume(0);
            this.muted = true;
        }
    }

    public synchronized void unmute() {
        if (this.mLibVLC != null) {
            this.mLibVLC.setVolume(3);
            this.muted = false;
        }
    }

    public synchronized boolean isMuted() {
        return this.mLibVLC != null && this.muted;
    }

    public synchronized void onPrepared(MediaPlayer mp) {
        Log.d(TAG, "Media player is ready (prepared). Requesting audio focus.");
        if (this.audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            Log.d(TAG, "Starting as foreground service");
            this.context.startService(new Intent(this.context, AudioPlayerService.class));
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, new Intent(context,
                    AudioPlayActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

            Notification notification = new Notification(android.R.drawable.ic_media_play,
                    context.getText(R.string.foreground_service_notificaton_ticker_text),
                    System.currentTimeMillis());
            notification.setLatestEventInfo(context,
                    context.getText(R.string.foreground_service_notification_title),
                    context.getText(R.string.foreground_service_notification_message),
                    pendingIntent);
            notification.flags |= Notification.FLAG_ONGOING_EVENT;
            this.audioPlayerService.get().startForeground(1, notification);
            Log.d(TAG, "Starting playback");
            //this.mLibVLC.setOnCompletionListener(this);
            this.mLibVLC.play();
            Log.d(TAG, "Registering for noisy audio events");
            context.registerReceiver(this.noisyAudioReceiver, AUDIO_BECOMING_NOISY_INTENT_FILTER);
            Log.d(TAG, "Registering for audio remote control");
            this.audioManager.registerMediaButtonEventReceiver(this.remoteControlReceiverName);
            this.muted = false;
        } else {
            Log.w(TAG, "Failed to get audio focus");
            this.stop();
        }
    }

    public void onCompletion(MediaPlayer mp) {
        Log.d(TAG, "Completed playback");
        this.stop();
    }

    // Called when MediaPlayer has encountered a problem from an async operation
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.e(TAG,
                String.format("Music player encountered an error: what=%d, extra=%d", what, extra));
        this.stop();
        return true;
    }

    public synchronized void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                Log.d(TAG, "Re/gained focus.");
                this.play(mFilePath);
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                Log.d(TAG, "Lost focus for an unbounded amount of time.");
                this.stop();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                Log.d(TAG, "Lost focus for a short time.");
                this.pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                Log.d(TAG, "Lost focus for a short time. Can duck. Lowering volume");
                if (this.mLibVLC != null) {
                    this.mLibVLC.setVolume(3);
                }
                break;
            default:
                Log.w(TAG, "Unexpected onAudioFocusChange(" + focusChange + ")");
        }
    }

    private class NoisyAudioReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                Log.d(TAG, "Audio becoming noisy. Pausing.");
                IAudioPlayerServiceImpl.this.pause();
            }
        }
    }
}
