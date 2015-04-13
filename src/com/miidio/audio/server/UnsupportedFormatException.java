package com.miidio.audio.server;

import javax.sound.sampled.AudioFormat;

/**
 * Created by hchu on 15/4/13.
 */
public class UnsupportedFormatException extends Exception {
    private AudioFormat format;

    public UnsupportedFormatException(String message, AudioFormat format) {
        super(message);
        this.format = format;
    }

    public AudioFormat getFormat() {
        return format;
    }
}
