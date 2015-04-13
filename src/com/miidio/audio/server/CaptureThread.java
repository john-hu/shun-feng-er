package com.miidio.audio.server;

import javax.sound.sampled.TargetDataLine;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by hchu on 15/4/13.
 */
public class CaptureThread extends Thread {
    private TargetDataLine line;
    private OutputStream out;
    private boolean stop;

    public CaptureThread(TargetDataLine line, OutputStream out) {
        this.line = line;
        this.out = out;
    }

    public synchronized boolean isStop() {
        return stop;
    }

    public synchronized void setStop(boolean stop) {
        this.stop = stop;
    }

    @Override
    public void run() {
        byte[] buffer = new byte[1024];
        this.setStop(false);
        int read = 0;
        try {
            while (!this.isStop()) {
                read = line.read(buffer, 0, buffer.length);
                if (read > 0) {
                    out.write(buffer, 0, read);
                }
            }
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
