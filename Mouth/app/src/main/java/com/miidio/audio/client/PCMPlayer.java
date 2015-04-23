package com.miidio.audio.client;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

public class PCMPlayer {

    private static final String TAG = "PCMPlayer";
    public int sampleRate = 44100;
    public int outChannelConfiguration = AudioFormat.CHANNEL_OUT_MONO;
    public int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    private AudioTrack audioTrack;
    private int bufferSize;

    public int prepareDevice() {
        this.close();
        Log.d(TAG, "init " + sampleRate + ", " + outChannelConfiguration + ", " + audioEncoding);
        bufferSize = AudioTrack.getMinBufferSize(sampleRate, outChannelConfiguration,
                audioEncoding);

        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, outChannelConfiguration,
                audioEncoding, bufferSize, AudioTrack.MODE_STREAM);

        if (AudioTrack.STATE_UNINITIALIZED == audioTrack.getState()) {
            throw new RuntimeException("audio device cannot be created");
        }
        Log.d(TAG, "audio track ready");
        audioTrack.play();
        return bufferSize;
    }

    public void play(byte[] buffer, int size) {
        if (null != audioTrack) {
            audioTrack.write(buffer, 0, size);
        }
    }

    public void close() {
        if (null != audioTrack) {
            audioTrack.stop();
            audioTrack.release();
            audioTrack = null;
        }
    }
}
