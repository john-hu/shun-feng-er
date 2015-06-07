package com.miidio.audio.server;


import javax.sound.sampled.*;
import java.io.InputStream;

public class AudioCapture {

    public AudioCapture() {
    }

    public static AudioFormat getAudioFormat(SampleRates sampleRate, Bits bits) {
        float rate;
        switch (sampleRate) {
            case EIGHT_K:
                rate = 8000f;
                break;
            case ELEVEN_K:
                rate = 11025f;
                break;
            case SIXTEEN_K:
                rate = 16000f;
                break;
            case TWENTY_TWO_K:
                rate = 22050f;
                break;
            case FORTY_FOUR_K:
                rate = 44100f;
                break;
            default:
                return null;
        }
        return new AudioFormat(
                rate, // sample rate
                bits.equals(Bits.EIGHT) ? 8 : 16, // bits per sample
                1, // channels
                true, // signed
                false); // bigEndian
    }

    public CaptureThread capture(AudioFormat format, Mixer mixer) throws UnsupportedFormatException,
            LineUnavailableException {
        DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, format);

        if (!AudioSystem.isLineSupported(dataLineInfo)) {
            throw new UnsupportedFormatException("format unsupported", format);
        }

        TargetDataLine target = null != mixer ? (TargetDataLine) mixer.getLine(dataLineInfo) :
                (TargetDataLine) AudioSystem.getLine(dataLineInfo);

        target.open(format);
        target.start();

        CaptureThread ret = new CaptureThread(target);
        ret.start();
        return ret;
    }

    public PlayerThread playAudio(AudioFormat format, InputStream in, boolean endWhileEmpty)
            throws LineUnavailableException {

        DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, format);
        SourceDataLine source = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
        source.open(format);
        source.start();

        PlayerThread ret = new PlayerThread(source, in, endWhileEmpty);
        ret.start();
        return ret;
    }

    public enum SampleRates {
        EIGHT_K, ELEVEN_K, SIXTEEN_K, TWENTY_TWO_K, FORTY_FOUR_K
    }

    public enum Bits {
        EIGHT, SIXTEEN
    }
}
