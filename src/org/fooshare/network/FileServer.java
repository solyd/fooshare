package org.fooshare.network;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

import org.fooshare.FooshareApplication;

import android.util.Log;

public class FileServer {
    private static final String TAG = "FileServer";

    private FooshareApplication _fooshare;
    private int                 _port;
    private ServerSocket        _servSocket;
    private Thread              _servThread;
    private volatile boolean    _isRunning = false;

    public FileServer(FooshareApplication fooshare, int port) {
        Log.i(TAG, "Creating file server on port: " + port);
        _fooshare = fooshare;
        _port = port;
    }

    public void start() throws IOException {
        Log.i(TAG, "Starting file server");

        _isRunning = true;
        _servSocket = new ServerSocket(_port);
        _servThread = new Thread(new Runnable() {
            public void run() {
                try {
                    while (true) {
                        new FileTransferSession(_servSocket.accept());
                    }
                }
                catch (IOException e) {
                    Log.i(TAG, "File server shutting down now...");
                }
            }
        });
        _servThread.setDaemon(true);
        _servThread.start();
    }

    public synchronized void stop() {
        Log.i(TAG, "Stopping file server");

        if (!_isRunning)
            return;

        try {
            _servSocket.close();
            _servThread.join();
        }
        catch (IOException e) {
            Log.i(TAG, Log.getStackTraceString(e));
        }
        catch (InterruptedException e) {
            Log.i(TAG, Log.getStackTraceString(e));
        }
        finally {
            _isRunning = false;
        }
    }

    public synchronized boolean isRunning() {
        return _isRunning;
    }

    public synchronized int port() {
        return _port;
    }

    private class FileTransferSession implements Runnable {
        private Socket _clientSocket;

        private static final int BUFFER_SIZE = 4096; // bytes

        public FileTransferSession(Socket clientSocket) {
            _clientSocket = clientSocket;
            Thread t = new Thread(this);
            t.setDaemon(true);
            t.start();
        }

        public void run() {
            try {
                InputStream in = _clientSocket.getInputStream();

                byte[] buf = new byte[BUFFER_SIZE];
                Arrays.fill(buf, (byte) '\0');
                int requestLen = in.read(buf, 0, BUFFER_SIZE);
                if (requestLen <= 0) {
                    Log.i(TAG, "Didn't read file request, session thread exit");
                    return;
                }

                String requestedFileName = new String(buf, 0, requestLen);
                Log.i(TAG, "Requested file is: " + requestedFileName);

                BufferedOutputStream out = new BufferedOutputStream(_clientSocket.getOutputStream(), BUFFER_SIZE);
                File requestedFile = new File(requestedFileName);
                Log.i(TAG, "Uploading file of size: " + requestedFile.length());

                BufferedInputStream fileIn = _fooshare.storage().getStream4Upload(requestedFileName);
                if (fileIn == null) {
                    Log.e(TAG, "Couldn't open InputStream to read file " + requestedFileName);
                    _clientSocket.close();
                    return;
                }

                int totalUploadedBytes = 0;
                int readBytes = 0;
                while ((readBytes = fileIn.read(buf)) > 0) {
                    out.write(buf, 0, readBytes);
                    totalUploadedBytes += readBytes;
                }
                out.flush();

                fileIn.close();
                _clientSocket.close();

                Log.i(TAG, String.format("Finished uploading %s, total: %d", requestedFileName, totalUploadedBytes));
            }
            catch (IOException e) {
                Log.i(TAG, Log.getStackTraceString(e));
            }
        }

    }
}
