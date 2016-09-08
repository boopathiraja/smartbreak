package com.sdl.hellosdlandroid;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;

public class SdlBroadcastReceiver extends BroadcastReceiver {
    public SdlBroadcastReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case BluetoothDevice.ACTION_ACL_CONNECTED:
                Intent startIntent = new Intent(context, SdlService.class);
                context.startService(startIntent);
                break;
            case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                break;
            case AudioManager.ACTION_AUDIO_BECOMING_NOISY:
                //stop audio playback
                break;
        }
    }
}
