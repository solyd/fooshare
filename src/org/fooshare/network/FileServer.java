package org.fooshare.network;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

import org.fooshare.FooshareApplication;

import android.os.ResultReceiver;
import android.util.Log;

public class FileServer {
    private static final String TAG = "FileServer";

    public static enum UploadStatus {
        UPLOADING {
            @Override
            public String toString() {
                return "Uploading";
            }
        },
        FINISHED {
            @Override
            public String toString() {
                return "Finished";
            }
        },
        CANCELED {
            @Override
            public String toString() {
                return "Canceled";
            }
        },
        FAILED {
            @Override
            public String toString() {
                return "Failed";
            }
        }
    }

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
                        Upload up = new Upload(_servSocket.accept());
                        _fooshare.addUpload(up);
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

    public class Upload implements Runnable {
        private Socket                _clientSocket;
        private volatile UploadStatus _status;
        private volatile long         _progressInBytes = 0;
        private volatile long         _fileLen = 0;
        private File                  _requestedFile;

        private ResultReceiver _updateReceiver;
        private Object         _updateLock = new Object();

        private static final int BUFFER_SIZE = 4096; // bytes

        public Upload(Socket clientSocket) {
            _clientSocket = clientSocket;
            _status = UploadStatus.UPLOADING;
            Thread t = new Thread(this);
            t.setDaemon(true);
            t.start();
        }

        public String getFileName() {
            if (_requestedFile == null)
                return null;
            return _requestedFile.getName();
        }

        public void setUpdateReceiver(ResultReceiver updateReceiver) {
            synchronized (_updateLock) {
                _updateReceiver = updateReceiver;
            }
        }

        public int getPercentageProgress() {
            assert(_fileLen != 0);
            return (int) (_progressInBytes * 100.0 / _fileLen);
        }

        public UploadStatus status() {
            return _status;
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
                _requestedFile = new File(requestedFileName);
                BufferedInputStream fileIn = _fooshare.storage().getStream4Upload(requestedFileName);
                if (fileIn == null) {
                    Log.e(TAG, "Couldn't open InputStream to read file " + requestedFileName);
                    _clientSocket.close();
                    return;
                }
                Log.i(TAG, "Uploading file of size: " + _requestedFile.length());
                _fileLen = _requestedFile.length();

                int readBytes = 0;
                while ((readBytes = fileIn.read(buf)) > 0) {
                    out.write(buf, 0, readBytes);
                    _progressInBytes += readBytes;

                    // TODO determine when its best to update and remove magic numbers
                    if (_progressInBytes % 4096 == 0 || _fileLen < 32768) {

                        // publish progress to ui
                        synchronized (_updateLock) {
                            if (_updateReceiver != null) {
                                _updateReceiver.send(0, null);
                            }
                        }
                    }


                }
                out.flush();

                fileIn.close();
                _clientSocket.close();

                Log.i(TAG, String.format("Finished uploading %s, total: %d", requestedFileName, _progressInBytes));
            }
            catch (IOException e) {
                Log.i(TAG, Log.getStackTraceString(e));
            }
            finally {
                synchronized (_updateLock) {
                    if (_updateReceiver != null) {
                        _updateReceiver.send(0, null);
                    }
                }

                if (_progressInBytes == _fileLen)
                    _status = UploadStatus.FINISHED;
                else
                    _status = UploadStatus.FAILED;
            }
        }

    }
}
