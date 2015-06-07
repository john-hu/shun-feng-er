package com.miidio.audio.server;

import javax.swing.event.EventListenerList;
import java.awt.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AudioSocket {

    private final static String CMD_KEYPRESS = "press";
    private final static String CMD_KEYRELEASE = "release";
    private final static String PASSWORD_PREFIX = "HELO ";
    private static Logger logger = Logger.getLogger(AudioSocket.class.getName());
    private final Object handlerLocker = new Object();
    private Thread serverThread;
    private ServerSocket serverSocket;
    private boolean running;
    private Robot robot;
    private EventListenerList listenerList = new EventListenerList();
    private String password;
    private String listenerPassword;
    private boolean hasController;
    private ArrayList<AudioSocketHandler> socketHandlers = new ArrayList<AudioSocketHandler>();

    public AudioSocket(ServerSocket serverSocket, String password, String listenerPassword)
            throws IOException, AWTException {

        this.serverSocket = serverSocket;
        this.password = password;
        this.listenerPassword = listenerPassword;
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

                logger.log(Level.INFO, "AudioServer is running with port " + serverSocket.getLocalPort() + ".");
                logger.log(Level.INFO, "AudioServer has password: " + (AudioSocket.this.password != null) + ".");
                while (running) {
                    try {
                        logger.log(Level.INFO, "Waiting for connection...");
                        handleSocket(serverSocket.accept());
                        logger.log(Level.INFO, "Client connected...");
                    } catch (IOException e) {
                        logger.log(Level.WARNING, "error while accepting connection, running = " + running);
                    }
                }
                closeAllSocketThreads();
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

    private void fireDisconnectedEvent(InputStream is, OutputStream os) {
        Listener[] listeners = listenerList.getListeners(Listener.class);
        for (Listener l : listeners) {
            try {
                l.onDisconnected(is, os);
            } catch (Throwable t) {
                logger.log(Level.SEVERE, "error while firing disconnected event", t);
            }
        }
    }

    private synchronized void closeServerSocket() throws IOException {
        if (!serverSocket.isClosed()) {
            logger.log(Level.INFO, "Close server socket.");
            serverSocket.close();
        }
    }

    private void handleSocket(Socket socket) {
        AudioSocketHandler handler = new AudioSocketHandler(socket);
        socketHandlers.add(handler);
        new Thread(handler).start();
    }

    private void closeAllSocketThreads() {
        for (AudioSocketHandler handler : socketHandlers) {
            try {
                handler.closeActiveSocket();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void stop() {
        running = false;
        logger.log(Level.INFO, "Try to stop audio server.");
        try {
            closeAllSocketThreads();
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

        void onDisconnected(InputStream is, OutputStream os);
    }

    /**
     * AudioSocketHandler handles remote socket's input.
     */
    private class AudioSocketHandler implements Runnable {
        private boolean connectionTrusted;
        private Socket activeSocket;
        private boolean controller;
        private InputStream is;
        private OutputStream os;

        public AudioSocketHandler(Socket socket) {
            this.activeSocket = socket;
        }

        /**
         * process the input and output. This thread is terminated when 1. wrong password, 2. remote
         * disconnected, and 3. other threas tries to close it.
         */
        @Override
        public void run() {
            logger.log(Level.INFO, "client: " + activeSocket.getInetAddress().toString());

            try {
                is = activeSocket.getInputStream();
                os = activeSocket.getOutputStream();
                this.connectionTrusted = false;
                AudioSocket.this.fireConnectedEvent(is, os);
                // We use the server thread to handle the remote input because we only
                // accept one connection at the same time.

                while (AudioSocket.this.running) {
                    if (activeSocket.isClosed() || !handleInputMessage(is)) {
                        // If the connection is closed, handleInputMessage returns false.
                        break;
                    }
                }

                logger.log(Level.INFO, "client disconnected or wrong password");
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

        /**
         * check password with the data information
         *
         * @param data the received data which should starting with PASSWORD_PREFIX
         */
        private void handlePassword(String data) {
            synchronized (handlerLocker) {
                // If we don't have controller, we should check the password as controller.
                if (!AudioSocket.this.hasController) {
                    if (null == AudioSocket.this.password) {
                        AudioSocket.this.hasController = true;
                        // always accept connection if no password found.
                        this.connectionTrusted = true;
                        this.controller = true;
                        AudioSocket.this.hasController = true;
                        logger.log(Level.INFO, "no password found => accepting socket as controller");
                        // controller found, we don't need to do anything more
                        return;
                    } else if ((PASSWORD_PREFIX + AudioSocket.this.password).equals(data)) {
                        this.connectionTrusted = true;
                        AudioSocket.this.hasController = true;
                        this.controller = true;
                        logger.log(Level.INFO, "password correct => accepting socket as controller");
                        // controller found, we don't need to do anything more
                        return;
                    }
                }
            }
            // check listener password
            this.connectionTrusted = (null != AudioSocket.this.listenerPassword) &&
                    (PASSWORD_PREFIX + AudioSocket.this.listenerPassword).equals(data);
            logger.log(Level.INFO, "challenge password as listener: " + this.connectionTrusted);
        }

        /**
         * Reads string from is
         *
         * @param is the input stream object which should not be null.
         * @return true if the connection should keep.
         * @throws IOException
         */
        private boolean handleInputMessage(InputStream is) throws IOException {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String data = reader.readLine();
            if (null == data) {
                return false;
            } else if (!"".equals(data)) {
                if (data.startsWith(PASSWORD_PREFIX)) {
                    handlePassword(data);
                    return this.connectionTrusted;
                } else if (this.controller) {
                    logger.log(Level.INFO, "data: " + data);
                    handleCommand(data.split(","));
                    return true;
                } else {
                    return this.connectionTrusted;
                }

            } else {
                return true;
            }
        }

        /**
         * executes and sends commands
         *
         * @param cmds commands for execution
         */
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

        /**
         * close the socket and try to terminate everything.
         *
         * @throws IOException
         */
        private void closeActiveSocket() throws IOException {
            // We should always check activeSocket because this method may be called by
            // other threads and this thread. And it may be called multple times when we
            // try to close this socket.
            if (null != activeSocket && !activeSocket.isClosed()) {
                if (this.controller) {
                    synchronized (handlerLocker) {
                        AudioSocket.this.hasController = false;
                    }
                }
                AudioSocket.this.fireDisconnectedEvent(is, os);
                logger.log(Level.INFO, "Close active socket, is controller:" + this.controller);
                activeSocket.close();
            }
        }
    }
}
