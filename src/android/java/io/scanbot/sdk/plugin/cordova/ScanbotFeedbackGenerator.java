/*
    Scanbot SDK Cordova Plugin

    Copyright (c) 2018 doo GmbH. All rights reserved.
 */
package io.scanbot.sdk.plugin.cordova;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Vibrator;


public class ScanbotFeedbackGenerator {

    private static final int TONE_DURATION = 200;
    private static final long VIBRATE_DURATION = 100L;

    private final ToneGenerator toneGenerator;
    private final Vibrator vibrator;

    public ScanbotFeedbackGenerator(final Activity activity,
                             final boolean playTone,
                             final boolean vibrate) {
        if (playTone) {
            this.toneGenerator = new ToneGenerator(AudioManager.STREAM_SYSTEM, ToneGenerator.MAX_VOLUME);
        }
        else {
            this.toneGenerator = null;
        }

        if (vibrate) {
            this.vibrator = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
        }
        else {
            this.vibrator = null;
        }
    }

    public void playBeepToneAndVibrate() {
        if (this.toneGenerator != null) {
            this.toneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, TONE_DURATION);
            //toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 250);
        }

        if (this.vibrator != null) {
            this.vibrator.vibrate(VIBRATE_DURATION);
        }
    }

    public synchronized void release() {
        if (this.toneGenerator != null) {
            this.toneGenerator.release();
        }
    }

}
