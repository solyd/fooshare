package org.fooshare.network;

import java.io.IOException;
import java.util.Calendar;
import java.util.Random;

import org.fooshare.FooshareApplication;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Running the file server from a service so it will continue working in the background when
 * the application isn't in focus
 */
public class FileServerService extends Service {
    private static final String TAG = "FileServerService";

    private FooshareApplication _fooshare;
    private FileServer          _fileServer;
    private Random              _randomGenerator = new Random(Calendar.getInstance().getTime().getSeconds());

    @Override
    public void onCreate() {
        Log.i(TAG, "FileServerService created");

        _fooshare = (FooshareApplication) getApplication();
        _fooshare.registerFileServer(this);

        init();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "FileServerService destroyed");

        _fileServer.stop();
    }

    public boolean init() {
        if (_fileServer != null && _fileServer.isRunning())
            return false;

        while (true) {
            _fileServer = new FileServer(_fooshare, getRandomValidPort());
            try {
                _fileServer.start();
                break;
            }
            catch (IOException e) {
                Log.i(TAG, String.format("Can't start file server: \n%s", Log.getStackTraceString(e)));
            }
        }

        return true;
    }

    public int fileServerPort() {
        if (_fileServer == null)
            return 0;
        return _fileServer.port();
    }

    private int getRandomValidPort() {
        return 1025 + _randomGenerator.nextInt(20000);
    }
}
