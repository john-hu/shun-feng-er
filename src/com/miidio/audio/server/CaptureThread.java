package com.miidio.audio.server;

import javax.sound.sampled.TargetDataLine;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author hchu on 15/4/13.
 */
public class CaptureThread extends Thread {
    private static Logger logger = Logger.getLogger(CaptureThread.class.getName());

    private TargetDataLine line;
    private OutputStream out;
    private boolean stop;

    public CaptureThread(TargetDataLine line, OutputStream out) {
        this.line = line;
        this.out = out;
    }

    public void setOutputStream(OutputStream out) {
        synchronized (this) {
            this.out = out;
        }
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
        int read;
        try {
            while (!this.isStop()) {
                read = line.read(buffer, 0, buffer.length);
                if (read > 0) {
                    synchronized (CaptureThread.this) {
                        // Once the audio socket connected, we may set the out stream.
                        if (null != out) {
                            out.write(buffer, 0, read);
                            out.flush();
                            logger.log(Level.FINEST, "write " + read + " bytes to output");
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            line.close();
        }
    }
}
