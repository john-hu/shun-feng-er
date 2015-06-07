package com.miidio.audio.server;

import javax.sound.sampled.TargetDataLine;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author hchu on 15/4/13.
 */
public class CaptureThread extends Thread {
    private static Logger logger = Logger.getLogger(CaptureThread.class.getName());

    private TargetDataLine line;
    private ArrayList<OutputStream> outList = new ArrayList<OutputStream>();
    private boolean stop;

    public CaptureThread(TargetDataLine line) {
        this.line = line;
    }

    public void addOutputStream(OutputStream out) {
        synchronized (this) {
            this.outList.add(out);
        }
    }

    public void removeOutputStream(OutputStream out) {
        synchronized (this) {
            this.outList.remove(out);
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
                        if (outList.size() > 0) {
                            for (OutputStream out : outList) {
                                out.write(buffer, 0, read);
                                out.flush();
                            }
                            logger.log(Level.FINEST, "write " + read + " bytes to output list");
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
