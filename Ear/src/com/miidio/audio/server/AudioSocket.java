package com.miidio.audio.server;

import javax.swing.event.EventListenerList;
import java.awt.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.EventListener;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AudioSocket {

    private final static String CMD_KEYPRESS = "press";
    private final static String CMD_KEYRELEASE = "release";
    private static Logger logger = Logger.getLogger(AudioSocket.class.getName());
    private Thread serverThread;
    private ServerSocket serverSocket;
    private Socket activeSocket;
    private boolean running;
    private Robot robot;
    private EventListenerList listenerList = new EventListenerList();

    public AudioSocket(int port) throws IOException, AWTException {
        serverSocket = new ServerSocket(port);
        robot = new Robot();
        robot.setAutoDelay(0);
    }

    public void addEventListener(Listener l) {
        listenerList.add(Listener.class, l);
    }

    public void removeEventListener(Listener l) {
        listenerList.remove(Listener.class, l);
    }

    public boolean isRunning() {
        return running;
    }

    public synchronized void start() {
        running = true;
        serverThread = new Thread(new Runnable() {

            @Override
            public void run() {

                logger.log(Level.INFO, "AudioServer is running.");
                while (running) {
                    try {
                        logger.log(Level.INFO, "Waiting for connection...");
                        activeSocket = serverSocket.accept();
                        logger.log(Level.INFO, "Client connected...");
                        handleSocket();
                        fireDisconnectedEvent();
                    } catch (IOException e) {
                        logger.log(Level.WARNING, "error while accepting connection, running = " + running);
                        fireDisconnectedEvent();
                    } finally {
                        try {
                            closeActiveSocket();
                        } catch (IOException e) {
                            logger.log(Level.SEVERE, "error while closing sockets", e);
                        }
                        activeSocket = null;
                    }
                }
                logger.log(Level.INFO, "AudioServer is down.");
            }
        });
        serverThread.start();
    }

    private void fireConnectedEvent(InputStream is, OutputStream os) {
        Listener[] listeners = listenerList.getListeners(Listener.class);
        for (Listener l : listeners) {
            try {
                l.onConnected(is, os);
            } catch (Throwable t) {
                logger.log(Level.SEVERE, "error while firing connected event", t);
            }
        }
    }

    private void fireDisconnectedEvent() {
        Listener[] listeners = listenerList.getListeners(Listener.class);
        for (Listener l : listeners) {
            try {
                l.onDisconnected();
            } catch (Throwable t) {
                logger.log(Level.SEVERE, "error while firing disconnected event", t);
            }
        }
    }

    private synchronized void closeActiveSocket() throws IOException {
        if (null != activeSocket && !activeSocket.isClosed()) {
            logger.log(Level.INFO, "Active socket found, close it.");
            activeSocket.close();
        }
    }

    private synchronized void closeServerSocket() throws IOException {
        if (!serverSocket.isClosed()) {
            logger.log(Level.INFO, "Close server socket.");
            serverSocket.close();
        }
    }

    private void handleSocket() {
        logger.log(Level.INFO, "client: " + activeSocket.getInetAddress().toString());
        InputStream is;
        OutputStream os;
        try {
            is = activeSocket.getInputStream();
            os = activeSocket.getOutputStream();
            fireConnectedEvent(is, os);
            // We use the server thread to handle the remote input because we only
            // accept one connection at the same time.

            while (running) {
                if (activeSocket.isClosed() || !handleInputMessage(is)) {
                    // If the connection is closed, handleInputMessage returns false.
                    break;
                }
            }

            logger.log(Level.INFO, "client disconnected");
        } catch (IOException e) {
            logger.log(Level.WARNING, "reading input from socket error", e);
        } finally {
            try {
                // is will be closed while we close socket
                closeActiveSocket();
            } catch (IOException e) {
                logger.log(Level.WARNING, "error while closing input stream of socket", e);
            }
        }
    }

    private boolean handleInputMessage(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String data = reader.readLine();
        logger.log(Level.INFO, "data: " + data);
        if (null == data) {
            return false;
        } else if (!"".equals(data)) {
            handleCommand(data.split(","));
            return true;
        } else {
            return true;
        }
    }

    private void handleCommand(String[] cmds) {
        if (cmds.length < 2) {
            return;
        }
        if (CMD_KEYPRESS.equals(cmds[0])) {
            logger.log(Level.INFO, "key press " + cmds[1]);
            robot.keyPress(Integer.parseInt(cmds[1]));
        } else if (CMD_KEYRELEASE.equals(cmds[0])) {
            logger.log(Level.INFO, "key release " + cmds[1]);
            robot.keyRelease(Integer.parseInt(cmds[1]));
        }
    }

    public synchronized void stop() {
        running = false;
        logger.log(Level.INFO, "Try to stop audio server.");
        try {
            closeActiveSocket();
            closeServerSocket();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "error while closing sockets", e);
        }
    }

    public void join() throws InterruptedException {
        if (null != serverThread) {
            serverThread.join();
            serverThread = null;
        }
    }

    public interface Listener extends EventListener {
        void onConnected(InputStream is, OutputStream os);

        void onDisconnected();
    }
}
