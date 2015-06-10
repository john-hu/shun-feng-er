package com.miidio.audio.client;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * This is a dummy Input Device Receiver for removing the warning of manifest file.
 */
public class DummyInputDeviceReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Do nothing
    }
}
