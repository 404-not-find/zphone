
package com.tqcenglish.vlcdemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;

public class RemoteControlReceiver extends BroadcastReceiver {
    private static final String TAG = "RemoteControlReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {
            KeyEvent event = (KeyEvent)intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                    Log.d(TAG, "Detected play/pause event");
                    context.startService(new Intent(AudioPlayerService.ACTION_PLAY_PAUSE));
                    break;
                case KeyEvent.KEYCODE_MEDIA_STOP:
                    Log.d(TAG, "Detected stop event");
                    context.startService(new Intent(AudioPlayerService.ACTION_STOP));
                    break;
            }
        }
    }
}
