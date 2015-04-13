package com.miidio.audio.server;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class Main {
    public static void main(String[] args) throws Exception {
        //Get and display a list of
        // available mixers.
        Mixer.Info[] mixerInfo =
                AudioSystem.getMixerInfo();
        System.out.println("Available mixers:");
        for(int cnt = 0; cnt < mixerInfo.length;
            cnt++){
            System.out.println(mixerInfo[cnt].
                    getName());
        }//end for loop

        Mixer mixer = AudioSystem.getMixer(AudioSystem.getMixerInfo()[3]);

        AudioCapture capture = new AudioCapture();
        AudioFormat format = AudioCapture.getAudioFormat(AudioCapture.SampleRates.EIGHT_K,
                AudioCapture.Bits.EIGHT);
        Thread.sleep(5 * 1000);
        System.out.println("Capturing");
        FileOutputStream fos = new FileOutputStream(new File("out.tmp"));
        CaptureThread cThread = capture.capture(format, fos, mixer);
        Thread.sleep(20 * 1000);
        cThread.setStop(true);
        cThread.join();
        System.out.println("Waiting 15 secs for changing settings");
        Thread.sleep(15 * 1000);
        System.out.println("Playing");
        FileInputStream fis = new FileInputStream(new File("out.tmp"));
        PlayerThread t = capture.playAudio(format, fis, true);
        t.join();

    }
}
